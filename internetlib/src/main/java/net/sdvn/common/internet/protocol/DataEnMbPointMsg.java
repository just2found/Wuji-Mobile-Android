package net.sdvn.common.internet.protocol;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.protocol.entity.FlowMbpointRatioModel;

public class DataEnMbPointMsg {

    private long id;
    /**
     * content : {"flow2mbpoint":1.4648E-4,"schemedate":1591604440,"delaytime":0,"devicename":"M8X2-4","type":"setmbpratio","mbpointratio":"6.666666666666GB","deviceid":"563104572243989","devicesn":"M8X2C3H0004"}
     * createtime : 1591604440
     * msgid : 4b7cdfe6-7082-4b37-8898-26b2f620dc4b_86
     * msgtype : 1
     */

    @SerializedName("content")
    private FlowMbpointRatioModel content;
    @SerializedName("createtime")
    private long createtime;
    @SerializedName("msgid")
    private String msgid;
    @SerializedName("msgtype")
    private int msgtype;

    public FlowMbpointRatioModel getContent() {
        return content;
    }

    public void setContent(FlowMbpointRatioModel content) {
        this.content = content;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public int getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(int msgtype) {
        this.msgtype = msgtype;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}
