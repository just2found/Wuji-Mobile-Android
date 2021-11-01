package net.sdvn.common.internet.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.Local;
import net.sdvn.common.internet.presenter.V1AgApiService;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.internal.functions.ObjectHelper;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by yun on 2018/1/17.
 */

public abstract class V1AgApiHttpLoader extends HttpLoader {
    protected String strRandom;
    protected Map<String, Object> bodyMap;


    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public V1AgApiHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    /**
     * 获取加密sign
     *
     * @return
     */
    @NonNull
    protected String getSignEncrypt() {
        return this.strRandom = genRandomNum();
//        return encodeSHA_256(AppConstants.STR_APP_KEY + "=" + AppConstants.CONFIG_APPKEY + "&" + AppConstants.STR_RANDOM + "=" + strRandom);
    }

    @NonNull
    public static String genRandomNum() {
        int maxNum = 10;
        int i;
        int count = 0;
        String str = "qwertyupasdfghjkzxcvbnmQWERTYUPASDFGHJKLZXCVBNM0123456789";
        StringBuilder builder = new StringBuilder();
        Random r = new Random();
        while (count < 6) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length()) {
                builder.append(str.charAt(i));
                count++;
            }
        }
        return builder.toString();
    }


    @NonNull
    public static HashMap<String, String> getMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("partid", (CMAPI.getInstance().getPartnerId()));
        map.put("appid", (CMAPI.getInstance().getAppId()));
        map.put("random", genRandomNum());
        return map;
    }

    protected void put(String key, @Nullable Object obj) {
        if (bodyMap != null) {
            if (key != null && key.length() > 0 && obj != null) {
                bodyMap.put(key, obj);
            }
        }
    }

    @Override
    public Observable<ResponseBody> structureObservable(@NonNull Retrofit mRetrofit) {
        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
        ObjectHelper.requireNonNull(this.bodyMap, "request body is null");
        put("lg", Local.getApiLanguage());
        put("lang", Local.getApiLanguage());
        return v1AgapiServcie.request(getMap(), this.bodyMap);
    }
}
