package com.github.eirikma.appstate.calendar.registry;

import com.github.eirikma.appstate.calendar.model.CalendarDate;
import com.github.eirikma.appstate.calendar.model.WeekdayType;
import com.github.eirikma.appstate.calendar.repository.JdbcCalendarRepository;
import com.github.eirikma.appstate.calendar.service.CalendarPopulator;
import com.github.eirikma.appstate.calendar.tagging.TaggingService;
import com.github.eirikma.appstate.calendar.tagging.WeekdayTypeTagger;
import com.github.eirikma.appstate.test.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("Custom tag-set registration integration test")
class CustomTagSetIT extends IntegrationTestBase {

    private JdbcCalendarRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcCalendarRepository(dataSource());
        repository.deleteMonth(YearMonth.of(2026, 4));
    }

    @Test
    @DisplayName("should apply custom tag-set after its dependency WeekdayType")
    void shouldApplyCustomTagSetAfterDependency() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(new WeekdayTypeTagger());
        registry.register(new WorkdayMarkerTagger());
        CalendarPopulator populator = new CalendarPopulator(registry, repository);

        populator.populate(YearMonth.of(2026, 4));

        List<CalendarDate> dates = repository.findDatesByTagSetInRange("WorkdayMarker",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        // April 2026 has 22 workdays (Mon-Fri)
        the(dates.size()).shouldEqual(22);
        for (CalendarDate date : dates) {
            the(date.tagsByTagSet().get("WorkdayMarker")).shouldContain("IS_WORKDAY");
        }
    }

    /**
     * A custom tagging service that tags workdays based on WeekdayType.
     */
    private static class WorkdayMarkerTagger implements TaggingService {
        @Override
        public String tagSet() {
            return "WorkdayMarker";
        }

        @Override
        public Set<String> dependencies() {
            return Set.of(WeekdayType.TAG_SET);
        }

        @Override
        public Map<LocalDate, Set<String>> tag(YearMonth yearMonth, Map<LocalDate, Set<String>> existingTags) {
            Map<LocalDate, Set<String>> result = new LinkedHashMap<>();
            existingTags.forEach((date, tags) -> {
                if (tags.contains("WORKDAY")) {
                    result.put(date, Set.of("IS_WORKDAY"));
                }
            });
            return result;
        }
    }
}
