package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by LSW on 2018/4/18.
 */
@Keep
public class GetUserAppInfoResultBean extends GsonBaseProtocol {
    @SerializedName("info")
    public String info;
    @SerializedName("version")
    public int version;
}
