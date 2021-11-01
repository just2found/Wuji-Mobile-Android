package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;
@Keep
public class ProcessNewsErrorResult extends GsonBaseProtocol {
    @SerializedName("process")
    public String process;
}
