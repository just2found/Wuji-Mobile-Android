package net.sdvn.nascommon.model.glide;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.OneOSAPIs;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Glide缓存配置文件
 */

public class GlideCacheConfig {
    private GlideCacheConfig() {
    }

    // 图片缓存最大容量，1000M，根据自己的需求进行修改
    public static final int GLIDE_CACHE_SIZE = 1000 * 1000 * 1000;

    // 图片缓存子目录
    public static final String GLIDE_CACHE_DIR = "glide";
    static String ENCODE_FILE_DOWNLOAD_SUFFIX;
    static String ENCODE_FILE_THUMBNAIL_SUFFIX;
    static String CHARSET_NAME;

    static {
        try {
            CHARSET_NAME = StandardCharsets.UTF_8.name();
            ENCODE_FILE_DOWNLOAD_SUFFIX = URLEncoder.encode(OneOSAPIs.FILE_DOWNLOAD_SUFFIX, CHARSET_NAME);
            ENCODE_FILE_THUMBNAIL_SUFFIX = URLEncoder.encode(OneOSAPIs.FILE_THUMBNAIL_SUFFIX, CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public static String getImageName(@NonNull String url) {
        try {
//            Log.d("EliCacheGlideUrl", "start cachekey: " + url);

            String key = null;
            boolean isOneOS = false;
            boolean needDecode = false;
            if (url.contains(OneOSAPIs.FILE_DOWNLOAD)) {
                key = OneOSAPIs.FILE_DOWNLOAD;
                isOneOS = true;
            } else if (url.contains(OneOSAPIs.FILE_THUMBNAIL)) {
                key = OneOSAPIs.FILE_THUMBNAIL;
                isOneOS = true;
            } else if (url.contains(OneOSAPIs.FILE_DOWNLOAD_SUFFIX)) {
                key = OneOSAPIs.FILE_DOWNLOAD_SUFFIX;
            } else if (url.contains(OneOSAPIs.FILE_THUMBNAIL_SUFFIX)) {
                key = OneOSAPIs.FILE_THUMBNAIL_SUFFIX;
            } else if (url.contains(ENCODE_FILE_DOWNLOAD_SUFFIX)) {
                key = OneOSAPIs.FILE_DOWNLOAD_SUFFIX;
                needDecode = true;
            } else if (url.contains(ENCODE_FILE_THUMBNAIL_SUFFIX)) {
                key = OneOSAPIs.FILE_THUMBNAIL_SUFFIX;
                needDecode = true;
            }
            if (key == null) {
                return replaceSession(url);
            }
            String imageName = url;
            if (isOneOS) {
                int start = url.lastIndexOf("&path=");
                if (start >= 0) {
                    imageName = url.substring(start);
                } else {
                    start = url.lastIndexOf(key + "?path=");
                    int end = url.lastIndexOf("&session=");
                    if (start >= 0 && end >= 0)
                        imageName = url.substring(start, end);
                }
            } else {
                imageName = replaceSession(url);
//                imageName = imageName.replaceFirst("(http|https)://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:\\d*/", "");
                imageName = imageName.replaceFirst("(http|https)://\\S*:\\d*/", "");
                if (needDecode) {
                    try {
                        imageName = URLDecoder.decode(imageName, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            String pattern = "\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}";
            Matcher matcher = Pattern.compile(pattern).matcher(url);
            String vip = null;
            if (matcher.find()) {
                vip = matcher.group();
            }
            if (vip == null) {

                return replaceSession(url);
            }
            String prefix = SessionManager.getInstance().genDevAccountTag(vip);
            String cacheKey = prefix + File.separator + key + File.separator + imageName;
//            Log.d("EliCacheGlideUrl", "cachekey: " + cacheKey);
            return cacheKey;
        } catch (Exception e) {
//            Log.e("EliCacheGlideUrl", "ErrorUrl: " + url, e);
            return url;
        }
    }

    @NotNull
    private static String replaceSession(@NonNull String url) {
        return url.replaceFirst("session=([0-9a-zA-Z.\\-_]+)", "");
    }
}
