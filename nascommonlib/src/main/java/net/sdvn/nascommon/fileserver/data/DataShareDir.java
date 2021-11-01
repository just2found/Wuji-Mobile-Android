
package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;
@Keep
public class DataShareDir {
    @SerializedName("path")
    private List<SFile> path;
    @SerializedName("period")
    private int period;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("password")
    private String password;
    @SerializedName("page")
    private int page;
    @SerializedName("total")
    private int total;

    public DataShareDir() {
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

    public List<SFile> getPath() {
        return this.path;
    }

    public int getPeriod() {
        return this.period;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPath(List<SFile> path) {
        this.path = path;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataShareDir)) return false;
        final DataShareDir other = (DataShareDir) o;
        if (!other.canEqual(this)) return false;
        final Object this$path = this.path;
        final Object other$path = other.path;
        if (!Objects.equals(this$path, other$path)) return false;
        if (this.period != other.period) return false;
        final Object this$userId = this.userId;
        final Object other$userId = other.userId;
        if (!Objects.equals(this$userId, other$userId))
            return false;
        final Object this$password = this.password;
        final Object other$password = other.password;
        return Objects.equals(this$password, other$password);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataShareDir;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $path = this.path;
        result = result * PRIME + ($path == null ? 43 : $path.hashCode());
        result = result * PRIME + this.period;
        final Object $userId = this.userId;
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $password = this.password;
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        return result;
    }

    public String toString() {
        return "DataShareDir(path=" + this.path + ", period=" + this.period + ", userId=" + this.userId + ", password=" + this.password + ")";
    }
}
