package io.weline.repo.repository

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.weline.repo.api.DefCopyOrMoveAction
import io.weline.repo.api.DefSysInfo
import io.weline.repo.data.model.*
import io.weline.repo.files.data.FileManageAction
import io.weline.repo.files.data.SharePathType
import libs.source.common.utils.DiskLruCacheHelper
import libs.source.common.utils.GsonUtils
import net.sdvn.common.internet.utils.LoginTokenUtil
import org.json.JSONArray
import timber.log.Timber
import java.util.*

/** 

Created by admin on 2020/10/28,14:17

 */
class V5SysInfoRepo() {

    fun loadCircleIntroduction(devId: String, ip: String?): Observable<BaseProtocol<DataCircleIntroduction>> {
        val nameKey = DefSysInfo.key_introduction_circle
        val levelKey = DefSysInfo.level_introduction_circle
        val key = "${devId}_${nameKey}_$levelKey"
        val observerLocal = Observable.create<BaseProtocol<DataCircleIntroduction>> {
            val diskCache = DiskLruCacheHelper.getDiskCache(key)
            Timber.d("diskCache-loadCircleIntroduction:$diskCache")
            if (!diskCache.isNullOrEmpty()) {
                val data = GsonUtils.decodeJSON(diskCache, DataSysInfoItem::class.java)
                if (!data?.value.isNullOrEmpty()) {
                    val decodeJSON = GsonUtils.decodeJSON(data!!.value, DataCircleIntroduction::class.java)
                    it.onNext(BaseProtocol(true, null, decodeJSON))
                } else {
                    it.onNext(BaseProtocol<DataCircleIntroduction>(false, null, null))
                }
            } else {
                it.onNext(BaseProtocol<DataCircleIntroduction>(false, null, null))
            }
            it.onComplete()
        }
        if (ip.isNullOrEmpty()) {
            return observerLocal
        }

        val observableNet = V5Repository.INSTANCE()
                .getSysInfo(devId, ip, "", false, nameKey, levelKey)
                .flatMap {
                    if (it.result) {
                        val value = it.data?.value
                        if (!value.isNullOrEmpty()) {
                            val decodeJSON = GsonUtils.decodeJSON(value, DataCircleIntroduction::class.java)
                            if (decodeJSON != null) {
                                val encodeJSON = GsonUtils.encodeJSON(it.data)
                                Timber.d("save $key:$encodeJSON")
                                DiskLruCacheHelper.putDiskCache(key, encodeJSON)
                                return@flatMap Observable.just(BaseProtocol(true, null, decodeJSON))
                            }
                        }
                    }
                    return@flatMap Observable.just(BaseProtocol<DataCircleIntroduction>(false, null, null))
                }
        return Observable.concat(listOf(observerLocal, observableNet))
    }

    fun publishCircleIntroduction(devId: String, ip: String, data: DataCircleIntroduction): Observable<BaseProtocol<Any>> {
        val nameKey = DefSysInfo.key_introduction_circle
        val levelKey = DefSysInfo.level_introduction_circle
        val mutableListOf = mutableListOf<Observable<BaseProtocol<Any>>>()
        val dir = DefSysInfo.dir_introduction_circle
        val key = "${devId}_${nameKey}_$levelKey"
        val globalType = SharePathType.GLOBAL.type
        mutableListOf.add(mkdir(devId, ip, globalType, dir))
        val iconFile = data.iconFile
        if (iconFile != null && iconFile.type != globalType) {
            val des = getDesFile(iconFile, globalType, dir, DefSysInfo.key_introduction_circle_icon_name)
            //删除原来的iconfile 目的 删除同名缩略图
            mutableListOf.add(delFile(devId, ip, des))
            //复制覆盖原来的文件
            mutableListOf.add(copyFileToGlobal(devId, ip, iconFile, des))
            data.iconFile = des
        }
        val bgFile = data.bgFile
        if (bgFile != null && bgFile.type != globalType) {
            val des = getDesFile(bgFile, globalType, dir, bgFile.name)
            mutableListOf.add(copyFileToGlobal(devId, ip, bgFile, des))
            data.bgFile = des
        }
        val mediaResources = data.mediaResources
        val newList = mutableListOf<GOsFile>()
        if (!mediaResources.isNullOrEmpty()) {
            mediaResources.forEach { file ->
                if (file != null && file.type != globalType) {
                    val des = getDesFile(file, globalType, dir, file.name)
                    mutableListOf.add(copyFileToGlobal(devId, ip, file, des))
                    newList.add(des)
                } else {
                    newList.add(file)
                }
            }
            data.mediaResources = newList
        }
        val sysInfo = V5Repository.INSTANCE().setSysInfo(devId, ip, LoginTokenUtil.getToken(),
                nameKey, Gson().toJson(data), levelKey)
        mutableListOf.add(sysInfo)
        return Observable.concat(mutableListOf)
                .takeLast(1)
                .doOnNext(Consumer {
                    if (it.result) {
                        DiskLruCacheHelper.putDiskCache(key, GsonUtils.encodeJSON(DataSysInfoItem(
                                0, levelKey, nameKey, 0, GsonUtils.encodeJSON(data))))
                    }
                })
    }

    fun loadDevIntroduction(devId: String, ip: String?): Observable<BaseProtocol<DataDevIntroduction>> {
        val nameKey = DefSysInfo.key_introduction_dev
        val levelKey = DefSysInfo.level_introduction_dev
        val key = "${devId}_${nameKey}_$levelKey"
        val observerLocal = Observable.create<BaseProtocol<DataDevIntroduction>> {
            val diskCache = DiskLruCacheHelper.getDiskCache(key)
            Timber.d("diskCache-loadDevIntroduction:$diskCache")
            if (!diskCache.isNullOrEmpty()) {
                val data = GsonUtils.decodeJSON(diskCache, DataSysInfoItem::class.java)
                if (!data?.value.isNullOrEmpty()) {
                    val decodeJSON = GsonUtils.decodeJSON<DataDevIntroduction>(data!!.value, DataDevIntroduction::class.java)
                    if (decodeJSON != null) {
                        it.onNext(BaseProtocol(true, null, decodeJSON))
                    } else {
                        it.onNext(BaseProtocol(false, null, null))
                    }
                } else {
                    it.onNext(BaseProtocol(false, null, null))
                }
            } else {
                it.onNext(BaseProtocol(false, null, null))
            }
            it.onComplete()
        }
        if (ip.isNullOrEmpty()) {
            return observerLocal
        }
        val observableNet = V5Repository.INSTANCE()
                .getSysInfo(devId, ip, "", false, nameKey, levelKey)
                .flatMap {
                    if (it.result) {
                        val value = it.data?.value
                        if (!value.isNullOrEmpty()) {
                            val decodeJSON = GsonUtils.decodeJSON(value, DataDevIntroduction::class.java)
                            if (decodeJSON != null) {
                                val encodeJSON = GsonUtils.encodeJSON(it.data)
                                Timber.d("saveDevIntroduction: $key : $encodeJSON")
                                DiskLruCacheHelper.putDiskCache(key, encodeJSON)
                                return@flatMap Observable.just(BaseProtocol(true, null, decodeJSON))
                            }
                        }
                    }
                    return@flatMap Observable.just(BaseProtocol<DataDevIntroduction>(false, null, null))
                }
        return Observable.concat(listOf(observerLocal, observableNet))
    }

    fun publishDeviceIntroduction(devId: String, ip: String, data: DataDevIntroduction): Observable<BaseProtocol<Any>> {
        val nameKey = DefSysInfo.key_introduction_dev
        val levelKey = DefSysInfo.level_introduction_dev
        val mutableListOf = mutableListOf<Observable<BaseProtocol<Any>>>()
        val key = "${devId}_${nameKey}_$levelKey"
        val dir = DefSysInfo.dir_introduction_dev
        val globalType = SharePathType.GLOBAL.type
        mutableListOf.add(mkdir(devId, ip, globalType, dir))
        val iconFile = data.iconFile
        if (iconFile != null && iconFile.type != globalType) {
            val des = getDesFile(iconFile, globalType, dir, DefSysInfo.key_introduction_dev_icon_name)
            mutableListOf.add(delFile(devId, ip, des))
            mutableListOf.add(copyFileToGlobal(devId, ip, iconFile, des))
            data.iconFile = des
        }
        val bgFile = data.bgFile
        if (bgFile != null && bgFile.type != globalType) {
            val des = getDesFile(bgFile, globalType, dir, bgFile.name)
            mutableListOf.add(copyFileToGlobal(devId, ip, bgFile, des))
            data.bgFile = des
        }
        val mediaResources = data.mediaResources
        val newList = mutableListOf<GOsFile>()
        if (!mediaResources.isNullOrEmpty()) {
            mediaResources.forEach { file ->
                if (file != null && file.type != globalType) {
                    val des = getDesFile(file, globalType, dir, file.name)
                    mutableListOf.add(copyFileToGlobal(devId, ip, file, des))
                    newList.add(des)
                } else {
                    newList.add(file)
                }
            }
            data.mediaResources = newList
        }
        val sysInfo = V5Repository.INSTANCE().setSysInfo(devId, ip, LoginTokenUtil.getToken(),
                nameKey, Gson().toJson(data), levelKey)
        mutableListOf.add(sysInfo)
        return Observable.concat(mutableListOf)
                .takeLast(1)
                .doOnNext {
                    if (it.result) {
                        DiskLruCacheHelper.putDiskCache(key, GsonUtils.encodeJSON(DataSysInfoItem(
                                0, levelKey, nameKey, 0, GsonUtils.encodeJSON(data))).also {
                            Timber.d("publishDeviceIntroduction: $it")
                        })
                    }
                }
    }

    fun copyFileToGlobal(devId: String, ip: String, src: GOsFile, des: GOsFile): Observable<BaseProtocol<Any>> {
        return V5Repository.INSTANCE().copyFile(devId, ip, LoginTokenUtil.getToken(),
                src.type, des.path, des.type, Collections.singletonList(src.path), DefCopyOrMoveAction.ACTION_REPLACE,false)
    }

    fun delFile(devId: String, ip: String, des: GOsFile): Observable<BaseProtocol<Any>> {
        return V5Repository.INSTANCE().optFileSync(devId, ip, LoginTokenUtil.getToken(),
                FileManageAction.DELETE_SHIFT.name.toLowerCase().replace("_", ""), JSONArray(Collections.singletonList(des.path)), des.type)
    }

    fun mkdir(devId: String, ip: String, type: Int, dir: String): Observable<BaseProtocol<Any>> {
        return V5Repository.INSTANCE().optFileSync(devId, ip, LoginTokenUtil.getToken(), FileManageAction.MKDIR.name.toLowerCase(), JSONArray(Collections.singleton(dir)), type)
    }

    companion object {
        val getDefaultDevIconFile: GOsFile by lazy {
            getDesFile(null, DefSysInfo.pathType, DefSysInfo.dir_introduction_dev, DefSysInfo.key_introduction_dev_icon_name)
        }
        val getDefaultCircleIconFile: GOsFile by lazy {
            getDesFile(null, DefSysInfo.pathType, DefSysInfo.dir_introduction_circle, DefSysInfo.key_introduction_circle_icon_name)
        }
        private fun getDesFile(bgFile: GOsFile?, type: Int, dir: String, name: String): GOsFile {
            return GOsFile(
                    type = type,
                    path = dir + name,
                    name = name,
                    size = bgFile?.size ?: 0,
                    md5 = bgFile?.md5,
                    ftype = bgFile?.ftype)
        }
    }
}