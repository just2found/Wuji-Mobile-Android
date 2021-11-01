package net.linkmate.app.ui.simplestyle.device.disk.data

import com.google.gson.annotations.SerializedName

//"slot": "hdd0", "status": "on"
data class DiskPower(
        @field: SerializedName("slot") val slot: Int,
        @field: SerializedName("status") val status: String) {

    fun diskIsOn(): Boolean {
        return status == "on"
    }
}
