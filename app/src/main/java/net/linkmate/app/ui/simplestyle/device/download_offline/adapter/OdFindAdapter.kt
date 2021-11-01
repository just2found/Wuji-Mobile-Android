package net.linkmate.app.ui.simplestyle.device.download_offline.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.utils.FileUtils
import java.util.*
import java.util.concurrent.TimeUnit

/**
create by: 86136
create time: 2021/3/3 14:56
Function description:
 */

class OdFindAdapter : BaseQuickAdapter<OneOSFile, BaseViewHolder>(R.layout.item_offline_retrieval) {

    override fun convert(baseViewHolder: BaseViewHolder, oneOSFile: OneOSFile) {
        if (oneOSFile.isDirectory()) {
            baseViewHolder.setText(R.id.file_size_tv, "")
            baseViewHolder.setImageResource(R.id.fileImage, R.drawable.icon_file_folder)
        } else {
            baseViewHolder.setText(R.id.file_size_tv, FileUtils.fmtFileSize(oneOSFile.getSize()))
            baseViewHolder.setImageResource(R.id.fileImage, R.drawable.icon_file_torrent)
        }
        baseViewHolder.setText(R.id.fileName, oneOSFile.getName())
        if (oneOSFile.getTime() != 0L) {
            baseViewHolder.setText(R.id.file_time_tv, MyConstants.sdf.format(Date(oneOSFile.getTime() * 1000)))
        } else {
            baseViewHolder.setText(R.id.file_time_tv, MyConstants.sdf.format(Date(System.currentTimeMillis())))
        }
    }


}