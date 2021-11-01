package net.linkmate.app.ui.nas.transfer_r


import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import io.weline.repo.SessionCache
import net.linkmate.app.R
import net.linkmate.app.util.FormatUtils
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.transfer.TransferException
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.utils.Utils
import java.io.File
import kotlin.math.abs

class QuickTransmissionAdapterR(data: List<MultiItemEntity>?, private val mActivity: FragmentActivity) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    companion object {
        const val PROGRESS_TITLE = 1
        const val PROGRESS_ITEM = 2
        const val COMPLETED_TITLE = 3
        const val COMPLETED_ITEM = 4

        fun getFailedInfo(context: Context, mElement: TransferEntityR): String? {
            var failedInfo: String? = null

            if (!Utils.isWifiAvailable(context) && SPUtils.getBoolean(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
                mElement.exception = TransferException.WIFI_UNAVAILABLE
            }

            when (mElement.exception) {
                TransferException.NONE -> return null
                TransferException.LOCAL_SPACE_INSUFFICIENT -> failedInfo = context.resources.getString(R.string.local_space_insufficient)
                TransferException.SERVER_SPACE_INSUFFICIENT -> failedInfo = context.resources.getString(R.string.server_space_insufficient)
                TransferException.FAILED_REQUEST_SERVER -> failedInfo = context.resources.getString(R.string.request_server_exception)
                TransferException.ENCODING_EXCEPTION -> failedInfo = context.resources.getString(R.string.decoding_exception)
                TransferException.IO_EXCEPTION -> failedInfo = context.resources.getString(R.string.io_exception)
                TransferException.FILE_NOT_FOUND -> failedInfo = if (mElement.isDownload) {
                    context.resources.getString(R.string.touch_file_failed)
                } else {
                    context.resources.getString(R.string.source_not_found)
                }
                TransferException.SERVER_FILE_NOT_FOUND -> failedInfo = context.resources.getString(R.string.source_not_found)
                TransferException.UNKNOWN_EXCEPTION -> failedInfo = context.resources.getString(R.string.unknown_exception)
                TransferException.SOCKET_TIMEOUT -> failedInfo = context.resources.getString(R.string.socket_timeout)
                TransferException.WIFI_UNAVAILABLE -> failedInfo = context.resources.getString(R.string.wifi_connect_break)
                TransferException.TEMPORARY_FILE_NOT_FOUND -> failedInfo = context.resources.getString(R.string.tmp_file_not_found)
                TransferException.NO_PERM -> failedInfo = context.resources.getString(R.string.ec_no_permission)
            }
            return failedInfo
        }
    }

    init {
        addItemType(PROGRESS_TITLE, R.layout.item_line_string)
        addItemType(PROGRESS_ITEM, R.layout.item_transfer)
        addItemType(COMPLETED_TITLE, R.layout.item_line_string)
        addItemType(COMPLETED_ITEM, R.layout.item_transfer)
    }


    override fun convert(helper: BaseViewHolder, entity: MultiItemEntity?) {
        when (helper.itemViewType) {
            PROGRESS_TITLE -> {
                if (entity is HeaderEntityR) {
                    helper.setText(R.id.header, "${mActivity.getString(R.string.processing)}(${entity.size})")
                    helper.setText(R.id.more, if (entity.isAllStart) mActivity.getString(R.string.pause_all) else mActivity.getString(R.string.start_all))
                    helper.addOnClickListener(R.id.more)
                }
            }
            PROGRESS_ITEM -> {
                if (entity is TransferEntityR) {
                    initEntitySharePart(helper, entity)
                    initProgressItem(helper, entity)
                }
            }
            COMPLETED_TITLE -> {
                if (entity is HeaderEntityR) {
                    helper.setText(R.id.header, "${mActivity.getString(R.string.complete)}(${entity.size})")
                    helper.setText(R.id.more, mActivity.getString(R.string.clear))
                    helper.addOnClickListener(R.id.more)
                }
            }
            COMPLETED_ITEM -> {
                if (entity is TransferEntityR) {
                    initEntitySharePart(helper, entity)
                    initCompletedItem(helper, entity)
                }
            }
        }
    }

    private fun initProgressItem(holder: BaseViewHolder, entity: TransferEntityR) {
        holder.setText(R.id.fileSize, FileUtils.fmtFileSize(entity.offset) +
                "/" + FileUtils.fmtFileSize(entity.fileSize))
        val cur = entity.offset
        val total = entity.fileSize
        val progress = if (entity.fileSize == 0L) 0 else {
            ((cur * 1f / total * 100f) + 0.5f).toInt()
        }
        //只有在进行时才显示进度
        holder.setProgress(R.id.progress, progress)
        holder.setGone(R.id.progress, true)
        holder.setGone(R.id.textView_path, false)
        when (entity.state) {
            TransferState.PAUSE -> {
                holder.setText(R.id.file_state, R.string.pause)
            }
            TransferState.START -> {
                val eta = if (entity.speed > 0) FormatUtils.getUptime((entity.fileSize - entity.offset) / entity.speed) else "∞"
                holder.setText(R.id.file_state, FileUtils.fmtFileSize(abs(entity.speed)) + "/s " + eta)
            }
            TransferState.NONE, TransferState.WAIT -> {
                holder.setText(R.id.file_state, R.string.waiting)
            }
            TransferState.FAILED -> {
                holder.setText(R.id.file_state, getFailedInfo(mActivity, entity))
            }
            TransferState.COMPLETE -> {
                holder.setText(R.id.file_state, R.string.completed)
            }
            TransferState.CANCELED -> holder.setText(R.id.file_state, R.string.canceled)
        }
    }

    private fun initCompletedItem(holder: BaseViewHolder, entity: TransferEntityR) {
        holder.setGone(R.id.progress, false)
        holder.setGone(R.id.fileSize, false)
        holder.setGone(R.id.file_state, false)
        val showPath: String
        val toPath = entity.toPath ?: ""
        if (entity.isDownload) {
            showPath = String.format(mActivity.getString(R.string.download_to__), toPath)
        } else {
            var devName = ""
            val deviceModel = SessionManager.getInstance()
                    .getDeviceModel(entity.devId)
            if (deviceModel != null) {
                devName = deviceModel.devName
            }
            devName = "$devName "
            val pathWithTypeName = OneOSFileType.getPathWithTypeName(toPath)
            showPath = String.format(mActivity.getString(R.string.upload_to__), devName, pathWithTypeName)
        }
        holder.setGone(R.id.textView_path, true)
        holder.setText(R.id.textView_path, showPath)
    }


    //其中图片和名称是全部需要加载的
    private fun initEntitySharePart(helper: BaseViewHolder, entity: TransferEntityR) {
        val name = entity.fileName
        helper.setText(R.id.fileName, name)
        val view = helper.getView<ImageView>(R.id.fileImage)
        if (entity.srcPath != null && FileUtils.isPicOrVideo(entity.fileName)) {
            if (entity.isDownload) {
                val loginSession = SessionManager.getInstance().getLoginSession(entity.devId)
                if (loginSession != null && loginSession.isLogin && !entity.isComplete) {
                    val uriString = if (SessionCache.Companion.instance.isV5(loginSession.id!!)) {
                        OneOSAPIs.genThumbnailUrlV5(     loginSession, entity.srcPath)
                    } else {
                        OneOSAPIs.genThumbnailUrl(loginSession, entity.srcPath)
                    }


                    Glide.with(view)
                            .asBitmap()
                            .centerCrop()
                            .placeholder(FileUtils.fmtFileIcon(name))
                            .load(EliCacheGlideUrl(uriString))
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(view)
                } else {
                    Glide.with(view)
                            .asBitmap()
                            .centerCrop()
                            .placeholder(FileUtils.fmtFileIcon(name))
                            .load(entity.toPath + File.separator + entity.fileName)
                            .into(view)
                }
            } else {
                Glide.with(view)
                        .asBitmap()
                        .centerCrop()
                        .placeholder(FileUtils.fmtFileIcon(name))
                        .load(entity.srcPath)
                        .into(view)
            }
        } else {
            helper.setImageResource(R.id.fileImage, FileUtils.fmtFileIcon(name))
        }
    }


}
