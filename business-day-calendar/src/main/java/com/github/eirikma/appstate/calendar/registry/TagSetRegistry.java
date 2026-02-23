package com.github.eirikma.appstate.calendar.registry;

import com.github.eirikma.appstate.calendar.tagging.TaggingService;

import java.util.List;
import java.util.Set;

/**
 * Manages registration of tagging services and resolves their execution order.
 */
public interface TagSetRegistry {

    void register(TaggingService taggingService);

    Set<String> registeredTagSets();

    List<TaggingService> resolutionOrder();
}
