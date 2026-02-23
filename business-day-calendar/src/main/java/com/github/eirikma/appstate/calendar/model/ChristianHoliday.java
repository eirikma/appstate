package com.github.eirikma.appstate.calendar.model;

/**
 * Classifies dates as Christian holidays, both fixed and Easter-relative.
 */
public enum ChristianHoliday {

    NEW_YEARS_DAY,
    MAUNDY_THURSDAY,
    GOOD_FRIDAY,
    EASTER_SUNDAY,
    EASTER_MONDAY,
    ASCENSION_DAY,
    WHIT_SUNDAY,
    WHIT_MONDAY,
    CHRISTMAS_DAY,
    SECOND_DAY_OF_CHRISTMAS;

    public static final String TAG_SET = "ChristianHoliday";
}
