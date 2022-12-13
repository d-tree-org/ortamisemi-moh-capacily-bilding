package org.smartregister.chw.actionhelper;


import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-03-28
 */
public class CareGiverResponsivenessActionHelper extends HomeVisitActionHelper {
    private String caregiver_interacts_with_child;
    private String caregiver_comfort_child;
    private String caregiver_response_cue;
    private Alert alert;

    public CareGiverResponsivenessActionHelper(Alert alert) {
        this.alert = alert;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        super.onJsonFormLoaded(jsonString, context, details);
    }

    @Override
    public void onPayloadReceived(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            caregiver_interacts_with_child = JsonFormUtils.getFieldValue(fields, "caregiver_interacts_with_child");
            caregiver_comfort_child = JsonFormUtils.getFieldValue(fields, "caregiver_comfort_child");
            caregiver_response_cue = JsonFormUtils.getFieldValue(fields, "caregiver_response_cue");

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
        String CAREGIVER_INTERACTING_WITH_CHILD = "Moves toward and with child, and talks to or makes sounds with child, responds to child";
        String CAREGIVER_RESPONDS_TO_CUES = "Looks into child’s eyes and talks softly to child, gently touches child or holds child closely";
        if (StringUtils.isBlank(caregiver_comfort_child) || StringUtils.isBlank(caregiver_interacts_with_child) || StringUtils.isBlank(caregiver_response_cue)) {
            return BaseAncHomeVisitAction.Status.PENDING;
        } else if (CAREGIVER_INTERACTING_WITH_CHILD.equalsIgnoreCase(caregiver_interacts_with_child) &&
                "Yes".equalsIgnoreCase(caregiver_comfort_child) && CAREGIVER_RESPONDS_TO_CUES.equalsIgnoreCase(caregiver_response_cue)) {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        }
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return isOverDue() ? BaseAncHomeVisitAction.ScheduleStatus.OVERDUE : BaseAncHomeVisitAction.ScheduleStatus.DUE;
    }

    private boolean isOverDue() {
        return new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
    }
}
