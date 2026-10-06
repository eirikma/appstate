# Tasks: Business Day Calendar

**Input**: Design documents from `/specs/001-business-day-calendar/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: TDD is mandated by project constitution. Each behavioural
task includes writing the failing test first, then implementing to
make it pass (Red-Green-Refactor).

**Organization**: Tasks are grouped by user story to enable
independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1-US4)
- Include exact file paths in descriptions

## Path Conventions

- **Base package**: `com.github.eirikma.appstate.calendar`
- **Main source**: `business-day-calendar/src/main/java/com/github/eirikma/appstate/calendar/`
- **Unit tests**: `business-day-calendar/src/test/java/com/github/eirikma/appstate/calendar/`
- **Integration tests**: `business-day-calendar-integration-tests/src/test/java/com/github/eirikma/appstate/calendar/`
- **Tests-common**: `tests-common/src/main/java/com/github/eirikma/appstate/test/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create Maven modules, configure dependencies, set up
database schema and integration test infrastructure.

- [ ] T001 Create business-day-calendar Maven module with pom.xml and directory structure at business-day-calendar/pom.xml
- [ ] T002 [P] Create tests-common Maven module with pom.xml at tests-common/pom.xml (depends on Testcontainers, Liquibase, PostgreSQL driver, JUnit 6)
- [ ] T003 [P] Create business-day-calendar-integration-tests Maven module with pom.xml at business-day-calendar-integration-tests/pom.xml (depends on business-day-calendar and tests-common)
- [ ] T004 Update parent pom.xml with three new modules and dependency management for Spring Boot, Liquibase, JDBC, Testcontainers, and PostgreSQL driver
- [ ] T005 Create Liquibase changeLogs: business-day-calendar/src/main/resources/db/changelog/db.changelog-master.xml and changes/001-create-calendar-tables.xml (calendar_date and calendar_date_tag tables per data-model.md schema)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core abstractions that ALL user stories depend on.
These are interfaces and records with no behaviour - no TDD needed.

**CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T006 [P] Create CalendarDate record in business-day-calendar/src/main/java/.../model/CalendarDate.java (fields: LocalDate date, Map<String, Set<String>> tagsByTagSet)
- [ ] T007 [P] Create TaggingService interface in business-day-calendar/src/main/java/.../tagging/TaggingService.java (methods: tagSet(), dependencies(), tag(YearMonth, Map) per contracts/api-contracts.md)
- [ ] T008 [P] Create CalendarRepository interface in business-day-calendar/src/main/java/.../repository/CalendarRepository.java (methods: deleteMonth, saveDates, saveTags, findDatesByRange, findDatesByTagSetInRange per contracts/api-contracts.md)
- [ ] T009 [P] Create CalendarQuery interface in business-day-calendar/src/main/java/.../service/CalendarQuery.java (methods: findByDateRange, findByTagSetInRange per contracts/api-contracts.md)
- [ ] T010 [P] Create CalendarLiquibaseCustomizer Spring bean in business-day-calendar/src/main/java/.../spring/CalendarLiquibaseCustomizer.java (exports module's db.changelog-master.xml)
- [ ] T011 Implement IntegrationTestBase in tests-common/src/main/java/.../test/IntegrationTestBase.java (PostgreSQL 17 testcontainer startup, Liquibase changeLog execution, DataSource provisioning)

**Checkpoint**: Foundation ready - all interfaces defined, integration test infrastructure operational.

---

## Phase 3: User Story 1 - Populate a Month with Tagged Dates (Priority: P1) MVP

**Goal**: Populate a month by running all registered tagging services
in dependency order, persisting dates with their tags.

**Independent Test**: Populate April 2026 with all 3 built-in tag-sets
and verify each date carries the correct tags from WeekdayType,
ChristianHoliday, and NorwegianRedDay, applied in dependency order.

### Implementation for User Story 1

- [ ] T012 [P] [US1] Create WeekdayType enum in business-day-calendar/src/main/java/.../model/WeekdayType.java (values: WORKDAY, WEEKEND) and implement WeekdayTypeTagger in business-day-calendar/src/main/java/.../tagging/WeekdayTypeTagger.java (TDD: verify WORKDAY for Mon-Fri, WEEKEND for Sat-Sun in a known month)
- [ ] T013 [P] [US1] Create TagSetRegistry interface in business-day-calendar/src/main/java/.../registry/TagSetRegistry.java and implement DefaultTagSetRegistry in business-day-calendar/src/main/java/.../registry/DefaultTagSetRegistry.java with Kahn's algorithm for topological sort (TDD: register single service returns it in resolution order; register two services with dependency returns correct order)
- [ ] T014 [P] [US1] Implement Computus (Easter date computation) as a pure static method in business-day-calendar/src/main/java/.../tagging/Computus.java (TDD: verify 2024→Mar 31, 2025→Apr 20, 2026→Apr 5, 2027→Mar 28, 2028→Apr 16; edge cases 1818→Mar 22, 1886→Apr 25, 1981→Apr 19, 1954→Apr 18; use Math.floorMod per research.md)
- [ ] T015 [US1] Implement CalendarPopulator in business-day-calendar/src/main/java/.../service/CalendarPopulator.java (TDD: given a WeekdayTypeTagger and in-memory repository stub, populate a month and verify 28-31 CalendarDate entries created with correct tags; depends on T012, T013)
- [ ] T016 [US1] Create ChristianHoliday enum in business-day-calendar/src/main/java/.../model/ChristianHoliday.java (10 values per data-model.md) and implement ChristianHolidayTagger in business-day-calendar/src/main/java/.../tagging/ChristianHolidayTagger.java (TDD: verify fixed holidays Jan 1, Dec 25, Dec 26 and movable holidays relative to Easter for April 2026; depends on T014)
- [ ] T017 [US1] Create NorwegianRedDay enum in business-day-calendar/src/main/java/.../model/NorwegianRedDay.java (12 values per data-model.md) and implement NorwegianRedDayTagger in business-day-calendar/src/main/java/.../tagging/NorwegianRedDayTagger.java (TDD: verify 5 fixed red days and 7 movable red days by reading ChristianHoliday tags from existingTags parameter; depends on T016)
- [ ] T018 [US1] Unit test: populate a month with all 3 tag-sets registered (WeekdayType, ChristianHoliday with dep on none, NorwegianRedDay with dep on ChristianHoliday) and verify topological execution order and tag visibility across dependencies in business-day-calendar/src/test/java/.../service/CalendarPopulatorTest.java (depends on T015, T016, T017)
- [ ] T019 [US1] Implement JdbcCalendarRepository in business-day-calendar/src/main/java/.../repository/JdbcCalendarRepository.java (deleteMonth, saveDates, saveTags methods using JDBC PreparedStatement; depends on T006, T008)
- [ ] T020 [US1] Integration test: populate April 2026 end-to-end with JdbcCalendarRepository and verify all 3 tag-sets persisted correctly (Easter Sunday Apr 5, Good Friday Apr 3, Maundy Thursday Apr 2 tagged as ChristianHoliday and NorwegianRedDay; weekdays tagged as WORKDAY/WEEKEND) in business-day-calendar-integration-tests/src/test/java/.../service/CalendarPopulatorIT.java (depends on T018, T019)

**Checkpoint**: User Story 1 fully functional - month population with dependency-ordered tagging works end-to-end with persistence.

---

## Phase 4: User Story 2 - Query Dates in a Date Range (Priority: P2)

**Goal**: Query all populated dates in a date range with their
complete set of tags from all tag-sets.

**Independent Test**: Populate a month, query a sub-range, verify
returned dates with all their tags in chronological order.

### Implementation for User Story 2

- [ ] T021 [US2] Implement findDatesByRange in JdbcCalendarRepository in business-day-calendar/src/main/java/.../repository/JdbcCalendarRepository.java (TDD: query range within populated month returns dates with all tags; range spanning unpopulated month returns only populated dates; empty calendar returns empty list; results in chronological order)
- [ ] T022 [US2] Implement DefaultCalendarQuery.findByDateRange in business-day-calendar/src/main/java/.../service/DefaultCalendarQuery.java (delegates to CalendarRepository.findDatesByRange; TDD: verify delegation and immutable result)
- [ ] T023 [US2] Integration test: populate Jan and Mar 2026, query Jan 15 to Mar 15 and verify only Jan and Mar populated dates returned (Feb excluded) in business-day-calendar-integration-tests/src/test/java/.../service/CalendarQueryIT.java

**Checkpoint**: User Stories 1 AND 2 both work independently - populate and query by date range.

---

## Phase 5: User Story 3 - Query Dates by Tag-Set in a Date Range (Priority: P3)

**Goal**: Query dates in a date range filtered to a specific tag-set,
returning only dates that carry tags from that tag-set.

**Independent Test**: Populate a month with multiple tag-sets, query
for NorwegianRedDay, verify only red days returned with only their
NorwegianRedDay tags.

### Implementation for User Story 3

- [ ] T024 [US3] Implement findDatesByTagSetInRange in JdbcCalendarRepository in business-day-calendar/src/main/java/.../repository/JdbcCalendarRepository.java (TDD: filter by tag_set column, return only matching tags; tag-set with no matches returns empty; results in chronological order)
- [ ] T025 [US3] Implement DefaultCalendarQuery.findByTagSetInRange in business-day-calendar/src/main/java/.../service/DefaultCalendarQuery.java (delegates to CalendarRepository.findDatesByTagSetInRange; TDD: verify delegation and only requested tag-set's tags in result)
- [ ] T026 [US3] Integration test: populate April 2026, query NorwegianRedDay tag-set and verify 4 red days returned (Skjærtorsdag, Langfredag, Første påskedag, Andre påskedag) with only NorwegianRedDay tags in business-day-calendar-integration-tests/src/test/java/.../service/CalendarQueryIT.java

**Checkpoint**: All query stories work independently - unfiltered and tag-set-filtered queries.

---

## Phase 6: User Story 4 - Register a Custom Tag-Set (Priority: P4)

**Goal**: Register new tag-sets with dependency validation (missing
deps rejected, circular deps detected).

**Independent Test**: Register a custom tag-set depending on
WeekdayType, populate a month, verify custom tags applied after
WeekdayType tags.

### Implementation for User Story 4

- [ ] T027 [US4] Add missing-dependency validation to DefaultTagSetRegistry.register() in business-day-calendar/src/main/java/.../registry/DefaultTagSetRegistry.java (TDD: register service declaring dependency on unregistered tag-set throws exception with message naming the missing tag-set)
- [ ] T028 [US4] Add circular-dependency detection to DefaultTagSetRegistry.register() in business-day-calendar/src/main/java/.../registry/DefaultTagSetRegistry.java (TDD: register A depends on B, then B depends on A throws exception naming both tag-sets in cycle; three-way cycle A→B→C→A also detected)
- [ ] T029 [US4] Integration test: register a custom test tag-set depending on WeekdayType, populate a month, verify custom tags applied after WeekdayType in business-day-calendar-integration-tests/src/test/java/.../registry/CustomTagSetIT.java

**Checkpoint**: All 4 user stories complete - full populate, query, and extensibility functionality.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Edge cases, documentation, and validation across stories.

- [ ] T030 [P] Integration test: re-populate an already-populated month and verify all tags replaced (FR-010) in business-day-calendar-integration-tests/src/test/java/.../service/CalendarPopulatorIT.java
- [ ] T031 Run quickstart.md validation against implemented code in specs/001-business-day-calendar/quickstart.md
- [ ] T032 Add Javadoc to public API interfaces and records (TaggingService, CalendarRepository, CalendarQuery, CalendarPopulator, CalendarDate, TagSetRegistry) stating single responsibility per constitution

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - BLOCKS US2, US3 (need populated data for queries)
- **User Story 2 (Phase 4)**: Depends on US1 completion (needs JdbcCalendarRepository and populated data)
- **User Story 3 (Phase 5)**: Depends on US1 completion; can run in parallel with US2
- **User Story 4 (Phase 6)**: Depends on Foundational only (registry validation is independent of persistence); can start after Phase 2
- **Polish (Phase 7)**: Depends on all stories being complete

### Within User Story 1

```text
T012 (WeekdayType) ──────────────────┐
T013 (TagSetRegistry) ───────────────┤
T014 (Computus) ──→ T016 (Christian) ┤
                                     ├──→ T015 (Populator) ──→ T017 (Norwegian)
                                     │                         ──→ T018 (Unit test all 3)
                                     │                              ──→ T020 (Integration)
T019 (JdbcRepository) ──────────────────────────────────────────────┘
```

### Parallel Opportunities

- **Setup**: T002, T003 can run in parallel (different modules)
- **Foundational**: T006, T007, T008, T009, T010 can all run in parallel (different files)
- **US1**: T012, T013, T014 can run in parallel (different packages); T019 can run in parallel with T015-T018 (different package)
- **US2 + US3**: Can run in parallel after US1 (different query methods)
- **US4**: Can run in parallel with US2/US3 (registry validation independent of query implementation)

---

## Parallel Example: User Story 1

```bash
# Launch independent taggers and infrastructure in parallel:
Task: "T012 WeekdayType enum + WeekdayTypeTagger"
Task: "T013 TagSetRegistry + DefaultTagSetRegistry (Kahn's)"
Task: "T014 Computus pure function"

# After T012+T013 complete, launch populator:
Task: "T015 CalendarPopulator"

# After T014 complete, launch ChristianHoliday:
Task: "T016 ChristianHoliday enum + tagger"

# In parallel with T015-T017, launch JDBC repository:
Task: "T019 JdbcCalendarRepository"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T005)
2. Complete Phase 2: Foundational (T006-T011)
3. Complete Phase 3: User Story 1 (T012-T020)
4. **STOP and VALIDATE**: Populate April 2026 with all 3 tag-sets, verify end-to-end
5. Integration test passes - MVP delivered

### Incremental Delivery

1. Setup + Foundational → Build compiles, integration test infra works
2. US1 (Populate) → Core functionality, integration test passes (MVP!)
3. US2 (Date range query) → Read path works
4. US3 (Tag-set filtered query) → Filtered read path works
5. US4 (Custom registration) → Extensibility validated
6. Polish → Edge cases, documentation

### Story Independence

- **US1**: Fully independent after Foundational
- **US2**: Requires US1's JdbcCalendarRepository; query logic is independent
- **US3**: Requires US1's JdbcCalendarRepository; can run in parallel with US2
- **US4**: Registry validation independent of persistence; can start after Foundational

---

## Notes

- TDD is NON-NEGOTIABLE per constitution: every behavioural task includes writing the failing test first
- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- Use `Math.floorMod()` (not `%`) in Computus implementation per research.md
- All domain objects are Java records (immutable) per constitution
- JDBC over JPA/Hibernate per research.md R5 decision
- Commit after each task or logical group; structural changes separate from behavioural
