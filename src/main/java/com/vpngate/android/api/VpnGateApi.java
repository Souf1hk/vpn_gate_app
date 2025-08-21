package com.vpngate.android.api;

import com.vpngate.android.model.VpnServer;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

/**
 * Retrofit API interface for VPN Gate service
 */
public interface VpnGateApi {
    
    /**
     * Get list of VPN servers from VPN Gate
     * The API returns CSV data which we'll parse into VpnServer objects
     */
    @GET("api/iphone/")
    Observable<String> getVpnServersCsv();
    
    /**
     * Get servers filtered by country
     */
    @GET("api/iphone/")
    Observable<String> getVpnServersByCountry(@Query("country") String countryCode);
    
    /**
     * Get public IP information
     */
    @GET("https://api.ipify.org?format=json")
    Observable<IpInfo> getPublicIpInfo();
    
    /**
     * Model for IP information response
     */
    class IpInfo {
        private String ip;
        
        public String getIp() {
            return ip;
        }
        
        public void setIp(String ip) {
            this.ip = ip;
        }
    }
}