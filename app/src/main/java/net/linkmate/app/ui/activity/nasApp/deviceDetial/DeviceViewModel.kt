package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.content.Context
import android.content.Intent
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceDetailRepository
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.sdvn.common.internet.protocol.ShareCode
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.CustomLoaderStateListener
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.model.oneos.api.BaseAPI
import net.sdvn.nascommon.utils.SPUtils

/**
 * @author Raleigh.Luo
 * date：20/7/30 11
 * describe：
 */
class DeviceViewModel : BriefCacheViewModel() {
    private val repository: DeviceDetailRepository = DeviceDetailRepository()
    var position = 0
    var deviceBoundType = 0
    lateinit var device: DeviceBean

    /***－－设备简介－－－－－－－－－－**/
    private val startGetDeviceBrief = MutableLiveData<String>()
    fun startGetDeviceBrief(deviceId: String) {
        startGetDeviceBrief.value = deviceId
    }

    val deviceBrief = startGetDeviceBrief.switchMap {
        BriefRepo.getBriefLiveData(it, BriefRepo.FOR_DEVICE)
    }


    //显示加载进度条
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    //取消加载进度条,用户手动取消请求
    private val _cancelRequest: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val cancelRequest: LiveData<Boolean> = _cancelRequest
    fun cancelRequest() {
        _isLoading.value = false
        _cancelRequest.value = true
        _cancelRequest.value = false
    }

    private val _toFinishActivity: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val toFinishActivity: LiveData<Boolean> = _toFinishActivity

    private val _toBackPress: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val toBackPress: LiveData<Boolean> = _toBackPress
    val refreshDeviceName: MutableLiveData<String> = MutableLiveData()
    val regexMask = Regex(MyConstants.REGEX_MASK)

    /*****二级弹框dialog问题 是否显示或关闭 切换windowbackgroud***************************/
    private val _isSecondaryDialogShow: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isSecondaryDialogShow: LiveData<Boolean> = _isSecondaryDialogShow
    //二级弹框dialog,用于加深多级弹框透明背景色
    /**
     * 设置二级弹框dialog是否已经显示, 用于加深多级弹框透明背景色
     */
    fun setSecondaryDialogShow(isDialogShow: Boolean) {
        _isSecondaryDialogShow.value = isDialogShow
    }

    /**
     * 刷新备注名
     */
    fun refreshMarkName() {
        val deviceModel = SessionManager.getInstance().getDeviceModel(device.id)
        /***备注名**********************************************************************/
        deviceModel?.let {
            //待同意的统一显示为设备名
            //是否等待同意，需使用hardData
            val isPendingAccept = device.isPendingAccept
            //待同意的统一显示为设备名
            if (device.isNas && !isPendingAccept && SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                refreshDeviceName.postValue(deviceModel.devName)
                it.devNameFromDB.subscribe(object : Consumer<String?> {
                    override fun accept(markname: String?) {
                        refreshDeviceName.postValue(markname)
                    }
                })
            } else {
                refreshDeviceName.postValue(device.name)
            }
        }
    }

    //加载框是否可被取消
    var isLoadingCancelable = true
    fun setLoadingStatus(isLoading: Boolean, isLoadingCancelable: Boolean = true) {
        this.isLoadingCancelable = isLoadingCancelable
        if (_isLoading.value == null || _isLoading.value != isLoading) {//避免重复并发
            _isLoading.value = isLoading
        }
    }

    /**
     * 检查格式化设备
     */
    val checkDeviceFormat: MutableLiveData<Int> = MutableLiveData<Int>()

    /**
     * @param function 对应FunctionHelper的功能值，用于页面跳转
     */
    fun checkDeviceFormat(function: Int) {
        checkDeviceFormat.value = function
    }

    /**
     * Activity finish
     */
    fun toFinishActivity() {
        _toFinishActivity.value = true
    }

    /**
     * 返回
     */
    fun toBackPress() {
        _toBackPress.value = true
    }

    fun init(position: Int, deviceBoundType: Int, device: DeviceBean) {
        this.position = position
        this.device = device
        this.deviceBoundType = deviceBoundType
        refreshMarkName()
    }

    /**
     * 获取设备分享码
     */
    fun getDeviceShareCode(backCall: androidx.arch.core.util.Function<ShareCode, Void>) {
        device.getHardData()?.let {
            if (it.enableshare) {
                repository.getDeviceShareCode(device.getId(), backCall)
            }
        }
    }

    /**
     * 开关 设备分享功能
     */
    fun savedEnableShareState(isChecked: Boolean, backCall: androidx.arch.core.util.Function<Boolean, Void?>) {
        repository.savedEnableShareState(device.getId(), isChecked, mStateListener, Function {
            if (it) device.getHardData()?.setEnableshare(isChecked)
            backCall.apply(it)
            null
        })
    }

    /**
     * 开关 设备需要验证
     */
    fun savedScanConfirmState(isChecked: Boolean, backCall: androidx.arch.core.util.Function<Boolean, Void?>) {
        repository.savedScanConfirmState(device.getId(), isChecked, mStateListener, Function {
            if (it) device.getHardData()?.setScanconfirm(isChecked)
            backCall.apply(it)
            null
        })
    }

    override fun dispose() {
        super.dispose()
        olderHttpRequest?.cancel()
        olderHttpRequest = null
    }

    //旧版API请求
    private var olderHttpRequest: BaseAPI? = null
    val mStateListener: CustomLoaderStateListener = object : CustomLoaderStateListener {
        override fun onLoadComplete() {
            setLoadingStatus(false)
        }

        override fun onLoadStart(http: BaseAPI) {//其它请求方式
            olderHttpRequest = null
            olderHttpRequest = http
        }

        override fun onLoadStart(disposable: Disposable?) {
            disposable?.let {
                addDisposable(disposable)
            }
            setLoadingStatus(true)
        }

        override fun isCanceled(): Boolean {
            return _isLoading.value == false
        }

        override fun onLoadError() {
            setLoadingStatus(false)
        }

    }

    fun sendRemoveDevBroadcast(context: Context) {
        device.id.let {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(AppConstants.LOCAL_BROADCAST_REMOVE_DEV).apply {
                putExtra(AppConstants.SP_FIELD_DEVICE_ID, it)
            })
        }
    }

}