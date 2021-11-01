package net.sdvn.nascommon.viewmodel

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.ArraySet
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.adapter.QuickTransmissionAdapter
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.transfer.*
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority
import net.sdvn.nascommon.model.oneos.transfer.thread.WorkQueueExecutor
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.MIMETypeUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommonlib.R
import org.view.libwidget.badgeview.DisplayUtil
import timber.log.Timber
import java.util.*

class DownloadFileViewModel : RxViewModel() {
    /**
     * =====================================使用系统分享===========================================
     *
     * @param selectedList
     */
    fun shareByOtherWays(context: Context, selectedList: MutableList<OneOSFile>, loginsession: LoginSession) {
        val oneOSFiles = checkFileDownloaded(selectedList)
        if (oneOSFiles.size > 0) {
            showHasNotDownloadedFile(context, selectedList, loginsession)
        } else {
            if (selectedList.size > 1) {
                val heads: MutableSet<String> = ArraySet()
                val regex = "/[a-z0-9*.-]+"
                val types = HashSet<String>()
                val uris = ArrayList<Uri>()
                var isNeedCalc = true
                for (oneOSFile in selectedList) {
                    val localFile = oneOSFile.localFile
                    if (localFile != null && localFile.exists()) {
                        val uri = FileUtils.getFileProviderUri(localFile)
                        uris.add(uri)
                        if (isNeedCalc) {
                            val type = MIMETypeUtils.getMIMEType(localFile.name)
                            if ("*/*" == type) {
                                types.add(type)
                                isNeedCalc = false
                            }
                            types.add(type)
                            val head = type.replace(regex.toRegex(), "")
                            /*
                             * image/png --\
                             *              |-> (image/ *);
                             * image/jpg --/
                             *
                             * text/plain --\
                             *               |-> (* / *)
                             * image/png  --/
                             *
                             * */if (!heads.contains(head)) {
                                if (heads.size > 0) {
                                    types.clear()
                                    types.add("*/*")
                                    isNeedCalc = false
                                } else {
                                    heads.add(head)
                                    types.add(type)
                                }
                            }
                        }
                    }
                }
                val mulIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
                mulIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                val typeStr = StringBuilder()
                val next = types.iterator().next()
                if (types.size > 1) {
                    val type = next.replaceFirst(regex.toRegex(), "/*")
                    typeStr.append(type)
                } else {
                    typeStr.append(next)
                }
                Logger.LOGD("shareByOtherWays", "type:$typeStr", " uris: $uris")
                mulIntent.type = typeStr.toString()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mulIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(mulIntent, ""))
            } else if (selectedList.size == 1) {
                val intent = Intent(Intent.ACTION_SEND)
                val oneOSFile = selectedList[0]
                val type = MIMETypeUtils.getMIMEType(oneOSFile.localFile!!.name)
                val uri = FileUtils.getFileProviderUri(oneOSFile.localFile!!)
                intent.type = type
                Logger.LOGD("shareByOtherWays", "type:$type", " uri: $uri")
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, ""))
            }
        }
    }

    private fun checkFileDownloaded(selectedList: List<OneOSFile>): ArrayList<OneOSFile> {
        val files = ArrayList<OneOSFile>()
        for (oneOSFile in selectedList) {
            if (!oneOSFile.hasLocalFile()) files.add(oneOSFile)
        }
        return files
    }


    private fun showHasNotDownloadedFile(context: Context, selectedListAll: MutableList<OneOSFile>, loginSession: LoginSession) {
        val selectedList = checkFileDownloaded(selectedListAll)
        val dialog = Dialog(context, R.style.DialogTheme)
        val contentView: View = LayoutInflater.from(context).inflate(R.layout.dialog_show_downloadfile, null)
        val screenHeight = DisplayUtil.getScreenHeight(contentView.context)
        contentView.findViewById<View>(R.id.relativeLayout8).minimumHeight = screenHeight * 2 / 3
        val recyclerView: RecyclerView = contentView.findViewById(R.id.recycle_view)
        val text_title = contentView.findViewById<TextView>(R.id.text_title)
        text_title.visibility = View.VISIBLE
        text_title.setText(R.string.download)
        val share = contentView.findViewById<View>(R.id.positive)
        contentView.findViewById<View>(R.id.spit_line).visibility = View.GONE
        share.visibility = View.GONE
        share.isEnabled = false
        val cancel = contentView.findViewById<View>(R.id.negative)
        val adapter = QuickTransmissionAdapter(recyclerView.context)
        share.setOnClickListener {
            shareByOtherWays(context, selectedListAll, loginSession)
            dialog.dismiss()
        }
        cancel.setOnClickListener { dialog.cancel() }
        recyclerView.adapter = adapter
        //        recyclerView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(recyclerView.getContext()));
        val layout: RecyclerView.LayoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.layoutManager = layout
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, layout.layoutDirection))
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, params)
        //        int totalHeight  = recyclerView.computeVerticalScrollRange();
//        int maxHeight = Utils.getWindowsSize(mActivity, false) * 2 / 3;
//        totalHeight = totalHeight > maxHeight ? maxHeight : totalHeight;
//        ViewGroup.LayoutParams params1 = recyclerView.getLayoutParams();
//        params1.height = totalHeight;
//        recyclerView.setLayoutParams(params1);
        dialog.setCancelable(false)
        dialog.show()
        val elements = ArrayList<TransferElement>(selectedList.size)
        val hashcodes = HashMap<Int, OneOSFile>()
        val listeners: OnTransferResultListener<DownloadElement> = OnTransferResultListener<DownloadElement> { element ->
            if (element.state == TransferState.COMPLETE) {
                val data = adapter.data
                var isAllComplete = true
                for (oneOSFile in selectedListAll) {
                    if (oneOSFile.getAllPath() == element.srcPath) {
                        oneOSFile.localFile = element.downloadFile
                    }
                }
                for (transferElement in data) {
                    if (transferElement.state != TransferState.COMPLETE) {
                        isAllComplete = false
                        break
                    }
                }
                if (isAllComplete) {
//                        share.setEnabled(true);
                    if (dialog.isShowing) {
                        shareByOtherWays(context, selectedListAll, loginSession)
                        dialog.dismiss()
                    }
                }
            }
        }
        val processor = PublishProcessor.create<TransferElement>()
        val workQueueExecutorImpl = WorkExecutor(Schedulers.io())
        val subscribe = processor
                .subscribeOn(Schedulers.single())
                .observeOn(Schedulers.single())
                .subscribe({
                    Timber.d("shareByOtherWays Thread -" + Thread.currentThread())
                    val downloadElement = it as DownloadElement
                    downloadElement.state = TransferState.NONE
                    DownloadFileTask(downloadElement, listeners, workQueueExecutorImpl).start()
                }) { throwable: Throwable? ->

                }
        addDisposable(subscribe)
        val disposable = Observable.create<Boolean> { emitter -> //暂停当前正在下载的
            var priority = Priority.UI_NORMAL + selectedList.size
            val toDevId: String = loginSession.id!!
            //添加当前未下载的文件
            for (file in selectedList) {
                val savePath = SessionManager.getInstance().getDefaultDownloadPathByID(toDevId, file)
                val element = DownloadElement(file, savePath)
                if (loginSession.isLogin) {
                    if (file.isPicture || file.isVideo || file.isGif) element.thumbUri = Uri.parse(OneOSAPIs.genThumbnailUrl(loginSession, file))
                }
                element.srcDevId = toDevId
                element.priority = priority--
                element.time = System.currentTimeMillis()
                hashcodes[element.hashCode()] = file
                elements.add(element)
            }
            elements.sortWith(Comparator { o1, o2 ->
                if (o1 != null && o2 != null) {
                    o2.priority - o1.priority
                } else 0
            })
            emitter.onNext(true)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isSuccess ->
                    if (isSuccess) {
                        adapter.setTransferList(elements, true)
                        for (element in elements) {
                            processor.onNext(element)
                        }
                    }
                }
        addDisposable(disposable)
        adapter.setOnControlListener(object : OnTransferControlListener {
            override fun onPause(element: TransferElement) {
                element.state = TransferState.PAUSE
            }

            override fun onContinue(element: TransferElement) {
                processor.onNext(element)
            }

            override fun onRestart(element: TransferElement) {
                element.state = TransferState.PAUSE
                processor.onNext(element)
            }

            override fun onCancel(element: TransferElement) {
                element.state = TransferState.PAUSE
                val iterator = selectedListAll.iterator()
                while (iterator.hasNext()) {
                    val osFile = iterator.next()
                    if (osFile != null) {
                        if (osFile.getAllPath() == element.srcPath) iterator.remove()
                    }
                }
                val data = adapter.data
                val index = data.indexOf(element)
                if (index >= 0 && index < data.size) adapter.remove(index)
                ToastHelper.showToast(context.getString(R.string.cancel_download).toString() + " " + element.srcName)
            }
        })
        dialog.setOnDismissListener {
            elements.forEach { it.state = TransferState.PAUSE }
            clear()
            workQueueExecutorImpl.release()
        }
    }

    inner class WorkExecutor(private val scheduler: Scheduler) : WorkQueueExecutor {
        private val weakHashMap = WeakHashMap<Runnable, Disposable>()
        override fun execute(task: Runnable) {
            weakHashMap[task] = scheduler.scheduleDirect(task)
        }

        override fun remove(task: Runnable) {
            weakHashMap[task]?.dispose()
        }

        fun release() {
            weakHashMap.values?.forEach { it.dispose() }
        }
    }
}