package net.sdvn.nascommon.model


import io.weline.repo.SessionCache
import io.weline.repo.api.GroupUserPerm
import io.weline.repo.files.data.SharePathType
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.phone.LocalFile
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.utils.EmptyUtils
import net.sdvn.nascommonlib.R
import java.io.File
import java.util.*

/**
 * Created by gaoyun@eli-tech.com on 2016/1/20.
 */
object FileManageItemGenerator {
    private var OPT_BASE_ID = 0x10000000
    private val TAG = FileManageItemGenerator::class.java.simpleName

    private val OPT_COPY = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_copy,
        R.drawable.btn_opt_copy_pressed,
        R.string.copy_file,
        FileManageAction.COPY
    )
    private val OPT_MOVE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_move,
        R.drawable.btn_opt_move_pressed,
        R.string.move_file,
        FileManageAction.MOVE
    )
    private val OPT_DELETE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_delete,
        R.drawable.btn_opt_delete_pressed,
        R.string.delete_file,
        FileManageAction.DELETE
    )
    private val OPT_RENAME = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_rename,
        R.drawable.btn_opt_rename_pressed,
        R.string.rename_file,
        FileManageAction.RENAME
    )
    private val OPT_DOWNLOAD = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_download,
        R.drawable.btn_opt_download_pressed,
        R.string.download_file,
        FileManageAction.DOWNLOAD
    )
    private val OPT_UPLOAD = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_upload,
        R.drawable.btn_opt_upload_pressed,
        R.string.upload_file,
        FileManageAction.UPLOAD
    )
    private val OPT_ENCRYPT = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_encrypt,
        R.drawable.btn_opt_encrypt_pressed,
        R.string.encrypt_file,
        FileManageAction.ENCRYPT
    )
    private val OPT_DECRYPT = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_decrypt,
        R.drawable.btn_opt_decrypt_pressed,
        R.string.decrypt_file,
        FileManageAction.DECRYPT
    )
    private val OPT_ATTR = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_attr,
        R.drawable.btn_opt_attr_pressed,
        R.string.attr_file,
        FileManageAction.ATTRIBUTES
    )
    private val OPT_CLEAN = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_delete,
        R.drawable.btn_opt_delete_pressed,
        R.string.clean_recycle_file,
        FileManageAction.CLEAN_RECYCLE
    )
    private val OPT_SHARE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_share,
        R.drawable.btn_opt_share_pressed,
        R.string.tab_share,
        FileManageAction.SHARE
    )
    private val OPT_CHMOD = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_chmod,
        R.drawable.btn_opt_chmod_pressed,
        R.string.chmod_file,
        FileManageAction.CHMOD
    )
    private val OPT_MORE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_more,
        R.drawable.btn_opt_more_pressed,
        R.string.more,
        FileManageAction.MORE
    )
    private val OPT_EXTRACT = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_extract,
        R.drawable.btn_opt_extract_pressed,
        R.string.extract_file,
        FileManageAction.EXTRACT
    )
    private val OPT_ARCHIVE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_archiver,
        R.drawable.btn_opt_extract_pressed,
        R.string.archiver_file,
        FileManageAction.ARCHIVER
    )
    private val OPT_BACK = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.btn_opt_back,
        R.drawable.btn_opt_back_pressed,
        R.string.title_back,
        FileManageAction.BACK
    )
    private val OPT_BT_GEN = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.icon_bt_white,
        R.drawable.icon_bt_white,
        R.string.fast_transfer,
        FileManageAction.TORRENT_CREATE
    )
    private val OPT_FAVORITE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.icon_favorite_w_20dp,
        R.drawable.icon_favorite_w_20dp,
        R.string.favorite,
        FileManageAction.FAVORITE
    )
    private val OPT_UNFAVORITE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.icon_unfavorite_w_20dp,
        R.drawable.icon_unfavorite_w_20dp,
        R.string.unfavorite,
        FileManageAction.UNFAVORITE
    )

    //    private static FileManageItem OPT_SHAING = new FileManageItem(OPT_BASE_ID++, R.drawable.btn_opt_share, R.drawable.btn_opt_share_pressed, R.string.opt_sharing, FileManageAction.SHARING);
    private val OPT_RESTORE = FileManageItem(
        OPT_BASE_ID++,
        R.drawable.icon_opt_file_restore,
        R.drawable.icon_opt_file_restore,
        R.string.restore,
        FileManageAction.RESTORE_RECYCLE
    );

    //文件浏览列表的选择
    fun generate(
        fileType1: OneOSFileType,
        selectedList: List<DataFile>,
        loginSession: LoginSession?,
        isGroup: Boolean = false
    ): ArrayList<FileManageItem>? {
        if (EmptyUtils.isEmpty(selectedList)) {
            return null
        }
        val mOptItems = ArrayList<FileManageItem>()
        var uid: Int = -10
        var isAdmin = false
        var isV5 = false
        var isBtAvailable = false
        var isEn = false
        var isNasV3 = false
        var favoriteId = 0
        var isEnableUserSpace = true
        var isAndroidTVNasV3 = false
        loginSession?.userInfo?.let {
            uid = it.uid ?: -1
            isAdmin = it.admin == 1
            favoriteId = it.favoriteId
        }

        val fileType = if (fileType1 == OneOSFileType.SAFE) {
            OneOSFileType.PRIVATE
        } else {
            fileType1
        }

        var isWriteable = fileType == OneOSFileType.PRIVATE
        loginSession?.let {
            isNasV3 = SessionCache.instance.isNasV3(it.id ?: "")
            isV5 = it.isV5
            SessionManager.getInstance().getDeviceModel(it.id)?.let { model ->
                isAdmin = model.hasAdminRights()
                isBtAvailable = model.isBtServerAvailable
                isEn = model.isInNetProvide
                isAndroidTVNasV3 = UiUtils.isAndroidTV(model.devClass) && isNasV3
            }
            isWriteable = PathTypeCompat.hasWriteablePerm(fileType, it.userInfo?.permissions)
            isEnableUserSpace = !(isNasV3 && it.userInfo?.isGuest ?: false)
        }
        //回收站
        if (fileType == OneOSFileType.RECYCLE) {
            mOptItems.add(OPT_MOVE)
            mOptItems.add(OPT_DELETE)
            mOptItems.add(OPT_CLEAN)
            if (isNasV3) {
                mOptItems.add(OPT_RESTORE)
            }
            var allOPTRestore = 0b0001
            for (file in selectedList) {
                val size = file.getPath().split(File.separator).size
                if (size != if (isAndroidTVNasV3) {
                        4
                    } else {
                        3
                    }
                ) {
                    allOPTRestore = allOPTRestore.xor(1)
                }
                if (allOPTRestore == 0) {
                    mOptItems.remove(OPT_CLEAN)
                    if (isNasV3) {
                        mOptItems.remove(OPT_RESTORE)
                    }
                    break
                }
            }
            val count = selectedList.size
            if (count == 1) {
                mOptItems.add(OPT_ATTR)
            }

        } else {
            //公共目录 不能分享

//            if (/*isAdmin ||*/ OneOSFileType.isDir(fileType)) {
//            mOptItems.add(OPT_SHARE)
            //                mOptItems.add(OPT_SHAING);
//            }
            //保险箱目录 不能分享
            if (fileType1 != OneOSFileType.SAFE && fileType1 != OneOSFileType.GROUP)
                mOptItems.add(OPT_SHARE)

            mOptItems.add(OPT_DOWNLOAD)
            if (!isNasV3 || (isNasV3 && isWriteable))
                mOptItems.add(OPT_MOVE)
            if (isEnableUserSpace)
                mOptItems.add(OPT_COPY)
            if (!isNasV3 || (isNasV3 && isWriteable))
                mOptItems.add(OPT_DELETE)

            val count = selectedList.size
            if (count == 1) {
                val file = selectedList[0] as OneOSFile
                if (isBtAvailable && fileType != OneOSFileType.EXTERNAL_STORAGE && isEn && isAdmin)
                    mOptItems.add(OPT_BT_GEN)
                if (file.isDirectory()) {
                    if (!isNasV3 || (isNasV3 && isWriteable))
                        mOptItems.add(OPT_RENAME)
                } else {
                    //                    mOptItems.add(OPT_MORE);
//                    if (!file.isDirectory() && file.isOwner(uid)
//                            && fileType != OneOSFileType.PUBLIC && !isV5) {    //加密解密
//                        if (file.isEncrypt()) {
//                            mOptItems.add(OPT_DECRYPT)
//                        } else {
//                            mOptItems.add(OPT_ENCRYPT)
//                        }
//                    }
                    val canExtract = !UiUtils.isM8(
                        SessionManager.getInstance()
                            .getDeviceModel(loginSession?.id)?.devClass ?: 0
                    )
                    if (!file.isDirectory() && file.isExtract) {
                        mOptItems.add(OPT_EXTRACT)
                    }
                    if (!file.isDirectory()) {
                        if (!isNasV3 || (isNasV3 && isWriteable))
                            mOptItems.add(OPT_RENAME)
                    }
                    //                    mOptItems.add(OPT_CHMOD);
                }
                mOptItems.add(OPT_ATTR)
            }
            if (isNasV3 && isEnableUserSpace && (fileType == OneOSFileType.PUBLIC
                        || fileType == OneOSFileType.PRIVATE
                        || OneOSFileType.isDB(fileType))
            ) {
                if (favoriteId != 0) {
                    mOptItems.add(OPT_FAVORITE)
                    var allHaveFavorite = 0b0000
                    val filter = selectedList.filterIsInstance<OneOSFile>()
                    for (file in filter) {
                        if (!file.isFavorite()) {
                            allHaveFavorite = allHaveFavorite.or(1)
                        }
                        if (allHaveFavorite != 0) {
                            break
                        }
                    }
                    if (allHaveFavorite == 0) {
                        mOptItems.remove(OPT_FAVORITE)
                        mOptItems.add(OPT_UNFAVORITE)
                    }
                }
            }
            if (isNasV3 && fileType == OneOSFileType.FAVORITES) {
                mOptItems.add(OPT_UNFAVORITE)
            }
            if (isNasV3 && isEnableUserSpace && (fileType == OneOSFileType.PUBLIC
                        || fileType == OneOSFileType.PRIVATE
                        )
            ) {
                mOptItems.add(OPT_ARCHIVE)
            }

            var allRemoved = 0b0011
            for (file in selectedList) {
                if (!isAdmin && file is OneOSFile && (fileType == OneOSFileType.PUBLIC
                            || file.share_path_type == SharePathType.PUBLIC.type) && !file.isOwner(
                        uid
                    )
                ) {
                    mOptItems.remove(OPT_DELETE)
                    mOptItems.remove(OPT_RENAME)
                    mOptItems.remove(OPT_MOVE)
                    allRemoved = allRemoved.xor(1)
                }
                if (file.isDirectory() && !isNasV3) {
                    mOptItems.remove(OPT_DOWNLOAD)
                    //                    if (!BuildConfig.DEBUG)
                    //                    mOptItems.remove(OPT_SHARE);
                    //                    mOptItems.remove(OPT_SHAING);
                    allRemoved = allRemoved.xor(1 shl 1)

                }
                if (allRemoved == 0) {
                    break
                }
            }
        }
        if (fileType1 == OneOSFileType.SAFE) {
            mOptItems.remove(OPT_BT_GEN)
            mOptItems.remove(OPT_FAVORITE)
            mOptItems.remove(OPT_EXTRACT)
            mOptItems.remove(OPT_ARCHIVE)
        }
        return mOptItems
    }

    fun generateMore(
        fileType: OneOSFileType,
        selectedList: ArrayList<OneOSFile>,
        loginSession: LoginSession?
    ): ArrayList<FileManageItem>? {
        if (EmptyUtils.isEmpty(selectedList)) {
            return null
        }

        val mOptItems = ArrayList<FileManageItem>()
        val file = selectedList[0]
        var uid = 0
        if (loginSession != null) {
            uid = loginSession.userInfo?.uid!!
        }

        if (!file.isDirectory() && file.isOwner(uid) && fileType != OneOSFileType.PUBLIC) {    //加密解密
            if (file.isEncrypt()) {
                mOptItems.add(OPT_DECRYPT)
            } else {
                mOptItems.add(OPT_ENCRYPT)
            }
        }

        //        if (!file.isDirectory() && file.isExtract()) {
        //            mOptItems.add(OPT_EXTRACT);
        //        }

        if (!file.isDirectory()) {
            mOptItems.add(OPT_RENAME)
        }

        mOptItems.add(OPT_COPY)
        //        if (!file.isDirectory() && fileType != OneOSFileType.PUBLIC) {
        //            mOptItems.add(OPT_SHARE);
        //        }

        mOptItems.add(OPT_ATTR)

        //        mOptItems.add(OPT_BACK);
        return mOptItems
    }


    fun generate(
        fileType: LocalFileType,
        selectedList: ArrayList<LocalFile>
    ): ArrayList<FileManageItem>? {
        if (EmptyUtils.isEmpty(selectedList)) {
            return null
        }

        val mOptItems = ArrayList<FileManageItem>()
        mOptItems.add(OPT_COPY)
        mOptItems.add(OPT_MOVE)
        if (selectedList.size <= 1) {
            mOptItems.add(OPT_RENAME)
        }
        mOptItems.add(OPT_DELETE)

        var hasDir = false
        for (file in selectedList) {
            if (file.isDirectory()) {
                hasDir = true
                break
            }
        }
        if (!hasDir) {
            mOptItems.add(OPT_SHARE)
            mOptItems.add(OPT_UPLOAD)
        }
        mOptItems.add(OPT_ATTR)

        return mOptItems
    }


    //文件浏览列表的选择
    fun generate(
        fileType: OneOSFileType,
        selectedList: List<DataFile>,
        loginSession: LoginSession?,
        perm: Int
    ): ArrayList<FileManageItem>? {
        if (EmptyUtils.isEmpty(selectedList)) {
            return null
        }
        val mOptItems = ArrayList<FileManageItem>()
        var uid: Int = -10
        var isAdmin = false
        var isV5 = false
        var isBtAvailable = false
        var isEn = false
        var isNasV3 = false
        var favoriteId = 0
        var isEnableUserSpace = true
        var isAndroidTVNasV3 = false
        loginSession?.userInfo?.let {
            uid = it.uid ?: -1
            isAdmin = it.admin == 1
            favoriteId = it.favoriteId
        }
        var isWriteable = fileType == OneOSFileType.PRIVATE
        loginSession?.let {
            isNasV3 = SessionCache.instance.isNasV3(it.id ?: "")
            isV5 = it.isV5
            SessionManager.getInstance().getDeviceModel(it.id)?.let { model ->
                isAdmin = model.hasAdminRights()
                isBtAvailable = model.isBtServerAvailable
                isEn = model.isInNetProvide
                isAndroidTVNasV3 = UiUtils.isAndroidTV(model.devClass) && isNasV3
            }
            isWriteable = PathTypeCompat.hasWriteablePerm(fileType, it.userInfo?.permissions)
            isEnableUserSpace = !(isNasV3 && it.userInfo?.isGuest ?: false)
        }
        //回收站
        if (fileType == OneOSFileType.RECYCLE) {
            //   checkAdd(mOptItems,OPT_MOVE,perm,GroupUserPerm.PERM_MOVE)
            checkAdd(mOptItems, OPT_DELETE, perm, GroupUserPerm.PERM_DELETE)
            checkAdd(mOptItems, OPT_CLEAN, perm, GroupUserPerm.PERM_DELETE)

            if (isNasV3) {
                checkAdd(mOptItems, OPT_RESTORE, perm, GroupUserPerm.PERM_DELETE)
            }
            var allOPTRestore = 0b0001
            for (file in selectedList) {
                val size = file.getPath().split(File.separator).size
                if (size != if (isAndroidTVNasV3) {
                        4
                    } else {
                        3
                    }
                ) {
                    allOPTRestore = allOPTRestore.xor(1)
                }
                if (allOPTRestore == 0) {
                    mOptItems.remove(OPT_CLEAN)
                    if (isNasV3) {
                        mOptItems.remove(OPT_RESTORE)
                    }
                    break
                }
            }
            val count = selectedList.size
            if (count == 1) {
                checkAdd(mOptItems, OPT_ATTR, perm, GroupUserPerm.PERM_VIEW)

            }

        } else {
            //公共目录 不能分享

//            if (/*isAdmin ||*/ OneOSFileType.isDir(fileType)) {
//            mOptItems.add(OPT_SHARE)
            //                mOptItems.add(OPT_SHAING);
//            }
            //保险箱目录 不能分享
            if (fileType != OneOSFileType.SAFE && fileType != OneOSFileType.GROUP)
                checkAdd(mOptItems, OPT_SHARE, perm, GroupUserPerm.PERM_DOWN)
            checkAdd(mOptItems, OPT_DOWNLOAD, perm, GroupUserPerm.PERM_DOWN)



            if (!isNasV3 || (isNasV3 && isWriteable))
                checkAdd(mOptItems, OPT_MOVE, perm, GroupUserPerm.PERM_MOVE)
            if (isEnableUserSpace)
                checkAdd(mOptItems, OPT_COPY, perm, GroupUserPerm.PERM_COPY)

            if (!isNasV3 || (isNasV3 && isWriteable))
                checkAdd(mOptItems, OPT_DELETE, perm, GroupUserPerm.PERM_DELETE)

            val count = selectedList.size
            if (count == 1) {
                val file = selectedList[0] as OneOSFile
                if (isBtAvailable && /*fileType == OneOSFileType.PUBLIC &&*/ isEn && isAdmin)
                    checkAdd(mOptItems, OPT_BT_GEN, perm, GroupUserPerm.PERM_DOWN)
                if (file.isDirectory()) {
                    if (!isNasV3 || (isNasV3 && isWriteable))
                        checkAdd(mOptItems, OPT_RENAME, perm, GroupUserPerm.PERM_RENAME)

                } else {
                    //                    mOptItems.add(OPT_MORE);
//                    if (!file.isDirectory() && file.isOwner(uid)
//                            && fileType != OneOSFileType.PUBLIC && !isV5) {    //加密解密
//                        if (file.isEncrypt()) {
//                            mOptItems.add(OPT_DECRYPT)
//                        } else {
//                            mOptItems.add(OPT_ENCRYPT)
//                        }
//                    }
                    val canExtract = !UiUtils.isM8(
                        SessionManager.getInstance()
                            .getDeviceModel(loginSession?.id)?.devClass ?: 0
                    )
                    if (!file.isDirectory() && file.isExtract) {
                        checkAdd(mOptItems, OPT_EXTRACT, perm, GroupUserPerm.PERM_MKDIR)
                    }
                    if (!file.isDirectory()) {
                        if (!isNasV3 || (isNasV3 && isWriteable))
                            checkAdd(mOptItems, OPT_RENAME, perm, GroupUserPerm.PERM_RENAME)
                    }
                    //                    mOptItems.add(OPT_CHMOD);
                }
                checkAdd(mOptItems, OPT_ATTR, perm, GroupUserPerm.PERM_VIEW)

            }
            if (isNasV3 && isEnableUserSpace && (fileType == OneOSFileType.PUBLIC
                        || fileType == OneOSFileType.PRIVATE
                        || OneOSFileType.isDB(fileType))
            ) {
                if (favoriteId != 0) {

                    var allHaveFavorite = 0b0000
                    val filter = selectedList.filterIsInstance<OneOSFile>()
                    for (file in filter) {
                        if (!file.isFavorite()) {
                            allHaveFavorite = allHaveFavorite.or(1)
                        }
                        if (allHaveFavorite != 0) {
                            break
                        }
                    }
                    if (allHaveFavorite == 0) {
                        mOptItems.remove(OPT_FAVORITE)
                        checkAdd(mOptItems, OPT_UNFAVORITE, perm, GroupUserPerm.PERM_MKDIR)

                    }
                }
            }
            if (isNasV3 && fileType == OneOSFileType.FAVORITES) {
                checkAdd(mOptItems, OPT_UNFAVORITE, perm, GroupUserPerm.PERM_RENAME)

            }
            if (isNasV3 && isEnableUserSpace && (fileType == OneOSFileType.PUBLIC
                        || fileType == OneOSFileType.PRIVATE
                        )
            ) {
                checkAdd(mOptItems, OPT_ARCHIVE, perm, GroupUserPerm.PERM_MKDIR)

            }

            var allRemoved = 0b0011
            for (file in selectedList) {
                if (!isAdmin && file is OneOSFile && (fileType == OneOSFileType.PUBLIC
                            || file.share_path_type == SharePathType.PUBLIC.type) && !file.isOwner(
                        uid
                    )
                ) {
                    mOptItems.remove(OPT_DELETE)
                    mOptItems.remove(OPT_RENAME)
                    //  mOptItems.remove(OPT_MOVE)
                    allRemoved = allRemoved.xor(1)
                }
                if (file.isDirectory()) {
                    mOptItems.remove(OPT_DOWNLOAD)
                    //                    if (!BuildConfig.DEBUG)
                    //                    mOptItems.remove(OPT_SHARE);
                    //                    mOptItems.remove(OPT_SHAING);
                    allRemoved = allRemoved.xor(1 shl 1)

                }
                if (allRemoved == 0) {
                    break
                }
            }
        }

        return mOptItems
    }


    private fun checkAdd(
        itemList: ArrayList<FileManageItem>,
        fileManageItem: FileManageItem,
        permissionList: ArrayList<FileManageAction>
    ) {
        if (permissionList.contains(fileManageItem.action)) {
            itemList.add(fileManageItem)
        }
    }

    private fun checkAdd(
        itemList: ArrayList<FileManageItem>,
        fileManageItem: FileManageItem,
        perm: Int,
        opt: Int
    ) {
        if (perm and opt != 0) {
            itemList.add(fileManageItem)
        }
    }
}
