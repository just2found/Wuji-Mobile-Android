package net.linkmate.app.ui.simplestyle.circledetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rxjava.rxlife.RxLife
import kotlinx.android.synthetic.main.item_home_simplestyle.view.*
import kotlinx.android.synthetic.main.layout_empty_view.view.*
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

/** 我的绑定设备
 * @author Raleigh.Luo
 * date：20/11/19 10
 * describe：
 */
class CircleDeviceAdapter(val context: Context, val viewModel: CircleDeviceViewModel, val mDeviceViewModel: DeviceViewModel) : RecyclerView.Adapter<ViewHolder>() {
    private val LOADING_TYPE = -3 //正在加载数据
    private val NO_DATA_TYPE = -1
    private val DEFAULT_TYPE = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            NO_DATA_TYPE -> {
                val layout = R.layout.layout_empty_view
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                with(view) {
                    textView.setText("")
                    layout_empty.setBackgroundResource(R.color.white)
                }
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
                    tvTitle.setBackgroundResource(R.color.white)
//                    //size = 16*16dip
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
        return when {
            viewModel.devices.value == null -> {
                LOADING_TYPE
            }
            getDataCount() == 0 -> {
                NO_DATA_TYPE
            }
            else -> {
                DEFAULT_TYPE
            }
        }
        return NO_DATA_TYPE
    }

    fun update() {
        if (getDataCount() == 0) {
            notifyDataSetChanged()
        } else {//局部刷新
            notifyItemRangeChanged(0, itemCount, arrayListOf(1))
        }
    }

    /**
     * 真实数据数量
     */
    fun getDataCount(): Int {
        return viewModel.devices.value?.size ?: 0
    }

    override fun getItemCount(): Int {
        return if (getDataCount() == 0) 1 else getDataCount()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == DEFAULT_TYPE) {
            with(holder.itemView) {
                val device = viewModel.devices.value?.get(position)
                device?.let {
                    //root设备id作为Tag，根据不同设备id,清除所有Tag
                    if (this.getTag() != it.id) {
                        //清空所有控件Tag
                        tvName.setTag(null)
                        tvContent.setTag(null)
                        ivImage.setTag(null)
                        this.setTag(it.id)
                    }

                    if (ivImage.getTag() == null) ivImage.setImageResource(DeviceBean.getIconSimple(it))

                    val tvTitleVisibility = if (position != 0 && isFirstItem(position, it.type)) View.VISIBLE else View.GONE
                    if (tvTitle.visibility != tvTitleVisibility) tvTitle.visibility = tvTitleVisibility
                    vTitleTitleLine.visibility = tvTitleVisibility
                    vTitleBottomLine.visibility = tvTitleVisibility

                    //类型最后一项，隐藏底部线条
                    val vBottomLineVisibility = if (isLastItem(position, it.type)) View.INVISIBLE else View.VISIBLE
                    if (vBottomLine.visibility != vBottomLineVisibility) vBottomLine.visibility = vBottomLineVisibility

                    if (it.isNas && SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
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
                    /**--DLT状态 直连------------------------------------------------***/
                    var visibility = if (it.isOnline && it.getDlt() != null && it.getDlt().clazz > 0) View.VISIBLE else View.GONE
                    if (visibility == View.VISIBLE) {//dlt直连
                        vStatus.setBackgroundResource(R.drawable.icon_green_dot_18dp)
                    } else if (it.isDevDisable || !it.isOnline) {//不可见设备，且非离线（networkid不为空）
                        visibility = View.VISIBLE
                        vStatus.setBackgroundResource(R.drawable.icon_red)
                    }
                    if (vStatus.visibility != visibility) vStatus.visibility = visibility




                    when (it.type) {
                        0 -> {//边缘节点
                            if (tvTitle.visibility == View.VISIBLE) tvTitle.setText(R.string.edge_node)
                            if (it.isDevDisable) {//设备异常
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
                            } else if (tvContent.getTag() == null) {
                                tvContent.setText(R.string.no_summary)
                                tvContent.setTag(tvContent.text.toString())
                            }
                            if (device.isNas) {
                                val brief = viewModel.getBrief(it.id)
                                viewModel.loadBrief(it.id, brief, ivImage, if (it.isDevDisable) null else tvContent, defalutImage = DeviceBean.getIconSimple(it))
                            }
                            //启用节点
                        }
                        1 -> {//设备
                            if (tvTitle.visibility == View.VISIBLE) tvTitle.setText(R.string.devices)
                            if (it.isDevDisable) {//设备异常
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
                            } else if (tvContent.getTag() == null) {
                                tvContent.setText(R.string.no_summary)
                                tvContent.setTag(tvContent.text.toString())
                            }
                            if (device.isNas) {
                                val brief = viewModel.getBrief(it.id)
                                viewModel.loadBrief(it.id, brief, ivImage, if (it.isDevDisable) null else tvContent, defalutImage = DeviceBean.getIconSimple(it))
                            }
                        }
                        2 -> {//终端
                            if (tvTitle.visibility == View.VISIBLE) tvTitle.setText(R.string.clients)
                            if (tvContent.getTag() != it.ownerName) {
                                tvContent.setText(it.ownerName)
                                tvContent.setTag(it.ownerName)
                            }
                            if (vStatus.visibility == View.VISIBLE) vStatus.visibility = View.GONE
                        }
                    }
                    root.setOnClickListener {//数据驱动事件
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

    /**是否是类型 首项
     * @param lastType 上一个类型
     */
    private fun isFirstItem(position: Int, currentType: Int): Boolean {
        //第一项
        if (position == 0) return true

        var isFirstItem = false
        //上一个位置
        val lastIndex = position - 1
        if (lastIndex >= 0) {
            viewModel.devices.value?.get(lastIndex)?.let {
                isFirstItem = it.type != currentType
            }
        }
        return isFirstItem
    }

    /**是否是类型 最后一项
     * @param lastType 上一个类型
     */
    private fun isLastItem(position: Int, currentType: Int): Boolean {
        //第一项
        if (position == itemCount - 1) return true

        var isLastItem = false
        //上一个位置
        val nextIndex = position + 1
        if (nextIndex < itemCount) {
            viewModel.devices.value?.get(nextIndex)?.let {
                isLastItem = it.type != currentType
            }
        }
        return isLastItem
    }

}