package io.weline.repo.repository

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.repo.api.ApiService
import io.weline.repo.api.TagCMD
import io.weline.repo.data.model.*
import io.weline.repo.data.remote.*
import io.weline.repo.files.data.FileTag
import io.weline.repo.files.data.SharePathType
import io.weline.repo.net.RetrofitSingleton
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


/**
 * @author Raleigh.Luo
 * date：20/9/17 13
 * describe：
 * 采用懒汉式 优化资源
 */
class V5Repository : V5RepositoryInterface {
    private val userRemote: UserRemoteSource
    private val systemRemote: SystemRemoteSource
    private val fileRemote: FileRemoteSource
    private val serviceRemote: ServiceRemoteSource
    private val groupSpaceRemoteSource: GroupSpaceRemoteSource

    private constructor() {
        val apiService: ApiService =
            RetrofitSingleton.instance.getRetrofit().create(ApiService::class.java)
        userRemote = UserRemoteSource(apiService)
        systemRemote = SystemRemoteSource(apiService)
        fileRemote = FileRemoteSource(apiService)
        serviceRemote = ServiceRemoteSource(apiService)
        groupSpaceRemoteSource = GroupSpaceRemoteSource(apiService)
    }

    companion object {
        private var instance: V5RepositoryInterface? = null
            get() {
                if (field == null) {
                    field = V5Repository()
                }
                return field
            }

        fun INSTANCE(): V5RepositoryInterface {
            return instance!!
        }
    }

    override fun getUserList(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Users>>
    ) {
        userRemote.getUserList(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun addUser(
        deviceId: String, ip: String, token: String, username: String,
        password: String, admin: Int, onNext: Observer<BaseProtocol<Any>>
    ) {
        userRemote.addUser(deviceId, ip, token, username, password, admin)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getUserInfo(
        deviceId: String,
        ip: String,
        token: String,
        username: String,
        onNext: Observer<BaseProtocol<User>>
    ) {
        userRemote.getUserInfo(deviceId, ip, token, username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun deleteUser(
        deviceId: String,
        ip: String,
        token: String,
        username: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        userRemote.deleteUser(deviceId, ip, token, username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    override fun clearUser(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        userRemote.clearUser(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun updateUserPassword(
        deviceId: String,
        ip: String,
        token: String,
        username: String,
        password: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        userRemote.updateUserPassword(deviceId, ip, token, username, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getUserSpace(
        deviceId: String,
        ip: String,
        token: String,
        username: String,
        onNext: Observer<BaseProtocol<UserSpace>>
    ) {
        userRemote.getUserSpace(deviceId, ip, token, username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun setUserSpace(
        deviceId: String,
        ip: String,
        token: String,
        username: String,
        space: Long,
        onNext: Observer<BaseProtocol<UserSpace>>
    ) {
        userRemote.setUserSpace(deviceId, ip, token, username, space)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun setUserMark(
        deviceId: String,
        ip: String,
        token: String,
        username: String,
        mark: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        userRemote.setUserMark(deviceId, ip, token, username, mark)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun setDeviceMark(
        deviceId: String,
        ip: String,
        token: String,
        markName: String?,
        markDesc: String?,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        userRemote.setDeviceMark(deviceId, ip, token, markName, markDesc)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    override fun getDeviceMark(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<DataDevMark>>
    ) {
        userRemote.getDevMark(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun setSysInfo(
        deviceId: String,
        ip: String,
        token: String,
        name: String,
        value: String,
        level: Int
    ): Observable<BaseProtocol<Any>> {
        return userRemote.setSysInfo(deviceId, ip, token, name, value, level)
    }

    override fun getSysInfo(
        deviceId: String,
        ip: String,
        token: String,
        needSession: Boolean,
        name: String,
        level: Int
    ): Observable<BaseProtocol<DataSysInfoItem>> {
        return userRemote.getSysInfo(deviceId, ip, token, needSession, name, level)
    }

    override fun getAllSysInfo(
        deviceId: String,
        ip: String,
        token: String,
        needSession: Boolean,
        level: Int
    ): Observable<BaseProtocol<List<DataSysInfoItem>>> {
        return userRemote.getAllSysInfo(deviceId, ip, token, needSession, level)
    }

    override fun setPermission(
        deviceId: String,
        ip: String,
        token: String,
        username: String,
        sharePathType: SharePathType,
        perm: Int
    ): Observable<BaseProtocol<Any>> {
        return userRemote.setPermission(deviceId, ip, token, username, sharePathType, perm)
    }

    override fun getBrief(deviceId: String, ip: String, token: String, type: Int, For: String, backgroudTimestamp: Long?, avatarTimestamp: Long?, text: Long?): Observable<BaseProtocol<Brief>> {
        return userRemote.getBrief(deviceId, ip, token, type, For, backgroudTimestamp, avatarTimestamp, text)
    }

    override fun setBrief(deviceId: String, ip: String, token: String, type: Int, For: String, data: String): Observable<BaseProtocol<BriefTimeStamp>> {
        return userRemote.setBrief(deviceId, ip, token, type, For, data)
    }

    override fun getFileList(
        deviceId: String,
        ip: String,
        token: String,
        aciton: String,
        params: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.getFileList(deviceId, ip, token, aciton, params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun cleanRecycleFile(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.cleanRecycle(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun cleanRecycleFile(
        deviceId: String,
        ip: String,
        token: String,
        groupId: Long?,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.cleanRecycle(deviceId, ip, token,groupId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun restoreRecycleFile(
        deviceId: String,
        ip: String,
        token: String,
        share_path_type: Int,
        path: List<String>,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.restoreRecycleFile(deviceId, ip, token, share_path_type, path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun restoreRecycleFile(
        deviceId: String,
        ip: String,
        token: String,
        share_path_type: Int,
        path: List<String>,
        groupId: Long?,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.restoreRecycleFile(deviceId, ip, token, share_path_type, path,groupId = groupId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun optFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        onNext: Observer<BaseProtocol<Any>>,
        groupId: Long?
    ) {
        fileRemote.optFileAsync(deviceId, ip, token, action, path, share_path_type,groupId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun optFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.optFileAsync(deviceId, ip, token, action, path, share_path_type,-1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun optFileSync(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        groupId: Long?
    ): Observable<BaseProtocol<Any>> {
        return fileRemote.optFileSync(deviceId, ip, token, action, path, share_path_type,groupId)
    }

    override fun optFileSync(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int
    ): Observable<BaseProtocol<Any>> {
        return fileRemote.optFileSync(deviceId, ip, token, action, path, share_path_type,-1)
    }

    override fun copyOrMoveFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        toDir: String,
        des_path_type: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.copyOrMoveFile(
            deviceId,
            ip,
            token,
            action,
            path,
            share_path_type,
            toDir,
            des_path_type
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun copyOrMoveFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        share_path_type: Int,
        toDir: String,
        des_path_type: Int,
        groupid:Long?, to_groupid:Long?,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.copyOrMoveFile(
            deviceId,
            ip,
            token,
            action,
            path,
            share_path_type,
            toDir,
            des_path_type,
            groupid=groupid,
            to_groupid = to_groupid
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    override fun getDuplicateFiles(
        deviceId: String,
        ip: String,
        token: String,
        path: JSONArray,
        share_path_type: Int,
        type: String,
        page: Int,
        num: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.getDuplicateFiles(deviceId, ip, token, path, share_path_type, type, page, num)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun optService(deviceId: String, ip: String, token: String, method: String, serviceId: Int, onNext: Observer<BaseProtocol<Any>>) {
        serviceRemote.optService(deviceId, ip, token, method, serviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext)
    }

    override fun dlna(deviceId: String, ip: String, token: String, params: JSONObject, onNext: Observer<BaseProtocol<List<DLNAPathResult>>>) {
        systemRemote.dlna(deviceId, ip, token, params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext)
    }

    override fun dlnaGetOption(deviceId: String, ip: String, token: String, params: JSONObject, onNext: Observer<BaseProtocol<DLNAOptionResult>>) {
        systemRemote.dlnaGetOption(deviceId, ip, token, params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext)
    }

    override fun samba(deviceId: String, ip: String, token: String, params: JSONObject, onNext: Observer<BaseProtocol<LanScanVisibleResult>>) {
        systemRemote.samba(deviceId, ip, token, params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext)
    }

    override fun copyFile(
        deviceId: String, ip: String, token: String, share_path_type: Int,
        toDir: String, des_path_type: Int, path: List<String>, action: Int, async: Boolean?
    ): Observable<BaseProtocol<Any>> {
        return fileRemote.copyOrMoveFile(
            deviceId,
            ip,
            token,
            "copy",
            JSONArray(path),
            share_path_type,
            toDir,
            des_path_type,
            action,
            async
        )
    }

    override fun copyFile(
        deviceId: String, ip: String, token: String, share_path_type: Int,
        toDir: String, des_path_type: Int, path: List<String>, action: Int, async: Boolean?,
        groupid:Long, to_groupid:Long
    ): Observable<BaseProtocol<Any>> {
        return fileRemote.copyOrMoveFile(
            deviceId,
            ip,
            token,
            "copy",
            JSONArray(path),
            share_path_type,
            toDir,
            des_path_type,
            action,
            async,
            groupid=groupid,
            to_groupid=to_groupid
        )
    }

    override fun moveFile(
        deviceId: String, ip: String, token: String, share_path_type: Int,
        toDir: String, des_path_type: Int, path: List<String>, action: Int, async: Boolean?,
        groupid:Long, to_groupid:Long
    ): Observable<BaseProtocol<Any>> {
        return fileRemote.copyOrMoveFile(
            deviceId,
            ip,
            token,
            "move",
            JSONArray(path),
            share_path_type,
            toDir,
            des_path_type,
            action,
            async,
            groupid,
            to_groupid
        )
    }

    override fun moveFileV1(
        deviceId: String, ip: String, token: String, share_path_type: Int, groupId: Long?,
        toDir: String, des_path_type: Int, toGroupId: Long?,
        path: List<String>, action: Int, async: Boolean?
    ): Observable<BaseProtocol<Any>> {
        return fileRemote.copyOrMoveFileV1(
            deviceId,
            ip,
            token,
            "move",
            JSONArray(path),
            share_path_type,
            groupId,
            toDir,
            des_path_type,
            toGroupId,
            action,
            async
        )
    }


    override fun renameFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        newname: String,
        share_path_type: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        renameFile(deviceId, ip, token, action, path, newname, share_path_type,-1,onNext)
    }

    override fun renameFile(
        deviceId: String,
        ip: String,
        token: String,
        action: String,
        path: JSONArray,
        newname: String,
        share_path_type: Int,
        groupId: Long?,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.renameFile(deviceId, ip, token, action, path, newname, share_path_type,groupId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun searchFile(
        deviceId: String,
        ip: String,
        token: String,
        params: JSONObject,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.searchFile(deviceId, ip, token, params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun extractFile(
        deviceId: String,
        ip: String,
        token: String,
        path: String,
        share_path_type: Int,
        todir: String,
        des_path_type: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.extractFile(deviceId, ip, token, path, share_path_type, todir, des_path_type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun archiverFile(
        deviceId: String,
        ip: String,
        token: String,
        path: List<String>,
        share_path_type: Int,
        todir: String,
        des_path_type: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.archiverFile(deviceId, ip, token, path, share_path_type, todir, des_path_type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun tags(
        deviceId: String, ip: String, token: String,
        observer: Observer<BaseProtocol<List<FileTag>>>
    ) {
        fileRemote.tags(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    override fun tagFiles(
        deviceId: String, ip: String, token: String, tagId: Int, share_path_type: Int,
        page: Int, num: Int, ftype: List<String>?, order: String?, pattern: String?,
        observer: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.tagFiles(
            deviceId,
            ip,
            token,
            tagId,
            share_path_type,
            page,
            num,
            ftype,
            order,
            pattern
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    override fun fileOptTag(
        deviceId: String, ip: String, token: String, cmd: TagCMD, tagId: Int, share_path_type: Int,
        path: List<String>, observer: Observer<BaseProtocol<OptTagFileError>>
    ) {
        fileRemote.fileOptTag(deviceId, ip, token, cmd.cmd, tagId, share_path_type, path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    override fun rebootOrHaltSystem(
        deviceId: String,
        ip: String,
        token: String,
        isPowerOff: Boolean,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.rebootOrHaltSystem(deviceId, ip, token, isPowerOff)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getHDSmartInforSystem(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getHDSmartInforSystem(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getHDInforSystem(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getHDInforSystem(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun formatSystem(
        deviceId: String,
        ip: String,
        token: String,
        cmd: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.formatSystem(deviceId, ip, token, cmd)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun formatHDStatus(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.formatHdStatus(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getSysStatus(ip: String): Observable<BaseProtocol<List<Int>>> {
        return systemRemote.getSysStatus(ip)
    }

    override fun initDisk(
        deviceId: String,
        ip: String,
        token: String,
        force: Int
    ): Observable<BaseProtocol<Any>> {
        return systemRemote.initDisk(deviceId, ip, token, force)
    }

    override fun queryDiskStatus(
        deviceId: String,
        ip: String,
        token: String
    ): Observable<BaseProtocol<DataDiskStatus>> {
        return systemRemote.queryDiskStatus(deviceId, ip, token)
    }

    override fun getServiceStatus(
        deviceId: String,
        ip: String,
        token: String
    ): Observable<BaseProtocol<List<ServiceStatus>>> {

        return serviceRemote.getServiceStatus(deviceId, ip, token)
    }

    override fun getServiceList(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<List<ServiceItem>>>
    ) {
        serviceRemote.getServiceList(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getNasVersion(deviceId: String, ip: String): Observable<BaseProtocol<NasVersion>> {
        return systemRemote.getNasVersion(deviceId, ip)
    }


    override fun openDiskPower(
        deviceId: String,
        ip: String,
        token: String,
        slot: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.openDiskPower(deviceId, ip, token, slot)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    override fun closeDiskPower(
        deviceId: String,
        ip: String,
        token: String,
        slot: Int,
        force: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.closeDiskPower(deviceId, ip, token, slot, force)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getDiskInfo(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getDiskInfo(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getDiskOperation(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getDiskOperation(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun manageDisk(
        deviceId: String,
        ip: String,
        token: String,
        cmd: String,
        mode: String,
        force: Int,
        devices: List<String>,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.manageDisk(deviceId, ip, token, cmd, mode, force, devices)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getDiskManageStatus(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getDiskManageStatus(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)

    }

    override fun getDiskPowerStatus(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getDiskPowerStatus(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    /**
     * 操作离线文件
     */
    override fun queryTaskList(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.queryTaskList(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    /**
     * 操作离线文件
     * "id": "135e52c09627bd48c07bbfcbb59bb00a",        // 于查看任务中显示的id值
    // btsubfile不为空时表示控制指定id的bt任务下的指定文件行为
    // btsubfile表示指定要控制暂停或开始下载的文件名，其值使用[目录名]+"/"+[文件名]
    "btsubfile": "EastEnders.2021.01.28.720p.WEB.H264-iPlayerTV[rarbg]/RARBG.txt",
    "cmd": 0
    // cmd值说明
    // btsubfile不为空时cmd仅支持0和1
    // 0 暂停指定id的下载任务
    // 1 恢复指定id的下载任务
    // 2 暂停所有下载任务
    // 3 恢复所有下载任务
    // 4 移除指定id下载任务
    // 5 移除所有下载任务
     */
    override fun optTaskStatus(
        deviceId: String,
        ip: String,
        token: String,
        id: String,
        btsubfile: List<String>?,
        cmd: Int,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.optTaskStatus(deviceId, ip, token, id, btsubfile, cmd)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    /**
     * 操作离线文件
     */
    override fun addDownloadOfflineTask(
        deviceId: String, ip: String, token: String,
        session: String, url: String?,
        savePath: String, share_path_type: Int, //这二个是下载存放位置
        btfile: String?, share_path_type_btfile: Int?, //这二个种子文件的
        onNext: Observer<BaseProtocol<Any>>
    ) {
        fileRemote.addDownloadOfflineTask(
            deviceId,
            ip,
            token,
            session,
            "ofldown",
            "/down",
            url,
            savePath,
            share_path_type,
            btfile,
            share_path_type_btfile
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun startDiskSelfCheck(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.startDiskSelfCheck(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getDiskCheckReport(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getDiskCheckReport(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getHDSmartInforScan(
        deviceId: String,
        ip: String,
        token: String,
        dev: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        systemRemote.getHDSmartInforScan(deviceId, ip, token, dev)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun querySafeBoxStatus(
        deviceId: String,
        ip: String,
        token: String,
        next: Observer<BaseProtocol<SafeBoxStatus>>
    ) {
        serviceRemote.querySafeBoxStatus(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(next)
    }


    override fun initSafeBoxStatus(
        deviceId: String,
        ip: String,
        token: String,
        newQuestion: String,
        newAnswer: String,
        newKey: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        serviceRemote.initSafeBoxStatus(deviceId, ip, token, newQuestion, newAnswer, newKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    override fun unlockSafeBoxStatus(
        deviceId: String,
        ip: String,
        token: String,
        oldKey: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        serviceRemote.unlockSafeBoxStatus(deviceId, ip, token, oldKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun lockSafeBoxStatus(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        serviceRemote.lockSafeBoxStatus(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun resetSafeBoxByOldKey(
        deviceId: String,
        ip: String,
        token: String,
        ranStr: String,
        newKey: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        serviceRemote.resetSafeBoxByOldKey(deviceId, ip, token, ranStr, newKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    override fun resetSafeBoxQuestion(
        deviceId: String,
        ip: String,
        token: String,
        trans: String,
        newQuestion: String,
        newAnswer: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        serviceRemote.resetSafeBoxQuestion(deviceId, ip, token, trans, newQuestion, newAnswer)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun checkSafeBoxOldPsw(
        deviceId: String,
        ip: String,
        token: String,
        oldPsw: String,
        onNext: Observer<BaseProtocol<SafeBoxCheckData>>
    ) {
        serviceRemote.checkSafeBoxOldPsw(deviceId, ip, token, oldPsw)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun checkSafeBoxOldAnswer(
        deviceId: String,
        ip: String,
        token: String,
        oldAnswer: String,
        next: Observer<BaseProtocol<SafeBoxCheckData>>
    ) {
        serviceRemote.checkSafeBoxOldAnswer(deviceId, ip, token, oldAnswer)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(next)
    }


    override fun resetSafeBox(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        serviceRemote.resetSafeBox(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getGroupListJoined(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        groupSpaceRemoteSource.getGroupListJoined(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    //获取设备信息
    override fun createGroupSpace(
        deviceId: String,
        ip: String,
        token: String,
        groupName: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        groupSpaceRemoteSource.createGroupSpace(deviceId, ip, token, groupName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    //获取设备信息
    override fun getGroupAnnouncementHistory(
        deviceId: String,
        ip: String,
        token: String,
        groupId: Long,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        groupSpaceRemoteSource.getGroupAnnouncementHistory(deviceId, ip, token, groupId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }


    override fun publishAnnouncement(
        deviceId: String,
        ip: String,
        token: String,
        groupId: Long,
        content: String,
        onNext: Observer<BaseProtocol<Any>>
    ) {
        groupSpaceRemoteSource.publishAnnouncement(deviceId, ip, token, groupId, content)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)

    }

    override fun getHardwareInformation(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
        systemRemote.getHardwareInformation(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }

    override fun getSystemStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>) {
        systemRemote.getSystemStatus(deviceId, ip, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
    }



}