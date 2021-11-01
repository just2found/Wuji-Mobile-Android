//package net.sdvn.nascommon.model.oneos.backup.file;
//
//import android.content.Context;
//import android.os.Build;
//import android.os.Environment;
//import android.os.Process;
//import android.text.TextUtils;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.work.Data;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import net.sdvn.nascommon.SessionManager;
//import net.sdvn.nascommon.constant.AppConstants;
//import net.sdvn.nascommon.db.BackupFileKeeper;
//import net.sdvn.nascommon.db.DeviceSettingsKeeper;
//import net.sdvn.nascommon.db.SPHelper;
//import net.sdvn.nascommon.db.objecbox.BackupFile;
//import net.sdvn.nascommon.db.objecbox.DeviceSettings;
//import net.sdvn.nascommon.model.oneos.backup.BackupPriority;
//import net.sdvn.nascommon.model.oneos.backup.BackupType;
//import net.sdvn.nascommon.model.oneos.backup.RecursiveFileObserver;
//import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
//import net.sdvn.nascommon.model.oneos.transfer.TransferState;
//import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
//import net.sdvn.nascommon.model.oneos.transfer.UploadFileTask;
//import net.sdvn.nascommon.utils.EmptyUtils;
//import net.sdvn.nascommon.utils.SDCardUtils;
//import net.sdvn.nascommon.utils.Utils;
//import net.sdvn.nascommon.utils.log.Logger;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//public class BackupAlbumWorker extends Worker {
//    private List<BackupElement> mBackupElementList =new CopyOnWriteArrayList<>();
//
//    public BackupAlbumWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
//        super(context, workerParams);
//    }
//
//    @NonNull
//    @Override
//    public Result doWork() {
//        final Data inputData = getInputData();
//
//        Logger.LOGD(TAG,"Process.myPid "+ Process.myPid());
//        this.deviceId = inputData.getString(AppConstants.SP_FIELD_DEVICE_ID);
//        if (TextUtils.isEmpty(deviceId) || !SessionManager.getInstance().isLogin(deviceId)) {
//            Logger.LOGD(TAG," failure .............");
//            return Result.failure();
//        }
//        mBackupList = BackupFileKeeper.all(deviceId, BackupType.ALBUM);
//        initBackupPhotoIfNeeds();
//
//        RecursiveFileObserver mFileObserver = null;
//        for (BackupFile info : mBackupList) {
//            if (mFileObserver == null) {
//                mFileObserver = new RecursiveFileObserver(Collections.singletonList(info),
//                        RecursiveFileObserver.EVENTS_BACKUP_PHOTOS, mObserverListener);
//                mFileObserverList.add(mFileObserver);
//            } else {
//                mFileObserver.addWatching(info);
//            }
//
//        }
//        ScanningAlbumThread    mBackupThread = new ScanningAlbumThread(mBackupList, mScanListener);
//        mBackupThread.run();
//        // for control backup only in wifi
//        boolean isOnlyWifiBackup = SPHelper.get(AppConstants.SP_FIELD_BAK_ALBUM_ONLY_WIFI_CARE, true);
//        while (isOnlyWifiBackup && !Utils.isWifiAvailable(getApplicationContext())) {
//            try {
//                Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "----Is Backup Only Wifi: " + isOnlyWifiBackup);
//                Thread.sleep(60000); // sleep 60 * 1000 = 60s
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        for (BackupElement element : mBackupElementList) {
//            if (element.getState() == TransferState.NONE) {
//                Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Start a New Backup Task "+element.getTag());
//                UploadFileTask  backupPhotoThread = new UploadFileTask(element, progressListener);
//                backupPhotoThread.start();
//            }
//        }
//        Logger.LOGD(TAG," done .............");
//        return Result.success();
//    }
//
//    private static final String TAG = BackupAlbumWorker.class.getSimpleName();
//    private static final boolean IS_LOG = Logger.Logd.BACKUP_ALBUM;
//
//    @NonNull
//    private List<RecursiveFileObserver> mFileObserverList = new ArrayList<>();
//    @Nullable
//    private OnTransferFileListener<UploadElement> progressListener = null;
//
//    @NonNull
//    private List<BackupFile> mBackupList;
//    private long mLastBackupTime = 0;
//
//    @NonNull
//    private ScanningAlbumThread.OnScanFileListener mScanListener =
//            new ScanningAlbumThread.OnScanFileListener() {
//                @Override
//                public void onComplete(ArrayList<BackupElement> mBackupList) {
//                    mBackupElementList.addAll(mBackupList);
//                    if (!EmptyUtils.isEmpty(mFileObserverList)) {
//                        for (RecursiveFileObserver observer : mFileObserverList) {
//                            observer.startWatching();
//                        }
//                    }
//                }
//            };
//
//    @Nullable
//    private RecursiveFileObserver.OnObserverCallback mObserverListener =
//            new RecursiveFileObserver.OnObserverCallback() {
//
//                @Override
//                public void onAdd(@NonNull BackupFile backupInfo, @NonNull File file) {
//                    BackupElement mElement = new BackupElement(backupInfo, file, true);
//                    mElement.setToDevId(deviceId);
//                    if (mElement != null) {
//                        mBackupElementList.add(mElement);
//                    }
//                }
//            };
//    private String deviceId;
//
//
//    private boolean initBackupPhotoIfNeeds() {
//        boolean isNewBackupPath = false;
////        final String pathsJson = SPHelper.get(AppConstants.SP_FIELD_CHOICE_BACKUP_ALBUM_PATHS, "[]");
////        final List<String> paths = GsonUtils.decodeJSON(pathsJson, new TypeToken<List<String>>() {
////        }.getType());
//        final DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
//
//        if (settings != null && settings.getBackupAlbumPaths().size() > 0) {
//            final List<String> paths = settings.getBackupAlbumPaths();
//            for (String path : paths) {
//                File file = new File(path);
//                if (file.exists()) {
//                    BackupFile info = BackupFileKeeper.getBackupInfo(deviceId, file.getAbsolutePath(), BackupType.ALBUM);
//                    if (null == info) {
//                        info = new BackupFile(null, deviceId, file.getAbsolutePath(),
//                                true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
//                        BackupFileKeeper.insertBackupAlbum(info);
//                        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
//                        if (!isNewBackupPath)
//                            isNewBackupPath = true;
//                        mBackupList.add(info);
//                    }
//                }
//            }
//            final List<BackupFile> all = BackupFileKeeper.all(deviceId, BackupType.ALBUM);
//            for (BackupFile backupFile : all) {
//                boolean isExists = false;
//                final String backupFilePath = backupFile.getPath();
//                for (String path : paths) {
//                    if (Objects.equals(backupFilePath, path)) {
//                        isExists = true;
//                        break;
//                    }
//                }
//                if (!isExists) {
//                    BackupFileKeeper.delete(backupFile);
//                }
//            }
//
//        } else {
//            ArrayList<File> extSDCards = SDCardUtils.getSDCardList();
//            if (null != extSDCards && !extSDCards.isEmpty()) {
//                for (File dir : extSDCards) {
//                    File extDCIM = new File(dir, "DCIM");
//                    if (extDCIM.exists() && extDCIM.canRead()) {
//                        BackupFile info = BackupFileKeeper.getBackupInfo(deviceId, extDCIM.getAbsolutePath(), BackupType.ALBUM);
//                        if (null == info) {
//                            info = new BackupFile(null, deviceId, extDCIM.getAbsolutePath(),
//                                    true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
//                            BackupFileKeeper.insertBackupAlbum(info);
//                            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
//                            isNewBackupPath = true;
//                            mBackupList.add(info);
//                        }
//                    }
//                }
//            }
//            if (Build.VERSION.SDK_INT < 29) {
//                File mInternalDCIMDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//                if (null != mInternalDCIMDir && mInternalDCIMDir.exists()) {
//                    BackupFile info = BackupFileKeeper.getBackupInfo(deviceId, mInternalDCIMDir.getAbsolutePath(), BackupType.ALBUM);
//                    if (null == info) {
//                        info = new BackupFile(null, deviceId, mInternalDCIMDir.getAbsolutePath(),
//                                true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
//                        BackupFileKeeper.insertBackupAlbum(info);
//                        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
//                        isNewBackupPath = true;
//                        mBackupList.add(info);
//                    }
//                }
//            }
//        }
//
//
//        return isNewBackupPath;
//    }
//
//
//    @Override
//    public void onStopped() {
//        super.onStopped();
//        stopBackup();
//    }
//
//    public void stopBackup() {
//
//        if (mFileObserverList != null) {
//            for (RecursiveFileObserver observer : mFileObserverList) {
//                observer.stopWatching();
//            }
//        }
//    }
//
//
//    public String getTarget() {
//        return deviceId;
//    }
//
//}
