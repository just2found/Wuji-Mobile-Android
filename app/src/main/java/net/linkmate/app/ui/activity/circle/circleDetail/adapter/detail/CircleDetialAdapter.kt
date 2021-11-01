package net.linkmate.app.ui.activity.circle.circleDetail.adapter.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_device_item_status.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.util.ClipboardUtils

/**
 * @author Raleigh.Luo
 * date：20/8/14 14
 * describe：圈子详情
 */
class CircleDetialAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleFragmentViewModel)
    : DialogBaseAdapter<CircleFragmentViewModel>(context,fragmentViewModel) {
    private val sources: ArrayList<StatusMenu> = ArrayList()

    //圈子名
    private var CIRCLE_NAME = -1

    //所有者
    private var CIRCLE_OWNER = -1

    //圈子ID
    private var CIRCLE_ID = -1

    //流量单价
    private var CIRCLE_FLOW_UNIT_PRICE = -1

    //简介
    private var CIRCLE_BRIEF = -1

    init {
        //设置头部显示圈子名称
        fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname)
        getItemSources()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_status, null, false)
        view.layoutParams =
                ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            tvStatusTitle.text = sources[position].title
            tvStatusContent.text = sources[position].content
            vRedDot.visibility = View.GONE
            if (sources[position].isEditable) {
                ivStatusEdit.visibility = View.VISIBLE
                ivStatusEdit.setTag(sources[position].function)
                ivStatusEdit.setOnClickListener {
                    //是否已经被拦截处理
                    val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                    //没有拦截，则可以内部处理
                    if (!isInterceptor) internalItemClick(it, position)
                }

            } else {
                ivStatusEdit.visibility = View.GONE
            }

            setOnClickListener {
                //是否已经被拦截处理
                val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                //没有拦截，则可以内部处理
                if (!isInterceptor) internalItemClick(it, position)
            }

        }
    }
    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.ivStatusEdit) {
//            when (view.getTag()) {//编辑
//                CIRCLE_NAME -> {//设备名称
//                    editDialog?.update(context.getString(R.string.rename),context.getString(R.string.rename),
//                            viewModel.circleDetail.value?.networkname)
//                    if(editDialog?.dialog?.isShowing != true){
//                        editDialog?.show(context.supportFragmentManager,"name")
//                    }
//                }
//                CIRCLE_BRIEF -> {//设备简介
//                    editDialog?.update(context.getString(R.string.summary),context.getString(R.string.summary),"")
//                    if(editDialog?.dialog?.isShowing != true){
//                        editDialog?.show(context.supportFragmentManager,"breif")
//                    }
//                }
//                CIRCLE_FLOW_UNIT_PRICE -> {//流量单价
//                    editDialog?.update(context.getString(R.string.flow_unit_price),context.getString(R.string.flow_unit_price),"")
//                    if(editDialog?.dialog?.isShowing != true){
//                        editDialog?.show(context.supportFragmentManager,"price")
//                    }
//                }
//            }
        } else {
            if(position>=0){
                clipString(sources[position].content ?: "")
            }

        }
    }

    private fun clipString(content: String) {
        if (context != null) {
            ClipboardUtils.copyToClipboard(context.requireContext(), content)
            ToastUtils.showToast(context.getString(R.string.Copied).toString() + content)
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }
    private fun getItemSources() {
        var index = 0
        sources.clear()
        CIRCLE_NAME = index++
        sources.add(getMenuByFunction(CIRCLE_NAME))
        CIRCLE_OWNER = index++
        sources.add(getMenuByFunction(CIRCLE_OWNER))
        CIRCLE_ID = index++
        sources.add(getMenuByFunction(CIRCLE_ID))
//        CIRCLE_FLOW_UNIT_PRICE = index++
//        sources.add(getMenuByFunction(CIRCLE_FLOW_UNIT_PRICE))
//        CIRCLE_BRIEF = index++
//        sources.add(getMenuByFunction(CIRCLE_BRIEF))
    }
    private fun getMenuByFunction(function:Int,isEditable: Boolean=false): StatusMenu {
        val menu = StatusMenu(function = function, isEditable = isEditable)
        viewModel.circleDetail.value?.let {
            when (function) {
                CIRCLE_NAME -> {
                    menu.title = context.getString(R.string.circle_name)
                    menu.content = it.networkname
                }
                CIRCLE_OWNER -> {
                    menu.title = context.getString(R.string.owner)
                    menu.content = it.getFullName()
                }
                CIRCLE_ID -> {
                    menu.title = context.getString(R.string.circle_id)
                    menu.content = it.networkid
                }
                CIRCLE_FLOW_UNIT_PRICE -> {
                    menu.title = context.getString(R.string.flow_unit_price)
                }
                CIRCLE_BRIEF -> {
                    menu.title = context.getString(R.string.summary)
                }
            }
        }

        return menu
    }

    class StatusMenu(var title: String? = null, var content: String? = null, var isEditable: Boolean = false
                     , var function: Int = 0)
}