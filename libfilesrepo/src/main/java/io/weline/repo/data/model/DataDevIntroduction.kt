package io.weline.repo.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/** 

Created by admin on 2020/10/27,15:52

 */
@Keep
data class DataDevIntroduction(
        @field:SerializedName("id")
        var id: Long,
        @field:SerializedName("iconFile")
        var iconFile: GOsFile? = null,
        @field:SerializedName("bgFile")
        var bgFile: GOsFile? = null,
        @field:SerializedName("title")
        var title: String? = null,
        @field:SerializedName("subtitle")
        var subtitle: String? = null,
        @field:SerializedName("content")
        var content: String? = null,
        @field:SerializedName("mediaResources")
        var mediaResources: List<GOsFile>? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readParcelable(GOsFile::class.java.classLoader),
            parcel.readParcelable(GOsFile::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createTypedArrayList(GOsFile)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeParcelable(iconFile, flags)
        parcel.writeParcelable(bgFile, flags)
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(content)
        parcel.writeTypedList(mediaResources)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataDevIntroduction> {
        override fun createFromParcel(parcel: Parcel): DataDevIntroduction {
            return DataDevIntroduction(parcel)
        }

        override fun newArray(size: Int): Array<DataDevIntroduction?> {
            return arrayOfNulls(size)
        }
    }
}
