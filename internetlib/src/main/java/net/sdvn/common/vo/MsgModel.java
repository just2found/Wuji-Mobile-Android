package net.sdvn.common.vo;

import androidx.annotation.Keep;

import io.objectbox.annotation.BaseEntity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Keep
@BaseEntity
public abstract class MsgModel<T> {
    @Id
    private long id;
    private String msgId;
    private long timestamp;
    private String title;
    private String userId;
    private String msgType;
    private String type;
    public boolean expired;
    @Transient
    public static final int CONFIRM_WITH_PWD = 2;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getConfirm() {
        return confirm;
    }

    public void setConfirm(int confirm) {
        this.confirm = confirm;
    }

    public int getConfirmAck() {
        return confirmAck;
    }

    public void setConfirmAck(int confirmAck) {
        this.confirmAck = confirmAck;
    }

    public long getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(long confirmTime) {
        this.confirmTime = confirmTime;
    }

    private int confirm;
    private int confirmAck;
    private long confirmTime;
    private boolean wasRead;

    private boolean display = true;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public abstract T getContent();

    public abstract void setContent(T content);

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isWasRead() {
        return wasRead;
    }

    public void setWasRead(boolean wasRead) {
        this.wasRead = wasRead;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public boolean isNeedConfirm() {
        return confirm >= 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
