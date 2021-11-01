package net.linkmate.app.ui.simplestyle.device.self_check

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.activity_disk_self_check.*
import kotlinx.android.synthetic.main.include_title_bar.*
import kotlinx.android.synthetic.main.part_load_nas.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.TipsBar
import org.view.libwidget.singleClick

class DiskSelfCheckActivity : BaseActivity() {

    private val viewModel by viewModels<DiskSelfCheckModel>()
    private var requestInterval = 3000L
    private val queryDiskCheckReport = 2022

    private val valueAnimator by lazy {
        ValueAnimator.ofInt(0, 360 * 100).let {
            it.duration = 3000 * 100
            it.addUpdateListener { animation ->
                val rotateValue = animation.animatedValue as Int
                load_ani_img.rotation = rotateValue.toFloat()
            }
            it.interpolator = DecelerateInterpolator()
            it.repeatCount = -1
            it
        }
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper()) { msg ->
            if (msg.what == queryDiskCheckReport) {
                getDiskCheckReport()
                true
            }
            false
        }
    }


    private var flag = false;
    private fun getDiskCheckReport() {
        if (flag) {
            handler.sendEmptyMessageDelayed(queryDiskCheckReport, requestInterval)
            return
        }
        flag = true
        handler.removeMessages(queryDiskCheckReport)
        viewModel.getDiskCheckReport(this, deviceId).observe(this, Observer {
            flag = false
            when (it.status) {
                Status.SUCCESS -> {
                    if (valueAnimator.isRunning) {
                        valueAnimator.pause()
                    }
                    viewModel.lastTime?.let { time ->
                        last_time_tv.text = "${getString(R.string.last_check_time)}$time"
                    }
                    if (it.data.isNullOrEmpty()) {
                        //无数据
                        no_record_tv.visibility = View.VISIBLE
                        last_time_tv.visibility = View.GONE
                        recycle_view.visibility = View.GONE
                        load_nas_part.visibility = View.GONE
                        start_check_btn.setBackgroundResource(R.drawable.bg_button_theme_primary_radius)
                        start_check_btn.text = getString(R.string.start_check)
                    } else {
                        no_record_tv.visibility = View.GONE
                        load_nas_part.visibility = View.GONE
                        last_time_tv.visibility = View.VISIBLE
                        recycle_view.visibility = View.VISIBLE
                        start_check_btn.setBackgroundResource(R.drawable.bg_button_theme_primary_radius)
                        start_check_btn.text = getString(R.string.check_again)
                        val adapter = RdAddFileAdapter()
                        adapter.setNewData(it.data)
                        recycle_view.adapter = adapter
                    }
                }
                Status.ERROR -> {
                    if (valueAnimator.isRunning) {
                        valueAnimator.pause()
                    }
                    ToastUtils.showToast(it.code?.let { it1 -> V5HttpErrorNo.getResourcesId(it1) })
                    start_check_btn.setBackgroundResource(R.drawable.bg_button_theme_primary_radius)
                    no_record_tv.visibility = View.VISIBLE
                    last_time_tv.visibility = View.GONE
                    recycle_view.visibility = View.GONE
                    load_nas_part.visibility = View.GONE
                    start_check_btn.text = getString(R.string.start_check)
                }

                Status.LOADING -> {
                    start_check_btn.setBackgroundResource(R.drawable.bg_button_gray_radius)
                    load_progress_tv.text = it.message
                    handler.sendEmptyMessageDelayed(queryDiskCheckReport, requestInterval)
                }
            }
        })
    }


    //开始磁盘检测
    private fun startCheck() {
        if (!valueAnimator.isRunning) valueAnimator.start()
        if (valueAnimator.isPaused) valueAnimator.resume()
        load_nas_part.visibility = View.VISIBLE
        load_progress_tv.text = "0%"
        no_record_tv.visibility = View.GONE
        last_time_tv.visibility = View.GONE
        recycle_view.visibility = View.GONE
        start_check_btn.setBackgroundResource(R.drawable.bg_button_gray_radius)
        viewModel.startDiskSelfCheck(deviceId).observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                handler.sendEmptyMessageDelayed(queryDiskCheckReport, requestInterval)
            } else {
                if (!valueAnimator.isPaused) valueAnimator.pause()
                no_record_tv.visibility = View.VISIBLE
                last_time_tv.visibility = View.GONE
                recycle_view.visibility = View.GONE
                load_nas_part.visibility = View.GONE
                start_check_btn.setBackgroundResource(R.drawable.bg_button_theme_primary_radius)
                ToastUtils.showToast(it.code?.let { it1 -> V5HttpErrorNo.getResourcesId(it1) })
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //布局
        setContentView(R.layout.activity_disk_self_check)
        itb_iv_left.isVisible = true
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.singleClick {
            onBackPressed()
        }
        itb_tv_title.setText(R.string.disk_self_check)
        start_check_btn.setOnClickListener {
            if (!valueAnimator.isPaused)
                return@setOnClickListener
            startCheck()
        }
        valueAnimator.start()
        getDiskCheckReport()
    }


    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    override fun getTopView(): View {
        return toolbar
    }
}