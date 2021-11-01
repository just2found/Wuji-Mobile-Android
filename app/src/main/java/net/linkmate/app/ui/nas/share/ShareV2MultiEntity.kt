package net.linkmate.app.ui.nas.share

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.nas.iface.RVItemType.TYPE_LEVEL_1
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import java.util.*


class ShareV2MultiEntity( var shareElementV2: ShareElementV2) : AbstractExpandableItem<SFDownloadEntity>(), MultiItemEntity {

    override fun getItemType(): Int {
        return TYPE_LEVEL_1
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ShareV2MultiEntity) return false
        val that = o as ShareV2MultiEntity?
        return shareElementV2 == that!!.shareElementV2
    }

    override fun hashCode(): Int {
        return Objects.hash(shareElementV2)
    }

    override fun getLevel(): Int {
        return 1
    }


}
