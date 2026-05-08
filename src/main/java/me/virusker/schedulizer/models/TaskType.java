package me.virusker.schedulizer.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the type of a scheduled task.
 */
public enum TaskType {
    ONCE("once"),
    DAILY("daily"),
    REPEAT("repeat"),
    CRON("cron");

    private final String value;

    /** Cached list of valid type names for tab completion */
    public static final List<String> VALID_TYPE_NAMES = Arrays.stream(values())
            .map(TaskType::getValue)
            .collect(Collectors.toList());

    TaskType(String value) {
        this.value = value;
    }

    /**
     * Get the string representation used in configuration files.
     * @return The config value string
     */
    public String getValue() {
        return value;
    }

    /**
     * Parse a string to TaskType.
     * @param value The string value from config
     * @return The corresponding TaskType, or null if invalid
     */
    public static TaskType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (TaskType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}