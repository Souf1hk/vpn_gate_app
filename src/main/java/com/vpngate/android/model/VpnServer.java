package com.vpngate.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/**
 * Model class representing a VPN Gate server
 */
public class VpnServer implements Parcelable {
    
    @SerializedName("hostname")
    private String hostname;
    
    @SerializedName("ip")
    private String ipAddress;
    
    @SerializedName("score")
    private long score;
    
    @SerializedName("ping")
    private int ping;
    
    @SerializedName("speed")
    private long speed;
    
    @SerializedName("country_long")
    private String countryLong;
    
    @SerializedName("country_short")
    private String countryShort;
    
    @SerializedName("num_vpn_sessions")
    private int numVpnSessions;
    
    @SerializedName("uptime")
    private long uptime;
    
    @SerializedName("total_users")
    private int totalUsers;
    
    @SerializedName("total_traffic")
    private long totalTraffic;
    
    @SerializedName("log_type")
    private String logType;
    
    @SerializedName("operator")
    private String operator;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("openvpn_config_data_base64")
    private String openVpnConfigDataBase64;
    
    // Connection status (not from API)
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isFavorite = false;
    
    // Default constructor
    public VpnServer() {}
    
    // Parcelable implementation
    protected VpnServer(Parcel in) {
        hostname = in.readString();
        ipAddress = in.readString();
        score = in.readLong();
        ping = in.readInt();
        speed = in.readLong();
        countryLong = in.readString();
        countryShort = in.readString();
        numVpnSessions = in.readInt();
        uptime = in.readLong();
        totalUsers = in.readInt();
        totalTraffic = in.readLong();
        logType = in.readString();
        operator = in.readString();
        message = in.readString();
        openVpnConfigDataBase64 = in.readString();
        isConnected = in.readByte() != 0;
        isConnecting = in.readByte() != 0;
        isFavorite = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hostname);
        dest.writeString(ipAddress);
        dest.writeLong(score);
        dest.writeInt(ping);
        dest.writeLong(speed);
        dest.writeString(countryLong);
        dest.writeString(countryShort);
        dest.writeInt(numVpnSessions);
        dest.writeLong(uptime);
        dest.writeInt(totalUsers);
        dest.writeLong(totalTraffic);
        dest.writeString(logType);
        dest.writeString(operator);
        dest.writeString(message);
        dest.writeString(openVpnConfigDataBase64);
        dest.writeByte((byte) (isConnected ? 1 : 0));
        dest.writeByte((byte) (isConnecting ? 1 : 0));
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<VpnServer> CREATOR = new Creator<VpnServer>() {
        @Override
        public VpnServer createFromParcel(Parcel in) {
            return new VpnServer(in);
        }
        
        @Override
        public VpnServer[] newArray(int size) {
            return new VpnServer[size];
        }
    };
    
    // Getters and Setters
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public long getScore() {
        return score;
    }
    
    public void setScore(long score) {
        this.score = score;
    }
    
    public int getPing() {
        return ping;
    }
    
    public void setPing(int ping) {
        this.ping = ping;
    }
    
    public long getSpeed() {
        return speed;
    }
    
    public void setSpeed(long speed) {
        this.speed = speed;
    }
    
    public String getCountryLong() {
        return countryLong;
    }
    
    public void setCountryLong(String countryLong) {
        this.countryLong = countryLong;
    }
    
    public String getCountryShort() {
        return countryShort;
    }
    
    public void setCountryShort(String countryShort) {
        this.countryShort = countryShort;
    }
    
    public int getNumVpnSessions() {
        return numVpnSessions;
    }
    
    public void setNumVpnSessions(int numVpnSessions) {
        this.numVpnSessions = numVpnSessions;
    }
    
    public long getUptime() {
        return uptime;
    }
    
    public void setUptime(long uptime) {
        this.uptime = uptime;
    }
    
    public int getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getTotalTraffic() {
        return totalTraffic;
    }
    
    public void setTotalTraffic(long totalTraffic) {
        this.totalTraffic = totalTraffic;
    }
    
    public String getLogType() {
        return logType;
    }
    
    public void setLogType(String logType) {
        this.logType = logType;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getOpenVpnConfigDataBase64() {
        return openVpnConfigDataBase64;
    }
    
    public void setOpenVpnConfigDataBase64(String openVpnConfigDataBase64) {
        this.openVpnConfigDataBase64 = openVpnConfigDataBase64;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void setConnected(boolean connected) {
        isConnected = connected;
    }
    
    public boolean isConnecting() {
        return isConnecting;
    }
    
    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }
    
    public boolean isFavorite() {
        return isFavorite;
    }
    
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    // Utility methods
    public String getFormattedSpeed() {
        if (speed == 0) return "Unknown";
        
        double speedMbps = speed / 1_000_000.0;
        if (speedMbps >= 1000) {
            return String.format("%.1f Gbps", speedMbps / 1000);
        } else if (speedMbps >= 1) {
            return String.format("%.1f Mbps", speedMbps);
        } else {
            return String.format("%.0f Kbps", speed / 1000.0);
        }
    }
    
    public String getFormattedTraffic() {
        if (totalTraffic == 0) return "Unknown";
        
        double trafficGB = totalTraffic / (1024.0 * 1024.0 * 1024.0);
        if (trafficGB >= 1024) {
            return String.format("%.1f TB", trafficGB / 1024);
        } else if (trafficGB >= 1) {
            return String.format("%.1f GB", trafficGB);
        } else {
            return String.format("%.0f MB", totalTraffic / (1024.0 * 1024.0));
        }
    }
    
    public String getFormattedUptime() {
        if (uptime == 0) return "Unknown";
        
        long days = uptime / (24 * 60 * 60 * 1000);
        long hours = (uptime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        
        if (days > 0) {
            return String.format("%d days, %d hours", days, hours);
        } else if (hours > 0) {
            return String.format("%d hours", hours);
        } else {
            return "< 1 hour";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VpnServer vpnServer = (VpnServer) o;
        return Objects.equals(hostname, vpnServer.hostname) &&
               Objects.equals(ipAddress, vpnServer.ipAddress);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hostname, ipAddress);
    }
    
    @Override
    public String toString() {
        return "VpnServer{" +
               "hostname='" + hostname + '\'' +
               ", ipAddress='" + ipAddress + '\'' +
               ", countryLong='" + countryLong + '\'' +
               ", ping=" + ping +
               ", speed=" + speed +
               '}';
    }
}