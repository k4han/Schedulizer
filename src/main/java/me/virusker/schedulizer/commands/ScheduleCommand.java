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
                    sender.sendMessage("add <name> <time> <type> <command>: add a task");
                    sender.sendMessage("remove <name>: remove a task");
                    sender.sendMessage("time <name> <time>: update a task time");
                    sender.sendMessage("status <name> <status>: update a task status");
                    sender.sendMessage("cmd <name> <command>; <command>: update a task command");
                    sender.sendMessage("condition <name> minplayers:<value> maxplayers:<value> timeofday:<day|night>: set task conditions");
                    sender.sendMessage("clearcondition <name>: remove all conditions from a task");
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    try {
                        sConfig.reload();
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

                    sConfig.addTask(name, time, type, cmd);
                    return true;
                } else if (args[0].equalsIgnoreCase("remove")) {

                    String name = args[1];
                    sConfig.removeTask(name);
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
                    sender.sendMessage("cmd");
                    String name = args[1];
                    String commandStr = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    List<String> cmd = List.of(commandStr.split("; "));
                    sConfig.updateCommand(name, cmd);
                    return true;
                } else if (args[0].equalsIgnoreCase("condition")) {
                    if (args.length < 3) {
                        sender.sendMessage("Usage: condition <name> minplayers:<value> maxplayers:<value> timeofday:<day|night>");
                        return false;
                    }
                    String name = args[1];
                    Integer minPlayers = null;
                    Integer maxPlayers = null;
                    String timeOfDay = null;
                    
                    for (int i = 2; i < args.length; i++) {
                        String arg = args[i];
                        if (arg.startsWith("minplayers:")) {
                            try {
                                minPlayers = Integer.parseInt(arg.substring(11));
                            } catch (NumberFormatException e) {
                                sender.sendMessage("Invalid minplayers value");
                                return false;
                            }
                        } else if (arg.startsWith("maxplayers:")) {
                            try {
                                maxPlayers = Integer.parseInt(arg.substring(11));
                            } catch (NumberFormatException e) {
                                sender.sendMessage("Invalid maxplayers value");
                                return false;
                            }
                        } else if (arg.startsWith("timeofday:")) {
                            timeOfDay = arg.substring(10);
                            if (!timeOfDay.equalsIgnoreCase("day") && !timeOfDay.equalsIgnoreCase("night")) {
                                sender.sendMessage("Invalid timeofday value (use 'day' or 'night')");
                                return false;
                            }
                        }
                    }
                    
                    sConfig.updateConditions(name, minPlayers, maxPlayers, timeOfDay);
                    sender.sendMessage("Conditions updated for task: " + name);
                    return true;
                } else if (args[0].equalsIgnoreCase("clearcondition")) {
                    if (args.length < 2) {
                        sender.sendMessage("Usage: clearcondition <name>");
                        return false;
                    }
                    String name = args[1];
                    sConfig.clearConditions(name);
                    sender.sendMessage("Conditions cleared for task: " + name);
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
            completions.add("condition"); // 3+ args
            completions.add("clearcondition"); // 2 args
            completions.add("reload"); // 0 args

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list") ||
                    args[0].equalsIgnoreCase("add") ||
                    args[0].equalsIgnoreCase("reload") ||
                    args[0].equalsIgnoreCase("help"))
                return null;
            completions = sConfig.getTasks().stream().map(task -> task.getName()).toList();
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("condition")) {
            completions.add("minplayers:");
            completions.add("maxplayers:");
            completions.add("timeofday:day");
            completions.add("timeofday:night");
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
