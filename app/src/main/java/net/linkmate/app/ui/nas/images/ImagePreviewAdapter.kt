package net.linkmate.app.ui.nas.images

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.chrisbanes.photoview.PhotoView
import io.weline.libimageloader.GenTagWorker
import io.weline.libimageloader.OnProgressListener
import io.weline.libimageloader.ProgressInterceptor
import libs.source.common.utils.ToastHelper
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.sdvn.nascommon.model.glide.GlideCacheConfig
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.receiver.NetworkStateManager
import org.view.libwidget.CircleProgressView
import timber.log.Timber

/**
 *
 * @Description: 相册Adapter
 * @Author: todo2088
 * @CreateDate: 2021/1/29 17:33
 */

class ImagePreviewAdapter(private val photosViewModel: IPhotosViewModel<out DataFile>
                          , private val context: Context)
    : BaseQuickAdapter<DataFile, BaseViewHolder>(R.layout.item_photo_preview) {
    private val screenHeight = Utils.getWindowsSize(context,true)
    private val screenWidth = Utils.getWindowsSize(context,false)
    private var itemClickListener: OnItemClickListener? = null

    init {
        setLoadMoreView(FullFileLoadMoreView())
    }

    override fun convert(holder: BaseViewHolder, item: DataFile?) {
        item?.let { model ->
            val iconView = holder.getView<PhotoView>(R.id.icon)
            iconView.setOnClickListener {
                itemClickListener?.onItemClick(this, it, holder.adapterPosition)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iconView.transitionName = photosViewModel.getItemShareTransitionName(model)
            }
            val tvOriginalImage = holder.getView<TextView>(R.id.tv_original_image)
            val progressBar = holder.getView<CircleProgressView>(R.id.circleProgressView)
            val glideMode = photosViewModel.getGlideMode(model)
            tvOriginalImage.visibility = View.GONE
            progressBar.visibility = View.GONE

            val glideModeTb = photosViewModel.getGlideModeTb(model)
            val loadTb = Glide.with(iconView)
                    .load(glideModeTb)
            Glide.with(iconView)
                    .load(glideMode)
                    .thumbnail(loadTb)
                    .centerInside()
                    .override(screenWidth, screenHeight)
                    .onlyRetrieveFromCache(true)//默认加载缓存中原图
                    .addListener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(e: GlideException?, any: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                            Timber.d("onLoadFailed from cache $any")
                            //当加载原图失败时,显示原图加载
                            if (any == glideMode) {
                                tvOriginalImage.post {
                                    tvOriginalImage.visibility = View.VISIBLE
                                }
                            }
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            Timber.d("onResourceReady from cache $model")
                            return false
                        }
                    })
                    .error(glideModeTb)
                    .into(iconView)
            tvOriginalImage.setOnClickListener(View.OnClickListener {
                if (!NetworkStateManager.instance.isNetAvailable()) {
                    ToastHelper.showToast(R.string.network_not_available)
                    return@OnClickListener
                }
                if (!NetworkStateManager.instance.isEstablished()) {
                    ToastHelper.showToast(R.string.tip_wait_for_service_connect)
                    return@OnClickListener
                }
                val glideMode = photosViewModel.getGlideMode(model)
                if (glideMode == null) {
                    ToastHelper.showToast(R.string.load_failed)
                    return@OnClickListener
                }
                val tag = glideMode.cacheKey
                if (tag.isNotEmpty()) {
                    val onProgressListener: OnProgressListener = object : OnProgressListener {
                        override fun onProgress(tag: Any, bytesRead: Long,
                                                totalBytes: Long, isDone: Boolean,
                                                exception: GlideException?) {
                            val progress = (bytesRead * 100f / totalBytes + 0.5f).toInt()
                            if (progress > 0 || isDone) {
                                progressBar.progress = progress
                                progressBar.visibility = if (isDone) View.GONE else View.VISIBLE
                            }
                        }
                    }
                    ProgressInterceptor.addListeners(tag, onProgressListener)
                    ProgressInterceptor.setGenTagWorker(object : GenTagWorker {
                        override fun getTagByUrl(url: String): Any {
                            return GlideCacheConfig.getImageName(url)
                        }
                    })
                    progressBar.tag = tag

                    Glide.with(iconView)
                            .load(glideMode)
                            .centerInside()
                            .override(screenWidth, screenHeight)
                            .placeholder(iconView.drawable)
                            .addListener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(e: GlideException?, model: Any?,
                                                          target: Target<Drawable?>?,
                                                          isFirstResource: Boolean): Boolean {
                                    progressBar.visibility = View.GONE
                                    ProgressInterceptor.removeListeners(tag)
                                    tvOriginalImage.visibility = View.VISIBLE
                                    ToastHelper.showLongToast(R.string.load_failed)
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any?,
                                                             target: Target<Drawable?>?,
                                                             dataSource: DataSource?,
                                                             isFirstResource: Boolean): Boolean {
                                    progressBar.visibility = View.GONE
                                    ProgressInterceptor.removeListeners(tag)
                                    return false
                                }
                            })
                            .into(iconView)
                    tvOriginalImage.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                }
            })
        }
    }

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    fun updateData(data: List<DataFile>?) {
        val pagesModel: OneFilePagesModel<out DataFile> = photosViewModel.getPagesPicModel()
        val isRefreshing = pagesModel.page == 0
        val hasMorePage = pagesModel.hasMorePage()
        if (isRefreshing) {
            setNewData(data)
        } else {
            if (data?.count() ?: 0 > 0) {
                addData(data ?: listOf())
            }
        }
        if (hasMorePage) {
            loadMoreComplete()
        } else {
            loadMoreEnd()
        }
        setEnableLoadMore(hasMorePage)
    }
}

