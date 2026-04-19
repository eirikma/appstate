package com.github.eirikma.appstate.calendar.service;

import com.github.eirikma.appstate.calendar.model.CalendarDate;
import com.github.eirikma.appstate.calendar.repository.CalendarRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Delegates calendar queries to the underlying repository.
 */
public class DefaultCalendarQuery implements CalendarQuery {

    private final CalendarRepository repository;

    public DefaultCalendarQuery(CalendarRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CalendarDate> findByDateRange(LocalDate from, LocalDate to) {
        return repository.findDatesByRange(from, to);
    }

    @Override
    public List<CalendarDate> findByTagSetInRange(String tagSet, LocalDate from, LocalDate to) {
        return repository.findDatesByTagSetInRange(tagSet, from, to);
    }
}
