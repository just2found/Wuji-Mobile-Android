package net.linkmate.app.ui.nas.group

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_group_member_list.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.ui.nas.group.adapter.GroupUserAdapter
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.db.objboxkt.GroupUser


/**
 * A simple [Fragment] subclass.
 * Use the [GroupMemberListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupMemberListFragment : NavTipsBackPressedFragment() {

    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    private val navArgs by navArgs<GroupOSSettingFragmentArgs>()
    private var mHeaderView: View? = null
    private val simpleUserAdapter: GroupUserAdapter by lazy {
        GroupUserAdapter().apply {
            mHeaderView = LayoutInflater.from(context).inflate(R.layout.layout_user_header, null)
            setOnItemClickListener { baseQuickAdapter, view, i ->
                val item = baseQuickAdapter.getItem(i)
                if (item is GroupUser && isGroupManagement()) {
                    findNavController().navigate(
                        R.id.action_groupMemberListFragment_to_groupSpaceUserDetailFragment,
                        GroupSpaceUserDetailFragmentArgs(item).toBundle()
                    )
                }
            }
        }
    }

    override fun initView(view: View) {
        initTitle()
        recycle_view.adapter = simpleUserAdapter
        refreshData()
        swipe_refresh_layout.setOnRefreshListener {
            refreshData()
        }
    }






    private fun refreshData() {
        viewModel.loadGroupUsers(navArgs.deviceid, navArgs.groupId)?.observe(this, Observer {
            swipe_refresh_layout.isRefreshing = false
            when (it.status) {
                Status.SUCCESS -> {
                    simpleUserAdapter.removeAllHeaderView()
                    if (it.data.isNullOrEmpty()) {
                        val inflate =
                            LayoutInflater.from(requireContext())
                                .inflate(R.layout.layout_empty_view, null)
                        simpleUserAdapter.setEmptyView(inflate)
                    } else {
                        simpleUserAdapter.addHeaderView(mHeaderView)
                        mHeaderView?.findViewById<TextView>(R.id.tv_user_header)?.text =
                            "$usersStr ${it?.data?.size ?: 0}"
                    }
                    it.data?.let { list ->
                        val sortedBy = list.sortedBy {//群组放第一个，其余按加入时间排序
                            if(it.isAdmin)
                            {
                                0
                            }else{
                                it.joinTime
                            }
                        }
                        simpleUserAdapter.setNewData(sortedBy)
                    }

                }


                Status.ERROR -> {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                }
            }
        })
    }

    private fun isGroupManagement(): Boolean {
        return viewModel.isGroupManagement(navArgs.deviceid, navArgs.groupId)
    }

    private val usersStr: String by lazy {
        resources.getString(R.string.tips_user_numbers)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_member_list
    }

    override fun getTopView(): View? {
        return titleBar
    }

    private fun initTitle() {
        titleBar.setBackListener {
            findNavController().popBackStack()
        }
        if (isGroupManagement()) {
            titleBar.addRightTextButton(getString(R.string.btn_add_user)) {
                findNavController().navigate(
                    R.id.action_groupMemberListFragment_to_groupOSAddMemberFragment,
                    GroupOSAddMemberFragmentArgs(
                        navArgs.deviceid, navArgs.groupId,
                        GroupOSAddMemberFragment.ACTION_ADD
                    ).toBundle()
                )
            }
        }
        mTipsBar = tipsBar
    }
}