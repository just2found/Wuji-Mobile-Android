package net.sdvn.common.internet.loader;

import androidx.annotation.NonNull;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;


public class RegisterHttpLoader extends V1AgApiHttpLoader {
//    private RegisterRequestBody body;

    public RegisterHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

//    public void setParams(RegisterRequestBody body) {
//        this.body = body;
//    }

    /**
     * "action":"requestauxcode",
     * "countrycode":"86",
     * "phone":"13888888888",
     * <p>
     * 接口请求校验串,计算方法：
     * string strAppKey = “b6WI34lQgULVVP5ON5btX0xdKF4qKQ46”;
     * string strRand = “7226249334”;
     * string sig = sha256(appkey=$strAppKey&random=$strRand);
     */
    public void setParamsRequestAuxCode(String countryCode, @NonNull String phone) {
//        if (body == null) this.body = new RegisterRequestBody();
//        body.action = "requestauxcode";
//        body.countrycode = countryCode;
//        body.phone = phone;

        bodyMap = new ConcurrentHashMap<>();
        put("action", "requestauxcode");
        put("countrycode", countryCode);
        put("phone", phone);
    }

    /**
     * {
     * "action":"authauxcode",
     * "countrycode":"86",
     * "phone":"13888888888",
     * "auxcode":"658953",
     * "sig":"30db206bfd3fea7ef0db929998642c8ea54cc7042a779c5a0d9897358f6e9505"
     * }
     */
    public void setParamsAuthAuxCode(@NonNull String auxcode) {
        if (bodyMap == null)
            throw new IllegalStateException("请先调用请求验证码");
//        this.body.action = "authauxcode";
//        this.body.auxcode = auxcod;

        put("action", "authauxcode");
        put("auxcode", auxcode);
    }

    /**
     * {
     * "action":"accregister",
     * "countrycode":"86",
     * "phone":"13888888888",
     * "auxcode":"658953",
     * "nick":"张三",
     * "password":"123456",
     * "sig":"30db206bfd3fea7ef0db929998642c8ea54cc7042a779c5a0d9897358f6e9505"
     * }
     */
    public void setParamsAccRgister(String nickName, String password) {
        if (bodyMap == null)
            throw new IllegalStateException("请先调用验证");

//        this.body.action = "accregister";
//        body.nick = nickName;
//        body.password = password;

        put("action", "accregister");
        put("nick", nickName);
        put("password", password);

    }

//
//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.body, "request body is null");
////        body.sig = getSignEncrypt();
//        return v1AgapiServcie.request(getMap(), this.body);
//    }


    public void setParamsRequestAuxCode(@NonNull String email) {
        if (bodyMap == null)
            bodyMap = new ConcurrentHashMap<>();
        put("action", "requestauxcode");
        put("email", email);
    }

    public void setParamsAuthAuxCode(String countryCode, @NonNull String phone, @NonNull String auxcode) {
        if (bodyMap == null)
            bodyMap = new ConcurrentHashMap<>();
        put("action", "authauxcode");
        put("countrycode", countryCode);
        put("phone", phone);
        put("auxcode", auxcode);
    }

    public void setParamsAuthAuxCode(@NonNull String email, @NonNull String auxcode) {
        if (bodyMap == null)
            bodyMap = new ConcurrentHashMap<>();
        put("action", "authauxcode");
        put("email", email);
        put("auxcode", auxcode);
    }

    public void setParamsAccRgister(String countryCode, @NonNull String phone, @NonNull String auxcode,
                                    String nickName, String password) {
        if (bodyMap == null)
            bodyMap = new ConcurrentHashMap<>();
        put("action", "accregister");
        put("countrycode", countryCode);
        put("phone", phone);
        put("auxcode", auxcode);
        put("nick", nickName);
        put("password", password);
    }

    public void setParamsAccRgister(@NonNull String email, @NonNull String auxcode,
                                    String nickName, String password) {
        if (bodyMap == null)
            bodyMap = new ConcurrentHashMap<>();
        put("action", "accregister");
        put("email", email);
        put("auxcode", auxcode);
        put("nick", nickName);
        put("password", password);
    }


}
