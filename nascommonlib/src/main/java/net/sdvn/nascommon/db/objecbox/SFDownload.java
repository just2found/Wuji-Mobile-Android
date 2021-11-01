package net.sdvn.nascommon.db.objecbox;

import androidx.annotation.Keep;

import net.sdvn.nascommon.model.oneos.transfer.TransferState;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Keep
@Entity
public class SFDownload {
    @Id
    private long id;
    private String userId;
    private String token;

    private String toDevId;

    private String toPath;

    private long timestamp;

    private ToOne<ShareElementV2> shareElementV2;
    private int desPathType;
    /**
     * Transmission state
     */
    @Convert(converter = TransferState.TransferStateConverter.class, dbType = String.class)
    protected TransferState state = TransferState.NONE;

    public SFDownload() {
    }

    public long getId() {
        return this.id;
    }

    public String getToken() {
        return this.token;
    }

    public String getToDevId() {
        return this.toDevId;
    }

    public String getToPath() {
        return this.toPath;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public ToOne<ShareElementV2> getShareElementV2() {
        return this.shareElementV2;
    }

    public TransferState getState() {
        return this.state;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setToDevId(String toDevId) {
        this.toDevId = toDevId;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setShareElementV2(ToOne<ShareElementV2> shareElementV2) {
        this.shareElementV2 = shareElementV2;
    }

    public void setState(TransferState state) {
        this.state = state;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SFDownload)) return false;
        final SFDownload other = (SFDownload) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.id != other.id) return false;
        final Object this$token = this.token;
        final Object other$token = other.token;
        if (this$token == null ? other$token != null : !this$token.equals(other$token))
            return false;
        final Object this$toDevId = this.toDevId;
        final Object other$toDevId = other.toDevId;
        if (this$toDevId == null ? other$toDevId != null : !this$toDevId.equals(other$toDevId))
            return false;
        final Object this$toPath = this.toPath;
        final Object other$toPath = other.toPath;
        if (this$toPath == null ? other$toPath != null : !this$toPath.equals(other$toPath))
            return false;
        if (this.timestamp != other.timestamp) return false;
        final Object this$shareElementV2 = this.shareElementV2;
        final Object other$shareElementV2 = other.shareElementV2;
        if (this$shareElementV2 == null ? other$shareElementV2 != null : !this$shareElementV2.equals(other$shareElementV2))
            return false;
        final Object this$state = this.state;
        final Object other$state = other.state;
        if (this$state == null ? other$state != null : !this$state.equals(other$state))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SFDownload;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.id;
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        final Object $token = this.token;
        result = result * PRIME + ($token == null ? 43 : $token.hashCode());
        final Object $toDevId = this.toDevId;
        result = result * PRIME + ($toDevId == null ? 43 : $toDevId.hashCode());
        final Object $toPath = this.toPath;
        result = result * PRIME + ($toPath == null ? 43 : $toPath.hashCode());
        final long $timestamp = this.timestamp;
        result = result * PRIME + (int) ($timestamp >>> 32 ^ $timestamp);
        final Object $shareElementV2 = this.shareElementV2;
        result = result * PRIME + ($shareElementV2 == null ? 43 : $shareElementV2.hashCode());
        final Object $state = this.state;
        result = result * PRIME + ($state == null ? 43 : $state.hashCode());
        return result;
    }

    public String toString() {
        return "SFDownload(id=" + this.id + ", token=" + this.token + ", toDevId=" + this.toDevId + ", toPath=" + this.toPath + ", timestamp=" + this.timestamp + ", shareElementV2=" + this.shareElementV2 + ", state=" + this.state + ")";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDesPathType() {
        return desPathType;
    }

    public void setDesPathType(int desPathType) {
        this.desPathType = desPathType;
    }
}
