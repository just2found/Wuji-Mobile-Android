package net.linkmate.app.bean;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import net.sdvn.common.internet.protocol.scorepay.ScoreStat;
import net.sdvn.common.internet.protocol.scorepay.ScoreUseRecordList;

public class ScoreUsedRecordBean implements MultiItemEntity {

    public ScoreUseRecordList.DataBean.HistoryBean data;
    public ScoreStat.DataBean.ListBean stat;
    public int itemType;

    public ScoreUsedRecordBean(long time) {
        data = new ScoreUseRecordList.DataBean.HistoryBean();
        data.billdate = time;
        this.itemType = 1;
    }

    public ScoreUsedRecordBean(ScoreStat.DataBean.ListBean stat) {
        data = new ScoreUseRecordList.DataBean.HistoryBean();
        this.stat = stat;
        data.billdate = stat.billtime;
        this.itemType = 1;
    }

    public ScoreUsedRecordBean(ScoreUseRecordList.DataBean.HistoryBean data) {
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
        } catch (Exception e) {
            return false;
        }
    }
}
