package net.linkmate.app.ui.simplestyle.dynamic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.util.Function
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.lxj.xpopup.XPopup
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.activity_dynamic_simplestyle.*
import kotlinx.android.synthetic.main.activity_dynamic_simplestyle.appBarLayout
import kotlinx.android.synthetic.main.activity_dynamic_simplestyle.recyclerView
import kotlinx.android.synthetic.main.activity_dynamic_simplestyle.toolbar
import kotlinx.android.synthetic.main.activity_dynamic_simplestyle.tvToolbarTitle
import kotlinx.android.synthetic.main.item_dynamic_header_simplestyle.*
import kotlinx.android.synthetic.main.item_dynamic_header_simplestyle.ivBackgroud
import kotlinx.android.synthetic.main.item_dynamic_header_simplestyle.ivPortrait
import kotlinx.android.synthetic.main.item_dynamic_header_simplestyle.rootPanel
import kotlinx.android.synthetic.main.item_dynamic_header_simplestyle.tipsBar
import kotlinx.android.synthetic.main.item_dynamic_header_simplestyle.tvBrief
import libs.source.common.livedata.Status
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyApplication
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.DevManager
import net.linkmate.app.manager.SDVNManager
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.mine.score.RechargeActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DevicelDetailActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.linkmate.app.ui.fragment.BaseFragment
import net.linkmate.app.ui.simplestyle.MainViewModel
import net.linkmate.app.ui.simplestyle.dynamic.CircleStatus.*
import net.linkmate.app.ui.simplestyle.dynamic.delegate.ImageDisplayDelegate
import net.linkmate.app.ui.simplestyle.dynamic.detial.DynamicDetailActivity
import net.linkmate.app.ui.simplestyle.dynamic.publish.DynamicPublishActivity
import net.linkmate.app.ui.simplestyle.dynamic.related.RelatedActivity
import net.linkmate.app.util.MySPUtils
import net.linkmate.app.util.SoftKeyBoardListener
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.CommentPopupWindow
import net.linkmate.app.view.ForbidLinearLayoutManager
import net.linkmate.app.view.TipsBar
import net.linkmate.app.view.dynamicRefresh.DynamicSwipeRefreshLayout
import net.linkmate.app.view.dynamicRefresh.PublishProgressPopup
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.DynamicDBHelper
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.scorepay.UserScore
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.common.vo.Dynamic
import net.sdvn.scorepaylib.score.ScoreAPIUtil
import timber.log.Timber

/** 动态朋友圈
 *
 *
 *
 * date：20/11/21 10
 * describe：
 */
class DynamicFragment : BaseFragment() {
    private val viewModel: DynamicViewModel by viewModels()
    private lateinit var adapter: DynamicAdapter
    private val activityViewModel: MainViewModel by activityViewModels()

    companion object {
        private const val PUBLISH_REQUEST_CODE = 0x001
        private const val DETAIL_REQUEST_CODE = 0x002
        private const val PURCHASE_SCORE_CODE = 0x003
    }

    private val mCommentPopupWindow: CommentPopupWindow by lazy {
        val comment = CommentPopupWindow(requireContext())
        comment.sendCallBack = Function {
            comment.dismiss()
            val position = viewModel.commentEvent.value?.position ?: -1
            viewModel.startPublishComment(it)
            null
        }
        comment
    }

    //评论后，需滑动到底部，且数据动态刷新后，记录项position

    override fun getLayoutId(): Int {
        return R.layout.activity_dynamic_simplestyle
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        initSoftKeyBoardListener()
        initView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObserver()
    }

    /**
     * 监听 输入框键盘弹起
     */
    private fun initSoftKeyBoardListener() {
        val kbLinst = SoftKeyBoardListener(requireActivity())
        kbLinst.setOnSoftKeyBoardChangeListener(object : SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
            override fun keyBoardShow(softkeyBoardHeight: Int) {
                //最底部位置，因不同设备，可能有虚拟菜单，底部位置会不同，因此不可直接使用屏幕高度，需以vBottom在底部的Y坐标
                //评论弹框位置坐标Y ＝底部屏幕高度－评论弹框高度－输入框高度
                if (vBottom != null) {
                    //获取当前根视图在屏幕上显示的大小
                    val r = Rect()
                    requireActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(r)
                    //虚拟键盘在屏幕的起始Y位置,未包含评论输入框
                    val softKeyBoardLocationY = r.bottom
                    //评论输入框的起始Y位置：虚拟键盘在屏幕的起始Y位置－评论输入框的高度
                    val popupWindowLocationY = softKeyBoardLocationY - mCommentPopupWindow.windowHeight
                    //触发列表滑动到对应位置
                    viewModel.setCommentPopupWindowLocationY(popupWindowLocationY)
                }
            }

            override fun keyBoardHide(h: Int) {
            }
        })
    }

    /**
     * 检查是否已连接
     */
    private fun isEstablished(): Boolean {
        if (CMAPI.getInstance().isEstablished) {
            return true
        } else {
            ToastUtils.showToast(R.string.tip_wait_for_service_connect)
            return false
        }
    }

    private val emptyOperateCallback = Function<Int, Void> {
        when (it) {
            WITHOUT_DEVICE_SERVER.type -> {//无主EN，去设置
                if (isEstablished()) {
                    //同时刷新EN信息，因为可能主EN被切换配置另一台设备，没有推送，无法获取主EN信息
                    DevManager.getInstance().refreshENServerData()
                    NetsRepo.refreshNetList()
                    /*---设置主EN-----------------------------------------------*/
                    CircleDetialActivity.startActivityForResult(requireActivity(), Intent(requireActivity(), CircleDetialActivity::class.java)
                            .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, viewModel.mLastNetworkId)
                            .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.FUNCTION, net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_SETTING_MAIN_EN_DEVICE), net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_SETTING_MAIN_EN_DEVICE
                    )
                }
            }
            WITHOUT_POINTS.type -> {//积分不足，购买积分
                if (isEstablished()) {
                    startActivityForResult(Intent(context, RechargeActivity::class.java), PURCHASE_SCORE_CODE)
                }

            }

            WITHOUT_PURCHASE_CIRCLE_FLOW.type -> {//未选购流量，购买
                if (isEstablished()) {
                    /*---网络状态异常 需购买流量 //流量状态 0-正常 1-已到期 -1-未订购-----------------------------------------------*/
                    CircleDetialActivity.startActivityForResult(requireActivity(), Intent(requireActivity(), CircleDetialActivity::class.java)
                            .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, viewModel.mLastNetworkId)
                            .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.FUNCTION, net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_PURCHASE_FLOW), net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_PURCHASE_FLOW
                    )
                }
            }
            WITHOUT_PURCHASE_DEVICE_FLOW.type -> {
                if (isEstablished()) {
                    /*---设备流量异常， 选购流量-----------------------------------------------*/
                    DevicelDetailActivity.startActivityForResult(requireActivity(), Intent(requireActivity(), DevicelDetailActivity::class.java)
                            .putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.mLastDeviceId)
                            .putExtra(AppConstants.SP_FIELD_DEVICE_NAME, viewModel.mainENDevice?.name)
                            .putExtra(FunctionHelper.FUNCTION, FunctionHelper.DEVICE_PURCHASE_FLOW), FunctionHelper.DEVICE_PURCHASE_FLOW
                    )
                }
            }
            UNSUBSCRIBE_DEVICE_SERVER.type -> {//未订阅服务，订阅
                if (isEstablished()) {
                    /*---未开通设备服务-----------------------------------------------*/
                    DevicelDetailActivity.startActivityForResult(requireActivity(), Intent(requireActivity(), DevicelDetailActivity::class.java)
                            .putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.mLastDeviceId)
                            .putExtra(AppConstants.SP_FIELD_DEVICE_NAME, viewModel.mainENDevice?.name)
                            .putExtra(FunctionHelper.FUNCTION, FunctionHelper.DEVICE_PURCHASE_JOIN), FunctionHelper.DEVICE_PURCHASE_JOIN
                    )
                }
            }
            NOMARL.type -> {//没有动态，发表或请求失败，重试
                if (viewModel.isRequestFailed()) {
                    viewModel.refresh(true)
                } else {
                    startToPublish()
                }
            }
        }
        null
    }

    /**
     * 跳转到发布动态
     */
    private fun startToPublish() {
        if (DynamicQueue.getUploadIdentification()?.isCurrentCircle() ?: false) {//有正在上传的动态，显示进度弹框
            val dynamic = DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()?.boxFor(Dynamic::class.java)?.get(DynamicQueue.getUploadIdentification()?.autoIncreaseId
                    ?: 0L)
            dynamic?.let {
                val customPopup = PublishProgressPopup(requireActivity() as AppCompatActivity, dynamic, false, false)
                XPopup.Builder(requireContext())
                        .enableDrag(false)
                        .dismissOnTouchOutside(true)
                        .dismissOnBackPressed(true)
                        .asCustom(customPopup)
                        .show()
                true
            }
        } else {
            startActivityForResult(Intent(context, DynamicPublishActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, DynamicQueue.deviceId)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_IP, DynamicQueue.deviceIP),
                    PUBLISH_REQUEST_CODE
            )
        }
    }

    /**
     * 获取屏幕信息
     */
    private fun getScreenHeight(context: Context): Int {
        // 获取屏幕信息
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private lateinit var portraitDisplayDelegate: ImageDisplayDelegate
    private lateinit var bgDisplayDelegate: ImageDisplayDelegate

    //双击头部
    private var timesHeaderPressed: Long = 0
    private fun initView() {
        //背景图片为屏幕1/3高
        val layoutParams = ivBackgroud.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.height = (UIUtils.getSreenWidth(requireContext()) * MyConstants.COVER_W_H_PERCENT + 0.5f).toInt()
        ivBackgroud.layoutParams = layoutParams

        portraitDisplayDelegate = ImageDisplayDelegate.create(ivPortrait)
        bgDisplayDelegate = ImageDisplayDelegate.create(ivBackgroud)
        recyclerView.layoutManager = ForbidLinearLayoutManager(requireContext())
        adapter = DynamicAdapter(requireActivity(), viewModel, recyclerView, emptyOperateCallback)
        recyclerView.adapter = adapter

        toolbar.inflateMenu(R.menu.dynamic_header_menu_simplestyle)
        //隐藏发布图标
        toolbar.menu.findItem(R.id.add).setVisible(false)
        toolbar.menu.findItem(R.id.add).setIcon(R.drawable.icon_home_add_48)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add -> {//发布
                    if (!TextUtils.isEmpty(DynamicQueue.deviceId)) {//必须有主EN
                        startToPublish()
                    }
                }
            }
            true
        }

        toolbar.setOnClickListener {
            if (System.currentTimeMillis() - timesHeaderPressed < 1000) {
                timesHeaderPressed = 0
                //双击回到顶部
                scrollToTop()
            } else {
                timesHeaderPressed = System.currentTimeMillis()
            }
        }

        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (viewModel.swipeRefreshLayoutEnable.value == true) {
                    val isEnable = verticalOffset >= 0
                    if (isEnable) mSwipeRefreshLayout.isRefreshing = false
                    if (isEnable != mSwipeRefreshLayout.isEnabled) {
                        mSwipeRefreshLayout.isEnabled = isEnable
                    }////当滑动到顶部的时候开启,否则不可下拉刷新
                }
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
                val minAlpheHeight = ivPortrait.height / 2f
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

        //默认不可下拉
        mSwipeRefreshLayout.setOnRefreshListener {
            if (viewModel.isWithOutPoint && CMAPI.getInstance().isEstablished) {
                //没有积分时请求
                getScore(true)
            } else {
                getScore(false)
                viewModel.refreshAndCheckStatus(true, Function {
                    if (!it) {//没有进行刷新，关闭刷新动画
                        mSwipeRefreshLayout.isRefreshing = false
                    }
                    null
                })
                viewModel.refreshAboutMessage()
                //刷新圈子信息，可能在其它设备
                NetsRepo.refreshNetList()
            }
        }

        mSwipeRefreshLayout.setOnLoadListener(object : DynamicSwipeRefreshLayout.OnLoadListener {
            override fun onLoad() {
                viewModel.loadDynamicList()
            }
        })

        btnNewMessage.setOnClickListener {
            viewModel.clearRelatedMessage()
            activityViewModel.updateDynamicMessageCount(0)
            startActivity(Intent(requireContext(), RelatedActivity::class.java))
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isRecyclerViewTop() && (viewModel.aboutMessageResult.value?.data?.momentid?.size
                                ?: 0) > 0 && btnNewMessage.visibility == View.GONE) {
                    //滑动到mNestedScrollView的顶部了,且有与我相关消息&消息按钮未显示，这里显示下
                    btnNewMessage.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun isRecyclerViewTop(): Boolean {
        return !recyclerView.canScrollVertically(-1)
    }

    private var isOnActivityResult = false
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        isOnActivityResult = true
        if (requestCode == PURCHASE_SCORE_CODE) {
            if (CMAPI.getInstance().isEstablished) getScore()
        } else if (requestCode == PUBLISH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //发布成功，有新数据需刷新db
            viewModel.loadFromDB()
        } else if (requestCode == DETAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {//详情删除返回
            //动态被删除，需要刷新数据
            viewModel.reloadDynamicList()
        } else {
            viewModel.initCircleStatus()
        }
    }

    private val REFRESH_ABOUT_DELAY = 10 * 1000L //刷新与我相关的间隔为10秒
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
//            if (msg?.what == 0 && !isPause && CMAPI.getInstance().isEstablished) {//不在当前页面，不执行与我相关刷新机制
            if (msg?.what == 0) {//不在当前页面，不执行与我相关刷新机制
                viewModel.refreshAboutMessage()
            }
        }
    }

    override fun onResume() {
        DynamicQueue.isDynamicDisplayed = true
        super.onResume()
        viewModel.refreshAboutMessage()
        //获取最新数据，后台子线程更新，且已加载过一次后
        if (!isOnActivityResult) {//非从跳转页面返回，如发布，详情
            viewModel.refreshNewestDynamicList()
        } else {
            isOnActivityResult = false
        }
        if (!MySPUtils.getBoolean(MyConstants.IS_LOGINED)) viewModel.updateCircleStatus(WITHOUT_NETWORK)
        getScore()
        if (viewModel.circleStatus.value != NOMARL) {//切页面时，非正常状态，滑动到顶部
            scrollToTop()
            //非正常状态，刷新状态
            viewModel.initCircleStatus()
        }
    }

    override fun onPause() {
        DynamicQueue.isDynamicDisplayed = false
        super.onPause()
    }

    /**
     *  @param isFromPull 是否为下拉请求
     */
    private fun getScore(isFromPull: Boolean = false) {
        if (CMAPI.getInstance().isEstablished) {
            ScoreAPIUtil.getScore(null, UserScore::class.java, object : ResultListener<UserScore> {
                override fun success(tag: Any?, data: UserScore) {
                    if (data.data.mbpoint <= 0) {
                        viewModel.updateCircleStatus(WITHOUT_POINTS)
                        //必须在后
                        viewModel.isWithOutPoint = true
                        if (isFromPull) {
                            mSwipeRefreshLayout.isRefreshing = false
                        }
                    } else {
                        viewModel.isWithOutPoint = false
                    }
                }

                override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                    viewModel.isWithOutPoint = false
                }
            })
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

    /**
     * @param isClearCache 是否清除原有缓存数据
     */
    private fun initCircleHeader(title: String, breif: String, isClearCache: Boolean = true) {
        tvToolbarTitle.setText(R.string.dynamic)
        tvTitle.setText(title)
        if (isClearCache) {
            tvBrief.setTag(null)
            tvBrief.setText(breif)
            ivPortrait.setTag(null)
            ivPortrait.setImageResource(R.drawable.icon_defualt_circle)
            ivBackgroud.setTag(null)
            ivBackgroud.setImageResource(R.color.darker)
            ivBackgroud.setOnClickListener(null)
            ivPortrait.setOnClickListener(null)
        }
    }

    /**
     * 加载圈子简介信息
     */
    private fun loadCircleBrief(networkId: String, deviceId: String) {
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

    @SuppressLint("RestrictedApi")
    private fun initObserver() {
        SDVNManager.instance.liveDataConnectionStatus.observe(viewLifecycleOwner, Observer {
            if (it == Constants.CS_ESTABLISHED) {//重新获取与我相关消息
                viewModel.refreshAboutMessage()
                //获取积分，检测是否积分不足
                getScore()
            }
        })
        viewModel.showAddMenu.observe(requireActivity(), Observer {
            val addMenu = toolbar.menu.findItem(R.id.add)
            if (it) {
                if (!addMenu.isVisible)//显示
                    toolbar.menu.findItem(R.id.add).setVisible(true)
            } else {
                if (addMenu.isVisible)//没有网络对象，不显示
                    toolbar.menu.findItem(R.id.add).setVisible(false)
            }
        })
        viewModel.swipeRefreshLayoutEnable.observe(requireActivity(), Observer {
        })
        activityViewModel.dynamicMessageCount.observe(requireActivity(), Observer {
            if ((it ?: 0) > 0) {//有消息，不处理显示，只更改文字
                btnNewMessage.setText(resources.getQuantityString(R.plurals.dynamic_message_count, it, it))
            } else {//无消息，隐藏按钮
                btnNewMessage.visibility = View.GONE
            }

        })

        viewModel.circleStatus.observe(requireActivity(), Observer {
            if (it == WITHOUT_NETWORK || it == NONE) {//无网络,未登录未连接
                //清除数据
                initCircleHeader(getString(R.string.dynamic), "")
                viewModel.swipeRefreshLayoutEnable.value = false //刷新不可用
            }

            if (it != NOMARL) {
                viewModel.clearDynamicList()
                if (it != WITHOUT_NETWORK && it != NONE) {
                    viewModel.swipeRefreshLayoutEnable.value = true //刷新可用,仅可上拉
                    //禁止下拉
                    mSwipeRefreshLayout.isLoadEnable = false
                }
                viewModel.showAddMenu(false)
                adapter.notifyDataSetChanged()
                //滑动到顶部
                scrollToTop()
            } else if (it == NOMARL && viewModel.getDynamicListSize() == 0) {//空数据
                viewModel.swipeRefreshLayoutEnable.value = true //刷新可用,仅可上拉
                //禁止下拉
                mSwipeRefreshLayout.isLoadEnable = false
                //滑动到顶部
                scrollToTop()
            } else if (it == NOMARL) {
                viewModel.swipeRefreshLayoutEnable.value = true //刷新可用
            }

        })
        viewModel.currentNetwork.observe(requireActivity(), Observer {
            //初始化圈子状态
            viewModel.initCircleStatus()
            if (!viewModel.isSameDynamic()) {
                //非相同动态网络，先隐藏发布按钮
                viewModel.showAddMenu(false)
                //不同帐号不同网络时，先隐藏按钮
                activityViewModel.updateDynamicMessageCount(0)
                if (TextUtils.isEmpty(it?.netId) || !TextUtils.isEmpty(CMAPI.getInstance().baseInfo.netid)) {//非抢占vpn隧道
                    //先清空数据库对象
                    viewModel.loadFromDB(null)
                }
            }
            //清空数据
            it?.let {
                val mainENDeviceId = it.getMainENDeviceId()
                if (!TextUtils.isEmpty(mainENDeviceId)) {//存在主EN
                    initCircleHeader(it.netName, getString(R.string.no_summary), false)
                    viewModel.refresh(true)
                    loadCircleBrief(it.netId, mainENDeviceId)
                } else {
                    initCircleHeader(it.netName, getString(R.string.no_summary))
                    viewModel.startGetCircleBrief(null)
                }
            }
        })

        viewModel.circleBrief.observe(viewLifecycleOwner, Observer {
            var brief: BriefModel? = null
            if (it != null && it.size > 0) {
                brief = it.get(0)
            }
            viewModel.loadBrief(viewModel.startGetCircleBrief.value ?: "", brief,
                    tvContent = tvBrief,
                    ivImage = ivPortrait,
                    defalutImage = R.drawable.icon_defualt_circle,
                    ivBackgroud = ivBackgroud,
                    defalutBgImage = R.color.breif_bg_defualt_color,
                    For = BriefRepo.FOR_CIRCLE,
                    isLoadOneDeviceBrief = true)
        })
        viewModel.refreshNewestDynamicListResult.observe(requireActivity(), Observer {
            if (it.status == Status.SUCCESS) {
                if (it.data?.code == DynamicQueue.SUCCESS_CODE) {
                    viewModel.updateCircleStatus(NOMARL)
                    viewModel.showAddMenu(true)
                    viewModel.loadFromDB(viewModel.getNewestDynamicListSize())
                }
            }
        })
        viewModel.refreshDynamicResult.observe(requireActivity(), Observer {
            when (it.status) {
                Status.ERROR -> {//网络请求错误提示
                    mSwipeRefreshLayout.isRefreshing = false
                    viewModel.updateLoading(false)
                    //请求被取消,说明无法获取Auth，设备不支持动态系统
                    if (DynamicQueue.isDynamicDisplayed)//在当前界面才提示
                        ToastUtils.showToast(if (it.message == DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC) getString(R.string.en_server_cant_connected) else getString(R.string.network_not_available))
                }
                Status.SUCCESS -> {
                    val reponseSize = it.data?.data?.size ?: 0
                    if (it.data?.code == DynamicQueue.SUCCESS_CODE) {
                        viewModel.updateCircleStatus(NOMARL)
                        viewModel.showAddMenu(true)
                    }
                    if (it.data?.code == DynamicQueue.SUCCESS_CODE && (reponseSize < viewModel.PAGE_SIZE)) {
                        //没有更多数据了，关闭加载更多功能
                        viewModel.updateLoadToEnd(true)
                    } else {
                        viewModel.updateLoadToEnd(false)
                    }
                    if (!(it.data?.code == DynamicQueue.SUCCESS_CODE && reponseSize == 0)) {//有新数据或加载失败才加载DB
                        viewModel.loadFromDB()
                    }
                    mSwipeRefreshLayout.isRefreshing = false
                    viewModel.updateLoading(false)
                }
                Status.LOADING -> {
                    if (mSwipeRefreshLayout.isRefreshing) {
                        mSwipeRefreshLayout.isRefreshing = true
                        //开启可加载更多功能
                        viewModel.updateLoadToEnd(false)
                    } else if (mSwipeRefreshLayout.isLoading) {
                        viewModel.updateLoading(true)
                    }
                }
                Status.NONE -> {
                    mSwipeRefreshLayout.isRefreshing = false
                    viewModel.updateLoading(false)
                    viewModel.updateLoadToEnd(true)
                    activityViewModel.updateDynamicMessageCount(0)
                }
            }
        })
        viewModel.loginResult.observe(requireActivity(), Observer {
            if (it.status == Status.LOADING) {
                viewModel.updateLoading(true)
            } else if (it.status == Status.SUCCESS && it.data?.code == DynamicQueue.SUCCESS_CODE) {
                //显示发布按钮
                viewModel.showAddMenu(true)
                //获取列表
                viewModel.refresh(true)
            } else if (it.status == Status.SUCCESS || it.status == Status.ERROR) {//登录异常
                mSwipeRefreshLayout.isRefreshing = false
                viewModel.updateLoading(false)
                if (DynamicQueue.isDynamicDisplayed) {//在动态界面才提示
                    ToastUtils.showToast(R.string.en_server_cant_connected)
                }
            }

        })
        viewModel.dynamicList.observe(requireActivity(), Observer {
            if (BuildConfig.DEBUG) Timber.e("dynamicList is update size=" + it.size + "->" + DynamicDBHelper.TAG)
            if (viewModel.circleStatus.value == NOMARL) {//正常状态下才刷新
                adapter.updateItems()
            }
        })

        viewModel.toastText.observe(requireActivity(), Observer {
            ToastUtils.showToast(it)
        })
        viewModel.commentEvent.observe(requireActivity(), Observer {
            it?.let {//有值才弹出，避免虚拟与键盘被输入框触发事件冲突
                //评论事件时 取消loading
                viewModel.updateLoading(false)
                mCommentPopupWindow.showAtLocation(toolbar, Gravity.BOTTOM, 0, 0)
                mCommentPopupWindow.setText("")
                mCommentPopupWindow.setHint(it.hint)
            }
        })

        viewModel.commentPopupWindowLocationY.observe(requireActivity(), Observer {
            /**
             * 列表滑动到评论弹框对应项位置，且在评论输入框上方
             * 由虚拟键盘弹出触发
             */
            viewModel.commentEvent.value?.let {
                if (viewModel.isTouchCommentEventing.value == true) {
                    //项底部的 Y位置 在屏幕中的位置
                    //评论触发位于项底部，回复位于评论项底部
                    val commentItemLocactionY: Int = if (it.type == CommentEvent.REPLY_TYPE) it.screenLocationY else getItemBottomLocationY(it.position)
                    //相对输入框弹框位置 移动距离
                    val y = commentItemLocactionY - (viewModel.commentPopupWindowLocationY.value
                            ?: 0)
                    recyclerView.smoothScrollBy(0, y)
                    //重要，否则无法触发AppBarLayout的behavior联动
                    recyclerView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
                    //结束本次触发，避免虚拟与键盘被输入框触发事件冲突
                    viewModel.endTouchCommentEvent()
                }
            }
        })

        viewModel.deleteCommentResult.observe(requireActivity(), Observer {
            when (it.status) {
                Status.LOADING -> {
                    (requireActivity() as BaseActivity).showLoading()
                }
                Status.ERROR -> {
                    (requireActivity() as BaseActivity).dismissLoading()
                    ToastUtils.showToast(R.string.en_server_cant_connected)
                }
                Status.SUCCESS -> {
                    (requireActivity() as BaseActivity).dismissLoading()
                    if (it.data?.code == DynamicQueue.DELETED_CODE) {//动态已被删除
                        ToastUtils.showToast(R.string.the_dynamic_is_deleted)
                    } else if (it.data?.code != DynamicQueue.SUCCESS_CODE) {
                        ToastUtils.showToast(R.string.en_server_cant_connected)
                    }
                }
            }
        })

        viewModel.accessDynamicDetailId.observe(requireActivity(), Observer {
            val intent = Intent(requireContext(), DynamicDetailActivity::class.java)
                    .putExtra(DynamicDetailActivity.DYNAMIC_ID, it)
                    .putExtra(AppConstants.SP_FIELD_NETWORK, viewModel.mLastNetworkId)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, DynamicQueue.deviceId)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_IP, DynamicQueue.deviceIP)
            viewModel.accessDynamicDetailComment?.let {
                intent.putExtra(DynamicDetailActivity.SCROLL_COMMENT, it)
            }
            startActivityForResult(intent, DETAIL_REQUEST_CODE)

        })

        viewModel.aboutMessageResult.observe(requireActivity(), Observer {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    if (it.message != "domain is empty") {
                        handler.sendEmptyMessageDelayed(0, REFRESH_ABOUT_DELAY)
                    }
                }
                Status.SUCCESS -> {
                    if ((it.data?.momentid?.size ?: 0) > 0) {
                        //由不可见显示为可见，这里需使用mNestedScrollView的setOnScrollChangeListener中联合处理联动效果，否则列表在中间时，会显示错位
                        if (isRecyclerViewTop()) {
                            btnNewMessage.visibility = View.VISIBLE
                        }
                        //刷新消息动态
                        viewModel.refreshDynamics()
                    }
                    activityViewModel.updateDynamicMessageCount(it.data?.momentid?.size ?: 0)
                    val hasMessages = handler.hasMessages(0)
                    if (!hasMessages) {
                        handler.sendEmptyMessageDelayed(0, REFRESH_ABOUT_DELAY)
                    }

                }
            }
        })
        viewModel.clearRelatedMessageResult.observe(requireActivity(), Observer {

        })

        viewModel.isLoading.observe(requireActivity(), Observer {
            it?.let {
                mSwipeRefreshLayout.isLoading = it
                adapter.notifyLoadingAnim()
            }
        })
        viewModel.isLoadToEnd.observe(requireActivity(), Observer {
            it?.let {
                mSwipeRefreshLayout.isLoadEnable = !it
            }
        })

        /**
         * 每次onEstablished都会触发
         */
        activityViewModel.hasLoggedin.observe(viewLifecycleOwner, Observer {
            viewModel.hasLoggedin.value = it
            adapter.notifyDataSetChanged()
        })
    }

    /**
     * 获取指定项底部的Y坐标
     */
    private fun getItemBottomLocationY(position: Int): Int {
        val mLayoutManager: LinearLayoutManager = recyclerView.getLayoutManager() as LinearLayoutManager
        val itemView = mLayoutManager.findViewByPosition(position)
        val itemHeight = itemView?.measuredHeight ?: 0
        return (itemView?.getScreenLocationY() ?: 0) + itemHeight
    }

    override fun getTopView(): View {
        return toolbar
    }

    override fun getHomeTipsBar(): TipsBar? {
        return tipsBar
    }
}

fun View.getScreenLocationY(): Int {
    val screenLocation = IntArray(2)
    //getLocationOnScreen屏幕顶端开始,包括了通知栏的高度。
    getLocationOnScreen(screenLocation)
    return screenLocation[1]
}