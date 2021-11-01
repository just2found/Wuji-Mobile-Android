package net.sdvn.nascommon.model

import io.weline.repo.data.model.PermissionsModel
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.model.oneos.OneOSFileType
import java.util.*


/**
 * Description:
 * @author  admin
 * CreateDate: 2021/5/19
 */
object PathTypeCompat {
    @JvmStatic
    fun getSharePathType(type: OneOSFileType): SharePathType {
        return when (type) {
            OneOSFileType.PUBLIC -> SharePathType.PUBLIC
            OneOSFileType.PRIVATE -> SharePathType.USER
            OneOSFileType.RECYCLE -> SharePathType.USER
            OneOSFileType.EXTERNAL_STORAGE -> SharePathType.EXTERNAL_STORAGE
            OneOSFileType.SAFE -> SharePathType.SAFE_BOX
            OneOSFileType.GROUP -> SharePathType.GROUP
            else -> {
                SharePathType.VIRTUAL
            }
        }
    }

    @JvmStatic
    fun getOneOSFileType(type: SharePathType): OneOSFileType {
        return getOneOSFileType(type.type)
    }

    @JvmStatic
    fun getOneOSFileType(type: Int): OneOSFileType {
        return when (type) {
            SharePathType.PUBLIC.type -> OneOSFileType.PUBLIC
            SharePathType.USER.type -> OneOSFileType.PRIVATE
            SharePathType.EXTERNAL_STORAGE.type -> OneOSFileType.EXTERNAL_STORAGE
            SharePathType.SAFE_BOX.type -> OneOSFileType.SAFE
            SharePathType.GROUP.type -> OneOSFileType.GROUP
            else -> {
                OneOSFileType.PRIVATE
            }
        }
    }

    @JvmStatic
    fun getAllStrPath(sharePathType: Int, tarPath: String?): String? {
        var result = tarPath
        when (sharePathType) {
            SharePathType.PUBLIC.type -> {
                //V5特殊处理
                val isStartsWithType = tarPath?.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)
                if (isStartsWithType == false) {
                    //没有public开头，前面加一个
                    result = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR.replace("/", "") + tarPath
                }
            }
            SharePathType.SAFE_BOX.type -> {
                val isStartsWithType = tarPath?.startsWith(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR)
                if (isStartsWithType == false) {
                    //没有public开头，前面加一个
                    result = OneOSAPIs.ONE_OS_SAFE_ROOT_DIR.replace("/", "") + tarPath
                }
            }
            SharePathType.EXTERNAL_STORAGE.type -> {
                val isStartsWithType = tarPath?.startsWith(OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR)
                if (isStartsWithType == false) {
                    //没有public开头，前面加一个
                    result = OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR.replace("/", "") + tarPath
                }
            }
            SharePathType.GROUP.type -> {
                val isStartsWithType = tarPath?.startsWith(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR)
                if (isStartsWithType == false) {
                    //没有public开头，前面加一个
                    result = OneOSAPIs.ONE_OS_GROUP_ROOT_DIR.replace("/", "") + tarPath
                }
            }

        }
        return result
    }

    @JvmStatic
    fun getSharePathType(filePath: String): Int {
        var share_path_type = SharePathType.USER.type
        if (filePath.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
            share_path_type = SharePathType.PUBLIC.type
        } else if (filePath.startsWith(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR)) {
            share_path_type = SharePathType.SAFE_BOX.type
        } else if (filePath.startsWith(OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR)) {
            share_path_type = SharePathType.EXTERNAL_STORAGE.type
        } else if (filePath.startsWith(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR)) {
            share_path_type = SharePathType.GROUP.type
        }
        return share_path_type
    }

    @JvmStatic
    fun getV5Path(srcPath: String): String? {
        return OneOSAPIs.getV5Path(srcPath)
    }

    fun hasWriteablePerm(fileType: OneOSFileType, permissions: List<PermissionsModel>?): Boolean {
        val sharePathType = getSharePathType(fileType)
        return when (sharePathType) {
            SharePathType.VIRTUAL -> {
                permissions?.find { it.isWriteable } != null
            }
            else -> {
                permissions?.find { it.sharePathType == sharePathType.type }?.isWriteable ?: false
            }
        }
    }

    fun getSharePathTypeArray(fileType: OneOSFileType): IntArray {
        return when (fileType) {
            OneOSFileType.PUBLIC -> {
                intArrayOf(SharePathType.PUBLIC.type)
            }
            OneOSFileType.PRIVATE -> {
                intArrayOf(SharePathType.USER.type)
            }
            OneOSFileType.RECYCLE -> {
                intArrayOf(SharePathType.USER.type)
            }
            OneOSFileType.EXTERNAL_STORAGE -> {
                intArrayOf(SharePathType.EXTERNAL_STORAGE.type)
            }
            OneOSFileType.GROUP -> {
                intArrayOf(SharePathType.GROUP.type)
            }
            else -> {
                intArrayOf(SharePathType.USER.type, SharePathType.PUBLIC.type)
            }
        }
    }

    @JvmStatic
    fun isAndroidTV(devId: String): Boolean {
        return UiUtils.isAndroidTV(
            SessionManager.getInstance().getDeviceModel(devId)?.devClass
                ?: 0
        )
    }

    @JvmStatic
    fun pathIsRoot(path: String?): Boolean {
        return (Objects.equals(
            OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR,
            path
        ) || Objects.equals(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, path))
    }

    @JvmStatic
    fun isNotEnablePath(deviceId: String, mNowType: Int, mNowPath: String?): Boolean {
        return (isAndroidTV(deviceId) && pathIsRoot(mNowPath)
                || (mNowType == io.weline.repo.files.data.SharePathType.EXTERNAL_STORAGE.type && pathIsRoot(
            mNowPath
        )))
    }

}