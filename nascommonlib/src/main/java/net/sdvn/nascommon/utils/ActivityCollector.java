package net.sdvn.nascommon.utils;//package com.eli.oneos.utils;
//
///**
// * Created by Administrator on 2017/8/15.
// */
//
///*
//public class ActivityCollector {
//    public static List<Activity> activities = new ArrayList<Activity>();
//
//    public static void addActivity(Activity activity) {
//        activities.add(activity);
//    }
//
//    public static void removeActivity(Activity activity) {
//        activities.remove(activity);
//    }
//
//    public static void finishAll() {
//        for (Activity activity : activities) {
//            if (!activity.isFinishing()) {
//                System.out.println("===================="+activity.getClass().getSimpleName());
//                activity.finish();
//            }
//        }
//    }
//    public static boolean isForeground(Activity activity) {
//        return isForeground(activity, activity.getClass().getName());
//    }
//
//    */
///**
//     * 判断某个界面是否在前台
//     *
//     * @param context   Context
//     * @param className 界面的类名
//     * @return 是否在前台显示
// *//*
//
//    public static boolean isForeground(Context context, String className) {
//        if (context == null || TextUtils.isEmpty(className))
//            return false;
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
//        if (list != null && list.size() > 0) {
//            ComponentName cpn = list.get(0).topActivity;
//            return className.equals(cpn.getClassName());
//        }
//        return false;
//    }
//}
//*/
