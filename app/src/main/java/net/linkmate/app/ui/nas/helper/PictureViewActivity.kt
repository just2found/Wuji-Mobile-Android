package net.linkmate.app.ui.nas.helper

import android.Manifest
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.libimageloader.GenTagWorker
import io.weline.libimageloader.OnProgressListener
import io.weline.libimageloader.ProgressInterceptor
import io.weline.repo.files.data.SharePathType
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.view.TipsBar
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.glide.GlideCacheConfig
import net.sdvn.nascommon.model.oneos.*
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.StatusBarUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.preview.HackyViewPager
import org.greenrobot.eventbus.EventBus
import org.view.libwidget.CircleProgressView
import timber.log.Timber
import java.io.File
import java.io.Serializable
import java.util.*

@Route(path = "/nas/pic_view", group = "nas")
class PictureViewActivity : BaseActivity() {

    private val mPicList = ArrayList<OneOSFile>()
    private val mLocalPicList = ArrayList<File>()

    protected var mLoginSession: LoginSession? = null

    //    private OneOSFileManageAPI fileManageAPI;
    private var mCurTxt: TextView? = null
    private var mTotalTxt: TextView? = null
    private var toolbar: Toolbar? = null
    private var mManagePanel: FileManagePanel? = null
    protected lateinit var mSlideInAnim: Animation
    protected lateinit var mSlideOutAnim: Animation

    private var startIndex = 0
    private var curPositon = -1
    private var isLocalPicture = false
    private val onBackListener = OnClickListener { onBackPressed() }
    private var mFileType: OneOSFileType? = null
    private var mViewPager: ViewPager? = null
    private var mPagerAdapter :PictureViewActivity.HackyPagerAdapter?= null
    private var fileNamesDL: MutableList<File>? = null
    private var isShown: Boolean = false
    private var fullScreenMode = false
    private var groupId:Long =-1


    private val mFileManageListener = object : FileManagePanel.OnFileManageListener<OneOSFile> {
        override fun onClick(view: View, selectedList: List<OneOSFile>, action: FileManageAction) {
            if (action != FileManageAction.MORE &&
                    action != FileManageAction.BACK &&
                    !NetworkStateManager.instance.isNetAvailable()) {
                ToastHelper.showToast(R.string.network_not_available)
                return
            }
            val fileManage = OneOSFileManage(this@PictureViewActivity, null,
                    mLoginSession!!, toolbar, OneOSFileManage.OnManageCallback {
                //                    autoPullToRefresh();
                showManageBar(false)
                if (it && action == FileManageAction.DELETE || action == FileManageAction.MOVE) {
                    mViewPager?.let {
                        var currentItem = it.currentItem
                        mPicList.removeAt(currentItem)
                        if (currentItem > mPicList.size - 1) {
                            currentItem = mPicList.size - 1
                        }
                        if (mPicList.isEmpty()) {
                            finish()
                            return@let
                        }
                        setIndicatorTxt(currentItem + 1, mPicList.size)
                        mPagerAdapter?.setNewData(mPicList)
                        it.adapter?.notifyDataSetChanged()
                    }
                }
            })

            val oneOSFile = mPicList[mViewPager!!.currentItem]
            val oneOSFiles = ArrayList<OneOSFile>(1)
            oneOSFiles.add(oneOSFile)
            if (action == FileManageAction.MORE) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Manage More======")
                updateManageBar(mFileType, oneOSFiles, true, this)
            } else if (action == FileManageAction.BACK) {
                updateManageBar(mFileType, oneOSFiles, false, this)
            } else
            /*if (FileManageAction.DOWNLOAD.equals(action)) {
                final File destFile = new File(SessionManager.getInstance().getDefaultDownloadPath(), oneOSFile.getName());
                Disposable subscribe = Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                        emitter.onNext(FileUtils.copyFile(file, destFile, false));
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean success) {
                                if (success) {
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)));
                                    refreshDownloadedFile();
                                }
                            }
                        });
            } else */ {
                fileManage.manage(mFileType, action, oneOSFiles)
            }
        }

        override fun onDismiss() {}
    }

    fun getLayoutId(): Int {
        return R.layout.layout_viewer_picture
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppCompatThemePicture)
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        //        if (Build.VERSION.SDK_INT >= 21) {
        //            View decorView = getWindow().getDecorView();
        //            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        //                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        //            decorView.setSystemUiVisibility(option);
        //            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        //            getWindow().setStatusBarColor(Color.TRANSPARENT);
        //        }

        handleIntent(intent)
        initView()
        setupSystemUI()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
                showSystemUI()
            else
                hideSystemUI()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            finish()
            return
        }
        startIndex = intent.getIntExtra("StartIndex", 0)
        isLocalPicture = intent.getBooleanExtra("IsLocalPicture", false)
        groupId=intent.getLongExtra("groupId",-1)
        startIndex = if (startIndex >= 0) startIndex else 0
        val pic = intent.getStringExtra("PictureList")
        val pictureList = FileInfoHolder.getInstance().retrieve(pic)
        if (pictureList == null || !NetworkStateManager.instance.isEstablished()
                || deviceId.isNullOrEmpty()) {
            finish()
            return
        }
        FileInfoHolder.getInstance().remove(pic)
        if (isLocalPicture) {
            mLocalPicList.addAll((pictureList as List<File>))
        } else {
            mPicList.addAll((pictureList as List<OneOSFile>))
        }
        mFileType = intent.getSerializableExtra("FileType") as OneOSFileType


        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "---Start Index: $startIndex")

        SessionManager.getInstance().getLoginSession(deviceId!!, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mLoginSession = loginSession
            }
        })
        //        fileManageAPI = new OneOSFileManageAPI(this.mLoginSession);
        //        fileManageAPI.setOnFileManageListener(new OneOSFileManageAPI.OnFileManageListener() {
        //            @Override
        //            public void onStart(String url, FileManageAction action) {
        //            }
        //
        //            @Override
        //            public void onSuccess(String url, FileManageAction action, String response) {
        //            }
        //
        //            @Override
        //            public void onFailure(String url, FileManageAction action, int errorNo, String errorMsg) {
        //            }
        //        });
        fileNamesDL = ArrayList()
//        refreshDownloadedFile()
        mPagerAdapter?.setNewData(if (isLocalPicture) mLocalPicList else mPicList)
    }


    private fun initView() {
        mSlideInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top)
        mSlideOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top)
        toolbar = findViewById(R.id.layout_title)
        setSupportActionBar(toolbar)
        if (supportActionBar != null)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        mCurTxt = findViewById(R.id.text_index)
        mTotalTxt = findViewById(R.id.text_total)
        toolbar!!.visibility = View.VISIBLE
        val ivBtn = findViewById<ImageButton>(R.id.btn_back)
        ivBtn.setOnClickListener(onBackListener)
        val mBackTxt = findViewById<TextView>(R.id.txt_title_back)
        mBackTxt.setOnClickListener(onBackListener)
        mManagePanel = findViewById(R.id.layout_operate_bottom_panel)
        mManagePanel!!.setBackgroundColor(resources.getColor(R.color.translucent_black))

        if (if (isLocalPicture) mLocalPicList.size > 0 else mPicList.size > 0) {
            // GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(this, isLocalPicture ? mLocalPicList : mPicList, isLocalPicture, mLoginSession, httpBitmap);
            // GalleryViewPager mViewPager = (GalleryViewPager) this.findViewById(R.id.switch_viewer);

            val pagerAdapter = HackyPagerAdapter(this, isLocalPicture)
            pagerAdapter.setNewData(if (isLocalPicture) mLocalPicList else mPicList)
            mPagerAdapter = pagerAdapter
            mViewPager = findViewById<HackyViewPager>(R.id.switch_viewer)
            mViewPager!!.offscreenPageLimit = 1
            mViewPager!!.adapter = pagerAdapter
            mViewPager!!.pageMargin = 100
            mViewPager!!.currentItem = startIndex
            curPositon = startIndex
            setIndicatorTxt(startIndex + 1, if (isLocalPicture) mLocalPicList.size else mPicList.size)
            mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    curPositon = position
                    setIndicatorTxt(position + 1, if (isLocalPicture) mLocalPicList.size else mPicList.size)
                    if (mPicList != null) {
                        val oneOSFiles = ArrayList<OneOSFile>()
                        oneOSFiles.add(mPicList[position])
                        updateManageBar(mFileType, oneOSFiles, false, mFileManageListener)
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        } else {
            ToastHelper.showToast(R.string.app_exception)
            finish()
        }
    }

    //    private void hideTitle(final boolean toHide) {
    //        if (titleIsAnimating) {
    //            return;
    //        }
    ////        int statusBarOffsetPx = StatusBarUtils.getStatusBarOffsetPx(this);
    //        if (mTitleLayoutEnd == 0) {
    //            mTitleLayoutEnd = toolbar.getMeasuredHeight();
    ////            mTitleLayoutEnd += statusBarOffsetPx;
    ////            mTitleLayoutStart = statusBarOffsetPx;
    //        }
    ////        Logger.LOGE(TAG, "StatusBarOffsetPx : " + statusBarOffsetPx);
    //        Logger.LOGE(TAG, "getPaddingBottom: " + toolbar.getPaddingBottom());
    //        int start, end;
    //        if (toHide) {
    //            start = mTitleLayoutStart;
    //            end = -mTitleLayoutEnd;
    //        } else {
    //            start = -mTitleLayoutEnd;
    //            end = mTitleLayoutStart;
    //        }
    //        ValueAnimator animator = ValueAnimator.ofInt(start, end);
    //        animator.setDuration(200);
    //        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    //            @Override
    //            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
    //                int nowTop = (int) animation.getAnimatedValue();
    //                toolbar.setPadding(toolbar.getPaddingLeft(), nowTop, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
    //                Logger.LOGE(TAG, "getPaddingTop: " + toolbar.getPaddingTop());
    //            }
    //        });
    //        animator.addListener(new Animator.AnimatorListener() {
    //            @Override
    //            public void onAnimationStart(Animator animation) {
    //                toolbar.setVisibility(View.VISIBLE);
    //                titleIsAnimating = true;
    //            }
    //
    //            @Override
    //            public void onAnimationEnd(Animator animation) {
    //                titleIsHide = toHide;
    //                titleIsAnimating = false;
    //                if (toHide) {
    //                    toolbar.setVisibility(View.GONE);
    //                }
    //            }
    //
    //            @Override
    //            public void onAnimationCancel(Animator animation) {
    //
    //            }
    //
    //            @Override
    //            public void onAnimationRepeat(Animator animation) {
    //
    //            }
    //        });
    //        animator.start();
    //        toggleSystemUI();
    //    }

    private fun setIndicatorTxt(curIndex: Int, total: Int) {
        if (curIndex <= total) {
            mCurTxt!!.text = curIndex.toString()
            mTotalTxt!!.text = total.toString()
        }
    }

    override fun finish() {
        super.finish()
        if (curPositon != -1) {
            val serializable = if (isLocalPicture)
                mLocalPicList.getOrNull(curPositon)
            else
                mPicList.getOrNull(curPositon)
            if (serializable != null)
                EventBus.getDefault().postSticky(serializable)
        }
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out)
    }

    public override fun onDestroy() {
        super.onDestroy()
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "OnDestroy")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Logger.LOGE(TAG, "----On Low Memory---")
        Glide.get(this).clearMemory()
        Glide.get(applicationContext).trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
        System.gc()
    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    inner class HackyPagerAdapter(private val context: Context, isLocalPicture: Boolean = false) : PagerAdapter() {
        private var isLocalPic = false
        private var mPhotoWidth: Int = 0
        private var mPhotoHeight: Int = 0
        private val mList = mutableListOf<Any>()
        //        internal var weakHashMap = WeakHashMap<ProgressBar, Int>()
        internal val weakHashMap = WeakHashMap<Any, Disposable>()

        init {
            this.isLocalPic = isLocalPicture
        }

        override fun getCount(): Int {
            return mList.size
        }

        override fun getItemPosition(any: Any): Int {
            return if (mList.contains(any)) {
                mList.indexOf(any)
            } else {
                POSITION_NONE
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val view = View.inflate(this@PictureViewActivity, R.layout.picture_view, null)
            val mPhotoDraweeView = view.findViewById<PhotoView>(R.id.pdv)
            if (mPhotoWidth == 0 || mPhotoHeight == 0)
                mPhotoDraweeView.post {
                    mPhotoWidth = mPhotoDraweeView.measuredWidth
                    mPhotoHeight = mPhotoDraweeView.measuredHeight
                    Logger.LOGI(TAG, "{width: $mPhotoWidth,height: $mPhotoHeight}")
                }
            val tvOriginalImage = view.findViewById<TextView>(R.id.tv_original_image)
            container.addView(view, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT)
            val progressBar = view.findViewById<CircleProgressView>(R.id.circleProgressView)
            loadImage(view, progressBar, mPhotoDraweeView, tvOriginalImage, position, false)
            return view
        }

        private fun loadImage(itemView: View, progressBar: ProgressBar, mPhotoDraweeView: PhotoView, tvOriginalImage: TextView, position: Int, disableCache: Boolean) {

            mPhotoDraweeView.attacher.setOnPhotoTapListener { _, x, y ->
                if (mPhotoDraweeView.isEnabled) {
                    //                        hideTitle(!titleIsHide);
                    toggleSystemUI()
                    showManageBar(false)
                } else {
                    loadImage(itemView, progressBar, mPhotoDraweeView, tvOriginalImage, position, true)
                }
            }
            mPhotoDraweeView.setOnLongClickListener(View.OnLongClickListener {
                if (isLocalPic) {
                    false
                } else {
                    //                        ArrayList<OneOSFile> osFiles = new ArrayList<>();
                    //                        osFiles.add((OneOSFile) mList.get(position));
                    //                        share(osFiles);
                    if (!fullScreenMode) {
                        toggleSystemUI()
                    }

                    if (mPicList != null) {
                        val oneOSFiles = ArrayList<OneOSFile>()
                        oneOSFiles.add(mPicList[position])
                        updateManageBar(mFileType, oneOSFiles, false, mFileManageListener)
                        showManageBar(true)
                        return@OnLongClickListener true
                    }
                    false
                }
            })

            //限制范围，使图片在加载完成前不能滑动

            val uri: String
            var thumbUri: String? = null
            var hasLocalFile = false
            val size: Long
            val file = if (isLocalPic) {
                val file = mList[position] as File
                uri = file.absolutePath
                size = file.length()
                ViewCompat.setTransitionName(mPhotoDraweeView, VIEW_NAME_IMAGE + file.path)
                file
            } else {
                val file = mList[position] as OneOSFile
                val toPath = SessionManager.getInstance().getDefaultDownloadPathByID(deviceId, file)
                hasLocalFile = File(toPath, file.getName()).also {
                    if (it.exists()) {
                        file.localFile = it
                    }
                }.exists()
                val url = getUrl(file)
                val thumburl = getThumbnailurl(file)

                uri = if (hasLocalFile) {
                    file.localFile!!.absolutePath
                } else
                    url
                thumbUri = thumburl
                size = file.getSize()
                ViewCompat.setTransitionName(mPhotoDraweeView, VIEW_NAME_IMAGE + file.getPath())
                file
            }
            val finalThumbUri1 = thumbUri

            if (isLocalPic || hasLocalFile) {
                tvOriginalImage.visibility = View.GONE
                Glide.with(mPhotoDraweeView)
                        .load(uri)
                        .centerInside()
                        .into(mPhotoDraweeView)
            } else {
                tvOriginalImage.visibility = View.GONE
                Glide.with(mPhotoDraweeView)
                        .load(if (finalThumbUri1.isNullOrEmpty()) null else EliCacheGlideUrl(finalThumbUri1))
                        .error(net.sdvn.nascommonlib.R.drawable.error_picture)
                        .into(mPhotoDraweeView)
                weakHashMap.put(itemView, Observable.create(ObservableOnSubscribe<Result<File?>> { emitter ->
                    var cachedFile: File? = null
                    try {
                        cachedFile = Glide.with(context).downloadOnly()
                                .load(EliCacheGlideUrl(uri))
                                .apply(RequestOptions().onlyRetrieveFromCache(true))
                                .submit()
                                .get()
                        if (cachedFile != null && cachedFile.exists()) {
                            Logger.LOGD(TAG, "cachedFile: ", cachedFile.absolutePath)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    emitter.onNext(Result(cachedFile))
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnDispose {
                            tvOriginalImage.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                        }
                        .subscribe({ result ->
                            if (result.data?.exists() == true) {
                                Glide.with(mPhotoDraweeView)
                                        .load(EliCacheGlideUrl(uri))
                                        .centerInside()
                                        .error(net.sdvn.nascommonlib.R.drawable.error_picture)
                                        .into(mPhotoDraweeView)
                            } else {
                                tvOriginalImage.visibility = View.VISIBLE
                                tvOriginalImage.setOnClickListener(OnClickListener {
                                    if (!NetworkStateManager.instance.isNetAvailable()) {
                                        ToastHelper.showToast(R.string.network_not_available)
                                        return@OnClickListener
                                    }
                                    if (!NetworkStateManager.instance.isEstablished()) {
                                        ToastHelper.showToast(R.string.tip_wait_for_service_connect)
                                        return@OnClickListener
                                    }
                                    val url = getUrl(file = file as OneOSFile)
                                    val onProgressListener: OnProgressListener = object : OnProgressListener {
                                        override fun onProgress(tag: Any, bytesRead: Long, totalBytes: Long, isDone: Boolean, exception: GlideException?) {
                                            val progress = (bytesRead * 100f / totalBytes + 0.5f).toInt()
                                            if (progress > 0 || isDone) {
                                                progressBar.progress = progress
                                                progressBar.visibility = if (isDone) GONE else View.VISIBLE
                                            }
                                        }
                                    }
                                    val tag = GlideCacheConfig.getImageName(url)
                                    ProgressInterceptor.addListeners(tag, onProgressListener)
                                    ProgressInterceptor.setGenTagWorker(object : GenTagWorker {
                                        override fun getTagByUrl(url: String): Any {
                                            return GlideCacheConfig.getImageName(url)
                                        }
                                    })
                                    progressBar.tag = tag
//                                    weakHashMap[progressBar] = position
                                    Glide.with(mPhotoDraweeView)
                                            .load(EliCacheGlideUrl(url))
                                            .centerInside()
                                            .placeholder(mPhotoDraweeView.drawable)
                                            .addListener(object : RequestListener<Drawable?> {
                                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                                                    progressBar.visibility = GONE
                                                    ProgressInterceptor.removeListeners(url)
                                                    tvOriginalImage.visibility = View.VISIBLE
                                                    ToastHelper.showLongToast(R.string.load_failed)
                                                    return false
                                                }

                                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                                    progressBar.visibility = GONE
                                                    ProgressInterceptor.removeListeners(url)
                                                    return false
                                                }
                                            })
                                            .into(mPhotoDraweeView)
                                    tvOriginalImage.visibility = View.GONE
                                    progressBar.visibility = View.VISIBLE
                                })
                            }
                        }, { it.printStackTrace() }))
            }

        }

        override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
            weakHashMap.get(any)?.dispose()
            weakHashMap.remove(any)
            container.removeView(any as View)
        }

        override fun isViewFromObject(view: View, any: Any): Boolean {
            return view === any
        }

        fun setNewData(arrayList: ArrayList<out Serializable>) {
            this.mList.clear()
            mList.addAll(arrayList)
            notifyDataSetChanged()
        }
    }

    private fun getThumbnailurl(file: OneOSFile): String {
        return if (mLoginSession != null)
            if (file.share_path_type == SharePathType.SAFE_BOX.type ||file.share_path_type == SharePathType.GROUP.type) {
                OneOSAPIs.genThumbnailUrlV5(file.share_path_type, mLoginSession!!, file.getPath(),groupId,null)
            } else {
                OneOSAPIs.genThumbnailUrl(mLoginSession!!, file)
            }
        else
            ""
    }

    private fun getUrl(file: OneOSFile): String {
        return if (mLoginSession != null)
//            if (file.share_path_type == SharePathType.SAFE_BOX.type) {
//                OneOSAPIs.genDownloadUrlV5(file.share_path_type, mLoginSession!!, file.getPath())
//            } else {
                OneOSAPIs.genDownloadUrl(mLoginSession!!,groupId, file)
//            }
        else
            ""

    }

    /**
     * =====================================ManagePanel相关操作===========================================
     */

    fun showManageBar(isShown: Boolean) {
        this.isShown = isShown
        if (isShown) {
            mManagePanel!!.showPanel()
        } else {
            mManagePanel!!.hidePanel()
        }
    }

    fun updateManageBar(fileType: OneOSFileType?, selectedList: ArrayList<OneOSFile>, isMore: Boolean, mListener: FileManagePanel.OnFileManageListener<*>?) {
        mManagePanel!!.setOnOperateListener(mListener)
        if (isMore) {
            //            mManagePanel.updatePanelItemsMore(fileType, selectedList);
        } else {
            val oneOSFiles = ArrayList<DataFile>(selectedList)
            mManagePanel!!.updatePanelItems(fileType!!, oneOSFiles, mLoginSession)
        }
    }

    private fun refreshDownloadedFile() {
        if (!isDestroyed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    return
            }
            val path = SessionManager.getInstance().defaultDownloadPath
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                fileNamesDL!!.clear()
                for (file in dir.listFiles()) {
                    //遍历下载目录下的文件
                    if (file.isFile)
                        fileNamesDL!!.add(file)
                }
            }
        }

    }

    fun localContainsFile(localFiles: List<File>, file: OneOSFile): Boolean {
        for (localFile in localFiles) {
            if (localFile.exists() && localFile.name == file.getName() && localFile.length() == file.getSize()) {
                file.localFile = localFile
                return true
            }
        }
        return false

    }

    fun toggleSystemUI() {
        if (fullScreenMode)
            showSystemUI()
        else
            hideSystemUI()
    }

    private fun hideSystemUI() {
        runOnUiThread {
            toolbar!!.animate()
                    .translationY((-toolbar!!.height).toFloat()).setInterpolator(AccelerateInterpolator())
                    .setDuration(200)
                    .start()

            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                Timber.w(TAG, "ui changed: $visibility")
            }
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)

            fullScreenMode = true
            window.navigationBarColor = Color.TRANSPARENT
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setupSystemUI() {
        toolbar!!.animate()
                .translationY(StatusBarUtils.getStatusBarOffsetPx(toolbar!!.context).toFloat())
                .setInterpolator(DecelerateInterpolator())
                .setDuration(0)
                .start()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.navigationBarColor = resources.getColor(R.color.bg_picture_viewer_title)
        window.statusBarColor = resources.getColor(R.color.bg_picture_viewer_title)
    }

    private fun showSystemUI() {
        runOnUiThread {
            toolbar!!.animate()
                    .translationY(StatusBarUtils.getStatusBarOffsetPx(toolbar!!.context).toFloat())
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(240)
                    .start()

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            fullScreenMode = false
            window.navigationBarColor = resources.getColor(R.color.bg_picture_viewer_title)
            window.statusBarColor = resources.getColor(R.color.bg_picture_viewer_title)
        }
    }

    companion object {
        private val TAG = PictureViewActivity::class.java.simpleName

        // View name of the header image. Used for activity scene transitions
        val VIEW_NAME_IMAGE = "pic:image:"

        fun startActivity(context: Context, devId: String, position: Int, pic: String, fileType: OneOSFileType): Intent {
            val intent = Intent(context, PictureViewActivity::class.java)
            intent.putExtra("StartIndex", position)
            intent.putExtra("PictureList", pic)
            intent.putExtra("FileType", fileType)
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
            return intent
        }
    }

}

