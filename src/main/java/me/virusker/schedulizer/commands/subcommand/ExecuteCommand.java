package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ExecuteCommand extends BaseCommand {
    
    public ExecuteCommand(PluginConfig pluginConfig) {
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

        // Execute the task commands
        List<String> commands = task.getCommand();
        int executed = 0;
        
        for (String command : commands) {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                executed++;
            } catch (Exception e) {
                pluginConfig.getPlugin().getLogger().warning(
                    "Failed to execute command '" + command + "' for task '" + name + "': " + e.getMessage()
                );
            }
        }

        sendSuccess(sender, "Task '&e" + name + "&a' executed successfully!");
        sendInfo(sender, "Executed &e" + executed + "&r/" + commands.size() + " &rcommands.");
        
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
        return "/Schedulizer execute <name>";
    }
}
