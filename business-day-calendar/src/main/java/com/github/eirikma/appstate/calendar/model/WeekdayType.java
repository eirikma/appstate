package com.github.eirikma.appstate.calendar.model;

/**
 * Classifies a date as either a workday or weekend day.
 */
public enum WeekdayType {

    WORKDAY,
    WEEKEND;

    public static final String TAG_SET = "WeekdayType";
}
