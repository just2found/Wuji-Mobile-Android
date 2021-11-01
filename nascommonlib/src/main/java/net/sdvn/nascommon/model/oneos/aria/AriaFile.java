package net.sdvn.nascommon.model.oneos.aria;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

@Keep
public class AriaFile {
    /**
     * { "completedLength":"0", "index":"1", "length":"284",
     * "path":"\/sata\/storage\/public\/DOWNLOAD\/name in utf-8\/path in utf-8", "selected":"true",
     * "uris":[] }
     */
    @Nullable
    private String completedLength = null;
    @Nullable
    private String index = null;
    @Nullable
    private String length = null;
    @Nullable
    private String path = null;
    @Nullable
    private String selected = null;
    // private String uris = null; //"uris":[]

    @Nullable
    public String getCompletedLength() {
        return completedLength;
    }

    public void setCompletedLength(String completedLength) {
        this.completedLength = completedLength;
    }

    @Nullable
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @Nullable
    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Nullable
    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
