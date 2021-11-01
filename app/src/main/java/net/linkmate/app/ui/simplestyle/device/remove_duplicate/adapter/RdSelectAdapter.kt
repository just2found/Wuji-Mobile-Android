package net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter

import android.text.TextUtils
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupBottom
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupHead
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupInfo
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupTotal
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.CheckableImageButton
import java.util.*

/**
create by: 86136
create time: 2021/1/31 22:49
Function description:
 */

class RdSelectAdapter(data: List<MultiItemEntity>?, private val mActivity: FragmentActivity) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    companion object {
        val TOTAL = 0 //总计
        val HEAD = 1 //头部
        val ITEM = 2 //条目
        val BOTTOM = 3 //底部
    }


    init {
        addItemType(TOTAL, R.layout.item_rd_select_total)
        addItemType(HEAD, R.layout.item_rd_select_head)
        addItemType(ITEM, R.layout.item_rd_select_item)
        addItemType(BOTTOM, R.layout.item_rd_select_bottom)
    }


    override fun convert(helper: BaseViewHolder, item: MultiItemEntity?) {
        when (helper.itemViewType) {
            TOTAL -> {
                if (item is DupTotal) {
                    val str = helper.itemView.context.getString(R.string.duplicate_removal_total);
                    helper.setText(R.id.text, String.format(str, item.groupSize, item.fileSize))
                }
            }
            HEAD -> {
                if (item is DupHead) {
                    if (TextUtils.isEmpty(item.imgUrl)) {
                        helper.setImageResource(R.id.img, item.defImg)
                    } else {
                        val view = helper.getView<ImageView>(R.id.img)
                        Glide.with(view)
                                .asBitmap()
                                .centerCrop()
                                .placeholder(item.defImg)
                                .load(item.imgUrl)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .into(view)
                    }
                    helper.setText(R.id.count_tv, item.count)
                    helper.setText(R.id.size_tv, item.size)
                }
            }
            ITEM -> {
                if (item is DupInfo) {
                    helper.getView<CheckableImageButton>(R.id.checkBox2).setOnCheckedChangeListener { v, b ->
                        item.onSelectFileListener.onSelectFileChange(item, b)
                    }
                    helper.itemView.setOnClickListener {
                        val checkBox = helper.itemView.findViewById<CheckableImageButton>(R.id.checkBox2)
                        checkBox.isChecked=!checkBox.isChecked
                    }
                    helper.setChecked(R.id.checkBox2, item.onSelectFileListener.hasSelect(item))
                    helper.setText(R.id.file_name_tv, item.name)
                    item.timeStr=item.timeStr?: MyConstants.sdf.format(Date(item.time * 1000))
                    item.sizeStr=item.sizeStr?: FileUtils.fmtFileSize(item.size)
                    helper.setText(R.id.file_time_tv, item.timeStr)
                    helper.setText(R.id.file_size_tv, item.sizeStr)
                }
            }
            BOTTOM -> {
                if (item is DupBottom) {

                }
            }
        }
    }


}