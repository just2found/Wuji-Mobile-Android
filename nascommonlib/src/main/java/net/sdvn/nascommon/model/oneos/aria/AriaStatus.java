package net.sdvn.nascommon.model.oneos.aria;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.ArrayList;

@Keep
public class AriaStatus {

    /*
     * {"bitfield":"00", "bittorrent": { Class:BitTorrent }, "completedLength":"0",
     * "connections":"0", "dir":"\/sata\/storage\/public\/DOWNLOAD", "downloadSpeed":"0", "files":[
     * AriaFile ], "gid":"56952b227aa11a25", "infoHash":"e8c46e50d37328728a6d9f55362e9ef19da8ed4e",
     * "numPieces":"3", "numSeeders":"0", "pieceLength":"128", "status":"active",
     * "totalLength":"384", "uploadLength":"0", "uploadSpeed":"0" }
     */

    @Nullable
    private String bitfield = null;
    @Nullable
    private BitTorrent bittorrent = null;
    @Nullable
    private String completedLength = null;
    @Nullable
    private String connections = null;
    @Nullable
    private String dir = null;
    @Nullable
    private String downloadSpeed = null;
    @Nullable
    private ArrayList<AriaFile> files = null;
    @Nullable
    private String gid = null;
    @Nullable
    private String infoHash = null;
    @Nullable
    private String numPieces = null;
    @Nullable
    private String numSeeders = null;
    @Nullable
    private String pieceLength = null;
    @Nullable
    private String status = null;
    @Nullable
    private String totalLength = null;
    @Nullable
    private String uploadLength = null;
    @Nullable
    private String uploadSpeed = null;

    // private String errorCode = null;
    // private String followedBy = null;
    // private String belongsTo = null;

    @Nullable
    public String getBitfield() {
        return bitfield;
    }

    public void setBitfield(String bitfield) {
        this.bitfield = bitfield;
    }

    @Nullable
    public BitTorrent getBittorrent() {
        return bittorrent;
    }

    public void setBittorrent(BitTorrent bittorrent) {
        this.bittorrent = bittorrent;
    }

    @Nullable
    public String getCompletedLength() {
        return completedLength;
    }

    public void setCompletedLength(String completedLength) {
        this.completedLength = completedLength;
    }

    @Nullable
    public String getConnections() {
        return connections;
    }

    public void setConnections(String connections) {
        this.connections = connections;
    }

    @Nullable
    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @Nullable
    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    @Nullable
    public ArrayList<AriaFile> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<AriaFile> files) {
        this.files = files;
    }

    @Nullable
    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    @Nullable
    public String getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(String infoHash) {
        this.infoHash = infoHash;
    }

    @Nullable
    public String getNumPieces() {
        return numPieces;
    }

    public void setNumPieces(String numPieces) {
        this.numPieces = numPieces;
    }

    @Nullable
    public String getNumSeeders() {
        return numSeeders;
    }

    public void setNumSeeders(String numSeeders) {
        this.numSeeders = numSeeders;
    }

    @Nullable
    public String getPieceLength() {
        return pieceLength;
    }

    public void setPieceLength(String pieceLength) {
        this.pieceLength = pieceLength;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Nullable
    public String getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(String totalLength) {
        this.totalLength = totalLength;
    }

    @Nullable
    public String getUploadLength() {
        return uploadLength;
    }

    public void setUploadLength(String uploadLength) {
        this.uploadLength = uploadLength;
    }

    @Nullable
    public String getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(String uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

}
