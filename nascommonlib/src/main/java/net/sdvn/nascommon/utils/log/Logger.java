package net.sdvn.nascommon.utils.log;

import android.util.Log;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommonlib.BuildConfig;


/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class Logger {
    private static final String LOG_PREFIX = "nas_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(Object o, Object... messages) {
        LOGD(makeLogTag(o.getClass()), messages);
    }

    public static void LOGD(String tag, Object... messages) {
        p(Level.DEBUG, tag, messages);
    }

    public static void LOGE(String tag, Object... messages) {
        LOGE(tag, null, messages);
    }

    public static void LOGE(String tag, Throwable t, Object... messages) {
        p(Level.ERROR, tag, t, messages);
    }

    public static void LOGI(String tag, Object... msg) {
        Logger.p(Logger.Level.INFO, tag, msg);
    }

    public static void p(Level level, boolean isLogged, String TAG, Object... msg) {
        if (level == Level.ERROR || isLogged) {
            p(level, TAG, msg);
        }
    }

    public static void p(Level level, boolean isLogged, String TAG, Throwable th, Object... msg) {
        if (level == Level.ERROR || isLogged) {
            p(level, TAG, th, msg);
        }
    }

    /**
     * Mack Log for tester.
     *
     * @param level
     * @param TAG
     * @param msg
     */
    public static void p(Level level, String TAG, Object... msg) {
        p(level, TAG, null, msg);
    }

    /**
     * Mack Log for tester.
     *
     * @param level
     * @param TAG
     * @param msg
     * @param th
     */
    public static void p(Level level, String TAG, Throwable th, Object... msg) {
        if (level == Level.ERROR) {//all print
            log(TAG, Log.ERROR, th, msg);
            return;
        }
        if (Logd.DEBUG) {//only debug print {warn,info,debug,verbose} level
            if (level == Level.WARN) {
                log(TAG, Log.WARN, th, msg);
            } else if (level == Level.INFO) {
                log(TAG, Log.INFO, th, msg);
            } else if (level == Level.DEBUG) {
                log(TAG, Log.DEBUG, th, msg);
            } else {
                log(TAG, Log.VERBOSE, th, msg);
            }
        }
    }

    private static void log(String tag, int level, @Nullable Throwable t, @Nullable Object... messages) {
        try {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                // handle this common case without the extra cost of creating a stringbuffer:
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    if (BuildConfig.DEBUG) {
                        if (!(m instanceof String)) {
                            if (m instanceof Throwable) {
                                sb.append(Log.getStackTraceString((Throwable) m));
                            } else
                                sb.append(GsonUtils.encodeJSON(m));
                        } else
                            sb.append(m);
                    }
                }
                if (t != null) {
                    sb.append("\n").append(Log.getStackTraceString(t));
                }
                message = sb.toString();
            }
            Log.println(level, tag, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Created by gaoyun@eli-tech.com on 2016/2/24.
     */
    public static class Logd {
        public static final boolean DEBUG = BuildConfig.DEBUG;

        public final static boolean CRASH_EXCEPTION = DEBUG;

        public final static boolean BACKUP_ALBUM = DEBUG;
        public final static boolean BACKUP_FILE = DEBUG;
        public final static boolean BACKUP_CONTACTS = DEBUG;
        public final static boolean BACKUP_SMS = DEBUG;
        public final static boolean UPLOAD = DEBUG;
        public final static boolean DOWNLOAD = DEBUG;
        public final static boolean DAO = DEBUG;
        public static final boolean SHARE = DEBUG;
        public static final boolean OSAPI = DEBUG;

    }

    /**
     * Created by gaoyun@eli-tech.com on 2016/2/24.
     */
    public enum Level {
        ERROR, WARN, INFO, DEBUG, VERBOSE
    }
}
