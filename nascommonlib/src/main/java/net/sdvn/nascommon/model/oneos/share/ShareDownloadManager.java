package net.sdvn.nascommon.model.oneos.share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.listener.CommonResultListener;
import net.sdvn.common.internet.loader.RemoveDownloadTokenHttpLoader;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.nascommon.db.DBHelper;
import net.sdvn.nascommon.db.objecbox.TransferHistory;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferResultListener;
import net.sdvn.nascommon.model.oneos.transfer.ShareElement;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.transfer.TransmissionManager;
import net.sdvn.nascommon.model.oneos.transfer.TransmissionRunnable;
import net.sdvn.nascommon.utils.MediaScanner;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.BoxStore;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/31.
 */
public class ShareDownloadManager extends TransmissionManager<ShareElement> {
    private static final String LOG_TAG = ShareDownloadManager.class.getSimpleName();

    @Nullable
    private static ShareDownloadManager sInstance;
    @NonNull
    private OnTransferResultListener<ShareElement> mDownloadResultListener = new OnTransferResultListener<ShareElement>() {

        @Override
        public void onResult(ShareElement mElement) {
            Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Download Result: " + mElement.getShareState());

            TransferState state = mElement.getState();
            if (state == TransferState.COMPLETE) {
//                DLShareElemsKeeper.update(mElement);
                removeDownloadToken(mElement);
                mTaskHashMap.remove(mElement);
                MediaScanner.getInstance().scanningFile(mElement.getToPath() + File.separator + mElement.getToName());
                notifyTransferComplete(mElement);
            } else {
                mTaskHashMap.remove(mElement);
//                DLShareElemsKeeper.update(mElement);
                Logger.p(Logger.Level.ERROR, IS_LOG, LOG_TAG, "Download Exception: " + state);
            }
            final Box<ShareElement> store = getStore();
            if (store != null) {
                store.put(mElement);
            }
        }
    };

    @Nullable
    private Box<ShareElement> getStore() {
        BoxStore boxStore = DBHelper.getBoxStore();
        return boxStore != null ? boxStore.boxFor(ShareElement.class) : null;
    }

    private void removeDownloadToken(ShareElement mElement) {
        RemoveDownloadTokenHttpLoader loader = new RemoveDownloadTokenHttpLoader(GsonBaseProtocol.class);
        loader.setParams(LoginTokenUtil.getToken(), mElement.getDownloadToken());
        loader.executor(new CommonResultListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol mBaseProtocol) {

            }

            @Override
            public void error(Object tag, GsonBaseProtocol mErrorProtocol) {

            }
        });
    }


    private ShareDownloadManager() {
        super(true);
        threadPool.setCorePoolSize(0, 1);
    }

    /**
     * Singleton instance method
     *
     * @return {@link ShareDownloadManager}
     */
    @NonNull
    public static ShareDownloadManager getInstance() {
        if (sInstance == null) {
            synchronized (ShareDownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new ShareDownloadManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Enqueue a new download or upload. It will start automatically once the manager is
     * ready to execute it and connectivity is available.
     *
     * @param element the parameters specifying this task
     * @return an ID for the task, unique across the system. This ID is used to make future
     * calls related to this task. If enqueue failed, return -1.
     */
    @Override
    public int enqueue(@Nullable ShareElement element) {
        if (element == null) {
            Logger.p(Logger.Level.ERROR, IS_LOG, LOG_TAG, "Download element is null");
            return -1;
        }

        if (findElement(element.getShareToken()) != null) {
            return -1;
        }

        element.setState(TransferState.WAIT);
        DownloadShareFileTask downloadShareFileTask = (DownloadShareFileTask) mTaskHashMap.get(element);
        if (downloadShareFileTask == null) {
            downloadShareFileTask = new DownloadShareFileTask(element, mDownloadResultListener);
            mTaskHashMap.put(element, downloadShareFileTask);
        }
        downloadShareFileTask.start();
        notifyTransferCount();

        return element.hashCode();
    }


    @Override
    protected TransferHistory getTransferHistory(ShareElement element) {
        return new TransferHistory();
    }

    /**
     * Cancel task and remove them from the manager. The task will be stopped if
     * it was running, and it will no longer be accessible through the manager. If there is
     * a temporary file, partial or complete, it is deleted.
     *
     * @return the id of task actually removed, if remove failed, return -1.
     */
    @Override
    public int cancel(String shareToken) {
        ShareElement element = findElement(shareToken);
        if (element != null) {
            DownloadShareFileTask downloadShareFileTask = (DownloadShareFileTask) mTaskHashMap.get(element);
            if (downloadShareFileTask != null) {
                downloadShareFileTask.cancel();
            }

            element.setShareState(ShareElement.STATE_SHARE_RECEIVE);
//            DLShareElemsKeeper.delete(shareToken);
            final Box<ShareElement> store = getStore();
            if (store != null) {
                store.remove(element);
            }
            mTaskHashMap.remove(element);
            notifyTransferCount();

            return element.hashCode();
        }
        return -1;
    }

    /**
     * Cancel all task and remove them from the manager.
     *
     * @return true if succeed, false otherwise.
     * @see #cancel(String)
     */
    @Override
    public boolean cancel() {
        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Remove all download tasks");
        final HashMap<ShareElement, TransmissionRunnable> map
                = new HashMap<>(mTaskHashMap);
        for (Map.Entry<ShareElement, TransmissionRunnable> shareElementDownloadShareFileTaskEntry
                : map.entrySet()) {
            TransmissionRunnable value = shareElementDownloadShareFileTaskEntry.getValue();
            if (value == null) {
                continue;
            }
            value.cancel();
        }
        executeBackgroundTask(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<ShareElement, TransmissionRunnable> entry : map.entrySet()) {
                    ShareElement element = entry.getKey();
                    if (element != null) {
                        mTaskHashMap.remove(element);
//                        DLShareElemsKeeper.delete(element.getShareToken());

                        final Box<ShareElement> store = getStore();
                        if (store != null) {
                            store.remove(element);
                        }
                    }
                }
            }
        });
        notifyTransferCount();

        return true;
    }

    @NonNull
    @Override
    protected TransmissionRunnable genTransmissionRunnable(ShareElement element) {
        return new DownloadShareFileTask(element, mDownloadResultListener);
    }

    /**
     * Destroy transfer manager
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }
}
