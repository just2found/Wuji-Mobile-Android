package net.linkmate.app.ui.nas.files.photo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.constant.Type
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.huantansheng.easyphotos.ui.adapter.AlbumItemsAdapter
import com.huantansheng.easyphotos.utils.color.ColorUtils
import com.huantansheng.easyphotos.utils.system.SystemUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.XPopupImageLoader
import kotlinx.android.synthetic.main.activity_nas_photos.*
import libs.source.common.livedata.Status
import libs.source.common.utils.UriUtils
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.files.configbrief.ConfigBriefCheckActivity
import net.linkmate.app.ui.nas.images.ImageViewType
import net.linkmate.app.ui.simplestyle.dynamic.video.DynamicVideoImagePopup
import net.linkmate.app.util.GlideEngine
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import java.io.File
import kotlinx.android.synthetic.main.activity_nas_photos.frame_progress as flProgress
import kotlinx.android.synthetic.main.activity_nas_photos.iv_back as ivBack
import kotlinx.android.synthetic.main.activity_nas_photos.m_bottom_bar as mBottomBar
import kotlinx.android.synthetic.main.activity_nas_photos.root_view_album_items as rootViewAlbumItems
import kotlinx.android.synthetic.main.activity_nas_photos.rv_album_items as rvAlbumItems
import kotlinx.android.synthetic.main.activity_nas_photos.rv_photos as rvPhotos
import kotlinx.android.synthetic.main.activity_nas_photos.tv_album_items as tvAlbumItems
import kotlinx.android.synthetic.main.activity_nas_photos.tv_done as tvDone
import kotlinx.android.synthetic.main.activity_nas_photos.tv_preview as tvPreview
import kotlinx.android.synthetic.main.activity_nas_photos.tv_progress_message as tvErrorMessage
import kotlinx.android.synthetic.main.activity_nas_photos.fab_camera as ivCamera

/**
 * @author Raleigh.Luo
 * date：21/5/6 14
 * describe：
 */
class NasPhotosActivity : AppCompatActivity() {
    companion object {
        const val IS_SINGLE = "is_single"//是否是单选
        const val MAX_COUNT = "max_count"//最大选择数量
        const val IS_DISPLAY_ALBUM = "isDisplayAlbum"//是否显示相册

        /*****--功能------------------------------------------------------*******/
        const val FUNCTION_FOR = "function_for"
        const val FUNCTION_TYPE = "function_type"

        /*****--nas 远程文件------------------------------------------------------*******/
        const val NAS_REMOTE_FILE_PATH = "nas_remote_file_path"//
        const val LOCAL_FILE_PATH = "local_file_path"//

        /*****--nas 远程文件 非简介，返参 list<OneOSFile>------------------------------------------------------*******/
        const val NAS_REMOTE_FILE_PATHS = "nas_remote_file_paths"//

        @JvmStatic
        fun startConfig(activity: Activity, deviceId: String, For: String, type: Int, requestCode: Int) {
            if (!CMAPI.getInstance().isEstablished) {
                ToastUtils.showToast(R.string.network_not_available)
                return
            }
            //配置头像／背景
            activity.startActivityForResult(Intent(activity, NasPhotosActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
                    .putExtra(FUNCTION_FOR, For)
                    .putExtra(FUNCTION_TYPE, type)
                    .putExtra(IS_SINGLE, true)
                    .putExtra(MAX_COUNT, 1), requestCode)
        }

        /**
         * 多选/单选 nas设备图片库
         * @param isDisplayAlbum 是否显示本地相册
         * @param isSingle 是否是单选
         * @param maxCount 最大数量
         */
        @JvmStatic
        fun start(activity: Activity, deviceId: String, isSingle: Boolean, maxCount: Int, requestCode: Int, isDisplayAlbum: Boolean = true, For: String = "Default") {
            if (!CMAPI.getInstance().isEstablished) {
                ToastUtils.showToast(R.string.network_not_available)
                return
            }
            activity.startActivityForResult(Intent(activity, NasPhotosActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
                    .putExtra(FUNCTION_FOR, For)
                    .putExtra(IS_DISPLAY_ALBUM, isDisplayAlbum)
                    .putExtra(IS_SINGLE, isSingle)
                    .putExtra(MAX_COUNT, maxCount), requestCode)
        }

    }

    private var year: Long? = null
    private lateinit var deviceId: String
    private val viewModel by viewModels<NasPhotosViewModel> { NasAndroidViewModel.ViewModeFactory(application, getDevId()) }

    private fun getDevId(): String {
        return deviceId
    }


    private lateinit var adapter: NasPhotosAdapter
    private var mMAX_COUNT = 1
    private var isSingle: Boolean = true

    //功能
    private var mFunctionFor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nas_photos)
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        if (intent.hasExtra(FUNCTION_FOR)) mFunctionFor = intent.getStringExtra(NasPhotosActivity.FUNCTION_FOR)
        isSingle = intent.getBooleanExtra(IS_SINGLE, true)
        mMAX_COUNT = intent.getIntExtra(MAX_COUNT, 1)
        hideActionBar()
        adaptationStatusBar()
        initView()
        initAlbumItems()
        initOberser()
        startInitRequest()
    }


    private fun initView() {
        //是否显示本地相册图标
        val isDisplayAlbum = intent.getBooleanExtra(IS_DISPLAY_ALBUM, true)

        adapter = NasPhotosAdapter(this, viewModel, photoClickListener, isSingle, mMAX_COUNT)

        //是否显示本地相册
        ivCamera.visibility = if (isDisplayAlbum) View.VISIBLE else View.GONE
        val columns = resources.getInteger(R.integer.photos_columns_easy_photos)
        rvPhotos.layoutManager = GridLayoutManager(this, columns)
        rvPhotos.adapter = adapter
        adapter.setOnLoadMoreListener(object : BaseQuickAdapter.RequestLoadMoreListener {
            override fun onLoadMoreRequested() {
                viewModel.loadImageMore(getDevId(), ImageViewType.DAY, year)
            }

        }, rvPhotos)
        //返回
        ivBack.setOnClickListener {
            finish()
        }
        //加载出错，重试
        tvErrorMessage.setOnClickListener {
            startInitRequest()
        }
        //预览
        tvPreview.setOnClickListener {
            if (adapter.getSelectedPhotos().size > 0) {
                previewPhotos(0, false)
            }
        }
        //完成
        tvDone.setOnClickListener {
            if (!isSingle || this.rootViewAlbumItems.visibility == View.GONE) {//底部菜单面板不可见时，确认
                val photos = adapter.getSelectedPhotos()
                if (photos.size > 0) {
                    if (mFunctionFor == BriefRepo.FOR_DEVICE || mFunctionFor == BriefRepo.FOR_CIRCLE) {
                        toConfigBrief(photos.get(0))
                        //清除记录
                        adapter.clearSelectedPhotos()
                    } else {
                        val intent = Intent()
                        intent.putExtra(NAS_REMOTE_FILE_PATHS, adapter.getSelectedPhotos())
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }
        //本地相册
        ivCamera.setOnClickListener {
            startAlbum(this@NasPhotosActivity, object : SelectCallback() {
                override fun onResult(photos: ArrayList<Photo>?, paths: ArrayList<String>?, isOriginal: Boolean) {
                    photos?.let {
                        if (it.size > 0) {//去剪裁
                            val path = it.get(0).path

                            val uri = UriUtils.uri2File(MyApplication.getContext(), Uri.parse(path))
                            val photoAbsolutePath = if (uri == null) path else uri.absolutePath
                            ConfigBriefCheckActivity.startLocal(this@NasPhotosActivity,
                                    deviceId, intent.getStringExtra(FUNCTION_FOR),
                                    intent.getIntExtra(FUNCTION_TYPE, 0), CONFIG_BRIEF_REQUESTCODE,
                                    photoAbsolutePath)
                        }
                    }
                }
            })
        }
    }

    private val CONFIG_BRIEF_REQUESTCODE = 1

    /**
     * 启动配置简介页面
     */
    private fun toConfigBrief(photo: OneOSFile) {
        if (!CMAPI.getInstance().isEstablished) {
            ToastUtils.showToast(R.string.network_not_available)
            return
        }
        ConfigBriefCheckActivity.start(this,
                intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
                , intent.getStringExtra(NasPhotosActivity.FUNCTION_FOR)
                , intent.getIntExtra(NasPhotosActivity.FUNCTION_TYPE, 0)
                , CONFIG_BRIEF_REQUESTCODE, photo.getAllPath())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONFIG_BRIEF_REQUESTCODE && resultCode == Activity.RESULT_OK) {
            //简介配置成功，返回
            setResult(resultCode)
            finish()
        }
    }

    private val photoClickListener = object : NasPhotosAdapter.OnPhotoClickListener {
        override fun onPhotoClick(position: Int) {//全部预览
            previewPhotos(position, true)
        }

        override fun onSelectorChanged() {
            if (isSingle) {
                tvDone.performClick()
            } else {
                shouldShowMenuDone()
            }
        }

        override fun onSelectError() {
            Toast.makeText(this@NasPhotosActivity, String.format("%s%s", getString(R.string.selector_reach_max_hint_easy_photos), mMAX_COUNT), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 启动本地相册
     */
    private fun startAlbum(activity: Activity, callback: SelectCallback) {
        if (isSingle) {//单选
            EasyPhotos.createAlbum(activity as FragmentActivity, true, GlideEngine.INSTANCE())
                    .filter(Type.IMAGE)//只显示图片
                    .setGif(true)
                    .enableSingleCheckedBack(true)
                    .setCleanMenu(false)//不显示清空按钮
                    .setPuzzleMenu(false)//不显示拼图按钮,不调用setOriginalMenu不显示原图按钮
                    .start(callback)
        } else {//多选
            EasyPhotos.createAlbum(activity as FragmentActivity, true, GlideEngine.INSTANCE())
                    .filter(Type.IMAGE)//只显示图片
                    .setGif(true)
                    .setCount(mMAX_COUNT)
                    .setCleanMenu(false)//不显示清空按钮
                    .setPuzzleMenu(false)//不显示拼图按钮,不调用setOriginalMenu不显示原图按钮
                    .start(callback)
        }


    }

    private fun shouldShowMenuDone() {
        if (adapter.getSelectedPhotos().size == 0) {
            if (View.VISIBLE == tvDone.visibility) {
                val scaleHide = ScaleAnimation(1f, 0f, 1f, 0f)
                scaleHide.duration = 200
                tvDone.startAnimation(scaleHide)
            }
            tvDone.visibility = View.INVISIBLE
            tvPreview.setVisibility(View.INVISIBLE)
            tvDone.text = getString(R.string.selector_action_done_easy_photos, adapter.getSelectedPhotos().size, mMAX_COUNT)
        } else {
            if (View.INVISIBLE == tvDone.visibility) {
                val scaleShow = ScaleAnimation(0f, 1f, 0f, 1f)
                scaleShow.duration = 200
                tvDone.startAnimation(scaleShow)
            }
            tvDone.visibility = View.VISIBLE
            tvPreview.setVisibility(View.VISIBLE)
            tvDone.text = getString(R.string.selector_action_done_easy_photos, adapter.getSelectedPhotos().size, mMAX_COUNT)
        }
    }

    /**
     * 预览图片
     * @param isAll 是否是预览所有图片，否则为预览选中图片（预览按钮）
     */
    private fun previewPhotos(position: Int, isAll: Boolean) {
        val placeholder = R.drawable.bg_image_placeholder
        val preview = { loginSession: LoginSession ->
            val customImagePopup = DynamicVideoImagePopup(this)
            XPopup.Builder(this).asCustom(customImagePopup
                    .setSrcView(null, position)
                    .setImageUrls((if (isAll) adapter.data else adapter.getSelectedPhotos()) as List<Any>?)
                    .isInfinite(false)
                    .isShowPlaceholder(false)
                    .setPlaceholderColor(-1)
                    .setPlaceholderStrokeColor(-1)
                    .setPlaceholderRadius(-1)
                    .isShowSaveButton(false)
                    .setSrcViewUpdateListener({ popupView, position ->
                        null
                    })
                    .setXPopupImageLoader(object : XPopupImageLoader {
                        override fun loadImage(position: Int, uri: Any, image: ImageView) {
                            customImagePopup.mLoadingListener.loading()
                            val file = uri as OneOSFile
                            val url = OneOSAPIs.genDownloadUrl(loginSession, file.getAllPath())
                            var requestBuidler = Glide.with(image)
                                    .load(url)
                            requestBuidler = requestBuidler.placeholder(placeholder)
                            requestBuidler
                                    .error(placeholder)
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            customImagePopup.mLoadingListener.error()
                                            return false
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            customImagePopup.mLoadingListener.success()
                                            return false
                                        }

                                    })
                                    .into(image)
                        }

                        override fun getImageFile(context: Context, uri: Any): File? {
                            return null
                        }

                    })
            ).show()
        }
        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                loginSession?.let {
                    preview(it)
                }
            }
        })
    }

    private fun initOberser() {
        viewModel.liveData.observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                it.data?.let {
                    adapter.addData(it)
                }
                if (adapter.data.size == 0) {//空数据
                    showProgress(false, getString(R.string.no_data))
                } else {
                    showProgress(false)
                }
                if (viewModel.getPagesModel().hasMorePage() == false) {//关闭加载更多
                    adapter.loadMoreEnd()
                }
            } else if (it.status == Status.ERROR) {
                if (adapter.data.size == 0) {//空数据 //添加自定义相册项
                    showProgress(false, getString(R.string.brvah_load_failed))
                } else {
                    showProgress(false)
                }
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.liveDataSummary.observe(this, Observer {
            mNasItemsAdapter.notifyDataSetChanged()
        })
    }

    private fun showProgress(show: Boolean, msg: String? = null) {
        runOnUiThread {
            if (show) {
                flProgress.visibility = View.VISIBLE
                mProgressBar.visibility = View.VISIBLE
                msg?.let {
                    tvErrorMessage.text = msg
                    tvErrorMessage.visibility = View.VISIBLE
                    true
                } ?: let {
                    tvErrorMessage.visibility = View.GONE
                }
            } else {
                msg?.let {
                    flProgress.visibility = View.VISIBLE
                    mProgressBar.visibility = View.GONE
                    tvErrorMessage.text = msg
                    tvErrorMessage.visibility = View.VISIBLE
                    true
                } ?: let {
                    flProgress.setOnClickListener(null)
                    flProgress.visibility = View.GONE
                }
            }
        }
    }

    private fun hideActionBar() {
        val actionBar = supportActionBar
        actionBar?.hide()
    }

    private fun adaptationStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var statusColor = window.statusBarColor
            if (statusColor == Color.TRANSPARENT) {
                statusColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
            }
            if (ColorUtils.isWhiteColor(statusColor)) {
                SystemUtils.getInstance().setStatusDark(this, true)
            }
        }
    }

    private var setHide: AnimatorSet? = null
    private var setShow: AnimatorSet? = null
    private lateinit var mNasItemsAdapter: NasItemsAdapter

    private fun startInitRequest() {
        adapter.setNewData(null)
        if (!CMAPI.getInstance().isEstablished) {
            ToastUtils.showToast(R.string.network_not_available)
            showProgress(false, getString(R.string.brvah_load_failed))
            return
        }
        //新页面，开始加载
        showProgress(true)
        viewModel.loadPhotosTimeline(deviceId, ImageViewType.DAY, year = year)
        if (viewModel.liveDataSummary.value == null || viewModel.liveDataSummary.value?.status == Status.ERROR) {
            //分类未请求成功，请求分类
            viewModel.loadPhotosTimelineSummary(deviceId)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAlbumItems() {
        this.mNasItemsAdapter = NasItemsAdapter(this, viewModel, AlbumItemsAdapter.OnClickListener { position, i2 ->
            showAlbumItems(false)
            year = mNasItemsAdapter.getYear(position)
            this.tvAlbumItems.setText(mNasItemsAdapter.getTitle(position))
            startInitRequest()
        })
        tvAlbumItems.setOnClickListener {//左下角菜单
            showAlbumItems(8 == this.rootViewAlbumItems.visibility)
        }
        iv_album_items.setOnClickListener {
            showAlbumItems(8 == this.rootViewAlbumItems.visibility)
        }
        this.rvAlbumItems.setLayoutManager(LinearLayoutManager(this))
        this.rvAlbumItems.setAdapter(this.mNasItemsAdapter)
        this.rvAlbumItems.setOnTouchListener(object : OnTouchListener {
            var lastY = 0f
            var canClose = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    1 -> if (canClose) {
                        canClose = false
                        this@NasPhotosActivity.showAlbumItems(false)
                        return true
                    }
                    2 -> {
                        val curY = event.y
                        if (!this@NasPhotosActivity.rvAlbumItems.canScrollVertically(-1)) {
                            val dy = if (lastY == 0.0f) 0.0f else curY - lastY
                            if (dy > ViewConfiguration.get(this@NasPhotosActivity).scaledTouchSlop.toFloat()) {
                                lastY = 0.0f
                                canClose = true
                            }
                        } else {
                            canClose = false
                        }
                        lastY = curY
                    }
                    else -> {
                        lastY = 0.0f
                        canClose = false
                    }
                }
                return false
            }
        })
    }


    private fun showAlbumItems(isShow: Boolean) {
        if (null == this.setShow) {
            this.newAnimators()
        }
        if (isShow) {
            this.rootViewAlbumItems.setVisibility(View.VISIBLE)
            this.setShow?.start()
        } else {
            this.setHide?.start()
        }
    }

    private fun newAnimators() {
        newHideAnim()
        newShowAnim()
    }

    private fun newShowAnim() {
        val translationShow = ObjectAnimator.ofFloat(this.rvAlbumItems, "translationY", *floatArrayOf(this.mBottomBar.getTop().toFloat(), 0.0f))
        val alphaShow = ObjectAnimator.ofFloat(this.rootViewAlbumItems, "alpha", *floatArrayOf(0.0f, 1.0f))
        translationShow.duration = 300L
        this.setShow = AnimatorSet()
        this.setShow?.setInterpolator(AccelerateDecelerateInterpolator())
        this.setShow?.play(translationShow)?.with(alphaShow)
    }

    private fun newHideAnim() {
        val translationHide = ObjectAnimator.ofFloat(this.rvAlbumItems, "translationY", *floatArrayOf(0.0f, this.mBottomBar.getTop().toFloat()))
        val alphaHide = ObjectAnimator.ofFloat(this.rootViewAlbumItems, "alpha", *floatArrayOf(1.0f, 0.0f))
        translationHide.duration = 300L
        this.setHide = AnimatorSet()
        this.setHide?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                this@NasPhotosActivity.rootViewAlbumItems.setVisibility(View.GONE)
            }
        })
        this.setHide?.setInterpolator(AccelerateInterpolator())
        this.setHide?.play(translationHide)?.with(alphaHide)
    }
}