package net.linkmate.app.poster.database

import androidx.room.*
import net.linkmate.app.poster.model.DeviceInfoModel
import net.linkmate.app.poster.model.LeftTabModel
import net.linkmate.app.poster.model.UserModel


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: UserModel)

    @Query("SELECT * FROM user_info WHERE user_name = :userName")
    fun getUser(userName: String): UserModel?

    @Query("SELECT * FROM user_info")
    fun getUser(): List<UserModel>?
}