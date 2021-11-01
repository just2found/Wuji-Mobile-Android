package net.linkmate.app.bean;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import net.sdvn.common.internet.protocol.scorepay.ScoreGetRecordList;
import net.sdvn.common.internet.protocol.scorepay.ScoreStat;

public class ScoreGetRecordBean implements MultiItemEntity {

    public ScoreGetRecordList.DataBean.ListBean data;
    public ScoreStat.DataBean.ListBean stat;
    public int itemType;

    public ScoreGetRecordBean(long time) {
        data = new ScoreGetRecordList.DataBean.ListBean();
        data.billdate = time;
        this.itemType = 1;
    }

    public ScoreGetRecordBean(ScoreStat.DataBean.ListBean stat) {
        data = new ScoreGetRecordList.DataBean.ListBean();
        this.stat = stat;
        data.billdate = stat.billtime;
        this.itemType = 1;
    }

    public ScoreGetRecordBean(ScoreGetRecordList.DataBean.ListBean data) {
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
            ScoreGetRecordBean bean = (ScoreGetRecordBean) obj;
            return this.data.billid.equals(bean.data.billid);
        }catch (Exception e){
            return false;
        }
    }
}
