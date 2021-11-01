package net.linkmate.app.ui.simplestyle.circle

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_pls_login.view.*
import kotlinx.android.synthetic.main.item_home_simplestyle.view.*
import net.linkmate.app.R
import net.linkmate.app.view.ViewHolder
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.utils.Utils

/**
 * @author Raleigh.Luo
 * date：20/11/25 16
 * describe：
 */
class CircleAdapter(val context: Context, val viewModel: CircleViewModel) : RecyclerView.Adapter<ViewHolder>() {
    //未登录
    private val NO_LOGIN_TYPE = -1
    private val DEFAULT_TYPE = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == NO_LOGIN_TYPE) {
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
        } else {
            val view =
                    LayoutInflater.from(context).inflate(R.layout.item_home_simplestyle, null, false)
            view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            with(view) {
                tvTitle.visibility = View.GONE
            }
            return ViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 未登录 显示未登录UI
        return if (!viewModel.checkLoggedin()) NO_LOGIN_TYPE else DEFAULT_TYPE
    }

    /**
     * 真实数据数量
     */
    private fun getDataCount(): Int {
        return viewModel.getCirclesSize()
    }

    override fun getItemCount(): Int {
        // 未登录 显示一项
        return if (!viewModel.checkLoggedin()) 1 else getDataCount()
    }

    /**
     * 是否是圈子首项
     */
    private fun isCircleFirstItem(position: Int): Boolean {
        return viewModel.getCirclesSize() > 0 && position == 0

    }

    fun update() {
        if (getDataCount() == 0) {
            notifyDataSetChanged()
        } else {//局部刷新
            notifyItemRangeChanged(0, itemCount, arrayListOf(1))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == DEFAULT_TYPE) {
            with(holder.itemView) {
//                    if (isCircleFirstItem(position)) {
//                        tvTitle.visibility = View.VISIBLE
//                        tvTitle.setText(R.string.my_circle)
//                    }
                //最后一项/设备最后一项，隐藏底部线条
                val vBottomLineVisibility = if (position + 1 == itemCount) View.INVISIBLE else View.VISIBLE
                if (vBottomLineVisibility != vBottomLine.visibility) vBottomLine.visibility = vBottomLineVisibility

                val index = position
                val circle = viewModel.circles.value?.get(index)
                if (index == 0) {//表示当前选中网络
                    val padding = context.resources.getDimensionPixelSize(R.dimen.common_12)
                    ivRightImage.setPaddingRelative(padding, 0, padding, 0)
                    ivRightImage.setImageResource(R.drawable.icon_circle_selected)
                } else {
                    val padding = context.resources.getDimensionPixelSize(R.dimen.common_12)
                    ivRightImage.setPaddingRelative(padding, 0, padding, 0)
                    ivRightImage.setImageResource(R.drawable.icon_circle_right)
                }

                circle?.let {
                    if (getTag() != it.netId) {
                        tvContent.setTag(null)
                        ivImage.setTag(null)
                        tvName.setTag(null)
                        setTag(it.netId)
                    }
                    if (tvContent.getTag() == null) tvContent.setText(context.getString(R.string.no_summary))
                    if (ivImage.getTag() == null) ivImage.setImageResource(R.drawable.icon_defualt_circle)
                    if (tvName.getTag() != it.netName) {
                        tvName.setText(it.netName)
                        tvName.setTag(it.netName)
                    }

                    val mainServerDeviceId = it.mainENDeviceId

                    val brief = if (TextUtils.isEmpty(mainServerDeviceId)) null else viewModel.getBrief(mainServerDeviceId!!)
                    viewModel.loadBrief(mainServerDeviceId
                            ?: "", brief, ivImage, if (it.userStatus == 0) tvContent else null, defalutImage = R.drawable.icon_defualt_circle, For = BriefRepo.FOR_CIRCLE)

                    //等待同意或状态异常的圈子 灰白处理
                    val alpha = if (it.userStatus != 0 || (it.isCharge == true && it.isDevSepCharge == false &&
                                    (it.flowStatus == 1 || it.flowStatus == -1))) 0.3f else 1f
                    if (alpha != ivImage.alpha) {
                        ivImage.alpha = alpha
                        llMiddle.alpha = if (alpha == 1f) 1f else 0.4f
                    }
                    if (it.userStatus == 1) {//等待同意
                        tvContent.setTag(null)
                        tvContent.setText(R.string.wait_for_consent)
                    }
                    root.setOnClickListener {//项点击事件，数据更新驱动事件
                        if (!Utils.isFastClick(it)) {
                            viewModel.currentOptNetwork.value = circle
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

}
