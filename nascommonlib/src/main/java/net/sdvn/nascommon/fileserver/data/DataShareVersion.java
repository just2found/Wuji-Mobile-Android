package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;
@Keep
public class DataShareVersion {

    /**
     * version : 0.51
     * buildstamp : 2019-06-28 15:19:02
     */

    @SerializedName("version")
    private String version;
    @SerializedName("buildstamp")
    private String buildstamp;

    public DataShareVersion() {
    }

    public String getVersion() {
        return this.version;
    }

    public String getBuildstamp() {
        return this.buildstamp;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setBuildstamp(String buildstamp) {
        this.buildstamp = buildstamp;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataShareVersion)) return false;
        final DataShareVersion other = (DataShareVersion) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$version = this.version;
        final Object other$version = other.version;
        if (!Objects.equals(this$version, other$version))
            return false;
        final Object this$buildstamp = this.buildstamp;
        final Object other$buildstamp = other.buildstamp;
        if (!Objects.equals(this$buildstamp, other$buildstamp))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataShareVersion;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $version = this.version;
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $buildstamp = this.buildstamp;
        result = result * PRIME + ($buildstamp == null ? 43 : $buildstamp.hashCode());
        return result;
    }

    public String toString() {
        return "DataShareVersion(version=" + this.version + ", buildstamp=" + this.buildstamp + ")";
    }
}
