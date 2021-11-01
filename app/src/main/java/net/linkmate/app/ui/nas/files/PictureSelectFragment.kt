package net.linkmate.app.ui.nas.files

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.cabriole.decorator.GridSpanMarginDecoration
import kotlinx.android.synthetic.main.fragment_pictrue_select.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.FileInfoHolder
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.CheckableImageButton
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.showRefreshAndNotify


/** 

Created by admin on 2020/10/29,15:27

 */
class PictureSelectFragment : TipsBaseFragment() {
    private var data: FileListModel? = null
    private val filesViewModel by viewModels<FilesViewModel>()
    private var count = Int.MAX_VALUE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (devId.isNullOrEmpty()) {
            ToastUtils.showError(R.string.msg_error_illegal_operation) }
        filesViewModel.liveData.observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                swipe_refresh_layout.isRefreshing = false
                this.data = it.data?.data
                val files = it.data?.data?.files
                if (files.isNullOrEmpty()) {
                    pictureAdapter.setEmptyView(R.layout.layout_empty_view, recycler_view)
                } else {
                    pictureAdapter.setNewData(files)
                }
                if (data?.hasMorePage() == false) {
                    pictureAdapter.loadMoreEnd()
                }
            } else if (it.status == Status.ERROR) {
                swipe_refresh_layout.isRefreshing = false
            }
            refreshAdapter()
        })
        filesViewModel.liveDataSession.observe(this, Observer {
            if (it != null) {
                pictureAdapter.refreshSession(it)
            }
        })
    }

    private fun refreshAdapter() {
        val gridLayoutManager = recycler_view.layoutManager as? GridLayoutManager
        if (data?.files.isNullOrEmpty()) {
            gridLayoutManager?.spanCount = 1
        } else {
            gridLayoutManager?.spanCount = 4
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_pictrue_select
    }

    override fun getTopView(): View? {
        return layout_title
    }

    private val pictureAdapter by lazy {
        SelectPictureAdapter(count).apply {

        }
    }

    override fun onResume() {
        super.onResume()
        arguments?.let {
            it.getInt("count")?.let {
                count = it
            }
        }
        pictureAdapter.count = count
        if (devId != null) {
            SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {
                override fun onSuccess(url: String?, loginSession: LoginSession) {
                    pictureAdapter.refreshSession(loginSession)
                }
            })
        }
        if (data == null) {
            refreshData()
        }
    }

    override fun initView(view: View) {
        tv_title.setText(R.string.title_select_pic)
        val gridLayoutManager = GridLayoutManager(recycler_view.context, 4)
        recycler_view.layoutManager = gridLayoutManager
        recycler_view.addItemDecoration(GridSpanMarginDecoration(
                margin = Dp2PxUtils.dp2px(requireContext(), 1),
                gridLayoutManager = gridLayoutManager
        ))
        pictureAdapter.setOnLoadMoreListener({
            if (data?.hasMorePage() == true) {
                filesViewModel.loadMoreImages(devId!!)
            }
        }, recycler_view)
        pictureAdapter.setOnItemClickListener { adapter, v, position ->
            pictureAdapter.selectPosition(position)
        }
        pictureAdapter.listener = object : OnSelectChangedListener {
            override fun onSelectChangedListener(selectedCount: Int, total: Int) {
                refreshSelectedTips(selectedCount, total)
            }
        }
        recycler_view.adapter = pictureAdapter
        iv_back.setOnClickListener {
            onBackPressed()
        }
        tv_select.setOnClickListener {
            if (pictureAdapter.selectList.isNotEmpty()) {
                FileInfoHolder.getInstance().save(FileInfoHolder.PIC, pictureAdapter.selectList)
                requireActivity().setResult(Activity.RESULT_OK)
                requireActivity().finish()
            }
        }
        swipe_refresh_layout.isRefreshing = true
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            refreshData()
        })
        swipe_refresh_layout.showRefreshAndNotify()

        refreshSelectedTips(0, count)
        refreshAdapter()
    }

    @SuppressLint("SetTextI18n")
    private fun refreshSelectedTips(selectedCount: Int, total: Int) {
        tv_select.isVisible = selectedCount > 0
        tv_select.text = "${getString(R.string.select)}($selectedCount/$total)"
    }

    private fun refreshData() {
        filesViewModel.loadImages(devId!!)
    }

    override fun onBackPressed(): Boolean {
        requireActivity().setResult(Activity.RESULT_CANCELED)
        requireActivity().finish()
        return true
    }
}

class SelectPictureAdapter(var count: Int) : BaseQuickAdapter<OneOSFile, BaseViewHolder>(R.layout.item_rv_grid_pic) {
    private lateinit var session: LoginSession
    val selectList = mutableListOf<OneOSFile>()
    var listener: OnSelectChangedListener? = null

    override fun convert(helper: BaseViewHolder, data: OneOSFile) {
        val imageView = helper.getView<ImageView>(R.id.rv_grid_iv_icon)
        showPicturePreview(session, imageView, data)
        helper.getView<CheckableImageButton>(R.id.rv_grid_cb_select).let {
            it.isVisible = true
            it.isChecked = selectList.contains(data)
        }
    }

    fun selectPosition(position: Int) {
        data.get(position)?.let {
            if (selectList.contains(it)) {
                selectList.remove(it)
                notifyItemChanged(position)
            } else {
                if (count == 1 && selectList.count() == 1) {
                    val indexOf = data.indexOf(selectList.get(0))
                    selectList.clear()
                    selectList.add(it)
                    notifyItemChanged(indexOf)
                    notifyItemChanged(position)
                } else {
                    if (selectList.count() < count) {
                        selectList.add(it)
                        notifyItemChanged(position)
                    } else {

                    }
                }
            }
        }
        listener?.onSelectChangedListener(selectList.count(), count)
    }


    fun refreshSession(session: LoginSession) {
        this.session = session
    }

    private fun showPicturePreview(mLoginSession: LoginSession, imageView: ImageView, file: OneOSFile) {
        val load = if (FileUtils.isGifFile(file.getName())) {
            val url = OneOSAPIs.genDownloadUrl(mLoginSession, file.getAllPath())
            Glide.with(imageView)
                    .asGif()
                    .load(EliCacheGlideUrl(url))
        } else {
            val url = OneOSAPIs.genThumbnailUrl(mLoginSession, file)
            Glide.with(imageView)
                    .asBitmap()
                    .centerCrop()
                    .load(EliCacheGlideUrl(url))
        }
        load.error(file.icon)
                .into(imageView)
    }
}

interface OnSelectChangedListener {
    fun onSelectChangedListener(selectedCount: Int, total: Int)
}