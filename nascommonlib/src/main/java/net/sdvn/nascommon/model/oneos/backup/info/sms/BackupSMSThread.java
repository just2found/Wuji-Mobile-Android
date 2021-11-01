package net.sdvn.nascommon.model.oneos.backup.info.sms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/26.
 */
public class BackupSMSThread implements Runnable {
    private static final String TAG = BackupSMSThread.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_SMS;

    private static final BackupInfoType TYPE = BackupInfoType.BACKUP_SMS;

    private Context context;
    @Nullable
    private OnBackupInfoListener mListener = null;
    @Nullable
    private BackupInfoException exception = null;
    //    private LoginSession loginSession = null;
    private String deviceId;

    public BackupSMSThread(String deviceId, @Nullable OnBackupInfoListener mListener) {
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
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Start Backup SMS");
        if (null != mListener) {
            mListener.onStart(TYPE);
        }

        if (exportSMS()) {
            uploadSMS();
        }

        if (null == exception) {
            long time = System.currentTimeMillis();
            Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Backup SMS Success, Update database: " + time);
            BackupInfoKeeper.update(deviceId, BackupInfoType.BACKUP_CONTACTS, time);
        }

        if (mListener != null) {
            mListener.onComplete(TYPE, exception);
        }

        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Complete Backup SMS");
    }

    public void setOnBackupInfoListener(OnBackupInfoListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Upload SMS file to server
     */
    private void uploadSMS() {
        File file = new File(context.getCacheDir().getAbsolutePath(), AppConstants.BACKUP_SMS_FILE_NAME);
        String path = AppConstants.BACKUP_INFO_ONEOS_ROOT_DIR;

        final UploadElement element = new UploadElement(file, path);
//        element.saveUid(LoginManage.getInstance().getLoginSession().getUserInfo().getId());
//        element.setFile(file);
//        element.setToPath(path);
        element.setOverwrite(true);
        element.setToDevId(deviceId);
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
     * Exporting SMS from the phone
     */
    public boolean exportSMS() {
        boolean result;
        String SMS_URI_ALL = "content://sms/";
        File file = new File(context.getCacheDir().getAbsolutePath(), AppConstants.BACKUP_SMS_FILE_NAME);
        DocumentBuilderFactory mFactory;
        Document document = null;

        Cursor cursor = null;
        try {
            mFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = mFactory.newDocumentBuilder();
            document = builder.newDocument();

            Element element = document.createElement("sms");
            document.appendChild(element);

            ContentResolver conResolver = context.getContentResolver();
            String[] projection = new String[]{SmsField.ADDRESS, SmsField.PERSON,
                    SmsField.DATE, SmsField.PROTOCOL, SmsField.READ, SmsField.STATUS,
                    SmsField.TYPE, SmsField.REPLY_PATH_PRESENT, SmsField.BODY, SmsField.LOCKED,
                    SmsField.ERROR_CODE, SmsField.SEEN};
            // type=1是收件箱，==2是发件箱;read=0表示未读，read=1表示读过，seen=0表示未读，seen=1表示读过
            Uri uri = Uri.parse(SMS_URI_ALL);
            cursor = conResolver.query(uri, projection, null, null, "_id asc");
            int total = cursor.getCount();
            int write = 0;
            setExportProgress(total, write);
            if (cursor.moveToFirst()) {
                // 查看数据库sms表得知 subject和service_center始终是null所以这里就不获取它们的数据了。
                String address;
                String person;
                String date;
                String protocol;
                String read;
                String status;
                String type;
                String reply_path_present;
                String body;
                String locked;
                String error_code;
                String seen;
                do {
                    write++;
                    // 如果address == null，xml文件中是不会生成该属性的,为了保证解析时，属性能够根据索引一一对应，必须要保证所有的item标记的属性数量和顺序是一致的
                    address = cursor.getString(cursor.getColumnIndex(SmsField.ADDRESS));
                    if (address == null) {
                        address = "";
                    }
                    person = cursor.getString(cursor.getColumnIndex(SmsField.PERSON));
                    if (person == null) {
                        person = "";
                    }
                    date = cursor.getString(cursor.getColumnIndex(SmsField.DATE));
                    if (date == null) {
                        date = "";
                    }
                    protocol = cursor.getString(cursor.getColumnIndex(SmsField.PROTOCOL));
                    if (protocol == null) {// 为了便于xml解析
                        protocol = "";
                    }
                    read = cursor.getString(cursor.getColumnIndex(SmsField.READ));
                    if (read == null) {
                        read = "";
                    }
                    status = cursor.getString(cursor.getColumnIndex(SmsField.STATUS));
                    if (status == null) {
                        status = "";
                    }
                    type = cursor.getString(cursor.getColumnIndex(SmsField.TYPE));
                    if (type == null) {
                        type = "";
                    }
                    reply_path_present = cursor.getString(cursor.getColumnIndex(SmsField.REPLY_PATH_PRESENT));
                    if (reply_path_present == null) {// 为了便于XML解析
                        reply_path_present = "";
                    }
                    body = cursor.getString(cursor.getColumnIndex(SmsField.BODY));
                    if (body == null) {
                        body = "";
                    }
                    locked = cursor.getString(cursor.getColumnIndex(SmsField.LOCKED));
                    if (locked == null) {
                        locked = "";
                    }
                    error_code = cursor.getString(cursor.getColumnIndex(SmsField.ERROR_CODE));
                    if (error_code == null) {
                        error_code = "";
                    }
                    seen = cursor.getString(cursor.getColumnIndex(SmsField.SEEN));
                    if (seen == null) {
                        seen = "";
                    }
                    // 生成xml子标记, 开始标记
                    Element childElement = document.createElement("item");
                    childElement.setAttributeNS(null, SmsField.ADDRESS, address);
                    childElement.setAttributeNS(null, SmsField.ADDRESS, address);
                    childElement.setAttributeNS(null, SmsField.PERSON, person);
                    childElement.setAttributeNS(null, SmsField.DATE, date);
                    childElement.setAttributeNS(null, SmsField.PROTOCOL, protocol);
                    childElement.setAttributeNS(null, SmsField.READ, read);
                    childElement.setAttributeNS(null, SmsField.STATUS, status);
                    childElement.setAttributeNS(null, SmsField.TYPE, type);
                    childElement.setAttributeNS(null, SmsField.REPLY_PATH_PRESENT,
                            reply_path_present);
                    try {
                        childElement.setAttributeNS(null, SmsField.BODY, body);
                    } catch (Exception e) {
                        Logger.p(Logger.Level.ERROR, IS_LOG, TAG, "Error: ADDRESS: " + address + "; BODY: " + body + "; DATE: " + date);
                    }
                    childElement.setAttributeNS(null, SmsField.LOCKED, locked);
                    childElement.setAttributeNS(null, SmsField.ERROR_CODE, error_code);
                    childElement.setAttributeNS(null, SmsField.SEEN, seen);
                    element.appendChild(childElement);

                    setExportProgress(total, write);
                } while (cursor.moveToNext());
                result = true;
            } else {
                exception = BackupInfoException.NO_BACKUP;
                result = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Logger.p(Logger.Level.ERROR, IS_LOG, TAG,e, "Error Export SMS");
            exception = BackupInfoException.ERROR_EXPORT;
            result = false;
        } finally {
            if (cursor != null) {
                cursor.close();// 手动关闭cursor，及时回收
            }

            if (document != null) {
                try {
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer transformer = tf.newTransformer();
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                    // 转换器类将xml源文件向一个目的路径保存转换后的结果，这里是一个xml文件
                    DOMSource source = new DOMSource(document);
                    StreamResult sResult = new StreamResult(file);
                    transformer.transform(source, sResult);

                } catch (TransformerException e) {
                    e.printStackTrace();
                    Logger.p(Logger.Level.ERROR, IS_LOG, TAG,e,"TransformerException");
                }
            }

        }
        Logger.p(Logger.Level.DEBUG, IS_LOG, TAG, "Export SMS Over");

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
