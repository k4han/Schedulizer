package me.virusker.schedulizer.models;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ScheduleTask {
    private final String name;
    private final List<String> command;
    private final String type; // "once", "repeat" or "daily"
    private boolean enabled;
    private LocalDateTime executionTime; // once
    private LocalTime dailyTime; // daily
    private long interval; // repeat
    private LocalDateTime lastRunTime;
    // Conditions
    private Integer minPlayers;
    private Integer maxPlayers;
    private String timeOfDay; // "day", "night", or null for any time

    public ScheduleTask(String name, List<String> command, String type, boolean enabled, LocalDateTime executionTime, LocalTime dailyTime, long interval) {
        this(name, command, type, enabled, executionTime, dailyTime, interval, null, null, null);
    }

    public ScheduleTask(String name, List<String> command, String type, boolean enabled, LocalDateTime executionTime, LocalTime dailyTime, long interval, Integer minPlayers, Integer maxPlayers, String timeOfDay) {
        this.name = name;
        this.command = command;
        this.type = type;
        this.enabled = enabled;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.timeOfDay = timeOfDay;

        if (type.equals("once")) {
            this.executionTime = executionTime;
        } else if (type.equals("daily")) {
            this.dailyTime = dailyTime;
        } else if (type.equals("repeat")) {
            this.interval = interval;
        }

    }

    public String getName() {
        return name;
    }

    public List<String> getCommand() {
        return command;
    }

    public String getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public LocalTime getDailyTime() {
        return dailyTime;
    }

    public long getInterval() {
        return interval;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

}
