package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import net.sdvn.common.Local;
import net.sdvn.common.repo.AccountRepo;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Keep
public class ShareUser {
    /**
     * mgrlevel : 0
     * firstname : yun
     * datetime : 2018-03-29 13:28:56
     * phone : 18890365697
     * usercode : 100317946
     * userid : 281552286301859
     * lastname :
     * username : 18890365697
     */

    public int mgrlevel;
    public String firstname;
    public String datetime;
    public String phone;
    public int usercode;
    public String userid;
    public String lastname;
    public String username;
    public String nickname;//昵称
    public String email;
    public boolean isSelected;

    public String devMarkName;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @NonNull
    @Override
    public String toString() {
        return "ShareUser{" +
                "mgrlevel=" + mgrlevel +
                ", firstname='" + firstname + '\'' +
                ", datetime='" + datetime + '\'' +
                ", phone='" + phone + '\'' +
                ", usercode=" + usercode +
                ", userid='" + userid + '\'' +
                ", lastname='" + lastname + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }


    @NonNull
    public String getFullName() {
        return Local.getLocalName(lastname, firstname);
    }

    public boolean isOwner() {
        return mgrlevel == 0;
    }

    public boolean isAdmin() {
        return mgrlevel == 1;
    }

    public boolean isCurrent() {
        return Objects.equals(userid, AccountRepo.INSTANCE.getUserId());
    }

    @NotNull
    public boolean hasAdminRights() {
        return isOwner() || isAdmin();
    }
}