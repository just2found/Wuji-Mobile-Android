package net.sdvn.nascommon.model.oneos.transfer;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.TransferHistoryKeeper;
import net.sdvn.nascommon.db.objecbox.TransferHistory;
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.log.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/31.
 */
public class UploadManager extends TransmissionManager<UploadElement> {

    private static final String TAG = UploadManager.class.getSimpleName();
    @Nullable
    private static UploadManager sInstance;
    @NonNull
    private OnTransferResultListener<UploadElement> uploadResultListener =
            new OnTransferResultListener<UploadElement>() {

                @Override
                public void onResult(final UploadElement element) {
                    executeBackgroundTask(new Runnable() {
                        @Override
                        public void run() {
                            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Upload Result: " + element.getState());
                            TransferState state = element.getState();
                            TransferHistory query = TransferHistoryKeeper.query(
                                    TransferHistoryKeeper.getTransferType(isDownload)
                                    , element.getDevId(), element.getSrcPath(),
                                    element.getSrcName(), element.getToPath());
                            if (state == TransferState.CANCELED) {
                                Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "TransferState.CANCELED");
                                runOnUIThread(() -> notifyTransferCount());
                                TransferHistoryKeeper.delete(query);
                                return;
                            }
                            if (state == TransferState.PAUSE
                                    || state == TransferState.FAILED) {
                                if (query != null) {
                                    query.setLength(element.getLength());
                                    query.setState(state);
                                    TransferHistoryKeeper.update(query);
                                }
                                Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "Upload pause or failure");
                            } else if (state == TransferState.START) {
                                if (query != null) {
                                    query.setLength(element.getLength());
                                    TransferHistoryKeeper.update(query);
                                }
                                Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "Upload block save");
                            } else if (state == TransferState.COMPLETE) {
                                if (query != null) {
                                    query.setTime(System.currentTimeMillis());
                                    query.setIsComplete(true);
                                    query.setLength(element.getLength());
                                    query.setState(state);
                                    TransferHistoryKeeper.update(query);
                                }
                                mTaskHashMap.remove(element);
                                getMainHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyTransferComplete(element);
                                        notifyTransferCount();
                                    }
                                });
                            }
                        }
                    });
                }

            };


    private UploadManager() {
        super(false);
        threadPool.setCorePoolSize(3, 3);
        executeBackgroundTask(new Runnable() {
            @Override
            public void run() {
                List<TransferHistory> histories = TransferHistoryKeeper.all(isDownload, false);
                for (TransferHistory history : histories) {
                    if ((long) history.getLength() == history.getSize()) {
                        history.setIsComplete(true);
                        TransferHistoryKeeper.update(history);
                        continue;
                    }
                    File file = new File(history.getSrcPath());
                    UploadElement element = new UploadElement(file, history.getToPath());
                    if (FileUtils.isPictureFile(file.getName())
                            || FileUtils.isVideoFile(file.getName())
                            || FileUtils.isGifFile(file.getName()))
                        element.setThumbUri(Uri.fromFile(file));
                    element.setLength(history.getLength());
                    element.setToDevId(history.getSrcDevId());
                    element.setId(history.getId());
                    element.setPriority(mPriority--);
                    TransferState state = history.getState();
                    if (file.exists()) {
                        if (state.ordinal() < TransferState.PAUSE.ordinal()) {
                            element.setState(TransferState.PAUSE);
                        } else {
                            element.setState(state);
                        }
                    } else {
                        element.setState(TransferState.FAILED);
                        element.setException(TransferException.FILE_NOT_FOUND);
                    }
                    mTaskHashMap.put(element, genTransmissionRunnable(element));

                }
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        notifyTransferCount();
                    }
                });
                {
                    Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "histories size : "
                            + histories.size() + " " + mTaskHashMap.size());
                }
            }
        });

    }

    /**
     * Singleton instance method
     *
     * @return {@code UploadManager}
     */
    @NonNull
    public static UploadManager getInstance() {
        if (sInstance == null) {
            synchronized (UploadManager.class) {
                if (sInstance == null) {
                    sInstance = new UploadManager();
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
    public int enqueue(@Nullable UploadElement element) {
        if (element == null) {
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "upload element is null");
            return -1;
        }
        if (findElement(element.getTag()) != null) {
            return -1;
        }
        TransferHistory query = TransferHistoryKeeper.query(TransferHistoryKeeper.getTransferType(isDownload)
                , element.getDevId(), element.getSrcPath(), element.getSrcName(), element.getToPath());
        if (query != null) {
            if (!query.getIsComplete()) {
                element.setLength(query.getLength());
            } else {
                element.setCheck(true);
            }
        }
        if (query == null) {
            query = getTransferHistory(element);
            element.id = TransferHistoryKeeper.insert(query);
        } else {
            query.setIsComplete(false);
            element.id = query.getId();
        }
        TransferHistoryKeeper.update(query);
        UploadFileTask uploadFileTask = (UploadFileTask) mTaskHashMap.get(element);
        if (uploadFileTask == null) {
            uploadFileTask = (UploadFileTask) genTransmissionRunnable(element);
            mTaskHashMap.put(element, uploadFileTask);
        }
        if (element.getPriority() == Priority.DEFAULT)
            element.setPriority((int) (mPriority - element.getId()));
        uploadFileTask.start();
        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                notifyTransferCount();
            }
        });
        return element.hashCode();

    }

    @Override
    protected @NotNull TransferHistory getTransferHistory(UploadElement element) {
        return new TransferHistory(null, null, TransferHistoryKeeper.getTransferType(isDownload),
                element.getSrcName(), element.getSrcPath(),
                element.getDevId(), element.getToPath(), element.getSize(),
                element.getLength(), 0L, element.getTime(), false, null);
    }


    @NonNull
    @Override
    protected TransmissionRunnable genTransmissionRunnable(UploadElement element) {
        return new UploadFileTask(element, new OnTransferFileListener<UploadElement>() {
            @Override
            public void onStart(String url, UploadElement element) {
                if (mOnTransferFileListener != null) {
                    mOnTransferFileListener.onStart(url, element);
                }
            }

            @Override
            public void onTransmission(String url, UploadElement element) {
                if (mOnTransferFileListener != null) {
                    mOnTransferFileListener.onTransmission(url, element);
                }
                if (uploadResultListener != null && mLimiter.shouldFetch(element.id)) {
                    uploadResultListener.onResult(element);
                }
            }

            @Override
            public void onComplete(String url, UploadElement element) {
                if (mOnTransferFileListener != null) {
                    mOnTransferFileListener.onComplete(url, element);
                }
                uploadResultListener.onResult(element);
            }
        }, getExecutor());
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
