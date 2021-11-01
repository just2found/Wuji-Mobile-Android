package net.linkmate.app.ui.nas.group

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chad.library.adapter.base.FloatDecoration
import com.chad.library.adapter.base.entity.MultiItemEntity
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.api.V5_ERR_DENIED_PERMISSION
import kotlinx.android.synthetic.main.fragment_group_o_s_index.*
import kotlinx.android.synthetic.main.fragment_group_o_s_index.fab
import kotlinx.android.synthetic.main.fragment_group_o_s_index.recycle_view
import kotlinx.android.synthetic.main.fragment_group_o_s_index.swipe_refresh_layout
import kotlinx.android.synthetic.main.fragment_group_o_s_index.tipsBar
import kotlinx.android.synthetic.main.fragment_nav_cloud_files.*
import kotlinx.android.synthetic.main.fragment_safe_box_nas_file.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.ui.nas.group.adapter.GroupListAdapter
import net.linkmate.app.ui.nas.group.data.TextHeadTitle
import net.linkmate.app.ui.nas.group.list.GroupSpaceNasFileActivity
import net.linkmate.app.ui.nas.transfer.TransferActivity
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objboxkt.GroupItem
import net.sdvn.nascommon.utils.DialogUtils
import org.view.libwidget.RecyelerViewScrollDetector
import org.view.libwidget.hideAndDisable
import org.view.libwidget.showAndEnable
import org.view.libwidget.singleClick


/**
 * A simple [Fragment] subclass.
 * Use the [GroupOSIndexFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupOSIndexFragment : NavTipsBackPressedFragment() {
    private val navArgs by navArgs<GroupOSIndexFragmentArgs>()
    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    private val groupListAdapter: GroupListAdapter by lazy {
        GroupListAdapter().apply {
            setOnItemClickListener { baseQuickAdapter, view, position ->
                val item = baseQuickAdapter.getItem(position)
                if (item is net.linkmate.app.ui.nas.group.data.GroupItem
                ) {
                    if(item.isEnable())
                    context?.let { GroupSpaceNasFileActivity.startActivity(it, devId!!, item.id) }
                    else
                    ToastUtils.showToast(R.string.not_joined_the_group)
                }
            }

            setOnItemLongClickListener { baseQuickAdapter, view, position ->
                val item = baseQuickAdapter.getItem(position)
                if (item is net.linkmate.app.ui.nas.group.data.GroupItem) {
                    findNavController().navigate(
                        R.id.action_groupOSIndexFragment_to_groupOSSettingFragment,
                        GroupOSSettingFragmentArgs(navArgs.deviceid, item.id).toBundle()
                    )
                    return@setOnItemLongClickListener true
                }
                return@setOnItemLongClickListener false
            }

            setOnItemChildClickListener { baseQuickAdapter, view, position ->
                val item = baseQuickAdapter.getItem(position)
                if (item is net.linkmate.app.ui.nas.group.data.GroupItem) {
                    findNavController().navigate(
                        R.id.action_groupOSIndexFragment_to_groupOSSettingFragment,
                        GroupOSSettingFragmentArgs(navArgs.deviceid, item.id).toBundle()
                    )
                    return@setOnItemChildClickListener
                }
            }
        }
    }
    private val floatDecoration = FloatDecoration(GroupSpaceModel.TITLE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.liveDataEnableCreate.observe(this, Observer {
            fab.isVisible = it
        })
//        viewModel.focusRefreshLoginSession(navArgs.deviceid)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_o_s_index
    }

    override fun initView(view: View) {
        initTitle()
        recycle_view.addOnScrollListener(listener)
        recycle_view.addItemDecoration(floatDecoration)
        initList()
        fab.singleClick {
            findNavController().navigate(
                R.id.action_groupOSIndexFragment_to_groupOSCreateFragment,
                GroupOSIndexFragmentArgs(navArgs.deviceid).toBundle()
            )
        }
    }

    override fun getTopView(): View? {
        return titleBar
    }

    private fun initTitle() {
        titleBar.setBackListener {
            findNavController().popBackStack()
        }
        mTipsBar = tipsBar


        titleBar.addRightImgButton(R.drawable.icon_cloud_transfer_24dp)
        {
            val intent = Intent(it.context, TransferActivity::class.java)
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, navArgs.deviceid)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        if(flag)
        {
            loadGroups()
            flag=false
        }
    }

    private var flag =false
    override fun onPause() {
        flag=true
        super.onPause()
    }


    private fun initList() {
        swipe_refresh_layout.setOnRefreshListener {
            loadGroups()
        }
        loadGroups()
        recycle_view.adapter = groupListAdapter
    }

    private var notifyDialog: Dialog? = null

    private fun loadGroups() {
        devId?.let {
            viewModel.getGroupListJoined(it)?.observe(this, Observer {
                swipe_refresh_layout.isRefreshing = false
                if (it.status == Status.SUCCESS) {
                    initData(it.data)
                } else if (it.status == Status.ERROR) {
                    if (it.code == V5_ERR_DENIED_PERMISSION) {
                        lifecycleScope.launchWhenResumed {
                            if (notifyDialog == null || notifyDialog?.isShowing == false) {
                                notifyDialog = DialogUtils.showNotifyDialog(
                                    requireContext(), 0,
                                    R.string.ec_no_permission, R.string.confirm
                                ) { _, isPositiveBtn ->
                                    if (isPositiveBtn) {
                                        findNavController().popBackStack(
                                            R.id.groupOSIndexFragment,
                                            true
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                    }
                }
            })
        }
    }


    private fun initData(listJoinGroupResult: List<GroupItem>?) {
        val dataList = mutableListOf<MultiItemEntity>()
        var hasAdmin = false
        var hasMember = false
        var hasNotJoined = false

        listJoinGroupResult?.forEach {
            if (it.isAdmin()) {
                if (!hasAdmin) {
                    dataList.add(TextHeadTitle(getString(R.string.mine)))
                    hasAdmin = true
                }
            } else if (it.isNotJoined()) {
                if (!hasNotJoined) {
                    dataList.add(TextHeadTitle(getString(R.string.group_not_joined)))
                    hasNotJoined = true
                }
            } else {
                if (!hasMember) {
                    dataList.add(TextHeadTitle(getString(R.string.friend_device)))
                    hasMember = true
                }
            }
            dataList.add(net.linkmate.app.ui.nas.group.data.GroupItem.convert(it))
        }
        if (dataList.isNullOrEmpty()) {
            val inflate = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_empty_textview, null)
                .apply {
                    findViewById<TextView>(R.id.txt_empty).setText(R.string.not_joined_the_group)
                }
            groupListAdapter.setEmptyView(inflate)
        }
        groupListAdapter.setNewData(dataList)
//        if (!listJoinGroupResult.admin.isNullOrEmpty()) {
//            dataList.add(TextHeadTitle(getString(R.string.mine)))
//            dataList.addAll(listJoinGroupResult.admin)
//        }
//        if (!listJoinGroupResult.member.isNullOrEmpty()) {
//            dataList.add(TextHeadTitle(getString(R.string.friend_device)))
//            dataList.addAll(listJoinGroupResult.member)
//        }

    }

    val listener = object : RecyelerViewScrollDetector() {
        override fun onScrollUp() {
            if (fab?.isVisible != true) {
                fab?.showAndEnable()
            }
        }

        override fun onScrollDown() {
            if (fab?.isVisible == true) {
                fab?.hideAndDisable()
            }
        }
    }

}