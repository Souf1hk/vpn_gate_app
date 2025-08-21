package com.vpngate.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for VPN connection statistics
 */
public class ConnectionStats implements Parcelable {
    
    private long bytesIn;
    private long bytesOut;
    private long packetsIn;
    private long packetsOut;
    private long connectionTime;
    private String connectionStatus;
    private String localIp;
    private String publicIp;
    private int ping;
    private double downloadSpeed;
    private double uploadSpeed;
    
    public ConnectionStats() {
        this.connectionTime = System.currentTimeMillis();
        this.connectionStatus = "DISCONNECTED";
    }
    
    protected ConnectionStats(Parcel in) {
        bytesIn = in.readLong();
        bytesOut = in.readLong();
        packetsIn = in.readLong();
        packetsOut = in.readLong();
        connectionTime = in.readLong();
        connectionStatus = in.readString();
        localIp = in.readString();
        publicIp = in.readString();
        ping = in.readInt();
        downloadSpeed = in.readDouble();
        uploadSpeed = in.readDouble();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(bytesIn);
        dest.writeLong(bytesOut);
        dest.writeLong(packetsIn);
        dest.writeLong(packetsOut);
        dest.writeLong(connectionTime);
        dest.writeString(connectionStatus);
        dest.writeString(localIp);
        dest.writeString(publicIp);
        dest.writeInt(ping);
        dest.writeDouble(downloadSpeed);
        dest.writeDouble(uploadSpeed);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<ConnectionStats> CREATOR = new Creator<ConnectionStats>() {
        @Override
        public ConnectionStats createFromParcel(Parcel in) {
            return new ConnectionStats(in);
        }
        
        @Override
        public ConnectionStats[] newArray(int size) {
            return new ConnectionStats[size];
        }
    };
    
    // Getters and Setters
    public long getBytesIn() {
        return bytesIn;
    }
    
    public void setBytesIn(long bytesIn) {
        this.bytesIn = bytesIn;
    }
    
    public long getBytesOut() {
        return bytesOut;
    }
    
    public void setBytesOut(long bytesOut) {
        this.bytesOut = bytesOut;
    }
    
    public long getPacketsIn() {
        return packetsIn;
    }
    
    public void setPacketsIn(long packetsIn) {
        this.packetsIn = packetsIn;
    }
    
    public long getPacketsOut() {
        return packetsOut;
    }
    
    public void setPacketsOut(long packetsOut) {
        this.packetsOut = packetsOut;
    }
    
    public long getConnectionTime() {
        return connectionTime;
    }
    
    public void setConnectionTime(long connectionTime) {
        this.connectionTime = connectionTime;
    }
    
    public String getConnectionStatus() {
        return connectionStatus;
    }
    
    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
    
    public String getLocalIp() {
        return localIp;
    }
    
    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }
    
    public String getPublicIp() {
        return publicIp;
    }
    
    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }
    
    public int getPing() {
        return ping;
    }
    
    public void setPing(int ping) {
        this.ping = ping;
    }
    
    public double getDownloadSpeed() {
        return downloadSpeed;
    }
    
    public void setDownloadSpeed(double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }
    
    public double getUploadSpeed() {
        return uploadSpeed;
    }
    
    public void setUploadSpeed(double uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }
    
    // Utility methods
    public String getFormattedBytesIn() {
        return formatBytes(bytesIn);
    }
    
    public String getFormattedBytesOut() {
        return formatBytes(bytesOut);
    }
    
    public String getFormattedTotalBytes() {
        return formatBytes(bytesIn + bytesOut);
    }
    
    public String getFormattedConnectionTime() {
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - connectionTime;
        
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds = seconds % 60;
        minutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    public void reset() {
        bytesIn = 0;
        bytesOut = 0;
        packetsIn = 0;
        packetsOut = 0;
        connectionTime = System.currentTimeMillis();
        connectionStatus = "DISCONNECTED";
        localIp = null;
        publicIp = null;
        ping = 0;
        downloadSpeed = 0;
        uploadSpeed = 0;
    }
}