package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.view.View
import android.widget.ImageView
import net.linkmate.app.R
import net.sdvn.common.vo.DynamicMedia

/** 多图显示 代理
 *
 * @author Raleigh.Luo
 * date：20/11/24 19
 * describe：
 */
abstract class ImagesDisplayDelegate {
    companion object {
        @JvmStatic
        fun create(ivPlays: List<ImageView>, imageViews: List<ImageView>): ImagesDisplayDelegate {
            return ImagesDisplayDelegateImpl(ivPlays, imageViews)
        }
    }

    open protected var defualtPlaceholderRes = R.drawable.bg_image_placeholder

    protected var ip: String = ""
    protected var deviceId: String = ""
    fun init(deviceId: String, ip: String) {
        this.deviceId = deviceId
        this.ip = ip
    }

    abstract fun clear(context: Context)

    abstract fun setVisibility(position: Int, visibility: Int)

    abstract fun setOnTouchListener(l: View.OnTouchListener?)

    /**
     * 加载图片
     */
    fun loadImage(position: Int, url: DynamicMedia?) {
        loadImage(position, url, defualtPlaceholderRes)
    }

    fun loadImage(position: Int, url: DynamicMedia?, placeholder: Int?) {
        loadImage(position, url, placeholder, defualtPlaceholderRes)
    }

    abstract fun loadImage(position: Int, url: DynamicMedia?, placeholder: Int?, errorRes: Int)

    /**
     * 按原图比例显示图片
     */
    fun loadWrapperImage(context: Context, position: Int, url: DynamicMedia?, fillWidth: Int? = null) {
        loadWrapperImage(context, position, url, defualtPlaceholderRes, fillWidth)
    }

    fun loadWrapperImage(context: Context, position: Int, url: DynamicMedia?, placeholder: Int?, fillWidth: Int? = null) {
        loadWrapperImage(context, position, url, placeholder, defualtPlaceholderRes, fillWidth)

    }

    abstract fun loadWrapperImage(context: Context, position: Int, url: DynamicMedia?, placeholder: Int?, errorRes: Int, fillWidth: Int? = null)

    /**
     * 显示大图监听
     */
    fun setDefaultListener(context: Context, imageUrls: List<DynamicMedia>?, isDisplayOneLargerImage: Boolean = true) {
        setDefaultListener(context, imageUrls, R.color.darker,isDisplayOneLargerImage)
    }

    fun setDefaultListener(context: Context, imageUrls: List<DynamicMedia>?, errorRes: Int, isDisplayOneLargerImage: Boolean = true) {
        setDefaultListener(context, imageUrls, null, errorRes,isDisplayOneLargerImage)
    }

    abstract fun setDefaultListener(context: Context, imageUrls: List<DynamicMedia>?, placeholder: Int?, errorRes: Int, isDisplayOneLargerImage: Boolean = true)

}