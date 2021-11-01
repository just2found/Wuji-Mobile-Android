package net.linkmate.app.ui.nas.transfer_r

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rxjava.rxlife.RxLife
import kotlinx.android.synthetic.main.fragment_nas_transfer.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_rv.*
import libs.source.common.utils.RateLimiter
import libs.source.common.utils.ThreadUtils
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.transfer_r.QuickTransmissionAdapterR.Companion.COMPLETED_TITLE
import net.linkmate.app.ui.nas.transfer_r.QuickTransmissionAdapterR.Companion.PROGRESS_TITLE
import net.linkmate.app.ui.viewmodel.DevSelectViewModel
import net.linkmate.app.util.Dp2PxUtils
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.transfer_r.DataRefreshEvent
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.CallBack
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.view.libwidget.setOnRefreshWithTimeoutListener
import java.io.File
import java.util.concurrent.TimeUnit

class TransferFragmentR : TipsBaseFragment(), View.OnClickListener {

    private var isDownload: Boolean = false
    private val deviceViewModel by viewModels<DeviceViewModel>()
    private val devViewModel by viewModels<DevSelectViewModel>()
    private val transmissionViewModel by viewModels<TransmissionViewModelR>() //传输数据的控制类

    private val mRateLimiter = RateLimiter<Any>(1500, TimeUnit.MILLISECONDS) //点击加间隔

    private val mQuickTransmissionAdapterR by lazy {
        QuickTransmissionAdapterR(transmissionViewModel.mMultiItemEntityList, requireActivity()).apply {
            setOnItemClickListener { baseQuickAdapter, view, position ->
                if (!NetworkStateManager.instance.checkNetworkWithCheckServer(true)) {
                    return@setOnItemClickListener
                }
                val item = baseQuickAdapter.getItem(position)

                if (item is TransferEntityR) {
                    if (mRateLimiter.shouldFetch(item)) {
                        if (!item.isComplete) {
                            transmissionViewModel.changeTransferStatus(item, position)
                        } else {
                            openItem(item)
                        }
                    }
                }
            }

            setOnItemChildClickListener { baseQuickAdapter, view, position ->
                if (!NetworkStateManager.instance.checkNetworkWithCheckServer(true)) {
                    return@setOnItemChildClickListener
                }
                val item = baseQuickAdapter.getItem(position)
                if (item is HeaderEntityR) {
                    if (mRateLimiter.shouldFetch(item)) {
                        if (item.itemType == PROGRESS_TITLE) {
                            transmissionViewModel.changeTransferStatus(item)
                            notifyDataSetChanged()
                        } else if (item.itemType == COMPLETED_TITLE) {
                            transmissionViewModel.deleteComplete()
                        }
                    }
                }
            }

            setOnItemLongClickListener { baseQuickAdapter, view, position ->
                val item = baseQuickAdapter.getItem(position)
                if (item is TransferEntityR) {
                    showDesItem(item, position)
                    return@setOnItemLongClickListener true
                }
                true
            }
            //设置加载更多的操作
            setOnLoadMoreListener({
                transmissionViewModel.loadMoreData()
            }, recycle_view)

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tab_left_bg -> selectTab(true)
            R.id.tab_right_bg -> selectTab(false)
        }
    }


    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_nas_transfer
    }

    override fun getTopView(): View? {
        return layout_title
    }

    override fun initView(view: View) {
        this.mTipsBar = tipsBar
        refreshTitle()
        layout_title.mBackTxt.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        layout_title.mBackTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        layout_title.setOnClickBack(requireActivity())
        layout_title.setRightButton(R.drawable.ic_more_vert_black_24dp)
                .setRightButtonVisible(View.VISIBLE)
                .setOnRightClickListener {
                    if (Utils.isFastClick(it, 800)) {
                        return@setOnRightClickListener
                    }
                    showBottomDialog(view)
                }
        layout_title.mRightIBtn1.setPadding(Dp2PxUtils.dp2px(requireContext(), 14))
        layout_title.setRightButton1(R.drawable.icon_filter_white)
                .setRightButton1Visible(View.VISIBLE)
                .setOnRight1ClickListener {
                    showMenuPopup(it)
                }
        selectTab(!isDownload)
        tab_left_bg.setOnClickListener(this)
        tab_right_bg.setOnClickListener(this)


        recycle_view.layoutManager = LinearLayoutManager(recycle_view.context)
        val emptyView = LayoutInflater.from(recycle_view!!.context)
                .inflate(R.layout.layout_empty_directory, null)
        recycle_view.setBackgroundColor(resources.getColor(R.color.color_bg_grey50))

        mQuickTransmissionAdapterR.emptyView = emptyView
        recycle_view.adapter = mQuickTransmissionAdapterR
        //这个是去掉recycle_view的刷新 及插入删除动画
        recycle_view.getItemAnimator()?.setAddDuration(0)
        recycle_view.getItemAnimator()?.setChangeDuration(0)
        recycle_view.getItemAnimator()?.setMoveDuration(0)
        recycle_view.getItemAnimator()?.setRemoveDuration(0)
        (recycle_view.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)

        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            transmissionViewModel.reLoadData()
        }, 1000)
        swipe_refresh_layout.isEnabled = true
        transmissionViewModel.setRepositoryOperationParameter(devId
                ?: TransmissionViewModelR.ALL_DEVICE, isDownload)
        transmissionViewModel.initTransmissionLiveData()
        transmissionViewModel.reLoadData()

    }

    private var flag = true

    override fun onResume() {
        super.onResume()
        if (!flag) {
            mQuickTransmissionAdapterR.notifyDataSetChanged()
            flag = true
        }
    }

    override fun onPause() {
        super.onPause()
        flag = false
    }

    override fun onStart() {
        super.onStart()
        transmissionViewModel.callBack = object : CallBack<DataRefreshEvent> {
            override fun onCallBack(dataRefreshEvent: DataRefreshEvent) {
                if (!flag)
                    return
                ThreadUtils.ensureRunOnMainThread {
                    when (dataRefreshEvent.refreshType) {
                        Repository.NO_MORE_DATA -> {
                            swipe_refresh_layout?.isRefreshing = false
                            mQuickTransmissionAdapterR.loadMoreComplete()
                            mQuickTransmissionAdapterR.setEnableLoadMore(false)
                        }
                        Repository.SOURCE_DATA -> {
                            mQuickTransmissionAdapterR.setNewData(transmissionViewModel.mMultiItemEntityList)
                            mQuickTransmissionAdapterR.loadMoreComplete()
                            mQuickTransmissionAdapterR.setEnableLoadMore(true)
                            swipe_refresh_layout?.isRefreshing = false
                        }
                        Repository.UPDATE_DATA -> {
                            if (mQuickTransmissionAdapterR.data.size < dataRefreshEvent.startPosition + dataRefreshEvent.itemCount) {
                                mQuickTransmissionAdapterR.notifyDataSetChanged()
                            } else {
                                mQuickTransmissionAdapterR.notifyItemRangeChanged(dataRefreshEvent.startPosition, dataRefreshEvent.itemCount)
                                mViewHolder?.let { holder ->
                                    if (dataRefreshEvent.itemCount == 1) {
                                        holder.refreshUi()
                                    }
                                }
                            }
                        }
                        Repository.INSERT_DATA -> {
                            if (mQuickTransmissionAdapterR.data.size < dataRefreshEvent.startPosition + dataRefreshEvent.itemCount) {
                                mQuickTransmissionAdapterR.notifyDataSetChanged()
                            } else {
                                mQuickTransmissionAdapterR.notifyItemRangeInserted(dataRefreshEvent.startPosition, dataRefreshEvent.itemCount)
                            }
                            mQuickTransmissionAdapterR.loadMoreComplete()
                        }
                        Repository.DELETE_DATA -> {
                            mQuickTransmissionAdapterR.notifyItemRangeRemoved(dataRefreshEvent.startPosition, dataRefreshEvent.itemCount)
                        }
                        Repository.REFRESH_DATA -> {
                            mQuickTransmissionAdapterR.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }


    private fun refreshTitle() {
        if (devId.isNullOrEmpty()) {
            layout_title.setBackTitle(resources.getString(R.string.all))
        } else {
            deviceViewModel.refreshDevNameById(devId!!)
                    .`as`(RxLife.`as`(this))
                    .subscribe({
                        layout_title.setBackTitle(it)
                    }, { t: Throwable? -> })
        }
    }


    private fun showMenuPopup(view: View) {
        devId = devId ?: ""
        devViewModel.showNasSelect(requireActivity(), view, devId!!, Callback {
            if (it != devId) {
                devId = it
                mQuickTransmissionAdapterR.setEnableLoadMore(false)
                transmissionViewModel.setRepositoryOperationParameter(deviceID = it)
                transmissionViewModel.reLoadData()
                refreshTitle()
            }
        })
    }


    private fun showBottomDialog(view: View) {
        val sheetDialog = BottomSheetDialog(view.context)
        val contextView = LayoutInflater.from(view.context).inflate(R.layout.layout_transfer_operator, null)
        val clearFailed = contextView.findViewById<TextView>(R.id.clear_failed)
        clearFailed.setOnClickListener {
            sheetDialog.dismiss()
            DialogUtils.showConfirmDialog(view.context, "", clearFailed.text.toString(),
                    getString(R.string.confirm), getString(R.string.cancel)) { _, isPositiveBtn ->
                if (isPositiveBtn) {
                    transmissionViewModel.clearTransferFailed()
                }
            }
        }
        val clearRunning = contextView.findViewById<TextView>(R.id.clear_all)
        clearRunning.setOnClickListener {
            sheetDialog.dismiss()
            DialogUtils.showConfirmDialog(view.context, "", clearRunning.text.toString(),
                    getString(R.string.confirm), getString(R.string.cancel)) { _, isPositiveBtn ->
                if (isPositiveBtn) {
                    transmissionViewModel.clearTransferProgress()
                }
            }
        }
        val cancel = contextView.findViewById<TextView>(R.id.cancel)
        cancel.setOnClickListener {
            sheetDialog.dismiss()
        }
        sheetDialog.setContentView(contextView)
        sheetDialog.show()
    }


    //TODO这个是长按弹出删除
    private fun showTipToRemove(itemEntity: TransferEntityR, view: View, position: Int, callback: Callback<Boolean>) {
        val resIdTitle = if (itemEntity.isDownload) {
            R.string.confirm_cancel_download
        } else {
            R.string.confirm_cancel_upload
        }
        val resIdTips = if (itemEntity.isDownload) {
            R.string.cancel_and_delete_local
        } else {
            0
        }
        DialogUtils.showCheckDialog(view.context, resIdTitle,
                resIdTips, R.string.confirm, R.string.cancel
        ) { isPositiveBtn, isChecked ->
            if (isPositiveBtn) {
                transmissionViewModel.removeEntity(position)
                if (isChecked) {
                    val file = File(itemEntity.toPath + File.separator +
                            (if (itemEntity.isComplete) itemEntity.fileName else itemEntity.tmpName))
                    if (file.exists()) {
                        if (file.delete()) {
                            Logger.LOGD(TAG, "Delete file succeed")
                        } else {
                            Logger.LOGE(TAG, "Delete file failure")
                        }
                    }
                }

            }
            callback.result(isPositiveBtn)
        }
    }

    var mViewHolder: ViewHolderR? = null

    private fun showDesItem(entity: TransferEntityR, position: Int) {
        mViewHolder = ViewHolderR(requireContext(), entity)
        val dialog = DialogUtils.showCustomDialog(context, mViewHolder!!.view)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        mViewHolder!!.negative.setOnClickListener {
            dialog.dismiss()
        }
        mViewHolder!!.positive.setOnClickListener {
            showTipToRemove(entity, it, position, Callback<Boolean> { t ->
                if (t) {
                    dialog.dismiss()
                }
            })
        }
        dialog.setOnDismissListener {
            mViewHolder = null
        }

    }

    private fun openItem(itemEntity: TransferEntityR) {
        if (itemEntity.isDownload) {
            val path = itemEntity.toPath
            if (!path.isNullOrEmpty() && !tryOpenLocalFile(path + File.separator + itemEntity.fileName)) {
                SessionManager.getInstance().getLoginSession(itemEntity.devId!!, object : GetSessionListener() {
                    override fun onSuccess(url: String?, loginSession: LoginSession?) {
                        if (loginSession != null) {
                            val genDownloadUrl = OneOSAPIs.genOpenUrl(loginSession, itemEntity.srcPath)
                            tryOpenOnlineFile(genDownloadUrl, itemEntity.fileName)

                        }
                    }
                })
            }

        } else {
            val path = itemEntity.srcPath
            if (!path.isNullOrEmpty() && !tryOpenLocalFile(path)) {
                SessionManager.getInstance().getLoginSession(itemEntity.devId!!, object : GetSessionListener() {
                    override fun onSuccess(url: String?, loginSession: LoginSession?) {
                        if (loginSession != null) {
                            val genDownloadUrl = OneOSAPIs.genOpenUrl(loginSession,
                                    itemEntity.toPath + File.separator + itemEntity.fileName)
                            tryOpenOnlineFile(genDownloadUrl, itemEntity.fileName)

                        }
                    }
                })
            }
        }
    }

    private fun tryOpenOnlineFile(genDownloadUrl: String?, fileName: String?) {
        FileUtils.show(genDownloadUrl, fileName, requireActivity())
    }

    private fun tryOpenLocalFile(path: String): Boolean {
        return FileUtils.openLocalFile(requireContext(), File(path))
    }

    private fun selectTab(isLeft: Boolean) {
        tab_left_bg.isSelected = isLeft
        tab_left_tv.isSelected = isLeft
        tab_right_bg.isSelected = !isLeft
        tab_right_tv.isSelected = !isLeft
        if (isLeft == isDownload) { //判断是否进行了切换
            isDownload = !isLeft
            mQuickTransmissionAdapterR.setEnableLoadMore(false)
            transmissionViewModel.setRepositoryOperationParameter(isDownLoad = isDownload)
            transmissionViewModel.reLoadData()
        }
    }

    companion object {
        fun newInstance(deviceId: String? = null): TransferFragmentR {
            val fragment = TransferFragmentR()
            if (deviceId.isNullOrEmpty()) {
                return fragment
            }
            val args = Bundle()
            args.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
            fragment.arguments = args
            return fragment
        }

        val TAG: String = TransferFragmentR::class.java.simpleName
    }

}
