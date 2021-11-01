package io.weline.mediaplayer.internal.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Preference<T>(private val context: Context, private val name: String, private val default: T) : ReadWriteProperty<Any?, T>{

  private val prefs: SharedPreferences by lazy {
    context.getSharedPreferences("SpExoPlayer", Context.MODE_PRIVATE)
  }

  override  fun getValue(thisRef: Any?, property: KProperty<*>): T =
      getSharedPreferences(name, default)

  override  fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
      putSharedPreferences(name, value)


  private fun putSharedPreferences(name: String, value: T) = with(prefs.edit()) {
    when (value) {
      is Int -> putInt(name, value)
      is Float -> putFloat(name, value)
      is Long -> putLong(name, value)
      is Boolean -> putBoolean(name, value)
      is String -> putString(name, value)
      else -> throw IllegalArgumentException("SharedPreference can't be save this type")
    }.apply()
  }

  private fun getSharedPreferences(name: String, default: T): T = with(prefs) {
    when (default) {
      is Int -> {
        return@with getInt(name, default) as T
      }
      is Float -> {
        return@with getFloat(name, default) as T
      }
      is Long -> {
        return@with getLong(name, default) as T
      }
      is Boolean -> {
        return@with getBoolean(name, default) as T
      }
      is String -> {
        return@with getString(name, default) as T
      }
      else -> throw IllegalArgumentException("SharedPreference can't be get this type")
    }
  }
}
