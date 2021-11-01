package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;
import java.util.Set;
@Keep
public class DataShareProgress {

    @SerializedName("total_size")
    private long totalSize;
    @SerializedName("current_size")
    private long currentSize;
    @SerializedName("speed")
    private long speed;
    @SerializedName("err")
    private int err;
    @SerializedName("err_files")
    private List<SFile> errFiles;
    @SerializedName("status")
    private int status;
    @SerializedName("download_path")
    private List<String> downloadPath;
    @SerializedName("err_files_num")
    private int errFilesNum;
    @SerializedName("download_path_num")
    private int downloadPathNum;
    @SerializedName("current_files")
    private Set<SFile> currentFiles;

    public DataShareProgress() {
    }

    public long getTotalSize() {
        return this.totalSize;
    }

    public long getCurrentSize() {
        return this.currentSize;
    }

    public long getSpeed() {
        return this.speed;
    }

    public int getErr() {
        return this.err;
    }

    public List<SFile> getErrFiles() {
        return this.errFiles;
    }

    public int getStatus() {
        return this.status;
    }

    public List<String> getDownloadPath() {
        return this.downloadPath;
    }

    public int getErrFilesNum() {
        return this.errFilesNum;
    }

    public int getDownloadPathNum() {
        return this.downloadPathNum;
    }

    public Set<SFile> getCurrentFiles() {
        return this.currentFiles;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public void setErr(int err) {
        this.err = err;
    }

    public void setErrFiles(List<SFile> errFiles) {
        this.errFiles = errFiles;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDownloadPath(List<String> downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setErrFilesNum(int errFilesNum) {
        this.errFilesNum = errFilesNum;
    }

    public void setDownloadPathNum(int downloadPathNum) {
        this.downloadPathNum = downloadPathNum;
    }

    public void setCurrentFiles(Set<SFile> currentFiles) {
        this.currentFiles = currentFiles;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataShareProgress)) return false;
        final DataShareProgress other = (DataShareProgress) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.totalSize != other.totalSize) return false;
        if (this.currentSize != other.currentSize) return false;
        if (this.speed != other.speed) return false;
        if (this.err != other.err) return false;
        final Object this$errFiles = this.errFiles;
        final Object other$errFiles = other.errFiles;
        if (!Objects.equals(this$errFiles, other$errFiles))
            return false;
        if (this.status != other.status) return false;
        final Object this$downloadPath = this.downloadPath;
        final Object other$downloadPath = other.downloadPath;
        if (!Objects.equals(this$downloadPath, other$downloadPath))
            return false;
        if (this.errFilesNum != other.errFilesNum) return false;
        if (this.downloadPathNum != other.downloadPathNum) return false;
        final Object this$currentFiles = this.currentFiles;
        final Object other$currentFiles = other.currentFiles;
        if (!Objects.equals(this$currentFiles, other$currentFiles))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataShareProgress;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $totalSize = this.totalSize;
        result = result * PRIME + (int) ($totalSize >>> 32 ^ $totalSize);
        final long $currentSize = this.currentSize;
        result = result * PRIME + (int) ($currentSize >>> 32 ^ $currentSize);
        final long $speed = this.speed;
        result = result * PRIME + (int) ($speed >>> 32 ^ $speed);
        result = result * PRIME + this.err;
        final Object $errFiles = this.errFiles;
        result = result * PRIME + ($errFiles == null ? 43 : $errFiles.hashCode());
        result = result * PRIME + this.status;
        final Object $downloadPath = this.downloadPath;
        result = result * PRIME + ($downloadPath == null ? 43 : $downloadPath.hashCode());
        result = result * PRIME + this.errFilesNum;
        result = result * PRIME + this.downloadPathNum;
        final Object $currentFiles = this.currentFiles;
        result = result * PRIME + ($currentFiles == null ? 43 : $currentFiles.hashCode());
        return result;
    }

    public String toString() {
        return "DataShareProgress(totalSize=" + this.totalSize + ", currentSize=" + this.currentSize + ", speed=" + this.speed + ", err=" + this.err + ", errFiles=" + this.errFiles + ", status=" + this.status + ", downloadPath=" + this.downloadPath + ", errFilesNum=" + this.errFilesNum + ", downloadPathNum=" + this.downloadPathNum + ", currentFiles=" + this.currentFiles + ")";
    }
}
