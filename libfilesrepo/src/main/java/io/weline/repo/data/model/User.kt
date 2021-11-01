package io.weline.repo.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * @author Raleigh.Luo
 * date：20/9/17 13
 * describe：
 */

@Keep
data class User(
        @field:SerializedName("admin")
        val admin: Int = 0,
        @field:SerializedName("gid")
        val gid: Int = 0,
        @field:SerializedName(value = "mark", alternate = ["remark"])
        val mark: String?,
        @field:SerializedName("uid")
        val uid: Int = 0,
        @field:SerializedName("username")
        val username: String?,
        @field:SerializedName("permissions")
        var permissions: List<PermissionsModel>?)

@Keep
data class Users(
        @field:SerializedName("users")
        val users: List<User>?)

@Keep
data class PermissionsModel(@field:SerializedName("share_path_type")
                            var sharePathType: Int,
                            @field:SerializedName("perm")
                            var perm: Int
) : Serializable {
    /**
     * share_path_type : 0
     * perm : 3
     */
    val isWriteable: Boolean
        get() = (perm and (1 shl 1)) != 0
    val isReadable: Boolean
        get() = (perm and 1) != 0

    fun setReadable(isReadable: Boolean): Int {
        return (if (isReadable) perm or 1 else perm xor 1).also { perm = it }
    }

    fun setWriteable(isWriteable: Boolean): Int {
        return (if (isWriteable) perm or (1 shl 1) else perm xor (1 shl 1)).also { perm = it }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is PermissionsModel) return false
        val that = o
        return sharePathType == that.sharePathType &&
                perm == that.perm
    }

    override fun hashCode(): Int {
        return Objects.hash(sharePathType, perm)
    }

    companion object {
        fun getAllowPerm(isAllow: Boolean): Int {
            return if (isAllow) 3 else 0
        }

        fun getReadPerm(isReadable: Boolean): Int {
            return if (isReadable) 1 else 0
        }
    }
}