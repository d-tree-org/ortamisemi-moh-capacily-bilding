package org.smartregister.chw.actionhelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.util.JsonFormUtils;

import java.text.MessageFormat;

import timber.log.Timber;

public class MalnutritionScreeningActionHelper extends HomeVisitActionHelper {

    String growthMonitoringSelectedKey = "";
    String growthMonitoringSelectedValue = "";
    String palmPallorValue = "";
    String palmPallorKey= "";
    String childGrowthMuacKey = "";
    String childGrowthMuacValue = "";

    @Override
    public void onPayloadReceived(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            growthMonitoringSelectedKey = JsonFormUtils.getValue(jsonObject, "growth_monitoring");
            growthMonitoringSelectedValue = JsonFormUtils.getCheckBoxValue(jsonObject, "growth_monitoring");
            palmPallorKey = JsonFormUtils.getValue(jsonObject, "palm_pallor");
            childGrowthMuacKey = JsonFormUtils.getValue(jsonObject, "child_growth_muac");
        }catch (JSONException e){
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (growthMonitoringSelectedKey.isEmpty()) return "";

        if (palmPallorKey.contains("palm_pallor_no")) {
            palmPallorValue = context.getString(R.string.no);
        }

        if (palmPallorKey.contains("palm_pallor_yes")) {
            palmPallorValue = context.getString(R.string.yes);
        }

        if (childGrowthMuacKey.contains("Red")) {
            childGrowthMuacValue = context.getString(R.string.palm_pallor_red);
        }

        if (childGrowthMuacKey.contains("Green")) {
            childGrowthMuacValue = context.getString(R.string.palm_pallor_green);
        }

        if (childGrowthMuacKey.contains("Yellow")) {
            childGrowthMuacValue = context.getString(R.string.palm_pallor_yellow);
        }

        return getContext().getString(R.string.malnutrition_screening_subtitle, childGrowthMuacValue, palmPallorValue);
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {

        if (growthMonitoringSelectedKey.isEmpty() || palmPallorKey.isEmpty()) {
            return BaseAncHomeVisitAction.Status.PENDING;
        } else if (childGrowthMuacKey.contains("Red") && palmPallorKey.contains("palm_pallor_yes")) {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }
}