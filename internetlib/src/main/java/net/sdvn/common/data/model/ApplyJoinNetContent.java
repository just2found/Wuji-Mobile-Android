package net.sdvn.common.data.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 *  
 * <p>
 * Created by admin on 2020/10/18,09:39
 */
@Keep
public class ApplyJoinNetContent {

    /**
     * type : applyjoin
     * networkid : 844493649608755
     * networkname : 高清电影
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
