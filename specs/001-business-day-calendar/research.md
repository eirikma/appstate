# Research: Business Day Calendar

## R1: Easter Date Computation (Computus)

**Decision**: Implement the Anonymous Gregorian algorithm (Meeus/Jones/Butcher)
from scratch as a pure function.

**Rationale**: The algorithm is 13 lines of integer arithmetic with no
dependencies, no branching, and no floating-point math. Pulling in a
library (Time4J, Jollyday) solely for Easter computation would be
disproportionate.

**Alternatives considered**:
- Time4J (`net.time4j:time4j-base`): Has `EasterRule` but is a
  heavyweight library. Rejected for simplicity.
- Jollyday (`de.focus-shift:jollyday-core`): Computes Easter internally
  but is designed as a holiday calendar, not a standalone Computus API.
  Rejected to avoid coupling to an external holiday model.
- `java.time` built-in: No Easter support exists in the JDK.

**Implementation notes**:
- Use `Math.floorMod()` instead of `%` for the `h` and `l` variables
  to avoid negative remainders (the single most common implementation
  bug).
- Valid for all Gregorian calendar years (1583+). Practical range for
  this project: any year representable by `java.time.LocalDate`.
- Key test dates: 2026 → April 5, 2024 → March 31, 2025 → April 20,
  1818 → March 22 (earliest possible), 1886 → April 25 (latest).
- Epact exception cases to test: 1981 (h=29), 1954 (h=28, a>10).

## R2: Norwegian Red Calendar Days (Røde Dager)

**Decision**: 12 red days total: 5 fixed-date + 7 Easter-dependent.

**Rationale**: Based on two Norwegian laws: Lov om helligdager og
helligdagsfred (LOV-1995-02-24-12) and Lov om 1. og 17. mai som
høgtidsdager (LOV-1947-04-26-1).

### Fixed-date red days (5)

| Norwegian Name        | English Name       | Date        | Category    |
|-----------------------|--------------------|-------------|-------------|
| Første nyttårsdag     | New Year's Day     | 1 January   | Helligdag   |
| Arbeidernes dag       | Labour Day         | 1 May       | Høytidsdag  |
| Grunnlovsdagen        | Constitution Day   | 17 May      | Høytidsdag  |
| Første juledag        | Christmas Day      | 25 December | Helligdag   |
| Andre juledag         | Boxing Day         | 26 December | Helligdag   |

### Easter-dependent red days (7)

| Norwegian Name         | English Name    | Easter Offset | Category  |
|------------------------|-----------------|---------------|-----------|
| Skjærtorsdag           | Maundy Thursday | -3            | Helligdag |
| Langfredag             | Good Friday     | -2            | Helligdag |
| Første påskedag        | Easter Sunday   | 0             | Helligdag |
| Andre påskedag         | Easter Monday   | +1            | Helligdag |
| Kristi himmelfartsdag  | Ascension Day   | +39           | Helligdag |
| Første pinsedag        | Whit Sunday     | +49           | Helligdag |
| Andre pinsedag         | Whit Monday     | +50           | Helligdag |

**Notable non-red days**: Christmas Eve (24 Dec), New Year's Eve
(31 Dec), Holy Saturday (Easter-1), and Whit Saturday (Pentecost-1)
are NOT red days but have partial work restrictions. These are out of
scope for the initial implementation.

## R3: Christian Holidays Tag-Set Scope

**Decision**: Include both fixed and movable Christian holidays
relevant to the Western (Gregorian) church calendar.

**Rationale**: The Norwegian red days tagger depends on Christian
holidays to locate Easter-relative dates. The Christian holiday tag-set
should be self-contained: it computes Easter and marks all major
observances so that downstream tag-sets (Norwegian red days, and
potentially other national calendars) can reference them.

**Tags in scope**:
- Movable (Easter-relative): Maundy Thursday (-3), Good Friday (-2),
  Easter Sunday (0), Easter Monday (+1), Ascension Day (+39),
  Whit Sunday (+49), Whit Monday (+50)
- Fixed: Christmas Day (Dec 25), Second Day of Christmas (Dec 26),
  New Year's Day (Jan 1)

**Alternatives considered**:
- Include Lent period (40 days): Deferred. Lent is a season, not a
  single day. Can be added as a separate tag-set if needed.
- Include Ash Wednesday, Palm Sunday, Holy Saturday: Deferred. Not
  needed for Norwegian red days. Can be added later.

## R4: Topological Sort for Dependency Resolution

**Decision**: Use Kahn's algorithm (BFS-based topological sort) for
resolving tag-set execution order.

**Rationale**: Kahn's algorithm naturally detects cycles (if the sorted
result contains fewer nodes than the input graph, a cycle exists). It
produces a deterministic ordering when using a sorted queue, which aids
test reproducibility.

**Alternatives considered**:
- DFS-based topological sort: Equally valid but cycle detection
  requires additional "visiting" state tracking. Kahn's is simpler for
  this use case.
- No formal algorithm (manual ordering): Rejected - fragile and
  violates the extensibility requirement (FR-004/FR-005).

## R5: Storage and Persistence Pattern

**Decision**: Repository interface in the calendar module with a
JDBC-based default implementation. Schema managed by Liquibase
changeLogs owned by the module.

**Rationale**: Per clarification, each module exports its own Liquibase
changeLog via a Spring customizer bean. A `tests-common` module provides
the integration test infrastructure (PostgreSQL 17 testcontainer,
Liquibase runner, test superclass).

**Schema design**:
- `calendar_date` table: one row per populated date, with a
  `year_month` column for efficient month-level operations.
- `calendar_date_tag` table: one row per tag applied to a date,
  composite key of (date, tag_set, tag).
- Re-population: delete all rows for the month, then re-insert.

**Alternatives considered**:
- Single denormalised table (date + JSON tags column): Rejected -
  harder to query by tag-set (FR-009).
- JPA/Hibernate: Rejected for simplicity - plain JDBC with records
  aligns better with the immutability principle.
