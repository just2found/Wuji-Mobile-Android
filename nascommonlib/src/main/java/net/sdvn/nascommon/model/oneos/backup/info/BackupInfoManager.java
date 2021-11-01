package net.sdvn.nascommon.model.oneos.backup.info;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.model.oneos.backup.info.contact.BackupContactsThread;
import net.sdvn.nascommon.model.oneos.backup.info.contact.RecoveryContactsThread;
import net.sdvn.nascommon.model.oneos.backup.info.sms.BackupSMSThread;
import net.sdvn.nascommon.model.oneos.backup.info.sms.RecoverySMSThread;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.concurrent.ConcurrentHashMap;

import libs.source.common.AppExecutors;

public class BackupInfoManager {
    private static final String TAG = BackupInfoManager.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_CONTACTS;

    private static class BackupInfoManagerHolder {
        private static BackupInfoManager instance = new BackupInfoManager();
    }

    @NonNull
    private OnBackupInfoListener mListener;
    @NonNull
    private ConcurrentHashMap<String, BackupContactsThread> backupContactsThreadMap = new ConcurrentHashMap<>();
    @NonNull
    private ConcurrentHashMap<String, RecoveryContactsThread> recoveryContactsThreadMap = new ConcurrentHashMap<>();
    @NonNull
    private ConcurrentHashMap<String, BackupSMSThread> backupSMSThreadMap = new ConcurrentHashMap<>();
    @NonNull
    private ConcurrentHashMap<String, RecoverySMSThread> recoverySMSThreadMap = new ConcurrentHashMap<>();
//    private BackupContactsThread backupContactsThread = null;
//    private RecoveryContactsThread recoveryContactsThread = null;
//    private BackupSMSThread backupSMSThread = null;
//    private RecoverySMSThread recoverySMSThread = null;

    /**
     * Singleton instance method
     *
     * @return singleton instance of class
     */
    @NonNull
    public static BackupInfoManager getInstance() {
        return BackupInfoManagerHolder.instance;
    }

    /**
     * Start Backup Contacts to server
     *
     * @param deviceId
     */
    public void startBackupContacts(@NonNull String deviceId, OnBackupInfoListener mListener) {
        BackupContactsThread backupContactsThread = backupContactsThreadMap.get(deviceId);
        if (null == backupContactsThread) {
            backupContactsThread = new BackupContactsThread(deviceId, mListener);
            backupContactsThreadMap.put(deviceId, backupContactsThread);
        } else {
            backupContactsThread.setOnBackupInfoListener(mListener);
        }
        AppExecutors.Companion.getInstance().networkIO().execute(backupContactsThread);
    }

    /**
     * Recover Contacts from server
     *
     * @param deviceId
     */
    public void startRecoverContacts(@NonNull String deviceId, OnBackupInfoListener mListener) {
        RecoveryContactsThread recoveryContactsThread = recoveryContactsThreadMap.get(deviceId);
        if (null == recoveryContactsThread) {
            recoveryContactsThread = new RecoveryContactsThread(deviceId, mListener);
            recoveryContactsThreadMap.put(deviceId, recoveryContactsThread);
        } else {
            recoveryContactsThread.setOnBackupInfoListener(mListener);
        }
        AppExecutors.Companion.getInstance().networkIO().execute(recoveryContactsThread);
    }

    /**
     * Start Backup SMS to server
     *
     * @param deviceId
     */
    public void startBackupSMS(@NonNull String deviceId, OnBackupInfoListener mListener) {
        BackupSMSThread backupSMSThread = backupSMSThreadMap.get(deviceId);
        if (null == backupSMSThread) {
            backupSMSThread = new BackupSMSThread(deviceId, mListener);
            backupSMSThreadMap.put(deviceId, backupSMSThread);
        } else {
            backupSMSThread.setOnBackupInfoListener(mListener);
        }
        AppExecutors.Companion.getInstance().networkIO().execute(backupSMSThread);
    }

    /**
     * Recover SMS from server
     *
     * @param deviceId
     */
    public void startRecoverSMS(@NonNull String deviceId, OnBackupInfoListener mListener) {
        RecoverySMSThread recoverySMSThread = recoverySMSThreadMap.get(deviceId);
        if (null == recoverySMSThread) {
            recoverySMSThread = new RecoverySMSThread(deviceId, mListener);
            recoverySMSThreadMap.put(deviceId, recoverySMSThread);

        } else {
            recoverySMSThread.setOnBackupInfoListener(mListener);
        }
        AppExecutors.Companion.getInstance().networkIO().execute(recoverySMSThread);
    }

    public void setOnBackupInfoListener(String deviceId, OnBackupInfoListener mListener) {
        this.mListener = mListener;
        BackupContactsThread backupContactsThread = backupContactsThreadMap.get(deviceId);
        if (null != backupContactsThread) {
            backupContactsThread.setOnBackupInfoListener(mListener);
        }
        RecoveryContactsThread recoveryContactsThread = recoveryContactsThreadMap.get(deviceId);
        if (null != recoveryContactsThread) {
            recoveryContactsThread.setOnBackupInfoListener(mListener);
        }
        BackupSMSThread backupSMSThread = backupSMSThreadMap.get(deviceId);
        if (null != backupSMSThread) {
            backupSMSThread.setOnBackupInfoListener(mListener);
        }
        RecoverySMSThread recoverySMSThread = recoverySMSThreadMap.get(deviceId);
        if (null != recoverySMSThread) {
            recoverySMSThread.setOnBackupInfoListener(mListener);
        }
    }
}
