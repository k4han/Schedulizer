package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class StatusCommand extends BaseCommand {
    private static final java.util.Set<String> ENABLING = java.util.Set.of("true", "1", "enable", "on");
    private static final java.util.Set<String> DISABLING = java.util.Set.of("false", "0", "disable", "off");

    public StatusCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "Usage: " + getUsage());
            return false;
        }

        String name = args[0];
        String statusArg = args[1].toLowerCase();

        ScheduleTask task = getTaskOrFail(sender, name);
        if (task == null) return false;

        boolean status;
        if (ENABLING.contains(statusArg)) {
            status = true;
        } else if (DISABLING.contains(statusArg)) {
            status = false;
        } else {
            sendMessage(sender, "Invalid status '&e" + statusArg + "&c'. Valid values: &etrue, false, 1, 0, enable, disable, on, off");
            return false;
        }

        pluginConfig.setTaskEnabled(name, status);
        task.setEnabled(status);

        String statusText = status ? "&aActive" : "&cDisabled";
        sendSuccess(sender, "Task '&e" + name + "&a' status set to: " + statusText);
        
        return true;
    }

    @Override
    public List<String> getCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return getTaskCompletions(sender, args[0]);
        } else if (args.length == 2) {
            return filterByPartial(List.of("true", "false"), args[1]);
        }
        return EMPTY_COMPLETIONS;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Toggle task enabled/disabled status";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/Schedulizer status <name> <true|false>";
    }
}
