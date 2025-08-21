package com.vpngate.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.vpngate.android.R;
import com.vpngate.android.adapter.ServerListAdapter;
import com.vpngate.android.model.VpnServer;
import com.vpngate.android.repository.VpnServerRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Activity for displaying and selecting VPN servers
 */
public class ServerListActivity extends AppCompatActivity implements ServerListAdapter.OnServerClickListener {
    
    public static final String EXTRA_SELECTED_SERVER = "selected_server";
    
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    
    private ServerListAdapter adapter;
    private VpnServerRepository repository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
        
        initializeComponents();
        setupUI();
        loadServers();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.server_list_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh) {
            refreshServers();
            return true;
        } else if (itemId == R.id.action_sort) {
            showSortDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onServerClick(VpnServer server) {
        // Return selected server to main activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_SERVER, server);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    @Override
    public void onServerFavoriteClick(VpnServer server) {
        if (server.isFavorite()) {
            repository.removeFromFavorites(server);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            repository.addToFavorites(server);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }
    
    private void initializeComponents() {
        repository = VpnServerRepository.getInstance(this);
        
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view);
        progressIndicator = findViewById(R.id.progress_indicator);
        
        adapter = new ServerListAdapter(new ArrayList<>(), this);
    }
    
    private void setupUI() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.server_list_title);
        }
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::refreshServers);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }
    
    private void loadServers() {
        showProgress(true);
        
        Disposable disposable = repository.getVpnServers(false)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::updateServerList,
                this::handleError
            );
        
        compositeDisposable.add(disposable);
    }
    
    private void refreshServers() {
        Disposable disposable = repository.getVpnServers(true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                servers -> {
                    swipeRefresh.setRefreshing(false);
                    updateServerList(servers);
                    Toast.makeText(this, "Servers refreshed", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    swipeRefresh.setRefreshing(false);
                    handleError(error);
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    private void updateServerList(List<VpnServer> servers) {
        showProgress(false);
        adapter.updateServers(servers);
        
        if (servers.isEmpty()) {
            // Show empty state
            Toast.makeText(this, R.string.no_servers_found, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void handleError(Throwable error) {
        showProgress(false);
        swipeRefresh.setRefreshing(false);
        
        String message = "Failed to load servers: " + error.getMessage();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        Timber.e(error, "Error loading servers");
    }
    
    private void showProgress(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showSortDialog() {
        String[] sortOptions = {
            "Ping", "Speed", "Score", "Country", "Sessions", "Uptime"
        };
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.sort_by)
            .setItems(sortOptions, (dialog, which) -> {
                VpnServerRepository.SortCriteria criteria;
                switch (which) {
                    case 0: criteria = VpnServerRepository.SortCriteria.PING; break;
                    case 1: criteria = VpnServerRepository.SortCriteria.SPEED; break;
                    case 2: criteria = VpnServerRepository.SortCriteria.SCORE; break;
                    case 3: criteria = VpnServerRepository.SortCriteria.COUNTRY; break;
                    case 4: criteria = VpnServerRepository.SortCriteria.SESSIONS; break;
                    case 5: criteria = VpnServerRepository.SortCriteria.UPTIME; break;
                    default: criteria = VpnServerRepository.SortCriteria.SCORE; break;
                }
                sortServers(criteria);
            })
            .show();
    }
    
    private void sortServers(VpnServerRepository.SortCriteria criteria) {
        showProgress(true);
        
        Disposable disposable = repository.getServersSorted(criteria)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::updateServerList,
                this::handleError
            );
        
        compositeDisposable.add(disposable);
    }
}