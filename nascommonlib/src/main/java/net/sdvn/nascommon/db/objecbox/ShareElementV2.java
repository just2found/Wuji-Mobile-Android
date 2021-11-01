package net.sdvn.nascommon.db.objecbox;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.sdvn.nascommon.db.converter.StringListConverter;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;

import java.util.List;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;
import io.objectbox.relation.ToMany;

@Keep
@Entity
public class ShareElementV2 {
    @Id
    private long id;
    @SerializedName("ticket_1")
    private String ticket1;
    @NonNull
    @SerializedName("ticket_2")
    private String ticket2;

    private String userId;

    private String srcDevId;
    @Nullable
    private String toDevId;

    private int type;

    @SerializedName("max_download")
    private int maxDownload;
    @SerializedName("path")
    @Convert(converter = StringListConverter.class, dbType = String.class)
    private List<String> path;
    @SerializedName("remain_download")
    private int remainDownload;
    @SerializedName("remain_period")
    private long remainPeriod;

    @SerializedName("timestamp")
    private long timestamp;
    @SerializedName("ticket")
    private String downloadId;

    private String fromOwner;
    @SerializedName("to_path")
    private String toPath;
    @SerializedName("password")
    private String password;
    /**
     * Transmission state
     */
    @Convert(converter = TransferState.TransferStateConverter.class, dbType = String.class)
    protected TransferState state = TransferState.NONE;
    @Transient
    private int errNo;
    @SerializedName("share_path_type")
    private int sharePathType = -1;

    public ShareElementV2(String ticket1, @NonNull String ticket2, String srcDevId, int type) {
        this.ticket1 = ticket1;
        this.ticket2 = ticket2;
        this.srcDevId = srcDevId;
        this.type = type;
    }

    public ShareElementV2() {
    }

    @Backlink
    private ToMany<SFDownload> sFDownloads;


    public long getId() {
        return this.id;
    }

    public String getTicket1() {
        return this.ticket1;
    }

    @NonNull
    public String getTicket2() {
        return this.ticket2;
    }

    public String getSrcDevId() {
        return this.srcDevId;
    }

    @Nullable
    public String getToDevId() {
        return this.toDevId;
    }

    public int getType() {
        return this.type;
    }

    public boolean isType(int type) {
        return (this.type & type) == type;
    }

    public int getMaxDownload() {
        return this.maxDownload;
    }

    public List<String> getPath() {
        return this.path;
    }

    public int getRemainDownload() {
        return this.remainDownload;
    }

    public long getRemainPeriod() {
        return this.remainPeriod;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getDownloadId() {
        return this.downloadId;
    }

    public String getFromOwner() {
        return this.fromOwner;
    }

    public String getToPath() {
        return this.toPath;
    }

    public String getPassword() {
        return this.password;
    }

    public TransferState getState() {
        return this.state;
    }

    public int getErrNo() {
        return this.errNo;
    }

    public ToMany<SFDownload> getSFDownloads() {
        return this.sFDownloads;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTicket1(String ticket1) {
        this.ticket1 = ticket1;
    }

    public void setTicket2(@NonNull String ticket2) {
        this.ticket2 = ticket2;
    }

    public void setSrcDevId(String srcDevId) {
        this.srcDevId = srcDevId;
    }

    public void setToDevId(@Nullable String toDevId) {
        this.toDevId = toDevId;
    }

    public void setType(int type) {
        this.type |= type;
    }

    public void setMaxDownload(int maxDownload) {
        this.maxDownload = maxDownload;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public void setRemainDownload(int remainDownload) {
        this.remainDownload = remainDownload;
    }

    public void setRemainPeriod(long remainPeriod) {
        this.remainPeriod = remainPeriod;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public void setFromOwner(String fromOwner) {
        this.fromOwner = fromOwner;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setState(TransferState state) {
        this.state = state;
    }

    public void setErrNo(int errNo) {
        this.errNo = errNo;
    }

    public void setSFDownloads(ToMany<SFDownload> sFDownloads) {
        this.sFDownloads = sFDownloads;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ShareElementV2)) return false;
        final ShareElementV2 other = (ShareElementV2) o;
        if (!other.canEqual(this)) return false;
        if (this.id != other.id) return false;
        final Object this$ticket1 = this.ticket1;
        final Object other$ticket1 = other.ticket1;
        if (this$ticket1 == null ? other$ticket1 != null : !this$ticket1.equals(other$ticket1))
            return false;
        final Object this$ticket2 = this.ticket2;
        final Object other$ticket2 = other.ticket2;
        if (this$ticket2 == null ? other$ticket2 != null : !this$ticket2.equals(other$ticket2))
            return false;
        final Object this$srcDevId = this.srcDevId;
        final Object other$srcDevId = other.srcDevId;
        if (this$srcDevId == null ? other$srcDevId != null : !this$srcDevId.equals(other$srcDevId))
            return false;
        final Object this$toDevId = this.toDevId;
        final Object other$toDevId = other.toDevId;
        if (this$toDevId == null ? other$toDevId != null : !this$toDevId.equals(other$toDevId))
            return false;
        if (this.type != other.type) return false;
        if (this.maxDownload != other.maxDownload) return false;
        final Object this$path = this.path;
        final Object other$path = other.path;
        if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
        if (this.remainDownload != other.remainDownload) return false;
        if (this.remainPeriod != other.remainPeriod) return false;
        if (this.timestamp != other.timestamp) return false;
        final Object this$downloadId = this.downloadId;
        final Object other$downloadId = other.downloadId;
        if (this$downloadId == null ? other$downloadId != null : !this$downloadId.equals(other$downloadId))
            return false;
        final Object this$fromOwner = this.fromOwner;
        final Object other$fromOwner = other.fromOwner;
        if (this$fromOwner == null ? other$fromOwner != null : !this$fromOwner.equals(other$fromOwner))
            return false;
        final Object this$toPath = this.toPath;
        final Object other$toPath = other.toPath;
        if (this$toPath == null ? other$toPath != null : !this$toPath.equals(other$toPath))
            return false;
        final Object this$password = this.password;
        final Object other$password = other.password;
        if (this$password == null ? other$password != null : !this$password.equals(other$password))
            return false;
        final Object this$state = this.state;
        final Object other$state = other.state;
        if (this$state == null ? other$state != null : !this$state.equals(other$state))
            return false;
        if (this.errNo != other.errNo) return false;
        final Object this$sFDownloads = this.sFDownloads;
        final Object other$sFDownloads = other.sFDownloads;
        return this$sFDownloads == null ? other$sFDownloads == null : this$sFDownloads.equals(other$sFDownloads);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ShareElementV2;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.id;
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        final Object $ticket1 = this.ticket1;
        result = result * PRIME + ($ticket1 == null ? 43 : $ticket1.hashCode());
        final Object $ticket2 = this.ticket2;
        result = result * PRIME + ($ticket2 == null ? 43 : $ticket2.hashCode());
        final Object $srcDevId = this.srcDevId;
        result = result * PRIME + ($srcDevId == null ? 43 : $srcDevId.hashCode());
        final Object $toDevId = this.toDevId;
        result = result * PRIME + ($toDevId == null ? 43 : $toDevId.hashCode());
        result = result * PRIME + this.type;
        result = result * PRIME + this.maxDownload;
        final Object $path = this.path;
        result = result * PRIME + ($path == null ? 43 : $path.hashCode());
        result = result * PRIME + this.remainDownload;
        final long $remainPeriod = this.remainPeriod;
        result = result * PRIME + (int) ($remainPeriod >>> 32 ^ $remainPeriod);
        final long $timestamp = this.timestamp;
        result = result * PRIME + (int) ($timestamp >>> 32 ^ $timestamp);
        final Object $downloadId = this.downloadId;
        result = result * PRIME + ($downloadId == null ? 43 : $downloadId.hashCode());
        final Object $fromOwner = this.fromOwner;
        result = result * PRIME + ($fromOwner == null ? 43 : $fromOwner.hashCode());
        final Object $toPath = this.toPath;
        result = result * PRIME + ($toPath == null ? 43 : $toPath.hashCode());
        final Object $password = this.password;
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        final Object $state = this.state;
        result = result * PRIME + ($state == null ? 43 : $state.hashCode());
        result = result * PRIME + this.errNo;
        final Object $sFDownloads = this.sFDownloads;
        result = result * PRIME + ($sFDownloads == null ? 43 : $sFDownloads.hashCode());
        return result;
    }

    public String toString() {
        return "ShareElementV2(id=" + this.id + ", ticket1=" + this.ticket1 + ", ticket2=" + this.ticket2 + ", srcDevId=" + this.srcDevId + ", toDevId=" + this.toDevId + ", type=" + this.type + ", maxDownload=" + this.maxDownload + ", path=" + this.path + ", remainDownload=" + this.remainDownload + ", remainPeriod=" + this.remainPeriod + ", timestamp=" + this.timestamp + ", downloadId=" + this.downloadId + ", fromOwner=" + this.fromOwner + ", toPath=" + this.toPath + ", password=" + this.password + ", state=" + this.state + ", errNo=" + this.errNo + ", sFDownloads=" + this.sFDownloads + ")";
    }

    public int getSharePathType() {
        return sharePathType;
    }

    public void setSharePathType(int sharePathType) {
        this.sharePathType = sharePathType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}


