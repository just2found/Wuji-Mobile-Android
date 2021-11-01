package net.linkmate.app.ui.nas.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioGroup
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.ui.nas.images.ImageViewType
import net.linkmate.app.util.Dp2PxUtils

class ImageViewerMenuPopupView(context: Context, var type: ImageViewType) : PopupWindow() {
    var baseViewHolder: BaseViewHolder
    var fileViewerTypeListener: OnCheckedChangeListener<ImageViewType>? = null
    var list: List<Int>
    var drawableSize: Int

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.sort_menu_popup_nor, null, false)
        contentView = view
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        baseViewHolder = BaseViewHolder(view)
        list = listOf<Int>(R.id.year, R.id.month, R.id.day)
        drawableSize = Dp2PxUtils.dp2px(context, 18)

        baseViewHolder.getView<RadioGroup>(R.id.group_view).apply {
            this.setOnCheckedChangeListener { group, checkedId ->
                fileViewerTypeListener?.onCheckedChangeListener(when (checkedId) {
                    R.id.display_by_day -> {
                        ImageViewType.DAY
                    }
                    R.id.display_by_month -> {
                        ImageViewType.MONTH
                    }
                    else -> {
                        ImageViewType.YEAR
                    }
                })
            }
            check(when (type) {
                ImageViewType.DAY -> {
                    R.id.display_by_day
                }
                ImageViewType.MONTH -> {
                    R.id.display_by_month
                }
                else -> {
                    R.id.display_by_year
                }
            })
        }
    }

    fun setOnViewerCheckedChangeListener(listener: OnCheckedChangeListener<ImageViewType>) {
        this.fileViewerTypeListener = listener
    }
}
