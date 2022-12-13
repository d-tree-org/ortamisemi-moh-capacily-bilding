package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-03-21
 */
public class PlayAssessmentCounselingActionHelper extends HomeVisitActionHelper {

    private Context context;
    private String interaction_with_baby;
    private String play_with_child;
    private String jsonString;
    private ServiceWrapper serviceWrapper;

    private boolean visit_3_visit_5 =  false;
    private boolean visit_6_visit_12 = false;

    public PlayAssessmentCounselingActionHelper(ServiceWrapper serviceWrapper) {
        this.serviceWrapper = serviceWrapper;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.jsonString = jsonString;
        this.context = context;
        super.onJsonFormLoaded(jsonString, context, details);
    }

    @Override
    public String getPreProcessed() {

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            if (serviceWrapper != null) {
                String servicePronoun = getPeriodNoun(serviceWrapper.getName());
                int period = getPeriod(serviceWrapper.getName());
                if ("days".equalsIgnoreCase(servicePronoun)) {
                    if (period >= 8) {
                        visit_3_visit_5 = true;
                    }
                } else if ("weeks".equalsIgnoreCase(servicePronoun)) {
                    if (period <= 5) {
                        visit_3_visit_5 = true;
                    }
                } else if ("months".equalsIgnoreCase(servicePronoun)) {
                    if (period >= 2) {
                        visit_6_visit_12 = true;
                    }
                }
            }

            if (visit_3_visit_5) {
                JsonFormUtils.getFieldJSONObject(fields, "visit_3_visit_5").put("value", "true");
            }

            if (visit_6_visit_12) {
                JsonFormUtils.getFieldJSONObject(fields, "visit_6_visit_12").put("value", "true");
            }

            return jsonObject.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }
        return super.getPreProcessed();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            interaction_with_baby = JsonFormUtils.getValue(jsonObject, "interaction_with_baby");
            play_with_child = JsonFormUtils.getValue(jsonObject, "play_with_child");

        } catch (JSONException e) {
            Timber.e(e);
        }

    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (visit_3_visit_5) {
            if (StringUtils.isBlank(interaction_with_baby)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else if (interaction_with_baby.contains("move_baby_arms_legs_stroke_gently") || interaction_with_baby.contains("get_baby_attention_shaker_toy")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        } else if (visit_6_visit_12) {
            if (StringUtils.isBlank(play_with_child)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else if (play_with_child.equalsIgnoreCase("Yes")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        } else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }

    }

    private String getPeriodNoun(String serviceName) {
        String[] nameSplit = serviceName.split(" ");
        return nameSplit[nameSplit.length -1];
    }

    private int getPeriod(String serviceName) {
        String[] nameSplit = serviceName.split(" ");
        String periodString = nameSplit[nameSplit.length -2];

        return Integer.parseInt(periodString);
    }
}
