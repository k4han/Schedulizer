package me.virusker.schedulizer.scheduler;

import org.bukkit.plugin.java.JavaPlugin;

public final class PlatformSchedulerFactory {
    private static final String FOLIA_MARKER = "io.papermc.paper.threadedregions.RegionizedServer";

    private PlatformSchedulerFactory() {
    }

    public static PlatformScheduler create(JavaPlugin plugin) {
        if (isFolia()) {
            return new FoliaPlatformScheduler(plugin);
        }
        return new BukkitPlatformScheduler(plugin);
    }

    public static boolean isFolia() {
        try {
            Class.forName(FOLIA_MARKER);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
