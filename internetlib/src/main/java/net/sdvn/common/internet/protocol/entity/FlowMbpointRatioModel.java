package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class FlowMbpointRatioModel {
    public enum Type {
        setmbpratio, cancelmbpratio
    }

    /**
     * flow2mbpoint : 1.4648E-4
     * schemedate : 1591604440
     * delaytime : 0
     * devicename : M8X2-4
     * type : setmbpratio
     * mbpointratio : 6.666666666666GB
     * deviceid : 563104572243989
     * devicesn : M8X2C3H0004
     */

    @SerializedName("flow2mbpoint")
    private double flow2mbpoint;
    @SerializedName("schemedate")
    private long schemedate;
    @SerializedName("delaytime")
    private int delaytime;
    @SerializedName("devicename")
    @Nullable
    private String devicename;
    @SerializedName("type")
    @Nullable
    private String type;
    @SerializedName("mbpointratio")
    @Nullable
    private String mbpointratio;
    @SerializedName("deviceid")
    @Nullable
    private String deviceid;
    @SerializedName("devicesn")
    @Nullable
    private String devicesn;
    @SerializedName("default")
    @Nullable
    private String defaultContent;
    @SerializedName("gb2cratio")
    private float gb2cRatio;

    public double getFlow2mbpoint() {
        return flow2mbpoint;
    }

    public void setFlow2mbpoint(double flow2mbpoint) {
        this.flow2mbpoint = flow2mbpoint;
    }

    public long getSchemedate() {
        return schemedate;
    }

    public void setSchemedate(long schemedate) {
        this.schemedate = schemedate;
    }

    public int getDelaytime() {
        return delaytime;
    }

    public void setDelaytime(int delaytime) {
        this.delaytime = delaytime;
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMbpointratio() {
        return mbpointratio;
    }

    public void setMbpointratio(String mbpointratio) {
        this.mbpointratio = mbpointratio;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getDevicesn() {
        return devicesn;
    }

    public void setDevicesn(String devicesn) {
        this.devicesn = devicesn;
    }

    public String getDefaultContent() {
        return defaultContent;
    }

    public void setDefaultContent(String defaultContent) {
        this.defaultContent = defaultContent;
    }

    public float getGb2cRatio() {
        return gb2cRatio;
    }

    public void setGb2cRatio(float gb2cRatio) {
        this.gb2cRatio = gb2cRatio;
    }

}