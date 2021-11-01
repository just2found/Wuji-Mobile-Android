package net.sdvn.nascommon.model.oneos.share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.oneos.api.share.OneOSDownloadShareFileAPI;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferResultListener;
import net.sdvn.nascommon.model.oneos.transfer.ShareElement;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.transfer.TransmissionRunnable;
import net.sdvn.nascommon.model.oneos.transfer.thread.PriorityRunnable;
import net.sdvn.nascommon.model.oneos.transfer.thread.TransferThreadManager;
import net.sdvn.nascommon.model.oneos.transfer.thread.TransferThreadPool;
import net.sdvn.nascommon.utils.log.Logger;

/**
 * The thread for download file from server, base on HTTP.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class DownloadShareFileTask implements TransmissionRunnable {
    private static final String TAG = DownloadShareFileTask.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.DOWNLOAD;
    private final TransferThreadPool executor;

    private ShareElement mElement;
    @Nullable
    private OnTransferResultListener<ShareElement> mListener = null;
    @Nullable
    private OneOSDownloadShareFileAPI downloadShareFileAPI = null;
    private PriorityRunnable priorityRunnable;

    public DownloadShareFileTask(ShareElement element, @Nullable OnTransferResultListener<ShareElement> listener) {
        if (listener == null) {
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "DownloadResultListener is NULL");
            throw new NullPointerException("DownloadResultListener is NULL");
        }
        this.mElement = element;
        this.mListener = listener;
        executor = ShareDownloadManager.getInstance().getExecutor();
    }

    @Override
    public void start() {
        if (mElement.getState() == TransferState.WAIT
                || mElement.getState() == TransferState.PAUSE) {
//            mElement.setDownloadState(ShareElement.WAIT);
            mElement.setState(TransferState.WAIT);
            priorityRunnable = new PriorityRunnable(mElement.getPriority(), this);
            executor.execute(priorityRunnable);
        }
    }


    @Override
    public void run() {
        // httpPostDownload();
        if (mElement.getState() == TransferState.WAIT) {
            downloadShareFileAPI = new OneOSDownloadShareFileAPI(mElement);
            downloadShareFileAPI.setOnDownloadFileListener(new OnTransferFileListener<ShareElement>() {
                @Override
                public void onStart(String url, @NonNull ShareElement element) {
                    Logger.p(Logger.Level.INFO, IS_LOG, TAG, "Start Download file: " + element.getFileName());
                }

                @Override
                public void onTransmission(String url, ShareElement element) {
//                    DLShareElemsKeeper.updateOffset(element.getShareToken(), element.getOffset());
                }

                @Override
                public void onComplete(String url, @NonNull ShareElement element) {
                    Logger.p(Logger.Level.INFO, IS_LOG, TAG, "Download file complete: " + element.getFileName() + ", state: " + element.getState());
                    if (mListener != null) {
                        mListener.onResult(element);
                    }
                }
            });
            downloadShareFileAPI.download();
        }
    }

    /**
     * Stop download file thread
     */
    public void stopDownload() {
        if (null != downloadShareFileAPI) {
            downloadShareFileAPI.stopDownload();
        }
//        mElement.setDownloadState(ShareElement.PAUSE);
        mElement.setState(TransferState.PAUSE);
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Stop download");
    }

    @Override
    public void pause() {
        stopDownload();
    }

    @Override
    public void restart() {

    }

    @Override
    public void cancel() {
        pause();
        executor.remove(priorityRunnable);
    }

}
