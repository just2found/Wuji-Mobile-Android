package net.linkmate.app.ui.nas.share

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.entity.MultiItemEntity
import kotlinx.android.synthetic.main.fragment_share_v2.*
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.scan.ScanActivity
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objecbox.ShareElementV2
import net.sdvn.nascommon.fileserver.constants.EntityType
import net.sdvn.nascommon.fileserver.constants.ShareCategory
import net.sdvn.nascommon.fileserver.constants.ShareCategory.*
import net.sdvn.nascommon.model.FileTypeItem
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.viewmodel.ShareViewModel2
import net.sdvn.nascommon.widget.TypePopupView
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.showRefreshAndNotify
import java.util.*

class ShareV2Fragment : TipsBaseFragment() {

    private val mViewModel by activityViewModels<ShareViewModel2>()
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mTvTitle: TextView? = null
    private var mAdapter: ShareMultiItemRvAdapter? = null
    private var titleRight: View? = null
    private var mTypePopView: TypePopupView? = null
    private val map = LinkedHashMap<ShareCategory, MutableList<MultiItemEntity>>()
    private val mapTitle = HashMap<ShareCategory, HeaderMultiEntity>()
    private var items = mapOf(ALL to R.string.all,
            SENDING to R.string.sent,
            RECEIVE to R.string.receivable,
            COPY to R.string.copy_file,
            DOWNLOADING to R.string.nas_downloading,
            ERROR to R.string.error,
            COMPLETED to R.string.completed,
            END to R.string.expired)
    private var mCurrentCategory: ShareCategory = ALL

    override fun getLayoutResId(): Int {
        return R.layout.fragment_share_v2
    }

    override fun initView(view: View) {
        mTvTitle = view.findViewById(R.id.tv_title)
        titleRight = view.findViewById(R.id.tv_operator_clean)
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        val mTitleLayout = view.findViewById<View>(R.id.title_layout)
        mRecyclerView = view.findViewById(R.id.recycle_view)
        val layout = LinearLayoutManager(mRecyclerView!!.context)
        mRecyclerView!!.layoutManager = layout
        mAdapter = ShareMultiItemRvAdapter(null, requireActivity())
//        mRecyclerView!!.addOnItemTouchListener(SwipeItemLayout.OnSwipeItemTouchListener(mRecyclerView!!.context))
        mRecyclerView!!.adapter = mAdapter
        val mEmptyView: View = LayoutInflater.from(view.context).inflate(R.layout.layout_empty_view, null)
        mAdapter!!.emptyView = mEmptyView
        mTvTitle!!.setText(R.string.share_file)
        mTvTitle!!.setOnClickListener { mTypePopView?.showPopupTop(mTitleLayout) }
        mSwipeRefreshLayout?.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            if (devId.isNullOrEmpty()) {
                mViewModel.refreshShare()
            } else {
                mViewModel.doGetList(devId)
            }
        }, 1000)
        titleRight!!.setOnClickListener {
            val list = ArrayList<ShareElementV2>()
            if (mAdapter != null) {
                val data = map.get(mCurrentCategory)
                if (data != null) {
                    for (datum in data) {
                        if (datum is ShareV2MultiEntity) {
                            list.add(datum.shareElementV2)
                        }
                    }
                }

            }
            DialogUtils.showConfirmDialog(it.context, R.string.clear_all, 0, R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
                if (isPositiveBtn) {
                    mViewModel.cancelAllCanceled(list)
                }
            }
        }
        view_scan?.visibility = View.VISIBLE
        view_scan?.setOnClickListener {
            val intent = Intent(requireContext(), ScanActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(intent, 110)
        }
        iv_return.setOnClickListener { requireActivity().finish() }
        initTypeView()
        this.mTipsBar = tipsBar
    }

    override fun getTopView(): View? {
        return title_layout
    }

    private fun initTypeView() {

        val mFileTypeList = ArrayList<FileTypeItem>()
        val privateItem = FileTypeItem(items[ALL]!!, R.drawable.icon_share_all,
                R.drawable.icon_share_all, ALL)
        mFileTypeList.add(privateItem)
        val picItem = FileTypeItem(items[SENDING]!!, R.drawable.icon_share_initiate,
                R.drawable.icon_share_initiate, SENDING)
        mFileTypeList.add(picItem)
        val videoItem = FileTypeItem(items[RECEIVE]!!, R.drawable.icon_share_receive,
                R.drawable.icon_share_receive, RECEIVE)
        mFileTypeList.add(videoItem)
        val copy = FileTypeItem(items[COPY]!!, R.drawable.icon_share_copy,
                R.drawable.icon_share_copy, COPY)
        mFileTypeList.add(copy)
        val audioItem = FileTypeItem(items[DOWNLOADING]!!, R.drawable.icon_download_ing,
                R.drawable.icon_download_ing, DOWNLOADING)
        mFileTypeList.add(audioItem)
        val docItem = FileTypeItem(items[ERROR]!!, R.drawable.icon_download_failure,
                R.drawable.icon_download_failure, ERROR)
        mFileTypeList.add(docItem)
        val docItemComplete = FileTypeItem(items[COMPLETED]!!, R.drawable.icon_download_end,
                R.drawable.icon_download_end, COMPLETED)
        mFileTypeList.add(docItemComplete)
        val docItemEnd = FileTypeItem(items[END]!!, R.drawable.icon_share_end,
                R.drawable.icon_share_end, END)
        mFileTypeList.add(docItemEnd)

        mTvTitle!!.setText(privateItem.title)
        mTypePopView = TypePopupView(requireActivity(), mFileTypeList, R.string.share_type)
        mTypePopView!!.setOnItemClickListener { parent, view, position, id ->
            val item = mFileTypeList[position]
            val category = item.flag as ShareCategory?
            if (category != null) {
                showShareCategory(category)
                mCurrentCategory = category
                mTvTitle!!.setText(item.title)
            }
            mTypePopView!!.dismiss()
        }
    }


    private fun showShareCategory(category: ShareCategory) {
        var sectionMultiEntities: MutableList<MultiItemEntity>? = null
        var isHasCanceled = false
        if (ALL == category) {
            sectionMultiEntities = ArrayList()
            for ((key, value) in map) {
                if (value.isNotEmpty()) {
                    sectionMultiEntities.add(mapTitle[key]!!)
                    sectionMultiEntities.addAll(value)
                }
            }
        } else {
            sectionMultiEntities = map[category]
            if (sectionMultiEntities?.isNotEmpty() == true) {
                isHasCanceled = true
            }
        }
        if (titleRight != null) {
            titleRight!!.visibility = if (isHasCanceled) View.VISIBLE else View.GONE
        }

        if (mAdapter != null) {
            mAdapter!!.setNewData(sectionMultiEntities)
            mAdapter!!.expandAll()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.shareElementV2s.observe(this, Observer { source1 ->
            val source2 = if (!devId.isNullOrEmpty()) {
                source1?.filter {
                    if (it.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
                        it.toDevId == devId
                    } else if (it.isType(EntityType.SHARE_FILE_V2_SEND)) {
                        it.srcDevId == devId
                    } else {
                        false
                    }
                }
            } else {
                source1
            }
            val isHasCanceled = false
            if (mAdapter != null) {
                if (map.size == 0) {
                    initMap()
                } else {
                    clearMap()
                }
                if (source2 != null)
                    for (shareElementV2 in source2) {
                        val multiEntity = ShareV2MultiEntity(shareElementV2)
                        if (shareElementV2.state == TransferState.CANCELED) {
                            val sectionMultiEntities = map[END]
                            sectionMultiEntities!!.add(multiEntity)
                        } else if (shareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
                            val sfDownloads = shareElementV2.sfDownloads
                            if (!sfDownloads.isEmpty() && sfDownloads.size > 1) {
                                val iterator = sfDownloads.iterator()
                                while (iterator.hasNext()) {
                                    val next = iterator.next()
                                    multiEntity.addSubItem(SFDownloadEntity(next))
                                }
                            }
                            if (shareElementV2.state == TransferState.WAIT
                                    || shareElementV2.state == TransferState.START
                                    || shareElementV2.state == TransferState.PAUSE) {
                                val sectionMultiEntities = map[DOWNLOADING]
                                sectionMultiEntities!!.add(multiEntity)
                            } else if (shareElementV2.state == TransferState.COMPLETE) {
                                val sectionMultiEntities = map[COMPLETED]
                                sectionMultiEntities!!.add(multiEntity)
                            } else if (shareElementV2.state == TransferState.FAILED) {
                                val sectionMultiEntities = map[ERROR]
                                sectionMultiEntities!!.add(multiEntity)
                            } else if (shareElementV2.type == EntityType.SHARE_FILE_V2_COPY) {
                                val sectionMultiEntities = map[COPY]
                                sectionMultiEntities!!.add(multiEntity)
                            } else {
                                val sectionMultiEntities = map[RECEIVE]
                                sectionMultiEntities!!.add(multiEntity)
                            }
                        } else {
                            val sectionMultiEntities = map[SENDING]
                            sectionMultiEntities!!.add(multiEntity)
                        }
                    }
                showShareCategory(mCurrentCategory)
            }
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout!!.isRefreshing = false
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mSwipeRefreshLayout?.showRefreshAndNotify()
    }

    override fun onStop() {
        super.onStop()
        mSwipeRefreshLayout?.isRefreshing = false
    }

    private fun clearMap() {
        for ((_, value) in map) {
            value.clear()
        }
    }

    private fun initMap() {
        map[RECEIVE] = ArrayList()
        map[COPY] = ArrayList()
        map[DOWNLOADING] = ArrayList()
        map[SENDING] = ArrayList()
        map[ERROR] = ArrayList()
        map[COMPLETED] = ArrayList()
        map[END] = ArrayList()
        mapTitle[SENDING] = HeaderMultiEntity(getString(items[SENDING]!!))
        mapTitle[RECEIVE] = HeaderMultiEntity(getString(items[RECEIVE]!!))
        mapTitle[COPY] = HeaderMultiEntity(getString(items[COPY]!!))
        mapTitle[DOWNLOADING] = HeaderMultiEntity(getString(items[DOWNLOADING]!!))
        mapTitle[ERROR] = HeaderMultiEntity(getString(items[ERROR]!!))
        mapTitle[COMPLETED] = HeaderMultiEntity(getString(items[COMPLETED]!!))
        mapTitle[END] = HeaderMultiEntity(getString(items[END]!!))
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {

    }

    companion object {
        fun newInstance(deviceId: String?): ShareV2Fragment {
            val fragment = ShareV2Fragment()
            if (deviceId == null) {
                return fragment
            }
            val args = Bundle()
            if (!deviceId.isNullOrEmpty()) {
                args.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
            }
            fragment.arguments = args
            return fragment
        }
    }

}
