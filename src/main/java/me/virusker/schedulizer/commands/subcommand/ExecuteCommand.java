package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ExecuteCommand extends BaseCommand {
    
    public ExecuteCommand(PluginConfig pluginConfig) {
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

        // Execute the task commands
        List<String> commands = task.getCommand();
        pluginConfig.executeCommands(commands);

        sendSuccess(sender, "Task '&e" + name + "&a' executed successfully!");
        sendInfo(sender, "Executed &e" + commands.size() + " &rcommands.");
        
        return true;
    }

    @Override
    public List<String> getCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return getTaskCompletions(sender, args[0]);
        }
        return EMPTY_COMPLETIONS;
    }

    @Override
    public String getName() {
        return "execute";
    }

    @Override
    public String getDescription() {
        return "Force execute a task immediately";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/schedulizer execute <name>";
    }
}
