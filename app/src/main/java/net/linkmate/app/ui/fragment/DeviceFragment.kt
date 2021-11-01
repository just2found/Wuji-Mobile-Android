package net.linkmate.app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Pair
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.arch.core.util.Function
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache.Companion.instance
import io.weline.repo.api.V5_ERR_DISK_BUILDING
import io.weline.repo.api.V5_ERR_DISK_FORMATTING
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseActivity.StopLoginNasDevice
import net.linkmate.app.base.DevBoundType
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.data.ScoreHelper
import net.linkmate.app.manager.DevManager
import net.linkmate.app.manager.DevManager.DevUpdateObserver
import net.linkmate.app.manager.DeviceDialogManage
import net.linkmate.app.manager.RefreshENServer
import net.linkmate.app.manager.StatePager
import net.linkmate.app.ui.nas.files.V2NasDetailsActivity
import net.linkmate.app.ui.nas.helper.HdManageActivity
import net.linkmate.app.ui.nas.info.NavigationContainerActivity
import net.linkmate.app.ui.simplestyle.device.disk.DiskSpaceActivity
import net.linkmate.app.ui.simplestyle.home.HomeViewModel
import net.linkmate.app.ui.viewmodel.DevCommonViewModel
import net.linkmate.app.ui.viewmodel.M8CheckUpdateViewModel
import net.linkmate.app.ui.viewmodel.TrafficPriceEditViewModel
import net.linkmate.app.util.AccessDeviceTool
import net.linkmate.app.util.CheckStatus.checkDeviceStatus
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.GridLayoutManager
import net.linkmate.app.view.HintDialog
import net.linkmate.app.view.adapter.HomeDevRVAdapter
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.UpdateInfo
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.protocal.EventObserver
import net.sdvn.common.ErrorCode
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.HardWareInfo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit

class DeviceFragment : BaseFragment(), DevUpdateObserver {
    private var mAccessDeviceTool: AccessDeviceTool? = null
    private var devBoundType = 0
    private var hasTypeTitle = false
    private var rvMyDevices: RecyclerView? = null
    private var mSrlDevice: SwipeRefreshLayout? = null
    private var myDeviceAdapter: HomeDevRVAdapter? = null
    private var mStatePager: StatePager? = null
    private val beans: MutableList<DeviceBean> = ArrayList()
    private val mDeviceViewModel: DeviceViewModel by viewModels({ requireActivity() })
    private val mM8CheckUpdateViewModel: M8CheckUpdateViewModel by viewModels({ requireActivity() })
    private val mTrafficPriceEditViewModel: TrafficPriceEditViewModel by viewModels({ requireActivity() })
    private val viewModel: HomeViewModel by viewModels()
    private var outRefreshLayout: SwipeRefreshLayout? = null
    private val devCommonViewModel: DevCommonViewModel by viewModels({ requireActivity() })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        mDeviceViewModel.liveDevices.observe(this, Observer<List<DeviceModel>> { deviceModels: List<DeviceModel> ->
//            onDevUpdate(devBoundType);
            mM8CheckUpdateViewModel.refreshDeviceUpdateInfo(deviceModels)
        })
        mM8CheckUpdateViewModel.updateInfosLiveData.observe(this, Observer<List<Pair<DeviceModel, UpdateInfo>>> { pairs: List<Pair<DeviceModel, UpdateInfo>>? ->
            if (devBoundType == DevBoundType.IN_THIS_NET) {
                if (!pairs.isNullOrEmpty()) {
                    mM8CheckUpdateViewModel.showM8Upgrade(requireContext(), requireFragmentManager(), pairs)
                }
            }
        })
        mM8CheckUpdateViewModel.updateResultsLiveData.observe(this, Observer<List<Pair<DeviceModel, Result<Int>>>> { pairs: List<Pair<DeviceModel, Result<Int>>>? ->
            if (devBoundType == DevBoundType.IN_THIS_NET) {
                if (!pairs.isNullOrEmpty()) {
                    if (pairs.size == 1) {
                        mM8CheckUpdateViewModel.showDeviceItemUpgradeResult(requireContext(), pairs[0])
                    } else {
                        mM8CheckUpdateViewModel.showM8UpgradeResults(requireContext(), requireFragmentManager(), pairs)
                    }
                }
            }
        })

        viewModel.deviceBriefs.observe(this, Observer {
            viewModel.initDeviceBriefsMap()
        })
        viewModel.refreshAdapter.observe(this, Observer {
            if (it == -1) {//刷新所有
                myDeviceAdapter?.let {
                    it?.notifyItemRangeChanged(0, it.itemCount, arrayListOf(1))
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mAccessDeviceTool?.cancel()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    fun onEvent(stopLoginDevice: StopLoginNasDevice?) {
        if (mLoginDeviceDisposable != null && !mLoginDeviceDisposable!!.isDisposed) mLoginDeviceDisposable!!.dispose()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home_device
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        bindView(view)
        mSrlDevice!!.setOnRefreshListener { refreshDevData() }
        val args = arguments
        if (args != null) {
            devBoundType = args.getInt("devBoundType")
            hasTypeTitle = args.getBoolean("hasTypeTitle", true)
            if (DevManager.getInstance().isInitting &&
                    isObserverRefresh(devBoundType)) {
                refresh(true)
            }
        }
        initRv()
        mStatePager = StatePager.builder(rvMyDevices!!)
                .emptyViewLayout(R.layout.pager_empty_text) //                .emptyViewLayout(devBoundType == DevBoundType.IN_THIS_NET || devBoundType == DevBoundType.LOCAL_DEVICES ?
                //                        R.layout.pager_empty_text : R.layout.pager_empty_text_add)
                //                .addRetryButtonId(R.id.home_iv_add)
                //                .setRetryClickListener(new View.OnClickListener() {
                //                    @Override
                //                    public void onClick(View v) {
                //                        ShowAddDialogUtil.showAddDialog(getContext(), AddPopUtil.SHOW_ADD_DEV);
                //                    }
                //                })
                .build()
        if (isObserverRefresh(devBoundType)) {
            refreshDevData()
        } else if (devBoundType == DevBoundType.LOCAL_DEVICES) {
            refreshDevData()
        }
        if (outRefreshLayout != null) {
            mSrlDevice!!.isEnabled = false
        }
    }

    /**
     * 添加外部刷新布局到此fragment
     * 添加时关闭当前fragment刷新布局的功能
     */
    fun setSwipeRefreshLayout(refreshLayout: SwipeRefreshLayout?) {
        if (refreshLayout == null) return
        outRefreshLayout = refreshLayout
        if (mSrlDevice != null) {
            mSrlDevice!!.isEnabled = false
        }
    }

    private fun refresh(refresh: Boolean) {
        //当添加了外部布局监听时，不再展示当前的刷新布局
        if (outRefreshLayout != null) {
            outRefreshLayout!!.isRefreshing = refresh
        } else {
            mSrlDevice!!.isRefreshing = refresh
        }
    }

    override fun onStart() {
        super.onStart()
        CMAPI.getInstance().subscribe(eventObserver)
        DevManager.getInstance().addDevUpdateObserver(this)
        initBeans()
        refreshDeviceView()
    }

    override fun onStop() {
        super.onStop()
        CMAPI.getInstance().unsubscribe(eventObserver)
        DevManager.getInstance().deleteDevUpdateObserver(this)
    }

    private val eventObserver: EventObserver = object : EventObserver() {
        override fun onNetworkChanged() {
            super.onNetworkChanged()
            //刷新数据
            Timber.d("RefreshENServer")
            EventBus.getDefault().post(RefreshENServer())//onNetworkChanged
        }
    }

    fun refreshDevData() {
        refresh(true)
        val listener: ResultListener<HardWareInfo> = object : ResultListener<HardWareInfo> {
            override fun success(tag: Any?, data: HardWareInfo) {
                if (devBoundType == DevBoundType.LOCAL_DEVICES && "LocalDevData" != tag) return
                if (mSrlDevice != null) refresh(false)
            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                if (mSrlDevice != null) refresh(false)
            }
        }
        if (devBoundType == DevBoundType.LOCAL_DEVICES) {
            DevManager.getInstance().initLocalDevData(listener)
        } else if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) {
            //刷新云设备
            DevManager.getInstance().refreshCloudDevices(null)
            DevManager.getInstance().initHardWareList(listener)//首页用户主动下拉
        } else {
            DevManager.getInstance().initHardWareList(listener)//首页用户主动下拉
        }
    }

    private fun initRv() {
        myDeviceAdapter = HomeDevRVAdapter(beans, viewModel, devCommonViewModel, devBoundType)
        myDeviceAdapter!!.setFragmentType(devBoundType)
        //点击device条目
        myDeviceAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            if (Utils.isFastClick(view)) {
                return@OnItemClickListener
            }
            val bean = adapter.getItem(position) as DeviceBean?
            //非标题设备
            if (bean != null && bean.type != -1) {
                checkStatus(bean, position)
            }
        }
        //长按device条目
        myDeviceAdapter!!.onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
            val bean = adapter.getItem(position) as DeviceBean?
            //非标题设备
            if (bean != null && bean.type != -1) {
                showDeviceDetailDialog(position, bean)
                true
            } else {
                false
            }
        }
        //点击详情按钮
        myDeviceAdapter!!.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            if (Utils.isFastClick(view, 800)) return@OnItemChildClickListener
            if (view.id == R.id.ihd_iv_more) {
                val item = adapter.getItem(position) as DeviceBean?
                item?.let { showDeviceDetailDialog(position, it) }
            }
        }
        val spanCount = if (devBoundType == DevBoundType.ALL_BOUND_DEVICES || devBoundType == DevBoundType.LOCAL_DEVICES) 1 else 2
        val layout = GridLayoutManager(requireContext(), spanCount, RecyclerView.VERTICAL, false)
        rvMyDevices!!.layoutManager = layout
        rvMyDevices!!.itemAnimator = null
        rvMyDevices!!.adapter = myDeviceAdapter
    }

    private fun isInHome(devBoundType: Int): Boolean {
        return devBoundType == DevBoundType.IN_THIS_NET || devBoundType == DevBoundType.MY_DEVICES || devBoundType == DevBoundType.SHARED_DEVICES
    }

    private fun checkStatus(bean: DeviceBean, position: Int) {
        if (bean.typeValue == 3) {//云设备，直接弹出状态
            showDeviceDetailDialog(position, bean)
        } else {
            val access = {
                if (bean.isPendingAccept) {//等待同意状态
                    DeviceDialogManage.showDeviceDetailDialog(activity, 0, 0, bean, null)
                } else {
                    if (bean.isDevDisable && bean.devDisableReason == 1) {
                        ScoreHelper.showNeedMBPointDialog(context)
                    } else {
                        val deviceId = bean.id
                        checkDeviceStatus(requireActivity(), requireActivity().supportFragmentManager, bean, Function { isNormalStatus: Boolean ->  //检查状态
                            if (isNormalStatus) {
                                if (mTrafficPriceEditViewModel!!.whetherShowTrafficTips(requireContext(), deviceId, Callback { result: Result<*>? -> open(bean, position) })) {
                                } else {
                                    open(bean, position)
                                }
                            }
                            null
                        }, null)
                    }
                }

            }

            var networkId = bean.hardData?.networkId
            if (!TextUtils.isEmpty(networkId)) {
                if (NetsRepo.getOwnNetwork(networkId ?: "") == null) {
                    bean.hardData?.networkIds?.forEach {
                        val id = it
                        NetsRepo.getOwnNetwork(it)?.let {
                            networkId = id
                            return@forEach
                        }
                    }
                }
            }
            if (!bean.isPendingAccept && !bean.isOnline && !TextUtils.isEmpty(networkId)) {//不在此圈子
                //   //2.点击异常设备立即弹窗询问对应操作：不在此圈子，询问“是否切换到对应的圈子”
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
    }

    private fun open(bean: DeviceBean, position: Int) {
        if (mAccessDeviceTool == null) {
            mAccessDeviceTool = AccessDeviceTool(requireActivity() as BaseActivity, this)
        }
        mAccessDeviceTool?.open(bean, position, Function {
            refreshDevData()
            null
        })
//        if (bean.isVNode) {
//            // TODO: 2019/12/12  vnode test
//            //虚拟节点
//            showDeviceDetailDialog(position, bean)
//        } else if (bean.isNas && bean.isOnline && isInHome(devBoundType)) {
//            if (bean.hardData != null && bean.hardData!!.isEnableUseSpace) {
//                //NAS
//                loginDevice(bean, position, bean.id)
//            } else if (bean.hardData != null && bean.isEn || bean.enServer != null && bean.enServer!!.isEn == true) {
//                startActivityWithId(requireContext(), bean.id, false)
//            } else {
//                showDeviceDetailDialog(position, bean)
//            }
//        } /*else if (bean.getType() == 0 || devBoundType == DevBoundType.ALL_BOUND_DEVICES ||
//                            devBoundType == DevBoundType.LOCAL_DEVICES) {
//                        //sn、我绑定的、本地的
//                        showDeviceDetailDialog(position, bean);
//                    } else if (devBoundType != DevBoundType.MY_DEVICES && devBoundType != DevBoundType.SHARED_DEVICES && bean.getType() != 2) {
//                        //设备
////                        gotoDevicePager(position, bean);
//                        showDeviceDetailDialog(position, bean);
//                    } */ else {
//            //终端
////                        gotoDevicePager(position, bean);
//            showDeviceDetailDialog(position, bean)
//        }
    }

    private var mLoginDeviceDisposable: Disposable? = null
    private fun loginDevice(dev: DeviceBean, position: Int, deviceId: String) {
        setStatus(BaseActivity.LoadingStatus.ACCESS_NAS)
        showNasLoading(R.string.connecting)
        val timerDisposable = Observable.timer(15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(RxLife.`as`(this))
                .subscribe { aLong: Long? -> showNasLoading(R.string.slow_request_wait_loading) }
        val timer60Disposable = Observable.timer(60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(RxLife.`as`(this))
                .subscribe { aLong: Long? ->
                    if (mLoginDeviceDisposable != null) {
                        mLoginDeviceDisposable!!.dispose()
                    }
                }
        mLoginDeviceDisposable = mDeviceViewModel!!.toLogin(dev, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose {
                    timerDisposable.dispose()
                    timer60Disposable.dispose()
                    setStatus(BaseActivity.LoadingStatus.DEFUALT)
                    dismissLoading()
                }
                .`as`(RxLife.`as`(this))
                .subscribe({ (status, data) ->
                    timerDisposable.dispose()
                    timer60Disposable.dispose()
                    setStatus(BaseActivity.LoadingStatus.DEFUALT)
                    dismissLoading()
                    if (status === Status.SUCCESS) {
                        startNasActivity(deviceId)
                    } else {
                        try {
                            val data = data!!
                            val nasV3 = instance.isNasV3(dev.id)
                            if (nasV3) {
                                if (dev.isOwner && (data.code == HttpErrorNo.ERR_ONESERVER_HDERROR
                                                || data.code == V5_ERR_DISK_FORMATTING
                                                || data.code == V5_ERR_DISK_BUILDING)) {
                                    startActivity(Intent(requireContext(), DiskSpaceActivity::class.java)
                                            .putExtra(AppConstants.SP_FIELD_DEVICE_ID, dev.id)
                                    )
                                } else {
                                    ToastHelper.showLongToast(HttpErrorNo.getResultMsg(data.code, data.msg))
                                }
                            } else {
                                if (data.code == HttpErrorNo.ERR_ONESERVER_HDERROR
                                        && data.msg.toInt() > 0) {
                                    openHDManagerView(deviceId, data.msg)
                                } else {
                                    ToastHelper.showLongToast(HttpErrorNo.getResultMsg(data.code, data.msg))
                                }
                            }
                        } catch (ignore: Exception) {
                            ignore.printStackTrace()
                            ToastHelper.showLongToast(R.string.tip_device_status_exp)
                        }
                        if (myDeviceAdapter != null) {
                            val item = myDeviceAdapter!!.getItem(position)
                            if (item != null) {
                                myDeviceAdapter!!.notifyItemChanged(position, arrayListOf(1))
                            }
                        }
                    }
                }) { throwable ->
                    timerDisposable.dispose()
                    timer60Disposable.dispose()
                    setStatus(BaseActivity.LoadingStatus.DEFUALT)
                    dismissLoading()
                    Timber.e(throwable)
                    if (throwable is SocketTimeoutException) {
                        ToastHelper.showLongToast(R.string.tip_request_timeout)
                    } else {
                        ToastHelper.showLongToast(R.string.tip_device_status_exp)
                    }
                }
    }

    private fun startNasActivity(deviceId: String) {
        if (isAdded) {
            val intent = Intent(context, V2NasDetailsActivity::class.java)
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
            startActivity(intent)
            Logger.LOGD(this, "openNasDetails :$deviceId")
        } else {
            Timber.d("startNasActivity : failed")
        }
    }

    private var isOpenHDManage = false
    private fun openHDManagerView(devID: String, countNum: String) {
        if (!isOpenHDManage) {
            isOpenHDManage = true
            val intent = Intent(requireContext(), HdManageActivity::class.java)
            intent.putExtra("count", countNum)
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devID)
            startActivityForResult(intent, AppConstants.REQUEST_CODE_HD_FORMAT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQUEST_CODE_HD_FORMAT) {
            isOpenHDManage = false
            mAccessDeviceTool?.resetOpenHDManageValue()
        }
    }

    fun showNasLoading(resId: Int) {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)!!.showLoading(resId)
        }
    }

    override fun dismissLoading() {
        if (activity is BaseActivity) {
            (activity as BaseActivity?)!!.dismissLoading()
        }
    }

    override fun onDevUpdate(devBoundType: Int) {
        if (this.devBoundType == devBoundType) {
            if (DevManager.getInstance().isInitting &&
                    isObserverRefresh(devBoundType)) {
                refresh(false)
            }
            initBeans()
            refreshDeviceView()
        }
    }

    private fun isObserverRefresh(devBoundType: Int): Boolean {
        return devBoundType == DevBoundType.MY_DEVICES || devBoundType == DevBoundType.SHARED_DEVICES || devBoundType == DevBoundType.ALL_BOUND_DEVICES
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initBeans() {
        beans.clear()
        when (devBoundType) {
            DevBoundType.IN_THIS_NET -> beans.addAll(DevManager.getInstance().adapterDevices)
            DevBoundType.MY_DEVICES -> beans.addAll(DevManager.getInstance().myAdapterDevices)
            DevBoundType.SHARED_DEVICES -> beans.addAll(DevManager.getInstance().sharedAdapterDevices)
            DevBoundType.ALL_BOUND_DEVICES -> beans.addAll(DevManager.getInstance().allBoundAdapterDevices)
            DevBoundType.LOCAL_DEVICES -> beans.addAll(DevManager.getInstance().localDevices)
        }
        viewModel.startGetDeviceBriefs(beans)

        //        } else {
//            switch (devBoundType) {
//                case DevBoundType.IN_THIS_NET:
//                    beans.addAll(DevManager.getInstance().getAdapterDevices(false));
//                    break;
//                case DevBoundType.MY_DEVICES:
//                    beans.addAll(DevManager.getInstance().getMyAdapterDevices(false));
//                    break;
//                case DevBoundType.SHARED_DEVICES:
//                    beans.addAll(DevManager.getInstance().getSharedAdapterDevices(false));
//                    break;
//            }
//        }
    }

    private fun refreshDeviceView() {
        if (beans.size == 0) {
            mStatePager!!.showEmpty().setText(R.id.tv_tips, if (devBoundType == DevBoundType.IN_THIS_NET) getString(R.string.tips_this_net_no_dev) else if (devBoundType == DevBoundType.LOCAL_DEVICES) getString(R.string.tips_local_net_no_dev) else getString(R.string.tips_no_dev))
        } else {
            mStatePager!!.showSuccess()
            myDeviceAdapter!!.setNewData(beans)
        }
    }

    //    private void gotoDevicePager(int position, DeviceBean bean) {
    //        bean.isNew = false;
    //        myDeviceAdapter.notifyDataSetChanged();
    //        switch (bean.getType()) {
    //            case 1:
    //                ToastUtils.showToast(R.string.coming_soon);
    //                break;
    //            case 2:
    //                ToastUtils.showToast(R.string.coming_soon);
    //                startActivity(new Intent(getContext(), DevClientActivity.class));
    //                break;
    //        }
    //    }
    private fun showDeviceDetailDialog(position: Int, bean: DeviceBean) {

//        if (devBoundType == DevBoundType.MY_DEVICES || devBoundType == DevBoundType.SHARED_DEVICES) {
//            bean.isNew = false;
//            myDeviceAdapter.notifyDataSetChanged();
//        }
        DeviceDialogManage.showDeviceDetailDialog(context, devBoundType, position, bean, parentFragmentManager)
    }

    fun showUpgradeDialog(device: DeviceBean) {
        val builder = AlertDialog.Builder(requireContext())
                .setMessage(R.string.update_device_version)
        builder.setPositiveButton(resources.getString(R.string.confirm)) { dialogInterface, i ->
            var result = -911
            if (!TextUtils.isEmpty(device.vip)) result = CMAPI.getInstance().deviceUpgrade(device.vip) else if (!TextUtils.isEmpty(device.priIp)) result = CMAPI.getInstance().deviceUpgrade(device.priIp)
            if (result != -911) ToastHelper.showLongToastSafe(ErrorCode.dr2String(result))
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun bindView(bindSource: View) {
        rvMyDevices = bindSource.findViewById(R.id.home_rv_devices)
        mSrlDevice = bindSource.findViewById(R.id.home_srl_device)
    }

    companion object {
        @JvmStatic
        fun newInstance(type: Int): DeviceFragment {
            return newInstance(type, null)
        }

        @JvmStatic
        fun newInstance(type: Int, hasTypeTitle: Boolean? = null): DeviceFragment {
            val args = Bundle()
            args.putInt("devBoundType", type)
            if (hasTypeTitle != null) {
                args.putBoolean("hasTypeTitle", hasTypeTitle)
            }
            val fragment = DeviceFragment()
            fragment.arguments = args
            return fragment
        }
    }
}