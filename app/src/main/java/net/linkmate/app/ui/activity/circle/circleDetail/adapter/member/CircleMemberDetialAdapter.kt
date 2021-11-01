package net.linkmate.app.ui.activity.circle.circleDetail.adapter.member

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import net.linkmate.app.R
import net.linkmate.app.data.model.CircleMember
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.view.HintDialog
import net.sdvn.nascommon.utils.ToastHelper

/**成员管理 详情－二级弹框
 * @author Raleigh.Luo
 * date：20/8/18 10
 * describe：
 */
class CircleMemberDetialAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleMemberDetialViewModel)
    : DialogBaseAdapter<CircleMemberDetialViewModel>(context, fragmentViewModel) {
    private val menus: ArrayList<FunctionHelper.DetailMenu> = ArrayList()
    private val isOwner: Boolean

    init {
        //获取上层传递过来的数据
        if (context.requireActivity().intent.hasExtra(FunctionHelper.EXTRA_ENTITY))
            fragmentViewModel.member = context.requireActivity().intent.getSerializableExtra(FunctionHelper.EXTRA_ENTITY) as CircleMember.Member?
        //空对象直接关闭页面
        if (fragmentViewModel.member == null) viewModel.toFinishActivity()
        isOwner = context.requireActivity().intent.getBooleanExtra("isOnwer", false)
        fragmentViewModel.updateViewStatusParams(headerTitle = fragmentViewModel.member?.getFullName(), headerDescribe = fragmentViewModel.member?.loginname)
        initObserver()
        getItemSources()
    }

    private fun initObserver() {
        fragmentViewModel.isDeleteSuccess.observe(context, Observer {
            if (it) {//删除成功
                ToastHelper.showToast(R.string.delete_user_succeed)
                context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                viewModel.toFinishActivity()
            }
        })
        fragmentViewModel.gradeMemberResult.observe(context, Observer {
            if(it){
                context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                viewModel.toFinishActivity()
            }
        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == TYPE_STATUS) R.layout.dialog_device_item_status
        else R.layout.dialog_device_item_detail
        val view =
                LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
//        return if (position == 0) TYPE_STATUS else TYPE_DEFALUT
        return TYPE_DEFALUT
    }

    override fun getItemCount(): Int {
        return menus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
//            TYPE_STATUS -> {
//                with(holder.itemView) {
//                    fragmentViewModel.member?.let {
//                        tvStatusTitle.setText(R.string.use_limit)
//                        tvStatusContent.setText(it.getExpire())
//                    }
//                }
//            }
            TYPE_DEFALUT -> {
                with(holder.itemView) {
                    ivDeviceDetailIcon.setImageResource(0)
                    ivDeviceDetailIcon.visibility = View.GONE
                    ivDeviceDetailTitle.setText(menus.get(position).title)
                    setOnClickListener {
                        //是否已经被拦截处理
                        val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                        //没有拦截，则可以内部处理
                        if (!isInterceptor) internalItemClick(it, position)
                    }

                }

            }

        }
    }

    var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                field = HintDialog.newInstance()
                        .setOnClickListener(View.OnClickListener {
                            if (it.id == R.id.positive) {//确定按钮
                                when (operateFunction) {
                                    FunctionHelper.CIRCLE_MEMBER_TRANSFOR_MANAGE ->{
                                        //升级
                                        fragmentViewModel.startGradeMember(1)
                                    }
                                    FunctionHelper.CIRCLE_MEMBER_TRANSFOR_MEMBER -> {
                                        //降级
                                        fragmentViewModel.startGradeMember(2)
                                    }
                                    FunctionHelper.CIRCLE_MEMBER_DELETE -> {
                                        //开始删除
                                        fragmentViewModel.startDeleteMember()
                                    }
                                }

                            }
                        })
                field?.onDismissListener = DialogInterface.OnDismissListener {
                    viewModel.setSecondaryDialogShow(false)
                }
                field?.onShowListener = DialogInterface.OnShowListener {
                    viewModel.setSecondaryDialogShow(true)
                }
            }
            return field
        }
    var operateFunction = -1
    override fun internalItemClick(view: View, position: Int) {
        if (position < 0) return
        operateFunction = menus.get(position).function
        when (menus.get(position).function) {
            FunctionHelper.CIRCLE_MEMBER_TRANSFER_OWNER -> {

            }
            FunctionHelper.CIRCLE_MEMBER_TRANSFOR_MANAGE -> {
                hintDialog?.update(context.getString(R.string.upgrade_to_administrator),
                        null,
                        confrimText = context.getString(R.string.confirm),
                        cancelText = context.getString(R.string.cancel))
                //升级提示
                if (hintDialog?.dialog?.isShowing != true)
                    hintDialog?.show(context.requireActivity().supportFragmentManager, "up")
            }
            FunctionHelper.CIRCLE_MEMBER_TRANSFOR_MEMBER -> {
                hintDialog?.update(context.getString(R.string.Downgrad_to_a_common_user),
                        null,
                        confrimText = context.getString(R.string.confirm),
                        cancelText = context.getString(R.string.cancel))
                //降级提示
                if (hintDialog?.dialog?.isShowing != true)
                    hintDialog?.show(context.requireActivity().supportFragmentManager, "down")
            }
            FunctionHelper.CIRCLE_MEMBER_DELETE -> {
                hintDialog?.update(context.getString(R.string.delete_user),
                        context.getString(R.string.delete_memebr_hint), R.color.red,
                        confrimText = context.getString(R.string.delete),
                        cancelText = context.getString(R.string.cancel))
                //删除用户提示
                if (hintDialog?.dialog?.isShowing != true)
                    hintDialog?.show(context.requireActivity().supportFragmentManager, "tag")
            }
        }

    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    fun getItemSources() {
        menus.clear()
        fragmentViewModel.member?.let {
//            if (isOwner) {
//                //owner
//                menus.add(FunctionHelper.DetailMenu(FunctionHelper.CIRCLE_MEMBER_TRANSFER_OWNER, context.getString(R.string.transfer_owner)))
//            }
            if (isOwner) {
                if (it.isManager()) {//管理员
                    menus.add(FunctionHelper.DetailMenu(FunctionHelper.CIRCLE_MEMBER_TRANSFOR_MEMBER, context.getString(R.string.Downgrad_to_a_common_user)))
                } else {//普通用户
                    menus.add(FunctionHelper.DetailMenu(FunctionHelper.CIRCLE_MEMBER_TRANSFOR_MANAGE, context.getString(R.string.upgrade_to_administrator)))
                }
            }
            if (!it.isOwner())
                menus.add(FunctionHelper.DetailMenu(FunctionHelper.CIRCLE_MEMBER_DELETE, context.getString(R.string.delete)))

        }
    }


}