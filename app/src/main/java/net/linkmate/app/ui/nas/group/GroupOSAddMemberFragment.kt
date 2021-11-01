package net.linkmate.app.ui.nas.group

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_group_o_s_add_member.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.ui.nas.group.adapter.DeviceUsersAdapter
import net.linkmate.app.ui.nas.group.list.GroupSpaceNasFileActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.ToastHelper


/**
 * A simple [Fragment] subclass.
 * Use the [GroupOSAddMemberFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupOSAddMemberFragment : NavTipsBackPressedFragment() {

    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    private val navArgs by navArgs<GroupOSAddMemberFragmentArgs>()
    private val deviceUsersAdapter: DeviceUsersAdapter by lazy {
        DeviceUsersAdapter()
    }

    override fun initView(view: View) {
        titleBar.setBackListener {
            findNavController().popBackStack()
        }
        mTipsBar=tipsBar
        titleBar.setTitleText(
            when {
                navArgs.action and ACTION_CHANGE_ADMIN != 0 -> {
                    R.string.transfer_management_authority
                }
                else -> {
                    R.string.add_family_member
                }
            }
        )
        titleBar.addRightTextButton(getString(R.string.confirm)) {
            when {
                navArgs.action and ACTION_CHANGE_ADMIN != 0 -> {
                    val selectedItems = deviceUsersAdapter.getSelectedItems()
                    if (selectedItems.isNullOrEmpty()) {
                        ToastHelper.showLongToast(R.string.pls_notify_select_one)
                        return@addRightTextButton
                    }
                    val username = selectedItems[0]
                    viewModel.transferGroup(navArgs.deviceid, navArgs.groupId, username)
                        .observe(this,
                            Observer {
                                if (it.status == Status.SUCCESS) {
                                    onBackPressed()
                                } else if (it.status == Status.ERROR) {
                                    viewModel.optErrorProcessor(it.code)
                                }
                            })
                }
                navArgs.action and ACTION_ADD != 0 -> {
                    val selectedItems = deviceUsersAdapter.getSelectedItems()
                    if (selectedItems.isNullOrEmpty()) {
                        ToastHelper.showLongToast(R.string.pls_notify_select_one)
                        return@addRightTextButton
                    }
                    val users = selectedItems.toTypedArray()
                    viewModel.addUsers(navArgs.deviceid, navArgs.groupId, *users)
                        .observe(this,
                            Observer {
                                if (it.status == Status.SUCCESS) {
                                    if (navArgs.action and ACTION_FROM_CREATE != 0) {
                                        GroupSpaceNasFileActivity.startActivity(
                                            requireContext(),
                                            navArgs.deviceid,
                                            navArgs.groupId
                                        )
                                        onBackPressed()
                                    } else {
                                        onBackPressed()
                                    }
                                } else if (it.status == Status.ERROR) {
                                    viewModel.optErrorProcessor(it.code)
                                }
                            })

                }
            }
        }
        refreshData()
        swipe_refresh_layout.setOnRefreshListener {
            refreshData()
        }
        recycle_view.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                RecyclerView.HORIZONTAL
            )
        )
    }

    private fun refreshData() {
        when {
            navArgs.action and ACTION_CHANGE_ADMIN != 0 -> {
                viewModel.loadGroupUsers(navArgs.deviceid, navArgs.groupId)
                    ?.observe(this, Observer {
                        swipe_refresh_layout.isRefreshing = false
                        if (it.status == Status.SUCCESS) {
                            if (it.data.isNullOrEmpty()) {
                                val inflate = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.layout_empty_textview, null)
                                    .apply {
                                        findViewById<TextView>(R.id.txt_empty).setText(R.string.no_more_users_to_add)
                                    }
                                deviceUsersAdapter.setEmptyView(inflate)
                            }
                            deviceUsersAdapter.setNewData(it.data?.let { it1 ->
                                filterAlreadyExists(it1.filter { it.userId != SessionManager.getInstance().userId }
                                    .map { it.username })
                            })
                            deviceUsersAdapter.isSingleSelection = true
                            recycle_view.adapter = deviceUsersAdapter
                        } else if (it.status == Status.ERROR) {
                            viewModel.optErrorProcessor(it.code)
                        }
                    })
            }
            navArgs.action and ACTION_ADD != 0 -> {
                viewModel.loadDevicesUser(navArgs.deviceid, navArgs.groupId)
                    .observe(this, Observer {
                        swipe_refresh_layout.isRefreshing = false
                        if (it.status == Status.SUCCESS) {
                            if (it.data.isNullOrEmpty()) {
                                val inflate = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.layout_empty_textview, null)
                                    .apply {
                                        findViewById<TextView>(R.id.txt_empty).setText(R.string.no_more_users_to_add)
                                    }

                                deviceUsersAdapter.setEmptyView(inflate)
                            }
                            deviceUsersAdapter.setNewData(it.data?.let { it1 ->
                                filterAlreadyExists(
                                    it1
                                )
                            })
                            deviceUsersAdapter.isSingleSelection = false
                            recycle_view.adapter = deviceUsersAdapter
                        } else if (it.status == Status.ERROR) {
                            viewModel.optErrorProcessor(it.code)
                        }
                    })
            }
        }

    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_o_s_add_member
    }

    override fun getTopView(): View? {
        return titleBar
    }

    private fun filterAlreadyExists(dataList: List<String>): List<String> {
        return dataList
    }


    companion object {
        const val ACTION_ADD = 1
        const val ACTION_CHANGE_ADMIN = 2
        const val ACTION_FROM_CREATE = 1.shl(2)
    }

}