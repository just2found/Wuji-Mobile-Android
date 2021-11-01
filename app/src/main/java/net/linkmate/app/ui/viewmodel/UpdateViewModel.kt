package net.linkmate.app.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import libs.source.common.AppExecutors.Companion.instance
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.base.MyConstants
import net.linkmate.app.service.UpdateService
import net.linkmate.app.util.*
import net.sdvn.app.config.AppConfig
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.SystemInformation
import net.sdvn.common.Local
import net.sdvn.common.internet.OkHttpClientIns
import net.sdvn.common.internet.protocol.UpdateInfo
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.utils.PermissionChecker
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.widget.NumberProgressBar
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.view.libwidget.MagicTextViewUtil
import timber.log.Timber
import java.io.*
import java.net.InetAddress
import java.util.*

class UpdateViewModel : ViewModel() {
    private val _resourceLiveData = MutableLiveData<Resource<UpdateInfo>>()
    private val resourceLiveData: LiveData<Resource<UpdateInfo>> = _resourceLiveData
    private var dialog: AlertDialog? = null
    fun checkAppVersion(byUser: Boolean): LiveData<Resource<UpdateInfo>>? {
        Thread(Runnable {
            Timber.d("check update start")
            if (NetworkUtils.checkNetwork(MyApplication.getContext()) && !byUser && !checkLastCheck(MyApplication.getContext())) {//非用户操作并且小于间隔查询时间
                Resource.error(MyApplication.getContext().resources.getString(R.string.update_fail), null)
                return@Runnable;
            }
            var fastDomain: String? = AppConfig.host
            try {
                val updateJson = MySPUtils.getString(MyApplication.getContext(), MySPUtils.UPDATE_INFO, "")
                val domains: MutableList<String> = ArrayList()
                if (!TextUtils.isEmpty(updateJson)) {
                    val updateResult = Gson().fromJson(updateJson, UpdateInfo::class.java)
                    if (updateResult.domains != null) {
                        domains.addAll(updateResult.domains)
                    }
                }
                if (domains.size == 0) {
                    domains.add(AppConfig.host_cn)
                    domains.add(AppConfig.host_us)
                }
                var minAvg: Long = 100000
                for (domain in domains) {
                    if (!TextUtils.isEmpty(domain)) {
                        try {
                            val start = System.currentTimeMillis()
                            val isReachable = InetAddress.getByName(domain).isReachable(10000)
                            val end = System.currentTimeMillis()
                            Timber.d("%s \nreachable:%s  %s ", domain, isReachable, end - start)
                            if (isReachable) {
                                val time = end - start
                                if (time < minAvg) {
                                    fastDomain = domain
                                    minAvg = time
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                        //                            String res = PingUtil.ping("ping -c 3 -W 3 " + domain);
//                            int avg = PingUtil.getAvgRTT(res);
//                            if (avg != -1 && avg < minAvg) {
//                                minAvg = avg;
//                                fastDomain = domain;
//                            }
                    }
                }
                var postBody =
                        "{" + "\"partnerid\":\"" + MyConstants.CONFIG_PARTID +
                                "\"," + "\"appid\":\"" + MyConstants.CONFIG_APPID +
                                "\"," + "\"deviceclass\":" + MyConstants.CONFIG_DEV_CLASS +
                                "," + "\"version\":\"" + "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"
                if (CMAPI.getInstance().baseInfo != null &&
                        !TextUtils.isEmpty(CMAPI.getInstance().baseInfo.userId)) {
                    postBody += "\"," + "\"userid\":\"" + CMAPI.getInstance().baseInfo.userId
                }
                if (!TextUtils.isEmpty(SystemInformation.os_version)) {
                    postBody += "\"," + "\"andriodver\":\"" + SystemInformation.os_version
                }
//                val curLocale = MyApplication.getContext().resources.configuration.locale
//                val language = curLocale.language
//                val script = curLocale.script
//                val country = curLocale.country
//                val lang: String
//                if ("zh" == language && ("cn" != country.toLowerCase() || "hant" == script.toLowerCase())) {
//                    lang = "tw"
//                } else {
//                    lang = language
//                }
//                if (!TextUtils.isEmpty(lang)) {
                    postBody += "\",\"lang\":\"${Local.getApiLanguage()}"
//                }
                postBody += "\"}"
                Timber.d("update request:%s", postBody)
                val request = Request.Builder()
                        .url(MyConstants.UPDATE_URL.replace("\$BASEURL$", fastDomain!!))
                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postBody))
                        .build()
                Timber.d("update request:%s", request.toString())
                val client = OkHttpClientIns.getApiClient();
                val response = client.newCall(request).execute()
                Timber.d("update response:%s", response)
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val json = response.body()!!.string()
                        val updateResult = Gson().fromJson(json, UpdateInfo::class.java)
                        Timber.d("update result:%s", updateResult.toString())
                        if (updateResult != null) {
                            _resourceLiveData.postValue(Resource.success(updateResult))
                            if (updateResult.result == 0) {
                                MySPUtils.saveString(MyApplication.getContext(), MySPUtils.UPDATE_INFO, json)
                            }
                            return@Runnable
                        }
                    }
                }
            } catch (ignore: Exception) {
            }
            _resourceLiveData.postValue(Resource.error(MyApplication.getContext().resources.getString(R.string.update_fail), null))
        }).start()
        return resourceLiveData
    }

    private fun checkLastCheck(context: Context): Boolean {
        val last = SPUtils.getValue(context, KEY_LAST_CHECK_UPDATE, "0")
        if (TextUtils.isEmpty(last)) return false
        try {
            val lastTime = last!!.toLong()
            if (!DateUtils.isToday(lastTime)) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun beginUpdate(updateInfo: UpdateInfo, context: Context) {
        PermissionChecker.checkPermission(context, Callback { strings: List<String?>? ->
            val filesModel = updateInfo.files[0]
            val downloadurl = filesModel.downloadurl
            if (updateInfo.isEnabled && !TextUtils.isEmpty(downloadurl)) {
                val service = Intent(context, UpdateService::class.java)
                service.putExtra(UpdateService.KEY_DOWNLOAD_URL, downloadurl)
                if (!TextUtils.isEmpty(filesModel.hash)) {
                    service.putExtra(UpdateService.KEY_DOWNLOAD_HASH, filesModel.hash)
                    SPUtils.setValue(context, KEY_LAST_CHECK_UPDATE_HASH, filesModel.hash)
                }
                context.startService(service)
            } else {
                ToastUtils.showToast(context.getString(R.string.update_fail))
            }
        },
                Callback { permissions: List<String?> ->
                    if (permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                            permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ToastUtils.showToast(context.getString(R.string.pls_grant_storage_permission))
                    }
                },
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @SuppressLint("SetTextI18n")
    fun showUpdateDialog(updateInfo: UpdateInfo, context: Context?) {
        if (context == null) {
            Logger.LOGD(TAG, "showUpdateDialog context can't be null")
            return
        }
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        SPUtils.setValue(context, KEY_LAST_CHECK_UPDATE, System.currentTimeMillis().toString())
        val builder = AlertDialog.Builder(context, R.style.DialogThemeAlert)
        builder.setCancelable(false)
        val view = LayoutInflater.from(builder.context).inflate(R.layout.layout_update_info, null)
        val ivUpdate = view.findViewById<ImageView>(R.id.txt_update)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvVision = view.findViewById<TextView>(R.id.tv_version)
        val tvFeatureHead = view.findViewById<TextView>(R.id.tv_feature_head)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val tvNegative = view.findViewById<View>(R.id.negative)
        val tvPositive = view.findViewById<TextView>(R.id.positive)
        val groupProgress = view.findViewById<View>(R.id.file_view_ll)
        val progressBar: NumberProgressBar = view.findViewById(R.id.file_view_pb)
        val viewCancel = view.findViewById<View>(R.id.file_view_iv_cancel)
        tvPositive.setText(R.string.upgrade_app_now)
        //        tvNegative.setText(R.string.upgrade_next_time);
        tvTitle.setText(R.string.strNotificationHaveNewVersion)
        tvFeatureHead.setText("${context.getString(R.string.strUpgradeDialogFeatureLabel)} :")
        tvVision.setText("V ${updateInfo.version}")
        val message = StringBuilder()
        when {
            UiUtils.isHans() -> {
                message.append(updateInfo.changelogchs)
            }
            UiUtils.isHant() -> {
                message.append(updateInfo.changelogcht)
            }
            else -> {
                message.append(updateInfo.changelogen)
            }
        }
        tvContent.text = message
        builder.setView(view)
        dialog = builder.show()
        tvNegative.setOnClickListener { dialog?.dismiss() }
        tvPositive.setOnClickListener {
            //                dialog.dismiss();
//                beginUpdate(updateInfo, context);

            PermissionChecker.checkPermission(context, Callback { strings: List<String?>? ->
                localInstall(updateInfo).observeForever {
                    when (it.status) {
                        Status.LOADING -> {
                            val downloadPercent = it.data as? Int ?: 0
                            tvPositive.visibility = View.INVISIBLE
                            tvNegative.visibility = View.INVISIBLE
                            groupProgress.visibility = View.VISIBLE
                            progressBar.progress = downloadPercent
                        }
                        Status.SUCCESS -> {
                            OpenFiles.openFile(context, it.data as File)
                            dialog?.dismiss()
                        }
                        else -> {
                            tvPositive.visibility = View.VISIBLE
                            tvNegative.visibility = View.VISIBLE
                            tvPositive.setText(R.string.click_to_retry)
                            groupProgress.visibility = View.GONE
                        }
                    }
                }

            },
                    Callback { permissions: List<String?>? ->
                        if (permissions?.contains(Manifest.permission.READ_EXTERNAL_STORAGE) == true ||
                                permissions?.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) == true) {
                            ToastUtils.showToast(context.getString(R.string.pls_grant_storage_permission))
                        }
                    },
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        }
    }

    private fun localInstall(updateInfo: UpdateInfo): MutableLiveData<Resource<*>> {
        val filesModel = updateInfo.files[0]
        val liveData = MutableLiveData<Resource<*>>()
        liveData.postValue(Resource.loading(0))
        instance.networkIO().execute {
            var `is`: InputStream? = null
            var bis: BufferedInputStream? = null
            var fos: FileOutputStream? = null
            var bos: BufferedOutputStream? = null
            var downloadPercent = 0
            try {
                val downloadFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + filesModel.filename)
                if (downloadFile.exists()) {
                    if (FileUtils.getFileMd5(downloadFile) != filesModel.hash) {
                        downloadFile.delete()
                    } else {
                        downloadPercent = 100
                    }
                }
                if (downloadPercent < 100) {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                            .url(filesModel.downloadurl)
                            .build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val length = response.body()!!.contentLength()
                    `is` = response.body()!!.byteStream()
                    downloadFile.createNewFile()
                    bis = BufferedInputStream(`is`)
                    fos = FileOutputStream(downloadFile)
                    bos = BufferedOutputStream(fos)
                    var read: Int
                    var count: Long = 0
                    var precent = 0
                    val buffer = ByteArray(1024)
                    while (-1 != bis.read(buffer).also { read = it }) {
                        bos.write(buffer, 0, read)
                        count += read.toLong()
                        precent = (count.toDouble() / length * 100).toInt()
                        //每下载完成1%就通知任务栏进行修改下载进度
                        if (precent - downloadPercent >= 1) {
                            downloadPercent = precent
                            liveData.postValue(Resource.loading(downloadPercent))
                        }
                    }
                }
                if (downloadPercent == 100) {
                    liveData.postValue(Resource.success(downloadFile))
                } else {
                    liveData.postValue(Resource.error("download failed", downloadPercent))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                liveData.postValue(Resource.error(e.message ?: "download failed", downloadPercent))
            } finally {
                try {
                    if (bos != null) {
                        bos.flush()
                        bos.close()
                    }
                    if (fos != null) {
                        fos.flush()
                        fos.close()
                    }
                    bis?.close()
                    `is`?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return liveData
    }

    companion object {
        val TAG = UpdateViewModel::class.java.simpleName
        const val KEY_LAST_CHECK_UPDATE = "KEY_LAST_CHECK_UPDATE"
        const val KEY_LAST_CHECK_UPDATE_HASH = "KEY_LAST_CHECK_UPDATE_HASH"
        private val INTERVALS_TIME = if (BuildConfig.DEBUG) 2 * 60 * 1000 else 24 * 60 * 60 * 1000.toLong()
    }
}