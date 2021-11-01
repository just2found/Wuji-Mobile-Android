package net.sdvn.nascommon.model.oneos;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class FileInfoHolder {
    public static final String PIC = "pic";
    public static final String SHARE_FILE = "share_files";
    private static FileInfoHolder sInstance;

    public static FileInfoHolder getInstance() {
        if (sInstance == null) {
            sInstance = new FileInfoHolder();
        }
        return sInstance;
    }

    @NonNull
    private HashMap<Object, WeakReference<Object>> data = new HashMap<>();

    public void save(Object id, Object object) {
        data.put(id, new WeakReference<>(object));
    }

    @Nullable
    public Object retrieve(Object id) {
        WeakReference<Object> objectWeakReference = data.get(id);
        if (objectWeakReference == null) return null;
        return objectWeakReference.get();
    }

    public void remove(Object id) {
        data.remove(id);
    }


}
