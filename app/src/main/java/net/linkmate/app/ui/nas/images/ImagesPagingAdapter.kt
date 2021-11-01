package net.linkmate.app.ui.nas.images

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.ui.viewmodel.GenFileUrl
import java.util.*

/**
 *
 * @Description: 相册Adapter
 * @Author: todo2088
 * @CreateDate: 2021/1/29 17:33
 */

class ImagesPagingAdapter : PagedListAdapter<PhotoModel, BaseViewHolder>(PhotoModelDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutResId = when (viewType) {
            ViewTypeHeader -> {
                R.layout.item_photo_header
            }
            ViewTypeList -> {
                R.layout.item_photo_list
            }
            else -> {
                R.layout.item_photo_grid
            }
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return BaseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        getItem(position)?.let { item ->
            when (item.viewType) {
                ViewTypeHeader -> {
                    holder.setText(R.id.tv_content, item.tag)
                }
                ViewTypeList -> {
                    holder.setText(R.id.tv_content, item.tag)
                    holder.setText(R.id.tv_content_tips, item.ext)
                    val iconView = holder.getView<ImageView>(R.id.icon)
                    loadImage(iconView, item)
                }
                else -> {
                    val iconView = holder.getView<ImageView>(R.id.icon)
                    loadImage(iconView, item)
                }
            }
        }
    }

    private fun loadImage(imageView: ImageView, item: PhotoModel) {
        val model: Any? = GenFileUrl.getGlideModeTb(item.devId,item.pathType,item.path)
        Glide.with(imageView.context)
                .asBitmap()
                .centerCrop()
                .load(model)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.icon_device_img)
                .into(imageView)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.viewType ?: ViewTypeGrid
    }

    companion object {
        const val ViewTypeGrid = 0
        const val ViewTypeList = 1
        const val ViewTypeHeader = 2
    }
}

data class PhotoModel(
        val viewType: Int,
        val id: Long, var devId: String, var pathType: Int,
        var path: String, var name: String, var size: Long,
        var time: Long, var tag: String, var ext: String,
        var isDir: Boolean)

object PhotoModelDiff : DiffUtil.ItemCallback<PhotoModel>() {
    override fun areItemsTheSame(oldItem: PhotoModel, newItem: PhotoModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PhotoModel, newItem: PhotoModel): Boolean {
        return Objects.equals(oldItem, newItem)
    }
}