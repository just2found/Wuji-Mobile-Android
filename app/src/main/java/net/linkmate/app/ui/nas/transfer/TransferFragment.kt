package net.linkmate.app.ui.nas.transfer

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rxjava.rxlife.RxLife
import kotlinx.android.synthetic.main.fragment_nas_transfer.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_rv.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.viewmodel.DevSelectViewModel
import net.linkmate.app.ui.viewmodel.TransmissionViewModel
import net.linkmate.app.util.Dp2PxUtils
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.ILoadingCallback
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.view.libwidget.setOnRefreshWithTimeoutListener
import java.io.File
import java.util.*

class TransferFragment : TipsBaseFragment(), View.OnClickListener {
    lateinit var adapter: QuickTransmissionAdapter
    private var isDownload: Boolean = false
    private val deviceViewModel by viewModels<DeviceViewModel>()
    private val mDownloadData = mutableListOf<TransferEntity>()
    private val mUploadData = mutableListOf<TransferEntity>()
    private val mAllDownloadData = mutableListOf<TransferEntity>()
    private val mAllUploadData = mutableListOf<TransferEntity>()
    private val viewModel by viewModels<TransmissionViewModel>()
    private val devViewModel by viewModels<DevSelectViewModel>()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tab_left_bg -> selectTab(true)
            R.id.tab_right_bg -> selectTab(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getTransferHistoryLiveDataPaged(true, null)
                .observe(this, Observer<List<TransferEntity>?> { t ->
                    swipe_refresh_layout?.isRefreshing = false
                    dismissLoading()
                    mAllDownloadData.clear()
                    t?.let {
                        mAllDownloadData.addAll(it)
                    }
                    notifyFilterChanged(TYPE_DOWN)
                })
        viewModel.getTransferHistoryLiveDataPaged(false, null)
                .observe(this, Observer<List<TransferEntity>?> { t ->
                    swipe_refresh_layout?.isRefreshing = false
                    dismissLoading()
                    mAllUploadData.clear()
                    t?.let {
                        mAllUploadData.addAll(it)
                    }
                    notifyFilterChanged(TYPE_UP)
                })
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_nas_transfer
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

        adapter = QuickTransmissionAdapter(recycle_view.context)
        recycle_view.layoutManager = LinearLayoutManager(recycle_view.context)
//        recycle_view.addItemDecoration(FloatDecoration(R.layout.item_line_string))
//        recycle_view.addOnItemTouchListener(SwipeItemLayout.OnSwipeItemTouchListener(recycle_view.context))
        val emptyView = LayoutInflater.from(recycle_view!!.context)
                .inflate(R.layout.layout_empty_directory, null)
        recycle_view.setBackgroundColor(resources.getColor(R.color.color_bg_grey50))
        adapter.emptyView = emptyView
        adapter.bindToRecyclerView(recycle_view)
        adapter.setOnItemClickListener { baseQuickAdapter, view, position ->
            if (!NetworkStateManager.instance.checkNetworkWithCheckServer(true)) {
                return@setOnItemClickListener
            }
            val itemEntity = baseQuickAdapter.getItem(position)
            if (itemEntity is TransferEntity) {
                doWithEntity(itemEntity)
                adapter.notifyItemChanged(position)
                view.postDelayed({
                    adapter.refreshProcessingStatus()
                }, 100)
            }
        }
        adapter.setOnItemChildClickListener { baseQuickAdapter, view, position ->
            if (!NetworkStateManager.instance.checkNetworkWithCheckServer(true)) {
                return@setOnItemChildClickListener
            }
            val itemEntity = baseQuickAdapter.getItem(position)
            when (view.id) {
                R.id.more -> {
                    if (view.tag == R.string.clear) {
                        viewModel.deleteComplete(devId, isDownload)
                    } else {
                        val isResume = view.tag is Boolean && view.tag as Boolean
                        lifecycleScope.launchWhenResumed {
                            val result = withContext(Dispatchers.Default) {
                                resumeOrPause(isResume)
                            }
                            launch(Dispatchers.Main) {
                                if (result) {
                                    if (view is TextView) {
                                        view.setTag(!isResume)
                                    }
                                }
                                view.postDelayed({
                                    adapter.notifyDataSetChanged()
                                    adapter.refreshProcessingStatus()
                                }, 100)
                            }
                        }

                    }
                }
                R.id.txt_delete -> {
                    if (itemEntity is TransferEntity) {
                        showTipToRemove(itemEntity, view, position, Callback<Boolean> { })
                    }
                }
            }
        }

        adapter.setOnItemLongClickListener { baseQuickAdapter, view, position ->
            val item: MultiItemEntity = baseQuickAdapter.data[position] as MultiItemEntity
            if (item is TransferEntity) {
                showDesItem(item, position)
                return@setOnItemLongClickListener true
            }
            return@setOnItemLongClickListener false

        }
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            adapter.refreshProcessingStatus()
            adapter.notifyDataSetChanged()
        }, 1000)
        swipe_refresh_layout?.isRefreshing = true
        swipe_refresh_layout.isEnabled = true
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
                notifyFilterChanged(TYPE_DOWN or TYPE_UP)
                refreshTitle()
            }
        })
    }

    private fun notifyFilterChanged(type: Int) {
        if (type and TYPE_DOWN != 0) {
            filterDownloadData()
            if (isDownload) {
                adapter.submit(mDownloadData)
            }
        }
        if (type and TYPE_UP != 0) {
            filterUpdateData()
            if (!isDownload) {
                adapter.submit(mUploadData)
            }
        }
    }

    private fun filterUpdateData() {
        mUploadData.clear()
        mUploadData.addAll(mAllUploadData.filter {
            if (devId.isNullOrEmpty()) {
                true
            } else {
                it.devId == devId
            }
        })
    }

    private fun filterDownloadData() {
        mDownloadData.clear()
        mDownloadData.addAll(mAllDownloadData.filter {
            if (devId.isNullOrEmpty()) {
                true
            } else {
                it.devId == devId
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
                    clearTransferType(TransferState.FAILED)
                }
            }
        }
        val clearRunning = contextView.findViewById<TextView>(R.id.clear_all)
        clearRunning.setOnClickListener {
            sheetDialog.dismiss()
            DialogUtils.showConfirmDialog(view.context, "", clearRunning.text.toString(),
                    getString(R.string.confirm), getString(R.string.cancel)) { _, isPositiveBtn ->
                if (isPositiveBtn) {
                    clearTransferType(TransferState.PAUSE)
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

    private fun clearTransferType(state: TransferState) {
        var size: Int
        if (isDownload) {
            if (state == TransferState.FAILED) {
                val tags = mDownloadData.filter { it.state() == TransferState.FAILED }
                        .map { it.tag }.also { size = it.count() }
                SessionManager.getInstance().service.cancelDownload(tags)
            } else {
                val tags = mDownloadData.map { it.tag }.also { size = it.count() }
                SessionManager.getInstance().service.cancelDownload(tags)
            }
        } else {
            if (state == TransferState.FAILED) {
                val tags = mUploadData.filter { it.state() == TransferState.FAILED }
                        .map { it.tag }.also { size = it.count() }
                SessionManager.getInstance().service.cancelUpload(tags)
            } else {
                val tags = mUploadData.map { it.tag }.also { size = it.count() }
                SessionManager.getInstance().service.cancelUpload(tags)
            }
        }
        if (size > 0) {
            showLoading()
        }
    }

    private fun showLoading() {
        val requireActivity = requireActivity()
        if (requireActivity is ILoadingCallback) {
            requireActivity.showLoading()
        }
    }

    private fun dismissLoading() {
        val requireActivity = requireActivity()
        if (requireActivity is ILoadingCallback) {
            requireActivity.dismissLoading()
        }
    }

    private fun showTipToRemove(itemEntity: TransferEntity, view: View, position: Int, callback: Callback<Boolean>) {
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
                if (removeEntity(itemEntity)) {
//                    viewModel.notifyChanged(isDownload, true)
                    if (Objects.equals(itemEntity, adapter.getItem(position))) {
                        adapter.notifyItemRemoved(position)
                    }
                }
                if (isChecked) {
                    val file = File(itemEntity.toPath + File.separator +
                            (if (itemEntity.isComplete()) itemEntity.fileName else itemEntity.tmpName))
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


    private fun showDesItem(entity: TransferEntity, position: Int) {
        val viewHolder = ViewHolder(requireContext(), entity)
        val dialog = DialogUtils.showCustomDialog(context, viewHolder.view)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnDismissListener {
            viewHolder.clean()
            adapter.refreshProcessingStatus()
            adapter.notifyItemChanged(position)
        }
        viewHolder.negative.setOnClickListener {
//            if (!entity.isComplete()) {
//                showTipToRemove(entity, it, position, Callback<Boolean> { t ->
//                    if (t) {
//                        dialog.dismiss()
//                    }
//                })
//            } else {
            dialog.dismiss()
//            }
        }
        viewHolder.positive.setOnClickListener {
//            if (!entity.isComplete()) {
//                doWithEntity(entity)
//                viewHolder.refreshUi()
//            } else {
            showTipToRemove(entity, it, position, Callback<Boolean> { t ->
                if (t) {
                    dialog.dismiss()
                }
            })
//            }
        }
    }

    private fun resumeOrPause(isResume: Boolean): Boolean {
        try {
            if (devId.isNullOrEmpty()) {
                if (isDownload) {
                    if (isResume)
                        SessionManager.getInstance().service.continueDownload()
                    else
                        SessionManager.getInstance().service.pauseDownload()
                } else {
                    if (isResume)
                        SessionManager.getInstance().service.continueUpload()
                    else
                        SessionManager.getInstance().service.pauseUpload()
                }
                return true
            } else {
                if (isDownload) {
                    val tags = mDownloadData.map { it.tag }
                    if (isResume) {
                        SessionManager.getInstance().service.continueDownload(tags)
                    } else {
                        SessionManager.getInstance().service.pauseDownload(tags)
                    }
                } else {
                    val tags = mUploadData.map { it.tag }
                    if (isResume) {
                        SessionManager.getInstance().service.continueUpload(tags)
                    } else {
                        SessionManager.getInstance().service.pauseUpload(tags)
                    }
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    private fun removeEntity(transferEntity: TransferEntity): Boolean {
        val tag = transferEntity.tag
        val result = transferEntity.getManager().cancel(tag)
        if (result == -1) {
            return TransferHistoryKeeper.delete(transferEntity.id)
        }
        return true
    }


    private fun doWithEntity(itemEntity: TransferEntity) {
        when (itemEntity.state()) {
            TransferState.FAILED,
            TransferState.NONE,
            TransferState.PAUSE -> itemEntity.getManager().resume(itemEntity.tag)
            TransferState.WAIT,
            TransferState.START -> itemEntity.getManager().pause(itemEntity.tag)
            TransferState.COMPLETE -> {
                openItem(itemEntity)
            }
            else -> Logger.LOGD(TAG, itemEntity.tag)
        }
    }

    private fun openItem(itemEntity: TransferEntity) {
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
        if (isLeft == isDownload) {
            isDownload = !isLeft
            adapter.setTransferList(isDownload)
            if (isDownload) {
                adapter.submit(mDownloadData)
            } else {
                adapter.submit(mUploadData)
            }
        }

    }

    companion object {
        fun newInstance(deviceId: String? = null): TransferFragment {
            val fragment = TransferFragment()
            if (deviceId.isNullOrEmpty()) {
                return fragment
            }
            val args = Bundle()
            args.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
            fragment.arguments = args
            return fragment
        }

        val TAG: String = TransferFragment::class.java.simpleName
        private const val TYPE_DOWN = 1 shl 1
        private const val TYPE_UP = 1 shl 2
    }

}
