package net.linkmate.app.util

import android.content.Intent
import androidx.activity.viewModels
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache
import io.weline.repo.api.V5_ERR_DISK_BUILDING
import io.weline.repo.api.V5_ERR_DISK_FORMATTING
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DeviceDialogManage
import net.linkmate.app.ui.nas.files.V2NasDetailsActivity
import net.linkmate.app.ui.nas.helper.HdManageActivity
import net.linkmate.app.ui.nas.info.NavigationContainerActivity
import net.linkmate.app.ui.nas.torrent.TorrentActivity
import net.linkmate.app.ui.simplestyle.device.disk.DiskSpaceActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * @author Raleigh.Luo
 * date：21/2/19 14
 * describe：
 */
class AccessDeviceTool(val activity: BaseActivity, var fragment: Fragment? = null) {
    protected var mLoginDeviceDisposable: Disposable? = null
    protected val mDeviceViewModel: DeviceViewModel by activity.viewModels()

    /**
     * 根据状态进入设备不同界面
     * @param callback 设备状态回调，true表示正常可开始访问，false表示状态异常
     */
    fun open(bean: DeviceBean, position: Int, callback: Function<Boolean, Void>, isComeCircle: Boolean = false) {
        if (bean.isVNode) {
            // TODO: 2019/12/12  vnode test
            //虚拟节点
            DeviceDialogManage.showDeviceDetailDialog(activity, 0, 0, bean, null)
        } else if (bean.isNas && bean.isOnline) {
            if (bean.hardData != null && bean.hardData!!.isEnableUseSpace) {
                accessDevice(bean, Function {
                    it?.let {
                        if (it) {
                            startNasActivity(bean,isComeCircle)
                        }
                        callback.apply(it)
                    }
                    null
                })
            } else if (bean.hardData != null && bean.isEn || bean.enServer != null && bean.enServer!!.isEn == true) {
                if (SessionManager.getInstance().getDeviceModel(bean.id) != null) {
                    accessDevice(bean, Function {
                        it?.let {
                            if (it) {
                                startNasActivity(bean,isComeCircle)
                            }
                            callback.apply(it)
                        }
                        null
                    })
                } else {
                    TorrentActivity.startActivityWithId(activity, bean.id,null)
                }
            } else {
                DeviceDialogManage.showDeviceDetailDialog(activity, 0, 0, bean, null)
            }
        } /*else if (bean.getType() == 0 || devBoundType == DevBoundType.ALL_BOUND_DEVICES ||
                            devBoundType == DevBoundType.LOCAL_DEVICES) {
                        //sn、我绑定的、本地的
                        showDeviceDetailDialog(position, bean);
                    } else if (devBoundType != DevBoundType.MY_DEVICES && devBoundType != DevBoundType.SHARED_DEVICES && bean.getType() != 2) {
                        //设备
//                        gotoDevicePager(position, bean);
                        showDeviceDetailDialog(position, bean);
                    } */ else {
            //终端
//                        gotoDevicePager(position, bean);
            DeviceDialogManage.showDeviceDetailDialog(activity, 0, 0, bean, null)
        }
    }

    /**
     * 访问设备简介
     * 先检查硬盘情况
     * @param callback 设备状态回调，true表示正常可开始访问，false表示状态异常
     */
    fun accessDevice(dev: DeviceBean, callback: Function<Boolean, Void>) {
        if (dev.mnglevel != 0 && dev.mnglevel != 1 && dev.mnglevel != 2) {//非已绑定设备，直接访问
            callback.apply(true)
            return
        }
        val deviceId: String = dev.id
        activity.status = BaseActivity.LoadingStatus.ACCESS_NAS
        activity.showLoading(R.string.connecting)
        val timerDisposable = Observable.timer(15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(RxLife.`as`(activity))
                .subscribe { aLong: Long? -> activity.showLoading(R.string.slow_request_wait_loading) }
        val timer60Disposable = Observable.timer(60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(RxLife.`as`(activity))
                .subscribe { aLong: Long? ->
                    if (mLoginDeviceDisposable != null) {
                        mLoginDeviceDisposable!!.dispose()
                    }
                }
        mLoginDeviceDisposable = mDeviceViewModel.toLogin(dev, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(Action {
                    timerDisposable.dispose()
                    timer60Disposable.dispose()
                    activity.status = BaseActivity.LoadingStatus.DEFUALT
                    activity.dismissLoading()
                })
                .`as`(RxLife.`as`<Resource<Result<*>>>(activity))
                .subscribe(Consumer<Resource<Result<*>>> { (status, data) ->
                    timerDisposable.dispose()
                    timer60Disposable.dispose()
                    activity.status = BaseActivity.LoadingStatus.DEFUALT
                    activity.dismissLoading()
                    if (status === Status.SUCCESS) {//正常，访问简介
                        callback.apply(true)
                    } else {
                        try {
                            val data = data!!
                            val nasV3 = SessionCache.instance.isNasV3(dev.id)
                            if (nasV3) {
                                if (dev.isOwner && (data.code == HttpErrorNo.ERR_ONESERVER_HDERROR ||
                                                data.code == V5_ERR_DISK_FORMATTING
                                                || data.code == V5_ERR_DISK_BUILDING)) {
                                    activity.startActivity(Intent(activity, DiskSpaceActivity::class.java)
                                            .putExtra(AppConstants.SP_FIELD_DEVICE_ID, dev.id) //是否是EN服务器
                                    )
                                } else {
                                    ToastHelper.showLongToast(HttpErrorNo.getResultMsg(data.code, data.msg))
                                }
                            } else {
                                if (data.code == HttpErrorNo.ERR_ONESERVER_HDERROR
                                        && data.msg.toInt() > 0) {
                                    openHDManagerView(deviceId, data.msg)
                                } else {
                                    ToastHelper.showLongToast(HttpErrorNo.getResultMsg(data.code, data.msg))
                                }
                            }
                        } catch (ignore: Exception) {
                            ignore.printStackTrace()
                            ToastHelper.showLongToast(R.string.unknown_exception)
                        }
                        callback.apply(false)
                    }
                }, Consumer<Throwable?> { throwable ->
                    timerDisposable.dispose()
                    timer60Disposable.dispose()
                    activity.status = BaseActivity.LoadingStatus.DEFUALT
                    activity.dismissLoading()
                    Timber.e(throwable)
                    if (throwable is SocketTimeoutException) {
                        ToastHelper.showLongToast(R.string.tip_request_timeout)
                    } else {
                        ToastHelper.showLongToast(R.string.unknown_exception)
                    }
                })
    }

    protected var isOpenHDManage = false
    protected fun openHDManagerView(devID: String, countNum: String) {
        if (!isOpenHDManage) {
            isOpenHDManage = true
            val intent = Intent(activity, HdManageActivity::class.java)
            intent.putExtra("count", countNum)
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devID)
            fragment?.let {
                it.startActivityForResult(intent, AppConstants.REQUEST_CODE_HD_FORMAT)
                true
            } ?: let {
                activity.startActivityForResult(intent, AppConstants.REQUEST_CODE_HD_FORMAT)
            }
        }
    }

    fun resetOpenHDManageValue() {
        isOpenHDManage = false
    }

    /**
     * 取消继续加载
     */
    fun cancel() {
        mLoginDeviceDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    private fun startNasActivity(device: DeviceBean, isComeCircle: Boolean = false) {
        val intent = Intent(activity, V2NasDetailsActivity::class.java)
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, device.id)
        intent.putExtra("isComeCircle", isComeCircle)
        activity.startActivity(intent)
        Logger.LOGD(this, "openNasDetails :${device.id}")
    }
}