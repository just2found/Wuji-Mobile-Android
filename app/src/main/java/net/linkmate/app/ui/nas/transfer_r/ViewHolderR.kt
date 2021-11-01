package net.linkmate.app.ui.nas.transfer_r

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import net.linkmate.app.R
import net.linkmate.app.ui.nas.transfer.TransferFragment.Companion.TAG
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.transfer.TransferElement
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.log.Logger
import java.io.File
import kotlin.math.abs

class ViewHolderR(val context: Context, val entity: TransferEntityR) {

    var view: View = LayoutInflater.from(context).inflate(R.layout.layout_transfer_des, null)
    var imageView: ImageView
    var fileState: TextView
    var viewPath: TextView
    var negative: TextView
    var positive: TextView
    var fileSize: TextView
    var fileName: TextView
    var progressBar: ProgressBar


    init {
        imageView = view.findViewById(R.id.fileImage)
        fileState = view.findViewById(R.id.file_state)
        viewPath = view.findViewById(R.id.textView_path)
        negative = view.findViewById(R.id.negative)
        progressBar = view.findViewById(R.id.progress)
        positive = view.findViewById(R.id.positive)
        fileName = view.findViewById(R.id.fileName)
        fileSize = view.findViewById(R.id.fileSize)
        initView()
        refreshUi()
    }

    fun initView() {
        fileName.text = entity.fileName
        if (entity.srcPath != null && FileUtils.isPicOrVideo(entity.fileName)) {
            if (entity.isDownload) {
                val loginSession = SessionManager.getInstance().getLoginSession(entity.devId)
                if (loginSession != null && loginSession.isLogin && !entity.isComplete) {
                    val uriString = OneOSAPIs.genThumbnailUrl(loginSession, entity.srcPath)
                    Logger.LOGD(TAG, "uriString : $uriString")
                    Glide.with(view)
                            .asBitmap()
                            .centerCrop()
                            .placeholder(FileUtils.fmtFileIcon(entity.fileName))
                            .load(EliCacheGlideUrl(uriString))
                            .into(imageView)
                } else {
                    Glide.with(view)
                            .asBitmap()
                            .centerCrop()
                            .placeholder(FileUtils.fmtFileIcon(entity.fileName))
                            .load(entity.toPath + File.separator + entity.fileName)
                            .into(imageView)
                    Logger.LOGD(TAG, "toPath : ${entity.toPath + File.separator + entity.fileName}")
                }
            } else {
                Logger.LOGD(TAG, "srcPath : ${entity.srcPath}")
                Glide.with(view)
                        .asBitmap()
                        .centerCrop()
                        .placeholder(FileUtils.fmtFileIcon(entity.fileName))
                        .load(entity.srcPath)
                        .into(imageView)
            }

        } else {
            imageView.setImageResource(FileUtils.fmtFileIcon(entity.fileName))
        }
        val showPath: String
        val toPath = entity.toPath ?: ""
        if (entity.isDownload) {
            showPath = String.format(context.getString(R.string.download_to__), toPath)
        } else {
            var devName = ""
            val deviceModel = SessionManager.getInstance()
                    .getDeviceModel(entity.devId)
            if (deviceModel != null) {
                devName = deviceModel.devName
            }
            devName = "$devName"
            val pathWithTypeName = OneOSFileType.getPathWithTypeName(toPath)
            showPath = String.format(context.getString(R.string.upload_to__), devName, pathWithTypeName)
        }
        viewPath.text = showPath
    }

    fun refreshUi() {
        val cur = entity.offset
        val total = entity.fileSize
        val progress = if (entity.fileSize == 0L) 0 else {
            ((cur * 1f / total * 100f) + 0.5f).toInt()
        }
        //只有在进行时才显示进度
        progressBar.progress = progress
        progressBar.isVisible = false
        if (entity.state != TransferState.START)
            progressBar.visibility = View.GONE

        when (entity.state) {
            TransferState.PAUSE -> {
                fileState.setText(R.string.pause)
                positive.setText(R.string.transfer_continue)
            }
            TransferState.START -> {
                progressBar.visibility = View.VISIBLE
                fileState.text = String.format("%s/s", FileUtils.fmtFileSpeed(abs(entity.speed)))
                positive.setText(R.string.pause)
            }
            TransferState.NONE,
            TransferState.WAIT
            -> {
                fileState.setText(R.string.waiting)
                positive.setText(R.string.pause)

            }
            TransferState.FAILED -> {
                fileState.text = QuickTransmissionAdapterR.getFailedInfo(context, entity)
                positive.setText(R.string.transfer_continue)
            }
            TransferState.COMPLETE -> {
                fileState.setText("")
            }
            else -> {
                fileState.setText(R.string.canceled)
            }
        }

        fileSize.text = FileUtils.fmtFileSize(entity.fileSize)
        positive.setText(R.string.remove)
        negative.setText(R.string.cancel)
    }
}