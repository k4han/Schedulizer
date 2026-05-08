package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends BaseCommand {
    
    public HelpCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sendInfo(sender, "&6=== Schedulizer Help ===");
        sender.sendMessage(colorize("&e/schedulizer list &7- List all tasks"));
        sender.sendMessage(colorize("&e/schedulizer help &7- Show this help"));
        sender.sendMessage(colorize("&e/schedulizer reload &7- Reload configuration"));
        sender.sendMessage(colorize("&e/schedulizer add <name> <type> <time...> <command> &7- Add a new task"));
        sender.sendMessage(colorize("&e/schedulizer remove &7- Remove a task"));
        sender.sendMessage(colorize("&e/schedulizer status &7- Toggle task status"));
        sender.sendMessage(colorize("&e/schedulizer cmd &7- Update task commands"));
        sender.sendMessage(colorize("&e/schedulizer info &7- View task details"));
        sender.sendMessage(colorize("&e/schedulizer execute &7- Force execute a task"));
        sender.sendMessage("");
        sender.sendMessage(colorize("&7Task types: &fonce, daily, repeat, cron"));
        sender.sendMessage(colorize("&7Time format: &fdd/MM/yyyy HH:mm (once), HH:mm (daily), minutes (repeat), UNIX cron (cron)"));
        
        return true;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show help information";
    }

    @Override
    public String getUsage() {
        return "/schedulizer help";
    }
}
