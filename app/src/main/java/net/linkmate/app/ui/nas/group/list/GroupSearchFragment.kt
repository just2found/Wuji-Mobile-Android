package net.linkmate.app.ui.nas.group.list

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.drag.DragSelectTouchListener
import com.chad.library.adapter.base.drag.DragSelectionProcessor
import com.chad.library.adapter.base.entity.SectionEntity
import io.weline.repo.files.data.SharePathType
import kotlinx.android.synthetic.main.fragment_file_search_new.*
import kotlinx.android.synthetic.main.fragment_files_base.*
import kotlinx.android.synthetic.main.fragment_files_base.fab
import kotlinx.android.synthetic.main.fragment_files_base.layout_file_manage_panel
import kotlinx.android.synthetic.main.fragment_files_base.layout_select_top_panel
import kotlinx.android.synthetic.main.fragment_safe_box_nas_file.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_recycle_view.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_recycle_view.recycle_view
import kotlinx.android.synthetic.main.include_swipe_refresh_and_recycle_view.swipe_refresh_layout
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.ui.nas.FilesBaseFragment
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.cloud.FileLoadMoreView
import net.linkmate.app.ui.nas.images.BaseImagePreviewFragmentArgs
import net.linkmate.app.ui.nas.images.ISelection
import net.linkmate.app.ui.nas.search.DataFileRVAdapter
import net.linkmate.app.ui.nas.search.FileSearchViewModel
import net.linkmate.app.ui.nas.widget.SearchPanelV2
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.FileSelectPanel
import org.view.libwidget.setOnRefreshWithTimeoutListener

/**
 *
 * @Description: Search
 * @Author: todo2088
 * @CreateDate: 2021/3/6 11:24
 */
class GroupSearchFragment : FilesBaseFragment(), SearchPanelV2.OnSearchActionListener {
    private var isSearch: Boolean = true
    private var lastData: LiveData<Resource<List<OneOSFile>>>? = null
    private val navArgs by navArgs<GroupSearchFragmentArgs>()
    private val mSafeBoxFileListModel by viewModels<GroupSpaceNasFileModel>({ requireParentFragment() })
    private val fileSearchViewModel by viewModels<FileSearchViewModel>({
        requireParentFragment()
    },
        { NasAndroidViewModel.ViewModeFactory(requireActivity().application, getDevId()) })
    private var isPreviewBack = false
    private val fileRvAdapter: DataFileRVAdapter by lazy {
        DataFileRVAdapter(requireContext(), fileSearchViewModel).apply {
            this.groupId = navArgs.groupid
            setOnItemChildClickListener { baseQuickAdapter, view, position ->
                if (view.id == R.id.rv_list_ibtn_select
                    || view.id == R.id.rv_grid_cb_select
                ) {
                    if (isSetMultiModel) {
                        return@setOnItemChildClickListener
                    }
                    val sectionEntity = baseQuickAdapter.getItem(position) as? SectionEntity<*>
                    if (sectionEntity?.isHeader == true) {
                        this.toggleSelectionHeader(position)
                        this.isSetMultiModel = true
                        mDragSelectTouchListener!!.startDragSelection(position)
                        return@setOnItemChildClickListener
                    }
                    val item = sectionEntity?.t
                    if (item is DataFile) {
                        setMultiModel(true, position)
                        return@setOnItemChildClickListener
                    }
                }
                return@setOnItemChildClickListener
            }
            setOnItemClickListener { baseQuickAdapter, view, position ->
                if (Utils.isFastClick(view)) {
                    return@setOnItemClickListener
                }
                if (isSetMultiModel) {
                    toggleSelection(pos = position)
                    updateSelect()
                    return@setOnItemClickListener
                }
                val sectionEntity = baseQuickAdapter.getItem(position) as? SectionEntity<*>
                val item = sectionEntity?.t
                if (item is OneOSFile) {
                    val fileType = navArgs.fileType
                    when {
                        navArgs.groupid < 0 && (fileType == OneOSFileType.PICTURE || item.isPicture) -> {
                            val indexOf = fileSearchViewModel.indexOfItem(item)
                            val args = BaseImagePreviewFragmentArgs(getDevId(), indexOf).toBundle()
                            val findNavController = findNavController()
                            if (findNavController.currentDestination?.id == R.id.searchFragment) {
                                findNavController.navigate(
                                    R.id.action_global_searchImagePreviewFragment,
                                    args,
                                    null,
                                    null
                                )
                                isPreviewBack = true
                            }
                        }
                        else -> {
                            if (item.isDirectory()) {
                                isSearch = false
//                                val data = fileSearchViewModel.openDir(getDevId(), item.getPath(), fileType, intArrayOf(item.getPathType()))
//                                observerData(data)
                                val findNavController = findNavController()
                                val pathType = item.getPathType()
                                when (pathType) {
                                    SharePathType.SAFE_BOX.type -> {

                                    }
                                    SharePathType.GROUP.type -> {
                                        if (navArgs.groupid > 0) {
                                            findNavController.navigate(
                                                R.id.quickCloudNavFragment,
                                                GroupSpaceQuickCloudNavFragmentArgs(
                                                    getDevId(),
                                                    navArgs.groupid,
                                                    SharePathType.GROUP.type,
                                                    item.getPath()
                                                ).toBundle()
                                            )
                                            isPreviewBack = true
                                        }
                                    }
                                    else -> {
                                        val osFileType = when (pathType) {
                                            SharePathType.USER.type -> {
                                                OneOSFileType.PRIVATE
                                            }
                                            SharePathType.PUBLIC.type -> {
                                                OneOSFileType.PUBLIC
                                            }
                                            SharePathType.EXTERNAL_STORAGE.type -> {
                                                OneOSFileType.EXTERNAL_STORAGE
                                            }
                                            else -> {
                                                null
                                            }
                                        }


                                    }
                                }
                            } else {
                                fileManageViewModel.openFile(
                                    getDevId(),
                                    requireActivity(),
                                    view,
                                    item,
                                    navArgs.groupid
                                )
                                isPreviewBack = true
                            }
                        }
                    }
                }
            }
            setOnItemLongClickListener { baseQuickAdapter, _, position ->
                val sectionEntity = baseQuickAdapter.getItem(position) as? SectionEntity<*>
                if (sectionEntity?.isHeader == true) {
                    this.toggleSelectionHeader(position)
                    this.isSetMultiModel = true
                    mDragSelectTouchListener!!.startDragSelection(position)
                    return@setOnItemLongClickListener true
                }
                val item = sectionEntity?.t
                if (item is DataFile) {
                    setMultiModel(true, position)
                    return@setOnItemLongClickListener true
                }
                return@setOnItemLongClickListener false
            }
        }
    }

    private fun observerData(data: LiveData<Resource<List<OneOSFile>>>) {
        if (lastData != null) {
            lastData!!.removeObserver(myDataObserver)
        }
        data.observe(this@GroupSearchFragment, myDataObserver)
        lastData = data
    }

    private var mDragSelectTouchListener: DragSelectTouchListener? = null
    private var mDragSelectionProcessor: DragSelectionProcessor? = null

    private fun initDragListener(recycleViewImages: RecyclerView, iSelection: ISelection) {
        // 2) Add the DragSelectListener
        mDragSelectionProcessor =
            DragSelectionProcessor(object : DragSelectionProcessor.ISelectionHandler {

                override fun getSelection(): MutableSet<Int> {
                    return iSelection.selection
                }

                override fun isSelected(index: Int): Boolean {
                    return iSelection.selection.contains(index)
                }

                override fun updateSelection(
                    start: Int,
                    end: Int,
                    isSelected: Boolean,
                    calledFromOnStart: Boolean
                ) {
                    iSelection.selectRange(start, end, isSelected)
                    updateSelect()
                }
            })
                .withMode(DragSelectionProcessor.Mode.FirstItemDependentToggleAndUndo)
        mDragSelectTouchListener = DragSelectTouchListener()
            .withSelectListener(mDragSelectionProcessor)
        recycleViewImages.addOnItemTouchListener(mDragSelectTouchListener!!)
    }

    private val mFileSelectListener = object : FileSelectPanel.OnFileSelectListener {
        override fun onSelect(isSelectAll: Boolean) {
            if (isSelectAll) {
                fileRvAdapter.selectAll()
            } else {
                fileRvAdapter.deselectAll()
            }
            updateSelect()
        }

        override fun onDismiss() {
            setMultiModel(false, null)
        }
    }


    private val mFileManageListener: FileManagePanel.OnFileManageListener<DataFile> =
        object : FileManagePanel.OnFileManageListener<DataFile> {
            override fun onClick(view: View, list: List<DataFile>, action: FileManageAction) {
                val selectedList = mutableListOf<OneOSFile>()
                list.forEach {
                    if (it is OneOSFile)
                        selectedList.add(it)
                }

                if (selectedList.isNullOrEmpty()) {
                    ToastHelper.showToast(R.string.tip_select_file)
                } else if (action != FileManageAction.MORE &&
                    action != FileManageAction.BACK &&
                    !CMAPI.getInstance().isConnected
                ) {
                    ToastHelper.showToast(R.string.network_not_available)
                    return
                }
                when (action) {
                    FileManageAction.MOVE -> {
                        moveOrCopy(selectedList, GroupSpaceMoveToFragment.MOVE)
                        optFile = true
                    }
                    FileManageAction.COPY -> {
                        moveOrCopy(selectedList, GroupSpaceMoveToFragment.COPY)
                        optFile = true
                    }
//                FileManageAction.DOWNLOAD -> {
//                    getOneOSFileManage().manage(OneOSFileType.GROUP, action, selectedList!!)
//                }
                    else -> {
                        getOneOSFileManage(true).manage(OneOSFileType.GROUP, action, selectedList!!)
                        fileRvAdapter.deselectAll()
                        setMultiModel(false, null)
                        updateSelect()

                    }
                }


            }

            override fun onDismiss() {
                setMultiModel(false, null)
            }
        }

    private fun moveOrCopy(selectedList: MutableList<OneOSFile>, type: String) {
        val list = Array(selectedList.size) { it -> "" }
        for ((index, oneOSFile) in selectedList.withIndex()) {
            list[index] = oneOSFile.getPath()
        }
        fileRvAdapter.deselectAll()
        setMultiModel(false, null)
        updateSelect()
        findNavController().navigate(
            R.id.action_search_to_move_to,
            GroupSpaceMoveToFragmentArgs(
                deviceid = navArgs.deviceid,
                type = type,
                groupid = navArgs.groupid,
                paths = list
            ).toBundle()
        )
    }


    private fun updateSelect() {
        if (!fileRvAdapter.isSetMultiModel) return
        val data = fileRvAdapter.data
        val filter = data.filter { !it.isHeader }
        updateSelectBar(filter.count(), fileRvAdapter.countSelected, mFileSelectListener)
        val selectedList = data.filterIndexed { index, sectionEntity ->
            !sectionEntity.isHeader && sectionEntity.t != null
                    && fileRvAdapter.selection.contains(index)
        }.map { it.t as DataFile }
        updateManageBar(
            getFileType(), selectedList, SessionManager.getInstance().getLoginSession(getDevId())
                ?: LoginSession(getDevId()), mFileManageListener, navArgs.groupid > 0
        )
    }

    fun setMultiModel(isSetMultiModel: Boolean, position: Int?): Boolean {

        if (isSetMultiModel == fileRvAdapter.isSetMultiModel) {
            position?.run {
                mDragSelectTouchListener?.startDragSelection(position)
            }
            return false
        }
        fileRvAdapter.isSetMultiModel = isSetMultiModel
        if (isSetMultiModel) {
            AnimUtils.shortVibrator()
            position?.run {
                mDragSelectTouchListener?.startDragSelection(position)
            }
            showManageBar(true)
            showSelectBar(true)
        } else {
            mDragSelectTouchListener?.setIsActive(false)
            showManageBar(false)
            showSelectBar(false)
        }
        swipe_refresh_layout?.isEnabled = !isSetMultiModel
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileManageViewModel.liveDataAction.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data) {
                        FileManageAction.MOVE, FileManageAction.DELETE -> {
                            onRefreshData()
                        }
                    }
                    setMultiModel(false, null)
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {
                    if (it.code != null) {
                        onCommonError(it.code!!)
                    }
                }
            }
        })
    }

    override fun getSubLayoutId(): Int {
        return R.layout.fragment_file_search_new
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        fileSearchViewModel.mGroupId = navArgs.groupid //在初始化设备的信息时候顺便设置群ID

        super.initView(view, savedInstanceState)
    }


    override fun getDevId(): String {
        return navArgs.deviceid
    }


    override fun initSubView(view: View, savedInstanceState: Bundle?) {
        layout_search_panel.showPanel(false, !isPreviewBack)
        layout_search_panel.setOnSearchListener(this)

        fab.isVisible = false
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            onRefreshData()
        })
        recycle_view.layoutManager = LinearLayoutManager(requireContext())
        recycle_view.adapter = fileRvAdapter
        fileRvAdapter.setOnLoadMoreListener {
            fileSearchViewModel.loadMore(isSearch)?.let {
                observerData(it)
            }
        }
        fileRvAdapter.setLoadMoreView(FileLoadMoreView())
        initDragListener(recycle_view, fileRvAdapter)
        val prefixName: String = if (OneOSFileType.isDB(getFileType())) {
            getString(OneOSFileType.getTypeName(getFileType()))
        } else {
            ""
        }
        file_path_panel?.updatePath(getFileType(), getCurrentPath(), prefixName)

    }

    private fun onRefreshData() {
        if (!layout_search_panel.searchFilter.isNullOrEmpty()) {
            onSearch(layout_search_panel.searchFilter!!)
        }else{
            swipe_refresh_layout.isRefreshing=false
        }
    }

    override fun onSearch(filter: String) {
        isSearch = true
        val data = fileSearchViewModel.searchFiles(
            getDevId(),
            getFileType(),
            filter,
            getPathType(),
            path = getCurrentPath()
        )
        observerData(data)
    }

    private val myDataObserver = Observer<Resource<List<OneOSFile>>> { resource ->
        when (resource.status) {
            Status.SUCCESS -> {
                swipe_refresh_layout.isRefreshing = false
                resource.data?.let { files ->
                    if (files.isNullOrEmpty()) {
                        fileRvAdapter.emptyView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.layout_empty_directory, null)
                    }
                    val data = files.map { SectionEntity(it) as SectionEntity<DataFile> }
                    val pagesModel = fileSearchViewModel.getPagesModel()
                    val isRefreshing = pagesModel.page == 0
                    val hasMorePage = pagesModel.hasMorePage()
                    if (isRefreshing) {
                        fileRvAdapter.setNewData(data)
                    } else {
                        fileRvAdapter.addData(data)
                    }
                    if (hasMorePage) {
                        fileRvAdapter.loadMoreComplete()
                    } else {
                        fileRvAdapter.loadMoreEnd()
                    }
                    fileRvAdapter.setEnableLoadMore(hasMorePage)
                }
            }
            Status.ERROR -> {
                swipe_refresh_layout.isRefreshing = false
                if (resource.code != null) {
                    onCommonError(resource.code!!)
                }
            }
            Status.LOADING -> {
                swipe_refresh_layout.isRefreshing = true
            }
        }
    }

    override fun onVisible(visible: Boolean) {

    }

    override fun getFileType(): OneOSFileType {
        return navArgs.fileType
    }

    override fun getCurrentPath(): String? {
        return navArgs.curPath
    }

    override fun getPathType(): IntArray {
        return navArgs.pathType
    }

    override fun onCancel() {
        findNavController().popBackStack()
    }

    private var optFile = false;
    override fun onResume() {
        super.onResume()
        if (optFile) {
            onRefreshData()
            optFile = false
        }


        if (navArgs.groupid != null && navArgs.groupid > 0)
            fab.isVisible = false
        if (isPreviewBack) {
            replaceData()
            isPreviewBack = false
        }
    }

    private fun getOneOSFileManage(
        refreshData: Boolean = false,
        callback: OneOSFileManage.OnManageCallback? = null
    ): OneOSFileManage {
        val manageCallback = callback ?: OneOSFileManage.OnManageCallback {
            if (it) {
                setSelectVisibility(false)
                fileRvAdapter.deselectAll()
                if (refreshData)
                    onRefreshData()
            }
        }
        return OneOSFileManage(
            requireActivity(),
            null,
            mSafeBoxFileListModel.mSessionLiveData.value!!,
            null,
            manageCallback,
            navArgs.groupid
        )
    }

    private var mIsSelectModel = false
    private fun setSelectVisibility(boolean: Boolean) {
        mIsSelectModel = boolean
        if (layout_file_manage_panel.visibility != View.VISIBLE && boolean) {
            layout_file_manage_panel.visibility = View.VISIBLE
        } else if (layout_file_manage_panel.visibility == View.VISIBLE && !boolean) {
            layout_file_manage_panel.visibility = View.GONE
        }

        if (layout_select_top_panel.visibility != View.VISIBLE && boolean) {
            layout_select_top_panel.visibility = View.VISIBLE
        } else if (layout_select_top_panel.visibility == View.VISIBLE && !boolean) {
            layout_select_top_panel.visibility = View.GONE
        }
    }


    private fun replaceData(isFocusRefresh: Boolean = false) {
        val pagesModel = fileSearchViewModel.getPagesModel()
        val files = pagesModel.files
        if (files.size > fileRvAdapter.data.size || isFocusRefresh) {
            fileRvAdapter.setNewData(files.map { SectionEntity(it) as SectionEntity<DataFile> })
        }
        val hasMorePage = pagesModel.hasMorePage()
        if (hasMorePage) {
            fileRvAdapter.loadMoreComplete()
        } else {
            fileRvAdapter.loadMoreEnd()
        }
        fileRvAdapter.setEnableLoadMore(hasMorePage)
    }

    override fun onBackPressed(): Boolean {
        if (fileRvAdapter.isSetMultiModel) {
            setMultiModel(false, null)
            return false
        }
        return findNavController().popBackStack()
    }

    override fun isEnableOnBackPressed(): Boolean {
        return true
    }


}