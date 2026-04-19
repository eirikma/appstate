package com.github.eirikma.appstate.calendar.model;

/**
 * Classifies dates as Norwegian public holidays (røde dager).
 */
public enum NorwegianRedDay {

    NEW_YEARS_DAY,
    MAUNDY_THURSDAY,
    GOOD_FRIDAY,
    EASTER_SUNDAY,
    EASTER_MONDAY,
    LABOUR_DAY,
    CONSTITUTION_DAY,
    ASCENSION_DAY,
    WHIT_SUNDAY,
    WHIT_MONDAY,
    CHRISTMAS_DAY,
    BOXING_DAY;

    public static final String TAG_SET = "NorwegianRedDay";
}
