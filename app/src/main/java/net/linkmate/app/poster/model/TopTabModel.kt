package net.linkmate.app.poster.model

import androidx.room.*

/**
 * Create by Admin on 2021-07-05-16:26
 */

const val TYPE_NFO = 0
const val TYPE_ALL = 1
const val TYPE_FOLDER = 2

@Entity(tableName = "top_tabs", indices = [Index(value = ["index","device_id"], unique = true)])
@TypeConverters(MediaConverters::class)
data class TopTabModel(
    // 当前导航对应的文件夹类别
    @ColumnInfo(name = "folder_type")
    val folderType: Int,
    // 当前排序
    @ColumnInfo(name = "index")
    val index: Int,
    // 导航名称
    @ColumnInfo(name = "name")
    val name: String,
    // 当前nfo筛选类别
    @ColumnInfo(name = "movie_type_filter")
    val movieTypeFilter: String? = null,
    // 类别筛选条件集合
    @ColumnInfo(name = "movie_condition_filter")
    val movieConditionFilter: ArrayList<String>? = null,
    // folder lists.txt
    @ColumnInfo(name = "folder_movie_list")
    val folderMovieList: ArrayList<String>? = null,
    // 当前tab所属设备
    @ColumnInfo(name = "device_id")
    var deviceId: String? = null
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var uid: Long = 0

    // 关联键
    var topId: String = ""

    @Ignore
    var posterData: ArrayList<MediaInfoModel>? = null
}
