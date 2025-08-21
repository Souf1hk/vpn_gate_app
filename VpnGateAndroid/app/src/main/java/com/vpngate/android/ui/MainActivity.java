package com.vpngate.android.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blongho.country_data.Country;
import com.blongho.country_data.World;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.vpngate.android.R;
import com.vpngate.android.model.ConnectionStats;
import com.vpngate.android.model.VpnServer;
import com.vpngate.android.repository.VpnServerRepository;
import com.vpngate.android.service.VpnGateService;
import com.vpngate.android.util.PreferenceManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Main activity for VPN Gate app
 */
public class MainActivity extends AppCompatActivity {
    
    private static final int VPN_REQUEST_CODE = 1001;
    
    // UI Components
    private ImageView ivConnectionStatus;
    private TextView tvConnectionStatus;
    private TextView tvServerInfo;
    private MaterialButton btnConnect;
    private MaterialButton btnAutoSelect;
    private MaterialButton btnServerList;
    private MaterialCardView cardStatistics;
    private MaterialCardView cardCurrentServer;
    private CircularProgressIndicator progressIndicator;
    
    // Statistics UI
    private TextView tvDuration;
    private TextView tvDownloaded;
    private TextView tvUploaded;
    private TextView tvPing;
    private TextView tvDownloadSpeed;
    private TextView tvUploadSpeed;
    
    // Current Server UI
    private ImageView ivCurrentServerFlag;
    private TextView tvCurrentServerCountry;
    private TextView tvCurrentServerIp;
    private TextView tvCurrentServerPing;
    
    // Service and Repository
    private VpnGateService vpnService;
    private boolean isServiceBound = false;
    private VpnServerRepository serverRepository;
    private PreferenceManager preferenceManager;
    
    // RxJava
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable statsUpdateDisposable;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> vpnPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeComponents();
        setupUI();
        setupActivityResultLaunchers();
        bindVpnService();
        
        // Check if this is first launch
        if (preferenceManager.isFirstLaunch()) {
            showWelcomeDialog();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceivers();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        unregisterBroadcastReceivers();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (statsUpdateDisposable != null) {
            statsUpdateDisposable.dispose();
        }
        unbindVpnService();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.action_refresh) {
            refreshServers();
            return true;
        } else if (itemId == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Initialize components
     */
    private void initializeComponents() {
        serverRepository = VpnServerRepository.getInstance(this);
        preferenceManager = new PreferenceManager(this);
        
        // Initialize UI components
        ivConnectionStatus = findViewById(R.id.iv_connection_status);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvServerInfo = findViewById(R.id.tv_server_info);
        btnConnect = findViewById(R.id.btn_connect);
        btnAutoSelect = findViewById(R.id.btn_auto_select);
        btnServerList = findViewById(R.id.btn_server_list);
        cardStatistics = findViewById(R.id.card_statistics);
        cardCurrentServer = findViewById(R.id.card_current_server);
        progressIndicator = findViewById(R.id.progress_indicator);
        
        // Statistics UI
        tvDuration = findViewById(R.id.tv_duration);
        tvDownloaded = findViewById(R.id.tv_downloaded);
        tvUploaded = findViewById(R.id.tv_uploaded);
        tvPing = findViewById(R.id.tv_ping);
        tvDownloadSpeed = findViewById(R.id.tv_download_speed);
        tvUploadSpeed = findViewById(R.id.tv_upload_speed);
        
        // Current Server UI
        ivCurrentServerFlag = findViewById(R.id.iv_current_server_flag);
        tvCurrentServerCountry = findViewById(R.id.tv_current_server_country);
        tvCurrentServerIp = findViewById(R.id.tv_current_server_ip);
        tvCurrentServerPing = findViewById(R.id.tv_current_server_ping);
    }
    
    /**
     * Setup UI event listeners
     */
    private void setupUI() {
        btnConnect.setOnClickListener(v -> toggleVpnConnection());
        btnAutoSelect.setOnClickListener(v -> autoSelectServer());
        btnServerList.setOnClickListener(v -> openServerList());
        
        updateConnectionStatus(VpnGateService.STATUS_DISCONNECTED, null);
    }
    
    /**
     * Setup activity result launchers
     */
    private void setupActivityResultLaunchers() {
        vpnPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Permission granted, proceed with connection
                    proceedWithConnection();
                } else {
                    // Permission denied
                    showVpnPermissionDeniedDialog();
                }
            }
        );
    }
    
    /**
     * Bind to VPN service
     */
    private void bindVpnService() {
        Intent serviceIntent = new Intent(this, VpnGateService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    /**
     * Unbind from VPN service
     */
    private void unbindVpnService() {
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
    
    /**
     * Service connection for VPN service
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            VpnGateService.VpnGateServiceBinder binder = (VpnGateService.VpnGateServiceBinder) service;
            vpnService = binder.getService();
            isServiceBound = true;
            
            // Update UI with current service state
            updateConnectionStatus(vpnService.getCurrentStatus(), vpnService.getCurrentServer());
            
            Timber.d("VPN service connected");
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            vpnService = null;
            isServiceBound = false;
            Timber.d("VPN service disconnected");
        }
    };
    
    /**
     * Register broadcast receivers
     */
    private void registerBroadcastReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(VpnGateService.ACTION_STATUS_CHANGED);
        filter.addAction(VpnGateService.ACTION_STATS_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(vpnBroadcastReceiver, filter);
    }
    
    /**
     * Unregister broadcast receivers
     */
    private void unregisterBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(vpnBroadcastReceiver);
    }
    
    /**
     * Broadcast receiver for VPN service events
     */
    private final BroadcastReceiver vpnBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (VpnGateService.ACTION_STATUS_CHANGED.equals(action)) {
                String status = intent.getStringExtra(VpnGateService.EXTRA_STATUS);
                VpnServer server = intent.getParcelableExtra(VpnGateService.EXTRA_SERVER);
                updateConnectionStatus(status, server);
                
            } else if (VpnGateService.ACTION_STATS_UPDATED.equals(action)) {
                ConnectionStats stats = intent.getParcelableExtra(VpnGateService.EXTRA_STATS);
                updateConnectionStats(stats);
            }
        }
    };
    
    /**
     * Toggle VPN connection
     */
    private void toggleVpnConnection() {
        if (vpnService == null) {
            return;
        }
        
        if (vpnService.isConnected()) {
            // Disconnect
            showDisconnectConfirmationDialog();
        } else if (vpnService.isConnecting()) {
            // Cancel connection
            vpnService.disconnect();
        } else {
            // Connect
            VpnServer lastServer = preferenceManager.getLastConnectedServer();
            if (lastServer != null) {
                connectToServer(lastServer);
            } else {
                autoSelectServer();
            }
        }
    }
    
    /**
     * Auto select best server and connect
     */
    private void autoSelectServer() {
        showProgress(true);
        
        Disposable disposable = serverRepository.getBestServer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::connectToServer,
                error -> {
                    showProgress(false);
                    showError("Failed to find best server: " + error.getMessage());
                    Timber.e(error, "Error finding best server");
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    /**
     * Connect to a specific server
     */
    private void connectToServer(VpnServer server) {
        showProgress(false);
        
        // Check VPN permission
        Intent prepareIntent = VpnService.prepare(this);
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent);
            return;
        }
        
        proceedWithConnection(server);
    }
    
    /**
     * Proceed with VPN connection after permission is granted
     */
    private void proceedWithConnection() {
        VpnServer lastServer = preferenceManager.getLastConnectedServer();
        if (lastServer != null) {
            proceedWithConnection(lastServer);
        } else {
            autoSelectServer();
        }
    }
    
    /**
     * Proceed with VPN connection to specific server
     */
    private void proceedWithConnection(VpnServer server) {
        if (vpnService != null) {
            vpnService.connectToServer(server);
            preferenceManager.saveLastConnectedServer(server);
        }
    }
    
    /**
     * Open server list activity
     */
    private void openServerList() {
        Intent intent = new Intent(this, ServerListActivity.class);
        startActivity(intent);
    }
    
    /**
     * Refresh server list
     */
    private void refreshServers() {
        showProgress(true);
        
        Disposable disposable = serverRepository.getVpnServers(true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                servers -> {
                    showProgress(false);
                    Toast.makeText(this, "Servers refreshed (" + servers.size() + " found)", 
                        Toast.LENGTH_SHORT).show();
                },
                error -> {
                    showProgress(false);
                    showError("Failed to refresh servers: " + error.getMessage());
                    Timber.e(error, "Error refreshing servers");
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    /**
     * Update connection status UI
     */
    private void updateConnectionStatus(String status, VpnServer server) {
        if (status == null) status = VpnGateService.STATUS_DISCONNECTED;
        
        switch (status) {
            case VpnGateService.STATUS_CONNECTED:
                ivConnectionStatus.setImageResource(R.drawable.ic_vpn_key);
                ivConnectionStatus.setColorFilter(getColor(R.color.connected));
                tvConnectionStatus.setText(R.string.connected);
                tvConnectionStatus.setTextColor(getColor(R.color.connected));
                btnConnect.setText(R.string.disconnect);
                btnConnect.setBackgroundTintList(getColorStateList(R.color.button_disconnect));
                showServerInfo(server);
                cardStatistics.setVisibility(View.VISIBLE);
                cardCurrentServer.setVisibility(View.VISIBLE);
                startStatsUpdates();
                break;
                
            case VpnGateService.STATUS_CONNECTING:
                ivConnectionStatus.setImageResource(R.drawable.ic_vpn_key);
                ivConnectionStatus.setColorFilter(getColor(R.color.connecting));
                tvConnectionStatus.setText(R.string.connecting);
                tvConnectionStatus.setTextColor(getColor(R.color.connecting));
                btnConnect.setText(R.string.cancel);
                btnConnect.setBackgroundTintList(getColorStateList(R.color.button_disconnect));
                if (server != null) {
                    tvServerInfo.setText("Connecting to " + server.getCountryLong());
                }
                break;
                
            case VpnGateService.STATUS_DISCONNECTING:
                tvConnectionStatus.setText("Disconnecting...");
                tvConnectionStatus.setTextColor(getColor(R.color.connecting));
                btnConnect.setText(R.string.disconnect);
                btnConnect.setEnabled(false);
                break;
                
            case VpnGateService.STATUS_ERROR:
                ivConnectionStatus.setImageResource(R.drawable.ic_error);
                ivConnectionStatus.setColorFilter(getColor(R.color.error_red));
                tvConnectionStatus.setText(R.string.connection_failed);
                tvConnectionStatus.setTextColor(getColor(R.color.error_red));
                btnConnect.setText(R.string.retry);
                btnConnect.setBackgroundTintList(getColorStateList(R.color.button_connect));
                btnConnect.setEnabled(true);
                hideConnectionInfo();
                stopStatsUpdates();
                break;
                
            default: // DISCONNECTED
                ivConnectionStatus.setImageResource(R.drawable.ic_vpn_key);
                ivConnectionStatus.setColorFilter(getColor(R.color.disconnected));
                tvConnectionStatus.setText(R.string.disconnected);
                tvConnectionStatus.setTextColor(getColor(R.color.disconnected));
                tvServerInfo.setText(R.string.select_server);
                btnConnect.setText(R.string.connect);
                btnConnect.setBackgroundTintList(getColorStateList(R.color.button_connect));
                btnConnect.setEnabled(true);
                hideConnectionInfo();
                stopStatsUpdates();
                break;
        }
    }
    
    /**
     * Show server information
     */
    private void showServerInfo(VpnServer server) {
        if (server != null) {
            tvServerInfo.setText(server.getCountryLong() + " • " + server.getIpAddress());
            
            // Update current server card
            Country country = World.getCountryFrom(server.getCountryShort().toLowerCase());
            if (country != null) {
                Glide.with(this)
                    .load(country.getFlagResource())
                    .into(ivCurrentServerFlag);
            }
            
            tvCurrentServerCountry.setText(server.getCountryLong());
            tvCurrentServerIp.setText(server.getIpAddress());
            tvCurrentServerPing.setText(server.getPing() + " ms");
            
            // Set ping color
            int pingColor = getPingColor(server.getPing());
            tvCurrentServerPing.setTextColor(pingColor);
        }
    }
    
    /**
     * Hide connection information
     */
    private void hideConnectionInfo() {
        cardStatistics.setVisibility(View.GONE);
        cardCurrentServer.setVisibility(View.GONE);
    }
    
    /**
     * Update connection statistics
     */
    private void updateConnectionStats(ConnectionStats stats) {
        if (stats == null) return;
        
        tvDuration.setText(stats.getFormattedConnectionTime());
        tvDownloaded.setText(stats.getFormattedBytesIn());
        tvUploaded.setText(stats.getFormattedBytesOut());
        tvPing.setText(stats.getPing() + " ms");
        tvDownloadSpeed.setText(String.format("%.2f Mbps", stats.getDownloadSpeed()));
        tvUploadSpeed.setText(String.format("%.2f Mbps", stats.getUploadSpeed()));
    }
    
    /**
     * Start periodic statistics updates
     */
    private void startStatsUpdates() {
        stopStatsUpdates();
        
        if (vpnService != null && vpnService.isConnected()) {
            statsUpdateDisposable = io.reactivex.rxjava3.core.Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tick -> {
                    if (vpnService != null) {
                        ConnectionStats stats = vpnService.getConnectionStats();
                        updateConnectionStats(stats);
                    }
                });
        }
    }
    
    /**
     * Stop statistics updates
     */
    private void stopStatsUpdates() {
        if (statsUpdateDisposable != null) {
            statsUpdateDisposable.dispose();
            statsUpdateDisposable = null;
        }
    }
    
    /**
     * Show/hide progress indicator
     */
    private void showProgress(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Get color for ping value
     */
    private int getPingColor(int ping) {
        if (ping <= 50) {
            return getColor(R.color.ping_excellent);
        } else if (ping <= 100) {
            return getColor(R.color.ping_good);
        } else if (ping <= 200) {
            return getColor(R.color.ping_fair);
        } else {
            return getColor(R.color.ping_poor);
        }
    }
    
    /**
     * Show welcome dialog for first launch
     */
    private void showWelcomeDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Welcome to VPN Gate")
            .setMessage("VPN Gate provides free VPN servers from volunteers around the world. " +
                       "This app helps you connect to these servers securely.\n\n" +
                       "Please use this service responsibly and respect the server operators.")
            .setPositiveButton("Get Started", (dialog, which) -> {
                preferenceManager.setFirstLaunchCompleted();
                dialog.dismiss();
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * Show disconnect confirmation dialog
     */
    private void showDisconnectConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_confirm_disconnect_title)
            .setMessage(R.string.dialog_confirm_disconnect_message)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                if (vpnService != null) {
                    vpnService.disconnect();
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }
    
    /**
     * Show VPN permission denied dialog
     */
    private void showVpnPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_vpn_permission_title)
            .setMessage(R.string.dialog_vpn_permission_message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }
    
    /**
     * Show about dialog
     */
    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("About VPN Gate")
            .setMessage("VPN Gate Android Client\n\n" +
                       "This app connects to public VPN servers provided by the VPN Gate " +
                       "academic experiment project at University of Tsukuba, Japan.\n\n" +
                       "Version: " + BuildConfig.VERSION_NAME)
            .setPositiveButton(R.string.ok, null)
            .show();
    }
}