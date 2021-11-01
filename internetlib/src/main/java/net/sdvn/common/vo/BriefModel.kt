package net.sdvn.common.vo

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import net.sdvn.common.repo.BriefRepo

/**
 * @author Raleigh.Luo
 * date：21/5/6 11
 * describe：设备简介
 */
@Keep
@Entity
data class BriefModel(@Id
                      var autoIncreaseId: Long = 0,
                      var deviceId: String = "",//设备id, 主键
                      var For: String = BriefRepo.FOR_DEVICE,//类型，圈子或设备类型, 忽略json解析字段
                      var brief: String? = null,//简介
                      var portraitPath: String? = null,//头像地址
                      var backgroudPath: String? = null,//背景地址
                      var briefTimeStamp: Long? = null,//简介时间戳
                      var portraitTimeStamp: Long? = null,//头像时间戳
                      var backgroudTimeStamp: Long? = null//背景时间戳
)