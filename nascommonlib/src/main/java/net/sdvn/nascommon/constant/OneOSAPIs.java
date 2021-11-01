package net.sdvn.nascommon.constant;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.fileserver.constants.SharePathType;
import net.sdvn.nascommon.model.PathTypeCompat;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.EmptyUtils;

import org.jetbrains.annotations.NotNull;

import io.weline.repo.SessionCache;
import io.weline.repo.data.model.IconSize;
import io.weline.repo.files.constant.AppConstants;
import okhttp3.HttpUrl;

/**
 * OneSpace OS 4.x API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class OneOSAPIs {
    public static final String ONE_OS_USB_ROOT_DIR = "/";
    private static final String TAG = OneOSAPIs.class.getSimpleName();
    public static final int OneOS_UPLOAD_SOCKET_PORT = 7777;
    public static final String SCHEME = "http";
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
    public static final String ONE_OS_SAFE_ROOT_DIR = "safe/";
    public static final String ONE_OS_EXT_STORAGE_ROOT_DIR = "ext/";
    public static final String ONE_OS_GROUP_ROOT_DIR = "group/";
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
        return genOpenUrl(loginSession, file, null);
    }

    public static String genOpenUrl(LoginSession loginSession, OneOSFile file, @Nullable Long groupId) {
        // http://192.168.1.17/home/admin/test.mp4?session=c5i6qqbe78oj0c1h78o0====
        //  String path = android.net.Uri.encodeSHA_256(file.getAbsolutePath(loginSession.getUserInfo().getName()));
        //return loginSession.getUrl() + "/" + path + "?session=" + loginSession.getSession();
        if (file.getShare_path_type() != -1) {
            return genDownloadUrl(loginSession, file.getShare_path_type(), file.getPath(), groupId);
        }
        return genOpenUrl(loginSession, file.getAllPath());
    }

    public static String genOpenUrl(LoginSession loginSession, String path) {
        // http://192.168.1.17/home/admin/test.mp4?session=c5i6qqbe78oj0c1h78o0====
        //  String path = android.net.Uri.encodeSHA_256(file.getAbsolutePath(loginSession.getUserInfo().getName()));
        //return loginSession.getUrl() + "/" + path + "?session=" + loginSession.getSession();
        return genDownloadUrl(loginSession, path);
    }

    public static String genDownloadUrl(LoginSession loginSession, String filePath) {
        boolean isPublicFile = filePath.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR);
        // "http://192.168.1.17/oneapi/file/download?path=home%2Fadmin%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        //String path = android.net.Uri.encodeSHA_256(file.getAbsolutePath(loginSession.getUserInfo().getName()));

        // http://ip/oneapi/file/download?session=xxx&path=/test.png
        //return loginSession.getUrl() + OneOSAPIs.FILE_API + "/download?session=" + loginSession.getSession() + "&path=" + path;
        //调用的方法都需要同步返回，这里直接同步请求，理论上，能获取缓存直接判断是否为V5了
        boolean isV5 = SessionCache.Companion.getInstance().isV5(loginSession.getId());
        if (isV5 || loginSession.isV5()) {


//            path = android.net.Uri.encode(path);
//            DataSessionUser sessionUser = SessionCache.Companion.getInstance().get(loginSession.getId());
//            if (sessionUser == null) {
//                SessionCache.Companion.getInstance().getOrAsynRequest(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), null);
//                sessionUser = new DataSessionUser("", new UserInfo());
//            }
            //android tv nas 1.0
            if (!isV5 && loginSession.isV5()) {
                isPublicFile = true;
            }
//            while (sessionUser == null) {//等待请求，理论上在SessionManager.getLoginSession会先请求好数据
//                try {
//                    Thread.sleep(200);
//                    sessionUser = SessionCache.Companion.getInstance().get(loginSession.getId());
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            if (isPublicFile) {
                String path = getV5Path(filePath);
                return genDownloadUrl(loginSession, SharePathType.PUBLIC.getType(), path);
            } else {
                return genDownloadUrlV5(loginSession, filePath);
            }
        } else {
            String path = android.net.Uri.encode(filePath);
            return url(loginSession.getIp(), OneOSAPIs.FILE_DOWNLOAD) + "?session=" + loginSession.getSession() + "&path=" + path;
        }
    }

    public static String genThumbnailUrl(LoginSession loginSession, OneOSFile file) {
        // "http://192.168.1.17/oneapi/file/thumbnail?path=%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
        if (file.getShare_path_type() != -1) {
            return genThumbnailUrl(loginSession, file.getShare_path_type(), file.getPath(), null);
        }
        return genThumbnailUrl(loginSession, file.getAllPath());
    }

    public static String genThumbnailUrl(LoginSession loginSession, String oneOSPath) {
        return genThumbnailUrl(loginSession, oneOSPath, null);
    }


    public static String genThumbnailUrl(LoginSession loginSession, String oneOSPath, IconSize iconSize) {
        boolean isPublicFile = oneOSPath.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR);
        // "http://192.168.1.17/oneapi/file/thumbnail?path=%2Fzxt01%2Fxxxxxxxxxxxx.JPG&session=c5i6qqbe78oj0c1h78o0====";
//            return String.format("http://%s:9898/file/thumbnail?session=%s&share_path_type=2&path=%s",
        String id = loginSession.getId();
        boolean isV5 = id != null && SessionCache.Companion.getInstance().isV5(id);
        if (isV5 || loginSession.isV5()) {
            //android tv nas 1.0
            if (!isV5 && loginSession.isV5()) {
                isPublicFile = true;
            }
//            path = android.net.Uri.encode(path);
//            DataSessionUser sessionUser = SessionCache.Companion.getInstance().get(id);
//            if (sessionUser == null) {
//                SessionCache.Companion.getInstance().getOrAsynRequest(id, loginSession.getIp(), LoginTokenUtil.getToken(), null);
//                sessionUser = new DataSessionUser("", new UserInfo());
//            }
//            while (sessionUser == null) {//等待请求，理论上在SessionManager.getLoginSession会先请求好数据
//                try {
//                    Thread.sleep(200);
//                    sessionUser = SessionCache.Companion.getInstance().get(id);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            if (isPublicFile) {
                String path = getV5Path(oneOSPath);
                return genThumbnailUrlV5(SharePathType.PUBLIC.getType(), loginSession, path);
            } else {
                return genThumbnailUrlV5(loginSession, oneOSPath);
            }
        } else {
            String path = android.net.Uri.encode(oneOSPath);
            return url(loginSession.getIp(), OneOSAPIs.FILE_THUMBNAIL) + "?session=" + loginSession.getSession() + "&path=" + path;
        }
    }


    //这个只有后面V5设备才能调用
    public static String genDownloadUrlV5(Integer shareType, LoginSession loginSession, String filePath) {
        return genDownloadUrl(loginSession, shareType, getV5Path(filePath));
    }

    //这个只有后面V5设备才能调用
    public static String genDownloadUrlV5(LoginSession loginSession, String filePath) {
        int share_path_type = getSharePathType(filePath);
        String path = getV5Path(filePath);
        return genDownloadUrl(loginSession, share_path_type, path);
    }

    public static int getSharePathType(@NonNull String filePath) {
        return PathTypeCompat.getSharePathType(filePath);
    }


    //这个只有后面V5设备才能调用
    public static String genThumbnailUrlV5(int share_path_type, LoginSession loginSession, String oneOSPath, @Nullable String size) {
        return genThumbnailUrl(loginSession, share_path_type, getV5Path(oneOSPath), size);
    }

    //这个只有后面V5设备才能调用
    public static String genThumbnailUrlV5(int share_path_type, LoginSession loginSession, String oneOSPath, Long GroupId, @Nullable String size) {
        return genThumbnailUrl(loginSession, share_path_type, getV5Path(oneOSPath), size, GroupId);
    }

    public static String genThumbnailUrlV5(int share_path_type, LoginSession loginSession, String oneOSPath) {
        return genThumbnailUrlV5(share_path_type, loginSession, oneOSPath, null);
    }


    //这个只有后面V5设备才能调用
    public static String genThumbnailUrlV5(LoginSession loginSession, String oneOSPath, @Nullable String size) {
        int share_path_type = getSharePathType(oneOSPath);
        return genThumbnailUrl(loginSession, share_path_type, getV5Path(oneOSPath), size);
    }

    //这个只有后面V5设备才能调用
    public static String genThumbnailUrlV5(LoginSession loginSession, String oneOSPath) {
        return genThumbnailUrlV5(loginSession, oneOSPath, null);
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

    @NotNull
    public static String genThumbnailUrl(@NotNull LoginSession loginSession, int pathType, @NotNull String path, @Nullable String size) {
        return genThumbnailUrl(loginSession, pathType, path, size, null);
    }


    @NotNull
    public static String genThumbnailUrl(@NotNull LoginSession loginSession, int pathType, @NotNull String path, @Nullable String size, @Nullable Long groupId) {
//        return String.format("http://%s:%s/file/thumbnail?session=%s&share_path_type=%s&size=%s&path=%s",
//                loginSession.getIp(),
//                AppConstants.HS_ANDROID_TV_PORT,
//                loginSession.getSession(),
//                pathType,
//                size, path);
        HttpUrl.Builder builder = new HttpUrl.Builder();
        if (!EmptyUtils.isEmpty(size)) {
            builder.addQueryParameter("size", size);
        }
        if (groupId != null && groupId > 0) {
            builder.addQueryParameter("groupid", String.valueOf(groupId));
        }
        return builder
                .scheme(SCHME_HTTP)
                .host(loginSession.getIp())
                .port(AppConstants.HS_ANDROID_TV_PORT)
                .addPathSegment("/file/thumbnail")
                .addQueryParameter("session", loginSession.getSession())
                .addQueryParameter("share_path_type", "" + pathType)
                .addQueryParameter("path", path)
                .build()
                .toString();
    }

    @NotNull
    public static String genDownloadUrl(@NotNull LoginSession loginSession, int pathType, @NotNull String path, @Nullable Long groupId) {
//        return String.format("http://%s:%s/file/download?session=%s&share_path_type=%s&path=%s",
//                loginSession.getIp(),
//                AppConstants.HS_ANDROID_TV_PORT,
//                loginSession.getSession(),
//                pathType,
//                path);
        HttpUrl.Builder builder = new HttpUrl.Builder();
        if (groupId != null && groupId > 0) {
            builder.addQueryParameter("groupid", String.valueOf(groupId));
        }
        return builder
                .scheme(SCHME_HTTP)
                .host(loginSession.getIp())
                .port(AppConstants.HS_ANDROID_TV_PORT)
                .addPathSegment("/file/download")
                .addQueryParameter("session", loginSession.getSession())
                .addQueryParameter("share_path_type", "" + pathType)
                .addQueryParameter("path", path)
                .build()
                .toString();

    }

    @NotNull
    public static String genDownloadUrl(@NotNull LoginSession loginSession, int pathType, @NotNull String path) {
        return genDownloadUrl(loginSession, pathType, path, null);
    }

    public static String getV5Path(String srcPath) {
        if (!srcPath.startsWith(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)) {
            //去掉public前缀
            int beginIndex = srcPath.indexOf(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR);
            if (beginIndex > 0) {
                srcPath = srcPath.substring(beginIndex);
            }
        }
        return srcPath;
    }

    public static String genDownloadUrl(@NotNull LoginSession loginSession, Long GroupId, @NotNull OneOSFile file) {
        if (file.getShare_path_type() != -1) {
            return genDownloadUrl(loginSession, file.getShare_path_type(), file.getPath(), GroupId);
        } else {
            return genOpenUrl(loginSession, file.getAllPath());
        }
    }

    public static String genDownloadUrl(@NotNull LoginSession loginSession, @NotNull OneOSFile file) {
        if (file.getShare_path_type() != -1) {
            return genDownloadUrl(loginSession, file.getShare_path_type(), file.getPath());
        }
        return genOpenUrl(loginSession, file.getAllPath());
    }
}
