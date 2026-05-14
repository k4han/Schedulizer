package me.virusker.schedulizer.scheduler;

public interface PlatformScheduler {
    PlatformTask runRepeating(Runnable task, long initialDelayTicks, long periodTicks);

    void executeGlobal(Runnable task);

    String getName();
}
