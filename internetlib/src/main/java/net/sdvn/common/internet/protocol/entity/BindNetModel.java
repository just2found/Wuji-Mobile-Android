package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.Local;

import java.util.List;

@Keep
public class BindNetModel {
    /**
     * networkid : 844493649608755
     * networkname : 高清电影
     * networkstatus : 0
     * devsepcharge : true
     * userstatus : 0
     * flowstatus : 0
     * uselevel : 2
     * srvprovide : true
     * addtime : 1597224897442
     * ownerid : xxx
     * firstname : xxx
     * lastname : xxxx
     * loginname : xxxx
     */

    @SerializedName("networkid")
    private String networkId;
    @SerializedName("networkname")
    private String networkName;
    @SerializedName("networkstatus")
    private int networkStatus;
    @SerializedName("devsepcharge")
    private boolean devSepCharge;
    @SerializedName("userstatus")
    private int userStatus;
    @SerializedName("flowstatus")
    private int flowStatus;
    @SerializedName("uselevel")
    private int userLevel;
    @SerializedName("srvprovide")
    private boolean srvProvide;
    @SerializedName("addtime")
    private long addTime;
    @SerializedName("ownerid")
    private String ownerId;
    @SerializedName("firstname")
    private String firstname;
    @SerializedName("lastname")
    private String lastname;
    @SerializedName("loginname")
    private String loginname;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("ischarge")
    private boolean isCharge;
    @SerializedName("srvmain")
    private List<String> serverMain;

    public List<String> getServerMain() {
        return serverMain;
    }

    public void setServerMain(List<String> serverMain) {
        this.serverMain = serverMain;
    }

    public String getMainENDeviceId() {
        return serverMain != null && serverMain.size() > 0 ? serverMain.get(0) : null;
    }


    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public int getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(int networkStatus) {
        this.networkStatus = networkStatus;
    }

    public boolean isDevSepCharge() {
        return devSepCharge;
    }

    public void setDevSepCharge(boolean devSepCharge) {
        this.devSepCharge = devSepCharge;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public int getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(int flowStatus) {
        this.flowStatus = flowStatus;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public boolean isSrvProvide() {
        return srvProvide;
    }

    public void setSrvProvide(boolean srvProvide) {
        this.srvProvide = srvProvide;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isCharge() {
        return isCharge;
    }

    public void setCharge(boolean charge) {
        isCharge = charge;
    }

    public String getFullName() {
        return Local.getLocalName(lastname, firstname);
    }
}