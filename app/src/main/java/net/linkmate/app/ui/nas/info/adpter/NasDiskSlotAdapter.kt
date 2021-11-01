package net.linkmate.app.ui.nas.info.adpter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.device.self_check.data.HdsInfo
import net.linkmate.app.view.ActivityItemLayout1

/**
create by: 86136
create time: 2021/4/22 11:20
Function description:
 */


//listLayoutID 列表展示时候的Id   //gridLayoutID九宫格展示时候的ID
class NasDiskSlotAdapter() :
    BaseQuickAdapter<HdsInfo, BaseViewHolder>(R.layout.item_disk_solt_info) {
    val noDisk by lazy {
        mContext.getString(R.string.disk_not_inserted)
    }

    override fun convert(viewHolder: BaseViewHolder, data: HdsInfo?) {
        data?.let { hdsInfo ->
            val solt = if (data.slot > 0) {
                data.slot
            } else {
                viewHolder.adapterPosition + 1
            }

            viewHolder.setText(
                R.id.hd_name_tv,
                "${mContext.getString(R.string.disk_name_pre)}${solt}"
            )
            hdsInfo.smartinfo?.let { smartinfo ->
                viewHolder.getView<ActivityItemLayout1>(R.id.sn_ail).setTips(smartinfo.serialNumber)
                viewHolder.getView<ActivityItemLayout1>(R.id.product_version_ail)
                    .setTips(smartinfo.deviceModel)
                smartinfo.userCapacity?.let { userCapacity ->
                    if (userCapacity.contains("[") && userCapacity.contains("]")) {
                        viewHolder.getView<ActivityItemLayout1>(R.id.capacity_ail).setTips(
                            userCapacity.substring(
                                userCapacity.indexOf("[") + 1,
                                userCapacity.indexOf("]")
                            )
                        )
                    }
                }
            }
            if (hdsInfo.smartinfo == null) {
                viewHolder.getView<ActivityItemLayout1>(R.id.sn_ail).setTips(noDisk)
                viewHolder.getView<ActivityItemLayout1>(R.id.product_version_ail)
                    .setTips(noDisk)
                viewHolder.getView<ActivityItemLayout1>(R.id.capacity_ail).setTips(
                    noDisk
                )
            }

        }
    }


}