package net.sdvn.nascommon.db;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/7/12.
 */
public abstract class DBKeeper<T> {
    @NonNull
    public abstract DBKeeper creator();

    @NonNull
    public abstract List<T> all();

    @NonNull
    public abstract T query(Object key);

    public abstract boolean insert(T t);

    public abstract boolean update(T t);

    public abstract boolean delete(T t);

}
