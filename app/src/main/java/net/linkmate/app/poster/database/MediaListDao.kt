package net.linkmate.app.poster.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.linkmate.app.poster.model.MediaInfoModel


@Dao
interface MediaListDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertList(list: List<MediaInfoModel>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg users: MediaInfoModel)

  @Query("DELETE FROM medias WHERE device_id = :deviceId")
  fun delete(deviceId: String)

  @Query("SELECT * FROM medias WHERE device_id = :deviceId")
  fun getMediasWithDeviceId(deviceId: String): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND title IN (:titles)")
  fun getMediasWithTitle(deviceId: String, titles: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND year IN (:values)")
  fun getMediasWithYear(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND showtitle IN (:values)")
  fun getMediasWithShowTitle(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND premiered IN (:values)")
  fun getMediasWithPremiered(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND director IN (:values)")
  fun getMediasWithDirector(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND video IN (:values)")
  fun getMediasWithVideo(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND plot IN (:values)")
  fun getMediasWithPlot(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND original_filename IN (:values)")
  fun getMediasWithOriginalFilename(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND rating > (:value)")
  fun getMediasWithRating(deviceId: String,value: Float): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND id IN (:values)")
  fun getMediasWithMovieId(deviceId: String,values: ArrayList<String>): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND genre LIKE '%' || :value || '%' ")
  fun getMediasWithGenre(deviceId: String,value: String): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND country LIKE '%' || :value || '%' ")
  fun getMediasWithCountry(deviceId: String,value: String): List<MediaInfoModel>

  @Query("SELECT * FROM medias WHERE device_id = :deviceId AND actor LIKE '%' || :value || '%' ")
  fun getMediasWithActor(deviceId: String,value: String): List<MediaInfoModel>


}