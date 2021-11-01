package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;

import java.util.List;

/**
 * Created by LSW on 2018/4/2.
 */
@Keep
public class DevicesLogs {

    public List<DeviceslogBean> deviceslog;

    @Keep
    public static class DeviceslogBean {
        /**
         * devicesn : Mdg2363djd13
         * ostype : 54
         * pwdhistory : ["123","321","5432t432"]
         */

        public String devicesn;
        public int ostype;
        public List<String> pwdhistory;
    }
}
