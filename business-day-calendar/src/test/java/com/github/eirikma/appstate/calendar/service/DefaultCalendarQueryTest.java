package com.github.eirikma.appstate.calendar.service;

import com.github.eirikma.appstate.calendar.model.CalendarDate;
import com.github.eirikma.appstate.calendar.repository.CalendarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("DefaultCalendarQuery")
class DefaultCalendarQueryTest {

    @Test
    @DisplayName("should delegate findByDateRange to repository")
    void shouldDelegateFindByDateRange() {
        CalendarDate date = new CalendarDate(LocalDate.of(2026, 4, 1), Map.of("WeekdayType", Set.of("WORKDAY")));
        StubRepository repository = new StubRepository(List.of(date));
        DefaultCalendarQuery query = new DefaultCalendarQuery(repository);

        List<CalendarDate> result = query.findByDateRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        the(result.size()).shouldEqual(1);
        the(result.getFirst().date()).shouldEqual(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("should delegate findByTagSetInRange to repository")
    void shouldDelegateFindByTagSetInRange() {
        CalendarDate date = new CalendarDate(LocalDate.of(2026, 4, 5), Map.of("ChristianHoliday", Set.of("EASTER_SUNDAY")));
        StubRepository repository = new StubRepository(List.of(date));
        DefaultCalendarQuery query = new DefaultCalendarQuery(repository);

        List<CalendarDate> result = query.findByTagSetInRange("ChristianHoliday",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        the(result.size()).shouldEqual(1);
        the(result.getFirst().tagsByTagSet().get("ChristianHoliday")).shouldContain("EASTER_SUNDAY");
    }

    private static class StubRepository implements CalendarRepository {
        private final List<CalendarDate> dates;

        StubRepository(List<CalendarDate> dates) {
            this.dates = dates;
        }

        @Override
        public void deleteMonth(YearMonth yearMonth) {}

        @Override
        public void saveDates(List<LocalDate> dates, YearMonth yearMonth) {}

        @Override
        public void saveTags(Map<LocalDate, Set<String>> tagsByDate, String tagSet) {}

        @Override
        public List<CalendarDate> findDatesByRange(LocalDate from, LocalDate to) {
            return List.copyOf(dates);
        }

        @Override
        public List<CalendarDate> findDatesByTagSetInRange(String tagSet, LocalDate from, LocalDate to) {
            return List.copyOf(dates);
        }
    }
}
