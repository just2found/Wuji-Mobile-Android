package net.linkmate.app.ui.nas.transfer

import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.R

class HeaderEntity(var mHeader: String) : MultiItemEntity {
    var rightResId: Int = 0
    override fun getItemType(): Int {
        return R.layout.item_line_string
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HeaderEntity) return false

        if (mHeader != other.mHeader) return false
        if (rightResId != other.rightResId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mHeader.hashCode()
        result = 31 * result + rightResId
        return result
    }

}
