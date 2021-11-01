package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.Local;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Keep
public class HardWareDevice implements Serializable {
    private static final long serialVersionUID = -3438142043514154247L;

    /**
     * {
     * "comment": "",
     * "datetime": "2019-03-04 11:15:54",
     * "deviceclass": 1,
     * "deviceid": "563027262833225",
     * "devicename": "m3",
     * "devicesn": "SP03BV100000179A",
     * "devicetype": 50,
     * "enableshare": "false",
     * "firstname": "",
     * "lastname": "Cocan",
     * "mgrlevel": 2,  用户权限级别： 0: 主管理员 1: 普通管理员 2: 普通用户 3.待确认
     * "ostype": 72066,
     * "scanconfirm": 1,
     * "userid": "281552286201423"
     * "networks":["",""]
     * }
     */
    @SerializedName("datetime")
    private String datetime = null;
    @SerializedName("deviceid")
    private String deviceid = null;
    @SerializedName("devicename")
    private String devicename = null;
    @SerializedName("devicesn")
    private String devicesn = null;
    @SerializedName("deviceclass")
    private int deviceclass;
    @SerializedName("devicetype")
    private int devicetype;
    @SerializedName("firstname")
    private String firstname = null;
    @SerializedName("lastname")
    private String lastname = null;
    @SerializedName("nickname")
    private String nickname = null;
    @SerializedName("username")
    private String username = null;
    @SerializedName("ostype")
    private int ostype;
    @SerializedName("userid")
    private String userid = null;
    private boolean online;
    @NonNull
    @SerializedName("scanconfirm")
    private String scanconfirm = "0";
    @SerializedName("gainmbp_url")
    private String gainmbp_url = null;
    @NonNull
    @SerializedName("enableshare")
    private String enableshare = "false";
    @SerializedName("mgrlevel")
    private String mgrlevel = "-1";
    @SerializedName("comment")
    private String comment = null;
    @SerializedName("location")
    private String location = null;
    //所有绑定圈子 可见
    @SerializedName("networks")
    private List<String> networkIds = null;
    //当前所处圈子
    @SerializedName("networkid")
    private String networkId = null;
    private String status = null;//离线在线状态  offline/online

    /**
     * scanconfirm : 0
     * mgrlevel : 2
     * isen : false
     * mbpointratio :
     * maxMbpointratio :
     * changeRatioAble : false
     * minMbpointratio : 1.00GB
     * mbprationSchemevalue :
     * mbpratioSchemetime : 0
     */

    @SerializedName("isen")
    private boolean isEN;
    @SerializedName("mbpointratio")
    private String mbpointratio = null;
    @SerializedName("maxMbpointratio")
    private String maxMbpointratio = null;
    @SerializedName("changeRatioAble")
    private boolean changeRatioAble;
    @SerializedName("minMbpointratio")
    private String minMbpointratio = null;
    @SerializedName("mbprationSchemevalue")
    private String mbprationSchemevalue = null;
    @SerializedName("mbpratioSchemetime")
    private long mbpratioSchemetime;

    @SerializedName("gb2cratio")
    private float gb2cRatio;
    @SerializedName("maxGb2cratio")
    private float maxGb2cRatio;
    @SerializedName("minGb2cratio")
    private float minGb2cRatio;
    @SerializedName("gb2cratioSchemevalue")
    private float gb2cRatioSchemeValue;

    @SerializedName("srvprovide")
    private boolean srcProvide;
    // 2021/4/22 收费方式 1-使用者付费 2-拥有者付费 需存储字段
    private int chargetype = 1;

    /****－－－－2021/4/22 云设备新增字段 不需要存储字段－－－－****/
    private int devicestatus = 0;  //设备状态 1-正常 2-管理员停用 4-已到期 5-解绑停用 6-欠费停用 7-用户停用(New)，已到期状态不显示在客户端
    private Long createtime = null; //注册时间戳
    private String physdevicesn = null;//关联物理设备SN
    private BigDecimal calcvalue; //算力值
    private int calcmode = 0;////算力份数

    private boolean enable = false;//能否取消禁用

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /****－－－－云设备新增 end－－－－****/




    public int getChargetype() {
        return chargetype;
    }

    public void setChargetype(int chargetype) {
        this.chargetype = chargetype;
    }

    public int getDevicestatus() {
        return devicestatus;
    }

    public void setDevicestatus(int devicestatus) {
        this.devicestatus = devicestatus;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }

    public String getPhysdevicesn() {
        return physdevicesn;
    }

    public void setPhysdevicesn(String physdevicesn) {
        this.physdevicesn = physdevicesn;
    }

    public BigDecimal getCalcvalue() {
        return calcvalue;
    }

    public void setCalcvalue(BigDecimal calcvalue) {
        this.calcvalue = calcvalue;
    }

    public int getCalcmode() {
        return calcmode;
    }

    public void setCalcmode(int calcmode) {
        this.calcmode = calcmode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean isRealOnline() {//是否真实在线
        return status != null && status.equals("online");
    }

    public float getGb2cRatio() {
        return gb2cRatio;
    }

    public void setGb2cRatio(float gb2cRatio) {
        this.gb2cRatio = gb2cRatio;
    }

    public float getMaxGb2cRatio() {
        return maxGb2cRatio;
    }

    public void setMaxGb2cRatio(float maxGb2cRatio) {
        this.maxGb2cRatio = maxGb2cRatio;
    }

    public float getMinGb2cRatio() {
        return minGb2cRatio;
    }

    public void setMinGb2cRatio(float minGb2cRatio) {
        this.minGb2cRatio = minGb2cRatio;
    }

    public float getGb2cRatioSchemeValue() {
        return gb2cRatioSchemeValue;
    }

    public void setGb2cRatioSchemeValue(float gb2cRatioSchemeValue) {
        this.gb2cRatioSchemeValue = gb2cRatioSchemeValue;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
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

    public String getDevicesn() {
        return devicesn;
    }

    public void setDevicesn(String devicesn) {
        this.devicesn = devicesn;
    }

    public int getDeviceclass() {
        return deviceclass;
    }

    public void setDeviceclass(int deviceclass) {
        this.deviceclass = deviceclass;
    }

    public int getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(int devicetype) {
        this.devicetype = devicetype;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String nickname) {
        this.username = username;
    }

    public int getOstype() {
        return ostype;
    }

    public void setOstype(int ostype) {
        this.ostype = ostype;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    @Deprecated
    public boolean isOnline() {
        return online;
    }

    @Deprecated
    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isScanconfirm() {
        return Objects.equals(scanconfirm, "1");
    }

    public void setScanconfirm(boolean scanconfirm) {
        this.scanconfirm = scanconfirm ? "1" : "0";
    }

    public String getGainmbp_url() {
        return gainmbp_url;
    }

    public void setGainmbp_url(String gainmbp_url) {
        this.gainmbp_url = gainmbp_url;
    }

    @NonNull
    public String getOwner() {
        return Local.getLocalName(lastname, firstname);
    }

    public boolean getEnableshare() {
        return Objects.equals(enableshare, "true");
    }

    public void setEnableshare(boolean enableshare) {
        this.enableshare = enableshare ? "true" : "false";
    }

    public String getMgrlevel() {
        return mgrlevel;
    }

    public void setMgrlevel(String mgrlevel) {
        this.mgrlevel = mgrlevel;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isOwner() {
        return MGR_LEVEL.OWNER.equals(this.mgrlevel);
    }

    public boolean isCommon() {
        return MGR_LEVEL.COMMON.equals(this.mgrlevel);
    }

    public boolean isAdmin() {
        return MGR_LEVEL.ADMIN.equals(this.mgrlevel);
    }

    public boolean isUnconfirmed() {
        return MGR_LEVEL.UNCONFIRMED.equals(this.mgrlevel);
    }

    public boolean isEnableUseSpace() {
        return isOwner() || isAdmin() || isCommon();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getNetworkIds() {
        return networkIds;
    }

    public void setNetworkIds(List<String> networkIds) {
        this.networkIds = networkIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HardWareDevice)) return false;
        HardWareDevice that = (HardWareDevice) o;
        return deviceclass == that.deviceclass &&
                devicetype == that.devicetype &&
                ostype == that.ostype &&
                online == that.online &&
                isEN == that.isEN &&
                changeRatioAble == that.changeRatioAble &&
                mbpratioSchemetime == that.mbpratioSchemetime &&
                Float.compare(that.gb2cRatio, gb2cRatio) == 0 &&
                Float.compare(that.maxGb2cRatio, maxGb2cRatio) == 0 &&
                Float.compare(that.minGb2cRatio, minGb2cRatio) == 0 &&
                Float.compare(that.gb2cRatioSchemeValue, gb2cRatioSchemeValue) == 0 &&
                srcProvide == that.srcProvide &&
                Objects.equals(datetime, that.datetime) &&
                Objects.equals(deviceid, that.deviceid) &&
                Objects.equals(devicename, that.devicename) &&
                Objects.equals(devicesn, that.devicesn) &&
                Objects.equals(firstname, that.firstname) &&
                Objects.equals(lastname, that.lastname) &&
                Objects.equals(nickname, that.nickname) &&
                Objects.equals(username, that.username) &&
                Objects.equals(userid, that.userid) &&
                Objects.equals(scanconfirm, that.scanconfirm) &&
                Objects.equals(gainmbp_url, that.gainmbp_url) &&
                Objects.equals(enableshare, that.enableshare) &&
                Objects.equals(mgrlevel, that.mgrlevel) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(location, that.location) &&
                Objects.equals(networkIds, that.networkIds) &&
                Objects.equals(networkId, that.networkId) &&
                Objects.equals(mbpointratio, that.mbpointratio) &&
                Objects.equals(maxMbpointratio, that.maxMbpointratio) &&
                Objects.equals(minMbpointratio, that.minMbpointratio) &&
                Objects.equals(mbprationSchemevalue, that.mbprationSchemevalue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime, deviceid, devicename, devicesn, deviceclass, devicetype, firstname, lastname, nickname, username, ostype, userid, online, scanconfirm, gainmbp_url, enableshare, mgrlevel, comment, location, networkIds, networkId, isEN, mbpointratio, maxMbpointratio, changeRatioAble, minMbpointratio, mbprationSchemevalue, mbpratioSchemetime, gb2cRatio, maxGb2cRatio, minGb2cRatio, gb2cRatioSchemeValue, srcProvide);
    }

    public boolean isEN() {
        return isEN;
    }

    public void setEn(boolean isen) {
        this.isEN = isen;
    }

    public String getMbpointratio() {
        return mbpointratio;
    }

    public void setMbpointratio(String mbpointratio) {
        this.mbpointratio = mbpointratio;
    }

    public String getMaxMbpointratio() {
        return maxMbpointratio;
    }

    public void setMaxMbpointratio(String maxMbpointratio) {
        this.maxMbpointratio = maxMbpointratio;
    }

    public boolean isChangeRatioAble() {
        return changeRatioAble;
    }

    public void setChangeRatioAble(boolean changeRatioAble) {
        this.changeRatioAble = changeRatioAble;
    }

    public String getMinMbpointratio() {
        return minMbpointratio;
    }

    public void setMinMbpointratio(String minMbpointratio) {
        this.minMbpointratio = minMbpointratio;
    }

    public String getMbprationSchemevalue() {
        return mbprationSchemevalue;
    }

    public void setMbprationSchemevalue(String mbprationSchemevalue) {
        this.mbprationSchemevalue = mbprationSchemevalue;
    }

    public long getMbpratioSchemetime() {
        return mbpratioSchemetime;
    }

    public void setMbpratioSchemetime(long mbpratioSchemetime) {
        this.mbpratioSchemetime = mbpratioSchemetime;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public boolean isSrcProvide() {
        return srcProvide;
    }

    public void setSrcProvide(boolean srcProvide) {
        this.srcProvide = srcProvide;
    }
}