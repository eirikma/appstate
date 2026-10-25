package com.github.eirikma.appstate.calendar.model;

/**
 * Classifies a date as either a workday or weekend day.
 */
public enum WeekdayType implements TagSet {

    WORKDAY,
    WEEKEND;

    public static final String TAG_SET = "WeekdayType";

    @Override
    public String getTagSetName() {
        return TAG_SET;
    }
}
