package com.github.eirikma.appstate.calendar.tagging;

import com.github.eirikma.appstate.calendar.model.ChristianHoliday;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Tags dates with Christian holidays, computing Easter-relative dates via Computus.
 */
public class ChristianHolidayTagger implements TaggingService {

    @Override
    public String tagSet() {
        return ChristianHoliday.TAG_SET;
    }

    @Override
    public Set<String> dependencies() {
        return Set.of();
    }

    @Override
    public Map<LocalDate, Set<String>> tag(YearMonth yearMonth, Map<LocalDate, Set<String>> existingTags) {
        Map<LocalDate, Set<String>> result = new LinkedHashMap<>();
        int year = yearMonth.getYear();
        LocalDate easter = Computus.easterSunday(year);

        addIfInMonth(result, yearMonth, LocalDate.of(year, 1, 1), ChristianHoliday.NEW_YEARS_DAY);
        addIfInMonth(result, yearMonth, easter.minusDays(3), ChristianHoliday.MAUNDY_THURSDAY);
        addIfInMonth(result, yearMonth, easter.minusDays(2), ChristianHoliday.GOOD_FRIDAY);
        addIfInMonth(result, yearMonth, easter, ChristianHoliday.EASTER_SUNDAY);
        addIfInMonth(result, yearMonth, easter.plusDays(1), ChristianHoliday.EASTER_MONDAY);
        addIfInMonth(result, yearMonth, easter.plusDays(39), ChristianHoliday.ASCENSION_DAY);
        addIfInMonth(result, yearMonth, easter.plusDays(49), ChristianHoliday.WHIT_SUNDAY);
        addIfInMonth(result, yearMonth, easter.plusDays(50), ChristianHoliday.WHIT_MONDAY);
        addIfInMonth(result, yearMonth, LocalDate.of(year, 12, 25), ChristianHoliday.CHRISTMAS_DAY);
        addIfInMonth(result, yearMonth, LocalDate.of(year, 12, 26), ChristianHoliday.SECOND_DAY_OF_CHRISTMAS);

        return result;
    }

    private static void addIfInMonth(Map<LocalDate, Set<String>> result,
                                     YearMonth yearMonth,
                                     LocalDate date,
                                     ChristianHoliday holiday) {
        if (YearMonth.from(date).equals(yearMonth)) {
            result.computeIfAbsent(date, k -> new LinkedHashSet<>()).add(holiday.name());
        }
    }
}
