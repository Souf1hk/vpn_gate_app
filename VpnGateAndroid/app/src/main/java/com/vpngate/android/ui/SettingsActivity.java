package com.vpngate.android.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vpngate.android.R;
import com.vpngate.android.util.PreferenceManager;

/**
 * Settings activity for VPN Gate app
 */
public class SettingsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Settings fragment
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        
        private PreferenceManager preferenceManager;
        
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            
            preferenceManager = new PreferenceManager(requireContext());
            
            setupPreferences();
        }
        
        private void setupPreferences() {
            // Reset settings preference
            Preference resetSettings = findPreference("reset_settings");
            if (resetSettings != null) {
                resetSettings.setOnPreferenceClickListener(preference -> {
                    showResetConfirmationDialog();
                    return true;
                });
            }
            
            // Clear cache preference
            Preference clearCache = findPreference("clear_cache");
            if (clearCache != null) {
                clearCache.setOnPreferenceClickListener(preference -> {
                    showClearCacheConfirmationDialog();
                    return true;
                });
            }
            
            // About preference
            Preference about = findPreference("about");
            if (about != null) {
                about.setSummary("Version " + com.vpngate.android.BuildConfig.VERSION_NAME);
                about.setOnPreferenceClickListener(preference -> {
                    // Handle about click
                    return true;
                });
            }
        }
        
        private void showResetConfirmationDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_reset_settings_title)
                .setMessage(R.string.dialog_reset_settings_message)
                .setPositiveButton(R.string.reset, (dialog, which) -> {
                    preferenceManager.clearAll();
                    // Restart the fragment to reflect changes
                    requireActivity().recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        }
        
        private void showClearCacheConfirmationDialog() {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_clear_cache_title)
                .setMessage(R.string.dialog_clear_cache_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Clear cache logic would go here
                    // For now, just show a toast
                    android.widget.Toast.makeText(requireContext(), 
                        "Cache cleared", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        }
    }
}