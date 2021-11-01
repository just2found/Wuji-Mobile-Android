package net.sdvn.nascommon.model.oneos.transfer.thread;

public interface WorkQueueExecutor {
    void execute(Runnable task);

    void remove(Runnable task);
}
