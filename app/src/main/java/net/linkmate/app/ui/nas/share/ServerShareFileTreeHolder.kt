package net.linkmate.app.ui.nas.share

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
//import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.internal.LinkedTreeMap
import com.rxjava.rxlife.RxLife
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache
import libs.source.common.AppExecutors
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.nas.cloud.FileLoadMoreView
import net.linkmate.app.ui.nas.files.V2NasDetailsActivity
import net.linkmate.app.util.FormatUtils
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.DBHelper
import net.sdvn.nascommon.db.objecbox.SFDownload
import net.sdvn.nascommon.db.objecbox.SFDownload_
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.fileserver.FileShareBaseResult
import net.sdvn.nascommon.fileserver.constants.*
import net.sdvn.nascommon.fileserver.constants.EntityType.DownloadID.*
import net.sdvn.nascommon.fileserver.constants.FileServerTransferState.STATUS_RUNNING
import net.sdvn.nascommon.fileserver.data.DataShareDir
import net.sdvn.nascommon.fileserver.data.DataShareProgress
import net.sdvn.nascommon.fileserver.data.SFile
import net.sdvn.nascommon.fileserver.data.SFileTree
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.ConsumerThrowable
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.PathTypeCompat
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.*
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import net.sdvn.nascommon.widget.NumberProgressBar
import net.sdvn.nascommon.widget.PopDialogFragment
import net.sdvn.nascommon.widget.ServerFileTreeView
import org.greenrobot.eventbus.EventBus
import org.view.libwidget.setOnRefreshWithTimeoutListener
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


class ServerShareFileTreeHolder(private val mActivity: FragmentActivity,
        //    private FilePathPanel mPathPanel;
                                private val mShareElementV2: ShareElementV2,
                                private val mSFDownload: SFDownload?, private val mTitleID: Int, mPositiveID: Int) {
    private var onDismissListener: OnDismissListener? = null
    private val mShareViewModel2: ShareViewModel2

    private var mCurPath: String? = null
    private val popDialogFragment: PopDialogFragment
    private val mRefreshLayout: SwipeRefreshLayout?
    private var mListView: RecyclerView? = null
    private val mPasteBtn: TextView
    private val mDeviceViewModel: DeviceViewModel
    private val mObserver: Observer<List<DeviceModel>>

    //    private val mLiveData = MutableLiveData<List<DeviceModel>>()
    private val sourceId: String?
    private var toId: String? = null

    //    private String mShowDevId;
    private var mAdapter: BaseQuickAdapter<SFile, BaseViewHolder>? = null
    private val mTitleTxt: TextView?
    private var mDeviceRVAdapter: BaseQuickAdapter<DeviceModel, BaseViewHolder>? = null
    private var mToPath: String? = null
    private var pathTYpe: Int = 0
    private val mBaseViewHolder: BaseViewHolder

    private var mShareRootPath: String? = null
    private var mShareStatus: ShareStatus? = null

    private var mDownloadPaths: MutableSet<String>? = null
    private var mErrFiles: Set<SFile>? = null
    private var mDownloadPathsNum: Int = 0
    private var mErrFilesNum: Int = 0
    private var mSFileTree: SFileTree? = null
    private var mCurrents: MutableSet<SFile>? = null
    private var mCompleted: MutableSet<SFile>? = null
    private var isMultiSelectMode: Boolean = false
    private var onNextProgress: Consumer<FileShareBaseResult<DataShareProgress>>? = null
    private var devName: String? = null
    private var isDevOnline = true
    private var compositeDisposable: CompositeDisposable? = null
    private var mFilesTotal: Int = 0
    private var mFilesPage = 1
    private var subscribeIntervalTask: Disposable? = null
    private val baseQuickAdapterLoadMoerListener: BaseQuickAdapter.RequestLoadMoreListener =
            BaseQuickAdapter.RequestLoadMoreListener { getFileTreeFromServer(mCurPath, true) }

    protected fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable!!.add(disposable)
    }

    protected fun dispose() {
        if (compositeDisposable != null) compositeDisposable!!.dispose()
    }

    init {
        mShareViewModel2 = ViewModelProviders.of(mActivity).get(ShareViewModel2::class.java)
        mDeviceViewModel = ViewModelProviders.of(mActivity).get(DeviceViewModel::class.java)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.layout_popup_share_file_tree, null)
        popDialogFragment = PopDialogFragment.newInstance(true, view)
        popDialogFragment.lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                refreshProgress()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                stopIntervalTask()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestory() {
                dispose()
            }
        })
        mBaseViewHolder = BaseViewHolder(view)
        mRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        mTitleTxt = view.findViewById(R.id.txt_title)
        mPasteBtn = view.findViewById(R.id.btn_paste)
        initData()
        initView(view)

        val btnOperate = view.findViewById<ImageView>(R.id.left_btn_operate)
        btnOperate.setOnClickListener {
            if (TextUtils.isEmpty(mCurPath) || mCurPath == File.separator) {
                dismiss()
            } else {
                getFileTreeFromServer(getParentPath(mCurPath!!))
            }
        }
        btnOperate.visibility = View.VISIBLE

        popDialogFragment.addDismissListener {
            onDismissListener?.onDismiss()
            dispose()
        }

        this.sourceId = mShareElementV2.srcDevId
        mObserver = Observer { deviceModels ->
            //            mLiveData.setValue(deviceModels)
            if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
                if (mShareElementV2.downloadId != null && mShareElementV2.toDevId != null) {
                    if (deviceModels != null) {
                        isDevOnline = false
                        for (deviceModel in deviceModels) {
                            if (deviceModel.devId == mShareElementV2.toDevId) {
                                if (deviceModel.device != null && deviceModel.device!!.isOnline)
                                    isDevOnline = true
                                break
                            }
                        }
                        mDeviceRVAdapter?.let {
                            filterData(deviceModels, it)
                        }
                        if (mAdapter != null) {
                            if (isDevOnline) {
                                mAdapter!!.notifyDataSetChanged()
                                refreshProgress()
                            } else {
                                refreshDevName(Consumer { s ->
                                    val msg = popDialogFragment.getString(R.string.tip_device_offline, s)
                                    mBaseViewHolder.setText(R.id.txt_status_to_device, msg)
                                })
                            }
                            mBaseViewHolder.itemView.isEnabled = isDevOnline
                        }
                    }
                }
            }
        }
        mDeviceViewModel.liveDevices.observe(popDialogFragment, mObserver)
    }

    private fun doRefresh() {
        getFileTreeFromServer(mCurPath)
        if (!(mShareElementV2.downloadId.isNullOrEmpty() && mShareElementV2.toDevId.isNullOrEmpty())) {
            refreshProgress()
        }
    }

    private fun initData() {
        if (mShareElementV2.type == EntityType.SHARE_FILE_V2_SEND) {
            val paths = mShareElementV2.path
            if ((paths).isNullOrEmpty()) {
                ToastHelper.showToast(R.string.no_data)
                return
            }
            var pathTmp: String? = null
            for (path in paths) {
                if (!(path).isNullOrEmpty()) {
                    pathTmp = path
                    break
                }
            }
            if ((pathTmp).isNullOrEmpty()) {
                ToastHelper.showToast(R.string.no_data)
                return
            }
            mShareRootPath = if (mShareElementV2.sharePathType == 1) {
                ""
            } else {
                if (OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR != pathTmp
                        && OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR != pathTmp) {
                    if (pathTmp.endsWith(File.separator)) {
                        pathTmp.substring(0, pathTmp.lastIndexOf(File.separator) + 1)
                                .substring(0, pathTmp.lastIndexOf(File.separator) + 1)
                    } else
                        pathTmp.substring(0, pathTmp.lastIndexOf(File.separator) + 1)
                } else {
                    pathTmp
                }
            }
            Logger.LOGD(this, "share>> mShareRootPath ", mShareRootPath)
        } else if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
            if (mSFDownload != null) {
                this.toId = mSFDownload.toDevId
                mShareElementV2.toDevId = mSFDownload.toDevId
                mShareElementV2.downloadId = mSFDownload.token
                mShareElementV2.toPath = mSFDownload.toPath
                refreshDevName(null)
                if (mShareElementV2.type == EntityType.SHARE_FILE_V2_COPY) {
                    mDownloadPaths = mutableSetOf("/")
                }
            }
            onNextProgress = Consumer { data ->
                if (data.isSuccessful) {
                    val result = data.result
                    val currentSize = result.currentSize
                    val currentSizeFmt = FileUtils.fmtFileSize(currentSize)
                    val totalSize = result.totalSize
                    val totalSizeFmt = FileUtils.fmtFileSize(totalSize)
                    val resultStatus = result.status
                    val remainingSize = totalSize - currentSize
                    val progress = when {
                        resultStatus == FileServerTransferState.STATUS_COMPLETE -> 100
                        totalSize == 0L -> 0
                        else -> (currentSize * 100f / totalSize + .5).toInt()
                    }
                    val speed = FileUtils.fmtFileSpeed2(result.speed)
                    val context = mBaseViewHolder.itemView.context
                    if (!TextUtils.isEmpty(toId) && devName == null) {
                        refreshDevName(null)
                    }
                    val o = if (devName == null)
                        ""
                    else
                        getTvPathText(context,
                                if (TextUtils.isEmpty(mShareElementV2.toPath))
                                    File.separator
                                else
                                    mShareElementV2.toPath, mShareElementV2.toDevId)
                    val eta = if (result.speed > 0) FormatUtils.getUptime(remainingSize / result.speed) else "∞"
                    val strSpeed = if (isDevOnline && resultStatus == STATUS_RUNNING) "${speed}/s \t $eta" else ""
                    var status = "${currentSizeFmt}/${totalSizeFmt} \t $strSpeed "

                    var shareStatus = ShareStatus.RECEIVE
                    when (resultStatus) {
                        FileServerTransferState.STATUS_INIT -> {
                            shareStatus = ShareStatus.RECEIVING
                            mShareElementV2.state = TransferState.WAIT
                            mBaseViewHolder.getView<View>(R.id.btn_paste).isEnabled = false
                        }
                        STATUS_RUNNING -> {
                            shareStatus = ShareStatus.RECEIVING
                            mShareElementV2.state = TransferState.START
                            mBaseViewHolder.getView<View>(R.id.btn_paste).isEnabled = true
                        }
                        FileServerTransferState.STATUS_STOPPED -> {
                            shareStatus = ShareStatus.RECEIVE_PAUSE
                            mShareElementV2.state = TransferState.PAUSE
                        }
                        FileServerTransferState.STATUS_COMPLETE -> {
                            shareStatus = ShareStatus.RECEIVE
                            if (mShareElementV2.state != TransferState.COMPLETE) {
                                mShareElementV2.state = TransferState.COMPLETE
                                getDownloadInfo(SHARE_DOWNLOAD_ID_COMPLETED, mCurPath)
                                stopIntervalTaskDelay(2000)
                            }
                        }
                        FileServerTransferState.STATUS_ERROR -> {
                            shareStatus = ShareStatus.RECEIVE_PAUSE
                            status = FileServerErrorCode.getString(result.err)
                            mShareElementV2.state = TransferState.FAILED
                            stopIntervalTask()
                        }
                        FileServerTransferState.STATUS_ERROR_BY_DISK_FULL -> {
                            shareStatus = ShareStatus.RECEIVE_PAUSE
                            status = Utils.getApp().getString(R.string.server_space_insufficient)
                            mShareElementV2.state = TransferState.FAILED
                            stopIntervalTask()
                        }
                    }
                    if (!isMultiSelectMode) {
                        toggleBtnByStatus(shareStatus)
                        if (subscribeIntervalTask == null && shareStatus == ShareStatus.RECEIVING) {
                            startObservableIntervalTask()
                        }
                        mBaseViewHolder.setText(R.id.btn_operate, R.string.action_refresh)
                    }
                    mBaseViewHolder.setText(R.id.txt_status, status)
                    mBaseViewHolder.setText(R.id.txt_status_to_device, String.format("%s", o))
                    mBaseViewHolder.getView<NumberProgressBar>(R.id.progress_bar_total_status).progress = progress
                    mBaseViewHolder.setGone(R.id.layout_status, !isMultiSelectMode)
                    mBaseViewHolder.setGone(R.id.progress_bar_total_status, true)
                    mBaseViewHolder.setGone(R.id.btn_operate, totalSize == 0L || currentSize < totalSize)
                    mBaseViewHolder.setGone(R.id.group_start, mShareElementV2.state != TransferState.COMPLETE)
                    mBaseViewHolder.setGone(R.id.txt_renew, mShareElementV2.state == TransferState.COMPLETE)

                    val downloadPathNum = result.downloadPathNum
                    var change = false
                    if (mDownloadPathsNum != downloadPathNum
                            && resultStatus != FileServerTransferState.STATUS_COMPLETE) {
                        mDownloadPathsNum = downloadPathNum
                        getDownloadInfo(SHARE_DOWNLOAD_ID_PATHS, null)
                        change = true
                    }
                    val errFilesNum = result.errFilesNum
                    if (mErrFilesNum != errFilesNum
                            && resultStatus != FileServerTransferState.STATUS_COMPLETE
                            && resultStatus != FileServerTransferState.STATUS_INIT) {
                        mErrFilesNum = errFilesNum
                        getDownloadInfo(SHARE_DOWNLOAD_ID_ERR_FILES, null)
                        change = true
                    }
                    val currentFiles = result.currentFiles
                    if (mCurrents == null || currentFiles != mCurrents) {
                        getDownloadInfo(SHARE_DOWNLOAD_ID_COMPLETED, mCurPath)
                        change = true
                    }
                    mCurrents = if (resultStatus != FileServerTransferState.STATUS_COMPLETE)
                        currentFiles
                    else {
                        null
                    }
                    if (subscribeIntervalTask == null || change
                            || resultStatus == FileServerTransferState.STATUS_COMPLETE) {
                        mShareViewModel2.putToDB(mShareElementV2)
                    }
                    if ((change || resultStatus == STATUS_RUNNING) && mAdapter != null) {
                        mAdapter!!.notifyDataSetChanged()
                    }
                } else {
                    if (data.status == FileServerErrorCode.MSG_ERROR_NO_TASK) {
                        resetTask(true)
                        ToastHelper.showToast(R.string.download_removed)
                    } else if (data.status == FileServerErrorCode.MSG_ERROR_CANCEL_SHARED || data.status == FileServerErrorCode.MSG_ERROR_EXCEED_DOWNLOADS) {
                        mShareElementV2.errNo = data.status
                        mShareViewModel2.showRemoveDialog(mActivity,
                                mShareElementV2) { dismiss() }
                    }
                }
            }
        }
    }


    private fun initView(view: View) {
        mListView = view.findViewById(R.id.recycle_view)
        val layout = LinearLayoutManager(mListView!!.context)
        mListView!!.layoutManager = layout
        toggleView()
        refreshTitle()
    }

    private fun initTvPath(visible: Boolean) {
        mBaseViewHolder.setGone(R.id.tv_path, visible)
        if (visible) {
            val view = mBaseViewHolder.getView<TextView>(R.id.tv_path)
            view.isEnabled = true
            view.setOnClickListener(View.OnClickListener { v -> if (Utils.isFastClick(v)) return@OnClickListener })
        }
    }

    private fun getTvPathText(context: Context, pathName: String,
                              showDevId: String?): SpannableString {
        val sb = StringBuilder()
        val pathWithTypeName = OneOSFileType.getPathWithTypeName(pathName)
        val deviceModel = SessionManager.getInstance()
                .getDeviceModel(showDevId)
        var devMarkName: String? = null
        if (deviceModel != null) {
            devMarkName = deviceModel.devName
        }
        if (!TextUtils.isEmpty(devMarkName)) {
            sb.append(devMarkName).append(":").append(pathWithTypeName)
        } else {
            sb.append(pathWithTypeName)
        }
        return Utils.setKeyWordColor(context, R.color.primary, sb.toString(), devMarkName)
    }

    private fun refreshTitle() {
        if (mTitleTxt != null) {
            if (!TextUtils.isEmpty(mCurPath) && mCurPath != File.separator) {
                if (mCurPath!!.endsWith(File.separator)) {
                    mCurPath = mCurPath!!.substring(0, mCurPath!!.length - 1)
                }
                val startIndex = mCurPath!!.lastIndexOf(File.separator) + 1
                val substring = mCurPath!!.substring(startIndex)
                mTitleTxt.text = substring
            } else {
                mTitleTxt.setText(R.string.all)
            }
        }
    }

    private fun toggleView() {
        isMultiSelectMode = false
        mBaseViewHolder.setGone(R.id.txt_renew, false)
        mBaseViewHolder.setText(R.id.txt_status, "")
        when (mShareElementV2.type) {
            EntityType.SHARE_FILE_V2_SEND -> {
                initViewSend()
            }
            else -> {
                initViewReceive()
            }
        }
    }

    private fun initViewReceive() {
        if (mAdapter == null) {
            mAdapter = object : BaseQuickAdapter<SFile, BaseViewHolder>(R.layout.item_rv_list_file_share) {
                override fun convert(helper: BaseViewHolder, item: SFile) {
                    if (helper.itemView is ViewGroup)
                        (helper.itemView as ViewGroup).descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                    val path = item.name
                    val imageResId: Int
                    imageResId = if (item.isDir) {
                        R.drawable.icon_device_folder
                    } else
                        FileUtils.fmtFileIcon(path)
                    helper.setImageResource(R.id.rv_list_iv_icon, imageResId)
                    helper.setText(R.id.rv_list_txt_name, path)
                    helper.setChecked(R.id.rv_list_cb_select, item.isSelected)
                    val cb = helper.getView<View>(R.id.rv_list_cb_select)
                    var value = ""
                    if (!item.isDir) {
                        value = FileUtils.fmtFileSize(item.size)
                    }
                    helper.addOnClickListener(R.id.rv_list_cb_select)
                    cb.visibility = if (mShareElementV2.state != TransferState.COMPLETE
                            && mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)
                            && !item.isDownloading && isDevOnline)
                        View.VISIBLE
                    else
                        View.GONE
                    helper.setVisible(R.id.rv_list_img_dl, item.isDownloading)
                    helper.setImageResource(R.id.rv_list_img_dl, R.drawable.icon_download_ing)
                    helper.getView<View>(R.id.rv_list_ibtn_select).visibility =
                            if (TextUtils.isEmpty(mShareElementV2.downloadId)
                                    && mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                    var progress = 0
                    if (mErrFiles != null && mErrFiles!!.contains(item)) {
                        helper.itemView.backgroundTintMode = PorterDuff.Mode.OVERLAY
                        for (sfile in mErrFiles!!) {
                            if (sfile == item) {
                                item.errNo = sfile.errNo
                                break
                            }
                        }
                        helper.setText(R.id.rv_list_txt_size, FileServerErrorCode.getString(item.errNo))
                        helper.setImageResource(R.id.rv_list_img_dl, R.drawable.icon_download_failure)
                    } else if (mCompleted != null && mCompleted!!.contains(item)) {
                        helper.setImageResource(R.id.rv_list_img_dl, R.drawable.icon_download_end)
                    } else if (mCurrents != null && mCurrents!!.contains(item)) {
                        for (sFile in mCurrents!!) {
                            if (Objects.equals(sFile, item)) {
                                val size = sFile.size
                                val currentSize = if (sFile.currentSize > item.currentSize) sFile.currentSize else item.currentSize
                                progress = if (size <= 0 || currentSize <= 0) {
                                    0
                                } else {
                                    (currentSize * 100f / size + 0.5f).toInt()
                                }
                                value = FileUtils.fmtFileSize(currentSize) + "/" + FileUtils.fmtFileSize(item.size)
                                helper.setVisible(R.id.rv_list_img_dl, true)
                                item.isDownloading = true
                                break
                            }
                        }
                    }
                    val view = helper.getView<View>(R.id.rv_list_progressbar)
                    if (view is NumberProgressBar)
                        view.progress = progress
                    else if (view is ProgressBar) {
                        view.progress = progress
                    }
                    helper.setText(R.id.rv_list_txt_size, value)
                }

            }
            mAdapter?.setLoadMoreView(FileLoadMoreView())
            mAdapter?.setOnLoadMoreListener(baseQuickAdapterLoadMoerListener)
            mAdapter?.emptyView = LayoutInflater.from(mActivity).inflate(R.layout.layout_empty_view, null)

            mAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
                if (Utils.isFastClick(view)) {
                    return@OnItemClickListener
                }
                val item = adapter.data[position] as SFile
                if (item.isDir) {
                    getFileTreeFromServer(item.path)
                } else {
                    onSFileClick(adapter, view, position, item)
                }
            }
            mAdapter!!.setOnItemChildClickListener { adapter, view, position ->
                if (Utils.isFastClick(view)) {
                    return@setOnItemChildClickListener
                }
                val item = adapter.data[position] as SFile
                if (view.id == R.id.rv_list_cb_select) {
                    onSFileClick(adapter, view, position, item)
                }
            }
            mAdapter!!.onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                val file = adapter.data[position] as SFile
                val isCompleted = mCompleted != null && mCompleted!!.contains(file)
                val dialog = DialogUtils.showNotifyDialog(mActivity, null,
                        file.name,
                        if (isCompleted || (file.isDir && !TextUtils.isEmpty(mShareElementV2.downloadId))) mActivity.getString(R.string.reavel_in_file_view) else ""
                ) { _, isPositiveBtn ->
                    if (isPositiveBtn) {
                        var path = file.path
                        if (mShareElementV2.toPath != null && !TextUtils.isEmpty(mShareElementV2.toDevId)) {
                            val deviceModel = SessionManager.getInstance().getDeviceModel(mShareElementV2.toDevId)
                            if (deviceModel != null) {
                                Logger.LOGD(this, "share>> path 1: ", path)
                                while (path.endsWith("/")) {
                                    path = path.substring(0, path.length - 1)
                                }
                                Logger.LOGD(this, "share>> path 2: ", path)
                                path = getDeviceFilePath(file)
//                            EventBus.getDefault().postSticky(deviceModel)
                                jumpToFileTree(file, path, deviceModel.devId)
                            }
                        }
                    }
                }
                if (dialog != null) {
                    dialog.setCancelable(true)
                    dialog.setCanceledOnTouchOutside(true)
                    return@OnItemLongClickListener true
                }
                false
            }
        }
        //                mPathPanel.setOnPathPanelClickListener(new FilePathPanel.OnPathPanelClickListener() {
        //                    @Override
        //                    public void onClick(View view, String path) {
        //                        if (Utils.isFastClick(view)) return;
        //                        getFileTreeFromServer(path);
        //                    }
        //                });
        //未下载的
        if (TextUtils.isEmpty(mShareElementV2.downloadId)) {
            if (mDeviceRVAdapter == null) {
                mDeviceRVAdapter = object : BaseQuickAdapter<DeviceModel, BaseViewHolder>
                (R.layout.item_listview_choose_device) {
                    private var briefs: List<BriefModel>? = null
                    override fun setNewData(data: List<DeviceModel>?) {
                        if (data == null) {
                            this.mData = arrayListOf()
                            briefs = null
                        } else {
                            this.mData = data
                            val ids = data.map {
                                it.devId
                            }.toTypedArray()
                            //加载简介数据
                            briefs = BriefRepo.getBriefs(ids, BriefRepo.FOR_DEVICE)
                        }
                        this.notifyItemRangeChanged(0, itemCount, arrayListOf(1))
                    }

                    override fun convertPayloads(helper: BaseViewHolder, item: DeviceModel?, payloads: MutableList<Any>) {
                        item?.let { convert(helper, it) }
                    }

                    override fun convert(holder: BaseViewHolder, item: DeviceModel) {
                        if (SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                            item.devNameFromDB
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .`as`(RxLife.`as`(popDialogFragment))
                                    .subscribe { s -> holder.setText(R.id.tv_device_name, s) }
                        } else {
                            val deviceModel = SessionManager.getInstance().getDeviceModel(item.devId)
                            if (deviceModel != null) {
                                deviceModel.device?.name
                                if (!deviceModel.device?.name.isNullOrEmpty()) {
                                    holder.setText(R.id.tv_device_name, deviceModel.device?.name)
                                } else {
                                    holder.setText(R.id.tv_device_name, item.devName)
                                }
                            }
                        }
                        val device = item.device
                        holder.setGone(R.id.select_box, true)
                        holder.setChecked(R.id.select_box, toId == item.devId)

                        if (device != null) {
                            holder.setText(R.id.tv_device_ip, device.vip)
                            val iconByeDevClass = io.weline.devhelper.IconHelper.getIconByeDevClass(device.devClass,
                                    device.isOnline, device.isOnline && device.dlt.clazz > 0)
                            val iconView = holder.getView<ImageView>(R.id.iv_device)
                            if (holder.itemView.getTag() != device?.id) {
                                iconView.setTag(null)
                                holder.itemView.setTag(device?.id)
                            }
                            if (iconView.getTag() == null) iconView.setImageResource(iconByeDevClass)
                            val brief = briefs?.find {
                                it.deviceId == device.id
                            }
                            LibApp.instance.getBriefDelegete().loadDeviceBrief(device?.id
                                    ?: "", brief, iconView, null, defalutImage = iconByeDevClass)

                        } else {
                            val iconByeDevClass = io.weline.devhelper.IconHelper.getIconByeDevClass(item.devClass, item.isOnline, true)
                            val iconView = holder.getView<ImageView>(R.id.iv_device)
                            if (holder.itemView.getTag() != device?.id) {
                                iconView.setTag(null)
                                holder.itemView.setTag(device?.id)
                            }
                            if (iconView.getTag() == null) iconView.setImageResource(iconByeDevClass)
                            val brief = briefs?.find {
                                it.deviceId == device?.id
                            }
                            LibApp.instance.getBriefDelegete().loadDeviceBrief(device?.id
                                    ?: "", brief, iconView, null, defalutImage = iconByeDevClass)
                        }
                    }
                }
                val inflate = LayoutInflater.from(mActivity).inflate(R.layout.layout_empty_view, null)
                inflate.findViewById<TextView>(R.id.txt_empty).setText(R.string.tips_no_dev)
                inflate.findViewById<TextView>(R.id.textView).isVisible = false
                mDeviceRVAdapter?.emptyView = inflate

                mDeviceRVAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
                    val o = adapter.data[position] as DeviceModel
                    toId = o.devId
                    mBaseViewHolder.setText(R.id.tv_path, getTvPathText(view.context,
                            OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR, toId))
                    toggleBtnByStatus(ShareStatus.TO_RECEIVE)
                    adapter.notifyItemRangeChanged(0, adapter.itemCount, listOf(o))
                }
            }

//            val observer = Observer<List<DeviceModel>> { deviceModels -> filterData(deviceModels, mDeviceRVAdapter!!) }
//            mLiveData.observe(mActivity, observer)
//            filterData(mDeviceViewModel.liveDevices?.value, mDeviceRVAdapter!!)


            mBaseViewHolder.getView<View>(R.id.tv_path).isEnabled = true
            //                    mPathPanel.setVisibility(View.GONE);
            mBaseViewHolder.setText(R.id.btn_operate, R.string.select_all)
            mBaseViewHolder.setGone(R.id.btn_operate, true)
            toggleBtnByStatus(ShareStatus.RECEIVE)
            mBaseViewHolder.getView<View>(R.id.btn_paste).isEnabled = false
            mBaseViewHolder.getView<View>(R.id.btn_paste)
                    .setOnClickListener(View.OnClickListener { v ->
                        if (Utils.isFastClick(v)) return@OnClickListener
                        when {
                            TextUtils.isEmpty(toId) -> {
                                showSelectDevice(v)
                            }
                            TextUtils.isEmpty(mToPath) -> {
                                if (TextUtils.isEmpty(toId)) {
                                    ToastHelper.showToast(R.string.tip_please_check_device)
                                    showSelectDevice(v)
                                    return@OnClickListener
                                }
                                selectToPath(v)
                            }
                            else -> download()
                        }
                    })
            mBaseViewHolder.getView<View>(R.id.btn_cancel)
                    .setOnClickListener(View.OnClickListener { v ->
                        if (Utils.isFastClick(v)) return@OnClickListener
                        if (mShareStatus == ShareStatus.RECEIVE) {
                            dismiss()
                        } else if (mShareStatus == ShareStatus.TO_RECEIVE) {
                            toId = null
                            mToPath = null
                            toggleView()
                        } else if (mShareStatus == ShareStatus.RECEIVE_DOWNLOAD) {
                            if (mListView!!.adapter === mDeviceRVAdapter) {
                                toId = null
                                mToPath = null
                                toggleView()
                            }
                        }
                    })
        } else {
            //正在下载
            mBaseViewHolder.setGone(R.id.btn_operate, false)
            mBaseViewHolder.setText(R.id.btn_operate, R.string.action_refresh)
            //                    mPathPanel.setVisibility(View.GONE);
            mBaseViewHolder.setGone(R.id.tv_path, false)
            mBaseViewHolder.setGone(R.id.txt_renew, mShareElementV2.state == TransferState.COMPLETE)
            toggleBtnByStatus(if (mShareElementV2.state == TransferState.PAUSE
                    || mShareElementV2.state == TransferState.FAILED)
                ShareStatus.RECEIVE_PAUSE
            else
                ShareStatus.RECEIVING)

            mRefreshLayout?.isEnabled = true

            refreshProgress()
            mBaseViewHolder.getView<View>(R.id.btn_paste)
                    .setOnClickListener(View.OnClickListener { v ->
                        if (Utils.isFastClick(v)) return@OnClickListener
                        if (mShareStatus == ShareStatus.RECEIVE_DOWNLOAD) {
                            download()
                        } else {
                            val str = (v as TextView).text.toString().trim()
                            if (str == v.getContext().getString(R.string.to_pause_download))
                                pauseShareDownload(v)
                            else
                                resumeShareDownload(v)
                        }
                    })
            mBaseViewHolder.getView<View>(R.id.btn_cancel)
                    .setOnClickListener(View.OnClickListener { v ->
                        if (Utils.isFastClick(v)) return@OnClickListener
                        if (mShareStatus == ShareStatus.RECEIVE_DOWNLOAD) {
                            toggleView()
                            toggleBtnByStatus(ShareStatus.RECEIVING)
                        } else
                            DialogUtils.showConfirmDialog(mActivity, 0,
                                    R.string.tips_warn_cancel_share,
                                    R.string.confirm, R.string.cancel) { _, isPositiveBtn ->
                                if (isPositiveBtn) {
                                    cancelShareDownload(false, v)
                                }
                            }
                    })
            mBaseViewHolder.getView<View>(R.id.txt_renew)
                    .setOnClickListener(View.OnClickListener { v ->
                        if (Utils.isFastClick(v)) return@OnClickListener
                        cancelShareDownload(true, v)
                    })
            getDownloadInfo(SHARE_DOWNLOAD_ID_PATHS, null)
            getDownloadInfo(SHARE_DOWNLOAD_ID_ERR_FILES, null)
        }
        // 统一的
        mBaseViewHolder.itemView.postDelayed({ getFileTreeFromServer(mCurPath) }, 600)
        mRefreshLayout!!.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            doRefresh()
        })
        mRefreshLayout.isRefreshing = true
        mBaseViewHolder.getView<View>(R.id.btn_operate)
                .setOnClickListener(View.OnClickListener { v ->
                    if (Utils.isFastClick(v)) return@OnClickListener
                    if (mShareStatus == ShareStatus.RECEIVE_PAUSE || mShareStatus == ShareStatus.RECEIVING) {
                        refreshProgress()
                    } else {
                        updateSelectAllOrNone(true)
                    }
                })
        mListView!!.adapter = mAdapter
    }

    private fun jumpToFileTree(file: SFile, path: String, devId: String) {
        val osFile = OneOSFile()
        osFile.setName(file.name)
        osFile.setPath(path)
        osFile.type = if (file.isDir) "dir" else "other"
        EventBus.getDefault().postSticky(osFile)
        //                                dismiss()
        Logger.LOGD(this, "share>> path 3 ", path, " file.name ", file.name)
        val type = OneOSFileType.getTypeByPath(path)
        val getParentDir = if (file.isDir) {
            removeSuffixFor(path, File.separator)
        } else {
            path.substring(0, path.length - file.name.length)
        }
        Logger.LOGD(this, "share>> path 4 ", getParentDir)
        val intent = Intent(mActivity, V2NasDetailsActivity::class.java)
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_PATH, getParentDir)
        intent.putExtra(AppConstants.SP_FIELD_FILE_TYPE, type)
        Logger.LOGD(this, "share>> put devId ", devId)
        Logger.LOGD(this, "share>> put path ", getParentDir)
        Logger.LOGD(this, "share>> put type ", type)
        mActivity?.startActivity(intent)
        mActivity?.finish()
        dismiss()
    }

    private fun selectToPath(v: View) {
//        if (v is CircularProgressButton) {
//            v.startAnimation()
//        }
        SessionManager.getInstance().getLoginSession(toId!!, object : GetSessionListener() {

            override fun onSuccess(url: String?, data: LoginSession?) {
                data?.let {
                    val fileTreeView = ServerFileTreeView(mActivity, null, data,
                            R.string.tip_download_file, R.string.download, R.string.previous)
                    fileTreeView.showPopupCenter()
                    fileTreeView.setOnPasteListener(object : ServerFileTreeView.OnPasteFileListener {
                        override fun onPaste(tarPath: String?, _share_path_type: Int) {
                            tarPath?.let {
                                mToPath = it
                                pathTYpe = _share_path_type
                                toggleBtnByStatus(ShareStatus.RECEIVE_DOWNLOAD)
                                download()
                            }
                        }
                    })
                }
//                if (v is CircularProgressButton) {
//                    v.revertAnimation()
//                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
//                if (v is CircularProgressButton) {
//                    v.revertAnimation()
//                }
            }
        })
    }

    private fun showSelectDevice(v: View) {
        isMultiSelectMode = false
        mDeviceRVAdapter?.let {
            filterData(mDeviceViewModel.liveDevices.value, it)
        }
        mListView!!.adapter = mDeviceRVAdapter
        if (mDeviceRVAdapter!!.data.size == 1) {
            val deviceModel = mDeviceRVAdapter!!.data[0]
            if (deviceModel != null) {
                toId = deviceModel.devId
                mBaseViewHolder.setText(R.id.tv_path, getTvPathText(v.context,
                        OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR, toId))
                mDeviceRVAdapter!!.notifyDataSetChanged()
            }
        }
        //                                        mPasteBtn.setText(R.string.download);
        mRefreshLayout?.isEnabled = false
        //                                        initTvPath(true);
        //                                        mPathPanel.setVisibility(View.GONE);
        mBaseViewHolder.setGone(R.id.btn_operate, false)
        toggleBtnByStatus(ShareStatus.TO_RECEIVE)
    }

    private fun initViewSend() {
        mBaseViewHolder.getView<View>(R.id.btn_paste).isEnabled = true
        mBaseViewHolder.setGone(R.id.btn_operate, false)
        //                mPathPanel.setVisibility(View.GONE);
        toggleBtnByStatus(ShareStatus.SENDING)
        mBaseViewHolder.getView<View>(R.id.swipe_refresh_layout).isEnabled = true
        refreshSendStatus()
        mBaseViewHolder.getView<View>(R.id.btn_paste).setOnClickListener(View.OnClickListener { v ->
            if (Utils.isFastClick(v)) return@OnClickListener
            mShareViewModel2.showQRCode(mActivity, mShareElementV2.ticket2,
                    AppConstants.sdf.format(Date(mShareElementV2.remainPeriod * 1000)), mShareElementV2.remainDownload.toLong(), mShareElementV2.password)
        })
        mBaseViewHolder.getView<View>(R.id.btn_cancel)
                .setOnClickListener(View.OnClickListener { v ->
                    if (Utils.isFastClick(v)) return@OnClickListener
                    mShareViewModel2.showRemoveDialog(mActivity, mShareElementV2) { fileShareBaseResult ->
                        if (fileShareBaseResult.isSuccessful) {
                            dismiss()
                        } else {
                            ToastHelper.showToast(FileServerErrorCode.getString(fileShareBaseResult.status))
                        }
                    }
                })
        if (mRefreshLayout != null) {
            mRefreshLayout.isRefreshing = true
        }
        mAdapter = object : BaseQuickAdapter<SFile, BaseViewHolder>(R.layout.item_rv_list_file_share) {
            override fun convert(helper: BaseViewHolder, item: SFile) {
                val name = item.name
                val imageResId: Int = if (item.isDir) {
                    R.drawable.icon_device_folder
                } else {
                    FileUtils.fmtFileIcon(name)
                }
                helper.setImageResource(R.id.rv_list_iv_icon, imageResId)
                helper.setText(R.id.rv_list_txt_name, name)
                var value = ""
                if (!item.isDir) {
                    value = FileUtils.fmtFileSize(item.size)
                }
                helper.setText(R.id.rv_list_txt_size, value)
                helper.setGone(R.id.rv_list_cb_select, false)
                helper.setGone(R.id.rv_list_ibtn_select, false)
            }
        }
        mAdapter?.setLoadMoreView(FileLoadMoreView())
        mAdapter?.setOnLoadMoreListener(baseQuickAdapterLoadMoerListener)
        mAdapter?.emptyView = LayoutInflater.from(mActivity).inflate(R.layout.layout_empty_view, null)

        mAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val file = adapter.data[position] as SFile
            if (file.isDir) {
                getFileTreeFromServer(file.path)
            }
        }
        mAdapter!!.onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
            val file = adapter.data[position] as SFile
            val dialog = DialogUtils.showNotifyDialog(mActivity, null,
                    file.name, mActivity.getString(R.string.reavel_in_file_view)
            ) { _, isPositiveBtn ->
                if (isPositiveBtn) {
                    var path = file.path
                    if (mShareRootPath != null && !TextUtils.isEmpty(mShareElementV2.srcDevId)) {
                        val deviceModel = SessionManager.getInstance()
                                .getDeviceModel(mShareElementV2.srcDevId)
                        if (deviceModel != null) {
                            Logger.LOGD(this, "share>> path send ", path, " rootPath ", mShareRootPath)
                            while (path.endsWith("/")) {
                                path = path.substring(0, path.length - 1)
                            }
                            Logger.LOGD(this, "share>> path send  ", path, " rootPath ", mShareRootPath)
                            val prefixPath = if (mShareElementV2.sharePathType == SharePathType.PUBLIC.type) {
                                OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR + mShareRootPath
                            } else {
                                mShareRootPath
                            }
                            path = prefixPath + path
                            path = fixPath(path)
//                            EventBus.getDefault().postSticky(deviceModel)
                            jumpToFileTree(file, path, deviceModel.devId)
                        }
                    }
                }
            }
            if (dialog != null) {
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)
                return@OnItemLongClickListener true
            }
            false
        }
        mBaseViewHolder.itemView.postDelayed({ getFileTreeFromServer(mCurPath) }, 1600)
        mRefreshLayout!!.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            getFileTreeFromServer(mCurPath)
        })
        mRefreshLayout.isRefreshing = true
        refreshSharedInfo()
        mListView!!.adapter = mAdapter
    }

    private fun removeSuffixFor(path: String, suffix: String): String {
        Logger.LOGD(this, "share>> path removeSuffixFor 1: ", path, " suffix ", suffix)
        var path111 = path
        while (path111 != suffix && path111.endsWith(suffix)) {
            path111 = path111.removeSuffix(suffix)
        }
        Logger.LOGD(this, "share>> path removeSuffixFor 2: ", path111)
        return path111
    }

    private fun onSFileClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int, item: SFile) {
        if (mShareElementV2.state != TransferState.COMPLETE && !item.isDownloading) {
            item.toggleSelected()
            mBaseViewHolder.setGone(R.id.btn_operate, true)
            if (!isMultiSelectMode) {
                if (TextUtils.isEmpty(mShareElementV2.downloadId)) {
                    toggleBtnByStatus(ShareStatus.RECEIVE)
                } else {
                    toggleBtnByStatus(ShareStatus.RECEIVE_DOWNLOAD)
                }
            }
            isMultiSelectMode = true
            mBaseViewHolder.setGone(R.id.layout_status, false)
            updateSelectAllOrNone(false)
            adapter.notifyItemChanged(position)
        } else if (mShareElementV2.state == TransferState.COMPLETE) {
            mShareElementV2.toDevId?.let {
                SessionManager.getInstance().getLoginSession(it, object : GetSessionListener() {
                    override fun onSuccess(url: String?, loginSession: LoginSession?) {
                        loginSession?.let { it1 ->
                            Logger.LOGD(TAG, " item : ", item)
                            val genDownloadUrl = OneOSAPIs.genOpenUrl(loginSession, getDeviceFilePath(item))
                            Logger.LOGD(TAG, " genDownloadUrl : ", genDownloadUrl)
                            FileUtils.show(genDownloadUrl, item.name, mActivity)
                        }
                    }
                })
            }
        }

    }

    private fun getDeviceFilePath(item: SFile): String {
        val result = if (mShareElementV2.sharePathType != 1) {
            mShareElementV2.toPath + File.separator + item.path
        } else {
            mShareElementV2.toPath + File.separator + item.name
        }
        val cmpatPath = if (mShareElementV2.sharePathType == SharePathType.PUBLIC.type) {
            PathTypeCompat.getAllStrPath(mShareElementV2.sharePathType, result)!!
        } else {
            result
        }
        return fixPath(cmpatPath)
    }

    private fun fixPath(result: String): String {
        var result1 = result
        val other = File.separator + File.separator
        Logger.LOGD(TAG, "share>>before result : $result1")
        while (result1.contains(other)) {
            result1 = result1.replace(other, File.separator)
        }
        Logger.LOGD(TAG, "share>>after result : $result1")
        return result1
    }

    private fun getParentPath(path: String): String {
        var path = path
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length - 1)
        }
        val startIndex = path.lastIndexOf(File.separator) + 1
        return path.substring(0, startIndex)
    }

    private fun refreshSendStatus() {
        val remainDownload = mShareElementV2.remainDownload
        val downloadTimes = if (remainDownload == FS_Config.CODE_DOWNLOAD_TIMES_UNLIMITED)
            mBaseViewHolder.itemView.context.getString(R.string.unlimited)
        else
            remainDownload.toString()
        val status = String.format(mActivity.getString(R.string.share_file_sending_status),
                if (mShareElementV2.remainPeriod > 0) {
                    AppConstants.sdf.format(Date(mShareElementV2.remainPeriod * 1000))
                } else {
                    ""
                }, downloadTimes)
        mBaseViewHolder.setText(R.id.txt_status, status)
    }


    private fun updateSelectAllOrNone(isClick: Boolean) {
        if (mAdapter != null) {
            val data = mAdapter!!.data
            val noSelectedFiles = ArrayList<SFile>()
            val selectableFiles = ArrayList<SFile>()
            for (sFile in data) {
                if (!sFile.isDownloading) {
                    if (!sFile.isSelected) {
                        noSelectedFiles.add(sFile)
                    }
                    selectableFiles.add(sFile)
                }
            }
            if (isClick) {
                if (noSelectedFiles.size > 0) {
                    for (noSelectedFile in noSelectedFiles) {
                        noSelectedFile.isSelected = true
                    }
                    noSelectedFiles.clear()
                } else {
                    for (sFile in selectableFiles) {
                        sFile.isSelected = false
                    }
                    noSelectedFiles.addAll(data)
                }
                mAdapter!!.notifyDataSetChanged()
            }
            if (isMultiSelectMode) {
                mBaseViewHolder.setText(R.id.btn_operate, if (data.size > 0)
                    if (noSelectedFiles.size == 0) {
                        R.string.select_none
                    } else {
                        R.string.select_all
                    } else {
                    R.string.empty
                })
                mPasteBtn.isEnabled = noSelectedFiles.size < selectableFiles.size
                Logger.LOGD(TAG, String.format("noSelect : %s selectable : %s",
                        noSelectedFiles.size, selectableFiles.size))
            } else {
                mBaseViewHolder.setText(R.id.btn_operate, R.string.action_refresh)
                mPasteBtn.isEnabled = true
            }

        }
    }

    private fun refreshSharedInfo() {
        val srcDevId = mShareElementV2.srcDevId
        if (!TextUtils.isEmpty(srcDevId)) {
            addDisposable(mShareViewModel2.getSharedInfo(srcDevId, mShareElementV2.ticket2)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Consumer { response ->
                        if (response.isSuccessful) {
                            val result = response.result
                            mShareElementV2.remainDownload = result.remainDownload
                            mShareElementV2.remainPeriod = result.remainPeriod
                            mShareElementV2.maxDownload = result.maxDownload
                            mShareElementV2.fromOwner = result.userId
                            refreshSendStatus()
                        } else {
                            if (response.status == FileServerErrorCode.MSG_ERROR_NO_TASK) {
                                mShareElementV2.errNo = response.status
                                mShareViewModel2.showRemoveDialog(mActivity, mShareElementV2) { dismiss() }
                            } else {
                                ToastHelper.showToast(FileServerErrorCode.getString(response.status))
                            }
                        }
                    }, ConsumerThrowable(TAG, " refreshSharedInfo fail")))
        }
    }


    private fun download() {
        mBaseViewHolder.setGone(R.id.include_refresh_view, true)
        isMultiSelectMode = false
        val stringList = HashSet<String>()
        val subscribe1 = Observable
                .fromIterable(mAdapter!!.data)
                .filter { sFile -> sFile.isSelected }
                .map { sFile -> sFile.path }
                .toList()
                .toObservable()
                .flatMap { strings ->
                    val fileShareBaseResultObservable: Observable<FileShareBaseResult<*>>
                    if (!TextUtils.isEmpty(mShareElementV2.downloadId)) {
                        fileShareBaseResultObservable = mShareViewModel2.addDownloadPath(toId, mShareElementV2.downloadId, strings)
                    } else {
                        val deviceModel = SessionManager.getInstance().getDeviceModel(toId)
                        val isPublicPath = mToPath != null && mToPath!!.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)
                                || pathTYpe == SharePathType.PUBLIC.type
                                || (deviceModel != null && UiUtils.isAndroidTV(deviceModel.devClass)
                                && !SessionCache.instance.isV5OrSynchRequest(deviceModel.devId, deviceModel.device?.vip
                                ?: ""))

                        var toPath = mToPath
                        if (isPublicPath) {
                            toPath = toPath!!.replaceFirst(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR.toRegex(), "")
                        }
                        fileShareBaseResultObservable = mShareViewModel2.download(toId,
                                mShareElementV2.ticket2, toPath, strings, isPublicPath,
                                mShareElementV2.password, mActivity)
                    }
                    stringList.addAll(strings)
                    fileShareBaseResultObservable
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { runOnUiThread { mBaseViewHolder.setGone(R.id.include_refresh_view, false) } }
                .subscribe({ fileShareBaseResult ->
                    if (fileShareBaseResult.isSuccessful) {
                        val result = fileShareBaseResult.result
                        if (TextUtils.isEmpty(mShareElementV2.downloadId)
                                && result is LinkedTreeMap<*, *>) {
                            @Suppress("UNCHECKED_CAST")
                            val map = result as LinkedTreeMap<String, Any?>
                            val ticket = map.get("ticket") as String?
                            updateDownloadTicket(ticket, stringList)
                        } else {
                            updateDownloadTicket(null, stringList)
                        }
                        startObservableIntervalTask()//download
                        toggleView()

                    } else {
                        if (fileShareBaseResult.status == FileServerErrorCode.MSG_ERROR_MORE_SHARED_TICKET) {
                            val result = fileShareBaseResult.result
                            if (TextUtils.isEmpty(mShareElementV2.downloadId)
                                    && result is LinkedTreeMap<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val map = result as LinkedTreeMap<String, Any?>
                                val ticket = map.get("exist_ticket") as String?
                                updateDownloadTicket(ticket, stringList)
                            } else {
                                updateDownloadTicket(null, stringList)
                            }
                            startObservableIntervalTask()//download
                            toggleView()
                        } else {
                            ToastHelper.showToast(FileServerErrorCode.getString(fileShareBaseResult.status))
                            if ((fileShareBaseResult.status == FileServerErrorCode.MSG_ERROR_EXCEED_DOWNLOADS
                                            || fileShareBaseResult.status == FileServerErrorCode.MSG_ERROR_NO_TASK)) {
                                mShareElementV2.state = TransferState.CANCELED
                                mShareViewModel2.putToDB(mShareElementV2)
                                dismiss()
                            } else {
                                toggleView()
                            }
                        }
                    }
                }, {
                    Logger.LOGE(TAG, it, "share download")
                })

        addDisposable(subscribe1)
    }

    private fun runOnUiThread(runnable: () -> Unit) {
        AppExecutors.instance.mainThread().execute(runnable)
    }

    private fun updateDownloadTicket(ticket: String?, stringList: HashSet<String>) {
        if (!ticket.isNullOrEmpty()) {
            mShareElementV2.downloadId = ticket
            mShareElementV2.toDevId = toId
            mShareElementV2.toPath = mToPath
            mShareElementV2.state = TransferState.START
            mShareElementV2.sharePathType = pathTYpe
            val sfDownload = SFDownload()
            sfDownload.token = ticket
            sfDownload.toDevId = toId
            sfDownload.toPath = mToPath
            sfDownload.desPathType = pathTYpe
            val shareElementV2Box = DBHelper.getBoxStore().boxFor(ShareElementV2::class.java)
            shareElementV2Box.attach(mShareElementV2)
            mShareElementV2.sfDownloads.add(sfDownload)
            mShareViewModel2.putToDB(mShareElementV2)

            refreshDevName(Consumer {
                val o = if (it == null) {
                    ""
                } else {
                    val pathName = if (TextUtils.isEmpty(mToPath)) {
                        File.separator
                    } else {
                        mToPath!!
                    }
                    getTvPathText(mBaseViewHolder.itemView.context, pathName, toId)
                }
                mBaseViewHolder.setText(R.id.txt_status_to_device, String.format("%s", o))
                Logger.LOGD(TAG, "status_to_device $o")
            })
        }
        if (mDownloadPaths != null) {
            mDownloadPaths!!.addAll(stringList)
        } else {
            mDownloadPaths = stringList
        }
        Logger.LOGD(this, "download() --> update download path: ", mDownloadPaths)
        refreshFileTree()

    }

    private fun refreshDevName(consumer: Consumer<String>?) {
        val toDevId = mShareElementV2.toDevId
        val deviceModel = SessionManager.getInstance().getDeviceModel(toDevId)
        if (deviceModel != null) {
            devName = deviceModel.devName
            val subscribe = deviceModel
                    .devNameFromDB
                    .subscribe { s ->
                        devName = s
                        consumer?.accept(s)
                    }
            addDisposable(subscribe)
        }
    }

    private fun startObservableIntervalTask() {
        stopIntervalTask()
        subscribeIntervalTask = Flowable
                .interval(100, 1200, TimeUnit.MILLISECONDS)
                .filter {
                    (popDialogFragment != null
                            && popDialogFragment.isResumed
                            && isDevOnline)
                }
                .subscribe(Consumer { aLong ->
                    Logger.LOGD(TAG, "startObservableIntervalTask : ", aLong)
                    val toDevId = mShareElementV2.toDevId
                    if (TextUtils.isEmpty(toDevId)) {
                        Logger.LOGD(TAG, "refreshProgress toDevId is null")
                        return@Consumer
                    }
                    val downloadId = mShareElementV2.downloadId
                    if (TextUtils.isEmpty(downloadId)) {
                        Logger.LOGD(TAG, "refreshProgress downloadId is null")
                        return@Consumer
                    }
                    val queryShareProgress = mShareViewModel2
                            .progress(toDevId, downloadId)
                            .throttleWithTimeout(3000, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(onNextProgress!!, ConsumerThrowable(TAG, "query share progress"))
                    addDisposable(queryShareProgress)
                    if (BuildConfig.DEBUG) {
                        addDisposable(mShareViewModel2.version(toDevId, Callback {
                            if (it.isSuccess) {
                                Logger.LOGD(TAG, "version : " + it.msg)
                            }
                        }))


                    }
                }, Consumer { throwable -> Logger.LOGD(TAG, "startObservableIntervalTask", throwable) })
        addDisposable(subscribeIntervalTask!!)
    }

    private fun stopIntervalTask() {
        compositeDisposable?.clear()
        subscribeIntervalTask?.dispose()
        subscribeIntervalTask = null
    }

    private fun stopIntervalTaskDelay(delay: Long) {
        addDisposable(Observable.timer(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { stopIntervalTask() })
    }

    private fun resumeShareDownload(v: View) {
        v.isEnabled = false
        val subscribe = mShareViewModel2
                .resumeDownload(mShareElementV2.toDevId, mShareElementV2.downloadId)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { runOnUiThread { v.isEnabled = true } }
                .subscribe(Consumer { fileShareBaseResult ->
                    if (fileShareBaseResult.isSuccessful) {
                        if (mSFDownload != null) {
                            mSFDownload.state = TransferState.START
                        }
                        mShareElementV2.state = TransferState.START
                        toggleBtnByStatus(ShareStatus.RECEIVING)
                        startObservableIntervalTask()//resumeDownload
                    } else {
                        ToastHelper.showToast(FileServerErrorCode.getString(fileShareBaseResult.status))
                    }
                }, ConsumerThrowable(TAG, " resumeShareDownload fail"))
        addDisposable(subscribe)
    }

    private fun pauseShareDownload(v: TextView) {
        v.isEnabled = false
        stopIntervalTask()
        val subscribe = mShareViewModel2
                .pauseDownload(mShareElementV2.toDevId, mShareElementV2.downloadId)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { runOnUiThread { v.isEnabled = true } }
                .subscribe(Consumer { fileShareBaseResult ->
                    if (fileShareBaseResult.isSuccessful) {
                        if (mSFDownload != null) {
                            mSFDownload.state = TransferState.PAUSE
                        }
                        toggleBtnByStatus(ShareStatus.RECEIVE_PAUSE)
                    } else {
                        ToastHelper.showToast(FileServerErrorCode.getString(fileShareBaseResult.status))
                    }
                }, ConsumerThrowable(TAG, " pauseShareDownload fail"))
        addDisposable(subscribe)
    }

    private fun cancelShareDownload(isRenew: Boolean, v: View) {
        val toDevId = mShareElementV2.toDevId
        val downloadId = mShareElementV2.downloadId
        if (toDevId.isNullOrEmpty() || downloadId.isNullOrEmpty()) {
            return
        }
        v.isEnabled = false
        val subscribe = mShareViewModel2
                .cancelDownload(toDevId, downloadId)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { runOnUiThread { v.isEnabled = true } }
                .subscribe(Consumer { fileShareBaseResult ->
                    if (fileShareBaseResult.isSuccessful) {
                        resetTask(isRenew)
                    } else {
                        if (!isRenew)
                            ToastHelper.showToast(FileServerErrorCode.getString(fileShareBaseResult.status))
                    }

                }, ConsumerThrowable(TAG, " cancelShareDownload fail"))
        addDisposable(subscribe)

    }

    private fun resetTask(isRenew: Boolean) {
        stopIntervalTask()
        val toDevId = mShareElementV2.toDevId
        val downloadId = mShareElementV2.downloadId
        if (mSFDownload != null) {
            val sfDownloads = mShareElementV2.sfDownloads
            sfDownloads.remove(mSFDownload)
            sfDownloads.applyChangesToDb()
        }
        val sfDownloadBox = DBHelper.getBoxStore().boxFor(SFDownload::class.java)
        val sfDownloads1 = sfDownloadBox.query()
                .equal(SFDownload_.token, downloadId)
                .equal(SFDownload_.toDevId, toDevId ?: "")
                .build()
                .find()
        sfDownloadBox.remove(sfDownloads1)
        mShareElementV2.downloadId = null
        mShareElementV2.toDevId = null
        mShareElementV2.toPath = null
        mShareElementV2.state = TransferState.NONE
        mDownloadPaths = null
        toId = null
        mCompleted = null
        mErrFiles = null
        mToPath = null
        mCurrents = null
        devName = null
        mShareViewModel2.putToDB(mShareElementV2)
        if (!isRenew)
            dismiss()
        else {
            mBaseViewHolder.setGone(R.id.include_refresh_view, true)
            mShareViewModel2.subscribeDevice(mShareElementV2.ticket2, false) { toggleView() }

        }

    }


    private fun refreshProgress() {
        if (subscribeIntervalTask != null && !subscribeIntervalTask!!.isDisposed) {

            return
        }
        val toDevId = mShareElementV2.toDevId
        if (TextUtils.isEmpty(toDevId)) {
            Logger.LOGD(TAG, "refreshProgress toDevId is null")
            return
        }
        val downloadId = mShareElementV2.downloadId
        if (TextUtils.isEmpty(downloadId)) {
            Logger.LOGD(TAG, "refreshProgress downloadId is null")
            return
        }

        val queryShareProgress = mShareViewModel2
                .progress(toDevId, downloadId)
                .doOnSubscribe {
                    mRefreshLayout?.post {
                        mRefreshLayout.isRefreshing = true
                    }
                }
                .doOnError {
                    mRefreshLayout?.post {
                        mRefreshLayout.isRefreshing = false
                    }
                }
                .doOnNext {
                    mRefreshLayout?.post {
                        mRefreshLayout.isRefreshing = false
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNextProgress!!, ConsumerThrowable(TAG, "query share progress"))
        addDisposable(queryShareProgress)
    }


    private fun refreshFileTree() {
        if (mAdapter != null) {
            val sFiles = mAdapter!!.data
            for (item in sFiles) {
                if (mDownloadPaths != null) {
                    item.isDownloading = false
                    for (downloadPath in mDownloadPaths!!) {
                        if (FileUtils.pathIsPrefix(item.path, downloadPath)) {
                            item.isDownloading = true
                            break
                        }
                    }
                }
            }
            mAdapter!!.notifyDataSetChanged()
        }
    }

    private fun toggleBtnByStatus(shareStatus: ShareStatus) {
        if (mBaseViewHolder == null) return
        var enabled = mBaseViewHolder.getView<View>(R.id.btn_paste).isEnabled
        if (mShareStatus != shareStatus) {
            mShareStatus = shareStatus
        }
        val cancel: Int
        val next: Int
        var operate = 0
        when (shareStatus) {
            ShareStatus.SENDING -> {
                cancel = R.string.to_cancel_share
                next = R.string.operate_show_qrcode
                enabled = true
            }
            ShareStatus.TO_RECEIVE -> {
                cancel = R.string.previous
                next = R.string.next
                enabled = !TextUtils.isEmpty(toId)
            }
            ShareStatus.RECEIVE_DOWNLOAD -> {
                cancel = R.string.previous
                next = R.string.download
                enabled = !TextUtils.isEmpty(mToPath)
            }
            ShareStatus.RECEIVING -> {
                operate = R.string.action_refresh
                cancel = R.string.to_cancel_download
                next = R.string.to_pause_download
            }
            ShareStatus.RECEIVE_PAUSE -> {
                operate = R.string.action_refresh
                cancel = R.string.to_cancel_download
                next = R.string.to_resume_download
                enabled = true
            }
            else -> {
                isMultiSelectMode = true
                mBaseViewHolder.setGone(R.id.layout_status, false)
                cancel = R.string.cancel
                next = R.string.next
            }
        }

        mBaseViewHolder.setText(R.id.btn_cancel, cancel)
        mBaseViewHolder.setText(R.id.btn_paste, next)
        mBaseViewHolder.getView<View>(R.id.btn_paste).isEnabled = enabled
        if (operate != 0) {
            mBaseViewHolder.setText(R.id.btn_operate, operate)
        }
        mBaseViewHolder.setGone(R.id.group_start, true)
    }

    private fun filterData(deviceModels: List<DeviceModel>?, mDeviceRVAdapter: BaseQuickAdapter<DeviceModel, BaseViewHolder>) {
        val models = ArrayList<DeviceModel>()
        if (deviceModels != null) {
            for (deviceModel in deviceModels) {
                if (deviceModel.isEnableDownloadShare()) {
                    if (!deviceModel.isShareV2Available) {
                        addDisposable(mShareViewModel2.version(deviceModel.devId) {
                            if (it.isSuccess) {
                                deviceModel.isShareV2Available = true
                                models.add(deviceModel)
                                mDeviceRVAdapter.notifyItemRangeChanged(0, mDeviceRVAdapter.itemCount, arrayListOf(1))
                                Logger.LOGD(TAG, "version : " + it.msg)
                            }
                        })
                    } else {
                        models.add(deviceModel)
                    }
                }/*&& !deviceModel.getDevId().equals(mShowDevId)*/
            }
        }
        mDeviceRVAdapter.setNewData(models)
    }

    private fun getFileTreeFromServer(path1: String?, isLoadMore: Boolean = false) {
        var path = path1

        if (path1.isNullOrEmpty()) {
            path = File.separator
        }
        if (path1?.startsWith(File.separator) == false) {
            path = File.separator + path1
        }
        val finalPath: String = path + ""
        val srcDevId = mShareElementV2.srcDevId
        val ticket1 = mShareElementV2.ticket1
        Observable.create<Any> { emitter ->
            if (TextUtils.isEmpty(srcDevId)) {
                emitter.onError(Throwable("getFileTreeFromServer isEmpty(srcDevId)"))
            }
            if (TextUtils.isEmpty(ticket1)) {
                emitter.onError(Throwable("getFileTreeFromServer isEmpty(ticket1)"))
            } else {
                emitter.onNext(true)
            }
        }
                .flatMap {
                    mFilesPage = if (isLoadMore && hasMorePages()) {
                        mFilesPage + 1
                    } else {
                        0
                    }
                    mShareViewModel2.getShareDir(srcDevId, null, ticket1, finalPath,
                            mShareElementV2.password, mActivity, FS_Config.PAGE_SIZE, mFilesPage)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : io.reactivex.Observer<FileShareBaseResult<DataShareDir>> {
                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }

                    override fun onNext(data: FileShareBaseResult<DataShareDir>) {
                        if (data.isSuccessful) {
                            val dataShareDir = data.result
                            if (dataShareDir != null) {
                                val sFiles = dataShareDir.path
                                for (item in sFiles) {
                                    if (mShareElementV2.sharePathType != 1) {
                                        val sb = StringBuilder()
                                        sb.append(finalPath)
                                        if (finalPath.isNotEmpty() && !finalPath.endsWith(File.separator)) {
                                            sb.append(File.separator)
                                        }
                                        sb.append(item.name)
                                        if (item.isDir) {
                                            if (!sb.toString().endsWith(File.separator)) {
                                                sb.append(File.separator)
                                            }
                                        }
                                        item.path = sb.toString()
                                    } else {
                                        try {
                                            item.path = item.name
                                            Timber.d("share>> path : ${item.path}")
                                            item.name = item.path.substring(item.path.lastIndexOf("/") + 1)
                                            Timber.d("share>> item : $item")
                                        } catch (ignore: Throwable) {
                                        }
                                    }
                                    if (mDownloadPaths != null) {
                                        item.isDownloading = false
                                        for (downloadPath in mDownloadPaths!!) {
                                            if (FileUtils.pathIsPrefix(item.path, downloadPath)) {
                                                item.isDownloading = true
                                                break
                                            }
                                        }
                                    }

                                }
//                                if (finalPath == File.separator) {
//                                    val dir = SFile()
//                                    dir.path = finalPath
//                                    dir.isDir = true
//                                    mSFileTree = SFileTree(sFiles, dir, null)
//                                }
                                mFilesTotal = dataShareDir.total
                                mFilesPage = dataShareDir.page
                                mAdapter?.let {
                                    if (isLoadMore) {
                                        it.addData(sFiles)
                                    } else {
                                        it.replaceData(sFiles)

                                    }
                                    if (hasMorePages()) {
                                        it.loadMoreComplete()
                                    } else {
                                        it.loadMoreEnd()
                                    }
                                    it.setEnableLoadMore(hasMorePages())
                                }
                                //                                if (mPathPanel != null) {
                                //                                    mPathPanel.updatePath(mActivity.getString(R.string.root_dir_all), finalPath);
                                //                                }
                                mCurPath = finalPath
                                refreshTitle()
                                if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
                                    updateSelectAllOrNone(false)
                                    getDownloadInfo(SHARE_DOWNLOAD_ID_COMPLETED, finalPath)
                                }
                            }
                        } else {
//                            if (data.status == FileServerErrorCode.MSG_ERROR_NO_TASK) {
//                                mShareElementV2.errNo = data.status
////                                mShareViewModel2.showRemoveDialog(mActivity, mShareElementV2) { dismiss() }
//                            } else {
                            ToastHelper.showToast(FileServerErrorCode.getString(data.status))
//                            }
                        }
                        onComplete()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        onComplete()
                    }

                    override fun onComplete() {
                        if (mRefreshLayout != null) {
                            mRefreshLayout.isRefreshing = false
                        }
                        mBaseViewHolder.setGone(R.id.include_refresh_view, false)
                    }
                })


    }

    private fun hasMorePages() = (mFilesPage * FS_Config.PAGE_SIZE) < mFilesTotal


    private fun getDownloadInfo(downloadID: EntityType.DownloadID, path: String?) {
        var page = 1
        if (downloadID == SHARE_DOWNLOAD_ID_COMPLETED) {
            if (mCompleted != null) {
                val size2 = mCompleted!!.size
                page = (ceil((size2 / (FS_Config.PAGE_SIZE * 1f)).toDouble())).toInt()
            }
        }
        val toDevId = mShareElementV2.toDevId
        val downloadId = mShareElementV2.downloadId
        if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)
                && !TextUtils.isEmpty(toDevId) && !TextUtils.isEmpty(downloadId)) {
            val subscribe = mShareViewModel2.getDownloadInfo(toDevId, downloadId, downloadID.ordinal, path, FS_Config.PAGE_SIZE, page)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ fileShareBaseResult ->
                        if (fileShareBaseResult.isSuccessful) {
                            val result = fileShareBaseResult.result
                            if (downloadID == SHARE_DOWNLOAD_ID_COMPLETED) {
                                if (result.completed != null) {
                                    if (mCompleted == null)
                                        mCompleted = HashSet()
                                    mCompleted!!.addAll(result.completed)
                                    if (mCompleted!!.size > 0 && mAdapter != null) {
                                        mAdapter!!.notifyDataSetChanged()
                                    }
                                }
                            } else if (downloadID == SHARE_DOWNLOAD_ID_PATHS) {
                                val downloadPaths = result.downloadPath
                                if (downloadPaths != null) {
                                    if (mDownloadPaths != null) {
                                        mDownloadPaths!!.addAll(downloadPaths)
                                    } else {
                                        mDownloadPaths = downloadPaths
                                    }
                                }
                                refreshFileTree()
                            } else if (downloadID == SHARE_DOWNLOAD_ID_ERR_FILES) {
                                mErrFiles = result.errFiles
                                if (mErrFiles != null && mAdapter != null) {
                                    mAdapter!!.notifyDataSetChanged()
                                }
                            }
                        }
                    }, { throwable -> Timber.e(throwable) })
            addDisposable(subscribe)
        }
    }

    fun dismiss() {
        popDialogFragment.dismiss()
    }

    fun showPopupCenter() {
        popDialogFragment.show(mActivity.supportFragmentManager, TAG)
    }

    fun addDismissLister(onDismissListener: OnDismissListener) {
        this.onDismissListener = onDismissListener
    }

    companion object {
        private val TAG = ServerShareFileTreeHolder::class.java.simpleName
    }

    @Keep
    interface OnDismissListener {
        /**
         * Called when this popup window is dismissed.
         */
        fun onDismiss()
    }

}


