package io.weline.repo.data.remote

import io.reactivex.Observable
import io.weline.repo.api.ApiService
import io.weline.repo.api.GroupAction
import io.weline.repo.data.model.BaseProtocol

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/2
 */
class GroupRemoteSource(private val apiService: ApiService) {

    fun loadGroups(devId: String, address: String, token: String): Observable<BaseProtocol<Any>> {
        return apiService.group(devId, address, token, mapOf(Pair("method", "list_group_all")))
    }

    fun loadDeviceUsers(
        devId: String,
        address: String,
        token: String
    ): Observable<BaseProtocol<Any>> {
        return apiService.group(devId, address, token, mapOf(Pair("method", "list_device_user")))
    }

    fun loadGroupUsers(
        devId: String,
        address: String,
        token: String,
        groupId: Long
    ): Observable<BaseProtocol<Any>> {
        return apiService.group(
            devId, address, token,
            mapOf(
                Pair("method", "list_user"),
                Pair("params", mapOf(Pair("id", groupId)))
            )
        )
    }


    fun getGroupNoticesHistory(
        devId: String,
        address: String,
        token: String,
        groupId: Long
    ): Observable<BaseProtocol<Any>> {
        return apiService.group(
            devId, address, token, mapOf(
                Pair("method", "get_group_notice_history"),
                Pair("params", mapOf(Pair("id", groupId)))
            )
        )
    }

    fun manageGroup(
        devId: String,
        address: String,
        token: String,
        groupId: Long,
        action: String,
        newName: String?
    ): Observable<BaseProtocol<Any>> {
        val second = hashMapOf<String, Any>(Pair("id", groupId))
        if (!newName.isNullOrEmpty()) {
            when (action) {
                GroupAction.ACTION_RENAME -> {
                    second["name"] = newName
                }
                GroupAction.ACTION_TRANSFER -> {
                    second["username"] = newName
                }

            }
        }
        return apiService.group(
            devId, address, token, mapOf(
                Pair("method", action),
                Pair("params", second)
            )
        )
    }

    fun manageGroupUser(
        devId: String,
        address: String,
        token: String,
        groupId: Long,
        action: String,
        users: List<String>? = null,
        username: String? = null,
        permission: Int? = null,
        newmarknanme: String? = null
    ): Observable<BaseProtocol<Any>> {
        val second = hashMapOf<String, Any>(Pair("id", groupId))
        when (action) {
            GroupAction.ACTION_DELETE_USER, GroupAction.ACTION_ADD_USER -> {
                if (!users.isNullOrEmpty()) {
                    second["username"] = users
                }
            }

            GroupAction.ACTION_USER_PERMISSION -> {
                if (!username.isNullOrEmpty()) {
                    second["username"] = username
                    second["perm"] = permission!!
                }
            }
            GroupAction.ACTION_USER_MARKNAME -> {
                if (!username.isNullOrEmpty()) {
                    second["username"] = username
                    second["markname"] = newmarknanme!!
                }
            }

        }
        return apiService.group(
            devId, address, token, mapOf(
                Pair("method", action),
                Pair("params", second)
            )
        )
    }

}