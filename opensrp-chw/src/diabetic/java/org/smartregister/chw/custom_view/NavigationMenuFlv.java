package org.smartregister.chw.custom_view;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NavigationMenuFlv extends DefaultNavigationMenuFlv {

    @Override
    public List<Pair<String, Locale>> getSupportedLanguages() {
        return Arrays.asList(Pair.of("English", Locale.ENGLISH), Pair.of("Kiswahili", new Locale("sw")));
    }

    @Override
    public String childNavigationMenuCountString() {
        return "select count(*) from ec_child c " +
                "inner join ec_family_member m on c.base_entity_id = m.base_entity_id COLLATE NOCASE " +
                "inner join ec_family f on f.base_entity_id = m.relational_id COLLATE NOCASE " +
                "where m.date_removed is null and m.is_closed = 0 " +
                "and ((( julianday('now') - julianday(c.dob))/365.25) < 5) and c.is_closed = 0 " +
                " and (( ( ifnull(entry_point,'') <> 'PNC' ) ) or (ifnull(entry_point,'') = 'PNC' and ( date (c.dob, '+0 days') <= date() and ((SELECT is_closed FROM ec_family_member WHERE base_entity_id = mother_entity_id ) = 0)))  or (ifnull(entry_point,'') = 'PNC'  and (SELECT is_closed FROM ec_family_member WHERE base_entity_id = mother_entity_id ) = 1)) ";
    }
}
