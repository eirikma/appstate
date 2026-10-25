package com.github.eirikma.appstate.calendar.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("TagSet")
class TagSetTest {

    @Test
    @DisplayName("should name WeekdayType as the tag set of a weekday type")
    void shouldNameWeekdayTypeTagSet() {
        TagSet tagSet = WeekdayType.WORKDAY;

        the(tagSet.getTagSetName()).shouldEqual(WeekdayType.TAG_SET);
    }

    @Test
    @DisplayName("should name ChristianHoliday as the tag set of a Christian holiday")
    void shouldNameChristianHolidayTagSet() {
        TagSet tagSet = ChristianHoliday.EASTER_SUNDAY;

        the(tagSet.getTagSetName()).shouldEqual(ChristianHoliday.TAG_SET);
    }

    @Test
    @DisplayName("should name NorwegianRedDay as the tag set of a Norwegian red day")
    void shouldNameNorwegianRedDayTagSet() {
        TagSet tagSet = NorwegianRedDay.CONSTITUTION_DAY;

        the(tagSet.getTagSetName()).shouldEqual(NorwegianRedDay.TAG_SET);
    }
}
