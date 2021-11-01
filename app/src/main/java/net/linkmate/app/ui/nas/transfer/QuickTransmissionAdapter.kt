package net.linkmate.app.ui.nas.transfer


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.ui.nas.cloud.FileLoadMoreView
import net.linkmate.app.util.FormatUtils
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.transfer.TransferElement
import net.sdvn.nascommon.model.oneos.transfer.TransferException
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.utils.log.Logger
import java.io.File
import java.util.*
import java.util.concurrent.Executor
import kotlin.math.abs
import kotlin.properties.Delegates

class QuickTransmissionAdapter(private val context: Context) :
        BaseQuickAdapter<MultiItemEntity, BaseViewHolder>(null) {
    private var completedHeaderEntity: HeaderEntity = HeaderEntity("")
    private var processingHeaderEntity: HeaderEntity = HeaderEntity("")

    init {
        mData = mutableListOf()
        setLoadMoreView(FileLoadMoreView())
    }

    private var isDownload: Boolean = false
    private var isResume by Delegates.notNull<Boolean>()


    fun setTransferList(isDownload: Boolean) {
        if (isDownload != this.isDownload) {
//            sDiffExecutor.removeAll()
            mData.clear()
            notifyDataSetChanged()
        }
        this.isDownload = isDownload
    }

    override fun getDefItemViewType(position: Int): Int {
        val item = this.mData[position]
        return item?.itemType ?: -255
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            R.layout.item_line_string -> this.createBaseViewHolder(parent, R.layout.item_line_string)
            R.layout.item_transfer -> {
                ViewHolder(getItemView(R.layout.item_transfer, parent))
            }
            else -> super.onCreateDefViewHolder(parent, viewType)
        }
    }

    fun submit(data: MutableList<TransferEntity>) {
        if (data.isNullOrEmpty()) {
            MainThreadExecutor.execute {
                mData.clear()
                notifyDataSetChanged()
            }
        } else {
            val lastSize = mData.size
            val newData = data.toMutableList()
            val start = System.currentTimeMillis()
            var countPaused = 0
            val runningData = mutableListOf<TransferEntity>()
            val finishData = mutableListOf<TransferEntity>()
            newData.forEach { entity ->
                if (!entity.isComplete()) {
                    if (entity.state() != TransferState.CANCELED) {
                        if (entity.state() == TransferState.PAUSE) {
                            countPaused++
                        }
                        runningData.add(entity)
                    }
                } else {
                    finishData.add(entity)
                }
            }
//            runningData.reverse()
            Logger.LOGD(TAG, "submit data work time consumed : ${System.currentTimeMillis() - start}")
            val startMain = System.currentTimeMillis()
            mData.clear()
            isResume = countPaused > 0
            val countProcessing = runningData.size
            if (runningData.size > 0) {
                processingHeaderEntity.mHeader = context.getString(R.string.processing) + "($countProcessing)"
                processingHeaderEntity.rightResId = if (isResume) R.string.start_all else R.string.pause_all
                mData.add(processingHeaderEntity)
            }
            mData.addAll(runningData)
            if (finishData.size > 0) {
                finishData.reverse()
                completedHeaderEntity.mHeader = context.getString(R.string.complete) + "(${finishData.size})"
                completedHeaderEntity.rightResId = R.string.clear
                mData.add(completedHeaderEntity)
            }
            mData.addAll(finishData)
            val newSize = mData.size
            if (lastSize != newSize) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeChanged(0, itemCount, arrayListOf(1))
            }
            Logger.LOGD(TAG, "submit data main time consumed : ${System.currentTimeMillis() - startMain}")
        }

    }

    fun refreshProcessingStatus() {
        data?.let {
            if (it.isEmpty()) {
                return@let
            }
            val startMain = System.currentTimeMillis()
            var countPaused = 0
            var countRunning = 0
            for (multiItemEntity in it) {
                if (multiItemEntity is TransferEntity && !multiItemEntity.isComplete()) {
                    if (multiItemEntity.state() == TransferState.PAUSE || multiItemEntity.state() == TransferState.FAILED) {
                        countPaused++
                    } else if (multiItemEntity.isStart()) {
                        countRunning++
                    }

                }
            }
            if (countRunning + countPaused > 0) {
                val temp = countPaused > 0
                if (temp != isResume) {
                    isResume = temp
                    processingHeaderEntity.rightResId = if (isResume) R.string.start_all else R.string.pause_all
                    if (mData[0] == processingHeaderEntity) {
                        notifyItemChanged(0)
                    }
                }
            }
            Logger.LOGD(TAG, "status main time consumed : ${System.currentTimeMillis() - startMain}")
        }
    }

    override fun convertPayloads(helper: BaseViewHolder, item: MultiItemEntity?, payloads: MutableList<Any>) {
        if (item != null) {
            convert(helper, item)
        }
    }

    override fun convert(holder: BaseViewHolder, multiItemEntity: MultiItemEntity) {
        when (holder.itemViewType) {
            R.layout.item_transfer -> {
                if (multiItemEntity is TransferEntity && holder is ViewHolder) {
//                    bindData(holder, multiItemEntity)
                    holder.bind(multiItemEntity, /*transferStateObserver*/null)
                    Logger.LOGD(TAG, "onBind ${multiItemEntity.tag}")
                }
            }
            R.layout.item_line_string -> {
                holder.setGone(R.id.more, true)
                if (multiItemEntity == processingHeaderEntity) {
                    holder.itemView.setBackgroundColor(context.resources.getColor(R.color.color_bg_grey50))
                    holder.setText(R.id.header, processingHeaderEntity.mHeader)
                    holder.setText(R.id.more, processingHeaderEntity.rightResId)
                    holder.setTag(R.id.more, isResume)
                } else if (multiItemEntity == completedHeaderEntity) {
                    holder.itemView.setBackgroundColor(context.resources.getColor(R.color.color_bg_grey50))
                    holder.setText(R.id.header, completedHeaderEntity.mHeader)
                    holder.setText(R.id.more, completedHeaderEntity.rightResId)
                    holder.setTag(R.id.more, R.string.clear)
                }
                holder.addOnClickListener(R.id.more)
            }
        }

    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        Logger.LOGD(TAG, "onViewRecycled")
//        if (holder is ViewHolder) {
//            holder.clean()
//        }

    }

//    private val transferStateObserver = TransferElement.TransferStateObserver { tag ->
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "tag: $tag")
//        if (recyclerView != null && recyclerView.isVisible) {
//            recyclerView.post {
//                val layoutManager = recyclerView.layoutManager
//                if (layoutManager is LinearLayoutManager) {
//                    //获取最后一个可见view的位置
//                    val lastItemPosition = layoutManager.findLastVisibleItemPosition()
//                    //获取第一个可见view的位置
//                    val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
//                    for (position in firstItemPosition..lastItemPosition) {
//                        val multiItemEntity = getItem(position)
//                        if (multiItemEntity is TransferEntity) {
//                            if (Objects.equals(multiItemEntity.tag, tag)) {
//                                Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "tag: $tag  position:$position ")
////                                notifyItemChanged(position)
//                                val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
//                                if (viewHolder is ViewHolder) {
//                                    viewHolder.refreshUi(tag)
//                                }
//                                break
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }


    companion object {
        private val TAG = QuickTransmissionAdapter::class.java.simpleName
        private val debug = BuildConfig.DEBUG

        //        private val sDiffExecutor = TransferThreadPool(3, 3)
        fun getFailedInfo(context: Context, mElement: TransferEntity): String? {
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
            }
            return failedInfo
        }
    }

    class ViewHolder(val view: View) : BaseViewHolder(view) {
        private var observer: TransferElement.TransferStateObserver = TransferElement.TransferStateObserver {
            view.post {
                if (view.isAttachedToWindow && view.isActivated
                        && view.isVisible && !view.isInLayout) {
                    refreshUi(it)
                }
            }
        }
        var entity: TransferEntity? = null
            set(value) {
                clean()
                field = value
                if (value != null) {
                    init()
                }
            }

        private fun init() {
            initView()
            refreshUi(entity?.tag)
            entity?.subscribe(observer)
            Logger.LOGD(TAG, "vh init")
        }

        fun clean() {
            entity?.dispose(observer)
            Logger.LOGD(TAG, "vh clean")
        }

        fun initView() {
            entity?.let { entity ->
                val holder = this
                val name = entity.fileName
                holder.setText(R.id.fileName, name)
                val view = holder.getView<ImageView>(R.id.fileImage)
                if (entity.srcPath != null && FileUtils.isPicOrVideo(entity.fileName)) {
                    if (entity.isDownload) {
                        val loginSession = SessionManager.getInstance().getLoginSession(entity.devId)
                        if (loginSession != null && loginSession.isLogin && !entity.isComplete()) {
                            val uriString = OneOSAPIs.genThumbnailUrl(loginSession, entity.srcPath)
                            Logger.LOGD(TAG, "uriString : $uriString")
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
                            Logger.LOGD(TAG, "toPath : ${entity.toPath + File.separator + entity.fileName}")
                        }
                    } else {
                        Logger.LOGD(TAG, "srcPath : ${entity.srcPath}")
                        Glide.with(view)
                                .asBitmap()
                                .centerCrop()
                                .placeholder(FileUtils.fmtFileIcon(name))
                                .load(entity.srcPath)
                                .into(view)
                    }

                } else {
                    holder.setImageResource(R.id.fileImage, FileUtils.fmtFileIcon(name))
                }

            }
        }

        private fun refreshUi(it: Any?) {
            if (Objects.equals(entity?.tag, it))
                entity?.let { entity ->
                    val holder = this
                    val context = holder.itemView.context
                    if (entity.isComplete() || entity.state() == TransferState.COMPLETE) {
                        holder.setGone(R.id.progress, false)
                        holder.setGone(R.id.fileSize, false)
                        holder.setGone(R.id.file_state, false)
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
                            devName = "$devName "
                            val pathWithTypeName = OneOSFileType.getPathWithTypeName(toPath)
                            showPath = String.format(context.getString(R.string.upload_to__), devName, pathWithTypeName)
                        }
                        holder.setGone(R.id.textView_path, true)
                        holder.setText(R.id.textView_path, showPath)
                    } else {
                        holder.setText(R.id.fileSize, FileUtils.fmtFileSize(entity.offset()) +
                                "/" + FileUtils.fmtFileSize(entity.fileSize))
                        val cur = entity.offset()
                        val total = entity.fileSize
                        val progress = if (entity.fileSize == 0L) 0 else {
                            ((cur * 1f / total * 100f) + 0.5f).toInt()
                        }
                        Logger.p(Logger.Level.DEBUG, debug, TAG, "offset : $cur ,total : $total  progress : $progress")
                        //只有在进行时才显示进度
                        holder.setProgress(R.id.progress, progress)
                        holder.setGone(R.id.progress, true)
                        holder.setGone(R.id.textView_path, false)
                        when (entity.state()) {
                            TransferState.PAUSE -> {
                                holder.setText(R.id.file_state, R.string.pause)
                            }
                            TransferState.START -> {
                                val eta = if (entity.getSpeed() > 0) FormatUtils.getUptime((entity.fileSize - entity.offset()) / entity.getSpeed()) else "∞"
                                holder.setText(R.id.file_state, FileUtils.fmtFileSize(abs(entity.getSpeed())) + "/s " + eta)
                            }
                            TransferState.NONE,
                            TransferState.WAIT -> {
                                holder.setText(R.id.file_state, R.string.waiting)
                            }
                            TransferState.FAILED -> {
                                holder.setText(R.id.file_state, getFailedInfo(context, entity))
                            }
                            TransferState.COMPLETE -> {
                                holder.setText(R.id.file_state, R.string.completed)
                            }
                            TransferState.CANCELED -> holder.setText(R.id.file_state, R.string.canceled)
                        }
                    }
                }
        }

        fun bind(multiItemEntity: TransferEntity, transferStateObserver: TransferElement.TransferStateObserver?) {
//            observer = transferStateObserver
            entity = multiItemEntity
        }
    }

    private object MainThreadExecutor : Executor {
        internal val mHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mHandler.post(command)
        }
    }

}
