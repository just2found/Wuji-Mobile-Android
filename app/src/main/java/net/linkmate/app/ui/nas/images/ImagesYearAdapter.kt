package net.linkmate.app.ui.nas.images

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R

/**
 *
 * @Description: 相册Adapter
 * @Author: todo2088
 * @CreateDate: 2021/1/29 17:33
 */

class ImagesYearAdapter(private val photosViewModel: PhotosViewModel)
    : BaseQuickAdapter<OneFileModel, BaseViewHolder>(R.layout.item_photo_list) {
    override fun convert(holder: BaseViewHolder, item: OneFileModel?) {
        item?.let { item ->
            holder.setText(R.id.tv_content, "(${item.ext1})")
            holder.setText(R.id.tv_content_tips, "(${item.ext})")
            val iconView = holder.getView<ImageView>(R.id.icon)
            photosViewModel.loadImage(iconView, item)
        } ?: kotlin.run {

        }
    }

    override fun convertPayloads(helper: BaseViewHolder, item: OneFileModel?, payloads: MutableList<Any>) {
        super.convertPayloads(helper, item, payloads)
    }
}

