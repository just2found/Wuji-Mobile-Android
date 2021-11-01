package net.linkmate.app.ui.activity.nasApp.deviceDetial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import libs.source.common.livedata.Resource
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.UpdateInfo
import net.sdvn.nascommon.model.oneos.api.sys.OneOSGetUpdateInfoAPI
import net.sdvn.nascommon.model.oneos.event.EventMsgManager
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.viewmodel.RxViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * @author Raleigh.Luo
 * date：20/7/23 17
 * describe：
 */
open class DeviceDetailViewModel: RxViewModel() {
    protected lateinit var mStateListener: HttpLoader.HttpLoaderStateListener

    fun init(mStateListener: HttpLoader.HttpLoaderStateListener) {
        this.mStateListener = mStateListener
    }

    var function: Int = 0

    /*---头部和底部样式---------------------------------------------------------*/
    private val _viewStatusParams: MutableLiveData<FunctionHelper.ViewStatusParams> = MutableLiveData<FunctionHelper.ViewStatusParams>()
    val viewStatusParams: LiveData<FunctionHelper.ViewStatusParams> = _viewStatusParams
    fun setViewStatusParams(params: FunctionHelper.ViewStatusParams) {
        _viewStatusParams.value = params
    }

    fun updateViewStatusParams(headerIcon: Int? = null,
                               headerTitle: String? = null,
                               headerDescribe: String? = null,
                               headBackButtonVisibility: Int? = null,
                               bottomAddTitle: String? = null,
                               bottomAddIsEnable: Boolean? = null,
                               bottomTitle: String? = null,
                               bottomIsFullButton: Boolean? = null,
                               bottomIsEnable: Boolean? = null) {
        if (_viewStatusParams.value == null) _viewStatusParams.value = FunctionHelper.ViewStatusParams()
        val params = _viewStatusParams.value
        headerIcon?.let {
            params?.headerIcon = headerIcon
        }
        headerTitle?.let {
            params?.headerTitle = headerTitle
        }
        headerDescribe?.let {
            params?.headerDescribe = headerDescribe
        }
        headBackButtonVisibility?.let {
            params?.headBackButtonVisibility = headBackButtonVisibility
        }
        bottomTitle?.let {
            params?.bottomTitle = bottomTitle
        }
        bottomIsFullButton?.let {
            params?.bottomIsFullButton = bottomIsFullButton
        }
        bottomIsEnable?.let {
            params?.bottomIsEnable = bottomIsEnable
        }
        bottomAddTitle?.let {
            params?.bottomAddTitle = bottomAddTitle
        }
        bottomAddIsEnable?.let {
            params?.bottomAddIsEnable = bottomAddIsEnable
        }
        _viewStatusParams.postValue(params)
    }

    val mUpdateInfo:MutableLiveData<Resource<UpdateInfo>> = MutableLiveData()
    val mUpdateInfoM8:MutableLiveData<net.sdvn.cmapi.UpdateInfo> = MutableLiveData()

    private var getUpdateInfoM8:Disposable?=null
    fun getUpdateInfoM8(deviceModel: DeviceModel){
         val t= Single.create(SingleOnSubscribe { emitter: SingleEmitter<net.sdvn.cmapi.UpdateInfo?> ->
            val deviceUpdateInfo = CMAPI.getInstance().getDeviceUpdateInfo(deviceModel.device!!.vip)
            emitter.onSuccess(deviceUpdateInfo!!)
        } as SingleOnSubscribe<net.sdvn.cmapi.UpdateInfo?>).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updateInfo: net.sdvn.cmapi.UpdateInfo? ->
                    if (updateInfo!!.result == Constants.CE_SUCC
                            && UiUtils.isNewVersion(updateInfo!!.version, updateInfo!!.newVersion)) {
                        mUpdateInfoM8.postValue(updateInfo)
                    }
                }) { t: Throwable? -> Timber.e(t) }
    }

    override fun onCleared() {
        super.onCleared()
        if(getUpdateInfoM8?.isDisposed==false)getUpdateInfoM8?.dispose()
    }
    /***
     * 升级固件信息
     */
    fun getUpdateInfo(infos: DeviceModel){
        SessionManager.getInstance().getLoginSession(infos.devId, object : GetSessionListener(false) {
            override fun onSuccess(url: String?, data: LoginSession) {
                val oneOSGetUpdateInfoAPI = OneOSGetUpdateInfoAPI(data)
                oneOSGetUpdateInfoAPI.setOnUpdateInfoListener(object : OneOSGetUpdateInfoAPI.OnUpdateInfoListener {
                    override fun onSuccess(url: String?, updateInfo: UpdateInfo?) {
                        if (updateInfo?.isNeedup == true) {
                            infos.loginSession?.oneOSInfo?.isNeedsUp = true
                            Observable.interval(100, 500, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.single())
                                    .subscribe(object : Observer<Long> {
                                        var count = 0
                                        var disposable: Disposable? = null

                                        override fun onSubscribe(d: Disposable) {
                                        }

                                        override fun onNext(aLong: Long) {
                                            if (EventMsgManager.instance.isReceive(infos.devId)) {
                                                mUpdateInfo.postValue(Resource.success(updateInfo))
                                                disposable?.dispose()
                                            } else {
                                                if (count % 4 == 0)
                                                    EventMsgManager.instance.startReceive(infos.devId)
                                                if (count >= 30) {
                                                    disposable?.dispose()
                                                    mUpdateInfo.postValue(Resource.error("subscribe failed", updateInfo))
                                                }
                                                count++
                                            }
                                        }

                                        override fun onError(e: Throwable) {
                                            Timber.e(e)
                                        }

                                        override fun onComplete() {

                                        }
                                    })
                        }
                    }

                    override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                        mUpdateInfo.postValue(Resource.error(errorMsg!!, null))
                    }

                    override fun onStart(url: String?) {
                    }
                })
                oneOSGetUpdateInfoAPI.get()
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                mUpdateInfo.postValue(Resource.error(errorMsg!!, null))
            }

            override fun onStart(url: String?) {
            }
        })
    }


}