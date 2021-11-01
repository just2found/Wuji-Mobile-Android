package net.linkmate.app.ui.nas.favorites;

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.SectionEntity
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.linkmate.app.R
import net.linkmate.app.ui.nas.images.IPhotosViewModel
import net.linkmate.app.ui.nas.images.ISelection
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.DisplayMode
import net.sdvn.nascommon.model.FileViewerType
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.phone.LocalFile
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.widget.CheckableImageButton
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.regex.Pattern

class DataFileRVAdapter(var context: Context, private val iPhotosViewModel: IPhotosViewModel<OneOSFile>)
    : BaseSectionQuickAdapter<SectionEntity<DataFile>, BaseViewHolder>
(R.layout.item_rv_filelist, R.layout.layout_timeline_header, null), ISelection {
    var isShowMedia: Boolean = false
    var mode: DisplayMode? = null
    var isMultiChooseEnable = true
    protected var enable: Boolean = false


    private var mFileViewerType = FileViewerType.LIST

    fun notifyDataSetChanged(changed: Boolean) {
//        refreshDownloadedFile()
        super.notifyDataSetChanged()
    }

    override fun convertHead(helper: BaseViewHolder, item: SectionEntity<DataFile>) {
        helper.setText(R.id.header, item.header)
    }

    fun toggleViewerType(type: FileViewerType) {
        mFileViewerType = type
        notifyDataSetChanged();
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

    private fun hasLocal(file: DataFile) =
            !file.isDirectory() && file is OneOSFile && localContainsFile(file)

    private fun localContainsFile(file: OneOSFile): Boolean {
        if (file.hasLocalFile()) {
            return true
        }
        val toPath = SessionManager.getInstance().getDefaultDownloadPathByID(iPhotosViewModel.getDeviceId(), file)
        val localFile = File(toPath, file.getName())
        if (localFile.exists() && localFile.length() == file.getSize()) {
            file.localFile = localFile
            return true
        }
        return false
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

        setIcon(file, mIconView, holder, true)
        mNameTxt.text = file.getName()
        hasLocalFile(file, holder, holder.getView(R.id.rv_list_img_dl)) {}
        mTimeTxt.text = if (file is OneOSFile && (file.isPicture || file.isVideo) && file.cttime > 0) {
            FileUtils.fmtTimeByZone(file.cttime)
        } else {
            FileUtils.fmtTimeByZone(file.getTime())
        }

        mSizeTxt.text = if (file.isDirectory()) "" else FileUtils.fmtFileSize(file.getSize())

        if (isMultiChooseEnable) {
            if (isSetMultiModel) {
                mSelectIBtn.visibility = View.GONE
                mSelectCb.visibility = View.VISIBLE
                mSelectCb.isChecked = selection.contains(holder.adapterPosition)
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
            }
        }
        holder.addOnClickListener(R.id.rv_list_ibtn_select)
    }

    private fun bindGrid(holder: BaseViewHolder, file: DataFile) {
        val mIconView = holder.getView<ImageView>(R.id.rv_grid_iv_icon)
        val mNameTxt = holder.getView<TextView>(R.id.rv_grid_txt_name)
        val mSelectCb = holder.getView<CheckableImageButton>(R.id.rv_grid_cb_select)
        setIcon(file, mIconView, holder, false)
        mNameTxt.text = file.getName()
        hasLocalFile(file, holder, holder.getView(R.id.rv_grid_img_dl)) {}
        if (isShowMedia)
            holder.setGone(mNameTxt.id, false)
        if (isMultiChooseEnable) {
            if (isSetMultiModel) {
                mSelectCb.visibility = View.VISIBLE
                mSelectCb.isChecked = selection.contains(holder.adapterPosition)
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
        }
    }


    private fun setIcon(file: DataFile, mIconView: ImageView, holder: BaseViewHolder, isList: Boolean) {
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
                if (FileUtils.isPicOrVideo(file.getName())) {
                    showPicturePreview(mIconView, file)
                } else {
                    loadImage(mIconView, file.icon)
                }
            }
        } else if (file is LocalFile && FileUtils.isPicOrVideo(file.getName())) {
            showLocalPreview(mIconView, file)
        } else {
            loadImage(mIconView, FileUtils.fmtFileIcon(file.getName()))
        }
    }

    private fun loadImage(mIconView: ImageView, model: Any?, @DrawableRes resourceId: Int? = null) {
        var load = Glide.with(mIconView.context)
                .asBitmap()
                .centerCrop()
                .load(model)
                .placeholder(R.drawable.image_placeholder)
        if (resourceId != null) {
            load = load.error(resourceId)
        }
        load
                .into(mIconView)
    }

    private fun loadGif(imageView: ImageView, model: Any?, @DrawableRes resourceId: Int) {
        Glide.with(imageView)
                .asGif()
                .load(model)
                .placeholder(R.drawable.image_placeholder)
                .error(resourceId)
                .into(imageView)
    }

    private fun showLocalPreview(imageView: ImageView, file: LocalFile) {
        if (FileUtils.isGifFile(file.getName())) {
            loadGif(imageView, file.getPath(), FileUtils.fmtFileIcon(file.getName()))
        } else {
            loadImage(imageView, file.getPath())
        }
    }

    private fun showPicturePreview(imageView: ImageView, file: OneOSFile) {
        val resourceId = FileUtils.fmtFileIcon(file.getName())
//        if (FileUtils.isGifFile(file.getName())) {
//            loadGif(imageView, iPhotosViewModel.getGlideModeTb(file), resourceId)
//        } else {
            loadImage(imageView, iPhotosViewModel.getGlideModeTb(file), resourceId)
//        }
    }


    fun toggleItemSelected(position: Int) {
        val sectionEntity = getItem(position)
        sectionEntity?.let {
            if (sectionEntity.isHeader) {
                return
            }
            val file = sectionEntity.t ?: return
            if (selection.contains(position)) {
                select(position, false)
            } else {
                select(position, true)
            }
            notifyItemChanged(position)
        }
    }

    fun notifyItemChanged(oneOSFile: DataFile) {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val last = layoutManager.findLastVisibleItemPosition()
            val first = layoutManager.findFirstVisibleItemPosition()
            if (last >= 0 && first >= 0) for (i in first..last) {
                val item = getItem(i) ?: continue
                if (item.t != null && Objects.equals(item.t, oneOSFile)) {
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    fun removeSelectedList() {
        mData?.let {
            val selected = mutableListOf<SectionEntity<DataFile>>()
            it.forEachIndexed { index, sectionEntity ->
                if (selection.contains(index)) {
                    selected.add(sectionEntity)
                }
            }
            it.removeAll(selected)
            deselectAll()
        }
    }


    // ----------------------
    // Selection
    // ----------------------

    var isSetMultiModel: Boolean = false
        set(value) {
            field = value
            if (value) {
                deselectAll()
            }
            notifyDataSetChanged()
        }
    override val selection: HashSet<Int> = hashSetOf()

    fun toggleSelection(pos: Int) {
        if (selection.contains(pos)) selection.remove(pos) else selection.add(pos)
        notifyItemChanged(pos)
    }

    fun select(pos: Int, selected: Boolean) {
        if (selected) selection.add(pos) else selection.remove(pos)
        notifyItemChanged(pos)
    }

    override fun selectRange(start: Int, end: Int, selected: Boolean) {
        for (i in start..end) {
            if (getItem(i)?.isHeader == true) {
                continue
            }
            if (selected) selection.add(i) else selection.remove(i)
        }
        notifyItemRangeChanged(start, end - start + 1)
    }

    fun deselectAll() {
        // this is not beautiful...
        selection.clear()
        notifyDataSetChanged()
    }

    fun selectAll() {
        mData?.forEachIndexed { index, sectionEntity ->
            if (!sectionEntity.isHeader) {
                selection.add(index)
            }
        }
        notifyDataSetChanged()
    }

    fun toggleSelectionHeader(position: Int) {

    }

    fun findFilterHeaderPosition(position: Int): Int {
        var index = 0
        var countHeader = 0
        for (entity in data) {
            if (entity.isHeader) {
                countHeader++
            }
            index++
            if (index == position) {
                break
            }
        }
        return position - countHeader
    }

    val countSelected: Int
        get() = selection.size
}
