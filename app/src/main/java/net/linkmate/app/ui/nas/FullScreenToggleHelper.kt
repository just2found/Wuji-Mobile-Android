package net.linkmate.app.ui.nas

import android.animation.Animator
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.UiThread
import net.linkmate.app.R
import net.sdvn.nascommon.iface.Callback
import timber.log.Timber

class FullScreenToggleHelper(val activity: Activity, val titleView: View? = null, val callback: Callback<Boolean>? = null) {

    @UiThread
    fun resetSystemUI() {
        systemUiVisibility?.let {
            activity.apply {
                window.decorView.systemUiVisibility = it
                navigationBarColor?.let {
                    window.navigationBarColor = it
                }
            }
        }
    }

    @UiThread
    fun toggleSystemUI() {
        if (fullScreenMode)
            showSystemUI()
        else
            hideSystemUI()
    }

    private var fullScreenMode: Boolean = false

    fun isFullScreen(): Boolean {
        return fullScreenMode
    }

    @UiThread
    fun hideSystemUI() {
        activity.apply {
            titleView?.let {
                it.animate()
                        .translationY((-it.height).toFloat().also {
                            Timber.d("hideSystemUI translationY:$it")
                        })
                        .setInterpolator(AccelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                publishResult(true)
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }
                        })
                        .setDuration(200)
                        .start()
            } ?: kotlin.run {
                publishResult(true)
            }
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)

            window.navigationBarColor = resources.getColor(R.color.bg_picture_viewer_title)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun publishResult(isFull: Boolean) {
        fullScreenMode = isFull
        callback?.result(isFull)
    }

    private var systemUiVisibility: Int? = null
    private var navigationBarColor: Int? = null
    private var titleBottom: Int = 0

    @UiThread
    fun setupSystemUI() {
        activity.apply {
            systemUiVisibility = window.decorView.systemUiVisibility
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                Timber.w("ui changed: $visibility")
            }
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            //动态修改状态栏颜色
            navigationBarColor = window.navigationBarColor
            window.navigationBarColor = resources.getColor(R.color.bg_picture_viewer_title)
            window.statusBarColor = Color.TRANSPARENT
            titleBottom = titleView?.bottom ?: 0
        }
    }

    @UiThread
    fun showSystemUI() {
        activity.apply {
            titleView?.let {
                it.animate()
                        .translationY((0).toFloat().also {
                            Timber.d("showSystemUI translationY:$it")
                        })
                        .setInterpolator(DecelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                publishResult(false)
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }
                        })
                        .setDuration(240)
                        .start()
            } ?: kotlin.run {
                publishResult(false)
            }

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
//           //动态修改状态栏颜色
            window.navigationBarColor = resources.getColor(R.color.bg_picture_viewer_title)
            window.statusBarColor = Color.TRANSPARENT
        }

    }
}