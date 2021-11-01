//package io.weline.mediaplayer.player
//
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Build
//import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
//import com.google.android.exoplayer2.offline.DownloadManager
//import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
//import com.google.android.exoplayer2.upstream.DataSource
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
//import com.google.android.exoplayer2.upstream.FileDataSourceFactory
//import com.google.android.exoplayer2.upstream.HttpDataSource
//import com.google.android.exoplayer2.upstream.cache.*
//import okhttp3.ConnectionPool
//import okhttp3.OkHttpClient
//import java.io.File
//import java.lang.ref.WeakReference
//import java.util.concurrent.TimeUnit
//
//class PlayerManager private constructor() {
//    private object Holder {
//        val instance = PlayerManager()
//    }
//
//    /**
//     * Returns a [DataSource.Factory].
//     */
//    fun buildDataSourceFactory(): DataSource.Factory {
//        val upstreamFactory = DefaultDataSourceFactory(sWRefContext!!.get(), buildHttpDataSourceFactory())
//        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache())
//    }
//
//    private var downloadDirectory: File? = null
//        private get() {
//            if (field == null) {
//                field = sWRefContext!!.get()!!.getExternalFilesDir(null)
//                if (field == null) {
//                    field = sWRefContext!!.get()!!.filesDir
//                }
//            }
//            return field
//        }
//    private var downloadCache: Cache? = null
//    private var downloadManager: DownloadManager? = null
//    private var downloadTracker: DownloadTracker? = null
//
//    @Synchronized
//    private fun getDownloadCache(): Cache {
//        if (downloadCache == null) {
//            val downloadContentDirectory = File(downloadDirectory, DOWNLOAD_CONTENT_DIRECTORY)
//            downloadCache = SimpleCache(downloadContentDirectory,
//                    LeastRecentlyUsedCacheEvictor(DEFAULT_CACHE_SIZE))
//        }
//        return downloadCache!!
//    }
//
//    /**
//     * Returns a [HttpDataSource.Factory].
//     */
//    fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
//        val builder = OkHttpClient.Builder()
//                .connectTimeout(10, TimeUnit.SECONDS)
//                .writeTimeout(50, TimeUnit.SECONDS)
//                .readTimeout(50, TimeUnit.SECONDS)
//                .retryOnConnectionFailure(true)
//                .connectionPool(ConnectionPool(Math.max(CORE_POOL_SIZE, 3),
//                        2, TimeUnit.MINUTES))
//        return OkHttpDataSourceFactory(builder.build()
//                , getUserAgent(sWRefContext!!.get()))
//    }
//
//    fun getDownloadManager(): DownloadManager? {
//        initDownloadManager()
//        return downloadManager
//    }
//
//    fun getDownloadTracker(): DownloadTracker? {
//        initDownloadManager()
//        return downloadTracker
//    }
//
//    @Synchronized
//    private fun initDownloadManager() {
//        if (downloadManager == null) {
//            val downloaderConstructorHelper = DownloaderConstructorHelper(getDownloadCache(), buildHttpDataSourceFactory())
//            downloadManager = DownloadManager(
//                    downloaderConstructorHelper,
//                    MAX_SIMULTANEOUS_DOWNLOADS,
//                    DownloadManager.DEFAULT_MIN_RETRY_COUNT,
//                    File(downloadDirectory, DOWNLOAD_ACTION_FILE))
//            downloadTracker = DownloadTracker( /* context= */
//                    sWRefContext!!.get()!!,
//                    buildDataSourceFactory(),
//                    File(downloadDirectory, DOWNLOAD_TRACKER_ACTION_FILE))
//            downloadManager!!.addListener(downloadTracker)
//        }
//    }
//
//    companion object {
//        private var sWRefContext: WeakReference<Context>? = null
//        fun init(context: Context) {
//            sWRefContext = WeakReference(context)
//        }
//
//        fun release() {
//            if (instance.downloadCache != null) {
//                instance.downloadCache!!.release()
//            }
//            sWRefContext = null
//        }
//
//        @JvmStatic
//        val instance: PlayerManager
//            get() {
//                if (sWRefContext == null) {
//                    throw NullPointerException("pls init with context")
//                }
//                return Holder.instance
//            }
//
//        private const val DOWNLOAD_ACTION_FILE = "actions"
//        private const val DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions"
//        private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
//        private const val MAX_SIMULTANEOUS_DOWNLOADS = 2
//        private const val DEFAULT_CACHE_SIZE = 100 * 1024 * 1024 //100M
//                .toLong()
//        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
//        private val MAX_POOL_SIZE = CPU_COUNT * 2 + 1 //最大线程池的数量
//        private val CORE_POOL_SIZE = if (CPU_COUNT + 1 >= 3) (CPU_COUNT + 1) / 3 else 1
//
//        /**
//         * Returns a user agent string based on the given application name and the library version.
//         *
//         * @param context A valid context of the calling application.
//         * @return A user agent string generated using the applicationName and the library version.
//         */
//        fun getUserAgent(context: Context?): String {
//            var versionName: String
//            var applicationName: String
//            try {
//                val packageName = context!!.packageName
//                applicationName = packageName.substring(packageName.lastIndexOf(".") + 1)
//                val info = context.packageManager.getPackageInfo(packageName, 0)
//                versionName = info.versionName
//            } catch (e: PackageManager.NameNotFoundException) {
//                versionName = "?"
//                applicationName = "unknown"
//            }
//            return (applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
//                    + ") ")
//        }
//
//        private fun buildReadOnlyCacheDataSource(
//                upstreamFactory: DefaultDataSourceFactory, cache: Cache): CacheDataSourceFactory {
//            return CacheDataSourceFactory(
//                    cache,
//                    upstreamFactory,
//                    FileDataSourceFactory(),  /* cacheWriteDataSinkFactory= */
//                    null,
//                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,  /* eventListener= */
//                    null)
//        }
//    }
//}