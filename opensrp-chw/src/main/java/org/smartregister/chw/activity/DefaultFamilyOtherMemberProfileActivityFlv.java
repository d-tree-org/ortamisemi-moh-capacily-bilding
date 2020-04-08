package org.smartregister.chw.activity;

import android.app.Activity;
import android.view.Menu;

import org.smartregister.chw.R;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public abstract class DefaultFamilyOtherMemberProfileActivityFlv implements FamilyOtherMemberProfileActivity.Flavor {

    public OnClickFloatingMenu getOnClickFloatingMenu(final Activity activity, final String familyBaseEntityId) {
        return viewId -> {
            if (viewId == R.id.fab) {
                FamilyCallDialogFragment.launchDialog(activity, familyBaseEntityId);
            }
        };
    }

    @Override
    public boolean isOfReproductiveAge(CommonPersonObjectClient commonPersonObject, String gender) {
        if (gender.equalsIgnoreCase("Female")) {
            return Utils.isMemberOfReproductiveAge(commonPersonObject, 10, 49);
        } else if (gender.equalsIgnoreCase("Male")) {
            return Utils.isMemberOfReproductiveAge(commonPersonObject, 15, 49);
        } else {
            return false;
        }
    }

    @Override
    public void updateFpMenuItems(String baseEntityId, Menu menu) {
//        TODO implement if wcaro would need fp module
    }

    @Override
    public void updateMalariaMenuItems(String baseEntityId, Menu menu) {
//        TODO implement if wcaro would need malaria module
    }

    @Override
    public boolean hasANC() {
        return true;
    }
}