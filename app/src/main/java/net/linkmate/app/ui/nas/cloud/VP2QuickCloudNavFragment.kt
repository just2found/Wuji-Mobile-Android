package net.linkmate.app.ui.nas.cloud

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.FloatDecoration
import com.chad.library.adapter.base.entity.SectionEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rxjava.rxlife.RxLife
import io.cabriole.decorator.GridSpanMarginDecoration
import io.reactivex.Observable
import io.weline.devhelper.DevTypeHelper
import io.weline.repo.SessionCache
import io.weline.repo.api.V5_ERR_DENIED_PERMISSION
import kotlinx.android.synthetic.main.fragment_nav_cloud_files.*
import kotlinx.android.synthetic.main.fragment_quick_nav_cloud_dir.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.activity.WebViewActivity
import net.linkmate.app.ui.activity.message.SystemMessageActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DevicelDetailActivity
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant
import net.linkmate.app.ui.nas.devhelper.SelectDeviceFragmentArgs
import net.linkmate.app.ui.nas.helper.*
import net.linkmate.app.ui.nas.helper.UpdateFileTypeHelper.addItem
import net.linkmate.app.ui.nas.helper.UpdateFileTypeHelper.offlineDownloadItem
import net.linkmate.app.ui.nas.search.SearchFragmentArgs
import net.linkmate.app.ui.nas.share.ShareActivity
import net.linkmate.app.ui.nas.transfer.TransferActivity
import net.linkmate.app.ui.nas.upload.UploadActivity
import net.linkmate.app.ui.nas.widget.OnCheckedChangeListener
import net.linkmate.app.ui.nas.widget.SortMenuPopupViewV2
import net.linkmate.app.ui.viewmodel.SystemMessageViewModel
import net.linkmate.app.ui.viewmodel.TorrentsViewModel
import net.linkmate.app.ui.viewmodel.TransferCountViewModel
import net.linkmate.app.util.Dp2PxUtils.dp2px
import net.linkmate.app.util.UIUtils
import net.linkmate.app.util.WindowUtil
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.DeviceSettingsKeeper
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.iface.*
import net.sdvn.nascommon.model.*
import net.sdvn.nascommon.model.UiUtils.isHans
import net.sdvn.nascommon.model.UiUtils.isHant
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.event.Content
import net.sdvn.nascommon.model.oneos.event.Progress
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.receiver.NetworkStateManager.Companion.STATUS_CODE_ESTABLISHED
import net.sdvn.nascommon.utils.*
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.viewmodel.FilesContract
import net.sdvn.nascommon.viewmodel.FilesViewModel
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import net.sdvn.nascommon.widget.*
import net.sdvn.nascommon.widget.badgeview.QBadgeView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.view.libwidget.RecyelerViewScrollDetector
import org.view.libwidget.anim.BezierViewHolder
import org.view.libwidget.hideAndDisable
import org.view.libwidget.showAndEnable
import org.view.libwidget.singleClick
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


open class VP2QuickCloudNavFragment : QuickBaseCloudFragment(),
    FilesContract.View<OneOSFile> {
    private var isEnableUseSpace: Boolean = true
    private var isAndroidTV: Boolean = false
    private val devicePreViewModel by viewModels<DevicePreViewModel>({ requireActivity() })
    private val deviceViewModel by viewModels<DeviceViewModel>({ requireParentFragment() })
    protected open val mFilesViewModel by viewModels<FilesViewModel>({ requireActivity() })
    private val messageViewModel by viewModels<SystemMessageViewModel>({ requireActivity() })
    private val transferCountViewModel by viewModels<TransferCountViewModel>({ requireActivity() })
    private val shareViewModel2 by viewModels<ShareViewModel2>({ requireActivity() })
    private val model by viewModels<TorrentsViewModel>({ requireActivity() })
    private val vP2QuickCloudNavFragmentArgs by navArgs<VP2QuickCloudNavFragmentArgs>()
    private var mSelectPanel: FileSelectPanel? = null
    private var mSearchPanel: SearchPanel? = null
    private var mSearchBtn: ImageView? = null
    private var mTransBtn: ImageView? = null
    private var mTypeBtn: TextView? = null
    private var mTitleLayout: ViewGroup? = null

    //    private var mMsgPopupView: MsgPopupView? = null
    private var mTypePopView: TypePopupView? = null
    private var mUploadPopView: TypePopupView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private val mAddPopView: MenuPopupView? = null
    private var mOrderPopView: MenuPopupView? = null
    private var mFooterView: View? = null
    private var mErrorView: View? = null
    private var mManagePanel: FileManagePanel? = null
    var fileAdapter: QuickSectionOneOSFilesRVAdapter? = null
        private set
    private var mLoadMoreListener: BaseQuickAdapter.RequestLoadMoreListener? = null

    private val mData = ArrayList<SectionEntity<DataFile>>()
    private var wantto: String? = null
    private var mCurrentPosition: Int = 0

    private var isClickRefresh: Boolean = false
    private var isRefreshing: Boolean = false
    private val floatDecoration = FloatDecoration(BaseSectionQuickAdapter.SECTION_HEADER_VIEW)
    private var messagesCount = 0
    private var sharesCount = 0
    private var transferCount = 0
    private var menuPopupView: MenuPopupView? = null
    private val fmt: String = LibApp.instance.getApp().getString(R.string.fmt_time_line)

    private val mOnItemClickListener =
        BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            if (isRefreshing) {
                return@OnItemClickListener
            }
            mLastClickPosition = position
            mLastClickItem2Top = view.top

            val mAdapter = adapter as QuickSectionOneOSFilesRVAdapter
            val o = mAdapter.getItem(position)
            o?.let {
                if (o.isHeader) {
                    return@OnItemClickListener
                }
                val file = o.t ?: return@OnItemClickListener
                if (mDeviceDisplayModel?.mode != null
                    && mDeviceDisplayModel?.mode == DisplayMode.SHARE
                ) {
                    setMultiModel(true, null)
                }
                val index = mFileList.indexOf(file)
                if (index < 0 || index >= mFileList.size) return@OnItemClickListener
                val isMultiMode = mAdapter.isMultiChooseModel
                if (mDeviceDisplayModel?.isSearch == true) {
                    mSearchPanel?.hidePanel(false)
                    mDeviceDisplayModel?.isSearch = false
                }
                if (isMultiMode) {
                    if (file.isDirectory() && mDeviceDisplayModel?.mode != null && mDeviceDisplayModel!!.mode == DisplayMode.SHARE) {
                        wantto = file.getPath()
                        isClickRefresh = true
                        autoPullToRefresh()//onclick mode share
                        return@OnItemClickListener
                    }

                    mAdapter.toggleItemSelected(position)
                    updateSelectAndManagePanel(
                        false,
                        mDeviceDisplayModel!!.mode,
                        mFileSelectListener,
                        mFileManageListener
                    )
                    showSelectAndOperatePanel(true)
                } else {
                    if (file.isDirectory()) {
                        wantto = file.getPath()
                        isClickRefresh = true
                        autoPullToRefresh()//click into dir

                    } else {
                        isSelectionLastPosition = true
                        FileUtils.openOneOSFile(
                            mDeviceDisplayModel!!.mLoginSession!!,
                            requireActivity(),
                            view,
                            index,
                            mFileList,
                            mDeviceDisplayModel!!.mFileType
                        )
                    }
                }
            }
        }


    private val mOnItemLongClickListener =
        BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
            val mAdapter = adapter as QuickSectionOneOSFilesRVAdapter
            val o = mAdapter.getItem(position) ?: return@OnItemLongClickListener false
            if (o.isHeader || fileAdapter?.isMultiChooseEnable == false) {
                return@OnItemLongClickListener false
            }
            val file: OneOSFile = o.t as OneOSFile? ?: return@OnItemLongClickListener false
            val isMultiMode = mAdapter.isMultiChooseModel
            if (!isMultiMode) {
                setMultiModel(true, position)
                updateSelectAndManagePanel(
                    false,
                    mDeviceDisplayModel!!.mode,
                    mFileSelectListener,
                    mFileManageListener
                )
            } else {
                mAdapter.toggleItemSelected(position)
                updateSelectAndManagePanel(
                    false,
                    mDeviceDisplayModel!!.mode,
                    mFileSelectListener,
                    mFileManageListener
                )
            }
            true
        }

    private val mFileSelectListener = object : FileSelectPanel.OnFileSelectListener {
        override fun onSelect(isSelectAll: Boolean) {
            fileAdapter!!.selectAllItem(isSelectAll)
            fileAdapter!!.notifyDataSetChanged()
            updateSelectAndManagePanel(false, mDeviceDisplayModel!!.mode)
        }

        override fun onDismiss() {
            setMultiModel(false, null)
        }
    }
    private val mFileManageListener = object : FileManagePanel.OnFileManageListener<OneOSFile> {
        override fun onClick(view: View, selectedList: List<OneOSFile>?, action: FileManageAction) {
            if (selectedList.isNullOrEmpty()) {
                ToastHelper.showToast(R.string.tip_select_file)
            } else {
                if (action != FileManageAction.MORE &&
                    action != FileManageAction.BACK &&
                    !CMAPI.getInstance().isConnected
                ) {
                    ToastHelper.showToast(R.string.network_not_available)
                    return
                }
                if (mDeviceDisplayModel!!.mLoginSession == null || mDeviceDisplayModel!!.mLoginSession?.isLogin != true) {
                    ToastHelper.showToast(R.string.tip_wait_for_service_connect)
                    return
                }
                isSelectionLastPosition = true
                val mFileType1 = mDeviceDisplayModel!!.mFileType
                val loadingCallback: ILoadingCallback? =
                    requireActivity().takeIf { it is ILoadingCallback } as ILoadingCallback?
                val fileManage = OneOSFileManage(requireActivity(),
                    loadingCallback,
                    mDeviceDisplayModel!!.mLoginSession!!,
                    mPathPanel,
                    OneOSFileManage.OnManageCallback {
                        if (it) {
                            when (action) {
                                FileManageAction.DELETE,
                                FileManageAction.DELETE_SHIFT,
                                FileManageAction.MOVE,
                                FileManageAction.RESTORE_RECYCLE -> {
                                    mFileList.removeAll(selectedList)
                                    mDeviceDisplayModel?.mOneOSFiles?.removeAll(selectedList)
                                    fileAdapter?.removeSelectedList()
                                }
                                FileManageAction.CLEAN_RECYCLE -> {
                                    if (FileManageAction.CLEAN_RECYCLE == action &&
                                        mDeviceDisplayModel!!.curPath != null &&
                                        mDeviceDisplayModel!!.curPath!!.startsWith(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR)
                                    ) {
                                        mDeviceDisplayModel!!.curPath =
                                            OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR
                                        Logger.LOGD(
                                            TAG,
                                            "CLEAN_RECYCLE",
                                            "=====Current Path: " + mDeviceDisplayModel!!.curPath + "========"
                                        )
                                        autoPullToRefresh() //clean recycle
                                    }
                                }
                                FileManageAction.FAVORITE,
                                FileManageAction.UNFAVORITE,
                                FileManageAction.RENAME -> {
                                    fileAdapter?.notifyDataSetChanged()
                                }
                                FileManageAction.DECRYPT,
                                FileManageAction.ENCRYPT -> {
                                    autoPullToRefresh()
                                }
                            }
                        }
                    })


                when (action) {
                    FileManageAction.MORE -> {
                        Logger.LOGD(TAG, "Manage More======")
                        updateSelectAndManagePanel(true, mDeviceDisplayModel!!.mode)
                    }
                    FileManageAction.TORRENT_CREATE -> {
                        showLoading()
                        val path = selectedList[0].getPath()
                        val sharePathType = selectedList[0].share_path_type
                        model.createTorrent(mDevId!!, path, sharePathType)
                            .observe(this@VP2QuickCloudNavFragment, Observer { resource ->
                                if (resource.status == Status.SUCCESS) {
                                    resource.data?.result?.let { btItem ->
                                        val isOwner = SessionManager.getInstance()
                                            .getDeviceModel(mDevId)?.isOwner
                                            ?: false
                                        model.showBtItemQRCodeView(
                                            requireActivity(),
                                            btItem,
                                            isOwner
                                        )
                                    }
                                }
                                dismissLoading()
                            })
                        showSelectAndOperatePanel(false)

                    }
                    FileManageAction.BACK -> updateSelectAndManagePanel(
                        false,
                        mDeviceDisplayModel!!.mode,
                        mFileSelectListener,
                        this
                    )

                    else -> {
                        if ((action == FileManageAction.DOWNLOAD) && SessionCache.instance.isNasV3(devId!!)
                        ) {
                            val requestKey = "select_device_${action.name}_$devId"
                            val isShareDownloadEnable =
                                mFileType1 == OneOSFileType.PRIVATE || mFileType1 == OneOSFileType.PUBLIC
                            if (isShareDownloadEnable/*&& BuildConfig.DEBUG*/) {
                                findNavController().navigate(
                                    R.id.global_action_to_selectDeviceFragment,
                                    SelectDeviceFragmentArgs(
                                        requestKey,
                                        SelectDeviceFragmentArgs.Companion.FilterType.FILE_SHARE,
                                        selectedList.find { it.isDirectory() } == null,
                                        arrayListOf(devId!!)
                                    ).toBundle()
                                )
                                setFragmentResultListener(
                                    requestKey = requestKey,
                                    listener = { requestKeyResult, bundle ->
                                        if (requestKey == requestKeyResult) {
                                            val mToDevId =
                                                bundle.getString(AppConstants.SP_FIELD_DEVICE_ID)
                                            if (mToDevId.isNullOrEmpty()){
                                                return@setFragmentResultListener
                                            }
                                            if (mToDevId == SELF) {
                                                normalManage(
                                                    action,
                                                    view,
                                                    selectedList,
                                                    fileManage,
                                                    mFileType1,
                                                        false
                                                )
                                                return@setFragmentResultListener
                                            }
                                            var rootPathType =
                                                NasFileConstant.CONTAIN_USER or NasFileConstant.CONTAIN_PUBLIC

                                            clearFragmentResult(requestKeyResult)
                                            selectPathToAction(
                                                action,
                                                mToDevId,
                                                rootPathType,
                                                selectedList,
                                                fileManage
                                            )
                                        }
                                    })
                            } else {
                                normalManage(action, view, selectedList, fileManage, mFileType1)
                            }
                            return
                        }
                        if ((action == FileManageAction.MOVE || action == FileManageAction.COPY)
                            && SessionCache.instance.isNasV3(devId!!)
                        ) {
                            var rootPathType =
                                NasFileConstant.CONTAIN_USER or NasFileConstant.CONTAIN_PUBLIC
                            if (devicePreViewModel.hasToolsServer(OneOSFileType.EXTERNAL_STORAGE)) {
                                rootPathType = rootPathType or NasFileConstant.CONTAIN_EXT_STORAGE
                            }
                            selectPathToAction(
                                action,
                                devId!!,
                                rootPathType,
                                selectedList,
                                fileManage
                            )
                            return
                        }
                        normalManage(action, view, selectedList, fileManage, mFileType1)
                    }
                }

            }
        }


        override fun onDismiss() {}
    }

    private fun normalManage(
        action: FileManageAction,
        view: View,
        selectedList: List<OneOSFile>,
        fileManage: OneOSFileManage,
        mFileType1: OneOSFileType,
        animationEnable:Boolean = true
    ) {
        if (animationEnable && action == FileManageAction.DOWNLOAD ) {
            //开始view
            val outLocation = IntArray(2)
            view.getLocationInWindow(outLocation)
            val startPoint = Point(outLocation[0], outLocation[1])
            //结束view
            val outLocation2 = IntArray(2)
            ibtn_nav_title_right2.getLocationInWindow(outLocation2)
            val endPoint = Point(outLocation2[0], outLocation2[1])

            val bezierViewHolder = BezierViewHolder(view.context).apply {
                setStartPosition(startPoint)
                setEndPosition(endPoint)
                files_root.addView(this)
                text = selectedList.count().toString()
            }
            bezierViewHolder.startBezierAnimation()
        }

        fileManage.manage(mFileType1, action, selectedList)
        showSelectAndOperatePanel(false)
    }

    private fun selectPathToAction(
        action: FileManageAction,
        mToDevId: String,
        rootPathType: Int,
        selectedList: List<OneOSFile>,
        fileManage: OneOSFileManage
    ) {
        val requestKeySelectPath = "select_path_${action.name}"
        val selectToPathFragmentArgs =
            SelectToPathFragmentArgs(
                mToDevId!!,
                rootPathType,
                action,
                requestKeySelectPath
            )
        findNavController().navigate(
            R.id.global_action_to_selectToPathFragment,
            selectToPathFragmentArgs.toBundle()
        )
        setFragmentResultListener(
            requestKey = requestKeySelectPath,
            listener = { requestKeyResult, bundle ->
                if (requestKeyResult == requestKeySelectPath) {
                    var devID = if (mToDevId != devId) {
                        mToDevId
                    } else null
                    val path = bundle.getString("path")
                    if (path.isNullOrEmpty()){
                        return@setFragmentResultListener
                    }
                    val sharePathType = bundle.getInt("sharePathType")
                    fileManage.manage(action, devID, sharePathType, path, selectedList,mDeviceDisplayModel?.mFileType!!)
                    showSelectAndOperatePanel(false)
                }
            })
    }


    private var mDeviceDisplayModel: FilesViewModel.DeviceDisplayModel? = null
    private var mAction: Runnable? = null


    //检测上传下载的任务数并显示
//    private var uploadCount = 0
//    private var downloadCount = 0
//    var transferCountListener: TransferManager.OnTransferCountListener = TransferManager.OnTransferCountListener { isDownload, count ->
//        if (isDownload) {
//            downloadCount = count
//        } else {
//            uploadCount = count
//        }
//        if (mActivity != null)
//            mActivity.runOnUiThread { refreshTrans() }
//    }

    internal var mSectionLetters = ArrayList<String>()
    internal var index = 0


    //    private SearchPanel.OnSearchActionListener mSearchListener = new SearchPanel.OnSearchActionListener() {
    override fun onVisible(visible: Boolean) {

    }

    override fun onSearch(filter: String) {
        mDeviceDisplayModel!!.isSearch = true
        mDevId?.let { mFilesViewModel!!.searchFiles(it, mDeviceDisplayModel!!.mFileType, filter) }
    }

    override fun onCancel() {
        // 加载缓存数据
        //        mFilesViewModel.loadLocalData(mDevId, mDeviceDisplayModel.curPath);
        onRefresh(false, mDeviceDisplayModel!!.mOneOSFiles?.toList())
        mDeviceDisplayModel!!.isSearch = false
    }
    //    };

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onProgress(contentProgress: Progress<Content>) {
        try {
            Logger.LOGD("EventMsgManager-VP2", contentProgress)
            if (!Objects.equals(mDevId, contentProgress.devId)) return

            when (contentProgress.action) {
                "share" -> {
                    val path = contentProgress.content?.path
                    val from = contentProgress.content?.from
                    val fname = path?.substring(path.lastIndexOf("/") + 1)
                    if (SPUtils.getValue(context, AppConstants.SP_FIELD_USERNAME) != from) {
                        val tips = String.format(
                            resources.getString(R.string.tip_receive_share_file),
                            from,
                            fname
                        )
                        ToastHelper.showToast(tips)
                    }

                }
                "extract", "encrypt", "decrypt", "copy", "move" -> {
                    val path =
                        contentProgress.content?.path//?.replace("\\s*|\t|\r|\n".toRegex(), "")
                    var progress = contentProgress.content?.progress
                    Timber.tag("EventMsgManager-VP2").d("filepath : {$path}")
                    if (!path.isNullOrEmpty()) {
                        if (OneOSFileType.getTypeByPath(path) == OneOSFileType.PRIVATE &&
                            !Objects.equals(
                                contentProgress.user,
                                SessionManager.getInstance().username
                            )
                        ) {
                            return
                        }
                        for (oneOSFile in mFileList) {
                            val filepath =
                                oneOSFile.getPath()//.replace("\\s*|\t|\r|\n".toRegex(), "")
                            if (Objects.equals(filepath, path)) {
                                if (progress == 100) progress = 0
                                oneOSFile.progress = progress ?: 0
                                fileAdapter!!.notifyItemChanged(oneOSFile)
                                break
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(args: Bundle) {
        args.let {
            val devId = it.getString(AppConstants.SP_FIELD_DEVICE_ID)
            if (Objects.equals(devId, mDevId)) {
                val path = it.getString(AppConstants.SP_FIELD_DEVICE_PATH)
                val type = it.getSerializable(AppConstants.SP_FIELD_FILE_TYPE)
                if (type != null) {
                    Timber.d("share>>vp2 receive by eventbus path : $path")
                    setFileType(type as OneOSFileType, path, DisplayMode.ALL)
                    EventBus.getDefault().removeStickyEvent(args)
                }
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(file: OneOSFile?) {
        if (file != null) {
            Logger.LOGD(TAG, "...onEvent... ", file)
            if (mFileList.contains(file)) {
                fileAdapter?.data?.let { data ->
                    var index = -1
                    for (i in data.indices) {
                        val entity = data[i]
                        if (!entity.isHeader && entity.t != null) {
                            if (file == entity.t) {
                                index = i
                                break
                            }
                        }
                    }
                    if (mRecyclerView != null && index != -1) {
                        mRecyclerView!!.scrollToPosition(index)
                    }
                }

            }
        }
        if (file != null) {
            EventBus.getDefault().removeStickyEvent(file)
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!onBackPressed()) {
                isEnabled = false
                requireActivity().onBackPressed()
                isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parentFragment = requireParentFragment()
        Timber.d("parentFragment : ${parentFragment.javaClass.name}")
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        mDevId?.let {
            mFilesViewModel.add(it)
            mFilesViewModel.setOSFileView(it, this@VP2QuickCloudNavFragment)
        }
        initParams()

        messageViewModel.messageCountLiveData.observe(this, androidx.lifecycle.Observer {
            messagesCount = it
            refreshTrans()
            menuPopupView?.setMenuCounts(mapOf(Pair(R.string.system_msg, it)))
        })
        transferCountViewModel.getLiveDataIncompleteCount(devId)
            .observe(this, androidx.lifecycle.Observer {
                transferCount = it
                refreshTrans()
                menuPopupView?.setMenuCounts(mapOf(Pair(R.string.transfer, it)))
            })
        transferCountViewModel.downloadCompleteLiveData.observe(this, androidx.lifecycle.Observer {
            onComplete(it)
        })
        shareViewModel2.shareElementV2sInComplete.observe(
            this,
            Observer<MutableList<ShareElementV2>?> { t ->
                sharesCount = t?.size ?: 0
                refreshTrans()
                menuPopupView?.let { view ->
                    view.setMenuCounts(mapOf(Pair(R.string.file_share, sharesCount)))
                }
            })
        if (!FilesCommonHelper.checkNetworkStatus()) {
            // 检查状态
        }

    }

    private fun initParams() {
        mDevId = vP2QuickCloudNavFragmentArgs.deviceid
        val path = vP2QuickCloudNavFragmentArgs.devicePath
        val type = vP2QuickCloudNavFragmentArgs.spFieldFileType
        mDeviceDisplayModel!!.mFileType = type
        val finalPath = path ?: OneOSFileType.getRootPath(type)
        mDeviceDisplayModel!!.curPath = finalPath
        isAndroidTV = FilesCommonHelper.getFileOperation(
            devId
                ?: "", type
        ) || type == OneOSFileType.EXTERNAL_STORAGE
        isEnableUseSpace = SessionManager.getInstance().getDeviceModel(devId)?.isEnableUseSpace
            ?: false
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_nav_cloud_files
    }


    override fun onResume() {
        super.onResume()
        devId?.let { deviceId ->
            devicePreViewModel.getToolItems(deviceId)
        }
        Logger.LOGD(TAG, ">>>>>>>>OnResume>>>>>>>" + mTypeBtn!!.text)
        mDeviceDisplayModel?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        if (mDeviceDisplayModel?.isChangedType == true) {
            initParams()
            mDeviceDisplayModel!!.isChangedType = false
            autoPullToRefresh()
        } else loadData()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        mDeviceDisplayModel?.mLoginSession?.apply {
            if (!isShareV2Available) {
                checkIfShareAvailable()
            }
        }
        val isRecycle = mDeviceDisplayModel!!.mFileType == OneOSFileType.RECYCLE
        layout_doc_tab.isVisible = OneOSFileType.DOCUMENTS == mDeviceDisplayModel!!.mFileType
        mSearchBtn?.isInvisible =
            isRecycle || mDeviceDisplayModel?.mFileType == OneOSFileType.EXTERNAL_STORAGE
        mSearchBtn?.isEnabled = !isRecycle
        refreshTrans()
    }

    override fun onPause() {
        super.onPause()
        Logger.LOGD(TAG, ">>>>>>>>On Pause>>>>>>>" + mTypeBtn!!.text)
        setMultiModel(false, null)
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        mDeviceDisplayModel?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

    }

    private var mBadgeViewFlag=false
    override fun onStop() {
        super.onStop()
        mQBadgeView?.hide(false)
        mQBadgeView=null
        mBadgeViewFlag=true
        Logger.LOGD(TAG, ">>>>>>>>>>onStop<<<<<<<<<<<<<$mDevId")
    }

    override fun onDetach() {
        super.onDetach()
        Logger.LOGD(TAG, ">>>>>>>>>>onDetach<<<<<<<<<<<<<$mDevId")

    }


    private var mQBadgeView: QBadgeView? = null
    private fun refreshTrans() {
        if( mQBadgeView?.badgeNumber?:0==transferCount)
            return
        val total = transferCount //+ messagesCount + sharesCount
        if (total > 0) {
            if (mQBadgeView == null) {
                mQBadgeView = QBadgeView(context).apply {
                    badgeGravity = Gravity.END or Gravity.TOP
                    bindTarget(ibtn_nav_title_right2)
                }
            }
            mQBadgeView?.badgeNumber = total
        } else {
            if (mQBadgeView != null) {
                mQBadgeView!!.hide(false)
            }
        }
    }

    override fun getTopView(): View? {
        initStatusBarPadding(mSearchPanel)
        initStatusBarPadding(mSelectPanel)
        return mTitleLayout
    }


    override fun initView(view: View) {
        mTitleLayout = view.findViewById(R.id.layout_title)
        mSelectPanel = view.findViewById(R.id.layout_select_top_panel)
        mSearchPanel = view.findViewById(R.id.layout_search_panel)
        mTypeBtn = view.findViewById(R.id.btn_sort)
        mTypeBtn!!.setOnClickListener {
//            mTypePopView!!.showPopupTop(mTitleLayout!!)
            val findNav = findNav(this@VP2QuickCloudNavFragment)
            if (findNav != null) {
                findNav.navigate(
                    R.id.action_VP2_to_selectTypeFragment,
                    SelectTypeFragmentArgs(devId).toBundle()
                )
                return@setOnClickListener
            }
        }
        (mTypeBtn as TextView).isSelected = true
        mTypeBtn?.setCompoundDrawables(null, null, null, null)
        mSearchBtn = view.findViewById(R.id.ibtn_nav_title_right)
        mSearchBtn!!.singleClick {
            val findNav = findNav(this@VP2QuickCloudNavFragment)
            val deviceId = vP2QuickCloudNavFragmentArgs.deviceid
            if (SessionCache.instance.isNasV3(deviceId) && findNav != null) {
                var fileType = vP2QuickCloudNavFragmentArgs.spFieldFileType
                var curPath = mDeviceDisplayModel?.curPath
                if (curPath != null) {
                    curPath = PathTypeCompat.getV5Path(curPath)
                }
                val pathType = PathTypeCompat.getSharePathTypeArray(fileType)

                findNav.navigate(
                    R.id.action_global_searchFragment, SearchFragmentArgs(
                        deviceId, fileType = fileType,
                        pathType = pathType, curPath = curPath
                    ).toBundle()
                )
            } else {
                mSearchPanel!!.showPanel(true)
            }
        }
        mTransBtn = view.findViewById(R.id.btn_trans)
        addSearchListener(this)

        mSlideInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_top)
        mSlideOutAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_to_top)

        mManagePanel = view.findViewById(R.id.layout_file_manage_panel)

        mErrorView = view.findViewById(R.id.include_error_view)
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        mRecyclerView = view.findViewById(R.id.recycle_view)
        mPathPanel = view.findViewById(R.id.layout_path_panel)
        mRecyclerView!!.visibility = View.VISIBLE

        mDeviceDisplayModel?.mFileType?.let {
            mRecyclerView?.removeItemDecoration(floatDecoration)
            if (OneOSFileType.isDB(it)) {
                mRecyclerView?.addItemDecoration(floatDecoration)
            }
        }
        initLayoutDocTab()
        initTypeView()
        initEvent()
        this.mTipsBar = tipsBar
    }

    private fun initLayoutDocTab() {
        sort_by.setOnClickListener {
            showOrderPopView(it)
        }
        val gridLayoutManager = GridLayoutManager(context, 5)
        val gridSpanMarginDecoration = GridSpanMarginDecoration(16, gridLayoutManager)
        recycle_view_tab.layoutManager = gridLayoutManager
        recycle_view_tab.addItemDecoration(gridSpanMarginDecoration)
        val documents = FileTypeHelper.getDocuments()
        val adapterTab =
            object : BaseQuickAdapter<FileTypeItem, BaseViewHolder>(R.layout.tab_text, documents) {
                override fun convert(p0: BaseViewHolder, item: FileTypeItem?) {
                    item?.let {
                        p0.setText(R.id.content, it.title)
                        p0.setChecked(R.id.content, it.flag == mDeviceDisplayModel!!.mFileType)
                    }
                }
            }
        recycle_view_tab.adapter = adapterTab
        adapterTab.setOnItemClickListener { baseQuickAdapter, view, i ->
            val item = baseQuickAdapter.getItem(i) as? FileTypeItem
            if (item != null) {
                val oneOSFileType = item.flag as OneOSFileType
                mDeviceDisplayModel!!.mFileType = oneOSFileType
                autoPullToRefresh()
                baseQuickAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initTypeView() {
        mTypePopView =
            TypePopupView(requireActivity(), FileTypeHelper.getFileTypes(), R.string.os_file_type)
        mTypePopView!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            if (FilesCommonHelper.checkNetworkStatus()) return@OnItemClickListener
            mCurrentPosition = position
            changeFragmentByPos(mCurrentPosition)
            mTypePopView!!.dismiss()
        })
        mUploadPopView = TypePopupView(
            requireActivity(),
            mUploadTypeList,
            R.string.please_select_file_type_upload
        )
        mUploadPopView!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            if (FilesCommonHelper.checkNetworkStatus()) return@OnItemClickListener
            Logger.LOGD(TAG, "onItemClick: position=$position")
            val item = mUploadTypeList.get(position)
            when (item.flag) {
                LocalFileType.NEW_FOLDER -> {
                    showNewFolder()
                    mUploadPopView!!.dismiss()
                    return@OnItemClickListener
                }
                LocalFileType.OFFLINE_DOWNLOAD -> {
                    devId?.let {
                        SessionManager.getInstance()
                            .getLoginSession(it, object : GetSessionListener() {
                                override fun onSuccess(url: String?, loginSession: LoginSession) {
                                    val url = OneOSAPIs.PREFIX_HTTP + loginSession.ip
                                    val intent = Intent(activity, WebViewActivity::class.java)
                                    var region = when {
                                        isHans() -> "zh-CN"
                                        isHant() -> "zh-TW"
                                        else -> "en"
                                    }

                                    intent.putExtra(
                                        WebViewActivity.WEB_VIEW_EXTRA_NAME_URL,
                                        "$url/transmission/web?lang=$region"
                                    )
                                    intent.putExtra(
                                        WebViewActivity.WEB_VIEW_EXTRA_NAME_TITLE,
                                        "Transmssion"
                                    )
                                    intent.putExtra(
                                        WebViewActivity.WEB_VIEW_EXTRA_NAME_HASTITLELAYOUT,
                                        false
                                    )

                                    startActivity(intent)
                                    mUploadPopView!!.dismiss()
                                }
                            })
                    }
                    return@OnItemClickListener
                }
                else -> {
                    if (item.flag is LocalFileType) {
                        val curPath = mFilesViewModel?.getPath(mDevId)
                        val finalFileType = item.flag as LocalFileType
                        val context = view.context

                        PermissionChecker.checkPermission(context, Callback {
                            getContext()?.runCatching {
                                jumpToUpload(this, finalFileType, curPath)
                            }
                        }, Callback {
                            getContext()?.run {
                                UiUtils.showStorageSettings(this)
                            }
                        }, Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }

        })
        fab.setOnClickListener { view ->
            if (Utils.isFastClick(view)) {
                return@setOnClickListener
            }
            val floatingActionButton = view as FloatingActionButton
            if (mUploadPopView?.isShow == true) {
                floatingActionButton.show()
                mUploadPopView!!.dismiss()
            } else {
                floatingActionButton.show()
                if (!FilesCommonHelper.checkNetworkStatus()) {
                    checkUploadTypeAvailable()
                    mUploadPopView!!.showPopupTop2(mTitleLayout!!)
                    floatingActionButton.hide()
                    mUploadPopView!!.setOnDismissListener { floatingActionButton.show() }
                }
            }
        }
    }

    private fun checkUploadTypeAvailable() {
        mUploadTypeList.remove(addItem)
        if (mDeviceDisplayModel?.curPath == null) {
            mUploadTypeList.remove(addItem)
        } else {
            mUploadTypeList.add(addItem)
        }
        mUploadTypeList.remove(offlineDownloadItem)
        val deviceModel = SessionManager.getInstance().getDeviceModel(mDevId)
        if (deviceModel != null && DevTypeHelper.isOneOSNas(deviceModel.devClass)
            && devId?.let { SessionCache.instance.isNasV3(it) } == false
        ) {
            mUploadTypeList.add(offlineDownloadItem)
        }
    }

    private fun initEvent() {
        btn_trans.singleClick {
            findNav(this)?.let {
                it.popBackStack()
            } ?: kotlin.run {
                onBackPressed()
            }
        }
        mErrorView!!.setOnClickListener {
            if (CMAPI.getInstance().isConnected) {
                mErrorView?.visibility = View.GONE
                include_refresh_view?.visibility = View.VISIBLE
                autoPullToRefresh()//error view click
            } else {
                ToastHelper.showToast(R.string.tip_wait_for_service_connect)
            }
        }
        mPathPanel!!.setNewFolderButtonRes(R.drawable.selector_button_upload)
        mPathPanel?.showOrderButton(true)
        mPathPanel!!.setOnPathPanelClickListener(FilePathPanel.OnPathPanelClickListener { view, path ->
            if (isRefreshing) {
                return@OnPathPanelClickListener
            }
            if (view.id == R.id.ibtn_new_folder) {
                //                    mAddPopView.showPopupDown(view, -1, true);
                if (mUploadPopView?.isShow == true) {
                    view.isSelected = false
                    mUploadPopView!!.dismiss()
                } else {
                    view.isSelected = false
                    if (!FilesCommonHelper.checkNetworkStatus()) {
                        checkUploadTypeAvailable()
                        mUploadPopView!!.showPopupTop2(view)
                        view.isSelected = true
                        mUploadPopView!!.setOnDismissListener { view.isSelected = false }
                    }
                }
            } else if (view.id == R.id.ibtn_order) {
                showOrderPopView(view)
            } else {
                Timber.d("PathPanel : $path")
                if (path != mDeviceDisplayModel?.curPath) {
                    wantto = path
                    isClickRefresh = true
                    autoPullToRefresh()// path panel click
                }
            }
        })
        mPathPanel!!.showNewFolderButton(/*mDeviceDisplayModel?.mFileType == OneOSFileType.PRIVATE
                || mDeviceDisplayModel?.mFileType == OneOSFileType.PUBLIC*/ false
        )


        fileAdapter = QuickSectionOneOSFilesRVAdapter(
            mRecyclerView!!.context, mFileList,
            mSelectedList
        )
        fileAdapter!!.mode = mDeviceDisplayModel?.mode

        mLoadMoreListener = BaseQuickAdapter.RequestLoadMoreListener {
            Timber.d("$mDevId vp2>> onPullUpToRefresh")
            onPullUpToRefresh()
        }
        mSwipeRefreshLayout!!.setOnRefreshListener {
            Timber.d("$mDevId share>> autoPullToRefresh:onRefresh")
            autoPullToRefresh() //onRefresh
        }

        fileAdapter!!.onItemClickListener = mOnItemClickListener
        fileAdapter!!.onItemLongClickListener = mOnItemLongClickListener
        fileAdapter!!.setOnLoadMoreListener(mLoadMoreListener, mRecyclerView)
        fileAdapter!!.onItemChildClickListener =
            BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                if (view.id == R.id.rv_list_ibtn_select) {
                    AnimUtils.shortVibrator()
                    val mAdapter = adapter as QuickSectionOneOSFilesRVAdapter
                    val data = mAdapter.data
                    val o = data[position]
                    if (o.isHeader) {
                        return@OnItemChildClickListener
                    }
                    setMultiModel(true, position)
                    updateSelectAndManagePanel(
                        false,
                        mDeviceDisplayModel!!.mode,
                        mFileSelectListener,
                        mFileManageListener
                    )
                }
            }
//        mRecyclerView?.addItemDecoration(DividerItemDecoration(mRecyclerView!!.context, RecyclerView.HORIZONTAL))
//        mRecyclerView?.addItemDecoration(DividerItemDecoration(mRecyclerView!!.context, RecyclerView.VERTICAL))
        mFooterView = LayoutInflater.from(mRecyclerView!!.context)
            .inflate(R.layout.layout_footer_view_single_line, null)
        fileAdapter!!.setFooterView(mFooterView!!)
        fileAdapter!!.setLoadMoreView(FileLoadMoreView())
        switchViewer(true)

        ibtn_nav_title_right2.setOnClickListener { v ->
            //            mMsgPopupView = MsgPopupView(mActivity)
//            mMsgPopupView?.showPopupTop(mTitleLayout!!)
//            mMsgPopupView?.setOnDismissListener {
//                //                mActivity?.finish()
//            }
            val clazz = TransferActivity::class.java
            startActivity(Intent(v.context, clazz).apply {
                putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
            })
            return@setOnClickListener
            val findNav = findNav(this@VP2QuickCloudNavFragment)
            if (findNav != null) {
//                findNav.navigate(R.id.action_VP2_to_selectTypeFragment, SelectTypeFragmentArgs(devId).toBundle())
                findNav.popBackStack()
                return@setOnClickListener
            }

            val isChinese = isHans() || isHant()

            val outMetrics = DisplayMetrics()
            val windowManager = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(outMetrics)
            val widthPixels = outMetrics.widthPixels
            menuPopupView =
                MenuPopupView(v.context, if (isChinese) widthPixels / 2 else widthPixels * 2 / 3)
            val titles = intArrayOf(
                R.string.system_msg,
                R.string.file_share,
                R.string.transfer,
                R.string.details_device
            )
            val resIds = intArrayOf(
                R.drawable.icon_sys_msg,
                R.drawable.icon_msg_share_v2,
                R.drawable.icon_msg_trans,
                R.drawable.icon_device_details
            )
            val counts = intArrayOf(messagesCount, sharesCount, transferCount, -1)
            menuPopupView!!.let { popupView ->
                popupView.setMenuItems(titles, resIds, counts)
                popupView.setOnMenuClickListener { index, view ->
                    var clazz: Class<*>? = null
                    when (index) {
                        0 -> clazz = SystemMessageActivity::class.java
                        1 -> clazz = ShareActivity::class.java
                        2 -> clazz = TransferActivity::class.java
                        3 -> clazz = DevicelDetailActivity::class.java
                    }
                    if (clazz != null) {
                        try {
                            if (DevicelDetailActivity::class.java == clazz) {
                                val intent = Intent(
                                    requireContext(),
                                    DevicelDetailActivity::class.java
                                ).apply {
                                    putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
                                }
                                DevicelDetailActivity.startActivity(requireActivity(), intent)
                            } else {
                                startActivity(Intent(view.context, clazz))
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

                //适配反转布局
                popupView.showPopupDown(v, -1, v.layoutDirection == View.LAYOUT_DIRECTION_LTR)
                popupView.setOnDismissListener { menuPopupView = null }
            }
//            mRecyclerView?.addOnScrollListener()
//----------PopupMenu 无图标----------
//            val popupMenu = PopupMenu(v.context, v, Gravity.BOTTOM)
//            popupMenu.inflate(R.menu.menu_msg)
//            popupMenu.setOnMenuItemClickListener {
//                var clazz: Class<*>? = null
//                when (it?.itemId) {
//                    R.id.msg_sys -> clazz = SystemMessageActivity::class.java
//                    R.id.msg_file_share -> clazz = ShareActivity::class.java
//                    R.id.msg_file_transfer -> clazz = TransferActivity::class.java
//                }
//                if (clazz != null) {
//                    startActivity(Intent(context, clazz))
//                    return@setOnMenuItemClickListener true
//                }
//                false
//            }
//            popupMenu.show()

        }
        val listener = object : RecyelerViewScrollDetector() {
            var isVisible = false
            override fun onScrollUp() {
                if (isVisible) {
                    fab?.showAndEnable()
                    isVisible = false
                }
            }

            override fun onScrollDown() {
                if (fab?.isVisible == true) {
                    isVisible = true
                    fab?.hideAndDisable()
                }
            }
        }
        mRecyclerView?.let {
            fab?.hideAndDisable()
            listener.setScrollThreshold(dp2px(it.context, 4))
            it.addOnScrollListener(listener)
        }
    }


    private fun loadData() {
        if (mDeviceDisplayModel != null) {
            if (!TextUtils.isEmpty(mDevId)) {
                SessionManager.getInstance()
                    .getLoginSession(mDevId!!, object : GetSessionListener(false) {
                        override fun onSuccess(url: String?, data: LoginSession?) {
                            if (isResumed && data != null) {
                                mDeviceDisplayModel!!.mLoginSession = data
                                Timber.d("$mDevId share>> autoPullToRefresh:loadData")
                                autoPullToRefresh()//loadData
                            }
                        }
                    })
            }
        }

        refreshDevNameById(mDevId)
    }

    private fun refreshDevNameById(mDevId: String?) {
        if (mDevId != null) {
            deviceViewModel.refreshDevNameById(mDevId)
                .`as`(RxLife.`as`(this))
                .subscribe({
                    mTypeBtn?.text = it
                }, { t: Throwable? ->

                })
        }

    }

    private fun checkSessionIsAvailable(requestSession: Boolean): Boolean {
        if (SessionManager.getInstance().isLogin(mDevId)) {
            return true
        }
        if (requestSession) {
            mDevId?.let {
                SessionManager.getInstance()
                    .getLoginSession(it, object : GetSessionListener(requestSession) {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            mDeviceDisplayModel?.mLoginSession = loginSession
                        }
                    })
            }
            ToastHelper.showLongToast(R.string.tip_wait_for_service_connect)
        }
        return false
    }


    private fun onPullDownToRefresh() {
        if (mDeviceDisplayModel!!.isSearch) {
            mSearchPanel?.searchFilter?.let {
                onSearch(it)
            }
        } else {
            setMultiModel(false, null)
            val path = if (isClickRefresh) wantto else mDeviceDisplayModel!!.curPath
            Timber.d("$mDevId pull down vp2 path : $path  curPath : ${mDeviceDisplayModel!!.curPath}")
            mDevId?.let {
                mFilesViewModel!!.getFiles(
                    it, mDeviceDisplayModel!!.mFileType,
                    path,
                    if (isClickRefresh) 0 else 0, false, mOrderType
                )
            }
            isClickRefresh = false
            isSelectionLastPosition = false
        }
    }

    private fun onPullUpToRefresh() {
//        setMultiModel(false, null)
        if (mDeviceDisplayModel?.isSearch == true) {
//            if (mDeviceDisplayModel!!.mPage < mDeviceDisplayModel!!.mPages - 1) {
//                mDevId?.let {
//                    mFilesViewModel!!.searchFilesMore(it)
//                }
//            } else {
            fileAdapter!!.loadMoreEnd()
//            }
        } else {
            if (mDeviceDisplayModel!!.mPage < mDeviceDisplayModel!!.mPages - 1) {
                mDevId?.let {
                    mFilesViewModel!!.getFiles(
                        it, mDeviceDisplayModel!!.mFileType,
                        mDeviceDisplayModel!!.curPath,
                        ++mDeviceDisplayModel!!.mPage, true, mOrderType
                    )
                }
            } else {
                fileAdapter!!.loadMoreEnd()
            }
            isSelectionLastPosition = false
        }
    }

    override fun showNewFolder() {
        isSelectionLastPosition = true
        val fileManage = mDeviceDisplayModel!!.mLoginSession?.let {
            OneOSFileManage(requireActivity(), null, it, mPathPanel,
                OneOSFileManage.OnManageCallback { isSuccess ->
                    if (isSuccess) {
                        autoPullToRefresh() //new Folder success
                    }
                })
        }

        mDeviceDisplayModel?.curPath?.let {
            val share_path_type = PathTypeCompat.getSharePathType(mDeviceDisplayModel!!.mFileType)
            fileManage?.manage(FileManageAction.MKDIR, it, share_path_type.type)
        }
    }


    var popup: SortMenuPopupViewV2? = null
    private fun showOrderPopView(view: View) {
        if (popup == null) {
            popup = SortMenuPopupViewV2(
                view.context,
                mOrderType,
                if (isListShown) FileViewerType.LIST else FileViewerType.GRID
            ).apply {
                setOnOrderCheckedChangeListener(object : OnCheckedChangeListener<FileOrderTypeV2> {
                    override fun onCheckedChangeListener(orderType: FileOrderTypeV2) {
                        if (orderType != mOrderType) {
                            mOrderType = orderType
                            mUserSettings?.fileOrderType = orderType.ordinal
                            DeviceSettingsKeeper.update(mUserSettings)
                            onRefresh(false, mDeviceDisplayModel?.mOneOSFiles?.toList())
                            dismiss()
                        }
                    }
                })
                setOnViewerCheckedChangeListener(object : OnCheckedChangeListener<FileViewerType> {
                    override fun onCheckedChangeListener(type: FileViewerType) {
                        isListShown = type == FileViewerType.LIST
                        mUserSettings?.fileViewerType = type.ordinal
                        DeviceSettingsKeeper.update(mUserSettings)
                        switchViewer(isListShown)
                        dismiss()
                    }
                })
            }

//            val baseViewHolder = popup!!.baseViewHolder
//            baseViewHolder.setOtherClickListener { v ->
//                var orderType = mOrderType
//                var isList = isListShown
//                when (v.id) {
//                    R.id.sort_by_name -> {
//                        orderType = FileOrderTypeV2.time_asc
//
//                    }
//                    R.id.sort_by_time -> {
//                        orderType = FileOrderTypeV2.time_desc
//                    }
//                    R.id.display_by_grid -> isList = false
//                    R.id.display_by_list -> isList = true
//                }
//                if (orderType != mOrderType) {
//                    mOrderType = orderType
//                    mUserSettings?.fileOrderType = DeviceSettingsKeeper.getFileOrderTypeID(mOrderType)
//                    DeviceSettingsKeeper.update(mUserSettings)
//                    onRefresh(false, mDeviceDisplayModel?.mOneOSFiles?.toList())
//                }
//                if (isList != isListShown) {
//                    isListShown = isList
//                    mUserSettings?.fileViewerType = DeviceSettingsKeeper.getFileViewerTypeID(if (isListShown) FileViewerType.LIST else FileViewerType.GRID)
//                    DeviceSettingsKeeper.update(mUserSettings)
//                    switchViewer(isListShown)
//                }
//                popup!!.dismiss()
//            }
        }
//        popup!!.baseViewHolder.getView<CompoundButton>(R.id.display_by_list).isChecked = isListShown
//        popup!!.baseViewHolder.getView<CompoundButton>(R.id.display_by_grid).isChecked = !isListShown
//        popup!!.baseViewHolder.getView<CompoundButton>(R.id.sort_by_name).isChecked = FileOrderType.isName(mOrderType)
//        popup!!.baseViewHolder.getView<CompoundButton>(R.id.sort_by_time).isChecked = !FileOrderType.isName(mOrderType)
        popup!!.showAsDropDown(view, dp2px(requireContext(), 16), 0)
        popup!!.isOutsideTouchable = true
        popup!!.isFocusable = true
        popup!!.update()
        view.isSelected = true
        popup!!.setOnDismissListener {
            view.isSelected = false
            WindowUtil.hintShadow(activity)
        }
        WindowUtil.showShadow(activity)

//        val order = if (FileOrderType.isName(mOrderType)) R.string.file_order_time else R.string.file_order_name
//        val viewer = if (isListShown) R.string.file_viewer_grid else R.string.file_viewer_list
//        val items = intArrayOf(order, viewer)
//        mOrderPopView = MenuPopupView(mActivity, Utils.dipToPx(130f))
//        mOrderPopView!!.setMenuItems(items, null)
//        mOrderPopView!!.setOnMenuClickListener { index, view ->
//            if (index == 0) {
//                if (mOrderType == FileOrderType.NAME) {
//                    mOrderType = FileOrderType.TIME
//                } else {
//                    mOrderType = FileOrderType.NAME
//                }
//                if (mUserSettings != null)
//                    mUserSettings!!.fileOrderType = UserSettingsKeeper.getFileOrderTypeID(mOrderType)
//            } else {
//                isListShown = !isListShown
//                if (mUserSettings != null)
//                    mUserSettings!!.fileViewerType = UserSettingsKeeper.getFileViewerTypeID(if (isListShown) FileViewerType.LIST else FileViewerType.GRID)
//            }
//            UserSettingsKeeper.update(mUserSettings)
//            //                notifyRefreshComplete(true);
//            switchViewer(isListShown)
//        }
//        mOrderPopView!!.showPopupDown(view, -1, false)
    }

    private var itemDecoration: RecyclerView.ItemDecoration? = null

    private fun switchViewer(isListShown: Boolean) {
        if (mRecyclerView == null) return
        var isList: Boolean? = null
        val layoutManager = mRecyclerView!!.layoutManager
        var scrollPosition = 0
        if (mRecyclerView!!.layoutManager != null) {
            scrollPosition = (mRecyclerView!!.layoutManager as GridLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        }
        if (layoutManager is GridLayoutManager) {
            isList = layoutManager.spanCount == 1
        }

        if (isList == null || isList != isListShown) {
            val layoutManager = if (isListShown) {
                GridLayoutManager(mRecyclerView!!.context, 1)
            } else {
                GridLayoutManager(mRecyclerView!!.context, 4)
            }
            mRecyclerView!!.layoutManager = layoutManager
            val margin = dp2px(mRecyclerView!!.context, 2)
            if (itemDecoration != null) {
                mRecyclerView!!.removeItemDecoration(itemDecoration!!)
            }
            itemDecoration = GridSpanMarginDecoration(margin, layoutManager)
            mRecyclerView!!.addItemDecoration(itemDecoration!!)
            fileAdapter!!.toggleViewerType(if (isListShown) FileViewerType.LIST else FileViewerType.GRID)
            mRecyclerView!!.adapter = fileAdapter
            mRecyclerView!!.scrollToPosition(scrollPosition)
        }
    }


    private fun backToParentDir(path: String) {
        val parentPath = FileUtils.getParentPath(path)
        Logger.LOGD(TAG, "----Parent Path: $parentPath------")
        isSelectionLastPosition = true
        wantto = parentPath
        isClickRefresh = true
        autoPullToRefresh()//back to parent dir
    }

    private fun tryBackToParentDir(): Boolean {
        val devicePath = vP2QuickCloudNavFragmentArgs.devicePath
        val isNotDevPath =
            !FileUtils.pathEqualsIgnoreLastSeparator(mDeviceDisplayModel?.curPath, devicePath)
        if (mDeviceDisplayModel?.curPath != null) {
            val curPath = mDeviceDisplayModel!!.curPath
            if (curPath == OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR) {
                return false
            }
            if (mDeviceDisplayModel!!.mFileType == OneOSFileType.PRIVATE) {
                if (isNotDevPath && curPath != OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR) {
                    backToParentDir(curPath!!)
                    return true
                }
            }
            if (mDeviceDisplayModel!!.mFileType == OneOSFileType.PUBLIC) {
                if (isNotDevPath && curPath != OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR) {
                    backToParentDir(curPath!!)
                    return true
                }
            }
            if (mDeviceDisplayModel!!.mFileType == OneOSFileType.RECYCLE) {
                if (isNotDevPath && curPath != OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR) {
                    backToParentDir(curPath!!)
                    return true
                }
            }
            if (mDeviceDisplayModel!!.mFileType == OneOSFileType.EXTERNAL_STORAGE) {
                if (isNotDevPath && curPath != OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR) {
                    backToParentDir(curPath!!)
                    return true
                }
            }
        }
        return false
    }

    override fun autoPullToRefresh() {
        if (CMAPI.getInstance().isConnected) {
            if (mSwipeRefreshLayout != null) {
                isRefreshing = true
                mSwipeRefreshLayout!!.isRefreshing = true
                if (mAction == null)
                    mAction = Runnable {
                        if (isRefreshing) {
                            showRefreshLayout(View.VISIBLE, false)
                        }
                    }
                mSwipeRefreshLayout!!.postDelayed(mAction, (10 * 1000).toLong())
            }
            onPullDownToRefresh()
        } else {
            showRefreshLayout(View.VISIBLE, false)
            include_refresh_view?.visibility = View.GONE
            include_error_view?.visibility = View.VISIBLE
        }
    }

//    private void notifyRefreshComplete(boolean isItemChanged) {
//        if (isItemChanged)
//            onRefresh(false, mFileList);
//    }


    override fun setMultiModel(isSetMultiModel: Boolean, position: Int?): Boolean {
        val curIsMultiModel = fileAdapter!!.isMultiChooseModel
        if (curIsMultiModel == isSetMultiModel) {
            return false
        }

        fileAdapter!!.setIsMultiModel(isSetMultiModel)
        return if (isSetMultiModel) {
            updateSelectAndManagePanel(
                false,
                mDeviceDisplayModel!!.mode,
                mFileSelectListener,
                mFileManageListener
            )
            showSelectAndOperatePanel(true)
            if (position != null) {
                fileAdapter?.toggleItemSelected(position)
            }
            true
        } else {
            showSelectAndOperatePanel(false)
            true
        }
    }

    private fun showRefreshLayout(visible: Int, b: Boolean) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout!!.visibility = visible
            mSwipeRefreshLayout!!.isRefreshing = b
            if (mAction != null) {
                mSwipeRefreshLayout!!.removeCallbacks(mAction)
            }
        }
    }


    private fun showErrorView(strRes: Int) {
        showErrorView(resources.getString(strRes))
    }

    private fun showErrorView(finalErrorMsg: String) {
        showRefreshLayout(View.GONE, false)
        if (mErrorView != null) {
            mErrorView!!.visibility = View.VISIBLE
            (mErrorView!!.findViewById<View>(R.id.txt_error) as TextView).text = finalErrorMsg
        }
    }

    private fun refreshModeByPages() {
        fileAdapter!!.setEnableLoadMore(mDeviceDisplayModel!!.mPage < mDeviceDisplayModel!!.mPages)
    }


    override fun onBackPressed(): Boolean {
        Logger.LOGD(TAG, "onBackPressed ", " devid :", mDevId)
        if (!isResumed)
            return false
        val fileAdapter = fileAdapter
        if (fileAdapter != null && fileAdapter.isMultiChooseModel) {
            showSelectAndOperatePanel(false)
            return true
        }
        if (mSearchPanel != null && mSearchPanel!!.visibility == View.VISIBLE) {
            mSearchPanel!!.cancel()
            return true
        }
        if (tryBackToParentDir()) {
            return true
        }
        val findNav = findNav(this@VP2QuickCloudNavFragment)
        if (findNav != null) {
//            findNav.navigate(R.id.action_VP2_to_selectTypeFragment, SelectTypeFragmentArgs(devId).toBundle())
            findNav.popBackStack()
            return true
        }
        return false
    }


    private fun jumpToUpload(context: Context, finalFileType: LocalFileType, curPath: String?) {
        val intent = Intent(context, UploadActivity::class.java)
        val fileType = (vP2QuickCloudNavFragmentArgs.spFieldFileType
            ?: mFileType)
        intent.putExtra("fileType", finalFileType)
        if (!mDevId.isNullOrEmpty()) {
            intent.putExtra(
                "path",
                FilesCommonHelper.getUploadPath(mDevId!!, context, curPath, fileType)
            )
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, mDevId)
        }
        startActivity(intent)
        mUploadPopView?.dismiss()
//        requireActivity().finish()
    }


    private fun changeFragmentByPos(position: Int) {
        val item = FileTypeHelper.getFileTypes()[position]
        val type = item.flag as OneOSFileType
        if (type == mDeviceDisplayModel!!.mFileType) return
        changeFragmentByType(type)
    }

    private fun changeFragmentByType(type: OneOSFileType) {
        val path = OneOSFileType.getRootPath(type)
        if (OneOSFileType.isDir(type)) {
            mPathPanel?.updatePath(type, path)
        }
        if (type == OneOSFileType.RECYCLE) {
            mSearchBtn!!.isEnabled = false
            mSearchBtn!!.visibility = View.GONE
        } else {
            mSearchBtn!!.isEnabled = true
            mSearchBtn!!.visibility = View.VISIBLE
        }
        Logger.LOGD(TAG, "changeFragmentByType", " Path: $path", "Type:$type")
        setFileType(type, path, DisplayMode.ALL)
    }

    override fun setFileType(type: OneOSFileType, path: String?, mode: DisplayMode) {
        if (this.mDeviceDisplayModel?.mFileType != type) {
            mRecyclerView?.removeItemDecoration(floatDecoration)
            if (OneOSFileType.isDB(type)) {
                mRecyclerView?.addItemDecoration(floatDecoration)
            }
        }
        var displayMode = mode
        if (mode == DisplayMode.ALL) {
            if (type == OneOSFileType.PUBLIC) {
                displayMode = DisplayMode.PUBLIC
            }
        }
        mDeviceDisplayModel!!.mode = displayMode
        mDeviceDisplayModel!!.mFileType = type
        val finalPath = path ?: OneOSFileType.getRootPath(type)
        mDeviceDisplayModel!!.curPath = finalPath

        Logger.LOGD(
            TAG,
            "share>> setFileType $type =====Current Path: " + mDeviceDisplayModel!!.curPath + "========"
        )
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .`as`(RxLife.asOnMain(this))
            .subscribe {
                mDeviceDisplayModel!!.mode = displayMode
                mDeviceDisplayModel!!.mFileType = type
                mDeviceDisplayModel!!.curPath = finalPath
                Timber.d("share>> doRefresh  =====Current Path: ${mDeviceDisplayModel?.curPath}  ========")
                autoPullToRefresh()//select FileType
            }

    }


    fun addSearchListener(listener: SearchPanel.OnSearchActionListener) {
        mSearchPanel!!.setOnSearchListener(listener)
    }


    fun showSelectBar(isShown: Boolean) {
        if (isShown) {
            mSelectPanel!!.showPanel(true)
        } else {
            mSelectPanel!!.hidePanel(true)
        }
    }

    override fun showTipMessage(content: String) {

    }

    override fun showLoading() {
        val requireActivity = requireActivity()
        if (requireActivity is BaseActivity) {
            requireActivity.showLoading(R.string.loading, true)
        }
    }

    private fun dismissLoading() {
        val requireActivity = requireActivity()
        if (requireActivity is BaseActivity) {
            requireActivity.dismissLoading()
        }
    }

    override fun showSelectAndOperatePanel(isShown: Boolean) {
        showSelectBar(isShown)
        showManageBar(isShown)
        //        mActivity.showAddBtn(!isShown);
    }


    override fun updateSelectAndManagePanel(
        isMore: Boolean,
        mode: DisplayMode?,
        mFileSelectListener: FileSelectPanel.OnFileSelectListener?,
        mFileManageListener: FileManagePanel.OnFileManageListener<OneOSFile>?
    ) {
        var size = mFileList.size
        if (mode != null && mode == DisplayMode.SHARE) {
            for (oneOSFile in mFileList) {
                if (oneOSFile.isDirectory())
                    size--
            }
        }
        updateSelectBar(size, mSelectedList.size, mFileSelectListener!!)
        updateManageBar(
            mDeviceDisplayModel!!.mFileType,
            mSelectedList,
            isMore,
            mFileManageListener!!
        )
    }

    fun updateSelectAndManagePanel(isMore: Boolean, mode: DisplayMode?) {
        updateSelectAndManagePanel(isMore, mode, mFileSelectListener, mFileManageListener)
    }


    override fun onLoadMore(oneOSFiles: List<OneOSFile>?) {
        refreshModeByPages()
        if (mDeviceDisplayModel!!.mode != null && mDeviceDisplayModel!!.mode == DisplayMode.SHARE) {
            updateSelectAndManagePanel(
                false,
                mDeviceDisplayModel!!.mode,
                mFileSelectListener,
                mFileManageListener
            )
        }

        if (mErrorView != null) {
            mErrorView!!.visibility = View.GONE
        }
        switchViewer(isListShown)
        if (oneOSFiles != null) {
            val mFileType1 = mDeviceDisplayModel!!.mFileType
            val entities = orderFiles(mFileType1, oneOSFiles)
            mData.addAll(entities)
            fileAdapter!!.addData(entities, mDeviceDisplayModel!!.mLoginSession)
            mFileList.addAll(oneOSFiles)
        }
        //        mPathPanel.updatePath(mDeviceDisplayModel.mFileType, mDeviceDisplayModel.curPath);
        var numFolders = 0
        var numFiles = 0
        for (oneOSFile in mFileList) {
            if (oneOSFile.isDirectory())
                numFolders++
            else
                numFiles++
        }
        if (mFooterView != null) {
            val viewInfo = mFooterView!!.findViewById<TextView>(R.id.footer_text_info)
            viewInfo.text = String.format(
                "%1\$s, %2\$s",
                resources.getQuantityString(R.plurals.folders, numFolders, numFolders),
                resources.getQuantityString(R.plurals.files, numFiles, numFiles)
            )
            viewInfo.visibility = if (mFileList.size > 0) View.VISIBLE else View.GONE
        }
        if (mDeviceDisplayModel!!.mPage >= mDeviceDisplayModel!!.mPages - 1) {
            fileAdapter!!.loadMoreEnd()
        } else {
            fileAdapter!!.loadMoreComplete()
        }
        if (isSelectionLastPosition && mLastClickPosition != -1) {
            mRecyclerView!!.scrollToPosition(mLastClickPosition)
            isSelectionLastPosition = false
        }/* else {
            mRecyclerView!!.scrollToPosition(mDeviceDisplayModel!!.mPage * AppConstants.PAGE_SIZE)
        }*/

        isRefreshing = false
        showRefreshLayout(View.VISIBLE, false)
        updateSelectAndManagePanel(false, mDeviceDisplayModel!!.mode)
    }

    override fun onRefresh(isSearch: Boolean, oneOSFiles: List<OneOSFile>?) {
        val mFileType1 = mDeviceDisplayModel!!.mFileType
        fileAdapter?.mode = mDeviceDisplayModel?.mode
        if (oneOSFiles.isNullOrEmpty()) {
            mRecyclerView?.context?.let {
                fileAdapter!!.emptyView = LayoutInflater.from(it)
                    .inflate(R.layout.layout_empty_directory, null)
            }
        }
        include_refresh_view?.visibility = View.GONE
        refreshModeByPages()
        refreshDevNameById(mDevId)
        val curPath = mDeviceDisplayModel!!.curPath
        Timber.d("onRefresh curPath: $curPath  $isAndroidTV")
        //如果是android tv nas 设备根目录 隐藏上传添加图标 禁止操作项
        if ((isAndroidTV && (curPath == OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR
                    || curPath == OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR
                    || curPath == OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR
                    || curPath == OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR))
        ) {
            updateSelectAndManagePanel(
                false, mDeviceDisplayModel!!.mode,
                mFileSelectListener, mFileManageListener
            )
            fileAdapter?.isMultiChooseEnable = false
            fab?.hideAndDisable()
            mPathPanel?.showNewFolderButton(false)
        } else {
            fab?.showAndEnable()
            mPathPanel?.showNewFolderButton(false)
            fileAdapter?.isMultiChooseEnable = true
        }
        //
        if ((mDeviceDisplayModel!!.mode != null &&
                    mDeviceDisplayModel!!.mode == DisplayMode.SHARE)
        ) {
            fab?.hideAndDisable()
            mPathPanel?.showNewFolderButton(false)
        }
        // 回收站隐藏  上传添加图标
        if (mFileType1 == OneOSFileType.RECYCLE) {
            fab?.hideAndDisable()
        }
        if (!isEnableUseSpace) {
            fab?.hideAndDisable()
        }

        if (mErrorView != null) {
            mErrorView!!.visibility = View.GONE
        }
        if (mUserSettings == null) {
            mUserSettings = mDeviceDisplayModel?.mLoginSession?.deviceSettings
        }
        if (mUserSettings != null) {
            val isList = FileViewerType.isList(mUserSettings!!.fileViewerType!!)
            if (isList != isListShown) {
                isListShown = isList
            }
            mOrderType = FileOrderTypeV2.getType(mUserSettings!!.fileOrderType!!)
        }
        switchViewer(isListShown)
        mData.clear()
        mFileList.clear()
        if (oneOSFiles != null) {
            if (mSectionLetters == null) {
                mSectionLetters = ArrayList()
            } else {
                mSectionLetters.clear()
            }
            index = 0
            val entities = orderFiles(mFileType1, oneOSFiles)
            mData.addAll(entities)
            mFileList.addAll(oneOSFiles)
        }
        fileAdapter!!.replaceData(mData, mDeviceDisplayModel!!.mLoginSession)
        fileAdapter?.isShowMedia =
            mFileType1 == OneOSFileType.PICTURE || mFileType1 == OneOSFileType.VIDEO
        if (!isSearch) {
            val showPath = if (OneOSFileType.isDir(mFileType1)) {
                if (mFileType1 == OneOSFileType.PUBLIC) {
                    curPath?.let {
                        if (it.startsWith(File.separator)) {
                            it.replaceFirst(
                                OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR,
                                OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR
                            )
                        } else {
                            it
                        }
                    }
                } else {
                    curPath
                }
            } else {
                when (mFileType1) {
                    OneOSFileType.PICTURE, OneOSFileType.AUDIO, OneOSFileType.VIDEO -> {
                        UIUtils.getContext().getString(OneOSFileType.getTypeName(mFileType1))
                    }
                    else -> {
                        null
                    }
                }
            }
            mPathPanel?.updatePath(mFileType1, showPath)
        } else {
            mPathPanel?.visibility = View.GONE
        }
        var numFolders = 0
        var numFiles = 0
        for (oneOSFile in mFileList) {
            if (oneOSFile.isDirectory())
                numFolders++
            else
                numFiles++
        }
        if (mFooterView != null) {
            val viewInfo = mFooterView!!.findViewById<TextView>(R.id.footer_text_info)
            viewInfo.text = String.format(
                "%1\$s, %2\$s",
                mFooterView!!.context.resources.getQuantityString(
                    R.plurals.folders,
                    numFolders,
                    numFolders
                ),
                mFooterView!!.context.resources.getQuantityString(
                    R.plurals.files,
                    numFiles,
                    numFiles
                )
            )
            viewInfo.visibility = if (mFileList.size > 0) View.VISIBLE else View.GONE
        }
        if (mDeviceDisplayModel!!.mPage >= mDeviceDisplayModel!!.mPages - 1) {
            fileAdapter!!.loadMoreEnd()
        } else {
            fileAdapter!!.loadMoreComplete()
        }
        if (isSelectionLastPosition && mLastClickPosition != -1) {
            mRecyclerView!!.scrollToPosition(mLastClickPosition)
            isSelectionLastPosition = false
        }
        isRefreshing = false
        showRefreshLayout(View.VISIBLE, false)
    }

    private fun orderFiles(
        mFileType1: OneOSFileType,
        oneOSFiles: List<OneOSFile>
    ): ArrayList<SectionEntity<DataFile>> {
        FileSortHelper.sortWith(mFileType1, mOrderType, oneOSFiles)
        val entities = ArrayList<SectionEntity<DataFile>>()
        for (file in oneOSFiles) {
            if (OneOSFileType.isDB(mFileType1) && mOrderType.ordinal <= FileOrderTypeV2.time_asc.ordinal) {
                var letter = FileUtils.formatTime(file.getTime() * 1000, fmt)
                if (mFileType1 == OneOSFileType.PICTURE) {
                    letter = FileUtils.formatTime(file.cttime * 1000, fmt)
                }
                if (!mSectionLetters.contains(letter)) {
                    mSectionLetters.add(letter)
                    file.section = index
                    entities.add(SectionEntity(true, letter))
                    index++
                } else {
                    file.section = mSectionLetters.indexOf(letter)
                }
            }
            entities.add(SectionEntity(file))
        }
        return entities
    }

    private var notifyDialog: Dialog? = null
    override fun onLoadFailed(err: String, errNo: Int) {
        include_refresh_view?.visibility = View.GONE
        showErrorView(err)
        isRefreshing = false
        if (errNo == V5_ERR_DENIED_PERMISSION) {
            lifecycleScope.launchWhenResumed {
                if (notifyDialog == null || notifyDialog?.isShowing == false) {
                    notifyDialog = DialogUtils.showNotifyDialog(
                        requireContext(), 0,
                        R.string.ec_no_permission, R.string.confirm
                    ) { _, isPositiveBtn ->
                        if (isPositiveBtn) {
                            val findNav = findNav(this@VP2QuickCloudNavFragment)
                            findNav?.navigate(
                                R.id.action_VP2_to_selectTypeFragment,
                                SelectTypeFragmentArgs(devId).toBundle()
                            )
                        }
                        notifyDialog = null
                    }
                }
            }
        }
    }

    override fun onRestoreState(deviceDisplayModel: FilesViewModel.DeviceDisplayModel) {
        mDeviceDisplayModel = deviceDisplayModel
    }

    private fun updateSelectBar(
        totalCount: Int,
        selectedCount: Int,
        mListener: FileSelectPanel.OnFileSelectListener
    ) {
        mSelectPanel!!.setOnSelectListener(mListener)
        mSelectPanel!!.updateCount(totalCount, selectedCount)

    }

    private fun showManageBar(isShown: Boolean) {
//        if (mActivity is MangerBarInterface<*, *>)
//            (mActivity as MangerBarInterface<*, *>).showManageBar(isShown)
        if (parentFragment != null && parentFragment is MangerBarInterface<*, *>) {
            (parentFragment as MangerBarInterface<*, *>).showManageBar(isShown)
        }

        if (isShown) {
            mManagePanel!!.showPanel()
        } else {
            mManagePanel!!.hidePanel()
        }
    }


    private fun updateManageBar(
        fileType: OneOSFileType,
        selectedList: ArrayList<DataFile>,
        isMore: Boolean?,
        mListener: FileManagePanel.OnFileManageListener<OneOSFile>
    ) {
//        if (mActivity is MangerBarInterface<*, *>)
//            (mActivity as MangerBarInterface<OneOSFileType, OneOSFile>).updateManageBar(fileType, selectedList, isMore, mListener)
        mManagePanel!!.setOnOperateListener(mListener)
//        if (isMore!!) {
//            mManagePanel!!.updatePanelItemsMore(fileType, selectedList)
//        } else {
        mManagePanel!!.updatePanelItems(fileType, selectedList, mDeviceDisplayModel?.mLoginSession)
//        }
    }

    private fun onComplete(element: DownloadElement) {
        if (activity != null && Objects.equals(element.srcDevId, mDevId)) {
            val file = element.file
            if (file != null) {
                lifecycleScope.launchWhenResumed {
                    if (fileAdapter != null) {
                        fileAdapter!!.notifyItemChanged(file)
                    }
                }
            }
        }
    }

    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
        super.onNetworkChanged(isAvailable, isWifiAvailable)
        fileAdapter?.setWifiAvailable(isWifiAvailable)
    }

    override fun onStatusConnection(statusCode: Int) {
        super.onStatusConnection(statusCode)
        if (statusCode == STATUS_CODE_ESTABLISHED) {
            refreshDevNameById(mDevId)
            if (include_refresh_view?.visibility == View.VISIBLE) {
                include_refresh_view?.visibility = View.GONE
            }
        }

    }

    companion object {
        private val TAG = VP2QuickCloudNavFragment::class.java.simpleName

        fun newInstance(devId: String): VP2QuickCloudNavFragment {
            val cloudNavFragment = VP2QuickCloudNavFragment()
            val args = Bundle()
            args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
            cloudNavFragment.arguments = args
            return cloudNavFragment
        }
    }

    //文件类型集
    private val mUploadTypeList = UpdateFileTypeHelper.getFileTypes().toMutableList()

}

fun findNav(fragment: Fragment): NavController? {
    var findFragment: Fragment? = fragment
    while (findFragment != null) {
        if (findFragment is NavHostFragment) {
            return findFragment.navController
        }
        val primaryNavFragment = findFragment.parentFragmentManager
            .primaryNavigationFragment
        if (primaryNavFragment is NavHostFragment) {
            return primaryNavFragment.navController
        }
        findFragment = findFragment.parentFragment
    }
    return null
}