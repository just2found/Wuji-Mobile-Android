package libs.source.common.thread;

public interface WorkQueueExecutor {
    void execute(Runnable task);

    void remove(Runnable task);
}
