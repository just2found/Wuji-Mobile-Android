package net.sdvn.common.internet.protocol;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;
@Keep
public class DataPages<T> {

    /**
     * total : 33
     * totalpage : 4
     * page : 1
     * data : [{"content":{"flow2mbpoint":1.4648E-4,"schemedate":1591604440,"delaytime":0,"devicename":"M8X2-4","type":"setmbpratio","mbpointratio":"6.666666666666GB","deviceid":"563104572243989","devicesn":"M8X2C3H0004"},"createtime":1591604440,"msgid":"4b7cdfe6-7082-4b37-8898-26b2f620dc4b_86","msgtype":1},{"content":{"flow2mbpoint":3.8297E-4,"schemedate":1591604397,"delaytime":0,"devicename":"M8X2C2T0002","type":"setmbpratio","mbpointratio":"2.55GB","deviceid":"563104572243988","devicesn":"M8X2C2T0002"},"createtime":1591604397,"msgid":"5537b613-b9e7-48e6-b0a8-e2c4a557deb0_86","msgtype":1},{"content":{"devicename":"M8X2C2D0003","type":"cancelmbpratio","deviceid":"563095982309384","devicesn":"M8X2C2D0003"},"createtime":1591604189,"msgid":"82eeb65b-8f03-49c6-b507-63567e8de7e1_86","msgtype":1},{"content":{"flow2mbpoint":9.8E-7,"schemedate":1591604167,"delaytime":0,"devicename":"M8X2C2D0003","type":"setmbpratio","mbpointratio":"1000GB","deviceid":"563095982309384","devicesn":"M8X2C2D0003"},"createtime":1591604167,"msgid":"277c5dcf-83da-4324-8ef4-9676f73b2c01_86","msgtype":1},{"content":{"flow2mbpoint":9.8E-7,"schemedate":1591604165,"delaytime":0,"devicename":"M8X2C2D0003","type":"setmbpratio","mbpointratio":"1000GB","deviceid":"563095982309384","devicesn":"M8X2C2D0003"},"createtime":1591604165,"msgid":"95bb960b-0b1f-4cbc-934c-2bee354214ff_86","msgtype":1},{"content":{"devicename":"M8X2C2D0003","type":"cancelmbpratio","deviceid":"563095982309384","devicesn":"M8X2C2D0003"},"createtime":1591603815,"msgid":"4dbb64ef-836d-49e8-8af4-94007c5cc4a5_86","msgtype":1},{"content":{"flow2mbpoint":4.8828E-4,"schemedate":1591603491,"delaytime":0,"devicename":"M8X2-4","type":"setmbpratio","mbpointratio":"2.0000001GB","deviceid":"563104572243989","devicesn":"M8X2C3H0004"},"createtime":1591603491,"msgid":"25168353-f124-480b-9ee2-653a587fffbe_86","msgtype":1},{"content":{"flow2mbpoint":9.8E-7,"schemedate":1591603471,"delaytime":0,"devicename":"M8X2C2D0003","type":"setmbpratio","mbpointratio":"1000GB","deviceid":"563095982309384","devicesn":"M8X2C2D0003"},"createtime":1591603471,"msgid":"4f62ee55-b0e6-46b7-801d-8b1800f06961_86","msgtype":1},{"content":{"flow2mbpoint":9.8E-7,"schemedate":1591603052,"delaytime":0,"devicename":"M8X2C2D0003","type":"setmbpratio","mbpointratio":"1000GB","deviceid":"563095982309384","devicesn":"M8X2C2D0003"},"createtime":1591603052,"msgid":"ffd57e33-d2af-42a9-ae5d-af6a39c10289_86","msgtype":1},{"content":{"default":"您绑定的设备[M8X2C2T0002]将于11:02 更改积分计费值为 每5GB流量消耗1积分"},"createtime":1591581722,"msgid":"4f15ecde-6e68-432a-9269-a45cb24c547f_86","msgtype":1}]
     */

    @SerializedName("total")
    private int total;
    @SerializedName("totalpage")
    private int totalPage;
    @SerializedName("page")
    private int page;
    @SerializedName("data")
    private List<T> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
