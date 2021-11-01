package net.sdvn.common.internet.protocol.scorepay;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class PayOrderInfo extends GsonBaseProtocol {

    @SerializedName("data")
    public DataBean data;

    @Keep
    public static class DataBean {
        public String result;
    }
}
