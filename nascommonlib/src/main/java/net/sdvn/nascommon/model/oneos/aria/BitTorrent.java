package net.sdvn.nascommon.model.oneos.aria;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

@Keep
public class BitTorrent {

    /**
     * "bittorrent":
     * {"announceList":[["http:\/\/tracker1"],["http:\/\/tracker2"],["http:\/\/tracker3"]],
     * "comment":"This is utf8 comment.", "creationDate":1123456789,
     * "info":{"info":"info in utf-8"}, "mode":"multi" },
     */
    // private ArrayList<String> announceList = new ArrayList<String>();
    @Nullable
    private String comment = null;
    private long creationDate = 0;
    @Nullable
    private BTInfo info = null;
    @Nullable
    private String mode = null;

    // public ArrayList<String> getAnnounceList() {
    // return announceList;
    // }
    //
    // public void setAnnounceList(ArrayList<String> announceList) {
    // this.announceList = announceList;
    // }

    @Nullable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    @Nullable
    public BTInfo getInfo() {
        return info;
    }

    public void setInfo(BTInfo info) {
        this.info = info;
    }

    @Nullable
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Keep
    public class BTInfo {
        @Nullable
        private String name = null;

        @Nullable
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
