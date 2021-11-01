package org.view.libwidget.handler

/**
create by: 86136
create time: 2021/3/18 14:01
Function description:
runnable 需要执行的单元 执行完成一次后可以置为空
DelayTime 延迟执行 不立刻执行，如果在等待时间里面有新的任务收到则立刻执行
maxIntervalTime  当上次间隔时间大于最大间隔数的时候立即执行不进行等待
 */
class DelayedUnit(var runnable: Runnable? = null, var lastTime: Long = 0L, var delayTime: Long = 500, var maxIntervalTime: Long = 1000) {

}