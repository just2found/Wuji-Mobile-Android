package io.weline.repo.files.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DataSessionUser( @SerializedName("session") var session: String?,
                            @SerializedName("user") var userInfo: UserInfo)

