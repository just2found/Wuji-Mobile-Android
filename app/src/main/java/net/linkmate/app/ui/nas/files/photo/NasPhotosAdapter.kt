package net.linkmate.app.ui.nas.files.photo

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils

/**
 * @author Raleigh.Luo
 * date：21/5/6 15
 * describe：
 */
class NasPhotosAdapter(private val mContext: Context, private val filesViewModel: NasPhotosViewModel,
                       private val listener: OnPhotoClickListener,
                       val isSingle: Boolean = true,
                       val maxCount: Int = 1
) : BaseQuickAdapter<OneOSFile, BaseViewHolder>(R.layout.item_rv_photos_easy_photos) {
    private var unable: Boolean = false
    private val mSelectedPhotos: ArrayList<OneOSFile> = arrayListOf()

    init {
        unable = mSelectedPhotos.size == maxCount
    }


    /**
     * 选中图片
     */
    fun getSelectedPhotos(): ArrayList<OneOSFile> {
        return mSelectedPhotos
    }

    /**
     * 清除选中记录
     */
    fun clearSelectedPhotos() {
        mSelectedPhotos.clear()
    }

    override fun convert(helper: BaseViewHolder, item: OneOSFile) {
        val ivPhoto = helper.getView<ImageView>(R.id.iv_photo)
        val tvSelector = helper.getView<TextView>(R.id.tv_selector)
        val vSelector = helper.getView<View>(R.id.v_selector)
        val tvType = helper.getView<TextView>(R.id.tv_type)

        showPicturePreview(filesViewModel.getLoginSession(), ivPhoto, item)
        val isSelected = mSelectedPhotos.find {
            it === item
        } != null
        updateSelector(tvSelector, isSelected, item)
        if (isSingle) {
            vSelector.visibility = View.GONE
            tvSelector.visibility = View.GONE
        } else {
            vSelector.visibility = View.VISIBLE
            tvSelector.visibility = View.VISIBLE
        }
        ivPhoto.setOnClickListener {
            if (isSingle) {
                vSelector.performClick()
            } else {
                listener.onPhotoClick(helper.layoutPosition)
            }
        }
        /**--选择事件处理---***/
        vSelector.setOnClickListener(View.OnClickListener {
            if (isSingle) {
                if (mSelectedPhotos.size > 0) {
                    if (mSelectedPhotos.get(0) == item) {
                        mSelectedPhotos.add(item)
//                         notifyItemChanged(position)
                    } else {
                        mSelectedPhotos.removeAt(0)
                        mSelectedPhotos.add(item)
                        notifyDataSetChanged()
                    }
                } else {
                    mSelectedPhotos.add(item)
                    notifyItemChanged(helper.layoutPosition)
                }
                listener.onSelectorChanged()
                return@OnClickListener
            }
            val isSelected = mSelectedPhotos.find {
                it === item
            } != null
            if (unable) {
                if (isSelected) {
                    mSelectedPhotos.remove(item)
                    if (unable) {
                        unable = false
                    }
                    listener.onSelectorChanged()
                    notifyDataSetChanged()
                    return@OnClickListener
                }
                listener.onSelectError()
                return@OnClickListener
            }
            if (!isSelected) {
                val res = mSelectedPhotos.add(item)
                tvSelector.setBackgroundResource(R.drawable.bg_select_true_easy_photos)
                val selectedCount = getSelectedPhotos().size
                tvSelector.text = "$selectedCount"
                if (selectedCount == maxCount) {
                    unable = true
                    notifyDataSetChanged()
                }
            } else {
                mSelectedPhotos.remove(item)
                if (unable) {
                    unable = false
                }
                notifyDataSetChanged()
            }
            listener.onSelectorChanged()
        })
        ivPhoto.setOnClickListener {
            if (isSingle) {
                vSelector.performClick()
            } else {
                listener.onPhotoClick(helper.layoutPosition)
            }
        }
    }

    private fun showPicturePreview(mLoginSession: LoginSession?, imageView: ImageView, file: OneOSFile) {
        mLoginSession?.let {
            val load = if (FileUtils.isGifFile(file.getName())) {
                val url = OneOSAPIs.genDownloadUrl(mLoginSession, file.getAllPath())
                Glide.with(imageView)
                        .asGif()
                        .load(EliCacheGlideUrl(url))
            } else {
                val url = OneOSAPIs.genThumbnailUrl(mLoginSession, file)
                Glide.with(imageView)
                        .asBitmap()
                        .centerCrop()
                        .load(EliCacheGlideUrl(url))
            }
            load.error(R.color.easy_photos_bar_primary)
                    .placeholder(R.color.easy_photos_bar_primary)
                    .into(imageView)
        }
    }


    private fun updateSelector(tvSelector: TextView, selected: Boolean, photo: OneOSFile) {
        if (selected) {
            val index = mSelectedPhotos.indexOfFirst {
                it === photo
            }
            val number = index + 1
            if (number == 0) {
                tvSelector.setBackgroundResource(R.drawable.bg_select_false_easy_photos)
                tvSelector.text = null
                return
            }
            tvSelector.text = number.toString()
            tvSelector.setBackgroundResource(R.drawable.bg_select_true_easy_photos)
            if (isSingle) {
                tvSelector.text = "1"
            }
        } else {
            if (unable) {
                tvSelector.setBackgroundResource(R.drawable.bg_select_false_unable_easy_photos)
            } else {
                tvSelector.setBackgroundResource(R.drawable.bg_select_false_easy_photos)
            }
            tvSelector.text = null
        }
    }


    interface OnPhotoClickListener {
        fun onPhotoClick(position: Int)
        fun onSelectorChanged()
        fun onSelectError()
    }
}