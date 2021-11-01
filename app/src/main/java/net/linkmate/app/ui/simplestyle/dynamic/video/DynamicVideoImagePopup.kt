package net.linkmate.app.ui.simplestyle.dynamic.video

import android.content.Context
import android.graphics.Matrix
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.huantansheng.easyphotos.constant.Type
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.lxj.xpopup.enums.PopupStatus
import com.lxj.xpopup.photoview.PhotoView
import kotlinx.android.synthetic.main.layout_xpopup_loading.view.*
import net.linkmate.app.R
import net.linkmate.app.data.model.dynamic.getDownloadUrlPath
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.VideoPlay
import net.sdvn.common.vo.DynamicMedia

/**动态预览图片界面
 * @author Raleigh.Luo
 * date：21/1/26 15
 * describe：
 */
class DynamicVideoImagePopup(context: Context) : ImageViewerPopupView(context) {
    //上一次页面的位置
    private var lastPosition = -1
    private lateinit var adapter: PhotoViewAdapter2
    override fun initPopupContent() {
        super.initPopupContent()
        adapter = PhotoViewAdapter2(object : Player.EventListener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                var mProgressBarVisibility = View.VISIBLE
                if (playWhenReady) {//开始播放
                    mProgressBarVisibility = View.GONE
                } else if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS
                        || reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY) {//没有资源播放,播放失败
                    mProgressBarVisibility = View.GONE
                }
                if (mProgressBar.visibility != mProgressBarVisibility) mProgressBar.visibility = mProgressBarVisibility
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                ToastUtils.showToast(R.string.error_generic)
                super.onPlayerError(error)
            }
        })
        pager.adapter = adapter
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                //停止上一个视频播放
                stopLastVideo()
                //播放当前视频
                playCurrentVideo()
                lastPosition = position
            }
        })
        pager.currentItem = position
        lastPosition = position
    }

    /**
     * 滑动到当前页面，自动播放视频
     */
    private fun playCurrentVideo() {
        val view = pager.findViewWithTag<View>(pager.currentItem)
        if (view != null && view is VideoPlay) {//当前是视频，则开始播放
            view.continuePlayer()
        }
    }

    private fun stopLastVideo() {
        if (lastPosition >= 0 && lastPosition != pager.currentItem) {
            val view = pager.findViewWithTag<View>(lastPosition)
            if (view != null && view is VideoPlay) {//当前是视频，则停止播放
                view.stopPlayer()
            }
        }

    }

    override fun getPopupLayoutId(): Int {//底层页面
        return R.layout._xpopup_image_viewer_popup_view2
    }


    /**
     * @param videoPlayListener 视频播放监听器，用于显示／隐藏加载进度条
     */
    inner class PhotoViewAdapter2(private val videoPlayListener: Player.EventListener) : PhotoViewAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val entity = urls.get(if (isInfinite) position % urls.size else position)
            var isVideo = false
            var videoUrl: String? = null

            //检查是否是图片
            if (entity is DynamicMedia) {
                isVideo = entity.type == entity.getVideoType()
                videoUrl = entity.getDownloadUrlPath(DynamicQueue.deviceId, DynamicQueue.deviceIP)
            } else if (entity is Photo) {
                isVideo = entity.type.contains(Type.VIDEO)
                videoUrl = entity.path
            }
            if (isVideo) {//是视频
                val view = VideoPlay(container.context)
                view.addListener(videoPlayListener)
                container.addView(view)
                view.setOnClickListener({ dismiss() })
                videoUrl?.let {
                    view.setPlayUri(Uri.parse(videoUrl))
                    if (this@DynamicVideoImagePopup.position == 0 && position == 0) {
                        //第一页不会触发滑动，自动播放
                        view.startPlayer(Uri.parse(videoUrl))
                    }
                }
                view.setTag(position)
                return view
            } else {//是图片
                return super.instantiateItem(container, position)
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            if (`object` is VideoPlay) {//释放视频播放资源
                `object`.releasePlayer()
            }
            super.destroyItem(container, position, `object`)
        }
    }

    override fun dismiss() {
        if (popupStatus != PopupStatus.Show) return
        popupStatus = PopupStatus.Dismissing
        if (srcView != null) {
            //snapshotView拥有当前pager中photoView的样子(matrix)
            val current = pager.getChildAt(pager.currentItem)
            current?.let {
                if (it is PhotoView) {
                    val matrix = Matrix()
                    it.getSuppMatrix(matrix)
                    snapshotView.setSuppMatrix(matrix)
                }
            }

            //释放视频资源
            for (i in 0 until pager.childCount) {
                val child = pager.getChildAt(i)
                if (child != null && child is VideoPlay) {
                    child.releasePlayer()
                }
            }
        }
        doDismissAnimation()
    }


}