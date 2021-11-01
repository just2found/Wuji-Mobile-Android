package net.linkmate.app.poster.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.linkmate.app.poster.model.TopTabModel

@Dao
interface TopTabDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(list: List<TopTabModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg tabs: TopTabModel)

    @Query("DELETE FROM top_tabs WHERE device_id = :deviceId")
    fun delete(deviceId: String)
}