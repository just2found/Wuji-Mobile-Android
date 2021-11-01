package net.sdvn.common.internet.protocol.flow;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class DevFlowDetailList extends GsonBaseProtocol {

    public DataBean data;

    @Keep
    public static class DataBean {

        public int totalRow;
        public int totalPage;
        public String lastVaildMonth;
        public List<ListBean> list;

        @Keep
        public static class ListBean {
            //  billdate:		//时间字符串
            //	mbpoint:		//收益积分
            //	total_flow:		//总流量
            //	bill_flow:		//计费流量
            //	free_flow:		//不计费流量

            //  billdate:		//时间字符串
            //	userid:			//使用用户ID
            //	loginname:	    //使用用户loginname
            //	phone:			//使用用户phone
            //	email:			//使用用户email
            //	deviceid:		//使用设备id
            //	devicename:	    //使用设备名称
            //	devicesn:		//使用设备序列号
            //	mbpoint:		//收益积分
            //	total_flow:		//总流量
            //	bill_flow:		//计费流量
            //	free_flow:		//不计费流量
            public long billtime;
            public double mbpoint;
            public String total_flow;
            public String bill_flow;
            public String free_flow;
            public String billdate;

            public String userid;
            public String loginname;
            public String nickname;
            public String phone;
            public String email;

            public String deviceid;
            public String devicename;
            public String devicesn;
        }
    }
}
