package net.sdvn.nascommon.fileserver.data;

import androidx.annotation.Keep;

import java.util.List;
@Keep
public final class SFileTree {
    private final List<SFile> list;
    private final SFile dir;
    private final SFileTree parent;


    public SFileTree(List<SFile> list, SFile dir, SFileTree parent) {
        this.list = list;
        this.dir = dir;
        this.parent = parent;
    }

    public List<SFile> getList() {
        return this.list;
    }

    public SFile getDir() {
        return this.dir;
    }

    public SFileTree getParent() {
        return this.parent;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SFileTree)) return false;
        final SFileTree other = (SFileTree) o;
        final Object this$list = this.list;
        final Object other$list = other.list;
        if (this$list == null ? other$list != null : !this$list.equals(other$list)) return false;
        final Object this$dir = this.dir;
        final Object other$dir = other.dir;
        if (this$dir == null ? other$dir != null : !this$dir.equals(other$dir)) return false;
        final Object this$parent = this.parent;
        final Object other$parent = other.parent;
        if (this$parent == null ? other$parent != null : !this$parent.equals(other$parent))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $list = this.list;
        result = result * PRIME + ($list == null ? 43 : $list.hashCode());
        final Object $dir = this.dir;
        result = result * PRIME + ($dir == null ? 43 : $dir.hashCode());
        final Object $parent = this.parent;
        result = result * PRIME + ($parent == null ? 43 : $parent.hashCode());
        return result;
    }

    public String toString() {
        return "SFileTree(list=" + this.list + ", dir=" + this.dir + ", parent=" + this.parent + ")";
    }
}