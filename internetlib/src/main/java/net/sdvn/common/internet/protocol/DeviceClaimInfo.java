package net.sdvn.common.internet.protocol;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class DeviceClaimInfo extends GsonBaseProtocol {

    /**
     * data : {"list":[{"key":"mbpoint","value":"100","title":"积分"}]}
     */

    @SerializedName("data")
    public DataBean data;

    @Keep
    public static class DataBean {
        @SerializedName("list")
        public List<ListBean> list;

        @Keep
        public static class ListBean {
            /**
             * key : mbpoint
             * value : 100
             * title : 积分
             */

            @SerializedName("key")
            public String key;
            @SerializedName("value")
            public String value;
            @SerializedName("title")
            public String title;
        }
    }
}
