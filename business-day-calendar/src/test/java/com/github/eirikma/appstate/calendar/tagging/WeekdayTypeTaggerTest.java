package com.github.eirikma.appstate.calendar.tagging;

import com.github.eirikma.appstate.calendar.model.WeekdayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("WeekdayTypeTagger")
class WeekdayTypeTaggerTest {

    private final WeekdayTypeTagger tagger = new WeekdayTypeTagger();

    @Test
    @DisplayName("should tag Monday through Friday as WORKDAY")
    void shouldTagWeekdaysAsWorkday() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 4), Map.of());

        // April 2026: 1st is Wednesday
        the(result.get(LocalDate.of(2026, 4, 1))).shouldContain("WORKDAY");
        the(result.get(LocalDate.of(2026, 4, 2))).shouldContain("WORKDAY");
        the(result.get(LocalDate.of(2026, 4, 3))).shouldContain("WORKDAY");
        the(result.get(LocalDate.of(2026, 4, 6))).shouldContain("WORKDAY");
        the(result.get(LocalDate.of(2026, 4, 10))).shouldContain("WORKDAY");
    }

    @Test
    @DisplayName("should tag Saturday and Sunday as WEEKEND")
    void shouldTagWeekendsAsWeekend() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 4), Map.of());

        // April 4 is Saturday, April 5 is Sunday
        the(result.get(LocalDate.of(2026, 4, 4))).shouldContain("WEEKEND");
        the(result.get(LocalDate.of(2026, 4, 5))).shouldContain("WEEKEND");
        the(result.get(LocalDate.of(2026, 4, 11))).shouldContain("WEEKEND");
        the(result.get(LocalDate.of(2026, 4, 12))).shouldContain("WEEKEND");
    }

    @Test
    @DisplayName("should tag all 30 days in April")
    void shouldTagAllDaysInMonth() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 4), Map.of());

        the(result.size()).shouldEqual(30);
    }

    @Test
    @DisplayName("should report WeekdayType as tag set name")
    void shouldReportTagSetName() {
        the(tagger.tagSet()).shouldEqual(WeekdayType.TAG_SET);
    }

    @Test
    @DisplayName("should have no dependencies")
    void shouldHaveNoDependencies() {
        the(tagger.dependencies().isEmpty()).shouldBeTrue();
    }
}
