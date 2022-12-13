package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ImmunizationsHelper extends HomeVisitActionHelper {

    private String clinicCard;
    private String received_bcg = "";
    private String received_bopv0 = "";
    private String received_bopv1 = "";
    private String received_dtp_hepb_hib1 = "";
    private String received_pcvi1 = "";
    private String received_rota1 = "";
    private String received_bopv2 = "";
    private String received_dtp_hepb_hib2 = "";
    private String received_pcvi2 = "";
    private String received_rota2 = "";
    private String received_bopv3 = "";
    private String received_dtp_hepb_hib3 = "";
    private String received_pcv3 = "";
    private String received_ipv;
    private String received_surua_rubella1= "";
    private String vaccines_up_to_date = "";
    private String vaccines_not_up_to_date_note;
    private String jsonString;

    private ServiceWrapper serviceWrapper;

    private Map<String, Boolean> visitPeriodMap;
    private final String visit_0_visit_3 = "visit_0_visit_3";
    private final String visit_0_visit_7 = "visit_0_visit_7";
    private final String visit_5_visit_15 = "visit_5_visit_15";
    private final String visit_6_visit_15 = "visit_6_visit_15";
    private final String visit_13_visit_15 = "visit_13_visit_15";

    public ImmunizationsHelper(ServiceWrapper serviceWrapper) {
        this.serviceWrapper = serviceWrapper;
        initVisitPeriodMap();
    }

    public void initVisitPeriodMap() {
        visitPeriodMap = new HashMap<>();
        visitPeriodMap.put(visit_0_visit_3, false);
        visitPeriodMap.put(visit_0_visit_7, false);
        visitPeriodMap.put(visit_5_visit_15, false);
        visitPeriodMap.put(visit_6_visit_15, false);
        visitPeriodMap.put(visit_13_visit_15, false);
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.jsonString = jsonString;
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            if (serviceWrapper != null) {
                String servicePronoun = getPeriodNoun(serviceWrapper.getName());
                int period = getPeriod(serviceWrapper.getName());
                if ("days".equalsIgnoreCase(servicePronoun) || "hours".equalsIgnoreCase(servicePronoun)) {
                    if (period == 24 || period <= 3) {
                        visitPeriodMap.put(visit_0_visit_3, true);
                    }
                    visitPeriodMap.put(visit_0_visit_7, true);
                } else if ("weeks".equalsIgnoreCase(servicePronoun)) {
                    visitPeriodMap.put(visit_0_visit_7, true);
                    if (period >= 5) {
                        visitPeriodMap.put(visit_5_visit_15, true);
                    }
                } else if ("months".equalsIgnoreCase(servicePronoun)) {
                    visitPeriodMap.put(visit_5_visit_15, true);
                    if (period <= 4) {
                        visitPeriodMap.put(visit_0_visit_7, true);
                    }
                    if (period >= 3) {
                        visitPeriodMap.put(visit_6_visit_15, true);
                    }
                    if (period >= 9) {
                        visitPeriodMap.put(visit_13_visit_15, true);
                    }
                }
            }
            for (Map.Entry<String, Boolean> entry : visitPeriodMap.entrySet()) {
                if (entry.getValue()) {
                    JsonFormUtils.getFieldJSONObject(fields, entry.getKey()).put("value", "true");
                }
            }

            return jsonObject.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }
        return super.getPreProcessed();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            clinicCard = JsonFormUtils.getValue(jsonObject, "clinic_card");
            received_bcg = JsonFormUtils.getValue(jsonObject, "received_bcg");
            received_bopv0 = JsonFormUtils.getValue(jsonObject, "received_bopv0");
            received_bopv1 = JsonFormUtils.getValue(jsonObject, "received_bopv1");
            received_dtp_hepb_hib1 = JsonFormUtils.getValue(jsonObject, "received_dtp_hepb_hib1");
            received_pcvi1 = JsonFormUtils.getValue(jsonObject, "received_pcvi1");
            received_rota1 = JsonFormUtils.getValue(jsonObject, "received_rota1");
            received_bopv2 = JsonFormUtils.getValue(jsonObject, "play_with_child");
            received_dtp_hepb_hib2 = JsonFormUtils.getValue(jsonObject, "received_dtp_hepb_hib2");
            received_pcvi2 = JsonFormUtils.getValue(jsonObject, "received_pcvi2");
            received_rota2 = JsonFormUtils.getValue(jsonObject, "received_rota2");
            received_bopv3 = JsonFormUtils.getValue(jsonObject, "received_bopv3");
            received_dtp_hepb_hib3 = JsonFormUtils.getValue(jsonObject, "received_dtp_hepb_hib3");
            received_pcv3 = JsonFormUtils.getValue(jsonObject, "received_pcv3");
            received_ipv = JsonFormUtils.getValue(jsonObject, "received_ipv");
            received_surua_rubella1 = JsonFormUtils.getValue(jsonObject, "received_surua_rubella1");
            vaccines_up_to_date = JsonFormUtils.getValue(jsonObject, "vaccines_up_to_date");
            vaccines_not_up_to_date_note = JsonFormUtils.getValue(jsonObject, "vaccines_not_up_to_date_note");

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        StringBuilder subTitle = new StringBuilder();
        if (vaccines_up_to_date.equalsIgnoreCase("no")) {
            if (received_bcg.equalsIgnoreCase("no")) {
                subTitle.append("BCG(TB) : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_bopv0.equalsIgnoreCase("no")) {
                subTitle.append("bOPV 0 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_bopv1.equalsIgnoreCase("no")) {
                subTitle.append("bOPV 1 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_dtp_hepb_hib1.equalsIgnoreCase("no")) {
                subTitle.append("DTP-HepB-Hib1(Penta) : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_pcvi1.equalsIgnoreCase("no")) {
                subTitle.append("PCV 1 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_rota1.equalsIgnoreCase("no")) {
                subTitle.append("Rota 1 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_bopv2.equalsIgnoreCase("no")) {
                subTitle.append("bOPV 2 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_dtp_hepb_hib2.equalsIgnoreCase("no")) {
                subTitle.append("DTP-HepB-Hib2(Penta) : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_pcvi2.equalsIgnoreCase("no")) {
                subTitle.append("PCV 2 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_rota2.equalsIgnoreCase("no")) {
                subTitle.append("Rota 2 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_bopv3.equalsIgnoreCase("no")) {
                subTitle.append("bOPV 3 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_dtp_hepb_hib3.equalsIgnoreCase("no")) {
                subTitle.append("DTP-HepB-Hib3(Penta) : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_pcv3.equalsIgnoreCase("no")) {
                subTitle.append("PCV 3 : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_ipv.equalsIgnoreCase("no")) {
                subTitle.append("IPV : ").append(context.getString(R.string.no)).append(" ");
            }

            if (received_surua_rubella1.equalsIgnoreCase("no")) {
                subTitle.append("Surua - Rubella 1 : ").append(context.getString(R.string.no)).append(" ");
            }
        }
        return subTitle.toString();
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {

        if (vaccines_up_to_date.equalsIgnoreCase("no")) {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            if (visitPeriodMap.get(visit_0_visit_3)) {
                if (StringUtils.isBlank(received_bcg)) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            } else if (visitPeriodMap.get(visit_0_visit_7)) {
                if (StringUtils.isBlank(received_bopv0)) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            } else if (visitPeriodMap.get(visit_5_visit_15)) {
                String[] keys = new String[]{received_bopv1, received_dtp_hepb_hib1, received_pcvi1, received_rota1};
                boolean isBlank = false;
                for (String key: keys) {
                    if (StringUtils.isBlank(key)) {
                        isBlank = true;
                    }
                }
                if (isBlank) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            } else if (visitPeriodMap.get(visit_6_visit_15)) {
                String[] keys = new String[]{received_bopv2, received_pcvi2, received_dtp_hepb_hib2, received_rota2, received_bopv3, received_dtp_hepb_hib3, received_pcv3};
                boolean isBlank = false;
                for (String key: keys) {
                    if (StringUtils.isBlank(key)) {
                        isBlank = true;
                    }
                }
                if (isBlank) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }else if (visitPeriodMap.get(visit_13_visit_15)) {
                if (StringUtils.isBlank(received_surua_rubella1)) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
            return BaseAncHomeVisitAction.Status.COMPLETED;
        }
    }

    private String getPeriodNoun(String serviceName) {
        String[] nameSplit = serviceName.split(" ");
        return nameSplit[nameSplit.length - 1];
    }

    private int getPeriod(String serviceName) {
        String[] nameSplit = serviceName.split(" ");
        String periodString = nameSplit[nameSplit.length - 2];

        return Integer.parseInt(periodString);
    }
}
