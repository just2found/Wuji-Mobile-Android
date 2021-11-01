package libs.source.common

import android.os.Handler
import android.os.Looper
import libs.source.common.thread.PriorityBlockingQueue
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class AppExecutors(
        private val diskIO: Executor,
        private val networkIO: Executor,
        private val mainThread: Executor
) {
    companion object {
        val instance: AppExecutors by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppExecutors()
        }
    }

    private constructor() : this(
            Executors.newSingleThreadExecutor(),
            ThreadPoolExecutor(3, 3, 6, TimeUnit.MINUTES,  //
                    PriorityBlockingQueue<Runnable>(),  //无限容量的缓冲队列
                    DefaultThreadFactory(),  //线程创建工厂
                    ThreadPoolExecutor.AbortPolicy()) //继续超出上限的策略，阻止
            ,
            MainThreadExecutor()
    )


    fun diskIO(): Executor {
        return diskIO
    }

    fun networkIO(): Executor {
        return networkIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    private class DefaultThreadFactory internal constructor() : ThreadFactory {
        private val group: ThreadGroup
        private val threadNumber = AtomicInteger(1)
        private val namePrefix: String
        override fun newThread(r: Runnable): Thread {
            val t = Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0)
            if (t.isDaemon) t.isDaemon = false
            if (t.priority != Thread.NORM_PRIORITY) t.priority = Thread.NORM_PRIORITY
            return t
        }

        companion object {
            private val poolNumber = AtomicInteger(1)
        }

        init {
            val s = System.getSecurityManager()
            group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-network-io-"
        }
    }
}
