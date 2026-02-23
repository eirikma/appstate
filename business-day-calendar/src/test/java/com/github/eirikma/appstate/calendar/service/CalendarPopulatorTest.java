package com.github.eirikma.appstate.calendar.service;

import com.github.eirikma.appstate.calendar.model.WeekdayType;
import com.github.eirikma.appstate.calendar.registry.DefaultTagSetRegistry;
import com.github.eirikma.appstate.calendar.repository.CalendarRepository;
import com.github.eirikma.appstate.calendar.model.CalendarDate;
import com.github.eirikma.appstate.calendar.tagging.ChristianHolidayTagger;
import com.github.eirikma.appstate.calendar.tagging.NorwegianRedDayTagger;
import com.github.eirikma.appstate.calendar.tagging.WeekdayTypeTagger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("CalendarPopulator")
class CalendarPopulatorTest {

    @Test
    @DisplayName("should populate all dates in a month with WeekdayType tags")
    void shouldPopulateWithWeekdayTypeTags() {
        InMemoryCalendarRepository repository = new InMemoryCalendarRepository();
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(new WeekdayTypeTagger());
        CalendarPopulator populator = new CalendarPopulator(registry, repository);

        populator.populate(YearMonth.of(2026, 4));

        the(repository.savedDates.size()).shouldEqual(30);
        the(repository.savedTagSets.containsKey(WeekdayType.TAG_SET)).shouldBeTrue();

        // April 1 2026 is Wednesday = WORKDAY
        Set<String> april1Tags = repository.savedTagSets.get(WeekdayType.TAG_SET)
                .get(LocalDate.of(2026, 4, 1));
        the(april1Tags).shouldContain("WORKDAY");

        // April 4 2026 is Saturday = WEEKEND
        Set<String> april4Tags = repository.savedTagSets.get(WeekdayType.TAG_SET)
                .get(LocalDate.of(2026, 4, 4));
        the(april4Tags).shouldContain("WEEKEND");
    }

    @Test
    @DisplayName("should populate with all 3 tag-sets in dependency order")
    void shouldPopulateWithAllThreeTagSets() {
        InMemoryCalendarRepository repository = new InMemoryCalendarRepository();
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(new WeekdayTypeTagger());
        registry.register(new ChristianHolidayTagger());
        registry.register(new NorwegianRedDayTagger());
        CalendarPopulator populator = new CalendarPopulator(registry, repository);

        populator.populate(YearMonth.of(2026, 4));

        the(repository.savedDates.size()).shouldEqual(30);
        the(repository.savedTagSets.size()).shouldEqual(3);

        // Easter Sunday April 5 should be tagged by ChristianHoliday
        Set<String> christianApril5 = repository.savedTagSets.get("ChristianHoliday")
                .get(LocalDate.of(2026, 4, 5));
        the(christianApril5).shouldContain("EASTER_SUNDAY");

        // Easter Sunday April 5 should also be tagged by NorwegianRedDay
        Set<String> redDayApril5 = repository.savedTagSets.get("NorwegianRedDay")
                .get(LocalDate.of(2026, 4, 5));
        the(redDayApril5).shouldContain("EASTER_SUNDAY");

        // Maundy Thursday April 2 should be tagged by both
        Set<String> christianApril2 = repository.savedTagSets.get("ChristianHoliday")
                .get(LocalDate.of(2026, 4, 2));
        the(christianApril2).shouldContain("MAUNDY_THURSDAY");

        Set<String> redDayApril2 = repository.savedTagSets.get("NorwegianRedDay")
                .get(LocalDate.of(2026, 4, 2));
        the(redDayApril2).shouldContain("MAUNDY_THURSDAY");
    }

    @Test
    @DisplayName("should delete existing month data before populating")
    void shouldDeleteExistingMonthData() {
        InMemoryCalendarRepository repository = new InMemoryCalendarRepository();
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(new WeekdayTypeTagger());
        CalendarPopulator populator = new CalendarPopulator(registry, repository);

        populator.populate(YearMonth.of(2026, 4));
        the(repository.deleteMonthCalled).shouldBeTrue();
    }

    @Test
    @DisplayName("should record tag-sets in topological order")
    void shouldRecordTagSetsInTopologicalOrder() {
        InMemoryCalendarRepository repository = new InMemoryCalendarRepository();
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(new WeekdayTypeTagger());
        registry.register(new ChristianHolidayTagger());
        registry.register(new NorwegianRedDayTagger());
        CalendarPopulator populator = new CalendarPopulator(registry, repository);

        populator.populate(YearMonth.of(2026, 4));

        // NorwegianRedDay must come after ChristianHoliday
        int christianIndex = repository.saveTagsOrder.indexOf("ChristianHoliday");
        int norwegianIndex = repository.saveTagsOrder.indexOf("NorwegianRedDay");
        the(christianIndex < norwegianIndex).shouldBeTrue();
    }

    private static class InMemoryCalendarRepository implements CalendarRepository {
        final List<LocalDate> savedDates = new ArrayList<>();
        final Map<String, Map<LocalDate, Set<String>>> savedTagSets = new LinkedHashMap<>();
        final List<String> saveTagsOrder = new ArrayList<>();
        boolean deleteMonthCalled = false;

        @Override
        public void deleteMonth(YearMonth yearMonth) {
            deleteMonthCalled = true;
            savedDates.clear();
            savedTagSets.clear();
        }

        @Override
        public void saveDates(List<LocalDate> dates, YearMonth yearMonth) {
            savedDates.addAll(dates);
        }

        @Override
        public void saveTags(Map<LocalDate, Set<String>> tagsByDate, String tagSet) {
            savedTagSets.put(tagSet, tagsByDate);
            saveTagsOrder.add(tagSet);
        }

        @Override
        public List<CalendarDate> findDatesByRange(LocalDate from, LocalDate to) {
            return List.of();
        }

        @Override
        public List<CalendarDate> findDatesByTagSetInRange(String tagSet, LocalDate from, LocalDate to) {
            return List.of();
        }
    }
}
