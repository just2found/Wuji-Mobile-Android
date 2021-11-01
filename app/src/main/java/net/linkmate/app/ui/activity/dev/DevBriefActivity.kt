package net.linkmate.app.ui.activity.dev

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.rxjava.rxlife.RxLife
import io.weline.devhelper.IconHelper
import kotlinx.android.synthetic.main.activity_brief.*
import kotlinx.android.synthetic.main.include_brief_content_scrolling.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.ui.viewmodel.DevCommonViewModel
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.TipsBar
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.singleClick
import kotlin.properties.Delegates


/**设备简介
 * @author admin
 * date：20/10/27 16
 * describe：
 */
class DevBriefActivity : BaseActivity() {
    private var isAdmin: Boolean = false
    private var devName: String? = null
    private var devOwner: String? = null
    private var devClass: Int = 0
    private var iconDefaultRes by Delegates.notNull<Int>()
    private val briefCacheViewModel by viewModels<BriefCacheViewModel>()
    private val commonViewModel by viewModels<DevCommonViewModel>()
    private val mDeviceViewModel by viewModels<DeviceViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (deviceId == null) {
            ToastUtils.showError(R.string.msg_error_illegal_operation)
            finish()
            return
        }
        setContentView(R.layout.activity_brief)
        isAdmin = intent.getBooleanExtra("is_admin", false)
        devName = intent.getStringExtra("dev_name")
        devOwner = intent.getStringExtra("dev_owner")
        devClass = intent.getIntExtra("dev_class", 0)
        initView()
        initObserver()
    }

    private fun initView() {
//        ivIcon
//        tvTitle
//        tvName
//        tvContent
//        tvTextOpt
        //设置背景图
//        toolbar_layout.setBackgroundResource()
        ab_iv_left.setOnClickListener() {//返回按钮
            finish()
        }
        val bgParam = ivTitleBg.layoutParams
        val screenWidth = Dp2PxUtils.getScreenWidth(this)
        val height = (screenWidth * MyConstants.COVER_W_H_PERCENT + 0.5f).toInt();
        bgParam.height = height
        ivTitleBg.layoutParams = bgParam

        val titleParam = ivTitleTransparentBg.layoutParams
        val statueBarHeight = UIUtils.getStatueBarHeight(this)
        titleParam.height = screenWidth * 2 / 10 + statueBarHeight
        ivTitleTransparentBg.layoutParams = titleParam

        val layoutParams = toolbar.layoutParams
        layoutParams.height = layoutParams.height + statueBarHeight
        toolbar.layoutParams = layoutParams

        toolbar_layout.minimumHeight = toolbar_layout.minimumHeight + statueBarHeight

        ab_iv_right.singleClick {
            //更多
//                startActivity(Intent(this,EditCircleBriefActivity::class.java))
            showBottomDialog()
        }
        app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset -> //verticalOffset始终为0以下的负数
            //verticalOffset始终为0以下的负数
            val percent = Math.abs(verticalOffset * 1.0f) / appBarLayout.totalScrollRange
            val limit = 0.8f
            //percent == 1f ->{//头部完全收起
            //percent == 0f ->{//头部展开
            //i = 0是表示完全展开，向上折叠到最小时 i = -totalScrollRange
            //i = 0是表示完全展开，向上折叠到最小时 i = -totalScrollRange
            if (verticalOffset == 0) {
                swipe_refresh_layout.setEnabled(true)
            } else {
                swipe_refresh_layout.setEnabled(false)
            }

            when {
                percent > limit -> {
                    clHeaderPanel.visibility = View.GONE

                    toolbar.setBackgroundColor(Color.argb((0xff * (percent - limit) / (1 - limit)).toInt(), 0xec, 0xec, 0xec))

                    ivTitleTransparentBg.visibility = View.GONE

                    ab_tv_title.text = tvTitle.text
                    ab_tv_title.visibility = View.VISIBLE
                    ab_tv_title.setTextColor(Color.argb((0xff * (percent - limit) / (1 - limit)).toInt(), 0x33, 0x33, 0x33))

                    ab_iv_left.alpha = (percent - limit) / (1 - limit)
                    ab_iv_right.alpha = (percent - limit) / (1 - limit)
                    ab_iv_left.setImageResource(R.drawable.icon_return_black)
                    ab_iv_right.setImageResource(R.drawable.icon_setting_black)

                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                }
                percent <= limit -> {
                    clHeaderPanel.visibility = View.VISIBLE
                    clHeaderPanel.alpha = 1f - percent

                    toolbar.setBackgroundColor(Color.TRANSPARENT)

                    ivTitleTransparentBg.visibility = View.VISIBLE

                    ab_tv_title.visibility = View.INVISIBLE

                    ab_iv_left.alpha = 1f
                    ab_iv_right.alpha = 1f
                    ab_iv_left.setImageResource(R.drawable.icon_return)
                    ab_iv_right.setImageResource(R.drawable.icon_setting_white)

                    window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                }
            }
        })
        iconDefaultRes = IconHelper.getIconByeDevClassSimple(devClass, true, true);
        ab_iv_right.isVisible = isAdmin
        tvTitle.text = devName ?: ""
        tvName.text = devOwner ?: ""
        tvTitle.text = devName ?: ""
        mDeviceViewModel.refreshDevNameById(deviceId)
                .`as`(RxLife.`as`(this))
                .subscribe({ s: String? ->
                    if (!s.isNullOrEmpty()) {
                        tvTitle.text = s
                    }
                }) { _: Throwable? ->

                }
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            //请求远端数据
            BriefManager.requestRemoteBrief(deviceId, BriefRepo.FOR_DEVICE, BriefRepo.ALL_TYPE, true)
        })
        ivIcon.isEnabled = isAdmin
        ivTitleBg.isEnabled = isAdmin
        tvContent.isEnabled = isAdmin
        ivIcon.singleClick {
            showBottomDialog(intArrayOf(R.string.change_avatar))
        }
        ivTitleBg.singleClick {
            showBottomDialog(intArrayOf(R.string.change_cover))
        }
        tvContent.singleClick {
            showBottomDialog(intArrayOf(R.string.edit_summary))
        }
    }

    private fun showBottomDialog(intArrayOf: IntArray? = null) {
        if (intArrayOf != null) {
            commonViewModel.showBottomDialog(this, deviceId, ids = intArrayOf, result = { intent, i ->
                startActivityForResult(intent, i)
            })
        } else {
            commonViewModel.showBottomDialog(this, deviceId, result = { intent, i ->
                startActivityForResult(intent, i)
            })
        }
    }

    private fun initObserver() {
        //获取本地数据
        mDeviceViewModel.startGetDeviceBrief(deviceId)
        //请求远端数据，强制获取
        BriefManager.requestRemoteBrief(deviceId, BriefRepo.FOR_DEVICE, BriefRepo.ALL_TYPE, true)
        mDeviceViewModel.deviceBrief.observe(this, Observer {
            var brief: BriefModel? = null
            if (it != null && it.size > 0) {
                brief = it.get(0)
            }
            briefCacheViewModel.loadBrief(deviceId, brief,
                    tvContent = tvContent,
                    ivImage = ivIcon,
                    defalutImage = iconDefaultRes,
                    ivBackgroud = ivTitleBg,
                    defalutBgImage = R.color.breif_bg_defualt_color,
                    isLoadOneDeviceBrief = true)
        })
    }


    override fun getTopView(): View? {
        return toolbar
    }

    override fun getTipsBar(): TipsBar? {
        return null
    }

    companion object {

        @JvmStatic
        fun start(context: Context, deviceId: String, deviceName: String, deviceOwner: String, isAdmin: Boolean, deviceClass: Int) {
            context.startActivity(Intent(context, DevBriefActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
                    .putExtra("dev_name", deviceName)
                    .putExtra("dev_owner", deviceOwner)
                    .putExtra("is_admin", isAdmin)
                    .putExtra("dev_class", deviceClass)

            )
        }

        @JvmStatic
        fun start(context: Activity, deviceId: String, deviceName: String, deviceOwner: String, isAdmin: Boolean, deviceClass: Int, requestCode: Int) {
            context.startActivityForResult(Intent(context, DevBriefActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
                    .putExtra("dev_name", deviceName)
                    .putExtra("dev_owner", deviceOwner)
                    .putExtra("is_admin", isAdmin)
                    .putExtra("dev_class", deviceClass), requestCode)
        }
    }
}