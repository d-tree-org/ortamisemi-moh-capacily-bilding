package org.smartregister.chw.actionhelper;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class NewBornCareIntroductionHelper extends HomeVisitActionHelper {

    private Context context;
    private String prematureBaby = "";
    private String jsonString;
    private final boolean firstVisitDone;

    public NewBornCareIntroductionHelper(Context context, boolean firstVisitDone) {
        this.context = context;
        this.firstVisitDone = firstVisitDone;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.jsonString = jsonString;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = org.smartregister.util.JsonFormUtils.fields(jsonObject);

            if (firstVisitDone) {
                org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "first_visit").put(JsonFormConstants.VALUE, "visit_done");
            }
            return jsonObject.toString();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return super.getPreProcessed();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            prematureBaby = JsonFormUtils.getValue(jsonObject, "premature_baby");
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (prematureBaby.isEmpty()) return "";
        return MessageFormat.format("{0}: {1}", context.getString(R.string.is_baby_premature), prematureBaby.equals("yes") ? context.getString(R.string.yes) : context.getString(R.string.no));
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (!firstVisitDone && StringUtils.isEmpty(prematureBaby)) {
            return BaseAncHomeVisitAction.Status.PENDING;
        } else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }
}
