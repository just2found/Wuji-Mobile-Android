package net.linkmate.app.ui.activity.mine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_identify_code.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.UserInfoManager
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.util.business.ShareUtil
import net.linkmate.app.view.TipsBar
import net.sdvn.common.repo.BriefRepo
import java.io.File


/**我的识别码
 * @author Raleigh.Luo
 * date：21/3/18 09
 * describe：
 */
class IdentifyCodeActivity : BaseActivity() {
    private val viewModel: IdentifyCodeViewModel by viewModels()

    companion object {
        const val DEVICE_NAME = "device_name"
        const val DEVICE_ID = "device_id"
        const val DEVICE_ICON_RES = "device_icon_res"
        const val DEVICE_OWNER = "device_owner"
        const val IDENTIFY_CODE_KEY = "identify_code_key"
        const val OWN_IDENTIFY_CODE = 1
        const val DEVICE_IDENTIFY_CODE = 2
        fun startMyIdCode(context: Context) {
            context.startActivity(Intent(context, IdentifyCodeActivity::class.java)
                    .putExtra(IDENTIFY_CODE_KEY, OWN_IDENTIFY_CODE))
        }

        fun startDeviceIdCode(context: Context, device: DeviceBean, deviceName: String) {
            var name = device.hardData?.nickname
            if (TextUtils.isEmpty(name)) name = device.ownerName
            context.startActivity(Intent(context, IdentifyCodeActivity::class.java)
                    .putExtra(IDENTIFY_CODE_KEY, DEVICE_IDENTIFY_CODE)
                    .putExtra(DEVICE_NAME, deviceName)
                    .putExtra(DEVICE_ID, device.id)
                    .putExtra(DEVICE_ICON_RES, DeviceBean.getIcon(device))
                    .putExtra(DEVICE_OWNER, name)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify_code)
        initNoStatusBar()
        initView()
        iniObserver()
        viewModel.start(if (intent.getIntExtra(IDENTIFY_CODE_KEY, 0) == DEVICE_IDENTIFY_CODE)
            intent.getStringExtra(DEVICE_ID) else null)
    }


    private fun iniObserver() {
        viewModel.getIdentifyCodeResult.observe(this, Observer {
            if (it.isSuccessful) {
                generateQRCode(it.data?.indentifycode)
            } else {
                mProgressBar.visibility = View.GONE
                ToastUtils.showError(it.result, it.errmsg)
            }
        })
    }

    private fun initView() {
        toolbar.inflateMenu(R.menu.dynamic_header_menu_simplestyle)
        toolbar.menu.findItem(R.id.add).setIcon(R.drawable.icon_qrcode_download)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add -> {//下载
                    if (!TextUtils.isEmpty(mShareCode)) {
                        saveImageToFile(mShareCode!!)
                    }
                }
            }
            true
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
        if (!intent.hasExtra(IDENTIFY_CODE_KEY)) {//参数错误，直接关闭页面
            finish()
        }
        if (intent.getIntExtra(IDENTIFY_CODE_KEY, 0) == DEVICE_IDENTIFY_CODE) {
            showDeviceQRCode()
        } else {
            showOwnQRCode()
        }


    }

    /**
     * 保存二维码到本地
     */
    private fun saveImageToFile(mShareCode: String) {
        val fileName: String = mShareCode + ".png"
        val dir = Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_PICTURES
        val file = File(dir, fileName)
        ShareUtil.saveImageToFileForPicture(clQrCode, this, mShareCode, object : ShareUtil.SaveImageResult {
            override fun onSuccess() {
                MediaScannerConnection.scanFile(this@IdentifyCodeActivity, arrayOf(file.absolutePath), arrayOf("image/png")) { path, uri ->
                    runOnUiThread {
                        ToastUtils.showToast(R.string.has_saved_in_phone)
                    }
                }
            }

            override fun onError() {

            }
        })
    }

    // 分享码
    private var mShareCode: String? = null

    // 分享提示
    private var mShartTips: String? = null

    // 分享码图片
    private var mShareBitmap: Bitmap? = null


    /**
     * 生成二维码
     */
    private fun generateQRCode(shareCode: String?) {
        val eventCode: Int = if (intent.getIntExtra(IDENTIFY_CODE_KEY, 0) == DEVICE_IDENTIFY_CODE)
            MyConstants.EVENT_CODE_DEVICE_CODE else MyConstants.EVENT_CODE_MY_IDENTIFY_CODE
        ShareUtil.generateQRCode(this, eventCode, shareCode, 0, object : ShareUtil.QRCodeResult {
            override fun onGenerated(bitmap: Bitmap?, tips: String?) {
                mProgressBar.visibility = View.GONE
                mShareBitmap = bitmap
                mShareCode = shareCode
                mShartTips = tips
                ivQRCode.setImageBitmap(mShareBitmap)
            }
        })
    }

    private fun showOwnQRCode() {
        toolbar.title = getString(R.string.my_identify_code)
        val bean = UserInfoManager.getInstance().userInfoBean
        if (bean != null) {
            tvContent.setText(bean.loginname)
            tvName.setText(bean.nickname ?: bean.loginname)
        }
    }

    private fun showDeviceQRCode() {
        toolbar.title = getString(R.string.device_identify_code)
        tvName.setText(intent.getStringExtra(DEVICE_NAME))
        tvContent.setText(intent.getStringExtra(DEVICE_OWNER))
        val defualt = intent.getIntExtra(DEVICE_ICON_RES, 0)
        ivPortrait.setImageResource(defualt)
        viewModel.loadBrief(intent.getStringExtra(DEVICE_ID),
                BriefRepo.getBrief(intent.getStringExtra(DEVICE_ID), BriefRepo.FOR_DEVICE),
                ivImage = ivPortrait,
                defalutImage = defualt,
                isLoadOneDeviceBrief = true)
    }

    override fun getTipsBar(): TipsBar? {
        return null
    }

    override fun getTopView(): View? {
        return null
    }

    /**
     * 状态栏修改
     */
    private fun initNoStatusBar() {
        //修改状态栏颜色
        window.setStatusBarColor(getResources().getColor(R.color.dynamic_toolbar_color));
        // 修改状态栏字体：深色SYSTEM_UI_FLAG_LIGHT_STATUS_BAR/浅色SYSTEM_UI_FLAG_LAYOUT_STABLE
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.decorView.systemUiVisibility = option
        flToolbar.setPadding(flToolbar.paddingLeft, UIUtils.getStatueBarHeight(this),
                flToolbar.paddingRight, flToolbar.paddingBottom)
    }
}