package net.linkmate.app.bean;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import net.sdvn.common.internet.protocol.flow.DevFlowDetailList;

import java.util.Objects;

public class DevFlowDetailsBean implements MultiItemEntity {

    public DevFlowDetailList.DataBean.ListBean data;
    public int itemType;

    public DevFlowDetailsBean(long time) {
        data = new DevFlowDetailList.DataBean.ListBean();
        data.billtime = time;
        this.itemType = 1;
    }

    public DevFlowDetailsBean(DevFlowDetailList.DataBean.ListBean data) {
        this.data = data;
        this.itemType = 2;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            DevFlowDetailsBean bean = (DevFlowDetailsBean) obj;
            //同一时间、同一用户在同一个设备上只会有一条数据
            return Objects.equals(this.itemType, bean.itemType) &&
                    Objects.equals(this.data.billtime, bean.data.billtime) &&
                    Objects.equals(this.data.userid, bean.data.userid) &&
                    Objects.equals(this.data.deviceid, bean.data.deviceid);
        } catch (Exception e) {
            return false;
        }
    }
}
