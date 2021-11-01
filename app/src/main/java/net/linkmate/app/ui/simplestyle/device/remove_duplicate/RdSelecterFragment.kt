package net.linkmate.app.ui.simplestyle.device.remove_duplicate


import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_rd_selecter.*
import kotlinx.android.synthetic.main.part_load_nas.*
import kotlinx.android.synthetic.main.part_no_data.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.simplestyle.device.download_offline.DownloadOfflineIndexFragment
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.OnSelectChangeListener
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.RdSelectAdapter
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupBottom
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupHead
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupInfo
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.data.DupTotal
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.ProgressDialog
import net.sdvn.nascommon.utils.FileUtils
import org.json.JSONArray
import org.view.libwidget.log.L
import java.util.*


class RdSelecterFragment : TipsBaseFragment(), OnSelectChangeListener {


    private val viewModel by viewModels<RemoveDuplicateModel>({ requireParentFragment() })


    private val mMultiItemEntityList = mutableListOf<MultiItemEntity>()
    private val mDupBottomList = mutableListOf<DupBottom>()
    private val mLoadingDialogFragment by lazy {
        ProgressDialog()
    }

    private val selectedItemFormat by lazy {
        getString(R.string.selected_item_format)
    }
    private val rdSelected by lazy {
        "${getString(R.string.delete)}(${getString(R.string.selected_rd)})"
    }
    private var mIntelligenceType = RemoveDuplicateModel.LONGER_NAME_TYPE//默认的自动选择模式
    private var mSelectItemNumber = 0;//选中项个数
    private var mSelectItemSize = 0L;//选中项大小

    private val mDupTotal = DupTotal(0, 0)
    private var page = 0

    private val mRequestLoadMoreListener by lazy {
        BaseQuickAdapter.RequestLoadMoreListener {
            page++
            getDuplicateFiles(page)
        }
    }

    override fun onPause() {
        super.onPause()
        if (valueAnimator.isRunning) {
            valueAnimator.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (load_ani_img.visibility == View.VISIBLE && valueAnimator.isPaused) {
            valueAnimator.resume()
        }
    }

    //转圈的
    val valueAnimator by lazy {
        ValueAnimator.ofInt(0, 360 * 100).let {
            it.duration = 3000 * 100
            it.addUpdateListener { animation ->
                val rotateValue = animation.animatedValue as Int
                load_ani_img.rotation = rotateValue.toFloat()
            }
            it.interpolator = DecelerateInterpolator()
            it.repeatCount = -1
            it
        }
    }


    val mRdSelectAdapter by lazy {
        val it = RdSelectAdapter(mMultiItemEntityList, requireActivity())
        it.setEnableLoadMore(true)
        it.setOnLoadMoreListener(mRequestLoadMoreListener, recyclerView)
        it.setOnItemClickListener { baseQuickAdapter, view, postion ->
            val item = baseQuickAdapter.getItem(postion)
            if (item is DupBottom) {
                val jsonArray = JSONArray()
                item.selectPathList.forEach {
                    if (it.path == "/") {
                        jsonArray.put(it.path + it.name)
                    } else if (it.path.endsWith("/")) {
                        jsonArray.put(it.path + it.name)
                    } else {
                        jsonArray.put(it.path + "/" + it.name)
                    }
                }
                if (jsonArray.length() == 0) {
                    return@setOnItemClickListener
                }
                deleteFile(jsonArray) {
                    if (mLoadingDialogFragment.dialog?.isShowing == true) {
                        mLoadingDialogFragment.dismiss()
                    }
                    mSelectItemNumber -= item.selectPathList.size
                    mSelectItemSize -= item.selectPathList.size * item.pathList[0].size
                    afterDeleteRefresh(item)
                    showSelectionUI()
                }
            }
        }
        it
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_rd_selecter
    }

    //这个是删除数据不重新刷UI
    private fun afterDelete(dupBottom: DupBottom): Boolean {
        if (dupBottom.selectPathList.size + 1 >= dupBottom.pathList.size)//那便
        {
            mMultiItemEntityList.remove(dupBottom.dupHead)
            mMultiItemEntityList.removeAll(dupBottom.pathList)
            mMultiItemEntityList.remove(dupBottom)
            mDupTotal.groupSize--
            mDupTotal.fileSize = mDupTotal.fileSize - dupBottom.pathList.size
            return true
        } else {
            mMultiItemEntityList.removeAll(dupBottom.selectPathList)
            dupBottom.pathList.removeAll(dupBottom.selectPathList)
            mDupTotal.fileSize = mDupTotal.fileSize - dupBottom.selectPathList.size
            dupBottom.selectPathList.clear()
            return false
        }
    }

    private fun afterDeleteRefresh(dupBottom: DupBottom) {
        if (dupBottom.selectPathList.size + 1 >= dupBottom.pathList.size)//如果剩余的文件个数小于等于一个
        {
            mRdSelectAdapter.remove(mMultiItemEntityList.indexOf(dupBottom.dupHead))
            dupBottom.pathList.forEach {
                mRdSelectAdapter.remove(mMultiItemEntityList.indexOf(it))
            }
            mRdSelectAdapter.remove(mMultiItemEntityList.indexOf(dupBottom))
            mDupBottomList.remove(dupBottom)
            mDupTotal.groupSize--
            mDupTotal.fileSize = mDupTotal.fileSize - dupBottom.pathList.size
            if (mDupTotal.groupSize <= 0) {
                showNoDataUi()
            }else{
                mRdSelectAdapter.notifyItemChanged(0)
            }

        } else {
            dupBottom.selectPathList.forEach {
                mRdSelectAdapter.remove(mMultiItemEntityList.indexOf(it))
                dupBottom.pathList.remove(it)
            }
            mDupTotal.fileSize = mDupTotal.fileSize - dupBottom.selectPathList.size
            dupBottom.selectPathList.clear()
            mRdSelectAdapter.notifyItemChanged(0)
        }
    }


    private fun selectByIntelligence(type: Int, dupBottom: DupBottom) {
        when (type) {
            RemoveDuplicateModel.LONGER_NAME_TYPE -> {
                dupBottom.selectLongerName()
            }
            RemoveDuplicateModel.SHORTER_NAME_TYPE -> {
                dupBottom.selectShorterName()
            }
            RemoveDuplicateModel.LONGER_PATH_TYPE -> {
                dupBottom.selectLongerPath()
            }
            RemoveDuplicateModel.SHORTER_PATH_TYPE -> {
                dupBottom.selectShorterPath()
            }
            RemoveDuplicateModel.EARLIER_TIME_TYPE -> {
                dupBottom.selectEarlierTime()
            }
            RemoveDuplicateModel.LATER_TIME_TYPE -> {
                dupBottom.selectLaterTime()
            }
        }
    }

    override fun getTopView(): View? {
        return title_bar
    }


    override fun initView(view: View) {
        viewModel.selectTypeLiveData.observe(this, Observer {
            mIntelligenceType = it
            if (!mDupBottomList.isNullOrEmpty()) {
                mSelectItemSize = 0L
                mSelectItemNumber = 0
                mDupBottomList.forEach { dupBottom ->
                    selectByIntelligence(mIntelligenceType, dupBottom)
                    mSelectItemNumber += dupBottom.selectPathList.size
                    mSelectItemSize += dupBottom.selectPathList.size * dupBottom.pathList[0].size
                }
                mRdSelectAdapter.notifyDataSetChanged()
            }
            showSelectionUI()
        })
        load_tips_tv.text = getString(R.string.under_monitoring)
        title_bar.setBackListener {
            findNavController().navigateUp()
        }

        mTipsBar = tipsBar
        title_bar.addRightTextButton(getString(R.string.intelligence_select)) {
            findNavController().navigate(R.id.action_select_to_intelligence)
        }

        //删除全部选中
        delete_ll.setOnClickListener {
            val jsonArray = JSONArray()
            mDupBottomList.forEach { dupBottom ->
                dupBottom.selectPathList.forEach {
                    if (it.path == "/") {
                        jsonArray.put(it.path + it.name)
                    } else {
                        jsonArray.put(it.path + "/" + it.name)
                    }
                }
            }
            if (jsonArray.length() == 0) {
                return@setOnClickListener
            }
            deleteFile(jsonArray) {//这个是全部删除
                L.i("删除成功", "initView", "RdSelecterFragment", "nwq", "2021/6/2");
                page = 0;//删除全部页数后 从第一页开始加载数据
                mMultiItemEntityList.clear()
                mDupBottomList.clear()
                mRdSelectAdapter.setNewData(mMultiItemEntityList)
                mRdSelectAdapter.notifyDataSetChanged()
                mSelectItemNumber = 0
                mSelectItemSize = 0L
                showSelectionUI()
                if (mLoadingDialogFragment.dialog?.isShowing != true) {
                    mLoadingDialogFragment.show(
                        childFragmentManager,
                        RdSelecterFragment::javaClass.name
                    )
                }
                getDuplicateFiles(page)
            }
        }
        showLoadUi()
        getDuplicateFiles(page)
    }


    private var isRequest = false;
    private fun deleteFile(jsonArray: JSONArray, successNext: () -> Unit) {
        if (isRequest) {
            ToastUtils.showToast(R.string.ec_too_many_requests);
        }
        if (mLoadingDialogFragment.dialog?.isShowing != true) {
            mLoadingDialogFragment.show(
                childFragmentManager,
                RdSelecterFragment::javaClass.name
            )
        }
        isRequest = true
        viewModel.deleteFile(devId!!, jsonArray).observe(this, Observer {
            if (it.status == Status.SUCCESS && it.data == true) {
                successNext.invoke()
                isRequest = false
            } else if (it.status == Status.ERROR) {
                L.i("删除失败", "deleteFile", "RdSelecterFragment", "nwq", "2021/6/2");
                if (mLoadingDialogFragment.dialog?.isShowing == true) {
                    mLoadingDialogFragment.dismiss()
                }
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                isRequest = false
            }
        })
    }


    private fun showLoadUi() {
        load_nas_part.visibility = View.VISIBLE
        if (!valueAnimator.isStarted) {
            valueAnimator.start()
        }
        delete_ll.visibility = View.GONE
        no_data_part.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    //显示有数据时候的
    private fun showDataUi() {
        if (valueAnimator.isRunning)
            valueAnimator.pause()
        load_nas_part.visibility = View.GONE
        no_data_part.visibility = View.GONE
        delete_ll.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
    }

    //显示没有数据时候的UI
    private fun showNoDataUi() {
        if (valueAnimator.isRunning)
            valueAnimator.pause()
        recyclerView.visibility = View.GONE
        delete_ll.visibility = View.GONE
        load_nas_part.visibility = View.GONE
        no_data_part.visibility = View.VISIBLE
        content_tv.text = getString(R.string.no_duplicate_files)
    }


    private fun getDuplicateFiles(page: Int) {
        //    handler.removeMessages(queryAdminStatus)
        viewModel.getDuplicateFiles(devId!!, page = page).observe(this, Observer { result ->
            if (mLoadingDialogFragment.dialog?.isShowing == true) {
                mLoadingDialogFragment.dismiss()
            }
            if (result.status == Status.SUCCESS) {
                L.i("加载数据成功", "getDuplicateFiles", "RdSelecterFragment", "nwq", "2021/6/2");
                result.data?.let { rdDuplicateFilesResult ->
                    if (rdDuplicateFilesResult.dupinfos.isNullOrEmpty()) {
                        if (page == 0 || mMultiItemEntityList.size == 0) {
                            showNoDataUi()
                        }
                        mRdSelectAdapter.loadMoreComplete()
                        mRdSelectAdapter.setEnableLoadMore(false)
                    } else {
                        if (page == 0) {
                            showDataUi()
                            mMultiItemEntityList.clear()
                            mDupTotal.groupSize = rdDuplicateFilesResult.filenum
                            mDupTotal.fileSize = rdDuplicateFilesResult.totalfiles
                            mMultiItemEntityList.add(mDupTotal)
                            mMultiItemEntityList.addAll(convertData(rdDuplicateFilesResult.dupinfos))
                            recyclerView.adapter = mRdSelectAdapter
                        } else {
                            mRdSelectAdapter.loadMoreComplete()
                            mRdSelectAdapter.setEnableLoadMore(true)
                            mRdSelectAdapter.addData(convertData(rdDuplicateFilesResult.dupinfos))
                            // 不需要这个 mMultiItemEntityList.addAll()
                        }
                        showSelectionUI()
                    }
                    if (mMultiItemEntityList.size >= mDupTotal.fileSize + mDupTotal.groupSize * 2) {
                        mRdSelectAdapter.loadMoreEnd()
                    }
                }
            } else {
                L.i(
                    "加载数据事变${result.code ?: 0}",
                    "getDuplicateFiles",
                    "RdSelecterFragment",
                    "nwq",
                    "2021/6/2"
                );
                if (mRdSelectAdapter.data.isNullOrEmpty()) {
                    showNoDataUi()
                }
            }
        })
    }


    //更新给用户
    private fun showSelectionUI() {
        if (mSelectItemNumber > 0 && mSelectItemSize > 0) {
            title_bar.setTitleText(String.format(selectedItemFormat, mSelectItemNumber))
            delete_tv.text = String.format(rdSelected, FileUtils.fmtFileSize(mSelectItemSize))
        } else {
            title_bar.setTitleText(getString(R.string.duplicate_removal))
            delete_tv.text = getString(R.string.delete)
        }
    }

    //这个是为了处理数据 进行分组做出头和底部
    private fun convertData(dupInfoList: List<MutableList<DupInfo>>): List<MultiItemEntity> {
        val multiItemEntityList = mutableListOf<MultiItemEntity>()
        val term = getString(R.string.term)
        for (list in dupInfoList) {
            if (list.isNotEmpty()) {
                val dupInfo = list[0]
                val defImg = FileUtils.fmtFileIcon(dupInfo.name)
                var imgUrl: String? = null
                if (defImg == net.sdvn.nascommonlib.R.drawable.icon_device_img || defImg == net.sdvn.nascommonlib.R.drawable.icon_device_vedio) {
                    imgUrl = viewModel.genThumbnailUrl(dupInfo.path + "/" + dupInfo.name)
                }
                val totalSize = FileUtils.fmtFileSize(dupInfo.size * list.size)
                val dupHead = DupHead(defImg, imgUrl, String.format(term, list.size), totalSize)
                Collections.sort(list, Comparator { o1, o2 ->
                    if (o1 != null && o2 != null) {
                        ((o2.time - o1.time).toInt())
                    } else 0
                })
                val dupBottom = DupBottom(list, dupHead, this)
                selectByIntelligence(mIntelligenceType, dupBottom)
                mSelectItemNumber += dupBottom.selectPathList.size
                mSelectItemSize += dupBottom.selectPathList.size * list[0].size
                for (dupInfo in list) {
                    dupInfo.onSelectFileListener = dupBottom
                }
                multiItemEntityList.add(dupHead)
                multiItemEntityList.addAll(list)
                multiItemEntityList.add(dupBottom)
                mDupBottomList.add(dupBottom)
            }
        }
        return multiItemEntityList
    }


//    private fun convertData(dupInfoList: List<DupInfo>): List<MultiItemEntity> {
//        val multiItemEntityList = mutableListOf<MultiItemEntity>()
//        var id = -1L
//        var totalSize = 0L
//        var defImg = -1
//        var imgUrl: String? = null
//        var temporary = mutableListOf<DupInfo>()
//        val term = getString(R.string.term)
//        dupInfoList.forEach { dupInfo ->
//            if (dupInfo.id != id) {
//                if (temporary.size > 1) {//只有重复项的文件大于一的时候才会添加
//                    val dupHead = DupHead(defImg, imgUrl, "${temporary.size}$term", FileUtils.fmtFileSize(totalSize))
//                    imgUrl = null
//                    multiItemEntityList.add(dupHead)
//                    Collections.sort(temporary, Comparator { o1, o2 ->
//                        if (o1 != null && o2 != null) {
//                            ((o2.time - o1.time).toInt())
//                        } else 0
//                    })
//                    multiItemEntityList.addAll(temporary)
//                    val dupBottom = DupBottom(temporary, dupHead, this)
//                    selectByIntelligence(mIntelligenceType, dupBottom)
//                    mDupBottomList.add(dupBottom)
//                    mSelectItemNumber += dupBottom.selectPathList.size
//                    mSelectItemSize += dupBottom.selectPathList.size * temporary[0].size
//                    multiItemEntityList.add(dupBottom)
//                    temporary.forEach {
//                        it.onSelectFileListener = dupBottom
//                    }
//                }
//                temporary = mutableListOf()//因为这个有传递给dupBottom 所以只能新建不能清楚
//                id = dupInfo.id
//                totalSize = dupInfo.size
//                defImg = FileUtils.fmtFileIcon(dupInfo.name)
//                if (defImg == net.sdvn.nascommonlib.R.drawable.icon_device_img || defImg == net.sdvn.nascommonlib.R.drawable.icon_device_vedio) {
//                    imgUrl = viewModel.genThumbnailUrl(dupInfo.path)
//                }
//                dupInfo.sizeStr = FileUtils.fmtFileSize(dupInfo.size)
//                dupInfo.timeStr = MyConstants.sdf.format(Date(dupInfo.time * 1000))
//                temporary.add(dupInfo)
//            } else {
//                totalSize += dupInfo.size
//                dupInfo.sizeStr = FileUtils.fmtFileSize(dupInfo.size)
//                dupInfo.timeStr = MyConstants.sdf.format(Date(dupInfo.time * 1000))
//                temporary.add(dupInfo)
//            }
//        }
//        if (temporary.size > 1) {//只有重复项的文件大于一的时候才会添加
//            val dupHead = DupHead(defImg, imgUrl, "${temporary.size}$term", FileUtils.fmtFileSize(totalSize))
//            multiItemEntityList.add(dupHead)
//            Collections.sort(temporary, Comparator { o1, o2 ->
//                if (o1 != null && o2 != null) {
//                    ((o2.time - o1.time).toInt())
//                } else 0
//            })
//
//            multiItemEntityList.addAll(temporary)
//            val dupBottom = DupBottom(temporary, dupHead, this)
//            selectByIntelligence(mIntelligenceType, dupBottom)
//            mDupBottomList.add(dupBottom)
//            mSelectItemNumber += dupBottom.selectPathList.size
//            val size = dupBottom.selectPathList.size * temporary[0].size
//            mSelectItemSize += dupBottom.selectPathList.size * temporary[0].size
//            multiItemEntityList.add(dupBottom)
//            temporary.forEach {
//                it.onSelectFileListener = dupBottom
//            }
//        }
//        return multiItemEntityList
//    }

    override fun onSelectFile(size: Long) {
        mSelectItemNumber++
        mSelectItemSize += size
        showSelectionUI()
    }

    override fun onDeselectFile(size: Long) {
        mSelectItemNumber--
        mSelectItemSize -= size
        showSelectionUI()
    }


}