package net.linkmate.app.repository

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import net.linkmate.app.base.NetworkBoundResource
import net.linkmate.app.data.model.Base
import net.linkmate.app.data.model.dynamic.*
import net.linkmate.app.data.remote.DynamicRemoteDataSource
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.delegate.db.DBDelegete
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.vo.Dynamic
import net.sdvn.common.vo.DynamicComment
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import android.os.Handler
import io.reactivex.schedulers.Schedulers
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.net.RetrofitSingleton
import net.linkmate.app.service.UploadStatus
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.vo.DynamicLike

/**动态仓库
 * @author Raleigh.Luo
 * date：20/12/24 16
 * describe：
 */
class DynamicRepository {
    private val remoteDataSource: DynamicRemoteDataSource

    init {
        remoteDataSource = DynamicRemoteDataSource()
    }

    /**
     * 登录
     */
    fun login(ip: String): LiveData<Resource<Login>> {
        return object : NetworkBoundResource<Login>(AppExecutors.instance) {
            override fun createCall() = remoteDataSource.login(ip)
            override fun saveCallResult(item: Login) {
                super.saveCallResult(item)
                if (item.code == DynamicQueue.SUCCESS_CODE) //请求成功
                {
                    item.token?.let {
                        RetrofitSingleton.instance.putDynamicAuthorization(DynamicQueue.deviceId, it)
                    }
                }
            }
        }.asLiveData()
    }

    /**
     * 获取动态列表
     */
    fun getDynamicList(networkId: String, deviceId: String, ip: String, limitMaxTime: Long, pageSize: Int): LiveData<Resource<DynamicList>> {
        return object : NetworkBoundResource<DynamicList>(AppExecutors.instance) {
            override fun createCall() = remoteDataSource.getDynamicList(deviceId, ip, limitMaxTime, pageSize)
            override fun saveCallResult(item: DynamicList) {
                super.saveCallResult(item)
                if (item.code == DynamicQueue.SUCCESS_CODE) //请求成功
                    item.data?.let {
                        DBDelegete.dynamicDelegete.insert(networkId, deviceId, item.data, limitMaxTime)
                        true
                    } ?: let {
                        DBDelegete.dynamicDelegete.insert(networkId, deviceId, null, limitMaxTime)
                    }
            }

            override fun checkResponseFailed(response: DynamicList): Resource<DynamicList>? {
                return response.checkResponseFailed(response)
            }

        }.asLiveData()
    }


    /**
     * 刷新动态列表
     */
    fun getNewestDynamicList(deviceId: String, ip: String, latest: Int = 10): LiveData<Resource<DynamicList>> {
        val limitMaxTime = System.currentTimeMillis()
        return object : NetworkBoundResource<DynamicList>(AppExecutors.instance) {
            override fun createCall() = remoteDataSource.getDynamicList(deviceId, ip, limitMaxTime, latest)
            override fun saveCallResult(item: DynamicList) {
                super.saveCallResult(item)
                if (item.code == DynamicQueue.SUCCESS_CODE) {//请求成功
                    val networkId = CMAPI.getInstance().baseInfo.netid ?: ""
                    item.data?.let {
                        DBDelegete.dynamicDelegete.insert(networkId, deviceId, it, System.currentTimeMillis() / 1000)
                        true
                    } ?: let {
                        DBDelegete.dynamicDelegete.insert(networkId, deviceId, null, System.currentTimeMillis() / 1000)
                    }
                }
            }

            override fun checkResponseFailed(response: DynamicList): Resource<DynamicList>? {
                return response.checkResponseFailed(response)
            }

        }.asLiveData()
    }

    /**
     * 获取指定动态
     */
    fun getDynamic(networkId: String, deviceId: String, ip: String, dynamicId: Long): LiveData<Resource<DynamicList>> {
        return object : NetworkBoundResource<DynamicList>(AppExecutors.instance) {
            override fun createCall() = remoteDataSource.getDynamic(deviceId, ip, dynamicId)
            override fun saveCallResult(item: DynamicList) {
                super.saveCallResult(item)
                if (item.code == DynamicQueue.SUCCESS_CODE) { //请求成功
                    if ((item.data?.size ?: 0) > 0) {
                        DBDelegete.dynamicDelegete.insert(networkId, deviceId, item.data?.get(0))
                    } else {//找不到,删除本地数据
                        DBDelegete.dynamicDelegete.deleteById(networkId, deviceId, dynamicId)
                    }
                }
            }

        }.asLiveData()
    }


    /**
     * 获取指定动态
     */
    fun getDynamicRx(deviceId: String, ip: String, dynamicId: Long) {
        val networkId = CMAPI.getInstance().baseInfo.netid ?: ""
        remoteDataSource.getDynamicRx(deviceId, ip, dynamicId).subscribe(object : Observer<DynamicList> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: DynamicList) {
                if (t.code == DynamicQueue.SUCCESS_CODE) {//更新发布后的动态
                    if ((t.data?.size ?: 0) > 0) {
                        DBDelegete.dynamicDelegete.insert(networkId, deviceId, t.data?.get(0))
                    } else {//找不到, 删除本地数据
                        DBDelegete.dynamicDelegete.deleteById(networkId, deviceId, dynamicId)
                    }
                }
            }

            override fun onError(e: Throwable) {
            }

        })
    }

    private val handler = Handler()

    private fun checkDynamicDeleted(dynamicAutoIncreaseId: Long?, code: Int?, msg: String?) {
        if (DynamicQueue.isDynamicDisplayed) {//在动态界面才提示
            if (code == DynamicQueue.DELETED_CODE) {//动态被删除，提示，并更新动态列表
                handler.post {
                    ToastUtils.showToast(R.string.the_dynamic_is_deleted)
                }
                dynamicAutoIncreaseId?.let {
                    DBDelegete.dynamicDelegete.delete(dynamicAutoIncreaseId)
                }
            } else {
                handler.post {
                    ToastUtils.showToast(R.string.en_server_cant_connected)
                }
            }
        }
    }

    private fun showFailedToast(textResId: Int = R.string.en_server_cant_connected) {
        if (DynamicQueue.isDynamicDisplayed) {//在动态界面才提示
            handler.post {
                ToastUtils.showToast(textResId)
            }
        }

    }

    /**
     * 发布动态
     */
    fun publishDynamicRx(dynamic: Dynamic, againToQueue: Function<Boolean, Void>? = null) {
        val deviceId = DynamicQueue.deviceId
        val ip = DynamicQueue.deviceIP
        val networkId = dynamic.networkId
        val userId = DynamicQueue.mLastUserId
        //单独开启子线程
        remoteDataSource.publishDynamicRx(deviceId, ip, dynamic)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : Observer<DynamicDetail> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: DynamicDetail) {
                        if (t.code == DynamicQueue.SUCCESS_CODE) {//更新发布后的动态
                            //URL 还是使用本地数据
                            DBDelegete.dynamicDelegete.update(dynamic.autoIncreaseId, t.data)
                        } else {//业务请求失败
                            val error = SdvnHttpErrorNo.ec2String(t.code ?: -1, t.msg)
                            DynamicQueue.updateUploadStatus(UploadStatus.FAILED, remark = error)
                        }
                    }

                    override fun onError(e: Throwable) {
                        val message = e.message ?: ""
                        when (message) {
                            "Canceled" -> {//被用户手动取消
                                DBDelegete.dynamicDelegete.delete(dynamic.autoIncreaseId)
                            }
                            DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC -> {
                                //设备不支持动态功能, 直接删除本地数据
                                DBDelegete.dynamicDelegete.delete(dynamic.autoIncreaseId)
                                showFailedToast()
                                DynamicQueue.updateUploadStatus(UploadStatus.NOT_SUPPORT, true)
                            }
                            else -> {
                                if (DynamicQueue.mUploadStatus.value == UploadStatus.CANCELED_BY_USER) {//被用户手动取消
                                    DBDelegete.dynamicDelegete.delete(dynamic.autoIncreaseId)
                                } else {//请求失败，网络异常
                                    val error = MyApplication.getContext().getString(R.string.tip_wait_for_service_connect)
                                    DynamicQueue.updateUploadStatus(UploadStatus.NETWORK_ERROR, remark = error)
                                }
                            }
                        }
                    }
                })
    }

    /**
     * 删除动态
     */
    fun deleteDynamicRx(dynamicAutoIncreaseId: Long, dynamicId: Long, againToQueue: Function<Boolean, Void>? = null) {
        val deviceId = DynamicQueue.deviceId
        val ip = DynamicQueue.deviceIP
        val userId = DynamicQueue.mLastUserId
        val networkId = DynamicQueue.mLastNetworkId
        remoteDataSource.deleteDynamicRx(deviceId, ip, dynamicId).subscribe(object : Observer<Base> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: Base) {
                DBDelegete.dynamicDelegete.delete(dynamicAutoIncreaseId)
            }

            override fun onError(e: Throwable) {//网络问题
                if (e.message == DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC) {//设备不支持动态功能, 直接删除本地数据
                    DBDelegete.dynamicDelegete.delete(dynamicAutoIncreaseId)
                    showFailedToast()
                } else {//重新添加
//                    if (DynamicQueue.isCurrentDynamicTask(networkId, deviceId, userId)) {
//                        //相同动态任务，重新添加到任务队列
//                        againToQueue?.apply(true)
//                    } else {
                    //非相同动态任务，直接删除
                    DBDelegete.dynamicDelegete.delete(dynamicAutoIncreaseId)
//                    }
                }
            }

        })
    }


    /**
     * 发布评论
     * @param callBack 请求回调 后台响应:true，后台无法响应:false
     */
    fun publishCommentRx(comment: DynamicComment, againToQueue: Function<Boolean, Void>? = null) {
        val deviceId = DynamicQueue.deviceId
        val ip = DynamicQueue.deviceIP
        val userId = DynamicQueue.mLastUserId
        val networkId = DynamicQueue.mLastNetworkId
        remoteDataSource.publishCommentRx(deviceId, ip, comment).subscribe(object : Observer<DynamicCommentDetail> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: DynamicCommentDetail) {
                if (t.code == DynamicQueue.SUCCESS_CODE) {
                    //请求成功,请求更新数据  动态
                    DBDelegete.commentDelegete.update(comment.autoIncreaseId, t.data)
                } else {//业务请求失败，直接删除
                    DBDelegete.commentDelegete.delete(comment.autoIncreaseId)
                    //检查是否动态被删除
                    checkDynamicDeleted(comment.dynamic.targetId, t.code, t.msg)
                }
            }

            override fun onError(e: Throwable) {//网络问题
                if (e.message == DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC) {//设备不支持动态功能, 直接删除本地数据
                    DBDelegete.commentDelegete.delete(comment.autoIncreaseId)
                    showFailedToast()
                } else {//重新添加
//                    if (DynamicQueue.isCurrentDynamicTask(networkId, deviceId, userId)) {
//                        //相同动态任务，重新添加到任务队列
//                        againToQueue?.apply(true)
//                    } else {
                    //非相同动态任务，直接删除
                    DBDelegete.commentDelegete.delete(comment.autoIncreaseId)
//                    }
                }
            }

        })
    }

    /**
     * 删除评论
     */
    fun deleteComment(deviceId: String, ip: String, commentId: Long, commentAutoIncreaseId: Long, dynamicAutoIncreaseId: Long): LiveData<Resource<Base>> {
        return object : NetworkBoundResource<Base>(AppExecutors.instance) {
            override fun createCall() = remoteDataSource.deleteComment(deviceId, ip, commentId)
            override fun saveCallResult(item: Base) {
                super.saveCallResult(item)
                if (item.code == DynamicQueue.SUCCESS_CODE) {//后台请求成功，删除本地数据库
                    DBDelegete.commentDelegete.delete(commentAutoIncreaseId)
                } else if (item.code == DynamicQueue.DELETED_CODE) {//动态已被删除
                    DBDelegete.dynamicDelegete.delete(dynamicAutoIncreaseId)
                }
            }
        }.asLiveData()
    }


    /**
     * 点赞
     * @param callBack 请求回调 后台响应:true，后台无法响应:false
     */
    fun likeRx(like: DynamicLike, againToQueue: Function<Boolean, Void>? = null) {
        val deviceId = DynamicQueue.deviceId
        val ip = DynamicQueue.deviceIP
        val userId = DynamicQueue.mLastUserId
        val networkId = DynamicQueue.mLastNetworkId
        val dynamicId = like.momentID ?: 0L
        val likeAutoIncreaseId = like.autoIncreaseId
        remoteDataSource.likeRx(deviceId, ip, dynamicId).subscribe(object : Observer<DynamicLikeDetail> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: DynamicLikeDetail) {
                if (t.code == DynamicQueue.SUCCESS_CODE) {
                    //请求成功,请求更新数据  点赞
                    DBDelegete.likeDelegete.update(likeAutoIncreaseId, t.data)
                } else {//业务上未请求成功,
                    //code＝DynamicQueue.REPEAT_OPERATE_CODE已经点赞过了，或动态被删除1001，直接删除本地数据
                    DBDelegete.likeDelegete.delete(likeAutoIncreaseId)
                    if (t.code == DynamicQueue.REPEAT_OPERATE_CODE) {//已经点赞过了,更新动态信息
                        getDynamicRx(deviceId, ip, dynamicId)
                    }
                    //检查是否动态被删除
                    checkDynamicDeleted(like.dynamic.targetId, t.code, t.msg)
                }
            }

            override fun onError(e: Throwable) {//网络／虚拟网连接问题
                if (e.message == DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC) {//设备不支持动态功能, 直接删除本地数据
                    DBDelegete.likeDelegete.delete(likeAutoIncreaseId)
                    showFailedToast()
                } else {//重新添加
//                    if (DynamicQueue.isCurrentDynamicTask(networkId, deviceId, userId)) {
//                        //相同动态任务，重新添加到任务队列
//                        againToQueue?.apply(true)
//                    } else {
                    //非相同动态任务，直接删除
                    DBDelegete.likeDelegete.delete(likeAutoIncreaseId)
//                    }
                }

            }

        })
    }


    /**
     * 取消赞
     * @param callBack 请求回调 后台响应:true，后台无法响应:false
     */
    fun unLikeRx(like: DynamicLike, againToQueue: Function<Boolean, Void>? = null) {
        val deviceId = DynamicQueue.deviceId
        val ip = DynamicQueue.deviceIP
        val userId = DynamicQueue.mLastUserId
        val networkId = DynamicQueue.mLastNetworkId
        val dynamicId = like.momentID ?: 0L
        val likeAutoIncreaseId = like.autoIncreaseId
        remoteDataSource.unLikeRx(deviceId, ip, dynamicId).subscribe(object : Observer<Base> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: Base) {
                DBDelegete.likeDelegete.delete(likeAutoIncreaseId)
                if (t.code == DynamicQueue.SUCCESS_CODE) {
                } else {//业务上未请求成功, 还原数据
                    //1005=还未点过赞，或动态被删除1001
                    //检查是否动态被删除
                    checkDynamicDeleted(like.dynamic.targetId, t.code, t.msg)
                }
            }

            override fun onError(e: Throwable) {//网络／虚拟网连接问题
                if (e.message == DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC) {//设备不支持动态功能，不处理
                    DBDelegete.likeDelegete.delete(likeAutoIncreaseId)
                    showFailedToast()
                } else {
//                    if (DynamicQueue.isCurrentDynamicTask(networkId, deviceId, userId)) {
//                        //相同动态任务，重新添加到任务队列
//                        againToQueue?.apply(true)
//                    } else {
                    //非相同动态任务，直接删除
                    DBDelegete.likeDelegete.delete(likeAutoIncreaseId)
//                    }
                }
            }

        })
    }


    /**
     * 与我相关消息
     * isDelete默认为0表示获取列表,如为1则调用一次后会清空与我相关
     */
    fun getRelatedMessage(deviceId: String, ip: String, isDelete: Int = 0): LiveData<Resource<AboutMessage>> {
        return object : NetworkBoundResource<AboutMessage>(AppExecutors.instance) {
            override fun createCall() = remoteDataSource.getRelatedMessage(deviceId, ip, isDelete)
        }.asLiveData()
    }

    /**
     * 与我相关
     * isDelete默认为0表示获取列表,如为1则调用一次后会清空与我相关
     */
    fun getRelatedList(networkId: String, deviceId: String, ip: String): LiveData<Resource<RelatedList>> {
        return object : NetworkBoundResource<RelatedList>(AppExecutors.instance) {
            override fun createCall() = remoteDataSource.getRelatedList(deviceId, ip)
            override fun saveCallResult(item: RelatedList) {
                super.saveCallResult(item)
                if (item.code == DynamicQueue.SUCCESS_CODE) //请求成功
                    item.data?.let {
                        DBDelegete.relatedDelegate.save(networkId, deviceId, it)
                        /******更新动态******************************************/
                        if (it.size > 0) {
                            val dynamicIds = arrayListOf<Long>()
                            val dynamicIdsArray = JSONArray()
                            it.forEach {
                                it.momentID?.let {
                                    if (!dynamicIds.contains(it)) { //去重
                                        dynamicIds.add(it)
                                        dynamicIdsArray.put(it)
                                    }
                                }
                            }
                            getDynamicsRx(networkId, deviceId, ip, dynamicIdsArray)
                        }
                    }
            }
        }.asLiveData()
    }

    /**
     * 获取指定动态
     */
    fun getDynamicsRx(networkId: String, deviceId: String, ip: String, dynamicIds: JSONArray) {
        remoteDataSource.getDynamicsRx(deviceId, ip, dynamicIds).subscribe(object : Observer<DynamicList> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {
            }

            override fun onNext(t: DynamicList) {
                if (t.code == DynamicQueue.SUCCESS_CODE) {//更新发布后的动态
                    t.data?.let {
                        DBDelegete.dynamicDelegete.insertOrUpdate(networkId, deviceId, t.data)
                    }
                }
            }

        })
    }


    /**
     * 与我相关
     * isDelete可为空,默认为0,如为1则调用一次后会清空与我相关
     */
    fun getFile(deviceId: String, ip: String, fileName: String): Call<ResponseBody> {
        return remoteDataSource.getFile(deviceId, ip, fileName)
    }
}