package net.linkmate.app.poster.model

import androidx.room.*

@Entity(tableName = "user_info", indices = [Index(value = ["user_name"], unique = true)])
@TypeConverters(MediaConverters::class)
data class UserModel(
        @ColumnInfo(name = "user_name")
        val userName: String,
        @ColumnInfo(name = "pwd")
        var pwd: String = "",
        @ColumnInfo(name = "pwd_new")
        var pwdNew: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var uid: Long = 0
}
