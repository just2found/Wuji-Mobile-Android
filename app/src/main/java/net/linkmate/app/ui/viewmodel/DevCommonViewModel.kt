package net.linkmate.app.ui.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.PopupWindow
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MediatorLiveData
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.constant.Type
import com.huantansheng.easyphotos.models.album.entity.Photo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.libimageloader.CacheKeyGlideUrl
import io.weline.repo.SessionCache.Companion.instance
import io.weline.repo.api.DefSysInfo
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.DataDevIntroduction
import io.weline.repo.data.model.GOsFile
import io.weline.repo.data.model.IconSize
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5SysInfoRepo
import libs.source.common.utils.UriUtils
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.manager.DevManager
import net.linkmate.app.manager.SDVNManager
import net.linkmate.app.ui.activity.dev.EditDevBriefActivity
import net.linkmate.app.ui.nas.files.configbrief.ConfigBriefCheckActivity
import net.linkmate.app.ui.nas.files.photo.NasPhotosActivity
import net.linkmate.app.util.BottomPopupWindowUtil
import net.linkmate.app.util.GlideEngine
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.DeviceInfoKeeper
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.oneos.FileInfoHolder
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.viewmodel.RxViewModel
import timber.log.Timber
import java.util.*

/** 

Created by admin on 2020/10/27,15:51

 */
class DevCommonViewModel : RxViewModel() {
    private val mV5SysInfoRepo: V5SysInfoRepo = V5SysInfoRepo()

    val liveDataIntroductionData = MediatorLiveData<DataDevIntroduction>()

    fun loadDevIntroduction(devId: String) {
        val vip = SDVNManager.instance.getDevVip(devId)
        addDisposable(mV5SysInfoRepo.loadDevIntroduction(devId, vip)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("loadDevIntroduction success:$it")
                    if (it.result) {
                        liveDataIntroductionData.postValue(it.data)
//                        Timber.d(IntroductionContentConverter().convertToDatabaseValue(it.data))
                        DeviceInfoKeeper.update(devId, it.data)
                        DevManager.getInstance().asyncUpdateGlobalModelToBean(devId)
                    }
                }, {
                    Timber.d("loadDevIntroduction error:$it")
                }))
    }

    fun loadDevIntroductionIcon(devId: String, vip: String?, callback: Callback<Pair<String, Any?>>) {
        addDisposable(mV5SysInfoRepo.loadDevIntroduction(devId, vip)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    callback.result(Pair(devId, if (it.result) {
                        val data = it.data
                        if (data?.iconFile != null) {
                            genCacheGlideUrlTb(data.iconFile!!.md5
                                    ?: data.iconFile!!.size.toString(), devId, data.iconFile!!)
                        } else {
                            genIntrDevIconCacheGlideUrl(devId, vip)
                        }
                    } else {
                        genIntrDevIconCacheGlideUrl(devId, vip)
                    }))
                }, {
                    callback.result(Pair(devId, genIntrDevIconCacheGlideUrl(devId, vip)))
                }))
    }

    fun publishDeviceIntroduction(devId: String, content: String? = null,
                                  iconFile: GOsFile? = null,
                                  bgFile: GOsFile? = null,
                                  callback: Callback<Result<Boolean>>) {
        val data = liveDataIntroductionData.value?.apply {
            if (!content.isNullOrEmpty()) {
                this.content = content
                this.id += 1
            }
            if (iconFile != null) {
                this.iconFile = iconFile
                this.id += 1
            }
            if (bgFile != null) {
                this.iconFile = iconFile
                this.id += 1
            }
        }
        if (data != null) {
            publishDeviceIntroduction(devId, data, callback)
        }

    }

    fun publishDeviceIntroduction(devId: String, data: DataDevIntroduction, callback: Callback<Result<Boolean>>? = null) {
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mV5SysInfoRepo.publishDeviceIntroduction(devId, loginSession.ip, data)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : V5Observer<Any>(devId) {

                            override fun success(result: BaseProtocol<Any>) {
                                if (result.result) {
                                    loadDevIntroduction(devId)
                                }
                                callback?.result(Result(result.result))
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

    fun genIntrDevIconCacheGlideUrl(devId: String, ip: String?): CacheKeyGlideUrl? {
        return GenFileUrl.genCacheGlideUrlTb("", devId, type = SharePathType.GLOBAL.type,
                path = "${DefSysInfo.dir_introduction_dev}${DefSysInfo.key_introduction_dev_icon_name}",
                ip = ip)
    }

    //加载原图
    fun genCacheGlideUrl(prefix: String?, devId: String, gOsFile: GOsFile): CacheKeyGlideUrl? {
        val vip = SDVNManager.instance.getDevVip(devId)
        return GenFileUrl.genCacheGlideUrl(prefix, devId, gOsFile.type, gOsFile.path, ip = vip)
    }

    //缩略图
    fun genCacheGlideUrlTb(prefix: String?, devId: String, gOsFile: GOsFile, iconSize: IconSize? = null): CacheKeyGlideUrl? {
        val vip = SDVNManager.instance.getDevVip(devId)
        return GenFileUrl.genCacheGlideUrlTb(prefix, devId, gOsFile.type, gOsFile.path, ip = vip, iconSize = iconSize)
    }

    private val mapUrls: HashMap<String, Any?> = HashMap()
    fun loadDevIntrIconAndGet(devId: String, vip: String?): Any? {
        return if (instance.isV5(devId)) {
            loadDevIntroductionIcon(devId, vip, Callback { pair ->
                val o = mapUrls[devId]
                if (o != pair.second) {
                    mapUrls[devId] = pair.second
                }
            })
            mapUrls[devId]
        } else {
            null
        }
    }

    fun onEditResult(deviceId: String, requestCode: Int, data: Intent?, result: (Any?) -> Unit) {
        val introduction = liveDataIntroductionData.value
                ?: DataDevIntroduction(0)
        when (requestCode) {
            REQUEST_CODE -> {
                val content = data?.getStringExtra("data")
                result(content)
                introduction.apply {
                    this.content = content
                    this.id += 1
                }
                publishDeviceIntroduction(deviceId, introduction)
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
                        publishDeviceIntroduction(deviceId, introduction)
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
                        publishDeviceIntroduction(deviceId, introduction)
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
                    NasPhotosActivity.startConfig(activity, deviceId, BriefRepo.FOR_DEVICE, BriefRepo.PORTRAIT_TYPE, REQUEST_CODE_ICON)
                }
                R.string.change_cover -> {
                    //                            ToastUtils.showToast(R.string.change_circle_cover)
                    NasPhotosActivity.startConfig(activity, deviceId, BriefRepo.FOR_DEVICE, BriefRepo.BACKGROUD_TYPE, REQUEST_CODE_BG)
                }
                R.string.edit_summary ->
                    result(Intent(activity, EditDevBriefActivity::class.java)
                            .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId), REQUEST_CODE)
            }
            popupWindow.dismiss()
        }
    }

    companion object {
        const val REQUEST_CODE = 1029
        const val REQUEST_CODE_ICON = 1030
        const val REQUEST_CODE_BG = 1031
    }
}

