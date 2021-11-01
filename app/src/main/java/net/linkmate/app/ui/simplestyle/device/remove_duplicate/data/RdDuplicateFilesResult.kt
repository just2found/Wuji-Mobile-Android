package net.linkmate.app.ui.simplestyle.device.remove_duplicate.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
create by: 86136
create time: 2021/1/31 22:02
Function description:
 */
@Keep
data class RdDuplicateFilesResult(@field:SerializedName("count") val filenum: Int,// 表示找到总共多少个指定类型的文件(如请求中type为pic则找所有图片文件)
                                  @field:SerializedName("num") val totalfiles: Int,  // 表示多少个文件有重复项
                                  @field:SerializedName("share_path_type") val share_path_type: Int, // 表示具体重复文件的个数
                                  @field:SerializedName("dupinfo") val dupinfos: List<MutableList<DupInfo>>
        //-------下面这二个是删除的返回结果
        //@field:SerializedName("totalDeleted") val totalDeleted: Int, // 表示清除的文件个数
        //@field:SerializedName("totalSize") val totalSize: Int  // 表示清除的文件个数
)
