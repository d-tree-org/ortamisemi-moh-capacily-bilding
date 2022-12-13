package org.smartregister.chw.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.smartregister.AllConstants;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.R;
import org.smartregister.chw.fragment.EnvironmentSelectDialogFragment;
import org.smartregister.chw.util.KkSwitchConstants;
import org.smartregister.repository.AllSharedPreferences;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-01-20
 */
public class KizaziLoginActivity extends LoginActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setServerUrl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(getString(R.string.switch_environment));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.switch_environment))) {
            if (hasPermissions()) {
                this.startActivity(new Intent(this, KkEnvironmentSwitchingActivity.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setServerUrl() {

        try {

            AllSharedPreferences sharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
            // Check if the preferences for the environment have been set yet
            if (!sharedPreferences.getPreference(KkSwitchConstants.KIZAZI_ENVIRONMENT).isEmpty()) {
                if (sharedPreferences.getBooleanPreference("enable_production")) {
                    updateEnvironmentUrl(BuildConfig.opensrp_url_production);
                    setTestEnvironmentIndicator(false);
                    updateSyncFilter();
                } else {
                    updateEnvironmentUrl(BuildConfig.opensrp_url_debug);
                    setTestEnvironmentIndicator(true);
                }
            } else {
                EnvironmentSelectDialogFragment switchFrag = new EnvironmentSelectDialogFragment();
                switchFrag.show(this.getSupportFragmentManager(), "switch_env");
                Toast.makeText(this, "Environment not configured yet, please choose environment ...", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateSyncFilter(){

    }

    private void updateEnvironmentUrl(String baseUrl) {
        try {

            AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();

            URL url = new URL(baseUrl);

            String base = url.getProtocol() + "://" + url.getHost();
            int port = url.getPort();

            Timber.i("Base URL: %s", base);
            Timber.i("Port: %s", port);

            allSharedPreferences.saveHost(base);
            allSharedPreferences.savePort(port);

            allSharedPreferences.savePreference(AllConstants.DRISHTI_BASE_URL, baseUrl);

            Timber.i("Saved URL: %s", allSharedPreferences.fetchHost(""));
            Timber.i("Port: %s", allSharedPreferences.fetchPort(0));
        } catch (MalformedURLException e) {
            Timber.e("Malformed Url: %s", baseUrl);
        }
    }

    private void setTestEnvironmentIndicator(boolean isTest) {
        RelativeLayout environmentIndicator = findViewById(R.id.environment_indicator);
        if (isTest)
            environmentIndicator.setVisibility(View.VISIBLE);
        else
            environmentIndicator.setVisibility(View.GONE);
    }

}
