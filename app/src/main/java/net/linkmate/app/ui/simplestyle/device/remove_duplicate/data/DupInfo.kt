package net.linkmate.app.ui.simplestyle.device.remove_duplicate.data

import androidx.annotation.Keep
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.OnSelectFileListener
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.RdSelectAdapter
@Keep
data class DupInfo(@field:SerializedName("id") val id: Long,
                   @field:SerializedName("name") val name: String,
                   @field:SerializedName("size") val size: Long,
                   @field:SerializedName("path") val path: String,
                   @field:SerializedName("time") val time: Long,
                   var sizeStr: String?,
                   var onSelectFileListener: OnSelectFileListener,
                   var timeStr: String?
) : MultiItemEntity {


    override fun getItemType(): Int {
        return RdSelectAdapter.ITEM
    }

}

