package net.linkmate.app.ui.nas.helper

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_file_view.*
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.SPHelper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.model.oneos.transfer.*
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority
import net.sdvn.nascommon.model.oneos.transfer.thread.WorkQueueExecutorImpl
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.service.NasService
import net.sdvn.nascommon.utils.*
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.widget.TitleBackLayout
import java.io.File


/** 

Created by admin on 2020/7/24,11:20

 */
class FileViewFragment : TipsBaseFragment(), View.OnClickListener {
    override fun getLayoutResId(): Int {
        return R.layout.activity_file_view
    }

    override fun getTopView(): View? {
        return layout_title
    }

    var mIVBtnMore: ImageView? = null
    var mIVFileIcon: ImageView? = null
    var mIVFileOperateCancel: ImageView? = null
    var mTVFileName: TextView? = null
    var mTVFileSize: TextView? = null
    var mTVFileOperate: TextView? = null
    var mLLContainerProgress: LinearLayout? = null
    var mPB: ProgressBar? = null
    var mTitleLayout: TitleBackLayout? = null
    var mFile: OneOSFile? = null
    var isLoading = false
    var rootView: View? = null
    private var mDownloadThread: WorkQueueExecutorImpl? = null
    var mDownloadElement: DownloadElement? = null
    private var downloadFileTask: DownloadFileTask? = null
    private var mState = TransferState.NONE
    private var isFoundInTransmission = false
    private var mService: NasService? = null
    private val liveData = MutableLiveData<Resource<DownloadElement>>()
    var groupId:Long=-1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveData.observe(this, Observer {
            if (it.status == Status.LOADING) {
                runStart.run()
            } else if (it.status == Status.SUCCESS) {
                if (it.data?.state == TransferState.START) {
                    actionDownloading.run()
                } else {
                    actionStop.run()
                    if (it.data?.state == TransferState.COMPLETE)
                        openFile()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        arguments?.let {
            val file = it.getSerializable(key_file) as? OneOSFile
            groupId= it.getLong(FileViewActivity.key_groupId)
            if (file != null && !TextUtils.isEmpty(devId)) {
                mFile = file
                mIVFileIcon!!.setImageResource(FileUtils.fmtFileIcon(mFile!!.getName()))
                mTVFileName!!.text = mFile!!.getName()
                mTitleLayout!!.setBackTitle(mFile!!.getName())
                mTVFileSize!!.text = FileUtils.fmtFileSize(mFile!!.getSize())
                var offset = 0L
                val loginSession = SessionManager.getInstance().getLoginSession(devId)
                val tmpName = "." + file.getName() + "_" + file.getTime() + AppConstants.TMP
                val defaultDownloadPath = SessionManager.getInstance().getDefaultDownloadPathByID(devId, file)
                val localPath = defaultDownloadPath + File.separator + tmpName
                if (file.isExtract) {
                    mTVFileOperate!!.setText(R.string.online_extract)
                } else {
                    val tmpFile = File(localPath)
                    if (tmpFile.exists() && tmpFile.isFile && tmpFile.length() < mFile!!.getSize()) {
                        mTVFileOperate!!.setText(R.string.continue_download)
                        offset = tmpFile.length()
                    } else {
                        mTVFileOperate!!.setText(R.string.download_file)
                    }
                    mService = SessionManager.getInstance().service
                    if (mService != null) {
                        val downloadList = mService!!.downloadList
                        for (element in downloadList) {
                            if (element.srcDevId == devId &&
                                    element.srcPath == file.getAllPath()) {
                                mDownloadElement = element
                                if(groupId>0) {
                                    mDownloadElement?.setGroup(groupId)
                                }
                                break
                            }
                        }
                        if (mDownloadElement != null) {
                            mDownloadElement!!.addTransferStateObserver { notifyDataChanged() }
                            notifyDataChanged()
                            isFoundInTransmission = true
                        }
                    }
                    if (mDownloadElement == null) {
                        mDownloadElement = DownloadElement(mFile!!, defaultDownloadPath, offset, tmpName)
                      if(groupId>0) {
                          mDownloadElement?.setGroup(groupId)
                      }
                        if (loginSession != null) {
                            mDownloadElement!!.srcDevId = loginSession.id
                        }
                    }
                }
            } else {
                ToastHelper.showToast(R.string.operate_failed)
                requireActivity().finish()
            }
        }
    }

    override fun initView(view: View) {
        mTipsBar = tipsBar
        rootView = view.findViewById<View>(R.id.layout_main)
        mTitleLayout = view.findViewById(R.id.layout_title)
        mIVBtnMore = view.findViewById(R.id.iv_more)
        mIVFileIcon = view.findViewById(R.id.file_view_iv_icon)
        mIVFileOperateCancel = view.findViewById(R.id.file_view_iv_cancel)
        mTVFileName = view.findViewById(R.id.file_view_tv_file_name)
        mTVFileSize = view.findViewById(R.id.file_view_tv_file_size)
        mTVFileOperate = view.findViewById(R.id.file_view_tv_file_operate)
        mLLContainerProgress = view.findViewById(R.id.file_view_ll)
        mPB = view.findViewById(R.id.file_view_pb)
        mIVBtnMore?.setVisibility(View.GONE)
        mTitleLayout?.setOnClickBack(requireActivity())
        mIVBtnMore?.setOnClickListener(this)
        mTVFileOperate?.setOnClickListener(this)
        mIVFileOperateCancel?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val iv_more = R.id.iv_more
        val fileViewTvFileOperate = R.id.file_view_tv_file_operate
        val fileViewIvCancel = R.id.file_view_iv_cancel
        if (v.id == fileViewTvFileOperate) { //                gotoDownload(loginSession, this, OneOSAPIs.genOpenUrl(loginSession, mFile), mFile.getName(), mFile.getSize());
            if (mFile!!.isExtract) {
                SessionManager.getInstance().getLoginSession(devId!!, object : GetSessionListener() {
                    override fun onSuccess(url: String, loginSession: LoginSession) {
                        rootView?.let {
                            val oneOSFileManage = OneOSFileManage(activity, null, loginSession, it, OneOSFileManage.OnManageCallback { })
                            oneOSFileManage.doOnlineExtract(mFile)
                        }
                    }
                })
            } else {
                if (mDownloadElement!!.state == TransferState.COMPLETE) {
                    openFile()
                } else {
                    if (!Utils.isWifiAvailable(requireContext()) && SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
                        DialogUtils.showConfirmDialog(requireContext(), R.string.tips, R.string.confirm_download_not_wifi, R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
                            if (isPositiveBtn) {
                                downloadFile()
                            }
                        }
                    } else {
                        downloadFile()
                    }
                }
            }
        } else if (v.id == fileViewIvCancel) { //                pauseDownload();
//                isLoading = false;
            stopDownloadFile()
        }
    }

    private fun openFile() {
        val file = mDownloadElement!!.downloadFile
        if (file.exists()) {
            FileUtils.openLocalFile(requireContext(), file);
        } else {
            ToastHelper.showLongToast(R.string.file_not_found)
        }
    }

    class InstallResultReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                Logger.LOGD(TAG, intent)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            Logger.LOGD(TAG, it)
        }
    }

    private fun downloadFile() {
        PermissionChecker.checkPermission(requireContext(), Callback { o: List<String?>? ->
            if (isFoundInTransmission) {
                if (mService != null) {
                    if (mDownloadElement != null) {
                        mDownloadElement!!.priority = Priority.UI_NORMAL
                        mService!!.continueDownload(mDownloadElement!!.tag)
                    }
                }
            } else {
                mDownloadThread = WorkQueueExecutorImpl(1)
                downloadFileTask = DownloadFileTask(mDownloadElement, mListener, mDownloadThread)
                mDownloadElement!!.state = TransferState.NONE
                downloadFileTask!!.start()
            }
        }, Callback { o: List<String?>? -> UiUtils.showStorageSettings(requireContext()) }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun stopDownloadFile() {
        if (isFoundInTransmission) {
            if (mService != null) {
                mService!!.pauseDownload(mDownloadElement!!.tag)
            }
        } else {
            if (downloadFileTask != null) {
                downloadFileTask!!.stopDownload()
                downloadFileTask = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (downloadFileTask != null) {
            downloadFileTask!!.cancel()
        }
        if (mDownloadThread != null) {
            mDownloadThread!!.release()
        }
        isLoading = false
    }

    fun notifyDataChanged() {
        if (mDownloadElement!!.state == TransferState.START
                || mDownloadElement!!.state == TransferState.WAIT) {
            if (mState != mDownloadElement!!.state) {
                requireActivity().runOnUiThread(runStart)
            }
            requireActivity().runOnUiThread(actionDownloading)
        } else {
            requireActivity().runOnUiThread(actionStop)
        }
        mState = mDownloadElement!!.state
    }

    var runStart = Runnable {
        mTVFileOperate!!.visibility = View.GONE
        mLLContainerProgress!!.visibility = View.VISIBLE
        mPB!!.max = (mFile!!.getSize() / 1024).toInt()
        mPB!!.progress = (mDownloadElement!!.offset / 1024).toInt()
    }
    var actionDownloading = Runnable { mPB!!.progress = (mDownloadElement!!.length / 1024).toInt() }
    var actionStop = Runnable {
        if (mDownloadElement!!.state == TransferState.COMPLETE) {
//            openFile()
            //MyApplication.getAppContext().startActivity(Intent.createChooser(intent, "标题"));
            mTVFileOperate!!.setText(R.string.open)
            MediaScanner.getInstance().scanningFile(mDownloadElement!!.toPath + File.separator + mDownloadElement!!.toName)
        }
        if (mDownloadElement!!.state == TransferState.FAILED) {
            ToastHelper.showToast(getFailedInfo(mDownloadElement!!))
        }
        if (mDownloadElement!!.state == TransferState.PAUSE) mTVFileOperate!!.setText(R.string.continue_download)
        mLLContainerProgress!!.visibility = View.GONE
        mTVFileOperate!!.visibility = View.VISIBLE
    }
    private val mListener: OnTransferFileListener<DownloadElement> = object : OnTransferFileListener<DownloadElement> {
        override fun onStart(url: String, element: DownloadElement) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG, "Download start" + element.length + " offset : " + element.offset)
            if (element == mDownloadElement) {
                liveData.postValue(Resource.loading(element))
            }
        }

        override fun onTransmission(url: String, element: DownloadElement) { //            Logger.p(Level.DEBUG, Logd.DOWNLOAD, TAG, "Downloading " + element.getLength());
            if (element == mDownloadElement) {
                liveData.postValue(Resource.success(element))
            }
        }

        override fun onComplete(url: String, element: DownloadElement) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG, "Download " + element.length)
            if (element == mDownloadElement) {
                liveData.postValue(Resource.success(element))
            }
        }
    }

    private fun getFailedInfo(mElement: TransferElement): String? {
        var failedInfo: String? = null
        val context = requireContext().applicationContext
        if (!Utils.isWifiAvailable(context)) {
            mElement.exception = TransferException.WIFI_UNAVAILABLE
        }
        val failedId = mElement.exception
        if (failedId == TransferException.NONE) {
            return null
        } else if (failedId == TransferException.LOCAL_SPACE_INSUFFICIENT) {
            failedInfo = context.resources.getString(R.string.local_space_insufficient)
        } else if (failedId == TransferException.SERVER_SPACE_INSUFFICIENT) {
            failedInfo = context.resources.getString(R.string.server_space_insufficient)
        } else if (failedId == TransferException.FAILED_REQUEST_SERVER) {
            failedInfo = context.resources.getString(R.string.request_server_exception)
        } else if (failedId == TransferException.ENCODING_EXCEPTION) {
            failedInfo = context.resources.getString(R.string.decoding_exception)
        } else if (failedId == TransferException.IO_EXCEPTION) {
            failedInfo = context.resources.getString(R.string.io_exception)
        } else if (failedId == TransferException.FILE_NOT_FOUND) {
            failedInfo = context.resources.getString(R.string.file_not_found)
        } else if (failedId == TransferException.SERVER_FILE_NOT_FOUND) {
            failedInfo = context.resources.getString(R.string.file_not_found)
        } else if (failedId == TransferException.UNKNOWN_EXCEPTION) {
            failedInfo = context.resources.getString(R.string.unknown_exception)
        } else if (failedId == TransferException.SOCKET_TIMEOUT) {
            failedInfo = context.resources.getString(R.string.socket_timeout)
        } else if (failedId == TransferException.WIFI_UNAVAILABLE) {
            failedInfo = context.resources.getString(R.string.wifi_connect_break)
        }
        return failedInfo
    }

    companion object {
        private const val key_file = "file"
        fun newInstance(devId: String?, file: OneOSFile,groupId:Long=-1): FileViewFragment {
            val args = Bundle()
            if (!devId.isNullOrEmpty()) {
                args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
                args.putSerializable(key_file, file)
                args.putLong(FileViewActivity.key_groupId,groupId)
            }
            val fragment = FileViewFragment()
            fragment.arguments = args
            return fragment
        }

        private val TAG = FileViewFragment::class.java.simpleName
    }
}