package libs.source.common.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SP<T>(private val key: String, private val default: T) : ReadWriteProperty<Any?, T> {
    companion object {
        lateinit var app: Application
        lateinit var name:String
        fun init(app: Application,name:String) {
            Companion.app = app
            Companion.name =name
        }

        val preference: SharedPreferences by lazy {
            app.getSharedPreferences(
                    name,
                    Context.MODE_PRIVATE
            )
        }

        fun clear() {
            preference.edit().clear().apply()
        }
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            with(preference) {
                val value = when (default) {
                    is String -> getString(key, default)
                    is Int -> getInt(key, default)
                    is Long -> getLong(key, default)
                    is Float -> getFloat(key, default)
                    is Boolean -> getBoolean(key, default)
                    else -> throw IllegalArgumentException("This type of data can not be get! ")
                }
                value as T
            }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
            with(preference.edit()) {
                when (default) {
                    is String -> putString(key, value as String)
                    is Int -> putInt(key, value as Int)
                    is Long -> putLong(key, value as Long)
                    is Float -> putFloat(key, value as Float)
                    is Boolean -> putBoolean(key, value as Boolean)
                    else -> throw IllegalArgumentException("This type of data can not be saved! ")
                }.apply()
            }
}