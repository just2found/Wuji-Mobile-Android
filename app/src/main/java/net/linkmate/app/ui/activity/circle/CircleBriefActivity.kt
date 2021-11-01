package net.linkmate.app.ui.activity.circle

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import io.weline.repo.data.model.DataCircleIntroduction
import kotlinx.android.synthetic.main.activity_brief.*
import kotlinx.android.synthetic.main.activity_brief.toolbar
import kotlinx.android.synthetic.main.activity_brief.toolbar_layout
import kotlinx.android.synthetic.main.activity_brief.tvName
import kotlinx.android.synthetic.main.activity_circle_simplestyle.*
import kotlinx.android.synthetic.main.include_brief_content_scrolling.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.ui.viewmodel.CircleCommonViewModel
import net.linkmate.app.ui.viewmodel.CircleCommonViewModel.Companion.REQUEST_CODE
import net.linkmate.app.ui.viewmodel.CircleCommonViewModel.Companion.REQUEST_CODE_BG
import net.linkmate.app.ui.viewmodel.CircleCommonViewModel.Companion.REQUEST_CODE_ICON
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.TipsBar
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.constant.AppConstants
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.singleClick
import timber.log.Timber
import kotlin.properties.Delegates


//getTipsBar 与tipsBar的get方法 kotlin 方法重名，所以需要重命名
/**圈子简介
 * @author Raleigh.Luo
 * date：20/10/27 16
 * describe：
 */
class CircleBriefActivity : BaseActivity() {
    private var isAdmin: Boolean = false
    private var circleOwner: String? = null
    private var circleName: String? = null
    private var circleId: String? = null
    private val briefCacheViewModel by viewModels<BriefCacheViewModel>()
    private var iconDefaultRes by Delegates.notNull<Int>()
    private val circleCommonViewModel by viewModels<CircleCommonViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (deviceId == null) {
            ToastUtils.showError(R.string.msg_error_illegal_operation)
            finish()
            return
        }
        setContentView(R.layout.activity_brief)
        circleName = intent.getStringExtra("circle_name")
        circleOwner = intent.getStringExtra("circle_owner")
        circleId = intent.getStringExtra("circle_id")
        isAdmin = intent.getBooleanExtra("is_admin", false)
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
        app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                //verticalOffset始终为0以下的负数
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
            }
        })
        iconDefaultRes = R.drawable.icon_defualt_circle
        ab_iv_right.isVisible = isAdmin
        tvTitle.text = circleName
        tvName.text = circleOwner

        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            //请求远端数据
            BriefManager.requestRemoteBrief(deviceId, BriefRepo.FOR_CIRCLE, BriefRepo.ALL_TYPE,true)
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

    private fun showBottomDialog(ids: IntArray = intArrayOf(R.string.change_avatar,
            R.string.change_cover, R.string.edit_summary)) {
        circleCommonViewModel.showBottomDialog(this, deviceId, ids) { intent, i ->
            startActivityForResult(intent, i)
        }
    }

    private fun initObserver() {
        circleCommonViewModel.startGetDeviceBrief(deviceId)
        circleCommonViewModel.deviceBrief.observe(this, Observer {
            swipe_refresh_layout.isRefreshing = false
            var brief: BriefModel? = null
            if (it != null && it.size > 0) {
                brief = it.get(0)
            }
            briefCacheViewModel.loadBrief(deviceId, brief,
                    tvContent = tvContent,
                    ivImage = ivIcon,
                    defalutImage = R.drawable.icon_defualt_circle,
                    ivBackgroud = ivTitleBg,
                    defalutBgImage = R.color.breif_bg_defualt_color,
                    For = BriefRepo.FOR_CIRCLE,
                    isLoadOneDeviceBrief = true
            )
        })
    }



    fun onClick(view: View) {
//        when(view.id){
//            R.id.ivBack ->{//返回
//                finish()
//            }
//            R.id.ivMore ->{//更多
//
//            }
//            R.id.tvTextOpt ->{//全文／收起
//
//            }
//        }
    }

    override fun getTopView(): View? {
        return toolbar
    }

    override fun getTipsBar(): TipsBar? {
        return null
    }

    companion object {
        @JvmStatic
        fun start(context: Activity, deviceId: String, circle_name: String?, circle_owner: String?, is_admin: Boolean, circle_id: String?, requestCode: Int = 0) {
            context.startActivityForResult(Intent(context, CircleBriefActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
                    .putExtra("circle_name", circle_name)
                    .putExtra("circle_owner", circle_owner)
                    .putExtra("is_admin", is_admin)
                    .putExtra("circle_id", circle_id), requestCode)
        }
    }
}