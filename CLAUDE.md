# Appstate

## Project Structure

Maven multi-module project.

```
appstate/                  (parent pom)
└── appstate-core/         (core module)
```

## Tech Stack

- Java 25
- Maven 3.9+
- JUnit 6 for testing
- JSpec (javalite-common) for BDD-style assertions (`shouldEqual`, `shouldContain`, etc.)
- gatherers4j for Stream Gatherer operations

## Build Commands

- Build and test: `mvn verify`
- Run tests only: `mvn test`
- Run single test: `mvn test -pl appstate-core -Dtest=ClassName`

## Testing

- Use JUnit 6 (`org.junit.jupiter`) for test structure
- Use JSpec assertions from `org.javalite.common.Collections` and `org.javalite.test.jspec.JSpec.*`
  - Prefer `the(actual).shouldEqual(expected)` over JUnit `assertEquals`
- Use `@DisplayName` for readable test descriptions
- Test classes go in `src/test/java` mirroring the main source package structure

## Task Management

Use the `tk` command to create and track planned development tasks.

## Dependencies

Versions are managed in the parent pom.xml via properties and `<dependencyManagement>`.
Module poms declare dependencies without version numbers.

## Active Technologies
- Java 25 + Spring Boot + Liquibase + JDBC (001-business-day-calendar)
- PostgreSQL via repository abstraction (001-business-day-calendar)
- Testcontainers PostgreSQL 17 (integration tests)

## Recent Changes
- 001-business-day-calendar: Business day calendar module with tag-based date classification
