package me.virusker.schedulizer.commands;

import me.virusker.schedulizer.config.PluginConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScheduleCommand implements TabExecutor {
    private PluginConfig sConfig;

    public ScheduleCommand(PluginConfig sConfig) {
        this.sConfig = sConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (label.equalsIgnoreCase("Schedulizer")) {
//            sender.sendMessage("Schedule command");
//            return true;

            if (args.length == 0) {
                sender.sendMessage("You must provide an argument");
                return false;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage("list");
                    sConfig.getTasks().forEach(task -> {
                        sender.sendMessage(task.getName());
                    });
                    return true;
                } else if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage("list: list all tasks");
                    sender.sendMessage("add <name> <time/cron> <type> <command>: add a task");
                    sender.sendMessage("remove <name>: remove a task");
                    sender.sendMessage("time <name> <time>: update a task time");
                    sender.sendMessage("status <name> <status>: update a task status");
                    sender.sendMessage("cmd <name> <command>; <command>: update a task command");
                    sender.sendMessage("Supported types: once, daily, repeat, cron");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    try {
                        sConfig.reload();
                        sender.sendMessage("Plugin reloaded successfully");
                    } catch (IOException | InvalidConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
            } else {
                if (args[0].equalsIgnoreCase("add")) {
                    if (args.length < 4) {
                        sender.sendMessage("You must provide a name, time, command and type");
                        return false;
                    }
                    String name = args[1];
                    String time = args[2];
                    String type = args[3];

                    String commandStr = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                    List<String> cmd = List.of(commandStr.split("; "));

                    boolean success = sConfig.addTask(name, time, type, cmd);
                    if (!success) {
                        sender.sendMessage("Failed to add task: Invalid time format or type");
                        return false;
                    }
                    sender.sendMessage("Task added successfully");
                    return true;
                } else if (args[0].equalsIgnoreCase("remove")) {
                    String name = args[1];
                    sConfig.removeTask(name);
                    sender.sendMessage("Task '" + name + "' removed successfully");
                    return true;
                } else if (args[0].equalsIgnoreCase("time")) {
//                    sender.sendMessage("update");
                    String name = args[1];
                    String time = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    String r = sConfig.updateTime(name, time);
                    sender.sendMessage(r);
//                    sConfig.updateTime();
                    return true;
                } else if (args[0].equalsIgnoreCase("status")) {

                    String name = args[1];
                    boolean status = args[2].equalsIgnoreCase("true") || args[2].equals("1");

                    sender.sendMessage("> " + name + ": " + status);
                    sConfig.updateStatus(name, status);

                    return true;
                } else if (args[0].equalsIgnoreCase("cmd")) {
                    String name = args[1];
                    String commandStr = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    List<String> cmd = List.of(commandStr.split("; "));
                    sConfig.updateCommand(name, cmd);
                    sender.sendMessage("Task '" + name + "' command updated successfully");
                    return true;


                } else {
                    sender.sendMessage("argument: " + args[0]);
                    return true;
                }

            }

        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help"); // 0 args
            completions.add("list");  // 0 args
            completions.add("add"); // 4 args
            completions.add("remove"); // 2 args
            completions.add("time"); // 3 args
            completions.add("status"); // 3 args
            completions.add("cmd"); // 3 args
            completions.add("reload"); // 0 args

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list") ||
                    args[0].equalsIgnoreCase("add") ||
                    args[0].equalsIgnoreCase("reload") ||
                    args[0].equalsIgnoreCase("help"))
                return null;
            completions = sConfig.getTasks().stream().map(task -> task.getName()).toList();
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("status")) {
                completions.add("true");
                completions.add("false");
            } else {
                return null;
            }
        }
        return completions;
    }
}
