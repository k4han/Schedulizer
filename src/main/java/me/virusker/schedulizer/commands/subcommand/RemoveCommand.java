package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RemoveCommand extends BaseCommand {
    
    public RemoveCommand(PluginConfig pluginConfig) {
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

        pluginConfig.removeTask(name);
        sendSuccess(sender, "Task '&e" + name + "&a' removed successfully!");
        
        return true;
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
