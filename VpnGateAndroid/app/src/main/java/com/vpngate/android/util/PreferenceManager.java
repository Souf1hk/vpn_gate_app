package com.vpngate.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.vpngate.android.model.VpnConfig;
import com.vpngate.android.model.VpnServer;
import java.util.HashSet;
import java.util.Set;

/**
 * Manager for app preferences and settings
 */
public class PreferenceManager {
    
    private static final String PREFS_NAME = "vpn_gate_prefs";
    
    // Preference keys
    private static final String KEY_VPN_CONFIG = "vpn_config";
    private static final String KEY_FAVORITE_SERVERS = "favorite_servers";
    private static final String KEY_LAST_CONNECTED_SERVER = "last_connected_server";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_AUTO_CONNECT = "auto_connect";
    private static final String KEY_KILL_SWITCH = "kill_switch";
    private static final String KEY_DNS_PROTECTION = "dns_protection";
    private static final String KEY_CUSTOM_DNS_1 = "custom_dns_1";
    private static final String KEY_CUSTOM_DNS_2 = "custom_dns_2";
    private static final String KEY_AUTO_RECONNECT = "auto_reconnect";
    private static final String KEY_RECONNECT_INTERVAL = "reconnect_interval";
    private static final String KEY_SHOW_NOTIFICATION = "show_notification";
    private static final String KEY_START_ON_BOOT = "start_on_boot";
    private static final String KEY_CONNECTION_TIMEOUT = "connection_timeout";
    private static final String KEY_BYPASS_LAN = "bypass_lan";
    private static final String KEY_BLOCK_ADS = "block_ads";
    private static final String KEY_BLOCK_MALWARE = "block_malware";
    private static final String KEY_PREFERRED_COUNTRY = "preferred_country";
    private static final String KEY_MAX_PING = "max_ping";
    private static final String KEY_MIN_SPEED = "min_speed";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ANALYTICS_ENABLED = "analytics_enabled";
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Get VPN configuration
     */
    public VpnConfig getVpnConfig() {
        String configJson = prefs.getString(KEY_VPN_CONFIG, null);
        if (configJson != null) {
            try {
                return gson.fromJson(configJson, VpnConfig.class);
            } catch (Exception e) {
                // Return default config if parsing fails
            }
        }
        return new VpnConfig(); // Default configuration
    }
    
    /**
     * Save VPN configuration
     */
    public void saveVpnConfig(VpnConfig config) {
        String configJson = gson.toJson(config);
        prefs.edit().putString(KEY_VPN_CONFIG, configJson).apply();
    }
    
    /**
     * Get favorite server IDs
     */
    public Set<String> getFavoriteServers() {
        return prefs.getStringSet(KEY_FAVORITE_SERVERS, new HashSet<>());
    }
    
    /**
     * Add server to favorites
     */
    public void addFavoriteServer(String serverId) {
        Set<String> favorites = new HashSet<>(getFavoriteServers());
        favorites.add(serverId);
        prefs.edit().putStringSet(KEY_FAVORITE_SERVERS, favorites).apply();
    }
    
    /**
     * Remove server from favorites
     */
    public void removeFavoriteServer(String serverId) {
        Set<String> favorites = new HashSet<>(getFavoriteServers());
        favorites.remove(serverId);
        prefs.edit().putStringSet(KEY_FAVORITE_SERVERS, favorites).apply();
    }
    
    /**
     * Get last connected server
     */
    public VpnServer getLastConnectedServer() {
        String serverJson = prefs.getString(KEY_LAST_CONNECTED_SERVER, null);
        if (serverJson != null) {
            try {
                return gson.fromJson(serverJson, VpnServer.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Save last connected server
     */
    public void saveLastConnectedServer(VpnServer server) {
        String serverJson = gson.toJson(server);
        prefs.edit().putString(KEY_LAST_CONNECTED_SERVER, serverJson).apply();
    }
    
    /**
     * Check if this is first launch
     */
    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    
    /**
     * Set first launch completed
     */
    public void setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
    
    // Individual preference getters and setters
    public boolean isAutoConnect() {
        return prefs.getBoolean(KEY_AUTO_CONNECT, false);
    }
    
    public void setAutoConnect(boolean autoConnect) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT, autoConnect).apply();
    }
    
    public boolean isKillSwitch() {
        return prefs.getBoolean(KEY_KILL_SWITCH, true);
    }
    
    public void setKillSwitch(boolean killSwitch) {
        prefs.edit().putBoolean(KEY_KILL_SWITCH, killSwitch).apply();
    }
    
    public boolean isDnsProtection() {
        return prefs.getBoolean(KEY_DNS_PROTECTION, true);
    }
    
    public void setDnsProtection(boolean dnsProtection) {
        prefs.edit().putBoolean(KEY_DNS_PROTECTION, dnsProtection).apply();
    }
    
    public String getCustomDns1() {
        return prefs.getString(KEY_CUSTOM_DNS_1, "8.8.8.8");
    }
    
    public void setCustomDns1(String dns) {
        prefs.edit().putString(KEY_CUSTOM_DNS_1, dns).apply();
    }
    
    public String getCustomDns2() {
        return prefs.getString(KEY_CUSTOM_DNS_2, "8.8.4.4");
    }
    
    public void setCustomDns2(String dns) {
        prefs.edit().putString(KEY_CUSTOM_DNS_2, dns).apply();
    }
    
    public boolean isAutoReconnect() {
        return prefs.getBoolean(KEY_AUTO_RECONNECT, true);
    }
    
    public void setAutoReconnect(boolean autoReconnect) {
        prefs.edit().putBoolean(KEY_AUTO_RECONNECT, autoReconnect).apply();
    }
    
    public int getReconnectInterval() {
        return prefs.getInt(KEY_RECONNECT_INTERVAL, 30);
    }
    
    public void setReconnectInterval(int interval) {
        prefs.edit().putInt(KEY_RECONNECT_INTERVAL, interval).apply();
    }
    
    public boolean isShowNotification() {
        return prefs.getBoolean(KEY_SHOW_NOTIFICATION, true);
    }
    
    public void setShowNotification(boolean showNotification) {
        prefs.edit().putBoolean(KEY_SHOW_NOTIFICATION, showNotification).apply();
    }
    
    public boolean isStartOnBoot() {
        return prefs.getBoolean(KEY_START_ON_BOOT, false);
    }
    
    public void setStartOnBoot(boolean startOnBoot) {
        prefs.edit().putBoolean(KEY_START_ON_BOOT, startOnBoot).apply();
    }
    
    public int getConnectionTimeout() {
        return prefs.getInt(KEY_CONNECTION_TIMEOUT, 30);
    }
    
    public void setConnectionTimeout(int timeout) {
        prefs.edit().putInt(KEY_CONNECTION_TIMEOUT, timeout).apply();
    }
    
    public boolean isBypassLan() {
        return prefs.getBoolean(KEY_BYPASS_LAN, true);
    }
    
    public void setBypassLan(boolean bypassLan) {
        prefs.edit().putBoolean(KEY_BYPASS_LAN, bypassLan).apply();
    }
    
    public boolean isBlockAds() {
        return prefs.getBoolean(KEY_BLOCK_ADS, false);
    }
    
    public void setBlockAds(boolean blockAds) {
        prefs.edit().putBoolean(KEY_BLOCK_ADS, blockAds).apply();
    }
    
    public boolean isBlockMalware() {
        return prefs.getBoolean(KEY_BLOCK_MALWARE, false);
    }
    
    public void setBlockMalware(boolean blockMalware) {
        prefs.edit().putBoolean(KEY_BLOCK_MALWARE, blockMalware).apply();
    }
    
    public String getPreferredCountry() {
        return prefs.getString(KEY_PREFERRED_COUNTRY, "");
    }
    
    public void setPreferredCountry(String country) {
        prefs.edit().putString(KEY_PREFERRED_COUNTRY, country).apply();
    }
    
    public int getMaxPing() {
        return prefs.getInt(KEY_MAX_PING, 300);
    }
    
    public void setMaxPing(int maxPing) {
        prefs.edit().putInt(KEY_MAX_PING, maxPing).apply();
    }
    
    public long getMinSpeed() {
        return prefs.getLong(KEY_MIN_SPEED, 0);
    }
    
    public void setMinSpeed(long minSpeed) {
        prefs.edit().putLong(KEY_MIN_SPEED, minSpeed).apply();
    }
    
    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, "system");
    }
    
    public void setThemeMode(String themeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode).apply();
    }
    
    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "system");
    }
    
    public void setLanguage(String language) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }
    
    public boolean isAnalyticsEnabled() {
        return prefs.getBoolean(KEY_ANALYTICS_ENABLED, true);
    }
    
    public void setAnalyticsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, enabled).apply();
    }
    
    /**
     * Clear all preferences (for reset functionality)
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}