package net.sdvn.nascommon.model.oneos.transfer_r.thread

import libs.source.common.thread.PriorityBlockingQueue
import net.sdvn.nascommon.model.oneos.transfer.TransferElement
import timber.log.Timber
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
create by: 86136
create time: 2021/2/18 10:09
Function description:
 */

class TransferThreadExecutor<T : TransferElement>(val corePoolSize: Int = DEFAULT_CORE_POOL_SIZE, val poolMaxSize: Int = MAX_POOL_SIZE) {

    private val TAG = TransferThreadExecutor::class.java.simpleName

    companion object {
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private const val MAX_POOL_SIZE = 3  //最大线程池的数量
        private const val DEFAULT_CORE_POOL_SIZE = 3
        val diskIOExecutor: Executor by lazy {
            Executors.newSingleThreadExecutor()
        }
    }

    private val KEEP_ALIVE_TIME = 6 //存活的时间
    private val UNIT = TimeUnit.MINUTES //时间单位

    private val mTaskRunnableList = ArrayList<TaskRunnable<T>>()//这个是正在运行中的

    //传输任务（上传下载传输过程）的执行类
    private val transferExecutor by lazy {
        Timber.e(" cup count : %s", CPU_COUNT)
        val it = ThreadPoolExecutor(corePoolSize, poolMaxSize, KEEP_ALIVE_TIME.toLong(), UNIT,
                PriorityBlockingQueue(),  //无限容量的缓冲队列
                DefaultThreadFactory(),  //线程创建工厂
                ThreadPoolExecutor.AbortPolicy()) //继续超出上限的策略，阻止
        it
    }


    //内部线程工厂类
    private class DefaultThreadFactory constructor() : ThreadFactory {
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
                    "-io-"
        }
    }

    fun getTaskCount(): Int {
        return mTaskRunnableList.size
    }

    fun runInIO(operation: () -> Unit) {
        diskIOExecutor.execute { operation.invoke() }
    }

    fun execute(task: TaskRunnable<T>) {
        mTaskRunnableList.add(task)
        transferExecutor.execute(task)
    }


    fun remove(task: TaskRunnable<T>) {
        transferExecutor.queue.remove(task)
    }

    fun isExist(tag: String): Boolean {
        for (taskRunnable in mTaskRunnableList) {
            if (tag == taskRunnable.getTag()) {
                return true
            }
        }
        return false
    }

    fun interrupt(tag: String) {
        val iterator: MutableIterator<TaskRunnable<T>> = mTaskRunnableList.iterator()
        while (iterator.hasNext()) {
            val runnable = iterator.next()
            if (tag == runnable.getTag()) {
                runnable.interruptRunnable()//打断后queue会自然释放 所以queue不需要额外处理
                iterator.remove()
            }
        }
    }

    fun onlyRemove(tag: String) {
        val iterator: MutableIterator<TaskRunnable<T>> = mTaskRunnableList.iterator()
        while (iterator.hasNext()) {
            val runnable = iterator.next()
            if (tag == runnable.getTag()) {
                iterator.remove()
            }
        }
    }


    fun interrupt(tagList: List<String>) {
        for (tag in tagList) {
            val iterator: MutableIterator<TaskRunnable<T>> = mTaskRunnableList.iterator()
            while (iterator.hasNext()) {
                val runnable = iterator.next()
                if (tag == runnable.getTag()) {
                    runnable.interruptRunnable()//打断后queue会自然释放 所以queue不需要额外处理
                    iterator.remove()
                }
            }
        }
    }


    fun interruptDeviceId(deviceId: String) {
        val iterator: MutableIterator<TaskRunnable<T>> = mTaskRunnableList.iterator()
        while (iterator.hasNext()) {
            val runnable = iterator.next()
            if (runnable.getDeviceId() == deviceId) {
                runnable.interruptRunnable()//打断后queue会自然释放 所以queue不需要额外处理
                iterator.remove()
            }
        }
    }

    fun interruptAll() {
        for (taskRunnable in mTaskRunnableList) {
            taskRunnable.interruptRunnable()
        }
        mTaskRunnableList.clear()
    }


}