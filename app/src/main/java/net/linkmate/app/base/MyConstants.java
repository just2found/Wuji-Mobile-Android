package net.linkmate.app.base;

import android.content.Context;

import net.linkmate.app.util.FileUtils;
import net.linkmate.app.util.NetworkUtils;
import net.sdvn.app.config.AppConfig;
import net.sdvn.common.Local;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * @author yun
 * @date 2017/9/14
 */

public interface MyConstants {
    int LOGIN_VPN_REQUEST_CODE = 8090;
    String IS_LOGINED = "isLogined";
    String MCS_LOGIN_KEEP = "mcs_login_keep";
    String DEFAULT_PATH_SAVE_CRASH = FileUtils.getCrashDir();
    String DEFAULT_EXTERNAL_FOLDER_IMG = FileUtils.getIconDir();
    String DEFAULT_FILE_CACHE_IMG = MyApplication.getContext().getCacheDir() + File.separator + "images";
    String UPDATE_URL = "https://$BASEURL$:8447/v2/updatesrv/checkupdate";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    SimpleDateFormat sdfNoYear = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    SimpleDateFormat sdfSimple = new SimpleDateFormat("HH:mm", Locale.getDefault());
    SimpleDateFormat sdfMMDD = new SimpleDateFormat("MM-dd", Locale.getDefault());

    String SP_SHOW_REMARK_NAME = "sp_show_remark_name";
    String SP_SHOW_HOME_AD = "sp_show_home_ad";
    boolean SP_SHOW_HOME_AD_DEFAULT_VALUE = true;

    String PAYPAL_LOG_PATH = "/WeLine/PayPalLog";


    /**
     * 文件传输相关
     * PROTOCOL_VERSION == 2
     * 新增预览缩略图片
     */
    //传输协议版本号
    int PROTOCOL_VERSION = 2;
    //参与一个传输任务的线程总数
    int THREAD_COUNT = 3;
    //服务器端口
    int SERVER_PORT = 6677;
    //心跳延迟
    int BEAT_PERIOD = 3000;
    //最大同时传输任务数
//    int MAX_TRANS_TASK = TransferThreadPool.MAX_POOL_SIZE / 3;

    /**
     * 文件选择相关
     */
    String MAX_NUMBER = "MaxNumber";
    String CURRENT_NUMBER = "CurrentNumber";

    int REQUEST_CODE_TAKE_IMAGE = 0x101;
    int REQUEST_CODE_BROWSER_IMAGE = 0x102;
    int REQUEST_CODE_FILE_PICK = 0x500;
    String RESULT_BROWSER_IMAGE = "ResultBrowserImage";
    String RESULT_FILE_PICK = "ResultFilePick";

    long DEFAULT_CACHE_SIZE = 100 * 1024 * 1024;

    String STATUS_DISCONNECTION = "SDVN_DISCONNECTION";

    String CONFIG_PARTID = AppConfig.CONFIG_PARTID;
    String CONFIG_APPID = AppConfig.CONFIG_APPID;
    int CONFIG_DEV_CLASS = AppConfig.CONFIG_DEV_CLASS;
//    String CONFIG_PARTID = "PXT89G3Y8MUT60KAL9HK";
//    String CONFIG_APPID = "P9JAAZG9RTGGVTUWCZV4";
//    int CONFIG_DEV_CLASS = 6553857;

    String STR_APP_KEY = "appkey";
    String STR_RANDOM = "random";

//    String CONFIG_PARTID = "1EB3ZEFXZOCAE16LVM9C";
//    String CONFIG_APPID = "AQHEVU7G83SP124BGB1C";

    int REQUEST_CODE_STORAGE = 1602;
    int DEVICE_LIST_REFRESH = 1632;
    String STATUS_APP_DESTROY = "sdvn_app_destroy";

    int DC_LINKMATE_ANDROID = 131329;
    int DC_LINKMATE_IOS = 131393;
    int DC_IZZBIE_ANDROID = 196865;
    int DC_IZZBIE_IOS = 196929;
    int REQUEST_CODE_CAMERA = 120;
    int EVENT_CODE_HARDWAER_DEVICE = 111213;
    int EVENT_CODE_NETWORK = 111212;
    int EVENT_CODE_CIRCLE_NETWORK = 111214;
    int EVENT_CODE_MY_IDENTIFY_CODE = 111215;//我的识别码
    int EVENT_CODE_DEVICE_CODE = 111216;//设备识别码
    int REQUEST_CODE_READ_PHONE_STATE = 12031;

    String NOTIFICATION_ID_VPN_DISCONNECTED = "id_vpn_disconnected";

    /**
     * 第三方支付
     */
    String WX_APPID = "wx16b2f729207f57af";//正式

    /**
     * 通知ID
     */
    int STATUS_NOTIFICATION_ID = 1908011301;
    int UPDATE_NOTIFICATION_ID = 1908011302;
    int FILE_RECV_NOTIFICATION_ID = 1908011303;

    /**
     * 通知管道
     */
    String STATUS_CHANNEL_ID = "SDVN_status_channel";
    String STATUS_CHANNEL_NAME = "SDVN connection status";
    String UPDATE_CHANNEL_ID = "SDVN_update_channel";
    String UPDATE_CHANNEL_NAME = "SDVN update info";
    String SERVICE_CHANNEL_ID = "SDVN_service_channel";
    String SERVICE_CHANNEL_NAME = "SDVN foreground service";
    String FILE_RECV_CHANNEL_ID = "SDVN_file_channel";
    String FILE_RECV_CHANNEL_NAME = "SDVN file transfer";


    /**
     * 密码至少包含 数字和英文，长度6-32
     */
    String regNumLetter = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,32}$";
    String regNumLetter6_20 = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";
//        Pattern PATTERN = Pattern.compile(regNumLetter);
    /**
     * 密码包含 数字,英文,字符中的两种以上，长度6-32
     */
    String regNumLetterChar = "^(?![0-9]+$)(?![a-z]+$)(?![A-Z]+$)(?!([^(0-9a-zA-Z)])+$).{6,32}$";

    /**
     * 至少包含数字字母跟字符，长度8-32
     */
//    String regNumLetterAndChar = "^(?=.*([a-zA-Z].*))(?=.*[0-9].*)(?=.*([*/+.,~!@#$%^&()]).*)[a-zA-Z0-9-*/+.,~!@#$%^&()]{8,32}$";
//    String regNumLetterAndChar = "^(?=(.*[a-zA-Z]){1,})(?=(.*[\\d]){1,})(?=(.*[\\W]){1,})(?!.*\\s).{8,32}$";
    String regNumLetterAndChar = "^(?=.*([a-zA-Z].*))(?=.*[0-9].*)(?=.*([*/+.,<>~!@#$%^&()_=?;:'\"`\\[\\]{}|\\\\-]).*)[a-zA-Z0-9-*/+.,<>~!@#$%^&()_=?;:'\"`\\[\\]{}|\\\\-]{8,32}$";
    //    test regex
//    Pattern PATTERN = Pattern.compile(regNumLetterAndChar);
    String REGEX_EMAIL = "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";
    String REMOTE_MANAGER_VERSION = "3.0.0.2505";


    String PRIVACY_URL = "file:///android_asset/privacy.html";
    String PRIVACY_URL_EN = "file:///android_asset/privacy_en.html";
    String PRIVACY_URL_TW = "file:///android_asset/privacy_tw.html";
    String PRIVACY_ONLINE_URL = "http://www.weline.io/privacy.html";
    String PRIVACY_ONLINE_URL_EN = "http://www.weline.io/privacy_en.html";
    String PRIVACY_ONLINE_URL_TW = "http://www.weline.io/privacy_tw.html";
    String USER_AGREEMENT_URL = "file:///android_asset/user_agreement.html";
    String USER_AGREEMENT_URL_EN = "file:///android_asset/user_agreement_en.html";
    String USER_AGREEMENT_URL_TW = "file:///android_asset/user_agreement_tw.html";
    String USER_AGREEMENT_ONLINE_URL = "http://www.weline.io/user_agreement.html";
    String USER_AGREEMENT_ONLINE_URL_EN = "http://www.weline.io/user_agreement_en.html";
    String USER_AGREEMENT_ONLINE_URL_TW = "http://www.weline.io/user_agreement_tw.html";

    String DEFAULT_UNIT = "GB";
    @NotNull String INTERNET_URL = "[a-zA-z]+://[^\\s]*";
    @NotNull String REGEX_MASK = "^(254|252|248|240|224|192|128|0)\\.0\\.0\\.0|255\\.(254|252|248|240|224|192|128|0)\\.0\\.0|255\\.255\\.(254|252|248|240|224|192|128|0)\\.0|255\\.255\\.255\\.(255|254|252|248|240|224|192|128|0)$";
    float COVER_W_H_PERCENT = 0.75f;

    static String getPrivacyUrlByLanguage(Context ctx) {
        String url;
//        Locale curLocale = ctx.getResources().getConfiguration().locale;
//        String script = curLocale.getScript();
//        String language = curLocale.getLanguage();
//        String country = curLocale.getCountry();//"CN""TW"
//        boolean isHans = "cn".equals(country.toLowerCase())
//                || "hans".equals(script.toLowerCase());
        if (NetworkUtils.checkNetwork(ctx)) {
            if (Local.isHans()) {
                url = MyConstants.PRIVACY_ONLINE_URL;
            } else if (Local.isHant()) {
                url = MyConstants.PRIVACY_ONLINE_URL_TW;
            } else {
                url = MyConstants.PRIVACY_ONLINE_URL_EN;
            }
        } else {
            if (Local.isHans()) {
                url = MyConstants.PRIVACY_URL;
            } else if (Local.isHant()) {
                url = MyConstants.PRIVACY_URL_TW;
            } else {
                url = MyConstants.PRIVACY_URL_EN;
            }
        }
        return url;
    }

    static String getAgreementUrlByLanguage(Context ctx) {
        String url;
//        Locale curLocale = ctx.getResources().getConfiguration().locale;
//        String script = curLocale.getScript();
//        String language = curLocale.getLanguage();
//        String country = curLocale.getCountry();//"CN""TW"
//        boolean isHans = "cn".equals(country.toLowerCase())
//                || "hans".equals(script.toLowerCase());
        if (NetworkUtils.checkNetwork(ctx)) {
            if (Local.isHans()) {
                url = MyConstants.USER_AGREEMENT_ONLINE_URL;
            } else if (Local.isHant()) {
                url = MyConstants.USER_AGREEMENT_ONLINE_URL_TW;
            } else {
                url = MyConstants.USER_AGREEMENT_ONLINE_URL_EN;
            }
        } else {
            if (Local.isHans()) {
                url = MyConstants.USER_AGREEMENT_URL;
            } else if (Local.isHant()) {
                url = MyConstants.USER_AGREEMENT_URL_TW;
            } else {
                url = MyConstants.USER_AGREEMENT_URL_EN;
            }
        }
        return url;
    }

    /**
     * 中文 数字 字母  加 下划线 @
     *
     * @param input
     * @return
     */
    static boolean regExInput(String input) {
        String regEx = "^[\\u4e00-\\u9fa5A-Za-z0-9_@]*$";
        return input.matches(regEx);
    }


    /**
     * 数字 字母
     *
     * @param input
     * @return
     */
    static boolean regExInputLoginName(String input) {
        String regEx1 = "^[A-Za-z]*$";
        String regEx2 = "^[0-9]*$";
        String regEx3 = "^[A-Za-z0-9]{8,24}$";
        return !input.matches(regEx1) && !input.matches(regEx2) && input.matches(regEx3);
    }
}
