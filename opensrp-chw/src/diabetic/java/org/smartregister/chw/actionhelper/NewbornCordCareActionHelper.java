package org.smartregister.chw.actionhelper;

import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;

/**
 * Created by Kassim Sheghembe on 2022-03-31
 */
public class NewbornCordCareActionHelper extends HomeVisitActionHelper {
    @Override
    public void onPayloadReceived(String s) {

    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        return BaseAncHomeVisitAction.Status.COMPLETED;
    }
}
