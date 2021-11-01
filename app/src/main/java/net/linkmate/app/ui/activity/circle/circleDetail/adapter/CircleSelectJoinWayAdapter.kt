package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_circle_type.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.view.FormRowLayout

/**
 * 选择加入方式
 * @author Raleigh.Luo
 * date：20/10/12 10
 * describe：
 */
class CircleSelectJoinWayAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleSelectJoinWayViewModel)
    : DialogBaseAdapter<CircleSelectJoinWayViewModel>(context, fragmentViewModel) {
    //上层页面传递已选择的方式
    private var selectedFeeIdExtra: String? = null

    init {
        //
        if (context.requireActivity().intent.hasExtra(FunctionHelper.POSITION))
            selectedFeeIdExtra = context.requireActivity().intent.getStringExtra(FunctionHelper.POSITION)
        //设置头部显示圈子名称
        fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname,
                bottomIsEnable = false)
        initObserver()
        //请求加入方式
        fragmentViewModel.startRequestRemoteSource()
    }

    private fun initObserver() {
        fragmentViewModel.joinWays.observe(context, Observer {
            it?.let {
                notifyDataSetChanged()
            }
        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_circle_type, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fragmentViewModel.joinWays.value?.size ?: 0
    }


    private fun getSubPanel(title: String, content: String): FormRowLayout {
        val frl = FormRowLayout(context.requireContext())
        frl.title.text = title
        frl.content.text = content
        return frl
    }

    //记录上个被选择的位置
    private var mLastCheckedPosition: Int = -1
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            setPaddingRelative(paddingLeft, if (position == 0) paddingLeft else 0, paddingLeft, paddingLeft)
            fragmentViewModel.joinWays.value?.get(position)?.let {
                tvCircleTypeName.text = it.title
                llCircleTypeForm.removeAllViews()
                llCircleTypeForm.addView(getSubPanel(context.getString(R.string.use_life), it.getDurationText()))
                llCircleTypeForm.addView(getSubPanel(context.getString(R.string.required_points), it.getValueText()))

                cbCircleTypeCheck.setOnCheckedChangeListener(null)
                var isChecked = mLastCheckedPosition == position
                if (mLastCheckedPosition == -1 && selectedFeeIdExtra != null) {
                    //上层页面传递已选择中的项，仅首次
                    it.feeid == selectedFeeIdExtra
                    isChecked = true
                    mLastCheckedPosition = position
                    fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                    cbCircleTypeCheck.setBackgroundResource(R.drawable.bg_item_dev_stroke)
                    ivTypeChecked.visibility = View.VISIBLE
                }
                cbCircleTypeCheck.isChecked = isChecked
                cbCircleTypeCheck.setBackgroundResource(
                        if (cbCircleTypeCheck.isChecked) R.drawable.bg_item_dev_stroke else R.drawable.bg_item_dev)
                ivTypeChecked.visibility = if (cbCircleTypeCheck.isChecked) View.VISIBLE else View.GONE
                cbCircleTypeCheck.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        //底部按钮可用
                        if (fragmentViewModel.viewStatusParams.value?.bottomIsEnable ?: false == false) {
                            fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                        }

                        val tempLastPositon = mLastCheckedPosition
                        mLastCheckedPosition = position
                        //取消上一个勾选
                        notifyItemChanged(tempLastPositon)
                    } else {
                        compoundButton.isChecked = true
                    }
                    cbCircleTypeCheck.setBackgroundResource(
                            if (cbCircleTypeCheck.isChecked) R.drawable.bg_item_dev_stroke else R.drawable.bg_item_dev)
                    ivTypeChecked.visibility = if (cbCircleTypeCheck.isChecked) View.VISIBLE else View.GONE
                }
            }


        }

    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.btnBottomConfirm) {
            if(mLastCheckedPosition>=0){
                val position: Int = mLastCheckedPosition
                context.requireActivity().setResult(FragmentActivity.RESULT_OK, Intent().putExtra(FunctionHelper.EXTRA_ENTITY, fragmentViewModel.joinWays.value?.get(position)))
                viewModel.toFinishActivity()
            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }
}