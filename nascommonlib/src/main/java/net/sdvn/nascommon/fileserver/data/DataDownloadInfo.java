package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.Set;
@Keep
public class DataDownloadInfo {

    @SerializedName("path")
    private Set<SFile> completed;
    @SerializedName("err_files")
    private Set<SFile> errFiles;
    @SerializedName("download_path")
    private Set<String> downloadPath;
    @SerializedName("page")
    private int page;
    @SerializedName("total")
    private int total;

    public DataDownloadInfo() {
    }

    public Set<SFile> getCompleted() {
        return this.completed;
    }

    public Set<SFile> getErrFiles() {
        return this.errFiles;
    }

    public Set<String> getDownloadPath() {
        return this.downloadPath;
    }

    public void setCompleted(Set<SFile> completed) {
        this.completed = completed;
    }

    public void setErrFiles(Set<SFile> errFiles) {
        this.errFiles = errFiles;
    }

    public void setDownloadPath(Set<String> downloadPath) {
        this.downloadPath = downloadPath;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataDownloadInfo)) return false;
        final DataDownloadInfo other = (DataDownloadInfo) o;
        if (!other.canEqual(this)) return false;
        final Object this$completed = this.completed;
        final Object other$completed = other.completed;
        if (!Objects.equals(this$completed, other$completed))
            return false;
        final Object this$errFiles = this.errFiles;
        final Object other$errFiles = other.errFiles;
        if (!Objects.equals(this$errFiles, other$errFiles))
            return false;
        final Object this$downloadPath = this.downloadPath;
        final Object other$downloadPath = other.downloadPath;
        return Objects.equals(this$downloadPath, other$downloadPath);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataDownloadInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $completed = this.completed;
        result = result * PRIME + ($completed == null ? 43 : $completed.hashCode());
        final Object $errFiles = this.errFiles;
        result = result * PRIME + ($errFiles == null ? 43 : $errFiles.hashCode());
        final Object $downloadPath = this.downloadPath;
        result = result * PRIME + ($downloadPath == null ? 43 : $downloadPath.hashCode());
        return result;
    }

    public String toString() {
        return "DataDownloadInfo(completed=" + this.completed + ", errFiles=" + this.errFiles + ", downloadPath=" + this.downloadPath + ")";
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
