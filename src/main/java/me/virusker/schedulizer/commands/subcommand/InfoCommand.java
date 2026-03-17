package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand extends BaseCommand {
    
    public InfoCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "Usage: " + getUsage());
            return false;
        }

        String name = args[1];
        ScheduleTask task = pluginConfig.getTask(name);

        if (task == null) {
            sendMessage(sender, "Task '&e" + name + "&c' not found!");
            return false;
        }

        sendInfo(sender, "&6=== Task Info: &e" + task.getName() + " &6===");
        sender.sendMessage(colorize(String.format(
            "  &fStatus: %s",
            task.isEnabled() ? "&aActive" : "&cDisabled"
        )));
        sender.sendMessage(colorize(String.format(
            "  &fType: &e%s",
            task.getType().toUpperCase()
        )));
        sender.sendMessage(colorize(String.format(
            "  &fTime: &e%s",
            getTimeInfo(task)
        )));
        sender.sendMessage(colorize("  &fCommands:"));
        for (String cmd : task.getCommand()) {
            sender.sendMessage(colorize("    &7- &f" + cmd));
        }

        return true;
    }

    private String getTimeInfo(ScheduleTask task) {
        switch (task.getType()) {
            case "once":
                return task.getExecutionTime().format(pluginConfig.getFormatter());
            case "daily":
                return task.getDailyTime().toString();
            case "repeat":
                return task.getInterval() + " minutes (start offset: " + task.getStartMinutes() + " min)";
            case "cron":
                return task.getCronExpression();
            default:
                return "Unknown";
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
        return "info";
    }

    @Override
    public String getDescription() {
        return "View detailed information about a task";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/Schedulizer info <name>";
    }
}
