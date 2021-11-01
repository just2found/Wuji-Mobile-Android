package io.weline.internetdb.vo;

import androidx.annotation.Keep;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

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
    public String netId;
    public String netName;
    public int netStatus;
    public String ownerId;
    public String firstName;
    public String lastName;
    public String loginName;
    public int userStatus;
    public int userLevel;
    public long addTime;
    public long expireTime;
    public boolean srvProvide;


    public boolean isDevSepCharge;
    public int flowStatus;
}
