package org.smartregister.chw.helper;

import android.content.Context;

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

public class ComplimentaryFeedingActionHelper extends HomeVisitActionHelper {

    private Context context;
    private Alert alert;
    private String complementaryFeedingCounselling = "";
    private String jsonPayload;

    public ComplimentaryFeedingActionHelper(Context context, Alert alert){
        this.alert = alert;
        this.context = context;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
        this.jsonPayload = jsonString;
    }

    @Override
    public String getPreProcessed() {
        return super.getPreProcessed();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try{
            JSONObject jsonObject = new JSONObject(jsonPayload);
            complementaryFeedingCounselling = JsonFormUtils.getValue(jsonObject, "comp_feed_counselling_status");
        }catch (Exception e){
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        return MessageFormat.format(context.getString(R.string.counselled_mother_for_comp_feeding)+" : {0}", complementaryFeedingCounselling.equals("yes") ? context.getString(R.string.yes) : context.getString(R.string.no));
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (complementaryFeedingCounselling.equals("yes"))
            return BaseAncHomeVisitAction.Status.COMPLETED;
        else if (complementaryFeedingCounselling.equals("no"))
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        else
            return BaseAncHomeVisitAction.Status.PENDING;
    }

}
