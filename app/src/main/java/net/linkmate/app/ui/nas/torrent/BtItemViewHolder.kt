package net.linkmate.app.ui.nas.torrent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import io.weline.repo.torrent.BTHelper
import io.weline.repo.torrent.constants.BTResultCode
import io.weline.repo.torrent.constants.BTStatus
import io.weline.repo.torrent.constants.BT_Config
import io.weline.repo.torrent.data.BTItem
import net.linkmate.app.R
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.utils.FileUtils
import kotlin.math.abs

class BtItemViewHolder(val context: Context, val btItem: BTItem) {
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
        progressBar.isVisible = false
        initView()
//        refreshUi()
    }


    fun initView() {
        fileName.text = btItem.name
        imageView.setImageResource(R.drawable.icon_bt_blue)
        val showPath: String
        val toPath = BT_Config.DEFAULT_DOWNLOAD_PATH
        var devName = ""
        val deviceModel = SessionManager.getInstance()
                .getDeviceModel(btItem.devId)
        if (deviceModel != null) {
            devName = deviceModel.devName
        }
        devName = "$devName "
        val pathWithTypeName = if (BTHelper.isLocal(btItem.devId)) {
            ""
        } else {
            OneOSFileType.getPathWithTypeName(toPath)
        }
        showPath = String.format(context.getString(R.string.download_to__), devName, pathWithTypeName)
        viewPath.text = ""
        val total = btItem.totalLen
        fileSize.text = FileUtils.fmtFileSize(total)
        positive.setText(R.string.remove)
        negative.setText(R.string.cancel)
    }

    private fun refreshUi() {
        val cur = btItem.downloadLen
        val total = btItem.totalLen
        val progress = if (btItem.totalLen == 0L) 0 else {
            ((cur * 1f / total * 100f) + 0.5f).toInt()
        }
        //只有在进行时才显示进度
        progressBar.progress = progress

        when (btItem.status) {
            BTStatus.STOPPED -> {
                fileState.setText(R.string.pause)
                positive.setText(R.string.transfer_continue)
            }
            BTStatus.DOWNLOADING -> {
                fileState.text = String.format("%s/s", FileUtils.fmtFileSpeed(abs(btItem.speed)))
                positive.setText(R.string.pause)
            }
            BTStatus.ERROR -> {
                fileState.text = BTResultCode.getErrorString(context, btItem)
                positive.setText(R.string.transfer_continue)
            }
            BTStatus.COMPLETE -> {
                fileState.setText("")
            }
            else -> {
                fileState.setText(R.string.canceled)
            }
        }
        if (btItem.status == BTStatus.COMPLETE) {
            fileSize.text = FileUtils.fmtFileSize(total)
            positive.setText(R.string.remove)
            negative.setText(R.string.cancel)
            progressBar.visibility = View.GONE
        } else {
            fileSize.text = String.format("%s/%s", FileUtils.fmtFileSize(cur),
                    FileUtils.fmtFileSize(total))
            negative.setText(R.string.remove)
            progressBar.visibility = View.VISIBLE
        }

    }
}