package net.linkmate.app.ui.activity.circle.circleDetail

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.libimageloader.CacheKeyGlideUrl
import io.weline.repo.data.model.GOsFile
import io.weline.repo.repository.V5SysInfoRepo
import net.linkmate.app.data.model.CircleDetail
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.manager.SDVNManager
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.ui.viewmodel.GenFileUrl
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.viewmodel.RxViewModel
import timber.log.Timber

/**
 * @author Raleigh.Luo
 * date：20/8/13 17
 * describe：
 */
class CircleDetialViewModel : BriefCacheViewModel() {
    //测试使用
//    val networkId = "844570959020131"
    var networkId: String = ""
        set(value) {
            isNomorlCircle = (NetsRepo.getOwnNetwork(value)?.userStatus ?: 0) == 0
            field = value
        }

    //是否正常圈子，非等待同意的圈子
    var isNomorlCircle = false
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _toFinishActivity: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val toFinishActivity: LiveData<Boolean> = _toFinishActivity

    private val _toBackPress: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val toBackPress: LiveData<Boolean> = _toBackPress
    fun setLoadingStatus(isLoading: Boolean) {
        if (_isLoading.value == null || _isLoading.value != isLoading) {//避免重复并发
            _isLoading.value = isLoading
        }
    }


    //取消加载进度条,用户手动取消请求
    private val _cancelRequest: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val cancelRequest: LiveData<Boolean> = _cancelRequest
    fun cancelRequest() {
        _isLoading.value = false
        _cancelRequest.value = true
        _cancelRequest.value = false
    }

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

    /*****请求获取圈子详情***************************************************/
    private val _startRequestCircleDetail: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val startRequestCircleDetail: LiveData<Boolean> = _startRequestCircleDetail

    //触发请求－
    fun startRequestCircleDetail() {
        _startRequestCircleDetail.value = true
    }

    val circleDetail: LiveData<CircleDetail.Circle> = startRequestCircleDetail.switchMap {
        CircleRepository().getCircleDetial(networkId, loaderStateListener = mStateListener)
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

    val mStateListener: HttpLoader.HttpLoaderStateListener = object : HttpLoader.HttpLoaderStateListener {
        override fun onLoadComplete() {
            setLoadingStatus(false)
        }

        override fun onLoadStart(disposable: Disposable?) {
            disposable?.let {
                addDisposable(disposable)
            }
            setLoadingStatus(true)
        }

        override fun onLoadError() {
            setLoadingStatus(false)
        }
    }

    //成功不关闭进度条
    val mUnDismissStateListener: HttpLoader.HttpLoaderStateListener = object : HttpLoader.HttpLoaderStateListener {
        override fun onLoadComplete() {
        }

        override fun onLoadStart(disposable: Disposable?) {
            disposable?.let {
                addDisposable(disposable)
            }
            setLoadingStatus(true)
        }

        override fun onLoadError() {
            setLoadingStatus(false)
        }
    }

    /*--onActivityResult 动态返回值-------------------------------------------------------------*/
    /**
     * 注意：因为共用的ViewModel,建议启动Activity时每个requestCode唯一
     * 1.可设置为FunctionHelper.function,如FunctionHelper.CIRCLE_MANANGER
     * 2.以requestCode来区分返回
     */
    private val _activityResult: MutableLiveData<ActivityResult> = MutableLiveData<ActivityResult>()
    val activityResult: LiveData<ActivityResult> = _activityResult
    fun updateActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (_activityResult.value == null) {
            _activityResult.value = (ActivityResult(requestCode, resultCode, data))
        } else {
            val result = _activityResult.value
            result?.requestCode = requestCode
            result?.resultCode = resultCode
            result?.data = data
            _activityResult.value = result
        }
    }

    /***－－圈子简介－－－－－－－－－－**/
    val startGetCircleBrief = MutableLiveData<String>()
    fun startGetCircleBrief(deviceId: String?) {
        if (startGetCircleBrief.value == null || startGetCircleBrief.value != deviceId) {
            startGetCircleBrief.value = deviceId
            if (!TextUtils.isEmpty(deviceId)) BriefManager.requestRemoteBrief(deviceId!!, BriefRepo.FOR_CIRCLE, BriefRepo.ALL_TYPE)
        }

    }

    val circleBrief = startGetCircleBrief.switchMap {
        if (TextUtils.isEmpty(it)) {
            MutableLiveData<List<BriefModel>>(null)
        } else {
            BriefRepo.getBriefLiveData(it, BriefRepo.FOR_CIRCLE)
        }
    }

    class ActivityResult(var requestCode: Int, var resultCode: Int, var data: Intent?)
}