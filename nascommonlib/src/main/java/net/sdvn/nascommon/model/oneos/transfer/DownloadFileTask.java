package net.sdvn.nascommon.model.oneos.transfer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.oneos.api.file.OneOSDownloadFileAPI;
import net.sdvn.nascommon.model.oneos.transfer.thread.PriorityRunnable;
import net.sdvn.nascommon.model.oneos.transfer.thread.WorkQueueExecutor;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.IOUtils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import libs.source.common.AppExecutors;

/**
 * The thread for download file from server, base on HTTP.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class DownloadFileTask implements TransmissionRunnable {
    private static final String TAG = DownloadFileTask.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.DOWNLOAD;

    private DownloadElement mElement;
    @Nullable
    private OnTransferResultListener<DownloadElement> mListener = null;
    @Nullable
    private OnTransferFileListener<DownloadElement> mDownloadListener = null;
    @Nullable
    private OneOSDownloadFileAPI downloadFileAPI = null;
    private PriorityRunnable priorityRunnable;
    @Nullable
    private WorkQueueExecutor executor;
    private LoginSession loginSession;

    public DownloadFileTask(DownloadElement element,
                            @Nullable OnTransferResultListener<DownloadElement> listener,
                            WorkQueueExecutor executor) {
        if (listener == null) {
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "DownloadResultListener is NULL");
            throw new NullPointerException("DownloadResultListener is NULL");
        }
        this.mElement = element;
        this.mListener = listener;
        this.executor = executor;
    }

    public DownloadFileTask(DownloadElement element,
                            @Nullable OnTransferFileListener<DownloadElement> downloadListener,
                            WorkQueueExecutor executor) {
        if (downloadListener == null) {
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "DownloadFileListener is NULL");
            throw new NullPointerException("DownloadFileListener is NULL");
        }
        this.mElement = element;
        this.mDownloadListener = downloadListener;
        this.executor = executor;
    }

    @Override
    public void start() {
        if (mElement.getState() != TransferState.WAIT
                && mElement.getState() != TransferState.START) {
            mElement.setState(TransferState.WAIT);
            priorityRunnable = new PriorityRunnable(mElement.getPriority(), this);
            if (executor != null) {
                executor.execute(priorityRunnable);
            } else {
                run();
            }
        } else if (mElement.getState() == TransferState.COMPLETE) {
            if (mElement.getToName() == null || mElement.getToPath() == null) {
//                postOnError(progress, new StorageException("the file of the task with tag:" + progress.tag + " may be invalid or damaged, please call the method restart() to download again！"));
                mElement.setState(TransferState.FAILED);
                mElement.setException(TransferException.DES_NOT_FOUND);
            } else {
                File file = new File(mElement.getToPath(), mElement.getToName());
                if (file.exists() && file.length() == mElement.getSize()) {
                    if (executor != null) {
                        executor.remove(priorityRunnable);
                    }
                } else {
                    mElement.setState(TransferState.FAILED);
                    mElement.setException(TransferException.DES_NOT_FOUND);
                }
                if (mDownloadListener != null) {
                    mDownloadListener.onComplete(null, mElement);
                } else if (mListener != null) {
                    mListener.onResult(mElement);
                }

            }
        }
    }

    @Override
    public void run() {
        // httpPostDownload();
        if (mElement.getState() == TransferState.WAIT) {
            loginSession = SessionManager.getInstance().getLoginSession(mElement.getDevId());
            if (loginSession != null && loginSession.isLogin()) {
                doDownload(loginSession);
            } else {
                final CountDownLatch latch = new CountDownLatch(1);
                AppExecutors.Companion.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        SessionManager.getInstance().getLoginSession(mElement.getSrcDevId(),
                                new GetSessionListener(false) {
                                    @Override
                                    public void onSuccess(String url, final LoginSession data) {
                                        loginSession = data;
                                        latch.countDown();
                                    }


                                    @Override
                                    public void onFailure(String url, int errorNo, String errorMsg) {
                                        doDownloadException(url);
                                        latch.countDown();
                                    }
                                });
                    }
                });
                try {
                    latch.await();
                    if (loginSession != null) {
                        doDownload(loginSession);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    doDownloadException("");
                }
            }
        }
    }

    private void doDownloadException(String url) {
        mElement.setState(TransferState.FAILED);
        mElement.setException(TransferException.FAILED_REQUEST_SERVER);
        if (mDownloadListener != null) {
            mDownloadListener.onComplete(url, mElement);
        } else {
            if (mListener != null) {
                mListener.onResult(mElement);
            }
        }
    }

    private void doDownload(@NonNull LoginSession data) {
        downloadFileAPI = new OneOSDownloadFileAPI(data, mElement);
        downloadFileAPI.setOnDownloadFileListener(new OnTransferFileListener<DownloadElement>() {
            @Override
            public void onStart(String url, @NonNull DownloadElement element) {
                Logger.p(Logger.Level.INFO, IS_LOG, TAG, "Start Download file: " + element.getSrcPath());
                if (mDownloadListener != null) {
                    mDownloadListener.onStart(url, element);
                }
            }

            @Override
            public void onTransmission(String url, final DownloadElement element) {
                if (mDownloadListener != null) {
                    mDownloadListener.onTransmission(url, element);
                }

            }

            @Override
            public void onComplete(String url, @NonNull final DownloadElement element) {
                Logger.p(Logger.Level.INFO, IS_LOG, TAG, "Download file complete: " + element.getSrcPath() + ", state: " + element.getState());
                if (mDownloadListener != null) {
                    mDownloadListener.onComplete(url, element);
                } else if (mListener != null) {
                    mListener.onResult(element);
                }
            }
        });

        downloadFileAPI.download();
    }

    /**
     * Stop download file thread
     */
    public void stopDownload() {
        if (null != downloadFileAPI) {
            downloadFileAPI.stopDownload();
        } else if (executor != null && mElement.getState() == TransferState.WAIT) {
            executor.remove(priorityRunnable);
        }
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Stop download");
    }

    @Override
    public void pause() {
        stopDownload();
        mElement.setState(TransferState.PAUSE);
    }

    @Override
    public void restart() {
        mElement.setState(TransferState.NONE);
        if (mElement.getTmpName() != null) {
            IOUtils.delFileOrFolder(new File(mElement.getTmpName()));
        }
        mElement.setOffset(0);
        mElement.setLength(0);
        mElement.setSpeed(0);
        start();
    }

    @Override
    public void cancel() {
        stopDownload();
        if (executor != null) {
            executor.remove(priorityRunnable);
        }
        mElement.setState(TransferState.CANCELED);
    }
}
