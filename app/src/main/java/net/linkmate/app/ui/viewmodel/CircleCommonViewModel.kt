package net.linkmate.app.ui.viewmodel

import android.app.Activity
import android.content.Intent
import android.widget.PopupWindow
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.libimageloader.CacheKeyGlideUrl
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.DataCircleIntroduction
import io.weline.repo.data.model.GOsFile
import io.weline.repo.data.model.IconSize
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5SysInfoRepo
import net.linkmate.app.R
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.manager.SDVNManager
import net.linkmate.app.ui.activity.circle.EditCircleBriefActivity
import net.linkmate.app.ui.nas.files.photo.NasPhotosActivity
import net.linkmate.app.util.BottomPopupWindowUtil
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.FileInfoHolder
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.viewmodel.RxViewModel
import timber.log.Timber

/** 

Created by admin on 2020/10/27,15:51

 */
class CircleCommonViewModel : RxViewModel() {
    private val mV5SysInfoRepo: V5SysInfoRepo = V5SysInfoRepo()

    val liveDataCircleData = MediatorLiveData<DataCircleIntroduction>()

    fun loadCircleIntroduction(devId: String, ip: String? = null) {
        val vip = ip ?: SDVNManager.instance.getDevVip(devId)
        addDisposable(mV5SysInfoRepo.loadCircleIntroduction(devId, vip)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("loadCircleIntroduction success:$it")
                    if (it.result) {
                        liveDataCircleData.postValue(it.data)
                    }
                }, {
                    Timber.d("loadCircleIntroduction error:$it")
                }))
    }

    fun publishCircleIntroduction(devId: String, data: DataCircleIntroduction) {
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mV5SysInfoRepo.publishCircleIntroduction(devId, loginSession.ip, data)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : V5Observer<Any>(devId) {
                            override fun success(result: BaseProtocol<Any>) {
                                if (result.result) {
                                    liveDataCircleData.postValue(data)
                                }
                            }

                            override fun fail(result: BaseProtocol<Any>) {

                            }

                            override fun isNotV5() {

                            }

                            override fun retry(): Boolean {
                                return true
                            }
                        })
            }
        })
    }

    fun genCacheGlideUrl(prefix: String?, devId: String, gOsFile: GOsFile): CacheKeyGlideUrl? {
        val vip = SDVNManager.instance.getDevVip(devId)
        return GenFileUrl.genCacheGlideUrl(prefix, devId, gOsFile.type, gOsFile.path, ip = vip)
    }

    //缩略图
    fun genCacheGlideUrlTb(prefix: String?, devId: String, gOsFile: GOsFile, iconSize: IconSize? = null): CacheKeyGlideUrl? {
        val vip = SDVNManager.instance.getDevVip(devId)
        return GenFileUrl.genCacheGlideUrlTb(prefix, devId, gOsFile.type, gOsFile.path, ip = vip, iconSize = iconSize)
    }


    fun onEditResult(deviceId: String, requestCode: Int, data: Intent?, result: (Any?) -> Unit) {
        val introduction = liveDataCircleData.value
                ?: DataCircleIntroduction(0)
        when (requestCode) {
            REQUEST_CODE -> {
                val content = data?.getStringExtra("data")
                result(content)
                introduction.apply {
                    this.content = content
                    this.id += 1
                }
                publishCircleIntroduction(deviceId, introduction)
            }
            REQUEST_CODE_ICON -> {
                val retrieve = FileInfoHolder.getInstance().retrieve(FileInfoHolder.PIC)
                if (retrieve is List<*>) {
                    val osFile = retrieve.get(0) as? OneOSFile
                    if (osFile != null) {
                        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                            override fun onSuccess(url: String?, loginSession: LoginSession) {
                                result(GenFileUrl.genEliCacheTB(loginSession, osFile))
                            }
                        })

                        introduction.apply {
                            val currentTimeMillis = System.currentTimeMillis()
                            this.iconFile = GOsFile(osFile.share_path_type, osFile.getPath(),
                                    osFile.getName(), osFile.getSize(), osFile.md5, osFile.type, "$currentTimeMillis")
                            this.id += 1
                        }
                        publishCircleIntroduction(deviceId, introduction)
                    }
                }
            }
            REQUEST_CODE_BG -> {
                val retrieve = FileInfoHolder.getInstance().retrieve(FileInfoHolder.PIC)
                if (retrieve is List<*>) {
                    val osFile = retrieve.get(0) as? OneOSFile
                    if (osFile != null) {
                        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                            override fun onSuccess(url: String?, loginSession: LoginSession) {
                                result(GenFileUrl.genEliCacheTB(loginSession, osFile))
                            }
                        })
                        introduction.apply {
                            val currentTimeMillis = System.currentTimeMillis()
                            this.bgFile = GOsFile(osFile.share_path_type, osFile.getPath(),
                                    osFile.getName(), osFile.getSize(), osFile.md5, osFile.type, "$currentTimeMillis")
                            this.id += 1
                        }
                        publishCircleIntroduction(deviceId, introduction)
                    }
                }
            }
        }
    }

    fun showBottomDialog(activity: Activity, deviceId: String, ids: IntArray = intArrayOf(R.string.change_avatar,
            R.string.change_cover, R.string.edit_summary), result: (Intent, Int) -> Unit) {
        BottomPopupWindowUtil.showSelectDialog(activity, ids) { id: Int, popupWindow: PopupWindow ->
            when (id) {
                R.string.change_avatar -> {
                    //                            ToastUtils.showToast(R.string.change_circle_avatar)
                    NasPhotosActivity.startConfig(activity, deviceId, BriefRepo.FOR_CIRCLE, BriefRepo.PORTRAIT_TYPE, REQUEST_CODE_ICON)
                }
                R.string.change_cover -> {
                    //                            ToastUtils.showToast(R.string.change_circle_cover)
                    NasPhotosActivity.startConfig(activity, deviceId, BriefRepo.FOR_CIRCLE, BriefRepo.BACKGROUD_TYPE, REQUEST_CODE_BG)
                }
                R.string.edit_summary ->
                    result(Intent(activity, EditCircleBriefActivity::class.java)
                            .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId ), REQUEST_CODE)
            }
            popupWindow.dismiss()
        }
    }
    /***－－设备简介－－－－－－－－－－**/
    private val startGetDeviceBrief = MutableLiveData<String>()
    fun startGetDeviceBrief(deviceId: String) {
        if (startGetDeviceBrief.value == null || startGetDeviceBrief.value != deviceId) {
            startGetDeviceBrief.value = deviceId
            //请求远端数据，强制获取
            BriefManager.requestRemoteBrief(deviceId, BriefRepo.FOR_CIRCLE, BriefRepo.ALL_TYPE,true)
        }

    }

    val deviceBrief = startGetDeviceBrief.switchMap {
        BriefRepo.getBriefLiveData(it, BriefRepo.FOR_CIRCLE)
    }

    companion object {
        const val REQUEST_CODE = 1029
        const val REQUEST_CODE_ICON = 1030
        const val REQUEST_CODE_BG = 1031
    }


}

