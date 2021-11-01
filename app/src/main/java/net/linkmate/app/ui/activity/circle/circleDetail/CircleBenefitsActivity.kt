package net.linkmate.app.ui.activity.circle.circleDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.activity_dev_flow_details.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DevManager
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.WindowUtil
import net.linkmate.app.view.TipsBar
import net.linkmate.app.view.adapter.PopupCheckRVAdapter
import net.sdvn.nascommon.utils.Utils
import java.util.*
import kotlinx.android.synthetic.main.activity_dev_flow_details.adfd_ll_expand as mLlExpabd
import kotlinx.android.synthetic.main.activity_dev_flow_details.adfd_rv_devices as mRv
import kotlinx.android.synthetic.main.activity_dev_flow_details.adfd_srl_device as mSrl
import kotlinx.android.synthetic.main.activity_dev_flow_details.adfd_tv_expand as mTvExpabd
import kotlinx.android.synthetic.main.activity_dev_flow_details.itb_iv_left as ivLeft
import kotlinx.android.synthetic.main.activity_dev_flow_details.itb_iv_right as ivRight
import kotlinx.android.synthetic.main.activity_dev_flow_details.itb_rl as rlTitle
import kotlinx.android.synthetic.main.activity_dev_flow_details.itb_tv_title as tvTitle
//getTipsBar 与tipsBar的get方法 kotlin 方法重名，所以需要重命名
import kotlinx.android.synthetic.main.include_title_bar.tipsBar as mTipsBar
/**
 * @author Raleigh.Luo
 * date：20/8/17 14
 * describe：
 */
class CircleBenefitsActivity : BaseActivity() {
    var defaultDeviceId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_flow_details)
        if (intent.hasExtra(FunctionHelper.EXTRA))//是否查看指定设备收益，否则null表示查看全部
            defaultDeviceId = intent.getStringExtra(FunctionHelper.EXTRA)
        tvTitle.setText(R.string.view_benefits)

        tvTitle.setTextColor(resources.getColor(R.color.text_white))
        ivLeft.visibility = View.VISIBLE
        ivLeft.setImageResource(R.drawable.icon_return)
        ivLeft.setOnClickListener(onClickListener)
        tvTitle
        ivRight.setOnClickListener(onClickListener)
        rlTitle
        mLlExpabd.setOnClickListener(onClickListener)
        mTvExpabd
        mRv
        mSrl
        mTvExpabd
        initView()

    }

    private lateinit var emptyView: RelativeLayout
    private lateinit var errorView: RelativeLayout
    private lateinit var adapter: CircleBenefitsAdapter
    private fun initView() {
        emptyView = View.inflate(this, R.layout.pager_empty_text, null) as RelativeLayout
        (emptyView.findViewById(R.id.tv_tips) as TextView).setText(R.string.no_record)
        emptyView.setOnClickListener(View.OnClickListener {
        })
        errorView = View.inflate(this, R.layout.layout_error_view, null) as RelativeLayout
        errorView.setOnClickListener(View.OnClickListener {
        })
        mSrl.setOnRefreshListener {
            mSrl.isRefreshing = false
        }
        adapter = CircleBenefitsAdapter(this)
//        adapter.setEmptyView(emptyView)
        mRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mRv.itemAnimator = null
        mRv.adapter = adapter
    }

    private val onClickListener = View.OnClickListener {
        if (!Utils.isFastClick(it)) {
            when (it.id) {
                R.id.itb_iv_left -> onBackPressed()
                R.id.adfd_ll_expand -> showDevPop()
            }
        }
    }

    private fun showDevPop() {
        if (!DevManager.getInstance().isInitting) {
            val deviceBeans: MutableList<DeviceBean> = ArrayList()
            for (bean in DevManager.getInstance().boundDeviceBeans) {
                if (bean.hardData != null && bean.hardData!!.isEN && bean.mnglevel == 0) {
                    deviceBeans.add(bean)
                }
            }
            val bean = DeviceBean(getString(R.string.all_dev), "", -1, 0)
            bean.id = ""
            deviceBeans.add(0, bean)
            val contentView = LayoutInflater.from(this).inflate(R.layout.popup_rv_check, null, false)
            val window = PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
            val rv: RecyclerView = contentView.findViewById(R.id.popup_rv)
//            val adapter = PopupCheckRVAdapter(deviceBeans, checkedDevId)
            val adapter = PopupCheckRVAdapter(deviceBeans, "")
            //点击device条目
            adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
                val deviceId = (adapter.data[position] as DeviceBean).id
//                if (checkedDevId != deviceId) {
//                    checkedDevId = deviceId
//                    currPage = 1
//                    lastVaildMonth = ""
//                    totalBeans.clear()
//                    trueBeans.clear()
//                    checkedDevName = (adapter.data[position] as DeviceBean).name
//                    initTotal()
//                }
                window.dismiss()
            }
            rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            rv.itemAnimator = null
            rv.adapter = adapter
            window.isOutsideTouchable = true
            window.isTouchable = true
            window.animationStyle = R.style.PopupWindowAnim
            window.setOnDismissListener { WindowUtil.hintShadow(this@CircleBenefitsActivity) }
            WindowUtil.showShadow(this)
            window.showAsDropDown(mLlExpabd, 0, 0)
        } else {
            ToastUtils.showToast(R.string.loading_data)
        }
    }

    override fun getTopView(): View? {
        return rlTitle
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
    }
}