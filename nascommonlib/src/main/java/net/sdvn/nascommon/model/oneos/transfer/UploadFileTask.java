package net.sdvn.nascommon.model.oneos.transfer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.FileManageAction;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.api.file.OneOSFileManageAPI;
import net.sdvn.nascommon.model.oneos.api.file.OneOSUploadFileAPI;
import net.sdvn.nascommon.model.oneos.transfer.thread.PriorityRunnable;
import net.sdvn.nascommon.model.oneos.transfer.thread.WorkQueueExecutor;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.weline.repo.SessionCache;
import libs.source.common.AppExecutors;

/**
 * The thread for upload file to server, based on HTTP or Socket.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class UploadFileTask implements TransmissionRunnable {
    private final String TAG = UploadFileTask.class.getSimpleName();
    @Nullable
    private WorkQueueExecutor executor;
    private LoginSession loginSession;

    private UploadElement mElement;
    @Nullable
    private OnTransferResultListener<UploadElement> mResultListener = null;
    @Nullable
    private OnTransferFileListener<UploadElement> mUploadListener = null;
    private OneOSUploadFileAPI uploadFileAPI;
    private PriorityRunnable priorityRunnable;

    /**
     * To listener upload result
     *
     * @param element
     * @param mListener
     */
    public UploadFileTask(UploadElement element, @Nullable OnTransferResultListener<UploadElement> mListener) {
        this(element, mListener, null);
    }

    public UploadFileTask(UploadElement element, @Nullable OnTransferResultListener<UploadElement> mListener, WorkQueueExecutor executor) {
        this.mElement = element;
        this.mResultListener = mListener;
        this.executor = executor;
    }


    /**
     * To listener upload progress
     *
     * @param element
     * @param mListener
     */
    public UploadFileTask(UploadElement element, @Nullable OnTransferFileListener<UploadElement> mListener, WorkQueueExecutor executor) {
        this.mElement = element;
        this.mUploadListener = mListener;
        this.executor = executor;
    }

    public UploadFileTask(UploadElement element, OnTransferFileListener<UploadElement> listener) {
        this(element, listener, null);
    }

    @Override
    public void run() {
        if (mElement.getState() == TransferState.WAIT) {
            loginSession = SessionManager.getInstance().getLoginSession(mElement.getDevId());
            if (loginSession != null && loginSession.isLogin()) {
                doUpload(loginSession);
            } else {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                AppExecutors.Companion.getInstance().mainThread().execute(() ->
                        SessionManager.getInstance().getLoginSession(mElement.getToDevId(),
                        new GetSessionListener(false) {

                            @Override
                            public void onSuccess(String url, LoginSession data) {
                                loginSession = data;
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onFailure(String url, int errorNo, String errorMsg) {
                                doUploadException(url);
                                countDownLatch.countDown();
                            }
                        }));
                try {
                    countDownLatch.await();
                    if (loginSession != null) {
                        doUpload(loginSession);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    doUploadException("");
                }
            }
        }
    }

    private void doUploadException(String url) {
        mElement.setState(TransferState.FAILED);
        mElement.setException(TransferException.FAILED_REQUEST_SERVER);
        if (mUploadListener != null) {
            mUploadListener.onComplete(url, mElement);
        } else {
            if (mResultListener != null) {
                mResultListener.onResult(mElement);
            }
        }
    }

    private void doUpload(@NonNull LoginSession data) {
        uploadFileAPI = new OneOSUploadFileAPI(data, mElement);
        uploadFileAPI.setOnUploadFileListener(new OnTransferFileListener<UploadElement>() {
            @Override
            public void onStart(String url, @NonNull UploadElement element) {
                Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "Start Upload file: " + element.getSrcPath());
                if (mUploadListener != null) {
                    mUploadListener.onStart(url, element);
                }
            }

            @Override
            public void onTransmission(String url, final UploadElement element) {
                if (mUploadListener != null) {
                    mUploadListener.onTransmission(url, element);
                }
            }

            @Override
            public void onComplete(String url, @NonNull final UploadElement element) {
                Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "Complete Upload file: " + element.getSrcPath() + ", state: " + element.getState());
                if (mUploadListener != null) {
                    mUploadListener.onComplete(url, element);
                } else {
                    if (mResultListener != null) {
                        mResultListener.onResult(element);
                    }
                }
            }
        });
        uploadFileAPI.upload();
    }

    public void stopUpload() {
        mElement.setState(TransferState.PAUSE);
        if (null != uploadFileAPI) {
            uploadFileAPI.stopUpload();
        } else if (executor != null && mElement.getState() == TransferState.WAIT) {
            executor.remove(priorityRunnable);
        }
        mElement.setState(TransferState.PAUSE);
        Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "Stop Upload file");
    }

    public void start() {
        if (mElement.getState() != TransferState.WAIT
                && mElement.getState() != TransferState.START) {
            if (mElement.getException() == TransferException.UNKNOWN_EXCEPTION) {
                restart();
            } else {
                mElement.setState(TransferState.WAIT);
                priorityRunnable = new PriorityRunnable(mElement.getPriority(), this);
                if (executor != null) {
                    executor.execute(priorityRunnable);
                } else {
                    run();
                }
                Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "add Upload file to queue" + mElement.getSrcName());
            }
        } else if (mElement.getState() == TransferState.COMPLETE) {
            if (mUploadListener != null) {
                mUploadListener.onComplete(null, mElement);
            } else if (mResultListener != null) {
                mResultListener.onResult(mElement);
            }
            if (executor != null) {
                executor.remove(priorityRunnable);
            }
        }
    }

    public void pause() {
        stopUpload();
    }

    @Override
    public void restart() {
        mElement.setLength(0);
        mElement.setOffset(0);
        mElement.setState(TransferState.NONE);
        mElement.setException(TransferException.NONE);
        deleteTmpFile(false);
    }

    private void deleteTmpFile(boolean isCancel) {
        SessionManager.getInstance().getLoginSession(mElement.getToDevId(), new GetSessionListener() {
            @Override
            public void onSuccess(String url, LoginSession data) {
                loginSession = data;
                OneOSFileManageAPI.OnFileManageListener listener = new OneOSFileManageAPI.OnFileManageListener() {
                    @Override
                    public void onStart(String url, FileManageAction action) {

                    }

                    @Override
                    public void onSuccess(String url, FileManageAction action, String response) {
                        if (!isCancel) {
                            start();
                        }
                    }

                    @Override
                    public void onFailure(String url, FileManageAction action, int errorNo, String errorMsg) {
                        if (!isCancel) {
                            start();
                        }
                    }
                };

                if (!SessionCache.Companion.getInstance().isV5(loginSession.getId())) {//旧接口
                    OneOSFileManageAPI oneOSFileManageAPI = new OneOSFileManageAPI(loginSession);
                    oneOSFileManageAPI.setOnFileManageListener(listener);
                    OneOSFile oneOSFileTmp = new OneOSFile();
                    String path = mElement.getSrcPath() + ".tmpdata";
                    oneOSFileTmp.setPath(path);
                    List<OneOSFile> delList = Collections.singletonList(oneOSFileTmp);
                    oneOSFileManageAPI.delete(delList, true);
                }


//                String action = "deleteshift";
//                JSONArray pathList = new JSONArray();
//                String path = mElement.toPath+mElement.getSrcName() + ".tmpdata";
//                int share_path_type = mElement.toPath.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)?
//                        AppConstants.PUBLIC_SHARE_PATH_TYPE:AppConstants.PRIVATE_SHARE_PATH_TYPE;
//                if (path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
//                    //去掉public前缀
//                    path = path.substring(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR.length() - 1);
//                }
//                pathList.put(path);
//                V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
//
//                    @Override
//                    public void isNotV5() {
//                        OneOSFileManageAPI oneOSFileManageAPI = new OneOSFileManageAPI(loginSession);
//                        oneOSFileManageAPI.setOnFileManageListener(listener);
//                        OneOSFile oneOSFileTmp = new OneOSFile();
//                        String path = mElement.getSrcPath() + ".tmpdata";
//                        oneOSFileTmp.setPath(path);
//                        List<OneOSFile> delList = Collections.singletonList(oneOSFileTmp);
//                        oneOSFileManageAPI.delete(delList, true);
//                    }
//
//                    @Override
//                    public void fail(@NotNull BaseProtocol<Object> result) {
//                        listener.onFailure("", FileManageAction.DELETE, result.getError().getCode(), result.getError().getMsg());
//                    }
//
//                    @Override
//                    public void success(@NotNull BaseProtocol<Object> result) {
//                        listener.onSuccess("", FileManageAction.DELETE, "");
//
//                    }
//
//                    @Override
//                    public boolean retry() {
//                        V5Repository.Companion.INSTANCE().optFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), action, pathList, share_path_type, this);
//                        return true;
//                    }
//                };
//
//                V5Repository.Companion.INSTANCE().optFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), action, pathList, share_path_type, observer);
            }
        });

    }

    public void cancel() {
        if (mElement.getLength() < mElement.getSize()) {
            deleteTmpFile(true);
        }
        mElement.setState(TransferState.CANCELED);
        if (executor != null) {
            executor.remove(priorityRunnable);
        }
        mElement.setState(TransferState.CANCELED);
    }
}