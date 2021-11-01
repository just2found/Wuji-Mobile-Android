package net.sdvn.common.data.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 *  
 * <p>
 * Created by admin on 2020/10/18,09:38
 */
@Keep
public class ApplyProvideSrvContent {

    /**
     * type : applyprovide
     * networkid : 844493649608755
     * networkname : 高清电影
     * deviceid : 563040147737735
     * devicename : E1
     * userid : 281569465991363
     * username : 张三
     * feeid : adfa123123bac
     * mbpoint : 100
     */

    @SerializedName("type")
    private String type;
    @SerializedName("networkid")
    private String networkid;
    @SerializedName("networkname")
    private String networkname;
    @SerializedName("deviceid")
    private String deviceid;
    @SerializedName("devicename")
    private String devicename;
    @SerializedName("userid")
    private String userid;
    @SerializedName("username")
    private String username;
    @SerializedName("feeid")
    private String feeid;
    @SerializedName("mbpoint")
    private int mbpoint;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNetworkid() {
        return networkid;
    }

    public void setNetworkid(String networkid) {
        this.networkid = networkid;
    }

    public String getNetworkname() {
        return networkname;
    }

    public void setNetworkname(String networkname) {
        this.networkname = networkname;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFeeid() {
        return feeid;
    }

    public void setFeeid(String feeid) {
        this.feeid = feeid;
    }

    public int getMbpoint() {
        return mbpoint;
    }

    public void setMbpoint(int mbpoint) {
        this.mbpoint = mbpoint;
    }
}
