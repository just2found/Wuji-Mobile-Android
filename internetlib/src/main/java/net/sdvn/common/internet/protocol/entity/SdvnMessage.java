package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;

@Keep
public class SdvnMessage {
    public static final String MESSAGE_STATUS_WAIT = "0";
    public static final String MESSAGE_STATUS_AGREE = "1";
    public static final String MESSAGE_STATUS_DISAGREE = "2";

    public static final String APPLY2NET = "apply2net";
    public static final String INVITE2NET = "invite2net";
    public static final String BIND_DEV = "bind_dev";
    public static final String BIND_MGR = "bind_mgr";

    /**
     * username : Chen Shuo
     * newsId : 6f5131a14631419b84b6cdc5b5d9934b
     * message : Chen Shuo(179500885@qq.com) 申请加入网络 tnet001
     * status : 0
     * type : apply2net
     * date : 2018-09-27 10:15:47.0
     */
    public String username;
    public String newsid;
    public String message;
    public String status; //0:等待  1:接受  2:拒绝
    public String type;
    public String date;
    public long timestamp;

    public boolean isSelect;

    public SdvnMessage(String username, String newsId, String message, String status, String type, String date, long timestamp) {
        this.username = username;
        this.newsid = newsId;
        this.message = message;
        this.status = status;
        this.type = type;
        this.date = date;
        this.timestamp = timestamp;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNewsid() {
        return newsid;
    }

    public void setNewsid(String newsid) {
        this.newsid = newsid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
