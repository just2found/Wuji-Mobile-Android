package net.sdvn.nascommon.model.oneos.transfer;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.fileserver.constants.EntityType;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

import java.util.Objects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Transient;
import io.objectbox.annotation.Unique;

/**
 * 内存中表示分享文件信息的bean类
 * <p>
 * ShareElem则是该类对象保存在DB中的精简数据
 */


@Keep
@Entity
public class ShareElement extends TransferElement {
    public static final String SHARE_TYPE_FILE = "0";
    public static final String SHARE_TYPE_FOLDER = "1";

    private long date;
    //分享者、被分享者、分享时间、文件名、文件大小、状态
    private String fileName;
    private String toName;//下载完成后的文件名(有重名修改机制)
    private String owner;
    private String ownerName;
    private String getter;
    private String getterName;
    @Unique
    private String shareToken;
    private String shareDate;
    private int shareState = STATE_ALL;
    //分享中、传输中、错误、已过期
//    public static final int STATE_ALL = 0;
//    public static final int STATE_SHARE_SHARING = 1;
//    public static final int STATE_SHARE_TRANSFERRING = 2;
//    public static final int STATE_SHARE_ERROR = 3;
//    public static final int STATE_SHARE_EXPIRED = 4;
//    //分享中、下载中、已完成、错误、已过期
//    public static final int STATE_RECV_SHARING = 5;
//    public static final int STATE_RECV_DOWNLOADING = 6;
//    public static final int STATE_RECV_COMPLETED = 7;
//    public static final int STATE_RECV_ERROR = 8;
//    public static final int STATE_RECV_EXPIRED = 9;
    @Transient
    public static final int STATE_ALL = 0;
    @Transient
    public static final int STATE_SHARE_SEND = 1;
    @Transient
    public static final int STATE_SHARE_RECEIVE = 2;
    @Transient
    public static final int STATE_SHARE_TRANSFERRING = 3;
    //分享中、下载中、已完成、错误、已过期
    @Transient
    public static final int STATE_SHARE_DOWNLOADING = 4;
    @Transient
    public static final int STATE_SHARE_COMPLETED = 5;
    @Transient
    public static final int STATE_SHARE_ERROR = 6;
    @Transient
    public static final int STATE_SHARE_EXPIRED = 7;
    @Transient
    public static final int STATE_SHARE_COPY = 8;
    @Transient
    public static final int TYPE_SHARE_SEND = EntityType.SHARE_FILE_V1_SEND;
    @Transient
    public static final int TYPE_SHARE_RECEIVE = EntityType.SHARE_FILE_V1_RECEIVE;
    @Transient
    public static final int TYPE_SHARE_COPY = EntityType.SHARE_FILE_V1_COPY;

    //下载的状态
//    private int downloadState;

//    public static final int NONE = 600;
//    public static final int WAIT = 601;
//    public static final int START_CHANNEL = 602;
//    public static final int PAUSE = 603;
//    public static final int FAILED = 604;

    //    private String downloadPath; //下载文件的目录
    private boolean downloadToPhone; //是否是下载到手机
    @Transient
    private String sourceIp;     //下载源ip
    private String sourceId;     //下载源设备id
    //    private String toID;         //目标设备id
    private String downloadToken;//下载的token
    @Transient
    private String dlErrorCode;  //下载失败的错误码（下载到设备）
    private int downloadedCount; //已下载次数
    private String type;
    //    private String path;
    private String fileDBId;
    private int shareType;

    public ShareElement() {
        super(0);
    }

    public ShareElement(ShareElement mElement) {
        super(mElement.id);
        this.id = (mElement.id);
        this.fileName = mElement.fileName;
        this.owner = mElement.owner;
        this.ownerName = mElement.ownerName;
        this.getterName = mElement.getterName;
        this.getter = mElement.getter;
        this.shareToken = mElement.shareToken;
        this.shareDate = mElement.shareDate;
        this.length = mElement.length;
        this.size = mElement.size;
        this.shareState = mElement.shareState;
        this.toPath = mElement.toPath;
        this.sourceId = mElement.sourceId;
        this.type = mElement.type;
        this.shareType = mElement.shareType;
        this.srcPath = mElement.srcPath;
        this.toName = mElement.toName;
        this.devId = mElement.devId;
        this.downloadToken = mElement.downloadToken;
        this.fileDBId = mElement.fileDBId;
        this.downloadToPhone = mElement.downloadToPhone;
        this.state = mElement.state;
        this.setTag(mElement.getTag());
        this.sourceIp = mElement.sourceIp;
        this.downloadedCount = mElement.downloadedCount;
        this.date = mElement.date;
        this.dlErrorCode = mElement.dlErrorCode;
    }

    public ShareElement(String oneOSFile, String sourceId, String toId, String toPath, long date, int shareType) {
        super(0);
        this.srcPath = oneOSFile;
        this.sourceId = sourceId;
        this.devId = toId;
        this.toPath = toPath;
        this.date = date;
        this.shareType = shareType;
    }

//    public ShareElement(CopyFile e) {
//        this(e.getId(), e.getShareToken(), e.getSrcPath(), e.getSourceId(), e.getToId(), e.getToPath(),
//                e.getFileName(), e.getToName(), e.getOwner(), e.getGetter(),
//                e.getOwnerName(), e.getGetterName(), e.getShareDate(), e.getLength(),
//                e.getFileSize(), e.getShareState(), e.getDownloadToken(), e.isDownloadToPhone(),
//                e.getType(), e.getFileDBId(), e.getShareType(), e.getState());
//    }

    public ShareElement(long id, String shareToken, String srcPath, String sourceId, String toId,
                        String toPath, String fileName, String toName, String owner, String getter,
                        String ownerName, String getterName, String shareDate, long length,
                        long fileSize, int shareState, String downloadToken, boolean downloadToPhone,
                        String type, String fileDBId, int shareType, TransferState state) {
        super(id);
        this.id = (id);
        this.fileName = fileName;
        this.owner = owner;
        this.ownerName = ownerName;
        this.getterName = getterName;
        this.getter = getter;
        this.shareToken = shareToken;
        this.shareDate = shareDate;
        this.length = length;
        this.size = fileSize;
        this.shareState = shareState;
        this.toPath = toPath;
        this.sourceId = sourceId;
        this.type = type;
        this.shareType = shareType;
        this.srcPath = srcPath;
        this.toName = toName;
        this.devId = toId;
        this.downloadToken = downloadToken;
        this.fileDBId = fileDBId;
        this.downloadToPhone = downloadToPhone;
        this.state = state;
    }

    public ShareElement(String name, String from, String fromName, String to, String toName,
                        String shareToken, String expire, long length, Long size, int state,
                        String downloadPath, String sourceId, String type, int shareType, String srcPath, String fileDBId) {
        super(0);
        this.fileName = name;
        this.owner = from;
        this.ownerName = fromName;
        this.getter = to;
        this.getterName = toName;
        this.shareToken = shareToken;
        this.shareDate = expire;
        this.length = length;
        this.size = size;
        this.shareState = state;
        this.toPath = downloadPath;
        this.sourceId = sourceId;
        this.type = type;
        this.shareType = shareType;
        this.srcPath = srcPath;
        this.fileDBId = fileDBId;

    }


    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getGetterName() {
        return getterName;
    }

    public void setGetterName(String getterName) {
        this.getterName = getterName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getOwner() {
        return owner;
    }

    public String getGetter() {
        return getter;
    }

    public String getShareToken() {
        return shareToken;
    }

    public String getShareDate() {
        return shareDate;
    }

    @Override
    public boolean isDownload() {
        return getShareType() == TYPE_SHARE_RECEIVE;
    }

    @Override
    public String getSrcPath() {
        return srcPath;
    }

    @Override
    public String getSrcName() {
        return fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    public long getFileSize() {
        return size;
    }

    public int getShareState() {
        return shareState;
    }

    public void setShareState(int shareState) {
        this.shareState = shareState;
    }

    public int getShareType() {
        return this.shareType;
    }


    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getToID() {
        return devId;
    }

    public void setToID(String toID) {
        this.devId = toID;
    }

    public String getDownloadToken() {
        return downloadToken;
    }

    public void setDownloadToken(String downloadtoken) {
        this.downloadToken = downloadtoken;
    }

    public boolean isDownloadToPhone() {
        return downloadToPhone;
    }

    public void setDownloadToPhone(boolean downloadToPhone) {
        this.downloadToPhone = downloadToPhone;
    }

    public String getDlErrorCode() {
        return dlErrorCode;
    }

    public void setDlErrorCode(String dlErrorCode) {
        this.dlErrorCode = dlErrorCode;
    }


    @NonNull
    public String getStateText() {
        int strResId = 0;
        switch (shareState) {
            case STATE_SHARE_SEND:
                strResId = R.string.sharing;
                break;
            case STATE_SHARE_TRANSFERRING:
                strResId = R.string.transferring;
                break;
            case STATE_SHARE_ERROR:
                strResId = R.string.error;
                break;
            case STATE_SHARE_EXPIRED:
                strResId = R.string.expired;
                break;
            case STATE_SHARE_RECEIVE:
                strResId = R.string.sharing;
                break;
            case STATE_SHARE_DOWNLOADING:
                if (state == TransferState.FAILED) {
                    strResId = R.string.failed;
                } else if (state == TransferState.WAIT) {
                    strResId = R.string.dl_waiting;
                } else if (state == TransferState.PAUSE) {
                    strResId = R.string.paused;
                } else if (state == TransferState.START) {
                    strResId = R.string.nas_downloading;
                }
                break;
            case STATE_SHARE_COMPLETED:
                strResId = R.string.completed;
                break;
            default:
                strResId = R.string.unknown_exception;
                break;
        }
        if (strResId != 0x0)
            return Utils.getApp().getResources().getString(strResId);
        return "";
    }

    public int getDownloadedCount() {
        return downloadedCount;
    }

    public void setDownloadedCount(int downloadedCount) {
        this.downloadedCount = downloadedCount;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ShareElement that = (ShareElement) o;
        return Objects.equals(shareToken, that.shareToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), shareToken);
    }

    @Override
    public String getTag() {
        return this.shareToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getFileDBId() {
        return fileDBId;
    }

    public void setFileDBId(String fileDBId) {
        this.fileDBId = fileDBId;
    }

    public void setShareType(int sType) {
        this.shareType = sType;
    }


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
