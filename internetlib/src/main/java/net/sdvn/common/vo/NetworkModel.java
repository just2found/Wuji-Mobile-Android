package net.sdvn.common.vo;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import net.sdvn.common.Local;

import java.util.Objects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

/**
 *  
 * <p>
 * Created by admin on 2020/10/17,13:20
 */
@Keep
@Entity
public class NetworkModel {
//        "networkid":"844493649608755", //网络ID
//            "networkname":"高清电影", //网络名称
//            "networkstatus":0, //网络状态 0-正常 1-待审批 2-审批不通过 3-系统锁定            4-owner锁定
//// "ad":false,
//// "ad_suffix":"",
////拥有者信息
//            "ownerid":"xxx",
//            "firstname":"xxx",
//            "lastname":"xxxx",
//// "usercode": "xxxx",
//            "loginname":"xxxx",
//// "phone": "xxxx",
//// "email": "xxxx",
//            "usestatus":0, //使用状态 0-正常 1-待确认 3-系统锁定 4-owner锁定             5 - 使用到期
//            "uselevel":0, //使用级别 0-owner 1-manager 2-user
//            "addtime":1597224897442, //用户加入网络的时间戳,毫秒
//            "expiretime":1607224897442, //用户在网络中的过期时间戳,毫秒
//            "srvprovide":true, //用户是否在网络中提供服务

    @Id
    public long id;
    public String userId;
    public String netId;//网络ID
    public String netName;//网络名称
    public int netStatus = -1; //网络状态 0-正常 1-待审批 2-审批不通过 3-系统锁定
    public String ownerId;
    public String firstName;
    public String lastName;
    public String loginName;
    public String nickname;
    public int userStatus = -1; //使用状态 0-正常 1-待确认 3-系统锁定 4-owner锁定
    public int userLevel = -1;//使用级别 0-owner 1-manager 2-user
    public long addTime;//用户加入网络的时间戳,毫秒
    public long expireTime;
    public boolean srvProvide;//用户是否在网络中提供服务

    public boolean isCharge;
    public boolean isDevSepCharge; //网络设备是否单独收费
    public int flowStatus = -1;//流量状态 0-正常 1-已到期 -1-未订购
    @Nullable
    public String mainENDeviceId; //主EN deviceid

    @Transient
    public boolean isCurrent;

    public boolean isOwner() {
        return userLevel == 0;
    }

    public boolean isAdmin() {
        return userLevel == 1;
    }

    public boolean isWaitingForConsent() {
        return userStatus == 1;
    }


    public String getMainENDeviceId() {
        return mainENDeviceId;
    }

    public void setMainENDeviceId(String mainENDeviceId) {
        this.mainENDeviceId = mainENDeviceId;
    }

    public String getOwner() {
        return Local.getLocalName(lastName, firstName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkModel)) return false;
        NetworkModel that = (NetworkModel) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(netId, that.netId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, netId);
    }
}
