package net.linkmate.app.ui.activity.circle.circleDetail.adapter.fee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_circle_fees.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.view.FormRowLayout
import java.util.*

/**
 * @author Raleigh.Luo
 * date：20/10/15 19
 * describe：
 */
class CircleOwnFeeRecordAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleOwnFeeRecordViewModel)
    : DialogBaseAdapter<CircleOwnFeeRecordViewModel>(context, fragmentViewModel) {
    init {
        initObserver()
        fragmentViewModel.startGetRecords()
    }

    private fun initObserver() {
        fragmentViewModel.records.observe(context, Observer {
            notifyDataSetChanged()
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_circle_fees, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fragmentViewModel.records.value?.size ?: 0
    }

    private fun getSubPanel(title: String, content: String): FormRowLayout {
        val frl = FormRowLayout(context.requireContext())
        frl.title.text = title
        frl.content.text = content
        return frl
    }

    //上一个费用类型
    private var mLastFeeType: String? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {//重置
            mLastFeeType = null
        }
        with(holder.itemView) {
            viewItemHead.visibility = View.GONE
            viewItemFoot.visibility = View.GONE
            if (position == 0) {
                viewItemHead.visibility = View.VISIBLE
            }
            if (position == itemCount - 1) {
                viewItemFoot.visibility = View.VISIBLE
            }
            llCircleTypeForm.removeAllViews()
            val record = fragmentViewModel.records.value?.get(position)
            record?.let {
                if (mLastFeeType == it.feetype) {//相同类型
                    gTitle.visibility = View.GONE
                } else {
                    viewItemHead.visibility = View.VISIBLE
                    gTitle.visibility = View.VISIBLE
                    tvTitle.setText(it.feetypename)
                    mLastFeeType = it.feetype
                }

                //生效时间
                val effectdate = it.effectdate ?: 0L
                if (effectdate != 0L) {
                    llCircleTypeForm.addView(getSubPanel(context.getString(R.string.effective_time), "" + MyConstants.sdf.format(Date((it.effectdate
                            ?: 0L)))))
                } else {//不限
                    llCircleTypeForm.addView(getSubPanel(context.getString(R.string.effective_time), context.getString(R.string.no_limit)))
                }
                //失效时间
                it.expiredate?.let {
                    if (it != 0L) {//显示失效时间
                        llCircleTypeForm.addView(getSubPanel(context.getString(R.string.expire_time), "" + MyConstants.sdf.format(Date(it))))
                    } else {//不限
                        llCircleTypeForm.addView(getSubPanel(context.getString(R.string.expire_time), context.getString(R.string.no_limit)))
                    }
                }

                if (!it.isFilterFlow()) {
                     //消费金额
                    llCircleTypeForm.addView(getSubPanel(context.getString(R.string.consumption_amount), "" + it.getMbpointValue()))
                }

                tvCircleTypeName.text = it.resname
            }


            cbCircleTypeCheck.setOnCheckedChangeListener { compoundButton, b ->
                cbCircleTypeCheck.isChecked = false
            }
        }
    }

    override fun internalItemClick(view: View, position: Int) {
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

}