package net.linkmate.app.ui.activity.circle.circleDetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_circle_setting_fees.*
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.view.TipsBar
//getTipsBar 与tipsBar的get方法 kotlin 方法重名，所以需要重命名
import kotlinx.android.synthetic.main.include_title_bar.tipsBar as mTipsBar
/**
 * @author Raleigh.Luo
 * date：20/10/16 10
 * describe：
 */
class CircleSettingFeesActivity : BaseActivity() {
    val viewModel: CircleSettingFeesViewModel by viewModels()
    private lateinit var adapter: CircleSettingFeesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circle_setting_fees)
        initView()
        initObserver()
        if (intent.hasExtra(FunctionHelper.NETWORK_ID)) {
            viewModel.startRequest(intent.getStringExtra(FunctionHelper.NETWORK_ID))
        } else {
            finish()
        }
    }

    private fun initView() {
        itb_tv_title.setText(R.string.settting_join_way)
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        adapter = CircleSettingFeesAdapter(this, viewModel)
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
            val intent = Intent()
                    .putExtra(FunctionHelper.FUNCTION, FunctionHelper.CIRCLE_SETTING_FEES_DETIAL)
                    .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId.value)
                    .putExtra(FunctionHelper.EXTRA_ENTITY, it)
            CircleDetialActivity.startActivityForResult(this,intent,FunctionHelper.CIRCLE_SETTING_FEES_DETIAL)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == FunctionHelper.CIRCLE_SETTING_FEES_DETIAL &&  resultCode == Activity.RESULT_OK){
            viewModel.startRequest()
//            var enable:Boolean? = null
//            var vadd_value:Float? = null
//            data?.let {
//                if(it.hasExtra("enable")){
//                    enable = it.getBooleanExtra("enable",false)
//                }
//                if(it.hasExtra("vadd_value")){
//                    vadd_value = it.getFloatExtra("vadd_value",0f)
//                }
//            }
//            //设置成功，刷新页面
//            adapter.notifyOperatePosition(enable,vadd_value)
        }
    }

    override fun getTopView(): View? {
        return itb_rl
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
    }

}