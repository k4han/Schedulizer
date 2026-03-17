package me.virusker.schedulizer.models;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ScheduleTask {
    private final String name;
    private final List<String> command;
    private final String type; // "once", "repeat", "daily" or "cron"
    private boolean enabled;
    private LocalDateTime executionTime; // once
    private LocalTime dailyTime; // daily
    private long interval; // repeat
    private String cronExpression; // cron
    private LocalDateTime lastRunTime;

    public ScheduleTask(String name, List<String> command, String type, boolean enabled, 
                        LocalDateTime executionTime, LocalTime dailyTime, long interval, String cronExpression) {
        this.name = name;
        this.command = command;
        this.type = type;
        this.enabled = enabled;

        if (type.equals("once")) {
            this.executionTime = executionTime;
        } else if (type.equals("daily")) {
            this.dailyTime = dailyTime;
        } else if (type.equals("repeat")) {
            this.interval = interval;
        } else if (type.equals("cron")) {
            this.cronExpression = cronExpression;
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

    public String getCronExpression() {
        return cronExpression;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

    }


}
