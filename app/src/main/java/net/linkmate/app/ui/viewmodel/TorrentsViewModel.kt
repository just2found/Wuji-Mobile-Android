package net.linkmate.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.weline.repo.torrent.BTHelper
import io.weline.repo.torrent.BTRepository
import io.weline.repo.torrent.BtBaseResult
import io.weline.repo.torrent.SessionCallback
import io.weline.repo.torrent.constants.BTResultCode
import io.weline.repo.torrent.constants.BTStatus
import io.weline.repo.torrent.constants.BT_Config
import io.weline.repo.torrent.data.BTItem
import io.weline.repo.torrent.data.BtSession
import libs.source.common.AppExecutors
import libs.source.common.livedata.ApiHTTPErrNO.STATUS_CODE_THROWABLE
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import libs.source.common.utils.RateLimiter
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.manager.DevManager
import net.linkmate.app.util.business.ShareUtil
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.widget.DeviceSelectDialog
import net.sdvn.nascommon.widget.SELF
import net.sdvn.nascommon.widget.ServerFileTreeView
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class TorrentsViewModel(val app: Application) : AndroidViewModel(app) {
    init {
        BTHelper.init(SessionManager.getInstance())
    }

    private val btRepository = BTRepository(AppExecutors.instance)
    private val sessions: MutableMap<String, BtSession> = mutableMapOf()
    private val _torrentsLiveData = MediatorLiveData<List<BTItem>?>()
    private val _torrentsMapLiveData = MutableLiveData<Map<String, List<BTItem>?>>()
    private val _mapData = linkedMapOf<String, List<BTItem>?>()
    val torrentsMapLiveData = _torrentsMapLiveData
    val torrentsLiveData: LiveData<List<BTItem>?> = _torrentsLiveData
    private val limiter = RateLimiter<String>(118, TimeUnit.MINUTES)

    fun createTorrent(devId: String, path: String, pathType: Int): LiveData<Resource<BtBaseResult<BTItem>>> {
        val path = path.substring(path.indexOf(File.separator, 0))
        val liveData: MediatorLiveData<Resource<BtBaseResult<BTItem>>> = MediatorLiveData()
        getSession(devId, object : SimpleSessionCallback(devId) {
            override fun onSuccess(btSession: BtSession) {
                liveData.addSource(btRepository.createByWS(devId, path, pathType, btSession.session), Observer {
                    if (it.status == Status.SUCCESS) {
                        liveData.postValue(it)
                    } else {
                        liveData.postValue(Resource.error("getSession error", null, it.data?.status
                                ?: it.code ?: STATUS_CODE_THROWABLE))
                        doOnError(devId, it.code, it.message)
                    }
                })

            }

            override fun onFailure(code: Int?, msg: String?) {
                super.onFailure(code, msg)
                liveData.postValue(Resource.error("getSession error", null, code
                        ?: STATUS_CODE_THROWABLE))
                doOnError(devId, code, msg)
            }
        })
        return liveData
    }

    private fun doOnError(devId: String, code: Int?, msg: String?, showToast: Boolean = true) {
        when (code) {
            BTResultCode.MSG_ERROR_SESSION -> {
                sessions.remove(devId)
            }
        }
        if (BuildConfig.DEBUG)
            ToastHelper.showLongToast("test(${code} ${msg})")
        if (showToast) {
            ToastHelper.showLongToast(getErrorMsgResId(app, code ?: -1, msg))
        }
    }

    fun getSession(devId: String, callback: SessionCallback) {
        val btSession = sessions.get(devId)
        if (btSession == null || limiter.shouldFetch(devId)) {
            val token = if (BTHelper.isLocal(devId)) {
                BT_Config.token
            } else {
                LoginTokenUtil.getToken()
            }
            btRepository.auth(devId, token).observeForever {
                if (it.status == Status.SUCCESS) {
                    val btSessionNew = it.data!!
                    sessions.put(devId, btSessionNew)
                    callback.onSuccess(btSessionNew)
                } else if (it.status == Status.ERROR) {
                    callback.onFailure(it.code, it.message)
                }
            }
        } else {
            callback.onSuccess(btSession)
        }

    }

    fun showBtItemQRCodeView(context: FragmentActivity, btItem: BTItem,
                             isOwner: Boolean = false) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_share_code_torrent, null)
        val ivShareCode = view.findViewById<ImageView>(R.id.ivShareCode)
        view.findViewById<TextView>(R.id.tvName).setText(btItem.name)
        view.findViewById<TextView>(R.id.tvSize).setText(FileUtils.fmtFileSize(btItem.totalLen))
        view.findViewById<TextView>(R.id.tvTime).setText(FileUtils.fmtTimeByZone(btItem.timestamp))

        val negative = view.findViewById<TextView>(R.id.negative)
        val positive = view.findViewById<TextView>(R.id.positive)
        val dialog = DialogUtils.showCustomDialog(context, view)
        val groupShareView = view.findViewById<View>(R.id.group_share_view)
        groupShareView.isVisible = isOwner
        if (isOwner) {
            val torrentTicket = "${btItem.btTicket ?: ""}_${btItem.remoteServer ?: ""}_${btItem.netId ?: ""}_${btItem.devId ?: ""}"
            val content = MyApplication.getContext().getResources().getString(R.string.downloadURL) +
                    "#tsc=$torrentTicket"
            val subscribe = Observable.create<Bitmap> { e ->
                val bitmap = QRCodeEncoder.syncEncodeQRCode(content,
                        MyApplication.getContext().getResources().getDisplayMetrics().widthPixels * 5 / 6)
                e.onNext(bitmap)
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .`as`(RxLife.`as`(context))
                    .subscribe(Consumer {
                        ivShareCode.setImageBitmap(it)
                    })
            view.findViewById<TextView>(R.id.tvShare).apply {
                setText(R.string.share)
            }
            groupShareView.setOnClickListener {
                ShareUtil.saveAndShareImg(ivShareCode, torrentTicket,
                        object : ShareUtil.SaveImageResult {
                            override fun onSuccess() {

                            }

                            override fun onError() {
                            }
                        })
            }
        }
        positive.setText(R.string.download)
        positive.setOnClickListener {
            dialog.dismiss()
            downloadTorrent(context, btItem.btTicket, btItem.remoteServer, btItem.devId)
        }
        negative.setText(R.string.cancel)
        negative.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun downloadTorrent(fragmentActivity: @NotNull FragmentActivity, btTicket: @NotNull String,
                        remoteServer: @NotNull String,
                        srcId: String? = null,
                        callback: Callback<Result<Boolean>>? = null) {
        DeviceSelectDialog(fragmentActivity,
                filterType = DeviceSelectDialog.FilterType.BT_DOWNLOAD,
                filterExtIds = if (srcId != null) {
                    Collections.singletonList(srcId)
                } else null,
                callback = Callback {
                    Timber.d("select a device : $it ")
                    if (it == SELF) {
                        downloadTorrent(BT_Config.BT_LOCAL_DEVICE_ID, btTicket, remoteServer, callback = callback)
                    } else {
                        SessionManager.getInstance().getLoginSession(it, object : GetSessionListener() {
                            override fun onSuccess(url: String?, loginSession: LoginSession) {
                                ServerFileTreeView(fragmentActivity, null,
                                        loginSession, R.string.tip_download_file, R.string.confirm, R.string.cancel)
                                        .setOnPasteListener(object : ServerFileTreeView.OnPasteFileListener {
                                            override fun onPaste(tarPath: String?, share_path_type: Int) {
                                                val saveDir = if (tarPath?.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR) == true) {
                                                    tarPath.replaceFirst(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, "/")
                                                } else {
                                                    tarPath
                                                }
                                                downloadTorrent(it, btTicket, remoteServer, saveDir = saveDir
                                                        ?: "/Downloads", pathType = share_path_type,
                                                        callback = callback)
                                            }
                                        })
                                        .showPopupCenter()
                            }
                        })
                    }
                }).show()
    }

    private fun downloadTorrent(devId: String, btTicket: String,
                                remoteServer: String,
                                saveDir: String = "/Downloads",
                                pathType: Int = 2,
                                callback: Callback<Result<Boolean>>? = null) {
        getSession(devId, object : SimpleSessionCallback(devId) {
            override fun onSuccess(btSession: BtSession) {
                btRepository.download(devId, btSession.session, btTicket, remoteServer, saveDir, pathType)
                        .observeForever {
                            if (it.status == Status.SUCCESS) {
                                ToastHelper.showLongToast(R.string.Added_to_the_queue_being_download)
                                callback?.result(Result(true))

                            } else if (it.status == Status.ERROR) {
                                val baseResult = it.data
                                doOnError(devId, baseResult?.status
                                        ?: -402, baseResult?.msg)
                                callback?.let { callback ->
                                    callback.result(Result(baseResult?.status
                                            ?: -402, baseResult?.msg))
                                }
                            }
                        }
            }

            override fun onFailure(code: Int?, msg: String?) {
                super.onFailure(code, msg)
                doOnError(devId, code ?: -402, msg)
                callback?.result(Result(code ?: -402, msg))
            }
        })
    }

    fun getList(devId: String,btName: String?, isGetFromServer: Boolean = false) {
        if (!isGetFromServer) {
            _torrentsLiveData.addSource(btRepository.list(devId,btName), Observer {
                if (it.status == Status.SUCCESS) {
                    val items = it.data!!.result.items
                    _torrentsLiveData.postValue(items)
                    _mapData.put(devId, items)
                    _torrentsMapLiveData.postValue(_mapData)
                }
            })
        } else {
            getSession(devId, object : SimpleSessionCallback(devId) {
                override fun onSuccess(btSession: BtSession) {
                    _torrentsLiveData.addSource(btRepository.list(devId,btName, btSession.session), Observer {
                        if (it.status == Status.SUCCESS) {
                            val items = it.data!!.result.items
                            _mapData.put(devId, items)
                            _torrentsMapLiveData.postValue(_mapData)
                            _torrentsLiveData.postValue(items)
                        } else if (it.status == Status.ERROR) {
                            val baseResult = it.data
                            doOnError(devId, baseResult?.status
                                    ?: -402, baseResult?.msg,false)
                        }
                    })
                }
            })

        }
    }

    fun showDownloadDialog(context: Context, btItem: BTItem) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_download_bt, null)
        view.findViewById<TextView>(R.id.rv_list_txt_name).text = btItem.name
        view.findViewById<TextView>(R.id.rv_list_txt_size).text = FileUtils.fmtFileSize(btItem.totalLen)
        view.findViewById<TextView>(R.id.rv_list_txt_time).text = FileUtils.fmtTimeByZone(btItem.timestamp)
        val negative = view.findViewById<TextView>(R.id.negative)
        val positive = view.findViewById<TextView>(R.id.positive)
        negative.setText(R.string.cancel)
        positive.setText(R.string.download)
        val dialog = DialogUtils.showCustomDialog(context, view)
        positive.setOnClickListener {

        }
        negative.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun getProgress(devId: String, dlTickets: List<String>) {
        getSession(devId, object : SimpleSessionCallback(devId, false) {
            override fun onSuccess(btSession: BtSession) {
                btRepository.progress(devId, btSession.session, dlTickets).observeForever(Observer {
                    if (it.status == Status.SUCCESS) {
                        val items = it.data!!.result.items
                        if (!items.isNullOrEmpty()) {
                            _mapData[devId]?.let { list ->
                                val toMutableList = items.toMutableList()
                                outer@ for (btItem in list) {
                                    val iterator = toMutableList.iterator()
                                    while (iterator.hasNext()) {
                                        val item = iterator.next()
                                        if (btItem.dlTicket == item.dlTicket) {
                                            btItem.downloadLen = item.downloadLen
                                            btItem.speed = item.speed
                                            btItem.status = item.status
                                            btItem.seeding = item.seeding
                                            iterator.remove()
                                            if (toMutableList.isEmpty()) {
                                                break@outer
                                            } else {
                                                break
                                            }
                                        }
                                    }
                                }
                                _torrentsLiveData.postValue(list)
                                _mapData.put(devId, list)
                                _torrentsMapLiveData.postValue(_mapData)
                            }
                        }
                    } else if (it.status == Status.ERROR) {
                        val baseResult = it.data
                        doOnError(devId, baseResult?.status
                                ?: -402, baseResult?.msg, showToast = false)
                    }
                })
            }
        })
    }

    fun stop(devId: String, item: BTItem) {
        stop(devId, listOf(item.dlTicket))
    }

    fun stop(devId: String, dlTickets: List<String>) {
        getSession(devId, object : SimpleSessionCallback(devId) {
            override fun onSuccess(btSession: BtSession) {
                _torrentsLiveData.addSource(btRepository.stop(devId, btSession.session, dlTickets), Observer {
                    if (it.status == Status.SUCCESS) {
//                        ToastHelper.showLongToast(R.string.success)
                    } else if (it.status == Status.ERROR) {
                        val baseResult = it.data
                        var showToast = true
                        when (baseResult?.status) {
                            BTResultCode.MSG_ERROR_IS_DOWNLOADING -> {
                                showToast = false
                            }
                        }
                        doOnError(devId, baseResult?.status
                                ?: -402, baseResult?.msg, showToast)
                    }
                })
            }
        })
    }

    fun resume(devId: String, dlTicket: String) {
        getSession(devId, object : SimpleSessionCallback(devId) {
            override fun onSuccess(btSession: BtSession) {
                _torrentsLiveData.addSource(btRepository.resume(devId, btSession.session, dlTicket), Observer {
                    if (it.status == Status.SUCCESS) {
                        _mapData[devId]?.let { list ->
                            outer@ for (btItem in list) {
                                if (btItem.dlTicket == dlTicket) {
                                    btItem.status = BTStatus.DOWNLOADING
                                    break@outer
                                }
                            }
                            _torrentsLiveData.postValue(list)
                            _mapData.put(devId, list)
                            _torrentsMapLiveData.postValue(_mapData)
                        }
//                        ToastHelper.showLongToast(R.string.success)
                    } else if (it.status == Status.ERROR) {
                        val baseResult = it.data
                        doOnError(devId, baseResult?.status
                                ?: -402, baseResult?.msg)
                    }
                })
            }
        })
    }

    fun cancel(devId: String, item: BTItem) {
        cancel(devId, listOf(item.dlTicket))
    }

    fun cancel(devId: String, dlTickets: List<String>) {
        getSession(devId, object : SimpleSessionCallback(devId) {
            override fun onSuccess(btSession: BtSession) {
                _torrentsLiveData.addSource(btRepository.cancel(devId, btSession.session, dlTickets)
                ) { resource ->
                    if (resource.status == Status.SUCCESS) {
                        ToastHelper.showLongToast(R.string.success)
                        val items = resource.data?.result?.items
                        val value = _mapData[devId]
                        if (!items.isNullOrEmpty() && !value.isNullOrEmpty()) {
                            val toMutableList = value.toMutableList()
                            toMutableList.removeAll(items)
                            _mapData.put(devId, toMutableList)
                            _torrentsMapLiveData.postValue(_mapData)
                            _torrentsLiveData.postValue(toMutableList)
                        }
                    } else if (resource.status == Status.ERROR) {
                        val baseResult = resource.data
                        doOnError(devId, baseResult?.status
                                ?: -402, baseResult?.msg)
                    }
                }
            }
        })
    }

    fun showScanQRCodeResult(context: FragmentActivity, btTicket: String, remoteDomain: String, callback: Callback<Result<Boolean>>) {
        btRepository.list(null, null, remoteDomain)
                .observe(context, Observer {
                    if (it.status == Status.SUCCESS) {
                        callback.result(Result(true))
                        val find = it.data?.result?.items?.find { it.btTicket == btTicket }
                        if (find != null) {
                            find.remoteServer = remoteDomain
                            val deviceBeanByDomain = DevManager.getInstance().getDeviceBeanByDomain(remoteDomain)
                            find.devId = deviceBeanByDomain?.id ?: ""
                            showBtItemQRCodeView(context, find, false)
                        } else {
                            ToastHelper.showLongToast(R.string.msg_error_canceled)
                        }
                    } else if (it.status == Status.ERROR) {
                        val baseResult = it.data
                        doOnError("", baseResult?.status
                                ?: -402, baseResult?.msg)
                        callback.result(Result(baseResult?.status
                                ?: -402, baseResult?.msg))
                    }
                })

    }

    fun getErrorMsgResId(context: Context, code: Int, msg: String?): String {
        val resId: Int = when (code) {
            BTResultCode.MSG_OK -> {
                // 0 //正确返回
                R.string.success
            }
            BTResultCode.MSG_ERROR_PARAM -> {
                R.string.msg_error_params_error
            }  //参数错误
            BTResultCode.MSG_NO_EXIST_USER -> {
                R.string.msg_error_user_no_exist
            }  //不存在的用户
            BTResultCode.MSG_ERROR_BT_PATH -> {
                R.string.msg_error_invalid_path
            }  //错误的路径
            BTResultCode.MSG_ERROR_BT_CREATE_SEED -> {
                R.string.msg_error_creat_ft
            } //错误的种子
            BTResultCode.MSG_ERROR_SESSION -> {
                R.string.msg_error_session
            }  //不存在的会话或者已经失效
            BTResultCode.MSG_ERROR_TOKEN -> {
                R.string.msg_error_invalid_token
            }  //错误的token
            BTResultCode.MSG_ERROR_RESUME -> {
                R.string.msg_error_resume
            }  //续传错误
            BTResultCode.MSG_ERROR_NO_SEED -> {
                R.string.msg_error_canceled
            }  //不存在的种子
            BTResultCode.MSG_ERROR_CONNECT_HOST -> {
                R.string.msg_error_connect_host
            }  //链接远端服务器失败
            BTResultCode.MSG_ERROR_NO_PERM -> {
                R.string.msg_error_no_permission
            } //没有权限创建种子
            BTResultCode.MSG_ERROR_VERIFY_PACKAGE -> {
                R.string.msg_error_verify_package
            } //  验证包，出错
            BTResultCode.MSG_ERROR_IS_NOT_EN -> {
                R.string.msg_error_is_not_en
            }//12//不是EN服务器
            BTResultCode.MSG_ERROR_DATA_CANNOT_EMPTY -> {
                R.string.msg_error_data_cannot_empty
            }
            BTResultCode.MSG_ERROR_SEED_ALREADY_EXISTS -> {
                R.string.msg_error_seed_already_exists
            }
            BTResultCode.MSG_ERROR_IS_DOWNLOADING -> {
                R.string.msg_error_is_downloading
            }
            BTResultCode.MSG_ERROR_DISKFULL -> {
                R.string.msg_error_disk_full
            }
            BTResultCode.MSG_ERROR_NO_DISK -> {
                R.string.tip_no_sata
            }
            BTResultCode.MSG_ERROR_NOT_IN_RING -> {
                R.string.msg_error_not_in_same_circle
            }
            else -> {
                R.string.ec_request
            }
        }
        return "${if (resId > 0) {
            context.getString(resId)
        } else {
            msg
        }}($code)"
    }

    abstract inner class SimpleSessionCallback(
            var devId: String, var showToast: Boolean = true) : SessionCallback() {
        override fun onFailure(code: Int?, msg: String?) {
            super.onFailure(code, msg)
            doOnError(devId, code, msg, showToast)
        }
    }
}

fun sorsTorrentService(context: Context, isStart: Boolean) {
//    if (isStart) {
//        PermissionChecker.checkPermission(context, Callback {
//            val intent = Intent(context, TorrentClientService::class.java)
//            intent.action = CLIENT_ACTION_START
//            intent.putExtra(CLIENT_ARG_DOWNLOAD_DIR_PATH, BT_Config.DEFAULT_DOWNLOAD_PATH)
//            intent.putExtra(CLIENT_ARG_TOKEN, BT_Config.token)
//            // bindService(intent, torrentClientServiceConn, Context.BIND_ABOVE_CLIENT)
//            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //     context.startForegroundService(intent)
//            // }else{
//            context.startService(intent)
//            //}
//            Timber.d("startTorrentClient 0")
//            // Timber.d("startTorrentClient ${NativeTorrent.token}")
//            // NativeTorrent.getInstance().init(applicationContext,"")
//        }, Callback {
//            UiUtils.showStorageSettings(context)
//        }, Permission.WRITE_EXTERNAL_STORAGE)
//
//    } else {
//        val intent = Intent(context, TorrentClientService::class.java)
//        intent.action = CLIENT_ACTION_STOP
//        context.startService(intent)
//        Timber.d("startTorrentClient -1 stop")
//    }
}