package net.sdvn.nascommon.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;

import com.rxjava.rxlife.RxLife;

import net.sdvn.cmapi.util.UriUtil;
import net.sdvn.common.internet.utils.NetworkUtils;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.db.BackupFileKeeper;
import net.sdvn.nascommon.db.DeviceSettingsKeeper;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.db.objecbox.DeviceSettings;
import net.sdvn.nascommon.fileserver.constants.SharePathType;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.backup.file.BackupAlbumManager;
import net.sdvn.nascommon.model.oneos.backup.file.BackupFileManager;
import net.sdvn.nascommon.model.oneos.event.EventMsgManager;
import net.sdvn.nascommon.model.oneos.tansfer_safebox.SafeBoxDownloadManager;
import net.sdvn.nascommon.model.oneos.tansfer_safebox.SafeBoxUploadManager;
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferElement;
import net.sdvn.nascommon.model.oneos.transfer.TransferManager;
import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
import net.sdvn.nascommon.model.oneos.transfer_r.DownloadManagerR;
import net.sdvn.nascommon.model.oneos.transfer_r.UploadManagerR;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommon.model.phone.LocalFileManage;
import net.sdvn.nascommon.receiver.NetworkStateManager;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.IOUtils;
import net.sdvn.nascommon.utils.MediaScanner;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.UriUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NasService extends LifecycleService {
    private static final String TAG = NasService.class.getSimpleName();

    private Context context;
    private ServiceBinder mBinder;
    @Nullable
    private BackupAlbumManager mBackupAlbumManager;
    @Nullable
    private BackupFileManager mBackupFileManager;
    private NetworkStateManager.OnNetworkStateChangedListener onNetworkStateChangedListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.LOGI(TAG, "NasService create.");
        context = this;
        onNetworkStateChangedListener = new NetworkStateManager.OnNetworkStateChangedListener() {
            private int statusCode;

            @Override
            public void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable) {
                boolean isLogined = SPUtils.getBoolean(AppConstants.SP_FIELD_IS_LOGINED);
                if (isAvailable && !isWifiAvailable) {
                    if (SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
                        Timber.d("pause by SP_FIELD_ONLY_WIFI_CARE");
                        DownloadManagerR.INSTANCE.pauseDeviceId("");
                        UploadManagerR.INSTANCE.pauseDeviceId("");
                    }
                } else if (!isAvailable) {
                    DownloadManagerR.INSTANCE.pauseDeviceId("");
                    UploadManagerR.INSTANCE.pauseDeviceId("");
                }
                if (mBackupAlbumManager != null) {
                    mBackupAlbumManager.onChanged(isWifiAvailable && isLogined);
                }
                if (mBackupFileManager != null) {
                    mBackupFileManager.onChanged(isWifiAvailable && isLogined);
                }
            }

            @Override
            public void onStatusConnection(int statusCode) {
                this.statusCode = statusCode;
            }
        };
        NetworkStateManager.Companion.getInstance().addNetworkStateChangedListener(onNetworkStateChangedListener);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Logger.LOGI(TAG, "onStartCommand " + intent.getAction());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetworkStateManager.Companion.getInstance().removeNetworkStateChangedListener(onNetworkStateChangedListener);
        notifyUserLogout();
        EventMsgManager.Companion.getInstance().onDestroy();
        MediaScanner.getInstance().stop();
        DownloadManagerR.INSTANCE.onDestroy();
        UploadManagerR.INSTANCE.onDestroy();
        // UploadManager.getInstance().onDestroy();
        Logger.LOGI(TAG, "NasService destroy.");
    }

    @Override
    public IBinder onBind(@NotNull Intent intent) {
        super.onBind(intent);
        if (mBinder == null) {
            mBinder = new ServiceBinder();
        }
        return mBinder;
    }


    public void addUploadTasks(final String toId, final String toPath, List<LocalFile> fileList,Long groupId, LocalFileManage.OnManageCallback callback) {
        final List<LocalFile> localFiles = Collections.synchronizedList(fileList);
        Disposable disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            ArrayList<UploadElement> elements = new ArrayList<>();
            for (LocalFile localFile : localFiles) {
                try {
                    File file = localFile.getFile();
                    if (file != null) {
                        UploadElement element = new UploadElement(file, toPath);
                        if (FileUtils.isPictureFile(file.getName()) || FileUtils.isVideoFile(file.getName()) || FileUtils.isGifFile(file.getName()))
                            element.setThumbUri(Uri.fromFile(file));
                        if (!TextUtils.isEmpty(toId))
                            element.setToDevId(toId);
                        element.setGroup(groupId);
                        element.setTime(System.currentTimeMillis());
                        elements.add(element);
                    }
                } catch (Exception ignore) {

                }
            }
            if (toPath.startsWith(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR)) {
                SafeBoxUploadManager.INSTANCE.enqueue(elements);
            } else {
                UploadManagerR.INSTANCE.enqueue(elements);
            }


            emitter.onNext(true);
        }).subscribeOn(getDiskIOExecutor())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxLife.as(this))
                .subscribe(isSuccess -> {
                    if (isSuccess)
                        ToastHelper.showToast(R.string.Added_to_the_queue_being_uploaded);
                }, throwable -> Logger.LOGE(TAG, throwable, "upload files"));
    }

    public void addUploadTasks(List<Uri> uris, final String toId, final String toPath, LocalFileManage.OnManageCallback callback, boolean nonWifiAccess) {
        final List<Uri> uriList = Collections.synchronizedList(uris);
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                for (Uri uri : uriList) {
                    Logger.LOGD("upload:", "uri: " + uri.toString());
                    if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                        InputStream is = null;
                        OutputStream os = null;

                        try {
                            String displayName = UriUtils.getDisplayNameForUri(uri, getApplicationContext());
                            if (displayName == null) {
                                displayName = Utils.generateDisplayName(uri);
                            }

                            File tmpFile = new File(FileUtils.getCachePath(), displayName);
                            if (tmpFile.exists()) {
                                tmpFile.delete();
                            }
                            tmpFile.createNewFile();
                            is = getContentResolver().openInputStream(uri);
                            os = new FileOutputStream(tmpFile);
                            byte[] buffer = new byte[512];
                            int count;
                            while ((count = is.read(buffer)) > 0) {
                                os.write(buffer, 0, count);
                            }
                            os.flush();
                            os.close();
                            addUploadTask(tmpFile, toPath, toId);
                            Logger.LOGD("upload:", "tmp file path : " + tmpFile.getAbsolutePath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            IOUtils.close(is);
                            IOUtils.close(os);
                        }

                    } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                        File file = new File(UriUtil.getPath(getApplicationContext(), uri));
                        if (file.exists()) {
                            addUploadTask(file, toPath, toId);
                        }
                    }

                }
                emitter.onNext(true);
            }
        }).subscribeOn(getDiskIOExecutor())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxLife.as(this))
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccess) {
                        if (isSuccess)
                            ToastHelper.showToast(R.string.Added_to_the_queue_being_uploaded);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Logger.LOGE(TAG, throwable, "upload files");
                    }
                });
    }

    @NotNull
    private Scheduler getDiskIOExecutor() {
        return Schedulers.single();
    }

    public void addDownloadTasks(List<OneOSFile> selectedList, String id,@Nullable Long mGroupId) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                List<DownloadElement> elements = new ArrayList<>();
                for (OneOSFile file : selectedList) {
                    String toPath = SessionManager.getInstance().getDefaultDownloadPathByID(id, file);
                    DownloadElement element = new DownloadElement(file, toPath);
                    LoginSession loginSession = SessionManager.getInstance().getLoginSession(id);
                    if (loginSession != null && loginSession.isLogin()) {
                        if (file.isPicture() || file.isVideo() || file.isGif())
                            element.setThumbUri(Uri.parse(OneOSAPIs.genThumbnailUrl(loginSession, file)));
                    }
                    element.setGroup(mGroupId);
                    element.setSrcDevId(id);
                    element.setTime(System.currentTimeMillis());
                    elements.add(element);
                }
                if (selectedList.get(0).getShare_path_type() == SharePathType.SAFE_BOX.getType()) {
                    SafeBoxDownloadManager.INSTANCE.enqueue(elements);
                } else {
                    DownloadManagerR.INSTANCE.enqueue(elements);
                }
                emitter.onNext(true);
            }
        }).subscribeOn(getDiskIOExecutor())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxLife.as(this))
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccess) {
                        if (isSuccess) {
                            ToastHelper.showToast(R.string.Added_to_the_queue_being_download);
                        }
                    }
                });
    }

    public class ServiceBinder extends Binder {
        @NonNull
        public NasService getService() {
            return NasService.this;
        }

        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    }

    public void notifyUserLogin(String deviceId) {
        if (!TextUtils.isEmpty(deviceId)) {
            startBackupAlbum(deviceId);
            startBackupFile();
            EventMsgManager.Companion.getInstance().startReceive(deviceId);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    public void notifyUserLogout() {
        pauseDownload();
        pauseUpload();
        stopBackupAlbum();
        stopBackupFile();
//        EventMsgManager.getInstance().stopReceive();
    }

    // ====================================建立长连接线程==========================================
//    public void addOnEventMsgListener(EventMsgManager.OnEventMsgListener listener) {
//        if (null != EventMsgManager.getInstance()) {
//            EventMsgManager.getInstance().setOnEventMsgListener(listener);
//        }
//    }
//
//    public void removeOnEventMsgListener(EventMsgManager.OnEventMsgListener listener) {
//        if (null != EventMsgManager.getInstance()) {
//            EventMsgManager.getInstance().removeOnEventMsgListener(listener);
//        }
//    }

    // ==========================================Auto Backup File==========================================
    public void startBackupFile() {
//        if (!LoginManage.getInstance().isHttp()) {
//            Logger.LOGE(TAG, "SSUDP, Do not open auto backup file");
//            return;
//        }
//        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
//        if (!(loginSession != null && loginSession.getUserSettings() != null
//                && loginSession.getUserSettings().getIsAutoBackupFile())) {
//            Logger.LOGE(TAG, "Do not open auto backup file");
//            return;
//        }

        if (mBackupFileManager == null) {
            mBackupFileManager = new BackupFileManager(context);
            mBackupFileManager.startBackup();
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "======Start BackupFile=======");
        }
    }

    public void addOnBackupFileListener(BackupFileManager.OnBackupFileListener listener) {
        if (null != mBackupFileManager) {
            mBackupFileManager.setOnBackupFileListener(listener);
        }
    }

    public boolean deleteBackupFile(@NonNull BackupFile file) {
        if (null != mBackupFileManager) {
            return mBackupFileManager.deleteBackupFile(file);
        }

        return false;
    }

    public boolean stopBackupFile(@NonNull BackupFile file) {
        if (null != mBackupFileManager) {
            return mBackupFileManager.stopBackupFile(file);
        }

        return false;
    }

    public boolean addBackupFile(@NonNull BackupFile file) {
        if (null != mBackupFileManager) {
            return mBackupFileManager.addBackupFile(file);
        }

        return false;
    }

    public boolean isBackupFile() {
        if (null != mBackupFileManager) {
            return mBackupFileManager.isBackup();
        }

        return false;
    }

    public void stopBackupFile() {
        if (mBackupFileManager != null) {
            mBackupFileManager.stopBackup();
            mBackupFileManager = null;
        }
    }
    // ==========================================Auto Backup File==========================================


    // ==========================================Auto Backup Album==========================================
    public void startBackupAlbum(final String deviceId) {
        final DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
        if (settings != null && settings.getIsAutoBackupAlbum()) {
            stopBackupAlbum();
            SPHelper.put(AppConstants.SP_FIELD_BAK_ALBUM_LAST_DEV_ID, deviceId);
            if (!NetworkUtils.isWifi(context) && settings.getIsBackupAlbumOnlyWifi()) {
                return;
            }
            mBackupAlbumManager = new BackupAlbumManager(deviceId, context.getApplicationContext());
            mBackupAlbumManager.startBackup();
//            final Constraints.Builder builder = new Constraints.Builder();
////            if (Build.VERSION.SDK_INT >= 24) {
////                builder.setTriggerContentUpdateDelay(1, TimeUnit.MINUTES);
////            }
////            if (Build.VERSION.SDK_INT >= 23) {
////                builder.setRequiresDeviceIdle(true);
////            }
//            final Constraints constraints = builder
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
//                    .setRequiresBatteryNotLow(true)
//                    .build();
//            final PeriodicWorkRequest workRequest = new PeriodicWorkRequest
//                    .Builder(BackupAlbumWorker.class, 4, TimeUnit.HOURS)
//                    .setConstraints(constraints)
//                    .setInputData(new Data.Builder()
//                            .putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
//                            .build())
//                    .build();
//            WorkManager.getInstance(this)
//                    .enqueueUniquePeriodicWork(AppConstants.SP_FIELD_BAK_ALBUM_LAST_DEV_ID,
//                            ExistingPeriodicWorkPolicy.REPLACE, workRequest);
//            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "======Start BackupAlbum=======");
        }


    }

    public void stopBackupAlbum() {
        if (mBackupAlbumManager != null) {
            mBackupAlbumManager.stopBackup();
            mBackupAlbumManager = null;
        }

    }

    public void resetBackupAlbum(String deviceId) {
        stopBackupAlbum();
        BackupFileKeeper.resetBackupAlbum(deviceId);
        startBackupAlbum(deviceId);
    }

    @Nullable
    public String getBackupAlbumTarget() {
        if (mBackupAlbumManager == null) {
            return null;
        }
        return mBackupAlbumManager.getTarget();
    }

    public int getBackupAlbumCount() {
        if (mBackupAlbumManager == null) {
            return 0;
        }
        return mBackupAlbumManager.getBackupListSize();
    }

    public void setOnBackupAlbumListener(OnTransferFileListener<UploadElement> listener) {
        if (mBackupAlbumManager != null) {
            mBackupAlbumManager.setOnBackupListener(listener);
        }
    }

    public void removeOnBackupAlbumListener(OnTransferFileListener<UploadElement> listener) {
        if (mBackupAlbumManager != null) {
            mBackupAlbumManager.removeOnBackupListener(listener);
        }
    }
    // ==========================================Auto Backup Album==========================================


    // ========================================Download and Upload file======================================
    // Download Operation
    public int addDownloadTask(@NonNull OneOSFile file, String savePath, String srcDevId) {
        DownloadElement element = new DownloadElement(file, savePath);
        LoginSession loginSession = SessionManager.getInstance().getLoginSession(srcDevId);
        if (loginSession != null && loginSession.isLogin()) {
            if (file.isPicture() || file.isVideo() || file.isGif())
                element.setThumbUri(Uri.parse(OneOSAPIs.genThumbnailUrl(loginSession, file)));
        }
        element.setSrcDevId(srcDevId);
        element.setTime(System.currentTimeMillis());
        return DownloadManagerR.INSTANCE.enqueue(element);
    }

    public int addDownloadTask(@NonNull OneOSFile file, int priority, String srcDevId) {
        String savePath = SessionManager.getInstance().getDefaultDownloadPathByID(srcDevId, file);
        DownloadElement element = new DownloadElement(file, savePath);
        LoginSession loginSession = SessionManager.getInstance().getLoginSession(srcDevId);
        if (loginSession != null && loginSession.isLogin()) {
            if (file.isPicture() || file.isVideo() || file.isGif())
                element.setThumbUri(Uri.parse(OneOSAPIs.genThumbnailUrl(loginSession, file)));
        }
        element.setSrcDevId(srcDevId);
        element.setPriority(priority);
        element.setTime(System.currentTimeMillis());
        if (file.getShare_path_type() == SharePathType.SAFE_BOX.getType()) {
            return SafeBoxDownloadManager.INSTANCE.enqueue(element);
        } else {
            return DownloadManagerR.INSTANCE.enqueue(element);
        }

    }

    @NonNull
    public List<DownloadElement> getDownloadList() {
        return new ArrayList<DownloadElement>();
    }

    public boolean pauseDownload(String tag) {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "pause download: " + tag);
        DownloadManagerR.INSTANCE.pause(tag);
        return true;
    }

    public boolean pauseDownload() {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "pause all download task");
        DownloadManagerR.INSTANCE.pause();
        return true;
    }

    public boolean continueDownload(String tag) {
        DownloadManagerR.INSTANCE.wakeUpTask(true);
        return true;
    }

    public void continueDownload(@NonNull List<String> tags) {
        DownloadManagerR.INSTANCE.wakeUpTask(true);
    }

    public void pauseDownload(@NotNull List<String> tags) {
        DownloadManagerR.INSTANCE.pause(tags);
    }


    public boolean continueDownload() {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "continue all download task");
        DownloadManagerR.INSTANCE.wakeUpTask(true);
        return true;
    }

    public int cancelDownload(String path) {
        // return DownloadManager.getInstance().cancel(path);
        return 0;
    }

    public void cancelDownload(List<String> tags) {
        //  DownloadManager.getInstance().cancel(tags);
    }

    public boolean cancelDownload() {
        // return DownloadManager.getInstance().cancel();
        return true;
    }

    // Upload Operation
    public int addUploadTask(@NonNull File file, String savePath, @NonNull LoginSession loginSession) {
        UploadElement element = new UploadElement(file, savePath);
        if (FileUtils.isPictureFile(file.getName()) || FileUtils.isVideoFile(file.getName()) || FileUtils.isGifFile(file.getName()))
            element.setThumbUri(Uri.fromFile(file));
//        element.saveUid(LoginManage.getInstance().getLoginSession().getUserInfo().getId());
        element.setToDevId(loginSession.getId());
        element.setTime(System.currentTimeMillis());

        if (savePath.startsWith(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR)) {
            return SafeBoxUploadManager.INSTANCE.enqueue(element);
        } else {
            return UploadManagerR.INSTANCE.enqueue(element);
        }
    } // Upload Operation

    public int addUploadTask(@NonNull File file, String savePath, @NonNull String toDev) {
        UploadElement element = new UploadElement(file, savePath);
        if (FileUtils.isPictureFile(file.getName()) || FileUtils.isVideoFile(file.getName()) || FileUtils.isGifFile(file.getName()))
            element.setThumbUri(Uri.fromFile(file));
//        element.saveUid(LoginManage.getInstance().getLoginSession().getUserInfo().getId());
        if (!TextUtils.isEmpty(toDev))
            element.setToDevId(toDev);
        element.setTime(System.currentTimeMillis());

        if (savePath.startsWith(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR)) {
            return SafeBoxUploadManager.INSTANCE.enqueue(element);
        } else {
            return UploadManagerR.INSTANCE.enqueue(element);
        }
    }


    public void pauseUpload(String tag) {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "pause upload: " + tag);
        // UploadManager.getInstance().pause(tag);
    }

    public void pauseUpload() {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "pause all upload task");
        // UploadManager.getInstance().pause();
    }

    public void continueUpload(@NotNull List<String> tags) {
        // UploadManager.getInstance().resume(tags);

    }

    public void pauseUpload(@NotNull List<String> tags) {
        // UploadManager.getInstance().pause(tags);

    }

    public void continueUpload(String tag) {
        // Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "continue upload: " + tag);
        //UploadManager.getInstance().resume(tag);
    }

    public void continueUpload() {
        //  Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "continue all upload task");
        //  UploadManager.getInstance().resume();
    }


    public void cancelUpload(List<String> tags) {
        //UploadManager.getInstance().cancel(tags);
    }

    public void cancelUpload() {
        //UploadManager.getInstance().cancel();
    }

    /**
     * add download complete listener
     */
    public boolean addDownloadCompleteListener(TransferManager.OnTransferCompleteListener<DownloadElement> listener) {
        return DownloadManagerR.INSTANCE.addTransferCompleteListener(listener);
    }

    /**
     * remove download complete listener
     */
    public boolean removeDownloadCompleteListener(TransferManager.OnTransferCompleteListener<DownloadElement> listener) {
        return DownloadManagerR.INSTANCE.removeTransferCompleteListener(listener);
    }

    public void setOnTransferFileListener(OnTransferFileListener<TransferElement> onTransferFileListener) {
        // DownloadManager.getInstance().setOnTransferFileListener(onTransferFileListener);
        //  UploadManager.getInstance().setOnTransferFileListener(onTransferFileListener);
    }

//    /**
//     * add upload complete listener
//     */
//    public boolean addUploadCompleteListener(TransferManager.OnTransferCompleteListener<UploadElement> listener) {
//        if (null != UploadManager.getInstance()) {
//            return UploadManager.getInstance().addTransferCompleteListener(listener);
//        }
//        return true;
//    }

//    /**
//     * remove upload complete listener
//     */
//    public boolean removeUploadCompleteListener(TransferManager.OnTransferCompleteListener<UploadElement> listener) {
//        if (null != UploadManager.getInstance()) {
//            return UploadManager.getInstance().removeTransferCompleteListener(listener);
//        }

//        return true;
//    }
    // ========================================Download and Upload file======================================

    // =====================================Download and Upload Count Changed================================
    public void addOnTransferCountListener(TransferManager.OnTransferCountListener listener) {
        DownloadManagerR.INSTANCE.addOnTransferCountListener(listener);
        UploadManagerR.INSTANCE.addOnTransferCountListener(listener);
    }

    public void removeOnTransferCountListener(TransferManager.OnTransferCountListener listener) {
        DownloadManagerR.INSTANCE.removeOnTransferCountListener(listener);
        UploadManagerR.INSTANCE.removeOnTransferCountListener(listener);
    }
    // =====================================Download and Upload Count Changed=================================

//    // ========================================Download share file======================================
//    // Download Operation
//    public long addShareDownloadTask(ShareElement element) {
//        return ShareDownloadManager.getInstance().enqueue(element);
//    }
//
//    @NonNull
//    public List<ShareElement> getShareDownloadList() {
//        return ShareDownloadManager.getInstance().getTransferList();
//    }
//
//    public void pauseShareDownload(String shareToken) {
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "pause download: " + shareToken);
//        ShareDownloadManager.getInstance().pause(shareToken);
//    }
//
//    public void pauseShareDownload() {
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "pause all download task");
//        ShareDownloadManager.getInstance().pause();
//    }
//
//    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
//    public boolean continueShareDownload(String shareToken) {
//        return ShareDownloadManager.getInstance().resume(shareToken);
//    }
//
//    public void continueShareDownload() {
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "NasService", "continue all download task");
//        ShareDownloadManager.getInstance().resume();
//    }
//
//    public void cancelShareDownload(String shareToken) {
//        ShareDownloadManager.getInstance().cancel(shareToken);
//    }
//
//    public void cancelShareDownload() {
//        ShareDownloadManager.getInstance().cancel();
//    }
//
//    public boolean addShareDownloadCompleteListener(TransferManager.OnTransferCompleteListener<ShareElement> listener) {
//        if (null != ShareDownloadManager.getInstance()) {
//            return ShareDownloadManager.getInstance().addTransferCompleteListener(listener);
//        }
//
//        return true;
//    }
//
//    /**
//     * remove download complete listener
//     */
//    public boolean removeShareDownloadCompleteListener(TransferManager.OnTransferCompleteListener<ShareElement> listener) {
//        if (null != ShareDownloadManager.getInstance()) {
//            return ShareDownloadManager.getInstance().removeTransferCompleteListener(listener);
//        }
//
//        return true;
//    }

}
