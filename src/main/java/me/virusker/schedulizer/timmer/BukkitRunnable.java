package me.virusker.schedulizer.timmer;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class BukkitRunnable extends org.bukkit.scheduler.BukkitRunnable {
    private final PluginConfig config;
    private Set<String> executedTasks = new HashSet<>();
    private int lastCheckedMinute = -1;

    public BukkitRunnable(PluginConfig config) {
        this.config = config;
    }

    @Override
    public void run() {

        LocalTime currentTime = LocalTime.now(config.getZoneId());
        if (lastCheckedMinute != currentTime.getMinute()) {
            lastCheckedMinute = currentTime.getMinute();
            executedTasks.clear();
        }
        
        // Calculate total minutes from epoch (1970-01-01) to avoid reset at midnight
        LocalDate today = LocalDate.now(config.getZoneId());
        long daysSinceEpoch = today.toEpochDay();
        long totalMinutes = daysSinceEpoch * 24 * 60 + 
                            currentTime.getHour() * 60 + 
                            currentTime.getMinute();
        
        ZonedDateTime now = ZonedDateTime.now(config.getZoneId());

        for (ScheduleTask task : config.getActiveTasks()) {

            if (!task.isEnabled()) continue;

            String taskKey = task.getName() + ":" + currentTime.getMinute();
            if (executedTasks.contains(taskKey)) continue;

            if (task.getType().equals("repeat")) {

                long interval = task.getInterval();
                long minutesSinceStart = totalMinutes - task.getStartMinutes();
                if (minutesSinceStart >= 0 && minutesSinceStart % interval == 0) {

                    for (String command : task.getCommand()) {
                        config.getPlugin().getServer().dispatchCommand(config.getPlugin().getServer().getConsoleSender(), command);
                    }
                    executedTasks.add(taskKey);
                }
            } else if (task.getType().equals("daily") &&
                    task.getDailyTime().getHour() == currentTime.getHour() &&
                    task.getDailyTime().getMinute() == currentTime.getMinute()) {

                for (String command : task.getCommand()) {
                    config.getPlugin().getServer().dispatchCommand(config.getPlugin().getServer().getConsoleSender(), command);
                }
                // task executed this minute
                executedTasks.add(taskKey);
            } else if (task.getType().equals("once") &&
                    task.getExecutionTime().getHour() == currentTime.getHour() &&
                    task.getExecutionTime().getMinute() == currentTime.getMinute()) {
//                    config.getPlugin().getLogger().info("Executing command: " + task.getCommand());
                for (String command : task.getCommand()) {
                    config.getPlugin().getServer().dispatchCommand(config.getPlugin().getServer().getConsoleSender(), command);
                }
                task.setEnabled(false);
                config.updateSchedule(task);
                config.saveConfig();
            } else if (task.getType().equals("cron")) {
                // Check if current time matches cron expression
                try {
                    CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(com.cronutils.model.CronType.UNIX));
                    Cron cron = cronParser.parse(task.getCronExpression());
                    ExecutionTime executionTime = ExecutionTime.forCron(cron);
                    
                    // Check if current time is an execution time
                    if (executionTime.isMatch(now)) {
                        for (String command : task.getCommand()) {
                            config.getPlugin().getServer().dispatchCommand(config.getPlugin().getServer().getConsoleSender(), command);
                        }
                        executedTasks.add(taskKey);
                    }
                } catch (Exception e) {
                    config.getPlugin().getLogger().warning("Error executing cron task " + task.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
