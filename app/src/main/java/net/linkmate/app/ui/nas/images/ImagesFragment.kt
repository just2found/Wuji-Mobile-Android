package net.linkmate.app.ui.nas.images

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigator
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
import kotlinx.android.synthetic.main.fragment_images.*
import libs.source.common.livedata.Status
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.ui.nas.FilesBaseFragment
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.widget.ImageViewerMenuPopupView
import net.linkmate.app.ui.nas.widget.OnCheckedChangeListener
import net.linkmate.app.util.Dp2PxUtils.dp2px
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.FileSelectPanel
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.showRefreshAndNotify
import org.view.libwidget.singleClick
import timber.log.Timber


/**
 *
 * @Description: 图片分类查看
 * @Author: todo2088
 * @CreateDate: 2021/1/27 13:17
 */
class ImagesFragment : FilesBaseFragment() {
    private var gridSpanMarginDecoration: GridSpanMarginDecoration? = null
    private val navArgs by navArgs<ImagesFragmentArgs>()
    private val photosViewModel by viewModels<PhotosViewModel>({ requireParentFragment() },
            { NasAndroidViewModel.ViewModeFactory(requireActivity().application, navArgs.deviceid) })
    private var viewType: ImageViewType = ImageViewType.YEAR
    private var isPreviewBack = false
    private var isYearBack = false
    private val imagesYearAdapter: ImagesYearAdapter by lazy {
        ImagesYearAdapter(photosViewModel).apply {
            emptyView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.layout_empty_directory, null)
            setOnItemClickListener { baseQuickAdapter, view, i ->
                if (Utils.isFastClick(view)) {
                    return@setOnItemClickListener
                }
                val item = baseQuickAdapter.getItem(i) as? OneFileModel
                if (item != null) {
                    val args = ImagesYearFragmentArgs(getDevId(), item.ext1, item.cttime).toBundle()
                    val findNavController = findNavController()
                    if (findNavController.currentDestination?.id == R.id.imagesFragment) {
                        photosViewModel.resetDayData()
                        findNavController.navigate(R.id.action_imagesFragment_to_imagesYearFragment, args)
                        isYearBack = true
                    }
                }
            }
        }
    }
    private val imagesSectionAdapter: ImagesSectionAdapter by lazy {
        ImagesSectionAdapter(photosViewModel).apply {
            setPreLoadNumber(AppConstants.PAGE_SIZE / 10)
        }
    }

    private fun initImagesSectionAdapterListeners() {
        imagesSectionAdapter.apply {
            setOnLoadMoreListener({
                Timber.d("ImagesSectionAdapter : OnLoadMoreListener")
                photosViewModel.loadImageMore(getDevId(), viewType)
            })

            setOnItemClickListener { baseQuickAdapter, view, i ->
                if (Utils.isFastClick(view)) {
                    return@setOnItemClickListener
                }
                if (isSetMultiModel) {
                    toggleSelection(pos = i)
                    updateSelect()
                    return@setOnItemClickListener
                }
                val sectionEntity = baseQuickAdapter.getItem(i) as? SectionEntity<*>
                val item = sectionEntity?.t
                if (item is OneFileModel) {
                    val indexOf = imagesSectionAdapter.findFilterHeaderPosition(i)
                    photosViewModel.setYear(null)
                    val args = BaseImagePreviewFragmentArgs(getDevId(), indexOf).toBundle()
                    val currentImage = view.findViewById<ImageView>(R.id.icon)
                    val extras = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        FragmentNavigator.Extras.Builder()
                                .addSharedElement(currentImage, photosViewModel.getItemShareTransitionName(item))
                                .build()
                    } else {
                        null
                    }
                    val findNavController = findNavController()
                    if (findNavController.currentDestination?.id == R.id.imagesFragment) {
                        findNavController.navigate(R.id.action_global_imagePreviewFragment, args, null, extras)
                        isPreviewBack = true
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
                if (item is OneFileModel) {
                    setMultiModel(true, position)
                    return@setOnItemLongClickListener true
                }
                return@setOnItemLongClickListener false
            }
        }
    }

    private var mDragSelectTouchListener: DragSelectTouchListener? = null
    private var mDragSelectionProcessor: DragSelectionProcessor? = null

    private fun initDragListener(recycleViewImages: RecyclerView, iSelection: ISelection?) {
        if (iSelection == null) {
            if (mDragSelectTouchListener != null) {
                recycleViewImages.removeOnItemTouchListener(mDragSelectTouchListener!!)
            }
            mDragSelectionProcessor = null
            mDragSelectTouchListener = null
        } else {
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
    }

    private val mFileSelectListener = object : FileSelectPanel.OnFileSelectListener {
        override fun onSelect(isSelectAll: Boolean) {
            if (isSelectAll) {
                imagesSectionAdapter.selectAll()
            } else {
                imagesSectionAdapter.deselectAll()
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
                    model.createTorrent(getDevId(), path, sharePathType).observe(this@ImagesFragment, Observer { resource ->
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
                    fileManageViewModel.manage(getDevId(), requireActivity(), this@ImagesFragment,
                            OneOSFileType.PICTURE, view, selectedList.map { it as OneOSFile }, action)
                }
            }

        }

        override fun onDismiss() {
            setMultiModel(false, null)
        }
    }

    private fun updateSelect() {
        val data = imagesSectionAdapter.data
        val filter = data.filter { !it.isHeader }
        updateSelectBar(filter.count(), imagesSectionAdapter.countSelected, mFileSelectListener)
        val selectedList = data.filterIndexed { index, sectionEntity ->
            !sectionEntity.isHeader && sectionEntity.t != null
                    && imagesSectionAdapter.selection.contains(index)
        }.map { it.t as DataFile }
        updateManageBar(OneOSFileType.PICTURE, selectedList, SessionManager.getInstance().getLoginSession(getDevId())
                ?: LoginSession(getDevId()), mFileManageListener)
    }

    fun setMultiModel(isSetMultiModel: Boolean, position: Int?): Boolean {

        if (isSetMultiModel == imagesSectionAdapter.isSetMultiModel) {
            position?.run {
                mDragSelectTouchListener?.startDragSelection(position)
            }
            return false
        }
        imagesSectionAdapter.isSetMultiModel = isSetMultiModel
        if (isSetMultiModel) {
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


    private fun setLayoutManager(spanCount: Int): GridLayoutManager? {
        recycle_view_images?.apply {
            var gridLayoutManager1 = layoutManager as? GridLayoutManager
            if (gridLayoutManager1 == null) {
                val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
                gridLayoutManager1 = gridLayoutManager
            }
            gridLayoutManager1.spanCount = spanCount
            layoutManager = gridLayoutManager1
            gridLayoutManager1.let {
                gridSpanMarginDecoration?.setGridLayoutManager(it)
            }
            return gridLayoutManager1
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photosViewModel.viewType.observe(this, Observer { imageViewType ->
            Timber.d("viewType $viewType ")
            if (viewType != imageViewType) {
                var isChangeAdapter = false
                var iSelection: ISelection? = null
                setMultiModel(false, null)
                when (imageViewType!!) {
                    ImageViewType.YEAR -> {
                        isChangeAdapter = true
                        swapAdapter(imagesYearAdapter)
                    }
                    ImageViewType.MONTH,
                    ImageViewType.DAY -> {
                        isChangeAdapter = viewType == ImageViewType.YEAR
                        if (!isChangeAdapter) {
                            photosViewModel.switchInDayMonthViewType(imageViewType).observe(this,
                                    Observer { bool ->
                                        if (bool) {
                                            replaceData(true)
                                        }
                                    })
                        }
                        iSelection = imagesSectionAdapter
                        swapAdapter(imagesSectionAdapter)
                    }
                }
                viewType = imageViewType
                setLayoutManager(getCountByType())
                if (isChangeAdapter) {
                    initDragListener(recycle_view_images, iSelection)
                    onRefreshData()
                }
            }
        })
        photosViewModel.liveData.observe(this, Observer {
            Timber.d("loadPhotosTimeline $viewType  ${it.status} ${it.code} ${it.message} ")
            if (viewType == ImageViewType.DAY || viewType == ImageViewType.MONTH) {
                when (it.status) {
                    Status.SUCCESS -> {
                        swipe_refresh_layout.isRefreshing = false
                        if (it.data.isNullOrEmpty()) {
//                    setLayoutManager(getCountByType())
                            imagesSectionAdapter.emptyView = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.layout_empty_directory, null)
                        }
                        swapAdapter(imagesSectionAdapter)
                        imagesSectionAdapter.updateData(it.data)
                        updateSelect()
                    }
                    Status.ERROR -> {
                        swipe_refresh_layout.isRefreshing = false
                        imagesSectionAdapter.loadMoreFail()
                        if (it.code != null) {
                            onCommonError(it.code!!)
                        }
                    }
                }
            }
        })
        photosViewModel.liveDataSummary.observe(this, Observer {
            Timber.d("loadSummary $viewType ${it.status} ${it.code} ${it.message} ")
            if (viewType == ImageViewType.YEAR) {
                when (it.status) {
                    Status.SUCCESS -> {
                        swipe_refresh_layout.isRefreshing = false
                        if (it.data.isNullOrEmpty()) {
                            imagesYearAdapter.emptyView = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.layout_empty_directory, null)
                        }
                        swapAdapter(imagesYearAdapter)
                        imagesYearAdapter.replaceData(it.data ?: listOf())
//                    setLayoutManager(getCountByType())
                    }
                    Status.ERROR -> {
                        swipe_refresh_layout.isRefreshing = false
                        if (it.code != null) {
                            onCommonError(it.code!!)
                        }
                    }
                }
            }
        })

        fileManageViewModel.liveDataAction.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data) {
                        FileManageAction.MOVE, FileManageAction.DELETE -> {
                            onRefreshData()
                        }
                        FileManageAction.FAVORITE,FileManageAction.UNFAVORITE->{
                            imagesSectionAdapter.notifyDataSetChanged()
                        }
                    }
                }
                Status.LOADING -> {
                    setMultiModel(false, null)
                }
                Status.ERROR -> {
                    if (it.code != null) {
                        onCommonError(it.code!!)
                    }
                }
            }
        })
    }

    private fun swapAdapter(adapter: RecyclerView.Adapter<*>) {
        if (recycle_view_images.adapter != adapter) {
            recycle_view_images.adapter = adapter
        }
    }

    private fun getCountByType(): Int {
        return when (viewType) {
            ImageViewType.YEAR -> 1
            ImageViewType.MONTH -> 6
            ImageViewType.DAY -> 4
            else -> 4
        }
    }

    override fun getSubLayoutId(): Int {
        return R.layout.fragment_images
    }

    override fun getDevId(): String {
        return navArgs.deviceid
    }

    override fun onBackPressed(): Boolean {
        //处于多选模式时 先退出多选模式
        if (imagesSectionAdapter.isSetMultiModel) {
            setMultiModel(false, null)
            return true
        }
        return findNavController().popBackStack()
    }

    override fun isEnableOnBackPressed(): Boolean {
        return true
    }

    override fun initSubView(view: View, savedInstanceState: Bundle?) {
        setLayoutManager(getCountByType())?.let { layoutManager ->
            recycle_view_images.addItemDecoration(GridSpanMarginDecoration(1, layoutManager).also {
                gridSpanMarginDecoration = it
            })
        }
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            onRefreshData()
        })
        if (!isYearBack && !isPreviewBack) {
            swipe_refresh_layout.showRefreshAndNotify()
        } else {
            when (viewType) {
                ImageViewType.YEAR -> {
                    swapAdapter(imagesYearAdapter)
                }
                ImageViewType.MONTH,
                ImageViewType.DAY -> {
                    swapAdapter(imagesSectionAdapter)
                }
            }
        }
        initImagesSectionAdapterListeners()
        iv_images_viewer.singleClick {
            showSwitchViewer(it)
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume $isPreviewBack")
        if (isPreviewBack) {
            replaceData()
            isPreviewBack = false
        }
        if (isYearBack) {
            photosViewModel.resetDayData()
            isYearBack = false
        }
    }

    private fun replaceData(isFocusRefresh: Boolean = false) {
        val pagesModel = photosViewModel.getPagesModel()
        val files = pagesModel.files
        if (files.size > imagesSectionAdapter.data.size || isFocusRefresh) {
            imagesSectionAdapter.setNewData(files)
        }
        val hasMorePage = pagesModel.hasMorePage()
        if (hasMorePage) {
            imagesSectionAdapter.loadMoreComplete()
        } else {
            imagesSectionAdapter.loadMoreEnd()
        }
        imagesSectionAdapter.setEnableLoadMore(hasMorePage)
    }


    private fun showSwitchViewer(iv: ImageView) {
        ImageViewerMenuPopupView(iv.context, viewType).let { popupView ->
            popupView.showAsDropDown(iv, dp2px(requireContext(), 16), 0)
            popupView.isOutsideTouchable = true
            popupView.isFocusable = true
            popupView.update()
            iv.isSelected = true
            popupView.setOnDismissListener {
                iv.isSelected = false
            }
            popupView.setOnViewerCheckedChangeListener(object : OnCheckedChangeListener<ImageViewType> {
                override fun onCheckedChangeListener(type: ImageViewType) {
                    photosViewModel.selectType(type)
                    popupView.dismiss()
                }
            })
        }
    }

    private fun onRefreshData() {
        swipe_refresh_layout.isRefreshing = true
        photosViewModel.loadPhotos(getDevId(), viewType)
    }

    override fun getPathType(): IntArray {
        return intArrayOf(SharePathType.USER.type, SharePathType.PUBLIC.type)
    }

    override fun getFileType(): OneOSFileType {
        return OneOSFileType.PICTURE
    }
}