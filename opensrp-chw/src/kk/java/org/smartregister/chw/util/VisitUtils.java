package org.smartregister.chw.util;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.utils.CoreConstants;

public class VisitUtils {
    public static boolean isFirstVisit(final MemberObject memberObject) {
        int gaWeeks = memberObject.getGestationAge();
        Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), CoreConstants.EventType.ANC_HOME_VISIT);
        // Assumption 6 months pregnancy is 24 weeks GA
        return gaWeeks < 24 && lastVisit == null;
    }

    public static boolean isSecondVisit(final MemberObject memberObject) {
        int gaWeeks = memberObject.getGestationAge();
        Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), CoreConstants.EventType.ANC_HOME_VISIT);

        LocalDate lastVisitDate = new LocalDate();
        if (lastVisit != null) {
            lastVisitDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate(new LocalDate(lastVisit.getDate()).toString());
            // When there is no visit take the createdDate to be the last visit date
        } else {
            lastVisitDate = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parseLocalDate(memberObject.getDateCreated());
        }

        LocalDate lmp = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastMenstrualPeriod());

        int gaInLastVisit = Days.daysBetween(lmp, lastVisitDate).getDays() / 7;
        // GA between 6 (24 weeks) months and 8 (32 weeks) months
        // GA during the last visit was between 24 weeks and 32 weeks then she had the second visit
        return (gaWeeks >= 24 && gaWeeks < 32) && !(gaInLastVisit >= 24 && gaInLastVisit < 32);
    }

    public static boolean isThirdVisit(final MemberObject memberObject) {
        int gaWeeks = memberObject.getGestationAge();
        return gaWeeks >= 32;
    }

    public static int getNumPrevVisits(MemberObject memberObject) {
        return AncLibrary.getInstance().visitRepository().getVisits(memberObject.getBaseEntityId(), CoreConstants.EventType.ANC_HOME_VISIT).size();
    }

}
