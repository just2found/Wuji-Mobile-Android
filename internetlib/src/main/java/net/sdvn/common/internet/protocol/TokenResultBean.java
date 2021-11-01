package net.sdvn.common.internet.protocol;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;
public class TokenResultBean extends GsonBaseProtocol {
    @SerializedName("token")
    public String token;
}
