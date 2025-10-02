package me.virusker.schedulizer.config;

import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
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

    private final String dailyPattern = "\\d{2}:\\d{2}";

    private final String repeatPattern = "\\d+";

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

        this.tasks = getSchedule();
    }

    public List<ScheduleTask> getTasks() {
        return tasks;
    }

    public ZoneId getZoneId() {
        String timezone = config.getString("timezone");
        return (timezone == null || timezone.isEmpty()) ? ZoneId.systemDefault() : ZoneId.of(timezone);
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
        List<ScheduleTask> schedule = new ArrayList<ScheduleTask>();
        activeTasks.clear();
        for (String key : scheduler.getConfigurationSection(nameConfig).getKeys(false)) {

            ConfigurationSection taskSection = scheduler.getConfigurationSection(nameConfig + "." + key);
            if (taskSection == null) continue;

//            if (isEnabled && !taskSection.getBoolean("enabled")) {
//                continue;
//            }
            List<String> command = taskSection.getStringList("command");
            String type = taskSection.getString("type");
            boolean enabled = taskSection.getBoolean("enabled");
            
            // Read conditions
            Integer minPlayers = taskSection.isSet("conditions.min-players") ? taskSection.getInt("conditions.min-players") : null;
            Integer maxPlayers = taskSection.isSet("conditions.max-players") ? taskSection.getInt("conditions.max-players") : null;
            String timeOfDay = taskSection.isSet("conditions.time-of-day") ? taskSection.getString("conditions.time-of-day") : null;

            if (type.equals("once")) {
                // format time (dd/MM/yyyy HH:mm)
                LocalDateTime time;
                try {
                    String time_ = taskSection.getString("time");
                    time = LocalDateTime.parse(time_, formatter);
                } catch (Exception e) {
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
                        minPlayers,
                        maxPlayers,
                        timeOfDay
                );
                if (taskSection.getBoolean("enabled")) {
                    activeTasks.add(task);
                }
                schedule.add(task);

            } else if (type.equals("daily")) {
                // format time (HH:mm)
                String time_ = taskSection.getString("time");
                if (!time_.matches(dailyPattern)) {
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
                        minPlayers,
                        maxPlayers,
                        timeOfDay
                );
                if (taskSection.getBoolean("enabled")) {
                    activeTasks.add(task);
                }
                schedule.add(task);

            } else if (type.equals("repeat")) {
                // format time (minutes)
                String time_ = taskSection.getString("interval");
                if (!time_.matches(repeatPattern)) {
                    continue;
                }
                long time = Long.parseLong(time_);
                ScheduleTask task = new ScheduleTask(
                        key,
                        command,
                        type,
                        enabled,
                        null,
                        null,
                        time,
                        minPlayers,
                        maxPlayers,
                        timeOfDay
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

    public void updateConditions(String name, Integer minPlayers, Integer maxPlayers, String timeOfDay) {
        if (minPlayers != null) {
            scheduler.set(nameConfig + "." + name + ".conditions.min-players", minPlayers);
        }
        if (maxPlayers != null) {
            scheduler.set(nameConfig + "." + name + ".conditions.max-players", maxPlayers);
        }
        if (timeOfDay != null) {
            scheduler.set(nameConfig + "." + name + ".conditions.time-of-day", timeOfDay);
        }
        saveConfig();
        tasks = getSchedule();
    }

    public void clearConditions(String name) {
        scheduler.set(nameConfig + "." + name + ".conditions", null);
        saveConfig();
        tasks = getSchedule();
    }

    public void addTask(String name, String time, String type, List<String> command) {
        scheduler.set(nameConfig + "." + name + ".enabled", true);
        scheduler.set(nameConfig + "." + name + ".type", type);
        if (type.equals("once")) {
            scheduler.set(nameConfig + "." + name + ".time", time);
        } else if (type.equals("daily")) {
            scheduler.set(nameConfig + "." + name + ".time", time);
        } else if (type.equals("repeat")) {
            scheduler.set(nameConfig + "." + name + ".interval", time);
        }
        scheduler.set(nameConfig + "." + name + ".command", command);
        saveConfig();
        this.tasks = getSchedule();
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
            e.printStackTrace();
        }
    }
}
