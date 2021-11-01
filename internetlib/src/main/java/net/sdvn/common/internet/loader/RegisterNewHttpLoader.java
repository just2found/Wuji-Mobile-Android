package net.sdvn.common.internet.loader;

import android.text.TextUtils;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;
@Deprecated
public class RegisterNewHttpLoader extends V2AgApiHttpLoader {

    public RegisterNewHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    //"loginname":"zhangsan", "phone":"13812345678", "email":"zhangsan@163.com",
    // "password":"fea260e356e385df4794b8c7479c0b11", "firstname":"张", "lastname":"三",
    // "nickname":"张三", "verifycode":"873459"
    public void setParams(String loginname, String phone, String email, String password, String firstname
            , String lastname, String nickname, String verifycode) {
        setAction("userregister");
        this.bodyMap = new ConcurrentHashMap<>();
        put("loginname", loginname);
        if (!TextUtils.isEmpty(phone)) {
            put("phone", phone);
        }
        if (!TextUtils.isEmpty(email)) {
            put("email", email);
        }
        put("password", password);
        put("firstname", firstname);
        put("lastname", lastname);
        put("nickname", nickname);
        put("verifycode", verifycode);
    }
}