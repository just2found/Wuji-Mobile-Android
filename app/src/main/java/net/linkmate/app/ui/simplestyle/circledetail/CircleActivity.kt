package net.linkmate.app.ui.simplestyle.circledetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_circle_simplestyle.*
import kotlinx.android.synthetic.main.activity_circle_simplestyle.appBarLayout
import kotlinx.android.synthetic.main.activity_circle_simplestyle.ivBackgroud
import kotlinx.android.synthetic.main.activity_circle_simplestyle.ivPortrait
import kotlinx.android.synthetic.main.activity_circle_simplestyle.llTipsBarPanel
import kotlinx.android.synthetic.main.activity_circle_simplestyle.recyclerView
import kotlinx.android.synthetic.main.activity_circle_simplestyle.rootPanel
import kotlinx.android.synthetic.main.activity_circle_simplestyle.toolbar
import kotlinx.android.synthetic.main.activity_circle_simplestyle.tvBrief
import kotlinx.android.synthetic.main.activity_circle_simplestyle.tvHeaderTitle
import kotlinx.android.synthetic.main.activity_circle_simplestyle.tvName
import kotlinx.android.synthetic.main.activity_circle_simplestyle.tvToolbarTitle
import kotlinx.android.synthetic.main.activity_circle_simplestyle.vTitleBottomLine
import kotlinx.android.synthetic.wuji.activity_circle_simplestyle.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.base.MyApplication
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.data.ScoreHelper
import net.linkmate.app.manager.DevManager
import net.linkmate.app.manager.DeviceDialogManage
import net.linkmate.app.poster.PosterActivity
import net.linkmate.app.ui.activity.circle.CircleBriefActivity
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.ui.viewmodel.CircleCommonViewModel
import net.linkmate.app.ui.viewmodel.TrafficPriceEditViewModel
import net.linkmate.app.util.*
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.view.libwidget.singleClick

/** 圈子首页
 * @author Raleigh.Luo
 * date：20/11/19 19
 * describe：
 */
class CircleActivity : BaseActivity() {
    private val iconDefaultRes: Int = R.drawable.icon_defualt_circle
    private var networkId: String = ""
    private var mainDevId = ""
    protected lateinit var adapter: CircleDeviceAdapter
    protected val viewModel: CircleDeviceViewModel by viewModels()
    protected val mDeviceViewModel: DeviceViewModel by viewModels()
    private val briefCacheViewModel by viewModels<BriefCacheViewModel>()
    private val circleCommonViewModel by viewModels<CircleCommonViewModel>()
    protected val mTrafficPriceEditViewModel: TrafficPriceEditViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initObserver()
    }

    /**
     *
     */
    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    fun initView() {
        setContentView(R.layout.activity_circle_simplestyle)

        //背景图片为屏幕1/3高
        val layoutParams = ivBackgroud.layoutParams as ConstraintLayout.LayoutParams
        val screenWidth = Dp2PxUtils.getScreenWidth(this)
        layoutParams.height = (screenWidth * 0.75f + 0.5f).toInt();
        ivBackgroud.layoutParams = layoutParams
        networkId = intent.getStringExtra(FunctionHelper.NETWORK_ID)

        initToolBar()
        initRecycleView()
        ivPortrait.singleClick {
            showBottomDialog(intArrayOf(R.string.change_avatar))
        }
        toolbar.singleClick {
            showBottomDialog(intArrayOf(R.string.change_cover))
        }
        ivBackgroud.singleClick {
            showBottomDialog(intArrayOf(R.string.change_cover))
        }
        tvBrief.singleClick {
            showBottomDialog(intArrayOf(R.string.edit_summary))
        }
        llTipsBarPanel.viewTreeObserver.addOnGlobalLayoutListener {
            val height = llTipsBarPanel.measuredHeight
            if (llTipsBarPanelHeight != height) {
                llTipsBarPanelHeight = height
                val layoutParams = recyclerView.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.topMargin = llTipsBarPanelHeight
                recyclerView.layoutParams = layoutParams
            }
        }
    }

    private var llTipsBarPanelHeight = 0

    private fun initRecycleView() {
        adapter = CircleDeviceAdapter(this, viewModel, mDeviceViewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = net.linkmate.app.view.LinearLayoutManager(this)
        //动态监听头部标题的显示
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (MySPUtils.getBoolean(MyConstants.IS_LOGINED) && adapter.getDataCount() > 0) {
                    displayHeaderTitle()
                } else {
                    if (tvHeaderTitle.visibility == View.VISIBLE) {
                        tvHeaderTitle.visibility = View.GONE
                        vTitleBottomLine.visibility = View.GONE
                    }
                }

            }
        })
        viewModel.refresh()
    }

    /**
     * 显示头部标题
     */
    private fun displayHeaderTitle() {
        try {
            var position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (position < 0) position = 0
            val size = adapter.getDataCount()
            if (size > 0 && position < size) {
                if (tvHeaderTitle.visibility == View.GONE) {
                    tvHeaderTitle.visibility = View.VISIBLE
                    vTitleBottomLine.visibility = View.VISIBLE
                }
                val type = viewModel.devices.value?.get(position)?.type
                val title = getString(when (type) {
                    0 -> {
                        R.string.edge_node
                    }
                    1 -> {
                        R.string.devices
                    }
                    2 -> {
                        R.string.clients
                    }
                    else -> {
                        R.string.app_state_unknown
                    }
                })
                if (tvHeaderTitle.text.toString() != title) tvHeaderTitle.setText(title)
            }
        } catch (e: Exception) {
        }
    }

    private fun scrollToTop() {
        val layoutParams: ViewGroup.LayoutParams = appBarLayout.layoutParams
        val behavior = (layoutParams as CoordinatorLayout.LayoutParams).getBehavior()
        if (behavior is AppBarLayout.Behavior) {

            //1.先滑动到列表顶部
            recyclerView.scrollToPosition(0)
            //重要，否则无法触发AppBarLayout的behavior联动
            recyclerView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
            //2.再滑动到appBarLayout顶部，该方法不会触发AppBarLayout的behavior联动
            behavior.topAndBottomOffset = 0
            toolbar.setBackgroundResource(R.drawable.bg_brief_title_transparent)
            toolbar.alpha = 1f
            tvToolbarTitle.visibility = View.GONE
        }
    }

    private fun loadBrief(deviceId: String?) {
        if (rootPanel.getTag() != deviceId) {
            tvBrief.setTag(null)
            ivPortrait.setTag(null)
            ivBackgroud.setTag(null)
            rootPanel.setTag(deviceId)
        }

        if (ivPortrait.getTag() == null) ivPortrait.setImageResource(R.drawable.icon_defualt_circle)
        if (ivBackgroud.getTag() == null) ivBackgroud.setImageResource(R.color.darker)
        viewModel.startGetCircleBrief(deviceId)
    }

    private fun findDeviceBean(devId: String): DeviceBean? {
        return DevManager.getInstance().deviceBeans.find {
            it.id == devId
        }
    }

    private var isOpen = false
    private fun initObserver() {
        viewModel.devices.observe(this, Observer {
            viewModel.startGetDeviceBriefs(it)
            adapter.update()
            if (it.size > 0 && !isOpen) {
                displayHeaderTitle()
                it.forEach { device->
                    if(device.enServer?.srvMain == true){
                        if (device.isDevDisable && device.devDisableReason == 1) {
                        } else {
                            val deviceId = device.id
                            CheckStatus.checkDeviceStatus(this, supportFragmentManager, device,
                                    androidx.arch.core.util.Function { isNormalStatus: Boolean ->  //检查状态
                                        if (isNormalStatus) {
                                            if (!mTrafficPriceEditViewModel.whetherShowTrafficTips(this, deviceId,
                                                            Callback<Result<*>> { result: Result<*>? ->
                                                                if(findDeviceBean(deviceId)?.isOwner != true){
                                                                    isOpen = true
                                                                    open(device, 0,true)
                                                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                                                }
                                                                else{
                                                                    loading_container.visibility = View.GONE
                                                                }
                                                            })
                                            ) {
                                                if(findDeviceBean(deviceId)?.isOwner != true){
                                                    isOpen = true
                                                    open(device, 0,true)
                                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                                }
                                                else{
                                                    loading_container.visibility = View.GONE
                                                }
                                            }
                                        }
                                null
                            }, null)
                        }
                    }
                }
            }
        })
        viewModel.currentOptDevice.observe(this, Observer {
            if (it.isDevDisable && it.devDisableReason == 1) {
                ScoreHelper.showNeedMBPointDialog(this)
            } else {
                val deviceId = it.id
                CheckStatus.checkDeviceStatus(this, getSupportFragmentManager(), it, androidx.arch.core.util.Function { isNormalStatus: Boolean ->  //检查状态
                    if (isNormalStatus) {
                        if (mTrafficPriceEditViewModel.whetherShowTrafficTips(this, deviceId, Callback<Result<*>> { result: Result<*>? -> open(it, 0) })) {
                        } else {
                            open(it, 0)
                        }
                    }
                    null
                }, null)
            }
        })
        viewModel.currentLongOptDevice.observe(this, Observer {
            DeviceDialogManage.showDeviceDetailDialog(this, 0, 0, it, null)
        })
        viewModel.cancelLoading.observe(this, Observer {
            if (it == LoadingStatus.ACCESS_NAS) {
                mAccessDeviceTool?.cancel()
            }
        })
        viewModel.currentNetwork.observe(this, Observer {
            it?.let {
                if (it.isCharge == false) {//免费圈子,只显示头部
                    if (rootPanel.visibility == View.VISIBLE) {
                        rootPanel.visibility = View.GONE
                        toolbar.setBackgroundResource(R.drawable.bg_title_bar_gradient)
                        toolbar.alpha = 1f
                        tvToolbarTitle.visibility = View.VISIBLE
                    }
                } else {
                    if (rootPanel.visibility == View.GONE) {
                        rootPanel.visibility = View.VISIBLE
                        scrollToTop()
                    }
                }
                tvToolbarTitle.setText(it.netName)
                if (tvName.text.toString() != it.netName) tvName.setText(it.netName)
                if (!TextUtils.isEmpty(it.mainENDeviceId)) {
                    mainDevId = it.mainENDeviceId!!
                    loadBrief(mainDevId)
                } else {
                    loadBrief(null)
                }
            }
        })
        viewModel.circleBrief.observe(this, Observer {
            var brief: BriefModel? = null
            if (it != null && it.size > 0) {
                brief = it.get(0)
            }
            briefCacheViewModel.loadBrief(viewModel.startGetCircleBrief.value ?: "", brief,
                    tvContent = tvBrief,
                    ivImage = ivPortrait,
                    defalutImage = R.drawable.icon_defualt_circle,
                    ivBackgroud = ivBackgroud,
                    defalutBgImage = R.color.breif_bg_defualt_color,
                    For = BriefRepo.FOR_CIRCLE,
                    isLoadOneDeviceBrief = true)

        })
        viewModel.deviceBriefs.observe(this, Observer {
            viewModel.initDeviceBriefsMap()
        })

        viewModel.refreshAdapter.observe(this, Observer {
            if (it == -1) {//刷新所有
                adapter.update()
            }
        })
    }

    private val ACCESS_DETAIL_CODE = 0x002
    private var lastClickMenuTime = 0L
    private val interval = 400
    private fun initToolBar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.inflateMenu(R.menu.circle_header_menu_simplestyle)
        toolbar.setOnMenuItemClickListener {
            val currentTime = SystemClock.uptimeMillis()
            val lagTime = currentTime - lastClickMenuTime
            if (lagTime >= interval) {//非连续点击
                lastClickMenuTime = currentTime
                when (it.itemId) {
                    R.id.add -> {//圈子详情
                        CircleDetialActivity.startActivityForResult(this, Intent(this, CircleDetialActivity::class.java)
                                .putExtra(FunctionHelper.NETWORK_ID, CMAPI.getInstance().baseInfo.netid), ACCESS_DETAIL_CODE)
                    }
                }
            }
            true
        }

        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                //verticalOffset始终为0以下的负数，AppBarLayout竖直方向偏移距离px
                val absVerticalOffset = Math.abs(verticalOffset * 1.0f)
                //AppBarLayout总的距离px
//                val totalScrollRange = appBarLayout.totalScrollRange
                //封面背景总的距离px
                val backgroudImageHeight = ivBackgroud.height
                val toolbarHeight = toolbar.height
                //背景封面 和Toolbar的相差高度,也就是开始收缩的边界值，值越小 开始收起操作的范围越宽
                val distanceHeight = backgroudImageHeight - toolbarHeight
                //最小底部显示高度。显示Toolbar 头像高度的一半时，显示toolbar小标题，背景被隐藏，值越小 完全隐藏背景的范围越宽
                val minAlpheHeight = ivPortrait.height / 4f
//                //滑动到触发展开或折叠前，设置tvCircleName的透明度
//                if (absVerticalOffset / distanceHeight < 1) {
//                    tvCircleName.alpha = 1 - absVerticalOffset / distanceHeight
//                }
                when {
                    absVerticalOffset >= distanceHeight -> {//开始收起
                        if (tvToolbarTitle.visibility == View.GONE) {
                            toolbar.setBackgroundResource(R.drawable.bg_title_bar_gradient)
                            tvToolbarTitle.visibility = View.VISIBLE
                        }
                        if (absVerticalOffset - distanceHeight >= minAlpheHeight) {//达到Toolbar位置
                            if (toolbar.alpha != 1f) {//避免频繁绘制
                                toolbar.alpha = 1f
                            }
                        } else {
                            if (toolbar.alpha == 1f) {//避免频繁绘制
                                //最低透明度 0.5起，另一半动态的alpha值
                                val halfAlpha = ((absVerticalOffset - distanceHeight) / minAlpheHeight) / 2f
                                toolbar.alpha = 0.5f + halfAlpha
                            }
                        }
                    }
                    else -> {//开始展开
                        if (tvToolbarTitle.visibility == View.VISIBLE) {
                            toolbar.setBackgroundResource(R.drawable.bg_brief_title_transparent)
                            toolbar.alpha = 1f
                            tvToolbarTitle.visibility = View.GONE
                        }
                    }

                }
            }
        })
    }

    private fun showBottomDialog(ids: IntArray = intArrayOf(R.string.change_avatar,
            R.string.change_cover, R.string.edit_summary)) {
        val currentNetwork = viewModel.currentNetwork.value
        currentNetwork?.getMainENDeviceId()?.let {
            val devId = it
            val mainENServer = DevManager.getInstance().deviceBeans.find { it.id == devId }
            mainENServer?.let {
                //2.检查设备是否在线
                if (!it.isOnline) {//设备离线
                    ToastUtils.showToast(R.string.circle_main_en_server_offline)
                } else {//设备在线
                    if (currentNetwork.isOwner) {
                        mainDevId = it.id
                        circleCommonViewModel.showBottomDialog(this, it.id, ids) { intent, i ->
                            startActivityForResult(intent, i)
                        }
                    } else {
                        //3.检查设备状态是否正常
                        CheckStatus.checkDeviceStatus(this, supportFragmentManager,
                                it, androidx.arch.core.util.Function {
                            if (it) {//状态正常，进入简介
                                CircleBriefActivity.start(this,
                                        devId,
                                        currentNetwork.netName,
                                        currentNetwork.owner,
                                        currentNetwork.isOwner(),
                                        currentNetwork.netId, 0)
                            }
                            null
                        })
                    }
                }
            } ?: let {//找不到设备对象
                ToastUtils.showToast(R.string.circle_main_en_server_offline)
            }
        } ?: let {////没有设置主EN
            if (currentNetwork?.ownerId == CMAPI.getInstance().baseInfo.userId) {
                ToastUtils.showToast(R.string.please_setting_main_en_device)
            } else {
                ToastUtils.showToast(R.string.not_find_main_en)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQUEST_CODE_HD_FORMAT) {
            mAccessDeviceTool?.resetOpenHDManageValue()
        }
        if (requestCode == ACCESS_DETAIL_CODE && resultCode == FunctionHelper.CIRCLE_EXIT) {//退出圈子，关闭页面
            finish()
        } else if (requestCode == ACCESS_DETAIL_CODE && resultCode == Activity.RESULT_OK) {//修改圈子名称，刷新圈子
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.refreshCurrentNetwork(networkId)
            adapter.update()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    override fun getTopView(): View {
        return toolbar
    }

    private fun open(bean: DeviceBean, position: Int, isComeCircle: Boolean = false) {
        if (mAccessDeviceTool == null) {
            mAccessDeviceTool = AccessDeviceTool(this)
        }
        mAccessDeviceTool?.open(bean, position, androidx.arch.core.util.Function {
            adapter.update()
            null
        },isComeCircle)
    }

    /***-------访问设备------------------------------------------------------------------**/
    private var mAccessDeviceTool: AccessDeviceTool? = null


}