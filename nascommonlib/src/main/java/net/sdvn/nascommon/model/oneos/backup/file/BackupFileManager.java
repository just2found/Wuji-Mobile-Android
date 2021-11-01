package net.sdvn.nascommon.model.oneos.backup.file;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.BackupFileKeeper;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.model.oneos.backup.BackupType;
import net.sdvn.nascommon.model.oneos.backup.RecursiveFileObserver;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferResultListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
import net.sdvn.nascommon.model.oneos.transfer.UploadFileTask;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BackupFileManager {
    private static final String TAG = BackupFileManager.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_FILE;
    private static final int MAX_UPLOAD_COUNT = 2;

    private Context context;
    //    @Nullable
//    private LoginSession mLoginSession = null;
    private int uploadCount = MAX_UPLOAD_COUNT;
    @NonNull
    private List<BackupFileThread> mBackupThreadList = new ArrayList<>();
    private OnBackupFileListener listener;
    @Nullable
    private OnBackupFileListener callback = new OnBackupFileListener() {
        @Override
        public void onBackup(BackupFile backupFile, File file) {
            if (null != listener) {
                listener.onBackup(backupFile, file);
            }
        }

        @Override
        public void onStop(BackupFile backupFile) {
            if (null != listener) {
                listener.onStop(backupFile);
            }
        }
    };

    public BackupFileManager(/*LoginSession mLoginSession,*/ /*String mDevId,*/ Context context) {
//        this.mLoginSession = mLoginSession;
        this.context = context;

        List<BackupFile> backupDirList = BackupFileKeeper.all(BackupType.FILE);

        if (null != backupDirList) {
            for (BackupFile file : backupDirList) {
                if (file != null && file.getAuto()) {
                    BackupFileThread thread = new BackupFileThread(file, callback);
                    mBackupThreadList.add(thread);
                }
            }
        }
    }

    public void startBackup() {
        for (BackupFileThread thread : mBackupThreadList) {
            thread.start();
        }
    }

    public void stopBackup() {
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            if (thread.isAlive()) {
                thread.stopBackup();
            }
            iterator.remove();
        }
    }

    public boolean addBackupFile(@NonNull BackupFile file) {
        for (BackupFileThread thread : mBackupThreadList) {
            final BackupFile backupFile = thread.getBackupFile();
            if (backupFile == null) continue;
            if (backupFile == file || backupFile.getId() == file.getId()) {
                Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Add Item is exist: " + file.getPath());
                return false;
            }
        }

        BackupFileThread thread = new BackupFileThread(file, callback);
        mBackupThreadList.add(thread);
        thread.start();
        return true;
    }

    public boolean stopBackupFile(@NonNull BackupFile file) {
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            BackupFile tFile = thread.getBackupFile();
            if (tFile == null) continue;
            if (tFile == file || tFile.getId() == file.getId()) {
                tFile.setAuto(false);
                if (thread.isAlive()) {
                    thread.stopBackup();
                }
                iterator.remove();

                return true;
            }
        }

        return false;
    }

    public boolean deleteBackupFile(@NonNull BackupFile file) {
        Iterator<BackupFileThread> iterator = mBackupThreadList.iterator();
        while (iterator.hasNext()) {
            BackupFileThread thread = iterator.next();
            final BackupFile backupFile = thread.getBackupFile();
            if (backupFile == null) continue;
            if (backupFile == file || backupFile.getId() == file.getId()) {
                if (thread.isAlive()) {
                    thread.stopBackup();
                }
                iterator.remove();

                return true;
            }
        }

        return false;
    }

    public boolean isBackup() {
        for (BackupFileThread thread : mBackupThreadList) {
            if (thread.isBackup()) {
                return true;
            }
        }

        return false;
    }

    public void setOnBackupFileListener(OnBackupFileListener listener) {
        this.listener = listener;
    }

    private synchronized void consume() {
        while (uploadCount <= 0) {
            try {
                Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Upload count shortage, waiting...");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Consume upload count: " + (uploadCount - 1));
        this.notify();
        uploadCount--;
    }

    private synchronized void produce() {
        Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Produce upload count: " + (uploadCount + 1));
        uploadCount++;
        this.notify();
    }

    public void onChanged(boolean isWifiAvailable) {

    }

    private class BackupFileThread extends Thread {
        private final String TAG = BackupFileThread.class.getSimpleName();
        @NonNull
        private BackupFile backupFile;
        // 扫描备份失败的文件和新增加的文件列表
        private List<BackupElement> mAdditionalList = Collections.synchronizedList(new ArrayList<BackupElement>());
        @Nullable
        private UploadFileTask uploadFileTask;
        private boolean hasBackupTask = false;
        private OnBackupFileListener listener;
        private RecursiveFileObserver mFileObserver;
        @NonNull
        private RecursiveFileObserver.OnObserverCallback mObserverListener = new RecursiveFileObserver.OnObserverCallback() {
            @Override
            public void onAdd(@NonNull BackupFile backupInfo, @NonNull File file) {
                BackupElement element = new BackupElement(backupInfo, file, true);
                notifyAddNewBackupItem(element);
            }
        };
//        private List<TmpElemet> mServerList;

        public BackupFileThread(@NonNull BackupFile file, OnBackupFileListener listener) {
            super(file.getDevUUID() + file.getPath());
            this.backupFile = file;
            this.listener = listener;
        }

        private boolean doUploadFile(@NonNull BackupElement element) {
            // for control backup only in wifi
            boolean isOnlyWifiBackup = SPHelper.get(AppConstants.SP_FIELD_BAK_ONLY_WIFI_CARE, true);
            while (isOnlyWifiBackup && !Utils.isWifiAvailable(context)) {
                try {
                    Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "----Backup only wifi, but current is not, sleep 60s----");
                    sleep(60000); // sleep 60 * 1000 = 60s
                    isOnlyWifiBackup = SPHelper.get(AppConstants.SP_FIELD_BAK_ONLY_WIFI_CARE, true);
                    Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "----Is Backup Only Wifi: " + isOnlyWifiBackup);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, ">>>>>>> Ask for consume upload: " + this.getId());
            consume();
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, ">>>>>>> Consume upload count: " + this.getId());

            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Upload File: " + element.getSrcPath());
            if (null != this.listener) {
                this.listener.onBackup(element.getBackupInfo(), element.getFile());
            }
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final boolean[] result = new boolean[1];
            uploadFileTask = new UploadFileTask(element, new OnTransferResultListener<UploadElement>() {
                @Override
                public void onResult(UploadElement element) {
                    result[0] = element.getState() == TransferState.COMPLETE;
                    countDownLatch.countDown();
                }
            });
            element.setState(TransferState.WAIT);
            uploadFileTask.run();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            uploadFileAPI = new OneOSUploadFileAPI(mLoginSession, element);
//            boolean result = uploadFileAPI.upload();
//            uploadFileAPI = null;
//            Logger.p(Level.ERROR, IS_LOG, TAG, "<<<<<<< Return upload: " + this.getId());
            produce();
            if (result[1]) {
                Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Backup File Success: " + element.getSrcPath());
                return false;
            } else {
                Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Backup File Failed: " + element.getSrcPath());
                return true;
            }
        }

        private void scanningAndBackupFiles(BackupFile backupFile, @NonNull File dir) {
            if (isInterrupted()) {
                return;
            }

            if (dir.exists()) {
                if (dir.isDirectory()) {
                    Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Scanning Dir: " + dir.getPath());
                    File[] files = dir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(@NonNull File f) {
                            return !f.isHidden() && f.length() > 0;
                        }
                    });
                    for (File file : files) {
                        scanningAndBackupFiles(backupFile, file);
                    }
                } else {

                    BackupElement element = new BackupElement(backupFile, dir, true);
                    if (doUploadFile(element)) {
                        mAdditionalList.add(element);
                        Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Add to Additional List");
                    }
                    if (!isInterrupted()) {
                        try {
                            sleep(20); // sleep 20ms
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

        @Override
        public void run() {
            hasBackupTask = true;
            backupFile.setCount(backupFile.getCount() + 1);
            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Start scanning and upload file: " + backupFile.getPath());

            scanningAndBackupFiles(backupFile, new File(backupFile.getPath()));
            mFileObserver = new RecursiveFileObserver(Collections.singletonList(backupFile),
                    RecursiveFileObserver.EVENTS_BACKUP_PHOTOS, mObserverListener);
            mFileObserver.startWatching();
            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Scanning and upload file complete");

            while (!isInterrupted()) {
                Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Start upload AdditionalList files: " + mAdditionalList.size());
                if (!EmptyUtils.isEmpty(mAdditionalList)) {
                    hasBackupTask = true;
                    Iterator<BackupElement> iterator = mAdditionalList.iterator();
                    while (!isInterrupted() && iterator.hasNext()) {
                        if (doUploadFile(iterator.next())) {
                            iterator.remove();
                            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Remove Additional Element");
                        }
                        try {
                            sleep(20); // sleep 20ms
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Upload AdditionalList files complete");
                hasBackupTask = false;
                backupFile.setTime(System.currentTimeMillis());
                BackupFileKeeper.update(backupFile);
                if (null != listener) {
                    listener.onStop(backupFile);
                }

                try {
                    Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Waiting for AdditionalList Changed...");
                    synchronized (mAdditionalList) {
                        mAdditionalList.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized boolean notifyAddNewBackupItem(@Nullable BackupElement mElement) {
            if (mElement == null) {
                return false;
            }

            synchronized (mAdditionalList) {
                if (mAdditionalList.add(mElement)) {
                    if (!hasBackupTask) {
                        mAdditionalList.notify();
                    }
                } else {
                    return false;
                }
            }

            return true;
        }

        /**
         * stop backup
         */
        public void stopBackup() {
            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "====Stop Backup====");
            interrupt();
            if (null != mFileObserver) {
                mFileObserver.stopWatching();
            }
            if (uploadFileTask != null) {
                uploadFileTask.cancel();
                uploadFileTask = null;
            }
            backupFile.setTime(System.currentTimeMillis());
            BackupFileKeeper.update(backupFile);
        }

        @Nullable
        public BackupFile getBackupFile() {
            if (!isInterrupted()) {
                return backupFile;
            }

            return null;
        }

        public boolean isBackup() {
            return hasBackupTask;
        }
    }


//    public static class TmpElemet {
//        @Nullable
//        private String fullName;
//        private long length;
//
//        public TmpElemet() {
//            this.fullName = null;
//            this.length = 0;
//        }
//
//        @Nullable
//        public String getFullName() {
//            return fullName;
//        }
//
//        public void setFullName(String fullName) {
//            this.fullName = fullName;
//        }
//
//        public long getLength() {
//            return length;
//        }
//
//        public void setLength(long length) {
//            this.length = length;
//        }
//
//    }


    public interface OnBackupFileListener {
        void onBackup(BackupFile backupFile, File file);

        void onStop(BackupFile backupFile);
    }
}
