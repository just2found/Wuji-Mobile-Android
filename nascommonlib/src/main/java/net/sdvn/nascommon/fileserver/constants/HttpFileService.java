package net.sdvn.nascommon.fileserver.constants;

import okhttp3.HttpUrl;
import timber.log.Timber;

import static net.sdvn.nascommon.fileserver.constants.FS_Config.PORT;
import static net.sdvn.nascommon.fileserver.constants.FS_Config.SCHEME;

public class HttpFileService {
    public static String getHost(String ip) {
        if (ip == null) {
            ip = "";
            Timber.e("HttpFileService:getHost host==null");
        }
        return new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(ip)
                .port(PORT)
                .build()
                .toString();
    }
}