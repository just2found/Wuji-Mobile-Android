package net.sdvn.common.internet.protocol.scorepay;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class ScoreGetRecordList extends GsonBaseProtocol {

    /**
     * data : {"totalRow":3,"totalPage":1,"list":[{"billtype":"transferin","billid":"2","billdate":1581492744000,"mbpoint":20.34},{"billtype":"buy","billid":"1","billdate":1581060469000,"mbpoint":100},{"billtype":"devicebind","billid":"3","billdate":1578555294000,"mbpoint":50}]}
     */

    public DataBean data;

    @Keep
    public static class DataBean {

        public int totalRow;
        public int totalPage;
        public List<ListBean> list;

        @Keep
        public static class ListBean {

            public String orderno;
            public String billid;
            public long billdate;
            public double mbpoint;
            public String billtype;
            public String title;
            public String paytype;
            public String username;
            public String devicename;
            public String vnodeid;
            public String networkname;
            public String networkid;
            public String flow;
        }
    }
}
