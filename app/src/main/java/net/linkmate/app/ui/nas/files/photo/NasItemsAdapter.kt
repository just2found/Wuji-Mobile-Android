package net.linkmate.app.ui.nas.files.photo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.ui.adapter.AlbumItemsAdapter
import net.linkmate.app.R
import net.linkmate.app.ui.nas.images.OneFileModel
import net.linkmate.app.ui.viewmodel.GenFileUrl

/**
 * @author Raleigh.Luo
 * date：21/5/10 11
 * describe：底部菜单适配器
 */
class NasItemsAdapter(private val mContext: Context, private val viewModel: NasPhotosViewModel,
                      private val listener: AlbumItemsAdapter.OnClickListener) : RecyclerView.Adapter<NasItemsAdapter.NasItemsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NasItemsViewHolder {
        val view =
                LayoutInflater.from(mContext).inflate(R.layout.item_dialog_album_items_easy_photos, null, false)
        view.layoutParams =
                ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        return NasItemsViewHolder(view);
    }

    fun getSources(): List<OneFileModel>? {
        return viewModel.liveDataSummary.value?.data
    }

    override fun getItemCount(): Int {
        return (getSources()?.size ?: 0) + 1
    }

    private var padding = 0
    private var selectedPosition = 0
    override fun onBindViewHolder(holder: NasItemsViewHolder, position: Int) {
        if (this.padding == 0) {
            this.padding = holder.mRoot.paddingLeft
        }

        if (position == this.itemCount - 1) {
            holder.mRoot.setPadding(this.padding, this.padding, this.padding, this.padding)
        } else {
            holder.mRoot.setPadding(this.padding, this.padding, this.padding, 0)
        }
        if (position == 0) {//所有图片
            holder.tvAlbumName.text = mContext.getString(R.string.selector_folder_all_easy_photos)
            holder.tvAlbumPhotosCount.text = ""
            holder.ivAlbumCover.setImageResource(R.color.black)
        } else {
            val index = position - 1
            val item = this.getSources()!!.get(index)
            val model: Any? = GenFileUrl.getGlideModeTb(item.devId, item.getPathType(), item.getPath())
            Glide.with(holder.ivAlbumCover.context)
                    .asBitmap()
                    .centerCrop()
                    .load(model)
                    .placeholder(R.color.black)
                    .error(R.drawable.icon_device_img)
                    .into(holder.ivAlbumCover)
            holder.tvAlbumName.text = item.ext1
            holder.tvAlbumPhotosCount.text = item.ext
        }
        if (this.selectedPosition == position) {
            holder.ivSelected.visibility = View.VISIBLE
        } else {
            holder.ivSelected.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            var realPosition = position
            val tempSelected: Int = this@NasItemsAdapter.selectedPosition
            this@NasItemsAdapter.selectedPosition = position
            this@NasItemsAdapter.notifyItemChanged(tempSelected)
            this@NasItemsAdapter.notifyItemChanged(position)
            this@NasItemsAdapter.listener?.onAlbumItemClick(position, realPosition)
        }
    }

    fun getTitle(position: Int): String {
        if (position == 0) {
            return mContext.getString(R.string.selector_folder_all_easy_photos)

        } else {
            return this.getSources()?.get(position - 1)?.ext1 ?: ""
        }
    }

    fun setSelectedPosition(position: Int) {
        var realPosition = position
        val tempSelected = selectedPosition
        selectedPosition = position
        this.notifyItemChanged(tempSelected)
        this.notifyItemChanged(position)
        listener!!.onAlbumItemClick(position, realPosition)
    }

    fun getYear(position: Int): Long? {
        return if (position == 0) {
            null
        } else {
            getSources()?.get(position - 1)?.cttime
        }
    }


    class NasItemsViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivAlbumCover: ImageView
        var tvAlbumName: TextView
        var tvAlbumPhotosCount: TextView
        var ivSelected: ImageView
        var mRoot: ConstraintLayout

        init {
            ivAlbumCover = itemView.findViewById(R.id.iv_album_cover)
            tvAlbumName = itemView.findViewById(R.id.tv_album_name)
            tvAlbumPhotosCount = itemView.findViewById(R.id.tv_album_photos_count)
            ivSelected = itemView.findViewById(R.id.iv_selected)
            mRoot = itemView.findViewById(R.id.m_root_view)
        }
    }
}