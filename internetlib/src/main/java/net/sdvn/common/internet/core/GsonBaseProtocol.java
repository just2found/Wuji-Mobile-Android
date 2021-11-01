package net.sdvn.common.internet.core;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


@Keep
public class GsonBaseProtocol {

    /**
     * result : 0
     * errmsg : success
     */
    @SerializedName("result")
    public int result = -1;
    @SerializedName("errmsg")
    public String errmsg;

    @NonNull
    @Override
    public String toString() {
        return "McsBaseProtocol{" +
                "result=" + result +
                ", errmsg='" + errmsg + '\'' +
                '}';
    }

    public boolean isSuccessful() {
        return result == 0;
    }

}
