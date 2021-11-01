package net.linkmate.app.ui.nas.share


import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.nas.iface.RVItemType.TYPE_LEVEL_0


class HeaderMultiEntity( val header: String) : AbstractExpandableItem<ShareV2MultiEntity>(), MultiItemEntity {

    override fun getItemType(): Int {
        return TYPE_LEVEL_0
    }

    override fun getLevel(): Int {
        return 0
    }
}
