package io.weline.repo.data.model

import com.google.gson.annotations.SerializedName


/**
 *
 * @Description: java类作用描述
 * @Author: todo2088
 * @CreateDate: 2021/1/27 13:46
 */
data class ServiceStatus(
        @field:SerializedName("error")
        val error: Error,
        @field:SerializedName("name")
        val name: String,
        @field:SerializedName("status")
        val status: Int
) {
    fun isFileService(): Boolean {
        return name == "file"
    }

    fun isFileShareService(): Boolean {
        return name == "fileshare"
    }

    fun isAvailable(): Boolean {
        return status == 1
    }
}

data class NasVersion(
        @field:SerializedName("buildstamp")
        val buildstamp: String,
        @field:SerializedName("version")
        val version: String
) {
    fun isNasV3(): Boolean {
        return version.startsWith("v3") || try {
            version.get(1).toInt() >= 3
        } catch (e: Exception) {
            false
        }
    }
}