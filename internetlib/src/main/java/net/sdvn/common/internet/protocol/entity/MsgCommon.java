package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 *  
 * <p>
 * Created by admin on 2020/10/22,10:26
 */
@Keep
public class MsgCommon {

    /**
     * createtime : 1603273741
     * confirm_ack : 0 //需要确认 (int) 默认为0 无需确认 1确认
     * msgid : edc65626-52af-4960-8a97-8dc564d6b2c1_365
     * userid : 281560876056584
     * content : zk订购的【网络流量费】资源账单即将于1天后到期,请及时续费
     * confirm : 0
     * expired : 0
     * msg_class : billdue_notice
     * instime : 2020-10-21 17:49:01   入库时间
     * already_read : 0
     * msgtype : user
     */

    @SerializedName("createtime")
    private long createTime;
    @SerializedName("confirm_ack")
    private int confirmAck;
    @SerializedName("msgid")
    private String msgId;
    @SerializedName("userid")
    private String userId;
    @SerializedName("content")
    private String content;
    @SerializedName("confirm")
    private int confirm;
    @SerializedName("confirmtime")
    private long confirmTime;
    @SerializedName("expired")
    private int expired;
    @SerializedName("msg_class")
    private String msgClass;
    @SerializedName("instime")
    private String insTime;
    @SerializedName("already_read")
    private int alreadyRead;
    @SerializedName("msgtype")
    private String msgType;
    @SerializedName("param")
    private String params;
    @SerializedName("title")
    private String title;
    @SerializedName("read_time")
    private long readTime;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getConfirmAck() {
        return confirmAck;
    }

    public void setConfirmAck(int confirmAck) {
        this.confirmAck = confirmAck;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getConfirm() {
        return confirm;
    }

    public void setConfirm(int confirm) {
        this.confirm = confirm;
    }

    public int getExpired() {
        return expired;
    }

    public void setExpired(int expired) {
        this.expired = expired;
    }

    public String getMsgClass() {
        return msgClass;
    }

    public void setMsgClass(String msgClass) {
        this.msgClass = msgClass;
    }

    public String getInsTime() {
        return insTime;
    }

    public void setInsTime(String insTime) {
        this.insTime = insTime;
    }

    public int getAlreadyRead() {
        return alreadyRead;
    }

    public void setAlreadyRead(int alreadyRead) {
        this.alreadyRead = alreadyRead;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public long getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(long confirmTime) {
        this.confirmTime = confirmTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getReadTime() {
        return readTime;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }
}
