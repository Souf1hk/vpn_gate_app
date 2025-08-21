package com.vpngate.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.vpngate.android.service.VpnGateService;
import com.vpngate.android.util.PreferenceManager;
import timber.log.Timber;

/**
 * Broadcast receiver for device boot events
 */
public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            
            PreferenceManager preferenceManager = new PreferenceManager(context);
            
            if (preferenceManager.isStartOnBoot() && preferenceManager.isAutoConnect()) {
                Timber.d("Device booted, starting VPN service");
                
                // Start VPN service
                Intent serviceIntent = new Intent(context, VpnGateService.class);
                context.startForegroundService(serviceIntent);
            }
        }
    }
}