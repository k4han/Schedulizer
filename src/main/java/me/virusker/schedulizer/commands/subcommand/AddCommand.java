package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddCommand extends BaseCommand {
    private static final List<String> VALID_TYPES = List.of("once", "daily", "repeat", "cron");
    
    public AddCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendMessage(sender, "Usage: " + getUsage());
            sendMessage(sender, "Example: /Schedulizer add myTask 10:30 daily broadcast; Hello World!");
            return false;
        }

        String name = args[1];
        String time = args[2];
        String type = args[3].toLowerCase();
        List<String> commands = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 4, args.length)));

        // Validate task type
        if (!VALID_TYPES.contains(type)) {
            sendMessage(sender, "Invalid task type '&e" + type + "&c'. Valid types: &e" + String.join(", ", VALID_TYPES));
            return false;
        }

        // Validate time format based on type
        if (!validateTimeFormat(time, type, sender)) {
            return false;
        }

        // Check if task already exists
        if (pluginConfig.getTask(name) != null) {
            sendMessage(sender, "Task '&e" + name + "&c' already exists!");
            return false;
        }

        boolean success = pluginConfig.addTask(name, time, type, commands);
        if (success) {
            sendSuccess(sender, "Task '&e" + name + "&a' added successfully!");
            sendInfo(sender, "Type: &e" + type.toUpperCase() + "&r, Time: &e" + time);
        } else {
            sendMessage(sender, "Failed to add task. Check console for details.");
        }

        return success;
    }

    private boolean validateTimeFormat(String time, String type, CommandSender sender) {
        switch (type) {
            case "once":
                try {
                    java.time.LocalDateTime.parse(time, pluginConfig.getFormatter());
                    return true;
                } catch (Exception e) {
                    sendMessage(sender, "Invalid time format for 'once' type.");
                    sendMessage(sender, "Expected format: &e" + pluginConfig.getDateTimeFormat());
                    sendMessage(sender, "Example: &e18/03/2026 20:00");
                    return false;
                }
            case "daily":
                if (!time.matches("^\\d{2}:\\d{2}(:\\d{2})?$")) {
                    sendMessage(sender, "Invalid time format for 'daily' type.");
                    sendMessage(sender, "Expected format: &eHH:mm &7or &eHH:mm:ss");
                    sendMessage(sender, "Example: &e10:30 &7or &e10:30:00");
                    return false;
                }
                return true;
            case "repeat":
                if (!time.matches("^\\d+$")) {
                    sendMessage(sender, "Invalid interval format for 'repeat' type.");
                    sendMessage(sender, "Expected: &e<minutes> &7(as integer)");
                    sendMessage(sender, "Example: &e60 &7(every 60 minutes)");
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
        } else if (args.length == 3) {
            return filterByPartial(VALID_TYPES, args[2]);
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Add a new scheduled task";
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public String getUsage() {
        return "/Schedulizer add <name> <time> <type> <command> [command2; ...]";
    }
}
