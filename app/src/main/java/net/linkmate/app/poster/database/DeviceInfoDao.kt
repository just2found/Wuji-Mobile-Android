package net.linkmate.app.poster.database

import androidx.room.*
import net.linkmate.app.poster.model.DeviceInfoModel
import net.linkmate.app.poster.model.LeftTabModel


@Dao
interface DeviceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: DeviceInfoModel)

    @Query("SELECT * FROM device_info WHERE device_id = :deviceId")
    fun getDeviceInfo(deviceId: String): DeviceInfoModel?

    @Query("SELECT * FROM device_info WHERE network_id = :networkId")
    fun getDeviceInfoNetworkId(networkId: String): DeviceInfoModel?

    @Query("DELETE FROM device_info WHERE device_id = :deviceId")
    fun delete(deviceId: String)
}