# Feature Specification: Business Day Calendar

**Feature Branch**: `001-business-day-calendar`
**Created**: 2026-02-14
**Status**: Draft
**Input**: User description: "The first feature module is 'business day calendar'. Calendar dates stored with tags from various tag-sets, tagging services with dependency ordering, and date/tag query capabilities."

## Clarifications

### Session 2026-02-14

- Q: Which concrete tag-set implementations are in scope? → A: Three:
  weekend/workday, Christian holidays, and Norwegian red calendar days.
  Norwegian red days depend on Christian holidays (movable red days are
  computed from the Easter date). Weekend/workday has no dependencies.
  Christian holidays has no dependencies.
- Q: Should the module own a concrete database schema or define a
  storage abstraction? → A: Module defines a storage abstraction
  (repository interface); ships with a default implementation.
  Planning note: each module owns its own Liquibase changeLog,
  exported via a Spring customizer bean. A new "tests-common" module
  provides integration test infrastructure (PostgreSQL 17
  testcontainer, Liquibase execution, test superclass) so integration
  tests contain only domain logic.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Populate a Month with Tagged Dates (Priority: P1)

A consumer of the calendar module requests that a specific month be
populated. The system creates date entries for every day in the month
and runs all registered tagging services in dependency order. Tagging
services with no dependencies (e.g., weekend/workday) run first.
Services that depend on other tag-sets (e.g., a national holidays
service depending on Christian holidays) run only after their
dependencies have completed. At the end, every date in the month
carries all applicable tags from every registered tag-set.

**Why this priority**: This is the foundational operation. Without
populating and tagging dates, no queries or downstream processing are
possible.

**Independent Test**: Can be fully tested by populating a single month
and verifying that each date carries the correct tags from all
registered tag-sets, applied in the correct dependency order.

**Acceptance Scenarios**:

1. **Given** a registered weekend/workday tagging service and an
   unpopulated month, **When** the consumer requests population of that
   month, **Then** every date in the month is created and tagged as
   either WORKDAY or WEEKEND according to its day of week.
2. **Given** registered weekend/workday, Christian holidays, and
   Norwegian red calendar days tagging services (where Norwegian red
   days depends on Christian holidays), **When** the consumer requests
   population of a month, **Then** Christian holidays runs before
   Norwegian red days, and the Norwegian red days service can read the
   Christian holiday tags to determine movable red days relative to
   Easter.
3. **Given** a month that has already been populated, **When** the
   consumer requests population of the same month again, **Then** the
   existing tags are replaced with freshly computed tags.

---

### User Story 2 - Query Dates in a Date Range (Priority: P2)

A consumer queries the calendar for all dates within a specified date
range and receives each date together with all of its tags. This
enables downstream logic such as counting business days between two
dates or listing all tagged events in a period.

**Why this priority**: Querying is the primary read path. Once dates
are populated, consumers need to retrieve them to derive business
value.

**Independent Test**: Can be tested by populating a known month,
querying a sub-range, and verifying the returned dates and their tags
match expectations.

**Acceptance Scenarios**:

1. **Given** a populated month, **When** the consumer queries a date
   range within that month, **Then** all dates in the range are
   returned with their complete set of tags.
2. **Given** a date range that spans two months where only the first is
   populated, **When** the consumer queries that range, **Then** only
   dates from the populated month are returned.
3. **Given** an empty calendar, **When** the consumer queries any date
   range, **Then** an empty result is returned.

---

### User Story 3 - Query Dates by Tag-Set in a Date Range (Priority: P3)

A consumer queries the calendar for dates within a date range that
carry at least one tag from a specified tag-set. For example, "all
dates tagged by the Christian Holidays tag-set in December 2026". The
result includes only those dates that were tagged by the requested
tag-set, together with the tags from that tag-set.

**Why this priority**: Filtered queries allow targeted lookups (e.g.,
"which days in Q1 are Christian holidays?") without the consumer having
to filter a full date-range result.

**Independent Test**: Can be tested by populating a month with multiple
tag-sets, querying for a single tag-set, and verifying only dates
carrying tags from that tag-set are returned.

**Acceptance Scenarios**:

1. **Given** a populated month with weekend/workday and Christian
   holiday tag-sets, **When** the consumer queries for Christian holiday
   tags in that month, **Then** only dates carrying Christian holiday
   tags are returned, each with its Christian holiday tag(s).
2. **Given** a populated month, **When** the consumer queries for a
   tag-set that tagged no dates in the range, **Then** an empty result
   is returned.

---

### User Story 4 - Register a Custom Tag-Set (Priority: P4)

A developer adds a new tag-set and its corresponding tagging service to
the system. The new tag-set can declare dependencies on existing
tag-sets. Once registered, the system automatically incorporates the
new tagging service into the processing order when populating months.

**Why this priority**: Extensibility is essential for the module's
long-term value (national holidays, business-specific calendars), but
the core populate-and-query workflow must work first.

**Independent Test**: Can be tested by registering a custom tag-set
that depends on the weekend/workday tag-set, populating a month, and
verifying the custom tags are applied after the weekend/workday tags.

**Acceptance Scenarios**:

1. **Given** a new tag-set with a tagging service and a declared
   dependency on an existing tag-set, **When** the developer registers
   it and populates a month, **Then** the new service runs after its
   dependency and the new tags appear on the appropriate dates.
2. **Given** a new tag-set whose declared dependency is not registered,
   **When** the developer attempts to register it, **Then** the system
   rejects the registration with a clear error indicating the missing
   dependency.
3. **Given** a registration that would create a circular dependency,
   **When** the developer attempts to register it, **Then** the system
   rejects the registration with a clear error describing the cycle.

---

### Edge Cases

- What happens when a tagging service encounters a date it cannot
  classify? It MUST leave the date untagged by its tag-set rather than
  failing the entire population.
- What happens when circular dependencies exist among tag-sets? The
  system MUST detect the cycle and reject the registration.
- What happens when a date range spans months that are not yet
  populated? Only populated dates are returned; unpopulated months are
  silently excluded.
- What happens when the same month is populated twice? The second
  population replaces all existing tags for that month.
- What happens when a tag-set has multiple dependencies? All
  dependencies MUST complete before the dependent service runs.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST store calendar dates with their associated
  tags persistently via a storage abstraction. The module MUST define
  a repository interface for persistence and ship with a default
  implementation.
- **FR-002**: System MUST support multiple tag-sets, each representing
  a distinct classification concern. The initial delivery MUST include
  three concrete tag-sets: weekend/workday, Christian holidays, and
  Norwegian red calendar days.
- **FR-003**: Each tag-set MUST be paired with a tagging service that
  can tag all dates in a given month.
- **FR-004**: Tag-sets MUST be able to declare dependencies on other
  tag-sets.
- **FR-005**: When populating a month, the system MUST resolve tag-set
  dependencies and execute tagging services in a valid topological
  order.
- **FR-006**: The system MUST detect circular dependencies and reject
  them at registration time with a descriptive error.
- **FR-007**: A tagging service MUST be able to read tags already
  applied by its dependency tag-sets when computing its own tags.
- **FR-008**: System MUST support querying all dates within a given
  date range, returning each date with its complete set of tags.
- **FR-009**: System MUST support querying dates within a date range
  filtered to a specific tag-set, returning only dates that carry tags
  from that tag-set.
- **FR-010**: Re-populating an already-populated month MUST replace
  all existing tags for that month.
- **FR-011**: A tagging service that finds no dates to tag in a month
  MUST complete without error.
- **FR-012**: The Christian holidays tagging service MUST compute
  movable feast dates (Easter and dependent days) using a well-known
  algorithm (e.g., the Computus).
- **FR-013**: The Norwegian red calendar days tagging service MUST
  depend on the Christian holidays tag-set and use the marked Christian
  holiday dates to determine movable Norwegian red days.

### Key Entities

- **Calendar Date**: A specific date (year, month, day) with a
  collection of tags from zero or more tag-sets.
- **Tag**: A named label belonging to exactly one tag-set. A date may
  carry zero or more tags from any given tag-set.
- **Tag-Set**: A named grouping of related tags representing a single
  classification concern. Defines which tags are valid members.
- **Tagging Service**: A service paired with exactly one tag-set. Given
  a month and read access to previously applied tags, it determines
  which tags from its tag-set apply to each date in the month.
- **Tag-Set Dependency**: A declared relationship where one tag-set
  requires another to be applied first. Dependencies form a directed
  acyclic graph.

### Assumptions

- Tag-sets are defined at compile time (as enumerations) rather than at
  runtime.
- A date may carry multiple tags from the same tag-set (e.g., a date
  may be both "Good Friday" and "End of Lent" in a Christian holidays
  tag-set).
- The granularity of population is one calendar month at a time.
- Tagging services are deterministic: given the same month and the same
  prior tags, they produce the same result.
- Persistence is accessed through a repository abstraction; the module
  does not couple to a specific storage technology at the spec level.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Populating a month with the 3 built-in tag-sets
  (weekend/workday, Christian holidays, Norwegian red calendar days)
  completes correctly in a single operation with all dates tagged,
  respecting the dependency from Norwegian red days to Christian
  holidays.
- **SC-002**: A date-range query spanning an entire year of populated
  dates returns all 365/366 dates with their tags.
- **SC-003**: A tag-set-filtered query returns only dates carrying tags
  from the requested tag-set, with zero false positives or false
  negatives against the tagging service's rules.
- **SC-004**: Circular dependency detection catches all cycles and
  provides an error message naming the tag-sets involved.
- **SC-005**: Adding a new custom tag-set requires only defining the
  tag-set, implementing the tagging service, and registering it - no
  changes to existing code.
