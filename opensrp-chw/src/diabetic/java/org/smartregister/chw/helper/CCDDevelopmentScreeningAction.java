package org.smartregister.chw.helper;

import android.content.Context;

import androidx.room.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.domain.Alert;

import java.text.MessageFormat;

import timber.log.Timber;

public class CCDDevelopmentScreeningAction  extends HomeVisitActionHelper {

    private Context context;
    private Alert alert;
    private String developmentIssuesValue, developmentIssuesKeys;

    public CCDDevelopmentScreeningAction(Context context, Alert alert){
        this.alert = alert;
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try{
            JSONObject jsonObject = new JSONObject(jsonPayload);
            developmentIssuesValue = JsonFormUtils.getCheckBoxValue(jsonObject, "child_development_issues");
            developmentIssuesKeys = JsonFormUtils.getValue(jsonObject, "child_development_issues");
        }catch (Exception e){
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        return MessageFormat.format("{0}: {1}", context.getString(R.string.ccd_development_screening_subtitle), developmentIssuesValue);
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(developmentIssuesKeys)){
            return BaseAncHomeVisitAction.Status.PENDING;
        } else if (StringUtils.isNotBlank(developmentIssuesKeys) && !developmentIssuesKeys.contains("chk_none")) {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }
}
