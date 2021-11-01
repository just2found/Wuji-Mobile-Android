package net.linkmate.app.ui.nas.helper

import android.content.Context
import io.weline.devhelper.DevTypeHelper
import io.weline.repo.SessionCache
import io.weline.repo.files.data.SharePathType
import net.linkmate.app.R
import net.linkmate.app.ui.nas.V5RepositoryWrapper
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.NasServiceKeeper
import net.sdvn.nascommon.db.objecbox.NasServiceItem
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.ToastHelper
import java.util.*

/** 
 *
 *Created by admin on 2020/8/20,16:05
 *
 */
object FilesCommonHelper {
    var devId: String? = null

    fun checkNetworkStatus(): Boolean {
        if (!NetworkStateManager.instance.isNetAvailable()) {
            ToastHelper.showToast(R.string.network_not_available)
            return true
        }
        if (!CMAPI.getInstance().isConnected) {
            ToastHelper.showToast(R.string.tip_wait_for_service_connect)
            return true
        }
        return false
    }


    fun getUploadPath(
        devId: String,
        context: Context,
        curPath: String?,
        fileType: OneOSFileType
    ): String {
        val defaultP = if (curPath.isNullOrEmpty()) {
            OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR
        } else {
            curPath
        }
        return if (OneOSFileType.isDB(fileType) && defaultP == OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR) {
            if (isAndroidTV(devId)) {
                defaultP
            } else {
                "${OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR}${context.resources.getString(
                    OneOSFileType.getTypeName(
                        fileType
                    )
                )}"
            }
        } else {
            defaultP
        }
    }

    fun getFileOperation(devId: String, oneOSFileType: OneOSFileType): Boolean {
        if (isAndroidTVAndNasV1(devId)) return true
        val serviceIds = listOfType(oneOSFileType)
        val list = NasServiceKeeper.all(devId)
        return !isEnable(list, serviceIds)

    }

    fun listOfType(oneOSFileType: OneOSFileType): List<Int> {
        return when (oneOSFileType) {
            OneOSFileType.PUBLIC -> listOf(NasServiceKeeper.SERVICE_TYPE_PUBLIC)
            OneOSFileType.PRIVATE -> listOf(NasServiceKeeper.SERVICE_TYPE_HOME)
            else -> {
                listOf(NasServiceKeeper.SERVICE_TYPE_PUBLIC, NasServiceKeeper.SERVICE_TYPE_HOME)
            }
        }
    }

    /**
     * @see FilesCommonViewModelTest#testIsEnable
     *
     * */
    fun isEnable(list: List<NasServiceItem>, serviceIds: List<Int>): Boolean {
        if (list.isEmpty()) {
            return true
        }
        var isEnable = 0
        for (item in list) {
            for (id in serviceIds) {
                if (item.serviceId == id) {
                    isEnable = isEnable.or(if (item.isServiceStatus) 1 else 0)
                    break
                }
            }
        }
        return isEnable != 0
    }

    fun isAndroidTVAndNasV1(devId: String): Boolean {
        if (isAndroidTV(devId) && !SessionCache.instance.isV5(
                devId ?: ""
            ) && !DevTypeHelper.isOneOSNas(
                SessionManager.getInstance().getDeviceModel(devId)?.devClass
                    ?: 0
            )
        ) {
            return true
        }
        return false
    }

    fun refreshFavoriteId(devId: String) {
        V5RepositoryWrapper.getTags(devId)
    }

    fun isAndroidTV(devId: String): Boolean {
        return UiUtils.isAndroidTV(
            SessionManager.getInstance().getDeviceModel(devId)?.devClass
                ?: 0
        )
    }

    fun pathIsRoot(path: String?): Boolean {
        return (Objects.equals(
            OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR,
            path
        ) || Objects.equals(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, path))
    }

    fun isNotEnablePath(deviceId: String, mNowType: Int, mNowPath: String?): Boolean {
        return (isAndroidTV(deviceId) && pathIsRoot(mNowPath)
                || (mNowType == SharePathType.EXTERNAL_STORAGE.type && pathIsRoot(mNowPath)))
    }

}