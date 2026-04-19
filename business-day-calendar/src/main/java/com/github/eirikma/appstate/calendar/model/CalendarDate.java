package com.github.eirikma.appstate.calendar.model;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * A specific date that has been populated in the calendar, carrying tags from zero or more tag-sets.
 */
public record CalendarDate(LocalDate date, Map<String, Set<String>> tagsByTagSet) {
}
