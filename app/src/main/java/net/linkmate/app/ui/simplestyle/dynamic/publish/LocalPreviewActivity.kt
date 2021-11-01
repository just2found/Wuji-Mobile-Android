package net.linkmate.app.ui.simplestyle.dynamic.publish

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.huantansheng.easyphotos.constant.Key
import com.huantansheng.easyphotos.models.album.AlbumModel
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.huantansheng.easyphotos.result.Result
import com.huantansheng.easyphotos.setting.Setting
import com.huantansheng.easyphotos.ui.PreviewFragment.OnPreviewFragmentClickListener
import com.huantansheng.easyphotos.ui.adapter.PreviewPhotosAdapter
import com.huantansheng.easyphotos.ui.adapter.PreviewPhotosAdapter.PreviewPhotosViewHolder
import com.huantansheng.easyphotos.utils.color.ColorUtils
import com.huantansheng.easyphotos.utils.system.SystemUtils
import kotlinx.android.synthetic.main.activity_local_preview_easy_photos.*
import net.linkmate.app.R
import java.util.*

/**本地资源预览
 * @author Raleigh.Luo
 * date：20/12/22 10
 * describe：
 */

class LocalPreviewActivity : AppCompatActivity(), PreviewPhotosAdapter.OnClickListener, OnPreviewFragmentClickListener {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable { SystemUtils.getInstance().systemUiHide(this@LocalPreviewActivity, decorView) }
    private val mShowPart2Runnable = Runnable { // 延迟显示UI元素
        mToolBar!!.visibility = View.VISIBLE
    }
    private var mVisible = false
    var decorView: View? = null
    private lateinit var adapter: PreviewPhotosAdapter
    private lateinit var snapHelper: PagerSnapHelper
    private lateinit var lm: LinearLayoutManager
    private var index = 0
    private val photos = ArrayList<Photo>()
    private var resultCode = RESULT_CANCELED
    private var lastPosition = 0 //记录recyclerView最后一次角标位置，用于判断是否转换了item
    private var statusColor = 0
    private var hasExternalPhotos = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decorView = window.decorView
        SystemUtils.getInstance().systemUiInit(this, decorView)
        setContentView(R.layout.activity_local_preview_easy_photos)
        hideActionBar()
        adaptationStatusBar()
        hasExternalPhotos = intent.hasExtra(Key.PREVIEW_EXTERNAL_PHOTOS)
        if (hasExternalPhotos) {
            initExternalData()
        } else {
            if (null == AlbumModel.instance) {
                finish()
                return
            }
            initData()
        }
        initView()
    }

    private fun adaptationStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            statusColor = ContextCompat.getColor(this, R.color.easy_photos_status_bar)
            if (ColorUtils.isWhiteColor(statusColor)) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    private fun hideActionBar() {
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.hide()
        }
    }

    private fun initData() {
        val intent = intent
        val albumItemIndex = intent.getIntExtra(Key.PREVIEW_ALBUM_ITEM_INDEX, 0)
        photos.clear()
        if (albumItemIndex == -1) {
            photos.addAll(Result.photos)
        } else {
            photos.addAll(AlbumModel.instance.getCurrAlbumItemPhotos(albumItemIndex))
        }
        index = intent.getIntExtra(Key.PREVIEW_PHOTO_INDEX, 0)
        lastPosition = index
        mVisible = true
    }

    private fun initExternalData() {
        val intent = intent
        val photos = intent.getSerializableExtra(Key.PREVIEW_EXTERNAL_PHOTOS) as ArrayList<Photo>
        this.photos.clear()
        this.photos.addAll(photos)
        val isShow = getIntent().getBooleanExtra(Key.PREVIEW_EXTERNAL_PHOTOS_BOTTOM_PREVIEW, false)
        if (isShow) Result.photos = photos else Result.photos.clear()
        index = intent.getIntExtra(Key.PREVIEW_PHOTO_INDEX, 0)
        lastPosition = index
        mVisible = true
    }

    override fun onDestroy() {
        if (hasExternalPhotos) {
            Setting.clear()
        }
        super.onDestroy()
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        val hideAnimation = AlphaAnimation(1.0f, 0.0f)
        hideAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mToolBar!!.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        hideAnimation.duration = UI_ANIMATION_DELAY.toLong()
        mToolBar!!.startAnimation(hideAnimation)
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        SystemUtils.getInstance().systemUiShow(this, decorView)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.post(mShowPart2Runnable)
    }

    override fun onPhotoClick() {
        toggle()
    }

    override fun onPhotoScaleChanged() {
        if (mVisible) hide()
    }


    private fun initView() {
        if (!SystemUtils.getInstance().hasNavigationBar(this)) {
            val mRootView: FrameLayout = findViewById(R.id.m_root_view)
            mRootView.fitsSystemWindows = true
            mToolBar.setPadding(0, SystemUtils.getInstance().getStatusBarHeight(this), 0, 0)
            if (ColorUtils.isWhiteColor(statusColor)) {
                SystemUtils.getInstance().setStatusDark(this, true)
            }
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = PreviewPhotosAdapter(this, photos, this)
        lm = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPhotos.setLayoutManager(lm)
        rvPhotos.setAdapter(adapter)
        rvPhotos.scrollToPosition(index)
        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvPhotos)
        rvPhotos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val view: View = snapHelper.findSnapView(lm) ?: return
                val position: Int = lm.getPosition(view)
                if (lastPosition == position) {
                    return
                }
                lastPosition = position
                tvNumber.text = getString(R.string.preview_current_number_easy_photos, lastPosition + 1, photos.size)
                val holder = rvPhotos.getChildViewHolder(view) as PreviewPhotosViewHolder
                if (holder.ivPhoto != null && holder.ivPhoto.scale != 1f) {
                    holder.ivPhoto.setScale(1f, true)
                }
                if (holder.ivBigPhoto != null && holder.ivBigPhoto.scale != 1f) {
                    holder.ivBigPhoto.resetScaleAndCenter()
                }
            }
        })
        tvNumber.text = getString(R.string.preview_current_number_easy_photos, index + 1,
                photos.size)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.iv_back -> {
                doBack()
            }
            R.id.ivDelete -> {
                if (lastPosition >= 0 && lastPosition < photos.size) {
                    //有删除时，需返回 结果集
                    resultCode = Activity.RESULT_OK
                    if (photos.size <= 1) {//直接返回
                        photos.removeAt(lastPosition)
                        doBack()
                    } else {
                        photos.removeAt(lastPosition)
                        lastPosition = if (lastPosition >= photos.size) lastPosition - 1 else lastPosition
                        tvNumber.text = getString(R.string.preview_current_number_easy_photos, lastPosition + 1, photos.size)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        doBack()
    }

    private fun doBack() {
        val intent = Intent()
        if (resultCode == Activity.RESULT_OK) {
            intent.putExtra(Key.PREVIEW_EXTERNAL_PHOTOS, photos)
        }
        setResult(resultCode, intent)
        finish()
    }

    override fun onPreviewPhotoClick(position: Int) {
        val path = Result.getPhotoPath(position)
        var i = 0
        val length = photos.size
        while (i < length) {
            if (TextUtils.equals(path, photos[i].path)) {
                rvPhotos!!.scrollToPosition(i)
                lastPosition = i
                tvNumber!!.text = getString(R.string.preview_current_number_easy_photos, lastPosition + 1, photos.size)
                return
            }
            i++
        }
    }

    companion object {
        fun start(act: Activity, photos: ArrayList<Photo>?, currentIndex: Int, requestCode: Int) {
            val intent = Intent(act, LocalPreviewActivity::class.java)
            intent.putExtra(Key.PREVIEW_EXTERNAL_PHOTOS, photos)
            intent.putExtra(Key.PREVIEW_EXTERNAL_PHOTOS_BOTTOM_PREVIEW, false)
            intent.putExtra(Key.PREVIEW_PHOTO_INDEX, currentIndex)
            act.startActivityForResult(intent, requestCode)
        }

        /**
         * 一些旧设备在UI小部件更新之间需要一个小延迟
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}