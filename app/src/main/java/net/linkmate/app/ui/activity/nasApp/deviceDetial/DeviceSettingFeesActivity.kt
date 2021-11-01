package net.linkmate.app.ui.activity.nasApp.deviceDetial

//getTipsBar 与tipsBar的get方法 kotlin 方法重名，所以需要重命名
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.activity_circle_setting_fees.*
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.view.TipsBar
import net.sdvn.nascommon.iface.EBRefreshHardWareDevice
import org.greenrobot.eventbus.EventBus
import kotlinx.android.synthetic.main.include_title_bar.tipsBar as mTipsBar

/**
 * @author Raleigh.Luo
 * date：20/10/18 14
 * describe：
 */
class DeviceSettingFeesActivity : BaseActivity() {
    val viewModel: DeviceSettingFeesViewModel by viewModels()
    private lateinit var adapter: DeviceSettingFeesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circle_setting_fees)
        initView()
        initObserver()
        if (intent.hasExtra(AppConstants.SP_FIELD_DEVICE_ID)) {
            viewModel.isENServer = intent.getBooleanExtra(FunctionHelper.EXTRA_IS_ENSERVER,false)
            viewModel.startRequest(intent.getStringExtra(AppConstants.SP_FIELD_NETWORK),intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID))
        } else {
            finish()
        }
    }

    private fun initView() {
        itb_tv_title.setText(R.string.settting_join_way)
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        adapter = DeviceSettingFeesAdapter(this, viewModel)
        mRecyclerView.adapter = adapter
        itb_iv_left.setOnClickListener({ onBackPressed() })
        mSwipeRefreshLayout
        mSwipeRefreshLayout.setOnRefreshListener {
            viewModel.startRequest()
        }
    }

    private fun initObserver() {
        viewModel.isLoading.observe(this, Observer {
            if (it) {
                mSwipeRefreshLayout.isRefreshing = true
            } else {
                mSwipeRefreshLayout.isRefreshing = false
            }
        })
        viewModel.fees.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })


        viewModel.currentOperateFee.observe(this, Observer {
            //二级弹框－详情
            DevicelDetailActivity.startActivityForResult(this,Intent(this, DevicelDetailActivity::class.java)
                    .putExtra(FunctionHelper.FUNCTION, FunctionHelper.DEVICE_SETTING_FEES_DETIAL)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.devcieId.value)
                    .putExtra(FunctionHelper.EXTRA_ENTITY, it)
                    .putExtra(FunctionHelper.EXTRA_IS_ENSERVER, viewModel.isENServer),
                    FunctionHelper.DEVICE_SETTING_FEES_DETIAL)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FunctionHelper.DEVICE_SETTING_FEES_DETIAL && resultCode == Activity.RESULT_OK) {
            //刷新设备
            EventBus.getDefault().post(EBRefreshHardWareDevice())
            viewModel.startRequest()
        }
    }

    override fun getTopView(): View? {
        return itb_rl
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
    }
}