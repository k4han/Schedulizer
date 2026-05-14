package me.virusker.schedulizer.scheduler;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.java.JavaPlugin;

public class FoliaPlatformScheduler implements PlatformScheduler {
    private final JavaPlugin plugin;
    private final GlobalRegionScheduler scheduler;

    public FoliaPlatformScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getGlobalRegionScheduler();
    }

    @Override
    public PlatformTask runRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        ScheduledTask scheduledTask = scheduler.runAtFixedRate(
                plugin,
                ignored -> task.run(),
                initialDelayTicks,
                periodTicks
        );
        return scheduledTask::cancel;
    }

    @Override
    public void executeGlobal(Runnable task) {
        scheduler.execute(plugin, task);
    }

    @Override
    public String getName() {
        return "Folia";
    }
}
