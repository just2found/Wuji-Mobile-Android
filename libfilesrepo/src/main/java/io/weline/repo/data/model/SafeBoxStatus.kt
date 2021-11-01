package io.weline.repo.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
create by: 86136
create time: 2021/4/1 15:32
Function description:
 */
@Keep
data class SafeBoxStatus(
        @SerializedName("lock") val lock: Int,//1.lock表示保险箱是否锁住，1上锁，0解锁
        @SerializedName("question") val question: String//queation表示当前用户所建保险箱的密保问题，”如果question为空，表示保险箱没有初始化”
)