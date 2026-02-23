package com.github.eirikma.appstate.calendar.tagging;

import com.github.eirikma.appstate.calendar.model.WeekdayType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tags each date in a month as either WORKDAY (Mon-Fri) or WEEKEND (Sat-Sun).
 */
public class WeekdayTypeTagger implements TaggingService {

    @Override
    public String tagSet() {
        return WeekdayType.TAG_SET;
    }

    @Override
    public Set<String> dependencies() {
        return Set.of();
    }

    @Override
    public Map<LocalDate, Set<String>> tag(YearMonth yearMonth, Map<LocalDate, Set<String>> existingTags) {
        Map<LocalDate, Set<String>> result = new LinkedHashMap<>();
        LocalDate date = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        while (!date.isAfter(end)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            String tag = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
                    ? WeekdayType.WEEKEND.name()
                    : WeekdayType.WORKDAY.name();
            result.put(date, Set.of(tag));
            date = date.plusDays(1);
        }
        return result;
    }
}
