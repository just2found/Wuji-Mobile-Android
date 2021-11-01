package net.linkmate.app.view

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.layout_dynamic_image.view.*
import net.linkmate.app.R
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.delegate.ImagesDisplayDelegate
import net.sdvn.common.vo.DynamicMedia


/**
 * @author Raleigh.Luo
 * date：20/11/21 13
 * describe：
 */
class DynamicImageLayout : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val root: View

    private var ip: String = ""
    private var deviceId: String = ""

    var imageUrls: List<DynamicMedia>? = arrayListOf()
        set(value) {
            this.deviceId = DynamicQueue.deviceId
            this.ip = DynamicQueue.deviceIP
            field = value
            imagesDisplayDelegate.init(deviceId, ip)
            refresh()
        }

    //是否设置点击事件
    var isConfigListener = true

    //一张图片时，是否显示一张大图
    var isDisplayOneLargerImage = true

    var imagesDisplayDelegate: ImagesDisplayDelegate

    init {
        root = View.inflate(context, R.layout.layout_dynamic_image, this)
        with(root) {
            imagesDisplayDelegate = ImagesDisplayDelegate.create(arrayListOf(ivPlay0, ivPlay1, ivPlay2, ivPlay3, ivPlay4
                    , ivPlay5, ivPlay6, ivPlay7, ivPlay8, ivPlay9), arrayListOf(ivImage0, ivImage1, ivImage2, ivImage3, ivImage4
                    , ivImage5, ivImage6, ivImage7, ivImage8, ivImage9))
        }
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener(l)
        imagesDisplayDelegate.setOnTouchListener(l)
    }

    /**
     * 显示图片大小，不能超过9个
     */
    private fun getImageSize(): Int {
        return Math.min(9, imageUrls?.size ?: 0)
    }

    private fun refresh() {
        val size = imageUrls?.size
        when (size) {
            0 -> {
                visibility = View.GONE
            }
            1 -> {
                if (isDisplayOneLargerImage) loadOne() else loadMore()
            }
            else -> {
                loadMore()
            }

        }
    }

    /**
     * 获取屏幕信息
     */
    private fun getScreenWidth(): Int {
        // 获取屏幕信息
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    //最大宽度，若布局有更改，需更改算法
    var fillWidth = 0
        get() {
            if (field == 0) {
                //item_friend_circle ivPortrait 的宽度
                val imageWidth = context.resources.getDimensionPixelSize(R.dimen.common_icon42)
                val imageMarginStart = context.resources.getDimensionPixelSize(R.dimen.common_16)
                val marginStart = context.resources.getDimensionPixelSize(R.dimen.common_10)
                field = getScreenWidth() - imageWidth - imageMarginStart - marginStart
            }
            return field
        }

    /**
     *  加载一张图片
     */
    private fun loadOne() {
        visibility = View.VISIBLE
        for (i in 0 until 9) {
            val imageViewIndex = i + 1
            imagesDisplayDelegate.setVisibility(imageViewIndex, View.GONE)
        }

        imagesDisplayDelegate.loadWrapperImage(context, 0, imageUrls?.get(0), fillWidth)
        //查看大图
        if (isConfigListener) imagesDisplayDelegate.setDefaultListener(context, imageUrls)
    }

    /**
     * 是否是同一行
     */
    private fun isSameRow(index: Int): Boolean {
        //current 1-9
        val getRow = { current: Int ->
            if (current % 3 == 0) current / 3 else ((current / 3) + 1)
        }
        return getRow(index + 1) == getRow(getImageSize())
    }

    /**
     *  加载一张以上的图片
     */
    private fun loadMore() {
        visibility = View.VISIBLE
        imagesDisplayDelegate.setVisibility(0, View.GONE)
        for (i in 0 until 9) {
            val imageViewIndex = i + 1
            if (i < getImageSize()) {
                imagesDisplayDelegate.loadImage(imageViewIndex, imageUrls?.get(i))
            } else {
                //同行需占位，不同行隐藏
                imagesDisplayDelegate.setVisibility(imageViewIndex, if (isSameRow(i)) View.INVISIBLE else View.GONE)
            }
        }
        //查看大图
        if (isConfigListener) imagesDisplayDelegate.setDefaultListener(context, imageUrls,isDisplayOneLargerImage)
    }

    fun clear() {
        imagesDisplayDelegate.clear(context)
    }
}