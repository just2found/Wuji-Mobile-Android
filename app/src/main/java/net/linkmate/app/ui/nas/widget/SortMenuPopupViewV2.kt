package net.linkmate.app.ui.nas.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.util.Dp2PxUtils
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.FileViewerType
import timber.log.Timber

class SortMenuPopupViewV2(context: Context, var currentOrder: FileOrderTypeV2,
                          var currentDisplay: FileViewerType) : PopupWindow() {

    private var fileViewerTypeListener: OnCheckedChangeListener<FileViewerType>? = null
    private var fileOrderTypeListener: OnCheckedChangeListener<FileOrderTypeV2>? = null
    var baseViewHolder: BaseViewHolder
    var list: List<Int>
    var drawableSize: Int

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.sort_menu_popup_v2, null, false)
        contentView = view
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        baseViewHolder = BaseViewHolder(view)
        baseViewHolder.addOnClickListener(R.id.sort_by_time)
        baseViewHolder.addOnClickListener(R.id.sort_by_name)
        baseViewHolder.addOnClickListener(R.id.sort_by_size)
        baseViewHolder.addOnClickListener(R.id.display_by_grid)
        baseViewHolder.addOnClickListener(R.id.display_by_list)
        list = listOf<Int>(R.id.sort_by_time, R.id.sort_by_name, R.id.sort_by_size)
        drawableSize = Dp2PxUtils.dp2px(context, 18)
        Timber.d("FileOrderTypeV2: $currentOrder ,FileViewerType: $currentDisplay")
        baseViewHolder.getView<RadioGroup>(R.id.group_sort).apply {
            check(when (currentOrder) {
                FileOrderTypeV2.time_desc, FileOrderTypeV2.time_asc -> {
                    R.id.sort_by_time
                }
                FileOrderTypeV2.size_desc, FileOrderTypeV2.size_asc -> {
                    R.id.sort_by_size
                }
                FileOrderTypeV2.name_desc, FileOrderTypeV2.name_asc -> {
                    R.id.sort_by_name
                }
                else -> {
                    View.NO_ID
                }
            }.also {
                updateViewCheck(context, it, isAsc())
            })
            setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.sort_by_time -> {
                        currentOrder = FileOrderTypeV2.time_desc
                    }
                    R.id.sort_by_name -> {
                        currentOrder = FileOrderTypeV2.name_desc
                    }
                    R.id.sort_by_size -> {
                        currentOrder = FileOrderTypeV2.size_desc
                    }
                }
                updateViewCheck(context, checkedId, isAsc())
                fileOrderTypeListener?.onCheckedChangeListener(currentOrder)
            }

        }
        baseViewHolder.getView<RadioGroup>(R.id.group_view).apply {
            check(when (currentDisplay) {
                FileViewerType.LIST -> {
                    R.id.display_by_list
                }
                FileViewerType.GRID -> {
                    R.id.display_by_grid
                }
                else -> {
                    View.NO_ID
                }
            })
            switchViewer(context, currentDisplay == FileViewerType.LIST)
            setOnCheckedChangeListener { group, checkedId ->
                currentDisplay = if (checkedId == R.id.display_by_list) {
                    FileViewerType.LIST
                } else {
                    FileViewerType.GRID
                }
                switchViewer(context, currentDisplay == FileViewerType.LIST)
                fileViewerTypeListener?.onCheckedChangeListener(currentDisplay)
            }
        }
        baseViewHolder.setOtherClickListener {
            var isChanged = false
            when (it.id) {
                R.id.sort_by_time -> {
                    if (currentOrder == FileOrderTypeV2.time_asc) {
                        currentOrder = FileOrderTypeV2.time_desc
                        isChanged = true
                    } else if (currentOrder == FileOrderTypeV2.time_desc) {
                        currentOrder = FileOrderTypeV2.time_asc
                        isChanged = true
                    }
                }
                R.id.sort_by_name -> {
                    if (currentOrder == FileOrderTypeV2.name_asc) {
                        currentOrder = FileOrderTypeV2.name_desc
                        isChanged = true
                    } else if (currentOrder == FileOrderTypeV2.name_desc) {
                        currentOrder = FileOrderTypeV2.name_asc
                        isChanged = true
                    }
                }
                R.id.sort_by_size -> {
                    if (currentOrder == FileOrderTypeV2.size_asc) {
                        currentOrder = FileOrderTypeV2.size_desc
                        isChanged = true
                    } else if (currentOrder == FileOrderTypeV2.size_desc) {
                        currentOrder = FileOrderTypeV2.size_asc
                        isChanged = true
                    }
                }
            }
            if (isChanged) {
                updateViewCheck(context, it.id, isAsc())
                fileOrderTypeListener?.onCheckedChangeListener(currentOrder)
            }
        }
    }

    private fun isAsc() = currentOrder.name.endsWith("_asc", true)

    private fun switchViewer(context: Context, isList: Boolean) {
        updateView(context, R.id.display_by_list, isList)
        updateView(context, R.id.display_by_grid, !isList)
    }

    private fun updateViewCheck(context: Context, @IdRes viewId: Int, isAsc: Boolean?) {
        list.forEach {
            updateView(context, it, it == viewId, isAsc)
        }
    }

    private fun updateView(context: Context, @IdRes viewId: Int, isChecked: Boolean = true, isAsc: Boolean? = null) {
//        val drawableStart = if (isChecked) {
//            ContextCompat.getDrawable(context, R.drawable.icon_checked)?.apply {
//                setBounds(0, 0, drawableSize, drawableSize)
//            }
//        } else {
//            ColorDrawable(ContextCompat.getColor(context, R.color.transparent)).apply {
//                setBounds(0, 0, drawableSize, drawableSize)
//            }
//        }
        val drawableRight: Drawable? = isAsc?.let {
            if (isChecked) {
                ContextCompat.getDrawable(context,
                        if (isAsc) {
                            R.drawable.icon_order
                        } else {
                            R.drawable.icon_reverse_order
                        }
                )?.apply {
                    setBounds(0, 0, drawableSize, drawableSize)
                }
            } else {
                null
            }
        } ?: kotlin.run { null }
        baseViewHolder.getView<RadioButton>(viewId)
                .setCompoundDrawables(null, null, drawableRight, null)
    }

    fun setOnOrderCheckedChangeListener(listener: OnCheckedChangeListener<FileOrderTypeV2>) {
        this.fileOrderTypeListener = listener
    }

    fun setOnViewerCheckedChangeListener(listener: OnCheckedChangeListener<FileViewerType>) {
        this.fileViewerTypeListener = listener
    }
}

interface OnCheckedChangeListener<T> {
    fun onCheckedChangeListener(type: T)
}
