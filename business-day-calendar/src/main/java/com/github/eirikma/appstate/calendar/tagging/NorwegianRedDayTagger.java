package com.github.eirikma.appstate.calendar.tagging;

import com.github.eirikma.appstate.calendar.model.ChristianHoliday;
import com.github.eirikma.appstate.calendar.model.NorwegianRedDay;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Tags dates as Norwegian public holidays by combining fixed dates with Easter-dependent
 * dates read from the ChristianHoliday tag-set.
 */
public class NorwegianRedDayTagger implements TaggingService {

    private static final Map<String, NorwegianRedDay> CHRISTIAN_TO_RED_DAY = Map.of(
            ChristianHoliday.MAUNDY_THURSDAY.name(), NorwegianRedDay.MAUNDY_THURSDAY,
            ChristianHoliday.GOOD_FRIDAY.name(), NorwegianRedDay.GOOD_FRIDAY,
            ChristianHoliday.EASTER_SUNDAY.name(), NorwegianRedDay.EASTER_SUNDAY,
            ChristianHoliday.EASTER_MONDAY.name(), NorwegianRedDay.EASTER_MONDAY,
            ChristianHoliday.ASCENSION_DAY.name(), NorwegianRedDay.ASCENSION_DAY,
            ChristianHoliday.WHIT_SUNDAY.name(), NorwegianRedDay.WHIT_SUNDAY,
            ChristianHoliday.WHIT_MONDAY.name(), NorwegianRedDay.WHIT_MONDAY
    );

    @Override
    public String tagSet() {
        return NorwegianRedDay.TAG_SET;
    }

    @Override
    public Set<String> dependencies() {
        return Set.of(ChristianHoliday.TAG_SET);
    }

    @Override
    public Map<LocalDate, Set<String>> tag(YearMonth yearMonth, Map<LocalDate, Set<String>> existingTags) {
        Map<LocalDate, Set<String>> result = new LinkedHashMap<>();
        int year = yearMonth.getYear();

        addFixedIfInMonth(result, yearMonth, LocalDate.of(year, 1, 1), NorwegianRedDay.NEW_YEARS_DAY);
        addFixedIfInMonth(result, yearMonth, LocalDate.of(year, 5, 1), NorwegianRedDay.LABOUR_DAY);
        addFixedIfInMonth(result, yearMonth, LocalDate.of(year, 5, 17), NorwegianRedDay.CONSTITUTION_DAY);
        addFixedIfInMonth(result, yearMonth, LocalDate.of(year, 12, 25), NorwegianRedDay.CHRISTMAS_DAY);
        addFixedIfInMonth(result, yearMonth, LocalDate.of(year, 12, 26), NorwegianRedDay.BOXING_DAY);

        existingTags.forEach((date, tags) -> {
            if (YearMonth.from(date).equals(yearMonth)) {
                tags.forEach(tag -> {
                    NorwegianRedDay redDay = CHRISTIAN_TO_RED_DAY.get(tag);
                    if (redDay != null) {
                        result.computeIfAbsent(date, k -> new LinkedHashSet<>()).add(redDay.name());
                    }
                });
            }
        });

        return result;
    }

    private static void addFixedIfInMonth(Map<LocalDate, Set<String>> result,
                                           YearMonth yearMonth,
                                           LocalDate date,
                                           NorwegianRedDay redDay) {
        if (YearMonth.from(date).equals(yearMonth)) {
            result.computeIfAbsent(date, k -> new LinkedHashSet<>()).add(redDay.name());
        }
    }
}
