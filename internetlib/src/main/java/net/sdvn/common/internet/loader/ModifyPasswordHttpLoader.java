package net.sdvn.common.internet.loader;

import androidx.annotation.NonNull;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2018/3/28.
 */

public class ModifyPasswordHttpLoader extends V1AgApiHttpLoader {
//    ModifyPasswordRequestBody body;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public ModifyPasswordHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public static String encode(String password) {
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

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.body, "request body is null");
//        return v1AgapiServcie.request(getMap(), this.body);
//    }

    public void setParams(@NonNull String oldPwd, @NonNull String newPwd) {
//        body = new ModifyPasswordRequestBody();
//        body.action = "modifypassword";
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();
//        body.userid = CMAPI.getInstance().getBaseInfo().getUserId();
//        body.oldpassword = encode(oldPwd);
//        body.newpassword = encode(newPwd);

        bodyMap = new ConcurrentHashMap<>();
        put("action", "modifypassword");
        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("oldpassword", encode(oldPwd));
        put("newpassword", encode(newPwd));

    }
}
