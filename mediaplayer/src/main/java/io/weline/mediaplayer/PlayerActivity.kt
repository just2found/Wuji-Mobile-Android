package io.weline.mediaplayer

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Util
import io.weline.mediaplayer.internal.exo.DownloadTracker
import io.weline.mediaplayer.internal.exo.ExoUtil
import io.weline.mediaplayer.internal.exo.PlayerControlView
import io.weline.mediaplayer.internal.util.AndroidUtils
import kotlinx.android.synthetic.main.activity_player.*
import java.util.*

/**
 * 播放器
 * 播放地址[Bundle]传递，[Key] :: urls  [value] :: ArrayList<String>
 */
class PlayerActivity : AppCompatActivity(), PlaybackPreparer {

    private var player: SimpleExoPlayer? = null
    private var simplePlayer: SimplePlayer? = null
    private var mediaItems: List<MediaItem>? = null
    private lateinit var orientationEventListener: OrientationEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        configurationChanged(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        initializePlayer()
        initToolbar()
        initView()
        initOrientationEventListener()
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.inflateMenu(R.menu.player_options_menu)
        val preferExtensionDecodersMenuItem = toolbar.menu.findItem(R.id.prefer_extension_decoders)
        preferExtensionDecodersMenuItem?.setVisible(ExoUtil.useExtensionRenderers())
        preferExtensionDecodersMenuItem.isChecked = simplePlayer?.preferExtensionDecoders ?: false
        toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.open_by_other_app -> {
                    simplePlayer?.getCurrentMediaItem()?.playbackProperties?.let {
                        val uri = it.uri
                        val type: String = it.mimeType ?: "video/*"
                        AndroidUtils.open(this, uri, type)
                    }
                    true
                }
                R.id.prefer_extension_decoders -> {
                    item.isChecked = !item.isChecked
                    simplePlayer?.savePrefDecoderExt(item.isChecked)
                    true
                }
                else -> {
                    false
                }
            }
        }

    }

    private fun initView() {
        playerView.setOnClickListener(PlayerControlView.MyOnClickListener {
            when (it.id) {
                R.id.player_back -> {
                    onBackPressed()
                }
                R.id.player_float_screen -> {
                    showFloatWindow()
                }
                R.id.player_full_screen_enter -> {
                    requestedOrientation(270)
                }
                R.id.player_full_screen_exit -> {
                    requestedOrientation(1)
                }
            }
        })
        playerView.setControllerVisibilityListener {
            if (it == View.VISIBLE) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        setupSystemUI()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        simplePlayer?.releasePlayer()
        setIntent(intent)
    }

    /**
     * 小窗播放
     */
    private fun showFloatWindow() {
        //检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(applicationContext)) {
            startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), 0)
        } else {
            simplePlayer?.showFloatWindow(playerView)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            //悬浮窗权限设置完后
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(applicationContext)) {
                    Handler().post {
                        if (Settings.canDrawOverlays(applicationContext)) {
                            showFloatWindow()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
            playerView.onResume()
        }
    }

    private fun initializePlayer(): Boolean {
        simplePlayer = SimplePlayer.getInstance(applicationContext)
        val isFloat = intent.getBooleanExtra("float", false)
        if (!isFloat) {
            val urls = intent.getStringArrayListExtra("urls")
            if (urls.isNullOrEmpty()) {
                mediaItems = createMediaItems(intent)
            }
            if (mediaItems.isNullOrEmpty() && urls.isNullOrEmpty()) {
                return false
            }
            player = simplePlayer!!.init()
            simplePlayer!!.closeFloatWindow()
            if (mediaItems?.isNotEmpty() == true) {
                simplePlayer!!.setMediaItems(mediaItems!!)
            } else {
                simplePlayer!!.player(urls)
            }
        } else {
            player = simplePlayer!!.init()
            simplePlayer!!.closeFloatWindow()
        }
        playerView.setShowLock(resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT)
        playerView.setUseSensorRotation(true)
        playerView.setPlaybackPreparer(this)
        playerView?.player = player
        player?.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                val progressBarVisibility = when (state) {
                    Player.STATE_BUFFERING -> {
                        View.VISIBLE
                    }
                    else -> View.GONE
                }
                player_loading?.visibility = progressBarVisibility
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                showToast(R.string.error_generic)
                if (BuildConfig.DEBUG)
                    error.printStackTrace()
            }
        })
        return true
    }

    private fun showToast(messageId: Int) {
        showToast(getString(messageId))
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun createMediaItems(intent: Intent): List<MediaItem>? {
        val action = intent.action
        val actionIsListView = IntentUtil.ACTION_VIEW_LIST == action
        if (!actionIsListView && IntentUtil.ACTION_VIEW != action) {
            showToast(getString(R.string.unexpected_intent_action, action))
            finish()
            return emptyList()
        }
        val mediaItems = createMediaItems(intent, ExoUtil.getDownloadTracker( /* context= */this))
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
        }
        return mediaItems
    }


    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            initializePlayer()
            playerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playerView.onPause()
            if (simplePlayer?.isFloatWindowPlayer() == false) player?.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            if (simplePlayer?.isFloatWindowPlayer() == false) player?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (simplePlayer?.isFloatWindowPlayer() == false) simplePlayer?.releasePlayer()
        orientationEventListener.disable()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        simplePlayer?.updateTrackSelectorParameters()
        simplePlayer?.updateStartPosition()
    }

    override fun preparePlayback() {
        player?.prepare()
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏先切换为竖屏
            requestedOrientation(1)
            return
        }
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mCurrentOrientation = resources.configuration.orientation
        configurationChanged(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT)
    }

    private fun configurationChanged(isPortrait: Boolean) {
        if (isPortrait) {
            /** 竖屏，显示状态栏 */
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            playerView.setShowFullScreenEnter(true)
            playerView.setShowFullScreenExit(false)
            playerView.setShowFloatScreen(true)
            playerView.setShowLock(false)
        } else {
            /** 横屏，全屏显示，不显示状态栏 */
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            playerView.setShowFullScreenEnter(false)
            playerView.setShowFullScreenExit(true)
            playerView.setShowFloatScreen(false)
            playerView.setShowLock(true)
        }
        playerView.setShowPortrait(isPortrait)
    }

    /**
     * 初始化方向监听
     * onCreate中初始化
     * onDestroy 中 disable注销
     */
    private fun initOrientationEventListener() {
        orientationEventListener = object : OrientationEventListener(applicationContext, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(rotation: Int) {
                // 设置竖屏
                if (isFinishing || !isScreenAutoRotate(applicationContext)) {
                    return
                }
                requestedOrientation(rotation) //根据传感器设置屏幕方向
            }
        }
        orientationEventListener.enable()
    }

    /**
     * 改变屏幕方向
     */
    private fun requestedOrientation(rotation: Int) { //此时的rotation为传感器旋转的角度
        if (playerView.isLocked) return
        if (rotation < 10 || rotation > 350) { // 手机顶部向上
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (rotation in 81..99) { // 手机左边向上
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        } else if (rotation in 171..189) { // 手机低边向上
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        } else if (rotation in 261..279) { // 手机右边向上
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    /**
     * 判断是否开启了 “屏幕自动旋转”,true则为开启
     */
    fun isScreenAutoRotate(context: Context): Boolean {
        var gravity = 0
        try {
            gravity = Settings.System.getInt(context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return gravity == 1
    }


    protected fun resetNoStatusBar() {
        val topView: View? = getTopView()
        topView?.setPadding(topView.paddingLeft, topView.paddingTop,
                topView.paddingRight, topView.paddingBottom)
    }

    protected fun initNoStatusBar() {
        val topView: View? = getTopView()
        topView?.setPadding(topView.paddingLeft, getStatueBarHeight(this),
                topView.paddingRight, topView.paddingBottom)
    }

    private fun getStatueBarHeight(context: Context): Int {
        val identifier = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (identifier > 0) {
            context.resources.getDimension(identifier).toInt()
        } else 0
    }

    private fun getTopView(): View? {
        return toolbar
    }


    private fun resetSystemUI() {
        systemUiVisibility?.let {
            window.decorView.systemUiVisibility = it
            navigationBarColor?.let {
                window.navigationBarColor = it
            }
        }
    }

    private var fullScreenMode: Boolean = false

    fun toggleSystemUI() {
        if (fullScreenMode)
            showSystemUI()
        else
            hideSystemUI()
    }

    private fun hideSystemUI() {
        runOnUiThread {
            getTopView()?.let {
                it.animate()
                        .translationY((-it.height).toFloat().also {
                            Log.d("PlayerActivty ", "hideSystemUI translationY:$it")
                        })
                        .setInterpolator(AccelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                resetNoStatusBar()
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }
                        })
                        .setDuration(200)
                        .start()
            }
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)

            fullScreenMode = true
            window.navigationBarColor = resources.getColor(R.color.control_view_bg)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private var systemUiVisibility: Int? = null
    private var navigationBarColor: Int? = null
    private var titleBottom: Int = 0
    private fun setupSystemUI() {
        systemUiVisibility = window.decorView.systemUiVisibility
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            Log.d("PlayerActivty ", "ui changed: $visibility")
        }
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        //动态修改状态栏颜色
        navigationBarColor = window.navigationBarColor
        val color = resources.getColor(R.color.control_view_bg)
        window.navigationBarColor = color
        window.statusBarColor = color
        titleBottom = getTopView()?.bottom ?: 0
        initNoStatusBar()
    }

    private fun showSystemUI() {
        runOnUiThread {
            getTopView()?.let {
                it.animate()
                        .translationY((0).toFloat().also {
                            Log.d("PlayerActivty ", "showSystemUI translationY:$it")
                        })
                        .setInterpolator(DecelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                initNoStatusBar()
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }
                        })
                        .setDuration(240)
                        .start()
            }

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            fullScreenMode = false
//           //动态修改状态栏颜色
            val color = resources.getColor(R.color.control_view_bg)
            window.navigationBarColor = color
            window.statusBarColor = color
        }
    }


    companion object {
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