package net.linkmate.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.layout_behavior_swipe_recycler.*
import net.linkmate.app.R
import net.linkmate.app.manager.LoginManager
import net.linkmate.app.manager.MessageManager
import net.linkmate.app.ui.adapter.main.MsgModelAdapter
import net.linkmate.app.ui.viewmodel.SystemMessageViewModel
import net.linkmate.app.ui.viewmodel.TrafficPriceEditViewModel
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.protocol.entity.FlowMbpointRatioModel
import net.sdvn.common.repo.EnMbPointMsgRepo
import net.sdvn.common.repo.SdvnMsgRepo
import net.sdvn.common.vo.EnMbPointMsgModel
import net.sdvn.common.vo.MsgCommonModel
import net.sdvn.common.vo.MsgModel
import net.sdvn.common.vo.SdvnMessageModel
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.EmptyUtils
import net.sdvn.nascommon.utils.Utils
import org.view.libwidget.setOnRefreshWithTimeoutListener
import timber.log.Timber

class SystemMsgFragment : BaseFragment() {
    private var msgModelLiveData: LiveData<List<MsgModel<*>>>? = null
    private var isShowHeader: Boolean = true
    private var modelAdapter: MsgModelAdapter? = null

    private val trafficPriceEditViewModel by viewModels<TrafficPriceEditViewModel>()
    private val systemMessageViewModel by viewModels<SystemMessageViewModel>({ requireActivity() })

    private val observer = Observer<List<MsgModel<*>>> { msgModels ->
        if (modelAdapter != null) {
            modelAdapter!!.setNewData(msgModels)
            var isShow = false
            if (msgModels != null) for (msgModel in msgModels) {
                if (!msgModel.isWasRead) {
                    isShow = true
                    break
                }
            }
            showOrHideHeader(isShow)
        }
        swipe_refresh_layout?.isRefreshing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoginManager.getInstance().loginedData.observe(this, Observer { isLogined: Boolean ->
            onLoginChanged(isLogined)
        })
        arguments?.let {
            isShowHeader = it.getBoolean(ARG_IS_SHOW_HEADER, true)
        }
    }

    private fun onLoginChanged(isLogined: Boolean) {
        if (isLogined) {
            val userId: String = CMAPI.getInstance().getBaseInfo().getUserId() ?: ""
            if (EmptyUtils.isNotEmpty(userId)) {
                msgModelLiveData = systemMessageViewModel.getMsgModelLiveData(userId)
                msgModelLiveData?.observe(this, observer)
            } else {
                clearData()
            }
        } else {
            clearData()
        }
    }

    override fun onResume() {
        super.onResume()
        onLoginChanged(LoginManager.getInstance().loginedData.value == true)
    }

    private fun clearData() {
        msgModelLiveData?.removeObserver(observer)
        if (modelAdapter != null) {
            modelAdapter!!.setNewData(null)
            showOrHideHeader(false)
            Timber.d("logout  set list null")
        }
        systemMessageViewModel.clearMsgModelLiveData()
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_behavior_swipe_recycler
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        val layout = LinearLayoutManager(view.context)
        home_nested_scroll_view?.let { mRecyclerView ->
            mRecyclerView.layoutManager = layout
//            val decor = DividerItemDecoration(view.context, layout.orientation)
//            val drawable = view.context.getDrawable(R.drawable.shape_line)?.let {
//                decor.setDrawable(it)
//            }
////            mRecyclerView.addItemDecoration(FloatDecoration(BaseQuickAdapter.HEADER_VIEW))
//            mRecyclerView.addItemDecoration(decor)
            modelAdapter = MsgModelAdapter()

            modelAdapter!!.setOnItemClickListener { baseQuickAdapter, view, i ->
                if (Utils.isFastClick(view)) return@setOnItemClickListener
                val data = baseQuickAdapter.getItem(i)
                when (data) {
                    is EnMbPointMsgModel -> {
                        data.isWasRead = true
                        EnMbPointMsgRepo.saveData(data)
                        if (data.type == FlowMbpointRatioModel.Type.setmbpratio.name) {
                            trafficPriceEditViewModel.showMsgView(view.context, data)
                        }
                    }
                    is SdvnMessageModel -> {
                        systemMessageViewModel.processSystemMsg(requireContext(), data)
                        data.isWasRead = true
                        SdvnMsgRepo.saveData(data)
                    }
                    is MsgCommonModel -> {
                        systemMessageViewModel.processMsgCommon(requireContext(),data)
                        data.isWasRead = true
                        SdvnMsgRepo.updateCommonData(data)
                    }
                }
            }
            modelAdapter!!.setOnItemLongClickListener { baseQuickAdapter, view, i ->
                val data = baseQuickAdapter.getItem(i)
                if (data != null) {
                    systemMessageViewModel.removeMsg(requireContext(), data as MsgModel<*>)
                    return@setOnItemLongClickListener true
                }
                return@setOnItemLongClickListener false
            }
            mRecyclerView.adapter = modelAdapter
        }
        swipe_refresh_layout?.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
//            MessageManager.getInstance().refreshEnMsg(true)
//            MessageManager.getInstance().refreshMessage()
            MessageManager.getInstance().loadNewMsg()

        })
    }

    private fun showOrHideHeader(isShow: Boolean) {
        modelAdapter?.let { modelAdapter ->
            try {
                if (isShow) {
                    modelAdapter.removeAllHeaderView()
                    val context = requireContext()
                    val headerView = LayoutInflater.from(context).inflate(R.layout.layout_header_center_btn, null)
                    val headerTv = headerView.findViewById<TextView>(R.id.header)
                    headerTv.setText(R.string.tips_mark_all_read)
                    headerTv.setOnClickListener {
                        DialogUtils.showConfirmDialog(context, 0, R.string.tips_mark_all_read, R.string.confirm,
                                R.string.cancel) { dialog, isPositiveBtn ->
                            dialog.dismiss()
                            if (isPositiveBtn) {
                                systemMessageViewModel.markAllMsgRead(modelAdapter.data)
                            }
                        }
                    }
                    modelAdapter.addHeaderView(headerView)
                } else {
                    modelAdapter.removeAllHeaderView()
//                if (isShowHeader) {
                    val context = requireContext()
                    val headerView = LayoutInflater.from(context).inflate(R.layout.layout_header_center_btn, null)
                    val headerTv = headerView.findViewById<TextView>(R.id.header)
                    headerTv.visibility = View.GONE
                    modelAdapter.addHeaderView(headerView)
//                } else {
//                }
                }
            } catch (e: Exception) {
                //处理Bug Fragment SystemMsgFragment{6ec2c31} (140b6b85-6054-4d7a-9c68-9b2aba946c28)} not attached to a context.
            }
        }

    }

    companion object {
        fun newInstance(isShowHeader: Boolean): SystemMsgFragment {
            val args = Bundle()
            args.putBoolean(ARG_IS_SHOW_HEADER, isShowHeader)
            val fragment = SystemMsgFragment()
            fragment.arguments = args
            return fragment
        }

        private const val ARG_IS_SHOW_HEADER = "arg_is_show_header"
    }
}