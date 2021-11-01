package net.linkmate.app.poster

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.arch.core.util.Function
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_payment_code.*
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.activity_product.toolbar
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.poster.model.ProductPaymentCode
import net.linkmate.app.poster.repository.PosterRepository
import net.linkmate.app.service.DynamicQueue.checkExistPublishingDynamic
import net.linkmate.app.ui.activity.LoginActivity
import net.linkmate.app.util.DialogUtil
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.util.business.ShareUtil
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.nascommon.SessionManager
import org.view.libwidget.singleClick
import timber.log.Timber
import java.io.File


class PaymentCodeActivity : BaseActivity() {

    private val tag = "PaymentCodeActivity"
    private val CHOOSE_PHOTO = 100

    private lateinit var domain: String
    private lateinit var mToken: String
    private lateinit var price: String
    private lateinit var productId: String
    private lateinit var paymentCode: ProductPaymentCode
    private lateinit var posterRepository: PosterRepository

    companion object {
        const val TOKEN = "token"
        const val DOMAIN = "domain"
        const val PRICE = "price"
        const val PRODUCTID = "productId"
        fun startActivity(activity: Activity, token: String, domain: String, price: String, productId: String) {
            activity.startActivity(Intent(activity, PaymentCodeActivity::class.java)
                    .putExtra(TOKEN, token)
                    .putExtra(DOMAIN, domain)
                    .putExtra(PRICE, price)
                    .putExtra(PRODUCTID, productId)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_code)
        initNoStatusBar()
        initView()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        posterRepository.destroy()
    }

    private fun initView() {
        /*toolbar.inflateMenu(R.menu.dynamic_header_menu_simplestyle)
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
        }*/
        toolbar.setNavigationOnClickListener {
            finish()
        }

        btn_next.singleClick{
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, CHOOSE_PHOTO) //打开相册

        }

        btnWechat.singleClick {
            btnWechat.setBackgroundResource(R.drawable.bg_button_radius)
            btnAlipay.setBackgroundResource(R.drawable.bg_button_radius_un)
            showQrCode(1)
        }
        btnAlipay.singleClick {
            btnAlipay.setBackgroundResource(R.drawable.bg_button_radius)
            btnWechat.setBackgroundResource(R.drawable.bg_button_radius_un)
            showQrCode(2)
        }
    }

    private fun initData(){
        domain = intent?.getStringExtra(DOMAIN) ?: ""
        mToken = intent?.getStringExtra(TOKEN) ?: ""
        price = intent?.getStringExtra(PRICE) ?: ""
        productId = intent?.getStringExtra(PRODUCTID) ?: ""
        tvName.text = "￥$price"
        posterRepository = PosterRepository()
        posterRepository.qrcode(domain, mToken, object : PosterRepository.OnCodeListener {
            override fun onResponse(data: ProductPaymentCode) {
                paymentCode = data
                showQrCode(1)
            }

            override fun onError() {
                finish()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CHOOSE_PHOTO -> if (resultCode == RESULT_OK) {
                //判断手机系统版本号
                if (data != null) {
                    val imagePath = handleImageOnKitKat(data)
                    Timber.i("$tag  onActivityResult  $imagePath")
                    uploadImage(imagePath)
                }
            }
            else -> {
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun handleImageOnKitKat(data: Intent) : String? {
        val uri: Uri = data.data
        var imagePath: String? = null
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的Uri，则通过document id处理
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority) {
                val id = docId.split(":".toRegex()).toTypedArray()[1] //解析出数字格式的id
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri: Uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public downloads"), java.lang.Long.valueOf(docId))
                imagePath = getImagePath(contentUri, null)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath = getImagePath(uri, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.path
        }
        return imagePath
    }

    //将选择的图片Uri转换为路径
    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        //通过Uri和selection来获取真实的图片路径
        val cursor: Cursor? = contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }

    private fun uploadImage(imagePath: String?){
        if(imagePath == null)  return
        mProgressBar.visibility = View.VISIBLE
        posterRepository.uploadScreenshot(domain, mToken, imagePath, object : PosterRepository.OnUploadScreenshotListener {
            override fun onResponse(path: String?) {
                Timber.i("$tag  uploadScreenshot  onResponse")
                place(path)
            }

            override fun onError() {
                Timber.i("$tag  uploadScreenshot  onError")
                mProgressBar.visibility = View.GONE
            }

        })
    }

    private fun place(imagePath: String?){
        if(imagePath == null)  return
        mProgressBar.visibility = View.VISIBLE
        posterRepository.place(domain, mToken, productId, "1", imagePath, object : PosterRepository.OnPlaceListener {
            override fun onResponse() {
                Timber.i("$tag  place  onResponse")
                mProgressBar.visibility = View.GONE

            }

            override fun onError() {
                Timber.i("$tag  place  onError")
                mProgressBar.visibility = View.GONE
            }

        })
    }

    private fun showQrCode(payType: Int){
        mProgressBar.visibility = View.GONE
        val urlImg =
                if (payType == 1){
                    paymentCode.qrWechat
                }
                else if (payType == 2){
                    paymentCode.qrAlipay
                }
                else{
                    paymentCode.integralExchange
                }
        Timber.i("$tag  showQrCode  $urlImg")
        urlImg?.let {
            if(it.isNotEmpty()){
                /*BitmapUtils().loadingAndSaveImg(
                        it,
                        ivQRCode,
                        "${applicationContext.filesDir?.path}/images/qrcode",
                        applicationContext)*/
                Glide
                        .with(applicationContext)
                        .load(it)
                        .skipMemoryCache(true)
                        .into(ivQRCode)
            }
        }
    }

    fun logout() {
        DialogUtil.showExtraSelectDialog(this, getString(R.string.tips_log_out_and_delete),
                getString(R.string.just_logout), { v, strEdit, dialog, isCheck -> },
                getString(R.string.cancel), null,
                getString(R.string.logout_and_delete), { v, strEdit, dialog, isCheck -> }
        )
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
                MediaScannerConnection.scanFile(this@PaymentCodeActivity, arrayOf(file.absolutePath), arrayOf("image/png")) { path, uri ->
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