package net.linkmate.app.ui.nas.group.list


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.FloatDecoration
import com.chad.library.adapter.base.entity.SectionEntity
import io.cabriole.decorator.GridSpanMarginDecoration
import io.weline.repo.files.data.SharePathType
import kotlinx.android.synthetic.main.fragment_nas_upload.*
import kotlinx.android.synthetic.main.fragment_quick_nav_cloud_dir.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_rv.*
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.cloud.FileLoadMoreView
import net.linkmate.app.ui.nas.cloud.QuickSectionOneOSFilesRVAdapter
import net.linkmate.app.ui.nas.widget.SortMenuPopupView
import net.linkmate.app.util.Dp2PxUtils
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.GroupsKeeper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.DisplayMode
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.*
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.phone.LocalFile
import net.sdvn.nascommon.model.phone.LocalFileManage
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.model.phone.LocalSortTask
import net.sdvn.nascommon.model.phone.comp.FileNameComparator
import net.sdvn.nascommon.model.phone.comp.FileTimeComparator
import net.sdvn.nascommon.utils.*
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.widget.DevicesPopupView
import net.sdvn.nascommon.widget.FileSelectPanel
import net.sdvn.nascommon.widget.SearchPanel
import net.sdvn.nascommon.widget.ServerFileTreeView
import java.io.File
import java.util.*

/**
 * Created by yun
 */
class GroupSpaceUploadNavFragment : TipsBaseFragment() {

    private var mLastClickItem2Top: Int = 0
    private var mLastClickPosition: Int = -1

    private lateinit var mArgs: GroupSpaceUploadNavFragmentArgs;

    private val mSafeBoxFileListModel by viewModels<GroupSpaceNasFileModel>({ requireParentFragment() })

    private val mFileManageCallback: LocalFileManage.OnManageCallback =
        object : LocalFileManage.OnManageCallback {
            override fun onComplete(isSuccess: Boolean) {
                if (isSuccess) {
                    Handler(Looper.getMainLooper()).post { autoPullToRefresh() }
                }
                if (activity is BaseActivity) {
                    (activity as? BaseActivity)?.dismissLoading()
                }
            }

            override fun onStart(resStrId: Int) {
                if (activity is BaseActivity) {
                    (activity as? BaseActivity)?.showLoading(resStrId)
                }
            }
        }
    private var rootPath: String? = null
    private var isSelectionLastPosition: Boolean = false
    private val mSDCardList: MutableList<File> = mutableListOf()
    private var isListShown: Boolean = true
    private var mOrderType: FileOrderType = FileOrderType.NAME
    private var mFileType: LocalFileType = LocalFileType.DOWNLOAD
    private var curDir: File? = null
    private val mOnItemClickListener: BaseQuickAdapter.OnItemClickListener =
        BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            mLastClickPosition = position
            mLastClickItem2Top = view.top
            val mAdapter = getFileAdapter()
            mAdapter.setIsMultiModel(true)

            val entity = mAdapter.getItem(position)
            if (entity?.isHeader == true) {
                return@OnItemClickListener
            }
            val file = entity?.t
            if (file is LocalFile) {
                if (file.isDirectory()) {
                    if (null == curDir) {
                        rootPath = file.file?.parent
                    }
                    curDir = file.file
                    mSelectedList.clear()
                    updateSelectAndManagePanel()
                    autoPullToRefresh()
                } else {
                    mAdapter.toggleItemSelected(position)
                    showSelectAndOperatePanel(true)
                    updateSelectAndManagePanel()
                }
            }
        }
    private val mOnItemLongClickListener: BaseQuickAdapter.OnItemLongClickListener =
        BaseQuickAdapter.OnItemLongClickListener { baseQuickAdapter, view, position ->
            mLastClickPosition = position
            mLastClickItem2Top = view.top
            AnimUtils.shortVibrator()
            val mAdapter = getFileAdapter()
            val entity = mAdapter.getItem(position)
            if (entity?.isHeader == true) {
                true
            } else {
                val file = entity?.t
                if (file is LocalFile) {
                    if (!file.isDirectory()) {
                        mAdapter.setIsMultiModel(true)
                        mAdapter.toggleItemSelected(position)
                        showSelectAndOperatePanel(true)
                        updateSelectAndManagePanel()
                    }
                }
                true
            }

        }
    private val mSelectedList: ArrayList<DataFile> = ArrayList()
    private val mFileList: MutableList<DataFile> = ArrayList()

    private var selectedFileCount = 0
    private var path: String? = null
    private var mShareMenu: DevicesPopupView? = null
    private lateinit var deviceList: MutableList<DeviceModel>
    private lateinit var fileAdapter: QuickSectionOneOSFilesRVAdapter
    private var mToId: String? = null
    val deviceViewModel by viewModels<DeviceViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = GroupSpaceUploadNavFragmentArgs.fromBundle(it)
        }
        deviceList = ArrayList()
        deviceViewModel.liveDevices.observe(this, androidx.lifecycle.Observer {
            deviceList.clear()
            deviceList.addAll(it.filter { deviceModel -> deviceModel != null && deviceModel.isOnline })
            if (mShareMenu != null)
                mShareMenu!!.addList(deviceList)
        })
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        mToId = SPUtils.getValue(activity, AppConstants.SP_FIELD_DEVICE_ID)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_nas_upload
    }

    override fun getTopView(): View? {
        return layout_title
    }

    override fun initView(view: View) {
        this.mTipsBar = tipsBar
        layout_title.setOnBackClickListener {
            findNavController().navigateUp()
        }
        tv_path.visibility = View.VISIBLE
        tv_path.setOnClickListener { v ->
            if (selectedFileCount == 0) {
                ToastHelper.showToast(getString(R.string.tip_select_file))
            } else {
                if (TextUtils.isEmpty(mToId)) {
                    showDevSelectPop(view)
                } else {
                    selectUpToPath(v)
                }

            }
        }
        btn_upload.setOnClickListener { v ->
            if (selectedFileCount == 0) {
                ToastHelper.showToast(getString(R.string.tip_select_file))
            } else {
                uploadFile(v)
            }
        }
        swipe_refresh_layout.setOnRefreshListener {
            autoPullToRefresh() //onRefresh
        }
        layout_path_panel?.visibility = View.GONE
        fileAdapter = QuickSectionOneOSFilesRVAdapter(
            recycle_view.context, mFileList,
            mSelectedList
        )
        fileAdapter.mode = DisplayMode.UPLOAD
        fileAdapter.onItemClickListener = mOnItemClickListener
        fileAdapter.onItemLongClickListener = mOnItemLongClickListener
        fileAdapter.onItemChildClickListener =
            BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                if (view.id == R.id.rv_list_ibtn_select) {
                    AnimUtils.shortVibrator()
                    val mAdapter = adapter as QuickSectionOneOSFilesRVAdapter
                    val data = mAdapter.data
                    val o = data[position]
                    if (o.isHeader) {
                        return@OnItemChildClickListener
                    }
                    mAdapter.setIsMultiModel(true)
                    mAdapter.toggleItemSelected(position)
                    showSelectAndOperatePanel(true)
                    updateSelectAndManagePanel()
                    return@OnItemChildClickListener
                }
            }

        recycle_view!!.addItemDecoration(FloatDecoration(BaseSectionQuickAdapter.SECTION_HEADER_VIEW))
        fileAdapter.emptyView = LayoutInflater.from(recycle_view!!.context)
            .inflate(R.layout.layout_empty_directory, null)
        fileAdapter.setLoadMoreView(FileLoadMoreView())
        recycle_view.adapter = fileAdapter

        layout_path_panel?.setOnPathPanelClickListener { view, path ->
            if (view.id == R.id.ibtn_new_folder && curDir != null) { // New Folder Button Clicked
                val localFileManage =
                    LocalFileManage(baseActivity(), layout_path_panel, mFileManageCallback)
                localFileManage.manage(FileManageAction.MKDIR, curDir!!.absolutePath)
            } else if (view.id == R.id.ibtn_order) {
                showOrderPopView(view)
            } else {
                Logger.LOGD(TAG, ">>>>>Click Path: $path, Root Path:$rootPath")
                if (null == path || rootPath == null) {
                    curDir = null
                    rootPath = null
                    autoPullToRefresh()
                } else {
                    val file = File(path)
                    if (mFileType == LocalFileType.PRIVATE) {
                        val root = File(rootPath!!)
                        if (file == root) {
                            curDir = null
                            rootPath = null
                        } else {
                            curDir = file
                        }
                    } else {
                        curDir = file
                    }
                    autoPullToRefresh()
                }
            }
        }
        layout_path_panel?.showNewFolderButton(false)
        changeFragmentByType()
    }

    private fun selectUpToPath(v: View) {
        if (path!!.startsWith(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR)) {
            mSafeBoxFileListModel.paths = mSelectedList
            findNavController().navigate(
                R.id.action_upload_to_set_path,
                GroupUploadSetPathFragmentArgs(mArgs.deviceid, mArgs.groupid).toBundle()
            )
        } else {
            SessionManager.getInstance()
                .getLoginSession(mToId!!, object : GetSessionListener() {
                    override fun onSuccess(url: String?, loginSession: LoginSession) {
                        val fileTreeView = ServerFileTreeView(
                            requireActivity(),
                            null,
                            loginSession,
                            R.string.tip_upload_file,
                            R.string.upload_file
                        )
                        fileTreeView.setOnUploadListener(object :
                            ServerFileTreeView.OnUploadFileListener {
                            override fun onUpload(
                                toId: String?,
                                tarPath: String?,
                                type: Int
                            ) {
                                mToId = toId
                                path = PathTypeCompat.getAllStrPath(type, tarPath)
                                showPath()
                                uploadFile(v)

                            }
                        })
                        fileTreeView.showPopupCenter()
                    }
                })
        }
    }

    private fun baseActivity() = requireActivity() as BaseActivity

    var sortMenuPopupView: SortMenuPopupView? = null
    private fun showOrderPopView(view: View) {
        if (sortMenuPopupView == null) {
            sortMenuPopupView = SortMenuPopupView(view.context)
            val baseViewHolder = sortMenuPopupView!!.baseViewHolder
            baseViewHolder.setOtherClickListener { v ->
                var orderType = mOrderType
                var isList = isListShown
                when (id) {
                    R.id.sort_by_name -> {
                        orderType = FileOrderType.NAME

                    }
                    R.id.sort_by_time -> {
                        orderType = FileOrderType.TIME
                    }
                    R.id.display_by_grid -> isList = false
                    R.id.display_by_list -> isList = true
                }
                if (orderType != mOrderType) {
                    mOrderType = orderType
                    notifyRefreshComplete(false)
                }
                if (isList != isListShown) {
                    isListShown = isList
                    switchViewer(isListShown)
                }
                sortMenuPopupView!!.dismiss()
            }
        }
        sortMenuPopupView!!.baseViewHolder.getView<CompoundButton>(R.id.display_by_list).isChecked =
            isListShown
        sortMenuPopupView!!.baseViewHolder.getView<CompoundButton>(R.id.display_by_grid).isChecked =
            !isListShown
        sortMenuPopupView!!.baseViewHolder.getView<CompoundButton>(R.id.sort_by_name).isChecked =
            FileOrderType.isName(mOrderType)
        sortMenuPopupView!!.baseViewHolder.getView<CompoundButton>(R.id.sort_by_time).isChecked =
            !FileOrderType.isName(mOrderType)

        sortMenuPopupView!!.showAsDropDown(view)
        view.isSelected = true
        sortMenuPopupView!!.setOnDismissListener {
            view.isSelected = false
        }
        sortMenuPopupView!!.update()

    }

    override fun onStart() {
        super.onStart()
        layout_path_panel.postDelayed({ autoPullToRefresh()},150)
    }

    private fun autoPullToRefresh() {
        PermissionChecker.checkPermission(requireContext(), Callback {
            getFileList(curDir)
        }, Callback {
            UiUtils.showStorageSettings(requireContext())
        }, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        switchViewer(isListShown)
    }

    private var itemDecoration: RecyclerView.ItemDecoration? = null

    private fun switchViewer(isListShown: Boolean) {
        if (recycle_view == null) return
        var isList: Boolean? = null
        val layoutManager = recycle_view.layoutManager
        var scrollPosition = 0
        if (recycle_view!!.layoutManager != null) {
            scrollPosition = (recycle_view.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        }
        if (layoutManager is GridLayoutManager) {
            isList = layoutManager.spanCount == 1
        }

        if (isList == null || isList != isListShown) {
            val layoutManager = if (isListShown) {
                GridLayoutManager(recycle_view.context, 1)
            } else {
                GridLayoutManager(recycle_view.context, 4)
            }
            recycle_view.layoutManager = layoutManager
            val margin = Dp2PxUtils.dp2px(recycle_view.context, 2)
            if (itemDecoration != null) {
                recycle_view.removeItemDecoration(itemDecoration!!)
            }
            itemDecoration = GridSpanMarginDecoration(margin, layoutManager)
            recycle_view.addItemDecoration(itemDecoration!!)
            fileAdapter.toggleViewerType(if (isListShown) FileViewerType.LIST else FileViewerType.GRID)
            recycle_view.adapter = fileAdapter
            recycle_view.scrollToPosition(scrollPosition)
        }
    }

    private fun uploadFile(view: View) {
        val fileManage = LocalFileManage(baseActivity(), layout_title,
            object : LocalFileManage.OnManageCallback {
                override fun onComplete(isSuccess: Boolean) {
                    FileListChangeObserver.getInstance().FileListChange()
                    if (activity is BaseActivity) {
                        (activity as? BaseActivity)?.dismissLoading()
                    }
                    if (activity != null) {
                        findNavController().navigateUp()
                    }

                }

                override fun onStart(resStrId: Int) {
                    if (activity is BaseActivity) {
                        (activity as? BaseActivity)?.showLoading(resStrId)
                    }
                }
            })

        if (TextUtils.isEmpty(mToId) || (SessionManager.getInstance()
                .getDeviceModel(mToId)?.isOnline != true)
        ) {
            showDevSelectPop(view)
            return
        }
        if (path.isNullOrEmpty()) {
            selectUpToPath(view)
            return
        }
        val sharePathType = PathTypeCompat.getSharePathType(path!!)
        fileManage.setUploadPath(
            mToId!!, path,
            if (sharePathType == SharePathType.GROUP.type) {
                mArgs.groupid
            } else null
        )
        val selected: MutableList<LocalFile> = mutableListOf()
        for (dataFile in mSelectedList) {
            if (dataFile is LocalFile) {
                selected.add(dataFile)
            }
        }
        fileManage.manage(LocalFileType.PRIVATE, FileManageAction.UPLOAD, selected)
    }

    private fun showDevSelectPop(v: View) {
        if (deviceList.size > 0) {
            mShareMenu = DevicesPopupView(requireActivity(), null, v)
            mShareMenu!!.setNeedPath(false)
            mShareMenu!!.addList(deviceList)
            mShareMenu!!.setTitleText(R.string.tv_select_device)
            mShareMenu!!.setConfirmOnClickListener {
                val isSelected = mShareMenu!!.isSelected
                var position = -1
                for ((key, value) in isSelected) {
                    if (value) {
                        position = key
                        break
                    }
                }
                if (position != -1 && position >= 0 && position < deviceList.size) {
                    val deviceModel = deviceList[position]
                    mToId = deviceModel.devId
                    showPath()
                    mShareMenu!!.dismiss()
                }
            }
        } else {
            ToastHelper.showToast(
                v.context.resources.getString(R.string.nullnull) + v.context.resources.getString(
                    R.string.app_name
                )
            )
        }
    }


    private fun showPath() {
        if (tv_path != null) {
            var pathName = path
            var devMarkName: String? = null
            if (!TextUtils.isEmpty(pathName)) {
                tv_path!!.visibility = View.VISIBLE

                pathName =  if(mArgs.groupid>0)
                {
                    val text= GroupsKeeper.findGroup(mArgs.deviceid, mArgs.groupid)?.name ?:getString(R.string.group)
                    text + path!!.replaceFirst(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR.toRegex(), OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)
                }else{
                    OneOSFileType.getPathWithTypeName(path!!)
                }


                //                if (pathName.startsWith(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)) {
                //                    pathName = mPathView.getContext().getResources().getString(R.string.root_dir_name_private) + pathName;
                if (!TextUtils.isEmpty(mToId) && SessionManager.getInstance()
                        .getDeviceModel(mToId) != null
                ) {
                    devMarkName = SessionManager.getInstance().getDeviceModel(mToId)!!.devName

                    if (!TextUtils.isEmpty(devMarkName)) {
                        pathName = "$devMarkName:$pathName"
                    }
                }
                tv_path!!.text =
                    Utils.setKeyWordColor(tv_path!!.context, R.color.primary, pathName, devMarkName)
            } else {
                tv_path!!.visibility = View.GONE
            }
        }
    }

    private var mSearchFilter: String? = null
    private var mData: MutableList<SectionEntity<DataFile>> = mutableListOf()

    private fun getFileList(dir: File?) {

        mFileList.clear()
        if (!TextUtils.isEmpty(mSearchFilter)) {
            if (mFileType == LocalFileType.PRIVATE) {
                val task = LocalSortTask(
                    requireContext(),
                    this,
                    mFileType,
                    mSearchFilter,
                    object : LocalSortTask.onLocalSortListener {
                        override fun onStart(type: LocalFileType) {}

                        override fun onComplete(
                            type: LocalFileType,
                            fileList: List<LocalFile>,
                            sectionList: List<String>
                        ) {
                            mFileList.addAll(fileList)
                            notifyRefreshComplete(true)
                        }
                    })
                task.execute(0)
            } else {
                val fileList = ArrayList<LocalFile>()
                searchDownloadDir(fileList, curDir!!)
                mFileList.addAll(fileList)
                notifyRefreshComplete(true)
            }
        } else {
            if (mFileType == LocalFileType.PRIVATE) {
                layout_path_panel?.visibility = View.VISIBLE
                if (dir == null) {
                    layout_path_panel?.showNewFolderButton(false)
                    for (f in mSDCardList) {
                        mFileList.add(LocalFile(f))
                    }
                } else {
                    layout_path_panel?.showNewFolderButton(false)
                    val files = dir.listFiles { f -> !f.isHidden }
                    if (null != files) {

                        val downloadPath = SessionManager.getInstance().defaultDownloadPath
                        for (f in files) {
                            val file = LocalFile(f)
                            file.isDownloadDir =
                                (downloadPath != null && f.absolutePath.startsWith(downloadPath))
                            mFileList.add(file)
                        }
                    }
                }
                notifyRefreshComplete(true)
            } else {
                Logger.LOGD(TAG, "---------File type: $mFileType")
                val task = LocalSortTask(
                    requireContext(),
                    this,
                    mFileType,
                    mSearchFilter,
                    object : LocalSortTask.onLocalSortListener {
                        override fun onStart(type: LocalFileType) {
                            //                baseActivity.showLoading();
                        }

                        override fun onComplete(
                            type: LocalFileType,
                            fileList: List<LocalFile>,
                            sectionList: List<String>
                        ) {
                            Logger.LOGD(TAG, "onComplete: fileList ================ $fileList")
                            mFileList.clear()
                            mFileList.addAll(fileList)
                            notifyRefreshComplete(true)
                        }
                    })
                task.execute(0)
            }
        }
    }

    private fun searchDownloadDir(fileList: ArrayList<LocalFile>, dir: File) {
        if (dir.isDirectory) {
            val files = dir.listFiles()
            if (null != files) {
                for (file in files) {
                    searchDownloadDir(fileList, file)
                }
            }
        } else {
            if (dir.name.contains(mSearchFilter!!)) {
                fileList.add(LocalFile(dir))
            }
        }
    }

    private fun notifyRefreshComplete(refresh: Boolean) {
        include_refresh_view?.visibility = View.GONE
        val entities: MutableList<SectionEntity<DataFile>> = mutableListOf()
        if (mOrderType == FileOrderType.NAME)
            mFileList.sortWith(FileNameComparator())
        else {
            mFileList.sortWith(FileTimeComparator())
        }
        var lastSection: String? = null
        val fmtDate = resources.getString(R.string.fmt_time_line)
        for (file in mFileList) {
            if (mOrderType == FileOrderType.TIME) {
                if (mFileType != LocalFileType.PRIVATE) {
                    val date = FileUtils.fmtTimeByZone(file.getTime(), fmtDate)
                    if (!Objects.equals(lastSection, date)) {
                        lastSection = date
                        entities.add(SectionEntity(true, date))
                    }
                }
            }
            entities.add(SectionEntity(file))
        }
        mData.clear()
        mData.addAll(entities)
        fileAdapter.replaceData(mData)
        swipe_refresh_layout.isRefreshing = false
        if (mFileType == LocalFileType.PRIVATE) {
            layout_path_panel.updatePath(
                mFileType,
                if (curDir == null) null else curDir!!.absolutePath,
                rootPath
            )
        } else {
            getFileAdapter().isShowMedia =
                (mFileType == LocalFileType.PICTURE || mFileType == LocalFileType.VIDEO)
        }
    }

    /**
     * Show/Hide Top Select Bar
     *
     * @param isShown Whether show
     */
    fun showSelectBar(isShown: Boolean) {
        if (isShown) {
            layout_select_top_panel!!.showPanel(true)
        } else {
            layout_select_top_panel!!.hidePanel(true)
        }
    }

    /**
     * Update Top Select Bar
     *
     * @param totalCount    Total select count
     * @param selectedCount Selected count
     * @param mListener     On file select listener
     */
    fun updateSelectBar(
        totalCount: Int,
        selectedCount: Int,
        mListener: FileSelectPanel.OnFileSelectListener
    ) {
        Logger.LOGD(TAG, "updateSelectBar: selectedCount = $selectedCount")
        layout_select_top_panel!!.setOnSelectListener(mListener)
        layout_select_top_panel.updateCount(totalCount, selectedCount)
        selectedFileCount = selectedCount
    }


    /**
     * Add search file listener
     *
     * @param listener
     */
    fun addSearchListener(listener: SearchPanel.OnSearchActionListener) {}


    private fun backToParentDir(dir: File): Boolean {
        isSelectionLastPosition = true
        for (f in mSDCardList) {
            if (dir == f) {
                curDir = null
                rootPath = null
                autoPullToRefresh()
                return false
            }
        }

        curDir = dir.parentFile
        Logger.LOGD(TAG, "----Parent Path: " + curDir!!.absolutePath + "------")
        autoPullToRefresh()
        return true
    }

    private fun directBackParentDir(dir: File) {
        isSelectionLastPosition = true
        val parent = dir.parentFile
        curDir = parent
        autoPullToRefresh()
    }

    private fun tryBackToParentDir(): Boolean {
        Logger.LOGD(TAG, "=====Current Path: $curDir========")
        if (mFileType == LocalFileType.PRIVATE) {
            if (curDir != null) {
                if (backToParentDir(curDir!!))
                    return true
            }
        } else {
            if (curDir != null && curDir!!.absolutePath != rootPath) {
                directBackParentDir(curDir!!)
                return true
            }
        }

        return false
    }

    private fun showSelectAndOperatePanel(isShown: Boolean) {
        showSelectBar(isShown)
    }

    private fun updateSelectAndManagePanel() {
        updateSelectBar(mFileList.size, mSelectedList.size, mFileSelectListener)
    }

    private val mFileSelectListener = object : FileSelectPanel.OnFileSelectListener {
        override fun onSelect(isSelectAll: Boolean) {
            getFileAdapter().selectAllItem(isSelectAll)
            getFileAdapter().notifyDataSetChanged()
            updateSelectAndManagePanel()
        }

        override fun onDismiss() {
            setMultiModel(false, 0)
        }
    }

    private fun setMultiModel(isSetMultiModel: Boolean, position: Int) {
        val curIsMultiModel = getFileAdapter().isMultiChooseModel
        if (curIsMultiModel == isSetMultiModel) {
            return
        }
        if (isSetMultiModel) {
            updateSelectAndManagePanel()
            showSelectAndOperatePanel(true)
            getFileAdapter().setIsMultiModel(true)
            mSelectedList.add(mFileList[position])
            getFileAdapter().notifyDataSetChanged()
            return
        } else {
            showSelectAndOperatePanel(false)
            getFileAdapter().setIsMultiModel(false)
            getFileAdapter().notifyDataSetChanged()
            return
        }
    }

    private fun getFileAdapter(): QuickSectionOneOSFilesRVAdapter {
        return fileAdapter
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    override fun onBackPressed(): Boolean {
        if (fileAdapter.isMultiChooseModel) {
            showSelectAndOperatePanel(false)
            setMultiModel(false, -1)
            return true
        }
        return tryBackToParentDir()
    }

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
        fileAdapter.setWifiAvailable(isWifiAvailable)
    }

    fun changeFragmentByType(
        devId: String = mArgs.deviceid,
        fileType: LocalFileType = mSafeBoxFileListModel.fileType,
        path: String = mArgs.path
    ) {
        mFileType = fileType
        this.path = path
        this.mToId = devId
        isListShown = !(fileType == LocalFileType.PICTURE || fileType == LocalFileType.VIDEO)
        mOrderType = if (mFileType == LocalFileType.PRIVATE) {
            val sdCardList = SDCardUtils.getSDCardList()
            if (sdCardList != null) {
                mSDCardList.clear()
                mSDCardList.addAll(sdCardList)
            }
            FileOrderType.NAME
        } else {
            FileOrderType.TIME
        }
        switchViewer(isListShown)
        showPath()
        layout_title?.setBackTitle(LocalFileType.getTypeTitle(fileType))
        layout_title?.visibility = View.VISIBLE
    }

    companion object {
        private val TAG = GroupSpaceUploadNavFragmentArgs::class.java.simpleName
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
}
