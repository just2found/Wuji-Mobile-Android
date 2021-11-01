package net.linkmate.app.ui.nas.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import io.weline.repo.api.GroupAction
import io.weline.repo.api.GroupUserPerm.PERM_RECYCLE
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.api.V5_ERR_DENIED_PERMISSION
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.User
import io.weline.repo.files.data.SharePathType
import io.weline.repo.net.V5Observer
import io.weline.repo.net.V5ObserverImpl
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.ui.nas.V5RepositoryWrapper
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.db.GroupsKeeper
import net.sdvn.nascommon.db.objboxkt.GroupItem
import net.sdvn.nascommon.db.objboxkt.GroupNotice
import net.sdvn.nascommon.db.objboxkt.GroupUser
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.repository.GroupRepository

/**
create by: 86136
create time: 2021/6/1 10:20
Function description:
 */

class GroupSpaceModel : ViewModel() {
    private val groupRepository: GroupRepository = GroupRepository()

    companion object {
        const val TITLE = 1
        const val ITEM = 2
    }

    val liveDataLoginSession = MutableLiveData<LoginSession>()
    val liveDataEnableCreate = liveDataLoginSession.map {
        it?.userInfo?.permissions?.find { it.sharePathType == SharePathType.GROUP.type }?.isWriteable
            ?: false
    }

    fun focusRefreshLoginSession(devID: String) {
        //检查用户创建权限
        getLoginSession(MutableLiveData<Resource<Any>>(), devID) { loginSession ->
            loginSession.userInfo?.username?.let { it1 ->
                V5RepositoryWrapper.getUserInfo(devID, it1, object : V5Observer<User>(devID) {
                    override fun success(result: BaseProtocol<User>) {
                        val data = result.data
                        loginSession.userInfo?.permissions = data?.permissions
                    }

                    override fun fail(result: BaseProtocol<User>) {
                    }

                    override fun isNotV5() {
                    }
                })
            }
        }

    }

    private fun <T> getLoginSession(
        liveData: MutableLiveData<Resource<T>>,
        devID: String,
        next: (loginSession: LoginSession) -> Unit
    ) {
        SessionManager.getInstance().getLoginSession(devID,
            object : GetSessionListener(false) {
                override fun onSuccess(url: String, data: LoginSession) {
                    liveDataLoginSession.postValue(data)
                    next(data)
                }

                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                    if (errorNo == V5_ERR_DENIED_PERMISSION) {
                        SessionManager.getInstance().removeSession(devID)
                    }
                    liveData.postValue(Resource.error("", null, errorNo))
                }
            })
    }


    fun getGroupListJoined(devID: String): LiveData<Resource<List<GroupItem>?>>? {
        groupItem = null
        return groupRepository.loadGroups(devID)
    }

    fun createGroupSpace(devID: String, groupName: String): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().createGroupSpace(
                loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), groupName, v5ObserverImpl
            )
        }
        return liveData
    }


    fun getGroupAnnouncementHistory(
        devID: String,
        groupId: Long
    ): LiveData<Resource<List<GroupNotice>?>>? {
        return groupRepository.loadGroupNotices(devID, groupId).map {
            if (it.status == Status.SUCCESS) {
                Resource.success(it.data?.sortedWith(compareByDescending { it.noticeId }))
            } else {
                it
            }
        }
    }

    fun publishAnnouncement(
        devID: String,
        groupId: Long,
        content: String
    ): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().publishAnnouncement(
                loginSession.id
                    ?: "",
                loginSession.ip,
                LoginTokenUtil.getToken(),
                groupId,
                content,
                v5ObserverImpl
            )
        }
        return liveData
    }

    fun loadGroupUsers(deviceid: String, groupId: Long): LiveData<Resource<List<GroupUser>?>>? {
        return groupRepository.loadGroupUsers(deviceid, groupId)
    }

    fun loadDevicesUser(devID: String, groupId: Long): LiveData<Resource<List<String>?>> {
        val findGroupUsers = GroupsKeeper.findGroupUsers(devID, groupId)
        return groupRepository.loadDeviceUsers(devID).map { resource ->
            if (resource.status == Status.SUCCESS) {
                val filter = resource.data?.filter { username ->
                    findGroupUsers.find { groupUser -> username == groupUser.username } == null
                            && username != SessionManager.getInstance().username
                }
                Resource.success(filter)
            } else {
                resource
            }
        }
    }


    private var groupItem: GroupItem? = null
    fun checkAndInitGroupItem(deviceid: String, groupId: Long): GroupItem? {
        if (groupItem?.devId != deviceid || groupItem?.id != groupId)
            groupItem = GroupsKeeper.findGroup(deviceid, groupId)
        return groupItem
    }

    fun updateGroupItemName(deviceid: String, groupId: Long, newName: String) {
        if (groupItem?.devId == deviceid && groupItem?.id == groupId) {
            groupItem?.name = newName
        }
    }

    fun getGroupItemName(deviceid: String, groupId: Long): String? {
        return checkAndInitGroupItem(deviceid, groupId)?.name
    }

    fun isGroupManagement(deviceid: String, groupId: Long): Boolean {
        return checkAndInitGroupItem(deviceid, groupId)?.isAdmin() ?: false
    }

    fun isDeviceManagement(deviceid: String): Boolean {
        return SessionManager.getInstance().getDeviceModel(deviceid)?.hasAdminRights() ?: false
    }

    fun isGroupMember(deviceid: String, groupId: Long): Boolean {
        return checkAndInitGroupItem(deviceid, groupId)?.isMember() ?: false
    }

    fun deleteGroup(devID: String, groupId: Long): LiveData<Resource<Any>> {
        groupItem = null
        return groupRepository.manageGroup(devID, groupId, GroupAction.ACTION_DELETE)
    }

    fun leaveGroup(devID: String, groupId: Long): LiveData<Resource<Any>> {
        groupItem = null
        return groupRepository.manageGroup(devID, groupId, GroupAction.ACTION_LEAVE)
    }

    fun renameGroup(devID: String, groupId: Long, newName: String): LiveData<Resource<Any>> {
        groupItem = null
        return groupRepository.manageGroup(devID, groupId, GroupAction.ACTION_RENAME, newName)
    }

    fun transferGroup(devID: String, groupId: Long, newName: String): LiveData<Resource<Any>> {
        groupItem = null
        return groupRepository.manageGroup(devID, groupId, GroupAction.ACTION_TRANSFER, newName)
    }

    fun deleteUser(devID: String, groupId: Long, vararg data: GroupUser): LiveData<Resource<Any>> {
        return groupRepository.deleteUser(devID, groupId, GroupAction.ACTION_DELETE_USER, data)
    }

    fun addUsers(devID: String, groupId: Long, vararg data: String): LiveData<Resource<Any>> {
        return groupRepository.addUsers(devID, groupId, GroupAction.ACTION_ADD_USER, data)
    }

    fun manageGroupUser(
        devID: String,
        groupId: Long,
        data: GroupUser,
        perm: Int
    ): LiveData<Resource<Any>> {
        return groupRepository.manageGroupUser(
            devID,
            groupId,
            GroupAction.ACTION_USER_PERMISSION,
            data,
            perm
        )
    }

    fun manageGroupUser(
        devID: String,
        groupId: Long,
        data: GroupUser,
        newName: String
    ): LiveData<Resource<Any>> {
        return groupRepository.manageGroupUser(
            devID,
            groupId,
            GroupAction.ACTION_USER_MARKNAME,
            data,
            newMarkname = newName
        )
    }

    fun optErrorProcessor(code: Int?) {
        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(code))
    }

    fun hasFileManage(deviceid: String, groupId: Long): Boolean {
        checkAndInitGroupItem(deviceid, groupId)?.perm?.let {
            return it and PERM_RECYCLE == PERM_RECYCLE
        }
        return false
    }

}