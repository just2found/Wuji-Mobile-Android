package net.sdvn.common.internet.protocol.scorepay;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class ScoreUseRecordList extends GsonBaseProtocol {

    /**
     * data : {"totalRow":3,"current":[],"totalPage":1,"history":[{"billtype":"transferout","billid":"6","billdate":1581665965000,"mbpoint":-11.11,"vnodename":"测试节点","vnodeid":"f1e5202b3558485aa4552e966bcbdc87","flow":"11.77MB"},{"billtype":"vnodeapply","vnodename":"测试节点","billid":"4","billdate":1580369800000,"mbpoint":-10,"vnodeid":"f1e5202b3558485aa4552e966bcbdc87"},{"billtype":"vnodeflow","vnodename":"VNodeTest-广州","billid":"5","billdate":1577808000000,"mbpoint":-24.5,"vnodeid":"711eb45bb863462e943fb63e6d06d733","flow":"11.77MB"}]}
     */

    public DataBean data;

    @Keep
    public static class DataBean {

        public int totalRow;
        public int totalPage;
        public List<HistoryBean> current;
        public List<HistoryBean> history;

        @Keep
        public static class HistoryBean {

            public String orderno;
            public String billid;
            public long billdate;
            public String billtype;
            public String title;
            public String devicename;
            public String vnodeid;
            public String networkname;
            public String networkid;
            public String flow;
            public double mbpoint;
            public String username;
        }
    }
}
