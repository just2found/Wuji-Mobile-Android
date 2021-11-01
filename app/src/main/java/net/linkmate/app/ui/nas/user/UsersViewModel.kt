package net.linkmate.app.ui.nas.user

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.arch.core.util.Function
import androidx.core.view.isGone
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.PermissionsModel
import io.weline.repo.data.model.User
import io.weline.repo.data.model.UserSpace
import io.weline.repo.files.data.SharePathType
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import libs.source.common.utils.EmptyUtils
import libs.source.common.utils.InputMethodUtils
import libs.source.common.utils.ToastHelper
import net.linkmate.app.R
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceControlRepository
import net.linkmate.app.ui.nas.V5RepositoryWrapper
import net.linkmate.app.view.HintEditDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.GradeBindDeviceHttpLoader
import net.sdvn.common.internet.loader.UnbindDeviceHttpLoader
import net.sdvn.common.internet.protocol.UnbindDeviceResult
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.OneOSHardDisk
import net.sdvn.nascommon.model.oneos.OneOSUser
import net.sdvn.nascommon.model.oneos.api.sys.OneOSSpaceAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.viewmodel.RxViewModel
import timber.log.Timber

/**
 *
 * @Description: User Settings
 * @Author: todo2088
 * @CreateDate: 2021/3/6 18:10
 */
class UsersViewModel : RxViewModel() {
    var total: Long = 0
    var isAdmin: Boolean = false
    var isOwner: Boolean = false
    private val _liveDataUser: MutableLiveData<OneOSUser> = MutableLiveData()
    val liveDataUser: LiveData<OneOSUser> = _liveDataUser

    private val _liveDataShareUser: MutableLiveData<ShareUser> = MutableLiveData()
    val liveDataShareUser: LiveData<ShareUser> = _liveDataShareUser

    private val _liveDataLoading: MutableLiveData<Resource<String>> = MutableLiveData()
    val liveDataLoading = _liveDataLoading

    private val _liveDataUnBind: MutableLiveData<Resource<String>> = MutableLiveData()
    val liveDataUnBind = _liveDataUnBind

    private val _liveDataRemoveUser: MutableLiveData<Resource<String>> = MutableLiveData()
    val liveDataRemoveUser = _liveDataRemoveUser
    private val _liveDataChangeMgrLevel: MutableLiveData<Resource<Pair<ShareUser,Int>>> = MutableLiveData()
    val liveDataChangeMgrLevel = _liveDataChangeMgrLevel


    fun setPermission(devId: String, oneOSUser: OneOSUser, pathType: SharePathType, perm: Int) {
        val username = oneOSUser.name
        if (username.isNullOrEmpty()) {
            ToastHelper.showLongToast(R.string.tip_pls_retry_later)
            return
        }
        V5RepositoryWrapper.setPermission(devId, username, pathType, perm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : V5Observer<Any>(devId) {
                    override fun success(result: BaseProtocol<Any>) {
                        oneOSUser.permissions?.find {
                            it.sharePathType == pathType.type
                        }?.apply {
                            this.perm = perm
                        }
                        _liveDataUser.postValue(oneOSUser)
                    }

                    override fun fail(result: BaseProtocol<Any>) {

                    }

                    override fun isNotV5() {
                    }
                })
    }

    fun getPermission(oneOSUser: OneOSUser, pathType: SharePathType): PermissionsModel? {
        return oneOSUser.permissions?.find { it.sharePathType == pathType.type }
    }

    fun setUser(osUser: OneOSUser) {
        _liveDataUser.postValue(osUser)
    }

    fun setShareUser(shareUser: ShareUser) {
        _liveDataShareUser.postValue(shareUser)
        _liveDataLoading.postValue(Resource.success(null))
    }


    fun deleteUser(context: Context, devId: String, user: OneOSUser, isOwner: Boolean, isAdmin: Boolean, delUid: String?, hintEditDialog: HintEditDialog?,isUnbind:Boolean =false): HintEditDialog? {
        val curUid = CMAPI.getInstance().baseInfo.userId
        if (isOwner) {
            if (curUid == delUid) { //所有者删除自己，要先转移所有者权限
                return showOwnerDeleteSelfDialog(context, devId, user, delUid, hintEditDialog)
            } else { //所有者删除其他用户
                return showDeleteUserDialog(context, devId, user, delUid, hintEditDialog,isUnbind)
            }
        } else if (curUid == delUid) { //用户删除自己账号
            if (isAdmin) {//管理员删除自己
                return showManangerDeleteSelfDialog(context, devId, user, delUid, hintEditDialog)
            } else {
                return showDeleteUserDialog(context, devId, user, delUid, hintEditDialog)
            }
        } else if (isAdmin && user.isAdmin != 0 && user.isAdmin != 1) { //管理员删除其他用户
            return showDeleteUserDialog(context, devId, user, delUid, hintEditDialog)
        } else { //非所有者用户删除其他普通用户
            DialogUtils.showNotifyDialog(context, R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null)
        }
        return null
    }


    /**
     * 所有者删除自己提示框
     */
    private fun showOwnerDeleteSelfDialog(context: Context, devId: String, delUser: OneOSUser, delUserID: String?, hintEditDialog: HintEditDialog?): HintEditDialog? {
        if (hintEditDialog?.dialog?.isShowing != true) {
            //本地用户  勾选清除用户数据，且不可取消
            val checkboxEnable: Boolean = !delUserID.isNullOrEmpty()
            val clearUserDataEnable: Boolean = delUser.type and OneOSUser.TYPE_LOCAL != 0
            hintEditDialog?.update(
                    title = context.getString(R.string.remove_device_title),
                    content = context.getString(R.string.manager_remove_device_content),
                    contentTextColor = R.color.red,
                    editHint = context.getString(R.string.manager_remove_device_hint),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText = context.getString(R.string.clear_user_data_hint),
                    isCheckedBox = clearUserDataEnable,
                    checkboxEnable = clearUserDataEnable,
                    confrimText = context.getString(R.string.confirm),
                    cancelText = context.getString(R.string.cancel)
            )
            val applicationContext = context.applicationContext
            hintEditDialog?.setOnClickListener(View.OnClickListener { view ->
                if (view?.id == R.id.positive) {
                    val isCheckedBox = hintEditDialog.isCheckedBox() ?: false
                    hintEditDialog.dismiss()
                    val repository = DeviceControlRepository()
                    if (isCheckedBox) {//勾选了清除全部用户数据
                        _liveDataLoading.postValue(Resource.loading())
                        //清除所有用户数据
                        repository.clearNasUser(devId, Function {
                            //解绑所有人
                            repository.clearDeviceUser(devId, null, Function {
                                if (it) {
                                    _liveDataUnBind.postValue(Resource.success(delUserID))
                                    sendRemoveDevBroadcast(applicationContext, devId)
                                }
                                null
                            })
                            null
                        }, Function {
                            ToastHelper.showToast(it)
                            _liveDataLoading.postValue(Resource.error(it, null))
                            null
                        })
                    } else {
                        //解绑所有人
                        repository.clearDeviceUser(devId, null, Function {
                            if (it) {
                                _liveDataUnBind.postValue(Resource.success(delUserID))
                                sendRemoveDevBroadcast(applicationContext, devId)
                            }
                            null
                        })
                    }
                }
            })
        }
        return hintEditDialog
    }

    /**
     * 管理员删除自己提示框
     */
    private fun showManangerDeleteSelfDialog(context: Context, devId: String, delUser: OneOSUser, delUserID: String?, hintEditDialog: HintEditDialog?): HintEditDialog? {
        if (hintEditDialog?.dialog?.isShowing != true) {
            //本地用户  勾选清除用户数据，且不可取消
            val checkboxEnable: Boolean = !delUserID.isNullOrEmpty()
            val clearUserDataEnable: Boolean = delUser.type and OneOSUser.TYPE_LOCAL != 0
            hintEditDialog?.update(
                    title = context.getString(R.string.remove_device_title),
                    content = context.getString(R.string.manager_remove_device_content),
                    contentTextColor = R.color.red,
                    editHint = context.getString(R.string.manager_remove_device_hint),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText = context.getString(R.string.clear_user_data_hint),
                    isCheckedBox = clearUserDataEnable,
                    checkboxEnable = clearUserDataEnable,
                    confrimText = context.getString(R.string.confirm),
                    cancelText = context.getString(R.string.cancel)
            )
            val applicationContext = context.applicationContext
            hintEditDialog?.setOnClickListener(View.OnClickListener { view ->
                if (view?.id == R.id.positive) {
                    val isCheckedBox = hintEditDialog?.isCheckedBox() ?: false
                    hintEditDialog?.dismiss()
                    if (isCheckedBox) {//勾选了清除数据
                        if (delUser.type and OneOSUser.TYPE_LOCAL != 0) {
                            deleteUserInDevice(applicationContext, devId, delUserID, delUser!!)
                        } else {
                            if (!delUserID.isNullOrEmpty()) {
                                unbind(applicationContext, devId, delUserID)
                            }
                        }
                    } else {
                        if (!delUserID.isNullOrEmpty()) {
                            unbind(applicationContext, devId, delUserID)
                        }
                    }
                }
            })
        }
        return hintEditDialog
    }


    /**
     * 非管理员／所有者删除自己提示框
     */
    private fun showDeleteUserDialog(context: Context, devId: String, delUser: OneOSUser, delUserID: String?, hintEditDialog: HintEditDialog?,isUnbind:Boolean =false): HintEditDialog? {
        if (hintEditDialog?.dialog?.isShowing != true) {
            //本地用户  勾选清除用户数据，且不可取消
            val checkboxEnable: Boolean = !delUserID.isNullOrEmpty()
            val clearUserDataEnable: Boolean = delUser.type and OneOSUser.TYPE_LOCAL != 0
            val clearUserDataEnable1= if(delUser.isRemote && !isUnbind)
            {
                clearUserDataEnable
            }else{
                false
            }
            hintEditDialog?.update(
                    title = context.getString(R.string.delete_user),
                    content = context.getString(R.string.delete_user_hint),
                    contentTextColor = R.color.red,
                    editHint = context.getString(R.string.confirm_delete),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText =  if(clearUserDataEnable1)context.getString(R.string.clear_user_data_hint) else null,
                    isCheckedBox = clearUserDataEnable1,
                    checkboxEnable = clearUserDataEnable1,
                    confrimText = context.getString(R.string.confirm),
                    cancelText = context.getString(R.string.cancel)
            )
            val applicationContext = context.applicationContext
            hintEditDialog?.setOnClickListener(View.OnClickListener { view ->
                if (view?.id == R.id.positive) {
                    val isCheckedBox = hintEditDialog?.isCheckedBox() ?: false
                    hintEditDialog?.dismiss()
                    if(!delUser.isRemote)
                    {
                        deleteUserInDevice(applicationContext, devId, delUserID, delUser)
                    }else{
                        if (isCheckedBox) {//勾选了清除数据
                            if (delUser.type and OneOSUser.TYPE_LOCAL != 0) {
                                deleteUserInDevice(applicationContext, devId, delUserID, delUser,isUnbind)
                            } else {
                                if (!delUserID.isNullOrEmpty()) {
                                    unbind(applicationContext, devId, delUserID)
                                }
                            }
                        } else {
                            if (!delUserID.isNullOrEmpty() ) {
                                unbind(applicationContext, devId, delUserID)
                            }
                        }
                    }
                }
            })
        }
        return hintEditDialog
    }

    private fun deleteUserInDevice(context: Context, devId: String, delUid: String?, delUser: OneOSUser,isUnbind:Boolean =false) {
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String, mLoginSession: LoginSession) {

                //删除用户
                val listener = object : OneOSUserManageAPI.OnUserManageListener {
                    override fun onStart(url: String) {
                        _liveDataLoading.postValue(Resource.loading(getString(R.string.delete_user)))
                    }

                    override fun onSuccess(url: String, cmd: String) {
                        if (!delUid.isNullOrEmpty()&& delUser.isRemote && !isUnbind) {
                            unbind(context.applicationContext, devId, delUid)
                        } else {
                            _liveDataLoading.postValue(Resource.success(getString(R.string.delete_user_succeed)))
                            _liveDataRemoveUser.postValue(Resource.success(delUser.name))
                        }
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        val errorMsgVar = HttpErrorNo.getResultMsg(errorNo, errorMsg)
                        ToastHelper.showToast(errorMsgVar)
                        _liveDataLoading.postValue(Resource.error("error", errorMsgVar))
                    }
                }

                val observer = object : V5Observer<Any>(mLoginSession.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        listener.onSuccess("", "delete")
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {
                        val manageAPI = OneOSUserManageAPI(mLoginSession)
                        manageAPI.setOnUserManageListener(listener)
                        manageAPI.delete(delUser.name)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().deleteUser(mLoginSession.id
                                ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), delUser.name
                                ?: "", this)
                        return true
                    }
                }
                V5Repository.INSTANCE().deleteUser(mLoginSession.id
                        ?: "", mLoginSession.ip, LoginTokenUtil.getToken(),
                        delUser.name ?: "", observer)
            }
        })

    }

    private fun unbind(context: Context, devId: String, userId: String) {
        _liveDataLoading.postValue(Resource.loading())
        //用户解绑
        val unbindDeviceHttpLoader = UnbindDeviceHttpLoader()

        unbindDeviceHttpLoader.unbindSingle(devId, userId, object : ResultListener<UnbindDeviceResult> {
            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                _liveDataLoading.postValue(Resource.error(null, getString(R.string.remove_device_failed)))
            }

            override fun success(tag: Any?, mGsonBaseProtocol: UnbindDeviceResult) {
                _liveDataLoading.postValue(Resource.success(LibApp.instance.getApp().getString(R.string.remove_success)))
                ToastHelper.showToast(R.string.remove_success)
                _liveDataUnBind.postValue(Resource.success(userId))
                sendRemoveDevBroadcast(context, devId)
            }
        })

    }

    fun sendRemoveDevBroadcast(context: Context, devId: String) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(AppConstants.LOCAL_BROADCAST_REMOVE_DEV).apply {
            putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
        })
    }

    fun getString(@StringRes resId: Int): String {
        return LibApp.instance.getApp().getString(resId)
    }


    fun editMarkName(context: Context, devId: String, user: OneOSUser) {
        val markName = user.markName ?: ""
        DialogUtils.showEditDialog(context, R.string.tip_set_user_name, R.string.hint_set_user_name, markName,
                net.sdvn.nascommonlib.R.string.max_name_length,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn, mContentEditText ->
            val context = mContentEditText.context
            if (isPositiveBtn) {
                val newName = mContentEditText.text.toString().trim { it <= ' ' }
                if (EmptyUtils.isEmpty(newName) || newName.length > 16) {
                    AnimUtils.sharkEditText(context, mContentEditText)
                } else {
                    changUserMarkName(devId, user, newName)
                    InputMethodUtils.hideKeyboard(context, mContentEditText)
                    dialog.dismiss()
                }
            } else {
                InputMethodUtils.hideKeyboard(context, mContentEditText)
                dialog.dismiss()
            }
        }


    }

    private fun changUserMarkName(devId: String, user: OneOSUser, newName: String) {
        val username = user.name ?: return
        Timber.d("changUserMarkName :$devId $newName")
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, data: LoginSession) {
                val listener = object : OneOSUserManageAPI.OnUserManageListener {
                    override fun onStart(url: String) {

                    }

                    override fun onSuccess(url: String, cmd: String) {
                        user.markName = newName
                        _liveDataShareUser.value?.devMarkName = newName
                        _liveDataUser.postValue(user)
                        ToastHelper.showLongToastSafe(R.string.modify_succeed)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        ToastHelper.showLongToastSafe(HttpErrorNo.getResultMsg(false, errorNo, errorMsg))

                    }
                }


                val observer = object : V5Observer<Any>(data.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        listener.onSuccess("", "")
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {
                        val oneOSUserManageAPI = OneOSUserManageAPI(data)
                        oneOSUserManageAPI.setOnUserManageListener(listener)
                        oneOSUserManageAPI.addMarkName(username, newName)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().setUserMark(data.id
                                ?: "", data.ip, LoginTokenUtil.getToken(), username, newName, this)
                        return true
                    }
                }
                V5Repository.INSTANCE().setUserMark(data.id
                        ?: "", data.ip, LoginTokenUtil.getToken(), username, newName, observer)

            }
        })

    }


    fun showChangedSpaceDialog(context: Context, devId: String, user: OneOSUser, totalGB: Long) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_space, null)
        val mDialog = Dialog(context, R.style.DialogTheme)
        val mTitleTxt = dialogView.findViewById<TextView>(R.id.txt_title)
        val switchUnlimited = dialogView.findViewById<Switch>(R.id.switch_unlimited)
        mTitleTxt.setText(R.string.modify_user_space)
        val mEditText = dialogView.findViewById<EditText>(R.id.et_content)
        val tvHint = dialogView.findViewById<TextView>(R.id.tv_content)
        val tipsView = dialogView.findViewById<TextView>(R.id.txt_tips)
        tipsView.setText(R.string.tips_set_change_space)
        tipsView.visibility = View.GONE
        mEditText.inputType = InputType.TYPE_CLASS_NUMBER
        tvHint.setText(R.string.tips_modify_user_space)
        val total = totalGB / 1024 / 1024 / 1024
        val userSpace = user.space ?: 0

        if (userSpace > 0 && ((userSpace) / 1024 / 1024 / 1024) != total) {
            val spaceInt = ((userSpace) / 1024 / 1024 / 1024).toInt()
            mEditText.setText(spaceInt.toString())
            mEditText.setSelection(0, mEditText.length())
            tvHint.isEnabled = false
        } else {
            switchUnlimited.isChecked = true
            mEditText.isEnabled = false
            tvHint.isEnabled = true
        }

        mEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvHint.isGone = mEditText.text.isNotEmpty()
                if (!s.isNullOrEmpty()) {
                    val s1 = "$total"
                    try {
                        val toLong = Integer.valueOf(s.toString()).toLong()
                        if (toLong > total) {
                            updateEdit(s1)
                        }
                    } catch (e: Exception) {
                        updateEdit(s1)
                    }
                }
            }

            private fun updateEdit(s1: String) {
                mEditText.setText(s1)
                mEditText.setSelection(s1.length)
                AnimUtils.sharkEditText(context, mEditText)
            }
        })
        switchUnlimited.setOnCheckedChangeListener { _, isChecked ->
            mEditText.isEnabled = !isChecked
            tvHint.isEnabled = isChecked
            mEditText.setText("")
        }
        InputMethodUtils.showKeyboard(context, mEditText, 200)

        val positiveBtn = dialogView.findViewById<TextView>(R.id.positive)
        positiveBtn.setText(R.string.modify)
        positiveBtn.visibility = View.VISIBLE

        positiveBtn.setOnClickListener {
            var space = "0"
            if (!switchUnlimited.isChecked) {
                space = mEditText.text.toString().trim { it <= ' ' }
            }
            val spaceLong = try {
                Integer.valueOf(space).toLong()
            } catch (e: Exception) {
                total
            }
            if (EmptyUtils.isEmpty(space) || spaceLong < 0 || spaceLong > total) {
                AnimUtils.sharkEditText(context, mEditText)
            } else {
                try {
                    SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {

                        override fun onSuccess(url: String, mLoginSession: LoginSession) {

                            val listener = object : OneOSUserManageAPI.OnUserManageListener {
                                override fun onStart(url: String) {
//                                    showLoading(R.string.modifying)
                                }

                                override fun onSuccess(url: String, cmd: String) {
                                    ToastHelper.showLongToastSafe(R.string.modify_succeed)
                                    user.space = (if (spaceLong > 0L) spaceLong else total) * 1024 * 1024 * 1024
                                    _liveDataUser.postValue(user)
                                }

                                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                                    ToastHelper.showLongToastSafe(HttpErrorNo.getResultMsg(errorNo, errorMsg))
                                }
                            }


                            val observer = object : V5Observer<UserSpace>(mLoginSession.id ?: "") {
                                override fun success(result: BaseProtocol<UserSpace>) {
                                    listener.onSuccess("", "")
                                }

                                override fun fail(result: BaseProtocol<UserSpace>) {
                                    listener.onFailure("", result.error?.code
                                            ?: 0, result.error?.msg ?: "")
                                }

                                override fun isNotV5() {//调用旧接口
                                    val manageAPI = OneOSUserManageAPI(mLoginSession)
                                    manageAPI.setOnUserManageListener(listener)
                                    manageAPI.chspace(user.name, spaceLong)
                                }

                                override fun retry(): Boolean {
                                    V5Repository.INSTANCE().setUserSpace(mLoginSession.id
                                            ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), user.name
                                            ?: "", spaceLong, this)
                                    return true
                                }
                            }
                            V5Repository.INSTANCE().setUserSpace(mLoginSession.id
                                    ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), user.name
                                    ?: "", spaceLong, observer)

                            InputMethodUtils.hideKeyboard(context, mEditText)
                            mDialog.dismiss()
                        }
                    })

                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastHelper.showToast(R.string.tips_invalid_user_space)
                }

            }
        }

        val negativeBtn = dialogView.findViewById<TextView>(R.id.negative)
        negativeBtn.setText(R.string.cancel)
        negativeBtn.setOnClickListener {
            InputMethodUtils.hideKeyboard(context, mEditText)
            mDialog.dismiss()
        }
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mDialog.addContentView(dialogView, params)
        mDialog.setCancelable(false)
        mDialog.show()
    }

    fun showModifyUserLevelDialog(context: Context, devId: String, shareUser: ShareUser, mgrlevel: Int) {
        val title = when (mgrlevel) {
            0 -> R.string.transfer_ownership_of_this_device
            1 -> R.string.upgrade_to_administrator
            else -> R.string.Downgrad_to_a_common_user
        }
        DialogUtils.showWarningDialog(context,
                title,
                -1,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
            dialog.dismiss()
            if (isPositiveBtn) {
                modifyUserLevel(devId, shareUser, mgrlevel)
            }
        }
    }

    fun modifyUserLevel(devId: String, shareUser: ShareUser, mgrlevel: Int) {
        _liveDataLoading.postValue(Resource.loading())
        val loader = GradeBindDeviceHttpLoader(GsonBaseProtocol::class.java)
        loader.setParams(shareUser.userid, devId, mgrlevel)
        loader.setHttpLoaderStateListener(object : HttpLoader.HttpLoaderStateListener {
            override fun onLoadError() {}
            override fun onLoadStart(disposable: Disposable) {
                addDisposable(disposable)
            }

            override fun onLoadComplete() {
                _liveDataLoading.postValue(Resource.success(getString(R.string.modify_succeed)))
            }
        })
        loader.executor(object : MyOkHttpListener<GsonBaseProtocol>() {
            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                shareUser.mgrlevel = mgrlevel
                setShareUser(shareUser)
                if (mgrlevel == 0) {
                    DevManager.getInstance().initHardWareList(null)
                }
                _liveDataChangeMgrLevel.postValue(Resource.success((Pair(shareUser,mgrlevel))))
            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                super.error(tag, baseProtocol)
                _liveDataLoading.postValue(Resource.error(baseProtocol.errmsg, SdvnHttpErrorNo.ec2String(baseProtocol.result)))
            }
        })

    }

    fun getUserInfo(devId: String, username: String) {
        V5RepositoryWrapper.getUserInfo(devId, username, object : V5Observer<User>(devId) {
            override fun success(result: BaseProtocol<User>) {
                val data = result.data
                val oldOneOSUser = _liveDataUser.value
                if (oldOneOSUser != null) {
                    setUser(oldOneOSUser.apply {
                        markName = data?.mark
                        permissions = data?.permissions
                        name = data?.username
                    })
                } else {
                    if (data != null) {
                        setUser(OneOSUser(data.username, data.uid, data.gid, data.admin, data.mark).apply {
                            permissions = data.permissions
                        })
                    }
                }
            }

            override fun fail(result: BaseProtocol<User>) {
            }

            override fun isNotV5() {
            }
        })
    }

    fun getUserSpace(devId: String, username: String) {
        V5RepositoryWrapper.getUserSpace(devId, username, object : V5Observer<UserSpace>(devId) {
            override fun success(result: BaseProtocol<UserSpace>) {
                val data = result.data
                val l = data?.space ?: 0
                val function = { total: Long ->
                    val oldOneOSUser = _liveDataUser.value
                    if (oldOneOSUser != null) {
                        setUser(oldOneOSUser.apply {
                            space = if (l > 0) {
                                l * 1024 * 1024 * 1024
                            } else {
                                total
                            }
                            used = data?.used ?: 0
                        })
                    }
                }
                if (l > 0) {
                    function(0)
                } else {
                    queryDeviceTotalSpace(devId, Callback {
                        function(it)
                    })
                }
            }

            override fun fail(result: BaseProtocol<UserSpace>) {
            }

            override fun isNotV5() {
            }
        })
    }

    fun queryDeviceTotalSpace(devId: String, callback: Callback<Long>) {
        val observer = object : V5Observer<Any>(devId ?: "") {
            override fun success(result: BaseProtocol<Any>) {
                val hd1 = OneOSHardDisk()
                val hd2 = OneOSHardDisk()
                OneOSSpaceAPI.getHDInfo(Gson().toJson(result.data), hd1, hd2)
                total = hd1.total
            }

            override fun fail(result: BaseProtocol<Any>) {

            }

            override fun isNotV5() {//调用旧接口
            }
        }
        V5RepositoryWrapper.getHDSmartInforSystem(devId, observer)
    }

}