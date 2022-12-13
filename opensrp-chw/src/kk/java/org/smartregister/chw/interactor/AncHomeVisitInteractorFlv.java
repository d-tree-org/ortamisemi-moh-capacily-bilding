package org.smartregister.chw.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.VisitLocationActionHelper;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.ContactUtil;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.util.StringUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class AncHomeVisitInteractorFlv implements AncHomeVisitInteractor.Flavor {

    private MemberObject memberObject;

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();
        this.memberObject = memberObject;

        Map<String, List<VisitDetail>> details = null;

        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.ANC_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        // get all ANC visits
        List<Visit> allAncVisits = new ArrayList<>();
        allAncVisits = AncLibrary.getInstance().visitRepository().getVisits(memberObject.getBaseEntityId(), CoreConstants.EventType.ANC_HOME_VISIT);

        // get contact
        LocalDate lastContact = new DateTime(memberObject.getDateCreated()).toLocalDate();
        boolean isFirst = (StringUtils.isBlank(memberObject.getLastContactVisit()));
        LocalDate lastMenstrualPeriod = new LocalDate();
        try {
            lastMenstrualPeriod = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastMenstrualPeriod());
        } catch (Exception e) {
            Timber.e(e);
        }

        if (StringUtils.isNotBlank(memberObject.getLastContactVisit())) {
            lastContact = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastContactVisit());
        }

        Map<Integer, LocalDate> dateMap = new LinkedHashMap<>();

        // today is the due date for the very first visit
        if (isFirst) {
            dateMap.put(0, LocalDate.now());
        }

        dateMap.putAll(ContactUtil.getContactWeeks(isFirst, lastContact, lastMenstrualPeriod));

        evaluateLocation(actionList, details, context);
        evaluateDangerSigns(actionList, details, context);
        evaluateBirthPreparedness(actionList, details, memberObject, dateMap, context);
        evaluateHIVAIDSGeneralInformation(actionList, memberObject, context);
        evaluatePMTCT(actionList, details,  memberObject, context);
        evaluateKkBreastFeeding(actionList, details, memberObject, context);
        evaluateAncClinicAttendance(actionList, details, memberObject, allAncVisits, context);
        evaluateNutritionCounselling(actionList, details, memberObject, allAncVisits, context);
        evaluateGenderIssues(actionList, details, memberObject, allAncVisits, context);
        evaluateMalaria(actionList, memberObject, details, context, allAncVisits);
        evaluatePostpartumPreparations(actionList, memberObject, details, context, allAncVisits);
        evaluateEarlyStimulation(actionList, details, context);
        evaluateHarmfulHabits(actionList, details, context);
        evaluatePartnerEngagement(actionList, details, context);
        evaluateChwObservation(actionList, details, context);

        return actionList;
    }

    @Override
    public void addExtraObs(Event event) {
        try {
            String visit_number = getAncVisitNumber(memberObject);
            List<Object> visitNumberObsValue = new ArrayList<>();
            visitNumberObsValue.add(visit_number);

            event.addObs(new Obs("concept", "text", "visit_number", "",
                    visitNumberObsValue, new ArrayList<>(), null, "visit_number"));

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private String getAncVisitNumber(MemberObject memberObject) {
        String visit_number = null;
        if (org.smartregister.chw.util.VisitUtils.isFirstVisit(memberObject)) {
            visit_number = "1";
        } else if (org.smartregister.chw.util.VisitUtils.isSecondVisit(memberObject)) {
            visit_number = "2";
        } else if (org.smartregister.chw.util.VisitUtils.isThirdVisit(memberObject)) {
            visit_number = "3";
        } else {
            visit_number = "followup_visit";
        }

        return visit_number;
    }

    private void evaluateLocation(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                  Map<String, List<VisitDetail>> details,
                                  final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.visit_location))
                .withDetails(details)
                .withOptional(false)
                .withFormName("hv_visit_location")
                .withHelper(new VisitLocationActionHelper(context))
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .build();
        actionList.put(context.getString(R.string.visit_location), action);
    }

    private void evaluateDangerSigns(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                     Map<String, List<VisitDetail>> details,
                                     final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction danger_signs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_danger_signs))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getDangerSigns())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_danger_signs), danger_signs);
    }


    private void evaluateBirthPreparedness(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                           Map<String, List<VisitDetail>> details,
                                           final MemberObject memberObject,
                                           Map<Integer, LocalDate> dateMap,
                                           final Context context) throws BaseAncHomeVisitAction.ValidationException {
        String visit_title = MessageFormat.format(context.getString(R.string.anc_home_visit_birth_preparedness), memberObject.getConfirmedContacts() + 1);
        BaseAncHomeVisitAction birth_preparedness = new BaseAncHomeVisitAction.Builder(context, visit_title)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new BirthPreparednessAction())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName("anc_hv_birth_preparedness")
                .build();

        actionList.put(visit_title, birth_preparedness);
    }

    private void evaluateHIVAIDSGeneralInformation(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                                   final MemberObject memberObject,
                                                   final Context context) throws BaseAncHomeVisitAction.ValidationException {


        if (org.smartregister.chw.util.VisitUtils.isFirstVisit(memberObject)) {
            String visit_tittle = context.getString(R.string.hiv_aids_general_info);

            BaseAncHomeVisitAction hiv_aids_general_info = new BaseAncHomeVisitAction.Builder(context, visit_tittle)
                    .withOptional(false)
                    .withFormName("anc_hv_hiv_aids_general_information")
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                    .build();

            actionList.put(visit_tittle, hiv_aids_general_info);
        }
    }

    private void evaluatePMTCT(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                               Map<String, List<VisitDetail>> details,
                               final MemberObject memberObject,
                               final Context context) throws BaseAncHomeVisitAction.ValidationException {

        String visitTitle  = context.getString(R.string.anc_home_visit_pmtct);

        BaseAncHomeVisitAction pmtctAction = new BaseAncHomeVisitAction.Builder(context, visitTitle)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new PmtctActionHelper())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName("anc_hv_pmctc")
                .build();

        actionList.put(visitTitle, pmtctAction);

    }

    private void evaluateKkBreastFeeding(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                         Map<String, List<VisitDetail>> details, final MemberObject memberObject,
                                         final Context context) throws BaseAncHomeVisitAction.ValidationException {
        if (org.smartregister.chw.util.VisitUtils.isSecondVisit(memberObject) || org.smartregister.chw.util.VisitUtils.isThirdVisit(memberObject)) {
            String action_title = context.getString(R.string.breast_feeding_action_title);

            BaseAncHomeVisitAction bread_feeding_action = new BaseAncHomeVisitAction.Builder(context, action_title)
                    .withOptional(false)
                    .withDetails(details)
                    .withHelper(new BreastFeedingActionHelper())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                    .withFormName("anc_hv_breastfeeding")
                    .build();

            actionList.put(action_title, bread_feeding_action);
        }

    }

    private void evaluateAncClinicAttendance(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                             Map<String, List<VisitDetail>> details,
                                             final MemberObject memberObject,
                                             List<Visit> allVisits,
                                             final Context context) throws BaseAncHomeVisitAction.ValidationException {

        //Check if first and second visit had already been conducted
        if (org.smartregister.chw.util.VisitUtils.isThirdVisit(memberObject))
            return;

        String visit_title = MessageFormat.format(context.getString(R.string.anc_hv_clinic_attendance), allVisits.size() + 1);
        BaseAncHomeVisitAction anc_clinic_attendance = new BaseAncHomeVisitAction.Builder(context, visit_title)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new ClinicAttendanceAction())
                .withFormName("anc_hv_clinic_attendance")
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .build();

        actionList.put(visit_title, anc_clinic_attendance);
    }

    private void evaluateNutritionCounselling(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                              Map<String, List<VisitDetail>> details,
                                              final MemberObject memberObject,
                                              List<Visit> allVisits,
                                              final Context context) throws BaseAncHomeVisitAction.ValidationException {

        //Check if first and second visit had already been conducted
        if (org.smartregister.chw.util.VisitUtils.isThirdVisit(memberObject))
            return;

        String visit_title = MessageFormat.format(context.getString(R.string.anc_hv_nutrition_counselling), allVisits.size() + 1);
        BaseAncHomeVisitAction nutrition_counselling = new BaseAncHomeVisitAction.Builder(context, visit_title)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new NutritionAction())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withFormName("anc_hv_nutrition_counselling")
                .build();

        actionList.put(visit_title, nutrition_counselling);
    }

    private void evaluateGenderIssues(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                      Map<String, List<VisitDetail>> details,
                                      final MemberObject memberObject,
                                      List<Visit> allAncVisits,
                                      final Context context) throws BaseAncHomeVisitAction.ValidationException {

        if (org.smartregister.chw.util.VisitUtils.isFirstVisit(memberObject)){
            String visit_title = MessageFormat.format(context.getString(R.string.anc_home_visit_gender_issues), "");
            BaseAncHomeVisitAction gender_issues_counselling = new BaseAncHomeVisitAction.Builder(context, visit_title)
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName("anc_hv_gender_issues")
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                    .withHelper(new GenderIssuesAction())
                    .build();
            actionList.put(context.getString(R.string.anc_home_visit_gender_issues), gender_issues_counselling);
        }
    }

    private void evaluateMalaria(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                 final MemberObject memberObject,
                                 Map<String, List<VisitDetail>> details,
                                 final Context context,
                                 List<Visit> allAncVisits) throws BaseAncHomeVisitAction.ValidationException {

        //Check if first and second visit had already been conducted
        if (org.smartregister.chw.util.VisitUtils.isThirdVisit(memberObject))
            return;

        BaseAncHomeVisitAction malaria_ba = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_malaria_prevention))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getMALARIA())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withHelper(new MalariaAction())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_malaria_prevention), malaria_ba);
    }

    private void evaluatePostpartumPreparations(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                                final MemberObject memberObject,
                                                Map<String, List<VisitDetail>> details,
                                                final Context context,
                                                List<Visit> allAncVisits) throws BaseAncHomeVisitAction.ValidationException {

        //Check if first and second visit had already been conducted
        if (!org.smartregister.chw.util.VisitUtils.isThirdVisit(memberObject))
            return;

        BaseAncHomeVisitAction postpartum = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_postpartum_preparation))
                .withOptional(false)
                .withDetails(details)
                .withFormName("anc_hv_postpartum")
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withHelper(new PostpartumPreparationActionHelper())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_postpartum_preparation), postpartum);
    }

    private void evaluatePartnerEngagement(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                           Map<String, List<VisitDetail>> details,
                                           final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction partner_engagement = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_partner_engagement))
                .withOptional(false)
                .withDetails(details)
                .withFormName("anc_hv_partner_engagement")
                .withHelper(new PartnerEngagementAction())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_partner_engagement), partner_engagement);
    }

    private void evaluateEarlyStimulation(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                     Map<String, List<VisitDetail>> details,
                                     final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction earlyStimulation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_early_stimulation))
                .withOptional(false)
                .withDetails(details)
                .withFormName("anc_hv_early_stimulation")
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_early_stimulation), earlyStimulation);
    }

    private void evaluateHarmfulHabits(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                       Map<String, List<VisitDetail>> details,
                                       final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction harmful_habits = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_harmful_habits))
                .withOptional(false)
                .withDetails(details)
                .withFormName("anc_hv_harmful_habits")
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_harmful_habits), harmful_habits);
    }

    private void evaluateChwObservation(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                          Map<String, List<VisitDetail>> details,
                                          final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction chw_observations = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_chw_observations))
                .withOptional(false)
                .withDetails(details)
                .withFormName("anc_hv_chw_observations")
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.COMBINED)
                .withHelper(new ChwObservationsAction())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_chw_observations), chw_observations);
    }

    private class DangerSignsAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private String danger_signs_counseling;
        private String danger_signs_present;
        private Context context;

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                danger_signs_present = JsonFormUtils.getCheckBoxValue(jsonObject, "danger_signs_present");
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format(context.getString(R.string.danger_sign_evaluate_sub_title), danger_signs_present);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(danger_signs_present)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class BirthPreparednessAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        private Context context;
        private String discussed_bango_kitita = "";

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
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
                discussed_bango_kitita = JsonFormUtils.getValue(jsonObject, "labour_signs");
            }catch (Exception e){
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
            return MessageFormat.format(context.getString(R.string.discussed_labour_signs_with_woman)+": {0}", discussed_bango_kitita);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (discussed_bango_kitita.equalsIgnoreCase("yes")){
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
            else if (discussed_bango_kitita.equalsIgnoreCase("No")){
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
            else
                return BaseAncHomeVisitAction.Status.PENDING;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction ancHomeVisitAction) {

        }
    }

    private class PmtctActionHelper implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        private Context context;
        String hiv_test;
        String disclose_status;
        String taking_art;
        String hiv_status;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                hiv_test = JsonFormUtils.getValue(jsonObject, "hiv_test").toLowerCase();
                disclose_status = JsonFormUtils.getValue(jsonObject, "disclose_status").toLowerCase();
                taking_art = JsonFormUtils.getValue(jsonObject, "taking_art").toLowerCase();
                hiv_status = JsonFormUtils.getValue(jsonObject, "hiv_status").toLowerCase();
            }catch (JSONException e){
                e.printStackTrace();
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

            if (hiv_test.contains("chk_hiv_test_no")){
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }

            if (hiv_test.contains("chk_hiv_test_yes") && disclose_status.contains("chk_disclose_status_no")){
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }

            if (hiv_test.contains("chk_hiv_test_yes") && disclose_status.contains("chk_disclose_status_yes") && taking_art.contains("chk_taking_art_no")){
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }

            if (hiv_test.contains("chk_hiv_test_yes") && hiv_status.contains("chk_hiv_status_negative")){
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }

            if (hiv_test.contains("chk_hiv_test_yes") && hiv_status.contains("chk_hiv_status_positive") && taking_art.contains("chk_taking_art_yes")){
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }

            return BaseAncHomeVisitAction.Status.PENDING;

        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction ancHomeVisitAction) {

        }
    }

    private class ClinicAttendanceAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        private Context context;
        private String clinic_attendance;

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                clinic_attendance = JsonFormUtils.getValue(jsonObject, "number_anc_clinic_visit").toLowerCase();
            }catch (JSONException e){
                e.printStackTrace();
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format("{0}: {1}", context.getString(R.string.anc_hv_clinic_attendance_sub_title), clinic_attendance);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(clinic_attendance)){
                return BaseAncHomeVisitAction.Status.PENDING;
            }else{
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {

        }
    }

    private class NutritionAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private Context context;
        private String available_foods = "";

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                available_foods = JsonFormUtils.getCheckBoxValue(jsonObject, "foods_available");
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format("{0}: {1}", context.getString(R.string.foods_available), available_foods);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (available_foods.isEmpty()){
                return BaseAncHomeVisitAction.Status.PENDING;
            }else {
                return  BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class GenderIssuesAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        Context context;
        private String counselling_given = "";

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                counselling_given = JsonFormUtils.getValue(jsonObject, "gender_issues_counselling_status").toLowerCase();
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            String subTitle = (counselling_given.contains("yes") ? context.getString(R.string.done).toLowerCase() : context.getString(R.string.not_done).toLowerCase());
            return MessageFormat.format("{0} {1}", context.getString(R.string.counselling), subTitle);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {

            if (counselling_given.isEmpty())
                return BaseAncHomeVisitAction.Status.PENDING;

            if (counselling_given.equalsIgnoreCase("yes"))
                return BaseAncHomeVisitAction.Status.COMPLETED;

            if (counselling_given.equalsIgnoreCase("no"))
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            else
                return BaseAncHomeVisitAction.Status.PENDING;

        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class MalariaAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private String llin_last_night = "";
        private String llin_condition = "";
        private String malaria_protective_measure = "";
        private Context context;

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                malaria_protective_measure = JsonFormUtils.getValue(jsonObject, "malaria_protective_measures");
                llin_last_night = JsonFormUtils.getValue(jsonObject, "llin_last_night");
                llin_condition = JsonFormUtils.getValue(jsonObject, "llin_condition");
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.slept_under_net), StringUtils.capitalize(llin_last_night.trim().toLowerCase())));
            stringBuilder.append(MessageFormat.format("{0}: {1}", context.getString(R.string.net_condition), StringUtils.capitalize(llin_condition.trim().toLowerCase())));

            return stringBuilder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (malaria_protective_measure.contains("chk_use_llin")){
                if (llin_last_night.isEmpty() || llin_condition.isEmpty())
                    return BaseAncHomeVisitAction.Status.PENDING;

                if (llin_last_night.equalsIgnoreCase("Yes") && llin_condition.equalsIgnoreCase("Okay"))
                    return BaseAncHomeVisitAction.Status.COMPLETED;

                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;

            }else if (!malaria_protective_measure.isEmpty()){
                    return BaseAncHomeVisitAction.Status.COMPLETED;
            }
            return BaseAncHomeVisitAction.Status.PENDING;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class PostpartumPreparationActionHelper implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        private String postpartum_psychological_changes;
        private String postpartum_danger_sign;
        private String immediate_newborn_care;
        private String newborn_danger_sign;
        private String followup_hiv_exposed_infant;
        private String lam;
        private String postpartum_family_planning;
        private Context context;

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                postpartum_psychological_changes = JsonFormUtils.getValue(jsonObject, "postpartum_psychological_changes");
                postpartum_danger_sign = JsonFormUtils.getValue(jsonObject, "postpartum_danger_signs");
                immediate_newborn_care = JsonFormUtils.getValue(jsonObject, "immediate_newborn_care");
                newborn_danger_sign = JsonFormUtils.getValue(jsonObject, "newborn_danger_sign");
                followup_hiv_exposed_infant = JsonFormUtils.getValue(jsonObject, "followup_hiv_exposed_infant");
                lam = JsonFormUtils.getValue(jsonObject, "lam");
                postpartum_family_planning = JsonFormUtils.getValue(jsonObject, "postpartum_family_planning");
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            StringBuilder stringBuilder = new StringBuilder();
            if (StringUtils.isNotBlank(postpartum_psychological_changes))
                stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.anc_home_visit_postpartum_preparation_psychological_changes), StringUtils.capitalize(postpartum_psychological_changes.trim().toLowerCase())));
            if (StringUtils.isNotBlank(postpartum_danger_sign))
                stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.anc_home_visit_postpartum_preparation_postpartum_danger_sign), StringUtils.capitalize(postpartum_danger_sign.trim().toLowerCase())));
            if (StringUtils.isNotBlank(newborn_danger_sign))
                stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.anc_home_visit_postpartum_preparation_new_born_danger_sign), StringUtils.capitalize(newborn_danger_sign.trim().toLowerCase())));
            if (StringUtils.isNotBlank(immediate_newborn_care))
                stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.anc_home_visit_postpartum_preparation_immediate_newborn_care), StringUtils.capitalize(immediate_newborn_care.trim().toLowerCase())));
            if (StringUtils.isNotBlank(followup_hiv_exposed_infant))
                stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.anc_home_visit_postpartum_preparation_followup_hiv_exposed_infant), StringUtils.capitalize(followup_hiv_exposed_infant.trim().toLowerCase())));
            if (StringUtils.isNotBlank(lam))
                stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.anc_home_visit_postpartum_preparation_lam), StringUtils.capitalize(lam.trim().toLowerCase())));
            if (StringUtils.isNotBlank(postpartum_family_planning))
                stringBuilder.append(MessageFormat.format("{0}: {1} \n", context.getString(R.string.anc_home_visit_postpartum_postpartum_family_planning), StringUtils.capitalize(postpartum_family_planning.trim().toLowerCase())));

            return stringBuilder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {

            if (StringUtils.isBlank(postpartum_psychological_changes) || StringUtils.isBlank(postpartum_danger_sign)
                    || StringUtils.isBlank(immediate_newborn_care) || StringUtils.isBlank(newborn_danger_sign)
                    || StringUtils.isBlank(followup_hiv_exposed_infant) || StringUtils.isBlank(lam)
                    || StringUtils.isBlank(postpartum_family_planning))
                return BaseAncHomeVisitAction.Status.PENDING;

            if (postpartum_psychological_changes.equalsIgnoreCase("no") || postpartum_danger_sign.equalsIgnoreCase("no")
                    || immediate_newborn_care.equalsIgnoreCase("no") || newborn_danger_sign.equalsIgnoreCase("no")
                    || followup_hiv_exposed_infant.equalsIgnoreCase("no") || lam.equalsIgnoreCase("no")
                    || postpartum_family_planning.equalsIgnoreCase("no")) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class BreastFeedingActionHelper implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        private String preg_woman_other_children;
        private String preg_woman_breastfeed;


        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {

        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String s) {

            try {
                JSONObject jsonObject = new JSONObject(s);
                preg_woman_other_children = JsonFormUtils.getValue(jsonObject, "preg_woman_other_children");
                preg_woman_breastfeed = JsonFormUtils.getValue(jsonObject, "preg_woman_breastfeed");
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(preg_woman_other_children) || StringUtils.isBlank(preg_woman_breastfeed)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else if (preg_woman_breastfeed.contains("chk_no")) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {

        }
    }

    private class PartnerEngagementAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private String partner_presence;
        private Context context;

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                partner_presence = JsonFormUtils.getCheckBoxValue(jsonObject, "partner_head_of_household");
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format(context.getString(R.string.partner_engagement_evaluate_sub_title), partner_presence);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (!StringUtils.isBlank(partner_presence))
                if(partner_presence.equalsIgnoreCase("yes") || partner_presence.equalsIgnoreCase("ndio")){
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                }else{
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                }
            else
                return BaseAncHomeVisitAction.Status.PENDING;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class ChwObservationsAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private String value = "";
        private String anyone_presence = "";
        private Context context;

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                value = JsonFormUtils.getCheckBoxValue(jsonObject, "anyone_present");
                anyone_presence = JsonFormUtils.getValue(jsonObject, "anyone_present");
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
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if(!value.isEmpty()){
                return MessageFormat.format(context.getString(R.string.chw_observations_evaluate_sub_title), value);
            }else{
                return value;
            }
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if(!anyone_presence.isEmpty()){
                if(anyone_presence.contains("anyone_present_yes")){
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                }else{
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                }
            }
            else
                return BaseAncHomeVisitAction.Status.PENDING;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }
}
