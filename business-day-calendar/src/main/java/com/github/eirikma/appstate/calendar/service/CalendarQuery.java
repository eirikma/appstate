package com.github.eirikma.appstate.calendar.service;

import com.github.eirikma.appstate.calendar.model.CalendarDate;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only access to populated calendar dates.
 */
public interface CalendarQuery {

    List<CalendarDate> findByDateRange(LocalDate from, LocalDate to);

    List<CalendarDate> findByTagSetInRange(String tagSet, LocalDate from, LocalDate to);
}
