package org.smartregister.chw.interactor;

import android.content.Context;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.PncDangerSignsActionHelper;
import org.smartregister.chw.actionhelper.PncInfectionPreventionControlActionHelper;
import org.smartregister.chw.actionhelper.VisitLocationActionHelper;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.rule.PncVisitAlertRule;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.core.utils.RecurringServiceUtil;
import org.smartregister.chw.pnc.PncLibrary;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.KkConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class PncHomeVisitInteractorFlv extends DefaultPncHomeVisitInteractorFlv {

    private Date lastVisitDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private PncVisitAlertRule visitSummary;


    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        context = view.getContext();
        actionList = new LinkedHashMap<>();
        this.memberObject = memberObject;
        editMode = view.getEditMode();
        this.view = view;

        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.PNC_HOME_VISIT);
            if (lastVisit != null) {
                details = Collections.unmodifiableMap(VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId())));
            }
        }

        visitSummary = getVisitSummary(memberObject.getBaseEntityId());

        try {
            evaluateVisitLocation();
            evaluateDangerSignsMother(visitSummary);
            evaluateMaternalNutrition(visitSummary);
            evaluateHIVGeneralInfo(visitSummary);
            evaluateLAM(visitSummary);
            evaluatePostpartumMotherCare(visitSummary);
            evaluatePostpartumFamilyPlanning(visitSummary);
            evaluateFollowupHEI(visitSummary);
            evaluatePostpartumPhysiologicalChanges(visitSummary);
            evaluateInfectionPreventionControl(visitSummary);
            evaluateMalariaPrevention(visitSummary);

        } catch (BaseAncHomeVisitAction.ValidationException e) {
            Timber.e(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return actionList;
    }

    @Override
    public void addExtraObs(Event baseEvent) {
        try {
            String visitNumber = getPncVisitNumber(visitSummary.getVisitID());
            if (visitNumber != null) {
                List<Object> visitNumberObsValue = new ArrayList<>();
                visitNumberObsValue.add(visitNumber);
                baseEvent.addObs(new Obs("concept", "text", "visit_number", "",
                        visitNumberObsValue, new ArrayList<>(), null, "visit_number"));
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private String getPncVisitNumber(String visitID) {

        switch (visitID) {

            case "1":
                return "1";
            case "3":
                return "2";
            case "8":
                return "3";
            case "21":
                return "4";
            case "35":
                return "5";
            default:
                return null;
        }

    }

    private void evaluateVisitLocation() throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.visit_location))
                .withOptional(false)
                .withFormName("hv_visit_location")
                .withHelper(new VisitLocationActionHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .build();
        actionList.put(context.getString(R.string.visit_location), action);
    }

    private void evaluateInfectionPreventionControl(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitID = visitSummary.getVisitID();

        if (visitID == null || visitID.equalsIgnoreCase("3") || visitID.equalsIgnoreCase("8") ||
                visitID.equalsIgnoreCase("21")) return;

        String title = context.getString(R.string.infection_prevention_control);

        BaseAncHomeVisitAction infectionPreventionControlAction = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withHelper(new PncInfectionPreventionControlActionHelper())
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncHvInfectionPreventionControl())
                .build();

        actionList.put(title, infectionPreventionControlAction);

    }

    private void evaluatePostpartumPhysiologicalChanges(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitID = visitSummary.getVisitID();

        if (visitID == null || visitID.equalsIgnoreCase("8") || visitID.equalsIgnoreCase("21")
                || visitID.equalsIgnoreCase("35") ) return;

        String title = context.getString(R.string.postpartum_psychological_changes);

        BaseAncHomeVisitAction ppPhysiologicalChangesAction = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncHvPostpartumPhysiologicalChanges())
                .build();

        actionList.put(title, ppPhysiologicalChangesAction);

    }

    private void evaluateFollowupHEI(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitID = visitSummary.getVisitID();

        if (visitID == null || visitID.equalsIgnoreCase("1")
                || visitID.equalsIgnoreCase("8")) return;

        String title = context.getString(R.string.follow_up_hiv_exposed_infant);

        BaseAncHomeVisitAction followupHivExposedInfantAction = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getFollowUpHivExposedInfant())
                .build();

        actionList.put(title, followupHivExposedInfantAction);

    }

    private void evaluatePostpartumFamilyPlanning(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitID = visitSummary.getVisitID();

        if (visitID == null || visitID.equalsIgnoreCase("1") || visitID.equalsIgnoreCase("3") ||
                visitID.equalsIgnoreCase("8")) return;

        String title = context.getString(R.string.pnc_postpartum_family_planning);

        BaseAncHomeVisitAction postpartumFamilyPlanning = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncHvPostpartumFamilyPlanning())
                .build();

        actionList.put(title, postpartumFamilyPlanning);

    }

    private void evaluatePostpartumMotherCare(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

            String visitID = visitID = visitSummary.getVisitID();

            if (visitID == null || visitID.equalsIgnoreCase("21") || visitID.equalsIgnoreCase("35")) return;

            String title = context.getString(R.string.pnc_postpartum_mother_care);

            BaseAncHomeVisitAction postpartumMotherCareAction = getBuilder(title)
                    .withOptional(false)
                    .withDetails(details)
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                    .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncMotherCare())
                    .build();

            actionList.put(title, postpartumMotherCareAction);

        }

    private void evaluateMaternalNutrition(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitID = visitSummary.getVisitID();


        if (visitID == null || visitID.equalsIgnoreCase("3") || visitID.equalsIgnoreCase("35")) return;


        String title = context.getString(R.string.maternal_nutrition);

        BaseAncHomeVisitAction action = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncHvMaternalNutrition())
                .build();

        actionList.put(title, action);

    }

    private Date getPncDeliveryDate() {
        return PNCDao.getPNCDeliveryDate(memberObject.getBaseEntityId());
    }

    private void evaluateDangerSignsMother(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitID = visitSummary.getVisitID();

        if (visitID == null || visitID.equalsIgnoreCase("21") || visitID.equalsIgnoreCase("35")) return;

        String title = context.getString(R.string.postpartum_danger_signs);

        BaseAncHomeVisitAction action = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncHvDangerSigns())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withHelper(new PncDangerSignsActionHelper())
                .build();

        actionList.put(title, action);
    }

    private void evaluateHIVGeneralInfo(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID  = visitSummary.getVisitID();

        if (visitID.equalsIgnoreCase("1")) {

            String title = context.getString(R.string.pnc_hiv_aids_general_info);

            BaseAncHomeVisitAction action = getBuilder(title)
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncHvHivAidsGeneralInfo())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                    .build();

            actionList.put(title, action);
        }
    }

    private void evaluateLAM(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitSummary.getVisitID();

        if (visitID == null || visitID.equalsIgnoreCase("1") ||
                visitID.equalsIgnoreCase("3") || visitID.equalsIgnoreCase("8")) return;

        String title = context.getString(R.string.pnc_hv_lam);

        BaseAncHomeVisitAction action = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncHvLam())
                .build();

        actionList.put(title, action);

    }

    private void evaluateMalariaPrevention(PncVisitAlertRule visitSummary) throws BaseAncHomeVisitAction.ValidationException {

        String visitID = visitSummary.getVisitID();

        if (visitID == null || visitID.equalsIgnoreCase("3") ||
                visitID.equalsIgnoreCase("21")) return;

        String title = context.getString(R.string.pnc_hv_malaria_prevention_action_title);
        BaseAncHomeVisitAction action = getBuilder(title)
                .withOptional(false)
                .withDetails(details)
                .withFormName(KkConstants.KKJSON_FORM_CONSTANT.KK_PNC_HOME_VISIT.getPncMalariaPrevention())
                .withHelper(new PncMalariaPreventionHelper())
                .build();

        actionList.put(title, action);

    }

    private PncVisitAlertRule getVisitSummary(String motherBaseID) {
        Rules rules = ChwApplication.getInstance().getRulesEngineHelper().rules(org.smartregister.chw.util.Constants.RULE_FILE.PNC_HOME_VISIT);
        Date lastVisitDate = getLastDateVisit(motherBaseID);

        return HomeVisitUtil.getPncVisitStatus(rules, lastVisitDate, getPncDeliveryDate());
    }

    private Date getLastDateVisit(String motherBaseID) {
        Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(motherBaseID, org.smartregister.chw.anc.util.Constants.EVENT_TYPE.PNC_HOME_VISIT);
        if (lastVisit != null) {
            lastVisitDate = lastVisit.getDate();
            return lastVisitDate;
        } else {
            return lastVisitDate = getDeliveryDate(motherBaseID);
        }
    }

    @Nullable
    private Date getDeliveryDate(String motherBaseID) {
        try {
            String deliveryDateString = PncLibrary.getInstance().profileRepository().getDeliveryDate(motherBaseID);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.parse(deliveryDateString);

        } catch (ParseException e) {
            Timber.e(e);
        }
        return null;
    }

    private class PncMalariaPreventionHelper implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        private String malaria_protective_measures;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {

        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                malaria_protective_measures = JsonFormUtils.getValue(jsonObject, "malaria_protective_measures");
            } catch (JSONException e) {
                Timber.e(e);
            }

        }

        @Override
        public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(malaria_protective_measures)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else if (malaria_protective_measures.contains("none")) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction ancHomeVisitAction) {

        }
    }
}
