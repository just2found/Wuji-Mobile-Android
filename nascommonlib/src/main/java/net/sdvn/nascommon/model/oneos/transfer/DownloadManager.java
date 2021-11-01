package net.sdvn.nascommon.model.oneos.transfer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.TransferHistoryKeeper;
import net.sdvn.nascommon.db.objecbox.TransferHistory;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority;
import net.sdvn.nascommon.utils.IOUtils;
import net.sdvn.nascommon.utils.MediaScanner;
import net.sdvn.nascommon.utils.log.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/31.
 */
public class DownloadManager extends TransmissionManager<DownloadElement> {
    private int mPriority = Priority.DEFAULT;

    private static final String LOG_TAG = DownloadManager.class.getSimpleName();

    @Nullable
    private static DownloadManager sInstance;

    @NonNull
    private OnTransferResultListener<DownloadElement> mDownloadResultListener = new OnTransferResultListener<DownloadElement>() {

        @Override
        public void onResult(final DownloadElement element) {
            executeBackgroundTask(new Runnable() {
                @Override
                public void run() {
                    TransferState state = element.getState();
                    TransferHistory query = TransferHistoryKeeper.query(TransferHistoryKeeper.getTransferType(isDownload),
                            element.getDevId(), element.getSrcPath(), element.getSrcName(), element.getToPath());
                    if (state == TransferState.CANCELED) {
                        Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "TransferState.CANCELED");
                        if (TransferHistoryKeeper.delete(query)) {
                            runOnUIThread(DownloadManager.this::notifyTransferCount);
                        }
                        return;
                    }
                    if (state == TransferState.START) {
                        if (query == null) {
                            TransferHistory history = new TransferHistory(null, null,
                                    TransferHistoryKeeper.getTransferType(isDownload), element.getSrcName(),
                                    element.getSrcPath(), element.getSrcDevId(), element.getToPath(),
                                    element.getSize(), element.getLength(), 0L,
                                    System.currentTimeMillis(), false, element.getTmpName());
                            long insert = TransferHistoryKeeper.insert(history);
                            Logger.p(Logger.Level.DEBUG, IS_LOG, LOG_TAG, "Save history: " + insert);
                        } else {
                            query.setLength(element.getLength());
                            TransferHistoryKeeper.update(query);
                        }
                    } else if (state == TransferState.PAUSE || state == TransferState.FAILED) {
                        if (query != null) {
                            query.setLength(element.getLength());
                            query.setState(state);
                            TransferHistoryKeeper.update(query);
                        }
                    } else if (state == TransferState.COMPLETE) {
                        if (query != null) {
                            query.setTime(System.currentTimeMillis());
                            if (element.getToName() != null) {
                                query.setName(element.getToName());
                            }
                            query.setState(state);
                            query.setIsComplete(true);
                            query.setLength(element.getLength());
                            TransferHistoryKeeper.update(query);
                        }
                        MediaScanner.getInstance().scanningFile(element.getToPath() + File.separator + element.getToName());

                        mTaskHashMap.remove(element);

                        getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                notifyTransferComplete(element);
                                notifyTransferCount();
                            }
                        });


                    } else {
                        Logger.p(Logger.Level.ERROR, IS_LOG, LOG_TAG, "Download Exception: " + state);
                    }
                }
            });

        }


    };


    private DownloadManager() {
        super(true);
        threadPool.setCorePoolSize(3, 3);
        executeBackgroundTask(new Runnable() {
            @Override
            public void run() {
                List<TransferHistory> histories = TransferHistoryKeeper.all(isDownload, false);
                for (TransferHistory history : histories) {
                    OneOSFile file = new OneOSFile();
                    file.setPath(history.getSrcPath());
                    file.setName(history.getName());
                    file.setSize(history.getSize());
                    DownloadElement element = new DownloadElement(file, history.getToPath(), history.getLength(), history.getTmpName());
                    element.setSrcDevId(history.getSrcDevId());
                    TransferState state = history.getState();
                    if (state.ordinal() < TransferState.PAUSE.ordinal()) {
                        element.setState(TransferState.PAUSE);
                    } else {
                        element.setState(state);
                    }
                    element.setId(history.getId());
                    element.setPriority((int) (Priority.DEFAULT - element.getId()));
//                    transferList.add(element);
                    mTaskHashMap.put(element, genTransmissionRunnable(element));
                }
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        notifyTransferCount();
                    }
                });
                {
                    Logger.p(Logger.Level.INFO, Logger.Logd.DOWNLOAD, LOG_TAG,
                            "histories size : " + histories.size() + " " + mTaskHashMap.size());
                }
            }
        });


    }

    @NonNull
    @Override
    protected TransmissionRunnable genTransmissionRunnable(DownloadElement element) {
        return new DownloadFileTask(element, new OnTransferFileListener<DownloadElement>() {
            @Override
            public void onStart(String url, DownloadElement element) {
                if (mOnTransferFileListener != null) {
                    mOnTransferFileListener.onStart(url, element);
                }
            }

            @Override
            public void onTransmission(String url, DownloadElement element) {
                if (mOnTransferFileListener != null) {
                    mOnTransferFileListener.onTransmission(url, element);
                }
                if (mDownloadResultListener != null && mLimiter.shouldFetch(element.id)) {
                    mDownloadResultListener.onResult(element);
                }
            }

            @Override
            public void onComplete(String url, DownloadElement element) {
                if (mOnTransferFileListener != null) {
                    mOnTransferFileListener.onComplete(url, element);
                }
                if (mDownloadResultListener != null) {
                    mDownloadResultListener.onResult(element);
                }
            }
        }, getExecutor());
    }

    /**
     * Singleton instance method
     *
     * @return {@link DownloadManager}
     */
    @NonNull
    public static DownloadManager getInstance() {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager();
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
    public int enqueue(@Nullable DownloadElement element) {
        if (element == null) {
            Logger.p(Logger.Level.ERROR, IS_LOG, LOG_TAG, "Download element is null");
            return -1;
        }

        if (findElement(element.getTag()) != null) {
            return 0;
        }

        TransferHistory query = TransferHistoryKeeper.query(TransferHistoryKeeper
                        .getTransferType(isDownload), element.getDevId(), element.getSrcPath()
                , element.getSrcName(), element.getToPath());
        if (query != null) {
            if (query.getSize() == element.getSize()) {
                if (!query.getIsComplete()) {
                    File tmpFile = new File(query.getToPath(), query.getTmpName());
                    if (tmpFile.exists()) {
                        element.setOffset(tmpFile.length());
                        element.setLength(tmpFile.length());
                        element.setTmpName(query.getTmpName());
                    }
                } else {
                    File file = new File(query.getToPath(), query.getName());
                    if (file.exists()) {
                        return 1;
                    } else {
                        query.setIsComplete(false);
                        IOUtils.delFileOrFolder(new File(query.getToPath(), query.getTmpName()));
                        query.setLength(0L);
                    }
                }
            } else {
                TransferHistoryKeeper.delete(query);
            }
        }
        if (query == null) {
            query = getTransferHistory(element);
            element.id = TransferHistoryKeeper.insert(query);
        } else {
            query.setIsComplete(false);
            query.setLength(element.getLength());
            query.setSize(element.getSize());
            element.id = query.getId();
            TransferHistoryKeeper.update(query);
        }
        query.setState(element.getState());
        DownloadFileTask downloadFileTask = (DownloadFileTask) mTaskHashMap.get(element);
        if (downloadFileTask == null) {
            downloadFileTask = (DownloadFileTask) genTransmissionRunnable(element);
            mTaskHashMap.put(element, downloadFileTask);
        }
        if (element.getPriority() == Priority.DEFAULT)
            element.setPriority((int) (Priority.DEFAULT - element.getId()));
        downloadFileTask.start();
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                notifyTransferCount();
            }
        });
        return element.hashCode();
    }

    @Override
    protected @NotNull TransferHistory getTransferHistory(DownloadElement element) {
        return new TransferHistory(null, null, TransferHistoryKeeper.getTransferType(isDownload), element.getSrcName(),
                element.getSrcPath(), element.getSrcDevId(), element.getToPath(), element.getSize(), element.getLength(), 0L,
                System.currentTimeMillis(), false, element.getTmpName());
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
