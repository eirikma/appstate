package com.github.eirikma.appstate.calendar.registry;

import com.github.eirikma.appstate.calendar.tagging.TaggingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.javalite.test.jspec.JSpec.the;

@DisplayName("DefaultTagSetRegistry")
class DefaultTagSetRegistryTest {

    @Test
    @DisplayName("should return single registered service in resolution order")
    void shouldReturnSingleService() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        TaggingService service = stubService("A", Set.of());
        registry.register(service);

        List<TaggingService> order = registry.resolutionOrder();

        the(order.size()).shouldEqual(1);
        the(order.getFirst().tagSet()).shouldEqual("A");
    }

    @Test
    @DisplayName("should return dependency before dependent in resolution order")
    void shouldResolveDependencyOrder() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        TaggingService serviceA = stubService("A", Set.of());
        TaggingService serviceB = stubService("B", Set.of("A"));
        registry.register(serviceA);
        registry.register(serviceB);

        List<TaggingService> order = registry.resolutionOrder();

        the(order.size()).shouldEqual(2);
        the(order.get(0).tagSet()).shouldEqual("A");
        the(order.get(1).tagSet()).shouldEqual("B");
    }

    @Test
    @DisplayName("should resolve three services with chain dependency")
    void shouldResolveThreeServiceChain() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(stubService("A", Set.of()));
        registry.register(stubService("B", Set.of("A")));
        registry.register(stubService("C", Set.of("B")));

        List<TaggingService> order = registry.resolutionOrder();

        the(order.get(0).tagSet()).shouldEqual("A");
        the(order.get(1).tagSet()).shouldEqual("B");
        the(order.get(2).tagSet()).shouldEqual("C");
    }

    @Test
    @DisplayName("should reject registration with missing dependency")
    void shouldRejectMissingDependency() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();

        try {
            registry.register(stubService("B", Set.of("A")));
            the(false).shouldBeTrue(); // should not reach here
        } catch (IllegalArgumentException e) {
            the(e.getMessage()).shouldContain("B");
            the(e.getMessage()).shouldContain("A");
        }
    }

    @Test
    @DisplayName("should reject registration creating two-way circular dependency")
    void shouldRejectTwoWayCircularDependency() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(stubService("A", Set.of()));
        registry.register(stubService("B", Set.of("A")));

        // Re-register A to depend on B, creating A -> B -> A cycle
        try {
            registry.register(stubService("A", Set.of("B")));
            the(false).shouldBeTrue(); // should not reach here
        } catch (IllegalStateException e) {
            the(e.getMessage()).shouldContain("A");
            the(e.getMessage()).shouldContain("B");
        }

        // Registry should still be valid after failed registration
        the(registry.resolutionOrder().size()).shouldEqual(2);
    }

    @Test
    @DisplayName("should reject registration creating three-way circular dependency")
    void shouldRejectThreeWayCircularDependency() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(stubService("A", Set.of()));
        registry.register(stubService("B", Set.of("A")));
        registry.register(stubService("C", Set.of("B")));

        // Re-register A to depend on C, creating A -> C -> B -> A cycle
        try {
            registry.register(stubService("A", Set.of("C")));
            the(false).shouldBeTrue(); // should not reach here
        } catch (IllegalStateException e) {
            the(e.getMessage()).shouldContain("A");
            the(e.getMessage()).shouldContain("B");
            the(e.getMessage()).shouldContain("C");
        }
    }

    @Test
    @DisplayName("should report registered tag sets")
    void shouldReportRegisteredTagSets() {
        DefaultTagSetRegistry registry = new DefaultTagSetRegistry();
        registry.register(stubService("A", Set.of()));
        registry.register(stubService("B", Set.of("A")));

        the(registry.registeredTagSets()).shouldContain("A");
        the(registry.registeredTagSets()).shouldContain("B");
    }

    private static TaggingService stubService(String tagSet, Set<String> dependencies) {
        return new TaggingService() {
            @Override
            public String tagSet() {
                return tagSet;
            }

            @Override
            public Set<String> dependencies() {
                return dependencies;
            }

            @Override
            public Map<LocalDate, Set<String>> tag(YearMonth yearMonth, Map<LocalDate, Set<String>> existingTags) {
                return Map.of();
            }
        };
    }
}
