package com.vpngate.android.repository;

import android.content.Context;
import android.util.Base64;
import com.vpngate.android.api.ApiClient;
import com.vpngate.android.api.VpnGateApi;
import com.vpngate.android.model.VpnServer;
import com.vpngate.android.util.PreferenceManager;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Repository for managing VPN server data
 */
public class VpnServerRepository {
    
    private static VpnServerRepository instance;
    private final VpnGateApi api;
    private final PreferenceManager preferenceManager;
    private final List<VpnServer> cachedServers;
    private long lastUpdateTime;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    
    private VpnServerRepository(Context context) {
        api = ApiClient.getInstance(context).getVpnGateApi();
        preferenceManager = new PreferenceManager(context);
        cachedServers = new ArrayList<>();
        lastUpdateTime = 0;
    }
    
    public static synchronized VpnServerRepository getInstance(Context context) {
        if (instance == null) {
            instance = new VpnServerRepository(context);
        }
        return instance;
    }
    
    /**
     * Get VPN servers with caching
     */
    public Observable<List<VpnServer>> getVpnServers(boolean forceRefresh) {
        if (!forceRefresh && isCacheValid()) {
            return Observable.just(new ArrayList<>(cachedServers));
        }
        
        return api.getVpnServersCsv()
            .subscribeOn(Schedulers.io())
            .map(this::parseCsvToServers)
            .doOnNext(servers -> {
                cachedServers.clear();
                cachedServers.addAll(servers);
                lastUpdateTime = System.currentTimeMillis();
                Timber.d("Updated server cache with %d servers", servers.size());
            })
            .doOnError(error -> Timber.e(error, "Error fetching VPN servers"));
    }
    
    /**
     * Get servers filtered by country
     */
    public Observable<List<VpnServer>> getServersByCountry(String countryCode) {
        return getVpnServers(false)
            .map(servers -> {
                List<VpnServer> filtered = new ArrayList<>();
                for (VpnServer server : servers) {
                    if (countryCode.equalsIgnoreCase(server.getCountryShort())) {
                        filtered.add(server);
                    }
                }
                return filtered;
            });
    }
    
    /**
     * Get servers sorted by various criteria
     */
    public Observable<List<VpnServer>> getServersSorted(SortCriteria criteria) {
        return getVpnServers(false)
            .map(servers -> {
                List<VpnServer> sorted = new ArrayList<>(servers);
                switch (criteria) {
                    case PING:
                        sorted.sort((a, b) -> Integer.compare(a.getPing(), b.getPing()));
                        break;
                    case SPEED:
                        sorted.sort((a, b) -> Long.compare(b.getSpeed(), a.getSpeed()));
                        break;
                    case SCORE:
                        sorted.sort((a, b) -> Long.compare(b.getScore(), a.getScore()));
                        break;
                    case COUNTRY:
                        sorted.sort((a, b) -> a.getCountryLong().compareToIgnoreCase(b.getCountryLong()));
                        break;
                    case SESSIONS:
                        sorted.sort((a, b) -> Integer.compare(a.getNumVpnSessions(), b.getNumVpnSessions()));
                        break;
                    case UPTIME:
                        sorted.sort((a, b) -> Long.compare(b.getUptime(), a.getUptime()));
                        break;
                }
                return sorted;
            });
    }
    
    /**
     * Get best server based on criteria
     */
    public Single<VpnServer> getBestServer() {
        return getVpnServers(false)
            .map(servers -> {
                if (servers.isEmpty()) {
                    throw new RuntimeException("No servers available");
                }
                
                // Filter out servers with high ping or low speed
                List<VpnServer> goodServers = new ArrayList<>();
                for (VpnServer server : servers) {
                    if (server.getPing() > 0 && server.getPing() < 300 && 
                        server.getSpeed() > 1_000_000) { // > 1 Mbps
                        goodServers.add(server);
                    }
                }
                
                if (goodServers.isEmpty()) {
                    goodServers = servers; // Fallback to all servers
                }
                
                // Sort by score (combination of ping, speed, and other factors)
                goodServers.sort((a, b) -> Long.compare(b.getScore(), a.getScore()));
                
                return goodServers.get(0);
            })
            .firstOrError();
    }
    
    /**
     * Search servers by hostname or country
     */
    public Observable<List<VpnServer>> searchServers(String query) {
        return getVpnServers(false)
            .map(servers -> {
                List<VpnServer> results = new ArrayList<>();
                String lowerQuery = query.toLowerCase();
                
                for (VpnServer server : servers) {
                    if (server.getHostname().toLowerCase().contains(lowerQuery) ||
                        server.getCountryLong().toLowerCase().contains(lowerQuery) ||
                        server.getIpAddress().contains(query)) {
                        results.add(server);
                    }
                }
                
                return results;
            });
    }
    
    /**
     * Get favorite servers
     */
    public List<VpnServer> getFavoriteServers() {
        Set<String> favoriteIds = preferenceManager.getFavoriteServers();
        List<VpnServer> favorites = new ArrayList<>();
        
        for (VpnServer server : cachedServers) {
            String serverId = server.getHostname() + "_" + server.getIpAddress();
            if (favoriteIds.contains(serverId)) {
                server.setFavorite(true);
                favorites.add(server);
            }
        }
        
        return favorites;
    }
    
    /**
     * Add server to favorites
     */
    public void addToFavorites(VpnServer server) {
        String serverId = server.getHostname() + "_" + server.getIpAddress();
        preferenceManager.addFavoriteServer(serverId);
        server.setFavorite(true);
    }
    
    /**
     * Remove server from favorites
     */
    public void removeFromFavorites(VpnServer server) {
        String serverId = server.getHostname() + "_" + server.getIpAddress();
        preferenceManager.removeFavoriteServer(serverId);
        server.setFavorite(false);
    }
    
    /**
     * Parse CSV data from VPN Gate API into VpnServer objects
     */
    private List<VpnServer> parseCsvToServers(String csvData) {
        List<VpnServer> servers = new ArrayList<>();
        
        try {
            String[] lines = csvData.split("\\n");
            
            // Skip header lines (first 2 lines are comments, 3rd is header)
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.startsWith("*") || line.startsWith("#")) {
                    continue;
                }
                
                try {
                    VpnServer server = parseCsvLine(line);
                    if (server != null && isValidServer(server)) {
                        servers.add(server);
                    }
                } catch (Exception e) {
                    Timber.w(e, "Error parsing CSV line: %s", line);
                }
            }
            
            Timber.i("Parsed %d servers from CSV data", servers.size());
            
        } catch (Exception e) {
            Timber.e(e, "Error parsing CSV data");
        }
        
        return servers;
    }
    
    /**
     * Parse a single CSV line into a VpnServer object
     */
    private VpnServer parseCsvLine(String line) {
        String[] fields = line.split(",");
        
        if (fields.length < 15) {
            return null; // Invalid line
        }
        
        try {
            VpnServer server = new VpnServer();
            
            server.setHostname(fields[0]);
            server.setIpAddress(fields[1]);
            server.setScore(Long.parseLong(fields[2]));
            server.setPing(parseIntSafe(fields[3]));
            server.setSpeed(Long.parseLong(fields[4]));
            server.setCountryLong(fields[5]);
            server.setCountryShort(fields[6]);
            server.setNumVpnSessions(parseIntSafe(fields[7]));
            server.setUptime(Long.parseLong(fields[8]));
            server.setTotalUsers(parseIntSafe(fields[9]));
            server.setTotalTraffic(Long.parseLong(fields[10]));
            server.setLogType(fields[11]);
            server.setOperator(fields[12]);
            server.setMessage(fields[13]);
            server.setOpenVpnConfigDataBase64(fields[14]);
            
            return server;
            
        } catch (Exception e) {
            Timber.w(e, "Error parsing server data from line: %s", line);
            return null;
        }
    }
    
    /**
     * Validate server data
     */
    private boolean isValidServer(VpnServer server) {
        return server.getHostname() != null && !server.getHostname().isEmpty() &&
               server.getIpAddress() != null && !server.getIpAddress().isEmpty() &&
               server.getOpenVpnConfigDataBase64() != null && !server.getOpenVpnConfigDataBase64().isEmpty() &&
               server.getCountryShort() != null && !server.getCountryShort().isEmpty();
    }
    
    /**
     * Check if cache is still valid
     */
    private boolean isCacheValid() {
        return !cachedServers.isEmpty() && 
               (System.currentTimeMillis() - lastUpdateTime) < CACHE_DURATION;
    }
    
    /**
     * Parse integer safely
     */
    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Get OpenVPN config from base64 data
     */
    public String getOpenVpnConfig(VpnServer server) {
        try {
            byte[] configData = Base64.decode(server.getOpenVpnConfigDataBase64(), Base64.DEFAULT);
            return new String(configData);
        } catch (Exception e) {
            Timber.e(e, "Error decoding OpenVPN config");
            return null;
        }
    }
    
    /**
     * Enum for sorting criteria
     */
    public enum SortCriteria {
        PING, SPEED, SCORE, COUNTRY, SESSIONS, UPTIME
    }
}