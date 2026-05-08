package me.virusker.schedulizer;

import me.virusker.schedulizer.commands.ScheduleCommand;
import me.virusker.schedulizer.config.PluginConfig;
import me.virusker.schedulizer.metrics.Metrics;
import me.virusker.schedulizer.timmer.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public final class Schedulizer extends JavaPlugin {

    private BukkitRunnable schedulerTask;
    private PluginConfig pluginConfig;
    private Metrics metrics;

    @Override
    public void onEnable() {

        int pluginId = 24886;
        this.metrics = new Metrics(this, pluginId);

        // Plugin startup logic
        saveDefaultConfig();

        pluginConfig = new PluginConfig(this);

        // register the command
        getCommand("Schedulizer").setExecutor(new ScheduleCommand(pluginConfig));

        schedulerTask = new BukkitRunnable(pluginConfig);
        schedulerTask.runTaskTimer(this, 100, pluginConfig.getTick());


    }

    @Override
    public void onDisable() {
        // Cancel scheduled task to prevent further execution
        if (schedulerTask != null) {
            schedulerTask.cancel();
            getLogger().info("Scheduler task cancelled.");
        }

        // Save configuration on shutdown
        if (pluginConfig != null) {
            pluginConfig.saveConfig();
        }
    }
}
