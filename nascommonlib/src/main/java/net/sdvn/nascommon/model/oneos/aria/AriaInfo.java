package net.sdvn.nascommon.model.oneos.aria;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * AriaInfo for show Aria generate list
 *
 * @author shz
 */
@Keep
public class AriaInfo {

    @Nullable
    private String gid = null;
    @Nullable
    private String status = null;
    @Nullable
    private BitTorrent bittorrent = null;
    @Nullable
    private List<AriaFile> files = null;
    @Nullable
    private String completedLength = null;
    @Nullable
    private String totalLength = null;
    @Nullable
    private String downloadSpeed = null;
    @Nullable
    private String uploadSpeed = null;
    @Nullable
    private String connections = null;
    @Nullable
    private String numSeeders = null;
    @Nullable
    private String dir = null;

    @Nullable
    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Nullable
    public BitTorrent getBittorrent() {
        return bittorrent;
    }

    public void setBittorrent(BitTorrent bittorrent) {
        this.bittorrent = bittorrent;
    }

    @Nullable
    public List<AriaFile> getFiles() {
        return files;
    }

    public void setFiles(List<AriaFile> files) {
        this.files = files;
    }

    @Nullable
    public String getCompletedLength() {
        return completedLength;
    }

    public void setCompletedLength(String completedLength) {
        this.completedLength = completedLength;
    }

    @Nullable
    public String getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(String totalLength) {
        this.totalLength = totalLength;
    }

    @Nullable
    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    @Nullable
    public String getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(String uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    @Nullable
    public String getConnections() {
        return connections;
    }

    public void setConnections(String connections) {
        this.connections = connections;
    }

    @Nullable
    public String getNumSeeders() {
        return numSeeders;
    }

    public void setNumSeeders(String numSeeders) {
        this.numSeeders = numSeeders;
    }

    @Nullable
    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

}
