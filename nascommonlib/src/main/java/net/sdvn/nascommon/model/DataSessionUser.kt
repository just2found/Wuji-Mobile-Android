package net.sdvn.nascommon.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.sdvn.nascommon.db.objecbox.UserInfo
import net.sdvn.nascommon.model.oneos.DevAttrInfo

@Keep
data class DataSessionUser(@SerializedName("session") val session: String?,
                           @SerializedName("user") val user: UserInfo,
                           @SerializedName("devinfo") val devAttrInfo: DevAttrInfo?)

