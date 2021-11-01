package io.weline.mediaplayer

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.weline.mediaplayer.internal.exo.ExoUtil
import io.weline.mediaplayer.internal.exo.PlayerView
import io.weline.mediaplayer.internal.util.Preference
import io.weline.mediaplayer.internal.view.FloatWindow

const val KEY_WINDOW = "window"
const val KEY_POSITION = "position"

class SimplePlayer private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: SimplePlayer? = null

        private lateinit var mContext: Context

        @Nullable
        private var player: SimpleExoPlayer? = null
        private lateinit var trackSelector: DefaultTrackSelector
        private lateinit var trackSelectorParameters: DefaultTrackSelector.Parameters
        private lateinit var dataSourceFactory: DataSource.Factory

        private lateinit var floatWindowControl: FloatWindow

        private var startAutoPlay = false
        private var startWindow = 0
        private var startPosition: Long = 0
        private var mediaSources: ArrayList<MediaSource> = arrayListOf()
        private var mediaItems: ArrayList<MediaItem> = arrayListOf()


        fun getInstance(context: Context) =
                instance ?: synchronized(this) {
                    instance
                            ?: SimplePlayer(context).also {
                                instance = it
                                mContext = context
                            }
                }

    }

    fun init(): SimpleExoPlayer {
        if (player == null) {
            trackSelectorParameters = DefaultTrackSelector.ParametersBuilder(mContext).build()

            trackSelector = DefaultTrackSelector(mContext)
            trackSelector.parameters = trackSelectorParameters
            val renderersFactory: RenderersFactory = ExoUtil.buildRenderersFactory( /* context= */mContext, preferExtensionDecoders)
            dataSourceFactory = DefaultDataSourceFactory(mContext,
                    Util.getUserAgent(mContext, "myExoPlayer"))
            val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            player = SimpleExoPlayer.Builder(mContext, renderersFactory)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .setTrackSelector(trackSelector)
                    .build()
            player?.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
            player?.playWhenReady = startAutoPlay
            floatWindowControl = FloatWindow(mContext)
        }
        return player as SimpleExoPlayer
    }

    /**
     * 小窗播放
     * @param playerView 正在播放使用的 PlayerView
     */
    fun showFloatWindow(playerView: PlayerView) {
        floatWindowControl.showFloatWindow(playerView)
    }

    /**
     * 关闭小窗播放
     */
    fun closeFloatWindow() {
        floatWindowControl.closeFloatWindow()
    }

    /**
     * 是否正在小窗播放
     */
    fun isFloatWindowPlayer() = floatWindowControl.isShow()

    /**
     * 获取 SimpleExoPlayer
     */
    fun getPlayer(): SimpleExoPlayer {
        return player as SimpleExoPlayer
    }

    /**
     * 播放
     * @param arr 播放地址列表，支持多个
     */
    fun player(arr: List<String>?) {
        if (arr.isNullOrEmpty()) {
            Log.w("SimplePlayer", "播放地址列表 isNullOrEmpty")
            return
        }
        val mutableListOf = mutableListOf<MediaItem>()
        for (str in arr) {
            mutableListOf.add(MediaItem.fromUri(str))
        }
        setMediaItems(mediaItems)
    }

    /**
     * 释放播放器
     */
    fun releasePlayer() {
        updateTrackSelectorParameters()
        updateStartPosition()
        player?.release()
        player = null
        mediaSources.clear()
    }

    fun updateTrackSelectorParameters() {
        trackSelectorParameters = trackSelector.parameters
    }

    fun updateStartPosition() {
        if (player != null) {
            startAutoPlay = player!!.playWhenReady
            startWindow = player!!.currentWindowIndex
            startPosition = Math.max(0, player!!.contentPosition)
            if (mediaItems.isNotEmpty()) {
                val currentMediaItem = getCurrentMediaItem()
                if (currentMediaItem != null) {
                    var a: Int by Preference(context, currentMediaItem.mediaId + KEY_WINDOW, C.INDEX_UNSET)
                    var b: Long by Preference(context, currentMediaItem.mediaId + KEY_POSITION, C.TIME_UNSET)
                    if (startPosition >= 1000L && startPosition < player!!.contentDuration - 1000 * 10) {
                        a = startWindow
                        b = startPosition
                    } else {
                        a = C.INDEX_UNSET
                        b = C.TIME_UNSET
                    }
                }
            }
        }
    }

    fun getCurrentMediaItem(): MediaItem? {
        if (player == null) {
            return null
        }
        if (mediaItems.isNullOrEmpty()) {
            return null
        }
        return mediaItems[player!!.currentPeriodIndex]
    }

    private fun clearStartPosition() {
        if (mediaItems.isNotEmpty()) {
            val mediaItem = mediaItems.getOrNull(0)
            if (mediaItem != null) {
                val a: Int by Preference(context, mediaItem.mediaId + KEY_WINDOW, C.INDEX_UNSET)
                val b: Long by Preference(context, mediaItem.mediaId + KEY_POSITION, C.TIME_UNSET)
                startAutoPlay = true
                startWindow = a
                startPosition = b
            }
        }
    }

    fun setMediaItems(mediaItems: List<MediaItem>) {
        if (mediaItems.isEmpty()) return
        SimplePlayer.mediaItems.apply {
            clear()
            addAll(mediaItems)
        }
        clearStartPosition()
        mediaSources.clear()
        for (item in mediaItems) {
            mediaSources.add(ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(item))
        }
        player?.setAudioAttributes(AudioAttributes.DEFAULT, true)
        player?.playWhenReady = startAutoPlay
        val haveStartPosition = startWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player?.seekTo(startWindow, startPosition)
        }
        player?.setMediaSources(mediaSources,  /* resetPosition= */!haveStartPosition)
        player?.prepare()
    }

    var preferExtensionDecoders: Boolean by Preference(context, IntentUtil.PREFER_EXTENSION_DECODERS_EXTRA, false)

    fun savePrefDecoderExt(isEnable: Boolean) {
        preferExtensionDecoders = isEnable
    }

}