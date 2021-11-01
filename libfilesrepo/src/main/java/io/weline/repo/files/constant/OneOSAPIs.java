package io.weline.repo.files.constant;


import io.weline.repo.files.data.LoginSession;
import io.weline.repo.files.data.OneOSFile;
import io.weline.repo.files.data.SharePathType;

/**
 * Created by admin on 2020/8/1,13:54
 */
public class OneOSAPIs {
    private static final String TAG = OneOSAPIs.class.getSimpleName();
    public static final int OneOS_UPLOAD_SOCKET_PORT = 7777;
    public static final int ONE_APIS_DEFAULT_PORT = 80;
    public static final String ONE_OS_PRIVATE_ROOT_DIR = "/";
    public static final String ONE_OS_PUBLIC_ROOT_DIR = "public/";
    public static final String ONE_OS_RECYCLE_ROOT_DIR = "/.recycle/";

    public static final String ONE_API_DEFAULT_PORT = "80";
    public static final String PREFIX_HTTP = "http://";
    public static final String SCHME_HTTP = "http";
    public static final String ONE_API = "/oneapi";
    public static final String SUFFIX_TOKEN = "token";

    public static final String USER = ONE_API + "/user";

    public static final String NET_GET_MAC = ONE_API + "/net";

    public static final String FILE_API = ONE_API + "/file";
    public static final String FILE_UPLOAD = ONE_API + "/file/upload";
    public static final String FILE_DOWNLOAD_SUFFIX = "/file/download";
    public static final String FILE_DOWNLOAD = ONE_API + FILE_DOWNLOAD_SUFFIX;
    public static final String FILE_THUMBNAIL_SUFFIX = "/file/thumbnail";
    public static final String FILE_THUMBNAIL = ONE_API + FILE_THUMBNAIL_SUFFIX;

    public static final String SHARE_API = ONE_API + "/share";
    public static final String SHARE_DOWNLOAD = ONE_API + "/share/download";

    public static final String SYSTEM_SYS = ONE_API + "/sys";
    public static final String SYSTEM_STAT = ONE_API + "/stat";
    public static final String SYSTEM_SSUDP_CID = ONE_API + "/sys/ssudpcid";

    public static final String APP_API = ONE_API + "/app";
    public static final String BD_SUB = ONE_API + "/event/sub";
    public static final String EVENT_PUB = ONE_API + "/event/pub";
    public static final String GET_VERSION = "/ver.json";

    public static String genOpenUrl(LoginSession loginSession, OneOSFile file) {
        // http://192.168.1.17/home/admin/test.mp4?session=c5i6qqbe78oj0c1h78o0====
        //  String path = android.net.Uri.encodeSHA_256(file.getAbsolutePath(loginSession.getUserInfo().getName()));
        //return loginSession.getUrl() + "/" + path + "?session=" + loginSession.getSession();
        return genOpenUrl(loginSession, file.getPath());
    }

    public static String genOpenUrl(LoginSession loginSession, String path) {
        // http://192.168.1.17/home/admin/test.mp4?session=c5i6qqbe78oj0c1h78o0====
        //  String path = android.net.Uri.encodeSHA_256(file.getAbsolutePath(loginSession.getUserInfo().getName()));
        //return loginSession.getUrl() + "/" + path + "?session=" + loginSession.getSession();
        return genDownloadUrl(loginSession, path);
    }

    public static String genDownloadUrl(LoginSession loginSession, String filePath) {
        // "http://192.168.1.17/oneapi/file/download?path=home%2Fadmin%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        //String path = android.net.Uri.encodeSHA_256(file.getAbsolutePath(loginSession.getUserInfo().getName()));

        // http://ip/oneapi/file/download?session=xxx&path=/test.png
        String path = android.net.Uri.encode(filePath);
        //return loginSession.getUrl() + OneOSAPIs.FILE_API + "/download?session=" + loginSession.getSession() + "&path=" + path;
        if (loginSession.isV5()) {
            return String.format("http://%s:9898/file/download?session=%s&share_path_type=2&path=%s",
                    loginSession.getAddress(),
                    loginSession.getSession(),
                    path);
        }
        return baseUrl(loginSession.getAddress()) + OneOSAPIs.FILE_DOWNLOAD + "?session=" + loginSession.getSession() + "&path=" + path;
    }

    public static String genDownloadUrl(LoginSession loginSession, String filePath, SharePathType sharePathType) {
        // "http://192.168.1.17/oneapi/file/download?path=home%2Fadmin%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        //String path = android.net.Uri.encodeSHA_256(file.getAbsolutePath(loginSession.getUserInfo().getName()));

        // http://ip/oneapi/file/download?session=xxx&path=/test.png
        String path = android.net.Uri.encode(filePath);
        //return loginSession.getUrl() + OneOSAPIs.FILE_API + "/download?session=" + loginSession.getSession() + "&path=" + path;
        if (loginSession.isV5()) {
            return String.format("http://%s:9898/file/download?session=%s&share_path_type=%s&path=%s",
                    loginSession.getAddress(),
                    loginSession.getSession(),
                    sharePathType.getType(),
                    path);
        }
        return baseUrl(loginSession.getAddress()) + OneOSAPIs.FILE_DOWNLOAD + "?session=" + loginSession.getSession() + "&path=" + path;
    }

    public static String genThumbnailUrl(LoginSession loginSession, OneOSFile file) {
        // "http://192.168.1.17/oneapi/file/thumbnail?path=%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        return genThumbnailUrl(loginSession, file.getPath());
    }

    public static String genThumbnailUrl(LoginSession loginSession, String oneOSPath) {
        // "http://192.168.1.17/oneapi/file/thumbnail?path=%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
//            return String.format("http://%s:9898/file/thumbnail?session=%s&share_path_type=2&path=%s",
        String path = android.net.Uri.encode(oneOSPath);
        if (loginSession.isV5()) {
            return String.format("http://%s:9898/file/thumbnail?session=%s&share_path_type=2&path=%s",
                    loginSession.getAddress(),
                    loginSession.getSession(),
                    path);
        }
        return baseUrl(loginSession.getAddress()) + OneOSAPIs.FILE_THUMBNAIL + "?session=" + loginSession.getSession() + "&path=" + path;

    }

    public static String genThumbnailUrl(LoginSession loginSession, String oneOSPath, SharePathType sharePathType) {
        // "http://192.168.1.17/oneapi/file/thumbnail?path=%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
//            return String.format("http://%s:9898/file/thumbnail?session=%s&share_path_type=2&path=%s",
        String path = android.net.Uri.encode(oneOSPath);
        if (loginSession.isV5()) {
            return String.format("http://%s:9898/file/thumbnail?session=%s&share_path_type=%s&path=%s",
                    loginSession.getAddress(),
                    loginSession.getSession(),
                    sharePathType.getType(),
                    path);
        }
        return baseUrl(loginSession.getAddress()) + OneOSAPIs.FILE_THUMBNAIL + "?session=" + loginSession.getSession() + "&path=" + path;

    }


    public static String url(String address, String action) {
        return String.format("%s%s%s", PREFIX_HTTP, address, action);
    }

    public static String baseUrl(String address) {
        return String.format("%s%s:%s", PREFIX_HTTP, address, OneOSAPIs.ONE_API_DEFAULT_PORT);
    }

    public static String getSubUrl(String ip) {
        return OneOSAPIs.PREFIX_HTTP + ip + ":" + OneOSAPIs.ONE_API_DEFAULT_PORT + OneOSAPIs.BD_SUB;
    }

    public static String getShareDownloadUrl(String sourceIp, String downloadToken) {
        return OneOSAPIs.PREFIX_HTTP + sourceIp
                + OneOSAPIs.SHARE_DOWNLOAD + "?token=" + downloadToken;
    }
}
