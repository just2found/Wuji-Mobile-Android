package net.linkmate.app.ui.nas.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.drag.DragSelectTouchListener
import com.chad.library.adapter.base.drag.DragSelectionProcessor
import com.chad.library.adapter.base.entity.SectionEntity
import io.cabriole.decorator.GridSpanMarginDecoration
import io.weline.repo.files.data.SharePathType
import kotlinx.android.synthetic.main.fragment_file_favroites.*
import kotlinx.android.synthetic.main.fragment_files_base.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_recycle_view.*
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.ui.nas.FilesBaseFragment
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.cloud.FileLoadMoreView
import net.linkmate.app.ui.nas.cloud.VP2QuickCloudNavFragmentArgs
import net.linkmate.app.ui.nas.helper.FileSortHelper
import net.linkmate.app.ui.nas.images.BaseImagePreviewFragmentArgs
import net.linkmate.app.ui.nas.images.ISelection
import net.linkmate.app.ui.nas.widget.OnCheckedChangeListener
import net.linkmate.app.ui.nas.widget.SearchPanelV2
import net.linkmate.app.ui.nas.widget.SortMenuPopupViewV2
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.WindowUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.FileViewerType
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.FileSelectPanel
import net.sdvn.nascommon.widget.SearchPanel
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.singleClick

/**
 *
 * @Description: Favorites
 * @Author: todo2088
 * @CreateDate: 2021/3/6 11:24
 */
class FavoritesFragment : FilesBaseFragment(), SearchPanelV2.OnSearchActionListener {
    private var popup: SortMenuPopupViewV2? = null
    private var itemDecoration: RecyclerView.ItemDecoration? = null
    private var mOrderType: FileOrderTypeV2 = FileOrderTypeV2.time_desc
    private var mFileViewerType: FileViewerType = FileViewerType.LIST
    private var lastData: LiveData<Resource<List<OneOSFile>>>? = null
    private val navArgs by navArgs<FavoritesFragmentArgs>()
    private var isSearch: Boolean = true
    private val fileFavoritesViewModel by viewModels<FileFavoritesViewModel>({ requireParentFragment() },
            { NasAndroidViewModel.ViewModeFactory(requireActivity().application, getDevId()) })
    private var isPreviewBack = false
    private val fileRvAdapter: DataFileRVAdapter by lazy {
        DataFileRVAdapter(requireContext(), fileFavoritesViewModel).apply {
            setOnItemChildClickListener { baseQuickAdapter, view, position ->
                if (view.id == R.id.rv_list_ibtn_select
                        || view.id == R.id.rv_grid_cb_select) {
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
                        fileType == OneOSFileType.PICTURE || item.isPicture -> {
                            val indexOf = fileFavoritesViewModel.indexOfItem(item)
                            val args = BaseImagePreviewFragmentArgs(getDevId(), indexOf).toBundle()
                            val findNavController = findNavController()
                            if (findNavController.currentDestination?.id == R.id.favoritesFragment) {
                                findNavController.navigate(R.id.action_global_favoritesImagePreviewFragment, args, null, null)
                                isPreviewBack = true
                            }
                        }
                        else -> {
                            if (item.isDirectory()) {
                                isSearch = false
//                                val data = fileFavoritesViewModel.openDir(getDevId(), item.getPath(), fileType,
//                                        intArrayOf(item.getPathType()), orderTypeV2 = mOrderType)
//                                observerData(data)
                                val findNavController = findNavController()
                                if (findNavController.currentDestination?.id == R.id.favoritesFragment) {
                                    val pathType = item.getPathType()
                                    when (pathType) {
                                        SharePathType.SAFE_BOX.type -> {

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
                                            if (osFileType != null) {
                                                findNavController.navigate(R.id.global_to_VP2QuickCloudNavFragment,
                                                        VP2QuickCloudNavFragmentArgs(getDevId(), osFileType, item.getAllPath()).toBundle())
                                                isPreviewBack = true
                                            }
                                        }
                                    }

                                }
                            } else {
                                fileManageViewModel.openFile(getDevId(), requireActivity(), view, item)
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
        data.observe(this@FavoritesFragment, myDataObserver)
        lastData = data
    }

    private var mDragSelectTouchListener: DragSelectTouchListener? = null
    private var mDragSelectionProcessor: DragSelectionProcessor? = null

    private fun initDragListener(recycleViewImages: RecyclerView, iSelection: ISelection) {
        // 2) Add the DragSelectListener
        mDragSelectionProcessor = DragSelectionProcessor(object : DragSelectionProcessor.ISelectionHandler {

            override fun getSelection(): MutableSet<Int> {
                return iSelection.selection
            }

            override fun isSelected(index: Int): Boolean {
                return iSelection.selection.contains(index)
            }

            override fun updateSelection(start: Int, end: Int, isSelected: Boolean, calledFromOnStart: Boolean) {
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
    private val mFileManageListener: FileManagePanel.OnFileManageListener<DataFile> = object : FileManagePanel.OnFileManageListener<DataFile> {
        override fun onClick(view: View, selectedList: List<DataFile>, action: FileManageAction) {
            when (action) {
                FileManageAction.TORRENT_CREATE -> {
                    val path = selectedList[0].getPath()
                    val sharePathType = selectedList[0].getPathType()
                    model.createTorrent(getDevId(), path, sharePathType).observe(this@FavoritesFragment, Observer { resource ->
                        if (resource.status == Status.SUCCESS) {
                            resource.data?.result?.let { btItem ->
                                val isOwner = SessionManager.getInstance().getDeviceModel(getDevId())?.isOwner
                                        ?: false
                                model.showBtItemQRCodeView(requireActivity(), btItem, isOwner)
                            }
                        }
                    })
                }
                else -> {
                    if (action == FileManageAction.DOWNLOAD) {
                        actionDownloadAnimation(view, selectedList.count())
                        setMultiModel(false, null)
                    }
                    fileManageViewModel.manage(getDevId(), requireActivity(), this@FavoritesFragment,
                            getFileType(), view, selectedList.map { it as OneOSFile }, action)
                }
            }

        }

        override fun onDismiss() {
            setMultiModel(false, null)
        }
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
        updateManageBar(getFileType(), selectedList, SessionManager.getInstance().getLoginSession(getDevId())
                ?: LoginSession(getDevId()), mFileManageListener)
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
                        FileManageAction.MOVE, FileManageAction.DELETE
                            , FileManageAction.UNFAVORITE -> {
                            onRefreshData()
                        }
                        else -> {
                            fileRvAdapter.notifyDataSetChanged()
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
        onRefreshData()
    }

    override fun getSubLayoutId(): Int {
        return R.layout.fragment_file_favroites
    }

    override fun getDevId(): String {
        return navArgs.deviceid
    }

    override fun initSubView(view: View, savedInstanceState: Bundle?) {
        if (navArgs.isSearch) {
            layout_search_panel.showPanel(false, !isPreviewBack)
            layout_search_panel.setOnSearchListener(this)
        }
        fab.isVisible = false
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            onRefreshData()
        })

        recycle_view.adapter = fileRvAdapter
        refreshViewer()
        fileRvAdapter.setOnLoadMoreListener {
            fileFavoritesViewModel.loadMore(isSearch)?.let {
                observerData(it)
            }
        }
        fileRvAdapter.setLoadMoreView(FileLoadMoreView())
        initDragListener(recycle_view, fileRvAdapter)
        swipe_refresh_layout.isRefreshing = true
        iv_images_viewer.singleClick {
            showOrderPopView(it)
        }
    }

    private fun showOrderPopView(view: View) {
        if (popup == null) {
            popup = SortMenuPopupViewV2(view.context, mOrderType, mFileViewerType).apply {
                setOnOrderCheckedChangeListener(object : OnCheckedChangeListener<FileOrderTypeV2> {
                    override fun onCheckedChangeListener(orderType: FileOrderTypeV2) {
                        if (orderType != mOrderType) {
                            mOrderType = orderType
                            resortDataByOrder(orderType)
                            dismiss()
                        }
                    }
                })
                setOnViewerCheckedChangeListener(object : OnCheckedChangeListener<FileViewerType> {
                    override fun onCheckedChangeListener(type: FileViewerType) {
                        mFileViewerType = type
                        refreshViewer()
                        dismiss()
                    }
                })
            }
        }
        popup!!.showAsDropDown(view, Dp2PxUtils.dp2px(requireContext(), 16), 0)
        popup!!.isOutsideTouchable = true
        popup!!.isFocusable = true
        popup!!.update()
        view.isSelected = true
        popup!!.setOnDismissListener {
            view.isSelected = false
            WindowUtil.hintShadow(activity)
        }
        WindowUtil.showShadow(activity)
    }

    private fun resortDataByOrder(orderType: FileOrderTypeV2) {
        FileSortHelper.sortWith(navArgs.fileType, orderType, fileFavoritesViewModel.getPagesModel().files)
        replaceData(true)
    }

    private fun refreshViewer() {
        val mRecyclerView = recycle_view ?: return
        var isList: Boolean? = null
        val layoutManager = mRecyclerView.layoutManager
        var scrollPosition = 0
        if (mRecyclerView.layoutManager != null) {
            scrollPosition = (mRecyclerView.layoutManager as GridLayoutManager)
                    .findFirstCompletelyVisibleItemPosition()
        }
        if (layoutManager is GridLayoutManager) {
            isList = layoutManager.spanCount == 1
        }
        val isListShown = mFileViewerType == FileViewerType.LIST
        val context = requireContext()
        if (isList == null || isList != isListShown) {
            val gridLayoutManager = if (isListShown) {
                GridLayoutManager(context, 1)
            } else {
                GridLayoutManager(context, 4)
            }
            mRecyclerView.layoutManager = gridLayoutManager
            val margin = Dp2PxUtils.dp2px(context, 2)
            if (itemDecoration != null) {
                mRecyclerView.removeItemDecoration(itemDecoration!!)
            }
            itemDecoration = GridSpanMarginDecoration(margin, gridLayoutManager)
            mRecyclerView.addItemDecoration(itemDecoration!!)
            fileRvAdapter.toggleViewerType(mFileViewerType)
        }
    }

//    override fun showSearchActivity(view: View) {
//        val args = FavoritesFragmentArgs(navArgs.deviceid, navArgs.tagId, navArgs.fileType, true).toBundle()
//        val findNavController = findNavController()
//        if (findNavController.currentDestination?.id == R.id.favoritesFragment) {
//            findNavController.navigate(R.id.action_global_favoritesFragment, args)
//        }
//    }

    private fun onRefreshData() {
        if (navArgs.isSearch) {
            onSearch(layout_search_panel?.searchFilter ?: "")
        } else {
            val data = fileFavoritesViewModel.loadFiles(getDevId(), navArgs.tagId, getFileType(),
                    path = getCurrentPath(), orderTypeV2 = mOrderType)
            observerData(data)
        }
    }

    override fun onSearch(filter: String) {
        isSearch = true
        val data = fileFavoritesViewModel.loadFiles(getDevId(), navArgs.tagId, getFileType(), filter,
                path = getCurrentPath(), orderTypeV2 = mOrderType)
        observerData(data)
    }

    private val myDataObserver = Observer<Resource<List<OneOSFile>>> { resource ->
        when (resource.status) {
            Status.SUCCESS -> {
                swipe_refresh_layout.isRefreshing = false
                resource.data?.let { files ->
                    if (files.isNullOrEmpty()) {
                        fileRvAdapter.emptyView = LayoutInflater.from(context)
                                .inflate(R.layout.layout_empty_directory, null)
                    }
                    val data = files.map { SectionEntity(it) as SectionEntity<DataFile> }
                    val pagesModel = fileFavoritesViewModel.getPagesModel()
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
        return null
    }

    override fun getPathType(): IntArray {
        return intArrayOf(SharePathType.USER.type)
    }

    override fun onCancel() {
        findNavController().popBackStack()
    }

    override fun onResume() {
        super.onResume()
        if (isPreviewBack) {
            replaceData()
            isPreviewBack = false
        }
    }


    private fun replaceData(isFocusRefresh: Boolean = false) {
        val pagesModel = fileFavoritesViewModel.getPagesModel()
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
        return findNavController().navigateUp()
    }

    override fun isEnableOnBackPressed(): Boolean {
        return true
    }


}