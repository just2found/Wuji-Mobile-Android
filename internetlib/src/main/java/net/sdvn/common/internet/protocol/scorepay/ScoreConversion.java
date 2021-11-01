package net.sdvn.common.internet.protocol.scorepay;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class ScoreConversion extends GsonBaseProtocol {

    @SerializedName("data")
    public DataBean data;

    @Keep
    public static class DataBean {
        public List<ListBean> list;

        @Keep
        public static class ListBean {
            /**
             * mbpoint : 0
             * sku : rmb2mbp10
             */

            public double mbpoint;
            public double price;
            public String sku;
            public double original_price;
            public double original_mbpoint;
            public double reward_mbpoint;
            //数量修改模式  0-不允许修改 1-整数调整
            public int amountmode = 0;
        }
    }
}
