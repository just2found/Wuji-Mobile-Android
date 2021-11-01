package io.weline.repo.torrent.constants;

import android.os.Environment;
import android.util.Base64;

import java.util.UUID;

public final class BT_Config {
    public static final boolean isDebug = false;
    public static final int PORT_DEBUG = 9894;
    public static final int PORT = 9895;
    public static final int WS_PORT_DEBUG = 9895;
    public static final int WS_PORT = 9894;
    public static final int PORT_LOCAL_DEBUG = 9996;
    public static final int PORT_LOCAL = 9996;
    public static final String DEFAULT_BT_SERVER_DIR = "";
    public static final String SCHEME = "http";
    public static final String BT_LOCAL_DEVICE_ID = "local";
    public static final String BT_LOCAL_DEVICE_HOST = "localhost";
    public static final String token = Base64.encodeToString(UUID.randomUUID().toString().getBytes(), Base64.DEFAULT);
    public static final String DEFAULT_DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    public static final int M_CMD_BT = 1;
    public static final int S_CMD_BT_CREATE = 1;


    public static final int S_CMD_RESPONSE = 0x8000;


    public static final String API_PATH_AUTH = "auth";
    public static final String API_PATH_CREATE = "create";
    public static final String API_PATH_DOWNLOAD = "download";
    public static final String API_PATH_PROGRESS = "progress";
    public static final String API_PATH_STOP = "stop";
    public static final String API_PATH_RESUME = "resume";
    public static final String API_PATH_LIST = "list";
    public static final String API_PATH_CANCEL = "cancel";

}
