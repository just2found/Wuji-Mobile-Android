//package io.weline.mediaplayer.player
//
//import android.app.Activity
//import android.app.AlertDialog
//import android.content.Context
//import android.content.DialogInterface
//import android.net.Uri
//import android.os.Handler
//import android.os.HandlerThread
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.ArrayAdapter
//import android.widget.ListView
//import android.widget.Toast
//import com.google.android.exoplayer2.C
//import com.google.android.exoplayer2.offline.*
//import com.google.android.exoplayer2.offline.DownloadAction.Deserializer
//import com.google.android.exoplayer2.offline.DownloadManager.TaskState
//import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper
//import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper
//import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper
//import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
//import com.google.android.exoplayer2.ui.TrackNameProvider
//import com.google.android.exoplayer2.upstream.DataSource
//import com.google.android.exoplayer2.util.Log
//import com.google.android.exoplayer2.util.Util
//import io.weline.mediaplayer.player.PlayerDownloadService
//import org.vlc.mediaplayer.R
//import java.io.File
//import java.io.IOException
//import java.util.*
//import java.util.concurrent.CopyOnWriteArraySet
//
///**
// * Tracks media that has been downloaded.
// *
// *
// * Tracked downloads are persisted using an [ActionFile], however in a real application
// * it's expected that state will be stored directly in the application's media database, so that it
// * can be queried efficiently together with other information about the media.
// */
//class DownloadTracker(
//        context: Context,
//        dataSourceFactory: DataSource.Factory,
//        actionFile: File?,
//        vararg deserializers: Deserializer) : DownloadManager.Listener {
//    /**
//     * Listens for changes in the tracked downloads.
//     */
//    interface Listener {
//        /**
//         * Called when the tracked downloads changed.
//         */
//        fun onDownloadsChanged()
//    }
//
//    private val context: Context = context.applicationContext
//    private val dataSourceFactory: DataSource.Factory = dataSourceFactory
//    private val trackNameProvider: TrackNameProvider
//    private val listeners: CopyOnWriteArraySet<Listener>
//    private val trackedDownloadStates: HashMap<Uri, DownloadAction>
//    private val actionFile: ActionFile = ActionFile(actionFile)
//    private val actionFileWriteHandler: Handler
//    fun addListener(listener: Listener) {
//        listeners.add(listener)
//    }
//
//    fun removeListener(listener: Listener?) {
//        listeners.remove(listener)
//    }
//
//    fun isDownloaded(uri: Uri?): Boolean {
//        return trackedDownloadStates.containsKey(uri)
//    }
//
//    fun getOfflineStreamKeys(uri: Uri?): List<StreamKey> {
//        return if (!trackedDownloadStates.containsKey(uri)) {
//            emptyList()
//        } else trackedDownloadStates[uri]!!.keys
//    }
//
//    fun toggleDownload(activity: Activity?, name: String?, uri: Uri?, extension: String) {
//        if (isDownloaded(uri)) {
//            val removeAction = getDownloadHelper(uri, extension).getRemoveAction(Util.getUtf8Bytes(name))
//            startServiceWithAction(removeAction)
//        } else {
//            val helper = StartDownloadDialogHelper(activity, getDownloadHelper(uri, extension), name)
//            helper.prepare()
//        }
//    }
//
//    // DownloadManager.Listener
//    override fun onInitialized(downloadManager: DownloadManager) {
//        // Do nothing.
//    }
//
//    override fun onTaskStateChanged(downloadManager: DownloadManager, taskState: TaskState) {
//        val action = taskState.action
//        val uri = action.uri
//        if (action.isRemoveAction && taskState.state == TaskState.STATE_COMPLETED
//                || !action.isRemoveAction && taskState.state == TaskState.STATE_FAILED) {
//            // A download has been removed, or has failed. Stop tracking it.
//            if (trackedDownloadStates.remove(uri) != null) {
//                handleTrackedDownloadStatesChanged()
//            }
//        }
//    }
//
//    override fun onIdle(downloadManager: DownloadManager) {
//        // Do nothing.
//    }
//
//    // Internal methods
//    private fun loadTrackedActions(deserializers: Array<Deserializer>) {
//        try {
//            val allActions = actionFile.load(*deserializers)
//            for (action in allActions) {
//                trackedDownloadStates[action.uri] = action
//            }
//        } catch (e: IOException) {
//            Log.e(TAG, "Failed to load tracked actions", e)
//        }
//    }
//
//    private fun handleTrackedDownloadStatesChanged() {
//        for (listener in listeners) {
//            listener.onDownloadsChanged()
//        }
//        val actions: Array<DownloadAction> = trackedDownloadStates.values.toTypedArray()
//        actionFileWriteHandler.post {
//            try {
//                actionFile.store(*actions)
//            } catch (e: IOException) {
//                Log.e(TAG, "Failed to store tracked actions", e)
//            }
//        }
//    }
//
//    private fun startDownload(action: DownloadAction) {
//        if (trackedDownloadStates.containsKey(action.uri)) {
//            // This content is already being downloaded. Do nothing.
//            return
//        }
//        trackedDownloadStates[action.uri] = action
//        handleTrackedDownloadStatesChanged()
//        startServiceWithAction(action)
//    }
//
//    private fun startServiceWithAction(action: DownloadAction) {
//        DownloadService.startWithAction(context, PlayerDownloadService::class.java, action, false)
//    }
//
//    private fun getDownloadHelper(uri: Uri?, extension: String): DownloadHelper {
//        val type = Util.inferContentType(uri, extension)
//        return when (type) {
//            C.TYPE_DASH -> DashDownloadHelper(uri, dataSourceFactory)
//            C.TYPE_SS -> SsDownloadHelper(uri, dataSourceFactory)
//            C.TYPE_HLS -> HlsDownloadHelper(uri, dataSourceFactory)
//            C.TYPE_OTHER -> ProgressiveDownloadHelper(uri)
//            else -> throw IllegalStateException("Unsupported type: $type")
//        }
//    }
//
//    private inner class StartDownloadDialogHelper(
//            activity: Activity?, private val downloadHelper: DownloadHelper, private val name: String?) : DownloadHelper.Callback, DialogInterface.OnClickListener {
//        private val builder: AlertDialog.Builder
//        private val dialogView: View
//        private val trackKeys: MutableList<TrackKey>
//        private val trackTitles: ArrayAdapter<String>
//        private val representationList: ListView
//        fun prepare() {
//            downloadHelper.prepare(this)
//        }
//
//        override fun onPrepared(helper: DownloadHelper) {
//            for (i in 0 until downloadHelper.periodCount) {
//                val trackGroups = downloadHelper.getTrackGroups(i)
//                for (j in 0 until trackGroups.length) {
//                    val trackGroup = trackGroups[j]
//                    for (k in 0 until trackGroup.length) {
//                        trackKeys.add(TrackKey(i, j, k))
//                        trackTitles.add(trackNameProvider.getTrackName(trackGroup.getFormat(k)))
//                    }
//                }
//            }
//            if (!trackKeys.isEmpty()) {
//                builder.setView(dialogView)
//            }
//            builder.create().show()
//        }
//
//        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
//            Toast.makeText(
//                    context.applicationContext, R.string.download_start_error, Toast.LENGTH_LONG)
//                    .show()
//            Log.e(TAG, "Failed to start download", e)
//        }
//
//        override fun onClick(dialog: DialogInterface, which: Int) {
//            val selectedTrackKeys = ArrayList<TrackKey>()
//            for (i in 0 until representationList.childCount) {
//                if (representationList.isItemChecked(i)) {
//                    selectedTrackKeys.add(trackKeys[i])
//                }
//            }
//            if (!selectedTrackKeys.isEmpty() || trackKeys.isEmpty()) {
//                // We have selected keys, or we're dealing with single stream content.
//                val downloadAction = downloadHelper.getDownloadAction(Util.getUtf8Bytes(name), selectedTrackKeys)
//                startDownload(downloadAction)
//            }
//        }
//
//        init {
//            builder = AlertDialog.Builder(activity)
//                    .setTitle(R.string.exo_download_description)
//                    .setPositiveButton(android.R.string.ok, this)
//                    .setNegativeButton(android.R.string.cancel, null)
//
//            // Inflate with the builder's context to ensure the correct style is used.
//            val dialogInflater = LayoutInflater.from(builder.context)
//            dialogView = dialogInflater.inflate(R.layout.start_download_dialog, null)
//            trackKeys = ArrayList()
//            trackTitles = ArrayAdapter(
//                    builder.context, android.R.layout.simple_list_item_multiple_choice)
//            representationList = dialogView.findViewById(R.id.representation_list)
//            representationList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
//            representationList.adapter = trackTitles
//        }
//    }
//
//    companion object {
//        private const val TAG = "DownloadTracker"
//    }
//
//    init {
//        trackNameProvider = DefaultTrackNameProvider(context.resources)
//        listeners = CopyOnWriteArraySet()
//        trackedDownloadStates = HashMap()
//        val actionFileWriteThread = HandlerThread("DownloadTracker")
//        actionFileWriteThread.start()
//        actionFileWriteHandler = Handler(actionFileWriteThread.looper)
//        loadTrackedActions(
//                if (deserializers.isNotEmpty()) deserializers.toTypedArray() else DownloadAction.getDefaultDeserializers())
//    }
//}