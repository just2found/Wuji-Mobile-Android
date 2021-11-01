package net.linkmate.app.poster.model

import androidx.room.*
import java.io.Serializable


@Entity(tableName = "medias", indices = [Index(value = ["path"], unique = true)])
@TypeConverters(MediaConverters::class)
data class MediaInfoModel(
  @ColumnInfo(name = "session")
  var session: String = "",
  @ColumnInfo(name = "local_session")
  var localSession: String = "",
  @ColumnInfo(name = "ip")
  var ip: String = "",
  @ColumnInfo(name = "path")
  var path: String = "",
  @ColumnInfo(name = "device_id")
  var deviceId: String = "",
  @ColumnInfo(name = "media_size")
  var mediaSize: Long = 0,
  @ColumnInfo(name = "title")
  var title: String = "",
  @ColumnInfo(name = "showtitle")
  var showTitle: String = "",
  @ColumnInfo(name = "premiered")
  var premiered: String = "",
  @ColumnInfo(name = "year")
  var year: String = "",
  @ColumnInfo(name = "runtime")
  var runtime: String = "",
  @ColumnInfo(name = "director")
  var director: String = "",
  @ColumnInfo(name = "video")
  var nfoVideo: String = "",
  @ColumnInfo(name = "plot")
  var plot: String = "",
  @ColumnInfo(name = "original_filename")
  var originalFileName: String = "",
  @ColumnInfo(name = "rating")
  var rating: Float = 0F,
  @ColumnInfo(name = "id")
  var movieId: String = "",
  @ColumnInfo(name = "genre")
  var genre: ArrayList<String>? = null,
  @ColumnInfo(name = "country")
  var country: ArrayList<String>? = null,
  @ColumnInfo(name = "fanart_list")
  var fanartList: ArrayList<String>? = null,
  @ColumnInfo(name = "poster_list")
  var posterList: ArrayList<String>? = null,
  @ColumnInfo(name = "trailer_list")
  var trailerList: ArrayList<String>? = null,
  @ColumnInfo(name = "actor")
  var actor: ArrayList<String>? = null,
  @ColumnInfo(name = "sample_list")
  var sampleList: ArrayList<String>? = null,
  @ColumnInfo(name = "video_set")
  var set: String = ""

) : Serializable {
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "_id")
  var uid: Long = 0

  @Ignore
  var videoList: ArrayList<MediaInfoModel>? = null
}
