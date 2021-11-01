package net.linkmate.app.ui.activity.circle

//getTipsBar 与tipsBar的get方法 kotlin 方法重名，所以需要重命名
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_join_circle.*
import kotlinx.android.synthetic.main.dialog_circle_join_way.*
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.data.model.CircleJoinWay
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.util.FormDialogUtil
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.FormRowLayout
import net.linkmate.app.view.FormRowLayout.FormRowDate
import net.linkmate.app.view.TipsBar
import net.sdvn.common.internet.core.HttpLoader.HttpLoaderStateListener
import kotlinx.android.synthetic.main.include_title_bar.tipsBar as mTipsBar

class JoinCircleActivity : BaseActivity(), HttpLoaderStateListener {
    val viewModel: JoinCircleViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_circle)
        if (!intent.hasExtra("shareCode")) {
            finish()
        } else {
            initView()
            initEvent()
            initObserver()
            viewModel.startRequestCircleDetail()
        }
    }

    private fun initView() {
        viewModel.shareCode = intent.getStringExtra("shareCode")
        itb_tv_title.setText(R.string.add_share_circle)
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        ajc_tv_total.text = String.format("%s：%s %s", getString(R.string.total), 0, getString(R.string.score))
        tvUser.setContent("")
        tvENDevice.setContent("")
        ajc_ttv_name.setContent("")
        ajc_ttv_owner.setContent("")
        cbJoinWayCheck.setOnCheckedChangeListener { compoundButton, b ->
            cbJoinWayCheck.isChecked = true
        }
        cbJoinWayCheck.setOnClickListener {
            showSelectJoinWayDialog()
        }
    }

    private fun initEvent() {
        itb_iv_left.setOnClickListener { onBackPressed() }
    }

    /**
     * 初始化简介
     */
    private fun initBrief(mainENDeviceId: String?) {
        if (TextUtils.isEmpty(mainENDeviceId)) {
            tvBriefTitle.visibility = View.GONE
            tvBrief.visibility = View.GONE
        } else {
            tvBriefTitle.visibility = View.VISIBLE
            tvBrief.visibility = View.VISIBLE
        }
        tvBrief.setOnClickListener {
            viewModel.subscribeMainENResult?.removeObserver(subscribeMainENResultObserver)
            //更改 dialog 状态 为订阅主EN
            status = LoadingStatus.SUBSCRIBE_MAIN_EN
            showLoading(R.string.loading, true)
            //观察
            viewModel.subscribeMainENResult = viewModel.startSubscribeMainEN()
            viewModel.subscribeMainENResult?.observe(this, subscribeMainENResultObserver)
        }

    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initObserver() {
        viewModel.circleDetail.observe(this, Observer { circle ->
            //初始化简介
            initBrief(circle.getMainENDeviceId())
            ajc_ttv_name.setContent(circle.networkname)
            ajc_ttv_owner.setContent(circle.getFullName())
            circle.networkprops?.network_scale?.let {

                //最大用户数
                var accountMaxValue = "0"
                val accountMax = it.filter {
                    it.key == "acct_max"
                }
                if (accountMax != null && accountMax.size > 0) {
                    accountMaxValue = accountMax?.get(0)?.value ?: "0"
                }

                //最大EN设备数
                var maxDeviceValue = "0"
                val maxDevice = it.filter {
                    it.key == "provide_max"
                }
                if (maxDevice != null && maxDevice.size > 0) {
                    maxDeviceValue = maxDevice?.get(0)?.value ?: "0"
                }

                tvUser.setContent(String.format("%s %s / %s %s", getString(R.string.current), circle.networkprops?.acct_count
                        ?: 0, getString(R.string.max), accountMaxValue))
                tvENDevice.setContent(String.format("%s %s / %s %s", getString(R.string.current), circle.networkprops?.provide_count
                        ?: 0, getString(R.string.max), maxDeviceValue))
            }

        })

        viewModel.isLoading.observe(this, Observer { aBoolean ->
            if (aBoolean) {
                mLoadingView.visibility = View.VISIBLE
            } else {
                if (mLoadingView.isShown) mLoadingView.visibility = View.GONE
            }
        })
        viewModel.joinResult.observe(this, Observer {
            if (it) {
                finish()
            }
        })
        viewModel.cancelLoading.observe(this, Observer {
            if (it == LoadingStatus.SUBSCRIBE_MAIN_EN) {//取消了订阅简介
                //取消，即不处理简介请求返回response
                viewModel.subscribeMainENResult?.removeObserver(subscribeMainENResultObserver)
            }
        })
    }

    private val subscribeMainENResultObserver = Observer<Boolean> {
        it?.let {
            //恢复 dialog 默认状态
            status = LoadingStatus.DEFUALT
            dismissLoading()
            if (it) {//订阅成功， 访问圈子简介
                accessCircleBrief()
            }
        }
    }

    /**
     * 访问圈子简介
     */
    private fun accessCircleBrief() {
        viewModel.circleDetail.value?.let { circle ->
            circle.getMainENDeviceId()?.let {
                val devId = it
                CircleBriefActivity.start(this, devId,
                        circle.networkname, circle.getFullName(),
                        false, circle.networkid)
            } ?: let {//找不到设备对象
                ToastUtils.showToast(R.string.not_find_main_en)
            }
        }
    }

    override fun getTopView(): View {
        return itb_rl
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
    }

    override fun onLoadStart(disposable: Disposable) {
        addDisposable(disposable)
        showLoading()
    }

    override fun onLoadComplete() {
        dismissLoading()
    }

    override fun onLoadError() {
        dismissLoading()
    }

    /**
     * 选择加入方式弹框
     */
    private fun showSelectJoinWayDialog() {
        viewModel.circleDetail.value?.let {
            val intent = Intent()
                    .putExtra(FunctionHelper.NETWORK_ID, it.networkid)
                    .putExtra(FunctionHelper.FUNCTION, FunctionHelper.CIRCLE_SELECT_JOIN_WAY)
            viewModel.seletedJoinMode?.feeid?.let {
                intent.putExtra(FunctionHelper.POSITION, it)
            }
            CircleDetialActivity.startActivityForResult(this,intent,FunctionHelper.CIRCLE_SELECT_JOIN_WAY)
        }
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.ajc_tv_introduction -> {
            }
            R.id.ajc_tv_type -> {
                showSelectJoinWayDialog()
            }
            R.id.ajc_btn_next -> showConfirmDialog()
        }
    }

    private fun getSubPanel(title: String, content: String): FormRowLayout {
        val frl = FormRowLayout(this)
        frl.title.text = title
        frl.content.text = content
        return frl
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FunctionHelper.CIRCLE_SELECT_JOIN_WAY && resultCode == Activity.RESULT_OK) {
            data?.let {
                if (data.hasExtra(FunctionHelper.EXTRA_ENTITY)) {
                    if (viewModel.seletedJoinMode == null) {
                        ajc_tv_type.visibility = View.GONE
                        iJoinWayType.visibility = View.VISIBLE
                    }
                    viewModel.seletedJoinMode = data.getSerializableExtra(FunctionHelper.EXTRA_ENTITY) as CircleJoinWay.Fee
                    viewModel.seletedJoinMode?.let {
                        llCircleTypeForm.removeAllViews()
                        tvJoinWay.setText(it.title)
                        ajc_tv_total.text = String.format("%s：%s %s", getString(R.string.total), it.getValueText(), getString(R.string.score))
                        llCircleTypeForm.addView(getSubPanel(getString(R.string.use_life), it.getDurationText()))
                        llCircleTypeForm.addView(getSubPanel(getString(R.string.required_points), it.getValueText()))
                        ajc_btn_next.isEnabled = true
                        //滚到底部
//                    scrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }
    }

    private fun showConfirmDialog() {
        viewModel.seletedJoinMode?.let {
            val dates: MutableList<FormRowDate> = ArrayList()
            dates.add(FormRowDate(getString(R.string.circle_name), viewModel.circleDetail.value?.networkname))
            dates.add(FormRowDate(getString(R.string.owner), viewModel.circleDetail.value?.getFullName()))
            dates.add(FormRowDate(getString(R.string.join_way), it.title))
            dates.add(FormRowDate(getString(R.string.use_life), it.getDurationText()))
            dates.add(FormRowDate(getString(R.string.required_points), it.getValueText()))
            FormDialogUtil.showSelectDialog(this, R.string.add_share_circle, dates,
                    R.string.confirm, { v, dialog ->
                dialog.dismiss()
                viewModel.startRequestJoinCircle()
            }, R.string.cancel) { v, dialog -> dialog.dismiss() }
        }
    }
}