
package net.sdvn.nascommon.fileserver.data;

import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;
@Keep
public class SFile {
    @SerializedName("isDir")
    private boolean isDir;
    @SerializedName("name")
    private String name;
    @SerializedName(value = "size", alternate = {"total_size"})
    private long size;
    private boolean isSelected = false;
    @SerializedName("path")
    private String path;
    private boolean isDownloading;
    @SerializedName("err")
    private int errNo;
    @SerializedName("current_size")
    private long currentSize;

    public SFile() {
    }

    public void toggleSelected() {
        isSelected = !isSelected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SFile)) return false;
        SFile sFile = (SFile) o;
        return Objects.equals(getPath(), sFile.getPath());
    }

    public String getPath() {
        if (!TextUtils.isEmpty(path) && !path.startsWith("/"))
            path = "/" + path;
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath());
    }

    public boolean isDir() {
        return this.isDir;
    }

    public String getName() {
        return this.name;
    }

    public long getSize() {
        return this.size;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public boolean isDownloading() {
        return this.isDownloading;
    }

    public int getErrNo() {
        return this.errNo;
    }

    public long getCurrentSize() {
        return this.currentSize;
    }

    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setPath(String path) {
        if (!TextUtils.isEmpty(path) && !path.startsWith("/"))
            path = "/" + path;
        this.path = path;
    }

    public void setDownloading(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }

    public void setErrNo(int errNo) {
        this.errNo = errNo;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    @Override
    public String toString() {
        return "SFile{" +
                "isDir=" + isDir +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", isSelected=" + isSelected +
                ", path='" + path + '\'' +
                ", isDownloading=" + isDownloading +
                ", errNo=" + errNo +
                ", currentSize=" + currentSize +
                '}';
    }
}
