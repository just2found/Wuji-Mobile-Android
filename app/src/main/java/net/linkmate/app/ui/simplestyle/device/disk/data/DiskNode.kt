package net.linkmate.app.ui.simplestyle.device.disk.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

//{"slot":5,"device":"/dev/sda","main":1,"mode":"raid5","status":"PASSED","size":"1.8T"}
@Keep
data class DiskNode(@field:SerializedName("slot") val slot: Int,
                    @field:SerializedName("device") val device: String,
                    @field:SerializedName("mode") val mode: String,
                    @field:SerializedName("status") val status: String,
                    @field:SerializedName("size") val size: String,
                    @field:SerializedName("main") var main: Int)
//"slot":1,
//"device":"/dev/sda",
//"mode" : "raid"
//"status":" PASSED",
//"size":"1.8T"
//"main":1 如果是main=1  说明盘正在使用中 extend 选 main=0的 磁盘