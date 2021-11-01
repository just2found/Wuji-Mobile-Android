package net.sdvn.nascommon.db.objboxkt

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Uid
@Keep
@Entity
data class BTItem(
        @Id
        var id: Long = 0,
        var userId: String,
        //traceServer id
        var devId: String,
        //network id
        var netId: String,
        @SerializedName("dl_ticket")
        var dlTicket: String,
        @SerializedName("bt_ticket")
        var btTicket: String,
        @SerializedName("download_len")
        var downloadLen: Long,
        @SerializedName("name")
        var name: String,
        @SerializedName("speed")
        @Uid(6253916565778302371L)
        var speed: Long,
        @SerializedName("status")
        var status: Int,
        @SerializedName("total_len", alternate = ["length"])
        var totalLen: Long,
        @SerializedName("user")
        var user: String,
        @SerializedName("seeding")
        var seeding: Boolean,
        @SerializedName("host_name")
        var remoteServer: String,
        @SerializedName("timestamp", alternate = ["createdate"])
        var timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BTItem) return false

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (devId != other.devId) return false
        if (netId != other.netId) return false
        if (dlTicket != other.dlTicket) return false
        if (btTicket != other.btTicket) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + devId.hashCode()
        result = 31 * result + netId.hashCode()
        result = 31 * result + dlTicket.hashCode()
        result = 31 * result + btTicket.hashCode()
        return result
    }

}
