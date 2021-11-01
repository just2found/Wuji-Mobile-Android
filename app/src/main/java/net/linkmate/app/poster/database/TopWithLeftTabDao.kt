package net.linkmate.app.poster.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import net.linkmate.app.poster.model.TopWithLeftTabModel


@Dao
interface TopWithLeftTabDao {
    @Transaction
    @Query("SELECT * FROM top_tabs")
    fun geTopWithLeftTabs(): List<TopWithLeftTabModel>

    @Transaction
    @Query("SELECT * FROM top_tabs WHERE device_id = :deviceId")
    fun getTopWithLeftTabsWithDeviceId(deviceId: String): List<TopWithLeftTabModel>
}