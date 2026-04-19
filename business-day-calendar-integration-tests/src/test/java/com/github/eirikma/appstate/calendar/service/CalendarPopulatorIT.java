package com.github.eirikma.appstate.calendar.service;

import com.github.eirikma.appstate.calendar.model.CalendarDate;
import com.github.eirikma.appstate.calendar.registry.DefaultTagSetRegistry;
import com.github.eirikma.appstate.calendar.repository.JdbcCalendarRepository;
import com.github.eirikma.appstate.calendar.tagging.ChristianHolidayTagger;
import com.github.eirikma.appstate.calendar.tagging.NorwegianRedDayTagger;
import com.github.eirikma.appstate.calendar.tagging.WeekdayTypeTagger;
import com.github.eirikma.appstate.test.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("CalendarPopulator integration test")
class CalendarPopulatorIT extends IntegrationTestBase {

    private JdbcCalendarRepository repository;
    private CalendarPopulator populator;

    @BeforeEach
    void setUp() {
        repository = new JdbcCalendarRepository(dataSource());
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(new WeekdayTypeTagger());
        registry.register(new ChristianHolidayTagger());
        registry.register(new NorwegianRedDayTagger());
        populator = new CalendarPopulator(registry, repository);

        // Clean up before each test
        repository.deleteMonth(YearMonth.of(2026, 4));
    }

    @Test
    @DisplayName("should populate April 2026 with all 3 tag-sets")
    void shouldPopulateApril2026() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> dates = repository.findDatesByRange(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        the(dates.size()).shouldEqual(30);
    }

    @Test
    @DisplayName("should tag weekdays as WORKDAY and weekends as WEEKEND")
    void shouldTagWeekdayTypes() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> dates = repository.findDatesByRange(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        // April 1 is Wednesday = WORKDAY
        CalendarDate april1 = findDate(dates, LocalDate.of(2026, 4, 1));
        the(april1.tagsByTagSet().get("WeekdayType")).shouldContain("WORKDAY");

        // April 4 is Saturday = WEEKEND
        CalendarDate april4 = findDate(dates, LocalDate.of(2026, 4, 4));
        the(april4.tagsByTagSet().get("WeekdayType")).shouldContain("WEEKEND");
    }

    @Test
    @DisplayName("should tag Easter Sunday April 5 as ChristianHoliday and NorwegianRedDay")
    void shouldTagEasterSunday() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> dates = repository.findDatesByRange(
                LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 5));

        CalendarDate easterSunday = dates.getFirst();
        the(easterSunday.tagsByTagSet().get("ChristianHoliday")).shouldContain("EASTER_SUNDAY");
        the(easterSunday.tagsByTagSet().get("NorwegianRedDay")).shouldContain("EASTER_SUNDAY");
    }

    @Test
    @DisplayName("should tag Maundy Thursday April 2 and Good Friday April 3")
    void shouldTagMaundyThursdayAndGoodFriday() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> dates = repository.findDatesByRange(
                LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 3));

        CalendarDate maundyThursday = findDate(dates, LocalDate.of(2026, 4, 2));
        the(maundyThursday.tagsByTagSet().get("ChristianHoliday")).shouldContain("MAUNDY_THURSDAY");
        the(maundyThursday.tagsByTagSet().get("NorwegianRedDay")).shouldContain("MAUNDY_THURSDAY");

        CalendarDate goodFriday = findDate(dates, LocalDate.of(2026, 4, 3));
        the(goodFriday.tagsByTagSet().get("ChristianHoliday")).shouldContain("GOOD_FRIDAY");
        the(goodFriday.tagsByTagSet().get("NorwegianRedDay")).shouldContain("GOOD_FRIDAY");
    }

    @Test
    @DisplayName("should replace all tags when re-populating an already-populated month")
    void shouldReplaceTagsOnRepopulation() {
        populator.populate(YearMonth.of(2026, 4));

        // Re-populate the same month
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> dates = repository.findDatesByRange(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        // Should still have exactly 30 dates (not 60 from duplicate inserts)
        the(dates.size()).shouldEqual(30);

        // Tags should still be correct
        CalendarDate easterSunday = findDate(dates, LocalDate.of(2026, 4, 5));
        the(easterSunday.tagsByTagSet().get("ChristianHoliday")).shouldContain("EASTER_SUNDAY");
    }

    private static CalendarDate findDate(List<CalendarDate> dates, LocalDate target) {
        return dates.stream()
                .filter(d -> d.date().equals(target))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Date not found: " + target));
    }
}
