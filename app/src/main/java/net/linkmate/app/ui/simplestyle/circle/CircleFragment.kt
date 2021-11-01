package net.linkmate.app.ui.simplestyle.circle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.util.Function
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_circle_simplestyle.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseActivity.LoadingStatus
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.fragment.BaseFragment
import net.linkmate.app.ui.scan.ScanActivity
import net.linkmate.app.ui.simplestyle.MainViewModel
import net.linkmate.app.ui.simplestyle.circledetail.CircleActivity
import net.linkmate.app.util.AddPopUtil
import net.linkmate.app.util.CheckStatus
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.business.ShowAddDialogUtil
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.ErrorCode
import net.sdvn.nascommon.utils.Utils

/** 圈子列表
 * @author Raleigh.Luo
 * date：20/11/25 16
 * describe：
 */
class CircleFragment : BaseFragment() {
    private lateinit var adapter: CircleAdapter
    private val viewModel: CircleViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_circle_simplestyle
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        adapter = CircleAdapter(requireContext(), viewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = net.linkmate.app.view.LinearLayoutManager(requireContext())
        toolbar.title = getString(R.string.circle)
        toolbar.inflateMenu(R.menu.circle_header_menu_simplestyle)
        toolbar.menu.findItem(R.id.add).icon = resources.getDrawable(R.drawable.icon_home_add_48)
        toolbar.setOnMenuItemClickListener {
            if (viewModel.checkLoggedin()) {
                when (it.itemId) {
                    R.id.add -> {
                        showAddPop()
                    }
                }
            } else {
                LoginManager.getInstance().showDialog(context)
            }
            true
        }
        mSwipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        //未登录不可刷新
        mSwipeRefreshLayout.isEnabled = viewModel.checkLoggedin()
        if (viewModel.checkLoggedin()) {
            viewModel.refresh()
        }
    }

    private fun showAddPop() {
        AddPopUtil.showAddPop(requireActivity(), vAddHelper, AddPopUtil.SHOW_SCAN or AddPopUtil.SHOW_ADD_NET, AddPopUtil.OnPopButtonClickListener { v, clickNum ->
            if (Utils.isFastClick(v)) {
                return@OnPopButtonClickListener
            }
            when (clickNum) {
                AddPopUtil.SHOW_SCAN -> {
                    val intent = Intent(context, ScanActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                AddPopUtil.SHOW_ADD_NET -> ShowAddDialogUtil.showAddDialog(context, clickNum)
            }
        })
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObserver()
    }

    private fun initObserver() {
        viewModel.circles.observe(viewLifecycleOwner, Observer {
            viewModel.startGetBriefs(it)
            mSwipeRefreshLayout.isRefreshing = false
            adapter.update()
        })
        activityViewModel.hasLoggedin.observe(viewLifecycleOwner, Observer {
            viewModel.refresh()
            viewModel.hasLoggedin.value = it
        })

        viewModel.circleBriefs.observe(viewLifecycleOwner, Observer {
            viewModel.initDeviceBriefsMap()
        })
        viewModel.refreshAdapter.observe(viewLifecycleOwner, Observer {
            if (it == -1) {//刷新所有
                adapter.update()
            }
        })

        viewModel.currentOptNetwork.observe(viewLifecycleOwner, Observer {
            val network = it
            if (network.isCurrent) {
                startActivity(Intent(requireContext(), CircleActivity::class.java)
                        .putExtra(FunctionHelper.NETWORK_ID, network.netId))
            } else {
                if (it.userStatus != 0) {//待同意圈子
                    CircleDetialActivity.startActivity(requireActivity(), Intent(requireContext(), CircleDetialActivity::class.java)
                            .putExtra(FunctionHelper.NETWORK_ID, it.netId))
                } else {
                    DynamicQueue.checkExistPublishingDynamic(requireActivity() as AppCompatActivity, Function {
                        if (it) {
                            CheckStatus.checkCircleStatus(requireActivity(), requireActivity().supportFragmentManager, network, androidx.arch.core.util.Function {
                                if (it) {//状态正常  状态回调
                                    if (getHomeTipsBar()?.isVisible ?: false == false) {//避免频繁操作
                                        // 切换网络
                                        with(requireActivity() as BaseActivity) {
                                            this.status = LoadingStatus.CHANGE_VIRTUAL_NETWORK
                                            this.showLoading(R.string._switch)
                                        }
                                        CMAPI.getInstance().switchNetwork(network.netId) { error ->
                                            viewModel.switchNetworkResult.value = error
                                        }
                                    }
                                }
                                null
                            }, Function {//操作回调
                                if (!it) { //用户选择不购买流量,切换网络
                                    // 切换网络
                                    with(requireActivity() as BaseActivity) {
                                        this.status = LoadingStatus.CHANGE_VIRTUAL_NETWORK
                                        this.showLoading(R.string._switch)
                                    }
                                    CMAPI.getInstance().switchNetwork(network.netId) { error ->
                                        viewModel.switchNetworkResult.value = error
                                    }
                                }
                                null
                            })
                        }
                        null
                    })
                }
            }
        })

        viewModel.switchNetworkResult.observe(viewLifecycleOwner, Observer {
            (requireActivity() as BaseActivity).dismissLoading()
            if (it != Constants.CE_SUCC) {
                ToastUtils.showToast(getString(ErrorCode.error2String(it)))
            } else {//刷新圈子
                viewModel.refresh()
                startActivity(Intent(requireContext(), CircleActivity::class.java)
                        .putExtra(FunctionHelper.NETWORK_ID, viewModel.currentOptNetwork.value?.netId))
            }
        })
    }

    override fun getHomeTipsBar(): TipsBar? {
        return tipsBar
    }

    override fun getTopView(): View? {
        return flToolbar
    }

}