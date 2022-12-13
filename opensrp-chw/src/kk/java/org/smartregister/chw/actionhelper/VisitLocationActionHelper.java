package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;

import timber.log.Timber;

public class VisitLocationActionHelper extends HomeVisitActionHelper {

    private Context context;
    private String gpsLocation;

    public VisitLocationActionHelper(Context context) {
        this.context = context;
    }

    @Override
    public void onPayloadReceived(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = JsonFormUtils.fields(jsonObject);
            gpsLocation = JsonFormUtils.getFieldValue(fields, "gps");

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (gpsLocation != null && !gpsLocation.isEmpty()) {
            return context.getString(R.string.location_captured);
        }else{
            return "";
        }
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(gpsLocation))
            return BaseAncHomeVisitAction.Status.PENDING;
        return BaseAncHomeVisitAction.Status.COMPLETED;
    }

}
