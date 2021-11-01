package net.sdvn.common.internet.protocol.scorepay;


import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class ScoreStat extends GsonBaseProtocol {

    public DataBean data;

    @Keep
    public static class DataBean {

        public int totalRow;
        public int totalPage;
        public List<ListBean> list;

        @Keep
        public static class ListBean {

            public double out_mbp;
            public long billtime;
            public double in_mbp;
            public String billdate;
        }
    }
}
