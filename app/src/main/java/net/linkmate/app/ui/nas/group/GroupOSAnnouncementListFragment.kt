package net.linkmate.app.ui.nas.group

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_group_o_s_announcement_list.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.ui.nas.group.adapter.AnnouncementListAdapter
import net.linkmate.app.ui.nas.group.adapter.HeaderAdapter
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.db.objboxkt.GroupNotice
import org.view.libwidget.singleClick


/**
 * A simple [Fragment] subclass.
 * Use the [GroupOSAnnouncementListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupOSAnnouncementListFragment : NavTipsBackPressedFragment() {

    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    private val navArgs by navArgs<GroupOSSettingFragmentArgs>()
    private fun getGroupId(): Long {
        return navArgs.groupId
    }

    override fun initView(view: View) {
        initTitle()
        initList()
        if (isGroupManagement()) {
            add_folder_btn.visibility = View.VISIBLE
        }
        swipe_refresh_layout.setOnRefreshListener {
            initList()
        }
        add_folder_btn.singleClick {
            findNavController().navigate(
                R.id.action_groupOSAnnouncementListFragment_to_groupPublishAnnouncementFragment,
                GroupOSSettingFragmentArgs(navArgs.deviceid, navArgs.groupId).toBundle()
            )
        }
        headerAdapter.updateHeader(viewModel.getGroupItemName(navArgs.deviceid, navArgs.groupId))
    }

    private fun isGroupManagement(): Boolean {
        return viewModel.isGroupManagement(navArgs.deviceid, navArgs.groupId)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_o_s_announcement_list
    }

    override fun getTopView(): View? {
        return title_bar
    }

    private fun initTitle() {
        title_bar.setBackListener {
            findNavController().popBackStack()
        }
        mTipsBar = tipsBar
    }

    private fun initList() {
        devId?.let {
            viewModel.getGroupAnnouncementHistory(it, getGroupId())?.observe(this, Observer {
                swipe_refresh_layout.isRefreshing = false
                if (it.status == Status.SUCCESS) {
                    initData(it.data)
                } else if (it.status == Status.ERROR) {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                }
            })
        }
    }


    private fun initData(groupAnnouncementListResult: List<GroupNotice>?) {
        if (groupAnnouncementListResult.isNullOrEmpty()) {
            val inflate =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_empty_view, null)
            announcementListAdapter.setEmptyView(inflate)
        }

        recycle_view.adapter = concatAdapter
        announcementListAdapter.setNewData(groupAnnouncementListResult)
    }

    private val headerAdapter by lazy {
        HeaderAdapter()
    }
    private val concatAdapter by lazy {
        ConcatAdapter(headerAdapter, announcementListAdapter)
    }

    private val announcementListAdapter: AnnouncementListAdapter by lazy {
        AnnouncementListAdapter().apply {
            setOnItemClickListener { baseQuickAdapter, view, position ->
                val item = baseQuickAdapter.getItem(position)
                if (item is GroupNotice) {
                    findNavController().navigate(
                        R.id.action_groupOSAnnouncementListFragment_to_groupAnnouncementDetailFragment,
                        GroupAnnouncementDetailFragmentArgs(item).toBundle()
                    )
                }
            }
            setOnItemChildClickListener { baseQuickAdapter, view, position ->

            }
        }
    }
}