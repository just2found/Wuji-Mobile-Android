package io.weline.mediaplayer.internal.exo;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.offline.ActionFileUpgradeUtil;
import com.google.android.exoplayer2.offline.DefaultDownloadIndex;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.Executors;

import io.weline.mediaplayer.BuildConfig;

//import com.google.android.exoplayer2.ext.cronet.CronetDataSource;
//import com.google.android.exoplayer2.ext.cronet.CronetDataSourceFactory;
//import com.google.android.exoplayer2.ext.cronet.CronetEngineWrapper;

public final class ExoUtil {

    public static final String DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel";

    /**
     * Whether the demo application uses Cronet for networking. Note that Cronet does not provide
     * automatic support for cookies (https://github.com/google/ExoPlayer/issues/5975).
     *
     * <p>If set to false, the platform's default network stack is used with a {@link CookieManager}
     * configured in {@link #getHttpDataSourceFactory}.
     */
    private static final boolean USE_CRONET_FOR_NETWORKING = true;

    private static final String USER_AGENT =
            "ExoPlayer/"
                    + ExoPlayerLibraryInfo.VERSION
                    + " (Linux; Android "
                    + Build.VERSION.RELEASE
                    + ") "
                    + ExoPlayerLibraryInfo.VERSION_SLASHY;
    private static final String TAG = "ExoUtil";
    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";

    private static DataSource.Factory dataSourceFactory;
    private static HttpDataSource.Factory httpDataSourceFactory;
    private static DatabaseProvider databaseProvider;
    private static File downloadDirectory;
    private static Cache downloadCache;
    private static DownloadManager downloadManager;
    private static DownloadTracker downloadTracker;
    private static DownloadNotificationHelper downloadNotificationHelper;

    /**
     * Returns whether extension renderers should be used.
     */
    public static boolean useExtensionRenderers() {
        return BuildConfig.USE_DECODER_EXTENSIONS;
    }

    @NonNull
    public static RenderersFactory buildRenderersFactory(
            @NonNull Context context, boolean preferExtensionRenderer) {
        @DefaultRenderersFactory.ExtensionRendererMode
        int extensionRendererMode =
                useExtensionRenderers()
                        ? (preferExtensionRenderer
                        ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        return new DefaultRenderersFactory(context.getApplicationContext())
                .setExtensionRendererMode(extensionRendererMode);
    }

    @NonNull
    public static synchronized HttpDataSource.Factory getHttpDataSourceFactory(@NonNull Context context) {
        if (httpDataSourceFactory == null) {
//            if (USE_CRONET_FOR_NETWORKING) {
//                context = context.getApplicationContext();
//                CronetEngineWrapper cronetEngineWrapper =
//                        new CronetEngineWrapper(context,null, /* preferGMSCoreCronet= */ false);
//                httpDataSourceFactory =
//                        new CronetDataSourceFactory(cronetEngineWrapper, Executors.newSingleThreadExecutor());
//            } else {
                CookieManager cookieManager = new CookieManager();
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
                CookieHandler.setDefault(cookieManager);
                httpDataSourceFactory = new DefaultHttpDataSourceFactory(USER_AGENT);
//            }
        }
        return httpDataSourceFactory;
    }

    /**
     * Returns a {@link DataSource.Factory}.
     */
    @NonNull
    public static synchronized DataSource.Factory getDataSourceFactory(@NonNull Context context) {
        if (dataSourceFactory == null) {
            context = context.getApplicationContext();
            DefaultDataSourceFactory upstreamFactory =
                    new DefaultDataSourceFactory(context, getHttpDataSourceFactory(context));
            dataSourceFactory = buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache(context));
        }
        return dataSourceFactory;
    }

    @NonNull
    public static synchronized DownloadNotificationHelper getDownloadNotificationHelper(
            @NonNull Context context) {
        if (downloadNotificationHelper == null) {
            downloadNotificationHelper =
                    new DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID);
        }
        return downloadNotificationHelper;
    }

    @NonNull
    public static synchronized DownloadManager getDownloadManager(@NonNull Context context) {
        ensureDownloadManagerInitialized(context);
        return downloadManager;
    }

    @NonNull
    public static synchronized DownloadTracker getDownloadTracker(@NonNull Context context) {
        ensureDownloadManagerInitialized(context);
        return downloadTracker;
    }

    private static synchronized Cache getDownloadCache(Context context) {
        if (downloadCache == null) {
            File downloadContentDirectory =
                    new File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache =
                    new SimpleCache(
                            downloadContentDirectory, new NoOpCacheEvictor(), getDatabaseProvider(context));
        }
        return downloadCache;
    }

    private static synchronized void ensureDownloadManagerInitialized(Context context) {
        if (downloadManager == null) {
            DefaultDownloadIndex downloadIndex = new DefaultDownloadIndex(getDatabaseProvider(context));
            upgradeActionFile(
                    context, DOWNLOAD_ACTION_FILE, downloadIndex, /* addNewDownloadsAsCompleted= */ false);
            upgradeActionFile(
                    context,
                    DOWNLOAD_TRACKER_ACTION_FILE,
                    downloadIndex,
                    /* addNewDownloadsAsCompleted= */ true);
            downloadManager =
                    new DownloadManager(
                            context,
                            getDatabaseProvider(context),
                            getDownloadCache(context),
                            getHttpDataSourceFactory(context),
                            Executors.newFixedThreadPool(/* nThreads= */ 6));
            downloadTracker =
                    new DownloadTracker(context, getHttpDataSourceFactory(context), downloadManager);
        }
    }

    private static synchronized void upgradeActionFile(
            Context context,
            String fileName,
            DefaultDownloadIndex downloadIndex,
            boolean addNewDownloadsAsCompleted) {
        try {
            ActionFileUpgradeUtil.upgradeAndDelete(
                    new File(getDownloadDirectory(context), fileName),
                    /* downloadIdProvider= */ null,
                    downloadIndex,
                    /* deleteOnFailure= */ true,
                    addNewDownloadsAsCompleted);
        } catch (IOException e) {
            Log.e(TAG, "Failed to upgrade action file: " + fileName, e);
        }
    }

    private static synchronized DatabaseProvider getDatabaseProvider(Context context) {
        if (databaseProvider == null) {
            databaseProvider = new ExoDatabaseProvider(context);
        }
        return databaseProvider;
    }

    private static synchronized File getDownloadDirectory(Context context) {
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(/* type= */ null);
            if (downloadDirectory == null) {
                downloadDirectory = context.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSource.Factory buildReadOnlyCacheDataSource(
            DataSource.Factory upstreamFactory, Cache cache) {
        return new CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setCacheWriteDataSinkFactory(null)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }

    private ExoUtil() {
    }
}
