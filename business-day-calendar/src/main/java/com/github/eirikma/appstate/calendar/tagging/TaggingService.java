package com.github.eirikma.appstate.calendar.tagging;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

/**
 * Classifies dates in a month by applying tags from a single tag-set.
 */
public interface TaggingService {

    String tagSet();

    Set<String> dependencies();

    Map<LocalDate, Set<String>> tag(YearMonth yearMonth, Map<LocalDate, Set<String>> existingTags);
}
