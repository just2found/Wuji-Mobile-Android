package net.linkmate.app.ui.nas.torrent

import android.widget.TextView
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.SectionEntity
import io.weline.repo.torrent.constants.BTStatus
import io.weline.repo.torrent.data.BTItem
import net.linkmate.app.R
import net.linkmate.app.util.FormatUtils
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.NumberProgressBar

/** 

Created by admin on 2020/7/13,19:22

 */
class TorrentMapAdapter : BaseSectionQuickAdapter<SectionEntity<BTItem>, BaseViewHolder>(
        R.layout.layout_item_torrent, R.layout.layout_timeline_header, null) {
    override fun convertHead(holder: BaseViewHolder, sectionEntity: SectionEntity<BTItem>) {
        holder.getView<TextView>(R.id.header).text = sectionEntity.header
    }

    override fun convert(holder: BaseViewHolder, sectionEntity: SectionEntity<BTItem>) {
        val btItem = sectionEntity.t
        btItem?.let { btItem ->
            holder.setText(R.id.rv_list_txt_name, btItem.name)
            val fmtFileSize = FileUtils.fmtFileSize(btItem.totalLen)
            val fmtTimeByZone = FileUtils.fmtTimeByZone(btItem.timestamp)
            if (btItem.isMainSeed) {
                holder.setText(R.id.rv_list_txt_size, fmtFileSize)
                holder.setText(R.id.rv_list_txt_time, fmtTimeByZone)
                holder.setGone(R.id.rv_list_progressbar, false)
            } else {
                when (btItem.status) {
                    BTStatus.DOWNLOADING -> {
                        if (btItem.downloadLen > 0) {
                            val progress = ((btItem.downloadLen * 1f / btItem.totalLen * 100f) + 0.5f).toInt()
                            holder.getView<NumberProgressBar>(R.id.rv_list_progressbar).progress = progress
                            holder.setText(R.id.rv_list_txt_size,
                                    FileUtils.fmtFileSize(btItem.downloadLen) +
                                            "/" + fmtFileSize)
                            val eta = if (btItem.speed > 0) FormatUtils.getUptime((btItem.totalLen - btItem.downloadLen) / btItem.speed) else "∞"
                            holder.setText(R.id.rv_list_txt_time, FileUtils.fmtFileSpeed2(btItem.speed) + "/s " + eta)
                        } else {
                            holder.setText(R.id.rv_list_txt_size, fmtFileSize)
                            holder.setText(R.id.rv_list_txt_time, fmtTimeByZone)
                        }
                        holder.setGone(R.id.rv_list_progressbar, true)
                    }
                    BTStatus.STOPPED -> {
                        if (btItem.downloadLen > 0) {
                            val progress = ((btItem.downloadLen * 1f / btItem.totalLen * 100f) + 0.5f).toInt()
                            holder.getView<NumberProgressBar>(R.id.rv_list_progressbar).progress = progress
                            holder.setText(R.id.rv_list_txt_size,
                                    FileUtils.fmtFileSize(btItem.downloadLen) +
                                            "/" + fmtFileSize)
                        }
                        holder.setGone(R.id.rv_list_progressbar, btItem.downloadLen > 0)
                        holder.setText(R.id.rv_list_txt_time, R.string.paused)
                    }
                    else -> {
                        holder.setText(R.id.rv_list_txt_size, fmtFileSize)
                        holder.setText(R.id.rv_list_txt_time, fmtTimeByZone)
                        holder.setGone(R.id.rv_list_progressbar, false)
                    }
                }
            }
        }
    }
}