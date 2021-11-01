package io.weline.repo.torrent.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BtSession(
        @SerializedName("session")
        val session: String,
        @SerializedName("username")
        val username: String,
        var timestamp: Long,
        var devId: String)