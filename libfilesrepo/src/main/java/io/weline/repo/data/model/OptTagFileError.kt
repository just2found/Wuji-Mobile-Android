package io.weline.repo.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/5/12
 */

//{"failed":[{"path":"/新建文件夹7_2_3jjhhhhhhhhhssjsjsjshdbbbdd","share_path_type":0,"error":{"code":-40058,"msg":"该文件不存在此标签"}}]}
@Keep
data class OptTagFileError(
        @field:SerializedName("failed")
        val failed: List<TagFileError>) {

}

@Keep
data class TagFileError(
        @field:SerializedName("path")
        val path: String,
        @field:SerializedName("share_path_type")
        val sharePathType: Int,
        @field:SerializedName("error")
        val error: Error) {

}