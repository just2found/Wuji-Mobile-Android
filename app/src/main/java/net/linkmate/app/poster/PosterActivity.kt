package net.linkmate.app.poster

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.material.appbar.AppBarLayout
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.activity_poster.*
import kotlinx.android.synthetic.main.activity_poster.appBar
import kotlinx.android.synthetic.main.activity_poster.bgImageView
import kotlinx.android.synthetic.main.activity_poster.collapseLayout
import kotlinx.android.synthetic.main.activity_poster.coverImageView
import kotlinx.android.synthetic.main.activity_poster.loading_container
import kotlinx.android.synthetic.main.activity_poster.toolbar
import kotlinx.android.synthetic.main.activity_poster.tvPlot
import kotlinx.android.synthetic.main.activity_poster.tvTitle
import kotlinx.android.synthetic.main.activity_poster.tvToolbarTitle
import kotlinx.android.synthetic.main.activity_poster.tv_loading_tip
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.fragment_nav_cloud_files.*
import kotlinx.android.synthetic.main.item_poster_video.view.*
import kotlinx.android.synthetic.main.item_pup_list_layout.view.*
import kotlinx.android.synthetic.main.pop_window.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.poster.adapter.MyViewPagerAdapter
import net.linkmate.app.poster.database.AppDatabase.Companion.getInstance
import net.linkmate.app.poster.model.*
import net.linkmate.app.poster.repository.PosterRepository
import net.linkmate.app.poster.utils.BitmapUtils
import net.linkmate.app.poster.utils.FileUtils
import net.linkmate.app.poster.utils.StatusBarUtils
import net.linkmate.app.ui.nas.transfer.TransferActivity
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.Utils
import timber.log.Timber
import java.io.File
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList

//const val ACTION_POSTER_BROADCAST = "net.linkmate.app.poster.ACTION_POSTER_BROADCAST"

class PosterActivity : AppCompatActivity() {

  /*companion object{
    val RESULT_CODE_TO_FILE_FRAGMENT = 1
    fun startActivityForResult(activity: Activity, intent: Intent, requestCode: Int){
      intent.setClass(activity, PosterActivity::class.java)
      activity.startActivityForResult(intent,requestCode)
//      activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
  }*/

  private var deviceId: String? = null

//  private var posterVideoAdapter: BaseQuickAdapter<MediaInfoModel, BaseViewHolder>? = null
//  private var deviceInfoModel: DeviceInfoModel? = null
  private var mLoginSession: LoginSession? = null
  private lateinit var posterRepository: PosterRepository
  private var domain: String? = null

//  private var titles = ArrayList<String>()
  private var secondTitles = HashMap<String, ArrayList<String>>()
  private val fragments = ArrayList<TabFragment>()
  private var currentPosition = 0
  private var rootView: View? = null
  private var adapterPopup: BaseQuickAdapter<String, BaseViewHolder>? = null
  private var isOnDestroy = false
  private var isComeCircle = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    StatusBarUtils.translucent(this, Color.TRANSPARENT)
    StatusBarUtils.setStatusBarLightMode(this)

    setContentView(R.layout.activity_poster)

    //背景图片为屏幕1/3高
    val layoutParams = collapseLayout.layoutParams as AppBarLayout.LayoutParams
    val screenWidth = Dp2PxUtils.getScreenWidth(this)
    layoutParams.height = (screenWidth * 2f / 3).toInt()
    collapseLayout.layoutParams = layoutParams

    rootView = LayoutInflater.from(this).inflate(R.layout.pop_window, null)
    adapterPopup = object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_pup_list_layout) {
      override fun convert(helper: BaseViewHolder, item: String) {
        helper.itemView.tvType.text = item
        val textColor = if(fragments[currentPosition].getCurrentPosition() == helper.layoutPosition){
          Color.parseColor("#1377cc")
        }
        else {
          Color.parseColor("#ffffff")
        }
        helper.itemView.tvType.setTextColor(textColor)
      }
    }
    adapterPopup?.setHasStableIds(true)
    rootView?.rvMovieType?.adapter = adapterPopup
    rootView?.rvMovieType?.layoutManager = LinearLayoutManager(this)

    initEvent()
//    registerReceiver()
    initDatas()
  }

  override fun onStart() {
    super.onStart()
  }

  override fun onDestroy() {
    val f = File("${MyApplication.getContext().filesDir?.path}/images/poster/")
    val fLength = FileUtils.getFolderSize(f) /1024/1024
    if(fLength > 200)
      FileUtils.deleteFolderFile("${MyApplication.getContext().filesDir?.path}/images/poster/")
    posterRepository.destroy()
    isOnDestroy = true
    SingletonPoster.get().token = null
    super.onDestroy()
  }

  override fun finish() {
    super.finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    if(requestCode == 0){
      if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
        getPosterData()
      }
      else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
        finish()
      }
      else{
        showStorageSettings(this)
      }
    }else{
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  /*private fun registerReceiver(){
    val intentFilter = IntentFilter()
    intentFilter.addAction(ACTION_POSTER_BROADCAST)
    registerReceiver(mBroadcastReceiver,intentFilter)
  }*/

  /*private val mBroadcastReceiver = MyBroadcastReceiver()
  class  MyBroadcastReceiver : BroadcastReceiver(){
    override fun onReceive(p0: Context?, p1: Intent?) {
      val deviceId = p1?.getStringExtra("deviceid") ?: ""
      val poster = PosterActivity()
      poster.getPosterData(deviceId)
    }
  }*/

  private fun initDatas() {
    domain = intent?.getStringExtra("domain") ?: ""
    deviceId = intent?.getStringExtra("deviceid") ?: ""
    isComeCircle = intent?.getBooleanExtra("isComeCircle", false) ?: false
    posterRepository = PosterRepository()
    getPosterData()
    loginCrm()
  }

  private fun loginCrm(){
    Timber.i("PosterActivity loginCrm")
    Thread{
      if (domain.isNullOrEmpty()){
        return@Thread
      }
      val un = CMAPI.getInstance().baseInfo.account
      if (un.isNullOrEmpty()){
        return@Thread
      }
      val dao = getInstance(applicationContext).getUserDao()
      val userModel = dao.getUser(un) ?: return@Thread

      val pwdNew = userModel.pwdNew
      if (pwdNew.isNullOrEmpty()){
        Timber.i("PosterActivity loginCrm pwdNew.isNullOrEmpty")
        finish()
        return@Thread
      }
      posterRepository.loginCrm(domain!!, un,userModel.pwd, pwdNew,
              object : PosterRepository.OnLoginCrmListener {
                override fun onLogin(token: String) {
                  SingletonPoster.get().token = token
                }

                override fun onError() {
                }
              })
    }.start()
  }

  private fun showStorageSettings(context: Context?) {
    DialogUtils.showConfirmDialog(context,
            net.sdvn.nascommonlib.R.string.permission_denied,
            net.sdvn.nascommonlib.R.string.perm_denied_storage,
            net.sdvn.nascommonlib.R.string.settings, net.sdvn.nascommonlib.R.string.cancel
    ) { _, isPositiveBtn ->
      if (isPositiveBtn) {
        Utils.gotoAppDetailsSettings(context!!)
      }
      else{
        finish()
      }
    }
  }

  private fun showFabMenu(){
    val leftTab = secondTitles[topTabLayout.getTabAt(currentPosition)?.text]
    if(leftTab == null || leftTab.isEmpty() || tvToolbarTitle.visibility == View.GONE){
      fabMenu.hide()
    }else {
      fabMenu.show()
    }
  }

  private fun initEvent() {
    appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
      if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
        if (tvToolbarTitle.visibility == View.GONE) {
          tvToolbarTitle.visibility = View.VISIBLE
          toolbar.navigationIcon = getDrawable(R.drawable.icon_return_black)
          showFabMenu()
        }
      } else {
        if (tvToolbarTitle.visibility == View.VISIBLE) {
          tvToolbarTitle.visibility = View.GONE
          toolbar.navigationIcon = getDrawable(R.drawable.icon_return)
          showFabMenu()
        }
      }
    })

    fabMenu.setOnClickListener {
      showPopupWindow()
    }
    toolbar.setNavigationOnClickListener {
      finish()
    }
    toolbar.setOnMenuItemClickListener {
      when(it.itemId){
        R.id.action_vip -> {
          val mIntent = Intent(MyApplication.getContext(), ProductActivity::class.java)
          mIntent.putExtra("deviceid",deviceId)
          mIntent.putExtra("domain",domain)
          startActivity(mIntent)
        }
        R.id.action_transfer -> {
          startActivity(Intent(MyApplication.getContext(), TransferActivity::class.java).apply {
            putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
          })
        }
        R.id.action_update -> {
          showUpdateTarDialog(this)
        }
      }
      true
    }
    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

      }

      override fun onPageSelected(position: Int) {
        currentPosition = position
        showFabMenu()
      }

      override fun onPageScrollStateChanged(state: Int) {
      }
    })
    adapterPopup?.setOnItemClickListener { _, _, position ->
      fragments[currentPosition].setCurrentPosition(position)
      adapterPopup?.notifyDataSetChanged()
    }
    loading_container.setOnClickListener {  }
    loading_container_bg.setOnClickListener {  }
  }

  private fun showUpdateTarDialog(context: Context?) {
    if (mLoginSession?.userInfo?.admin != 0){
      setResult(Activity.RESULT_OK)
      finish()
      return
    }
    DialogUtils.showConfirmDialog(context,
            R.string.rebuild,
            R.string.rebuild_hint,
            net.sdvn.nascommonlib.R.string.pickerview_submit, net.sdvn.nascommonlib.R.string.cancel
    ) { _, isPositiveBtn ->
      if (isPositiveBtn) {
        loading_container?.visibility = View.VISIBLE
        updateFile()
      }
//      else{
//        setResult(Activity.RESULT_OK)
//        finish()
//      }
    }
  }

  private fun updateFile(){
    posterRepository.updateTar(mLoginSession!!.session!!, object : PosterRepository.OnUpdateListener {
      override fun onUpdate(isSuccess: Boolean, code: Int, msg: String) {
        runOnUiThread {
          loading_container?.visibility = View.GONE
          if (isSuccess) {
            showUpdateDialog()
          } else {
            ToastUtils.showToast("$code $msg")
          }
        }
      }
    }, MyApplication.getContext())
  }

  private fun showPopupWindow() {
    adapterPopup?.setNewData(secondTitles[topTabLayout.getTabAt(currentPosition)?.text]!!)
    val popupWindow = PopupWindow(
            rootView,
            500,
            700,
            true
    )
    popupWindow.isTouchable = true
    popupWindow.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_pop))
    popupWindow.showAtLocation(viewPager, Gravity.CENTER, 0, 100)
    rootView?.rvMovieType?.scrollToPosition(fragments[currentPosition].getCurrentPosition())
  }

  private fun getPosterData(){
    if(deviceId.isNullOrEmpty()){
      Thread{
        while (deviceId.isNullOrEmpty() && !isOnDestroy){
          if (!MyApplication.getDeviceId().isNullOrEmpty()){
            deviceId = MyApplication.getDeviceId()
            runOnUiThread{
              getPosterData()
            }
          }
        }
      }.start()
      return
    }
    if(mLoginSession != null) return
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      //申请权限
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
      return
    }
    loading_container_bg?.visibility = View.VISIBLE
    loading_container?.visibility = View.VISIBLE
    SessionManager.getInstance().getLoginSession(deviceId!!, object : GetSessionListener(false) {
      override fun onSuccess(url: String?, loginSession: LoginSession?) {
        Timber.i("cyb=$url---${loginSession?.session}")

        mLoginSession = loginSession
        if (mLoginSession?.userInfo?.admin == 0) {
          toolbar.inflateMenu(R.menu.menu_toolbar_own)
        } else {
          toolbar.inflateMenu(R.menu.menu_toolbar)
        }
        posterRepository.getPosterData(
                onPullTarListener,
                mLoginSession!!.session!!,
                deviceId!!,
                mLoginSession!!.deviceInfo!!.vIp,
                MyApplication.getContext())
      }
    })
  }

  private val onPullTarListener = object : PosterRepository.OnPullTarListener{
    override fun onStart() {
      loading_container_bg?.visibility = View.VISIBLE
      loading_container?.visibility = View.VISIBLE
    }

    override fun onProgress(progress: Int) {
      tv_loading_tip?.text = "${getString(R.string.loading)}${progress}%"
    }

    override fun onSuccess(data: List<TopWithLeftTabModel>, deviceInfoModel: DeviceInfoModel?, isScan: Boolean) {
//      Handler().postDelayed(Runnable{
        initTab(data, deviceInfoModel)
//      }, if(isScan) 2000 else 100)
      tv_loading_tip?.text = getString(R.string.loading)
    }

    override fun onToFileFragment() {
      setResult(Activity.RESULT_OK)
      finish()
    }

    override fun update() {
      tv_loading_tip?.text = getString(R.string.loading)
      showUpdateDialog()
    }

    override fun onNoTar() {
      tv_loading_tip?.text = getString(R.string.loading)
      showUpdateTarDialog(this@PosterActivity)
    }

  }

  private fun initTab(list: List<TopWithLeftTabModel>, deviceInfoModel: DeviceInfoModel?) {
    if(list.isNotEmpty()){
      loading_container_bg.visibility = View.GONE
    }
    loading_container?.visibility = View.GONE
    tvToolbarTitle.text = deviceInfoModel?.name
    tvTitle.text = deviceInfoModel?.name
    tvPlot.text = deviceInfoModel?.plot
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
    fragments.clear()
    topTabLayout.removeAllTabs()
    val data:ArrayList<TopWithLeftTabModel> = arrayListOf()
    data.addAll(list)
    //topTab 排序
    data.sortBy { it.topTabModel.index }
    secondTitles.clear()
    data.forEach {
      it.leftTabs.sortedBy { it1 -> it1.index }
      val listTabName = ArrayList<String>()
      it.leftTabs.forEach { left ->
        listTabName.add(left.name)
      }
      secondTitles[it.topTabModel.name] = listTabName
      Timber.i("TabFragment = ${it.topTabModel.name} ${it.topTabModel.posterData?.size ?: 0}")
      fragments.add(TabFragment.newInstance(domain!!,it.topTabModel.posterData, it.leftTabs, mLoginSession, isComeCircle))
      topTabLayout.addTab(topTabLayout.newTab(), false)
    }

    val adapter = MyViewPagerAdapter(fragments, supportFragmentManager)
    viewPager.adapter = adapter
    viewPager.offscreenPageLimit = 0
    topTabLayout.setupWithViewPager(viewPager, false)

    data.forEachIndexed { index, it ->
      topTabLayout.getTabAt(index)?.text = it.topTabModel.name
    }

  }

  private fun showUpdateDialog() {
    DialogUtils.showConfirmDialog(
            this@PosterActivity,
            R.string.update_poster_title,
            R.string.update_poster_hint,
            R.string.confirm, R.string.cancel
    ) { _, isPositiveBtn ->
      if (isPositiveBtn) {
        loading_container_bg?.visibility = View.VISIBLE
        loading_container?.visibility = View.VISIBLE
        posterRepository.getTarAndParse()
      }
    }
  }

  /*private fun scan(devId: String){
    val device =
            PosterBundleModel(mLoginSession!!.session!!, "", mLoginSession!!.deviceInfo!!.vIp, devId, "")
    val scan = PosterScan()
    scan.startScan(device, object : PosterScan.OnScanListener {
      override fun onQuerySuccess(data: List<TopWithLeftTabModel>, deviceInfoModel: DeviceInfoModel?,isScan: Boolean) {
        Handler().postDelayed(Runnable{
          initTab(data, deviceInfoModel)
        }, if(isScan) 2000 else 100)
      }

      override fun onStartScan() {
        Timber.i("cyb = onStartScan")
        loading_container_bg?.visibility = View.VISIBLE
        loading_container?.visibility = View.VISIBLE
      }

      override fun onFileAdd(progress: Int) {
        Timber.i("cyb = onFileAdd=$progress")
      }

      override fun onFileScanSuccess() {
        Timber.i("cyb = onFileScanSuccess")
      }

      override fun onToFileFragment() {
        Timber.i("cyb = onToFileFragment")
        setResult(Activity.RESULT_OK)
        finish()
      }

      override fun onParsingNfoStart(max: Int) {
        Timber.i("cyb = max=$max")
      }

      override fun onParsingNfoAdd(progress: Int) {
        Timber.i("cyb = progress=$progress")
      }

      override fun update() {
        DialogUtils.showConfirmDialog(
                this@PosterActivity,
                R.string.tips,
                R.string.update_poster_hint,
                R.string.confirm, R.string.cancel
        ) { dialog, isPositiveBtn ->
          if (isPositiveBtn) {
            scan.scan()
            loading_container_bg?.visibility = View.VISIBLE
            loading_container?.visibility = View.VISIBLE
          }
        }
      }

    }, MyApplication.getInstance())
  }*/

  /*private fun initFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.statusBarColor = Color.TRANSPARENT
    }
    val statueBarHeight = UIUtils.getStatueBarHeight(MyApplication.getContext())
    val layoutParams = layout_title.layoutParams
    layoutParams.height = layoutParams.height + statueBarHeight
    layout_title.layoutParams = layoutParams
  }*/

  /*fun onClickDownloadFiles(selectedList: List<OneOSFile>){
    if (!net.sdvn.nascommon.utils.Utils.isWifiAvailable(MyApplication.getContext())
            && SPHelper.get(net.sdvn.nascommon.constant.AppConstants.SP_FIELD_ONLY_WIFI_CARE, true))
    {
      DialogUtils.showConfirmDialog(
              MyApplication.getContext(),
              R.string.tips,
              R.string.confirm_download_not_wifi,
              R.string.confirm, R.string.cancel
      ) { dialog, isPositiveBtn ->
        if (isPositiveBtn) {
          mLoginSession?.id?.let {
            downloadFiles(selectedList, it)
          }
        }
      }
    } else
    {
      mLoginSession?.id?.let {
        downloadFiles(selectedList, it)
      }
    }
  }*/

  /*private fun downloadFiles(selectedList: List<OneOSFile>, id: String) {
    PermissionChecker.checkPermission(
            MyApplication.getContext(),
            Callback {
              val service = SessionManager.getInstance().service
              service?.addDownloadTasks(selectedList, id, null)
            },
            Callback { UiUtils.showStorageSettings(MyApplication.getContext()) },
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
  }*/

}