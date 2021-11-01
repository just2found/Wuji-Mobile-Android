package net.linkmate.app.ui.simplestyle.device.remove_duplicate.data

import androidx.annotation.Keep
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.RdSelectAdapter

/**
create by: 86136
create time: 2021/1/31 23:02
Function description:
 */
@Keep
data class DupTotal(@field:SerializedName("groupSize") var groupSize: Int,
                    @field:SerializedName("fileSize") var fileSize: Int) : MultiItemEntity {

    override fun getItemType(): Int {
        return RdSelectAdapter.TOTAL
    }

}