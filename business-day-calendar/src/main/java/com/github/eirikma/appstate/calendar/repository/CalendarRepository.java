package com.github.eirikma.appstate.calendar.repository;

import com.github.eirikma.appstate.calendar.model.CalendarDate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persistence abstraction for calendar dates and their tags.
 */
public interface CalendarRepository {

    void deleteMonth(YearMonth yearMonth);

    void saveDates(List<LocalDate> dates, YearMonth yearMonth);

    void saveTags(Map<LocalDate, Set<String>> tagsByDate, String tagSet);

    List<CalendarDate> findDatesByRange(LocalDate from, LocalDate to);

    List<CalendarDate> findDatesByTagSetInRange(String tagSet, LocalDate from, LocalDate to);
}
