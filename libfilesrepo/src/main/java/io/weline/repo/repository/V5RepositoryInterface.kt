package io.weline.repo.repository

import io.reactivex.Observable
import io.reactivex.Observer
import io.weline.repo.api.TagCMD
import io.weline.repo.data.model.*
import io.weline.repo.files.data.FileTag
import io.weline.repo.files.data.SharePathType
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author Raleigh.Luo
 * date：20/9/18 15
 * describe：
 */
interface V5RepositoryInterface {
    /*---用户接口-------------------------------------------------------------------------------*/
    /**
     * 获取用户列表
     */
    fun getUserList(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Users>>)

    /**
     * 添加用户
     */
    fun addUser(deviceId: String, ip: String, token: String, username: String,
                password: String, admin: Int = 0, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 添加用户
     */
    fun getUserInfo(deviceId: String, ip: String, token: String, username: String,
                    onNext: Observer<BaseProtocol<User>>)

    /**
     * 删除用户
     */
    fun deleteUser(deviceId: String, ip: String, token: String, username: String, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 清除用户
     */
    fun clearUser(deviceId: String, ip: String, token: String,
                  onNext: Observer<BaseProtocol<Any>>)

    /**
     * 更新用户密码
     */
    fun updateUserPassword(deviceId: String, ip: String, token: String, username: String, password: String,
                           onNext: Observer<BaseProtocol<Any>>)

    /**
     * 获取用户空间
     */
    fun getUserSpace(deviceId: String, ip: String, token: String, username: String,
                     onNext: Observer<BaseProtocol<UserSpace>>)

    /**
     * 设置用户空间
     */
    fun setUserSpace(deviceId: String, ip: String, token: String, username: String,
                     space: Long, onNext: Observer<BaseProtocol<UserSpace>>)

    /**
     * 设置用户备注
     */
    fun setUserMark(deviceId: String, ip: String, token: String, username: String, mark: String, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 设置设备备注
     */
    fun setDeviceMark(deviceId: String, ip: String, token: String, markName: String?, markDesc: String?, onNext: Observer<BaseProtocol<Any>>)

    /**
     * get 设备备注
     */
    fun getDeviceMark(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<DataDevMark>>)

    /*
    * 用户系统信息
    * */
    fun setSysInfo(deviceId: String, ip: String, token: String, name: String, value: String, level: Int): Observable<BaseProtocol<Any>>
    fun getSysInfo(deviceId: String, ip: String, token: String, needSession: Boolean, name: String, level: Int): Observable<BaseProtocol<DataSysInfoItem>>
    fun getAllSysInfo(deviceId: String, ip: String, token: String, needSession: Boolean, level: Int): Observable<BaseProtocol<List<DataSysInfoItem>>>

    /*设置用户权限
    * */
    fun setPermission(deviceId: String, ip: String, token: String, username: String, sharePathType: SharePathType, perm: Int): Observable<BaseProtocol<Any>>

    /**
     * 获取简介
     */
    fun getBrief(deviceId: String, ip: String, token: String, type: Int, For: String, backgroudTimestamp: Long? = null,
                 avatarTimestamp: Long? = null, text: Long? = null): Observable<BaseProtocol<Brief>>

    /**
     * 设置简介
     */
    fun setBrief(deviceId: String, ip: String, token: String, type: Int, For: String, data: String): Observable<BaseProtocol<BriefTimeStamp>>

    /*---文件接口-------------------------------------------------------------------------------*/


    fun getFileList(deviceId: String, ip: String, token: String, action: String, params: String, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 清空回收站
     */
    fun cleanRecycleFile(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun cleanRecycleFile(deviceId: String, ip: String, token: String,  groupId: Long?=null, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 恢复回收站文件
     * */
    fun restoreRecycleFile(deviceId: String, ip: String, token: String,share_path_type: Int,path: List<String>, onNext: Observer<BaseProtocol<Any>>)
    fun restoreRecycleFile(deviceId: String, ip: String, token: String,share_path_type: Int,path: List<String>,    groupId: Long?=null, onNext: Observer<BaseProtocol<Any>>)


    /**
     * 获取文件属性/删除文件
     */
    fun optFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int,onNext: Observer<BaseProtocol<Any>>)
    fun optFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int,onNext: Observer<BaseProtocol<Any>>, groupId: Long?=null)
    fun optFileSync(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int): Observable<BaseProtocol<Any>>
    fun optFileSync(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int,groupId: Long?=null): Observable<BaseProtocol<Any>>

    /**
     * 拷贝/移动
     */
    fun copyOrMoveFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int,
                       toDir: String, des_path_type: Int, onNext: Observer<BaseProtocol<Any>>)
    fun copyOrMoveFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, share_path_type: Int,
                       toDir: String, des_path_type: Int,   groupid:Long?=null, to_groupid:Long?=null, onNext: Observer<BaseProtocol<Any>>)


    fun copyFile(deviceId: String, ip: String, token: String, share_path_type: Int,
                 toDir: String, des_path_type: Int, path: List<String>, action: Int = 0, async: Boolean? = null)
            : Observable<BaseProtocol<Any>>

    fun copyFile(deviceId: String, ip: String, token: String, share_path_type: Int,
                 toDir: String, des_path_type: Int, path: List<String>, action: Int = 0, async: Boolean? = null,  groupid:Long=-1, to_groupid:Long=-1)
            : Observable<BaseProtocol<Any>>

    fun moveFile(deviceId: String, ip: String, token: String, share_path_type: Int,
                 toDir: String, des_path_type: Int, path: List<String>, action: Int = 0, async: Boolean? = null,
                 groupid:Long=-1, to_groupid:Long=-1
    )
            : Observable<BaseProtocol<Any>>

    /**
     * 重命名文件
     */
    fun renameFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, newname: String, share_path_type: Int, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 重命名文件
     */
    fun renameFile(deviceId: String, ip: String, token: String, action: String, path: JSONArray, newname: String, share_path_type: Int,groupId: Long?=null ,onNext: Observer<BaseProtocol<Any>>)

    /**
     * 搜索
     */
    fun searchFile(deviceId: String, ip: String, token: String, params: JSONObject, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 解压文件
     */
    fun extractFile(deviceId: String, ip: String, token: String, path: String, share_path_type: Int,
                    todir: String, des_path_type: Int, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 压缩文件
     */
    fun archiverFile(deviceId: String, ip: String, token: String, path: List<String>, share_path_type: Int,
                     todir: String, des_path_type: Int, onNext: Observer<BaseProtocol<Any>>)

    fun tags(deviceId: String, ip: String, token: String, observer: Observer<BaseProtocol<List<FileTag>>>)
    fun tagFiles(deviceId: String, ip: String, token: String,
                 tagId: Int,
                 share_path_type: Int,
                 page: Int = 0,
                 num: Int = 100,
                 ftype: List<String>? = null,
                 order: String? = null,
                 pattern: String? = null,
                 observer: Observer<BaseProtocol<Any>>)

    fun fileOptTag(deviceId: String, ip: String, token: String,
                   cmd: TagCMD,
                   tagId: Int,
                   share_path_type: Int,
                   path: List<String>,
                   observer: Observer<BaseProtocol<OptTagFileError>>)

    /*---系统接口-------------------------------------------------------------------------------*/
    /**
     * 系统重启/关机
     */
    fun rebootOrHaltSystem(deviceId: String, ip: String, token: String, isPowerOff: Boolean, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 获取磁盘smart相关信息
     */
    fun getHDSmartInforSystem(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 获取磁盘相关信息
     */
    fun getHDInforSystem(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 格式化
     */
    fun formatSystem(deviceId: String, ip: String, token: String, cmd: String, onNext: Observer<BaseProtocol<Any>>)

    /**
     * 格式化
     */
    fun formatHDStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)

    /**查询系统状态
     * */
    fun getSysStatus(ip: String): Observable<BaseProtocol<List<Int>>>

    /**M8X4 nas2.0 临时格式化*/
    fun initDisk(deviceId: String, ip: String, token: String, force: Int = 0): Observable<BaseProtocol<Any>>

    /**M8X4 nas2.0 临时格式化状态查询*/
    fun queryDiskStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<DataDiskStatus>>

    /**nas3.0 服务状态查询接口*/
    fun getServiceStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<List<ServiceStatus>>>

    /**nas3.0 服务版本号查询接口*/
    fun getNasVersion(deviceId: String, ip: String): Observable<BaseProtocol<NasVersion>>

    fun openDiskPower(deviceId: String, ip: String, token: String, slot: Int, onNext: Observer<BaseProtocol<Any>>)
    fun closeDiskPower(deviceId: String, ip: String, token: String, slot: Int, force: Int, onNext: Observer<BaseProtocol<Any>>)
    fun getDiskInfo(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun getDiskOperation(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun manageDisk(deviceId: String, ip: String, token: String, cmd: String, mode: String, force: Int, devices: List<String>, onNext: Observer<BaseProtocol<Any>>)
    fun getDiskManageStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun getDiskPowerStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun addDownloadOfflineTask(deviceId: String, ip: String, token: String, session: String, url: String?, savePath: String, share_path_type: Int, btfile: String?, share_path_type_btfile: Int?, onNext: Observer<BaseProtocol<Any>>)
    fun queryTaskList(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)

    fun optTaskStatus(deviceId: String, ip: String, token: String, id: String, btsubfile: List<String>?, cmd: Int, onNext: Observer<BaseProtocol<Any>>)
    fun startDiskSelfCheck(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun getDiskCheckReport(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun getHDSmartInforScan(deviceId: String, ip: String, token: String, dev: String, onNext: Observer<BaseProtocol<Any>>)
    fun getServiceList(deviceId: String, ip: String, token: String,onNext: Observer<BaseProtocol<List<ServiceItem>>>)
    fun resetSafeBox(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun resetSafeBoxQuestion(deviceId: String, ip: String, token: String, trans: String, newQuestion: String, newAnswer: String, onNext: Observer<BaseProtocol<Any>>)
    fun resetSafeBoxByOldKey(deviceId: String, ip: String, token: String, oldKey: String, newKey: String, onNext: Observer<BaseProtocol<Any>>)
    fun lockSafeBoxStatus(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)
    fun unlockSafeBoxStatus(deviceId: String, ip: String, token: String, oldKey: String, onNext: Observer<BaseProtocol<Any>>)
    fun initSafeBoxStatus(deviceId: String, ip: String, token: String, newQuestion: String, newAnswer: String, newKey: String, onNext: Observer<BaseProtocol<Any>>)
    fun querySafeBoxStatus(deviceId: String, ip: String, token: String, next: Observer<BaseProtocol<SafeBoxStatus>>)
    fun checkSafeBoxOldPsw(deviceId: String, ip: String, token: String, oldPsw: String, next: Observer<BaseProtocol<SafeBoxCheckData>>)
    fun checkSafeBoxOldAnswer(deviceId: String, ip: String, token: String, oldAnswer: String, next: Observer<BaseProtocol<SafeBoxCheckData>>)
    fun getDuplicateFiles(deviceId: String, ip: String, token: String, path: JSONArray, share_path_type: Int, type: String, page: Int, num: Int, onNext: Observer<BaseProtocol<Any>>)
    fun getGroupListJoined(deviceId: String, ip: String, token: String, onNext: Observer<BaseProtocol<Any>>)

    fun createGroupSpace(
        deviceId: String,
        ip: String,
        token: String,
        groupName: String,
        onNext: Observer<BaseProtocol<Any>>
    )

    fun getGroupAnnouncementHistory(
        deviceId: String,
        ip: String,
        token: String,
        groupId: Long,
        onNext: Observer<BaseProtocol<Any>>
    )

    fun publishAnnouncement( deviceId: String, ip: String, token: String, groupId: Long, content: String,  onNext: Observer<BaseProtocol<Any>>)
    fun moveFileV1(
        deviceId: String,
        ip: String,
        token: String,
        share_path_type: Int,
        groupId: Long?,
        toDir: String,
        des_path_type: Int,
        toGroupId: Long?,
        path: List<String>,
        action: Int,
        async: Boolean?
    ): Observable<BaseProtocol<Any>>
    fun getHardwareInformation(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    )

    fun getSystemStatus(
        deviceId: String,
        ip: String,
        token: String,
        onNext: Observer<BaseProtocol<Any>>
    )

    /**
     * 操作服务 开启或停止
     */
    fun optService(deviceId: String, ip: String, token: String, method: String, serviceId: Int, onNext: Observer<BaseProtocol<Any>>)
    fun dlna(deviceId: String, ip: String, token: String, params:JSONObject, onNext: Observer<BaseProtocol<List<DLNAPathResult>>>)
    fun dlnaGetOption(deviceId: String, ip: String, token: String, params:JSONObject, onNext: Observer<BaseProtocol<DLNAOptionResult>>)
    fun samba(deviceId: String, ip: String, token: String, params:JSONObject, onNext: Observer<BaseProtocol<LanScanVisibleResult>>)
}