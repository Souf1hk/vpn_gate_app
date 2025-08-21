package com.vpngate.android;

import android.app.Application;
import android.content.Context;
import com.blongho.country_data.World;
import timber.log.Timber;

/**
 * Main Application class for VPN Gate Android app
 */
public class VpnGateApplication extends Application {
    
    private static Context applicationContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        applicationContext = this;
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        
        // Initialize country data for flags
        World.init(this);
        
        Timber.d("VPN Gate Application initialized");
    }
    
    public static Context getAppContext() {
        return applicationContext;
    }
}