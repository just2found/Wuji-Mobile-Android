package net.linkmate.app.ui.nas.user

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.arch.core.util.Function
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.SwipeItemLayout
import io.reactivex.disposables.Disposable
import io.weline.repo.SessionCache
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.UserSpace
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import kotlinx.android.synthetic.main.fragment_user_manage.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_rv.*
import libs.source.common.utils.EmptyUtils
import libs.source.common.utils.InputMethodUtils
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceControlRepository
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.DataItemLayout
import net.linkmate.app.view.HintEditDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.GradeBindDeviceHttpLoader
import net.sdvn.common.internet.loader.UnbindDeviceHttpLoader
import net.sdvn.common.internet.protocol.SharedUserList
import net.sdvn.common.internet.protocol.UnbindDeviceResult
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSUser
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI
import net.sdvn.nascommon.model.oneos.user.LoginManage
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.viewmodel.UserModel
import org.view.libwidget.handler.DelayedUnit
import org.view.libwidget.handler.DelayedUtils
import org.view.libwidget.handler.DelayedUtils.DELAY_USER_MANAGE_LIST
import org.view.libwidget.setOnRefreshWithTimeoutListener

/** 

Created by admin on 2020/7/23,09:14

 */
class UserManageFragment : TipsBaseFragment() {
    private val usersViewModel by viewModels<UsersViewModel>({ requireActivity() })
    private val deviceViewModel by viewModels<DeviceViewModel>()
    override fun getLayoutResId(): Int {
        return R.layout.fragment_user_manage
    }

    private var isM8: Boolean = false

    private val mUserList = ArrayList<OneOSUser>()
    private var serverUserList: SharedUserList? = null
    private val mUserModel by viewModels<UserModel>()
    private var isOwner: Boolean = false
    private var isAdmin: Boolean = false

    private var mFooterView: View? = null
    private var mHeaderView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (TextUtils.isEmpty(devId)) {
            devId = LoginManage.getInstance().loginSession!!.id
        }
        if (devId.isNullOrEmpty()) {
            ToastHelper.showLongToast(R.string.tip_wait_for_service_connect)
            finish()
            return
        }
        val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
        if (deviceModel != null) {
            isOwner = deviceModel.isOwner
            isAdmin = deviceModel.isAdmin
            isM8 = UiUtils.isM8(deviceModel.devClass)
        }

        deviceViewModel.liveDevices.observe(this, Observer {
            val deviceModel2 = SessionManager.getInstance().getDeviceModel(devId)
            if (deviceModel2 != null) {
                isOwner = deviceModel2.isOwner
                isAdmin = deviceModel2.isAdmin
                isM8 = UiUtils.isM8(deviceModel2.devClass)
                if (deviceModel2.isOnline) {
                    getUserList()
                }
            }
        })
        mUserModel.mUserList.observe(this, Observer<List<OneOSUser>?> { oneOSUsers ->
            mUserList.clear()
            if (oneOSUsers != null) {
                mUserList.addAll(oneOSUsers)
            }
        })


//        mUserModel.mUserEntity.observe(this, Observer {
//            if (mFooterView != null) {
//                val viewInfo = mFooterView!!.findViewById<TextView>(R.id.footer_text_info)
//                viewInfo.text = resources.getQuantityString(R.plurals.users, mUserList.size, mUserList.size.toString())
//                viewInfo.visibility = if (mUserList.size > 0) View.VISIBLE else View.GONE
//            }
//            if (swipe_refresh_layout != null) {
//             //   swipe_refresh_layout!!.isRefreshing = false
//            }
//        })

        mUserModel.mServerUserList.observe(this, Observer {
            serverUserList = it
            simpleUserAdapter.replaceData(it?.users ?: listOf())
            mHeaderView?.findViewById<TextView>(R.id.tv_user_header)?.text = "$usersStr ${it?.users?.size} $usersLimitStr ${it.maxlimit}"
            if (swipe_refresh_layout != null) {
                swipe_refresh_layout!!.isRefreshing = false
            }
        })
    }

    private val usersStr: String by lazy {
        resources.getString(R.string.tips_user_numbers)
    }
    private val usersLimitStr: String by lazy {
        resources.getString(R.string.tips_user_max_limit)
    }

    private val simpleUserAdapter: SimpleUserAdapter by lazy {
        SimpleUserAdapter().apply {
            setOnItemClickListener { adapter, view, position ->
                if (Utils.isFastClick(view)) return@setOnItemClickListener
                val item = adapter.getItem(position)
                if (item is ShareUser) {
                    val shareUser = item
                    mUserModel.getOneOsUser(shareUser)?.let { oneOSUser ->
                        if (devId?.let { it1 -> SessionCache.instance.isNasV3(it1) } == true &&
                           //     oneOSUser.type and OneOSUser.TYPE_REMOTE != 0 &&
                                oneOSUser.type and OneOSUser.TYPE_LOCAL != 0 &&
                                isOwner) {
                            usersViewModel.isAdmin = isAdmin
                            usersViewModel.isOwner = isOwner
                            usersViewModel.total = mUserModel.total
                            val oldOneOSUser = usersViewModel.liveDataUser.value
                            if (oldOneOSUser != null) {
                                usersViewModel.setUser(oldOneOSUser.apply {
                                    total = oneOSUser.total
                                    type=oneOSUser.type
                                    space = oneOSUser.space
                                    markName = oneOSUser.markName
                                    used = oneOSUser.used
                                    permissions = oneOSUser.permissions
                                    name = oneOSUser.name
                                })
                            } else {
                                usersViewModel.setUser(oneOSUser)
                            }
                            usersViewModel.setShareUser(shareUser)
                            val findNavController = findNavController()
                            if (findNavController.currentDestination?.id == R.id.userManageFragment) {
                                findNavController.navigate(R.id.action_userManageFragment_to_userSettingFragment, devId?.let { it1 -> UserSettingFragmentArgs(it1).toBundle() })
                            }
                            return@setOnItemClickListener
                        }
                        showEditDialog(oneOSUser, adapter, position)
                    } ?: kotlin.run {

                    }
                }
            }
        }
    }

    private fun finish() {
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        if (mUserList.size == 0 && swipe_refresh_layout != null) {
            swipe_refresh_layout.isRefreshing = true
        }
        getUserList()
    }



    private fun getUserList() {
        DelayedUtils.addDelayedUnit(DELAY_USER_MANAGE_LIST, DelayedUnit(mReCountRunnable, delayTime = 1000, maxIntervalTime = 2000))
    }

    private val mReCountRunnable = Runnable {
        getUserList1()
    }

    private fun getUserList1() {
          SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {
            override fun onSuccess(url: String?, data: LoginSession) {
                lifecycleScope.launchWhenResumed {
                    mUserModel.getUserList(requireActivity() as BaseActivity, devId!!, data)
                    if (mUserList.size == 0 && swipe_refresh_layout != null) {
                        swipe_refresh_layout.isRefreshing = true
                    }
                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                swipe_refresh_layout?.isRefreshing = false
            }
        })
    }


    override fun getTopView(): View? {
        return layout_title
    }

    override fun initView(view: View) {
        layout_title!!.setOnBackClickListener {
            if (Utils.isFastClick(it)) {
                return@setOnBackClickListener
            }
            finish()
        }
        layout_title!!.setBackTitle(R.string.title_user_management)
        if (isOwner) {
            layout_title!!.setRightText(R.string.btn_add_user)
            layout_title!!.setRightTextVisible(View.GONE)
            layout_title!!.setOnRightTextClickListener {
                //showAddUserDialog();
                gotoContacts()
            }
        } else {
            layout_title!!.setRightTextVisible(View.GONE)
        }
        mTipsBar = tipsBar
        swipe_refresh_layout?.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener { getUserList() })

        val recyclerView = recycle_view
        recyclerView.background = ColorDrawable(resources.getColor(R.color.color_bg_grey150))
        recyclerView!!.addOnItemTouchListener(SwipeItemLayout.OnSwipeItemTouchListener(recyclerView!!.context))
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = simpleUserAdapter
    }

//    private val mAdapter by lazy {
//        QuickUserAdapter(null, isOwner).apply {
//            onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
//                if (Utils.isFastClick(view)) return@OnItemClickListener
//                val oneOSUser = adapter.getItem(position)
//                oneOSUser?.let {
//                    val sectionEntity = it as SectionEntity<OneOSUser>
//                    sectionEntity.t?.let { oneOSUser ->
//                        if (BuildConfig.DEBUG && SessionCache.instance.isNasV3(devId!!)) {
//                            usersViewModel.isAdmin = isAdmin
//                            usersViewModel.isOwner = isOwner
//                            usersViewModel.total = mUserModel.total
//                            usersViewModel.setUser(oneOSUser)
//                            findNavController().navigate(R.id.action_userManageFragment_to_userSettingFragment, devId?.let { it1 -> UserSettingFragmentArgs(it1).toBundle() })
//                            return@OnItemClickListener
//                        }
//                        showEditDialog(oneOSUser, adapter, position)
//                    }
//                }
//            }
//            val inflate = LayoutInflater.from(requireContext()).inflate(R.layout.layout_footer_view_single_line, null)
//            mFooterView = inflate
//            addFooterView(inflate)
//        }
//    }

    private fun showEditDialog(oneOSUser: OneOSUser, adapter: BaseQuickAdapter<Any, BaseViewHolder>, position: Int) {
        val userLiveData = MutableLiveData(oneOSUser)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog_nas_user_manage, null)
        val dataItemAccount = view.findViewById<DataItemLayout>(R.id.nmg_des_dil_account)
        val dataItemName = view.findViewById<DataItemLayout>(R.id.nmg_des_dil_name)
        val dataItemSpace = view.findViewById<DataItemLayout>(R.id.nmg_des_dil_space)
        val tvTransOwner = view.findViewById<View>(R.id.nmg_des_btn_transfer_owner)
        val tvMdfLevel = view.findViewById<TextView>(R.id.nmg_des_btn_up)
        val tvDelete = view.findViewById<View>(R.id.nmg_des_btn_delete)
        val ivBack = view.findViewById<View>(R.id.iv_back)
//        dataItemSpace.isVisible = !isM8
        userLiveData.observe(this, Observer {
            dataItemAccount.setText(oneOSUser.name)
            dataItemName.setText(oneOSUser.markName)
            val space = oneOSUser.space / 1024 / 1024 / 1024
            dataItemSpace.setText("$space GB")
            adapter.notifyItemChanged(position)
        })
        tvDelete.isVisible = oneOSUser.name != AppConstants.DEFAULT_USERNAME_ADMIN
        val dialog = Dialog(requireContext(), R.style.DialogTheme)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.setOnDismissListener { adapter.notifyItemChanged(position) }
        dialog.show()

        if (isOwner
                && CMAPI.getInstance().baseInfo.userId != mUserModel.getServerUserId(oneOSUser.name)
                && oneOSUser.isRemote) {
            //如果是所有者，并且选的不是自己
            tvMdfLevel.visibility = View.VISIBLE
            if (oneOSUser.isAdmin == 1) {
                tvMdfLevel.text = getString(R.string.Downgrad_to_a_common_user)
            }
            tvMdfLevel.setOnClickListener {
                if (oneOSUser.isAdmin == 1) {
                    showModifyUserLevelDialog(oneOSUser, 2)
                } else {
                    showModifyUserLevelDialog(oneOSUser, 1)
                }
                dialog.dismiss()
            }
            tvTransOwner.visibility = View.VISIBLE
            tvTransOwner.setOnClickListener {
                showModifyUserLevelDialog(oneOSUser, 0)
                dialog.dismiss()
            }
        }
        ivBack.setOnClickListener { v -> dialog.dismiss() }
        if (oneOSUser.type and OneOSUser.TYPE_LOCAL != 0) {
            dataItemName.mIv.visibility = View.VISIBLE
            dataItemSpace.mIv.visibility = View.VISIBLE
            dataItemSpace.setOnClickListener { changeSpace(userLiveData) }
            dataItemName.setOnClickListener { editMarkName(userLiveData, oneOSUser.name ?: "") }
        } else {
            dataItemSpace.mIv.visibility = View.GONE
            dataItemName.mIv.visibility = View.GONE
        }

        tvDelete.setOnClickListener {
            deleteUser(oneOSUser, oneOSUser.name)
            dialog.dismiss()
        }
    }

    private fun showModifyUserLevelDialog(oneOSUser: OneOSUser, mgrlevel: Int) {
        val title = when (mgrlevel) {
            0 -> R.string.transfer_ownership_of_this_device
            1 -> R.string.upgrade_to_administrator
            else -> R.string.Downgrad_to_a_common_user
        }
        DialogUtils.showWarningDialog(requireContext(),
                title,
                -1,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
            dialog.dismiss()
            if (isPositiveBtn) {
                modifyUserLevel(oneOSUser, mgrlevel)
            }
        }
    }

    private fun modifyUserLevel(oneOSUser: OneOSUser, mgrlevel: Int) {
        showLoading()
        val loader = GradeBindDeviceHttpLoader(GsonBaseProtocol::class.java)
        loader.setParams(mUserModel.getServerUserId(oneOSUser.name), devId, mgrlevel)
        loader.setHttpLoaderStateListener(object : HttpLoader.HttpLoaderStateListener {
            override fun onLoadError() {}
            override fun onLoadStart(disposable: Disposable?) {}
            override fun onLoadComplete() {
                dismissLoading()
                swipe_refresh_layout?.isRefreshing = true
                getUserList()
            }
        })
        loader.executor(object : MyOkHttpListener<GsonBaseProtocol>() {
            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                ToastHelper.showToast(R.string.success)
                if (mgrlevel == 0) {
                    finish()
                    DevManager.getInstance().initHardWareList(null)//用户管理 更改owner
                }
            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                super.error(tag, baseProtocol)
                dismissLoading()
            }
        })

    }

    fun editMarkName(user: MutableLiveData<OneOSUser>, name: String) {
        val markName = user.value?.markName ?: ""
        DialogUtils.showEditDialog(requireContext(), R.string.tip_set_user_name, R.string.hint_set_user_name, markName,
                net.sdvn.nascommonlib.R.string.max_name_length,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn, mContentEditText ->
            val context = mContentEditText.context
            if (isPositiveBtn) {
                val newName = mContentEditText.text.toString().trim { it <= ' ' }
                if (EmptyUtils.isEmpty(newName) || newName.length > 16) {
                    AnimUtils.sharkEditText(context, mContentEditText)
                } else {
                    changUserMarkName(user, newName, name)
                    InputMethodUtils.hideKeyboard(context, mContentEditText)
                    dialog.dismiss()
                }
            } else {
                InputMethodUtils.hideKeyboard(context, mContentEditText)
                dialog.dismiss()
            }
        }
    }

    private fun deleteUser(user: OneOSUser, name: String?) {
        val curUid = CMAPI.getInstance().baseInfo.userId
        val delUid = mUserModel.getServerUserId(name)
        if (isOwner) {
            if (curUid == delUid) { //所有者删除自己，要先转移所有者权限
                showOwnerDeleteSelfDialog(user)
            } else { //所有者删除其他用户
                showDeleteUserDialog(user)
            }
        } else if (curUid == delUid) { //用户删除自己账号
            if (isAdmin) {//管理员删除自己
                showManangerDeleteSelfDialog(user)
            } else {
                showDeleteUserDialog(user)
            }
        } else if (isAdmin && user.isAdmin != 0 && user.isAdmin != 1) { //管理员删除其他用户
            showDeleteUserDialog(user)
        } else { //非所有者用户删除其他普通用户
            DialogUtils.showNotifyDialog(requireContext(), R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null)
        }
    }

    private fun resetNasLocalPasswd(user: OneOSUser) {
        SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {

            override fun onSuccess(url: String?, mLoginSession: LoginSession) {
                if (user.name == CMAPI.getInstance().baseInfo.account) {
                    showModifyPwdDialog(user)
                } else if (mLoginSession.isAdmin) {
                    showResetPwdDialog(user)
                } else {
                    DialogUtils.showNotifyDialog(requireContext(), R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null)
                }
            }


        })
    }

    private fun changeSpace(user: MutableLiveData<OneOSUser>) {
        if (isAdmin || isOwner) {
            showChangedSpaceDialog(user)
        } else {
            DialogUtils.showNotifyDialog(requireContext(), R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null)
        }
    }


    private fun changUserMarkName(user: MutableLiveData<OneOSUser>, newName: String, username: String) {
        SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {
            override fun onSuccess(url: String?, data: LoginSession) {
                val listener = object : OneOSUserManageAPI.OnUserManageListener {
                    override fun onStart(url: String) {

                    }

                    override fun onSuccess(url: String, cmd: String) {
                        user.value?.markName = newName
                        usersViewModel.liveDataShareUser.value?.devMarkName = newName
                        user.postValue(user.value)
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

    private fun showResetPwdDialog(user: OneOSUser) {
        DialogUtils.showWarningDialog(requireContext(), R.string.reset_user_pwd, R.string.tips_reset_user_pwd, R.string.reset_now, R.string.cancel
        ) { dialog, isPositiveBtn ->
            dialog.dismiss()
            if (isPositiveBtn) {
                modifyPwd(user, "123456")
            }
        }
    }

    private fun showModifyPwdDialog(user: OneOSUser) {
        DialogUtils.showEditPwdDialog(requireContext(), R.string.modify_user_pwd, R.string.warning_modify_user_pwd, 0, R.string.enter_new_pwd, R.string.confirm_new_pwd,
                R.string.modify, R.string.cancel) { _, isPositiveBtn, mEditText, mOld ->
            if (isPositiveBtn) {
                val newPwd = mEditText.text.toString().trim { it <= ' ' }
                modifyPwd(user, newPwd)
            }
        }
    }

    private fun modifyPwd(user: OneOSUser, newPwd: String) {
        SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {

            override fun onSuccess(url: String?, mLoginSession: LoginSession) {
                val listener = object : OneOSUserManageAPI.OnUserManageListener {
                    override fun onStart(url: String) {
//                        showLoading()
                    }

                    override fun onSuccess(url: String, cmd: String) {
                        val intent = Intent(AppConstants.LOCAL_BROADCAST_RELOGIN)
                        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, mLoginSession.id)
                        LocalBroadcastManager.getInstance(requireContext().applicationContext).sendBroadcast(intent)
                        ToastHelper.showLongToastSafe(R.string.modify_succeed)
                        dismissLoading()
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        ToastHelper.showLongToastSafe(HttpErrorNo.getResultMsg(false, errorNo, errorMsg))
                        dismissLoading()
                    }
                }

                val observer = object : V5Observer<Any>(mLoginSession.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        listener.onSuccess("", "")
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {
                        val oneOSUserManageAPI = OneOSUserManageAPI(mLoginSession)
                        oneOSUserManageAPI.setOnUserManageListener(listener)
                        oneOSUserManageAPI.chpwd(user.name, newPwd)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().updateUserPassword(mLoginSession.id
                                ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), user.name
                                ?: "", newPwd, this)
                        return true
                    }
                }
                V5Repository.INSTANCE().updateUserPassword(mLoginSession.id
                        ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), user.name
                        ?: "", newPwd, observer)


            }
        })

    }

    private fun showChangedSpaceDialog(user: MutableLiveData<OneOSUser>) {

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_space, null)
        val mDialog = Dialog(requireContext(), R.style.DialogTheme)
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
        val total = mUserModel!!.total / 1024 / 1024 / 1024
        val userSpace = user.value?.space ?: 0

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
                if (!TextUtils.isEmpty(s)) {
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
                AnimUtils.sharkEditText(requireContext(), mEditText)
            }
        })

        switchUnlimited.setOnCheckedChangeListener { _, isChecked ->
            mEditText.isEnabled = !isChecked
            tvHint.isEnabled = isChecked
            mEditText.setText("")
        }
        InputMethodUtils.showKeyboard(requireContext(), mEditText, 200)

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
                AnimUtils.sharkEditText(requireContext(), mEditText)
            } else {
                try {
                    SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {

                        override fun onSuccess(url: String, mLoginSession: LoginSession) {

                            val listener = object : OneOSUserManageAPI.OnUserManageListener {
                                override fun onStart(url: String) {
//                                    showLoading(R.string.modifying)
                                }

                                override fun onSuccess(url: String, cmd: String) {
                                    ToastHelper.showLongToastSafe(R.string.modify_succeed)
                                    user.value?.space = (if (spaceLong > 0L) spaceLong else total) * 1024 * 1024 * 1024
                                    user.postValue(user.value)
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
                                    manageAPI.chspace(user.value?.name, spaceLong)
                                }

                                override fun retry(): Boolean {
                                    V5Repository.INSTANCE().setUserSpace(mLoginSession.id
                                            ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), user.value?.name
                                            ?: "", spaceLong, this)
                                    return true
                                }
                            }
                            V5Repository.INSTANCE().setUserSpace(mLoginSession.id
                                    ?: "", mLoginSession.ip, LoginTokenUtil.getToken(), user.value?.name
                                    ?: "", spaceLong, observer)

                            InputMethodUtils.hideKeyboard(requireContext(), mEditText)
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
            InputMethodUtils.hideKeyboard(requireContext(), mEditText)
            mDialog.dismiss()
        }
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mDialog.addContentView(dialogView, params)
        mDialog.setCancelable(false)
        mDialog.show()
    }

    /**
     * 所有者删除自己提示框
     */
    private fun showOwnerDeleteSelfDialog(delUser: OneOSUser) {
        val delUserID = mUserModel.getServerUserId(currentOptUser?.name)
        currentOptUser = delUser
        if (hintEditDialog?.dialog?.isShowing != true) {
            //本地用户  勾选清除用户数据，且不可取消
            var checkboxEnable: Boolean = if (!delUserID.isNullOrEmpty()) true else false
            val device = DevManager.getInstance().deviceBeans.find {
                it.id == devId
            }
            val isNas = device?.isNas ?: false
            //2021/3/25:“同时清除用户数据”的勾选框在非NAS设备隐藏
            hintEditDialog?.update(
                    title = requireContext().getString(R.string.remove_device_title),
                    content = requireContext().getString(R.string.manager_remove_device_content),
                    contentTextColor = R.color.red,
                    editHint = requireContext().getString(R.string.manager_remove_device_hint),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText = if (isNas) requireContext().getString(R.string.clear_user_data_hint) else null,
                    isCheckedBox = isNas,
                    checkboxEnable = checkboxEnable,
                    confrimText = getString(R.string.confirm),
                    cancelText = getString(R.string.cancel)
            )
            hintEditDialog?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (view?.id == R.id.positive) {
                        val isCheckedBox = hintEditDialog?.isCheckedBox() ?: false
                        val repository = DeviceControlRepository()
                        if (isCheckedBox) {//勾选了清除全部用户数据
                            showLoading()
                            //清除所有用户数据
                            repository.clearNasUser(devId!!, Function {
                                //解绑所有人
                                repository.clearDeviceUser(devId!!, null, Function {
                                    if (it) {
                                        sendRemoveDevBroadcast(devId)
                                        finish()
                                    }
                                    null
                                })
                                null
                            }, Function {
                                ToastUtils.showToast(R.string.clear_user_data_failed_hint)
//                                ToastHelper.showToast(it)
                                dismissLoading()
                                null
                            })
                        } else {
                            //解绑所有人
                            repository.clearDeviceUser(devId!!, null, Function {
                                if (it) {
                                    sendRemoveDevBroadcast(devId)
                                    finish()
                                }
                                null
                            })
                        }
                    }
                }

            })

            hintEditDialog?.show(requireActivity().getSupportFragmentManager(), "hintEditDialog")
        }
    }

    /**
     * 管理员删除自己提示框
     */
    private fun showManangerDeleteSelfDialog(delUser: OneOSUser) {
        currentOptUser = delUser
        val delUserID = mUserModel.getServerUserId(currentOptUser?.name)
        if (hintEditDialog?.dialog?.isShowing != true) {
            //本地用户  勾选清除用户数据，且不可取消
            var checkboxEnable: Boolean = if (!delUserID.isNullOrEmpty()) true else false
            val device = DevManager.getInstance().deviceBeans.find {
                it.id == devId
            }
            val isNas = device?.isNas ?: false
            hintEditDialog?.update(
                    title = requireContext().getString(R.string.remove_device_title),
                    content = requireContext().getString(R.string.manager_remove_device_content),
                    contentTextColor = R.color.red,
                    editHint = requireContext().getString(R.string.manager_remove_device_hint),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText = if (isNas) requireContext().getString(R.string.clear_user_data_hint) else null,
                    isCheckedBox = isNas,
                    checkboxEnable = checkboxEnable,
                    confrimText = getString(R.string.confirm),
                    cancelText = getString(R.string.cancel)
            )
            hintEditDialog?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (view?.id == R.id.positive) {
                        val isCheckedBox = hintEditDialog?.isCheckedBox() ?: false
                        if (isCheckedBox) {//勾选了清除数据
                            if (currentOptUser?.type!! and OneOSUser.TYPE_LOCAL != 0) {
                                deleteUserInDevice(delUserID, currentOptUser!!)
                            } else {
                                if (!delUserID.isNullOrEmpty()) {
                                    unbind(delUserID)
                                }
                            }
                        } else {
                            if (!delUserID.isNullOrEmpty()) {
                                unbind(delUserID)
                            }
                        }
                    }
                }

            })
            hintEditDialog?.show(requireActivity().getSupportFragmentManager(), "hintEditDialog")
        }
    }

    fun sendRemoveDevBroadcast(devId: String?) {
        devId?.let {
            usersViewModel.sendRemoveDevBroadcast(MyApplication.getContext(), it)
        }
    }

    /**
     * 非管理员／所有者删除自己提示框
     */
    private fun showDeleteUserDialog(delUser: OneOSUser) {
        currentOptUser = delUser
        val delUserID = getServerUserId(currentOptUser?.name)
        if (hintEditDialog?.dialog?.isShowing != true) {
            //本地用户  勾选清除用户数据，且不可取消
            var checkboxEnable: Boolean = if (!delUserID.isNullOrEmpty()) true else false
            val device = DevManager.getInstance().deviceBeans.find {
                it.id == devId
            }
            val isNas = device?.isNas ?: false
            hintEditDialog?.update(
                    title = requireContext().getString(R.string.delete_user),
                    content = requireContext().getString(R.string.delete_user_hint),
                    contentTextColor = R.color.red,
                    editHint = requireContext().getString(R.string.confirm_delete),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText = if (isNas) requireContext().getString(R.string.clear_user_data_hint) else null,
                    isCheckedBox = isNas,
                    checkboxEnable = checkboxEnable,
                    confrimText = getString(R.string.confirm),
                    cancelText = getString(R.string.cancel)
            )
            hintEditDialog?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (view?.id == R.id.positive) {
                        val isCheckedBox = hintEditDialog?.isCheckedBox() ?: false
                        if (isCheckedBox) {//勾选了清除数据
                            if (currentOptUser?.type!! and OneOSUser.TYPE_LOCAL != 0) {
                                deleteUserInDevice(delUserID, currentOptUser!!)
                            } else {
                                if (!delUserID.isNullOrEmpty() && delUser.isRemote  ) {
                                    unbind(delUserID)
                                }
                            }
                        } else {
                            if (!delUserID.isNullOrEmpty()&& delUser.isRemote) {
                                unbind(delUserID)
                            }
                        }
                    }
                }

            })
            hintEditDialog?.show(requireActivity().getSupportFragmentManager(), "hintEditDialog")
        }
    }


    //当前操作的用户
    private var currentOptUser: OneOSUser? = null
    private var hintEditDialog: HintEditDialog? = null
        get() {
            if (field == null) {
                field = HintEditDialog.newInstance()
            }
            return field
        }


    private fun deleteUserInDevice(delUid: String?, delUser: OneOSUser) {
        SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {
            override fun onSuccess(url: String, mLoginSession: LoginSession) {
                //删除用户

                val listener = object : OneOSUserManageAPI.OnUserManageListener {
                    override fun onStart(url: String) {
                        showLoading(R.string.delete_user)
                    }

                    override fun onSuccess(url: String, cmd: String) {
                        if (!delUid.isNullOrEmpty() && delUser.isRemote) {
                            unbind(delUid)
                        } else {
                            //需刷新，否则有历史文件会保留
                            DevManager.getInstance().initHardWareList(null)
                            mUserModel?.removeByName(delUser.name)
                            ToastHelper.showLongToastSafe(R.string.delete_user_succeed)
                            swipe_refresh_layout?.isRefreshing = true
                            getUserList()
                            try {
                                dismissLoading()
                                if (hintEditDialog?.dialog?.isShowing == true) hintEditDialog?.dismiss()
                            } catch (e: java.lang.Exception) {
                            }
                        }
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        val errorMsgVar = HttpErrorNo.getResultMsg(errorNo, errorMsg)
                        ToastHelper.showToast(errorMsgVar)
                        dismissLoading()
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
                        V5Repository.INSTANCE().deleteUser(devId!!, mLoginSession.ip, LoginTokenUtil.getToken(), delUser.name
                                ?: "", this)
                        return true
                    }
                }
                V5Repository.INSTANCE().deleteUser(devId!!, mLoginSession.ip, LoginTokenUtil.getToken(), delUser.name
                        ?: "", observer)
            }
        })

    }

    private fun unbind(userId: String) {
        showLoading()
        //用户解绑
        val unbindDeviceHttpLoader = UnbindDeviceHttpLoader()

        unbindDeviceHttpLoader.unbindSingle(devId, userId, object : ResultListener<UnbindDeviceResult> {
            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                dismissLoading()
                ToastHelper.showToast(R.string.remove_device_failed)
                showTipView(baseProtocol.errmsg, false)
            }

            override fun success(tag: Any?, mGsonBaseProtocol: UnbindDeviceResult) {
                //需刷新，否则有历史文件会保留
                DevManager.getInstance().initHardWareList(null)
                try {
                    dismissLoading()
                    if (hintEditDialog?.dialog?.isShowing == true) hintEditDialog?.dismiss()
                } catch (e: java.lang.Exception) {
                }

                val curUid = CMAPI.getInstance().baseInfo.userId
                ToastHelper.showToast(R.string.remove_success)
                if (curUid == userId) {
                    sendRemoveDevBroadcast(devId)
                    finish()
                } else {
                    mUserModel?.removeByName(userId)
                    swipe_refresh_layout?.isRefreshing = true
                    getUserList()
                }
            }
        })
    }

    private fun showTipView(errmsg: String?, b: Boolean) {
        if (errmsg != null) {
            (requireActivity() as BaseActivity).showTipView(errmsg, b)
        }
    }

    private fun dismissLoading() {
        (requireActivity() as BaseActivity).dismissLoading()
    }

    private fun showLoading(resStrId: Int = 0) {
        if (resStrId > 0) {
            (requireActivity() as BaseActivity).showLoading(resStrId)
        } else {
            (requireActivity() as BaseActivity).showLoading()
        }
    }

    private fun getServerUserId(username: String?): String? {
        return mUserModel.getServerUserId(username)
    }

    private fun gotoContacts() {}

    companion object {
        fun newInstance(devId: String?): UserManageFragment {
            val args = Bundle()
            if (!devId.isNullOrEmpty()) {
                args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
            }
            val fragment = UserManageFragment()
            fragment.arguments = args
            return fragment
        }
    }
}