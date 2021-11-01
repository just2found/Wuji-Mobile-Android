package net.sdvn.nascommon.model.oneos.backup.info.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/26.
 */
public class RecoverySMSThread implements Runnable {
    private static final String TAG = BackupSMSThread.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_SMS;

    private static final BackupInfoType TYPE = BackupInfoType.RECOVERY_SMS;

    @Nullable
    private OnBackupInfoListener mListener = null;
    @Nullable
    private BackupInfoException exception = null;
    //    private LoginSession loginSession = null;
    private Context context;
    private String deviceId;

    public RecoverySMSThread(String deviceId, @Nullable OnBackupInfoListener mListener) {
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
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Start Recovery SMS");
        if (null != mListener) {
            mListener.onStart(TYPE);
        }

        if (downloadSMS()) {
            if (importSMS()) {
                exception = null;
            }
        }

        if (null == exception) {
            long time = System.currentTimeMillis();
            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Recovery SMS Success, Update database: " + time);
            BackupInfoKeeper.update(deviceId, BackupInfoType.RECOVERY_SMS, time);
        }

        if (mListener != null) {
            mListener.onComplete(TYPE, exception);
        }

        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Complete Recovery SMS");
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
    }

    private boolean downloadSMS() {
        String path = AppConstants.BACKUP_INFO_ONEOS_ROOT_DIR + AppConstants.BACKUP_SMS_FILE_NAME;
        OneOSFile file = new OneOSFile();
        file.setPath(path);
        file.setName(AppConstants.BACKUP_SMS_FILE_NAME);

        String targetPath = context.getCacheDir().getAbsolutePath();
        final DownloadElement downloadElement = new DownloadElement(file, targetPath);
        downloadElement.setCheck(false);
        final DownloadFileTask downloadFileTask = new DownloadFileTask(downloadElement, new OnTransferFileListener<DownloadElement>() {
            @Override
            public void onStart(String url, DownloadElement element) {
                if (null != mListener) {
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, 0);
                }
            }

            @Override
            public void onTransmission(String url, DownloadElement element) {
                if (null != mListener) {
                    int progress = (int) (((float) element.getLength() / (float) element.getSize()) * 100);
                    mListener.onBackup(TYPE, BackupInfoStep.DOWNLOAD, progress);
                }
            }

            @Override
            public void onComplete(String url, DownloadElement element) {
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
    private boolean importSMS() {
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "----Start Import SMS----");

        List<SmsItem> smsItems;
        /** import SMS to phone */
        ContentResolver conResolver = context.getContentResolver();
        /**
         * 放一个解析xml文件的模块
         */
        smsItems = this.getSmsItemsFromXml();
        if (smsItems == null || smsItems.size() == 0) {
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "---- SMS List is NULL ----");
            return false;
        }
        int total = smsItems.size();
        int write = 0;
        Logger.p(Logger.Level.INFO, IS_LOG, TAG, "---- Start to import SMS ----");

        for (SmsItem item : smsItems) {
            write++;
            Logger.p(Logger.Level.INFO, IS_LOG, TAG, "----Total SMS = " + total + " ; Import SMS : " + write);

            // 判断短信数据库中是否已包含该条短信，如果有，则不需要恢复
            try {
                Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "SMS Date: " + item.getDate());
                Cursor cursor = conResolver.query(Uri.parse("content://sms"),
                        new String[]{SmsField.DATE}, SmsField.DATE + "=?",
                        new String[]{item.getDate()}, null);
                if (cursor != null) {
                    if (!cursor.moveToFirst()) {// 没有该条短信
                        ContentValues values = new ContentValues();
                        values.put(SmsField.ADDRESS, item.getAddress());
                        // 如果是空字符串说明原来的值是null，所以这里还原为null存入数据库
                        values.put(SmsField.PERSON,
                                Objects.equals(item.getPerson(), "") ? null : item.getPerson());
                        values.put(SmsField.DATE, item.getDate());
                        values.put(SmsField.PROTOCOL,
                                Objects.equals(item.getProtocol(), "") ? null : item.getProtocol());
                        values.put(SmsField.READ, item.getRead());
                        values.put(SmsField.STATUS, item.getStatus());
                        values.put(SmsField.TYPE, item.getType());
                        values.put(SmsField.REPLY_PATH_PRESENT, Objects.equals(item.getReply_path_present(), "") ? null : item.getReply_path_present());
                        values.put(SmsField.BODY, item.getBody());
                        values.put(SmsField.LOCKED, item.getLocked());
                        values.put(SmsField.ERROR_CODE, item.getError_code());
                        values.put(SmsField.SEEN, item.getSeen());
                        conResolver.insert(Uri.parse("content://sms"), values);
                    } else {
                        Logger.p(Logger.Level.INFO, IS_LOG, TAG, "---- Skip import ");
                    }
                    cursor.close();
                }
                setProgress(write, total);
            } catch (Exception e) {
                Logger.p(Logger.Level.ERROR, IS_LOG, TAG, e, "Import Sms Exception");
            }

            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "----Total SMS = " + total + " ; Import SMS : " + write);
        }

        return true;
    }

    /**
     * parse xml file of SMS
     */
    private List<SmsItem> getSmsItemsFromXml() {
        List<SmsItem> mSmsList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        String absolutePath = context.getCacheDir().getAbsolutePath() + File.separator + AppConstants.BACKUP_SMS_FILE_NAME;
        File file = new File(absolutePath);
        if (!file.exists()) {
            exception = BackupInfoException.NO_RECOVERY;
            return null;
        }
        try {
            FileInputStream inputStream = new FileInputStream(file);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream, "UTF-8");
            Element rootElement = document.getDocumentElement();
            NodeList nodeList = rootElement.getElementsByTagName("item");
            //Logger.LOGD(TAG, "====NodeList size: " + nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element childElement = (Element) nodeList.item(i);
                NamedNodeMap mChildMap = childElement.getAttributes();

                //Logger.LOGD(TAG, "====Child NodeList size: " + mChildMap.getLength());
                SmsItem smsItem = new SmsItem();
                for (int j = 0; j < mChildMap.getLength(); j++) {
                    Attr itemAttr = (Attr) mChildMap.item(j);
                    String nodeName = itemAttr.getNodeName().trim();
                    String nodeValue = itemAttr.getNodeValue();

                    if (Objects.equals(nodeName, SmsField.ADDRESS)) {
                        smsItem.setAddress(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.PERSON)) {
                        smsItem.setPerson(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.DATE)) {
                        smsItem.setDate(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.PROTOCOL)) {
                        smsItem.setProtocol(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.READ)) {
                        smsItem.setRead(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.STATUS)) {
                        smsItem.setStatus(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.TYPE)) {
                        smsItem.setType(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.REPLY_PATH_PRESENT)) {
                        smsItem.setReply_path_present(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.BODY)) {
                        smsItem.setBody(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.LOCKED)) {
                        smsItem.setLocked(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.ERROR_CODE)) {
                        smsItem.setError_code(nodeValue);
                    } else if (Objects.equals(nodeName, SmsField.SEEN)) {
                        smsItem.setSeen(nodeValue);
                    }
                }

                if (smsItem != null) {
                    mSmsList.add(smsItem);
                }
            }
        } catch (Exception e) {
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG, e, "Parse Sms Xml");
            exception = BackupInfoException.ERROR_IMPORT;
            return null;
        }

        return mSmsList;
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