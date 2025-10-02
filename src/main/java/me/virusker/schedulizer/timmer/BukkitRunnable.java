package me.virusker.schedulizer.timmer;

import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.Bukkit;

import java.time.LocalTime;
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
        int totalMinutes = currentTime.getHour() * 60 + currentTime.getMinute();

        for (ScheduleTask task : config.getActiveTasks()) {

            if (!task.isEnabled()) continue;
            
            // Check conditions before executing
            if (!checkConditions(task)) continue;

            String taskKey = task.getName() + ":" + currentTime.getMinute();
            if (executedTasks.contains(taskKey)) continue;

            if (task.getType().equals("repeat")) {

                long interval = task.getInterval();
                if ( totalMinutes % interval == 0) {

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
            }
        }
    }

    private boolean checkConditions(ScheduleTask task) {
        // Check player count conditions
        if (task.getMinPlayers() != null || task.getMaxPlayers() != null) {
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            
            if (task.getMinPlayers() != null && onlinePlayers < task.getMinPlayers()) {
                return false;
            }
            
            if (task.getMaxPlayers() != null && onlinePlayers > task.getMaxPlayers()) {
                return false;
            }
        }
        
        // Check time of day condition (Minecraft time)
        if (task.getTimeOfDay() != null && !task.getTimeOfDay().isEmpty()) {
            long mcTime = config.getPlugin().getServer().getWorlds().get(0).getTime();
            // Minecraft time: 0-12000 is day, 12000-24000 is night
            boolean isDay = mcTime >= 0 && mcTime < 12000;
            
            if (task.getTimeOfDay().equalsIgnoreCase("day") && !isDay) {
                return false;
            }
            
            if (task.getTimeOfDay().equalsIgnoreCase("night") && isDay) {
                return false;
            }
        }
        
        return true;
    }
}
