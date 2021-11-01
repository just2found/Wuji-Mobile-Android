package net.sdvn.common.internet.loader;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;
import net.sdvn.common.internet.presenter.V1AgApiService;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by LSW on 2019/9/23.
 * 设置用户昵称
 */

public class SetNickHttpLoader extends V1AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public SetNickHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(@NonNull String ticket, String firstname, String lastname, String nickname) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", ticket);
        if (!TextUtils.isEmpty(firstname)) {
            bodyMap.put("firstname", firstname);
            if (!TextUtils.isEmpty(lastname)) {
                bodyMap.put("lastname", lastname);
            }
        } else {
            put("nickname", nickname);
        }
    }

    @Override
    public Observable<ResponseBody> structureObservable(@NonNull Retrofit mRetrofit) {
        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
        return v1AgapiServcie.requestV2("setnick", getMap(), this.bodyMap);
    }
}
