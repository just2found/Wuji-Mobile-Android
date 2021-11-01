/*
 * Copyright 2018 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.linkmate.app.ui.simplestyle.dynamic.video

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.AudioManager
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import net.linkmate.app.net.RetrofitSingleton
import net.linkmate.app.service.DynamicQueue

/**
 * Creates and manages a [com.google.android.exoplayer2.ExoPlayer] instance.
 */

data class PlayerState(var window: Int = 0,
                       var position: Long = 0,
                       var whenReady: Boolean = true)

class PlayerHolder(private val context: Context,
                   private val playerState: PlayerState,
                   private val playerView: PlayerView) {
    val audioFocusPlayer: ExoPlayer

    // Create the player instance.
    init {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build()
        audioFocusPlayer = AudioFocusWrapper(
                audioAttributes,
                audioManager,
                ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
                        .also { playerView.player = it }
        )
    }

    private fun buildMediaSource(uris: Uri): MediaSource {
        val uriList = mutableListOf<MediaSource>()
        uriList.add(createExtractorMediaSource(uris))

        return ConcatenatingMediaSource(*uriList.toTypedArray())
    }

    private fun createExtractorMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer-learning", null)
        //添加动态Authorization
        dataSourceFactory.defaultRequestProperties.set("Authorization", String.format("Bearer %s", RetrofitSingleton.instance.getDynamicAuthorization(DynamicQueue.deviceId)
                ?: ""))


        val source = ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(context, null, dataSourceFactory))
                .createMediaSource(uri)
//  不循环      return source
        //循环播放
        return LoopingMediaSource(source)
    }

    // Prepare playback.
    fun start(uri: Uri) {
        // Load media.
        audioFocusPlayer.prepare(buildMediaSource(uri))
        // Restore state (after onResume()/onStart())
        with(playerState) {
            // Start playback when media has buffered enough
            // (whenReady is true by default).
            audioFocusPlayer.playWhenReady = whenReady
            audioFocusPlayer.seekTo(window, position)
            // Add logging.
//            attachLogging(audioFocusPlayer)
        }
    }

    fun addListener(listener: Player.EventListener) {
        audioFocusPlayer.addListener(listener)
    }

    // Stop playback and release resources, but re-use the player instance.
    fun stop() {
        with(audioFocusPlayer) {
            // Save state
            with(playerState) {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }
            // Stop the player (and release it's resources). The player instance can be reused.
            stop(true)
        }
    }

    // Destroy the player instance.
    fun release() {
        audioFocusPlayer.release() // player instance can't be used again.
    }

    /**
     * For more info on ExoPlayer logging, please review this
     * [codelab](https://codelabs.developers.google.com/codelabs/exoplayer-intro/#5).
     */
    private fun attachLogging(exoPlayer: ExoPlayer) {
        // Show toasts on state changes.
        exoPlayer.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
//                        ToastUtils.showToast("playback ended")
                    }
                    Player.STATE_READY -> when (playWhenReady) {
                        true -> {
//                            ToastUtils.showToast("playback started")
                        }
                        false -> {
//                            ToastUtils.showToast("playback paused")
                        }
                    }
                }
            }
        })
//        // Write to log on state changes.
//            fun getStateString(state: Int): String {
//                return when (state) {
//                    Player.STATE_BUFFERING -> "STATE_BUFFERING"
//                    Player.STATE_ENDED -> "STATE_ENDED"
//                    Player.STATE_IDLE -> "STATE_IDLE"
//                    Player.STATE_READY -> "STATE_READY"
//                    else -> "?"
//                }
//            }
    }

}