package net.linkmate.app.ui.nas.files

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.util.SparseArray
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.layout_fragment_nav_cloud.*
import kotlinx.android.synthetic.main.layout_refresh_view.*
import net.linkmate.app.R
import net.linkmate.app.ui.nas.cloud.QuickBaseCloudFragment
import net.linkmate.app.ui.viewmodel.SystemMessageViewModel
import net.sdvn.nascommon.BaseNavFileFragment
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.BackHandledInterface
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.MangerBarInterface
import net.sdvn.nascommon.iface.OnBackPressedListener
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

class CloudVPFragment : BaseNavFileFragment<OneOSFileType, OneOSFile>(),
        MangerBarInterface<OneOSFileType, OneOSFile>, BackHandledInterface {
    private var dialogNoDevice: Dialog? = null
    private lateinit var mFragmentTags: MutableList<String>
    private var mAdapter: NavFragmentPagerAdapter? = null
    private lateinit var mSparseArray: SparseArray<String>
    private var onBackPressedListenerRef: WeakReference<OnBackPressedListener>? = null
    private val deviceViewModel by activityViewModels<DeviceViewModel>()
    private var isFirst = true
    private val messageViewModel by activityViewModels<SystemMessageViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFragmentTags = mutableListOf()
        mSparseArray = SparseArray()
        deviceViewModel.liveDevices.observe(this, Observer { deviceModels -> notifyDataChanged(deviceModels) })
        isFirst = true
        messageViewModel.observerMessageInit()
    }

    private fun notifyDataChanged(deviceModels: List<DeviceModel>?) {
        if (deviceModels != null) {
            val fragmentTags: MutableList<String> = ArrayList()
            val parseArray: SparseArray<String> = SparseArray()
            for (deviceModel in deviceModels) {
                if (deviceModel.isOnline && deviceModel.isEnable && deviceModel.isEnableUseSpace) {
                    val devId = deviceModel.devId
                    fragmentTags.add(devId)
                    parseArray.put(fragmentTags.size - 1, devId)
                }
            }
            if (fragmentTags.isNotEmpty()) {
                if (mAdapter != null) {
                    if (!(fragmentTags.containsAll(mFragmentTags)
                                    && mFragmentTags.containsAll(fragmentTags))) {
                        mFragmentTags.clear()
                        mSparseArray.clear()
                        mFragmentTags.addAll(fragmentTags)
                        mSparseArray = parseArray
                        mAdapter!!.replaceData(mFragmentTags)
                        showRefreshView(false)
                        if (isFirst) {
                            isFirst = false
                            if (isResumed) {
                                switchToSelected()
                            }
                        }
                    }
                }
                dialogNoDevice?.dismiss()
            } else {
                if (dialogNoDevice == null) {
                    dialogNoDevice = DialogUtils.showNotifyDialog(requireContext(), null,
                            getString(R.string.tip_wait_for_service_connect), getString(R.string.confirm))
                    { _, _ ->
                        activity?.finish()
                    }
                }
            }
        } else {
            dialogNoDevice?.dismiss()
            showRefreshView(true)
        }
    }

    override fun onStart() {
        super.onStart()
        Logger.LOGD(TAG, ">>>>>>>>>>onStart<<<<<<<<<<<<<")

    }

    override fun onStop() {
        super.onStop()
        val childFragmentManager = childFragmentManager
        val backStackEntryCount = childFragmentManager.backStackEntryCount
        Logger.LOGD(TAG, ">>>>>>>>>>onStop<<<<<<<<<<<<<$backStackEntryCount")
//        mAdapter?.replaceData(null)
    }


    override fun onResume() {
        super.onResume()
        if (deviceViewModel.liveDevices.value.isNullOrEmpty()) {
            deviceViewModel.updateDevices(null)
            showRefreshView(true)
        } else if (mAdapter?.data.isNullOrEmpty()) {
            mAdapter?.replaceData(mFragmentTags)
            showRefreshView(false)
        }
        Logger.LOGD(TAG, ">>>>>>>>On Resume>>>>>>>")
        //        notifyDataChanged(deviceViewModel.getLiveDevices().getValue());
        switchToSelected()
    }

    private fun switchToSelected() {
        arguments?.let {
            val devId = it.getString(AppConstants.SP_FIELD_DEVICE_ID)
            val path = it.getString(AppConstants.SP_FIELD_DEVICE_PATH)
            val type = it.getSerializable(AppConstants.SP_FIELD_FILE_TYPE)

            Logger.LOGD(TAG, ">>>>>>>>switchToSelected  devId : $devId")

            val currentItem = fragment_nav_vp.currentItem
            if (fragment_nav_vp != null && !TextUtils.isEmpty(devId) && mFragmentTags.size > 0) {
                var j = -1
                for (i in mFragmentTags.indices) {
                    if (devId == mFragmentTags[i]) {
                        j = i
                        break
                    }
                }
                Logger.LOGD(TAG, "switchToSelected mFragments.size " + mFragmentTags.size)
                if (j >= 0 && currentItem >= 0) {
                    it.putString(AppConstants.SP_FIELD_DEVICE_ID, "")
                    isFirst = false
                    fragment_nav_vp?.setCurrentItem(j, false)
                    if (!path.isNullOrEmpty()) {
                        Timber.d("share>> vp path : $path")
                        val args = Bundle()
                        args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
                        args.putString(AppConstants.SP_FIELD_DEVICE_PATH, path)
                        args.putSerializable(AppConstants.SP_FIELD_FILE_TYPE, type)
                        it.putString(AppConstants.SP_FIELD_DEVICE_PATH, "")
                        it.putSerializable(AppConstants.SP_FIELD_FILE_TYPE, null)
//                        mAdapter?.currentPrimaryItem?.let { fragment ->
//                            if (fragment.isResumed) {
//                                if (fragment is BaseFragment){
//                                    if (fragment.devId == devId){
//                                        fragment.arguments =args
//                                    }
//                                }
//                            } else {
//                                Timber.d("share>>vp apply by setArgs path : $path")
//                                fragment.arguments = args
//                            }
//
//                        } ?: kotlin.run {
//                            Timber.d("share>>vp apply by eventbus path : $path")
//                            EventBus.getDefault().postSticky(args)
//                        }
                        Timber.d("share>>vp apply by eventbus path : $path")
                        EventBus.getDefault().postSticky(args)
                    }
                }
            }
        }
    }

    fun remove(fragment: QuickBaseCloudFragment) {
        childFragmentManager.beginTransaction() //通过碎片管理器对象得到碎片事务对象
                .remove(fragment)//删除指定的Fragment
                .commit()//提交事务
    }

    override fun getLayoutResId(): Int {
        return R.layout.layout_fragment_nav_cloud
    }

    override fun initView(view: View) {
        showRefreshView(true)
        mAdapter = NavFragmentPagerAdapter(childFragmentManager)
        fragment_nav_vp?.adapter = mAdapter
        fragment_nav_vp?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (isResumed && !isFirst) {
                    val devId = mFragmentTags[position]
//                    val loginSession = SessionManager.getInstance().getLoginSession(devId)
//                    if (loginSession != null) {
//                        LoginManage.getInstance().setLoginSession(loginSession)
//                    }
//                    if (!devId.isNullOrEmpty()) {
//                        arguments?.let {
//                            it.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
//                        }
//                    }
                    SessionManager.getInstance().setSelectDeviceModel(devId)
                    //选择通知
//                    EventBus.getDefault().postSticky(DevSelectedEvent(devId))
                }
                Logger.LOGD(TAG, "currentItem:" + fragment_nav_vp!!.currentItem)
                Logger.LOGD(TAG, "currentPosition:$position")
                val next = position + 1
                if (next < mFragmentTags.count()) {
                    val devId2 = mFragmentTags[next]
                    if (!devId2.isNullOrEmpty()) {
                        SessionManager.getInstance().getLoginSession(devId2, object : GetSessionListener(false) {
                            override fun onSuccess(url: String?, loginSession: LoginSession?) {

                            }
                        })
                    }
                }
                val pre = position - 1
                if (pre > 0) {
                    val devId2 = mFragmentTags[pre]
                    if (!devId2.isNullOrEmpty()) {
                        SessionManager.getInstance().getLoginSession(devId2, object : GetSessionListener(false) {
                            override fun onSuccess(url: String?, loginSession: LoginSession?) {

                            }
                        })
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        iv_loading_cancel.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.LOGD(TAG, ">>>>>>>>>>onPause<<<<<<<<<<<<<")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.LOGD(TAG, ">>>>>>>>>>onHiddenChanged<<<<<<<<<<<<<$hidden")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Logger.LOGD(TAG, ">>>>>>>>onConfigurationChanged>>>>>>>$newConfig")
    }

    override fun onBackPressed(): Boolean {
        if (mAdapter != null) {
            val fragment = mAdapter!!.currentPrimaryItem
            if (fragment is OnBackPressedListener && fragment.isResumed) {
                return (fragment as OnBackPressedListener).onBackPressed()
            }
        }
        return false
    }

    private fun autoPullToRefresh() {
        if (mAdapter != null) {
            val fragment = mAdapter!!.currentPrimaryItem
            if (fragment is QuickBaseCloudFragment && fragment.isAdded)
                fragment.autoPullToRefresh()
        }
    }

//    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
//        if (mAdapter != null) {
//            val fragment = mAdapter?.currentPrimaryItem
//            if (fragment is BaseFragment) {
//                fragment.onNetworkChanged(isAvailable, isWifiAvailable)
//            }
//        }
//    }

    override fun onStatusConnection(statusCode: Int) {
        super.onStatusConnection(statusCode)
        if (statusCode == NetworkStateManager.STATUS_CODE_ESTABLISHED) {
            showRefreshView(false)
            iv_loading_cancel.isVisible = false
            messageViewModel.observerMessageInit()
            fragment_nav_vp?.isEnableScroll = true
        } else {
            messageViewModel.clearMsgModelLiveData()
            showRefreshView(true)
            iv_loading_cancel.isVisible = true
            tv_loading_tip.setText(R.string.tip_wait_for_service_connect)
            fragment_nav_vp?.isEnableScroll = false
        }

    }

    private fun showRefreshView(visibility: Boolean) {
        include_refresh_view.isVisible = false
    }

    override fun showManageBar(isShown: Boolean) {
        fragment_nav_vp?.isEnableScroll = !isShown
    }


    override fun setSelectedFragment(onBackPressedListener: OnBackPressedListener) {
        this.onBackPressedListenerRef = WeakReference(onBackPressedListener)
    }

    companion object {

        private val TAG = CloudVPFragment::class.java.simpleName
    }
}