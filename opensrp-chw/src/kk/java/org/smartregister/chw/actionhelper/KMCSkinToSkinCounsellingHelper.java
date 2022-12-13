package org.smartregister.chw.actionhelper;

import org.joda.time.LocalDate;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.domain.Alert;

public class KMCSkinToSkinCounsellingHelper extends HomeVisitActionHelper {
    private Alert alert;

    public KMCSkinToSkinCounsellingHelper(Alert alert) {
        this.alert = alert;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        // Do nothing
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return isOverDue() ? BaseAncHomeVisitAction.ScheduleStatus.OVERDUE : BaseAncHomeVisitAction.ScheduleStatus.DUE;  // todo -> return null
    }

    private boolean isOverDue() {
        return new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
    }

    @Override
    public String evaluateSubTitle() {
        return ""; // Not required
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        return BaseAncHomeVisitAction.Status.COMPLETED;
    }
}
