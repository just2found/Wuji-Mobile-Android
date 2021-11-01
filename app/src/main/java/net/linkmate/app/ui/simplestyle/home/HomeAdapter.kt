package net.linkmate.app.ui.simplestyle.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rxjava.rxlife.RxLife
import kotlinx.android.synthetic.main.fragment_pls_login.view.*
import kotlinx.android.synthetic.main.item_home_simplestyle.view.*
import kotlinx.android.synthetic.main.layout_loading_view.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.view.ViewHolder
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.ErrorCode
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.DeviceViewModel

/** 首页适配器
 * @author Raleigh.Luo
 * date：20/11/18 10
 * describe：
 */
class HomeAdapter(val context: Context, val viewModel: HomeViewModel, val deviceViewModel: DeviceViewModel) : RecyclerView.Adapter<ViewHolder>() {
    //未登录
    private val LOADING_TYPE = -3 //正在加载数据
    private val NO_LOGIN_TYPE = -2
    private val NO_DATA_TYPE = -1
    private val DEFAULT_TYPE = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            NO_LOGIN_TYPE -> {
                val layout = R.layout.fragment_pls_login
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                with(view) {
                    pls_login_btn_login.setOnClickListener {
                        context.startActivity(android.content.Intent(context, net.linkmate.app.ui.activity.LoginActivity::class.java))
                    }
                }
                return ViewHolder(view)
            }
            NO_DATA_TYPE -> {
                val view =
                        LayoutInflater.from(context).inflate(R.layout.layout_empty_view, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                return ViewHolder(view)
            }
            LOADING_TYPE -> {
                val view =
                        LayoutInflater.from(context).inflate(R.layout.layout_loading_view, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                with(view) {
                    Glide.with(context).asGif().load(R.drawable.loading).transition(DrawableTransitionOptions.withCrossFade()).into(ivLoadingImage)
                }
                return ViewHolder(view)
            }
            else -> {
                val view =
                        LayoutInflater.from(context).inflate(R.layout.item_home_simplestyle, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                with(view) {
                    ivRightImage.visibility = View.GONE
                    //size = 16*16dip
//                    val padding = context.resources.getDimensionPixelSize(R.dimen.common_16)
//                    ivRightImage.setPaddingRelative(padding, 0, padding, 0)
//                    root.background = context.getDrawable(R.drawable.item_click_white_ripple)
//                    ivRightImage.setImageResource(R.drawable.icon_detail)
                }
                return ViewHolder(view)

            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        when {
            viewModel.checkLoggedin() == false -> {//未登录
                return NO_LOGIN_TYPE
            }
            viewModel.refreshDevices.value == null -> {
                return LOADING_TYPE
            }
            getDataCount() == 0 -> {//无数据
                return NO_DATA_TYPE
            }
            else -> {
                return DEFAULT_TYPE
            }
        }
    }

    /**
     * 真实数据数量
     */
    private fun getDataCount(): Int {
        //未请求到数据前不显示
        return getDevicesSize()
    }

    fun update() {
        if (getDataCount() == 0) {
            notifyDataSetChanged()
        } else {//局部刷新
            notifyItemRangeChanged(0, itemCount, arrayListOf(1))
        }
    }

    override fun getItemCount(): Int {
        when {
            viewModel.checkLoggedin() == false -> {//未登录
                return 1
            }
            viewModel.refreshDevices.value == null -> {//未请求到数据前,加载动画
                return 1
            }
            getDataCount() == 0 -> {//无数据
                return 1
            }
            else -> {
                return getDataCount()
            }
        }
    }

    /**
     *
     */
    private fun getDevicesSize(): Int {
        return viewModel.getDevicesSize()
    }

    /**
     * 是否是我的设备第一项
     */
    private fun isOwnerFirstItem(position: Int): Boolean {
        if (position >= getDevicesSize()) return false
        val isOwner = viewModel.devices?.getOrNull(position)?.isOwner ?: false
        return position == 0 && isOwner
    }

    /**
     * 是否是好友的设备第一项
     */
    private fun isOtherFirstItem(position: Int): Boolean {
        if (position >= getDevicesSize()) return false

        val isOwner = viewModel.devices?.getOrNull(position)?.isOwner ?: false
        return !isOwner && (position == 0 || viewModel.devices?.getOrNull(position - 1)?.isOwner ?: false)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == DEFAULT_TYPE) {
            with(holder.itemView) {
                //最后一项/设备最后一项，隐藏底部线条
                val vBottomLineVisibility = if (position + 1 == itemCount || (position + 1 != itemCount && isOtherFirstItem(position + 1))) View.INVISIBLE else View.VISIBLE
                if (vBottomLineVisibility != vBottomLine.visibility) vBottomLine.visibility = vBottomLineVisibility
                val index = position
                val device = viewModel.devices.getOrNull(index)
                device?.let {
                    //root设备id作为Tag，根据不同设备id,清除所有Tag

                    if (this.getTag() != it.id) {
                        //清空所有控件Tag
                        tvName.setTag(null)
                        tvContent.setTag(null)
                        ivImage.setTag(null)
                        this.setTag(it.id)
                    }
                    if (tvContent.getTag() == null) {
                        tvContent.setText(R.string.no_summary)
                        tvContent.setTag(tvContent.text.toString())
                    }
                    if (ivImage.getTag() == null) ivImage.setImageResource(DeviceBean.getIconSimple(it))
                    //待同意的统一显示为设备名
                    //是否等待同意，需使用hardData
                    val isPendingAccept = it.isPendingAccept
                    if (it.isNas && !isPendingAccept && SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                        val deviceModel = SessionManager.getInstance()
                                .getDeviceModel(it.id)
                        if (deviceModel != null) {
                            if (tvName.getTag() == null) tvName.setText(deviceModel.devName)
                            deviceModel.devNameFromDB
                                    .`as`(RxLife.`as`(holder.itemView))
                                    .subscribe({ s: String? ->
                                        if (tvName.getTag() != s) {
                                            tvName.setText(s)
                                        }
                                        tvName.setTag(tvName.text.toString())
                                    }) { throwable: Throwable? -> }
                        } else {
                            val text = it.name.trim { it <= ' ' }
                            if (tvName.getTag() != text) {
                                tvName.setText(text)
                                tvName.setTag(text)
                            }
                        }
                    } else {
                        val text = it.name.trim { it <= ' ' }
                        if (tvName.getTag() != text) {
                            tvName.setText(text)
                            tvName.setTag(text)
                        }
                    }


                    var tvTitleVisibility = View.GONE
                    if (position != 0) {//第一项不显示头部
                        if (isOwnerFirstItem(position)) {
                            tvTitleVisibility = View.VISIBLE
                            tvTitle.setText(R.string.mine)
                        } else if (isOtherFirstItem(position)) {
                            tvTitleVisibility = View.VISIBLE
                            tvTitle.setText(R.string.friend_device)
                        }
                    }
                    if (tvTitle.visibility != tvTitleVisibility) tvTitle.visibility = tvTitleVisibility
                    if (it.isNas) {
                        val brief = viewModel.getBrief(it.id)
                        viewModel.loadBrief(it.id, brief, ivImage, if (it.isDevDisable || !it.isOnline || isPendingAccept) null else tvContent, defalutImage = DeviceBean.getIconSimple(it))
                    } else {
                        ivImage.setImageResource(DeviceBean.getIconSimple(it))
                        ivImage.setTag(DeviceBean.getIconSimple(it))
                        ivImage.setOnClickListener(null)
                    }
                    if (isPendingAccept) {//等待同意
                        tvContent.setTag(null)
                        tvContent.setText(R.string.wait_for_consent)
                    } else if (!it.isOnline) {//不在此圈子
                        tvContent.setTag(null)
                        tvContent.setText(if (it.hardData?.isRealOnline
                                        ?: false) R.string.in_other_networks else R.string.offline)
                    } else if (it.isDevDisable) {//设备异常
                        tvContent.setTag(null)
                        tvContent.setText(ErrorCode.ec2String(it.devDisableReason))
                        //设备无法联通，处于不可用状态：当前网络下，且非免费旧圈子，加入状态异常
                        if (it.enServer != null) {
                            if (it.enServer!!.joinStatus == -1) {
                                tvContent.setText(R.string.not_subscribed)
                            } else if (it.enServer!!.flowStatus == 1) {
                                tvContent.setText(R.string.flow_is_expired)
                            } else if (it.enServer!!.flowStatus != 0) {
                                tvContent.setText(R.string.not_purchase_circle_flow)
                            }
                        }
                    }
                    //是否真实在线
                    val isRealOnline = it.hardData?.isRealOnline() ?: false

                    /**--等待同意设备mnglevel == 3／状态异常的设备 灰白处理------------------------------------------------***/
                    val alpha = if (isPendingAccept || ((it.isDevDisable || !it.isOnline) && !isRealOnline)) 0.3f else 1f
                    if (alpha != ivImage.alpha) {
                        ivImage.alpha = alpha
                        llMiddle.alpha = if (alpha == 1f) 1f else 0.4f
                    }
                    var vStatusVisibility = View.GONE

                    if (alpha == 1f) {//变灰色的状态，不显示状态
                        /**-- it.type != 2 显示DLT状态 直连------------------------------------------------***/
                        vStatusVisibility = if (it.isOnline && it.type != 2 && it.getDlt() != null && it.getDlt().clazz > 0) View.VISIBLE else View.GONE
                        if (vStatusVisibility == View.VISIBLE) {//dlt直连
                            vStatus.setBackgroundResource(R.drawable.icon_green_dot_18dp)
                        } else if (it.isDevDisable || !it.isOnline) {//不可见设备，且非离线（networkid不为空）
                            vStatusVisibility = if (isRealOnline) View.VISIBLE else View.GONE
                            if (vStatusVisibility == View.VISIBLE)
                                vStatus.setBackgroundResource(R.drawable.icon_red)
                        }
                    }
                    if (vStatus.visibility != vStatusVisibility) {
                        vStatus.visibility = vStatusVisibility
                    }

                    /**--节点处理------------------------------------------------***/
                    if (it.getSelectable() || it.isVNode()) {
                        //是否启用节点
                        val isSelect = (it.getSelectable() || it.isVNode()) &&
                                CMAPI.getInstance().baseInfo.hadSelectedSn(it.getId())
                        tvContent.setStartDrawable(context.getDrawable(if (isSelect) R.drawable.icon_net_selected else R.drawable.icon_net_selectable))
                    } else {
                        tvContent.setStartDrawable(null)
                    }

//                    ivRightImage.setOnClickListener {
//                        viewModel.currentLongOptDevice.value = device
//                    }
                    root.setOnClickListener {//项点击事件，数据更新驱动事件
                        if (!Utils.isFastClick(it)) {
                            viewModel.currentOptDevice.value = device
                        }
                    }
                    root.setOnLongClickListener {//数据驱动事件
                        viewModel.currentLongOptDevice.value = device
                        true
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }


}