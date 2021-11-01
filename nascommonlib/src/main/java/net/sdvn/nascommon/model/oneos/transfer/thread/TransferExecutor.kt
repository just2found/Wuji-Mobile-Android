package net.sdvn.nascommon.model.oneos.transfer.thread


import java.util.concurrent.*

/**
create by: 86136
create time: 2021/2/18 10:13
Function description: 这个只能执行AffairrRunable这个类型的Runable

 */

class TransferExecutor(corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long, unit: TimeUnit, workQueue: BlockingQueue<Runnable>, threadFactory: ThreadFactory, handler: RejectedExecutionHandler) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler) {
    /**
     * 任务结束后回调
     */
    override fun afterExecute(r: Runnable?, t: Throwable?) {
        synchronized(XExecutor::class.java) {
            super.afterExecute(r, t)
            //当前正在运行的数量为1 表示当前正在停止的任务，同时队列中没有任务，表示所有任务下载完毕
            if (activeCount <= 1 && queue.size == 0) {
                if (allTaskEndListenerList.size > 0) {
                    for (listener in allTaskEndListenerList) {
                        listener.onAllTaskEnd()
                    }
                }
            }
        }
    }


    private val taskEndListenerList: MutableList<OnTaskEndListener> by lazy {
        mutableListOf<OnTaskEndListener>()
    }

    fun addOnTaskEndListener(taskEndListener: OnTaskEndListener) {
        taskEndListenerList.add(taskEndListener)
    }

    fun removeOnTaskEndListener(taskEndListener: OnTaskEndListener?) {
        taskEndListenerList.remove(taskEndListener)
    }

    interface OnTaskEndListener {
        fun onTaskEnd(r: Runnable?)
    }

    private val allTaskEndListenerList: MutableList<OnAllTaskEndListener> by lazy {
        mutableListOf<OnAllTaskEndListener>()
    }

    fun addOnAllTaskEndListener(allTaskEndListener: OnAllTaskEndListener) {
        allTaskEndListenerList.add(allTaskEndListener)
    }

    fun removeOnAllTaskEndListener(allTaskEndListener: OnAllTaskEndListener?) {
        allTaskEndListenerList.remove(allTaskEndListener)
    }

    interface OnAllTaskEndListener {
        fun onAllTaskEnd()
    }
}