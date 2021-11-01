package net.sdvn.common.internet.protocol;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.protocol.entity.HardWareDevice;

import java.util.List;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class HardWareInfo extends GsonBaseProtocol {
    @SerializedName("devices")
    public List<HardWareDevice> devices;

  /*  @Entity
    public static class HardWareDevice {
        *//**
     * datetime : 2017-12-12 10:02:30
     * deviceid : 563018672898052
     * devicename : C1
     * deviceid : Mdg2363djd13
     * devicetype : 50
     * firstname : 张
     * lastname : 三
     * ostype : 51
     * userid : 563018672898052
     *//*
        @Id
        private long id;
        public String datetime;
        public String deviceid;
        public String devicename;
        public String deviceid;
        public int devicetype;
        public String firstname;
        public String lastname;
        public String ostype;
        public String userid;
        @Transient
        public boolean online;

        public String getFullName() {
            StringBuilder owner = new StringBuilder();
            if (!TextUtils.isEmpty(lastname))
                owner.append(lastname);
            if (!TextUtils.isEmpty(firstname)) {
                owner.append(" ").append(firstname);
            }
            return owner.toString();
        }
    }
*/

}
