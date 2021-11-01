package net.linkmate.app.ui.nas.helper

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.util.Dp2PxUtils
import net.sdvn.nascommon.model.FileTypeItem
import org.view.libwidget.OnItemClickListener
import timber.log.Timber
import kotlin.math.ceil

class ToolsPagerAdapter : PagerAdapter() {
    private var listener: OnItemClickListener<FileTypeItem>? = null
    private var data = mutableListOf<FileTypeItem>()
    fun setData(list: List<FileTypeItem>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun getCount(): Int {
        return ceil((data.size * 1f / getPageCount()).toDouble()).toInt()
    }

    private fun getPageCount(): Int {
        return 4
    }

    override fun getItemPosition(any: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val context = container.context
        val layout = ConstraintLayout(context)
        layout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(layout)
        var i = 0
        val index: Int = position * getPageCount()
        Timber.d("instantiateItem-position : $position -index : $index")
        val count: Int = data.count()
        var lastItemId: Int? = null
        val c = ConstraintSet()
        while (i < 4) {
            val i1 = index + i
            Timber.d("instantiateItem: $i1")

            if (i1 < count) {
                val itemView = LayoutInflater.from(context).inflate(R.layout.item_gridview_card_tool_lenovo, null)
                data.getOrNull(i1)?.let { data ->
                    bindData(itemView, data, position)
                }
                itemView.id = View.generateViewId()
                layout.addView(itemView)
//                app:layout_constraintBottom_toBottomOf="parent"
//                app:layout_constraintStart_toStartOf="parent"
//                app:layout_constraintTop_toTopOf="parent"
//                app:layout_constraintWidth_percent="0.25"
                c.connect(itemView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                c.connect(itemView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                c.connect(itemView.id, ConstraintSet.START, lastItemId
                        ?: ConstraintSet.PARENT_ID, if (lastItemId != null) {
                    ConstraintSet.END
                } else {
                    ConstraintSet.START
                })
                c.constrainPercentWidth(itemView.id, 0.25f)
                lastItemId = itemView.id
                Timber.d("instantiateItem-lastItemId: $lastItemId")
                i += 1
            } else {
                break
            }
        }
        c.applyTo(layout)
        layout.postInvalidate()
        return layout
    }

    private fun bindData(itemView: View, data: FileTypeItem, position: Int) {
        val keyItem = 77581234
        val holder = BaseViewHolder(itemView)
        val tag = holder.itemView.getTag(keyItem)
        val margins = Dp2PxUtils.dp2px(holder.itemView.context, 10)
        if (tag != data.flag) {
            holder.itemView.setTag(keyItem, data.flag)
            holder.setText(R.id.txt_type, data.title)
            holder.setTextColor(R.id.txt_type, holder.itemView.context.resources.getColor(R.color.text_black))
            holder.setImageResource(R.id.iv_icon, data.normalIcon)
        }
        val value = if (data.ext2 is Int) {
            if (data.ext2 as Int <= 0) {
                null
            } else {
                if (data.ext2 as Int <= 99) {
                    data.ext2.toString()
                } else {
                    "99+"
                }
            }
        } else {
            null
        }
        holder.setText(R.id.tv_tips, value)
        itemView.setOnClickListener {
            listener?.OnItemClick(data, position, itemView)
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<FileTypeItem>) {
        this.listener = onItemClickListener
    }

}

/**
 * 标示点适配器
 */
class IndicatorHelper(@ColorInt val mSelectedColor: Int, @ColorInt val mUnselectedColor: Int,
                      var count: Int, private val viewPager: ViewPager) {
    var mUnselectedDrawable: Drawable
    var mSelectedDrawable: Drawable
    val context: Context = viewPager.getContext()
    private val indicatorAdapter = IndicatorAdapter()
    protected val indicatorContainer = RecyclerView(context)

    init {
        //指示器部分
        val indicatorLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        indicatorContainer.setLayoutManager(indicatorLayoutManager)
        indicatorContainer.setAdapter(indicatorAdapter)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.bottomMargin = dp2px(6)
        val parent = viewPager.parent
        if (parent is ViewGroup) {
            parent.addView(indicatorContainer, params)
        }
        //绘制默认选中状态图形
        val selectedGradientDrawable = GradientDrawable()
        selectedGradientDrawable.shape = GradientDrawable.RECTANGLE
        selectedGradientDrawable.setColor(mSelectedColor)
        selectedGradientDrawable.setSize(dp2px(16), dp2px(2))
        mSelectedDrawable = LayerDrawable(arrayOf<Drawable>(selectedGradientDrawable))
        //绘制默认未选中状态图形
        val unSelectedGradientDrawable = GradientDrawable()
        unSelectedGradientDrawable.shape = GradientDrawable.RECTANGLE
        unSelectedGradientDrawable.setColor(mUnselectedColor)
        unSelectedGradientDrawable.setSize(dp2px(16), dp2px(2))
        mUnselectedDrawable = LayerDrawable(arrayOf<Drawable>(unSelectedGradientDrawable))

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                setPosition(position)
            }
        })
        indicatorContainer.isVisible = count > 1
        indicatorAdapter.notifyDataSetChanged()
    }

    fun updateCount(count: Int) {
        if (this.count != count) {
            this.count = count
            setPosition(0)
            indicatorContainer.isVisible = count > 1
            indicatorAdapter.notifyDataSetChanged()
        }
    }

    var currentPosition = 0
    fun setPosition(currentPosition: Int) {
        this.currentPosition = currentPosition
        indicatorAdapter.notifyDataSetChanged()
    }

    inner class IndicatorAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val context = parent.context
            val bannerPoint = ImageView(context)
            val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            val indicatorMargin: Int = 0
            lp.setMargins(indicatorMargin, indicatorMargin, indicatorMargin, indicatorMargin)
            bannerPoint.layoutParams = lp
            return object : RecyclerView.ViewHolder(bannerPoint) {}
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bannerPoint = holder.itemView as ImageView
            bannerPoint.setImageDrawable(if (currentPosition == position) mSelectedDrawable else mUnselectedDrawable)
        }

        override fun getItemCount(): Int {
            return count
        }
    }

    private fun dp2px(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density + 0.5).toInt()
    }

}