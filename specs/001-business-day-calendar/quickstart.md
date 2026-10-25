# Quickstart: Business Day Calendar

## Add the dependency

Add the `business-day-calendar` module to your project's dependencies.
The module requires a configured data source for its default JDBC
repository implementation.

## Basic usage

### 1. Set up the registry and populator

Create a TagSetRegistry and register the built-in tagging services.
The three shipped tag-sets are:

- **WeekdayType**: Tags every date as WORKDAY or WEEKEND.
- **ChristianHoliday**: Tags Christian observances (Easter cycle,
  Christmas, New Year's Day).
- **NorwegianRedDay**: Tags the 12 Norwegian public holidays. Depends
  on ChristianHoliday.

Register services in any order - the registry resolves execution order
from declared dependencies.

### 2. Populate a month

Call `CalendarPopulator.populate(YearMonth.of(2026, 4))` to populate
April 2026. The populator:

1. Creates a CalendarDate for each day in the month.
2. Runs WeekdayType tagger (no dependencies) - marks workdays and
   weekends.
3. Runs ChristianHoliday tagger (no dependencies) - marks Easter
   Sunday (April 5), Good Friday (April 3), Easter Monday (April 6),
   Maundy Thursday (April 2).
4. Runs NorwegianRedDay tagger (depends on ChristianHoliday) - reads
   the Christian holiday tags to mark Skjærtorsdag, Langfredag,
   Første påskedag, and Andre påskedag as red days.

### 3. Query dates

**All dates in a range**:
```
calendarQuery.findByDateRange(
    LocalDate.of(2026, 4, 1),
    LocalDate.of(2026, 4, 30))
```
Returns 30 CalendarDate objects, each with tags from all three
tag-sets.

**Only Norwegian red days**:
```
calendarQuery.findByTagSetInRange(
    NorwegianRedDay.TAG_SET,
    LocalDate.of(2026, 4, 1),
    LocalDate.of(2026, 4, 30))
```
Returns 4 dates (Maundy Thursday, Good Friday, Easter Sunday, Easter
Monday), each with only their NorwegianRedDay tags.

### 4. Add a custom tag-set

Implement the `TaggingService` interface:

1. Define your tag-set identifier and tags.
2. Declare dependencies (e.g., depend on ChristianHoliday if your
   tags are Easter-relative).
3. Implement the `tag(yearMonth, existingTags)` method.
4. Register with the TagSetRegistry.

The populator automatically includes your service in the correct
execution position based on declared dependencies.

## Re-population

Calling `populate()` on an already-populated month replaces all
existing dates and tags for that month. This is useful when:

- A new tag-set has been registered.
- Tag computation logic has been corrected.
- The calendar needs to be refreshed.

## Integration testing

The `tests-common` module provides an `IntegrationTestBase` superclass
that:

- Starts a PostgreSQL 17 testcontainer.
- Runs Liquibase changeLogs from all registered modules.
- Provides a clean database for each test.

Extend this class in your integration tests to verify repository
behaviour and end-to-end population/query flows.
