package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LSW on 2018/4/18.
 */
@Keep
public class UserAppInfo {
    public List<String> pwdHistory;

    public UserAppInfo(@NonNull List<String> pwdHistory) {
        this.pwdHistory = new ArrayList<>();
        this.pwdHistory.addAll(pwdHistory);
    }

    public static UserAppInfo fromJson(String json) {
        return new Gson().fromJson(json, UserAppInfo.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
