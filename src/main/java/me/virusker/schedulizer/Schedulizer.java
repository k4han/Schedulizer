package me.virusker.schedulizer;

import me.virusker.schedulizer.commands.ScheduleCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.metrics.Metrics;
import me.virusker.schedulizer.scheduler.PlatformScheduler;
import me.virusker.schedulizer.scheduler.PlatformSchedulerFactory;
import me.virusker.schedulizer.scheduler.PlatformTask;
import me.virusker.schedulizer.timmer.ScheduleTicker;
import org.bukkit.plugin.java.JavaPlugin;

public final class Schedulizer extends JavaPlugin {

    private PlatformTask schedulerTask;
    private PlatformScheduler platformScheduler;
    private PluginConfig pluginConfig;
    private Metrics metrics;

    @Override
    public void onEnable() {

        int pluginId = 24886;
        this.metrics = new Metrics(this, pluginId);

        // Plugin startup logic
        saveDefaultConfig();

        platformScheduler = PlatformSchedulerFactory.create(this);
        getLogger().info("Using " + platformScheduler.getName() + " scheduler.");

        pluginConfig = new PluginConfig(this, platformScheduler);

        // register the command
        getCommand("schedulizer").setExecutor(new ScheduleCommand(pluginConfig));

        schedulerTask = platformScheduler.runRepeating(new ScheduleTicker(pluginConfig), 100, pluginConfig.getTick());


    }

    @Override
    public void onDisable() {
        // Cancel scheduled task to prevent further execution
        if (schedulerTask != null) {
            schedulerTask.cancel();
            getLogger().info("Scheduler task cancelled.");
        }

        if (metrics != null) {
            metrics.shutdown();
        }

        // Save configuration on shutdown
        if (pluginConfig != null) {
            pluginConfig.saveConfig();
        }
    }
}
