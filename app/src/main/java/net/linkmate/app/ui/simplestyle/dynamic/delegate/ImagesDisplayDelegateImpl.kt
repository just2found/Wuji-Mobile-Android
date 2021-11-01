package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.XPopupImageLoader
import net.linkmate.app.R
import net.linkmate.app.data.model.dynamic.getDownloadThumbnailUrl
import net.linkmate.app.data.model.dynamic.getDownloadThumbnailUrlKey
import net.linkmate.app.data.model.dynamic.getDownloadUrl
import net.linkmate.app.data.model.dynamic.isLocalPath
import net.linkmate.app.net.RetrofitSingleton
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.video.DynamicVideoImagePopup
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.vo.DynamicMedia
import java.io.File


/** 多图显示 代理
 * @author Raleigh.Luo
 * date：20/11/24 19
 * describe：
 */
class ImagesDisplayDelegateImpl(private val ivPlays: List<ImageView>, private val imageViews: List<ImageView>) : ImagesDisplayDelegate() {

    private fun checkPosition(position: Int): Boolean {
        return position < imageViews.size
    }

    override fun clear(context: Context) {
        imageViews.forEach {
            it.setTag(null)
            it.setImageDrawable(null)
            it.setOnClickListener(null)
            Glide.with(context).clear(it);
        }
    }

    override fun setVisibility(position: Int, visibility: Int) {
        if (!checkPosition(position)) return
        imageViews.get(position).visibility = visibility
        if (visibility == View.GONE) {
            ivPlays.get(position).visibility = View.GONE
        }
    }

    override fun setOnTouchListener(l: View.OnTouchListener?) {
        ivPlays.forEach {
            it.setOnTouchListener(l)
        }
        imageViews.forEach {
            it.setOnTouchListener(l)
        }
    }


    private fun checkVideo(position: Int, imageView: ImageView, url: DynamicMedia?): Boolean {
        val ivPlay = ivPlays.get(position)
        var isVideo = false
        if (url != null && url?.type == url?.getVideoType()) {//是视频，显示播放图标
            ivPlay.visibility = View.VISIBLE
            isVideo = true
        } else {
            ivPlay.visibility = View.GONE
        }
        return isVideo
    }

    override fun loadImage(position: Int, url: DynamicMedia?, placeholder: Int?, errorRes: Int) {
        if (!checkPosition(position)) return
        val imageView = imageViews.get(position)
        imageView.visibility = View.VISIBLE
        val isVideo = checkVideo(position, imageView, url)
        //使用Tag记录已经加载过，避免重复刷新闪烁
        val tag = imageView.getTag()
        var urlTag = url?.getDownloadThumbnailUrlKey(deviceId, ip)

        var imageUrl = url?.getDownloadThumbnailUrl(deviceId, ip)

        if (tag != null && tag == urlTag) return

        if ((url?.id
                        ?: -1L) == -1L || !TextUtils.isEmpty(RetrofitSingleton.instance.getDynamicAuthorization(deviceId))) {
            //本地数据或有Authorization才加载
            imageView.setTag(urlTag)
            var requestBuidler = Glide.with(imageView)
                    .load(imageUrl)

            if (isVideo) requestBuidler = requestBuidler.frame(1000)//加载视频第一帧图片
            if (url?.isLocalPath()
                            ?: false) requestBuidler = requestBuidler.thumbnail(0.4f)//本地资源，生成缩略图
            placeholder?.let {
                requestBuidler = requestBuidler.placeholder(placeholder)
            }
            requestBuidler.centerCrop()
                    .override(imageView.width, imageView.height)
                    .error(errorRes)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            //加载失败，清空Tag
                            imageView.setTag(null)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                    })
                    .into(imageView)
        } else {
            imageView.setImageResource(defualtPlaceholderRes)
        }
    }

    //后台目前配置图片的最小边宽度256
    private val REMOTE_BITMAP_BOUND = 256
    override fun loadWrapperImage(context: Context, position: Int, url: DynamicMedia?, placeholder: Int?, errorRes: Int, fillWidth: Int?) {
        if (!checkPosition(position)) return
        val imageView = imageViews.get(position)
        imageView.visibility = View.VISIBLE
        val isVideo = checkVideo(position, imageView, url)
        var urlTag = url?.getDownloadThumbnailUrlKey(deviceId, ip)

        var imageUrl = url?.getDownloadThumbnailUrl(deviceId, ip)

        //使用Tag记录已经加载过，避免重复刷新闪烁
        val tag = imageView.getTag()
        if (tag != null && tag == urlTag) return

        val screenWidth = getScreenWidth(context)
        val maxHeight = fillWidth ?: screenWidth //最大边界，高宽为正方形
        val maxWidth = maxHeight
        val fixHeight = (maxHeight * 2 / 3f).toInt() //最小高度，2宫格

        val layoutParams = imageView.layoutParams
        val originalWidth = url?.width ?: 0
        val originalHeight = url?.height ?: 0

        if (originalWidth > 0 && originalHeight > 0) {
            initRatio(imageView, originalWidth, originalHeight, fixHeight, maxWidth)
            if ((url?.id
                            ?: -1L) == -1L || !TextUtils.isEmpty(RetrofitSingleton.instance.getDynamicAuthorization(deviceId))) {
                //本地数据或有Authorization才加载
                imageView.setTag(urlTag)
                var requestBuidler = Glide.with(imageView)
                        .load(imageUrl)

                if (isVideo) requestBuidler = requestBuidler.frame(1000)//加载视频第一帧图片
                if (url?.isLocalPath()
                                ?: false) requestBuidler = requestBuidler.thumbnail(0.4f)//本地资源，生成缩略图
                placeholder?.let {
                    requestBuidler = requestBuidler.placeholder(placeholder)
                }
                requestBuidler.centerCrop()
                        .error(errorRes)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                //加载失败，清空Tag
                                imageView.setTag(null)
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                return false
                            }

                        })
                        .into(imageView)
            } else {
                imageView.setImageResource(defualtPlaceholderRes)
            }
        } else {
            layoutParams.width = (maxHeight / 3f).toInt()
            layoutParams.height = fixHeight
            imageView.setLayoutParams(layoutParams)
            imageView.setImageResource(R.drawable.bg_image_placeholder)
            imageView.visibility = View.VISIBLE
            val customTarget = object : CustomTarget<Bitmap>() {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    //加载失败，清空Tag
                    imageView.setTag(null)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    var bitmapWidth: Int = resource.width
                    var bitmapHeight: Int = resource.height

                    /**后台目前缩略图策略，最小边固定为REMOTE_BITMAP_BOUND
                     * 1.压缩图片尺寸，不改变原长宽比例
                    2.压缩后图片长宽值较低的数值为256，较长的按比例缩小（原尺寸边长小于256的不进行缩放）
                    3.压缩后缩略图大小须小于300K
                    前端：
                    1.高度固定为宫格的2倍
                    2.宽度根据图片比例自适应，最大为宫格的3倍
                     */
                    if (bitmapWidth < bitmapHeight) {
                        bitmapWidth = Math.min(REMOTE_BITMAP_BOUND, bitmapWidth)
                        bitmapHeight = (bitmapWidth * resource.height.toFloat() / resource.width).toInt()
                    } else {
                        bitmapHeight = Math.min(REMOTE_BITMAP_BOUND, bitmapHeight)
                        bitmapWidth = (bitmapHeight * resource.width.toFloat() / resource.height).toInt()
                    }

                    val layoutParams = imageView.layoutParams
                    var afterHeight = fixHeight
                    var afterWidth = (bitmapWidth * afterHeight.toFloat() / bitmapHeight).toInt()
                    afterWidth = Math.min(afterWidth, maxWidth)

                    layoutParams.width = afterWidth
                    layoutParams.height = afterHeight
                    imageView.setLayoutParams(layoutParams)

                    var requestBuidler = Glide.with(imageView)
                            .load(imageUrl)

                    if (isVideo) requestBuidler = requestBuidler.frame(1000)//加载视频第一帧图片
                    if (url?.isLocalPath()
                                    ?: false) requestBuidler = requestBuidler.thumbnail(0.4f)//本地资源，生成缩略图
                    placeholder?.let {
                        requestBuidler = requestBuidler.placeholder(placeholder)
                    }
                    requestBuidler.centerCrop()
                            .error(errorRes)
                            .into(imageView)
                }

            }
            if ((url?.id
                            ?: -1L) == -1L || !TextUtils.isEmpty(RetrofitSingleton.instance.getDynamicAuthorization(deviceId))) {
                //本地数据或有Authorization才加载
                imageView.setTag(urlTag)
                var requestBuidler = Glide.with(imageView)
                        .asBitmap()
                        .load(imageUrl)

                if (isVideo) requestBuidler = requestBuidler.frame(1000)//加载视频第一帧图片
                if (url?.isLocalPath()
                                ?: false) requestBuidler = requestBuidler.thumbnail(0.4f)//本地资源，生成缩略图
                placeholder?.let {
                    requestBuidler = requestBuidler.placeholder(placeholder)
                }
                requestBuidler.centerCrop()
                        .error(errorRes)
                        .into(customTarget)
            } else {
                imageView.setImageResource(defualtPlaceholderRes)
            }

        }
    }

    /**
     * 初始化分辨率
     */
    private fun initRatio(imageView: ImageView, originalWidth: Int, originalHeight: Int, fixHeight: Int, maxWidth: Int) {
        var bitmapWidth: Int = originalWidth
        var bitmapHeight: Int = originalHeight

        /**后台目前缩略图策略，最小边固定为REMOTE_BITMAP_BOUND
         * 1.压缩图片尺寸，不改变原长宽比例
        2.压缩后图片长宽值较低的数值为256，较长的按比例缩小（原尺寸边长小于256的不进行缩放）
        3.压缩后缩略图大小须小于300K
        前端：
        1.高度固定为宫格的2倍
        2.宽度根据图片比例自适应，最大为宫格的3倍
         */
        if (bitmapWidth < bitmapHeight) {
            bitmapWidth = Math.min(REMOTE_BITMAP_BOUND, bitmapWidth)
            bitmapHeight = (bitmapWidth * originalHeight.toFloat() / originalWidth).toInt()
        } else {
            bitmapHeight = Math.min(REMOTE_BITMAP_BOUND, bitmapHeight)
            bitmapWidth = (bitmapHeight * originalWidth.toFloat() / originalHeight).toInt()
        }

        val layoutParams = imageView.layoutParams
        var afterHeight = fixHeight
        var afterWidth = (bitmapWidth * afterHeight.toFloat() / bitmapHeight).toInt()
        afterWidth = Math.min(afterWidth, maxWidth)

        layoutParams.width = afterWidth
        layoutParams.height = afterHeight
        imageView.setLayoutParams(layoutParams)
    }

    /**
     * 获取屏幕信息
     */
    private fun getScreenWidth(context: Context): Int {
        // 获取屏幕信息
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }


    /**
     * @param isDisplayOneLargerImage 一张图时，是否显示了大图
     */
    override fun setDefaultListener(context: Context, imageUrls: List<DynamicMedia>?, placeholder: Int?, errorRes: Int, isDisplayOneLargerImage: Boolean) {
        //只能显示9个
        val urls = if ((imageUrls?.size ?: 0) > 9) imageUrls?.subList(0, 9) else imageUrls
        val displayLargerImage = { imageView: ImageView, position: Int ->
            val customImagePopup = DynamicVideoImagePopup(context)
            XPopup.Builder(context).asCustom(customImagePopup
                    .setSrcView(imageView, position)
                    .setImageUrls(urls)
                    .isInfinite(false)
                    .isShowPlaceholder(false)
                    .setPlaceholderColor(-1)
                    .setPlaceholderStrokeColor(-1)
                    .setPlaceholderRadius(-1)
                    .isShowSaveButton(true)
                    .setSrcViewUpdateListener({ popupView, position ->
                        if (checkPosition(position)) {
                            if (isDisplayOneLargerImage && imageUrls?.size == 1) {//只有一张大图
                                popupView.updateSrcView(imageViews.get(position))
                            } else {
                                popupView.updateSrcView(imageViews.get(position + 1))
                            }
                        }
                    })
                    .setXPopupImageLoader(object : XPopupImageLoader {
                        override fun loadImage(position: Int, uri: Any, image: ImageView) {
                            customImagePopup.mLoadingListener.loading()
                            var requestBuidler = Glide.with(image)
                                    .load((uri as DynamicMedia).getDownloadUrl(deviceId, ip))
                            placeholder?.let {
                                requestBuidler = requestBuidler.placeholder(placeholder)
                            }
                            requestBuidler
                                    .error(errorRes)
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            customImagePopup.mLoadingListener.error()
                                            return false
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            customImagePopup.mLoadingListener.success()
                                            return false
                                        }

                                    })
                                    .into(image)
                        }

                        override fun getImageFile(context: Context, uri: Any): File? {
                            try {
                                //保存
                                val media = uri as DynamicMedia
                                if (media.type == media.getVideoType()) {
                                    val mainHandler = Handler(Looper.getMainLooper())
                                    if (TextUtils.isEmpty(media.url)) {
                                        mainHandler.post {
                                            ToastUtils.showToast(R.string.file_not_found)
                                        }
                                    } else {
                                        mainHandler.post {
                                            ToastUtils.showToast(R.string.start_download_file)
                                        }
                                        DynamicQueue.downloadFile(deviceId, ip, media.url
                                                ?: "", defualtSuffix = "mp4")
                                    }
                                    return null
                                } else {
                                    return Glide.with(context).downloadOnly().load(media.getDownloadUrl(deviceId, ip)).submit().get();
                                }
                            } catch (e: Exception) {
                                return null
                            }
                        }

                    })
            ).show()
//            XPopup.Builder(context).asImageViewer(
//                    imageView,
//                    position,
//                    imageUrls,
//                    { popupView, position -> if (checkPosition(position)) popupView.updateSrcView(imageViews.get(position)) },
//                    object : XPopupImageLoader {
//                        override fun loadImage(position: Int, uri: Any, image: ImageView) {
//                            placeholder?.let {
//                                Glide.with(image)
//                                        .asBitmap()
//                                        .load((uri as DynamicMedia).getDownloadUrl(deviceId, ip))
//                                        .placeholder(placeholder)
//                                        .error(errorRes)
//                                        .into(image)
//                            } ?: let {
//                                Glide.with(image)
//                                        .asBitmap()
//                                        .load((uri as DynamicMedia).getDownloadUrl(deviceId, ip))
//                                        .error(errorRes)
//                                        .into(image)
//                            }
//
//                        }
//
//                        override fun getImageFile(context: Context, uri: Any): File? {
//                            return Glide.with(context).downloadOnly().load((uri as DynamicMedia).getDownloadUrl(deviceId, ip)).submit().get();
//                        }
//
//                    }
//            ).show()
            true
        }

        //点击事件
        for (position in 0 until imageViews.size) {
            val imageView = imageViews.get(position)
            val imageUrlsSize = imageUrls?.size ?: 0
            //点击的是图片展示位置
            var isImageViewInUrls = false
            when {
                imageUrlsSize == 0 -> {
                    isImageViewInUrls = false
                }
                imageUrlsSize == 1 -> {//只有一张大图
                    isImageViewInUrls = position == 0
                }
                else -> {//多图
                    isImageViewInUrls = position != 0 && position <= imageUrlsSize
                }
            }
            if (isImageViewInUrls) {
                imageView.setOnClickListener {
                    displayLargerImage(imageView, if (imageUrlsSize == 1) position else (position - 1))
                }
            } else {
                imageView.setOnClickListener(null)
            }
        }
    }


}