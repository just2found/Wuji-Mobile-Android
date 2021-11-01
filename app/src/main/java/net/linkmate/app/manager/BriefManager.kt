package net.linkmate.app.manager

import android.text.TextUtils
import android.util.Log
import androidx.arch.core.util.Function
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.Brief
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.utils.Base64
import net.linkmate.app.base.MyApplication
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.utils.FileUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Raleigh.Luo
 * date：21/5/10 20
 * describe：
 */
object BriefManager {

    val briefOutputDirPath = if (MyApplication.getContext().externalCacheDir == null || TextUtils.isEmpty(MyApplication.getContext().externalCacheDir.absolutePath)) {//优先使用外部存储
        MyApplication.getContext().cacheDir.absolutePath + File.separator + "images"
    } else {
        MyApplication.getContext().externalCacheDir.absolutePath + File.separator + "images"
    }

    //保存文件夹,优先使用外部存储
    private val dowloadOutputDir = briefOutputDirPath + File.separator + "brief"

    /**
     * 优先使用本地缓存
     */
    fun requestRemoteWhenNoCacheBrief(deviceId: String, For: String, type: Int) {
        if (BriefRepo.getBrief(deviceId, For) == null) {
            requestRemoteBrief(deviceId, For, type)
        }
    }

    /**
     *   正在请求的数据
     */
    private val mRequestingRemote: CopyOnWriteArrayList<String> = CopyOnWriteArrayList()

    /**
     * 请求远程数据
     * @param isForcus 是否强制获取
     * 默认不强制获取，后台根据缓存时间戳对比是否返回数据
     * @param callBack 是否进行了请求回调, 主要是为了处理 非当前圈子的设备 ip为空的情况，切换为自己圈子后，ip正常，
     * 区分BriefCacheViewModel缓存一次的策略，前提是必须发起了请求
     */
    fun requestRemoteBrief(devId: String, For: String, type: Int, isForcus: Boolean = false, callBack: Function<Boolean, Void?>? = null) {
        val key = "$devId-$For-$type-$isForcus"
        //正在请求数据，不重复请求，强制更新优先级最高
        if (mRequestingRemote.contains(key) || mRequestingRemote.contains("$devId-$For-$type-true")) {
            callBack?.apply(true)
            return
        }
        checkLoginToken(Function {
            val ip = SDVNManager.instance.getDevVip(devId)
            if (it && !TextUtils.isEmpty(ip)) {
                callBack?.apply(true)
                val breif = BriefRepo.getBrief(devId, For)
                mRequestingRemote.add(key)
                V5Repository.INSTANCE().getBrief(devId, ip!!, LoginTokenUtil.getToken(),
                        type, For, if (isForcus) 0 else (breif?.backgroudTimeStamp
                        ?: 0), if (isForcus) 0 else (breif?.portraitTimeStamp
                        ?: 0), if (isForcus) 0 else (breif?.briefTimeStamp ?: 0))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : V5Observer<Brief>(devId) {
                            override fun onSubscribe(d: Disposable) {
                                super.onSubscribe(d)
                                DevManager.getInstance().addDisposable(d)
                            }

                            override fun success(result: BaseProtocol<Brief>) {
                                if (result.result) {
                                    if (result.data != null) {//返回为空，说明没有更改过简介
                                        saveBrief(deviceId, For, type, result.data!!, isForcus)
                                    }
                                }
                                mRequestingRemote.remove(key)
                            }

                            override fun fail(result: BaseProtocol<Brief>) {
                                mRequestingRemote.remove(key)
                                if (result.error?.msg == "HTTP 404 Not Found") {
                                    //不支持此功能，设备重置等情况，删除本地数据
                                    BriefRepo.removeBrief(deviceId, For)
                                }
                            }

                            override fun isNotV5() {
                                mRequestingRemote.remove(key)
                            }

                            override fun retry(): Boolean {
                                return true
                            }
                        })
            } else {
                callBack?.apply(false)
            }
            null
        })
    }


    private fun saveBrief(deviceId: String, For: String, type: Int, briefRemoteData: Brief, isForcus: Boolean) {
        DevManager.getInstance().addDisposable(Observable.create(ObservableOnSubscribe<BriefModel> {
            val brief: BriefModel = BriefModel(For = For)
            brief.deviceId = deviceId
            briefRemoteData.bg?.let {

                brief.backgroudPath = it.data
                brief.backgroudTimeStamp = it.update_at
            }
            briefRemoteData.avatar?.let {
                brief.portraitPath = it.data
                brief.portraitTimeStamp = it.update_at
            }
            briefRemoteData.text?.let {
                brief.brief = it.data
                brief.briefTimeStamp = it.update_at
            }
            val afterBrief = checkValueIsChanged(brief, type, isForcus)
            if (afterBrief == null) {
                it.onComplete()
            } else {
                it.onNext(afterBrief)
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //添加到数据库完成
                    BriefRepo.insertAsync(it)
                }, {
                }))

    }

    /**
     * 对比是否更改了数据
     */
    fun checkValueIsChanged(brief: BriefModel, type: Int, isForcusUpdate: Boolean): BriefModel? {
        val localBrief = BriefRepo.getBrief(brief.deviceId, brief.For)
        localBrief?.let {
            brief.autoIncreaseId = localBrief.autoIncreaseId
        }

        val checkPortraitValueIsKeepCache = { isForcusUseCache: Boolean ->
            val newTimeStamp = brief.portraitTimeStamp ?: 0L
            val olderTimeStamp = localBrief?.portraitTimeStamp ?: 0L

            val isUseCache = isForcusUseCache ||
                    (newTimeStamp != 0L && (newTimeStamp < olderTimeStamp || (newTimeStamp == olderTimeStamp && !isForcusUpdate)))
            if (isUseCache) {
                brief.portraitTimeStamp = localBrief?.portraitTimeStamp
                brief.portraitPath = localBrief?.portraitPath
            } else {
                val base64 = brief.portraitPath ?: ""
                if (!TextUtils.isEmpty(base64)) {//保存图片文件到本地
                    val path = String.format("%s/%s-%s-avatar", dowloadOutputDir, brief.deviceId, brief.For)
                    val result = decodeBase64ToFile(base64, path)
                    brief.portraitPath = path
                }
            }
            isUseCache
        }

        val checkBackgroudValueIsKeepCache = { isForcusUseCache: Boolean ->
            val newTimeStamp = brief.backgroudTimeStamp ?: 0L
            val olderTimeStamp = localBrief?.backgroudTimeStamp ?: 0L
            val isUseCache = isForcusUseCache ||
                    (newTimeStamp != 0L && (newTimeStamp < olderTimeStamp || (newTimeStamp == olderTimeStamp && !isForcusUpdate)))
            if (isUseCache) {
                brief.backgroudTimeStamp = localBrief?.backgroudTimeStamp
                brief.backgroudPath = localBrief?.backgroudPath
            } else {
                val base64 = brief.backgroudPath ?: ""
                if (!TextUtils.isEmpty(base64)) {//保存图片文件到本地
                    val path = String.format("%s/%s-%s-bg", dowloadOutputDir, brief.deviceId, brief.For)
                    val result = decodeBase64ToFile(base64, path)
                    brief.backgroudPath = path
                }
            }
            isUseCache
        }

        val checkBriefValueIsKeepCache = { isForcusUseCache: Boolean ->
            val newTimeStamp = brief.briefTimeStamp ?: 0L
            val olderTimeStamp = localBrief?.briefTimeStamp ?: 0L
            val isUseCache = isForcusUseCache ||
                    (newTimeStamp != 0L && (newTimeStamp < olderTimeStamp || (newTimeStamp == olderTimeStamp && !isForcusUpdate)))

            //0表示未设置过，TimeStamp 相同时使用缓存
            if (isUseCache) {
                brief.briefTimeStamp = localBrief?.briefTimeStamp
                brief.brief = localBrief?.brief
            }
            isUseCache
        }
        var isForcusPortraitUseCache = false
        var isForcusBackgroudUseCache = false
        var isForcusBriefUseCache = false
        when (type) {//非所有类型，只更新部分
            BriefRepo.BACKGROUD_TYPE -> {
                isForcusBriefUseCache = true
                isForcusPortraitUseCache = true
            }
            BriefRepo.PORTRAIT_TYPE -> {
                isForcusBriefUseCache = true
                isForcusBackgroudUseCache = true
            }
            BriefRepo.BRIEF_TYPE -> {
                isForcusBackgroudUseCache = true
                isForcusPortraitUseCache = true
            }
            BriefRepo.PORTRAIT_AND_BRIEF_TYPE -> {
                isForcusBackgroudUseCache = true
            }
            BriefRepo.BACKGROUD_AND_BRIEF_TYPE -> {
                isForcusPortraitUseCache = true
            }
        }
        val briefValueIsKeepCache = checkBriefValueIsKeepCache(isForcusBriefUseCache)
        val backgroudValueIsKeepCache = checkBackgroudValueIsKeepCache(isForcusBackgroudUseCache)
        val portraitValueIsKeepCache = checkPortraitValueIsKeepCache(isForcusPortraitUseCache)
        if (briefValueIsKeepCache && backgroudValueIsKeepCache && portraitValueIsKeepCache) {
            //所有数据均使用本地缓存，不需更改
            return null
        } else {
            return brief
        }
    }

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


    @Throws(IOException::class)
    fun decodeBase64ToFile(base64: String, path: String): Boolean? {
        val decodeFile = Base64.decode(base64, Base64.NO_WRAP)
        val input: InputStream = ByteArrayInputStream(decodeFile)
        return FileUtils.writeFile(input, path, true)
    }
}