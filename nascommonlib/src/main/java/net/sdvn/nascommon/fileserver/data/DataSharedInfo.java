
package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@SuppressWarnings("unused")
@Keep
public class DataSharedInfo {


    /**
     * max_download : 2
     * remain_download : 0
     * remain_period : 1565258960
     * user_id : 18174192545
     */

    @SerializedName("max_download")
    private int maxDownload;
    @SerializedName("remain_download")
    private int remainDownload;
    @SerializedName("remain_period")
    private long remainPeriod;
    @SerializedName("user_id")
    private String userId;

    public DataSharedInfo() {
    }

    public int getMaxDownload() {
        return this.maxDownload;
    }

    public int getRemainDownload() {
        return this.remainDownload;
    }

    public long getRemainPeriod() {
        return this.remainPeriod;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setMaxDownload(int maxDownload) {
        this.maxDownload = maxDownload;
    }

    public void setRemainDownload(int remainDownload) {
        this.remainDownload = remainDownload;
    }

    public void setRemainPeriod(long remainPeriod) {
        this.remainPeriod = remainPeriod;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataSharedInfo)) return false;
        final DataSharedInfo other = (DataSharedInfo) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.maxDownload != other.maxDownload) return false;
        if (this.remainDownload != other.remainDownload) return false;
        if (this.remainPeriod != other.remainPeriod) return false;
        final Object this$userId = this.userId;
        final Object other$userId = other.userId;
        if (!Objects.equals(this$userId, other$userId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataSharedInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.maxDownload;
        result = result * PRIME + this.remainDownload;
        final long $remainPeriod = this.remainPeriod;
        result = result * PRIME + (int) ($remainPeriod >>> 32 ^ $remainPeriod);
        final Object $userId = this.userId;
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        return result;
    }

    public String toString() {
        return "DataSharedInfo(maxDownload=" + this.maxDownload + ", remainDownload=" + this.remainDownload + ", remainPeriod=" + this.remainPeriod + ", userId=" + this.userId + ")";
    }
}
