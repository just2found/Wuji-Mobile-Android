package net.sdvn.common.repo

import android.util.Log
import io.objectbox.TxCallback
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.query
import io.objectbox.query.LazyList
import net.sdvn.common.IntrDBHelper
import net.sdvn.common.vo.BriefModel
import net.sdvn.common.vo.BriefModel_

/**
 * @author Raleigh.Luo
 * date：21/5/6 13
 * describe：简介
 */
object BriefRepo {
    const val FOR_DEVICE = "device"

    //    const val FOR_CIRCLE = "circle"
    const val FOR_CIRCLE = "cycle"
    const val BACKGROUD_TYPE = 4
    const val PORTRAIT_TYPE = 2
    const val BRIEF_TYPE = 1
    const val PORTRAIT_AND_BRIEF_TYPE = 3//头像＋简介
    const val BACKGROUD_AND_BRIEF_TYPE = 5//背景+简介
    const val PORTRAIT_AND_BACKGROUD_TYPE = 6//背景+头像
    const val ALL_TYPE = 7

    private fun getBox() = IntrDBHelper.getBoxStore().boxFor(BriefModel::class.java)

    @JvmStatic
    fun insertAsync(brief: BriefModel, callback: TxCallback<Void>? = null) {
        IntrDBHelper.getBoxStore().runInTxAsync(Runnable {
            //去重
            getBox().query {
                equal(BriefModel_.deviceId, brief.deviceId)
                equal(BriefModel_.For, brief.For)
            }.remove()
            getBox().put(brief)
        }, callback)
    }

    /**
     * 异步添加到数据库
     */
    @JvmStatic
    fun insertAsync(deviceId: String, For: String, type: Int, data: String?, timeStamp: Long?, callback: TxCallback<Void>) {
        IntrDBHelper.getBoxStore().runInTxAsync(Runnable {
            val breif = getBox().query {
                equal(BriefModel_.deviceId, deviceId)
                equal(BriefModel_.For, For)
            }.findFirst() ?: BriefModel(For = For)
            when (type) {
                BACKGROUD_TYPE -> {
                    breif.backgroudPath = data
                    breif.backgroudTimeStamp = timeStamp
                }
                PORTRAIT_TYPE -> {
                    breif.portraitPath = data
                    breif.portraitTimeStamp = timeStamp
                }
                BRIEF_TYPE -> {
                    breif.brief = data
                    breif.briefTimeStamp = timeStamp
                }
            }
            //去重
            getBox().query {
                equal(BriefModel_.deviceId, deviceId)
                equal(BriefModel_.For, For)
            }.remove()
            getBox().put(breif)
        }, callback)
    }

    @JvmStatic
    fun getBriefs(deviceIds: Array<String>, For: String): List<BriefModel> {
        return getBox().query {
            `in`(BriefModel_.deviceId, deviceIds)
            equal(BriefModel_.For, For)
        }.find()
    }


    @JvmStatic
    fun getBriefsLiveData(deviceIds: Array<String>, For: String): ObjectBoxLiveData<BriefModel> {
        return ObjectBoxLiveData(getBox().query {
            `in`(BriefModel_.deviceId, deviceIds)
            equal(BriefModel_.For, For)
        })
    }

    @JvmStatic
    fun getBriefLiveData(deviceId: String, For: String): ObjectBoxLiveData<BriefModel> {
        return ObjectBoxLiveData(getBox().query {
            equal(BriefModel_.deviceId, deviceId)
            equal(BriefModel_.For, For)
        })
    }

    /**
     * @param For FOR_DEVICE or FOR_CIRCLE
     */
    @JvmStatic
    fun getBrief(deviceId: String, For: String): BriefModel? {
        val query = getBox().query {
            equal(BriefModel_.deviceId, deviceId)
            equal(BriefModel_.For, For)
        }
        val brief = query.findFirst()
        query.close()
        return brief
    }

    /**删除本地简介对象
     * @param For FOR_DEVICE or FOR_CIRCLE
     */
    @JvmStatic
    fun removeBrief(deviceId: String, For: String) {
        IntrDBHelper.getBoxStore().runInTxAsync(Runnable {
            getBox().query {
                equal(BriefModel_.deviceId, deviceId)
                equal(BriefModel_.For, For)
            }.remove()
        }, null)
    }
}