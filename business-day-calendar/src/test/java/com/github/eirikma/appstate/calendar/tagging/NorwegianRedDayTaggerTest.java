package com.github.eirikma.appstate.calendar.tagging;

import com.github.eirikma.appstate.calendar.model.NorwegianRedDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("NorwegianRedDayTagger")
class NorwegianRedDayTaggerTest {

    private final NorwegianRedDayTagger tagger = new NorwegianRedDayTagger();

    @Test
    @DisplayName("should tag New Year's Day in January")
    void shouldTagNewYearsDay() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 1), Map.of());

        the(result.get(LocalDate.of(2026, 1, 1))).shouldContain("NEW_YEARS_DAY");
    }

    @Test
    @DisplayName("should tag Labour Day and Constitution Day in May")
    void shouldTagMayFixedDays() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 5), Map.of());

        the(result.get(LocalDate.of(2026, 5, 1))).shouldContain("LABOUR_DAY");
        the(result.get(LocalDate.of(2026, 5, 17))).shouldContain("CONSTITUTION_DAY");
    }

    @Test
    @DisplayName("should tag Christmas Day and Boxing Day in December")
    void shouldTagDecemberFixedDays() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 12), Map.of());

        the(result.get(LocalDate.of(2026, 12, 25))).shouldContain("CHRISTMAS_DAY");
        the(result.get(LocalDate.of(2026, 12, 26))).shouldContain("BOXING_DAY");
    }

    @Test
    @DisplayName("should tag movable red days from ChristianHoliday existing tags in April 2026")
    void shouldTagMovableRedDaysFromExistingTags() {
        // Easter 2026 is April 5: Maundy Thursday=Apr 2, Good Friday=Apr 3, Easter Monday=Apr 6
        Map<LocalDate, Set<String>> existingTags = Map.of(
                LocalDate.of(2026, 4, 2), Set.of("MAUNDY_THURSDAY"),
                LocalDate.of(2026, 4, 3), Set.of("GOOD_FRIDAY"),
                LocalDate.of(2026, 4, 5), Set.of("EASTER_SUNDAY"),
                LocalDate.of(2026, 4, 6), Set.of("EASTER_MONDAY")
        );

        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 4), existingTags);

        the(result.get(LocalDate.of(2026, 4, 2))).shouldContain("MAUNDY_THURSDAY");
        the(result.get(LocalDate.of(2026, 4, 3))).shouldContain("GOOD_FRIDAY");
        the(result.get(LocalDate.of(2026, 4, 5))).shouldContain("EASTER_SUNDAY");
        the(result.get(LocalDate.of(2026, 4, 6))).shouldContain("EASTER_MONDAY");
    }

    @Test
    @DisplayName("should tag Ascension Day, Whit Sunday, and Whit Monday from existing tags in May 2026")
    void shouldTagMovableRedDaysInMay() {
        // Easter 2026 Apr 5: Ascension=May 14, Whit Sunday=May 24, Whit Monday=May 25
        Map<LocalDate, Set<String>> existingTags = Map.of(
                LocalDate.of(2026, 5, 14), Set.of("ASCENSION_DAY"),
                LocalDate.of(2026, 5, 24), Set.of("WHIT_SUNDAY"),
                LocalDate.of(2026, 5, 25), Set.of("WHIT_MONDAY")
        );

        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 5), existingTags);

        the(result.get(LocalDate.of(2026, 5, 14))).shouldContain("ASCENSION_DAY");
        the(result.get(LocalDate.of(2026, 5, 24))).shouldContain("WHIT_SUNDAY");
        the(result.get(LocalDate.of(2026, 5, 25))).shouldContain("WHIT_MONDAY");
    }

    @Test
    @DisplayName("should return empty for month with no red days and no existing tags")
    void shouldReturnEmptyForMonthWithNoRedDays() {
        Map<LocalDate, Set<String>> result = tagger.tag(YearMonth.of(2026, 8), Map.of());

        the(result.isEmpty()).shouldBeTrue();
    }

    @Test
    @DisplayName("should report NorwegianRedDay as tag set name")
    void shouldReportTagSetName() {
        the(tagger.tagSet()).shouldEqual(NorwegianRedDay.TAG_SET);
    }

    @Test
    @DisplayName("should depend on ChristianHoliday")
    void shouldDependOnChristianHoliday() {
        the(tagger.dependencies()).shouldContain("ChristianHoliday");
    }
}
