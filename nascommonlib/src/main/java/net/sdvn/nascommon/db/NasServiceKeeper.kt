package net.sdvn.nascommon.db

import androidx.lifecycle.LiveData
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.equal

import io.objectbox.query.QueryBuilder
import net.sdvn.nascommon.db.objecbox.NasServiceItem
import net.sdvn.nascommon.db.objecbox.NasServiceItem_

object NasServiceKeeper {

    const val SERVICE_TYPE_PUBLIC = 0
    const val SERVICE_TYPE_HOME = 1
    const val SERVICE_TYPE_FILESERVICE = 2
    const val SERVICE_TYPE_FILESHARE = 3
    const val SERVICE_TYPE_BTSERVICE = 4
    const val SERVICE_TYPE_FRIENDCYCLE = 5
    const val SERVICE_TYPE_FILEDUP = 6
    const val SERVICE_TYPE_OFFLINEDOWNLOAD = 7
    const val SERVICE_TYPE_DISKSELFCHECK = 8
    const val SERVICE_TYPE_SAFEBOX = 9
    const val SERVICE_TYPE_SAMBA = 10
    const val SERVICE_TYPE_DLNA = 11

    const val SERVICE_TYPE_USBWEBSTORAGE = 12
    const val SERVICE_TYPE_FAVORITE = 13
    const val SERVICE_TYPE_DEVICE_INFORMATION= 14
    const val SERVICE_TYPE_GROUP= 15

    //根据devId查询全部服务
    fun all(devId: String): List<NasServiceItem> {
        val queryBuilder = getNasServiceItemQueryBuilder()
        queryBuilder.equal(NasServiceItem_.devId, devId)
        return queryBuilder.build().find()
    }

    //根据devId查询全部服务
    fun insertOrUpdate(list: List<NasServiceItem>) {
        for (nasServiceItem in list) {
            val oldItem = query(nasServiceItem)
            if (oldItem != null) {
                nasServiceItem.id = oldItem.id
            }
            nasServiceItem.userId = DBHelper.getAccount()
        }
        val dao = getNasServiceItemBox()
        return dao.put(list)
    }

    private fun query(nasServiceItem: NasServiceItem): NasServiceItem? {
        val queryBuilder = getNasServiceItemQueryBuilder()
        queryBuilder.equal(NasServiceItem_.devId, nasServiceItem.devId)
        queryBuilder.equal(NasServiceItem_.serviceName, nasServiceItem.serviceName)
        queryBuilder.equal(NasServiceItem_.serviceId, nasServiceItem.serviceId)
        return queryBuilder.build().findFirst()
    }


    private fun getNasServiceItemQueryBuilder(): QueryBuilder<NasServiceItem> {
        val dao = getNasServiceItemBox()
        val queryBuilder = dao.query()
        queryBuilder.equal(NasServiceItem_.userId, DBHelper.getAccount())
        return queryBuilder
    }

    private fun getNasServiceItemBox(): Box<NasServiceItem> {
        return DBHelper.getBoxStore().boxFor(NasServiceItem::class.java)
    }

    fun queryNasServiceItem(devId: String,serviceId:Int): NasServiceItem? {
        val queryBuilder = getNasServiceItemQueryBuilder()
        queryBuilder.equal(NasServiceItem_.devId, devId)
        queryBuilder.equal(NasServiceItem_.serviceId, serviceId)
        return queryBuilder.build().findFirst()
    }


    //
    fun liveDataByDevId(devId: String): LiveData<List<NasServiceItem>> {
        val queryBuilder = getNasServiceItemQueryBuilder()
        queryBuilder.equal(NasServiceItem_.devId, devId)
        return ObjectBoxLiveData(queryBuilder.build())
    }
}