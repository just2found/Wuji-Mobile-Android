package net.linkmate.app.poster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_poster.*
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.activity_product.appBar
import kotlinx.android.synthetic.main.activity_product.bgImageView
import kotlinx.android.synthetic.main.activity_product.collapseLayout
import kotlinx.android.synthetic.main.activity_product.coverImageView
import kotlinx.android.synthetic.main.activity_product.loading_container
import kotlinx.android.synthetic.main.activity_product.toolbar
import kotlinx.android.synthetic.main.activity_product.tvPlot
import kotlinx.android.synthetic.main.activity_product.tvTitle
import kotlinx.android.synthetic.main.activity_product.tvToolbarTitle
import kotlinx.android.synthetic.main.fragment_nav_cloud_files.*
import kotlinx.android.synthetic.main.item_poster_video.view.*
import kotlinx.android.synthetic.main.item_pup_list_layout.view.*
import kotlinx.android.synthetic.main.pop_window.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.poster.adapter.ProductAdapter
import net.linkmate.app.poster.database.AppDatabase
import net.linkmate.app.poster.database.AppDatabase.Companion.getInstance
import net.linkmate.app.poster.model.*
import net.linkmate.app.poster.repository.PosterRepository
import net.linkmate.app.poster.utils.BitmapUtils
import net.linkmate.app.poster.utils.StatusBarUtils
import net.linkmate.app.ui.nas.files.V2NasDetailsActivity2
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.view.LinearLayoutManager
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.aria.AriaInfo
import net.sdvn.nascommon.model.oneos.user.LoginSession
import timber.log.Timber
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList

//const val ACTION_POSTER_BROADCAST = "net.linkmate.app.poster.ACTION_POSTER_BROADCAST"

class ProductActivity : AppCompatActivity() {

  /*companion object{
    val RESULT_CODE_TO_FILE_FRAGMENT = 1
    fun startActivityForResult(activity: Activity, intent: Intent, requestCode: Int){
      intent.setClass(activity, PosterActivity::class.java)
      activity.startActivityForResult(intent,requestCode)
//      activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
  }*/

  private var rootView: View? = null
//  private var mToken: String? = null
  private lateinit var deviceId: String
  private lateinit var domain: String
  private var deviceInfoModel: DeviceInfoModel? = null
  private var mLoginSession: LoginSession? = null
  private lateinit var posterRepository: PosterRepository
  private lateinit var adapter: ProductAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    StatusBarUtils.translucent(this, Color.TRANSPARENT)
    StatusBarUtils.setStatusBarLightMode(this)
    setContentView(R.layout.activity_product)

    initView()

    initEvent()

    initData()
  }

  override fun onStart() {
    super.onStart()
  }

  override fun onDestroy() {
    super.onDestroy()
    posterRepository.destroy()
  }

  override fun finish() {
    super.finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  }

  private fun initView() {
    posterRepository = PosterRepository()
    domain = intent?.getStringExtra("domain") ?: ""
    deviceId = intent?.getStringExtra("deviceid") ?: ""
    //背景图片为屏幕1/3高
    val layoutParams = collapseLayout.layoutParams as AppBarLayout.LayoutParams
    val screenWidth = Dp2PxUtils.getScreenWidth(this)
    layoutParams.height = (screenWidth * 2f / 3).toInt()
    collapseLayout.layoutParams = layoutParams
    rootView = LayoutInflater.from(this).inflate(R.layout.pop_window, null)

    adapter = ProductAdapter()
    recyclerView.layoutManager = LinearLayoutManager(applicationContext)
    recyclerView.itemAnimator = null
    recyclerView.adapter = adapter
  }

  private fun initEvent() {
    appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
      if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
        if (tvToolbarTitle.visibility == View.GONE) {
          tvToolbarTitle.visibility = View.VISIBLE
          toolbar.navigationIcon = getDrawable(R.drawable.icon_return_black)
        }
      } else {
        if (tvToolbarTitle.visibility == View.VISIBLE) {
          tvToolbarTitle.visibility = View.GONE
          toolbar.navigationIcon = getDrawable(R.drawable.icon_return)
        }
      }
    })

    toolbar.setNavigationOnClickListener {
      finish()
    }

    adapter.setOnItemClickListener { _, _, i ->
      val product = adapter.getItem(i)
      if(product != null){
        PaymentCodeActivity.startActivity(
                this,
                SingletonPoster.get().token!!,
                domain,
                product.price?:"",
                product.id)
      }
    }

  }

  /*private fun showPopupWindow() {
    val popupWindow = PopupWindow(
            rootView,
            500,
            700,
            true
    )
    popupWindow.isTouchable = true
    popupWindow.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_pop))
    popupWindow.showAtLocation(viewPager, Gravity.CENTER, 0, 100)
  }*/

  private fun initData(){
    if(!SingletonPoster.get().token.isNullOrEmpty()){
      loading_container?.visibility = View.VISIBLE
      initTab()
      getProductLists()
      getOrders()
      return
    }
    Timber.i("PosterActivity loginCrm")
    Thread{
      if (domain.isNullOrEmpty()){
        Timber.i("PosterActivity loginCrm domain.isNullOrEmpty")
        finish()
        return@Thread
      }
      val un = CMAPI.getInstance().baseInfo.account
      if (un.isNullOrEmpty()){
        Timber.i("PosterActivity loginCrm un.isNullOrEmpty")
        finish()
        return@Thread
      }
      val dao = getInstance(applicationContext).getUserDao()
      val userModel = dao.getUser(un)
      if(userModel == null){
        Timber.i("PosterActivity loginCrm userModel == null un:$un")
        finish()
        return@Thread
      }

      val pwdNew = userModel.pwdNew
      if (pwdNew.isNullOrEmpty()){
        Timber.i("PosterActivity loginCrm pwdNew.isNullOrEmpty")
        finish()
        return@Thread
      }
      runOnUiThread {
        loading_container?.visibility = View.VISIBLE
        initTab()
      }
      posterRepository.loginCrm(domain, un,userModel.pwd, pwdNew,
              object : PosterRepository.OnLoginCrmListener {
                override fun onLogin(token: String) {
                  SingletonPoster.get().token = token
                  getProductLists()
                  getOrders()
                }

                override fun onError() {
                  loading_container?.visibility = View.GONE
                  finish()
                }
              })
    }.start()
  }

  private fun getProductLists(){
    posterRepository.productLists(domain,SingletonPoster.get().token!!,object : PosterRepository.OnProductListsListener{
      override fun onResponse(list: ArrayList<ProductResult>) {
        loading_container?.visibility = View.GONE
        adapter.setNewData(list)
      }

      override fun onError() {
        loading_container?.visibility = View.GONE
        finish()
      }

    })
  }

  private fun getOrders(){
    posterRepository.orders(domain, SingletonPoster.get().token!!,object : PosterRepository.OnOrdersListener{
      override fun onResponse(data: ArrayList<OrderModel>?) {
        if(data.isNullOrEmpty()){
          return
        }
        tvPlot.text = "有效期：${data[0].endTime}"
      }

      override fun onError() {
      }
    })
  }

  private fun initTab(){
    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener(false) {

      override fun onSuccess(url: String?, loginSession: LoginSession?) {

        Timber.i("cyb=$url---${loginSession?.session}")
        mLoginSession = loginSession
        Thread{
          deviceInfoModel =
                  AppDatabase.getInstance(applicationContext).getDeviceInfoDao()
                          .getDeviceInfo(deviceId)
          runOnUiThread{
            tvToolbarTitle.text = deviceInfoModel?.name
            tvTitle.text = deviceInfoModel?.name
            //tvPlot.text = deviceInfoModel?.plot
            deviceInfoModel?.movie_poster_bg?.let {
              if(it.isNotEmpty()){
                BitmapUtils().loadingAndSaveImg(
                        it,
                        bgImageView,
                        "${MyApplication.getContext().filesDir?.path}/images/poster/${mLoginSession?.deviceInfo?.id.toString()}",
                        mLoginSession?.session!!, mLoginSession?.deviceInfo?.vIp!!, mLoginSession?.deviceInfo?.id.toString(),
                        MyApplication.getContext())
              }
            }
            deviceInfoModel?.movie_poster_cover?.let {
              if(it.isNotEmpty()){
                BitmapUtils().loadingAndSaveImg(
                        it,
                        coverImageView,
                        "${MyApplication.getContext().filesDir?.path}/images/poster/${mLoginSession?.deviceInfo?.id.toString()}",
                        mLoginSession?.session!!, mLoginSession?.deviceInfo?.vIp!!, mLoginSession?.deviceInfo?.id.toString(),
                        MyApplication.getContext())
              }
            }
          }

        }.start()

      }
    })
  }

}