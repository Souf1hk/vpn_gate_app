package com.vpngate.android.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.vpngate.android.R;
import com.vpngate.android.model.ConnectionStats;
import com.vpngate.android.model.VpnConfig;
import com.vpngate.android.model.VpnServer;
import com.vpngate.android.ui.MainActivity;
import com.vpngate.android.util.OpenVpnManager;
import com.vpngate.android.util.PreferenceManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Main VPN service that handles VPN connections using OpenVPN
 */
public class VpnGateService extends VpnService {
    
    public static final String ACTION_CONNECT = "com.vpngate.android.CONNECT";
    public static final String ACTION_DISCONNECT = "com.vpngate.android.DISCONNECT";
    public static final String ACTION_STATUS_CHANGED = "com.vpngate.android.STATUS_CHANGED";
    public static final String ACTION_STATS_UPDATED = "com.vpngate.android.STATS_UPDATED";
    
    public static final String EXTRA_SERVER = "extra_server";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_STATS = "extra_stats";
    
    public static final String STATUS_DISCONNECTED = "DISCONNECTED";
    public static final String STATUS_CONNECTING = "CONNECTING";
    public static final String STATUS_CONNECTED = "CONNECTED";
    public static final String STATUS_RECONNECTING = "RECONNECTING";
    public static final String STATUS_DISCONNECTING = "DISCONNECTING";
    public static final String STATUS_ERROR = "ERROR";
    
    private static final String NOTIFICATION_CHANNEL_ID = "VPN_GATE_CHANNEL";
    private static final int NOTIFICATION_ID = 1001;
    
    private final IBinder binder = new VpnGateServiceBinder();
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicReference<String> currentStatus = new AtomicReference<>(STATUS_DISCONNECTED);
    private final AtomicReference<VpnServer> currentServer = new AtomicReference<>();
    private final ConnectionStats connectionStats = new ConnectionStats();
    
    private NotificationManager notificationManager;
    private PreferenceManager preferenceManager;
    private OpenVpnManager openVpnManager;
    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private VpnConfig vpnConfig;
    
    public class VpnGateServiceBinder extends Binder {
        public VpnGateService getService() {
            return VpnGateService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Timber.d("VpnGateService created");
        
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        preferenceManager = new PreferenceManager(this);
        openVpnManager = new OpenVpnManager(this);
        vpnConfig = preferenceManager.getVpnConfig();
        
        createNotificationChannel();
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification(STATUS_DISCONNECTED));
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_CONNECT.equals(action)) {
                VpnServer server = intent.getParcelableExtra(EXTRA_SERVER);
                if (server != null) {
                    connectToServer(server);
                }
            } else if (ACTION_DISCONNECT.equals(action)) {
                disconnect();
            }
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        Timber.d("VpnGateService destroyed");
        
        disconnect();
        stopForeground(true);
    }
    
    /**
     * Connect to a VPN server
     */
    public void connectToServer(VpnServer server) {
        if (isConnecting.get() || isConnected.get()) {
            Timber.w("Already connecting or connected");
            return;
        }
        
        Timber.i("Connecting to server: %s", server.getHostname());
        
        currentServer.set(server);
        isConnecting.set(true);
        updateStatus(STATUS_CONNECTING);
        
        // Start connection in background thread
        new Thread(() -> {
            try {
                // Check if VPN permission is granted
                Intent prepareIntent = VpnService.prepare(this);
                if (prepareIntent != null) {
                    Timber.e("VPN permission not granted");
                    updateStatus(STATUS_ERROR);
                    return;
                }
                
                // Test server connectivity
                if (!testServerConnectivity(server)) {
                    Timber.e("Server connectivity test failed");
                    updateStatus(STATUS_ERROR);
                    return;
                }
                
                // Establish VPN connection
                if (establishVpnConnection(server)) {
                    isConnected.set(true);
                    isConnecting.set(false);
                    updateStatus(STATUS_CONNECTED);
                    
                    // Start connection monitoring
                    startConnectionMonitoring();
                    
                    Timber.i("Successfully connected to %s", server.getHostname());
                } else {
                    isConnecting.set(false);
                    updateStatus(STATUS_ERROR);
                    Timber.e("Failed to establish VPN connection");
                }
                
            } catch (Exception e) {
                Timber.e(e, "Error connecting to VPN server");
                isConnecting.set(false);
                updateStatus(STATUS_ERROR);
            }
        }).start();
    }
    
    /**
     * Disconnect from VPN
     */
    public void disconnect() {
        if (!isConnected.get() && !isConnecting.get()) {
            return;
        }
        
        Timber.i("Disconnecting from VPN");
        
        updateStatus(STATUS_DISCONNECTING);
        
        try {
            // Stop OpenVPN
            if (openVpnManager != null) {
                openVpnManager.stopVpn();
            }
            
            // Close VPN interface
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
            
            // Stop monitoring thread
            if (vpnThread != null) {
                vpnThread.interrupt();
                vpnThread = null;
            }
            
            isConnected.set(false);
            isConnecting.set(false);
            currentServer.set(null);
            connectionStats.reset();
            
            updateStatus(STATUS_DISCONNECTED);
            
            Timber.i("VPN disconnected successfully");
            
        } catch (Exception e) {
            Timber.e(e, "Error disconnecting VPN");
        }
    }
    
    /**
     * Test server connectivity before connecting
     */
    private boolean testServerConnectivity(VpnServer server) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(server.getIpAddress(), 1194), 5000);
            socket.close();
            return true;
        } catch (Exception e) {
            Timber.w(e, "Server connectivity test failed for %s", server.getIpAddress());
            return false;
        }
    }
    
    /**
     * Establish VPN connection using OpenVPN
     */
    private boolean establishVpnConnection(VpnServer server) {
        try {
            // Configure VPN interface
            Builder builder = new Builder();
            builder.setMtu(1500);
            builder.addAddress("10.8.0.2", 24);
            builder.addRoute("0.0.0.0", 0);
            
            // Configure DNS
            if (vpnConfig.isDnsProtection()) {
                builder.addDnsServer(vpnConfig.getCustomDns1());
                builder.addDnsServer(vpnConfig.getCustomDns2());
            }
            
            // Set session name
            builder.setSession(getString(R.string.app_name));
            
            // Establish the interface
            vpnInterface = builder.establish();
            
            if (vpnInterface == null) {
                Timber.e("Failed to establish VPN interface");
                return false;
            }
            
            // Start OpenVPN with the server configuration
            return openVpnManager.startVpn(server, vpnInterface);
            
        } catch (Exception e) {
            Timber.e(e, "Error establishing VPN connection");
            return false;
        }
    }
    
    /**
     * Start monitoring connection status and statistics
     */
    private void startConnectionMonitoring() {
        vpnThread = new Thread(() -> {
            while (isConnected.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Update connection statistics
                    updateConnectionStats();
                    
                    // Broadcast stats update
                    broadcastStatsUpdate();
                    
                    Thread.sleep(1000); // Update every second
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Timber.e(e, "Error in connection monitoring");
                }
            }
        });
        vpnThread.start();
    }
    
    /**
     * Update connection statistics
     */
    private void updateConnectionStats() {
        // This would typically read from the VPN interface or OpenVPN logs
        // For now, we'll simulate some basic stats
        if (openVpnManager != null) {
            ConnectionStats stats = openVpnManager.getConnectionStats();
            if (stats != null) {
                connectionStats.setBytesIn(stats.getBytesIn());
                connectionStats.setBytesOut(stats.getBytesOut());
                connectionStats.setPacketsIn(stats.getPacketsIn());
                connectionStats.setPacketsOut(stats.getPacketsOut());
                connectionStats.setPing(stats.getPing());
                connectionStats.setDownloadSpeed(stats.getDownloadSpeed());
                connectionStats.setUploadSpeed(stats.getUploadSpeed());
            }
        }
    }
    
    /**
     * Update connection status and notify listeners
     */
    private void updateStatus(String status) {
        currentStatus.set(status);
        connectionStats.setConnectionStatus(status);
        
        // Update notification
        updateNotification(status);
        
        // Broadcast status change
        Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra(EXTRA_STATUS, status);
        if (currentServer.get() != null) {
            intent.putExtra(EXTRA_SERVER, currentServer.get());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
        Timber.d("Status updated: %s", status);
    }
    
    /**
     * Broadcast statistics update
     */
    private void broadcastStatsUpdate() {
        Intent intent = new Intent(ACTION_STATS_UPDATED);
        intent.putExtra(EXTRA_STATS, connectionStats);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Gate Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("VPN Gate connection status");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Create notification for the service
     */
    private Notification createNotification(String status) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String title = getString(R.string.app_name);
        String content = getStatusDescription(status);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_vpn_key)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW);
        
        // Add action buttons
        if (STATUS_CONNECTED.equals(status)) {
            Intent disconnectIntent = new Intent(this, VpnGateService.class);
            disconnectIntent.setAction(ACTION_DISCONNECT);
            PendingIntent disconnectPendingIntent = PendingIntent.getService(
                this, 0, disconnectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(R.drawable.ic_power_off, "Disconnect", disconnectPendingIntent);
        }
        
        return builder.build();
    }
    
    /**
     * Update the notification
     */
    private void updateNotification(String status) {
        if (vpnConfig.isShowNotification()) {
            Notification notification = createNotification(status);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
    
    /**
     * Get human-readable status description
     */
    private String getStatusDescription(String status) {
        VpnServer server = currentServer.get();
        String serverName = server != null ? server.getCountryLong() : "Unknown";
        
        switch (status) {
            case STATUS_CONNECTING:
                return "Connecting to " + serverName + "...";
            case STATUS_CONNECTED:
                return "Connected to " + serverName;
            case STATUS_RECONNECTING:
                return "Reconnecting to " + serverName + "...";
            case STATUS_DISCONNECTING:
                return "Disconnecting...";
            case STATUS_ERROR:
                return "Connection failed";
            default:
                return "Disconnected";
        }
    }
    
    // Public getters for service state
    public boolean isConnected() {
        return isConnected.get();
    }
    
    public boolean isConnecting() {
        return isConnecting.get();
    }
    
    public String getCurrentStatus() {
        return currentStatus.get();
    }
    
    public VpnServer getCurrentServer() {
        return currentServer.get();
    }
    
    public ConnectionStats getConnectionStats() {
        return connectionStats;
    }
}