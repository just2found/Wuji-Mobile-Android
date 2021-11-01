package net.linkmate.app.view.dynamicRefresh

import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.lxj.xpopup.core.BottomPopupView
import kotlinx.android.synthetic.main.layout_publish_dynamic_progress.view.*
import net.linkmate.app.R
import net.linkmate.app.manager.SDVNManager

import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.service.UploadStatus
import net.linkmate.app.util.NetworkUtils
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.vo.Dynamic
import net.sdvn.nascommon.utils.FileUtils

/**
 * @author Raleigh.Luo
 * date：21/3/16 11
 * describe：
 */
class PublishProgressPopup(val activity: AppCompatActivity, val dynamic: Dynamic, val isCurrentPublished: Boolean, val isCanFinished: Boolean = true) : BottomPopupView(activity) {
    override fun getImplLayoutId(): Int {
        return R.layout.layout_publish_dynamic_progress
    }

    override fun initPopupContent() {
        super.initPopupContent()
        btnBack.setOnClickListener {
            if (btnBack.text.toString() == context.getString(R.string.restart_publish) &&
                    (CMAPI.getInstance().isConnected || CMAPI.getInstance().isEstablished)) {//重新发布
                DynamicQueue.push(DynamicQueue.getUploadIdentification()?.identification ?: "")
            }
            if (isCanFinished) activity.finish()
            dismiss()
        }
        btnCancel.setOnClickListener {
            DynamicQueue.cancelPublishDynamic()
            dismiss()
        }
        dynamic.let {
            tvContent.visibility = if (TextUtils.isEmpty(it.Content)) View.GONE else View.VISIBLE
            tvContent.setText(it.Content)
            clImagePanel.isDisplayOneLargerImage = false
            clImagePanel.isConfigListener = false
            val urls = if (it.MediasPO.size > 3) it.MediasPO.subList(0, 3) else it.MediasPO
            clImagePanel.imageUrls = urls
            true
        }

        if (DynamicQueue.mUploadStatus != UploadStatus.NONE
                && DynamicQueue.mUploadStatus != UploadStatus.UPLOADING
                && DynamicQueue.mUploadStatus != UploadStatus.SUCCESS) {//失败，显示失败原因
            tvTitle.setText(DynamicQueue.getUploadIdentification()?.remark)
            btnBack.setText(R.string.restart_publish)
        }

        DynamicQueue.mUploadStatus.observe(activity, Observer {
            when (it) {
                UploadStatus.NONE,
                UploadStatus.UPLOADING,
                UploadStatus.SUCCESS -> {//监听正在发布的进度，
                    if (btnBack.text.toString() == context.getString(R.string.restart_publish)) {
                        tvTitle.setText(R.string.dynamic_is_publishing_title)
                        btnBack.setText(R.string.title_back)
                    }

                    val bytesWritten = DynamicQueue.getUploadIdentification()?.bytesWritten
                            ?: 0L
                    val contentLength = DynamicQueue.getUploadIdentification()?.contentLength
                            ?: 0L
                    if (it == UploadStatus.SUCCESS) {
                        mProgressBar.setProgress(100)
                        tvProgressText.setText(String.format("%s/%s", FileUtils.fmtFileSpeed(mContentLength), FileUtils.fmtFileSpeed(mContentLength)))
                        if (isCurrentPublished) {//是当前发布的，且不是取消发布，关闭页面
                            if (isCanFinished) activity.finish()
                        }
                        dismiss()
                    } else {
                        mContentLength = contentLength
                        val progress = if (contentLength == 0L) 0 else (bytesWritten * 100L) / contentLength
                        mProgressBar.setProgress(progress.toInt())
                        tvProgressText.setText(String.format("%s/%s", FileUtils.fmtFileSpeed(bytesWritten), FileUtils.fmtFileSpeed(contentLength)))
                    }
                }
                else -> {
                    tvTitle.setText(DynamicQueue.getUploadIdentification()?.remark)
//                    tvErrorHint.setText(DynamicQueue.getUploadIdentification()?.remark)
                    btnBack.setText(R.string.restart_publish)
                }
            }
        })
        SDVNManager.instance.liveDataConnectionStatus.observe(activity, Observer {
            val netAvailable = NetworkUtils.checkNetwork(context)
            when (it) {
                Constants.CS_UNKNOWN,
                Constants.CS_PREPARE -> {
                }
                Constants.CS_CONNECTING,
                Constants.CS_WAIT_RECONNECTING -> {
                    if (TextUtils.isEmpty(tvTitle.text.toString())) tvTitle.setText(if (netAvailable) R.string.connecting else R.string.network_not_available)
                }
                Constants.CS_DISCONNECTING -> {
                    if (TextUtils.isEmpty(tvTitle.text.toString())) tvTitle.setText(if (netAvailable) R.string.disconnecting else R.string.network_not_available)
                }
                Constants.CS_ESTABLISHED -> {
                }
                Constants.CS_CONNECTED,
                Constants.CS_AUTHENTICATED -> {
                    if (TextUtils.isEmpty(tvTitle.text.toString())) tvTitle.setText(if (netAvailable) R.string.loading_data else R.string.network_not_available)
                }
                Constants.CS_DISCONNECTED -> {
                    if (TextUtils.isEmpty(tvTitle.text.toString())) tvTitle.setText(if (netAvailable) R.string.disconnected else R.string.network_not_available)
                }
                else -> {
                }
            }

        })
    }

    private var mContentLength = 0L

//    override fun getMaxHeight(): Int {
//        return (XPopupUtils.getWindowHeight(context) * .85f).toInt()
//    }

}