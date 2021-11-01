package net.linkmate.app.ui.activity.circle.circleDetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_circle_fees.view.*
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.data.model.CircleManagerFees
import net.linkmate.app.view.FormRowLayout

/**
 * @author Raleigh.Luo
 * date：20/10/16 10
 * describe：
 */
class CircleSettingFeesAdapter(val context: Context, val viewModel: CircleSettingFeesViewModel) : RecyclerView.Adapter<CircleSettingFeesAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val NO_DATA_TYPE = -1
    private val DEFAULT_TYPE = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == NO_DATA_TYPE) {
            val layout = R.layout.layout_empty_view
            val view =
                    LayoutInflater.from(context).inflate(layout, null, false)
            view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            return ViewHolder(view)
        } else {
            val layout = R.layout.dialog_circle_fees
            val view =
                    LayoutInflater.from(context).inflate(layout, null, false)
            view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            return ViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (viewModel.fees.value?.size ?: 0 == 0) NO_DATA_TYPE else DEFAULT_TYPE
    }

    override fun getItemCount(): Int {
//        var total = 0
////        viewModel.fees.value?.forEach {
////            total += it.fees?.size ?: 0
////        }
////        return total
        val size = viewModel.fees.value?.size ?: 0
        return if (size == 0) 1 else size
    }

    private fun getSubPanel(title: String, content: String): FormRowLayout {
        val frl = FormRowLayout(context)
        frl.title.text = title
        frl.content.text = content
        return frl
    }

    //上一个费用类型
    private var mLastFeeType: String? = null

    //当前同类型的第一个项的位置
    private var mCurentTypeFirstItemPostion = 0

    //操作项
    private var operatePosition = 0
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            NO_DATA_TYPE -> {
                mCurentTypeFirstItemPostion = -1
                mLastFeeType = null
                with(holder.itemView) {
//                    txt_empty.setText("")
                }
            }
            DEFAULT_TYPE -> {
                if (position == 0) {//重置
                    mCurentTypeFirstItemPostion = 0
                    mLastFeeType = null
                }
                with(holder.itemView) {
                    viewItemHead.visibility = View.GONE
                    viewItemFoot.visibility = View.GONE
                    llCircleTypeForm.removeAllViews()
                    if (position == 0) {
                        viewItemHead.visibility = View.VISIBLE
                    }
                    if (position == itemCount - 1) {
                        viewItemFoot.visibility = View.VISIBLE
                    }
                    var fee: CircleManagerFees.Fee? = viewModel.fees.value?.get(position)
                    fee?.let {
                        if (mLastFeeType == it.feetype) {//相同类型
                            gTitle.visibility = View.GONE
                        } else {
                            viewItemHead.visibility = View.VISIBLE
                            mCurentTypeFirstItemPostion = position
                            gTitle.visibility = View.VISIBLE
                            tvTitle.setText(it.titleUpper)
                            mLastFeeType = it.feetype
                        }

                        tvCircleTypeName.text = it.title
                        llCircleTypeForm.addView(getSubPanel(context.getString(R.string.use_life), it.getDurationText()))
                        if (!it.isFilterFlow())
                            llCircleTypeForm.addView(getSubPanel(context.getString(R.string.required_points), it.getValueText()))
                        cbCircleTypeCheck.setOnCheckedChangeListener(null)
                        val isEnable: Boolean = it.enable ?: false
                        cbCircleTypeCheck.isChecked = it.enable ?: false
                        ivTypeChecked.visibility = if (isEnable) View.VISIBLE else View.INVISIBLE

                        cbCircleTypeCheck.setOnClickListener {
                            if (!Utils.isFastClick(it)) {
                                operatePosition = position
                                //驱动打开编辑页面
                                viewModel.currentOperateFee.value = fee
                            }
                        }
                        cbCircleTypeCheck.setBackgroundResource(
                                if (cbCircleTypeCheck.isChecked) R.drawable.bg_item_dev_stroke else R.drawable.bg_item_dev)
                    }
                }
            }

//            viewModel.fees.value?.let {
//                var lastFeesCount = 0
//                it.forEach {
//                    if (position >= lastFeesCount + (it.fees?.size ?: 0)) {
//                        lastFeesCount += (it.fees?.size ?: 0)
//                    } else {//
//                        if(position == operatePosition){//更新操作项
//                            operatePosition = -1
//                            operationEnable?.let {
//                                fee?.enable = operationEnable
//                                operationEnable = null
//                            }
//                            operationVaddValue?.let {
//                                fee?.vadd?.value = operationVaddValue
//                                operationVaddValue = null
//                            }
//                        }
//
//                        if (mLastFeeType == it.feetype) {//相同类型
//                            gTitle.visibility = View.GONE
//                        } else {
//                            viewItemHead.visibility = View.VISIBLE
//                            mCurentTypeFirstItemPostion = position
//                            gTitle.visibility = View.VISIBLE
//                            tvTitle.setText(it.title)
//                            mLastFeeType = it.feetype
//                        }
//                        val feesIndex = position - mCurentTypeFirstItemPostion
//                        if (it.fees?.size ?: 0 > feesIndex && feesIndex >= 0) {
//                            fee = it.fees?.get(feesIndex)
//                            fee?.feetype = it.feetype
//                        }
//                        fee?.let {
//                            tvCircleTypeName.text = it.title
//                            llCircleTypeForm.removeAllViews()
//                            llCircleTypeForm.addView(getSubPanel(context.getString(R.string.use_life), it.getDurationText()))
//                            llCircleTypeForm.addView(getSubPanel(context.getString(R.string.required_points), it.getValueText()))
//                            cbCircleTypeCheck.setOnCheckedChangeListener(null)
//                            val isEnable:Boolean = it.enable ?:false
//                            cbCircleTypeCheck.isChecked = it.enable ?: false
//                            ivTypeChecked.visibility = if (isEnable) View.VISIBLE else View.INVISIBLE
//                        }
//                        cbCircleTypeCheck.setOnClickListener {
//                            if (!Utils.isFastClick(it)) {
//                                operatePosition = position
//                                //驱动打开编辑页面
//                                viewModel.currentOperateFee.value = fee
//                            }
//                        }
//                        cbCircleTypeCheck.setBackgroundResource(
//                                if (cbCircleTypeCheck.isChecked) R.drawable.bg_item_dev_stroke else R.drawable.bg_item_dev)
//                        //跳出循环
//                        return
//                    }
//                }
//            }
        }
    }
}