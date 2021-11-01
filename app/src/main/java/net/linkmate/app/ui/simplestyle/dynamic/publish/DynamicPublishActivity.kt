package net.linkmate.app.ui.simplestyle.dynamic.publish

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.arch.core.util.Function
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.constant.Key
import com.huantansheng.easyphotos.constant.Type
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.XPopupCallback
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.activity_dynamic_publish_simplestyle.*
import kotlinx.android.synthetic.main.include_dynamic_publish_picture_simplestyle.*
import kotlinx.android.synthetic.main.include_dynamic_publish_video_simplestyle.*
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.util.GlideEngine
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.TipsBar
import net.linkmate.app.view.dynamicRefresh.PublishProgressPopup
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.DynamicDBHelper
import net.sdvn.common.vo.Dynamic
import java.util.*
import kotlinx.android.synthetic.main.activity_dynamic_publish_simplestyle.tipsBar as mTipsBar


/** 动态发布
 * @author Raleigh.Luo
 * date：20/12/21 10
 * describe：
 */
class DynamicPublishActivity : BaseActivity() {
    private lateinit var adapter: DynamicPublishAdapter
    private val DEFAULT_MAX_PHOTO_COUNT = 9 //默认图片最大张数
    private val DEFAULT_MAX_VIDEO_COUNT = 1 //默认视频最大张数
    private val MEDIA_TOTAL_COUNT = 9 //视频＋图片不能超过9
    private var MAX_PHOTO_COUNT = DEFAULT_MAX_PHOTO_COUNT
    private var MAX_VIDEO_COUNT = DEFAULT_MAX_VIDEO_COUNT

    private val viewModel: DynamicPublishViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_publish_simplestyle)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0)
        }
        initNoStatusBar()
        showProgress(false)
        viewModel.init(intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID), intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_IP))
        initView()
        initObserver()
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

    /**
     * @param isCurrentPublished 是否当前正在发布的
     */
    private fun showProgress(isCurrentPublished: Boolean) {
        if (DynamicQueue.getUploadIdentification()?.isCurrentCircle() ?: false) {//有正在上传的动态，显示进度弹框
            val dynamic = DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()?.boxFor(Dynamic::class.java)?.get(DynamicQueue.getUploadIdentification()?.autoIncreaseId
                    ?: 0L)
            dynamic?.let {
                val customPopup = PublishProgressPopup(this, dynamic, isCurrentPublished)
                XPopup.Builder(this)
                        .enableDrag(false)
                        .dismissOnTouchOutside(false)
                        .dismissOnBackPressed(true)
                        .setPopupCallback(object : XPopupCallback {
                            override fun onBackPressed(): Boolean {
                                finish()
                                return false
                            }

                            override fun onDismiss() {
                            }

                            override fun beforeShow() {
                            }

                            override fun onCreated() {
                            }

                            override fun onShow() {
                            }
                        })
                        .asCustom(customPopup)
                        .show()
                true
            }
        }
    }

    private fun initView() {
        tvVideoTitle.setOnTouchListener { view, motionEvent ->
            onClick(view)
            true
        }
        tvPictureTitle.setOnTouchListener { view, motionEvent ->
            onClick(view)
            true
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
        tvVideoTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_video), 0, MAX_VIDEO_COUNT))
        tvPictureTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_pic), 0, MAX_PHOTO_COUNT))

        pictureRecyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = DynamicPublishAdapter(this, viewModel)
        pictureRecyclerView.adapter = adapter
        etContent.addTextChangedListener {//显示剩余文字大小
            tvNumber.setText(String.format("%s/%s", etContent.text.length, 500))
            checkPublishEnable()
        }
        //处理输入框可滑动
        etContent.setOnTouchListener { view, motionEvent ->
            if (view.getId() === R.id.etContent) {
                view.getParent().requestDisallowInterceptTouchEvent(true)
                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> view.getParent().requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    /**
     * 检查是否可以发布
     */
    private fun checkPublishEnable() {
        if (etContent.text.trim().length == 0 && viewModel.selectedVideo.value == null
                && (viewModel.selectedPictures.value == null || (viewModel.selectedPictures.value?.size
                        ?: 0) == 0)) {
            tvPublish.isEnabled = false
        } else {
            tvPublish.isEnabled = true
        }
    }

    /**
     * 更新图片最大数量
     */
    private fun updatePhotoMaxCountAfterSelectedVideo() {
        val currentVideoSize = if (viewModel.selectedVideo.value == null) 0 else 1
        val currentPhotoSize = viewModel.selectedPictures.value?.size ?: 0
        MAX_PHOTO_COUNT = Math.min(MEDIA_TOTAL_COUNT - currentVideoSize, DEFAULT_MAX_PHOTO_COUNT)
        //显示UI
        tvPictureTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_pic), currentPhotoSize, MAX_PHOTO_COUNT))
        btnPictureAdd.visibility = if (currentPhotoSize == MAX_PHOTO_COUNT) View.GONE else View.VISIBLE
    }

    /**
     * 更新视频最大数量
     */
    private fun updateVideoMaxCountAfterSelectedPhoto() {
        val currentPhotoSize = viewModel.selectedPictures.value?.size ?: 0
        val currentVideoSize = if (viewModel.selectedVideo.value == null) 0 else 1
        MAX_VIDEO_COUNT = Math.min(MEDIA_TOTAL_COUNT - currentPhotoSize, DEFAULT_MAX_VIDEO_COUNT)
        //显示UI
        tvVideoTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_video), currentVideoSize, MAX_VIDEO_COUNT))
        btnVideoAdd.visibility = if (currentVideoSize == MAX_VIDEO_COUNT) View.GONE else View.VISIBLE
    }


    private fun initObserver() {
        viewModel.selectedVideo.observe(this, Observer {
            //操作时，取消输入框焦点
            etContent.clearFocus()
            checkPublishEnable()
            updatePhotoMaxCountAfterSelectedVideo()
            it?.let {//有选中视频，加载显示 不显示添加按钮
                tvVideoTitle.isEnabled = true
                tvVideoTitle.isChecked = true
                mGroupVideo.visibility = View.VISIBLE
                btnVideoAdd.visibility = View.GONE
                //加载
                GlideEngine.INSTANCE().loadPhoto(this, it.path, ivVideo)
                tvVideoTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_video), 1, MAX_VIDEO_COUNT))
            } ?: let {//未选中视频
                tvVideoTitle.isChecked = false
                tvVideoTitle.isEnabled = false
                mGroupVideo.visibility = View.GONE
                btnVideoAdd.visibility = View.VISIBLE
                tvVideoTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_video), 0, MAX_VIDEO_COUNT))
            }
        })

        viewModel.selectedPictures.observe(this, Observer {
            //操作时，取消输入框焦点
            etContent.clearFocus()
            checkPublishEnable()
            updateVideoMaxCountAfterSelectedPhoto()
            it?.let {
                //触发标题隐藏后 恢复
                tvPictureTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_pic), it.size, MAX_PHOTO_COUNT))
                btnPictureAdd.visibility = if (it.size < MAX_PHOTO_COUNT) View.VISIBLE else View.GONE
                if (it.size > 0) {
                    pictureRecyclerView.visibility = View.VISIBLE
                    tvPictureTitle.isEnabled = true
                    tvPictureTitle.isChecked = true
                } else {
                    pictureRecyclerView.visibility = View.GONE
                    tvPictureTitle.isChecked = false
                    tvPictureTitle.isEnabled = false
                }
            } ?: let {
                tvPictureTitle.setText(String.format("%s %s/%s", getString(R.string.file_type_pic), 0, MAX_PHOTO_COUNT))
                pictureRecyclerView.visibility = View.GONE
                btnPictureAdd.visibility = View.VISIBLE
                tvPictureTitle.isChecked = false
                tvPictureTitle.isEnabled = false
            }
            adapter.notifyDataSetChanged()
        })

    }

    fun onClick(v: View) {
        if (Utils.isFastClick(v)) return
        when (v.id) {
            R.id.tvVideoTitle -> {//视频标题隐藏
                viewModel.selectedVideo.value?.let {
                    val isDisplaying = mGroupVideo.visibility == View.VISIBLE
                    mGroupVideo.visibility = if (isDisplaying) View.GONE else View.VISIBLE
                    tvVideoTitle.isChecked = !isDisplaying
//                    tvVideoTitle.setStartDrawable(getDrawable(if (isDisplaying) R.drawable.icon_right else R.drawable.icon_down))
                    true
                } ?: let {
                    tvVideoTitle.isChecked = false
                }
            }
            R.id.tvPictureTitle -> {//图片标题隐藏
                viewModel.selectedPictures.value?.let {
                    if (it.size > 0) {
                        val isDisplaying = pictureRecyclerView.visibility == View.VISIBLE
                        pictureRecyclerView.visibility = if (isDisplaying) View.GONE else View.VISIBLE
                        tvPictureTitle.isChecked = !isDisplaying
                    }
                    true
                } ?: let {
                    tvPictureTitle.isChecked = false
                }
            }
            R.id.ivVideo -> {//查看视频
                viewModel.selectedVideo.value?.let {
                    LocalPreviewActivity.start(this@DynamicPublishActivity, arrayListOf(it), 0,
                            viewModel.VIDEO_REQUEST_CODE)
                }
            }
            R.id.ivVideoDelete -> {//删除视频
                //移除当前选中视频
                viewModel.updateSelectedVideo(null)
            }
            R.id.btnPictureAdd -> {//添加图片
                EasyPhotos.createAlbum(this, true, GlideEngine.INSTANCE())
                        .setCount(MAX_PHOTO_COUNT - adapter.itemCount)
                        .filter(Type.IMAGE)//只显示图片
                        .setGif(true)
                        .setCleanMenu(false)//不显示清空按钮
                        .setPuzzleMenu(false)//不显示拼图按钮,不调用setOriginalMenu不显示原图按钮
                        .start(object : SelectCallback() {
                            //回调
                            override fun onResult(photos: ArrayList<Photo>?, paths: ArrayList<String>?, isOriginal: Boolean) {
                                photos?.let {
                                    if (it.size > 0) {
                                        photos.forEach {
                                            if (it.width == 0 || it.height == 0) {//重新读取图片高宽
                                                val options = BitmapFactory.Options()
                                                /**
                                                 * 最关键在此，把options.inJustDecodeBounds = true;
                                                 * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
                                                 */
                                                options.inJustDecodeBounds = true
                                                val bitmap = BitmapFactory.decodeFile(it.path, options) // 此时返回的bitmap为null
                                                /**
                                                 *options.outHeight为原始图片的高
                                                 */
                                                it.width = options.outWidth
                                                it.height = options.outHeight
                                            }
                                        }
                                        viewModel.addSelectedPictures(photos)
                                    }
                                }
                            }
                        })

            }
            R.id.btnVideoAdd -> {//添加视频
                EasyPhotos.createAlbum(this, true, GlideEngine.INSTANCE())
                        .filter(Type.VIDEO)//只显示视频，单选
                        .setCleanMenu(false)//不显示清空按钮
                        .setMaxFileSize(100 * 1024 * 1024)//100M
//                        .setVideoPreviewCallback()
                        .start(object : SelectCallback() {
                            //选择视频后回调
                            override fun onResult(photos: ArrayList<Photo>?, paths: ArrayList<String>?, isOriginal: Boolean) {
                                photos?.let {
                                    if (it.size > 0) {
                                        viewModel.updateSelectedVideo(photos.get(0))
                                    }
                                }
                            }
                        })
            }
            R.id.tvPublish -> {
                if (TextUtils.isEmpty(CMAPI.getInstance().baseInfo.netid)) {//没有网络
                    ToastUtils.showToast(R.string.tip_wait_for_service_connect)
                } else {
                    showLoading()
                    viewModel.publish(etContent.text.trim().toString(), Function {
                        dismissLoading()
                        setResult(Activity.RESULT_OK)
                        showProgress(true)
                        null
                    })
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                viewModel.PICTURE_REQUEST_CODE -> {
                    if (data?.hasExtra(Key.PREVIEW_EXTERNAL_PHOTOS) ?: false)
                        viewModel.updateSelectedPictures(data?.getSerializableExtra(Key.PREVIEW_EXTERNAL_PHOTOS) as ArrayList<Photo>)
                }
                viewModel.VIDEO_REQUEST_CODE -> {
                    viewModel.updateSelectedVideo(null)
                }
            }

        }
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
    }

    override fun getTopView(): View? {
        return null
    }

    override fun onResume() {
        DynamicQueue.isDynamicDisplayed = true
        super.onResume()
    }

    override fun onPause() {
        DynamicQueue.isDynamicDisplayed = false
        super.onPause()
    }
}