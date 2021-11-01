package net.linkmate.app.ui.function_ui.choicefile.base

import android.text.TextUtils
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils
import java.util.regex.Pattern

/**
create by: 86136
create time: 2021/4/22 11:20
Function description:
 */


class ChoiceNasFileAdapter : BaseQuickAdapter<OneOSFile, BaseViewHolder>(R.layout.item_choice_file) {

    /**
     * 下面是对外暴露的可以操作的
     */
    var mLoginSession: LoginSession? = null//在每次更新数据，添加新数据的时候更
    var showFolderSelect = false//这是十文件夹是否可以被选中
    var showSplitLine = false
    var mMaxNum: Int = 1//最大能选择的个数

    /**
     * 下面是对外暴露的可以读取的
     */
    val mSelectList = mutableListOf<OneOSFile>()
    val mSelectLiveData = MutableLiveData<List<OneOSFile>>()

    /**
     * 下面是私有的
     */
    private val UN_SELECT = -100
    private var lastSelectPosition = UN_SELECT


    override fun setNewData(data: MutableList<OneOSFile>?) {
        mSelectList.clear()  //当目录切换的时候清除当前全部的已选中
        super.setNewData(data)
    }


    //全选或者全不选
    fun selectAll(boolean: Boolean) {
        mSelectList.clear()
        if (boolean) {
            mSelectList.addAll(mData)
        }
        mSelectLiveData.postValue(mSelectList)
        notifyDataSetChanged()
    }


    fun changeItemSelect(position: Int) {
        if (mData.size > position) {
            if (!mSelectList.contains(mData[position])) {
                addSelectItem(position)
            } else {
                deleteSelectItem(position)
            }
            mSelectLiveData.postValue(mSelectList)
        }
    }

    fun onLongClickAdd(position: Int) {
        if (mData.size > position) {
            if (!mSelectList.contains(mData[position])) {
                addSelectItem(position)
                mSelectLiveData.postValue(mSelectList)
            }
        }
    }


    private fun addSelectItem(position: Int) {
        if (mMaxNum == 1) {//单选逻辑，下一个选中会取消掉上一个选中
            mSelectList.clear()
            if (lastSelectPosition != UN_SELECT && !recyclerView.isComputingLayout) {
                notifyItemChanged(lastSelectPosition)
            }
            mSelectList.add(mData[position])
            notifyItemChanged(position)
            lastSelectPosition = position
        } else if (mMaxNum > 1) {
            if (mMaxNum > mSelectList.size) {
                if (!mSelectList.contains(mData[position]))
                    mSelectList.add(mData[position])
                if (!recyclerView.isComputingLayout) {
                    notifyItemChanged(position)
                }
            } else {
                String.format(mContext.getString(R.string.selector_reach_max_hint_easy_photos), mMaxNum)
                if (mSelectList.contains(mData[position]))
                    mSelectList.remove(mData[position])
                if (!recyclerView.isComputingLayout) {
                    notifyItemChanged(position)
                }
            }
        }
    }


    /**
     * End对外暴露的可以操作的
     */
    private fun deleteSelectItem(position: Int) {
        if (mData.size > position) {
            mSelectList.remove(mData[position])
            if (!recyclerView.isComputingLayout) {
                notifyItemChanged(position)
            }
        }
    }


    override fun convert(holder: BaseViewHolder, oneOSFile: OneOSFile) {
        setFileImage(holder, oneOSFile)
        holder.setText(R.id.file_name, oneOSFile.getName())
        holder.setText(R.id.file_time_tv, if ((oneOSFile.isPicture || oneOSFile.isVideo) && oneOSFile.cttime > 0) {
            FileUtils.fmtTimeByZone(oneOSFile.cttime)
        } else {
            FileUtils.fmtTimeByZone(oneOSFile.getTime())
        })
        holder.setText(R.id.file_size_tv, if (oneOSFile.isDirectory()) "" else FileUtils.fmtFileSize(oneOSFile.getSize()))
        if (mMaxNum > 1) {
            if (!showFolderSelect && oneOSFile.isDirectory()) {//文件夹不能被选中
                holder.setVisible(R.id.rv_list_cb_select, false)
            } else {
                holder.setVisible(R.id.rv_list_cb_select, !(TextUtils.isEmpty(oneOSFile.getPath()) || oneOSFile.getPath() == "/"))
                holder.addOnClickListener(R.id.rv_list_cb_select)
                holder.setChecked(R.id.rv_list_cb_select, mSelectList.contains(oneOSFile))
            }
        } else {
            holder.setVisible(R.id.rv_list_cb_select, false)
        }
        if (showSplitLine) {
            holder.setVisible(R.id.view3, true)
        }

    }


    //||||vvv 下面都是设置图标ICON相关
    private fun setFileImage(holder: BaseViewHolder, oneOSFile: OneOSFile) {
        val mIconView = holder.getView<ImageView>(R.id.file_image)
        if (oneOSFile.isDirectory()) {
            loadImage(mIconView, R.drawable.icon_device_folder)
            return
        }
        if (Pattern.matches(AppConstants.REGEX_UPLOAD_TMP_FILE, oneOSFile.getName())) {
            loadImage(mIconView, R.drawable.icon_file_upload_tmp)
            return
        }
        if (oneOSFile.isEncrypt()) {
            loadImage(mIconView, R.drawable.icon_device_encrypt)
        } else if (oneOSFile.isExtract) {
            loadImage(mIconView, R.drawable.icon_device_zip)
        } else {
            //todo fileServer加载视频bug
            if (FileUtils.isPicOrVideo(oneOSFile.getName()) &&
                    mLoginSession != null
                    && SessionManager.getInstance().isLogin(mLoginSession!!.id)
            ) {
                showPicturePreview(mLoginSession!!, mIconView, oneOSFile)
            } else {
                loadImage(mIconView, oneOSFile.icon)
            }
        }
    }

    private fun loadImage(mIconView: ImageView, model: Any, @DrawableRes resourceId: Int? = null) {
        var load = Glide.with(mIconView.context)
                .asBitmap()
                .centerCrop()
                .load(model)
                .placeholder(R.drawable.image_placeholder)
        if (resourceId != null) {
            load = load.error(resourceId)
        }
        load.into(mIconView)
    }


    private fun showPicturePreview(mLoginSession: LoginSession, imageView: ImageView, file: OneOSFile) {
        if (FileUtils.isGifFile(file.getName())) {
            val url = OneOSAPIs.genDownloadUrl(mLoginSession, file.getAllPath())
            loadGif(imageView, EliCacheGlideUrl(url), file.icon)
        } else {
            loadThumbByFresco(mLoginSession, imageView, file)
        }
    }

    private fun loadGif(imageView: ImageView, model: Any, @DrawableRes resourceId: Int) {
        Glide.with(imageView)
                .asGif()
                .load(model)
                .placeholder(R.drawable.image_placeholder)
                .error(resourceId)
                .into(imageView)
    }


    private fun loadThumbByFresco(mLoginSession: LoginSession, imageView: ImageView, file: OneOSFile) {
        val url = if (FileUtils.isPictureFile(file.getName()) && mLoginSession.isV5) {
            OneOSAPIs.genThumbnailUrl(mLoginSession, file)
        } else {
            OneOSAPIs.genThumbnailUrl(mLoginSession, file)
        }
        loadImage(imageView, EliCacheGlideUrl(url), file.icon)
    }

}