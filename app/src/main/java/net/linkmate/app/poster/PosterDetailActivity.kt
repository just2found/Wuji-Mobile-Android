package net.linkmate.app.poster

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_poster_detail.*
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.activity_product.tvPlot
import kotlinx.android.synthetic.main.activity_product.tvTitle
import kotlinx.android.synthetic.main.fragment_nav_cloud_files.*
import kotlinx.android.synthetic.main.fragment_nav_cloud_files.btn_trans
import kotlinx.android.synthetic.main.tab_text.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity.LoadingStatus
import net.linkmate.app.base.MyApplication
import net.linkmate.app.poster.adapter.ImageViewPagerAdapter
import net.linkmate.app.poster.database.AppDatabase
import net.linkmate.app.poster.model.*
import net.linkmate.app.poster.repository.PosterRepository
import net.linkmate.app.poster.utils.StatusBarUtils
import net.linkmate.app.poster.utils.isISOVideo
import net.linkmate.app.ui.activity.LoginActivity
import net.linkmate.app.ui.nas.files.V2NasDetailsActivity2
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.UIUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils
import org.view.libwidget.singleClick
import timber.log.Timber

class PosterDetailActivity : AppCompatActivity() {

  private lateinit var mediaInfoModel: MediaInfoModel
  private lateinit var id: String
  private lateinit var session: String
  private lateinit var ip: String
  private lateinit var domain: String
//  private var mToken: String? = null
  private var isComeCircle = false
  private lateinit var posterRepository: PosterRepository
  private var clickState = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    StatusBarUtils.translucent(this, Color.TRANSPARENT)
//    StatusBarUtils.setStatusBarLightMode(this)
    setContentView(R.layout.activity_poster_detail)

    posterRepository = PosterRepository()
    domain = intent?.getStringExtra("domain") ?: ""
    mediaInfoModel = intent?.getSerializableExtra("data") as MediaInfoModel
    id = intent?.getStringExtra("id") ?: ""
    session = intent?.getStringExtra("session") ?: ""
    ip = intent?.getStringExtra("ip") ?: ""
    isComeCircle = intent?.getBooleanExtra("isComeCircle", false) ?: false
//    navHostFragment = NavHostFragment.create(R.navigation.files_nav,
//            SelectTypeFragmentArgs(id).toBundle())
    initView()

    initEvent()

    initData()

  }

  override fun onDestroy() {
    super.onDestroy()
    posterRepository.destroy()
  }

  private fun initView(){

    val statueBarHeight = UIUtils.getStatueBarHeight(MyApplication.getContext())
    val layoutParams = layout_tab.layoutParams
    layoutParams.height = layoutParams.height + statueBarHeight
    layout_tab.layoutParams = layoutParams
    if(!mediaInfoModel.path.contains(".") || isISOVideo(mediaInfoModel.path)){
      btn_play.visibility = View.GONE
    }

    mediaInfoModel.fanartList?.let {
      val layoutParams = layout_view_pager.layoutParams
      val screenWidth = Dp2PxUtils.getScreenWidth(this)
      layoutParams.height = (screenWidth * 9f / 16).toInt()
      layout_view_pager.layoutParams = layoutParams
      val layoutManager = LinearLayoutManager(MyApplication.getContext())
      layoutManager.orientation = RecyclerView.HORIZONTAL
      val adapter = ImageViewPagerAdapter(id, ip, session, it, this)
      picturePhotoViewPager.adapter = adapter
      picturePhotoViewPager.setCurrentItem(0, false)
      pictureCircleIndicatorLayout.setCount(it.size, 0)
    }

    tvTitle.text = mediaInfoModel.title
    tvInfo.text = getInfo()
    tvActor.text = getActor()
    tvPlot.text = mediaInfoModel.plot
    mediaInfoModel.posterList?.let {
      if(it.isNotEmpty()){
        val url = "http://${ip}:9898/file/download?session=${session}&path=${it[0]}"
        Glide.with(MyApplication.getContext())
                .load(url)
                .into(imgCover)
      }
    }
  }

  private fun getActor() : String {
    var info = "导演：${mediaInfoModel.director}\n"
    mediaInfoModel.actor?.forEachIndexed { index, it->
      if(index == 0){
        info += "主演：${it}"
      }
      else{
        info += "/${it}"
      }
    }
    return info
  }

  private fun getInfo() : String {
    var info = "${mediaInfoModel.premiered}/${mediaInfoModel.runtime}"
    info += "/${Formatter.formatFileSize(applicationContext, mediaInfoModel.mediaSize)}"
    mediaInfoModel.country?.forEach { info += "/${it}" }
    mediaInfoModel.genre?.forEach { info += "/${it}" }
    return info
  }

  private fun initEvent() {
    //返回键
    btn_trans.singleClick {
      finish()
    }

    btn_download.singleClick {
      downloadFiles()
    }

    btn_play.singleClick {
      play()
    }

    picturePhotoViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) {
        pictureCircleIndicatorLayout.setCurrentIndex(position)
      }

      override fun onPageScrollStateChanged(state: Int) {
      }

    })
  }

  private fun initData(){
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
      val dao = AppDatabase.getInstance(applicationContext).getUserDao()
      val userModel = dao.getUser(un)
      if(userModel == null){
        Timber.i("PosterActivity loginCrm userModel == null un:$un")
        runOnUiThread {
          //退出登录
          CMAPI.getInstance().cancelLogin()
          CMAPI.getInstance().removeUser(CMAPI.getInstance().baseInfo.account)
          SessionManager.getInstance().removeAccount()
          startActivity(Intent(this, LoginActivity::class.java))
          finish()
        }
        return@Thread
      }

      val pwdNew = userModel.pwdNew
      if (pwdNew.isNullOrEmpty()){
        Timber.i("PosterActivity loginCrm pwdNew.isNullOrEmpty")
        finish()
        return@Thread
      }
      if (SingletonPoster.get().token == null){
        posterRepository.loginCrm(domain, un, userModel.pwd, pwdNew,
                object : PosterRepository.OnLoginCrmListener {
                  override fun onLogin(token: String) {
                    SingletonPoster.get().token = token
                    if (mProgressBar.visibility == View.VISIBLE) {
                      mProgressBar.visibility = View.GONE
                      when (clickState) {
                        1 -> play()
                        2 -> downloadFiles()
                      }
                    }
                  }

                  override fun onError() {
                  }
                })
      }
    }.start()
  }

  private fun startProductActivity(){
    val mIntent = Intent(MyApplication.getContext(), ProductActivity::class.java)
    mIntent.putExtra("deviceid", id)
    mIntent.putExtra("domain", domain)
    startActivity(mIntent)
  }

  private fun play(){
    val path = mediaInfoModel.path
    if(path.isEmpty()) {
      return
    }
    if(SingletonPoster.get().token == null){
      mProgressBar.visibility = View.VISIBLE
      clickState = 1
      return
    }
    posterRepository.orders(domain, SingletonPoster.get().token!!, object : PosterRepository.OnOrdersListener {
      override fun onResponse(data: ArrayList<OrderModel>?) {
        if (data.isNullOrEmpty()) {
          startProductActivity()
          return
        }
        SessionManager.getInstance().getLoginSession(id, object : GetSessionListener(false) {
          override fun onSuccess(url: String?, loginSession: LoginSession) {
            val file = OneOSFile()
            file.share_path_type = 2
            file.setPath(path)
            val pathS = path.split("/")
            val name = pathS[(pathS.size - 1)]
            file.setName(name)
            FileUtils.openOneOSFile(
                    loginSession,
                    this@PosterDetailActivity,
                    file
            )
          }
        })
      }

      override fun onError() {
        initData()
      }
    })
  }

  private fun downloadFiles() {
    if(SingletonPoster.get().token == null){
      mProgressBar.visibility = View.VISIBLE
      clickState = 2
      return
    }
    posterRepository.orders(domain, SingletonPoster.get().token!!, object : PosterRepository.OnOrdersListener {
      override fun onResponse(data: ArrayList<OrderModel>?) {
        if (data.isNullOrEmpty()) {
          startProductActivity()
          return
        }
        val intent = Intent(applicationContext, V2NasDetailsActivity2::class.java)
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, id)
        intent.putExtra("path", mediaInfoModel.path)
        intent.putExtra("isComeCircle", isComeCircle)
        startActivity(intent)
      }

      override fun onError() {
        initData()
      }
    })
  }

}