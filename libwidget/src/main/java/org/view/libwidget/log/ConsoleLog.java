package org.view.libwidget.log;

import android.util.Log;

/**
 * android 开发工具的控制台日志
 */
public class ConsoleLog {

    /**
     * error的日志
     *
     * @param tag  源头
     * @param text 内容
     */
    static void e(String tag, String text) {
        Log.e(tag, text);
    }

    /**
     * debug的日志
     *
     * @param tag  源头
     * @param text 内容
     */
    static void d(String tag, String text) {
        Log.d(tag, text);
    }

    /**
     * info的日志
     *
     * @param tag  源头
     * @param text 内容
     */
    static void i(String tag, String text) {
        Log.i(tag, text);
    }



}
