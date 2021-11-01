package io.weline.repo.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/** 

Created by admin on 2021/1/6,23:38

 */
@Keep
data class DataDiskStatus(
        @SerializedName("status")
        var status: String
) {
    fun isOK(): Boolean {
        return "ok".equals(status, true)
    }

    fun isFailed(): Boolean {
        return "failed".equals(status, true)
    }

    fun isRunning(): Boolean {
        return "running".equals(status, true)
    }

}