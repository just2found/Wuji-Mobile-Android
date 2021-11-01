package net.linkmate.app.poster.model

import androidx.room.*
import java.io.Serializable

/**
 * Create by Admin on 2021-07-05-16:26
 */

@Entity(tableName = "device_info", indices = [Index(value = ["device_id"], unique = true)])
@TypeConverters(MediaConverters::class)
data class DeviceInfoModel(
        // 当前导航对应的文件夹类别
        @ColumnInfo(name = "device_id")
        val deviceId: String,
    // 圈子网络id
    @ColumnInfo(name = "network_id")
    var networkId: String = "",
    @ColumnInfo(name = "vip")
    var vip: String = "",
    @ColumnInfo(name = "user_id")
    var userid: String = "",
    @ColumnInfo(name = "dev_class")
    var devClass: Int = 0,
    @ColumnInfo(name = "is_selectable")
    var isSelectable: Boolean = false,
    @ColumnInfo(name = "sdvn_name")
    var sdvnName: String = "",
        // 设备名称
        @ColumnInfo(name = "name")
        var name: String = "",
        // 简介
        @ColumnInfo(name = "plot")
        var plot: String = "",
        // 跟新时间
        @ColumnInfo(name = "updateTime")
        var updateTime: String = "",
        // 跟新时间
        @ColumnInfo(name = "type")
        var type: String = "",
        //
        @ColumnInfo(name = "movie_poster_bg")
        var movie_poster_bg: String = "",
        //
        @ColumnInfo(name = "movie_poster_cover")
        var movie_poster_cover: String = "",
        //
        @ColumnInfo(name = "movie_poster_logo")
        var movie_poster_logo: String = "",
        //
        @ColumnInfo(name = "movie_poster_wall")
        var movie_poster_wall: String = ""
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var uid: Long = 0
}
