
package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;
@Keep
public class DataCreate {
    @SerializedName("ticket_2")
    private String ticket2;

    private List<String> paths;

    public DataCreate() {
    }

    public String getTicket2() {
        return this.ticket2;
    }

    public List<String> getPaths() {
        return this.paths;
    }

    public void setTicket2(String ticket2) {
        this.ticket2 = ticket2;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataCreate)) return false;
        final DataCreate other = (DataCreate) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$ticket2 = this.ticket2;
        final Object other$ticket2 = other.ticket2;
        if (!Objects.equals(this$ticket2, other$ticket2))
            return false;
        final Object this$paths = this.paths;
        final Object other$paths = other.paths;
        if (!Objects.equals(this$paths, other$paths))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataCreate;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $ticket2 = this.ticket2;
        result = result * PRIME + ($ticket2 == null ? 43 : $ticket2.hashCode());
        final Object $paths = this.paths;
        result = result * PRIME + ($paths == null ? 43 : $paths.hashCode());
        return result;
    }

    public String toString() {
        return "DataCreate(ticket2=" + this.ticket2 + ", paths=" + this.paths + ")";
    }
}
