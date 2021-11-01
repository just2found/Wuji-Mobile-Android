package net.linkmate.app.ui.nas.images

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
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
import kotlinx.android.synthetic.main.fragment_files_base.*
import kotlinx.android.synthetic.main.fragment_images.*
import libs.source.common.livedata.Status
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.ui.nas.FilesBaseFragment
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.FileSelectPanel
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.showRefreshAndNotify
import timber.log.Timber


/**
 *
 * @Description: 图片分类查看 年
 * @Author: todo2088
 * @CreateDate: 2021/1/27 13:17
 */
class ImagesYearFragment : FilesBaseFragment() {
    private val photosViewModel by viewModels<PhotosViewModel>({ requireParentFragment() }, { NasAndroidViewModel.ViewModeFactory(requireActivity().application, navArgs.deviceid) })
    private val navArgs by navArgs<ImagesYearFragmentArgs>()
    private var viewType: ImageViewType = ImageViewType.DAY
    private var isPreviewBack = false

    private val imagesSectionAdapter: ImagesSectionAdapter by lazy {
        ImagesSectionAdapter(photosViewModel).apply {

            setOnLoadMoreListener({
                photosViewModel.loadImageMore(getDevId(), viewType, navArgs.year)
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
                    photosViewModel.setYear(navArgs.year)
                    val args = BaseImagePreviewFragmentArgs(getDevId(), indexOf, "${navArgs.year}").toBundle()
                    val currentImage = view.findViewById<ImageView>(R.id.icon)
                    val extras = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        FragmentNavigator.Extras.Builder()
                                .addSharedElement(currentImage, photosViewModel.getItemShareTransitionName(item))
                                .build()
                    } else {
                        null
                    }
                    val findNavController = findNavController()
                    if (findNavController.currentDestination?.id == R.id.imagesYearFragment) {
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
                    model.createTorrent(getDevId(), path, sharePathType).observe(this@ImagesYearFragment, Observer { resource ->
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
                    fileManageViewModel.manage(getDevId(), requireActivity(), this@ImagesYearFragment,
                            OneOSFileType.PICTURE, view, selectedList.map { it as OneOSFile }, action)
                }
            }

        }

        override fun onDismiss() {
            setMultiModel(false, null)
        }
    }

    private fun updateSelect() {
        if (!imagesSectionAdapter.isSetMultiModel) return
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

    private var gridSpanMarginDecoration: GridSpanMarginDecoration? = null

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
        photosViewModel.liveData.observe(this, Observer {
            Timber.d("${it.status} ${it.code} ${it.message} ")
            when (it.status) {
                Status.SUCCESS -> {
                    swipe_refresh_layout.isRefreshing = false
                    if (it.data?.isEmpty() == true) {
                        imagesSectionAdapter.emptyView = LayoutInflater.from(requireContext())
                                .inflate(R.layout.layout_empty_directory, null)
                    }
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
                Status.LOADING -> {
                    swipe_refresh_layout.isRefreshing = true
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

    private fun getCountByType(): Int {
        return when (viewType) {
            ImageViewType.YEAR -> 1
            ImageViewType.MONTH -> 10
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

    override fun initSubView(view: View, savedInstanceState: Bundle?) {
        ibtn_nav_title_right2?.apply {
            isInvisible = true
            isEnabled = false
        }
        ibtn_nav_title_right?.apply {
            isInvisible = true
            isEnabled = false
        }
        top_layout?.isVisible = false
        setLayoutManager(getCountByType())?.let { layoutManager ->
            recycle_view_images.addItemDecoration(GridSpanMarginDecoration(1, layoutManager).also {
                gridSpanMarginDecoration = it
            })
        }
        fab.isVisible = false
        recycle_view_images.adapter = imagesSectionAdapter

        initDragListener(recycle_view_images!!, imagesSectionAdapter)

        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            onRefreshData()
        })
        if (!isPreviewBack) {
            swipe_refresh_layout.showRefreshAndNotify()
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume $isPreviewBack")
        if (isPreviewBack) {
            updateMoreData()
            isPreviewBack = false
        }
    }

    /*
    * 更新其他界面加载的更多数据
    * */
    private fun updateMoreData() {
        val pagesModel = photosViewModel.getPagesModel()
        val files = pagesModel.files
        if (files.size > imagesSectionAdapter.data.size) {
            imagesSectionAdapter.replaceData(files)
            val hasMorePage = pagesModel.hasMorePage()
            if (hasMorePage) {
                imagesSectionAdapter.loadMoreComplete()
            } else {
                imagesSectionAdapter.loadMoreEnd()
            }
            imagesSectionAdapter.setEnableLoadMore(hasMorePage)
        }
    }

    protected override fun refreshDevNameById(mDevId: String?) {
        btn_sort?.text = navArgs.title
    }

    private fun onRefreshData() {
        swipe_refresh_layout.isRefreshing = true
        photosViewModel.loadPhotos(getDevId(), viewType, navArgs.year)
    }

    override fun isEnableOnBackPressed(): Boolean {
        return true
    }
}