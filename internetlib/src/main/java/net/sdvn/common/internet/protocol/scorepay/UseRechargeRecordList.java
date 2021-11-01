package net.sdvn.common.internet.protocol.scorepay;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class UseRechargeRecordList extends GsonBaseProtocol {

    /**
     * data : {"totalRow":2,"totalPage":1,"list":[{"orderno":"10031","orderid":"9605b319f1914432aec420d037310f49","totalfee":20,"createdate":1582766616000,"currency":"RMB","mbpoint":24,"orderstate":8,"ordername":"积分兑换5元档 X 4","paytype":"manual","producttype":"mbpoint"},{"orderno":"10030","orderid":"e8bf1ac4ee8a4855858c5fe642e9fb2a","totalfee":20,"createdate":1582766559000,"currency":"RMB","mbpoint":26,"orderstate":8,"ordername":"积分兑换10元档 X 2","paytype":"manual","producttype":"mbpoint"}]}
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
            public String orderid;
            public double totalfee;
            public long createdate;
            public String currency;
            public double mbpoint;
            public int orderstate;
            public String ordername;
            public String paytype;
            public String producttype;
        }
    }
}
