package net.sdvn.nascommon.utils.log;


import net.sdvn.nascommonlib.BuildConfig;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class Logged {
    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static boolean CRASH_EXCEPTION = DEBUG;

    public static boolean BACKUP_ALBUM = DEBUG;
    public static boolean BACKUP_FILE = DEBUG;
    public static boolean BACKUP_CONTACTS = DEBUG;
    public static boolean BACKUP_SMS = DEBUG;
    public static boolean UPLOAD = DEBUG;
    public static boolean DOWNLOAD = DEBUG;
    public static boolean DAO = DEBUG;
    public static final boolean SHARE = DEBUG;
    public static final boolean OSAPI = DEBUG;
    public static final boolean HD = false;


}
