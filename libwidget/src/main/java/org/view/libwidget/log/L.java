package org.view.libwidget.log;

import android.text.TextUtils;

import org.view.libwidget.BuildConfig;

import java.util.Date;

/**
 * 注意这里的
 * 日志总控制台
 */
public class L {

    public static final int ConsoleType = 1;
    public static final int FileType = 2;
    public static final int ViewType = 4;
    public static final int No = 32;


    public static final String LogName = "nwq";
    public static final String FILTER_FLAG = "NWQ_FILTER: ";  //进行过滤筛选的
    public static final Date LogDate = new Date("2021/3/40");//("2020/8/30");

    //用来控制日志输出类型
    public static int Type = BuildConfig.DEBUG ? ConsoleType : No;

    /**
     * @param content
     * @param methodName
     * @param className
     * @param userName
     * @param dateString
     */
    public static void d(String content, String methodName, String className, String userName, String dateString) {

        if (isOutputRequired(userName, dateString))
            d(className, FILTER_FLAG + methodName + ": " + content);
    }

    /**
     * @param content
     * @param methodName
     * @param className
     * @param userName
     * @param dateString
     */
    public static void i(String content, String methodName, String className, String userName, String dateString) {
        if (isOutputRequired(userName, dateString))
            i(className, FILTER_FLAG + methodName + ": " + content);
    }

    /**
     * @param content
     * @param methodName
     * @param className
     * @param userName
     * @param dateString
     */
    public static void e(String content, String methodName, String className, String userName, String dateString) {
        if (isOutputRequired(userName, dateString))
            e(className, FILTER_FLAG + methodName + ": " + content);
    }


    /**
     * 控制日志是否输出
     *
     * @param userName
     * @param dateString
     * @return
     */
    private static boolean isOutputRequired(String userName, String dateString) {
        boolean required = true;
        if (!TextUtils.isEmpty(LogName) && !TextUtils.isEmpty(userName) && !LogName.equals(userName)) {
            required = false;
        } else if (!TextUtils.isEmpty(dateString)) {
            Date date = new Date(dateString);
            if (date.before(LogDate)) {
                required = false;
            }
        }
        return required;
    }

    @Deprecated
    public static void d(String Tag, String content) {
        if ((Type & ConsoleType) == ConsoleType) {
            ConsoleLog.d(Tag, content);
        }
        if ((Type & FileType) == FileType) {
            FileLog.d(Tag, content);
        }
        if ((Type & ViewType) == ViewType) {
            ViewLog.d(Tag, content);
        }
    }

    @Deprecated
    public static void i(String Tag, String content) {
        if ((Type & ConsoleType) == ConsoleType) {
            ConsoleLog.i(Tag, content);
        }
        if ((Type & FileType) == FileType) {
            FileLog.i(Tag, content);
        }
        if ((Type & ViewType) == ViewType) {
            ViewLog.i(Tag, content);
        }
    }

    @Deprecated
    public static void e(String Tag, String content) {
        if ((Type & ConsoleType) == ConsoleType) {
            ConsoleLog.e(Tag, content);
        }
        if ((Type & FileType) == FileType) {
            FileLog.e(Tag, content);
        }
        if ((Type & ViewType) == ViewType) {
            ViewLog.e(Tag, content);
        }
    }


}
