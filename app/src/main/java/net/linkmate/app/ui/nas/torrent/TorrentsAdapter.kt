package net.linkmate.app.ui.nas.torrent

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.weline.repo.torrent.constants.BTStatus
import io.weline.repo.torrent.data.BTItem
import net.linkmate.app.R
import net.linkmate.app.util.FormatUtils
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.NumberProgressBar

class TorrentsAdapter()
    : BaseQuickAdapter<BTItem, BaseViewHolder>(R.layout.layout_item_torrent) {
    override fun convert(holder: BaseViewHolder, btItem: BTItem?) {
        btItem?.let { btItem ->
            holder.setText(R.id.rv_list_txt_name, btItem.name)
            when (btItem.status) {
                BTStatus.DOWNLOADING -> {
                    holder.setGone(R.id.rv_list_progressbar, true)
                    if (btItem.downloadLen > 0) {
                        val progress = ((btItem.downloadLen * 1f / btItem.totalLen * 100f) + 0.5f).toInt()
                        holder.getView<NumberProgressBar>(R.id.rv_list_progressbar).progress = progress
                        holder.setText(R.id.rv_list_txt_size,
                                FileUtils.fmtFileSize(btItem.downloadLen) +
                                        "/" + FileUtils.fmtFileSize(btItem.totalLen))
                    }
                    val eta = if (btItem.speed > 0) FormatUtils.getUptime((btItem.totalLen - btItem.downloadLen) / btItem.speed) else "∞"
                    holder.setText(R.id.rv_list_txt_time, FileUtils.fmtFileSpeed2(btItem.speed) + "/s " + eta)
                }
                BTStatus.STOPPED -> {
                    holder.setText(R.id.rv_list_txt_size,
                            FileUtils.fmtFileSize(btItem.downloadLen) +
                                    "/" + FileUtils.fmtFileSize(btItem.totalLen))
                    holder.setText(R.id.rv_list_txt_time, R.string.paused)
                    holder.setGone(R.id.rv_list_progressbar, false)
                }
                else -> {
                    holder.setText(R.id.rv_list_txt_size, FileUtils.fmtFileSize(btItem.totalLen))
                    holder.setText(R.id.rv_list_txt_time, FileUtils.fmtTimeByZone(btItem.timestamp))
                    holder.setGone(R.id.rv_list_progressbar, false)
                }
            }
        }
    }
}
