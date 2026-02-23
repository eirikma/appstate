package com.github.eirikma.appstate.calendar.tagging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("Computus")
class ComputusTest {

    @Test
    @DisplayName("should compute Easter 2024 as March 31")
    void shouldComputeEaster2024() {
        the(Computus.easterSunday(2024)).shouldEqual(LocalDate.of(2024, 3, 31));
    }

    @Test
    @DisplayName("should compute Easter 2025 as April 20")
    void shouldComputeEaster2025() {
        the(Computus.easterSunday(2025)).shouldEqual(LocalDate.of(2025, 4, 20));
    }

    @Test
    @DisplayName("should compute Easter 2026 as April 5")
    void shouldComputeEaster2026() {
        the(Computus.easterSunday(2026)).shouldEqual(LocalDate.of(2026, 4, 5));
    }

    @Test
    @DisplayName("should compute Easter 2027 as March 28")
    void shouldComputeEaster2027() {
        the(Computus.easterSunday(2027)).shouldEqual(LocalDate.of(2027, 3, 28));
    }

    @Test
    @DisplayName("should compute Easter 2028 as April 16")
    void shouldComputeEaster2028() {
        the(Computus.easterSunday(2028)).shouldEqual(LocalDate.of(2028, 4, 16));
    }

    @Test
    @DisplayName("should compute earliest possible Easter (March 22) for 1818")
    void shouldComputeEarliestEaster() {
        the(Computus.easterSunday(1818)).shouldEqual(LocalDate.of(1818, 3, 22));
    }

    @Test
    @DisplayName("should compute latest possible Easter (April 25) for 1886")
    void shouldComputeLatestEaster() {
        the(Computus.easterSunday(1886)).shouldEqual(LocalDate.of(1886, 4, 25));
    }

    @Test
    @DisplayName("should handle epact exception case h=29 for 1981")
    void shouldHandleEpactExceptionH29() {
        the(Computus.easterSunday(1981)).shouldEqual(LocalDate.of(1981, 4, 19));
    }

    @Test
    @DisplayName("should handle epact exception case h=28 and a>10 for 1954")
    void shouldHandleEpactExceptionH28() {
        the(Computus.easterSunday(1954)).shouldEqual(LocalDate.of(1954, 4, 18));
    }
}
