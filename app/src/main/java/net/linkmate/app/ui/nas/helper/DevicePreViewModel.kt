package net.linkmate.app.ui.nas.helper

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chad.library.adapter.base.entity.SectionEntity
import io.weline.devhelper.DevTypeHelper
import io.weline.repo.SessionCache
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.ServiceItem
import io.weline.repo.files.data.SharePathType
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.linkmate.app.R
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.common.repo.DevicesRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.db.NasServiceKeeper
import net.sdvn.nascommon.db.objecbox.NasServiceItem
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.FileTypeItem
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession

/** 

Created by admin on 2020/7/31,19:33

 */
class DevicePreViewModel(app: Application) : AndroidViewModel(app) {

    fun getFileItems(devId: String?): List<SectionEntity<FileTypeItem>> {
        val types = mutableListOf<SectionEntity<FileTypeItem>>()
        FileTypeHelper.getFileTypes().forEach {
            types.add(SectionEntity(it))
        }
        return types
    }

    fun getDirs(devId: String?): List<SectionEntity<FileTypeItem>> {
        val types = mutableListOf<SectionEntity<FileTypeItem>>()
        types.add(SectionEntity(true, getString(R.string.storage)))
        FileTypeHelper.getDirs().forEach {
            types.add(SectionEntity(it))
        }
        return types
    }

    fun getMedia(devId: String?): List<SectionEntity<FileTypeItem>> {
        val types = mutableListOf<SectionEntity<FileTypeItem>>()
//        types.add(SectionEntity(true, getString(R.string.media_library)))
        FileTypeHelper.getMedia().forEach {
            types.add(SectionEntity(it))
        }
        return types
    }

    fun getTransmissionItems(devId: String?): List<SectionEntity<FileTypeItem>> {
        val types = mutableListOf<SectionEntity<FileTypeItem>>()
        types.add(SectionEntity(true, getString(R.string.nav_title_transfer)))
        val transfer = FileTypeItem(
            R.string.transfer,
            R.drawable.icon_msg_trans, 0, type_transfer
        )
        types.add(SectionEntity(transfer))
        val fileShare = FileTypeItem(
            R.string.file_share,
            R.drawable.icon_msg_share_v2, 0, type_file_share
        )
        types.add(SectionEntity(fileShare))
        val torrentsTransfer = FileTypeItem(
            R.string.fast_transfer,
            R.drawable.icon_bt_blue, 0, type_torrents_transfer
        )
        val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
        if (deviceModel?.isOwner == true && deviceModel?.isBtServerAvailable)
            types.add(SectionEntity(torrentsTransfer))
        return types
    }

    fun getPagerItems(devId: String): List<FileTypeItem> {
        val isAndroidTV = FilesCommonHelper.isAndroidTV(devId)
        val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
        val isManager = deviceModel?.hasAdminRights()
            ?: false
        val enableUseSpace = deviceModel?.isEnableUseSpace ?: false
        val types = mutableListOf<FileTypeItem>()
        val file_recycle = FileTypeItem(
            R.string.root_dir_name_recycle,
            R.drawable.icon_file_recycle_23dp, 0, OneOSFileType.RECYCLE
        )
        if (enableUseSpace && !FilesCommonHelper.isAndroidTVAndNasV1(devId))
            types.add(file_recycle)

        val file_share = FileTypeItem(
            R.string.file_share,
            R.drawable.icon_file_share_24dp, 0, type_file_share
        )
        if (enableUseSpace && deviceModel?.isShareV2Available == true)
            types.add(file_share)

        if (!isAndroidTV && isManager) {
            val type_self_check = FileTypeItem(
                R.string.disk_self_check,
                R.drawable.icon_file_disk_check_24dp, 0, type_self_check
            )
            if (hasToolsServerTrue(type_self_check.flag))
                types.add(type_self_check)
        }
        if (isManager) {
            val offline_download = FileTypeItem(
                R.string.action_offline_download,
                R.drawable.icon_offline_download_24dp, 0, type_download_offline
            )
            if (hasToolsServerTrue(offline_download.flag))
                types.add(offline_download)
        }

        val type_torrents_transfer = FileTypeItem(
            R.string.fast_transfer,
            R.drawable.icon_file_lightransfer_24dp, 0, Companion.type_torrents_transfer
        )
        if (deviceModel?.isBtServerAvailable == true)
            types.add(type_torrents_transfer)

        val fileFavorites = FileTypeItem(
            R.string.favorites,
            R.drawable.icon_file_fav_23dp, 0, type_file_favorites
        )
        if (enableUseSpace && hasToolsServerTrue(fileFavorites.flag))
            types.add(fileFavorites)
        if (enableUseSpace && !isAndroidTV) {
            val fileSafebox = FileTypeItem(
                R.string.root_dir_name_safe_box,
                R.drawable.icon_file_safe_23dp, 0, type_safe_deposit_box
            )
            if (hasToolsServerTrue(fileSafebox.flag))
                types.add(fileSafebox)
            val external_storage = FileTypeItem(
                R.string.external_storage,
                R.drawable.icon_file_usb_storage_23dp, 0, OneOSFileType.EXTERNAL_STORAGE
            )
            if (hasToolsServerTrue(external_storage.flag))
                types.add(external_storage)
        }
        val deduplication = FileTypeItem(R.string.duplicate_removal, R.drawable.icon_file_dup_24dp, 0, type_duplicate_removal)
        if (enableUseSpace && hasToolsServerTrue(deduplication.flag))
            types.add(deduplication)

        val file_samb = FileTypeItem(
            R.string.samba,
            R.drawable.icon_file_samba_24dp, 0, type_file_samba
        )
        if (enableUseSpace && hasToolsServerTrue(file_samb.flag))//&& (!DevTypeHelper.isM3(deviceModel?.devClass) || isManager
        {
            types.add(file_samb)
        }
        val file_dlna = FileTypeItem(
            R.string.dlna,
            R.drawable.icon_file_dlna_24dp, 0, type_file_dlna
        )
        if (enableUseSpace && hasToolsServerTrue(file_dlna.flag) ) {   //&& (!DevTypeHelper.isM3(deviceModel?.devClass) || isManager)
            types.add(file_dlna)
        }

        val type_device_information = FileTypeItem(
            R.string.device_information,
            R.drawable.ic_device_information, 0, type_device_information
        )
        if (enableUseSpace && hasToolsServerTrue(type_device_information.flag)) {
            types.add(type_device_information)
        }
        return types
    }

    private fun switchServiceItem2FileTypeItem(list: List<NasServiceItem>): List<SectionEntity<FileTypeItem>>? {
        if (list.isNullOrEmpty()) {
            return null
        }
        val types = mutableListOf<SectionEntity<FileTypeItem>>()
        types.add(SectionEntity(true, getString(R.string.tool)))
        for (serviceItem in list) {
            val isOwner = SessionManager.getInstance().getDeviceModel(serviceItem.devId)?.isOwner == true
            val isAdmin = SessionManager.getInstance().getDeviceModel(serviceItem.devId)?.isAdmin?:false
            when (serviceItem.serviceId) {
                NasServiceKeeper.SERVICE_TYPE_FILEDUP -> {//去重
                    if (serviceItem.isServiceStatus)
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.deduplication,
                                    R.drawable.icon_file_dup_24dp, 0, type_duplicate_removal
                                )
                            )
                        )//
                }
                NasServiceKeeper.SERVICE_TYPE_OFFLINEDOWNLOAD -> {//offlineDownload
                    if (serviceItem.isServiceStatus && isOwner)
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.action_offline_download,
                                    R.drawable.icon_offline_download_24dp, 0, type_download_offline
                                )
                            )
                        )//
                }
                NasServiceKeeper.SERVICE_TYPE_DISKSELFCHECK -> {//diskSelfCheck
                    if (serviceItem.isServiceStatus && isOwner)
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.disk_self_check,
                                    R.drawable.icon_file_disk_check_24dp, 0, type_self_check
                                )
                            )
                        )//
                }
                NasServiceKeeper.SERVICE_TYPE_SAFEBOX -> {//保险箱
                    if (serviceItem.isServiceStatus) {
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.root_dir_name_safe_box,
                                    R.drawable.icon_file_safe_23dp, 0, type_safe_deposit_box
                                )
                            )
                        )//
                    }
                }
                NasServiceKeeper.SERVICE_TYPE_FAVORITE -> {//收藏夹
                    if (serviceItem.isServiceStatus) {
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.favorites,
                                    R.drawable.icon_file_fav_23dp, 0, type_file_favorites
                                )
                            )
                        )//
                    }
                }
                NasServiceKeeper.SERVICE_TYPE_USBWEBSTORAGE -> {//外部存储
                    if (serviceItem.isServiceStatus) {
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.external_storage,
                                    R.drawable.icon_file_usb_storage_23dp,
                                    0,
                                    OneOSFileType.EXTERNAL_STORAGE
                                )
                            )
                        )//
                    }
                }
                NasServiceKeeper.SERVICE_TYPE_SAMBA -> {//SAMBA
                    if (serviceItem.isServiceStatus) {
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.samba,
                                    R.drawable.icon_file_samba_24dp,
                                    0,
                                    type_file_samba
                                )
                            )
                        )//
                    }
                }
                NasServiceKeeper.SERVICE_TYPE_DLNA -> {//外部存储
                    if (serviceItem.isServiceStatus) {
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.dlna,
                                    R.drawable.icon_file_dlna_24dp,
                                    0,
                                    type_file_dlna
                                )
                            )
                        )//
                    }
                }

                NasServiceKeeper.SERVICE_TYPE_DEVICE_INFORMATION -> {//设备信息
                    if (serviceItem.isServiceStatus && (isOwner||isAdmin)) {
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.device_information,
                                    R.drawable.ic_device_information,
                                    0,
                                    type_device_information
                                )
                            )
                        )//
                    }
                }
                NasServiceKeeper.SERVICE_TYPE_GROUP -> {//外部存储
                    if (serviceItem.isServiceStatus) {
                        types.add(
                            SectionEntity(
                                FileTypeItem(
                                    R.string.group,
                                    R.drawable.icon_space_group,
                                    0,
                                    OneOSFileType.GROUP
                                )
                            )
                        )//
                    }
                }
            }
        }
        return if (types.size > 1) {
            types
        } else {
            null
        }
    }

    fun hasToolsServerTrue(tag: Any?): Boolean {
        val items = liveData.value ?: return false
        items.forEach {
            if (it.t?.flag == tag)
                return true
        }
        return false
    }


    fun hasToolsServer(tag: Any): Boolean {
        val items = liveData.value ?: return false
        items.forEach {
            if (it.t?.flag == tag)
                return true
        }
        return false
    }


    fun switchServiceItem2NasServiceItem(
        devId: String,
        list: List<ServiceItem>
    ): List<NasServiceItem> {
        val nasServiceItemList = mutableListOf<NasServiceItem>()
        for (serviceItem in list) {
            if (TextUtils.isEmpty(serviceItem.serviceName)) {
                continue
            }
            val nasServiceItem = NasServiceItem()
            nasServiceItem.devId = devId
            nasServiceItem.isServiceStatus = serviceItem.serviceStatus
            nasServiceItem.serviceId = serviceItem.serviceId
            nasServiceItem.serviceName = serviceItem.serviceName
            nasServiceItem.serviceType = serviceItem.serviceType
            nasServiceItemList.add(nasServiceItem)
        }
        return nasServiceItemList;
    }

    val liveData = MutableLiveData<List<SectionEntity<FileTypeItem>>?>()

    fun getToolItems(devId: String): LiveData<List<SectionEntity<FileTypeItem>>?> {
        val sectionEntityList = switchServiceItem2FileTypeItem(NasServiceKeeper.all(devId))
        if (!sectionEntityList.isNullOrEmpty()) {
            liveData.postValue(sectionEntityList)
        }

        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                if (loginSession == null) {
                    return
                }
                val observer = object : V5Observer<List<ServiceItem>>(loginSession.id ?: "") {
                    override fun success(result: BaseProtocol<List<ServiceItem>>) {
                        if (result.result) {
                            result.data?.let {
                                val list = switchServiceItem2NasServiceItem(devId, it)
                                NasServiceKeeper.insertOrUpdate(list)
                                liveData.postValue(switchServiceItem2FileTypeItem(list))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<List<ServiceItem>>) {
                    }

                    override fun isNotV5() {

                    }

                }
                V5Repository.INSTANCE().getServiceList(
                    loginSession.id
                        ?: "", loginSession.ip, LoginTokenUtil.getToken(), observer
                )

            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
            }
        })

        return liveData;
    }


    fun getToolItems(): List<SectionEntity<FileTypeItem>> {
        val types = mutableListOf<SectionEntity<FileTypeItem>>()
        types.add(SectionEntity(true, getString(R.string.tool)))
        types.add(
            SectionEntity(
                FileTypeItem(
                    R.string.action_offline_download,
                    R.drawable.ic_download_offline, 0, type_download_offline
                )
            )
        )//
//        types.add(SectionEntity(FileTypeItem(R.string.details_device,
//                R.drawable.ic_duplicate_removal, 0, type_duplicate_removal)))//
        types.add(
            SectionEntity(
                FileTypeItem(
                    R.string.disk_self_check,
                    R.drawable.ic_self_check, 0, type_self_check
                )
            )
        )//
//        types.add(SectionEntity(FileTypeItem(R.string.details_device,
//                R.drawable.ic_safe_deposit_box, 0, type_safe_deposit_box)))//
        return types
    }


    fun getMoreItems(devId: String?): List<SectionEntity<FileTypeItem>> {
        val types = mutableListOf<SectionEntity<FileTypeItem>>()
        types.add(SectionEntity(true, getString(R.string.more)))
        types.add(
            SectionEntity(
                FileTypeItem(
                    R.string.details_device,
                    R.drawable.icon_device_details, 0, type_details
                )
            )
        )
        if (devId != null) {
            val deviceModel = DevicesRepo.getDeviceModel(devId)
            if (deviceModel != null && !deviceModel.gainMBPUrl.isNullOrEmpty()) {
                types.add(
                    SectionEntity(
                        FileTypeItem(
                            R.string.receive_score,
                            R.drawable.icon_score, 0, type_receive_score,
                            deviceModel.gainMBPUrl
                        )
                    )
                )
            }
        }
        return types
    }


    fun getPreList(devId: String?): List<SectionEntity<FileTypeItem>> {
        val types = mutableListOf<SectionEntity<FileTypeItem>>()

        return types
    }

    fun noPerm(devId: String, type: OneOSFileType): Boolean {
        if (SessionCache.instance.isNasV3(devId)) {
            val permissions =
                SessionManager.getInstance().getLoginSession(devId)?.userInfo?.permissions
                    ?: return false
            return when (type) {
                OneOSFileType.PRIVATE, OneOSFileType.RECYCLE -> {
                    permissions.find { it.sharePathType == SharePathType.USER.type }?.perm == 0
                }
                OneOSFileType.PUBLIC -> {
                    permissions.find { it.sharePathType == SharePathType.PUBLIC.type }?.perm == 0
                }
                else -> {
                    (permissions.find { it.sharePathType == SharePathType.USER.type }?.perm == 0
                            && permissions.find { it.sharePathType == SharePathType.PUBLIC.type }?.perm == 0)
                }
            }
        }
        return false
    }

    companion object {
        const val type_details = "type_details"
        const val type_torrents_transfer = "type_torrents_transfer"
        const val type_transfer = "type_transfer"
        const val type_file_share = "type_file_share"
        const val type_receive_score = "type_receive_score"
        const val type_sys_msg = "type_sys_msg"
        const val type_device_LAN_access = "type_device_LAN_access"
        const val type_download_offline = "type_download_offline"
        const val type_duplicate_removal = "type_duplicate_removal"
        const val type_self_check = "type_self_check"
        const val type_safe_deposit_box = "type_safe_deposit_box"
        const val type_file_favorites = "type_file_favorites"
        const val type_usb_storage = "type_usb_storage"
        const val type_file_samba = "type_file_samba"
        const val type_file_dlna = "type_file_dlna"
        const val type_device_information = "type_device_information"
    }
}


fun AndroidViewModel.getString(resId: Int): String {
    return getApplication<Application>().getString(resId)
}