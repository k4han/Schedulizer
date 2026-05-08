package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;

public class ReloadCommand extends BaseCommand {
    
    public ReloadCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            pluginConfig.reload();
            sendSuccess(sender, "Configuration reloaded successfully!");
            sendInfo(sender, "Loaded &e" + pluginConfig.getTasks().size() + "&r tasks.");
        } catch (IOException | InvalidConfigurationException e) {
            pluginConfig.getPlugin().getLogger().severe("Failed to reload configuration: " + e.getMessage());
            sendMessage(sender, "Failed to reload configuration. Check console for details.");
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload plugin configuration";
    }

    @Override
    public String getUsage() {
        return "/Schedulizer reload";
    }
}
