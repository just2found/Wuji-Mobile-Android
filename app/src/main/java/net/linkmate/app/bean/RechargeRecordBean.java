package net.linkmate.app.bean;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import net.sdvn.common.internet.protocol.scorepay.UseRechargeRecordList;

public class RechargeRecordBean implements MultiItemEntity {

    public UseRechargeRecordList.DataBean.ListBean data;
    public int itemType;

    public RechargeRecordBean(long time) {
        data = new UseRechargeRecordList.DataBean.ListBean();
        data.createdate = time;
        this.itemType = 1;
    }

    public RechargeRecordBean(UseRechargeRecordList.DataBean.ListBean data) {
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
            RechargeRecordBean bean = (RechargeRecordBean) obj;
            return this.data.orderid.equals(bean.data.orderid);
        }catch (Exception e){
            return false;
        }
    }
}
