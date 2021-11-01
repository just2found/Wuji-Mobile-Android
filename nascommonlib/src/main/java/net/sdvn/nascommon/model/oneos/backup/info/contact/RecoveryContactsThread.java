package net.sdvn.nascommon.model.oneos.backup.info.contact;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.BackupInfoKeeper;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoException;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoStep;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoType;
import net.sdvn.nascommon.model.oneos.backup.info.OnBackupInfoListener;
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement;
import net.sdvn.nascommon.model.oneos.transfer.DownloadFileTask;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferException;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.utils.IOUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class RecoveryContactsThread implements Runnable {
    private static final String TAG = BackupContactsThread.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_CONTACTS;

    private static final BackupInfoType TYPE = BackupInfoType.RECOVERY_CONTACTS;

    @Nullable
    private OnBackupInfoListener mListener = null;
    @Nullable
    private BackupInfoException exception = null;
    //    private LoginSession loginSession = null;
    private Context context;
    private String deviceId;

    public RecoveryContactsThread(String deviceId, @Nullable OnBackupInfoListener mListener) {
        this.deviceId = deviceId;
//        if (null == mListener) {
//            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "BackupInfoListener is NULL");
//            new Throwable(new NullPointerException("BackupInfoListener is NULL"));
//            return;
//        }
        this.mListener = mListener;
        context = Utils.getApp();
//        loginSession = LoginManage.getInstance().getLoginSession();
    }

    @Override
    public void run() {
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Start Recovery Contacts");
        if (null != mListener) {
            mListener.onStart(TYPE);
        }

        if (downloadContacts()) {
            if (importContacts()) {
                exception = null;
            }
        }

        if (null == exception) {
            long time = System.currentTimeMillis();
            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Recovery Contacts Success, Update database: " + time);
            BackupInfoKeeper.update(deviceId, BackupInfoType.RECOVERY_CONTACTS, time);
        }

        if (mListener != null) {
            mListener.onComplete(TYPE, exception);
        }

        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Complete Recovery Contacts");
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
    }

    private boolean downloadContacts() {
        String path = AppConstants.BACKUP_INFO_ONEOS_ROOT_DIR + AppConstants.BACKUP_CONTACTS_FILE_NAME;
        OneOSFile file = new OneOSFile();
        file.setPath(path);
        file.setName(AppConstants.BACKUP_CONTACTS_FILE_NAME);

        String targetPath = context.getCacheDir().getAbsolutePath();
        final DownloadElement downloadElement = new DownloadElement(file, targetPath);
        downloadElement.setCheck(false);
        DownloadFileTask downloadFileTask = new DownloadFileTask(downloadElement, new OnTransferFileListener<DownloadElement>() {
            @Override
            public void onStart(String url, DownloadElement element) {
                if (null != mListener) {
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, 0);
                }
            }

            @Override
            public void onTransmission(String url, @NonNull DownloadElement element) {
                if (null != mListener) {
                    int progress = (int) (((float) element.getLength() / (float) element.getSize()) * 100);
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, progress);
                }
            }

            @Override
            public void onComplete(String url, @NonNull DownloadElement element) {
                if (null != mListener) {
                    if (element.getState() == TransferState.COMPLETE) {
                        mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, 100);
                        exception = null;
                    } else {
                        if (element.getException() == TransferException.SERVER_FILE_NOT_FOUND) {
                            exception = BackupInfoException.NO_RECOVERY;
                        } else {
                            exception = BackupInfoException.DOWNLOAD_ERROR;
                        }
                    }
                }
            }
        }, null);
        downloadFileTask.start();
        return true;
    }

    /**
     * Importing SMS from the server to phone
     */
    private boolean importContacts() {
        boolean result;
        BufferedReader buffer = null;
        try {
            String path = context.getCacheDir().getAbsolutePath() + File.separator + AppConstants.BACKUP_CONTACTS_FILE_NAME;
            File file = new File(path);
            buffer = new BufferedReader(new FileReader(path));
            long maxLen = file.length();
            Logger.p(Logger.Level.INFO, IS_LOG, TAG, "All contacts length = " + maxLen);
            if (maxLen > 0) {
                long importLen = 0;
                Contact contact = new Contact();
                long read = 0;
                do {
                    read = contact.parseVCard(buffer);
                    if (read < 0) {
                        break;
                    }
                    contact.addContact(context, 0, false);
                    importLen += contact.getParseLen();
                    setProgress(importLen, maxLen);
                } while (true);
                result = true;
            } else {
//                showSyncTips(R.string.no_contact_to_sync);
                exception = BackupInfoException.NO_RECOVERY;
                result = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
//            showSyncTips(R.string.error_import_contacts);
            exception = BackupInfoException.ERROR_IMPORT;
            result = false;
        } finally {
            IOUtils.close(buffer);
        }

        return result;
    }

    /**
     * set import SMS progress_sync
     */
    private void setProgress(long write, long total) {
        Logger.p(Logger.Level.INFO, IS_LOG, TAG, "ExportProgress: total = " + total + " ; write = " + write);
        int progress = (int) (((float) write / (float) total) * 100);
        if (null != mListener) {
            mListener.onBackup(TYPE, BackupInfoStep.IMPORT, progress);
        }
    }

}