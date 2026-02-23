package com.github.eirikma.appstate.calendar.tagging;

import com.github.eirikma.appstate.calendar.model.ChristianHoliday;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("ChristianHolidayTagger")
class ChristianHolidayTaggerTest {

    private final ChristianHolidayTagger tagger = new ChristianHolidayTagger();

    @Test
    @DisplayName("should tag New Year's Day in January")
    void shouldTagNewYearsDay() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 1), Map.of());

        the(result.get(LocalDate.of(2026, 1, 1))).shouldContain("NEW_YEARS_DAY");
    }

    @Test
    @DisplayName("should tag Christmas Day and Second Day of Christmas in December")
    void shouldTagChristmasDays() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 12), Map.of());

        the(result.get(LocalDate.of(2026, 12, 25))).shouldContain("CHRISTMAS_DAY");
        the(result.get(LocalDate.of(2026, 12, 26))).shouldContain("SECOND_DAY_OF_CHRISTMAS");
    }

    @Test
    @DisplayName("should tag Easter holidays in April 2026 (Easter April 5)")
    void shouldTagEasterHolidays() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 4), Map.of());

        the(result.get(LocalDate.of(2026, 4, 2))).shouldContain("MAUNDY_THURSDAY");
        the(result.get(LocalDate.of(2026, 4, 3))).shouldContain("GOOD_FRIDAY");
        the(result.get(LocalDate.of(2026, 4, 5))).shouldContain("EASTER_SUNDAY");
        the(result.get(LocalDate.of(2026, 4, 6))).shouldContain("EASTER_MONDAY");
    }

    @Test
    @DisplayName("should tag Ascension Day in May 2026 (Easter + 39 = May 14)")
    void shouldTagAscensionDay() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 5), Map.of());

        the(result.get(LocalDate.of(2026, 5, 14))).shouldContain("ASCENSION_DAY");
    }

    @Test
    @DisplayName("should tag Whit Sunday and Whit Monday in May 2026 (Easter + 49/50)")
    void shouldTagWhitsun() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 5), Map.of());

        the(result.get(LocalDate.of(2026, 5, 24))).shouldContain("WHIT_SUNDAY");
        the(result.get(LocalDate.of(2026, 5, 25))).shouldContain("WHIT_MONDAY");
    }

    @Test
    @DisplayName("should return empty map for month with no Christian holidays")
    void shouldReturnEmptyForMonthWithNoHolidays() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 8), Map.of());

        the(result.isEmpty()).shouldBeTrue();
    }

    @Test
    @DisplayName("should report ChristianHoliday as tag set name")
    void shouldReportTagSetName() {
        the(tagger.tagSet()).shouldEqual(ChristianHoliday.TAG_SET);
    }

    @Test
    @DisplayName("should have no dependencies")
    void shouldHaveNoDependencies() {
        the(tagger.dependencies().isEmpty()).shouldBeTrue();
    }
}
