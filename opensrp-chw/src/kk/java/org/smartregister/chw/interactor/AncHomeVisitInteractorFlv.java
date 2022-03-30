package org.smartregister.chw.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.ANCCounselingAction;
import org.smartregister.chw.actionhelper.HealthFacilityVisitAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.ContactUtil;
import org.smartregister.chw.util.JsonFormUtils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class AncHomeVisitInteractorFlv implements AncHomeVisitInteractor.Flavor {

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

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

        evaluateDangerSigns(actionList, details, context);
        evaluateBirthPreparedness(actionList, details, memberObject, dateMap, context);
        evaluateHIVAIDSGeneralInformation(actionList, memberObject, context);
        evaluateKkBreastFeeding(actionList, details, memberObject, context);
        evaluateAncClinicAttendance(actionList, details, memberObject, allAncVisits, context);
        evaluateNutritionCounselling(actionList, details, memberObject, allAncVisits, context);
        evaluateGenderIssues(actionList, details, memberObject, allAncVisits, context);
        evaluateMalaria(actionList, memberObject, details, context, allAncVisits);

        return actionList;
    }

    private void evaluateDangerSigns(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                     Map<String, List<VisitDetail>> details,
                                     final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction danger_signs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_danger_signs))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_danger_signs), danger_signs);
    }


    private void evaluateBirthPreparedness(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                           Map<String, List<VisitDetail>> details,
                                           final MemberObject memberObject,
                                           Map<Integer, LocalDate> dateMap,
                                           final Context context) throws BaseAncHomeVisitAction.ValidationException {
        String visit_title = MessageFormat.format("Birth Preparedness", memberObject.getConfirmedContacts() + 1);
        BaseAncHomeVisitAction birth_preparedness = new BaseAncHomeVisitAction.Builder(context, visit_title)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new BirthPreparednessAction())
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
                    .build();

            actionList.put(visit_tittle, hiv_aids_general_info);
        }
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

        String visit_title = MessageFormat.format("ANC Clinic attendance", allVisits.size() + 1);
        BaseAncHomeVisitAction anc_clinic_attendance = new BaseAncHomeVisitAction.Builder(context, visit_title)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new ClinicAttendanceAction())
                .withFormName("anc_hv_clinic_attendance")
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

        String visit_title = MessageFormat.format("Nutrition Counselling", allVisits.size() + 1);
        BaseAncHomeVisitAction nutrition_counselling = new BaseAncHomeVisitAction.Builder(context, visit_title)
                .withOptional(false)
                .withDetails(details)
                .withHelper(new NutritionAction())
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
            String visit_title = MessageFormat.format("Gender Issues", "");
            BaseAncHomeVisitAction gender_issues_counselling = new BaseAncHomeVisitAction.Builder(context, visit_title)
                    .withOptional(false)
                    .withDetails(details)
                    .withFormName("anc_hv_gender_issues")
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
                .withHelper(new MalariaAction())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_malaria_prevention), malaria_ba);
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
            return MessageFormat.format("Danger signs: {0}", danger_signs_present);
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
        private String discussed_bango_kitita;

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
                discussed_bango_kitita = JsonFormUtils.getValue(jsonObject, "labour_signs")
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
            return null;
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
            return MessageFormat.format("{0}: {1}", "Number of clinic attendance", clinic_attendance );
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
        private String nutrition_status;

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
            /*try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                nutrition_status = JsonFormUtils.getValue(jsonObject, "nutrition_status").toLowerCase();
            } catch (JSONException e) {
                Timber.e(e);
            }*/
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
            return MessageFormat.format("{0}: {1}", context.getString(R.string.nutrition_status), "Nutrition counselling complete");
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class GenderIssuesAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        Context context;
        private String counselling_given;

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
            if (counselling_given.contains("yes")){
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class MalariaAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private String llin_last_night;
        private String llin_condition;
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
            if (llin_last_night.equalsIgnoreCase("Yes") && llin_condition.equalsIgnoreCase("Okay")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

    private class BreastFeedingActionHelper implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {

        private String preg_woman_other_children;


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
                preg_woman_other_children = JsonFormUtils.getCheckBoxValue(jsonObject, "preg_woman_other_children");
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
            if (StringUtils.isBlank(preg_woman_other_children)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            } else {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {

        }
    }
}
