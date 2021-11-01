package libs.source.common.utils;

import android.content.Context


/**
create by: 86136
create time: 2020/10/21 14:38
Function description:
 */
object SPUtilsN {


    val context: Context by lazy {
        Utils.getApp()
    }

    var name: String = "nwq_for_net"

   const val GROUP_ANNOUNCEMENT_TIME: String = "Group_Announcement"


    private val mSharedPreferences by lazy {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    private val mEditor by lazy {
        mSharedPreferences.edit()
    }


    /**
     * 存储
     */
    fun <T> put(key: String, value: T) {
        when (value) {
            is String -> {
                mEditor.putString(key, value)
            }
            is Boolean -> {
                mEditor.putBoolean(key, value)
            }
            is Float -> {
                mEditor.putFloat(key, value)
            }
            is Int -> {
                mEditor.putInt(key, value)
            }
            is Long -> {
                mEditor.putLong(key, value)
            }

        }
        mEditor.apply()
    }

    /**
     * 获取保存的数据
     */
    fun <T> get(key: String, defaultValue: T): T {
        when (defaultValue) {
            is String -> {
                return mSharedPreferences.getString(key, defaultValue) as T
            }
            is Boolean -> {
                return mSharedPreferences.getBoolean(key, defaultValue) as T
            }
            is Float -> {
                return mSharedPreferences.getFloat(key, defaultValue) as T
            }
            is Int -> {
                return mSharedPreferences.getInt(key, defaultValue) as T
            }
            is Long -> {
                return mSharedPreferences.getLong(key, defaultValue) as T
            }
            else ->
                return defaultValue
        }
    }


}