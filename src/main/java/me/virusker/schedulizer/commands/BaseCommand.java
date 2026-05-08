package me.virusker.schedulizer.commands;

import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand implements CommandHandler {
    protected final PluginConfig pluginConfig;

    /** Shared empty list for tab completion fallback - avoids repeated allocations */
    protected static final List<String> EMPTY_COMPLETIONS = List.of();

    public BaseCommand(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    @Override
    public String getPermission() {
        return "schedulizer.command." + getName();
    }

    /**
     * Get the sub-command name
     * @return Command name
     */
    public abstract String getName();

    /**
     * Get the description of this command
     * @return Description
     */
    public abstract String getDescription();

    /**
     * Send a message to sender without automatic color prefix
     */
    protected void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Send success message to sender
     */
    protected void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(colorize("&a" + message));
    }

    /**
     * Send info message to sender
     */
    protected void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(colorize("&b" + message));
    }

    /**
     * Send warning message to sender
     */
    protected void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(colorize("&e" + message));
    }

    /**
     * Colorize a message using Adventure API.
     * Converts legacy '&' color codes to Adventure Components.
     */
    public static Component colorize(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    /**
     * Check if sender has permission
     */
    protected boolean hasPermission(CommandSender sender) {
        String perm = getPermission();
        return perm == null || sender.hasPermission(perm);
    }

    /**
     * Check if sender has permission and send error if not
     */
    protected boolean checkPermission(CommandSender sender) {
        if (!hasPermission(sender)) {
            sendMessage(sender, "You do not have permission to use this command.");
            return false;
        }
        return true;
    }

    /**
     * Get completions for player names
     */
    protected List<String> getTaskCompletions(CommandSender sender, String partial) {
        List<String> completions = new ArrayList<>();
        pluginConfig.getTasks().stream()
                .map(task -> task.getName())
                .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                .forEach(completions::add);
        return completions;
    }

    /**
     * Filter a list of strings by partial match
     */
    protected List<String> filterByPartial(List<String> list, String partial) {
        List<String> completions = new ArrayList<>();
        list.stream()
                .filter(s -> s.toLowerCase().startsWith(partial.toLowerCase()))
                .forEach(completions::add);
        return completions;
    }

    /**
     * Get a task by name, sending an error message if not found.
     * Returns null if the task does not exist (caller should return false).
     */
    protected ScheduleTask getTaskOrFail(CommandSender sender, String name) {
        ScheduleTask task = pluginConfig.getTask(name);
        if (task == null) {
            sendMessage(sender, "Task '&e" + name + "&c' not found!");
        }
        return task;
    }

    /**
     * Get the status label for a task (for display in list/info commands).
     */
    protected String getStatusLabel(boolean enabled) {
        return enabled ? "&a[ACTIVE]" : "&c[DISABLED]";
    }

    /**
     * Default implementation returns empty list for tab completion.
     */
    @Override
    public List<String> getCompletions(CommandSender sender, String[] args) {
        return EMPTY_COMPLETIONS;
    }
}
