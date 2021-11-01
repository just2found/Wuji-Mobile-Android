package net.sdvn.nascommon.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.repo.RepoApi
import io.weline.repo.api.ApiService
import io.weline.repo.api.GroupAction
import io.weline.repo.api.GroupUserPerm
import io.weline.repo.api.V5_ERR_ERROR_PARAMS
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.User
import io.weline.repo.data.remote.GroupRemoteSource
import io.weline.repo.net.RealV5Observer
import io.weline.repo.net.RetrofitSingleton
import libs.source.common.livedata.Resource
import libs.source.common.utils.JsonUtilN
import net.sdvn.nascommon.data.model.GNotices
import net.sdvn.nascommon.data.model.GroupUsers
import net.sdvn.nascommon.data.model.Groups
import net.sdvn.nascommon.db.GroupsKeeper
import net.sdvn.nascommon.db.objboxkt.GroupItem
import net.sdvn.nascommon.db.objboxkt.GroupNotice
import net.sdvn.nascommon.db.objboxkt.GroupUser
import timber.log.Timber

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/2
 */
class GroupRepository() {
    private val groupRemoteSource: GroupRemoteSource
    private val repoApi: RepoApi

    init {
        repoApi = RepoApiImpl()
        val apiService: ApiService =
            RetrofitSingleton.instance.getRetrofit().create(ApiService::class.java)
        groupRemoteSource = GroupRemoteSource(apiService)
    }

    fun loadGroups(devId: String): LiveData<Resource<List<GroupItem>?>> {
        val liveData = MediatorLiveData<Resource<List<GroupItem>?>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }
        val comparator = Comparator<GroupItem> { o1, o2 ->
            if (o1.mgrLevel == o2.mgrLevel) {
                return@Comparator o1.id.compareTo(o2.id)
            }
            return@Comparator o1.mgrLevel.compareTo(o2.mgrLevel)
        }
        groupRemoteSource.loadGroups(devId, address, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    val groups: Groups? = if (result.data == null) {
                        null
                    } else {
                        JsonUtilN.toJsonObject(
                            JsonUtilN.objectToString(
                                result.data!!
                            )
                        )
                    }

                    Timber.d("groups: $groups")
                    val saveNewGroups = GroupsKeeper.saveNewGroups(devId, groups?.groups)
                    liveData.postValue(Resource.success(saveNewGroups?.sortedWith(comparator)))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        liveData.addSource(GroupsKeeper.liveDataGroups(devId), Observer {
            liveData.postValue(Resource.success(it.sortedWith(comparator)))
        })
        return liveData
    }

    fun loadDeviceUsers(devId: String): LiveData<Resource<List<String>?>> {
        val liveData = MutableLiveData<Resource<List<String>?>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }
        groupRemoteSource.loadDeviceUsers(devId, address, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    val groups: List<String>? =
                        (result.data as? Map<String, *>)?.get("device_user") as List<String>?

                    Timber.d("groups: $groups")
                    liveData.postValue(Resource.success(groups))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        return liveData
    }

    fun loadGroupUsers(devId: String, groupId: Long): LiveData<Resource<List<GroupUser>?>> {
        val liveData = MediatorLiveData<Resource<List<GroupUser>?>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }
        groupRemoteSource.loadGroupUsers(devId, address, token, groupId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    val groups: GroupUsers? = if (result.data == null) {
                        null
                    } else {
                        JsonUtilN.toJsonObject(
                            JsonUtilN.objectToString(
                                result.data!!
                            )
                        )
                    }

                    Timber.d("groups: $groups")
                    val saveNewGroupUsers =
                        GroupsKeeper.saveNewGroupUsers(devId, groupId, groups?.users)
                    liveData.postValue(Resource.success(saveNewGroupUsers))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        liveData.addSource(GroupsKeeper.liveDataUsers(devId, groupId), Observer {
            liveData.postValue(Resource.success(it))
        })
        return liveData
    }

    fun loadGroupNotices(
        devId: String,
        groupId: Long
    ): LiveData<Resource<List<GroupNotice>?>> {
        val liveData = MediatorLiveData<Resource<List<GroupNotice>?>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }
        groupRemoteSource.getGroupNoticesHistory(devId, address, token, groupId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    val groups: GNotices? = if (result.data == null) {
                        null
                    } else {
                        JsonUtilN.toJsonObject(
                            JsonUtilN.objectToString(
                                result.data!!
                            )
                        )
                    }

                    Timber.d("groups: $groups")
                    val saveNewGroupUsers =
                        GroupsKeeper.saveNewGroupNotices(devId, groupId, groups?.notices)
                    liveData.postValue(Resource.success(saveNewGroupUsers))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        liveData.addSource(GroupsKeeper.liveDataNotices(devId, groupId), Observer {
            liveData.postValue(Resource.success(it))
        })
        return liveData
    }

    private fun checkParamsValid(devId: String): Pair<String, String>? {
        val address = repoApi.getAddress(devId)
        val token = repoApi.getToken()
        return if (address.isNullOrEmpty() || token.isNullOrEmpty()) {
            null
        } else {
            Pair(address, token)
        }
    }

    fun manageGroup(
        devId: String,
        groupId: Long,
        actionDelete: String,
        newName: String? = null
    ): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }

        groupRemoteSource.manageGroup(devId, address, token, groupId, actionDelete, newName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    if (actionDelete == GroupAction.ACTION_LEAVE || actionDelete == GroupAction.ACTION_DELETE) {
                        GroupsKeeper.deleteGroup(devId, groupId)
                    } else if (actionDelete == GroupAction.ACTION_TRANSFER) {
                        loadGroups(devId)
                    }
                    liveData.postValue(Resource.success(null))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        return liveData
    }

    fun deleteUser(
        devId: String,
        groupId: Long,
        action: String,
        users: Array<out GroupUser>
    ): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }
        groupRemoteSource.manageGroupUser(
            devId,
            address,
            token,
            groupId,
            action,
            users = users.map { it.username })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    GroupsKeeper.deleteUser(*users)
                    liveData.postValue(Resource.success(null))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        return liveData
    }

    fun manageGroupUser(
        devId: String,
        groupId: Long,
        action: String,
        user: GroupUser,
        perm: Int? = null,
        newMarkname: String? = null
    ): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }
        groupRemoteSource.manageGroupUser(
            devId,
            address,
            token,
            groupId,
            action,
            username = user.username,
            permission = perm,
            newmarknanme = newMarkname
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    GroupsKeeper.updateUser(user.apply {
                        if (perm != null) {
                            user.perm = perm
                        }
                        if (!newMarkname.isNullOrEmpty()) {
                            user.mark = newMarkname
                        }
                    })
                    liveData.postValue(Resource.success(null))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        return liveData
    }

    fun addUsers(
        devId: String,
        groupId: Long,
        action: String,
        data: Array<out String>
    ): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        val (address, token) = checkParamsValid(devId) ?: return liveData.also {
            liveData.postValue(
                Resource.error(
                    "params error", null,
                    V5_ERR_ERROR_PARAMS
                )
            )
        }
        groupRemoteSource.manageGroupUser(
            devId,
            address,
            token,
            groupId,
            action,
            users = data.toList()
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : RealV5Observer<Any>(devId) {
                override fun success(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.success(null))
                }

                override fun fail(result: BaseProtocol<Any>) {
                    liveData.postValue(Resource.error(result.error?.msg, null, result.error?.code))
                }
            })
        return liveData
    }
}