package com.vpngate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for VPN configuration settings
 */
public class VpnConfig implements Parcelable {
    
    private boolean autoConnect;
    private boolean killSwitch;
    private boolean dnsProtection;
    private String customDns1;
    private String customDns2;
    private boolean autoReconnect;
    private int reconnectInterval;
    private boolean showNotification;
    private boolean startOnBoot;
    private String protocol;
    private int connectionTimeout;
    private boolean bypassLan;
    private boolean blockAds;
    private boolean blockMalware;
    private String preferredCountry;
    private int maxPing;
    private long minSpeed;
    
    public VpnConfig() {
        // Default values
        autoConnect = false;
        killSwitch = true;
        dnsProtection = true;
        customDns1 = "8.8.8.8";
        customDns2 = "8.8.4.4";
        autoReconnect = true;
        reconnectInterval = 30;
        showNotification = true;
        startOnBoot = false;
        protocol = "OpenVPN";
        connectionTimeout = 30;
        bypassLan = true;
        blockAds = false;
        blockMalware = false;
        preferredCountry = "";
        maxPing = 300;
        minSpeed = 0;
    }
    
    protected VpnConfig(Parcel in) {
        autoConnect = in.readByte() != 0;
        killSwitch = in.readByte() != 0;
        dnsProtection = in.readByte() != 0;
        customDns1 = in.readString();
        customDns2 = in.readString();
        autoReconnect = in.readByte() != 0;
        reconnectInterval = in.readInt();
        showNotification = in.readByte() != 0;
        startOnBoot = in.readByte() != 0;
        protocol = in.readString();
        connectionTimeout = in.readInt();
        bypassLan = in.readByte() != 0;
        blockAds = in.readByte() != 0;
        blockMalware = in.readByte() != 0;
        preferredCountry = in.readString();
        maxPing = in.readInt();
        minSpeed = in.readLong();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (autoConnect ? 1 : 0));
        dest.writeByte((byte) (killSwitch ? 1 : 0));
        dest.writeByte((byte) (dnsProtection ? 1 : 0));
        dest.writeString(customDns1);
        dest.writeString(customDns2);
        dest.writeByte((byte) (autoReconnect ? 1 : 0));
        dest.writeInt(reconnectInterval);
        dest.writeByte((byte) (showNotification ? 1 : 0));
        dest.writeByte((byte) (startOnBoot ? 1 : 0));
        dest.writeString(protocol);
        dest.writeInt(connectionTimeout);
        dest.writeByte((byte) (bypassLan ? 1 : 0));
        dest.writeByte((byte) (blockAds ? 1 : 0));
        dest.writeByte((byte) (blockMalware ? 1 : 0));
        dest.writeString(preferredCountry);
        dest.writeInt(maxPing);
        dest.writeLong(minSpeed);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<VpnConfig> CREATOR = new Creator<VpnConfig>() {
        @Override
        public VpnConfig createFromParcel(Parcel in) {
            return new VpnConfig(in);
        }
        
        @Override
        public VpnConfig[] newArray(int size) {
            return new VpnConfig[size];
        }
    };
    
    // Getters and Setters
    public boolean isAutoConnect() {
        return autoConnect;
    }
    
    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }
    
    public boolean isKillSwitch() {
        return killSwitch;
    }
    
    public void setKillSwitch(boolean killSwitch) {
        this.killSwitch = killSwitch;
    }
    
    public boolean isDnsProtection() {
        return dnsProtection;
    }
    
    public void setDnsProtection(boolean dnsProtection) {
        this.dnsProtection = dnsProtection;
    }
    
    public String getCustomDns1() {
        return customDns1;
    }
    
    public void setCustomDns1(String customDns1) {
        this.customDns1 = customDns1;
    }
    
    public String getCustomDns2() {
        return customDns2;
    }
    
    public void setCustomDns2(String customDns2) {
        this.customDns2 = customDns2;
    }
    
    public boolean isAutoReconnect() {
        return autoReconnect;
    }
    
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }
    
    public int getReconnectInterval() {
        return reconnectInterval;
    }
    
    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }
    
    public boolean isShowNotification() {
        return showNotification;
    }
    
    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }
    
    public boolean isStartOnBoot() {
        return startOnBoot;
    }
    
    public void setStartOnBoot(boolean startOnBoot) {
        this.startOnBoot = startOnBoot;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public boolean isBypassLan() {
        return bypassLan;
    }
    
    public void setBypassLan(boolean bypassLan) {
        this.bypassLan = bypassLan;
    }
    
    public boolean isBlockAds() {
        return blockAds;
    }
    
    public void setBlockAds(boolean blockAds) {
        this.blockAds = blockAds;
    }
    
    public boolean isBlockMalware() {
        return blockMalware;
    }
    
    public void setBlockMalware(boolean blockMalware) {
        this.blockMalware = blockMalware;
    }
    
    public String getPreferredCountry() {
        return preferredCountry;
    }
    
    public void setPreferredCountry(String preferredCountry) {
        this.preferredCountry = preferredCountry;
    }
    
    public int getMaxPing() {
        return maxPing;
    }
    
    public void setMaxPing(int maxPing) {
        this.maxPing = maxPing;
    }
    
    public long getMinSpeed() {
        return minSpeed;
    }
    
    public void setMinSpeed(long minSpeed) {
        this.minSpeed = minSpeed;
    }
}