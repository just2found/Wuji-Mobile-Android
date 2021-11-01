/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.weline.mediaplayer.demo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Pair
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.DebugTextViewHelper
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import io.weline.mediaplayer.IntentUtil
import io.weline.mediaplayer.R
import io.weline.mediaplayer.internal.exo.DownloadTracker
import io.weline.mediaplayer.internal.exo.ExoUtil
import java.util.*

/**
 * An activity that plays media using [SimpleExoPlayer].
 */
class DemoPlayerActivity : AppCompatActivity(), View.OnClickListener, StyledPlayerControlView.VisibilityListener {
    protected var playerView: StyledPlayerView? = null
    protected var debugRootView: LinearLayout? = null
    protected var debugTextView: TextView? = null
    protected var player: SimpleExoPlayer? = null
    private var isShowingTrackSelectionDialog = false
    private var selectTracksButton: Button? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private var mediaItems: List<MediaItem>? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var debugViewHelper: DebugTextViewHelper? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    private var startAutoPlay = false
    private var startWindow = 0
    private var startPosition: Long = 0

    // For ad playback only.
    private var adsLoader: AdsLoader? = null

    //    private SimplePlayer simplePlayer;
    // Activity lifecycle.
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSourceFactory = ExoUtil.getDataSourceFactory( /* context= */this)
        setContentView()
        debugRootView = findViewById(R.id.controls_root)
        debugTextView = findViewById(R.id.debug_text_view)
        selectTracksButton = findViewById(R.id.select_tracks_button)
        if (selectTracksButton != null) {
            selectTracksButton!!.setOnClickListener(this)
        }
        playerView = findViewById(R.id.player_view)
        playerView!!.setControllerVisibilityListener(this)
        playerView!!.setErrorMessageProvider(PlayerErrorMessageProvider())
        playerView!!.requestFocus()
        if (savedInstanceState != null) {
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS)
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY)
            startWindow = savedInstanceState.getInt(KEY_WINDOW)
            startPosition = savedInstanceState.getLong(KEY_POSITION)
        } else {
            val builder = ParametersBuilder( /* context= */this)
            trackSelectorParameters = builder.build()
            clearStartPosition()
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        releasePlayer()
        releaseAdsLoader()
        clearStartPosition()
        setIntent(intent)
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
            if (playerView != null) {
                playerView!!.onResume()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
            if (playerView != null) {
                playerView!!.onResume()
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView!!.onPause()
            }
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            if (playerView != null) {
                playerView!!.onPause()
            }
            releasePlayer()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        releaseAdsLoader()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size == 0) {
            // Empty results are triggered if a permission is requested while another request was already
            // pending and can be safely ignored in this case.
            return
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer()
        } else {
            showToast(R.string.storage_permission_denied)
            finish()
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateTrackSelectorParameters()
        updateStartPosition()
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters)
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay)
        outState.putInt(KEY_WINDOW, startWindow)
        outState.putLong(KEY_POSITION, startPosition)
    }

    // Activity input
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // See whether the player view wants to handle media or DPAD keys events.
        return playerView!!.dispatchKeyEvent(event) || super.dispatchKeyEvent(event)
    }

    // OnClickListener methods
    override fun onClick(view: View) {
        if (view === selectTracksButton && !isShowingTrackSelectionDialog
                && TrackSelectionDialog.willHaveContent(trackSelector)) {
            isShowingTrackSelectionDialog = true
            val trackSelectionDialog = TrackSelectionDialog.createForTrackSelector(
                    trackSelector  /* onDismissListener= */
            ) { isShowingTrackSelectionDialog = false }
            trackSelectionDialog.show(supportFragmentManager,  /* tag= */null)
        }
    }

    // PlayerControlView.VisibilityListener implementation
    override fun onVisibilityChange(visibility: Int) {
        debugRootView!!.visibility = visibility
    }

    // Internal methods
    protected fun setContentView() {
        setContentView(R.layout.demo_player_activity)
    }

    /**
     * @return Whether initialization was successful.
     */
    protected fun initializePlayer(): Boolean {
        if (player == null) {
            val intent = intent
            mediaItems = createMediaItems(intent)
            if (mediaItems!!.isEmpty()) {
                return false
            }
            val preferExtensionDecoders = intent.getBooleanExtra(IntentUtil.PREFER_EXTENSION_DECODERS_EXTRA, false)
            val renderersFactory = ExoUtil.buildRenderersFactory( /* context= */this, preferExtensionDecoders)
            val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory!!) //              .setAdsLoaderProvider(this::getAdsLoader)
                    .setAdViewProvider(playerView)
            trackSelector = DefaultTrackSelector( /* context= */this)
            trackSelector!!.parameters = trackSelectorParameters!!
            lastSeenTrackGroupArray = null
            player = SimpleExoPlayer.Builder( /* context= */this, renderersFactory)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .setTrackSelector(trackSelector!!)
                    .build()
            //            simplePlayer = SimplePlayer.Companion.getInstance(this);
//            player = simplePlayer.init();
//            trackSelector = (DefaultTrackSelector) player.getTrackSelector();
            player!!.addListener(PlayerEventListener())
            player!!.addAnalyticsListener(EventLogger(trackSelector))
            player!!.setAudioAttributes(AudioAttributes.DEFAULT,  /* handleAudioFocus= */true)
            player!!.playWhenReady = startAutoPlay
            playerView!!.player = player
            debugViewHelper = DebugTextViewHelper(player!!, debugTextView!!)
            debugViewHelper!!.start()
        }
        val haveStartPosition = startWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(startWindow, startPosition)
        }
        player!!.setMediaItems(mediaItems!!,  /* resetPosition= */!haveStartPosition)
        player!!.prepare()
        updateButtonVisibility()
        return true
    }

    private fun createMediaItems(intent: Intent): List<MediaItem> {
        val action = intent.action
        val actionIsListView = IntentUtil.ACTION_VIEW_LIST == action
        if (!actionIsListView && IntentUtil.ACTION_VIEW != action) {
            showToast(getString(R.string.unexpected_intent_action, action))
            finish()
            return emptyList()
        }
        val mediaItems = createMediaItems(intent, ExoUtil.getDownloadTracker( /* context= */this))
        val hasAds = false
        for (i in mediaItems.indices) {
            val mediaItem = mediaItems[i]
            if (!Util.checkCleartextTrafficPermitted(mediaItem)) {
                showToast(R.string.error_cleartext_not_permitted)
                finish()
                return emptyList()
            }
            if (Util.maybeRequestReadExternalStoragePermission( /* activity= */this, mediaItem)) {
                // The player will be reinitialized if the permission is granted.
                return emptyList()
            }
            val drmConfiguration = Assertions.checkNotNull(mediaItem.playbackProperties).drmConfiguration
            if (drmConfiguration != null) {
                if (Util.SDK_INT < 18) {
                    showToast(R.string.error_drm_unsupported_before_api_18)
                    finish()
                    return emptyList()
                } else if (!FrameworkMediaDrm.isCryptoSchemeSupported(drmConfiguration.uuid)) {
                    showToast(R.string.error_drm_unsupported_scheme)
                    finish()
                    return emptyList()
                }
            }
            //      hasAds |= mediaItem.playbackProperties.adsConfiguration != null;
        }
        if (!hasAds) {
            releaseAdsLoader()
        }
        return mediaItems
    }

    //  private AdsLoader getAdsLoader(MediaItem.AdsConfiguration adsConfiguration) {
    //    // The ads loader is reused for multiple playbacks, so that ad playback can resume.
    //    if (adsLoader == null) {
    //      adsLoader = new ImaAdsLoader.Builder(/* context= */ this).build();
    //    }
    //    adsLoader.setPlayer(player);
    //    return adsLoader;
    //  }
    protected fun releasePlayer() {
//        if (simplePlayer != null)
//            simplePlayer.releasePlayer();
        if (player != null) {
            updateTrackSelectorParameters()
            updateStartPosition()
            debugViewHelper!!.stop()
            debugViewHelper = null
            player!!.release()
            player = null
            mediaItems = emptyList()
            trackSelector = null
        }
        if (adsLoader != null) {
            adsLoader!!.setPlayer(null)
        }
    }

    private fun releaseAdsLoader() {
        if (adsLoader != null) {
            adsLoader!!.release()
            adsLoader = null
            playerView!!.overlayFrameLayout!!.removeAllViews()
        }
    }

    private fun updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector!!.parameters
        }
    }

    private fun updateStartPosition() {
        if (player != null) {
            startAutoPlay = player!!.playWhenReady
            startWindow = player!!.currentWindowIndex
            startPosition = Math.max(0, player!!.contentPosition)
        }
    }

    protected fun clearStartPosition() {
        startAutoPlay = true
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }

    // User controls
    private fun updateButtonVisibility() {
        if (trackSelector != null) {
            selectTracksButton!!.isEnabled = player != null && TrackSelectionDialog.willHaveContent(trackSelector)
        }
    }

    private fun showControls() {
        debugRootView!!.visibility = View.VISIBLE
    }

    private fun showToast(messageId: Int) {
        showToast(getString(messageId))
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                showControls()
            }
            updateButtonVisibility()
        }

        override fun onPlayerError(e: ExoPlaybackException) {
            if (isBehindLiveWindow(e)) {
                player!!.seekToDefaultPosition()
                player!!.prepare()
            } else {
                updateButtonVisibility()
                showControls()
            }
        }

        override fun onTracksChanged(
                trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
            updateButtonVisibility()
            if (trackGroups !== lastSeenTrackGroupArray) {
                val mappedTrackInfo = trackSelector!!.currentMappedTrackInfo
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video)
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio)
                    }
                }
                lastSeenTrackGroupArray = trackGroups
            }
        }
    }

    private inner class PlayerErrorMessageProvider : ErrorMessageProvider<ExoPlaybackException> {
        override fun getErrorMessage(e: ExoPlaybackException): Pair<Int, String> {
            var errorString = getString(R.string.error_generic)
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                val cause = e.rendererException
                if (cause is DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    val decoderInitializationException = cause
                    errorString = if (decoderInitializationException.codecInfo == null) {
                        if (decoderInitializationException.cause is DecoderQueryException) {
                            getString(R.string.error_querying_decoders)
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            getString(
                                    R.string.error_no_secure_decoder, decoderInitializationException.mimeType)
                        } else {
                            getString(R.string.error_no_decoder, decoderInitializationException.mimeType)
                        }
                    } else {
                        getString(
                                R.string.error_instantiating_decoder,
                                decoderInitializationException.codecInfo!!.name)
                    }
                }
            }
            return Pair.create(0, errorString)
        }
    }

    companion object {
        // Saved instance state keys.
        private const val KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters"
        private const val KEY_WINDOW = "window"
        private const val KEY_POSITION = "position"
        private const val KEY_AUTO_PLAY = "auto_play"
        private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
            if (e.type != ExoPlaybackException.TYPE_SOURCE) {
                return false
            }
            var cause: Throwable? = e.sourceException
            while (cause != null) {
                if (cause is BehindLiveWindowException) {
                    return true
                }
                cause = cause.cause
            }
            return false
        }

        private fun createMediaItems(intent: Intent, downloadTracker: DownloadTracker): List<MediaItem> {
            val mediaItems: MutableList<MediaItem> = ArrayList()
            for (item in IntentUtil.createMediaItemsFromIntent(intent)) {
                val downloadRequest = downloadTracker.getDownloadRequest(Assertions.checkNotNull(item.playbackProperties).uri)
                if (downloadRequest != null) {
                    val builder = item.buildUpon()
                    builder
                            .setMediaId(downloadRequest.id)
                            .setUri(downloadRequest.uri)
                            .setCustomCacheKey(downloadRequest.customCacheKey)
                            .setMimeType(downloadRequest.mimeType)
                            .setStreamKeys(downloadRequest.streamKeys)
                            .setDrmKeySetId(downloadRequest.keySetId)
                            .setDrmLicenseRequestHeaders(getDrmRequestHeaders(item))
                    mediaItems.add(builder.build())
                } else {
                    mediaItems.add(item)
                }
            }
            return mediaItems
        }

        private fun getDrmRequestHeaders(item: MediaItem): Map<String, String>? {
            val drmConfiguration = item.playbackProperties!!.drmConfiguration
            return drmConfiguration?.requestHeaders
        }
    }
}