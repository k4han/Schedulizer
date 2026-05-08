package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InfoCommand extends BaseCommand {

    public InfoCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendMessage(sender, "Usage: " + getUsage());
            return false;
        }

        String name = args[0];
        ScheduleTask task = getTaskOrFail(sender, name);
        if (task == null) return false;

        sendInfo(sender, "&6=== Task Info: &e" + task.getName() + " &6===");
        sender.sendMessage(colorize(String.format(
            "  &fStatus: %s",
            getStatusLabel(task.isEnabled())
        )));
        sender.sendMessage(colorize(String.format(
            "  &fType: &e%s",
            task.getType().getValue().toUpperCase()
        )));
        sender.sendMessage(colorize(String.format(
            "  &fTime: &e%s",
            task.getTimeDisplay(pluginConfig.getFormatter())
        )));
        sender.sendMessage(colorize("  &fCommands:"));
        for (String cmd : task.getCommand()) {
            sender.sendMessage(colorize("    &7- &f" + cmd));
        }

        return true;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public List<String> getCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return getTaskCompletions(sender, args[0]);
        }
        return EMPTY_COMPLETIONS;
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
