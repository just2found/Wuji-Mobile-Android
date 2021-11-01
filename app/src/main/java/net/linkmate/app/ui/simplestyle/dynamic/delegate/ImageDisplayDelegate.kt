package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.widget.ImageView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.XPopupImageLoader
import net.linkmate.app.R

/** 单图片显示代理
 * @author Raleigh.Luo
 * date：20/11/24 18
 * describe：
 */
abstract class ImageDisplayDelegate {
    companion object {
        @JvmStatic
        fun create(ivImage: ImageView): ImageDisplayDelegate {
            return ImageDisplayDelegateImpl(ivImage)
        }
    }

    open protected var defualtPlaceholderRes = R.drawable.bg_image_placeholder

    /**
     * 加载图片
     */
    fun loadImage(url: Any?) {
        loadImage(url, defualtPlaceholderRes)
    }

    fun loadImage(url: Any?, errorRes: Int) {
        loadImage(url, defualtPlaceholderRes, errorRes)
    }

    abstract fun loadImage(url: Any?, placeholder: Int?, errorRes: Int)

    /**
     * 显示大图监听
     * @param originalUrl 原图地址
     */
    fun setDefaultListener(context: Context, originalUrl: Any?) {
        setDefaultListener(context, originalUrl, R.color.darker)
    }

    fun setDefaultListener(context: Context, originalUrl: Any?, errorRes: Int) {
        setDefaultListener(context, originalUrl, null, R.color.darker)
    }

    abstract fun setDefaultListener(context: Context, originalUrl: Any?, placeholder: Int?, errorRes: Int)

    fun asCustomImageViewer(context: Context, srcView: ImageView, url: Any?, imageLoader: XPopupImageLoader) {
        XPopup.Builder(context).asCustom(net.linkmate.app.ui.simplestyle.dynamic.video.ImageViewerPopupView(context)
                .setSingleSrcView(srcView, url)
                .setXPopupImageLoader(imageLoader)).show()
    }

}

