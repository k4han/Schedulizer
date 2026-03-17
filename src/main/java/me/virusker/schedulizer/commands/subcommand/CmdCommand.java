package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdCommand extends BaseCommand {
    
    public CmdCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, "Usage: " + getUsage());
            return false;
        }

        String name = args[1];
        String commandStr = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        List<String> commands = Arrays.asList(commandStr.split(";\\s*"));
        
        ScheduleTask task = pluginConfig.getTask(name);
        if (task == null) {
            sendMessage(sender, "Task '&e" + name + "&c' not found!");
            return false;
        }

        if (commands.isEmpty() || commands.get(0).isEmpty()) {
            sendMessage(sender, "Commands cannot be empty!");
            return false;
        }

        pluginConfig.updateCommand(name, commands);
        
        sendSuccess(sender, "Task '&e" + name + "&a' commands updated!");
        sendInfo(sender, "Total commands: &e" + commands.size());
        
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
        return "cmd";
    }

    @Override
    public String getDescription() {
        return "Update task commands (separate multiple commands with ';')";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/Schedulizer cmd <name> <command1; command2; ...>";
    }
}
