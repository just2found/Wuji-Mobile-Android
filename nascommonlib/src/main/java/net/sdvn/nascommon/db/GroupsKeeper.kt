package net.sdvn.nascommon.db

import androidx.lifecycle.LiveData
import io.objectbox.BoxStore
import io.objectbox.android.ObjectBoxLiveData
import net.sdvn.nascommon.db.objboxkt.*

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/1
 */
object GroupsKeeper {
    fun saveNewGroups(devId: String, newData: List<GroupItem>?): List<GroupItem>? {
        val boxFor = getBoxStore().boxFor(GroupItem::class.java)
        val dbUserId = DBHelper.getAccount()
        val find = boxFor.query()
            .equal(GroupItem_.dbUserId, dbUserId)
            .equal(GroupItem_.devId, devId)
            .build()
            .find()
        if (newData == null) {
            boxFor.remove(find)
        } else {
            val toMutableList = find.toMutableList()
            newData.forEach {
                val iterator = toMutableList.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (it.id == next.id) {
                        it.dbid = next.dbid
                        iterator.remove()
                    }
                }
                it.devId = devId
                it.dbUserId = dbUserId
            }
            if (toMutableList.isNotEmpty()) {
                boxFor.remove(toMutableList)
            }
            boxFor.put(newData)
        }
        return newData
    }


    fun updateGroupName(devId: String,groupId:Long,newName:String ):Boolean {
        val boxFor = getBoxStore().boxFor(GroupItem::class.java)
        val dbUserId = DBHelper.getAccount()
        val item=   boxFor.query()
            .equal(GroupItem_.dbUserId, dbUserId)
            .equal(GroupItem_.devId, devId)
            .equal(GroupItem_.id, groupId)
            .build()
            .findFirst()?:return false
        boxFor.remove(item)
        item.name=newName
        boxFor.put(item)
        return true
    }

    fun getBoxStore(): BoxStore {
        return DBHelper.getBoxStore()
    }

    fun liveDataGroups(devId: String): LiveData<List<GroupItem>> {
        val boxFor = getBoxStore().boxFor(GroupItem::class.java)
        val dbUserId = DBHelper.getAccount()
        val query = boxFor.query()
            .equal(GroupItem_.dbUserId, dbUserId)
            .equal(GroupItem_.devId, devId)
            .build()
        return ObjectBoxLiveData(query)
    }

    fun saveNewGroupUsers(
        devId: String,
        groupId: Long,
        newData: List<GroupUser>?
    ): List<GroupUser>? {
        val boxFor = getBoxStore().boxFor(GroupUser::class.java)
        val dbUserId = DBHelper.getAccount()
        val find = boxFor.query()
            .equal(GroupUser_.dbUserId, dbUserId)
            .equal(GroupUser_.devId, devId)
            .equal(GroupUser_.groupId, groupId)
            .build()
            .find()
        if (newData == null) {
            boxFor.remove(find)
        } else {
            val toMutableList = find.toMutableList()
            newData.forEach {
                val iterator = toMutableList.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (it.username == next.username) {
                        it.dbId = next.dbId
                        iterator.remove()
                    }
                }
                it.devId = devId
                it.dbUserId = dbUserId
            }
            if (toMutableList.isNotEmpty()) {
                boxFor.remove(toMutableList)
            }
            boxFor.put(newData)
        }
        return newData
    }

    fun saveNewGroupNotices(
        devId: String,
        groupId: Long,
        newData: List<GroupNotice>?
    ): List<GroupNotice>? {
        val boxFor = getBoxStore().boxFor(GroupNotice::class.java)
        val dbUserId = DBHelper.getAccount()
        val find = boxFor.query()
            .equal(GroupNotice_.dbUserId, dbUserId)
            .equal(GroupNotice_.devId, devId)
            .equal(GroupNotice_.groupId, groupId)
            .build()
            .find()
        if (newData == null) {
            boxFor.remove(find)
        } else {
            val toMutableList = find.toMutableList()
            newData.forEach {
                val iterator = toMutableList.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (it.noticeId == next.noticeId) {
                        it.dbId = next.dbId
                        iterator.remove()
                    }
                }
                it.devId = devId
                it.dbUserId = dbUserId
            }
            if (toMutableList.isNotEmpty()) {
                boxFor.remove(toMutableList)
            }
            boxFor.put(newData)
        }
        return newData
    }

    fun liveDataNotices(devId: String, groupId: Long): LiveData<List<GroupNotice>> {
        val boxFor = getBoxStore().boxFor(GroupNotice::class.java)
        val dbUserId = DBHelper.getAccount()
        val query = boxFor.query()
            .equal(GroupNotice_.dbUserId, dbUserId)
            .equal(GroupNotice_.devId, devId)
            .equal(GroupNotice_.groupId, groupId)
            .build()
        return ObjectBoxLiveData(query)
    }

    fun findGroup(devId: String, groupId: Long): GroupItem? {
        val boxFor = getBoxStore().boxFor(GroupItem::class.java)
        val dbUserId = DBHelper.getAccount()
        return boxFor.query()
            .equal(GroupItem_.dbUserId, dbUserId)
            .equal(GroupItem_.devId, devId)
            .equal(GroupItem_.id, groupId)
            .build()
            .findFirst()
    }

    fun deleteGroup(devId: String, groupId: Long) {
        val boxFor = getBoxStore().boxFor(GroupItem::class.java)
        val dbUserId = DBHelper.getAccount()
        val query = boxFor.query()
            .equal(GroupItem_.dbUserId, dbUserId)
            .equal(GroupItem_.devId, devId)
            .equal(GroupItem_.id, groupId)
            .build()
            .find()
        boxFor.remove(query)
    }

    fun findNotice(devId: String, groupId: Long, noticeId: Long): GroupNotice? {
        val boxFor = getBoxStore().boxFor(GroupNotice::class.java)
        val dbUserId = DBHelper.getAccount()
        return boxFor.query()
            .equal(GroupNotice_.dbUserId, dbUserId)
            .equal(GroupNotice_.devId, devId)
            .equal(GroupNotice_.groupId, groupId)
            .equal(GroupNotice_.noticeId, noticeId)
            .build()
            .findFirst()
    }

    fun liveDataUsers(devId: String, groupId: Long): LiveData<List<GroupUser>> {
        val boxFor = getBoxStore().boxFor(GroupUser::class.java)
        val dbUserId = DBHelper.getAccount()
        val query = boxFor.query()
            .equal(GroupUser_.dbUserId, dbUserId)
            .equal(GroupUser_.devId, devId)
            .equal(GroupUser_.groupId, groupId)
            .build()
        return ObjectBoxLiveData(query)
    }

    fun updateUser(user: GroupUser) {
        val boxFor = getBoxStore().boxFor(GroupUser::class.java)
        boxFor.put(user)
    }

    fun deleteUser(vararg users: GroupUser) {
        val boxFor = getBoxStore().boxFor(GroupUser::class.java)
        boxFor.remove(*users)
    }

    fun findGroupUsers(devId: String, groupId: Long): List<GroupUser> {
        val boxFor = getBoxStore().boxFor(GroupUser::class.java)
        val dbUserId = DBHelper.getAccount()
        val query = boxFor.query()
            .equal(GroupUser_.dbUserId, dbUserId)
            .equal(GroupUser_.devId, devId)
            .equal(GroupUser_.groupId, groupId)
            .build()
        return query.find()
    }

}