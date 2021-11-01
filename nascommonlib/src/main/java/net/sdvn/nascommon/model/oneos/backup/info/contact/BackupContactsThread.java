package net.sdvn.nascommon.model.oneos.backup.info.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.BackupInfoKeeper;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoException;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoStep;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoType;
import net.sdvn.nascommon.model.oneos.backup.info.OnBackupInfoListener;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
import net.sdvn.nascommon.model.oneos.transfer.UploadFileTask;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public class BackupContactsThread implements Runnable {
    private static final String TAG = BackupContactsThread.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_CONTACTS;

    private static final BackupInfoType TYPE = BackupInfoType.BACKUP_CONTACTS;

    private Context context;
    @Nullable
    private OnBackupInfoListener mListener = null;
    @Nullable
    private BackupInfoException exception = null;
    //    private LoginSession loginSession = null;
    private String deviceId;

    public BackupContactsThread(String deviceId, @Nullable OnBackupInfoListener mListener) {
        this.deviceId = deviceId;
//        if (null == mListener) {
//            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "BackupInfoListener is NULL");
//            throw (new NullPointerException("BackupInfoListener is NULL"));
//        }
        this.mListener = mListener;
        context = Utils.getApp();
//        loginSession = LoginManage.getInstance().getLoginSession();
    }

    @Override
    public void run() {
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Start Backup Contacts");
        if (null != mListener) {
            mListener.onStart(TYPE);
        }

        if (exportContacts()) {
            uploadContacts();
        }

        if (null == exception) {
            long time = System.currentTimeMillis();
            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Backup Contacts Success, Update database: " + time);
            BackupInfoKeeper.update(deviceId, BackupInfoType.BACKUP_CONTACTS, time);
        }

        if (mListener != null) {
            mListener.onComplete(TYPE, exception);
        }

        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Complete Backup Contacts");
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Upload contacts file to server
     */
    private void uploadContacts() {
        File file = new File(context.getCacheDir().getAbsolutePath() + File.separator + AppConstants.BACKUP_CONTACTS_FILE_NAME);
        String path = AppConstants.BACKUP_INFO_ONEOS_ROOT_DIR;

        final UploadElement element = new UploadElement(file, path);
        element.setToDevId(deviceId);
//        element.saveUid(LoginManage.getInstance().getLoginSession().getUserInfo().getId());
//        element.setFile(file);
//        element.setToPath(path);
        element.setOverwrite(true);
        final UploadFileTask uploadFileTask = new UploadFileTask(element, new OnTransferFileListener<UploadElement>() {
            @Override
            public void onStart(String url, UploadElement element) {
                if (null != mListener) {
                    mListener.onBackup(TYPE, BackupInfoStep.UPLOAD, 0);
                }
            }

            @Override
            public void onTransmission(String url, UploadElement element) {
                if (null != mListener) {
                    int progress = (int) (((float) element.getLength() / (float) element.getSize()) * 100);
                    mListener.onBackup(TYPE, BackupInfoStep.UPLOAD, progress);
                }
            }

            @Override
            public void onComplete(String url, UploadElement element) {
                if (null != mListener) {
                    if (element.getState() == TransferState.COMPLETE) {
                        exception = null;
                        mListener.onBackup(TYPE, BackupInfoStep.UPLOAD, 100);
                    } else {
                        exception = BackupInfoException.UPLOAD_ERROR;
                    }
                }
            }
        });
        uploadFileTask.start();

    }

    /**
     * Exporting contacts from the phone
     */
    private boolean exportContacts() {
        boolean result = true;

        String fileName = context.getCacheDir().getAbsolutePath() + File.separator + AppConstants.BACKUP_CONTACTS_FILE_NAME;
        ContentResolver cResolver = context.getContentResolver();
        String[] projection = {ContactsContract.Contacts._ID};
        Cursor cursor = cResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
        BufferedWriter buffer = null;
        try {
            buffer = new BufferedWriter(new FileWriter(fileName));
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    final long maxlen = cursor.getCount();
                    // 线程中执行导出
                    long exportlen = 0;
                    String id;
                    Contact parseContact = new Contact();
                    do {
                        id = cursor.getString(0);
                        parseContact.getContactInfoFromPhone(id, cResolver);
                        parseContact.writeVCard(buffer);
                        ++exportlen;
                        // 更新进度条
                        setExportProgress(maxlen, exportlen);
                    } while (cursor.moveToNext());

                    setExportProgress(maxlen, exportlen);
                    buffer.flush();
                    buffer.close();
                    result = true;
                } else {
                    Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "No Contacts to Export");
                    //                showSyncTips(R.string.no_contact_to_sync);
                    exception = BackupInfoException.NO_BACKUP;
                    result = false;
                }
            }
        } catch (Exception e) {
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Error Export Contacts", e);
//            showSyncTips(R.string.error_export_contacts);
            exception = BackupInfoException.ERROR_EXPORT;
            result = false;
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    exception = BackupInfoException.ERROR_EXPORT;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    private void setExportProgress(long total, long read) {
        Logger.p(Logger.Level.INFO, IS_LOG, TAG, "ExportProgress: total = " + total + " ; read = " + read);
        int progress = (int) (((float) read / (float) total) * 100);
        if (null != mListener) {
            mListener.onBackup(TYPE, BackupInfoStep.EXPORT, progress);
        }
    }
}
