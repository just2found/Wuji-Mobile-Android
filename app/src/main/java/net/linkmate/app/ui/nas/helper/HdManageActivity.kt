package net.linkmate.app.ui.nas.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.rxjava.rxlife.RxLife
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.devhelper.DevTypeHelper
import io.weline.repo.api.V5_ERR_DISK_NOT_MOUNTED
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import kotlinx.android.synthetic.main.activity_hd_manage.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.ui.viewmodel.HDManageViewModel
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Device
import net.sdvn.cmapi.protocal.EventObserver
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.api.sys.OneOSHDFormatAPI
import net.sdvn.nascommon.model.oneos.api.sys.OneOSPowerAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSLoginAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.NasLanAccessViewModel
import net.sdvn.nascommon.widget.TitleBackLayout
import timber.log.Timber

class HdManageActivity : BaseActivity(), View.OnClickListener {
    private val nasLanAccessViewModel by viewModels<NasLanAccessViewModel>()
    private var isM8: Boolean = false
    private var oneOSHDFormatAPI: OneOSHDFormatAPI? = null
    private var countHD: String? = null
    private var cmd: OneOSHDFormatAPI.HD_FType? = null
    private var oneHDLayout: View? = null
    private var moreHDLayout: View? = null
    private var formatLayout: View? = null
    private var raid1Layout: View? = null
    private var raid0Layout: View? = null
    private var lvmLyout: View? = null
    private var checkRaid1View: ImageView? = null
    private var checkRaid0View: ImageView? = null
    private var checkLvmView: ImageView? = null

    //    private String SN;
    private var mEventObserver: EventObserver? = null
    private var isReboot: Boolean = false
    private var savedDeviceId: String? = null
    private var isSuccessFormat: Boolean = false
    private val hdManageViewModel: HDManageViewModel by viewModels()
//    override fun getLayoutId(): Int {
//        return R.layout.activity_hd_manage
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hd_manage)
        initViews()
        mEventObserver = object : EventObserver() {
            override fun onDeviceStatusChange(device: Device) {
                if (device.id == savedDeviceId && !device.isOnline) {
                    isReboot = false
                    ToastHelper.showToast(R.string.success_reboot_device)
                    finish()
                }
            }
        }
        CMAPI.getInstance().subscribe(mEventObserver!!)
        initData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initData()
    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    override fun getTopView(): View? {
        return layout_title
    }
    private fun initViews() {
        val mTitleLayout = findViewById<TitleBackLayout>(R.id.layout_title)
        mTitleLayout.setOnClickBack(this)
        mTitleLayout.setBackTitle(R.string.hd_one_title)

        oneHDLayout = findViewById(R.id.oneHD)
        moreHDLayout = findViewById(R.id.moreHD)
        formatLayout = findViewById(R.id.rlt_format)

        findViewById<View>(R.id.text_format).setOnClickListener(this)
    }

    private fun initData() {
        val intent = intent
        if (null != intent) {
            countHD = intent.getStringExtra("count")
            Logger.LOGD(TAG, "initData intent = $countHD")
            //            SN = intent.getStringExtra("sn");
            savedDeviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)

            val deviceModel = SessionManager.getInstance().getDeviceModel(deviceId)
            val tvTipsLvm = findViewById<View>(R.id.tips_lvm)
            tvTipsLvm.isVisible = true
            deviceModel?.devClass?.let {
                isM8 = UiUtils.isM8(it)
                if (isM8) {
                    Logger.LOGD(TAG, "initData isM8 = $countHD")
                    cmd = if (countHD == "1") {
                        oneHDLayout?.visibility = View.VISIBLE
                        moreHDLayout?.visibility = View.GONE
                        lvmHD?.visibility = View.GONE
                        OneOSHDFormatAPI.HD_FType.fixinternal
                    } else {
                        oneHDLayout?.visibility = View.GONE
                        moreHDLayout?.visibility = View.GONE
                        lvmHD?.visibility = View.VISIBLE
                        checkbox_lvm_add.isSelected = true
                        checkbox_lvm2.isSelected = false
                        lvm_add?.setOnClickListener {
                            checkbox_lvm_add.isSelected = true
                            checkbox_lvm2.isSelected = false
                            cmd = OneOSHDFormatAPI.HD_FType.extend
                        }
                        lvm2?.setOnClickListener {
                            checkbox_lvm_add.isSelected = false
                            checkbox_lvm2.isSelected = true
                            cmd = OneOSHDFormatAPI.HD_FType.entire
                        }
                        OneOSHDFormatAPI.HD_FType.extend
                    }
                } else {
                    Logger.LOGD(TAG, "initData else = $countHD")
                    m3AndM3PlusFormat()
                }
                nasLanAccessViewModel.getLoginSession(deviceId, object : GetSessionListener() {
                    override fun onSuccess(url: String?, loginSession: LoginSession) {
                        val formatAPI = OneOSHDFormatAPI(loginSession)
                        formatAPI.setOnFormatListener(object : OneOSHDFormatAPI.OnFormatListener {
                            override fun onStart(url: String?) {

                            }

                            override fun onSuccess(url: String?) {
                                doFormatSuccess(loginSession)
                            }

                            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                                dismissLoading()
                            }
                        })
                        formatAPI.queryFormatStatus(!isM8,true)
                        showLoading(R.string.loading_data,true)
                        oneOSHDFormatAPI = formatAPI
                    }
                })
            } ?: finish()
        }
    }

    private fun m3AndM3PlusFormat() {
        if (countHD == "1") {
            cmd = OneOSHDFormatAPI.HD_FType.BASIC
            oneHDLayout!!.visibility = View.VISIBLE
            moreHDLayout!!.visibility = View.GONE
        } else {
            cmd = OneOSHDFormatAPI.HD_FType.RAID1
            oneHDLayout!!.visibility = View.GONE
            moreHDLayout!!.visibility = View.VISIBLE

            raid0Layout = findViewById(R.id.radi0)
            raid1Layout = findViewById(R.id.raid1)
            lvmLyout = findViewById(R.id.lvm)
            checkRaid0View = findViewById(R.id.checkbox_raid0)
            checkRaid1View = findViewById(R.id.checkbox_raid1)
            checkLvmView = findViewById(R.id.checkbox_lvm)

            raid0Layout!!.setOnClickListener {
                cmd = OneOSHDFormatAPI.HD_FType.RAID0
                checkRaid0View!!.setImageResource(R.drawable.selector_check_box)
                checkRaid0View!!.isSelected = true
                checkRaid1View!!.setImageResource(R.drawable.selector_check_box)
                checkRaid1View!!.isSelected = false
                checkLvmView!!.setImageResource(R.drawable.selector_check_box)
                checkLvmView!!.isSelected = false
            }

            raid1Layout!!.setOnClickListener {
                cmd = OneOSHDFormatAPI.HD_FType.RAID1
                checkRaid0View!!.setImageResource(R.drawable.selector_check_box)
                checkRaid0View!!.isSelected = false
                checkRaid1View!!.setImageResource(R.drawable.selector_check_box)
                checkRaid1View!!.isSelected = true
                checkLvmView!!.setImageResource(R.drawable.selector_check_box)
                checkLvmView!!.isSelected = false
            }

            lvmLyout!!.setOnClickListener {
                cmd = OneOSHDFormatAPI.HD_FType.LVM
                checkRaid0View!!.setImageResource(R.drawable.selector_check_box)
                checkRaid0View!!.isSelected = false
                checkRaid1View!!.setImageResource(R.drawable.selector_check_box)
                checkRaid1View!!.isSelected = false
                checkLvmView!!.setImageResource(R.drawable.selector_check_box)
                checkLvmView!!.isSelected = true
            }
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()
        oneOSHDFormatAPI?.cancel()
        CMAPI.getInstance().unsubscribe(mEventObserver)
    }


    private fun showPowerDialog(isPowerOff: Boolean) {
        val contentRes = if (isPowerOff) R.string.confirm_power_off_device else R.string.confirm_reboot_device
        DialogUtils.showConfirmDialog(this@HdManageActivity, R.string.tips, contentRes, R.string.confirm, R.string.cancel) { _, isPositiveBtn ->
            if (isPositiveBtn) {
                doPowerOffOrRebootDevice(isPowerOff)
            }
        }
    }


    private fun doPowerOffOrRebootDevice(isPowerOff: Boolean) {
        //        String sn = SPUtils.getValue(HdManageActivity.this, "sn");
        SessionManager.getInstance().getLoginSession(savedDeviceId!!, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {

                val listener = object : OneOSPowerAPI.OnPowerListener {
                    override fun onStart(url: String) {

                    }

                    override fun onSuccess(url: String, isPowerOff: Boolean) {
                        isReboot = true
                        val resId = R.string.rebooting_device
                        showLoading(resId)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        ToastHelper.showLongToastSafe(HttpErrorNo.getResultMsg(errorNo, errorMsg))
                    }
                }

                val observer = object : V5Observer<Any>(deviceId) {
                    override fun success(result: BaseProtocol<Any>) {
                        listener.onSuccess("", isPowerOff)
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {
                        val powerAPI = OneOSPowerAPI(loginSession!!)
                        powerAPI.setOnPowerListener(listener)
                        powerAPI.power(isPowerOff)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().rebootOrHaltSystem(deviceId, loginSession?.ip
                                ?: "", LoginTokenUtil.getToken(), isPowerOff, this)
                        return true
                    }
                }

                V5Repository.INSTANCE().rebootOrHaltSystem(deviceId, loginSession?.ip
                        ?: "", LoginTokenUtil.getToken(), isPowerOff, observer)


            }

        })
    }

    override fun finish() {
        super.finish()
        val result = if (isSuccessFormat) Activity.RESULT_OK else Activity.RESULT_CANCELED
        val intent = Intent()
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, savedDeviceId)
        this@HdManageActivity.setResult(result, intent)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.text_format -> DialogUtils.showWarningDialog(this, R.string.hd_format, R.string.tip_format_disk,
                    R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
                if (isPositiveBtn) {
                    showLoading(R.string.formating)
                    nasLanAccessViewModel.getLoginSession(savedDeviceId!!,object : GetSessionListener() {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            doFormat(loginSession)
                        }

                        override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                            val deviceModel = SessionManager.getInstance().getDeviceModel(savedDeviceId!!)
                            if (deviceModel?.isOwner == true
                                && deviceModel.isOnline
                                && errorNo == HttpErrorNo.ERR_ONE_REQUEST
                                && DevTypeHelper.isOneOSNas(deviceModel.devClass)
                            /* && deviceModel?.device?.appVersion == "3.0.0.1839"*/) {
                                loginByAdmin(deviceModel)
                            } else if (deviceModel?.isOwner == true
                                && deviceModel.isOnline
                                && errorNo == V5_ERR_DISK_NOT_MOUNTED) {
                                formatWithToken(deviceModel, true)
                            } else {
                                super.onFailure(url, errorNo, errorMsg)
                                dismissLoading()
                                v.postDelayed({ ToastHelper.showToast(R.string.hdformat_failed) }, 1000)
                            }
                        }
                    })
                    dialog.dismiss()
                }
            }
        }
    }

    private fun formatWithToken(deviceModel: DeviceModel, force: Boolean) {
        hdManageViewModel.formatWithToken(deviceModel, force)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.`as`(RxLife.`as`(this))
                ?.subscribe({
                    Timber.d("formatWithToken ${it}")
                    dismissLoading()
                    if (it == true) {
                        ToastHelper.showToast(R.string.success)
                        finish()
                    } else if (it == false) {
                        ToastHelper.showToast(R.string.hdformat_failed)
                        finish()
                    }
                }, {
                    dismissLoading()
                    Timber.e(it)
                })
    }

    private fun loginByAdmin(deviceModel: DeviceModel) {
        val oneOSLoginAPI = OneOSLoginAPI(deviceModel.device?.vip
                ?: "", OneOSAPIs.ONE_API_DEFAULT_PORT,
                AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD,
                deviceModel.devId)
        oneOSLoginAPI.setTrueUser(AppConstants.DEFAULT_USERNAME_ADMIN)
        oneOSLoginAPI.setOnLoginListener(object : OneOSLoginAPI.OnLoginListener {
            override fun onStart(url: String?) {

            }

            override fun onSuccess(url: String, loginSession: LoginSession) {
                doFormat(loginSession)
            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                dismissLoading()
                ToastHelper.showLongToastSafe(R.string.hdformat_failed)

            }
        })
        oneOSLoginAPI.login(AppConstants.DOMAIN_DEVICE_VIP)
    }

    private fun doFormat(loginSession: LoginSession?) {
        loginSession?.let {
            oneOSHDFormatAPI = OneOSHDFormatAPI(loginSession)
            oneOSHDFormatAPI?.setOnFormatListener(object : OneOSHDFormatAPI.OnFormatListener {
                override fun onStart(url: String?) {

                }

                override fun onSuccess(url: String?) {
                    doFormatSuccess(loginSession)
                }

                override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                    dismissLoading()
                    ToastHelper.showToast(R.string.hdformat_failed)
                    oneOSHDFormatAPI = null
                    finish()
                }
            })
            oneOSHDFormatAPI?.format(loginSession.session, cmd)
        }
    }

    private fun doFormatSuccess(loginSession: LoginSession) {
        isSuccessFormat = true
        loginSession.isHDStatusEnable = true
        loginSession.hdError = -1
        dismissLoading()
        if (!isM8) {
            doPowerOffOrRebootDevice(false)
        } else {
            ToastHelper.showToast(R.string.success)
            finish()
        }
    }

    companion object {
        private const val TAG = "HdManageActivity"
    }
}
