package net.sdvn.nascommon.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.reflect.TypeToken
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.GsonUtils
import timber.log.Timber

fun <T> loadDiskCache(key: String): LiveData<T?> {
    val mutableLiveData = MutableLiveData<T?>()
    val diskCache = FileUtils.getDiskCache(key)
    Timber.d("diskCache files: $diskCache")
    val model = if (diskCache.isNullOrEmpty()) {
        null
    } else {
        GsonUtils.decodeJSONCatchException<T>(diskCache, object : TypeToken<T>() {}.type)
    }
    mutableLiveData.postValue(model)
    return mutableLiveData

}
