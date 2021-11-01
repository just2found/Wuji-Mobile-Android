package net.linkmate.app.poster.model

import androidx.room.TypeConverter

class MediaConverters {
    @TypeConverter
    fun fromTimestamp(value: String?): ArrayList<String>? {
        if (value == null || value.isEmpty()) {
            return null
        }
        val list = ArrayList<String>()
        list.addAll(value.split(","))
        return list
    }

    @TypeConverter
    fun dateToTimestamp(list: ArrayList<String>?): String {
        if (list == null || list.isEmpty()) {
            return ""
        }
        val str = StringBuilder()
        list.forEachIndexed { index, value ->
            if (index == 0) {
                str.append(value)
            } else {
                str.append(",").append(value)
            }
        }
        return str.toString()
    }
}