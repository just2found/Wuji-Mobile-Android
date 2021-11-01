package io.weline.internetdb.vo;

import java.util.Objects;

import io.objectbox.annotation.BaseEntity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@BaseEntity
public abstract class MsgModel<T> {
    @Id
    private long id;
    @Unique
    private String msgId;
    private long timestamp;

    private String userId;

    private String type;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MsgModel)) return false;
        MsgModel that = (MsgModel) o;
        return Objects.equals(msgId, that.msgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId);
    }

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
}
