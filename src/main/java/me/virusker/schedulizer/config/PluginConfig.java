package me.virusker.schedulizer.config;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PluginConfig {
    private final String scheduleFile = "schedule.yml";
    private final String nameConfig = "schedules";
    private final FileConfiguration config;
    private final FileConfiguration scheduler;
    private final JavaPlugin plugin;
    private List<ScheduleTask> tasks = new ArrayList<>();
    private List<ScheduleTask> activeTasks = new ArrayList<>();
    private String dateTimeFormat;
    private DateTimeFormatter formatter;

    private final String dailyPattern = "^\\d{2}:\\d{2}(:\\d{2})?$";

    private final String repeatPattern = "^\\d+$";
    private final CronParser cronParser;

    public PluginConfig(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), scheduleFile);
        if (!file.exists()) {
            plugin.saveResource(scheduleFile, false);
        }
        this.scheduler = YamlConfiguration.loadConfiguration(file);
        this.config = plugin.getConfig();
        this.plugin = plugin;
        dateTimeFormat = config.getString("datetime-format");
        if (dateTimeFormat.isEmpty())
            this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        else
            this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);

        // Initialize cron parser with UNIX type (minute, hour, day, month, weekday)
        this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        this.tasks = getSchedule();
    }

    public List<ScheduleTask> getTasks() {
        return tasks;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public CronParser getCronParser() {
        return cronParser;
    }

    public ZoneId getZoneId() {
        String timezone = config.getString("timezone");
        if (timezone == null || timezone.isEmpty()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            plugin.getLogger().warning("Invalid timezone '" + timezone + "', using system default: " + e.getMessage());
            return ZoneId.systemDefault();
        }
    }

    public long getTick() {
        return config.getLong("tick");
    }
    public void reload() throws IOException, InvalidConfigurationException {
        // Reload config.yml
        plugin.reloadConfig();
        this.config.load(new File(plugin.getDataFolder(), "config.yml"));

        // Reload schedule.yml
        File file = new File(plugin.getDataFolder(), scheduleFile);
        if (!file.exists()) {
            plugin.saveResource(scheduleFile, false);
        }
        this.scheduler.load(file);

        this.tasks = getSchedule();
    }

    public ScheduleTask getTask(String name) {
        for (ScheduleTask task : tasks) {
            if (task.getName().equals(name)) {
                return task;
            }
        }
        return null;
    }

    public List<ScheduleTask> getSchedule() {
        List<ScheduleTask> schedule = new ArrayList<>();
        activeTasks.clear();
        
        ConfigurationSection section = scheduler.getConfigurationSection(nameConfig);
        if (section == null) {
            return schedule;
        }
        
        for (String key : section.getKeys(false)) {
            ConfigurationSection taskSection = scheduler.getConfigurationSection(nameConfig + "." + key);
            if (taskSection == null) continue;

            List<String> command = taskSection.getStringList("command");
            String type = taskSection.getString("type");
            boolean enabled = taskSection.getBoolean("enabled");

            if (type == null || command == null) {
                plugin.getLogger().warning("Task '" + key + "' has invalid configuration (missing type or command). Skipping...");
                continue;
            }



            if (type.equals("once")) {
                // format time (dd/MM/yyyy HH:mm)
                LocalDateTime time;
                try {
                    String time_ = taskSection.getString("time");
                    if (time_ == null) {
                        plugin.getLogger().warning("Task '" + key + "' (once) is missing 'time' field. Skipping...");
                        continue;
                    }
                    time = LocalDateTime.parse(time_, formatter);
                } catch (Exception e) {
                    plugin.getLogger().warning("Task '" + key + "' has invalid time format: " + e.getMessage() + ". Skipping...");
                    continue;
                }
                ScheduleTask task = new ScheduleTask(
                        key,
                        command,
                        type,
                        enabled,
                        time,
                        null,
                        0,
                        null
                );
                if (taskSection.getBoolean("enabled")) {
                    activeTasks.add(task);
                }
                schedule.add(task);

            } else if (type.equals("daily")) {
                // format time (HH:mm)
                String time_ = taskSection.getString("time");
                if (time_ == null) {
                    plugin.getLogger().warning("Task '" + key + "' (daily) is missing 'time' field. Skipping...");
                    continue;
                }
                if (!time_.matches(dailyPattern)) {
                    plugin.getLogger().warning("Task '" + key + "' (daily) has invalid time format: '" + time_ + "'. Expected HH:mm. Skipping...");
                    continue;
                }
                LocalTime time = LocalTime.parse(time_);

                ScheduleTask task = new ScheduleTask(
                        key,
                        command,
                        type,
                        enabled,
                        null,
                        time,
                        0,
                        null
                );
                if (taskSection.getBoolean("enabled")) {
                    activeTasks.add(task);
                }
                schedule.add(task);

            } else if (type.equals("repeat")) {
                // format time (minutes)
                String time_ = taskSection.getString("interval");
                if (time_ == null) {
                    plugin.getLogger().warning("Task '" + key + "' (repeat) is missing 'interval' field. Skipping...");
                    continue;
                }
                if (!time_.matches(repeatPattern)) {
                    plugin.getLogger().warning("Task '" + key + "' (repeat) has invalid interval format: '" + time_ + "'. Expected integer (minutes). Skipping...");
                    continue;
                }
                long time = Long.parseLong(time_);
                int startMinutes = taskSection.getInt("start_minutes", 0);
                ScheduleTask task = new ScheduleTask(
                        key,
                        command,
                        type,
                        enabled,
                        null,
                        null,
                        time,
                        null,
                        startMinutes
                );
                if (taskSection.getBoolean("enabled")) {
                    activeTasks.add(task);
                }
                schedule.add(task);
            } else if (type.equals("cron")) {
                // format: cron expression (e.g., "0 0 * * *")
                String cronExpr = taskSection.getString("cron");
                if (cronExpr == null) {
                    plugin.getLogger().warning("Task '" + key + "' (cron) is missing 'cron' field. Skipping...");
                    continue;
                }
                try {
                    cronParser.parse(cronExpr);
                } catch (Exception e) {
                    plugin.getLogger().warning("Task '" + key + "' (cron) has invalid cron expression: '" + cronExpr + "'. " + e.getMessage() + ". Skipping...");
                    continue;
                }
                ScheduleTask task = new ScheduleTask(
                        key,
                        command,
                        type,
                        enabled,
                        null,
                        null,
                        0,
                        cronExpr
                );
                if (taskSection.getBoolean("enabled")) {
                    activeTasks.add(task);
                }
                schedule.add(task);
            }
        }
        return schedule;
    }

    public String updateTime(String name, String time) {
        ScheduleTask task = getTask(name);
        if (task == null) {
            return "Task not found";
        }

        String taskType = task.getType();
        String path = nameConfig + "." + name + ".time";


        if (taskType.equals("once")) {
            try {
                LocalDateTime.parse(time, formatter);
            } catch (Exception e) {
                return "Invalid time format (" + dateTimeFormat + ")";
            }

            if (LocalDateTime.parse(time, formatter).isBefore(LocalDateTime.now())) {
                return "Time must be in the future";
            }


            scheduler.set(nameConfig + "." + name + ".time", time);

        } else if (taskType.equals("daily")) {
            if (!time.matches(dailyPattern)) {
                return "Invalid time format (HH:mm)";
            }
            scheduler.set(nameConfig + "." + name + ".time", time);
        } else if (taskType.equals("repeat")) {
            if (!time.matches(repeatPattern)) {
                return "Invalid time format (minutes)";
            }
            scheduler.set(nameConfig + "." + name + ".interval", time);
            // Also update start_minutes if provided in config (default 0)
            int startMinutes = scheduler.getInt(nameConfig + "." + name + ".start_minutes", 0);
            scheduler.set(nameConfig + "." + name + ".start_minutes", startMinutes);
        } else if (taskType.equals("cron")) {
            try {
                cronParser.parse(time);
            } catch (Exception e) {
                return "Invalid cron expression";
            }
            scheduler.set(nameConfig + "." + name + ".cron", time);
        }
        saveConfig();
        tasks = getSchedule();
        return "Time updated";
    }

    public void updateStatus(String name, boolean status) {
        scheduler.set(nameConfig + "." + name + ".enabled", status);
        saveConfig();
        tasks = getSchedule();
    }

    public void updateCommand(String name, List<String> command) {
        scheduler.set(nameConfig + "." + name + ".command", command);
        saveConfig();
        tasks = getSchedule();
    }

    public boolean addTask(String name, String time, String type, List<String> command) {
        // Validate time format based on task type
        if (type.equals("once")) {
            try {
                LocalDateTime.parse(time, formatter);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid time format for task '" + name + "': " + time + " (expected: " + dateTimeFormat + ")");
                return false;
            }
            scheduler.set(nameConfig + "." + name + ".time", time);
        } else if (type.equals("daily")) {
            if (!time.matches(dailyPattern)) {
                plugin.getLogger().warning("Invalid time format for task '" + name + "': " + time + " (expected: HH:mm)");
                return false;
            }
            scheduler.set(nameConfig + "." + name + ".time", time);
        } else if (type.equals("repeat")) {
            if (!time.matches(repeatPattern)) {
                plugin.getLogger().warning("Invalid time format for task '" + name + "': " + time + " (expected: minutes as integer)");
                return false;
            }
            scheduler.set(nameConfig + "." + name + ".interval", time);
        } else if (type.equals("cron")) {
            try {
                cronParser.parse(time);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid cron expression for task '" + name + "': " + time);
                return false;
            }
            scheduler.set(nameConfig + "." + name + ".cron", time);
        } else {
            plugin.getLogger().warning("Invalid task type for task '" + name + "': " + type + " (supported: once, daily, repeat, cron)");
            return false;
        }
        
        scheduler.set(nameConfig + "." + name + ".enabled", true);
        scheduler.set(nameConfig + "." + name + ".type", type);
        scheduler.set(nameConfig + "." + name + ".command", command);
        saveConfig();
        this.tasks = getSchedule();
        return true;
    }

    public void removeTask(String name) {
        scheduler.set(nameConfig + "." + name, null);
        saveConfig();
        this.tasks = getSchedule();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public List<ScheduleTask> getActiveTasks() {
        return activeTasks;
    }

    public void updateSchedule(ScheduleTask task) {
        String key = task.getName();
        scheduler.set(nameConfig + "." + key + ".command", task.getCommand());
        scheduler.set(nameConfig + "." + key + ".type", task.getType());
        scheduler.set(nameConfig + "." + key + ".enabled", task.isEnabled());
        saveConfig();
    }


    public void saveConfig() {
        try {
            scheduler.save(new File(plugin.getDataFolder(), "schedule.yml"));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save schedule.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
