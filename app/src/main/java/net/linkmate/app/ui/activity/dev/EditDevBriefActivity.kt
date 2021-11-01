package net.linkmate.app.ui.activity.dev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import io.objectbox.TxCallback
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.BriefTimeStamp
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import kotlinx.android.synthetic.main.activity_edit_dev_brief.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.TipsBar
import net.linkmate.app.view.notify.LoadingDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession

/**编辑圈子简介
 * @author Raleigh.Luo
 * date：20/10/27 19
 * describe：
 */
class EditDevBriefActivity : BaseActivity() {
    private lateinit var mLoadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_dev_brief)
        initView()
        mLoadingDialog = LoadingDialog(this, false, getString(R.string.loading))

    }

    private var data: String? = null
    private fun initView() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
        data = BriefRepo.getBrief(deviceId, BriefRepo.FOR_DEVICE)?.brief
        etContent.setText(data)
        //可以滚动
        etContent.movementMethod = ScrollingMovementMethod.getInstance()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.tvPublish -> {
                if (data != etContent.text.toString()) {
                    setBriefText()
                }
            }

        }
    }

    private fun setBriefText() {
        if (!CMAPI.getInstance().isEstablished) {
            ToastUtils.showToast(R.string.network_not_available)
            return
        }
        mLoadingDialog.show()
        mLoadingDialog.setCancelVisibility(true)
        mLoadingDialog.setOnCancelClickListener {
            mLoadingDialog.dismiss()
            dispose()
        }
        val content = etContent.text.toString()
        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().setBrief(deviceId, loginSession.ip, LoginTokenUtil.getToken(),
                        BriefRepo.BRIEF_TYPE, BriefRepo.FOR_DEVICE, content)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : V5Observer<BriefTimeStamp>(deviceId) {
                            override fun onSubscribe(d: Disposable) {
                                super.onSubscribe(d)
                                addDisposable(d)
                            }

                            override fun success(result: BaseProtocol<BriefTimeStamp>) {
                                mLoadingDialog.dismiss()
                                if (result.result) {
                                    BriefRepo.insertAsync(deviceId, BriefRepo.FOR_DEVICE, BriefRepo.BRIEF_TYPE, content, result.data?.update_at?.text, TxCallback { result, error ->
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    })
                                } else {
                                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code ?: 0))
                                }
                            }

                            override fun fail(result: BaseProtocol<BriefTimeStamp>) {
                                mLoadingDialog.dismiss()
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code ?: 0))
                            }

                            override fun isNotV5() {
                                mLoadingDialog.dismiss()
                                ToastUtils.showToast(R.string.ec_exception)
                            }

                            override fun retry(): Boolean {
                                return true
                            }
                        })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(errorNo))
            }
        })
    }

    override fun getTopView(): View? {
        return fStatus
    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }
}