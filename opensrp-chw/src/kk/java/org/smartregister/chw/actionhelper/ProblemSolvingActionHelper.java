package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.domain.Alert;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-03-23
 */
public class ProblemSolvingActionHelper extends HomeVisitActionHelper {

    private String experience_challenges_playing_communicating = "";
    private Alert alert;
    private Context context;

    public ProblemSolvingActionHelper(Alert alert, Context context) {
        this.alert = alert;
        this.context = context;
    }

    @Override
    public void onPayloadReceived(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            experience_challenges_playing_communicating = JsonFormUtils.getFieldValue(fields, "experience_challenges_playing_communicating");

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (experience_challenges_playing_communicating.isEmpty()) return "";
        return experience_challenges_playing_communicating.equalsIgnoreCase("Yes") ? getContext().getString(R.string.yes) : getContext().getString(R.string.no) ;
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(experience_challenges_playing_communicating))
            return BaseAncHomeVisitAction.Status.PENDING;
        return experience_challenges_playing_communicating.equalsIgnoreCase("Yes") ? BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED : BaseAncHomeVisitAction.Status.COMPLETED;
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return isOverDue() ? BaseAncHomeVisitAction.ScheduleStatus.OVERDUE : BaseAncHomeVisitAction.ScheduleStatus.DUE;
    }

    private boolean isOverDue() {
        return new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
    }
}
