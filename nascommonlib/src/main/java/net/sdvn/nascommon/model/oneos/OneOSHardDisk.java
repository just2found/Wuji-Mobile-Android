package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/29.
 */
@Keep
public class OneOSHardDisk implements Serializable {
    private static final long serialVersionUID = 111181567L;

    @Nullable
    private String name = null;
    private long total = -1;
    private long free = -1;
    private long used = -1;
    private int tmp = -1;
    private int time = -1;
    @Nullable
    private String model = null;
    @Nullable
    private String serial = null;
    @Nullable
    private String capacity = null;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getFree() {
        return free;
    }

    public void setFree(long free) {
        this.free = free;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public int getTmp() {
        return tmp;
    }

    public void setTmp(int tmp) {
        this.tmp = tmp;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Nullable
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Nullable
    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Nullable
    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
}
