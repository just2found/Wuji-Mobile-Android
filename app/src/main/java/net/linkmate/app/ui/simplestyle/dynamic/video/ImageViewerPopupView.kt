package net.linkmate.app.ui.simplestyle.dynamic.video

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.*
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.huantansheng.easyphotos.constant.Type
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.ImageType
import com.lxj.xpopup.enums.PopupStatus
import com.lxj.xpopup.interfaces.XPopupImageLoader
import com.lxj.xpopup.photoview.PhotoView
import com.lxj.xpopup.util.PermissionConstants
import com.lxj.xpopup.util.XPermission
import com.lxj.xpopup.util.XPopupUtils
import kotlinx.android.synthetic.main.layout_xpopup_loading.view.*
import net.linkmate.app.R
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.vo.DynamicMedia
import java.io.*
import java.util.concurrent.Executors

/**
 * @author Raleigh.Luo
 * date：21/1/27 15
 * describe：
 */
open class ImageViewerPopupView(context: Context) : com.lxj.xpopup.core.ImageViewerPopupView(context) {
    override fun initPopupContent() {
        tv_pager_indicator = findViewById(R.id.tv_pager_indicator)
        tv_save = findViewById(R.id.tv_save)
        placeholderView = findViewById(R.id.placeholderView)
        photoViewContainer = findViewById(R.id.photoViewContainer)
        photoViewContainer.setOnDragChangeListener(this)
        pager = findViewById(R.id.pager)
        pager.setAdapter(PhotoViewAdapter())
        pager.setOffscreenPageLimit(urls!!.size)
        pager.setCurrentItem(position)
        pager.setVisibility(View.INVISIBLE)
        addOrUpdateSnapshot()
        if (isInfinite) pager.setOffscreenPageLimit(urls!!.size / 2)
        pager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(i: Int) {
                position = i
                showPagerIndicator()
                //更新srcView
                if (srcViewUpdateListener != null) {
                    srcViewUpdateListener!!.onSrcViewUpdate(this@ImageViewerPopupView, i)
                }
            }
        })
        if (!isShowIndicator) tv_pager_indicator.setVisibility(View.GONE)
        if (!isShowSaveBtn) {
            tv_save.setVisibility(View.GONE)
        } else {
            tv_save.setOnClickListener(this)
        }
    }

    private fun setupPlaceholder() {
        placeholderView.visibility = if (isShowPlaceholder) View.VISIBLE else View.INVISIBLE
        if (isShowPlaceholder) {
            if (placeholderColor != -1) {
                placeholderView.color = placeholderColor
            }
            if (placeholderRadius != -1) {
                placeholderView.radius = placeholderRadius
            }
            if (placeholderStrokeColor != -1) {
                placeholderView.strokeColor = placeholderStrokeColor
            }
            XPopupUtils.setWidthHeight(placeholderView, rect!!.width(), rect!!.height())
            placeholderView.translationX = rect!!.left.toFloat()
            placeholderView.translationY = rect!!.top.toFloat()
            placeholderView.invalidate()
        }
    }

    private fun showPagerIndicator() {
        if (urls!!.size > 1) {
            val posi = if (isInfinite) position % urls!!.size else position
            tv_pager_indicator!!.text = (posi + 1).toString() + "/" + urls!!.size
        }
        if (isShowSaveBtn) tv_save!!.visibility = View.VISIBLE
    }

    private fun addOrUpdateSnapshot() {
        if (srcView == null) return
        if (snapshotView == null) {
            snapshotView = PhotoView(context)
            photoViewContainer!!.addView(snapshotView)
            snapshotView!!.scaleType = srcView!!.scaleType
            snapshotView!!.translationX = rect!!.left.toFloat()
            snapshotView!!.translationY = rect!!.top.toFloat()
            XPopupUtils.setWidthHeight(snapshotView, rect!!.width(), rect!!.height())
        }
        setupPlaceholder()
//        修改原始方法，解决动图 返回出现动图加载花屏问题
        if (imageLoader != null) imageLoader.loadImage(position, urls.get(position), snapshotView);
    }

    override fun updateSrcView(srcView: ImageView?) {
        setSrcView(srcView, position)
        addOrUpdateSnapshot()
    }

    interface LoadingListener {
        fun success()
        fun error()
        fun loading()
    }

    val mLoadingListener: LoadingListener = object : LoadingListener {
        override fun success() {
            mProgressBar.visibility = View.GONE
        }

        override fun error() {
            mProgressBar.visibility = View.GONE
            ToastUtils.showToast(R.string.load_failed)
        }

        override fun loading() {
            mProgressBar.visibility = View.VISIBLE
        }

    }

    override fun save() {
        //check permission
        XPermission.create(context, PermissionConstants.STORAGE)
                .callback(object : XPermission.SimpleCallback {
                    override fun onGranted() {
                        //save bitmap to album.
                        saveBmpToAlbum(context, imageLoader, urls[if (isInfinite) position % urls.size else position])
                    }

                    override fun onDenied() {
                        Toast.makeText(context, R.string.error_manage_perm_deny, Toast.LENGTH_SHORT).show()
                    }
                }).request()
    }

    open fun saveBmpToAlbum(context: Context, imageLoader: XPopupImageLoader, uri: Any) {
        val mainHandler = Handler(Looper.getMainLooper())
        val executor = Executors.newSingleThreadExecutor()
        executor.execute(Runnable {
            try {
                val source = imageLoader.getImageFile(context, uri)
                if (!isImage(uri)) return@Runnable
                //非图片，不走下面逻辑
                if (source == null) {
                    mainHandler.post {
                        Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show()
                    }
                    return@Runnable
                }
                //1. create path，保存到图片图库中

                val dirFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_PICTURES)
                val dirPath = dirFile.absolutePath
                if (!dirFile.exists()) dirFile.mkdirs()

                val type = ImageHeaderParser.getImageType(FileInputStream(source))
                val ext = getFileExt(type)
                val target = File(dirPath, System.currentTimeMillis().toString() + "." + ext)
                //2. save
                writeFileFromIS(target, FileInputStream(source))
                //3. notify
                MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf("image/$ext")) { path, uri ->
                    mainHandler.post {
                        Toast.makeText(context, context.getString(R.string.file_is_saved) + path, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                mainHandler.post {
                    Toast.makeText(context, R.string.error_manage_perm_deny, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getFileExt(type: ImageType): String? {
        when (type) {
            ImageType.GIF -> return "gif"
            ImageType.PNG, ImageType.PNG_A -> return "png"
            ImageType.WEBP, ImageType.WEBP_A -> return "webp"
            ImageType.JPEG -> return "jpeg"
        }
        return "jpeg"
    }


    private fun writeFileFromIS(file: File, `is`: InputStream): Boolean {
        var os: OutputStream? = null
        return try {
            os = BufferedOutputStream(FileOutputStream(file))
            val data = ByteArray(8192)
            var len: Int
            while (`is`.read(data, 0, 8192).also { len = it } != -1) {
                os.write(data, 0, len)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun getImplLayoutId(): Int {//上层自定义页面，显示加载进度条
        return R.layout.layout_xpopup_loading
    }

    protected fun isImage(uri: Any?): Boolean {
        var isVideo = false
        uri?.let {
            //检查是否是图片
            if (uri is DynamicMedia) {
                isVideo = uri.type == uri.getVideoType()
            } else if (uri is Photo) {
                isVideo = uri.type.contains(Type.VIDEO)
            }
        }
        return !isVideo
    }
}