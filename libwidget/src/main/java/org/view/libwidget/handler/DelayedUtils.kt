package org.view.libwidget.handler

import android.os.Handler
import android.os.Looper

/**
create by: 86136
create time: 2021/3/18 13:55
Function description:
 */

object DelayedUtils {
    const val NOTIFY_TRANSFER_COUNT_DOWNLOAD = 1101
    const val NOTIFY_TRANSFER_COUNT_UPLOAD = 1102
    const val WAKE_UP_TRANSFER_TASK_DOWNLOAD = 1103
    const val WAKE_UP_TRANSFER_TASK_UPLOAD = 1104

    const val DELAY_SAVE_TRANSFER_DOWNLOAD = 1105
    const val DELAY_SAVE_TRANSFER_UPLOAD = 1106

    const val REFRESH_PROCESS_TITLE = 1201


    const val NOTIFY_SAFE_BOX_COUNT_DOWNLOAD = 1301
    const val NOTIFY_SAFE_BOX_COUNT_UPLOAD = 1302
    const val WAKE_UP_SAFE_BOX_DOWNLOAD = 1303
    const val WAKE_UP_SAFE_BOX_UPLOAD = 1304
    const val DELAY_SAVE_SAFE_BOX_DOWNLOAD = 305
    const val DELAY_SAVE_SAFE_BOX_UPLOAD = 1306


    const val DELAY_USER_MANAGE_LIST = 2101
    const val DELAY_DEVICE_MANAGE_LIST = 2102

    private val mHashMap by lazy {
        mutableMapOf<Int, DelayedUnit>()
    }


    private val mHandler by lazy {
        Handler(Looper.getMainLooper()) {
            dealDelayedUnit(it.what)
        }
    }

    private fun dealDelayedUnit(key: Int): Boolean {
        val oldUnit = mHashMap[key]
        if (oldUnit != null) {
            oldUnit.runnable?.run()
            oldUnit.runnable = null
            oldUnit.lastTime = System.currentTimeMillis()
            return true
        }
        return false
    }


    fun addDelayedUnit(msgKey: Int, delayedUnit: DelayedUnit) {
        val oldUnit = mHashMap[msgKey]
        mHandler.removeMessages(msgKey)
        when {
            oldUnit == null -> {
                mHashMap[msgKey] = delayedUnit
                mHandler.sendEmptyMessage(msgKey)
            }
            System.currentTimeMillis() - oldUnit.lastTime >= oldUnit.maxIntervalTime -> {
                oldUnit.runnable = delayedUnit.runnable
                mHandler.sendEmptyMessage(msgKey)
            }
            else -> {
                oldUnit.runnable = delayedUnit.runnable
                mHandler.sendEmptyMessageDelayed(msgKey, delayedUnit.delayTime)
            }
        }


    }
}