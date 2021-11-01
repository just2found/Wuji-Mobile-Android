package libs.source.common.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.reflect.TypeToken;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 *  
 * <p>
 * Created by admin on 2020/8/1,10:26
 */
public class DiskLruCacheHelper {
    public static final int DISK_LRU_CACHE_APP_VERSION = 1;

    /**
     * 获取应用的cache目录
     */
    public static String getCachePath() {
        File f = Utils.getApp().getCacheDir();
        if (null == f) {
            return null;
        } else {
            return f.getAbsolutePath() + "/";
        }
    }

    public static boolean putDiskCache(@NonNull String key, String value) {
        File directory = new File(getCachePath() + "httpNas");
        boolean success = true;
        if (!directory.exists()) {
            success = directory.mkdir();
        }
        DiskLruCache diskLruCache = null;
        try {
            Timber.d("putDiskCache -->%s:%s",key, Thread.currentThread());
            diskLruCache = DiskLruCache.open(directory, DISK_LRU_CACHE_APP_VERSION, 1, 200 * 1000 * 1000);
            String keyMD5 = Md5Utils.encode(key);
            DiskLruCache.Editor editor = diskLruCache.edit(keyMD5);
            if (editor != null) {
                editor.set(0, value);
                editor.commit();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (diskLruCache != null) {
                try {
                    diskLruCache.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @NonNull
    public static <T> LiveData<T> loadDiskCache(@NonNull String key) {
        MutableLiveData<T> mutableLiveData = new MutableLiveData();
        String diskCache = getDiskCache(key);
        Timber.d("diskCache : %s", diskCache);
        T value = EmptyUtils.isEmpty(diskCache) ?
                null
                : (T) GsonUtils.decodeJSONCatchException(diskCache, new TypeToken<T>() {
        }.getType());
        mutableLiveData.postValue(value);
        return mutableLiveData;
    }

    public static String getDiskCache(@NonNull String key) {
        File directory = new File(getCachePath() + "httpNas");
        if (directory.exists()) {
            DiskLruCache diskLruCache = null;
            try {
                Timber.d("getDiskCache -->%s:%s",key, Thread.currentThread());
                diskLruCache = DiskLruCache.open(directory, DISK_LRU_CACHE_APP_VERSION, 1, 200 * 1000 * 1000);
                String keyMD5 = Md5Utils.encode(key);
                DiskLruCache.Snapshot snapshot = diskLruCache.get(keyMD5);
                if (snapshot != null) {
                    return snapshot.getString(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (diskLruCache != null) {
                    try {
                        diskLruCache.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
