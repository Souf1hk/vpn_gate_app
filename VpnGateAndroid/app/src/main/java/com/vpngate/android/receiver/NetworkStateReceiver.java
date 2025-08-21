package com.vpngate.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

/**
 * Broadcast receiver for network state changes
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    
    public static final String ACTION_NETWORK_CHANGED = "com.vpngate.android.NETWORK_CHANGED";
    public static final String EXTRA_IS_CONNECTED = "is_connected";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            
            Timber.d("Network state changed: connected = %b", isConnected);
            
            // Broadcast network state change
            Intent networkIntent = new Intent(ACTION_NETWORK_CHANGED);
            networkIntent.putExtra(EXTRA_IS_CONNECTED, isConnected);
            LocalBroadcastManager.getInstance(context).sendBroadcast(networkIntent);
        }
    }
}