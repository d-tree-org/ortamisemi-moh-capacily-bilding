package org.smartregister.chw.util;


import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

/**
 * Created by Kassim Sheghembe on 2022-03-04
 */
public class KkChildDBConstants {

    private static final int FIVE_YEAR = 5;

    public static String childAgeLimitFilter() {
        return childAgeLimitFilter(DBConstants.KEY.DOB, FIVE_YEAR, org.smartregister.chw.core.utils.ChildDBConstants.KEY.ENTRY_POINT, ChildDBConstants.KEY.MOTHER_ENTITY_ID);
    }

    private static String childAgeLimitFilter(String dateColumn, int age, String entryPoint, String motherEntityId) {
        return " ((( julianday('now') - julianday(" + CoreConstants.TABLE_NAME.CHILD + "." + dateColumn + "))/365.25) <" + age + ")  " +
                " and (( ifnull(" + CoreConstants.TABLE_NAME.CHILD + "." + entryPoint + ",'') <> 'PNC' ) or (ifnull(" + CoreConstants.TABLE_NAME.CHILD + "." + entryPoint + ",'') = 'PNC' and ( date(" + CoreConstants.TABLE_NAME.CHILD + "." + dateColumn + ", '+0 days') <= date() and ((SELECT is_closed FROM ec_family_member WHERE base_entity_id = " + CoreConstants.TABLE_NAME.CHILD + "." + motherEntityId + " ) = 0)))  or (ifnull(ec_child.entry_point,'') = 'PNC'  and (SELECT is_closed FROM ec_family_member WHERE base_entity_id = ec_child.mother_entity_id ) = 1)) " +
                " and ((( julianday('now') - julianday(" + CoreConstants.TABLE_NAME.CHILD + "." + dateColumn + "))/365.25) < 5) ";
    }

}
