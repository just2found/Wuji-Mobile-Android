package org.view.libwidget.log;



/**
 * PopupWindow 的弹窗可见日志
 * //TODO 怎么做个悬浮的日志显示
 * 先不做实现
 */
public class ViewLog {

    /**
     * error的日志
     * @param tag 源头
     * @param text 内容
     */
    public static void e(String tag, String text) {


    }

    /**
     * debug的日志
     * @param tag 源头
     * @param text 内容
     */
    public static void d(String tag, String text) {

    }

    /**
     * info的日志
     * @param tag 源头
     * @param text 内容
     */
    public static void i(String tag, String text) {

    }

    public String getLogTag(){
       return ViewLog.class.getSimpleName();
    }


}


