package com.optic.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.optic.console.infrastructure.exception.DebugException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DebugUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    /**
     * Dump and Die - Stops execution and returns formatted JSON response.
     * Works anywhere in your code. Use with 'return' to satisfy compiler:
     * 
     * Usage:
     * return DebugUtil.dd(user);
     * return DebugUtil.dd(user, request, token);
     * 
     * @param objects Objects to dump (all arguments are dumped, no special label handling)
     * @param <T> Return type (always throws, never actually returns)
     * @return Never returns - always throws DebugException
     */
    @SuppressWarnings("unreachable")
    public static <T> T dd(Object... objects) {
        List<DebugException.DebugItem> debugItems = createDebugItems(objects);

        logDebugItems(debugItems);

        throw new DebugException(debugItems);
    }

    /**
     * Dump and Continue - Logs to console but continues execution.
     * 
     * Usage:
     * DebugUtil.dump(user);
     * DebugUtil.dump(user, request, token);
     */
    public static void dump(Object... objects) {
        List<DebugException.DebugItem> debugItems = createDebugItems(objects);
        logDebugItems(debugItems);
        // No exception thrown - execution continues
    }


    private static List<DebugException.DebugItem> createDebugItems(Object... objects) {
        List<DebugException.DebugItem> items = new ArrayList<>();

        if (objects == null || objects.length == 0) {
            items.add(createDebugItem(null));
            return items;
        }

        // Dump all objects
        for (Object object : objects) {
            items.add(createDebugItem(object));
        }

        return items;
    }

    private static DebugException.DebugItem createDebugItem(Object object) {
        String type = object != null ? object.getClass().getName() : "null";
        Map<String, Object> metadata = new HashMap<>();

        // Add metadata for collections/arrays
        if (object != null) {
            if (object instanceof java.util.Collection) {
                metadata.put("size", ((java.util.Collection<?>) object).size());
                metadata.put("type", "Collection");
            } else if (object instanceof Map) {
                metadata.put("size", ((Map<?, ?>) object).size());
                metadata.put("type", "Map");
            } else if (object.getClass().isArray()) {
                metadata.put("size", java.lang.reflect.Array.getLength(object));
                metadata.put("type", "Array");
            }
        }

        return new DebugException.DebugItem(type, object, metadata.isEmpty() ? null : metadata);
    }

    private static void logDebugItems(List<DebugException.DebugItem> items) {
        try {
            String formatted = objectMapper.writeValueAsString(items);
            log.info("\n" + "=".repeat(80) + "\nDEBUG DUMP:\n" + formatted + "\n" + "=".repeat(80));
        } catch (Exception e) {
            // Fallback if JSON serialization fails
            log.info("\n" + "=".repeat(80) + "\nDEBUG DUMP:\n");
            for (DebugException.DebugItem item : items) {
                log.info("Type: {}, Value: {}",
                        item.getType(), item.getValue());
            }
            log.info("=".repeat(80));
        }
    }
}