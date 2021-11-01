package io.weline.mediaplayer.internal.view

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import io.weline.mediaplayer.PlayerActivity
import io.weline.mediaplayer.R
import io.weline.mediaplayer.SimplePlayer
import io.weline.mediaplayer.internal.exo.PlayerControlView
import io.weline.mediaplayer.internal.exo.PlayerView

/**
 * @param mContext  Context
 * @param windowWidth  小窗宽
 * @param windowHeight 小窗高
 */
class FloatWindow(private var mContext: Context,
                  private var windowWidth: Int = 220,
                  private var windowHeight: Int = 140) : View.OnTouchListener {

    private var mWindowManager: WindowManager? = null
    private lateinit var layoutParams: WindowManager.LayoutParams

    private lateinit var floatingLayout: FrameLayout
    private lateinit var playerView: PlayerView
    private var isShow = false

    /**
     * 初始化小窗
     */
    private fun init() {
        if (mWindowManager == null) {
            mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            layoutParams = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            layoutParams.gravity = Gravity.RIGHT or Gravity.TOP
            layoutParams.format = PixelFormat.TRANSPARENT
            //悬浮窗尺寸
            layoutParams.width = (windowWidth * mContext.resources.displayMetrics.density).toInt()
            layoutParams.height = (windowHeight * mContext.resources.displayMetrics.density).toInt()
            //悬浮窗初始位置
            layoutParams.x = 0
            layoutParams.y = mContext.resources.displayMetrics.heightPixels * 2 / 5
            var inflater = LayoutInflater.from(mContext)
            floatingLayout = inflater.inflate(R.layout.float_window, null) as FrameLayout
            //PlayerView
            playerView = floatingLayout.findViewById(R.id.playerView)
            playerView.onTouchListener = this
            //悬浮窗关闭按钮
            floatingLayout.findViewById<ImageButton>(R.id.imageButtonFloatWindow)
                    .setOnClickListener(View.OnClickListener {
                        SimplePlayer.getInstance(mContext).releasePlayer()
                        closeFloatWindow()
                    })
        }
    }

    /**
     * 小窗播放
     * @param v 当前正在播放使用的playerView
     */
    fun showFloatWindow(v: PlayerView) {
        init()
        if (!isShow) {
            mWindowManager?.addView(floatingLayout, layoutParams)
            PlayerView.switchTargetView(SimplePlayer.getInstance(mContext).getPlayer(), v, playerView)
            playerView.setOnClickListener(PlayerControlView.MyOnClickListener {
                when (it.id) {
                    R.id.player_full_screen_enter -> {
                        closeFloatWindow()
                        val intent = Intent(mContext, PlayerActivity::class.java)
                        intent.putExtra("float", true)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        mContext.startActivity(intent)
                    }
                }
            })
            isShow = true
        }
    }

    /**
     * 关闭小窗播放
     */
    fun closeFloatWindow() {
        if (isShow) {
            playerView.player = null
            mWindowManager?.removeView(floatingLayout)
            isShow = false
        }
    }

    /**
     * 是否小窗播放
     */
    fun isShow(): Boolean = isShow

    /**
     * PlayerView  onTouch监听，同时分发onTouch事件到小窗这边用于拖动
     */
    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchStartX = event.rawX.toInt()
                mTouchStartY = event.rawY.toInt()

            }
            MotionEvent.ACTION_MOVE -> {
                var mTouchCurrentX = event.rawX.toInt()
                var mTouchCurrentY = event.rawY.toInt()
                layoutParams.x -= mTouchCurrentX - mTouchStartX
                layoutParams.y += mTouchCurrentY - mTouchStartY
                mWindowManager?.updateViewLayout(floatingLayout, layoutParams)
                mTouchStartX = mTouchCurrentX
                mTouchStartY = mTouchCurrentY
            }
        }
        return false
    }

    private var mTouchStartX = 0
    private var mTouchStartY = 0

}