package net.sdvn.nascommon.model.oneos.backup.file;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.common.internet.utils.NetworkUtils;
import net.sdvn.nascommon.db.BackupFileKeeper;
import net.sdvn.nascommon.db.DeviceSettingsKeeper;
import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.db.objecbox.DeviceSettings;
import net.sdvn.nascommon.model.oneos.backup.BackupPriority;
import net.sdvn.nascommon.model.oneos.backup.BackupType;
import net.sdvn.nascommon.model.oneos.backup.RecursiveFileObserver;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
import net.sdvn.nascommon.model.oneos.transfer.UploadFileTask;
import net.sdvn.nascommon.rx.RxWork;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.SDCardUtils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class BackupAlbumManager extends RxWork {
    private static final String TAG = BackupAlbumManager.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_ALBUM;
    private CopyOnWriteArraySet<BackupElement> mSet = new CopyOnWriteArraySet<>();
    @NonNull
    private List<RecursiveFileObserver> mFileObserverList = new ArrayList<>();
    @Nullable
    private OnTransferFileListener<UploadElement> progressListener = null;
    @NonNull
    private List<BackupFile> mBackupFiles;
    private Context context;

    @NonNull
    private ScanningAlbumThread.OnScanFileListener mScanListener =
            new ScanningAlbumThread.OnScanFileListener() {
                @Override
                public void onComplete(ArrayList<BackupElement> mBackupList) {
                    addBackupElements(mBackupList);
                    if (!EmptyUtils.isEmpty(mFileObserverList)) {
                        for (RecursiveFileObserver observer : mFileObserverList) {
                            observer.startWatching();
                        }
                    }
                }
            };

    @Nullable
    private RecursiveFileObserver.OnObserverCallback mObserverListener =
            new RecursiveFileObserver.OnObserverCallback() {

                @Override
                public void onAdd(@NonNull BackupFile backupInfo, @NonNull File file) {
                    BackupElement mElement = new BackupElement(backupInfo, file, true);
                    mElement.setToDevId(deviceId);
                    if (mElement != null) {
                        addBackupElement(mElement);
                    }
                }
            };
    private String deviceId;
    private Disposable mBackingSubscribe;

    public BackupAlbumManager(String deviceId, Context context) {
        this.deviceId = deviceId;

        mBackupFiles = BackupFileKeeper.all(deviceId, BackupType.ALBUM);
        this.context = context;
        initBackupPhotoIfNeeds();

        RecursiveFileObserver mFileObserver = new RecursiveFileObserver(mBackupFiles,
                RecursiveFileObserver.EVENTS_BACKUP_PHOTOS, mObserverListener);
        mFileObserverList.add(mFileObserver);

    }

    private boolean initBackupPhotoIfNeeds() {
        boolean isNewBackupPath = false;
//        final String pathsJson = SPHelper.get(AppConstants.SP_FIELD_CHOICE_BACKUP_ALBUM_PATHS, "[]");
//        final List<String> paths = GsonUtils.decodeJSON(pathsJson, new TypeToken<List<String>>() {
//        }.getType());
        final DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);

        if (settings != null && settings.getBackupAlbumPaths().size() > 0) {
            final List<String> paths = settings.getBackupAlbumPaths();
            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    BackupFile info = BackupFileKeeper.getBackupInfo(deviceId, file.getAbsolutePath(), BackupType.ALBUM);
                    if (null == info) {
                        info = new BackupFile(null, deviceId, file.getAbsolutePath(),
                                true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
                        BackupFileKeeper.insertBackupAlbum(info);
                        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
                        if (!isNewBackupPath)
                            isNewBackupPath = true;
                        mBackupFiles.add(info);
                    }
                }
            }
            final List<BackupFile> all = BackupFileKeeper.all(deviceId, BackupType.ALBUM);
            for (BackupFile backupFile : all) {
                boolean isExists = false;
                final String backupFilePath = backupFile.getPath();
                for (String path : paths) {
                    if (backupFilePath.equals(path)) {
                        isExists = true;
                        break;
                    }
                }
                if (!isExists) {
                    BackupFileKeeper.delete(backupFile);
                }
            }

        } else {
            ArrayList<File> extSDCards = SDCardUtils.getSDCardList();
            if (null != extSDCards && !extSDCards.isEmpty()) {
                for (File dir : extSDCards) {
                    File extDCIM = new File(dir, "DCIM");
                    if (extDCIM.exists() && extDCIM.canRead()) {
                        BackupFile info = BackupFileKeeper.getBackupInfo(deviceId, extDCIM.getAbsolutePath(), BackupType.ALBUM);
                        if (null == info) {
                            info = new BackupFile(null, deviceId, extDCIM.getAbsolutePath(),
                                    true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
                            BackupFileKeeper.insertBackupAlbum(info);
                            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
                            isNewBackupPath = true;
                            mBackupFiles.add(info);
                        }
                    }
                }
            }

            File mInternalDCIMDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            if (null != mInternalDCIMDir && mInternalDCIMDir.exists()) {
                BackupFile info = BackupFileKeeper.getBackupInfo(deviceId, mInternalDCIMDir.getAbsolutePath(), BackupType.ALBUM);
                if (null == info) {
                    info = new BackupFile(null, deviceId, mInternalDCIMDir.getAbsolutePath(),
                            true, BackupType.ALBUM, BackupPriority.MAX, 0L, 0L);
                    BackupFileKeeper.insertBackupAlbum(info);
                    Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Add New Backup Album Dir: " + info.getPath());
                    isNewBackupPath = true;
                    mBackupFiles.add(info);
                }
            }
        }


        return isNewBackupPath;
    }

    public void startBackup() {
        addDisposable(Observable.create(emitter -> {
            new ScanningAlbumThread(mBackupFiles, mScanListener).run();
        }).subscribeOn(Schedulers.io()).subscribe());

    }

    public void stopBackup() {
        if (mFileObserverList != null) {
            for (RecursiveFileObserver observer : mFileObserverList) {
                observer.stopWatching();
            }
        }
        clear();
    }

    public int getBackupListSize() {
        return mSet.size();
    }

    private boolean addBackupElements(List<BackupElement> mList) {
        if (mSet.addAll(mList)) {
            notifyDataChanged();
            return true;
        }
        return false;
    }

    private boolean addBackupElement(BackupElement mElement) {
        if (mSet.add(mElement)) {
            notifyDataChanged();
            return true;
        }
        return false;
    }

    public void onChanged(boolean isWifiAvailable) {
        if (!isWifiAvailable && isOnlyWifiTransfer()) {
            stopBackup();
        }
    }

    public boolean isOnlyWifiTransfer() {
        final DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
        return settings != null && settings.getIsAutoBackupAlbum() && settings.getIsBackupAlbumOnlyWifi() && !NetworkUtils.isWifi(context);
    }

    private synchronized void notifyDataChanged() {
        if (mBackingSubscribe == null) {
            Iterator<BackupElement> iterator = mSet.iterator();
            AtomicReference<Boolean> runFlag = new AtomicReference<>(false);
            AtomicReference<UploadFileTask> backupTaskRef = new AtomicReference<>();
            mBackingSubscribe = Observable.create(emitter -> {
                while (iterator.hasNext()) {
                    if (isOnlyWifiTransfer() || runFlag.get()) {
                        break;
                    }
                    BackupElement element = iterator.next();
                    backupTaskRef.set(new UploadFileTask(element, listener));
                    backupTaskRef.get().start();
                    if (element.getState() == TransferState.COMPLETE) {
                        element.setTime(System.currentTimeMillis());
                        long mLastBackupTime = element.getFile().lastModified();
                        if (mLastBackupTime > element.getBackupInfo().getTime()) {
                            element.getBackupInfo().setTime(mLastBackupTime);
                            if (BackupFileKeeper.update(element.getBackupInfo())) {
                                Timber.d("complete %s", element.getSrcName());
                            }
                        }
                    }
                    mSet.remove(element);
                }
                Timber.d("finish");
                emitter.onComplete();
            }).subscribeOn(Schedulers.io())
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {
                            runFlag.set(true);
                            UploadFileTask uploadFileTask = backupTaskRef.get();
                            if (uploadFileTask != null) {
                                uploadFileTask.stopUpload();
                            }
                        }
                    })
                    .subscribe(o -> {
                        mBackingSubscribe = null;
                    }, Timber::e);
            addDisposable(mBackingSubscribe);
        }
    }

    public void setOnBackupListener(OnTransferFileListener<UploadElement> listener) {
        Logger.LOGE(TAG, "----------------------");
        this.progressListener = listener;
    }

    public void removeOnBackupListener(OnTransferFileListener<UploadElement> listener) {
        if (this.progressListener == listener) {
            this.progressListener = null;
        }
    }

    public String getTarget() {
        return deviceId;
    }

    @NonNull
    private OnTransferFileListener<UploadElement> listener = new OnTransferFileListener<UploadElement>() {
        @Override
        public void onStart(String url, UploadElement element) {
            if (null != progressListener) {
                progressListener.onStart(url, element);
            }
        }

        @Override
        public void onTransmission(String url, UploadElement element) {
            if (null != progressListener) {
                progressListener.onTransmission(url, element);
            }
        }

        @Override
        public void onComplete(String url, UploadElement element) {
            if (null != progressListener) {
                progressListener.onComplete(url, element);
            }
        }
    };


}
