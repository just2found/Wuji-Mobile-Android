package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class SnBindResult extends ShareBindResult {
    @SerializedName("deviceid")
    public String deviceid;
    @SerializedName("gainmbp_url")
    public String gainmbp_url;
}
