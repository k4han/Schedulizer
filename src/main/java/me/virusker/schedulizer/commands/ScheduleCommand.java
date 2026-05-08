package me.virusker.schedulizer.commands;

import me.virusker.schedulizer.commands.subcommand.*;
import me.virusker.schedulizer.config.PluginConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class ScheduleCommand implements TabExecutor {
    private final PluginConfig pluginConfig;
    private final Map<String, BaseCommand> subCommands;

    public ScheduleCommand(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
        this.subCommands = new HashMap<>();
        
        // Register all sub-commands
        registerSubCommand(new HelpCommand(pluginConfig));
        registerSubCommand(new ListCommand(pluginConfig));
        registerSubCommand(new ReloadCommand(pluginConfig));
        registerSubCommand(new AddCommand(pluginConfig));
        registerSubCommand(new RemoveCommand(pluginConfig));
        registerSubCommand(new StatusCommand(pluginConfig));
        registerSubCommand(new CmdCommand(pluginConfig));
        registerSubCommand(new InfoCommand(pluginConfig));
        registerSubCommand(new ExecuteCommand(pluginConfig));
    }

    private void registerSubCommand(BaseCommand command) {
        subCommands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check base permission
        if (!sender.hasPermission("schedulizer.use")) {
            sender.sendMessage(BaseCommand.colorize("&cYou do not have permission to use this command."));
            return true;
        }

        // If no arguments, show help
        if (args.length == 0) {
            subCommands.get("help").execute(sender, args);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        BaseCommand subCommand = subCommands.get(subCommandName);

        // Unknown sub-command
        if (subCommand == null) {
            sender.sendMessage(BaseCommand.colorize("&cUnknown command: &e" + subCommandName));
            sender.sendMessage(BaseCommand.colorize("&7Use &e/schedulizer help &7to see available commands."));
            return true;
        }

        // Check sub-command permission
        if (!subCommand.checkPermission(sender)) {
            return true;
        }

        // Check minimum arguments
        if (args.length - 1 < subCommand.getMinArgs()) {
            sender.sendMessage(BaseCommand.colorize("&cUsage: " + subCommand.getUsage()));
            return true;
        }

        // Execute the sub-command
        try {
            return subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            pluginConfig.getPlugin().getLogger().severe(
                "Error executing command '" + subCommand.getName() + "': " + e.getMessage()
            );
            sender.sendMessage(BaseCommand.colorize("&cAn error occurred while executing this command. Check console for details."));
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Check base permission
        if (!sender.hasPermission("schedulizer.use")) {
            return new ArrayList<>();
        }

        // If only one argument, complete sub-command names
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            
            for (BaseCommand subCommand : subCommands.values()) {
                if (subCommand.hasPermission(sender)) {
                    String name = subCommand.getName().toLowerCase();
                    if (name.startsWith(partial)) {
                        completions.add(name);
                    }
                }
            }
            
            return completions;
        }

        // Get the sub-command for further completions
        String subCommandName = args[0].toLowerCase();
        BaseCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null && subCommand.hasPermission(sender)) {
            // Pass the remaining arguments to the sub-command
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return subCommand.getCompletions(sender, subArgs);
        }

        return new ArrayList<>();
    }
}
