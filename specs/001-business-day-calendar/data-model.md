# Data Model: Business Day Calendar

## Entities

### CalendarDate

A specific date that has been populated in the calendar.

| Field      | Type          | Constraints                          |
|------------|---------------|--------------------------------------|
| date       | LocalDate     | Primary key, not null                |
| yearMonth  | YearMonth     | Derived from date, indexed           |
| tags       | Set of Tag    | Zero or more, from any tag-set       |

**Identity**: Uniquely identified by `date`.
**Lifecycle**: Created during month population. Deleted and recreated
on re-population.

### Tag

A label applied to a CalendarDate, belonging to exactly one TagSet.

| Field   | Type   | Constraints                            |
|---------|--------|----------------------------------------|
| tagSet  | TagSet | Not null, identifies the owning set    |
| name    | String | Not null, unique within the tag-set    |

**Identity**: Uniquely identified by (tagSet, name).
**Immutability**: Tags are value objects defined at compile time.

### TagSet

A named grouping of related tags representing a single classification
concern.

| Field        | Type          | Constraints                      |
|--------------|---------------|----------------------------------|
| name         | String        | Unique identifier                |
| tags         | Set of Tag    | The valid members of this set    |
| dependencies | Set of TagSet | Tag-sets that must be applied first |

**Identity**: Uniquely identified by `name`.
**Constraint**: Dependencies MUST form a directed acyclic graph (DAG).

### Concrete Tag-Sets

#### WeekdayType

No dependencies.

| Tag     | Rule                                    |
|---------|-----------------------------------------|
| WORKDAY | Monday through Friday                   |
| WEEKEND | Saturday and Sunday                     |

#### ChristianHoliday

No dependencies. Computes Easter via the Computus algorithm.

| Tag                       | Date Rule             |
|---------------------------|-----------------------|
| NEW_YEARS_DAY             | 1 January (fixed)     |
| MAUNDY_THURSDAY           | Easter - 3 days       |
| GOOD_FRIDAY               | Easter - 2 days       |
| EASTER_SUNDAY             | Easter (computed)     |
| EASTER_MONDAY             | Easter + 1 day        |
| ASCENSION_DAY             | Easter + 39 days      |
| WHIT_SUNDAY               | Easter + 49 days      |
| WHIT_MONDAY               | Easter + 50 days      |
| CHRISTMAS_DAY             | 25 December (fixed)   |
| SECOND_DAY_OF_CHRISTMAS   | 26 December (fixed)   |

#### NorwegianRedDay

Depends on: ChristianHoliday.

| Tag               | Date Rule                              |
|-------------------|----------------------------------------|
| NEW_YEARS_DAY     | 1 January (fixed)                      |
| MAUNDY_THURSDAY   | From ChristianHoliday tag on date      |
| GOOD_FRIDAY       | From ChristianHoliday tag on date      |
| EASTER_SUNDAY     | From ChristianHoliday tag on date      |
| EASTER_MONDAY     | From ChristianHoliday tag on date      |
| LABOUR_DAY        | 1 May (fixed)                          |
| CONSTITUTION_DAY  | 17 May (fixed)                         |
| ASCENSION_DAY     | From ChristianHoliday tag on date      |
| WHIT_SUNDAY       | From ChristianHoliday tag on date      |
| WHIT_MONDAY       | From ChristianHoliday tag on date      |
| CHRISTMAS_DAY     | 25 December (fixed)                    |
| BOXING_DAY        | 26 December (fixed)                    |

## Relationships

```text
TagSet 1──* Tag           (a tag-set defines its valid tags)
TagSet *──* TagSet        (dependency: applied-before relationship, DAG)
CalendarDate 1──* Tag     (a date carries tags from multiple tag-sets)
```

## Database Schema

### Table: calendar_date

| Column     | Type    | Constraints |
|------------|---------|-------------|
| date       | DATE    | PRIMARY KEY |
| year_month | CHAR(7) | NOT NULL, indexed (format: "2026-04") |

### Table: calendar_date_tag

| Column   | Type         | Constraints                       |
|----------|--------------|-----------------------------------|
| date     | DATE         | NOT NULL, FK → calendar_date.date |
| tag_set  | VARCHAR(100) | NOT NULL                          |
| tag      | VARCHAR(100) | NOT NULL                          |

**Primary key**: (date, tag_set, tag)
**Indexes**: (tag_set, date) for tag-set-filtered queries (FR-009)

### Re-population Strategy

To re-populate month M:
1. DELETE FROM calendar_date_tag WHERE date IN
   (SELECT date FROM calendar_date WHERE year_month = M)
2. DELETE FROM calendar_date WHERE year_month = M
3. INSERT new dates and tags

This ensures atomicity: all tags from all tag-sets for the month are
replaced in one operation.
