package net.linkmate.app.util

import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Raleigh.Luo
 * date：21/1/5 15
 * describe：
 */
object TimeUtil {
//    /**
//     * 动态列表 最近时间
//     */
//    fun getRecentlyTime(timeInMillis: Long?): String {
//        var result = ""
//        timeInMillis?.let {
//            val distanceDays: Int = compareWithCurDate(timeInMillis) //不比较时分，只比较年月日
//            val context = MyApplication.getContext()
//            result = when {
//                distanceDays == 0 -> {//今天的
//                    val distanceTimeInMillis = Calendar.getInstance().timeInMillis - timeInMillis
//                    //相差的分钟数, 最小为1分钟
//                    val minute = distanceTimeInMillis / 1000 / 60
//                    val distanceMinute: Long = Math.max(1L, minute)
//                    if ((minute) < 1) context.getString(R.string.just_now)
//                    else if (distanceMinute < 60) String.format(context.getString(R.string.before_minutes), distanceMinute)
//                    else String.format(context.getString(R.string.before_hours), distanceMinute / 60)
//                }
//                distanceDays == -1 -> {//昨天的
//                    context.getString(R.string.yestody)
//                }
//                (Math.abs(distanceDays) <= 30) -> {//前30天内,显示 x天前
//                    String.format(context.getString(R.string.before_days), Math.abs(distanceDays))
//                }
//                else -> {
//                    getDateFormat(timeInMillis)
//                }
//            }
//        }
//        return result
//    }

    /**
     * 动态详情时间
     */
    fun getDetailTime(timeInMillis: Long?): String {
        var result = ""
        timeInMillis?.let {
            val distanceDays: Int = compareWithCurDate(timeInMillis) //不比较时分，只比较年月日
            val context = MyApplication.getContext()
            result = when {
                distanceDays == 0 -> {//今天的
                    getShortTimeFormat(timeInMillis)
                }
                distanceDays == -1 -> {//昨天的
                    String.format("%s %s", context.getString(R.string.yestody), getShortTimeFormat(timeInMillis))
                }
                else -> {
                    getDateFormat(timeInMillis)
                }
            }
        }
        return result
    }


    /**与当前时间比较大小， 并返回相差的天数（不比较时分秒比较）
     * @param timeInMillis 时间毫秒数
     * @return 相差的天数，小于0：小于当前时间，0等于当前时间，大于0 大于当前时间，
     */
    fun compareWithCurDate(timeInMillis: Long): Int {
        var result = -1
        try {
            val mCal = Calendar.getInstance()
            mCal.timeInMillis = timeInMillis
            val nowCal = Calendar.getInstance()
            result = if (mCal[Calendar.YEAR] === nowCal[Calendar.YEAR]) { //同年
                mCal[Calendar.DAY_OF_YEAR] - nowCal[Calendar.DAY_OF_YEAR]
            } else if (mCal[Calendar.YEAR] > nowCal[Calendar.YEAR]) { //下一年，大于今年
                //今年的最后一天
                val thisYearLastDay = nowCal.getActualMaximum(Calendar.DAY_OF_YEAR)
                //今年剩余的天数+下一年的年天数
                thisYearLastDay - nowCal[Calendar.DAY_OF_YEAR] + mCal[Calendar.DAY_OF_YEAR]
            } else { //上一年，小于今年
                //上一年的最后一天
                val lastYearLastDay = mCal.getActualMaximum(Calendar.DAY_OF_YEAR)
                //上一年剩余的天数+今年的年天数
                -(lastYearLastDay - mCal[Calendar.DAY_OF_YEAR] + nowCal[Calendar.DAY_OF_YEAR])
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**获取当前系统格式 时分
     * @param timeInMillis 时间毫秒数
     * @return 格式 （下午）06:09
     */
    fun getShortTimeFormat(timeInMillis: Long?): String {
        var result = ""
        try {
            timeInMillis?.let {
                val date = Date(timeInMillis)
                val df = SimpleDateFormat("HH:mm")
//                val df: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
                result = df.format(date)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**获取系统日期格式 年月日时分
     * @param timeInMillis 时间毫秒数
     * @return 格式2014年09月08日 （下午）06:09，不是本年返回9月8日（下午）06:09
     */
    fun getDateFormat(timeInMillis: Long?): String {
        var result = ""
        try {
            timeInMillis?.let {
                val date = Date(timeInMillis)
                val df = SimpleDateFormat("yyyy-MM-dd HH:mm")
//                val df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT)
                result = df.format(date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }


}