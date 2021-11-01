package net.linkmate.app.ui.nas.group.list

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.linkmate.app.R
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.CheckableImageButton
import timber.log.Timber
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.Pattern

/**
create by: 86136
create time: 2021/4/22 11:20
Function description:
 */


//listLayoutID 列表展示时候的Id   //gridLayoutID九宫格展示时候的ID
class GroupSpaceNasFileAdapter(
    private val listLayoutID: Int = R.layout.item_choice_file,
    private val gridLayoutID: Int = R.layout.item_choice_file_grid
) : BaseQuickAdapter<OneOSFile, BaseViewHolder>(R.layout.item_choice_file) {
    /**
     * 下面是对外暴露的可以操作的
     */
    var mLoginSession: LoginSession? = null//在每次更新数据，添加新数据的时候更新
    var mSelectList = mutableListOf<OneOSFile>()//这个是选中全部的
    val mSelectLiveData = MutableLiveData<List<OneOSFile>>()
    var mLastCheckableImageButton: CheckableImageButton? = null
    private var fileNamesDL: MutableList<File> = CopyOnWriteArrayList()
    var groupId: Long = -1

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

    //列表展示
    fun setListShow() {
        mLayoutResId = listLayoutID
    }

    fun onDownloadFinish(transferElement: DownloadElement) {
        val file = transferElement.file ?: return
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val last = layoutManager.findLastVisibleItemPosition()
            val first = layoutManager.findFirstVisibleItemPosition()
            if (last >= 0 && first >= 0) for (i in first..last) {
                val item = getItem(i) ?: continue
                //改变为相同的判断依据
                if (item != null && file.getName() == item.getName() && file.getPath() == item.getPath()) {
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    //九宫格展示
    fun setGridShow() {
        mLayoutResId = gridLayoutID
    }

    fun changeSelectItem(position: Int) {
        if (mSelectList.contains(mData[position])) {
            mSelectList.remove(mData[position])
        } else {
            mSelectList.add(mData[position])
            if (mLayoutResId == gridLayoutID && mSelectList.size == 1) {
                notifyDataSetChanged()
            }
        }
        mSelectLiveData.postValue(mSelectList)
        notifyItemChanged(position)
    }

    /**
     * End对外暴露的可以操作的
     */
    private val mOnCheckedChangeListener by lazy {
        CheckableImageButton.OnCheckedChangeListener { buttonView, isChecked ->
            buttonView.tag?.let {
                if (it is OneOSFile) {
                    if (isChecked) {
                        if (!mSelectList.contains(it)) {
                            mSelectList.add(it)
                            mSelectLiveData.postValue(mSelectList)
                        }
                    } else {
                        mSelectList.remove(it)
                        mSelectLiveData.postValue(mSelectList)
                    }
                }
            }
        }
    }

    override fun convert(holder: BaseViewHolder, oneOSFile: OneOSFile) {
        if (mLayoutResId == listLayoutID) {
            setFileImage(holder.getView(R.id.file_image), oneOSFile)
            holder.setText(R.id.file_name, oneOSFile.getName())
            holder.setText(
                R.id.file_time_tv,
                if ((oneOSFile.isPicture || oneOSFile.isVideo) && oneOSFile.cttime > 0) {
                    FileUtils.fmtTimeByZone(oneOSFile.cttime)
                } else {
                    FileUtils.fmtTimeByZone(oneOSFile.getTime())
                }
            )
            holder.setText(
                R.id.file_size_tv,
                if (oneOSFile.isDirectory()) "" else FileUtils.fmtFileSize(oneOSFile.getSize())
            )
            holder.addOnClickListener(R.id.rv_list_cb_select)
            val view = holder.getView<CheckableImageButton>(R.id.rv_list_cb_select)
            view.tag = oneOSFile
            view.setOnCheckedChangeListener(mOnCheckedChangeListener)
            view.isChecked = mSelectList.contains(oneOSFile)
            hasLocalFile(oneOSFile, holder, holder.getView(R.id.rv_list_img_dl)) {

            }
        } else if (mLayoutResId == gridLayoutID) {
            setFileImage(holder.getView(R.id.rv_grid_iv_icon), oneOSFile)
            holder.setText(R.id.rv_grid_txt_name, oneOSFile.getName())
            holder.addOnClickListener(R.id.rv_grid_cb_select)
            holder.setVisible(R.id.rv_grid_cb_select, mSelectList.size > 0)
            val view = holder.getView<CheckableImageButton>(R.id.rv_grid_cb_select)
            view.tag = oneOSFile
            view.setOnCheckedChangeListener(mOnCheckedChangeListener)
            view.isChecked = mSelectList.contains(oneOSFile)
            hasLocalFile(oneOSFile, holder, holder.getView(R.id.rv_grid_img_dl)) {
            }
        }
    }


    private fun hasLocalFile(
        file: DataFile,
        holder: BaseViewHolder,
        imageView: ImageView,
        result: (Boolean) -> Unit
    ) {
        val tag = file.getTag()
        if (imageView.getTag() != tag) {
            imageView.visibility = View.INVISIBLE
        }
        Observable.create<Boolean> {
            it.onNext(hasLocal(file))
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(RxLife.`as`(holder.itemView, true))
            .subscribe({
                result(it)
                imageView.visibility = if (it) View.VISIBLE else View.INVISIBLE
                imageView.setTag(tag)
            }, {
                Timber.e(it)
                result(false)
                imageView.visibility = View.INVISIBLE
            })
    }


    private fun hasLocal(file: DataFile) =
        !file.isDirectory() && file is OneOSFile && localContainsFile(fileNamesDL, file)

    private fun localContainsFile(localFiles: List<File>, file: OneOSFile): Boolean {
        if (file.hasLocalFile()) {
            return true
        }
        val toPath =
            SessionManager.getInstance().getDefaultDownloadPathByID(mLoginSession!!.id, file)
        val localFile = File(toPath, file.getName())
        if (localFile != null && localFile.exists()
            && localFile.length() == file.getSize()
        ) {
            file.localFile = localFile
            return true
        }
        return false
    }

    //||||vvv 下面都是设置图标ICON相关
    private fun setFileImage(mIconView: ImageView, oneOSFile: OneOSFile) {
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


    private fun showPicturePreview(
        mLoginSession: LoginSession,
        imageView: ImageView,
        file: OneOSFile
    ) {
//        if (FileUtils.isGifFile(file.getName())) {
//            val url = OneOSAPIs.genThumbnailUrlV5(mLoginSession, file.getAllPath())
//            loadGif(imageView, EliCacheGlideUrl(url), file.icon)
//        } else {
        loadThumbByFresco(mLoginSession, imageView, file)
//        }
    }

    private fun loadGif(imageView: ImageView, model: Any, @DrawableRes resourceId: Int) {
        Glide.with(imageView)
            .asGif()
            .load(model)
            .placeholder(R.drawable.image_placeholder)
            .error(resourceId)
            .into(imageView)
    }

    private fun loadThumbByFresco(
        mLoginSession: LoginSession,
        imageView: ImageView,
        file: OneOSFile
    ) {
        val url = OneOSAPIs.genThumbnailUrlV5(
            SharePathType.GROUP.type,
            mLoginSession,
            file.getPath(),
            groupId,
            "min"

        )
        loadImage(imageView, EliCacheGlideUrl(url), file.icon)
    }

    //EDN都是设置图标ICON相关

}