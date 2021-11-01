package net.linkmate.app.service

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.util.Function
import androidx.lifecycle.MutableLiveData
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.repository.DynamicRepository
import net.linkmate.app.ui.simplestyle.dynamic.CircleStatus
import net.linkmate.app.ui.simplestyle.dynamic.delegate.db.DBDelegete
import net.linkmate.app.util.FileUtils
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.http.DynamicUploadRequestBody
import net.linkmate.app.view.HintDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.BuildConfig
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.utils.LoginTokenUtil
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/** 保存动态队列值
 * @author Raleigh.Luo
 * date：21/1/23 14
 * describe：
 */
object DynamicQueue {
    /***--操作类型------------------------------------------------****/
    const val UNLIKE_TYPE = "unlike"
    const val COMMENT_TYPE = "comment"
    const val DELETE_DYNAMIC_TYPE = "deleteDynamic"
    const val PUBLISH_DYNAMIC_TYPE = "publishDynamic"
    const val LIKE_TYPE = "like"


    /**throw exception***/
    const val THE_CIRCLE_NOT_SUPPORT_DYNAMIC = "the_circle_not_support_dynamic"


    const val TAG = "DynamicQueue"
    private val MESSAGE_QUEUE_LIMIT = 40// 消息队列大小
    const val DELETED_CODE = 1002//被删除code
    const val SUCCESS_CODE = 200//请求成功
    const val REPEAT_OPERATE_CODE = 1004//重复操作
    const val ERROR_OPERATE_CODE = 1005//错误操作，如，未点赞却进行取消点赞操作
    const val TOKEN_EXPIRE = 401//Toke过期

    //上一个用户id
    //上一个网络id
    var mLastUserId: String? = null
        private set(value) {
            field = value
        }
    var mLastNetworkId: String? = null
        private set(value) {
            field = value
        }

    //实时当前网络最新主EN设备id值,仅在动态页面更新
    var deviceId = ""
        set(value) {
            val currentUserId = CMAPI.getInstance().baseInfo.userId ?: ""
            val currentNetworkId = CMAPI.getInstance().baseInfo.netid ?: ""
            if (field != value || mLastUserId != currentUserId || mLastNetworkId != currentNetworkId) {
                //主EN或网络或帐号变化时，清空本地动态数据,清空任务队列
                setRun(false)
                msgQueue.clear()
                field = value
                mLastUserId = currentUserId
                mLastNetworkId = currentNetworkId
                DBDelegete.dynamicDelegete.clearLocal()
            } else {
                field = value
            }

        }

    //实时当前网络最新主EN设备ip值,仅在动态页面更新
    var deviceIP = ""

    //实时当前网络最新状态值,仅在动态页面更新
    var currentCircleStatus = CircleStatus.WITHOUT_NETWORK.type

    //当前用户是否时圈子所有者
    var isCircleOwner = false

    /**
     * 检查是否是当前圈子当前用户的动态任务
     */
    fun isCurrentDynamicTask(networkId: String?, deviceId: String?, userId: String?): Boolean {
        return (this.deviceId == deviceId && this.mLastNetworkId == networkId && this.mLastUserId == userId)
    }

    //是否显示的时当前动态页面
    var isDynamicDisplayed = false


    /**
     * 任务队列，BlockingQueue可重复
     */
    private val msgQueue: BlockingQueue<String> = ArrayBlockingQueue<String>(MESSAGE_QUEUE_LIMIT)


    /**
     * 是否正在执行
     * 线程同步值
     */
    @Volatile
    private var run = false

    /**
     * 检查LoginToken,必须先获取
     */
    fun checkLoginToken(callBack: Function<Boolean, Void?>) {
        if (TextUtils.isEmpty(LoginTokenUtil.getToken())) {//Token优先处理
            LoginTokenUtil.getLoginToken(object : LoginTokenUtil.TokenCallback {
                override fun error(protocol: GsonBaseProtocol?) {
                    callBack.apply(false)
                }

                override fun success(token: String?) {
                    callBack.apply(true)
                }
            })

        } else {
            callBack.apply(true)
        }
    }

    /**
     * 开启或停止 并操作服务
     */
    fun setRun(run: Boolean) {
        if (this.run == false && run == true) {
            checkLoginToken(Function {
                if (it) {
                    this.run = true
                    startService()
                    if (BuildConfig.DEBUG) Timber.d(TAG + "-> DynamicService start (setRun)")
                }
                null
            })
        } else if (this.run == true && run == false) {
            stopService()
            if (BuildConfig.DEBUG) Timber.d(TAG + "-> DynamicService stop (setRun)")
        }
    }

    /**
     * service 正常停止，不操作服务
     */
    fun close() {
        this.run = false
        if (BuildConfig.DEBUG) Timber.d("DynamicService end (close)")
    }


    private fun startService() {
        MyApplication.getInstance().startService(Intent(MyApplication.getInstance(), DynamicService::class.java))
    }

    private fun stopService() {
        this.run = false
        MyApplication.getInstance().stopService(Intent(MyApplication.getInstance(), DynamicService::class.java))
    }

    fun isEmpty(): Boolean {
        return msgQueue.size == 0
    }

    /**
     * 拿出消息
     */
    internal fun poll(): String? {
        return msgQueue.poll()
    }


    @Synchronized
    fun push(type: String, id: Long): Boolean {
        //注意不能用符号－，避免冲突负数如-1
        val key = String.format("%s/%s", type, id)
        val result = push(key)
        if (type == PUBLISH_DYNAMIC_TYPE && result) {//发布动态
            mUploadIdentificationTemp = UploadIdentification(key, id)
            updateUploadStatus(UploadStatus.NONE)
        }
        return result
    }

    @Synchronized
    fun push(message: String): Boolean {
        if (TextUtils.isEmpty(message)) {
            return false
        }
        return if (msgQueue.size < MESSAGE_QUEUE_LIMIT) {

            //不过滤的数据可以加
            if (!msgQueue.contains(message)) {
                msgQueue.add(message)
                if (BuildConfig.DEBUG) {
                    Timber.e("push msgQueue message = $message,msgQueue" + msgQueue)
                }
                if (run == false && CMAPI.getInstance().isEstablished) {//未启动服务 且虚拟网已连接
                    setRun(true)
                }
                true
            } else {
                false
            }
        } else {
            if (BuildConfig.DEBUG) {
                Timber.e("push msgQueue is oversize")
            }
            false
        }
    }

    /**
     * 是否已经包含
     */
    fun containsAndRemove(type: String, id: Long): Boolean {
        val key = String.format("%s/%s", type, id)
        val result = msgQueue.contains(key)
        if (result) msgQueue.remove(key)
        return result
    }

    /**
     * 是否已经包含
     */
    fun containsAndRemove(message: String): Boolean {
        val result = msgQueue.contains(message)
        if (result) msgQueue.remove(message)
        return result
    }


    private val repository = DynamicRepository()
    fun downloadFile(deviceId: String, ip: String, fileName: String, dirPath: String? = null, defualtSuffix: String = "") {
        repository.getFile(deviceId, ip, fileName).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //下载失败
                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.post {
                    ToastUtils.showToast(R.string.download_failed)
                }
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val contentType = response.headers().get("Content-Type")
                val split = contentType?.split("/")
                var suffix: String = ""
                if ((split?.size ?: 0) > 1) {
                    suffix = split?.get(1) ?: ""
                }
                if (TextUtils.isEmpty(suffix)) {
                    suffix = defualtSuffix
                }
                val ext = suffix
                suffix = "." + suffix
                //将Response写入到从磁盘中
                var mDirPath = dirPath
                if (mDirPath == null) {
                    //保存到视频图库中
                    val dirFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_MOVIES)
                    if (!dirFile.exists()) dirFile.mkdirs()
                    mDirPath = dirFile.absolutePath
                }
                val path = mDirPath + "/" + System.currentTimeMillis().toString() + suffix
                FileUtils.writeFile(response.body()?.byteStream(), path, true, 8192)
                //3. notify
                MediaScannerConnection.scanFile(MyApplication.getContext(), arrayOf(path), arrayOf("video/$ext")) { path, uri ->
                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.post {
                        ToastUtils.showToast(MyApplication.getContext().getString(R.string.file_is_saved) + path)
                    }
                }

            }
        })
    }

    /**
     * 取消发布动态
     */
    fun cancelPublishDynamic() {
        try {
            val message = getUploadIdentification()?.identification ?: ""
            mProgressRequestListener?.cancel()
            mProgressRequestListener?.clear()
            mProgressRequestListener = null
            mUploadIdentificationTemp = null
            if (!TextUtils.isEmpty(message)) {
                if (msgQueue.contains(message)) {//正在队列中，移除
                    msgQueue.remove(message)
                    DBDelegete.dynamicDelegete.delete(getUploadIdentification()?.autoIncreaseId
                            ?: 0L)
                }
            }
            updateUploadStatus(UploadStatus.CANCELED_BY_USER)
        } catch (e: Exception) {
        }
    }


    //为false时，表示取消请求
    var mUploadStatus = MutableLiveData<UploadStatus>()

    /**
     * @param isNotify 是否通知更新
     */
    fun updateUploadStatus(status: UploadStatus, isClearProgressListener: Boolean = false, remark: String? = null) {
        if (isClearProgressListener) {
            mProgressRequestListener?.clear()
            mProgressRequestListener = null
        } else if (!TextUtils.isEmpty(remark)) {
            getUploadIdentification()?.remark = remark
        }
        mUploadStatus.postValue(status)
    }

    fun getUploadIdentification(): UploadIdentification? {
        return mProgressRequestListener?.getUploadIdentification() ?: mUploadIdentificationTemp
    }

    private var mUploadIdentificationTemp: UploadIdentification? = null
    private var mProgressRequestListener: DynamicUploadRequestBody.ProgressRequestListener? = null
    fun getUploadIdentification(call: okhttp3.Call): DynamicUploadRequestBody.ProgressRequestListener {
        mProgressRequestListener = object : DynamicUploadRequestBody.ProgressRequestListener {
            private var call: okhttp3.Call? = null
            var mUploadIdentification: UploadIdentification? = null
            override fun isCanceled(): Boolean {
                return mUploadStatus.value == UploadStatus.CANCELED_BY_USER
            }

            override fun cancel() {
                mUploadIdentification = null
                this.call?.cancel()
            }

            override fun start(identification: String?, contentLength: Long) {
                this.call = call
                val messageSplit = identification?.split("/")
                if ((messageSplit?.size ?: 0) < 2) return
                val autoIncreseId = messageSplit?.get(1)?.toLong()
                mUploadIdentification = UploadIdentification(identification, autoIncreseId, contentLength = contentLength)
                updateUploadStatus(UploadStatus.STARTED, false)
                mUploadIdentificationTemp = null
            }

            override fun onRequestProgress(bytesWritten: Long, contentLength: Long, done: Boolean) {
                if (this == mProgressRequestListener) {
                    if (done) {
                        updateUploadStatus(UploadStatus.SUCCESS, true)
                    } else {
                        mUploadIdentification?.bytesWritten = bytesWritten
                        mUploadIdentification?.contentLength = contentLength
                        updateUploadStatus(UploadStatus.UPLOADING)
                    }
                }
            }

            override fun getUploadIdentification(): UploadIdentification? {
                return mUploadIdentification
            }

            override fun clear() {
                this.call = null
                mUploadIdentification = null
            }
        }

        return mProgressRequestListener!!
    }

    data class UploadIdentification(var identification: String?,
                                    var autoIncreaseId: Long?,
                                    var bytesWritten: Long = 0L,
                                    var contentLength: Long = 0L,
                                    var networkId: String? = DynamicQueue.mLastNetworkId,
                                    var deviceId: String = DynamicQueue.deviceId,
                                    var userId: String? = DynamicQueue.mLastUserId,
                                    var remark: String? = null//失败原因
    ) {
        /**
         * 是否是当前圈子上传进度数据
         */
        fun isCurrentCircle(): Boolean {
            return networkId == DynamicQueue.mLastNetworkId && deviceId == DynamicQueue.deviceId && userId == DynamicQueue.mLastUserId
        }
    }

    /**
     * 检查是否有未发布成功的动态
     */
    fun checkExistPublishingDynamic(context: AppCompatActivity, callBack: Function<Boolean, Void?>) {
        if (getUploadIdentification() != null && (getUploadIdentification()?.isCurrentCircle()
                        ?: false)) {
            HintDialog.newInstance(context.getString(R.string.warning),
                    context.getString(R.string.stop_publish_dynamic_hint),
                    R.color.red,
                    context.getString(R.string.dialog_continue),
                    context.getString(R.string.cancel),
                    cancelColor = R.color.text_gray
            ).setOnClickListener(View.OnClickListener {
                if (it.id == R.id.positive) {//确定按钮
                    //取消发布动态任务
                    cancelPublishDynamic()
                    callBack.apply(true)
                }
            }).show(context.getSupportFragmentManager(), "hintDialog")
        } else {
            callBack.apply(true)
        }
    }
}

enum class UploadStatus {
    NONE,
    STARTED,
    UPLOADING,//上传中
    CANCELED_BY_USER,//用户取消上传
    NOT_SUPPORT,//不支持
    FAILED,//请求失败，服务器异常，ec_exception
    NETWORK_ERROR,//请求失败，网络异常
    SUCCESS//请求成功
}