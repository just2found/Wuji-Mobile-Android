package net.linkmate.app.ui.simplestyle.dynamic.delegate.db

import android.text.TextUtils
import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import io.objectbox.TxCallback
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.query
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.sdvn.common.DynamicDBHelper
import net.sdvn.common.vo.*

/**
 * @author Raleigh.Luo
 * date：21/1/19 13
 * describe：发布动态
 */
class DynamicDelegeteImpl : DBDelegete<Dynamic>() {
    /**
     * 发布动态后 更新
     * @param networkId 需传入networkId，避免数据产生混乱
     */
    override fun update(autoIncreaseId: Long, data: Dynamic?) {
        data?.let {
            getBoxStore()?.let {
                it.runInTx(Runnable {
                    val box = it.boxFor(Dynamic::class.java)
                    val olderData = box.get(autoIncreaseId)
                    if (olderData.MediasPO.size > 0 && !TextUtils.isEmpty(olderData.MediasPO?.get(0).localPath)) {
                        //有本地资源时（自己本地发布的），本地路径保留，增加显示速度
                        data.Medias?.forEach {
                            val index = it.index
                            it.localPath = olderData.MediasPO.find { it.index == index }?.localPath
                        }
                    }
                    //删除相关子表数据
                    removeRelationSubTable(olderData)
                    data.autoIncreaseId = autoIncreaseId
                    data.transfer(olderData.networkId ?: "", olderData.deviceId ?: "", it)
                    box.put(data)
                })
            }
        }
    }

    /**
     * 发布动态  同步  且为本地数据，非服务器同步数据
     * @param callback 返回动态数据的autoIncreaseId
     */
    override fun updateLocal(data: Dynamic, dynamicAutoIncreaseId: Long, callback: Function<Long, Void>) {
        var dynamicAutoIncreaseId: Long = -1
        getBoxStore()?.let {
            it.runInTxAsync(Runnable {
                data.ID = -1
                data.CreateAt = System.currentTimeMillis() / 1000
                it.boxFor(Dynamic::class.java)?.put(data)
                //put后，autoIncreaseId会自动更新
                dynamicAutoIncreaseId = data.autoIncreaseId
            }, TxCallback { result, error ->
                callback.apply(dynamicAutoIncreaseId)
            })
        }
    }

    override fun deleteLocal(autoIncreaseId: Long, callback: Function<Long, Void>) {
        if (DynamicQueue.containsAndRemove(DynamicQueue.PUBLISH_DYNAMIC_TYPE, autoIncreaseId)) {
            //未发布就删除
            getBoxStore()?.let {
                it.runInTxAsync(Runnable {
                    it.boxFor(Dynamic::class.java).remove(autoIncreaseId)
                }, TxCallback { result, error ->
                })
            }
        } else {
            getBoxStore()?.let {
                it.runInTxAsync(Runnable {
                    val dynamic = it.boxFor(Dynamic::class.java)?.get(autoIncreaseId)
                    dynamic?.isDeleted = true
                    it.boxFor(Dynamic::class.java)?.put(dynamic)
                    //put后，autoIncreaseId会自动更新
                }, TxCallback { result, error ->
                    callback.apply(autoIncreaseId)
                })
            }
        }

    }

    override fun delete(autoIncreaseId: Long) {
        getBoxStore()?.boxFor(Dynamic::class.java)?.remove(autoIncreaseId)
    }

    override fun deleteById(networkId: String, deviceId: String, id: Long) {
        getBoxStore()?.let {
            it.runInTx {
                it.boxFor(Dynamic::class.java).query {
                    equal(Dynamic_.networkId, networkId)
                    equal(Dynamic_.deviceId, deviceId)
                    equal(Dynamic_.ID, id)
                }.remove()
            }
        }
    }

    /**
     * 清除已解除关系的子表数据
     *
     * 适用于：修改
     */
    private fun removeRelationSubTable(data: Dynamic) {
        getBoxStore()?.boxFor(DynamicLike::class.java)?.remove(data.LikesPO)
        getBoxStore()?.boxFor(DynamicComment::class.java)?.remove(data.CommentsPO)
        getBoxStore()?.boxFor(DynamicAttachment::class.java)?.remove(data.AttachmentsPO)
        getBoxStore()?.boxFor(DynamicMedia::class.java)?.remove(data.MediasPO)
    }

    override fun check(autoIncreaseId: Long, operateType: String, callback: Function<Dynamic, Void>) {
        val userId = DynamicQueue.mLastUserId
        val networkId = DynamicQueue.mLastNetworkId
        DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()?.let {
            val dynamic = it.boxFor(Dynamic::class.java)?.get(autoIncreaseId)
            if (dynamic != null) {
                if (DynamicQueue.isCurrentDynamicTask(dynamic.networkId, dynamic.deviceId, userId)) {
                    if (operateType == DynamicQueue.DELETE_DYNAMIC_TYPE && dynamic.ID == -1L) {//是删除动态操作，且为本地数据（未发布成功），直接删除
                        delete(autoIncreaseId)
                    } else {
                        callback.apply(dynamic)
                    }
                } else {
                    delete(autoIncreaseId)
                }
            } else {
                delete(autoIncreaseId)
            }
        }
    }


    /**
     * 添加数据 同步
     * @param limitMaxTime 最新时间，查找最新时间前的指定数量数据
     */
    fun insert(mNetworkId: String, deviceId: String, datas: List<Dynamic>?, limitMaxTime: Long) {
        getBoxStore()?.let {
            val boxStore = it
            datas?.forEach {
                it.transfer(mNetworkId, deviceId, boxStore)
            }
            it.runInTx({//同步事务
                val box = it.boxFor(Dynamic::class.java)
                //过滤条件 <=lastTime >=datas.lastIndex createTime
                if (datas != null && datas.size > 0) {
                    val limitMinTime = Math.min(datas.get(0).CreateAt
                            ?: 0, datas.get(datas.size - 1).CreateAt ?: 0)
                    //过滤不需要被删除的数据id
                    val filterRemoveIds = arrayListOf<Long>()
                    datas.forEach {
                        val newDynamic = it
                        //有旧数据 自增id,优先使用旧数据的自增id
                        val olderDynamic = box.query {
                            equal(Dynamic_.networkId, mNetworkId)
                            equal(Dynamic_.deviceId, deviceId)
                            equal(Dynamic_.ID, it.ID ?: 0L)
                        }.findFirst()
                        olderDynamic?.let {
                            filterRemoveIds.add(it.autoIncreaseId)
                            newDynamic.autoIncreaseId = it.autoIncreaseId
                            newDynamic.isDeleted = it.isDeleted
                            removeRelationSubTable(it)
                        }
                    }


                    //删除 中间被删除的数据
                    box.query {
                        equal(Dynamic_.networkId, mNetworkId)
                        equal(Dynamic_.deviceId, deviceId)
                        notIn(Dynamic_.ID, filterRemoveIds.toLongArray())
                        notEqual(Dynamic_.ID, -1)//不删除本地未发布成功数据
                        between(Dynamic_.CreateAt, limitMinTime, limitMaxTime)
                    }.remove()

                    box.put(datas)
                } else {
                    //过滤条件 <=lastTime
                    box.query {
                        equal(Dynamic_.networkId, mNetworkId)
                        equal(Dynamic_.deviceId, deviceId)
                        notEqual(Dynamic_.ID, -1)//不删除本地未发布成功数据
                        less(Dynamic_.CreateAt, limitMaxTime).or().equal(Dynamic_.CreateAt, limitMaxTime)
                    }.remove()
                }
                //清除已解除关系的子表数据
                removeNullSubTable()
            })
        }
    }

    /**
     * 同步添加数据
     * @param networkId 需传入networkId，避免数据产生混乱
     */
    fun insert(networkId: String, deviceId: String, data: Dynamic?) {
        data?.let {
            getBoxStore()?.let {
                it.runInTx(Runnable {
                    val box = it.boxFor(Dynamic::class.java)
                    val olderData = box.query {
                        equal(Dynamic_.ID, data.ID ?: 0)
                        equal(Dynamic_.networkId, networkId)
                        equal(Dynamic_.deviceId, deviceId)
                    }.findFirst()
                    olderData?.let {
                        data.autoIncreaseId = olderData.autoIncreaseId
                        data.isDeleted = olderData.isDeleted
                        //解除原来的子表数据关系
                        removeRelationSubTable(olderData)
                    }
                    data.transfer(networkId, deviceId, it)
                    box.put(data)
                    //清除已解除关系的子表数据
                    removeNullSubTable()
                })
            }
        }
    }


    /**
     * 更新指定动态数据 同步
     */
    fun insertOrUpdate(mNetworkId: String, deviceId: String, datas: List<Dynamic>?) {
        datas?.let {
            getBoxStore()?.let {
                val boxStore = it
                datas.forEach {
                    it.transfer(mNetworkId, deviceId, boxStore)
                }
                it.runInTx({//同步事务
                    val box = it.boxFor(Dynamic::class.java)
                    //过滤条件 <=lastTime >=datas.lastIndex createTime
                    if (datas != null && datas.size > 0) {
                        datas.forEach {
                            val data = it
                            val olderData = box.query {
                                equal(Dynamic_.ID, data.ID ?: 0)
                                equal(Dynamic_.networkId, mNetworkId)
                                equal(Dynamic_.deviceId, deviceId)
                            }.findFirst()
                            olderData?.let {
                                data.autoIncreaseId = olderData.autoIncreaseId
                                data.isDeleted = olderData.isDeleted
                                //解除原来的子表数据关系
                                removeRelationSubTable(olderData)
                            }
                            data.transfer(mNetworkId, deviceId, getBoxStore()!!)
                        }
                        box.put(datas)
                    }
                    //清除已解除关系的子表数据
                    removeNullSubTable()
                })
            }
        }
    }


    /**
     * 清除已解除关系的子表数据
     * dynamicId 为0或是空的
     *
     * 适用于：删除记录后 更新，修改不适用
     */
    private fun removeNullSubTable() {
        getBoxStore()?.boxFor(DynamicLike::class.java)?.query {
            isNull(DynamicLike_.dynamicId).or().equal(DynamicLike_.dynamicId, 0)
        }?.remove()
        getBoxStore()?.boxFor(DynamicComment::class.java)?.query {
            isNull(DynamicComment_.dynamicId).or().equal(DynamicComment_.dynamicId, 0)
        }?.remove()
        getBoxStore()?.boxFor(DynamicAttachment::class.java)?.query {
            isNull(DynamicAttachment_.dynamicId).or().equal(DynamicAttachment_.dynamicId, 0)
        }?.remove()
        getBoxStore()?.boxFor(DynamicMedia::class.java)?.query {
            isNull(DynamicMedia_.dynamicId).or().equal(DynamicMedia_.dynamicId, 0)
        }?.remove()
    }


    /**
     * 查询所有动态 按时间倒叙
     * @param totalPageSize 总页数
     */
    fun querysLiveData(networkId: String, deviceId: String, totalPageSize: Long): LiveData<List<Dynamic>>? {
        if (TextUtils.isEmpty(networkId) || TextUtils.isEmpty(deviceId)) return null
        val limitMaxTime = 1 + System.currentTimeMillis() / 1000 //最大边界值 时间戳秒数，当前时间
        var result: LiveData<List<Dynamic>>? = null
        getBoxStore()?.let {
            var autoIncreaseIds: LongArray? = null
            //先分页查找出所有id
            autoIncreaseIds = it.boxFor(Dynamic::class.java).query {
                equal(Dynamic_.networkId, networkId)
                equal(Dynamic_.deviceId, deviceId)
                equal(Dynamic_.isDeleted, false)
                //设备时间不同步问题，不加此条件
//                less(Dynamic_.CreateAt, limitMaxTime).or().equal(Dynamic_.CreateAt, limitMaxTime)
                orderDesc(Dynamic_.CreateAt)
            }.findIds(0, totalPageSize)
            if (autoIncreaseIds == null) {
                autoIncreaseIds = LongArray(0)
            }
            //再查询livedata
            val query = it.boxFor(Dynamic::class.java).query {
                `in`(Dynamic_.autoIncreaseId, autoIncreaseIds)
                equal(Dynamic_.isDeleted, false)
                orderDesc(Dynamic_.CreateAt)
            }
            result = ObjectBoxLiveData(query)
//            }
        }
        return result
    }


    /**
     * 同步 查询单条动态
     */
    fun query(networkId: String, deviceId: String, dynamicId: Long): Dynamic? {
        return getBoxStore()?.boxFor(Dynamic::class.java)?.query {
            equal(Dynamic_.networkId, networkId)
            equal(Dynamic_.deviceId, deviceId)
            equal(Dynamic_.ID, dynamicId)
        }?.findFirst()
    }

    /**
     * 清空本地动态数据－未发布成功回调
     */
    fun clearLocal() {
        getBoxStore()?.let {
            it.runInTxAsync(Runnable {
                it.boxFor(Dynamic::class.java)?.query {
                    equal(Dynamic_.ID, -1)
                }?.remove()
            }, null)
        }
    }


    fun getDynamic(networkId: String, deviceId: String, dynamicId: Long): LiveData<List<Dynamic>> {
        val query = getBoxStore()?.boxFor(Dynamic::class.java)?.query {
            equal(Dynamic_.ID, dynamicId)
            equal(Dynamic_.networkId, networkId)
            equal(Dynamic_.deviceId, deviceId)
        }
        return ObjectBoxLiveData(query)
    }


}