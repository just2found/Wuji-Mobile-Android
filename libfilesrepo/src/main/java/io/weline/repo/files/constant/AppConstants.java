package io.weline.repo.files.constant;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.weline.repo.files.BuildConfig;


/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class AppConstants {
    //---------------------- time --------------------------//
    public static final int DELAY_TIME_AUTO_REFRESH = 350;
    public static final long SHARE_FILE_REFRESH_PERIOD = 2 * 60 * 1000;
    public static final long DEVICE_SHARE_FILE_REFRESH_PERIOD = 1200;
    public final static int SESSION_LIVE_TIME = 110 * 60 * 1000;
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    //------------------- count -------------------------//
    public static final long DEFAULT_CACHE_SIZE = 100 * 1024 * 1024;
    public static final int MAX_BACKUP_FILE_COUNT = 5;

    //-------------------------- path -----------------------------//
    public static final String BACKUP_FILE_ONEOS_ROOT_DIR_NAME = "/From：" + Build.BRAND + "-" + Build.MODEL + "/";
    public static final String BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM = BACKUP_FILE_ONEOS_ROOT_DIR_NAME + "Album";
    public static final String BACKUP_FILE_ONEOS_ROOT_DIR_NAME_FILES = BACKUP_FILE_ONEOS_ROOT_DIR_NAME + "Files";

    public static final String BACKUP_INFO_ONEOS_ROOT_DIR = "/";
    public static final String BACKUP_CONTACTS_FILE_NAME = ".contactsfromandroid.vcf";
    public static final String BACKUP_SMS_FILE_NAME = ".messagefromandroid.xml";

    private static final String DEFAULT_EXTERNAL_FOLDER_IMG = "%s" + File.separator + "images";
    public static final int HS_ANDROID_TV_PORT = 9898;
    public static final int HS_DYNAMIC_PORT = 8000;
//    public static final int PRIVATE_SHARE_PATH_TYPE = 0;//个人目录
//    public static final int PUBLIC_SHARE_PATH_TYPE = 2;//公共目录
//
    public static String getDefaultExternalFolderImg(@NonNull Context context) {
        return String.format(DEFAULT_EXTERNAL_FOLDER_IMG, context.getCacheDir().getAbsolutePath());
    }

    public static final String DEFAULT_APP_ROOT_DIR_NAME = "/WeLine";
    public static final String DEFAULT_DOWNLOAD_DIR_NAME = "/WeLine/Download";

    public static final String REGEX_UPLOAD_FILE = "^(\\S*.upload)|(\\S*.upload[A-Za-z0-9]{32})|(\\S*.tmpdata)";

    public static final String ROOT_DIR = "Android/data/";
    private static final String DEFAULT_PATH_SAVE_CRASH =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + ROOT_DIR + "%s"
                    + File.separator + "crash" + File.separator;

    public static String getDefaultPathSaveCrash(@NonNull Context context) {
        return String.format(DEFAULT_PATH_SAVE_CRASH, context.getPackageName());
    }

    public static final String SHARE_DOWNLOADS_PATH = "/ShareDownloads";

    //------------ placeholder-------------
    public static final String TMP = ".cdr";
    public static final String PHOTO_DATE_UNKNOWN = "Unknown";
    public static final String STR_RANDOM = "random";
    public static final String DEFAULT_USERNAME_ADMIN = "admin";
    public static final String DEFAULT_DEV_NAME_PREFIX = "SN-";
    public static final String DEFAULT_USERNAME_PWD = "123456";
    //----------------------db ------------------//
    public static final String OBJECT_BOX_NAS = "OB_NAS";
    public static final int DISK_LRU_CACHE_APP_VERSION = 1;


    public static final boolean DISPLAY_IMAGE_WITH_GLIDE = true;
    //-------------------------domain device------------------------------//
    public static final int DOMAIN_DEVICE_LAN = 0;
    public static final int DOMAIN_DEVICE_WAN = 1;
    public static final int DOMAIN_DEVICE_SSUDP = 2;
    public static final int DOMAIN_DEVICE_VIP = 3;

    //----------------------config---------------------//
//    public static final String CONFIG_PARTID = AppConfig.PARTNERID;
//    public static final String CONFIG_APPID = AppConfig.APPID;
//    public static final int CONFIG_DEV_CLASS = AppConfig.DEV_CLASS;


    //    --------------------- sp _field----------------------------//
    public static final String SP_FIELD_PWD = "devsert";
    public static final String SP_FIELD_DEVICE_ID = "deviceid";
    public static final String SP_FIELD_DEVICE_NAME = "deviceName";
    public static final String SP_FIELD_DEVICE_SN = "sn";
    public static final String SP_FIELD_DEVICE_IP = "ip";
    public static final String SP_FIELD_NETWORK = "network";
    public static final String SP_FIELD_DEVICE_BASEURL = "baseUrl";
    public static final String SP_FIELD_DEVICE_DOMAINS = "domains";
    public static final String SP_FIELD_DEVICE_SESSION = "session";
    public static final String SP_FIELD_DEVICE_IS_ADMIN = "isAdmin";
    public static final String SP_FIELD_DEVICE_PATH = "device_path";
    @NotNull
    public static final String SP_FIELD_FILE_TYPE = "sp_field_file_type";

    public static final String SP_FIELD_IS_LOGINED = "isLogined";
    public static final String SP_FIELD_USERNAME = "username";
    public static final String SP_FIELD_USER_ID = "user_id";
    public static final String SP_FIELD_ONLY_WIFI_CARE = "isTipTransferNotWifi";
    public static final String SP_FIELD_PIC_ONLY_WIFI_CARE = "isPreviewPicOnlyWifi";
    public static final String SP_FIELD_BAK_ONLY_WIFI_CARE = "isBackupFileOnlyWifi";
    public static final String SP_FIELD_AUTO_BAK_FILE_CARE = "isAutoBackupFile";
    public static final String SP_FIELD_AUTO_BAK_ALBUM_CARE = "isAutoBackupAlbum";
    public static final String SP_FIELD_BAK_ALBUM_ONLY_WIFI_CARE = "isBackupAlbumOnlyWifi";

    public static final String SP_FIELD_BAK_ALBUM_LAST_DEV_ID = "bak_album_last_dev_id";
    public static final String SP_FIELD_BAK_INFO_SMS_LAST_DEV_ID = "bak_info_sms_last_dev_id";
    public static final String SP_FIELD_BAK_INFO_CONTACT_LAST_DEV_ID = "bak_info_contact_last_dev_id";


    public static final String SP_FIELD_DEFAULT_LOCAL_DOWNLOAD_PATH = "sp_field_default_local_download_path";

    public static final String SP_FIELD_CHOICE_BACKUP_ALBUM_PATHS = "sp_field_choice_backup_album_paths";


    //-------------------- local broadcast----------------------//
    public static final String LOCAL_BROADCAST_REMOVE_DEV = "local_broadcast_remove_dev";
    public static final String LOCAL_BROADCAST_RELOGIN = "local_broadcast_relogin";
    public static final String NOTIFY_UPDATE_LISTVIEW = "com.eli.app.activity.update_listview";
    public static final String TRANSMISSION_RECEIVE_NEW_FILE = "local_broadcast_transmission_receive_new_file";
    public static final String TRANSMISSION_RECEIVE_FILE_READ = "local_broadcast_transmission_receive_file_read";
    public static final String DEV_TO_DEV_FILE_COPY = "local_broadcast_dev_to_dev_file_copy";

    //-------------------request code ---------------------------//
    public static final int LOGIN_VPN_REQUEST_CODE = 8090;
    public static final int REQUEST_CODE_HD_FORMAT = 8898;
    public static final int REQUEST_CODE_SELECT_BACKUP_ALBUM = 8899;


    public static final int NEW_VER_NO = 51000;

    public static final String NOTIFY_DEVICE_READY_GO_FINDER = "notify_device_ready_go_finder";
    public static final String MSG_SUCC = "Success";

    public static final Integer CODE_SUCC = 0;

    public static final String UPDATE_CHANNEL_ID = "update_channel_id";
    public static final String UPDATE_CHANNEL_NAME = "update_channel_name";
    public static final int UPDATE_NOTIFICATION_ID = 17778888;
    public static final int PAGE_SIZE = BuildConfig.DEBUG ? 50 : 100;

    public static final long M8_SYSTEM_SPACE = 159843876864L;
}
