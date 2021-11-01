package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class ShareBindResult extends GsonBaseProtocol {
    @SerializedName("scanconfirm")
    public int scanconfirm;// 1为需要确认 0为不需要
    @SerializedName("domain")
    public String domain;
    @SerializedName("sn")
    public String sn;
//    public Result data;
//
//    @Keep
//    public static class Result {
//        public int scanconfirm; // 1为需要确认 0为不需要
//    }
}
