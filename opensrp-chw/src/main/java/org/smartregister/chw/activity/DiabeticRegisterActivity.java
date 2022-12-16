package org.smartregister.chw.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.listener.CoreBottomNavigationListener;
import org.smartregister.chw.fragment.DiabeticRegisterFragment;
import org.smartregister.chw.presenter.DiabeticRegisterPresenter;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.contract.BaseRegisterContract;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DiabeticRegisterActivity extends BaseRegisterActivity implements BaseRegisterContract.View {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(R.id.action_register);
            bottomNavigationView.getMenu().removeItem(R.id.action_search);
            bottomNavigationView.getMenu().removeItem(R.id.action_library);

            bottomNavigationView.inflateMenu(R.menu.bottom_nav_family_menu);

            bottomNavigationHelper.disableShiftMode(bottomNavigationView);

            CoreBottomNavigationListener childBottomNavigationListener = new CoreBottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(childBottomNavigationListener);

        }
    }

    @Override
    protected void initializePresenter() {
        presenter = new DiabeticRegisterPresenter();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new DiabeticRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    public void startFormActivity(String formName, String entityId, Map<String, String> metaData) {

    }

    @Override
    public void startFormActivity(JSONObject form) {

    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(Utils.metadata().familyRegister.config);
    }

    @Override
    public void startRegistration() {

    }
}
