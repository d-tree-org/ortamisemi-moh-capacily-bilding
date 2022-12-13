package org.smartregister.chw.util;

import org.joda.time.LocalDate;
import org.smartregister.util.DateUtil;

import java.util.Date;

/**
 * Author: Isaya Mollel (@issyzac)
 */

public class ChildVisitUtil {

    /**
     * First Visit should be within 24 hours after birth
     *
     * @return TRUE if Anytime between 24 hours and the third day
     */
    public static boolean isFirstVisit(Date dob){
        LocalDate dateOfBirth = DateUtil.getLocalDate(dob.toString());
        LocalDate today = DateUtil.today();
        int daysDifference = DateUtil.dayDifference(dateOfBirth, today);
        if (daysDifference < 3 ){
            return true;
        }else
            return false;
    }

    /**
     * Third day after birth
     * @return TRUE if Anytime between the 3rd and the 8th day
     */
    public static boolean isSecondVisit(Date dob){
        LocalDate dateOfBirth = DateUtil.getLocalDate(dob.toString());
        LocalDate today = DateUtil.today();
        int daysDifference = DateUtil.dayDifference(dateOfBirth, today);
        if (daysDifference >= 3 && daysDifference < 8){
            return true;
        }else
            return false;
    }

    /**
     * Eighth day after birth
     * @return TRUE if Anytime between the 8th day and the 3rd week
     */
    public static boolean isThirdVisit(Date dob){
        LocalDate dateOfBirth = DateUtil.getLocalDate(dob.toString());
        LocalDate today = DateUtil.today();
        int daysDifference = DateUtil.dayDifference(dateOfBirth, today);
        int weekDifference = DateUtil.weekDifference(dateOfBirth, today);
        if (daysDifference >= 8 && weekDifference < 3 ){
            return true;
        }else
            return false;
    }

    /**
     * Third Week after birth
     * @return TRUE if Anytime between the 3rd and the 5th week
     */
    public static boolean isFourthVisit(Date dob){
        LocalDate dateOfBirth = DateUtil.getLocalDate(dob.toString());
        LocalDate today = DateUtil.today();
        int weekDifference = DateUtil.weekDifference(dateOfBirth, today);
        if (weekDifference >= 3 && weekDifference < 5 ){
            return true;
        }else
            return false;
    }

    /**
     * 5th week after birth
     * @return TRUE if Anytime between the 5th and the 7th week
     */
    public static boolean isFifthVisit(Date dob){
        LocalDate dateOfBirth = DateUtil.getLocalDate(dob.toString());
        LocalDate today = DateUtil.today();
        int weekDifference = DateUtil.weekDifference(dateOfBirth, today);
        if (weekDifference >= 5 && weekDifference < 7 ){
            return true;
        }else
            return false;
    }


    /**
     * Sixth week onwards needs to be once a month
     * @return TRUE if Anytime above the 7th week
     */
    public static boolean isMonthlyVisit(Date dob){
        LocalDate dateOfBirth = DateUtil.getLocalDate(dob.toString());
        LocalDate today = DateUtil.today();
        int weekDifference = DateUtil.weekDifference(dateOfBirth, today);
        if (weekDifference > 7){
            return true;
        }else
            return false;
    }








}
