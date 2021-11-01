package net.linkmate.app.ui.nas.share

import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.nas.iface.RVItemType.TYPE_LEVEL_2
import net.sdvn.nascommon.db.objecbox.SFDownload
import java.util.*


class SFDownloadEntity( val sfDownload: SFDownload) : MultiItemEntity {

    override fun getItemType(): Int {
        return TYPE_LEVEL_2
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is SFDownloadEntity) return false
        val that = o as SFDownloadEntity?
        return sfDownload == that!!.sfDownload
    }

    override fun hashCode(): Int {
        return Objects.hash(sfDownload)
    }
}
