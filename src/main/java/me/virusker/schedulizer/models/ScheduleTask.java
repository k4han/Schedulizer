package me.virusker.schedulizer.models;

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import me.virusker.schedulizer.models.TaskType;

public class ScheduleTask {
    private static final DateTimeFormatter DAILY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final String name;
    private final List<String> command;
    private final TaskType type;
    private boolean enabled;
    private LocalDateTime executionTime; // once
    private LocalTime dailyTime; // daily
    private long interval; // repeat
    private String cronExpression; // cron
    private int startMinutes; // minutes from epoch (1970-01-01) when task should start

    // Cached parsed cron to avoid reparsing on each tick
    private transient ExecutionTime cachedExecutionTime;
    private static CronParser cronParser;

    /**
     * Initialize the shared cron parser. Must be called before creating cron tasks.
     */
    public static void initCronParser(CronParser parser) {
        cronParser = parser;
    }

    public ScheduleTask(String name, List<String> command, TaskType type, boolean enabled,
                        LocalDateTime executionTime, LocalTime dailyTime, long interval, String cronExpression) {
        this(name, command, type, enabled, executionTime, dailyTime, interval, cronExpression, 0);
    }

    public ScheduleTask(String name, List<String> command, TaskType type, boolean enabled,
                        LocalDateTime executionTime, LocalTime dailyTime, long interval, String cronExpression, int startMinutes) {
        this.name = name;
        this.command = command;
        this.type = type;
        this.enabled = enabled;
        this.startMinutes = startMinutes;

        if (type == TaskType.ONCE) {
            this.executionTime = executionTime;
        } else if (type == TaskType.DAILY) {
            this.dailyTime = dailyTime;
        } else if (type == TaskType.REPEAT) {
            this.interval = interval;
        } else if (type == TaskType.CRON) {
            this.cronExpression = cronExpression;
            this.cachedExecutionTime = parseCronExpression(cronExpression);
        }

    }

    private ExecutionTime parseCronExpression(String expression) {
        if (cronParser == null) {
            return null;
        }
        try {
            Cron cron = cronParser.parse(expression);
            return ExecutionTime.forCron(cron);
        } catch (Exception e) {
            return null;
        }
    }

    public ExecutionTime getCronExecutionTime() {
        return cachedExecutionTime;
    }

    public String getName() {
        return name;
    }

    public List<String> getCommand() {
        return command;
    }

    public TaskType getType() {
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

    public int getStartMinutes() {
        return startMinutes;
    }

    public void setStartMinutes(int startMinutes) {
        this.startMinutes = startMinutes;
    }

    /**
     * Get display string for time info (used in list/info commands).
     * @param formatter The datetime formatter for 'once' tasks (may be null for default display)
     * @return Human-readable time description
     */
    public String getTimeDisplay(DateTimeFormatter formatter) {
        switch (type) {
            case ONCE:
                return formatter != null ? formatter.format(executionTime) : executionTime.toString();
            case DAILY:
                return DAILY_TIME_FORMATTER.format(dailyTime);
            case REPEAT:
                return interval + " minutes (offset: " + startMinutes + ")";
            case CRON:
                return cronExpression;
            default:
                return "Unknown";
        }
    }

}
