package net.linkmate.app.view

import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.arch.core.util.Function
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.lxj.xpopup.impl.PartShadowPopupView
import io.weline.devhelper.DevTypeHelper
import kotlinx.android.synthetic.main.dialog_device_flow_detail_filter.view.*
import net.linkmate.app.R
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.ui.adapter.FlexDeviceAdapter
import net.linkmate.app.ui.adapter.FlexUserAdapter

/**
 * @author Raleigh.Luo
 * date：21/4/23 15
 * describe：流量明细过滤弹框
 */
class DeviceFlowDetailFilterDialog(context: Context) : PartShadowPopupView(context) {
    private var mFilterListener: FilterListener? = null
    fun setFilterListener(mFilterListener: FilterListener) {
        this.mFilterListener = mFilterListener
    }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_device_flow_detail_filter
    }

    //已选中的id
    private var selectedUserId: String? = null
    private var selectedDeviceId: String? = null
    private var flexDeviceAdapter: FlexDeviceAdapter? = null
    private var flexUserAdapter: FlexUserAdapter? = null

    override fun onCreate() {
        super.onCreate()
        val initRecyclerView = { recyclerView: RecyclerView ->
            //view
            val layoutManager = FlexboxLayoutManager(context)
            //主轴为水平方向，起点在左端
            layoutManager.setFlexDirection(FlexDirection.ROW)
            //按正常方向换行
            layoutManager.setFlexWrap(FlexWrap.WRAP)
            //定义项目在副轴轴上如何对齐
            layoutManager.setAlignItems(AlignItems.CENTER)
            //多个轴对齐方式
            layoutManager.setJustifyContent(JustifyContent.FLEX_START)
            recyclerView.layoutManager = layoutManager
            //添加分割线
            //FlexboxItemDecoration错位
        }

        flexDeviceAdapter = FlexDeviceAdapter(context)
        flexUserAdapter = FlexUserAdapter(context)
        flexDeviceAdapter?.selectedListener = object : FlexDeviceAdapter.SelectedListener {
            override fun selected(bean: DeviceBean?, isRecovery: Boolean) {
                if (!isRecovery) flexUserAdapter?.clear()
                bean?.let {
                    flexUserAdapter?.update(it.id, DevTypeHelper.isHasUserData(it.devClass) && !it.isDevDisable())
                    tvUserHint.visibility = View.GONE
                } ?: let {///全部设备
                    tvUserHint.visibility = View.VISIBLE
                }
            }
        }
        mDeviceRecyclerView.adapter = flexDeviceAdapter
        initRecyclerView(mDeviceRecyclerView)

        initRecyclerView(mUserRecyclerView)
        mUserRecyclerView.adapter = flexUserAdapter

        btnReset.setOnClickListener {
            flexDeviceAdapter?.reset()
            tvUserHint.visibility = View.VISIBLE
            flexUserAdapter?.clear()
        }
        btnConfirm.setOnClickListener {
            dismiss()
            selectedUserId = flexUserAdapter?.selectedUserId
            selectedDeviceId = flexDeviceAdapter?.selectedDeviceId
            mFilterListener?.confirm(selectedDeviceId ?: "", flexDeviceAdapter?.getDeviceName()
                    ?: "", selectedUserId ?: "")
        }

        flexUserAdapter?.selectedUserId = selectedUserId
        flexDeviceAdapter?.recovery(selectedDeviceId)
    }

    override fun onShow() {
        super.onShow()
    }

    override fun onDismiss() {
        super.onDismiss()
    }

    fun initData(selectedDeviceId: String, selectedUserId: String) {
        this.selectedUserId = if (TextUtils.isEmpty(selectedUserId)) null else selectedUserId
        this.selectedDeviceId = if (TextUtils.isEmpty(selectedDeviceId)) null else selectedDeviceId
    }

    interface FilterListener {
        fun confirm(deviceId: String, deviceName: String, userId: String)
    }
}