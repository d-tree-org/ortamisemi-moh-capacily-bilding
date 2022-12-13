package org.smartregister.chw.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.R;
import org.smartregister.chw.util.KkSwitchConstants;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-01-24
 */
public class EnvironmentSelectDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String[] listOfEnvironment = new String[]{"Production", "Test"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Select the environment you want to switch to");
        builder.setSingleChoiceItems(R.array.kk_environment, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setSelectedEnvironment(listOfEnvironment[which]);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setDefaultEnvironment();
            }
        });

        return builder.create();
    }

    private void setSelectedEnvironment(String selectedEnvironment) {
        AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
        if (selectedEnvironment.equalsIgnoreCase(KkSwitchConstants.PRODUCTION_ENV)) {
            updateUrl(BuildConfig.opensrp_url_production);
            Objects.requireNonNull(allSharedPreferences.getPreferences()).edit().putString(KkSwitchConstants.KIZAZI_ENVIRONMENT, KkSwitchConstants.PRODUCTION_ENV).apply();
            allSharedPreferences.getPreferences().edit().putBoolean(KkSwitchConstants.IS_PRODUCTION_ENABLED, true).apply();
        } else {
            Objects.requireNonNull(allSharedPreferences.getPreferences()).edit().putString(KkSwitchConstants.KIZAZI_ENVIRONMENT, KkSwitchConstants.TEST_ENV).apply();
            allSharedPreferences.getPreferences().edit().putBoolean(KkSwitchConstants.IS_PRODUCTION_ENABLED, false).apply();
            updateUrl(BuildConfig.opensrp_url_debug);
        }
        restartLoginActivity();

    }

    // To be safe lets set the default environment to Production
    private void setDefaultEnvironment() {
        AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
        updateUrl(BuildConfig.opensrp_url_production);
        Objects.requireNonNull(allSharedPreferences.getPreferences()).edit().putString(KkSwitchConstants.KIZAZI_ENVIRONMENT, KkSwitchConstants.PRODUCTION_ENV).apply();
        allSharedPreferences.getPreferences().edit().putBoolean(KkSwitchConstants.IS_PRODUCTION_ENABLED, true).apply();
        restartLoginActivity();
    }

    private void updateUrl(String baseUrl) {
        try {

            AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();

            URL url = new URL(baseUrl);

            String base = url.getProtocol() + "://" + url.getHost();
            int port = url.getPort();

            Timber.i("Base URL: %s", base);
            Timber.i("Port: %s", port);

            allSharedPreferences.saveHost(base);
            allSharedPreferences.savePort(port);

            Timber.i("Saved URL: %s", allSharedPreferences.fetchHost(""));
            Timber.i("Port: %s", allSharedPreferences.fetchPort(0));
        } catch (MalformedURLException e) {
            Timber.e("Malformed Url: %s", baseUrl);
        }
    }

    private void restartLoginActivity() {
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        requireActivity().overridePendingTransition(0, 0);
        requireActivity().startActivity(intent);
        requireActivity().overridePendingTransition(0, 0);
    }
}
