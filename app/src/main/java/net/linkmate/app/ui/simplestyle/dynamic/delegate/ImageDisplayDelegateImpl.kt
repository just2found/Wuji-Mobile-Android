package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.lxj.xpopup.interfaces.XPopupImageLoader
import java.io.File

/** 单图片显示代理
 * @author Raleigh.Luo
 * date：20/11/24 18
 * describe：
 */
internal class ImageDisplayDelegateImpl(private val ivImage: ImageView) : ImageDisplayDelegate() {
    override fun loadImage(url: Any?, placeholder: Int?, errorRes: Int) {
        //不重复加载相同url,设置Tag表示加载过
        if (ivImage.getTag() == url) return

        ivImage.setTag(url)
        ivImage.visibility = View.VISIBLE
        placeholder?.let {
            Glide.with(ivImage)
                    .asBitmap()
                    .load(url)
                    .thumbnail(0.1f)//生成缩略图
                    .centerCrop()
                    .placeholder(placeholder)//加载中显示的图片
                    .error(errorRes)
                    .into(ivImage)
        } ?: let {
            Glide.with(ivImage)
                    .asBitmap()
                    .load(url)
                    .thumbnail(0.1f)//生成缩略图
                    .centerCrop()
                    .error(errorRes)
                    .into(ivImage)
        }

    }

    override fun setDefaultListener(context: Context, originalUrl: Any?, placeholder: Int?, errorRes: Int) {
        ivImage.setOnClickListener {
            asCustomImageViewer(context,
                    ivImage,
                    originalUrl,
                    object : XPopupImageLoader {
                        override fun loadImage(position: Int, uri: Any, image: ImageView) {
                            placeholder?.let {
                                Glide.with(image)
                                        .asBitmap()
                                        .load(uri)
                                        .placeholder(placeholder)//加载中显示的图片
                                        .error(errorRes)
                                        .into(image)
                                true
                            } ?: let {
                                Glide.with(image)
                                        .asBitmap()
                                        .load(uri)
                                        .error(errorRes)
                                        .into(image)
                            }
                        }

                        override fun getImageFile(context: Context, uri: Any): File? {
                            return Glide.with(context).downloadOnly().load(uri).submit().get();
                        }

                    }
            )
        }
    }

}