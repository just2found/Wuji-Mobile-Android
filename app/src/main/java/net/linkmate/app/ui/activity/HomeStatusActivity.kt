package net.linkmate.app.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_device_layout.*
import kotlinx.android.synthetic.main.include_dialog_device_bottom.*
import kotlinx.android.synthetic.main.include_dialog_device_header.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FullyLinearLayoutManager
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.linkmate.app.util.UIUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.RealtimeInfo
import net.sdvn.cmapi.protocal.EventObserver

/**
 * @author Raleigh.Luo
 * date：20/8/3 21
 * describe：
 */
class HomeStatusActivity : AppCompatActivity() {
    private lateinit var adapter: HomeStatusAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_device_layout)
        initNoStatusBar()

        ivHeaderImage.setImageResource(R.drawable.icon_defaultuser)
        //底部按钮显示＋头部副标题headerDescribe
        val bottomStatusParams: FunctionHelper.ViewStatusParams = FunctionHelper.ViewStatusParams()
        bottomStatusParams.headerDescribe = getString(R.string.status)
        bottomStatusParams.headerTitle = CMAPI.getInstance().baseInfo.account

        adapter = HomeStatusAdapter(this)
        initViewStatus(bottomStatusParams)
        recyclerView.layoutManager = FullyLinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        ivHeaderBack.visibility = View.GONE
        //关闭按钮
        ivHeaderClose.setOnClickListener {
            finish()
        }

    }

    private fun initNoStatusBar() {
        // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
        // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.decorView.systemUiVisibility = option
        //头部宽度
        root.setPadding(0, UIUtils.getStatueBarHeight(this), 0, 0)
        //修改状态栏为全透明
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

    }

    /**
     * 初始化头部副标题和底部按钮
     */
    private fun initViewStatus(statusParams: FunctionHelper.ViewStatusParams) {
        setBottomPanelStatus(statusParams)
        tvHeaderTitle.text = statusParams.headerTitle
        tvHeaderDescribe.text = statusParams.headerDescribe
    }

    /**
     * 初始化底部按钮
     */
    private fun setBottomPanelStatus(params: FunctionHelper.ViewStatusParams) {
        flBottom.visibility = if (TextUtils.isEmpty(params.bottomTitle)) View.GONE else View.VISIBLE
        params.bottomTitle?.let {
            if (params.bottomIsFullButton) {//蓝色按钮
                btnBottomConfirm.text = params.bottomTitle
                btnBottomConfirm.visibility = View.VISIBLE
                tvBottom.visibility = View.GONE
            } else {
                tvBottom.text = params.bottomTitle
                tvBottom.visibility = View.VISIBLE
                btnBottomConfirm.visibility = View.GONE
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.device_slide_out_to_bottom);
    }

    private val mEventObserver: EventObserver = object : EventObserver() {
        override fun onRealTimeInfoChanged(info: RealtimeInfo) {
            adapter.refreshRealTimeInfo(info)
        }
    }

    override fun onStart() {
        super.onStart()
        CMAPI.getInstance().subscribe(mEventObserver)
    }

    override fun onStop() {
        super.onStop()
        CMAPI.getInstance().unsubscribe(mEventObserver)
    }
}