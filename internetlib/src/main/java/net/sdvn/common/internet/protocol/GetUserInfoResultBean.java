package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class GetUserInfoResultBean extends GsonBaseProtocol {
    /**
     * data : {"firstname":"k","phone":"18676697923","loginname":"zk11","usercode":"868130","email":"zk11@qq.com","lastname":"z"}
     */

    public DataBean data;

    @Keep
    public static class DataBean {
        public String firstname;
        public String phone;
        public String loginname;
        public String usercode;
        public String email;
        public String lastname;
        public String nickname;
    }
}
