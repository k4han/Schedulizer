package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.TaskType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddCommand extends BaseCommand {
    private static final List<String> NAME_HINT = List.of("<name>");
    private static final List<String> ONCE_DATE_HINT = List.of("<date>");
    private static final List<String> ONCE_CLOCK_HINT = List.of("<time>");
    private static final List<String> DAILY_TIME_HINT = List.of("<HH:mm>");
    private static final List<String> REPEAT_TIME_HINT = List.of("<minutes>");
    private static final List<String> CRON_MINUTE_HINT = List.of("<minute>");
    private static final List<String> CRON_HOUR_HINT = List.of("<hour>");
    private static final List<String> CRON_DAY_HINT = List.of("<day>");
    private static final List<String> CRON_MONTH_HINT = List.of("<month>");
    private static final List<String> CRON_WEEKDAY_HINT = List.of("<weekday>");
    private static final List<String> COMMAND_HINT = List.of("<command>");
    
    public AddCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "Usage: " + getUsage());
            sendMessage(sender, "Examples:");
            sendMessage(sender, "- /schedulizer add myTask daily 10:30 broadcast Hello World!");
            sendMessage(sender, "- /schedulizer add myCron cron 0 0 * * * broadcast Midnight!");
            sendMessage(sender, "- /schedulizer add myOnce once 18/03/2026 20:00 broadcast Event!");
            return false;
        }

        String name = args[0];
        String type = args[1].toLowerCase();

        // Validate task type early to decide how to parse time args.
        TaskType taskType = TaskType.fromString(type);
        if (taskType == null) {
            sendMessage(sender, "Invalid task type '&e" + type + "&c'. Valid types: &eonce, daily, repeat, cron");
            return false;
        }

        String time;
        int commandStartIndex;

        switch (taskType) {
            case CRON:
                // UNIX cron: minute hour day month weekday
                // /Schedulizer add <name> cron <minute> <hour> <day> <month> <weekday> <command...>
                if (args.length < 7) {
                    sendMessage(sender, "Usage: &e/schedulizer add <name> cron <minute> <hour> <day> <month> <weekday> <command> [command2; ...]");
                    sendMessage(sender, "Example: &e/schedulizer add myCron cron 0 0 * * * broadcast Midnight!");
                    return false;
                }
                time = String.join(" ", Arrays.copyOfRange(args, 2, 7));
                commandStartIndex = 7;
                break;
            case ONCE:
                // Once time format may contain spaces (depends on datetime-format).
                // We progressively join args until the formatter can parse it.
                time = null;
                commandStartIndex = -1;

                for (int timeEndIndex = 2; timeEndIndex < args.length; timeEndIndex++) {
                    String candidate = String.join(" ", Arrays.copyOfRange(args, 2, timeEndIndex + 1));
                    try {
                        java.time.LocalDateTime.parse(candidate, pluginConfig.getFormatter());
                        time = candidate;
                        commandStartIndex = timeEndIndex + 1;
                        break;
                    } catch (Exception ignored) {
                        // keep trying
                    }
                }

                if (time == null) {
                    sendMessage(sender, "Invalid time format for 'once' type.");
                    sendMessage(sender, "Expected format: &e" + pluginConfig.getDateTimeFormat());
                    sendMessage(sender, "Example: &e18/03/2026 20:00");
                    sendMessage(sender, "Usage: &e/schedulizer add <name> once <date_time...> <command> [command2; ...]");
                    return false;
                }
                break;
            default:
                if (args.length < 4) {
                    sendMessage(sender, "Usage: " + getUsage());
                    sendMessage(sender, "Example: /schedulizer add myTask daily 10:30 broadcast Hello World!");
                    return false;
                }
                time = args[2];
                commandStartIndex = 3;
                break;
        }

        String commandStr = commandStartIndex >= args.length
                ? ""
                : String.join(" ", Arrays.copyOfRange(args, commandStartIndex, args.length));
        List<String> commands = new ArrayList<>(Arrays.asList(commandStr.split(";\\s*")));

        if (commands.isEmpty() || commands.get(0).trim().isEmpty()) {
            sendMessage(sender, "Commands cannot be empty!");
            return false;
        }

        // Validate time format based on type
        if (!validateTimeFormat(time, taskType, sender)) {
            return false;
        }

        boolean existed = pluginConfig.getTask(name) != null;

        boolean success = pluginConfig.addTask(name, time, taskType, commands);
        if (success) {
            if (existed) {
                sendSuccess(sender, "Task '&e" + name + "&a' updated successfully!");
            } else {
                sendSuccess(sender, "Task '&e" + name + "&a' added successfully!");
            }
            sendInfo(sender, "Type: &e" + type.toUpperCase() + "&r, Time: &e" + time);
        } else {
            sendMessage(sender, "Failed to add task. Check console for details.");
        }

        return success;
    }

    private boolean validateTimeFormat(String time, TaskType taskType, CommandSender sender) {
        switch (taskType) {
            case ONCE:
                try {
                    java.time.LocalDateTime parsedTime = java.time.LocalDateTime.parse(time, pluginConfig.getFormatter());
                    // Check if time is in the future
                    if (parsedTime.isBefore(java.time.LocalDateTime.now())) {
                        sendMessage(sender, "Time must be in the future!");
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    sendMessage(sender, "Invalid time format for 'once' type.");
                    sendMessage(sender, "Expected format: &e" + pluginConfig.getDateTimeFormat());
                    sendMessage(sender, "Example: &e18/03/2026 20:00");
                    return false;
                }
            case DAILY:
                if (!pluginConfig.getDailyTimePattern().matcher(time).matches()) {
                    sendMessage(sender, "Invalid time format for 'daily' type.");
                    sendMessage(sender, "Expected format: &eHH:mm");
                    sendMessage(sender, "Example: &e10:30");
                    return false;
                }
                return true;
            case REPEAT:
                if (!pluginConfig.getRepeatPatternCompiled().matcher(time).matches()) {
                    sendMessage(sender, "Invalid interval format for 'repeat' type.");
                    sendMessage(sender, "Expected: &e<minutes> &7(as integer)");
                    sendMessage(sender, "Example: &e60 &7(every 60 minutes)");
                    return false;
                }
                return true;
            case CRON:
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
        if (args.length == 1) {
            return filterByPartial(NAME_HINT, args[0]);
        }

        if (args.length == 2) {
            return filterByPartial(TaskType.VALID_TYPE_NAMES, args[1]);
        }

        TaskType taskType = args.length >= 2 ? TaskType.fromString(args[1].toLowerCase()) : null;

        if (args.length == 3) {
            if (taskType == TaskType.ONCE) {
                return filterByPartial(ONCE_DATE_HINT, args[2]);
            }
            if (taskType == TaskType.CRON) {
                return filterByPartial(CRON_MINUTE_HINT, args[2]);
            }
            if (taskType == TaskType.DAILY) {
                return filterByPartial(DAILY_TIME_HINT, args[2]);
            }
            if (taskType == TaskType.REPEAT) {
                return filterByPartial(REPEAT_TIME_HINT, args[2]);
            }
            return EMPTY_COMPLETIONS;
        }

        if (args.length == 4) {
            if (taskType == TaskType.ONCE) {
                return filterByPartial(ONCE_CLOCK_HINT, args[3]);
            }
            if (taskType == TaskType.CRON) {
                return filterByPartial(CRON_HOUR_HINT, args[3]);
            }
            return filterByPartial(COMMAND_HINT, args[3]);
        }

        if (args.length == 5) {
            if (taskType == TaskType.CRON) {
                return filterByPartial(CRON_DAY_HINT, args[4]);
            }
            if (taskType == TaskType.ONCE) {
                return filterByPartial(COMMAND_HINT, args[4]);
            }
        }

        if (args.length == 6 && taskType == TaskType.CRON) {
            return filterByPartial(CRON_MONTH_HINT, args[5]);
        }

        if (args.length == 7 && taskType == TaskType.CRON) {
            return filterByPartial(CRON_WEEKDAY_HINT, args[6]);
        }

        if (args.length == 8 && taskType == TaskType.CRON) {
            return filterByPartial(COMMAND_HINT, args[7]);
        }

        return EMPTY_COMPLETIONS;
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
        return "/schedulizer add <name> <type> <time...> <command> [command2; ...]";
    }
}
