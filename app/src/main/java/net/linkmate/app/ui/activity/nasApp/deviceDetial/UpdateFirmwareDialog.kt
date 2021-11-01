package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ProgressBar
import net.linkmate.app.R

/**
 * @author Raleigh.Luo
 * date：20/7/31 17
 * describe：
 */
class UpdateFirmwareDialog(context: Context): Dialog(context, R.style.DialogTheme)  {
    private var mProgressBarDownload: ProgressBar? = null
    private var mProgressBarInstall: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LayoutInflater.from(getContext()).inflate(R.layout.layout_upgrade_progress, null))
        mProgressBarDownload = findViewById(R.id.progressBar_download)
        mProgressBarInstall =  findViewById(R.id.progressBar_install)
    }
    fun setProgress( progress:Int){
        mProgressBarDownload?.setProgress(progress)
    }
    fun setInstallProgress( progress:Int){
        mProgressBarInstall?.setProgress(progress)
    }
}