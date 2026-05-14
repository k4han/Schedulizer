package me.virusker.schedulizer.config;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import me.virusker.schedulizer.models.ScheduleTask;
import me.virusker.schedulizer.models.TaskType;
import me.virusker.schedulizer.scheduler.PlatformScheduler;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class PluginConfig {
    private final String scheduleFile = "schedule.yml";
    private final String nameConfig = "schedules";
    private final FileConfiguration config;
    private final FileConfiguration scheduler;
    private final JavaPlugin plugin;
    private final PlatformScheduler platformScheduler;
    private final Object schedulerLock = new Object();
    private volatile List<ScheduleTask> tasks = new ArrayList<>();
    private volatile List<ScheduleTask> activeTasks = new CopyOnWriteArrayList<>();
    private volatile String dateTimeFormat;
    private volatile DateTimeFormatter formatter;
    private volatile ZoneId cachedZoneId;

    private static final Pattern DAILY_TIME_PATTERN = Pattern.compile("^\\d{2}:\\d{2}$");
    private static final Pattern REPEAT_PATTERN = Pattern.compile("^\\d+$");
    private final CronParser cronParser;

    public PluginConfig(JavaPlugin plugin, PlatformScheduler platformScheduler) {
        File file = new File(plugin.getDataFolder(), scheduleFile);
        if (!file.exists()) {
            plugin.saveResource(scheduleFile, false);
        }
        this.scheduler = YamlConfiguration.loadConfiguration(file);
        this.config = plugin.getConfig();
        this.plugin = plugin;
        this.platformScheduler = platformScheduler;
        dateTimeFormat = config.getString("datetime-format");
        if (dateTimeFormat == null || dateTimeFormat.isEmpty())
            this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        else
            this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);

        // Initialize cron parser with UNIX type (minute, hour, day, month, weekday)
        this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

        // Initialize shared cron parser in ScheduleTask
        ScheduleTask.initCronParser(this.cronParser);

        // Cache timezone to avoid repeated YAML reads on hot path
        this.cachedZoneId = loadZoneId();

        this.tasks = getSchedule();
    }

    /**
     * Load and cache the ZoneId from config.
     */
    private ZoneId loadZoneId() {
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

    public String getDailyPattern() {
        return DAILY_TIME_PATTERN.pattern();
    }

    public String getRepeatPattern() {
        return REPEAT_PATTERN.pattern();
    }

    public Pattern getDailyTimePattern() {
        return DAILY_TIME_PATTERN;
    }

    public Pattern getRepeatPatternCompiled() {
        return REPEAT_PATTERN;
    }

    public void executeCommands(List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        List<String> commandSnapshot = new ArrayList<>(commands);
        platformScheduler.executeGlobal(() -> {
            org.bukkit.Server server = plugin.getServer();
            org.bukkit.command.CommandSender console = server.getConsoleSender();
            for (String command : commandSnapshot) {
                if (command == null || command.trim().isEmpty()) {
                    continue;
                }
                server.dispatchCommand(console, command);
            }
        });
    }

    public ZoneId getZoneId() {
        return cachedZoneId;
    }

    public long getTick() {
        return Math.max(1L, config.getLong("tick", 300L));
    }
    public void reload() throws IOException, InvalidConfigurationException {
        synchronized (schedulerLock) {
            // Reload config.yml
            plugin.reloadConfig();
            this.config.load(new File(plugin.getDataFolder(), "config.yml"));

            dateTimeFormat = config.getString("datetime-format");
            if (dateTimeFormat == null || dateTimeFormat.isEmpty()) {
                this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            } else {
                this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
            }

            // Update cached timezone after reload
            this.cachedZoneId = loadZoneId();

            // Reload schedule.yml
            File file = new File(plugin.getDataFolder(), scheduleFile);
            if (!file.exists()) {
                plugin.saveResource(scheduleFile, false);
            }
            this.scheduler.load(file);

            this.tasks = getSchedule();
        }
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
        synchronized (schedulerLock) {
            List<ScheduleTask> schedule = new CopyOnWriteArrayList<>();
            List<ScheduleTask> newActiveTasks = new CopyOnWriteArrayList<>();

            ConfigurationSection section = scheduler.getConfigurationSection(nameConfig);
            if (section == null) {
                this.activeTasks = newActiveTasks;
                return schedule;
            }

            for (String key : section.getKeys(false)) {
                ConfigurationSection taskSection = scheduler.getConfigurationSection(nameConfig + "." + key);
                if (taskSection == null) continue;

                List<String> command = taskSection.getStringList("command");
                String typeStr = taskSection.getString("type");
                TaskType type = TaskType.fromString(typeStr);
                boolean enabled = taskSection.getBoolean("enabled");

                if (type == null || command == null) {
                    plugin.getLogger().warning("Task '" + key + "' has invalid configuration (missing type or command). Skipping...");
                    continue;
                }



                if (type == TaskType.ONCE) {
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
                    int startMinutes = (int) (time.atZone(getZoneId()).toEpochSecond() / 60);
                    ScheduleTask task = new ScheduleTask(
                            key,
                            command,
                            type,
                            enabled,
                            time,
                            null,
                            0,
                            null,
                            startMinutes
                    );
                    if (enabled) {
                        newActiveTasks.add(task);
                    }
                    schedule.add(task);

                } else if (type == TaskType.DAILY) {
                    // format time (HH:mm)
                    String time_ = taskSection.getString("time");
                    if (time_ == null) {
                        plugin.getLogger().warning("Task '" + key + "' (daily) is missing 'time' field. Skipping...");
                        continue;
                    }
                    if (!DAILY_TIME_PATTERN.matcher(time_).matches()) {
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
                    if (enabled) {
                        newActiveTasks.add(task);
                    }
                    schedule.add(task);

                } else if (type == TaskType.REPEAT) {
                    // format time (minutes)
                    String time_ = taskSection.getString("interval");
                    if (time_ == null) {
                        plugin.getLogger().warning("Task '" + key + "' (repeat) is missing 'interval' field. Skipping...");
                        continue;
                    }
                    if (!REPEAT_PATTERN.matcher(time_).matches()) {
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
                    if (enabled) {
                        newActiveTasks.add(task);
                    }
                    schedule.add(task);
                } else if (type == TaskType.CRON) {
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
                    if (enabled) {
                        newActiveTasks.add(task);
                    }
                    schedule.add(task);
                }
            }

            // Atomically replace active tasks list
            this.activeTasks = newActiveTasks;
            return schedule;
        }
    }

    public String updateTime(String name, String time) {
        synchronized (schedulerLock) {
            ScheduleTask task = getTask(name);
            if (task == null) {
                return "Task not found";
            }

            TaskType taskType = task.getType();

            if (taskType == TaskType.ONCE) {
                LocalDateTime parsed;
                try {
                    parsed = LocalDateTime.parse(time, formatter);
                } catch (Exception e) {
                    return "Invalid time format (" + dateTimeFormat + ")";
                }

                if (parsed.isBefore(LocalDateTime.now())) {
                    return "Time must be in the future";
                }

                scheduler.set(nameConfig + "." + name + ".time", time);

            } else if (taskType == TaskType.DAILY) {
                if (!DAILY_TIME_PATTERN.matcher(time).matches()) {
                    return "Invalid time format (HH:mm)";
                }
                scheduler.set(nameConfig + "." + name + ".time", time);
            } else if (taskType == TaskType.REPEAT) {
                if (!REPEAT_PATTERN.matcher(time).matches()) {
                    return "Invalid time format (minutes)";
                }
                scheduler.set(nameConfig + "." + name + ".interval", time);
                // Also update start_minutes if provided in config (default 0)
                int startMinutes = scheduler.getInt(nameConfig + "." + name + ".start_minutes", 0);
                scheduler.set(nameConfig + "." + name + ".start_minutes", startMinutes);
            } else if (taskType == TaskType.CRON) {
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
    }

    public void updateStatus(String name, boolean status) {
        synchronized (schedulerLock) {
            scheduler.set(nameConfig + "." + name + ".enabled", status);
            saveConfig();
            tasks = getSchedule();
        }
    }

    /**
     * Targeted update of task enabled flag without full schedule rebuild.
     */
    public void setTaskEnabled(String name, boolean status) {
        synchronized (schedulerLock) {
            scheduler.set(nameConfig + "." + name + ".enabled", status);
            saveConfig();
        }
    }

    public void updateCommand(String name, List<String> command) {
        synchronized (schedulerLock) {
            scheduler.set(nameConfig + "." + name + ".command", command);
            saveConfig();
            tasks = getSchedule();
        }
    }

    public boolean addTask(String name, String time, TaskType taskType, List<String> command) {
        synchronized (schedulerLock) {
            String basePath = nameConfig + "." + name;

            if (taskType == null) {
                plugin.getLogger().warning("Invalid task type for task '" + name + "': null");
                return false;
            }

            // Reset all schedule-specific fields to avoid stale data when overwriting task type.
            scheduler.set(basePath + ".time", null);
            scheduler.set(basePath + ".interval", null);
            scheduler.set(basePath + ".cron", null);

            // Validate time format based on task type
            if (taskType == TaskType.ONCE) {
                try {
                    LocalDateTime.parse(time, formatter);
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid time format for task '" + name + "': " + time + " (expected: " + dateTimeFormat + ")");
                    return false;
                }
                scheduler.set(basePath + ".time", time);
            } else if (taskType == TaskType.DAILY) {
                if (!DAILY_TIME_PATTERN.matcher(time).matches()) {
                    plugin.getLogger().warning("Invalid time format for task '" + name + "': " + time + " (expected: HH:mm)");
                    return false;
                }
                scheduler.set(basePath + ".time", time);
            } else if (taskType == TaskType.REPEAT) {
                if (!REPEAT_PATTERN.matcher(time).matches()) {
                    plugin.getLogger().warning("Invalid time format for task '" + name + "': " + time + " (expected: minutes as integer)");
                    return false;
                }
                scheduler.set(basePath + ".interval", time);
            } else if (taskType == TaskType.CRON) {
                try {
                    cronParser.parse(time);
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid cron expression for task '" + name + "': " + time);
                    return false;
                }
                scheduler.set(basePath + ".cron", time);
            }

            scheduler.set(basePath + ".enabled", true);
            scheduler.set(basePath + ".type", taskType.getValue());
            scheduler.set(basePath + ".command", command);
            saveConfig();
            this.tasks = getSchedule();
            return true;
        }
    }

    public void removeTask(String name) {
        synchronized (schedulerLock) {
            scheduler.set(nameConfig + "." + name, null);
            saveConfig();
            this.tasks = getSchedule();
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public List<ScheduleTask> getActiveTasks() {
        return activeTasks;
    }

    public void updateSchedule(ScheduleTask task) {
        synchronized (schedulerLock) {
            String key = task.getName();
            scheduler.set(nameConfig + "." + key + ".command", task.getCommand());
            scheduler.set(nameConfig + "." + key + ".type", task.getType().getValue());
            scheduler.set(nameConfig + "." + key + ".enabled", task.isEnabled());
            saveConfig();
        }
    }


    public void saveConfig() {
        synchronized (schedulerLock) {
            try {
                scheduler.save(new File(plugin.getDataFolder(), "schedule.yml"));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save schedule.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
