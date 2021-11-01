package net.sdvn.common.internet.protocol;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.protocol.entity.ShareUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class SharedUserList extends GsonBaseProtocol {

    @SerializedName("users")
    public List<ShareUser> users;
    @NotNull
    public int maxlimit;

    /* public static class ShareUser {
     *//**
     * datetime : 2017-12-12 10:02:30
     * email : ronin@ciernet.net
     * firstname : 张
     * lastname : 三
     * phone : 18973459283
     * userid : 563018672898052
     *//*

        public String datetime;
        public String email;
        public String firstname;
        public String lastname;
        public String phone;
        public String userid;
        public boolean isSelected;

        public String getFullName() {
            StringBuilder owner = new StringBuilder();
            if (!TextUtils.isEmpty(this.lastname))
                owner.append(this.lastname);
            if (!TextUtils.isEmpty(this.firstname)) {
                owner.append(" ").append(this.firstname);
            }
            return owner.toString();
        }
    }*/


}
