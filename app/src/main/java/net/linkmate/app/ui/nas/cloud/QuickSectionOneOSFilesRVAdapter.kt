package net.linkmate.app.ui.nas.cloud

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.SectionEntity
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache
import net.linkmate.app.R
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.DisplayMode
import net.sdvn.nascommon.model.FileViewerType
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.phone.LocalFile
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.CheckableImageButton
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.Pattern

class QuickSectionOneOSFilesRVAdapter(
        var context: Context, private var mFileList: List<DataFile>,
        private var mSelectedList: ArrayList<DataFile>
) : BaseSectionQuickAdapter<SectionEntity<DataFile>, BaseViewHolder>
(R.layout.item_rv_filelist, R.layout.layout_timeline_header, null) {
    var isShowMedia: Boolean = false
    var mode: DisplayMode? = null


    //    private val mConfig: AsyncDifferConfig<SectionEntity<DataFile>>
    var isMultiChooseModel = false
        private set
    var isMultiChooseEnable = true
    private var mLoginSession: LoginSession? = null
    private var isWifiAvailable = true
    private var fileNamesDL: MutableList<File> = CopyOnWriteArrayList()
    private var mDevId: String? = null
    private var downloadPath: String? = null
    protected var enable: Boolean = false

    //    private val mAsyncListDiffer: AsyncListDiffer<SectionEntity<DataFile>>
    val selectedList: ArrayList<DataFile>?
        get() = if (isMultiChooseModel) {
            mSelectedList
        } else null

    val selectedCount: Int
        get() {
            var count = 0
            if (isMultiChooseModel) {
                count = mSelectedList.size
            }
            return count
        }


    private var mFileViewerType = FileViewerType.LIST

    init {

//        mConfig = AsyncDifferConfig.Builder(diffCallback).build()
//        mAsyncListDiffer = AsyncListDiffer(AdapterListUpdateCallback(this), mConfig)
//        clearSelectedList()
    }


    /**
     * init Selected Map
     */
    private fun clearSelectedList() {
        if (mSelectedList == null) {
            Timber.e("Selected List is NULL")
            return
        }
        mSelectedList.clear()
    }

    fun replaceData(data: List<SectionEntity<DataFile>>) {
        replaceData(data, null)
    }

    fun replaceData(data: List<SectionEntity<DataFile>>, loginSession: LoginSession?) {
        if (loginSession != null) {
            this.mLoginSession = loginSession
            mDevId = loginSession.id
        }
//        refreshDownloadedFile()
        super.replaceData(data)
    }


    fun notifyDataSetChanged(changed: Boolean) {
//        refreshDownloadedFile()
        if (changed) {
            clearSelectedList()
        }
        super.notifyDataSetChanged()
    }

    fun setIsMultiModel(isMulti: Boolean) {
        if (this.isMultiChooseModel != isMulti) {
            this.isMultiChooseModel = isMulti
            if (isMulti) {
                clearSelectedList()
            }
            notifyDataSetChanged()
        }
    }

    fun selectAllItem(isSelectAll: Boolean) {
        if (isMultiChooseModel && null != mSelectedList) {
            mSelectedList.clear()
            if (isSelectAll) {
                if (mode != null && mode == DisplayMode.SHARE || mode == DisplayMode.UPLOAD) {
                    //分享模式下 目前不分享文件夹和公共目录文件
                    for (oneOSFile in mFileList) {
                        if (!oneOSFile.isDirectory() && !oneOSFile.isPublicFile())
                            mSelectedList.add(oneOSFile)
                    }
                } /*else if (mode != null && mode == DisplayMode.PUBLIC) {
                    //public 下  是否onlyRead
                    for (oneOSFile in mFileList) {
                        if (oneOSFile is OneOSFile && !fileIsOnlyRead(oneOSFile)) {
                            mSelectedList.add(oneOSFile)
                        }
                    }
                } */ else
                    mSelectedList.addAll(mFileList)
            }
        }
    }

    fun setWifiAvailable(isWifiAvailable: Boolean) {
        this.isWifiAvailable = isWifiAvailable
    }

    private fun hasLocal(file: DataFile) =
            !file.isDirectory() && file is OneOSFile && localContainsFile(fileNamesDL, file)

    private fun localContainsFile(localFiles: List<File>, file: OneOSFile): Boolean {
        if (file.hasLocalFile()) {
            return true
        }
        val toPath = SessionManager.getInstance().getDefaultDownloadPathByID(mDevId, file)
        val localFile = File(toPath, file.getName())
        if (localFile != null && localFile.exists()
                && localFile.length() == file.getSize()) {
            file.localFile = localFile
            return true
        }
        return false

    }

    override fun convertHead(helper: BaseViewHolder, item: SectionEntity<DataFile>) {
        helper.setText(R.id.header, item.header)
    }


    fun toggleViewerType(type: FileViewerType) {
        mFileViewerType = type
        //        notifyDataSetChanged();
    }

    override fun convert(holder: BaseViewHolder, item: SectionEntity<DataFile>) {
        val file = item.t
        if (isList()) {
            holder.setGone(R.id.rv_layout_grid, false)
            holder.setGone(R.id.rv_layout_list, true)
            if (file != null) {
                bindList(holder, file)
            }
        } else {
            holder.setGone(R.id.rv_layout_grid, true)
            holder.setGone(R.id.rv_layout_list, false)
            if (file != null) {
                bindGrid(holder, file)
            }
        }
    }

    private fun isList() = mFileViewerType == FileViewerType.LIST

    private fun bindList(holder: BaseViewHolder, file: DataFile) {
        val mIconView = holder.getView<ImageView>(R.id.rv_list_iv_icon)
        val mNameTxt = holder.getView<TextView>(R.id.rv_list_txt_name)
        val mTimeTxt = holder.getView<TextView>(R.id.rv_list_txt_time)
        val mSelectCb = holder.getView<CheckableImageButton>(R.id.rv_list_cb_select)
        val mSizeTxt = holder.getView<TextView>(R.id.rv_list_txt_size)
        val mProgressBar = holder.getView<ProgressBar>(R.id.rv_list_progressbar)
        val mSelectIBtn = holder.getView<ImageButton>(R.id.rv_list_ibtn_select)

        mProgressBar.isGone = true
        setIcon(file, mIconView, holder, true)
        mNameTxt.text = file.getName()

        mTimeTxt.text = if (file is OneOSFile && (file.isPicture || file.isVideo) && file.cttime > 0) {
            FileUtils.fmtTimeByZone(file.cttime)
        } else {
            FileUtils.fmtTimeByZone(file.getTime())
        }

        hasLocalFile(file, holder, holder.getView(R.id.rv_list_img_dl)) {}

        mSizeTxt.text = if (file.isDirectory()) "" else FileUtils.fmtFileSize(file.getSize())

        if (isMultiChooseEnable) {
            if (isMultiChooseModel) {
                mSelectIBtn.visibility = View.GONE
                mSelectCb.visibility = View.VISIBLE
                mSelectCb.isChecked = selectedList!!.contains(file)
            } else {
                mSelectCb.isChecked = false
                mSelectCb.visibility = View.GONE
                mSelectIBtn.visibility = View.VISIBLE
            }
            mIconView.isSelected = mSelectCb.isSelected
        } else {
            mIconView.isSelected = false
            mSelectCb.visibility = View.GONE
            mSelectIBtn.visibility = View.GONE
        }
        if (mode != null) {
            if (mode == DisplayMode.SHARE || (mode == DisplayMode.UPLOAD)) {
                if (file.isDirectory()) {
                    mSelectCb.visibility = View.GONE
                    mSelectIBtn.visibility = View.GONE
                }
            } /*else if (mode == DisplayMode.PUBLIC && file is OneOSFile && fileIsOnlyRead(file)) {
                mSelectCb.visibility = View.GONE
                mSelectIBtn.visibility = View.GONE
            }*/
        }
        holder.addOnClickListener(R.id.rv_list_ibtn_select)
    }

    private fun hasLocalFile(file: DataFile, holder: BaseViewHolder, imageView: ImageView, result: (Boolean) -> Unit) {
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

    private fun bindGrid(holder: BaseViewHolder, file: DataFile) {
        val mIconView = holder.getView<ImageView>(R.id.rv_grid_iv_icon)
        val mNameTxt = holder.getView<TextView>(R.id.rv_grid_txt_name)
        val mSelectCb = holder.getView<CheckableImageButton>(R.id.rv_grid_cb_select)
        setIcon(file, mIconView, holder, false)
        mNameTxt.text = file.getName()
        //        mIconView.setElement(file.getName());

        hasLocalFile(file, holder, holder.getView(R.id.rv_grid_img_dl)) {

        }

        holder.setGone(mNameTxt.id, !isShowMedia)
        if (isMultiChooseEnable) {
            if (isMultiChooseModel) {
                mSelectCb.visibility = View.VISIBLE
                mSelectCb.isChecked = selectedList!!.contains(file)
            } else {
                mSelectCb.isChecked = false
                mSelectCb.visibility = View.GONE
            }
            mIconView.isSelected = mSelectCb.isSelected
        } else {
            mIconView.isSelected = false
            mSelectCb.visibility = View.GONE

        }
        if (mode != null) {
            if (mode == DisplayMode.SHARE || (mode == DisplayMode.UPLOAD)) {
                if (file.isDirectory()) {
                    mSelectCb.visibility = View.GONE
                }
            }
//            if (file is OneOSFile && mode == DisplayMode.PUBLIC && fileIsOnlyRead(file)) {
//                mSelectCb.visibility = View.GONE
//            }
        }
    }


    private fun setIcon(file: DataFile, mIconView: ImageView, holder: BaseViewHolder, isList: Boolean) {
        if (file is OneOSFile) {
            holder.setGone(if (isList) {
                R.id.rv_list_iv_favorite
            } else {
                R.id.rv_grid_iv_favorite
            }, file.isFavorite())
        }
//        Glide.with(mIconView.context).clear(mIconView)
        if (file.isDirectory()) {
            loadImage(mIconView, R.drawable.icon_device_folder)
            if (isList) {
                holder.setProgress(R.id.rv_list_progressbar, 0)
            }
            return
        }
        if (file is OneOSFile) {
            if (Pattern.matches(AppConstants.REGEX_UPLOAD_TMP_FILE, file.getName())) {
                loadImage(mIconView, R.drawable.icon_file_upload_tmp)
                return
            }
            if (isList) {
                holder.setProgress(R.id.rv_list_progressbar, file.progress)
            }
            if (file.isEncrypt()) {
                loadImage(mIconView, R.drawable.icon_device_encrypt)
            } else if (file.isExtract) {
                loadImage(mIconView, R.drawable.icon_device_zip)
            } else {
                //todo fileServer加载视频bug
                if (FileUtils.isPicOrVideo(file.getName()) &&
                        mLoginSession != null
                        && SessionManager.getInstance().isLogin(mLoginSession!!.id)
                ) {
                    showPicturePreview(mLoginSession!!, mIconView, file)
                } else {
                    loadImage(mIconView, file.icon)
                }
            }
        } else if (file is LocalFile && (file.isPicOrVideo || FileUtils.isPicOrVideo(file.getName()))) {
            showLocalPreview(mIconView, file)
        } else {
            loadImage(mIconView, FileUtils.fmtFileIcon(file.getName()))
        }
    }

    private fun loadImage(mIconView: ImageView, model: Any, @DrawableRes resourceId: Int? = null) {
        var load = Glide.with(mIconView.context)
                .asDrawable()
                .centerCrop()
                .load(model)
                .placeholder(R.drawable.image_placeholder)
        if (resourceId != null) {
            load = load.error(resourceId)
        }
        load
//                .apply(options)
                .into(mIconView)
    }

    private fun showLocalPreview(imageView: ImageView, file: LocalFile) {
        if (FileUtils.isGifFile(file.getName())) {
            loadGif(imageView, file.getPath(), FileUtils.fmtFileIcon(file.getName()))
        } else {
            loadImage(imageView, file.getPath())
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

    private fun showPicturePreview(mLoginSession: LoginSession, imageView: ImageView, file: OneOSFile) {
        if (FileUtils.isGifFile(file.getName()) && mLoginSession.isV5) {
            val url = OneOSAPIs.genOpenUrl(mLoginSession, file)
            loadGif(imageView, EliCacheGlideUrl(url), file.icon)
        } else {
            loadThumbByFresco(mLoginSession, imageView, file)
        }
    }

    //设置图片圆角角度
//    var roundedCorners: RoundedCorners = RoundedCorners(6)

    //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
    // RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(300, 300);
//    var options: RequestOptions = RequestOptions.bitmapTransform(roundedCorners).override(300, 300)

    private fun loadThumbByFresco(mLoginSession: LoginSession, imageView: ImageView, file: OneOSFile) {
        val isV5 = SessionCache.instance.isV5(mLoginSession.id!!);
        val url = if (FileUtils.isPictureFile(file.getName()) && mLoginSession.isV5 && !isV5) {
//            OneOSAPIs.genOpenUrl(mLoginSession, file)
            OneOSAPIs.genDownloadUrl(mLoginSession, file.getAllPath())
        } else {
            OneOSAPIs.genThumbnailUrl(mLoginSession, file)
        }
        loadImage(imageView, EliCacheGlideUrl(url), file.icon)
        //                    .load(new EliCacheGlideUrl(url))

    }

    fun addData(data: List<SectionEntity<DataFile>>, loginSession: LoginSession?) {
        if (loginSession != null) {
            this.mLoginSession = loginSession
        }
        addData(data)

    }

    fun toggleItemSelected(position: Int) {
        val sectionEntity = getItem(position)
        sectionEntity?.let {
            if (sectionEntity.isHeader) {
                return
            }
            val file = sectionEntity.t ?: return
            if (mSelectedList.contains(file)) {
                mSelectedList.remove(file)
            } else {
//                if (mode != null && mode == DisplayMode.PUBLIC && file is OneOSFile && !fileIsOnlyRead(file))
                mSelectedList.add(file)
            }
            notifyItemChanged(position)
        }
    }

    fun notifyItemChanged(oneOSFile: DataFile) {
        val layoutManager = recyclerView?.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val last = layoutManager.findLastVisibleItemPosition()
            val first = layoutManager.findFirstVisibleItemPosition()
            if (last >= 0 && first >= 0) for (i in first..last) {
                val item = getItem(i) ?: continue
                //改变为相同的判断依据
                if (item.t != null && oneOSFile.getName() == item.t!!.getName() && oneOSFile.getPath() == item.t!!.getPath()) {
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    fun removeSelectedList() {
        mData?.let {
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                val file = iterator.next().t ?: continue
                if (mSelectedList.contains(file)) {
                    iterator.remove()
                }
            }
            notifyDataSetChanged()
        }
    }

    fun fileIsOnlyRead(file: OneOSFile): Boolean {
        if (mLoginSession?.isAdmin == true || file.uid == mLoginSession?.userInfo?.uid) {
            return false
        }
        return true
    }
}
