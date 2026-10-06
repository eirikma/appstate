# API Contracts: Business Day Calendar

This module is a Java library. Contracts are defined as Java interfaces
that consumers program against.

## TaggingService

The core extension point. Each tag-set is paired with exactly one
tagging service.

```
TaggingService
  tagSet()       → TagSet identifier for this service
  dependencies() → Set of TagSet identifiers that must be applied first
  tag(yearMonth, existingTags) → Map<LocalDate, Set<Tag>>
```

**Contract**:
- `tag()` receives a YearMonth and read-only access to tags already
  applied by dependency tag-sets.
- Returns a map of date → tags for that month. Dates not in the map
  receive no tags from this tag-set.
- MUST be deterministic: same input → same output.
- MUST NOT modify the existingTags parameter.
- MUST complete without error even if no dates are tagged.

## TagSetRegistry

Manages registration of tag-sets and their tagging services.

```
TagSetRegistry
  register(taggingService)  → void (throws on circular dependency
                               or missing dependency)
  registeredTagSets()       → Set of TagSet identifiers
  resolutionOrder()         → List of TaggingService in topological order
```

**Contract**:
- `register()` validates that all declared dependencies are already
  registered. Rejects with descriptive error if:
  - A declared dependency is not registered.
  - Registration would create a circular dependency.
- `resolutionOrder()` returns services sorted so that dependencies
  come before dependents (topological order).

## CalendarPopulator

Populates a month by running all registered tagging services in
dependency order.

```
CalendarPopulator
  populate(yearMonth) → void
```

**Contract**:
- Creates a CalendarDate entry for every day in the month.
- Executes tagging services in the order returned by
  `TagSetRegistry.resolutionOrder()`.
- Each service receives read access to tags applied by prior services.
- If the month was previously populated, all existing dates and tags
  for that month are replaced.
- Operation is atomic: either the entire month is populated or no
  changes are persisted.

## CalendarQuery

Read-only access to populated calendar dates.

```
CalendarQuery
  findByDateRange(from, to)              → List<CalendarDate>
  findByTagSetInRange(tagSet, from, to)  → List<CalendarDate>
```

**Contract**:
- `findByDateRange()`: Returns all populated dates in [from, to]
  inclusive, each with its complete set of tags from all tag-sets.
  Results are ordered chronologically. Returns empty list if no
  populated dates exist in the range.
- `findByTagSetInRange()`: Returns only dates in [from, to] that
  carry at least one tag from the specified tag-set. Each returned
  date includes only the tags from the requested tag-set. Results are
  ordered chronologically. Returns empty list if no matching dates
  exist.

## CalendarRepository

Persistence abstraction. The module defines this interface; a default
JDBC implementation is provided.

```
CalendarRepository
  deleteMonth(yearMonth)                     → void
  saveDates(dates)                           → void
  saveTags(tags)                             → void
  findDatesByRange(from, to)                 → List<CalendarDate>
  findDatesByTagSetInRange(tagSet, from, to) → List<CalendarDate>
```

**Contract**:
- `deleteMonth()`: Removes all dates and their tags for the given
  year-month.
- `saveDates()`: Persists CalendarDate entries (without tags).
- `saveTags()`: Persists tag associations for existing dates.
- Query methods return dates with their tags as specified by the
  corresponding CalendarQuery methods.
