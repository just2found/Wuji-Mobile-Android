package net.linkmate.app.ui.activity.mine

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_modify_pwd.*
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyConstants
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.poster.database.AppDatabase.Companion.getInstance
import net.linkmate.app.poster.model.UserModel
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.data.remote.UserRemoteDataSource
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader.HttpLoaderStateListener
import java.util.regex.Pattern

class ModifyPwdActivity : BaseActivity(), HttpLoaderStateListener {
    var compile = Pattern.compile(MyConstants.regNumLetterAndChar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_pwd)
        itb_tv_title.setText(R.string.modify_pwd)
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.setOnClickListener { onBackPressed() }

//        ampEtNewPwd.addTextChangedListener(new PasswdTextWatcher(ampEtNewPwd, compile));
        amp_et_old_pwd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                amp_tv_old_pwd?.isVisible = charSequence.length <= 0
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        amp_et_new_pwd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                amp_tv_new_pwd?.isVisible = charSequence.length <= 0
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        amp_et_confirm_pwd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                amp_tv_confirm_pwd?.isVisible = charSequence.length <= 0
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        amp_btn_confirm.setOnClickListener {
            onViewClicked()
        }
        iv_visible.setOnClickListener {
            UIUtils.togglePasswordStatus(it, amp_et_old_pwd)
        }
        iv_visible_1.setOnClickListener {
            UIUtils.togglePasswordStatus(it, amp_et_new_pwd, amp_et_confirm_pwd)
        }
    }

    override fun getTopView(): View? {
        return itb_rl
    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    private fun onViewClicked() {
        val ampEtOldPwd = amp_et_old_pwd
        val oldPwd = ampEtOldPwd.text.toString().trim { it <= ' ' }
        val ampEtNewPwd = amp_et_new_pwd
        val newPwd = ampEtNewPwd.text.toString().trim { it <= ' ' }
        val ampEtConfirmPwd = amp_et_confirm_pwd
        val confirm = ampEtConfirmPwd.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(oldPwd)) {
            ToastUtils.showToast(R.string.pls_input_old_pwd)
            ampEtOldPwd?.requestFocus()
            return
        }
        if (TextUtils.isEmpty(newPwd)) {
            ToastUtils.showToast(R.string.pls_input_new_pwd)
            ampEtNewPwd?.requestFocus()
            return
        }
        if (TextUtils.isEmpty(confirm)) {
            ToastUtils.showToast(R.string.pls_input_new_pwd_again)
            ampEtConfirmPwd?.requestFocus()
            return
        }
        if (newPwd != confirm) {
            ToastUtils.showToast(R.string.new_pwd_are_different)
            ampEtNewPwd?.requestFocus()
            return
        }
        if (!compile.matcher(newPwd).matches()) {
            ToastUtils.showToast(R.string.password_must_contains_num_letter_char)
            return
        }
        modifyPaaaassssswoooooorrrrd(oldPwd, newPwd)
    }

    private fun modifyPaaaassssswoooooorrrrd(oldPwd: String, newPwd: String) {
        UserRemoteDataSource().modifyPwd(oldPwd, newPwd, object : MyOkHttpListener<GsonBaseProtocol>() {
            override fun success(tag: Any?, data: GsonBaseProtocol) {
                updateUser(CMAPI.getInstance().baseInfo.account, newPwd)
                val intent = Intent()
                intent.putExtra("account", CMAPI.getInstance().baseInfo.account)
                intent.putExtra("pwd", newPwd)
                setResult(1, intent)
                onBackPressed()
            }
        }).setHttpLoaderStateListener(this)
//        val loader = ModifyPasswordV2HttpLoader(GsonBaseProtocol::class.java)
//        loader.setParams(oldPwd, newPwd)
//        loader.setHttpLoaderStateListener(this)
//        loader.executor(object : MyOkHttpListener<GsonBaseProtocol>() {
//            override fun success(tag: Any?, data: GsonBaseProtocol) {
//                val intent = Intent()
//                intent.putExtra("account", CMAPI.getInstance().baseInfo.account)
//                intent.putExtra("pwd", newPwd)
//                setResult(1, intent)
//                onBackPressed()
//            }
//        })
    }


    private fun updateUser(account: String, password: String) {
        Thread {
            val dao = getInstance(applicationContext).getUserDao()
            var userModel = dao.getUser(account)
            if (userModel == null) {
                userModel = UserModel(account, password, password)
            }
            if (password != userModel.pwdNew) {
                userModel.pwdNew = password
                dao.insert(userModel)
            }
        }.start()
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

}