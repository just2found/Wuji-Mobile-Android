package net.linkmate.app.ui.nas.images

import android.os.Build
import android.util.SparseIntArray
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.SectionEntity
import net.linkmate.app.R
import net.linkmate.app.ui.nas.cloud.FileLoadMoreView
import kotlin.math.roundToInt

/**
 *
 * @Description: 相册带头部Adapter
 * @Author: todo2088
 * @CreateDate: 2021/1/29 17:33
 */

class ImagesSectionAdapter(private val photosViewModel: PhotosViewModel)
    : BaseSectionQuickAdapter<SectionEntity<OneFileModel>, BaseViewHolder>(
        R.layout.item_photo_grid, R.layout.item_photo_header, null), ISelection {
    private val density: Float

    init {
        setSpanSizeLookup { gridLayoutManager, i ->
            val itemViewType = getItemViewType(i)
            if (itemViewType == SECTION_HEADER_VIEW) {
                return@setSpanSizeLookup gridLayoutManager.spanCount
            } else {
                return@setSpanSizeLookup 1
            }
        }
        setLoadMoreView(FileLoadMoreView())
        density = photosViewModel.app.resources.displayMetrics.density
    }


    override fun convert(holder: BaseViewHolder, p1: SectionEntity<OneFileModel>?) {
        p1?.t?.let { item ->
            val iconView = holder.getView<ImageView>(R.id.icon)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iconView.transitionName = photosViewModel.getItemShareTransitionName(item)
            }
            val contains = selection.contains(holder.adapterPosition)
//            holder.getView<View>(R.id.selected_icon).apply {
//                val padding = getPaddingByWidth(width)
//                setPadding(padding, padding, padding, padding)
//            }
            holder.setChecked(R.id.selected_icon, contains)
            holder.setChecked(R.id.foreground_mask, contains)
            photosViewModel.loadImage(iconView, item)
            holder.setGone(R.id.iv_favorite, item.isFavorite())
        }
        holder.itemView.findViewById<View>(R.id.group_check)?.let {
            it.isVisible = isSetMultiModel

        }
    }

    private val sparseArray = SparseIntArray()

    private fun getPaddingByWidth(width: Int): Int {
        if (width >= 12 * density) {
            val i = sparseArray.get(width)
            if (i > 0) {
                return i
            }
            return (((3 * width - 16) * 1f / 20) * density).roundToInt().also {
                sparseArray.put(width, it)
            }
        } else {
            return (4 * density).roundToInt()
        }
    }

    override fun convertHead(holder: BaseViewHolder, p1: SectionEntity<OneFileModel>?) {
        holder.setText(R.id.item_photo_header_tv_content, p1?.header ?: "")
    }

    fun updateData(data: List<SectionEntity<OneFileModel>>?) {
        val pagesModel: OneFilePagesModel<SectionEntity<OneFileModel>> = photosViewModel.getPagesModel()
        val isRefreshing = pagesModel.page == 0
        val hasMorePage = pagesModel.hasMorePage()
        if (isRefreshing) {
            setNewData(data)
        } else {
            addData(data ?: listOf())
        }
        if (hasMorePage) {
            loadMoreComplete()
        } else {
            loadMoreEnd()
        }
        setEnableLoadMore(hasMorePage)
    }

    // ----------------------
    // Selection
    // ----------------------

    var isSetMultiModel: Boolean = false
        set(value) {
            field = value
            if (value) {
                deselectAll()
            }
            notifyDataSetChanged()
        }
    override val selection: HashSet<Int> = hashSetOf()

    fun toggleSelection(pos: Int) {
        if (selection.contains(pos)) selection.remove(pos) else selection.add(pos)
        notifyItemChanged(pos)
    }

    fun select(pos: Int, selected: Boolean) {
        if (selected) selection.add(pos) else selection.remove(pos)
        notifyItemChanged(pos)
    }

    override fun selectRange(start: Int, end: Int, selected: Boolean) {
        for (i in start..end) {
            if (getItem(i)?.isHeader == true) {
                continue
            }
            if (selected) selection.add(i) else selection.remove(i)
        }
        notifyItemRangeChanged(start, end - start + 1)
    }

    fun deselectAll() {
        // this is not beautiful...
        selection.clear()
        notifyDataSetChanged()
    }

    fun selectAll() {
        mData?.forEachIndexed { index, sectionEntity ->
            if (!sectionEntity.isHeader) {
                selection.add(index)
            }
        }
        notifyDataSetChanged()
    }

    fun toggleSelectionHeader(position: Int) {

    }

    fun findFilterHeaderPosition(position: Int): Int {
        var index = 0
        var countHeader = 0
        for (entity in data) {
            if (entity.isHeader) {
                countHeader++
            }
            index++
            if (index == position) {
                break
            }
        }
        return position - countHeader
    }

    val countSelected: Int
        get() = selection.size

}

