package me.virusker.schedulizer.commands.subcommand;

import me.virusker.schedulizer.commands.BaseCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.models.ScheduleTask;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends BaseCommand {
    
    public ListCommand(PluginConfig pluginConfig) {
        super(pluginConfig);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        List<ScheduleTask> tasks = pluginConfig.getTasks();
        
        if (tasks.isEmpty()) {
            sendInfo(sender, "No tasks found. Use /Schedulizer help to see available commands.");
            return true;
        }

        sendInfo(sender, "&6=== Scheduled Tasks (&e" + tasks.size() + "&6) ===");
        
        for (ScheduleTask task : tasks) {
            String status = task.isEnabled() ? "&a[ACTIVE]" : "&c[DISABLED]";
            String type = "&e" + task.getType().toUpperCase();
            String time = getTimeInfo(task);
            
            sender.sendMessage(colorize(String.format(
                "  &f%s %s %s &8- %s",
                status, type, task.getName(), time
            )));
        }

        return true;
    }

    private String getTimeInfo(ScheduleTask task) {
        switch (task.getType()) {
            case "once":
                return "&7at: &f" + task.getExecutionTime().format(pluginConfig.getFormatter());
            case "daily":
                return "&7at: &f" + task.getDailyTime().toString();
            case "repeat":
                return "&7every: &f" + task.getInterval() + " minutes";
            case "cron":
                return "&7cron: &f" + task.getCronExpression();
            default:
                return "&7unknown type";
        }
    }

    @Override
    public List<String> getCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all scheduled tasks";
    }

    @Override
    public String getUsage() {
        return "/Schedulizer list";
    }
}
