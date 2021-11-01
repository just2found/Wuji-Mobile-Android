package net.linkmate.app.ui.nas

import io.reactivex.Observable
import io.reactivex.Observer
import io.weline.repo.api.TagCMD
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.Error
import io.weline.repo.data.model.User
import io.weline.repo.data.model.UserSpace
import io.weline.repo.files.constant.HttpErrorNo
import io.weline.repo.files.data.FileTag
import io.weline.repo.files.data.SharePathType
import io.weline.repo.repository.V5Repository
import net.linkmate.app.manager.SDVNManager
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.utils.LoginTokenUtil
import java.util.function.BiFunction

/**
 *
 * @Description: java类作用描述
 * @Author: todo2088
 * @CreateDate: 2021/3/6 18:32
 */
object V5RepositoryWrapper {
    interface Function<U, T> {
        fun apply(u: U, t: T)
    }

    fun <T> wrapper(deviceId: String, function: Function<String, String>, observer: Observer<BaseProtocol<T>>) {
        LoginTokenUtil.getLoginToken(object : LoginTokenUtil.TokenCallback {
            override fun success(token: String) {
                val devVip = SDVNManager.instance.getDevVip(deviceId)
                        ?: return observer.onNext(BaseProtocol<T>(false, error = Error(HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, "dev offline"), data = null))
                function.apply(devVip, token)
            }

            override fun error(protocol: GsonBaseProtocol) {
                observer.onNext(BaseProtocol<T>(false, error = Error(protocol.result, protocol.errmsg), data = null))
            }
        })
    }


//    override fun getUserList(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Users>>) {
//
//    }

    fun getUserInfo(deviceId: String, username: String, observer: Observer<BaseProtocol<User>>) {
        wrapper(deviceId, object : Function<String, String> {
            override fun apply(devVip: String, token: String) {
                V5Repository.INSTANCE().getUserInfo(deviceId, devVip, token, username, onNext = observer)
            }
        }, observer)

    }

    //    override fun addUser(deviceId: String, ip: String, token: String, username: String, password: String, admin: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun deleteUser(deviceId: String, ip: String, token: String, username: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun clearUser(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun updateUserPassword(deviceId: String, ip: String, token: String, username: String, password: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
    fun getUserSpace(deviceId: String, username: String, observer: Observer<BaseProtocol<UserSpace>>) {
        wrapper(deviceId, object : Function<String, String> {
            override fun apply(devVip: String, token: String) {
                V5Repository.INSTANCE().getUserSpace(deviceId, devVip, token, username, onNext = observer)
            }
        }, observer)
    }
//
//    override fun setUserSpace(deviceId: String, ip: String, token: String, username: String, space: Long, onNext: Observer<BaseProtocol<UserSpace>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun setUserMark(deviceId: String, ip: String, token: String, username: String, mark: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun setDeviceMark(deviceId: String, ip: String, token: String, markName: String?, markDesc: String?, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getDeviceMark(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<DataDevMark>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun setSysInfo(deviceId: String, ip: String, token: String, name: String, value: String, level: Int): Observable<BaseProtocol<Any>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getSysInfo(deviceId: String, ip: String, token: String, needSession: Boolean, name: String, level: Int): Observable<BaseProtocol<DataSysInfoItem>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getAllSysInfo(deviceId: String, ip: String, token: String, needSession: Boolean, level: Int): Observable<BaseProtocol<List<DataSysInfoItem>>> {
//        TODO("Not yet implemented")
//    }

    fun setPermission(deviceId: String, username: String, sharePathType: SharePathType, perm: Int): Observable<BaseProtocol<Any>> {
        val devVip = SDVNManager.instance.getDevVip(deviceId)
                ?: return Observable.just(BaseProtocol<Any>(false, error = Error(HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, "dev offline"), data = null))
        val token = LoginTokenUtil.getToken()
        return V5Repository.INSTANCE().setPermission(deviceId, devVip, token, username, sharePathType, perm)
    }

    //    override fun getFileList(deviceId: String, ip: String, token: String, action: String, params: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun cleanRecycleFile(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun optFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun optFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int): Observable<BaseProtocol<Any>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun copyOrMoveFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int, toDir: String, des_path_type: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun copyFile(deviceId: String, ip: String, token: String, share_path_type: Int, toDir: String, des_path_type: Int, path: List<String>, action: Int): Observable<BaseProtocol<Any>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun moveFile(deviceId: String, ip: String, token: String, share_path_type: Int, toDir: String, des_path_type: Int, path: List<String>, action: Int): Observable<BaseProtocol<Any>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun renameFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, newname: String, share_path_type: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun searchFile(deviceId: String, ip: String, token: String, params: JSONObject, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun extractFile(deviceId: String, ip: String, token: String, path: String, share_path_type: Int, todir: String, des_path_type: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun archiverFile(deviceId: String, ip: String, token: String, path: List<String>, share_path_type: Int, todir: String, des_path_type: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }

    fun tags(deviceId: String, observer: Observer<BaseProtocol<List<FileTag>>>) {
        wrapper(deviceId, object : Function<String, String> {
            override fun apply(devVip: String, token: String) {
                V5Repository.INSTANCE().tags(deviceId, devVip, token, observer = observer)
            }
        }, observer)
    }

//    fun fileOptTag(deviceId: String, cmd: TagCMD, tagId: Int, sharePathType: Int, paths: List<String>, observer: Observer<BaseProtocol<OptTagFileError>>) {
//        wrapper(deviceId, object : Function<String, String> {
//            override fun apply(devVip: String, token: String) {
//                V5Repository.INSTANCE().fileOptTag(deviceId, devVip, token, cmd, tagId, sharePathType, paths, observer = observer)
//            }
//        }, observer)
//    }

    //
//    override fun rebootOrHaltSystem(deviceId: String, ip: String, token: String, isPowerOff: Boolean, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
    fun getHDSmartInforSystem(deviceId: String, observer: Observer<BaseProtocol<Any>>) {
        wrapper(deviceId, object : Function<String, String> {
            override fun apply(devVip: String, token: String) {
                V5Repository.INSTANCE().getHDSmartInforSystem(deviceId, devVip, token, onNext = observer)
            }
        }, observer)
    }

    fun getTags(devId: String) {
        TODO("Not yet implemented")
    }


//
//    override fun getHDInforSystem(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun formatSystem(deviceId: String, ip: String, token: String, cmd: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun formatHDStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getSysStatus(ip: String): Observable<BaseProtocol<List<Int>>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun initDisk(deviceId: String, ip: String, token: String, force: Int): Observable<BaseProtocol<Any>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun queryDiskStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<DataDiskStatus>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getServiceStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<List<ServiceStatus>>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getNasVersion(deviceId: String, ip: String): Observable<BaseProtocol<NasVersion>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun openDiskPower(deviceId: String, ip: String, token: String, slot: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun closeDiskPower(deviceId: String, ip: String, token: String, slot: Int, force: Int, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getDiskInfo(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getDiskOperation(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun manageDisk(deviceId: String, ip: String, token: String, cmd: String, mode: String, force: Int, devices: List<String>, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getDiskManageStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getDiskPowerStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
//        TODO("Not yet implemented")
//    }


}
