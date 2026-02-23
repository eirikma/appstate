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

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("CalendarQuery integration test")
class CalendarQueryIT extends IntegrationTestBase {

    private JdbcCalendarRepository repository;
    private DefaultCalendarQuery query;
    private CalendarPopulator populator;

    @BeforeEach
    void setUp() {
        repository = new JdbcCalendarRepository(dataSource());
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(new WeekdayTypeTagger());
        registry.register(new ChristianHolidayTagger());
        registry.register(new NorwegianRedDayTagger());
        populator = new CalendarPopulator(registry, repository);
        query = new DefaultCalendarQuery(repository);

        repository.deleteMonth(YearMonth.of(2026, 1));
        repository.deleteMonth(YearMonth.of(2026, 2));
        repository.deleteMonth(YearMonth.of(2026, 3));
        repository.deleteMonth(YearMonth.of(2026, 4));
    }

    @Test
    @DisplayName("should return dates within a populated month range")
    void shouldReturnDatesWithinRange() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> result = query.findByDateRange(
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 15));

        the(result.size()).shouldEqual(6);
        the(result.getFirst().date()).shouldEqual(LocalDate.of(2026, 4, 10));
        the(result.getLast().date()).shouldEqual(LocalDate.of(2026, 4, 15));
    }

    @Test
    @DisplayName("should return only populated dates when range spans unpopulated months")
    void shouldReturnOnlyPopulatedDates() {
        populator.populate(YearMonth.of(2026, 1));
        populator.populate(YearMonth.of(2026, 3));

        List<CalendarDate> result = query.findByDateRange(
                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 15));

        // Jan 15-31 = 17 days, Mar 1-15 = 15 days, Feb excluded = 32 total
        long janCount = result.stream().filter(d -> d.date().getMonthValue() == 1).count();
        long febCount = result.stream().filter(d -> d.date().getMonthValue() == 2).count();
        long marCount = result.stream().filter(d -> d.date().getMonthValue() == 3).count();

        the(janCount).shouldEqual(17L);
        the(febCount).shouldEqual(0L);
        the(marCount).shouldEqual(15L);
    }

    @Test
    @DisplayName("should return empty list for empty calendar")
    void shouldReturnEmptyForEmptyCalendar() {
        List<CalendarDate> result = query.findByDateRange(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        the(result.isEmpty()).shouldBeTrue();
    }

    @Test
    @DisplayName("should return dates with all tags from all tag-sets")
    void shouldReturnDatesWithAllTags() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> result = query.findByDateRange(
                LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 5));

        CalendarDate easterSunday = result.getFirst();
        the(easterSunday.tagsByTagSet().containsKey("WeekdayType")).shouldBeTrue();
        the(easterSunday.tagsByTagSet().containsKey("ChristianHoliday")).shouldBeTrue();
        the(easterSunday.tagsByTagSet().containsKey("NorwegianRedDay")).shouldBeTrue();
    }

    @Test
    @DisplayName("should return NorwegianRedDay tags for April 2026 filtered query")
    void shouldReturnFilteredByTagSet() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> result = query.findByTagSetInRange("NorwegianRedDay",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        // April 2026 Norwegian red days: Maundy Thursday (2), Good Friday (3),
        // Easter Sunday (5), Easter Monday (6) = 4 days
        the(result.size()).shouldEqual(4);
        the(result.getFirst().date()).shouldEqual(LocalDate.of(2026, 4, 2));
        the(result.getLast().date()).shouldEqual(LocalDate.of(2026, 4, 6));
    }

    @Test
    @DisplayName("should return only requested tag-set's tags in filtered query")
    void shouldReturnOnlyRequestedTagSetTags() {
        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> result = query.findByTagSetInRange("NorwegianRedDay",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        for (CalendarDate date : result) {
            the(date.tagsByTagSet().containsKey("NorwegianRedDay")).shouldBeTrue();
            the(date.tagsByTagSet().containsKey("WeekdayType")).shouldBeFalse();
        }
    }
}
