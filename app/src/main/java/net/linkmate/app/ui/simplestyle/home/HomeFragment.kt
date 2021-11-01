package net.linkmate.app.ui.simplestyle.home

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.arch.core.util.Function
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home_simplestyle.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.data.ScoreHelper
import net.linkmate.app.manager.DevManager
import net.linkmate.app.manager.DeviceDialogManage
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.manager.RefreshENServer
import net.linkmate.app.ui.activity.HomeStatusActivity
import net.linkmate.app.ui.fragment.BaseFragment
import net.linkmate.app.ui.scan.ScanActivity
import net.linkmate.app.ui.simplestyle.MainViewModel
import net.linkmate.app.ui.viewmodel.TrafficPriceEditViewModel
import net.linkmate.app.util.*
import net.linkmate.app.util.AddPopUtil.OnPopButtonClickListener
import net.linkmate.app.util.business.ShowAddDialogUtil
import net.linkmate.app.view.HintDialog
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.protocal.EventObserver
import net.sdvn.common.ErrorCode
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.HardWareInfo
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/** 首页
 * @author Raleigh.Luo
 * date：20/11/17 17
 * describe：
 */
class HomeFragment : BaseFragment() {
    private lateinit var adapter: HomeAdapter
    private val viewModel: HomeViewModel by viewModels()
    protected val mDeviceViewModel: DeviceViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    protected val mTrafficPriceEditViewModel: TrafficPriceEditViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_home_simplestyle
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        adapter = HomeAdapter(requireContext(), viewModel, mDeviceViewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = net.linkmate.app.view.LinearLayoutManager(requireContext())

        //动态监听头部标题的显示
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (MySPUtils.getBoolean(MyConstants.IS_LOGINED) && viewModel.getDevicesSize() > 0) {
                    if (tvHeaderTitle.visibility == View.GONE) tvHeaderTitle.visibility = View.VISIBLE
                    val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val isOwner = viewModel.devices?.getOrNull(position)?.isOwner ?: false
                    val title = getString(if (isOwner) R.string.mine else R.string.friend_device)
                    if (tvHeaderTitle.text.toString() != title) tvHeaderTitle.setText(title)
                } else {
                    if (tvHeaderTitle.visibility == View.VISIBLE) tvHeaderTitle.visibility = View.GONE
                }

            }
        })
        toolbar.inflateMenu(R.menu.home_header_menu_simplestyle)
        toolbar.setOnMenuItemClickListener {
            if (MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
                when (it.itemId) {
                    R.id.scan -> {
                        val intent = Intent(context, ScanActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    R.id.circle -> {
                        startActivity(Intent(context, HomeStatusActivity::class.java))
                    }
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
            if (MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
                DevManager.getInstance().initHardWareList(object : ResultListener<HardWareInfo> {
                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                        viewModel.initHardWareListResult.value = false
                    }

                    override fun success(tag: Any?, data: HardWareInfo?) {
                        viewModel.initHardWareListResult.value = true
                    }
                })
            } else {
                mSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //未登录不可刷新
        mSwipeRefreshLayout.isEnabled = viewModel.checkLoggedin()
    }

    private fun showAddPop() {
        AddPopUtil.showAddPop(requireActivity(), vAddHelper, AddPopUtil.SHOW_SCAN or AddPopUtil.SHOW_ADD_DEV, OnPopButtonClickListener { v, clickNum ->
            if (Utils.isFastClick(v)) {
                return@OnPopButtonClickListener
            }
            when (clickNum) {
                AddPopUtil.SHOW_SCAN -> {
                    val intent = Intent(context, ScanActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                AddPopUtil.SHOW_ADD_DEV -> ShowAddDialogUtil.showAddDialog(context, clickNum)
                AddPopUtil.SHOW_ADD_NET -> ShowAddDialogUtil.showAddDialog(context, clickNum)
                AddPopUtil.SHOW_STATUS -> {
                    startActivity(Intent(context, HomeStatusActivity::class.java))
                }
            }
        })
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObserver()
    }


    var index = 0
    private fun initObserver() {
        viewModel.initHardWareListResult.observe(viewLifecycleOwner, Observer {//下拉刷新结果
            mSwipeRefreshLayout.isRefreshing = false
        })
        /**
         * 每次onEstablished都会触发
         */
        activityViewModel.hasLoggedin.observe(viewLifecycleOwner, Observer {
            viewModel.hasLoggedin.value = it
            //未登录不可刷新,必须在hasLoggedin后
            mSwipeRefreshLayout.isEnabled = viewModel.checkLoggedin()
            viewModel.refresh()
            adapter.update()
        })
        viewModel.refreshDevices.observe(viewLifecycleOwner, Observer {
            mSwipeRefreshLayout.isRefreshing = false
            adapter.update()


        })
        viewModel.deviceBriefs.observe(viewLifecycleOwner, Observer {
            viewModel.initDeviceBriefsMap()
        })
        viewModel.refreshAdapter.observe(viewLifecycleOwner, Observer {
            if (it == -1) {//刷新所有
                index++
                adapter.update()
            }
        })

        viewModel.currentOptDevice.observe(viewLifecycleOwner, Observer {
            val deviceId = it.id
            //因优先加载本地数据，deviceBeanInDevManager可能不及时，所以这里找不到则不响应
            val deviceBeanInDevManager = DevManager.getInstance().boundDeviceBeans.find {
                it.id == deviceId
            }
            deviceBeanInDevManager?.let {
                val access = {
                    if (it.isPendingAccept) {//等待同意状态
                        DeviceDialogManage.showDeviceDetailDialog(activity, 0, 0, it, null)
                    } else {
                        //访问进入设备
                        if (it.isDevDisable && it.devDisableReason == 1) {
                            ScoreHelper.showNeedMBPointDialog(requireContext())
                        } else {
                            val deviceId = it.id
                            CheckStatus.checkDeviceStatus(requireActivity(), requireActivity().getSupportFragmentManager(), it, Function { isNormalStatus: Boolean ->  //检查状态
                                if (isNormalStatus) {
                                    if (mTrafficPriceEditViewModel.whetherShowTrafficTips(requireContext(), deviceId, Callback<Result<*>> { result: Result<*>? -> open(it, 0) })) {
                                    } else {
                                        open(it, 0)
                                    }
                                }
                                null
                            }, null)
                        }
                    }
                }
                //   //2.点击异常设备立即弹窗询问对应操作：不在此圈子，询问“是否切换到对应的圈子”
                var networkId = it.hardData?.networkId
                if (!TextUtils.isEmpty(networkId)) {
                    if (NetsRepo.getOwnNetwork(networkId ?: "") == null) {
                        it.hardData?.networkIds?.forEach {
                            val id = it
                            NetsRepo.getOwnNetwork(it)?.let {
                                networkId = id
                                return@forEach
                            }
                        }
                    }
                }

                if (!it.isPendingAccept && !it.isOnline && !TextUtils.isEmpty(networkId)) {//不在此圈子
                    HintDialog.newInstance(getString(R.string.device_disable_switch_circle_hint),
                            confrimText = getString(R.string.confirm),
                            cancelText = getString(R.string.cancel)
                    ).setOnClickListener(View.OnClickListener {
                        if (it.id == R.id.positive) {//确定按钮
                            // 切换网络
                            with(requireActivity() as BaseActivity) {
                                this.status = BaseActivity.LoadingStatus.CHANGE_VIRTUAL_NETWORK
                                this.showLoading(R.string._switch)
                            }
                            CMAPI.getInstance().switchNetwork(networkId, object : net.sdvn.cmapi.protocal.ResultListener {
                                override fun onError(error: Int) {
                                    try {
                                        (requireActivity() as BaseActivity).dismissLoading()
                                        if (error == Constants.CE_SUCC) {
                                            NetsRepo.refreshNetList()
                                        } else {
                                            ToastUtils.showToast(getString(ErrorCode.error2String(error)));
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                            })
                        } else {
                            access()
                        }
                    }).show(requireActivity().getSupportFragmentManager(), "hintDialog")

                } else {
                    access()
                }
            }
        })

        viewModel.currentLongOptDevice.observe(viewLifecycleOwner, Observer {
            DeviceDialogManage.showDeviceDetailDialog(requireContext(), 0, 0, it, null)
        })

        activityViewModel.cancelLoading.observe(viewLifecycleOwner, Observer {
            if (it == BaseActivity.LoadingStatus.ACCESS_NAS) {
                mAccessDeviceTool?.cancel()
            }
        })

    }

    private fun open(bean: DeviceBean, position: Int) {
        if (mAccessDeviceTool == null) {
            mAccessDeviceTool = AccessDeviceTool(requireActivity() as BaseActivity, this)
        }
        mAccessDeviceTool?.open(bean, position, Function {
            adapter.update()
            null
        })
    }

    private var mAccessDeviceTool: AccessDeviceTool? = null

    override fun getHomeTipsBar(): TipsBar? {
        return tipsBar
    }

    override fun getTopView(): View? {
        return flToolbar
    }

    private val eventObserver: EventObserver = object : EventObserver() {
        override fun onNetworkChanged() {
            super.onNetworkChanged()
            //刷新数据
            Timber.d("RefreshENServer")
            EventBus.getDefault().post(RefreshENServer())//onNetworkChanged
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAccessDeviceTool?.cancel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkRefresh()
        adapter.update()
        CMAPI.getInstance().subscribe(eventObserver)
    }

    override fun onStop() {
        super.onStop()
        CMAPI.getInstance().unsubscribe(eventObserver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQUEST_CODE_HD_FORMAT) {
            mAccessDeviceTool?.resetOpenHDManageValue()
        }
    }
}