package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RemoveCommand extends BaseCommand {
    
    public RemoveCommand(PluginConfig pluginConfig) {
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

        pluginConfig.removeTask(name);
        sendSuccess(sender, "Task '&e" + name + "&a' removed successfully!");
        
        return true;
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
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove a scheduled task";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/Schedulizer remove <name>";
    }
}
