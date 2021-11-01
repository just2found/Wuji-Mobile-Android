package net.linkmate.app.view.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.bean.ScoreGetRecordBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class ScoreGetRecordRVAdapter extends BaseMultiItemQuickAdapter<ScoreGetRecordBean, BaseViewHolder> {

    public ScoreGetRecordRVAdapter(@Nullable List<ScoreGetRecordBean> data) {
        super(data);
        addItemType(1, R.layout.item_recharge_record_title);
        addItemType(2, R.layout.item_score_record);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ScoreGetRecordBean data) {
        if (data.getItemType() == 1) {
            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_title));
            helper.setText(R.id.irrt_tv_title, sf.format(data.data.billdate));
            if (data.stat != null) {
                helper.setGone(R.id.irrt_tv_tip, true)
                        .setGone(R.id.irrt_tv_totle, true)
                        .setText(R.id.irrt_tv_tip, R.string.get_score)
                        .setText(R.id.irrt_tv_totle, BigDecimal.valueOf(data.stat.in_mbp)
                                .stripTrailingZeros()
                                .toPlainString());
            } else {
                helper.setGone(R.id.irrt_tv_tip, false)
                        .setGone(R.id.irrt_tv_totle, false);
            }
        } else if (data.getItemType() == 2) {
            helper.setVisible(R.id.irr_tv_target, false)
                    .setGone(R.id.irr_tv_flow, false);

            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_item));
            String title = data.data.billtype;
            if ("buy".equals(data.data.billtype)) {
                title = mContext.getString(R.string.recharge);
            } else if ("devicebind".equals(data.data.billtype)) {
                title = mContext.getString(R.string.score_for_bound_devices);
                helper.setGone(R.id.irr_tv_target, true)
                        .setText(R.id.irr_tv_target, mContext.getString(R.string.device) + "：" + data.data.devicename);
            } else if ("transferin".equals(data.data.billtype)) {
                title = mContext.getString(R.string.score_transfer);
                helper.setGone(R.id.irr_tv_target, true)
                        .setText(R.id.irr_tv_target, mContext.getString(R.string.from_target) + data.data.username);
            } else if ("register".equals(data.data.billtype)) {
                title = mContext.getString(R.string.Bonus_score_of_register);
            } else if ("flowreward".equals(data.data.billtype)) {
                title = mContext.getString(R.string.reward_for_activities);
                helper.setGone(R.id.irr_tv_target, true)
                        .setText(R.id.irr_tv_target, mContext.getString(R.string.device) + "：" + data.data.devicename)
                        .setGone(R.id.irr_tv_flow, true)
                        .setText(R.id.irr_tv_flow, mContext.getString(R.string.flow_used) + "：" + data.data.flow);
            }else {
                if (!TextUtils.isEmpty(data.data.title))
                    title = data.data.title;
            }

            String score = "+" + BigDecimal.valueOf(data.data.mbpoint)
                    .stripTrailingZeros()
                    .toPlainString();
            if (data.data.mbpoint == 0) {
                score = "...";
            }
            helper.setText(R.id.irr_tv_time, sf.format(data.data.billdate))
                    .setText(R.id.irr_tv_text, title)
                    .setTextColor(R.id.irr_tv_amount, mContext.getResources().getColor(R.color.text_orange))
                    .setText(R.id.irr_tv_amount, score);
        }
    }
}