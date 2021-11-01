package net.linkmate.app.ui.activity.circle.circleDetail.adapter.member

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_circle_member.view.*
import net.linkmate.app.R
import net.linkmate.app.data.model.CircleMember
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.sdvn.cmapi.CMAPI

/**
 * @author Raleigh.Luo
 * date：20/8/14 15
 * describe：
 */
class CircleMemberAdapter(context: Fragment, val viewModel: CircleDetialViewModel,
                          fragmentViewModel: CircleMemberViewModel) : DialogBaseAdapter<CircleMemberViewModel>(context, fragmentViewModel) {
    init {
        //设置头部显示圈子名称
        fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname)
        initObserver()
        fragmentViewModel.startRequestRemoteSource()
    }

    fun initObserver() {
        fragmentViewModel.members.observe(context, Observer {
            notifyDataSetChanged()
        })
        viewModel.activityResult.observe(context, Observer {
            if (it.requestCode == FunctionHelper.CIRCLE_MEMBER_DETAIL
                    && it.resultCode == FragmentActivity.RESULT_OK) {
                //删除页面返回，刷新数据
                fragmentViewModel.startRequestRemoteSource()
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_circle_member, null, false)
        view.layoutParams =
                ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fragmentViewModel.members.value?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            val member: CircleMember.Member? = fragmentViewModel.members.value?.get(position)
            member?.let {
                tvMemberName.text = it.getFullName() +"(${it.loginname})"
                tvExpire.text = it.getExpire()
                when {
                    it.isOwner() -> {
                        tvMemberName.setEndDrawable(context.getDrawable(R.drawable.icon_user_admin))
                    }
                    it.isManager() -> {
                        tvMemberName.setEndDrawable(context.getDrawable(R.drawable.icon_user_master))
                    }
                    else -> {
                        tvMemberName.setEndDrawable(null)
                    }
                }
            }
            setOnClickListener {
                //是否已经被拦截处理
                val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                //没有拦截，则可以内部处理
                if (!isInterceptor) internalItemClick(it, position)
            }
        }
    }

    //记录当前操作的位置
    private var operatePosition = -1

    override fun internalItemClick(view: View, position: Int) {
        if (position < 0) return
        if (fragmentViewModel.members.value?.get(position)?.isOwner() ?: false == false) {//所有者不能删除
            //管理员只能删除普通用户或者自己
            if (viewModel.circleDetail.value?.isOwner()?:false == true
                    || fragmentViewModel.members.value?.get(position)?.isManager() ?: false == false
                    || fragmentViewModel.members.value?.get(position)?.userid == CMAPI.getInstance().baseInfo.userId) {
                operatePosition = position
                //叠加弹框阴影效果
                viewModel.setSecondaryDialogShow(true)
                //二级弹框－详情
                CircleDetialActivity.startActivityForResult(context.requireActivity(),Intent(context.requireContext(), CircleDetialActivity::class.java)
                        .putExtra(FunctionHelper.FUNCTION, FunctionHelper.CIRCLE_MEMBER_DETAIL)
                        .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId)
                        .putExtra("isOnwer", viewModel.circleDetail.value?.isOwner())
                        .putExtra(FunctionHelper.EXTRA_ENTITY, fragmentViewModel.members.value?.get(position)),
                        FunctionHelper.CIRCLE_MEMBER_DETAIL)
            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

}
