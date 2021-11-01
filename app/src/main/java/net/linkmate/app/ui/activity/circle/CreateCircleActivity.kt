package net.linkmate.app.ui.activity.circle

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_create_circle.*
import kotlinx.android.synthetic.main.dialog_circle_type.*
import kotlinx.android.synthetic.main.include_title_bar.*
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.data.model.CircleType
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.util.FormDialogUtil
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.FormRowLayout
import net.linkmate.app.view.FormRowLayout.FormRowDate
import net.linkmate.app.view.TipsBar
import net.sdvn.common.internet.core.HttpLoader.HttpLoaderStateListener
import java.util.*
//getTipsBar 与tipsBar的get方法 kotlin 方法重名，所以需要重命名
import kotlinx.android.synthetic.main.include_title_bar.tipsBar as mTipsBar

class CreateCircleActivity() : BaseActivity(), HttpLoaderStateListener {
    val viewModel: CreateCircleViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_circle)
        itb_tv_title.setText(R.string.create_circle)
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        acc_tv_total.text = String.format("%s：%s %s", getString(R.string.total), 0, getString(R.string.score))
        initEvent()
        initObserver()
    }

    private fun initObserver() {
        viewModel.nextButtonEnable.observe(this, Observer { aBoolean: Boolean -> acc_btn_next.setEnabled((aBoolean)) })
        viewModel.isLoading.observe(this, Observer { aBoolean: Boolean -> })
        viewModel.createResult.observe(this, Observer { aBoolean: Boolean ->
            if (aBoolean) { //创建成功
                ToastUtils.showToast(R.string.create_success)
                finish()
            }
        })
        viewModel.isLoading.observe(this, Observer { aBoolean: Boolean ->
            if (aBoolean) {
                mLoadingView.setVisibility(View.VISIBLE)
            } else {
                if (mLoadingView.isShown()) mLoadingView.setVisibility(View.GONE)
            }
        })
    }

    private fun initEvent() {
        itb_iv_left.setOnClickListener(View.OnClickListener { onBackPressed() })
        acc_et_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.circleName = s.toString().trim { it <= ' ' }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun getTopView(): View {
        return (itb_rl)
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

    fun onClick(view: View) {
        if (Utils.isFastClick(view)) return
        when (view.id) {
            R.id.acc_tv_type -> {
                enterSelectTypeDialog()
            }
            R.id.acc_btn_next -> showConfirmDialog()
        }
    }

    /**
     * 进入选择类型页面
     */
    private fun enterSelectTypeDialog() {

        val intent = Intent()
                .putExtra(FunctionHelper.FUNCTION, FunctionHelper.SELECT_CIRCLE_TYPE)
        viewModel.circleType?.modelid?.let {
            //传已经选择的项id
            intent.putExtra(FunctionHelper.EXTRA, it)
        }
        CircleDetialActivity.startActivityForResult(this, intent, FunctionHelper.SELECT_CIRCLE_TYPE)
    }

    private fun showConfirmDialog() {
        val dates: MutableList<FormRowDate> = ArrayList()
        dates.add(FormRowDate(getString(R.string.circle_name), viewModel.circleName))

        viewModel.circleType?.modelprops?.network_scale?.forEach {
            dates.add(FormRowDate(it.title, it.value.toString()))
        }
        viewModel.circleType?.modelprops?.network_fee?.forEach {
            var content = it.value.toString()
            it.key?.let { key ->
                if (key == "create_fee") {
                    content = it.value.toString()
                }
            }
            dates.add(FormRowDate(it.title, content))
        }
        FormDialogUtil.showSelectDialog(this, R.string.create_circle, dates,
                R.string.confirm, object : FormDialogUtil.OnDialogButtonClickListener {
            override fun onClick(v: View, dialog: Dialog) {
                dialog.dismiss()
                //开始创建
                viewModel.startRequestCreateCircle()
            }
        }, R.string.cancel, object : FormDialogUtil.OnDialogButtonClickListener {
            override fun onClick(v: View, dialog: Dialog) {
                dialog.dismiss()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FunctionHelper.SELECT_CIRCLE_TYPE && resultCode == Activity.RESULT_OK) {
            data?.let {
                if (viewModel.circleType == null) {
                    acc_tv_type.visibility = View.GONE
                    iCircleType.visibility = View.VISIBLE
                }
                llCircleTypeForm.removeAllViews()
                viewModel.circleType = data.getSerializableExtra(FunctionHelper.EXTRA_ENTITY) as CircleType.Type

                viewModel.circleType?.modelprops?.network_scale?.forEach {
                    val frl = FormRowLayout(this)
                    frl.title.text = it.title
                    frl.content.text = it.value.toString()
                    llCircleTypeForm.addView(frl)
                }
                viewModel.circleType?.modelprops?.network_fee?.forEach {
                    val frl = FormRowLayout(this)
                    frl.title.text = it.title
                    frl.content.text = it.value.toString()
                    it.key?.let { key ->
                        if (key == "create_fee") {
                            frl.content.text = it.value.toString()
                        }
                    }

                    llCircleTypeForm.addView(frl)
                }
                tvCircleTypeName.text = viewModel.circleType?.modelname

                viewItemHead.visibility = View.GONE
                viewItemFoot.visibility = View.GONE
                //默认勾选选项
                cbCircleTypeCheck.isChecked = true
                cbCircleTypeCheck.setBackgroundResource(R.drawable.bg_item_dev_stroke)
                ivTypeChecked.visibility = View.VISIBLE
                //一直勾选
                cbCircleTypeCheck.setOnCheckedChangeListener { compoundButton, b ->
                    cbCircleTypeCheck.isChecked = true
                }
                cbCircleTypeCheck.setOnClickListener {
                    enterSelectTypeDialog()
                }

                acc_tv_total.text = String.format("%s：%s %s", getString(R.string.total), viewModel.getCreateFee(), getString(R.string.score))
            }
        }
    }
}