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

/**选择圈子类型
 * @author Raleigh.Luo
 * date：20/8/14 13
 * describe：
 */
class CircleSelectTypeAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleSelectTypeViewModel)
    : DialogBaseAdapter<CircleSelectTypeViewModel>(context, fragmentViewModel) {

    //上一层传过来已选中的Id
    private var defaultSelectedModelId: String? = null

    // 初始化数据
    init {
        initObserver()
        fragmentViewModel.startRequestRemoteSource()
        fragmentViewModel.updateViewStatusParams(bottomIsEnable = false)
        //获取上一层传过来已选中的Id
        if (context.requireActivity().intent.hasExtra(FunctionHelper.EXTRA))
            defaultSelectedModelId = context.requireActivity().intent.getStringExtra(FunctionHelper.EXTRA)
    }

    private fun initObserver() {
        fragmentViewModel.circleTypes.observe(context, Observer {
            notifyDataSetChanged()
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
        return fragmentViewModel.circleTypes.value?.size ?: 0
    }

    //记录上个被选择的位置
    private var mLastCheckedPosition: Int = -1
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            llCircleTypeForm.removeAllViews()
            cbCircleTypeCheck.setOnCheckedChangeListener(null)
            val type = fragmentViewModel.circleTypes.value?.get(position)
            type?.modelprops?.network_scale?.forEach {
                val frl = FormRowLayout(context)
                frl.title.text = it.title
                frl.content.text = it.value.toString()
                llCircleTypeForm.addView(frl)
            }
            type?.modelprops?.network_fee?.forEach {
                val frl = FormRowLayout(context)
                frl.title.text = it.title
                frl.content.text = it.value.toString()
                it.key?.let { key ->
                    if (key == "create_fee") {
                        frl.content.text = it.value.toString()
                    }
                }

                llCircleTypeForm.addView(frl)
            }
            tvCircleTypeName.text = type?.modelname

            viewItemHead.visibility = View.GONE
            viewItemFoot.visibility = View.GONE
            if (position == 0) {
                viewItemHead.visibility = View.VISIBLE
            }
            if (position == itemCount - 1) {
                viewItemFoot.visibility = View.VISIBLE
            }
            //默认勾选选项
            val isChecked = defaultSelectedModelId == type?.modelid
            if (mLastCheckedPosition == -1) {
                cbCircleTypeCheck.isChecked = isChecked
                if (isChecked){
                    fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                    mLastCheckedPosition = position
                }
            } else {
                cbCircleTypeCheck.isChecked = mLastCheckedPosition == position
            }

            refreshCheckBg(this, cbCircleTypeCheck.isChecked)
            cbCircleTypeCheck.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {//选择了其他网络
                    if (fragmentViewModel.viewStatusParams.value?.bottomIsEnable != true) {
                        fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                    }
                    val tempLastPositon = mLastCheckedPosition
                    mLastCheckedPosition = position
                    //取消上一个勾选
                    notifyItemChanged(tempLastPositon)
                } else {
                    //不能取消自己
                    cbCircleTypeCheck.isChecked = true
                }
                refreshCheckBg(this, cbCircleTypeCheck.isChecked)
            }
        }
    }

    private fun refreshCheckBg(view: View, checked: Boolean) {
        view.cbCircleTypeCheck.setBackgroundResource(
                if (checked) R.drawable.bg_item_dev_stroke else R.drawable.bg_item_dev)
        view.ivTypeChecked.visibility = if (checked) View.VISIBLE else View.GONE
    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.btnBottomConfirm) {//底部按钮
            if(mLastCheckedPosition>=0){
                // 点击确定返回
                context.requireActivity().setResult(FragmentActivity.RESULT_OK,
                        Intent().putExtra(
                                FunctionHelper.EXTRA_ENTITY,
                                fragmentViewModel.circleTypes.value?.get(mLastCheckedPosition)))
                viewModel.toFinishActivity()
            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }
}