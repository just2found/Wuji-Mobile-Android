package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

@Keep
public class DownloadTokenResultBean extends GsonBaseProtocol {
    @SerializedName("downloadtoken")
    public String downloadtoken;
    @SerializedName("deviceid")
    public String deviceid;
}
