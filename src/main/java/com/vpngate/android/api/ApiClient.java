package com.vpngate.android.api;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import timber.log.Timber;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * API client for VPN Gate services
 */
public class ApiClient {
    
    private static final String VPN_GATE_BASE_URL = "https://www.vpngate.net/";
    private static final String IP_API_BASE_URL = "https://api.ipify.org/";
    
    private static ApiClient instance;
    private final VpnGateApi vpnGateApi;
    private final Context context;
    
    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        
        OkHttpClient okHttpClient = createOkHttpClient();
        
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(VPN_GATE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build();
        
        vpnGateApi = retrofit.create(VpnGateApi.class);
    }
    
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }
    
    public VpnGateApi getVpnGateApi() {
        return vpnGateApi;
    }
    
    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        
        // Set timeouts
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        
        // Add cache
        File cacheDir = new File(context.getCacheDir(), "http_cache");
        Cache cache = new Cache(cacheDir, 10 * 1024 * 1024); // 10MB
        builder.cache(cache);
        
        // Add logging interceptor for debug builds
        if (com.vpngate.android.BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> Timber.d("HTTP: %s", message)
            );
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(loggingInterceptor);
        }
        
        // Add user agent interceptor
        builder.addInterceptor(chain -> {
            return chain.proceed(
                chain.request().newBuilder()
                    .addHeader("User-Agent", "VPN Gate Android Client")
                    .build()
            );
        });
        
        return builder.build();
    }
    
    private Gson createGson() {
        return new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    }
}