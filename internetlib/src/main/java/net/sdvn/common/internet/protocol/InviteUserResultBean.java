package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by LSW on 2018/4/2.
 */
@Keep
public class InviteUserResultBean extends GsonBaseProtocol {
    @SerializedName("newuser")
    public boolean newuser;
}
