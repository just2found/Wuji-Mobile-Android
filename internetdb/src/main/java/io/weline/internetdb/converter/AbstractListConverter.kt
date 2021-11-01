package io.weline.internetdb.converter

import androidx.annotation.NonNull
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.objectbox.converter.PropertyConverter
import java.util.*

abstract class AbstractListConverter<E> : PropertyConverter<List<E>, String> {
    @NonNull
    override fun convertToEntityProperty(databaseValue: String?): List<E> {
        val strings: MutableList<E> = ArrayList()
        if (databaseValue == null) return strings
        try {
            return Gson().fromJson(databaseValue, object : TypeToken<List<E>>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strings
    }

    override fun convertToDatabaseValue(entityProperty: List<E>): String? {
        return try {
            Gson().toJson(entityProperty)
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }
}