package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.domain.Alert;

import java.text.MessageFormat;

import timber.log.Timber;

public class NeonatalDangerSignsActionHelper extends HomeVisitActionHelper {
    private Context context;
    private String neoNateDangerSignsValues, neoNateDangerSignsKeys;

    public NeonatalDangerSignsActionHelper(Context context) {
        this.context = context;
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            neoNateDangerSignsKeys = JsonFormUtils.getValue(jsonObject, "neonate_danger_signs");
            neoNateDangerSignsValues = JsonFormUtils.getCheckBoxValue(jsonObject, "neonate_danger_signs");
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        return MessageFormat.format("{0}: {1}", context.getString(R.string.neonatal_danger_signs), neoNateDangerSignsValues);
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(neoNateDangerSignsKeys)) {
            return BaseAncHomeVisitAction.Status.PENDING;
        } else if (neoNateDangerSignsKeys.contains("chk_none")){
            return BaseAncHomeVisitAction.Status.COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        }
    }

}
