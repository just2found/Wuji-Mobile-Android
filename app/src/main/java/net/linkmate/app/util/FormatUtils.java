package net.linkmate.app.util;

import android.content.Context;

import net.linkmate.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class FormatUtils {

    /**
     * 将long型的文件大小转换成格式规范的String
     */
    public static String getSizeFormat(long size) {
        String sizeFormat;
        long B = size % 1024;
        long KB = size / 1024;
        long MB = KB / 1024;
        long GB = MB / 1024;
        long TB = GB / 1024;
        if (TB > 0) {
            long decimals = GB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = TB + "." + decimalsStr + " TB";
        } else if (GB > 0) {
            long decimals = MB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = GB + "." + decimalsStr + " GB";
        } else if (MB > 0) {
            long decimals = KB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = MB + "." + decimalsStr + " MB";
        } else if (KB > 0) {
            long decimals = B % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = KB + "." + decimalsStr + " KB";
        } else {
            sizeFormat = B + " B";
        }
        return sizeFormat;
    }

    public static String getSizeFormat(long size, boolean isBit) {
        String sizeFormat;
        if (isBit)
            size *= 8;
        long B = size % 1024;
        long KB = size / 1024;
        long MB = KB / 1024;
        long GB = MB / 1024;
        long TB = GB / 1024;
        if (TB > 0) {
            long decimals = GB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = TB + "." + decimalsStr + " " + (isBit ? "t" : "T");
        } else if (GB > 0) {
            long decimals = MB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = GB + "." + decimalsStr + " " + (isBit ? "g" : "G");
        } else if (MB > 0) {
            long decimals = KB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = MB + "." + decimalsStr + " " + (isBit ? "m" : "M");
        } else if (KB > 0) {
            long decimals = B % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = KB + "." + decimalsStr + " " + (isBit ? "k" : "K");
        } else {
            sizeFormat = B + " ";
        }
        sizeFormat = sizeFormat + (isBit ? "b" : "B");
        return sizeFormat;
    }

    /**
     * 将long型的传输速度转换成格式规范的String
     */
    public static String getSizeSpeedFormat(long size) {
        String sizeFormat;
        long B = size % 1024;
        long KB = size / 1024;
        long MB = KB / 1024;
        long GB = MB / 1024;
        long TB = GB / 1024;
        if (TB > 0) {
            long decimals = GB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = TB + "." + decimalsStr + " TB/s";
        } else if (GB > 0) {
            long decimals = MB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = GB + "." + decimalsStr + " GB/s";
        } else if (MB > 0) {
            long decimals = KB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = MB + "." + decimalsStr + " MB/s";
        } else if (KB > 0) {
            long decimals = B % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = KB + "." + decimalsStr + " KB/s";
        } else {
            sizeFormat = B + " B/s";
        }
        return sizeFormat;
    }

    /**
     * 将long型的秒数转换成格式规范的String
     */
    public static String getUptime(long time) {
        if (time > 0) {
            int hour = (int) (time / 60 / 60);
            int min = (int) (time / 60 % 60);
            int second = (int) (time % 60);
            final String h = hour > 9 ? "" + hour : "0" + hour;
            final String m = min > 9 ? "" + min : "0" + min;
            final String s = second > 9 ? "" + second : "0" + second;
            return h + ":" + m + ":" + s;
        } else return "00:00:00";
    }

    /**
     * 将long型的秒数转换成格式规范的String
     */
    public static String getUptimeDay(long time,Context ctx ) {
        if (time > 0) {
            int hour = (int) (time / 60 / 60);
            int min = (int) (time / 60 % 60);
            int second = (int) (time % 60);
            int day = 0;
            if (hour > 24) {
                day = hour / 24;
                hour = hour % 24;
            }

            final String h = hour > 9 ? "" + hour : "0" + hour;
            final String m = min > 9 ? "" + min : "0" + min;
            final String s = second > 9 ? "" + second : "0" + second;
            if(day>0)
            {
                return day+ ctx.getString(R.string.day)+"  "+h + ":" + m + ":" + s;
            }else {
                return h + ":" + m + ":" + s;
            }

        } else return "00:00:00";
    }


    public static String getLatencyText(int latency) {
        String latency_text;
        if (latency >= 1000) {
            latency = latency / 1000;
            latency_text = latency + " s";
        } else latency_text = latency + " ms";
        return latency_text;
    }

    /**
     * 英文环境下将数字月份格式化为英文
     *
     * @param ctx
     * @param date
     * @return
     */
    public static String monthFormatToEn(Context ctx, String date) {
        Locale curLocale = ctx.getResources().getConfiguration().locale;
        String language = curLocale.getLanguage();
        String month = date;
        if (!"zh".equals(language) &&
                !"ja".equals(language) &&
                !"ko".equals(language)) {
            try {
                SimpleDateFormat sf1 = new SimpleDateFormat("MM");
                Date parse = sf1.parse(date);
                SimpleDateFormat sf2 = new SimpleDateFormat("MMM");
                month = sf2.format(parse);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        return month;
    }

    public static String monthFormatToNumber(Context ctx, String date) {
        Locale curLocale = ctx.getResources().getConfiguration().locale;
        String language = curLocale.getLanguage();
        String month = date;
        if (!"zh".equals(language)) {
            try {
                SimpleDateFormat sf1 = new SimpleDateFormat("MMM", Locale.US);
                Date parse = sf1.parse(date);
                SimpleDateFormat sf2 = new SimpleDateFormat("MM");
                month = sf2.format(parse);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        return month;
    }
}
