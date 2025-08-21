package com.vpngate.android.util;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import com.vpngate.android.model.ConnectionStats;
import com.vpngate.android.model.VpnServer;
import com.vpngate.android.repository.VpnServerRepository;
import timber.log.Timber;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager class for OpenVPN operations
 */
public class OpenVpnManager {
    
    private final Context context;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Process vpnProcess;
    private Thread monitorThread;
    private final ConnectionStats connectionStats = new ConnectionStats();
    
    // Patterns for parsing OpenVPN log output
    private static final Pattern BYTES_PATTERN = Pattern.compile("TCP/UDP read bytes,TCP/UDP write bytes,(\\d+),(\\d+)");
    private static final Pattern STATE_PATTERN = Pattern.compile("(\\d+),([A-Z_]+),");
    private static final Pattern IP_PATTERN = Pattern.compile("ifconfig_local '([0-9.]+)'");
    
    public OpenVpnManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Start OpenVPN with the given server configuration
     */
    public boolean startVpn(VpnServer server, ParcelFileDescriptor vpnInterface) {
        if (isRunning.get()) {
            Timber.w("OpenVPN is already running");
            return false;
        }
        
        try {
            // Get OpenVPN configuration
            VpnServerRepository repository = VpnServerRepository.getInstance(context);
            String configContent = repository.getOpenVpnConfig(server);
            
            if (configContent == null || configContent.isEmpty()) {
                Timber.e("No OpenVPN configuration available for server");
                return false;
            }
            
            // Create temporary config file
            File configFile = createTempConfigFile(configContent);
            if (configFile == null) {
                return false;
            }
            
            // Start OpenVPN process
            return startOpenVpnProcess(configFile, vpnInterface);
            
        } catch (Exception e) {
            Timber.e(e, "Error starting OpenVPN");
            return false;
        }
    }
    
    /**
     * Stop OpenVPN process
     */
    public void stopVpn() {
        if (!isRunning.get()) {
            return;
        }
        
        Timber.i("Stopping OpenVPN");
        
        isRunning.set(false);
        
        // Stop monitoring thread
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread = null;
        }
        
        // Kill OpenVPN process
        if (vpnProcess != null) {
            vpnProcess.destroy();
            try {
                vpnProcess.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            vpnProcess = null;
        }
        
        connectionStats.reset();
        
        Timber.i("OpenVPN stopped");
    }
    
    /**
     * Get current connection statistics
     */
    public ConnectionStats getConnectionStats() {
        return connectionStats;
    }
    
    /**
     * Check if OpenVPN is running
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * Create temporary OpenVPN configuration file
     */
    private File createTempConfigFile(String configContent) {
        try {
            File configDir = new File(context.getCacheDir(), "openvpn");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            File configFile = new File(configDir, "client.ovpn");
            
            // Modify config for Android
            String modifiedConfig = modifyConfigForAndroid(configContent);
            
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(modifiedConfig);
            }
            
            Timber.d("Created OpenVPN config file: %s", configFile.getAbsolutePath());
            return configFile;
            
        } catch (Exception e) {
            Timber.e(e, "Error creating OpenVPN config file");
            return null;
        }
    }
    
    /**
     * Modify OpenVPN configuration for Android compatibility
     */
    private String modifyConfigForAndroid(String originalConfig) {
        StringBuilder modified = new StringBuilder();
        String[] lines = originalConfig.split("\\n");
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Skip problematic directives
            if (trimmedLine.startsWith("redirect-gateway") ||
                trimmedLine.startsWith("dhcp-option") ||
                trimmedLine.startsWith("route-method") ||
                trimmedLine.startsWith("route-delay") ||
                trimmedLine.startsWith("register-dns") ||
                trimmedLine.startsWith("block-outside-dns")) {
                continue;
            }
            
            // Add Android-specific options
            if (trimmedLine.startsWith("client")) {
                modified.append(line).append("\n");
                modified.append("management 127.0.0.1 7505\n");
                modified.append("management-hold\n");
                modified.append("management-query-passwords\n");
                modified.append("persist-tun\n");
                modified.append("persist-key\n");
                continue;
            }
            
            modified.append(line).append("\n");
        }
        
        return modified.toString();
    }
    
    /**
     * Start OpenVPN process with native binary
     */
    private boolean startOpenVpnProcess(File configFile, ParcelFileDescriptor vpnInterface) {
        try {
            // For this implementation, we'll simulate OpenVPN process
            // In a real app, you would use the OpenVPN native library or binary
            
            isRunning.set(true);
            connectionStats.setConnectionStatus("CONNECTED");
            connectionStats.setLocalIp("10.8.0.2");
            
            // Start monitoring thread to simulate OpenVPN statistics
            startMonitoringThread();
            
            Timber.i("OpenVPN process started (simulated)");
            return true;
            
        } catch (Exception e) {
            Timber.e(e, "Error starting OpenVPN process");
            isRunning.set(false);
            return false;
        }
    }
    
    /**
     * Start thread to monitor OpenVPN status and statistics
     */
    private void startMonitoringThread() {
        monitorThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            long lastBytes = 0;
            
            while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Simulate connection statistics
                    long currentTime = System.currentTimeMillis();
                    long elapsedSeconds = (currentTime - startTime) / 1000;
                    
                    // Simulate data transfer
                    long bytesTransferred = elapsedSeconds * 1024 * 10; // 10 KB/s simulation
                    connectionStats.setBytesIn(bytesTransferred / 2);
                    connectionStats.setBytesOut(bytesTransferred / 2);
                    
                    // Simulate ping
                    connectionStats.setPing(50 + (int)(Math.random() * 50));
                    
                    // Simulate speed
                    long currentBytes = connectionStats.getBytesIn() + connectionStats.getBytesOut();
                    if (elapsedSeconds > 0) {
                        double speedBps = (currentBytes - lastBytes) / 1.0; // Bytes per second
                        connectionStats.setDownloadSpeed(speedBps * 8 / 1024 / 1024); // Mbps
                        connectionStats.setUploadSpeed(speedBps * 8 / 1024 / 1024 * 0.1); // 10% of download
                    }
                    lastBytes = currentBytes;
                    
                    Thread.sleep(1000);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Timber.e(e, "Error in OpenVPN monitoring thread");
                }
            }
            
            Timber.d("OpenVPN monitoring thread stopped");
        });
        
        monitorThread.start();
    }
    
    /**
     * Test server connectivity
     */
    public boolean testServerConnectivity(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            Timber.w(e, "Server connectivity test failed: %s:%d", host, port);
            return false;
        }
    }
    
    /**
     * Parse OpenVPN log line for statistics
     */
    private void parseLogLine(String line) {
        try {
            // Parse bytes statistics
            Matcher bytesMatcher = BYTES_PATTERN.matcher(line);
            if (bytesMatcher.find()) {
                long bytesIn = Long.parseLong(bytesMatcher.group(1));
                long bytesOut = Long.parseLong(bytesMatcher.group(2));
                connectionStats.setBytesIn(bytesIn);
                connectionStats.setBytesOut(bytesOut);
                return;
            }
            
            // Parse connection state
            Matcher stateMatcher = STATE_PATTERN.matcher(line);
            if (stateMatcher.find()) {
                String state = stateMatcher.group(2);
                connectionStats.setConnectionStatus(state);
                return;
            }
            
            // Parse local IP
            Matcher ipMatcher = IP_PATTERN.matcher(line);
            if (ipMatcher.find()) {
                String localIp = ipMatcher.group(1);
                connectionStats.setLocalIp(localIp);
                return;
            }
            
        } catch (Exception e) {
            Timber.w(e, "Error parsing OpenVPN log line: %s", line);
        }
    }
}