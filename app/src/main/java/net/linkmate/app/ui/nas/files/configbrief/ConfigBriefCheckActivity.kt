package net.linkmate.app.ui.nas.files.configbrief

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import com.lxj.xpopup.interfaces.XPopupCallback
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.CropImageView
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView.TransformImageListener
import io.objectbox.TxCallback
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.activity_nas_photos_check.*
import libs.source.common.utils.UriUtils
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.ui.nas.files.photo.NasPhotosActivity
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import java.io.File
import java.lang.reflect.InvocationTargetException

/**
 * @author Raleigh.Luo
 * date：21/5/7 15
 * describe：配置简介确认图片
 */
class ConfigBriefCheckActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        fun start(activity: Activity, deviceId: String, For: String, type: Int, requestCode: Int, path: String) {
            //头像比例框1:1正方形
            //背景比例框1:0.75
            val ratioX = if (type == BriefRepo.BACKGROUD_TYPE) 1f else 1f
            val ratioY = if (type == BriefRepo.BACKGROUD_TYPE) 0.75f else 1f
            //配置头像／背景
            activity.startActivityForResult(Intent(activity, ConfigBriefCheckActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
                    .putExtra(NasPhotosActivity.FUNCTION_FOR, For)
                    .putExtra(NasPhotosActivity.FUNCTION_TYPE, type)
                    .putExtra(NasPhotosActivity.NAS_REMOTE_FILE_PATH, path)
                    //裁剪框高宽比例
                    .putExtra(UCrop.EXTRA_ASPECT_RATIO_X, ratioX)
                    .putExtra(UCrop.EXTRA_ASPECT_RATIO_Y, ratioY), requestCode)
        }

        @JvmStatic
        fun startLocal(activity: Activity, deviceId: String, For: String, type: Int, requestCode: Int, path: String) {
            //头像比例框1:1正方形
            //背景比例框1:0.75
            val ratioX = if (type == BriefRepo.BACKGROUD_TYPE) 1f else 1f
            val ratioY = if (type == BriefRepo.BACKGROUD_TYPE) 0.75f else 1f
            //配置头像／背景
            activity.startActivityForResult(Intent(activity, ConfigBriefCheckActivity::class.java)
                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
                    .putExtra(NasPhotosActivity.FUNCTION_FOR, For)
                    .putExtra(NasPhotosActivity.FUNCTION_TYPE, type)
                    .putExtra(NasPhotosActivity.LOCAL_FILE_PATH, path)
                    //裁剪框高宽比例
                    .putExtra(UCrop.EXTRA_ASPECT_RATIO_X, ratioX)
                    .putExtra(UCrop.EXTRA_ASPECT_RATIO_Y, ratioY), requestCode)
        }
    }

    private lateinit var viewModel: ConfigBriefViewModel

    //nas 远程文件路径,以mNasRemoteFilePath优先
    private var mNasRemoteFilePath: String? = null
    private var mLocalFilePath: String? = null
    private lateinit var mGestureCropImageView: GestureCropImageView
    private lateinit var mOverlayView: OverlayView
    private lateinit var mLoadingPopup: LoadingPopupView


    private val mImageListener: TransformImageListener = object : TransformImageListener {
        override fun onRotate(currentAngle: Float) {
//            setAngleText(currentAngle)
        }

        override fun onScale(currentScale: Float) {
//            setScaleText(currentScale)
        }

        override fun onLoadComplete() {
            //加载成功
            frame_progress.visibility = View.GONE
            tvDone.visibility = View.VISIBLE
            mUCropView.animate().alpha(1f).setDuration(300).interpolator = AccelerateInterpolator()
            invalidateOptionsMenu()
        }

        override fun onLoadFailure(e: Exception) {//下载原图后，可能无法解析图片
            mUCropView.visibility = View.GONE
            if (isLoadThumbnailUrl != null && isLoadThumbnailUrl == false) {//刚加载过原图解析失败，再加载缩略图
                isLoadThumbnailUrl = true
                loadRemotePhoto()
            } else {//解析原图和缩略图都失败
                showError(R.string.the_picture_format_error)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nas_photos_check)
        init()
        initView()
        initOberser()
    }

    private fun showError(showHint: Int) {
        mProgressBar.visibility = View.GONE
        tvMessage.visibility = View.VISIBLE
        tvMessage.setText(showHint)
    }

    //是否加载的是缩略图
    private var isLoadThumbnailUrl: Boolean? = null

    /**
     * 加载原图
     */
    private fun loadRemotePhoto() {
        mUCropView.visibility = View.GONE
        frame_progress.visibility = View.VISIBLE
        mProgressBar.visibility = View.VISIBLE
        tvMessage.visibility = View.GONE
        SessionManager.getInstance().getLoginSession(viewModel.deviceId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                Observable.create(ObservableOnSubscribe<File> {
                    val url = if (isLoadThumbnailUrl
                                    ?: false) OneOSAPIs.genThumbnailUrl(loginSession, mNasRemoteFilePath) else OneOSAPIs.genDownloadUrl(loginSession, mNasRemoteFilePath)
                    //优先使用glide缓存原图文件,没有缓存则进行下载
                    val result = Glide.with(this@ConfigBriefCheckActivity).asFile().load(url).submit().get()
                            ?: Glide.with(this@ConfigBriefCheckActivity).downloadOnly().load(url)
                                    .submit().get()
                    if (result == null) {
                        it.onError(Throwable())
                    } else {
                        it.onNext(result)
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer<File> {
                            if (it != null) {
                                mLocalFilePath = it.absolutePath
                                loadLocalPhoto()
                            }
                        }, Consumer<Throwable> {
                            showError(if (isLoadThumbnailUrl
                                            ?: false) R.string.the_picture_format_error else R.string.brvah_load_failed)
                        })
            }
        })
    }

    private fun loadLocalPhoto() {
        try {
            //setImageUri前必须先显示CropView ，TransformImageListener才会有监听
            mUCropView.visibility = View.VISIBLE
            //保存文件夹，加载图片
            val outputDir = File(BriefManager.briefOutputDirPath)
            if (!outputDir.exists()) outputDir.mkdirs()
            val inputUri = Uri.fromFile(File(mLocalFilePath))
            mGestureCropImageView.setImageUri(inputUri, Uri.fromFile(File(outputDir, String.format("%s.jpg", System.currentTimeMillis()))))
        } catch (e: Exception) {
            mUCropView.visibility = View.GONE
            showError(R.string.the_picture_format_error)
        }

    }


    /**
     * 剪裁并保存图片
     */
    private fun cropAndSaveImage() {
        if (!CMAPI.getInstance().isEstablished) {
            ToastUtils.showToast(R.string.network_not_available)
            return
        }
        viewModel.isLoading(true)
        invalidateOptionsMenu()
        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, object : BitmapCropCallback {
            override fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
                val file = UriUtils.uri2File(MyApplication.getContext(), resultUri)
                val path = if (file == null) resultUri.path else file.absolutePath
                viewModel.startConfigBrief(path)
            }

            override fun onCropFailure(t: Throwable) {//剪裁失败，使用原图
                mLocalFilePath?.let {
                    viewModel.startConfigBrief(it)
                    true
                } ?: let {
                    viewModel.isLoading(false)
                }
            }
        })
    }

    private fun initView() {
        mLoadingPopup = XPopup.Builder(this)
                .setPopupCallback(object : XPopupCallback {
                    ////如果你自己想拦截返回按键事件，则重写这个方法，返回true即可,拦截的返回按键，按返回键XPopup不会被关闭
                    override fun onBackPressed(): Boolean {// 按返回键时，取消任务
                        viewModel.dispose()
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
                .dismissOnTouchOutside(false)
                .asLoading(getString(R.string.loading))
        mGestureCropImageView = mUCropView.getCropImageView()
        mOverlayView = mUCropView.getOverlayView()
        processOptions(intent)
        tvDone.setOnClickListener {//剪裁
            if (tvDone.visibility == View.VISIBLE)
                cropAndSaveImage()
        }
        ivBack.setOnClickListener {
            finish()
        }
        tvMessage.setOnClickListener {
            if (tvMessage.text.toString() == getString(R.string.brvah_load_failed)) {
                //加载失败重试
                if (TextUtils.isEmpty(mNasRemoteFilePath)) {
                    loadLocalPhoto()
                } else {
                    loadRemotePhoto()
                }
            }
        }
        tvTitle
        mGestureCropImageView.setTransformImageListener(mImageListener)
        if (TextUtils.isEmpty(mNasRemoteFilePath)) {
            loadLocalPhoto()
        } else {
            isLoadThumbnailUrl = false
            loadRemotePhoto()
        }

    }


    private fun initOberser() {
        viewModel.configResult.observe(this, Observer {
            it?.let {
                if (it.result) {//配置成功
                    val timeStamp = if (viewModel.type == BriefRepo.PORTRAIT_TYPE) it.data?.update_at?.avatar else it.data?.update_at?.bg
                    BriefRepo.insertAsync(viewModel.deviceId, viewModel.For, viewModel.type, viewModel.startConfig.value!!, timeStamp, TxCallback { result, error ->
                        //添加到数据库完成，关闭页面，不论添加成功失败
                        runOnUiThread {
                            viewModel.isLoading(false)
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    })
                } else {
                    viewModel.isLoading(false)
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.error?.code ?: 0))
                }
                true
            } ?: let {
                viewModel.isLoading(false)
                ToastUtils.showToast(R.string.ec_exception)
            }
        })
        viewModel.isLoading.observe(this, Observer {
            if (it) {
                mLoadingPopup.show()
            } else {
                mLoadingPopup.dismiss()
            }
        })
    }

    private fun init() {
        mLocalFilePath = intent.getStringExtra(NasPhotosActivity.LOCAL_FILE_PATH)
        val deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        val function_for = intent.getStringExtra(NasPhotosActivity.FUNCTION_FOR)
        val function_type = intent.getIntExtra(NasPhotosActivity.FUNCTION_TYPE, 0)
        mNasRemoteFilePath = intent.getStringExtra(NasPhotosActivity.NAS_REMOTE_FILE_PATH)
        //必须有路径
        if (TextUtils.isEmpty(mNasRemoteFilePath) && TextUtils.isEmpty(mLocalFilePath)) finish()

        viewModel = viewModels<ConfigBriefViewModel>({
            object : ViewModelProvider.AndroidViewModelFactory(MyApplication.getInstance()) {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    if (ConfigBriefViewModel::class.java.isAssignableFrom(modelClass)) {
                        return try {
                            modelClass.getConstructor(String::class.java, String::class.java, Int::class.java).newInstance(deviceId, function_for, function_type)
                        } catch (e: NoSuchMethodException) {
                            throw RuntimeException("Cannot create an instance of $modelClass", e)
                        } catch (e: IllegalAccessException) {
                            throw RuntimeException("Cannot create an instance of $modelClass", e)
                        } catch (e: InstantiationException) {
                            throw RuntimeException("Cannot create an instance of $modelClass", e)
                        } catch (e: InvocationTargetException) {
                            throw RuntimeException("Cannot create an instance of $modelClass", e)
                        }
                    }
                    return super.create(modelClass)
                }
            }
        }).value
    }

    private var mCompressQuality = 90
    private var mCompressFormat = Bitmap.CompressFormat.JPEG
    private fun processOptions(intent: Intent) {
        // Bitmap compression options
        val compressionFormatName = intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME)
        var compressFormat: CompressFormat? = null
        if (!TextUtils.isEmpty(compressionFormatName)) {
            mCompressFormat = CompressFormat.valueOf(compressionFormatName)
        }
        mCompressQuality = intent.getIntExtra(UCrop.Options.EXTRA_COMPRESSION_QUALITY, mCompressQuality)

        // Crop image view options
        mGestureCropImageView.maxBitmapSize = intent.getIntExtra(UCrop.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE)
        mGestureCropImageView.setMaxScaleMultiplier(intent.getFloatExtra(UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER))
        mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(intent.getIntExtra(UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION).toLong())

        // Overlay view options
        mOverlayView.setDimmedColor(intent.getIntExtra(UCrop.Options.EXTRA_DIMMED_LAYER_COLOR, resources.getColor(R.color.ucrop_color_default_dimmed)))
        //是否为暗显层中有一个圆
        mOverlayView.setCircleDimmedLayer(intent.getBooleanExtra(UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER, OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER))
        //是否显示剪裁框
        mOverlayView.setShowCropFrame(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME))
        mOverlayView.setCropFrameColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_COLOR, resources.getColor(R.color.ucrop_color_default_crop_frame)))
        //裁剪框线条
        mOverlayView.setCropFrameStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH, resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)))
        //是否显示网格线条
        mOverlayView.setShowCropGrid(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_GRID, false))
        mOverlayView.setCropGridRowCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT, OverlayView.DEFAULT_CROP_GRID_ROW_COUNT))
        mOverlayView.setCropGridColumnCount(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT, OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT))
        mOverlayView.setCropGridColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLOR, resources.getColor(R.color.ucrop_color_default_crop_grid)))
        mOverlayView.setCropGridStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH, resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width)))

        // Aspect ratio options  裁剪框高框比例，0f表示全局框裁剪
        val aspectRatioX = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_X, 0f)
        val aspectRatioY = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_Y, 0f)
        val aspectRationSelectedByDefault = intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0)
        if (aspectRatioX > 0 && aspectRatioY > 0) {
            mGestureCropImageView.targetAspectRatio = aspectRatioX / aspectRatioY
        } else {
            mGestureCropImageView.targetAspectRatio = CropImageView.SOURCE_IMAGE_ASPECT_RATIO
        }

        // Result bitmap max size options
        val maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0)
        val maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0)
        if (maxSizeX > 0 && maxSizeY > 0) {
            mGestureCropImageView.setMaxResultImageSizeX(maxSizeX)
            mGestureCropImageView.setMaxResultImageSizeY(maxSizeY)
        }
    }
}