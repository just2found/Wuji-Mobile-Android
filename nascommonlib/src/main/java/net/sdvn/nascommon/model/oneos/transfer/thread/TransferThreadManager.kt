package net.sdvn.nascommon.model.oneos.transfer.thread

import net.sdvn.nascommon.model.oneos.transfer.inter.AffairRunnable
import timber.log.Timber
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
create by: 86136
create time: 2021/2/18 10:09
Function description:
 */

class TransferThreadManager(val corePoolSize: Int = DEFAULT_CORE_POOL_SIZE, val poolMaxSize: Int = MAX_POOL_SIZE) {

    companion object {
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val MAX_POOL_SIZE = CPU_COUNT  //最大线程池的数量
        private val DEFAULT_CORE_POOL_SIZE = (CPU_COUNT) / 3 + 1
    }

    private val TAG = TransferThreadManager::class.java.simpleName
    private val KEEP_ALIVE_TIME = 6 //存活的时间
    private val UNIT = TimeUnit.MINUTES //时间单位

    private val mAffairrRunableList = ArrayList<AffairRunnable>()//这个是正在运行中的

    //传输任务（上传下载传输过程）的执行类
    private val transferExecutor by lazy {
        Timber.e(" cup count : %s", CPU_COUNT)
        val it = TransferExecutor(corePoolSize!!, poolMaxSize, KEEP_ALIVE_TIME.toLong(), UNIT,  //
                PriorityBlockingQueue(),  //无限容量的缓冲队列
                DefaultThreadFactory(),  //线程创建工厂
                ThreadPoolExecutor.AbortPolicy()) //继续超出上限的策略，阻止
        it
    }

    //操作过程的线程，这个单线程操作，防止操作之间冲突
    private val operationExecutor = Executors.newSingleThreadExecutor()

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
        return corePoolSize - mAffairrRunableList.size
    }

    fun performOperation(operation: () -> Unit) {
        operationExecutor.execute { operation.invoke() }
    }

    fun performOperation(r: Runnable) {
        operationExecutor.execute(r)
    }

    fun executeAffairrRunable(task: AffairRunnable) {
        mAffairrRunableList.add(task)
        transferExecutor.execute(task)
    }

    fun execute(task: Runnable) {
        transferExecutor.execute(task)
    }

    fun remove(task: Runnable) {
        transferExecutor.queue.remove(task)
    }

    fun isExist(tag: String): Boolean {
        for (affairrRunable in mAffairrRunableList) {
            if (tag == affairrRunable.getMark()) {
                return true
            }
        }
        return false
    }

    fun interrupt(tag: String) {
        val iterator: MutableIterator<AffairRunnable> = mAffairrRunableList.iterator()
        while (iterator.hasNext()) {
            val runnable = iterator.next()
            if (tag == runnable.getMark()) {
                runnable.interrupt()//打断后queue会自然释放 所以queue不需要额外处理
                iterator.remove()
            }
        }
    }

    fun onFinishTask(tag: String) {
        val iterator: MutableIterator<AffairRunnable> = mAffairrRunableList.iterator()
        while (iterator.hasNext()) {
            val runnable = iterator.next()
            if (tag == runnable.getMark()) {
                iterator.remove()
            }
        }
    }

    fun interruptAll() {
        transferExecutor.queue.clear()
        for (r in mAffairrRunableList.reversed()) {
            r.interrupt()
        }
        mAffairrRunableList.clear()
    }

    fun addOnTaskEndListener(taskEndListener: TransferExecutor.OnTaskEndListener) {
        transferExecutor.addOnTaskEndListener(taskEndListener)
    }

    fun removeOnTaskEndListener(taskEndListener: TransferExecutor.OnTaskEndListener?) {
        transferExecutor.removeOnTaskEndListener(taskEndListener)
    }
}