package net.linkmate.app.view.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.bean.ScoreUsedRecordBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class ScoreUsedRecordRVAdapter extends BaseMultiItemQuickAdapter<ScoreUsedRecordBean, BaseViewHolder> {

    public ScoreUsedRecordRVAdapter(@Nullable List<ScoreUsedRecordBean> data) {
        super(data);
        addItemType(1, R.layout.item_recharge_record_title);
        addItemType(2, R.layout.item_score_record);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ScoreUsedRecordBean data) {
        if (data.getItemType() == 1) {
            if (data.data.billdate < 0) {
                helper.setText(R.id.irrt_tv_title, mContext.getString(R.string.current_month_data))
                        .setGone(R.id.irrt_tv_tip, false)
                        .setGone(R.id.irrt_tv_totle, false);
            } else {
                SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_title));
                helper.setText(R.id.irrt_tv_title, sf.format(data.data.billdate));
                if (data.stat != null) {
                    helper.setGone(R.id.irrt_tv_tip, true)
                            .setGone(R.id.irrt_tv_totle, true)
                            .setText(R.id.irrt_tv_tip, R.string.used_score)
                            .setText(R.id.irrt_tv_totle, BigDecimal.valueOf(data.stat.out_mbp)
                                    .stripTrailingZeros()
                                    .toPlainString());
                } else {
                    helper.setGone(R.id.irrt_tv_tip, false)
                            .setGone(R.id.irrt_tv_totle, false);
                }
            }
        } else if (data.getItemType() == 2) {
            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_item));
//            String vnode = TextUtils.isEmpty(data.data.vnodeid) ? "" : " - " + data.data.vnodeid;

            helper.setVisible(R.id.irr_tv_target, false)
                    .setGone(R.id.irr_tv_flow, false);
            String title = mContext.getString(R.string.vnodeapply);
            if ("vnodeflow".equals(data.data.billtype)) {
                title = mContext.getString(R.string.vnodeflow);
                helper.setGone(R.id.irr_tv_target, true)
                        .setText(R.id.irr_tv_target, mContext.getString(R.string.device) + "：" + data.data.devicename)
                        .setGone(R.id.irr_tv_flow, true)
                        .setText(R.id.irr_tv_flow, mContext.getString(R.string.flow_used) + "：" + data.data.flow);
                sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_item2));
            } else if ("vnodeapply".equals(data.data.billtype)) {
                title = mContext.getString(R.string.vnodeapply);
            } else if ("transferout".equals(data.data.billtype)) {
                title = mContext.getString(R.string.score_transfer);
                helper.setGone(R.id.irr_tv_target, true)
                        .setText(R.id.irr_tv_target, mContext.getString(R.string.to_target) + data.data.username);
            } else {
                if (!TextUtils.isEmpty(data.data.title))
                    title = data.data.title;
            }

            String score = BigDecimal.valueOf(data.data.mbpoint)
                    .stripTrailingZeros()
                    .toPlainString();
            if (data.data.mbpoint == 0) {
                score = "...";
            }
            helper.setText(R.id.irr_tv_time, data.data.billdate > 0 ?
                    sf.format(data.data.billdate) :
                    mContext.getString(R.string.current_month_data))
                    .setText(R.id.irr_tv_text, title)
                    .setText(R.id.irr_tv_amount, data.data.mbpoint > 0 ? "-" + score : score);
        }
    }
}