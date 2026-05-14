package me.virusker.schedulizer.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitPlatformScheduler implements PlatformScheduler {
    private final JavaPlugin plugin;

    public BukkitPlatformScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PlatformTask runRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        BukkitTask bukkitTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, initialDelayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    @Override
    public void executeGlobal(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public String getName() {
        return "Bukkit";
    }
}
