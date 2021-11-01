package net.sdvn.common.internet.loader;

import androidx.annotation.NonNull;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2018/3/28.
 */

public class ForgetPasswordHttpLoader extends V1AgApiHttpLoader {
//    ForgetPasswordRequestBody body;

    public ForgetPasswordHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParamsRequestResetPwdAuxCode(String countryCode, @NonNull String phone) {
        bodyMap = new ConcurrentHashMap<>();
        put("action", "applyresetpwdauxcode");
        put("nationcode", countryCode);
        put("phone", phone);
    }

    public void setParamsRequestResetPwd(@NonNull String loginname, @NonNull String auxcode, @NonNull String password) {
        bodyMap = new ConcurrentHashMap<>();
        put("action", "resetpassword");
        put("loginname", loginname);
        put("newpassword", encode(password));
        put("auxcode", auxcode);
    }


    public void setParamsRequestResetPwdAuxCode(@NonNull String username) {
        bodyMap = new ConcurrentHashMap<>();
        put("action", "applyresetpwdauxcode");
        put("email", username);
    }


    private String encode(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(password.getBytes());
            //将结果转换成十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                int number = (b & 0xff);
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //can't reach
            return "";
        }
    }
}
