package com.github.eirikma.appstate.calendar.registry;

import com.github.eirikma.appstate.calendar.tagging.TaggingService;

import java.util.*;

/**
 * Resolves tagging service execution order using Kahn's algorithm for topological sort.
 */
public class DefaultTagSetRegistry implements TagSetRegistry {

    private final Map<String, TaggingService> servicesByTagSet = new LinkedHashMap<>();

    @Override
    public void register(TaggingService taggingService) {
        validateDependencies(taggingService);
        TaggingService previous = servicesByTagSet.put(taggingService.tagSet(), taggingService);
        try {
            validateNoCycles();
        } catch (IllegalStateException e) {
            // Restore previous state
            if (previous != null) {
                servicesByTagSet.put(taggingService.tagSet(), previous);
            } else {
                servicesByTagSet.remove(taggingService.tagSet());
            }
            throw e;
        }
    }

    @Override
    public Set<String> registeredTagSets() {
        return Set.copyOf(servicesByTagSet.keySet());
    }

    @Override
    public List<TaggingService> resolutionOrder() {
        return topologicalSort();
    }

    private void validateDependencies(TaggingService taggingService) {
        for (String dependency : taggingService.dependencies()) {
            if (!servicesByTagSet.containsKey(dependency)) {
                throw new IllegalArgumentException(
                        "Tag-set '%s' declares dependency on unregistered tag-set '%s'"
                                .formatted(taggingService.tagSet(), dependency));
            }
        }
    }

    private void validateNoCycles() {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> dependents = new HashMap<>();

        for (TaggingService service : servicesByTagSet.values()) {
            inDegree.putIfAbsent(service.tagSet(), 0);
            for (String dependency : service.dependencies()) {
                dependents.computeIfAbsent(dependency, k -> new ArrayList<>()).add(service.tagSet());
                inDegree.merge(service.tagSet(), 1, Integer::sum);
            }
        }

        Queue<String> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        Set<String> sorted = new LinkedHashSet<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sorted.add(current);
            for (String dependent : dependents.getOrDefault(current, List.of())) {
                int newDegree = inDegree.merge(dependent, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
        }

        if (sorted.size() != servicesByTagSet.size()) {
            Set<String> inCycle = new LinkedHashSet<>(servicesByTagSet.keySet());
            inCycle.removeAll(sorted);
            throw new IllegalStateException(
                    "Circular dependency detected among tag-sets: %s".formatted(inCycle));
        }
    }

    private List<TaggingService> topologicalSort() {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> dependents = new HashMap<>();

        for (TaggingService service : servicesByTagSet.values()) {
            inDegree.putIfAbsent(service.tagSet(), 0);
            for (String dependency : service.dependencies()) {
                dependents.computeIfAbsent(dependency, k -> new ArrayList<>()).add(service.tagSet());
                inDegree.merge(service.tagSet(), 1, Integer::sum);
            }
        }

        Queue<String> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<TaggingService> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sorted.add(servicesByTagSet.get(current));
            for (String dependent : dependents.getOrDefault(current, List.of())) {
                int newDegree = inDegree.merge(dependent, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
        }

        return List.copyOf(sorted);
    }
}
