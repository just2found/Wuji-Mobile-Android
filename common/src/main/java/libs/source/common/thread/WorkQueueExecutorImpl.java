package libs.source.common.thread;

import androidx.annotation.NonNull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkQueueExecutorImpl implements WorkQueueExecutor {
    private ThreadPoolExecutor mExecutorService;
    private static final int KEEP_ALIVE_TIME = 6;        //存活的时间
    private static final TimeUnit UNIT = TimeUnit.MINUTES; //时间单位
    private boolean isPurge;

    public WorkQueueExecutorImpl(int nThreads) {
        mExecutorService = new ThreadPoolExecutor(nThreads, nThreads,
                KEEP_ALIVE_TIME, UNIT,
                new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void execute(@NonNull Runnable task) {
        if (mExecutorService != null)
            mExecutorService.execute(task);
    }

    @Override
    public void remove(Runnable task) {
        if (mExecutorService != null) {
            mExecutorService.remove(task);
        }
    }

    public void release() {
        if (mExecutorService != null && !isPurge) {
            isPurge = true;
            mExecutorService.purge();
        }
    }
}