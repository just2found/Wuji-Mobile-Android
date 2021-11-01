package libs.source.common.thread;

import androidx.annotation.Nullable;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 * Created by yun on 2018/1/10.
 */

public class TransferThreadPool implements WorkQueueExecutor {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;          //最大线程池的数量
    private static final int KEEP_ALIVE_TIME = 6;        //存活的时间
    private static final TimeUnit UNIT = TimeUnit.MINUTES; //时间单位
    private int corePoolSize;                        //核心线程池的数量，同时能执行的线程数量，默认3个
    private XExecutor executor;               //线程池执行器
    private int poolSize;
    private boolean isPurge;
    private PriorityBlockingQueue<Runnable> mWorkQueue;
    private static final int DEFAULT_CORE_POOL_SIZE = (CPU_COUNT + 1) / 2 + 1;

    public XExecutor getExecutor() {
        if (executor == null) {
            synchronized (TransferThreadPool.class) {
                if (executor == null) {
//                    int availableProcessors = Runtime.getRuntime().availableProcessors()
                    Timber.e(" cup count : %s", CPU_COUNT);
                    mWorkQueue = new PriorityBlockingQueue<>();
                    executor = new XExecutor(corePoolSize, poolSize, KEEP_ALIVE_TIME, UNIT, //
                            mWorkQueue,   //无限容量的缓冲队列
                            new DefaultThreadFactory(),        //线程创建工厂
                            new ThreadPoolExecutor.AbortPolicy());   //继续超出上限的策略，阻止
                }
            }
        }
        return executor;
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-io-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public int getWorkQueueSize() {
        return mWorkQueue.size();
    }

    public TransferThreadPool() {
        corePoolSize = DEFAULT_CORE_POOL_SIZE;
        poolSize = MAX_POOL_SIZE;
    }

    public TransferThreadPool(int corePoolSize, int maxPoolSize) {
        setCorePoolSize(corePoolSize, maxPoolSize);
    }

    /**
     * 必须在首次执行前设置，否者无效 ,范围1-5之间
     */
    public void setCorePoolSize(int corePoolSize, int maxPoolSize) {
        if (corePoolSize <= 0) corePoolSize = 1;
        if (corePoolSize > DEFAULT_CORE_POOL_SIZE) corePoolSize = DEFAULT_CORE_POOL_SIZE;
        if (maxPoolSize > 0 && maxPoolSize <= MAX_POOL_SIZE) poolSize = maxPoolSize;
        this.corePoolSize = corePoolSize;
    }

    /**
     * 执行任务
     */
    @Override
    public void execute(@Nullable Runnable runnable) {
        if (runnable != null) {
            getExecutor().execute(runnable);
        }
    }

    /**
     * 移除线程
     */
    @Override
    public void remove(@Nullable Runnable runnable) {
        if (runnable != null && executor != null && !isPurge) {
            executor.remove(runnable);
        }
    }

    public void removeAll() {
        if (executor != null && !isPurge) {
            isPurge = true;
            executor.getQueue().clear();
            isPurge = false;
        }
    }
}
