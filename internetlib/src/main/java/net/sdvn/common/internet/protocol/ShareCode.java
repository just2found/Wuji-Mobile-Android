package net.sdvn.common.internet.protocol;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class ShareCode extends GsonBaseProtocol {

    /**
     * sharecode : 2f6sM5Tb
     */
    @SerializedName("sharecode")
    public String sharecode;
    @SerializedName("ttl")
    public long expireTime;//时效时间
}
