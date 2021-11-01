package net.linkmate.app.ui.simplestyle.device.self_check

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.device.self_check.data.DiskCheckUiInfo

/**
create by: 86136
create time: 2021/1/31 19:47
Function description:
 */

class RdAddFileAdapter(layoutId: Int = R.layout.item_disk_check_report) : BaseQuickAdapter<DiskCheckUiInfo, BaseViewHolder>(layoutId) {


    override fun convert(baseViewHolder: BaseViewHolder, diskCheckUiInfo: DiskCheckUiInfo) {
        val context = baseViewHolder.itemView.context;
        val hour = context.getString(R.string.hours)
        val disk = context.getString(R.string.disk)
        baseViewHolder.setText(R.id.disk_number_tv, "${disk}${baseViewHolder.adapterPosition+1}")//${diskCheckUiInfo?.solt}
        baseViewHolder.setText(R.id.disk_name_content_tv, " ${diskCheckUiInfo?.diskName?:""}")
        baseViewHolder.setText(R.id.disk_size_content_tv, diskCheckUiInfo?.diskSize?:"")
        baseViewHolder.setText(R.id.disk_serial_number_content_tv, diskCheckUiInfo?.serialNumber?:"")
        baseViewHolder.setText(R.id.disk_time_content_tv, " ${diskCheckUiInfo?.useTime?:"0"}$hour")
        if (diskCheckUiInfo.goodCondition) {
            baseViewHolder.setImageResource(R.id.disk_status_img, R.drawable.ic_disk_normal)
            baseViewHolder.setText(R.id.disk_status_tv, baseViewHolder.itemView.context.getString(R.string.disk_status_good))
            baseViewHolder.setTextColor(R.id.disk_status_tv, ContextCompat.getColor(baseViewHolder.itemView.context, R.color.color_49B811))
        } else {
            baseViewHolder.setImageResource(R.id.disk_status_img, R.drawable.ic_disk_abnormal)
            baseViewHolder.setText(R.id.disk_status_tv, baseViewHolder.itemView.context.getString(R.string.disk_status_abnormal))
            baseViewHolder.setTextColor(R.id.disk_status_tv, ContextCompat.getColor(baseViewHolder.itemView.context, R.color.color_D1061E))
        }


    }


}