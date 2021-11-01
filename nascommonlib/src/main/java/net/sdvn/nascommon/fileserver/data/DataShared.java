
package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.nascommon.db.objecbox.ShareElementV2;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
@Keep
public class DataShared {
    @SerializedName("shared_list")
    private List<ShareElementV2> sharedList;
    @SerializedName("download_list")
    private List<ShareElementV2> downloadList;

    public DataShared() {
    }

    public List<ShareElementV2> getSharedList() {
        return this.sharedList;
    }

    public List<ShareElementV2> getDownloadList() {
        return this.downloadList;
    }

    public void setSharedList(List<ShareElementV2> sharedList) {
        this.sharedList = sharedList;
    }

    public void setDownloadList(List<ShareElementV2> downloadList) {
        this.downloadList = downloadList;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataShared)) return false;
        final DataShared other = (DataShared) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$sharedList = this.sharedList;
        final Object other$sharedList = other.sharedList;
        if (!Objects.equals(this$sharedList, other$sharedList))
            return false;
        final Object this$downloadList = this.downloadList;
        final Object other$downloadList = other.downloadList;
        if (!Objects.equals(this$downloadList, other$downloadList))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataShared;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $sharedList = this.sharedList;
        result = result * PRIME + ($sharedList == null ? 43 : $sharedList.hashCode());
        final Object $downloadList = this.downloadList;
        result = result * PRIME + ($downloadList == null ? 43 : $downloadList.hashCode());
        return result;
    }

    public String toString() {
        return "DataShared(sharedList=" + this.sharedList + ", downloadList=" + this.downloadList + ")";
    }
}
