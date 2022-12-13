package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.util.JsonFormUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * @author issyzac 5/30/22
 */
public class PncInfectionPreventionControlActionHelper extends HomeVisitActionHelper {

    private String jsonString;
    private Map<String, List<VisitDetail>> details;
    private String hasLatrine;
    private String hasElectricity;

    public PncInfectionPreventionControlActionHelper() {
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.jsonString = jsonString;
        this.details = details;
        this.context = context;
    }

    @Override
    public void onPayloadReceived(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            hasLatrine = JsonFormUtils.getCheckBoxValue(jsonObject, "latrine_household");
            hasElectricity = JsonFormUtils.getValue(jsonObject, "electricity_home");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (StringUtils.isNotBlank(hasElectricity)) {
            String electricityStatus = MessageFormat.format("{0}: {1}", context.getString(R.string.pnc_hv_ipc_has_electricity), hasElectricity.equalsIgnoreCase("yes") ? context.getString(R.string.yes) : context.getString(R.string.no));
            String latrineStatus = MessageFormat.format("{0}: {1}", context.getString(R.string.pnc_hv_ipc_has_latrine), hasLatrine);
            return MessageFormat.format("{0} \n{1}", electricityStatus, latrineStatus);
        }
        return null;
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (hasLatrine != null && hasElectricity != null) {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }else{
            return BaseAncHomeVisitAction.Status.PENDING;
        }
    }
}
