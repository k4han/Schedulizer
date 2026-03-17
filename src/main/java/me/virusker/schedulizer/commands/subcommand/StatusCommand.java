package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class StatusCommand extends BaseCommand {
    private static final List<String> VALID_STATUS = List.of("true", "false", "1", "0", "enable", "disable", "on", "off");
    
    public StatusCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, "Usage: " + getUsage());
            return false;
        }

        String name = args[1];
        String statusArg = args[2].toLowerCase();
        
        ScheduleTask task = pluginConfig.getTask(name);
        if (task == null) {
            sendMessage(sender, "Task '&e" + name + "&c' not found!");
            return false;
        }

        boolean status;
        if (statusArg.equals("true") || statusArg.equals("1") || statusArg.equals("enable") || statusArg.equals("on")) {
            status = true;
        } else if (statusArg.equals("false") || statusArg.equals("0") || statusArg.equals("disable") || statusArg.equals("off")) {
            status = false;
        } else {
            sendMessage(sender, "Invalid status '&e" + statusArg + "&c'. Valid values: &etrue, false, 1, 0, enable, disable, on, off");
            return false;
        }

        pluginConfig.updateStatus(name, status);
        
        String statusText = status ? "&aActive" : "&cDisabled";
        sendSuccess(sender, "Task '&e" + name + "&a' status set to: " + statusText);
        
        return true;
    }

    @Override
    public List<String> getCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return getTaskCompletions(sender, args[1]);
        } else if (args.length == 3) {
            return filterByPartial(List.of("true", "false"), args[2]);
        }
        return new ArrayList<>();
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
