package net.sdvn.common.internet.protocol;


import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class FindUserResultBean extends GsonBaseProtocol {
    /**
     * data : {"firstname":"swl","nickname":"kk","userid":"281629595534405","email":"5920*****@qq.com"}
     */

    public DataBean data;

    @Keep
    public static class DataBean {
        /**
         * firstname : swl
         * nickname : kk
         * userid : 281629595534405
         * email : 5920*****@qq.com
         */

        public String firstname;
        public String loginname;
        public String nickname;
        public String userid;
        public String email;
        public String phone;
    }
}
