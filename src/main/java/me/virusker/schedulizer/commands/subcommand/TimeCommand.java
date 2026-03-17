package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeCommand extends BaseCommand {
    
    public TimeCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, "Usage: " + getUsage());
            return false;
        }

        String name = args[1];
        String time = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        
        ScheduleTask task = pluginConfig.getTask(name);
        if (task == null) {
            sendMessage(sender, "Task '&e" + name + "&c' not found!");
            return false;
        }

        // Validate time format based on task type
        if (!validateTimeFormat(time, task.getType(), sender)) {
            return false;
        }

        String result = pluginConfig.updateTime(name, time);
        
        if (result.equals("Time updated")) {
            sendSuccess(sender, "Task '&e" + name + "&a' time updated to: &e" + time);
        } else {
            sendMessage(sender, result);
            return false;
        }

        return true;
    }

    private boolean validateTimeFormat(String time, String type, CommandSender sender) {
        switch (type) {
            case "once":
                try {
                    java.time.LocalDateTime.parse(time, pluginConfig.getFormatter());
                    // Check if time is in the future
                    if (java.time.LocalDateTime.parse(time, pluginConfig.getFormatter()).isBefore(java.time.LocalDateTime.now())) {
                        sendMessage(sender, "Time must be in the future!");
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    sendMessage(sender, "Invalid time format for 'once' type.");
                    sendMessage(sender, "Expected format: &e" + pluginConfig.getDateTimeFormat());
                    return false;
                }
            case "daily":
                if (!time.matches("^\\d{2}:\\d{2}(:\\d{2})?$")) {
                    sendMessage(sender, "Invalid time format for 'daily' type.");
                    sendMessage(sender, "Expected format: &eHH:mm &7or &eHH:mm:ss");
                    return false;
                }
                return true;
            case "repeat":
                if (!time.matches("^\\d+$")) {
                    sendMessage(sender, "Invalid interval format for 'repeat' type.");
                    sendMessage(sender, "Expected: &e<minutes> &7(as integer)");
                    return false;
                }
                return true;
            case "cron":
                try {
                    pluginConfig.getCronParser().parse(time);
                    return true;
                } catch (Exception e) {
                    sendMessage(sender, "Invalid cron expression.");
                    sendMessage(sender, "Example: &e0 0 * * * &7(every day at midnight)");
                    return false;
                }
            default:
                return false;
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return getTaskCompletions(sender, args[1]);
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public String getDescription() {
        return "Update task time/interval";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/Schedulizer time <name> <time/interval/cron>";
    }
}
