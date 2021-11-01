package io.weline.repo.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** 

Created by admin on 2020/10/28,19:24

 */
@Keep
data class GOsFile(
        @field:SerializedName("type")
        var type: Int,
        @field:SerializedName("path")
        var path: String,
        @field:SerializedName("name")
        var name: String,
        @field:SerializedName("size")
        var size: Long,
        @field:SerializedName("md5")
        var md5: String?,
        @field:SerializedName("ftype")
        var ftype: String?,
        @field:SerializedName("tag")
        var tag: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeString(path)
        parcel.writeString(name)
        parcel.writeLong(size)
        parcel.writeString(md5)
        parcel.writeString(ftype)
        parcel.writeString(tag)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GOsFile> {
        override fun createFromParcel(parcel: Parcel): GOsFile {
            return GOsFile(parcel)
        }

        override fun newArray(size: Int): Array<GOsFile?> {
            return arrayOfNulls(size)
        }
    }
}