package me.virusker.schedulizer.timmer;

import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import me.virusker.schedulizer.models.TaskType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.cronutils.model.time.ExecutionTime;

public class BukkitRunnable extends org.bukkit.scheduler.BukkitRunnable {
    private final PluginConfig config;
    private Set<String> executedTasks = ConcurrentHashMap.newKeySet();
    private int lastCheckedMinute = -1;
    private long lastCheckedDay = -1;

    public BukkitRunnable(PluginConfig config) {
        this.config = config;
    }

    @Override
    public void run() {

        LocalTime currentTime = LocalTime.now(config.getZoneId());
        LocalDate currentDate = LocalDate.now(config.getZoneId());

        // Reset at new day or minute change to avoid memory buildup and allow re-execution
        if (lastCheckedDay != currentDate.toEpochDay() || lastCheckedMinute != currentTime.getMinute()) {
            lastCheckedDay = currentDate.toEpochDay();
            lastCheckedMinute = currentTime.getMinute();
            executedTasks.clear();
        }

        // Calculate total minutes from epoch (1970-01-01) to avoid reset at midnight
        long daysSinceEpoch = currentDate.toEpochDay();
        long totalMinutes = daysSinceEpoch * 24 * 60 +
                            currentTime.getHour() * 60 +
                            currentTime.getMinute();

        // Pre-compute ZonedDateTime for cron tasks (lazy - only create if cron tasks exist)
        ZonedDateTime nowForCron = null;

        for (ScheduleTask task : config.getActiveTasks()) {

            if (!task.isEnabled()) continue;

            // Include date in taskKey to prevent cross-day duplicates
            String taskKey = task.getName() + ":" + currentDate.toEpochDay() + ":" + totalMinutes;
            if (executedTasks.contains(taskKey)) continue;

            TaskType type = task.getType();
            if (type == TaskType.REPEAT) {

                long interval = task.getInterval();
                long minutesSinceStart = totalMinutes - task.getStartMinutes();
                if (minutesSinceStart >= 0 && minutesSinceStart % interval == 0) {
                    executedTasks.add(taskKey);
                    config.executeCommands(task.getCommand());
                }
            } else if (type == TaskType.DAILY &&
                    task.getDailyTime() != null &&
                    task.getDailyTime().getHour() == currentTime.getHour() &&
                    task.getDailyTime().getMinute() == currentTime.getMinute()) {

                executedTasks.add(taskKey);
                config.executeCommands(task.getCommand());
            } else if (type == TaskType.ONCE &&
                    task.getExecutionTime() != null &&
                    task.getExecutionTime().toLocalDate().equals(currentDate) &&
                    task.getExecutionTime().getHour() == currentTime.getHour() &&
                    task.getExecutionTime().getMinute() == currentTime.getMinute()) {
                executedTasks.add(taskKey);
                config.executeCommands(task.getCommand());
                task.setEnabled(false);
                config.setTaskEnabled(task.getName(), false);
            } else if (type == TaskType.CRON) {
                // Lazy initialization - create ZonedDateTime only when first cron task is encountered
                if (nowForCron == null) {
                    nowForCron = ZonedDateTime.now(config.getZoneId());
                }
                ExecutionTime executionTime = task.getCronExecutionTime();
                if (executionTime != null && executionTime.isMatch(nowForCron)) {
                    executedTasks.add(taskKey);
                    config.executeCommands(task.getCommand());
                }
            }
        }
    }
}
