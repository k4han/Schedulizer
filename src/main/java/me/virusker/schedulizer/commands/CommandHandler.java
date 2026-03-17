package me.virusker.schedulizer.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandHandler {
    /**
     * Execute the command
     * @param sender Command sender
     * @param args Command arguments
     * @return true if command executed successfully
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Get tab completions for the command
     * @param sender Command sender
     * @param args Command arguments
     * @return List of completions
     */
    List<String> getCompletions(CommandSender sender, String[] args);

    /**
     * Get the permission required for this command
     * @return Permission string or null if no permission required
     */
    String getPermission();

    /**
     * Get the minimum arguments required for this command
     * @return Minimum argument count
     */
    default int getMinArgs() {
        return 0;
    }

    /**
     * Get the usage message for this command
     * @return Usage message
     */
    String getUsage();
}
