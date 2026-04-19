package com.github.eirikma.appstate.calendar.service;

import com.github.eirikma.appstate.calendar.registry.TagSetRegistry;
import com.github.eirikma.appstate.calendar.repository.CalendarRepository;
import com.github.eirikma.appstate.calendar.tagging.TaggingService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Stream;

/**
 * Populates a month by running all registered tagging services in dependency order.
 */
public class CalendarPopulator {

    private final TagSetRegistry registry;
    private final CalendarRepository repository;

    public CalendarPopulator(TagSetRegistry registry, CalendarRepository repository) {
        this.registry = registry;
        this.repository = repository;
    }

    public void populate(YearMonth yearMonth) {
        repository.deleteMonth(yearMonth);

        List<LocalDate> dates = datesInMonth(yearMonth);
        repository.saveDates(dates, yearMonth);

        Map<String, Map<LocalDate, Set<String>>> allTagsByTagSet = new LinkedHashMap<>();

        for (TaggingService service : registry.resolutionOrder()) {
            Map<LocalDate, Set<String>> existingTags = mergeExistingTags(allTagsByTagSet, service.dependencies());
            Map<LocalDate, Set<String>> newTags = service.tag(yearMonth, existingTags);
            allTagsByTagSet.put(service.tagSet(), newTags);
            repository.saveTags(newTags, service.tagSet());
        }
    }

    private static List<LocalDate> datesInMonth(YearMonth yearMonth) {
        return Stream.iterate(yearMonth.atDay(1), date -> !date.isAfter(yearMonth.atEndOfMonth()), date -> date.plusDays(1))
                .toList();
    }

    private static Map<LocalDate, Set<String>> mergeExistingTags(
            Map<String, Map<LocalDate, Set<String>>> allTagsByTagSet,
            Set<String> dependencies) {
        Map<LocalDate, Set<String>> merged = new LinkedHashMap<>();
        for (String dependency : dependencies) {
            Map<LocalDate, Set<String>> depTags = allTagsByTagSet.getOrDefault(dependency, Map.of());
            depTags.forEach((date, tags) ->
                    merged.computeIfAbsent(date, k -> new LinkedHashSet<>()).addAll(tags));
        }
        return merged;
    }
}
