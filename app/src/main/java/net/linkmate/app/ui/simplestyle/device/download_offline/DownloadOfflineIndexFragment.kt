package net.linkmate.app.ui.simplestyle.device.download_offline

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.entity.MultiItemEntity
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_download_offline_add.*
import kotlinx.android.synthetic.main.fragment_download_offline_index.*
import kotlinx.android.synthetic.main.fragment_download_offline_index.ivBack
import kotlinx.android.synthetic.main.fragment_download_offline_index.toolbar_layout
import kotlinx.android.synthetic.main.include_swipe_refresh_and_rv.*
import libs.source.common.livedata.Status
import libs.source.common.utils.RateLimiter
import net.linkmate.app.R
import net.linkmate.app.ui.nas.files.V2NasDetailsActivity
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.ui.simplestyle.device.download_offline.DownloadOfflineModel.Companion.CMD_APPOINT_DELETE
import net.linkmate.app.ui.simplestyle.device.download_offline.DownloadOfflineModel.Companion.CMD_DELETE
import net.linkmate.app.ui.simplestyle.device.download_offline.adapter.OdTaskListAdapter
import net.linkmate.app.ui.simplestyle.device.download_offline.data.OfflineDownLoadTask
import net.linkmate.app.ui.simplestyle.device.download_offline.data.TaskListTitle
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.FormatEnsureDialog
import net.linkmate.app.view.ProgressDialog
import net.sdvn.nascommon.BaseFragment
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.FileUtils
import org.view.libwidget.setOnRefreshWithTimeoutListener
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class  DownloadOfflineIndexFragment : BaseFragment() {
    private val viewModel by viewModels<DownloadOfflineModel>({ requireParentFragment() })
    private var mIsAllStart by Delegates.notNull<Boolean>()
    private val MSG_KEY = 1021
    private val INTERVAL = 3000L

    private val mRateLimiter = RateLimiter<Any>(1500, TimeUnit.MILLISECONDS) //点击加间隔

    private val mDeleteList = mutableListOf<String>()//用于记录被删除的数据

    private val mLoadingDialogFragment by lazy {
        ProgressDialog()
    }
    private val mOdTaskListAdapter by lazy {
        val it = OdTaskListAdapter(mutableListOf())
        it.setOnItemClickListener { baseQuickAdapter, view, postion ->
            val item = baseQuickAdapter.getItem(postion)
            item?.let {
                if (mRateLimiter.shouldFetch(it)) {
                    if (item is OfflineDownLoadTask) {
                        when (item.status) {
                            OfflineDownLoadTask.START, OfflineDownLoadTask.LOAD -> {
                                optTaskStatus(item.id, DownloadOfflineModel.CMD_APPOINT_SUSPEND, postion)
                            }
                            OfflineDownLoadTask.SUSPEND -> {
                                optTaskStatus(item.id, DownloadOfflineModel.CMD_APPOINT_START, postion)
                            }
                            OfflineDownLoadTask.ERROR -> {
                                optTaskStatus(item.id, DownloadOfflineModel.CMD_APPOINT_START, postion)
                            }
                            OfflineDownLoadTask.FINISH -> {
                                openItem(item)
                            }
                        }
                    }
                }
            }
        }

        it.setOnItemChildClickListener { baseQuickAdapter, view, postion ->
            val item = baseQuickAdapter.getItem(postion)
            item?.let {
                if (item is TaskListTitle) {
                    if (item.itemType == OdTaskListAdapter.PROGRESS_TITLE) {
                        if (mIsAllStart) {
                            optTaskStatus(DownloadOfflineModel.CMD_SUSPEND, postion)
                        } else {
                            optTaskStatus(DownloadOfflineModel.CMD_START, postion)
                        }
                    } else if (item.itemType == OdTaskListAdapter.COMPLETE_TITLE) {
                        showConfirmDialog(getString(R.string.clear_all_complete_task)) {
                            optTaskStatus(DownloadOfflineModel.CMD_DELETE, postion)
                        }
                    }
                }
            }
        }


        it.setOnItemLongClickListener { baseQuickAdapter, view, postion ->
            val item = baseQuickAdapter.getItem(postion)
            if (item is OfflineDownLoadTask) {
                showMenuPopu(item, view, postion)
            }
            true
        }

        val emptyView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_empty_directory, null)
        it.emptyView = emptyView
        it
    }

    //打开文件夹
    private fun openItem(item: OfflineDownLoadTask) {
        val intent = Intent(requireActivity(), V2NasDetailsActivity::class.java)
        val type = if (item.sharePathType == OneOSFileType.PRIVATE.ordinal) {
            OneOSFileType.PRIVATE
        } else {
            OneOSFileType.PUBLIC
        }
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_PATH, if (item.savePath.isNullOrEmpty()) "/" else item.savePath)
        intent.putExtra(AppConstants.SP_FIELD_FILE_TYPE, type)
        startActivity(intent)
    }


    //CMD
    // 2 暂停所有下载任务
    // 3 恢复所有下载任务
    // 5 移除所有下载任务
    private fun optTaskStatus(cmd: Int, postion: Int) {
        devId?.let {
//            when (cmd) {
//                CMD_DELETE -> {
//                    for (index in (mOdTaskListAdapter.data.size - 1) downTo postion) {
//                        mOdTaskListAdapter.remove(index)
//                    }
//                }
//            }
            mHandler.removeMessages(MSG_KEY)
            viewModel.optTaskStatus(it, "", null, cmd).observe(this, Observer { resource ->
                if (resource.status == Status.SUCCESS) {
                    resource.data?.let { code ->
                        if (code >= 0) {
                            requestDownLoadTaskList()
                        } else {
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(code))
                        }
                    }
                } else {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(resource.code))
                }
                mHandler.sendEmptyMessage(MSG_KEY)
                //mHandler.sendEmptyMessageDelayed(MSG_KEY, INTERVAL)
            })
        }
    }


    //CMD
    // 0 暂停指定id的下载任务
    // 1 恢复指定id的下载任务
    // 4 移除指定id下载任务
    private fun optTaskStatus(id: String, cmd: Int, postion: Int, btsubfile: List<String>? = null) {
        //先修改状态，如果状态不对则进行修正
        when (cmd) {
            DownloadOfflineModel.CMD_APPOINT_START -> {
                val item = mOdTaskListAdapter.getItem(postion)
                if (item is OfflineDownLoadTask) {
                    item.status = OfflineDownLoadTask.START
                    mOdTaskListAdapter.notifyItemChanged(postion)
                }
            }
            DownloadOfflineModel.CMD_APPOINT_SUSPEND -> {
                val item = mOdTaskListAdapter.getItem(postion)
                if (item is OfflineDownLoadTask) {
                    item.status = OfflineDownLoadTask.SUSPEND
                    mOdTaskListAdapter.notifyItemChanged(postion)
                }
            }
            CMD_APPOINT_DELETE -> {
                mDeleteList.add(id)
                mOdTaskListAdapter.remove(postion)
                mOdTaskListAdapter.cleanTitle()
            }
        }

        devId?.let {
            mHandler.removeMessages(MSG_KEY)
            viewModel.optTaskStatus(it, id, btsubfile, cmd).observe(this, Observer { resource ->
                if (resource.status == Status.SUCCESS) {
                    resource.data?.let { code ->
                        if (code >= 0) {

                        } else {
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(code))
                            if (cmd == CMD_APPOINT_DELETE) {
                                mDeleteList.remove(id)
                            }
                        }
                    }
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.code?.let { it1 -> V5HttpErrorNo.getResourcesId(it1) })
                    if (cmd == CMD_APPOINT_DELETE) {
                        mDeleteList.remove(id)
                    }
                }
                mHandler.sendEmptyMessage(MSG_KEY)
               // mHandler.sendEmptyMessageDelayed(MSG_KEY, INTERVAL)
            })

        }
    }


    private fun showMenuPopu(offlineDownLoadTask: OfflineDownLoadTask, view: View, postion: Int) {
        var popupWindow: PopupWindow? = null
        val rootView = LayoutInflater.from(requireContext()).inflate(R.layout.pop_offline_item_click, null)
        val startTv = rootView.findViewById<TextView>(R.id.start_tv)
        val pauseTv = rootView.findViewById<TextView>(R.id.pause_tv)
        val detailTv = rootView.findViewById<TextView>(R.id.detail_tv)
        val deleteTv = rootView.findViewById<TextView>(R.id.delete_tv)
        startTv.setOnClickListener {
            optTaskStatus(offlineDownLoadTask.id, DownloadOfflineModel.CMD_APPOINT_START, postion)
            popupWindow?.dismiss()
        }
        pauseTv.setOnClickListener {
            optTaskStatus(offlineDownLoadTask.id, DownloadOfflineModel.CMD_APPOINT_SUSPEND, postion)
            popupWindow?.dismiss()
        }
        detailTv.setOnClickListener {

            viewModel.mShowItem = offlineDownLoadTask
            findNavController().navigate(R.id.action_index_to_detail, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
            popupWindow?.dismiss()
        }
        deleteTv.setOnClickListener {
            optTaskStatus(offlineDownLoadTask.id, DownloadOfflineModel.CMD_APPOINT_DELETE, postion)
            popupWindow?.dismiss()
        }
//        if (offlineDownLoadTask.btsubfiles != null && offlineDownLoadTask.btsubfiles.size > 1) {
//            detailTv.visibility = View.VISIBLE
//        }
        if (offlineDownLoadTask.status == OfflineDownLoadTask.START || offlineDownLoadTask.status == OfflineDownLoadTask.LOAD) {
            startTv.visibility = View.GONE
        }
        if (offlineDownLoadTask.status == OfflineDownLoadTask.SUSPEND) {
            pauseTv.visibility = View.GONE
        }
        if (offlineDownLoadTask.status == OfflineDownLoadTask.ERROR) {
            detailTv.visibility = View.GONE
            startTv.visibility = View.GONE
            pauseTv.visibility = View.GONE
        }
        if (offlineDownLoadTask.status == OfflineDownLoadTask.FINISH) {
            detailTv.visibility = View.GONE
            startTv.visibility = View.GONE
            pauseTv.visibility = View.GONE
        }

        popupWindow = PopupWindow(rootView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true;
        popupWindow.setBackgroundDrawable(BitmapDrawable()) //只有设置背景之后在focsable为true时点击弹出框外面才会消失，
        if (Build.VERSION.SDK_INT < 24) {
            popupWindow.showAsDropDown(view);
        } else {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = location[0]
            val y = location[1]
            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, view.width / 2, y + (view.height / 3 * 2))
        }

    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_download_offline_index
    }

    override fun getTopView(): View? {
        return toolbar_layout
    }

    private val mHandler = Handler(Looper.getMainLooper()) {
        if (it.what == MSG_KEY) {
            requestDownLoadTaskList()
            true
        }
        false
    }

    //请求下载列表
    private fun requestDownLoadTaskList() {
        mHandler.removeMessages(MSG_KEY)
        devId?.let {
            viewModel.queryTaskList(it).observe(this, Observer { res ->
                mHandler.sendEmptyMessageDelayed(MSG_KEY, INTERVAL)
                swipe_refresh_layout.isRefreshing = false
                if (mLoadingDialogFragment.dialog?.isShowing == true) {
                    mLoadingDialogFragment.dismiss()
                }
                if (res.status == Status.SUCCESS) {
                    res.data?.let { offlineDLTaskResult ->
                        if (offlineDLTaskResult.taskinfo != null) {
                            convertData(offlineDLTaskResult.taskinfo)
                        }
                    }
                }
            })
        }
    }

    private fun showConfirmDialog(contentStr: String, Next: () -> Unit) {
        DialogUtils.showConfirmDialog(requireContext(), "", contentStr, getString(R.string.ok), getString(R.string.cancel),
                DialogUtils.OnDialogClickListener { dialog, isPositive ->
                    if (isPositive) {
                        Next.invoke()
                    }
                })
    }


    override fun onPause() {
        super.onPause()
        mHandler.removeMessages(MSG_KEY)
    }


    override fun onResume() {
        super.onResume()
        recycle_view.adapter = mOdTaskListAdapter
        if (mLoadingDialogFragment.dialog?.isShowing != true) {
            mLoadingDialogFragment.show(childFragmentManager, DownloadOfflineIndexFragment::javaClass.name)
        }
        mHandler.sendEmptyMessageDelayed(MSG_KEY, INTERVAL / 3)
    }


    //只要有一个任务是暂停的就不
    private fun allTaskIsStart(list: MutableList<MultiItemEntity>): Boolean {
        var isAllStart = true//是不是所有任务都已经开始
        list.forEach {
            if (it is OfflineDownLoadTask) {
                if (it.status == OfflineDownLoadTask.SUSPEND) {
                    isAllStart = false
                }
            }
        }
        return isAllStart
    }

    //Boolean 是否存在进行中的任务
    private fun convertData(list: List<OfflineDownLoadTask>): Boolean {
        val dataList = mutableListOf<MultiItemEntity>()
        val processList = mutableListOf<OfflineDownLoadTask>()
        val finishList = mutableListOf<OfflineDownLoadTask>()
        var isAllStart = true//是不是所有任务都已经开始
        for (offlineDownLoadTask in list) {
            if (mDeleteList.contains(offlineDownLoadTask.id))
                continue
            if (offlineDownLoadTask.status == OfflineDownLoadTask.FINISH || (offlineDownLoadTask.filesize > 0 && offlineDownLoadTask.cursize == offlineDownLoadTask.filesize)) {
                offlineDownLoadTask.status = OfflineDownLoadTask.FINISH
                val defImg = FileUtils.fmtFileIcon(offlineDownLoadTask.filename)
                offlineDownLoadTask.defImg = defImg
                var imgUrl: String? = null
                if (defImg == net.sdvn.nascommonlib.R.drawable.icon_device_img || defImg == net.sdvn.nascommonlib.R.drawable.icon_device_vedio) {
                    val srcPath = offlineDownLoadTask.savePath + "/" + offlineDownLoadTask.filename
                    imgUrl = viewModel.genThumbnailUrl(offlineDownLoadTask.sharePathType, srcPath)
                }
                offlineDownLoadTask.imgUrl = imgUrl
                finishList.add(offlineDownLoadTask)
            } else {
                val defImg = FileUtils.fmtFileIcon(offlineDownLoadTask.filename)
                offlineDownLoadTask.defImg = defImg
                processList.add(offlineDownLoadTask)
                if (offlineDownLoadTask.status == OfflineDownLoadTask.ERROR || offlineDownLoadTask.status == OfflineDownLoadTask.SUSPEND) {
                    isAllStart = false
                }
            }
        }

        mIsAllStart = isAllStart
        if (processList.size > 0) {
            val processTitle = TaskListTitle("${getString(R.string.processing)}(${processList.size})", if (mIsAllStart) getString(R.string.pause_all) else getString(R.string.start_all), OdTaskListAdapter.PROGRESS_TITLE)
            dataList.add(processTitle)
            dataList.addAll(processList)
        }
        if (finishList.size > 0) {
            val completeTitle = TaskListTitle("${getString(R.string.complete)}(${finishList.size})", getString(R.string.clear), OdTaskListAdapter.COMPLETE_TITLE)
            dataList.add(completeTitle)
            dataList.addAll(finishList)
        }
        mOdTaskListAdapter.setNewData(dataList)
        return processList.size > 0
    }


    override fun initView(view: View) {
        add_task_fb.setOnClickListener {
            findNavController().navigate(R.id.action_index_to_add, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
        }
        devId?.let {
            viewModel.initSavePath(it)
        }

        ivBack.setOnClickListener {
            requireActivity().finish()
        }
        recycle_view.layoutManager = LinearLayoutManager(requireContext());
        recycle_view.getItemAnimator()?.setAddDuration(0)
        recycle_view.getItemAnimator()?.setChangeDuration(0)
        recycle_view.getItemAnimator()?.setMoveDuration(0)
        recycle_view.getItemAnimator()?.setRemoveDuration(0)
        (recycle_view.getItemAnimator() as SimpleItemAnimator).setSupportsChangeAnimations(false)
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            requestDownLoadTaskList()
            swipe_refresh_layout.isRefreshing = true
        }, 1000)
        swipe_refresh_layout.isEnabled = true
//
    }


    override fun onBackPressed(): Boolean {
        requireActivity().finish()
        return true
    }
}