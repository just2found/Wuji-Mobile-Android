package net.sdvn.common.internet;


public class NetConfig {
    //test访问地址
//    String host = "https://192.168.1.86:8445/";
    public static final String schema = "https";
    public static final int port = 8445;
    public static final int port2 = 8447;
    final static long DEFAULT_CACHE_SIZE = 100 * 1024 * 1024;
    public static boolean isPubTest = false;

    public static String host() {
        return OkHttpClientIns.getHost();
    }
}
