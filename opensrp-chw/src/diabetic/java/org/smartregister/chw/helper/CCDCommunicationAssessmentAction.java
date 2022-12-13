package org.smartregister.chw.helper;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.domain.Alert;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class CCDCommunicationAssessmentAction extends HomeVisitActionHelper {

    private Context context;
    private final Alert alert;
    private String communicatesWithChild = "";
    private String communicatesWithChildObservation = "";
    private String jsonPayload;
    private final int ageInMonth;

    public CCDCommunicationAssessmentAction(Context context, Alert alert, int ageInMonth) {
        this.alert = alert;
        this.context = context;
        this.ageInMonth = ageInMonth;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
        this.jsonPayload = jsonString;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            JSONObject child_age_in_month = JsonFormUtils.getFieldJSONObject(fields, "child_age_in_months");
            assert child_age_in_month != null;
            child_age_in_month.remove(JsonFormConstants.VALUE);
            child_age_in_month.put(JsonFormConstants.VALUE, ageInMonth);

            return jsonObject.toString();

        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            communicatesWithChild = JsonFormUtils.getValue(jsonObject, "communication_with_child");
            communicatesWithChildObservation = JsonFormUtils.getValue(jsonObject, "child_communication_observation");
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (communicatesWithChild.isEmpty()) return "";
        return MessageFormat.format("{0}: {1}",context.getString(R.string.ccd_communication_assessment_subtitle),communicatesWithChild.equals("yes") ? context.getString(R.string.yes) : context.getString(R.string.no));
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {

        if (communicatesWithChild.equalsIgnoreCase("yes") &&
                communicatesWithChildObservation.contains("chk_force_smile")) {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        } else if (communicatesWithChild.equals("yes") && !StringUtils.isEmpty(communicatesWithChild)) {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.PENDING;
        }
    }
}
