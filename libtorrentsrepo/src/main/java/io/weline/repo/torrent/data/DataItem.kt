package io.weline.repo.torrent.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.weline.repo.torrent.constants.BTStatus

@Keep
data class BTItem(
        var id: Long = 0,
        var userId: String = "",
        //traceServer id
        @SerializedName("device_id")
        var remoteServerId: String = "",
        var devId: String = "",
        //network id
        @SerializedName("network_id")
        var netId: String = "",
        @SerializedName("dl_ticket")
        var dlTicket: String = "",
        @SerializedName("bt_ticket")
        var btTicket: String,
        @SerializedName("download_len", alternate = ["dl_length"])
        var downloadLen: Long = -1,
        @SerializedName("name")
        var name: String,
        @SerializedName("speed")
        var speed: Long = 0,
        @SerializedName("status")
        var status: Int = BTStatus.INIT,
        @SerializedName("total_len", alternate = ["length"])
        var totalLen: Long,
        @SerializedName("user")
        var user: String = "",
        @SerializedName("seeding")
        var seeding: Boolean = false,
        @SerializedName("host_name")
        var remoteServer: String,
        @SerializedName("timestamp", alternate = ["createdate"])
        var timestamp: Long,
        @SerializedName("is_main_seed")
        var isMainSeed: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BTItem) return false

        if (id != other.id) return false
//        if (userId != other.userId) return false
//        if (devId != other.devId) return false
//        if (netId != other.netId) return false
        if (dlTicket != other.dlTicket) return false
        if (btTicket != other.btTicket) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
//        result = 31 * result + userId.hashCode()
//        result = 31 * result + devId.hashCode()
//        result = 31 * result + netId.hashCode()
        result = 31 * result + dlTicket.hashCode()
        result = 31 * result + btTicket.hashCode()
        return result
    }

}

@Keep
data class BTItems(@SerializedName("list_items", alternate = ["resume_items", "stop_items", "cancel_items", "progress"])
                   val items: List<BTItem>?)


