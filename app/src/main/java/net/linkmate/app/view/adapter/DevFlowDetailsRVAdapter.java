package net.linkmate.app.view.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.bean.DevFlowDetailsBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class DevFlowDetailsRVAdapter extends BaseMultiItemQuickAdapter<DevFlowDetailsBean, BaseViewHolder> {

    public DevFlowDetailsRVAdapter(@Nullable List<DevFlowDetailsBean> data) {
        super(data);
        addItemType(1, R.layout.item_dev_flow_detail_title);
        addItemType(2, R.layout.item_dev_flow_detail);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, DevFlowDetailsBean data) {
        if (data.getItemType() == 1) {
            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_title));

            String score = BigDecimal.valueOf(data.data.mbpoint)
                    .stripTrailingZeros()
                    .toPlainString();
            if (data.data.mbpoint == 0) {
                score = "...";
            }
            helper.setText(R.id.idfd_tv_title, sf.format(data.data.billtime))
                    .setGone(R.id.idfd_tr_valid, true)
                    .setText(R.id.idfd_tv_valid, data.data.bill_flow)
                    .setGone(R.id.idfd_tr_score, true)
                    .setText(R.id.idfd_tv_score, score)
                    .addOnClickListener(R.id.idfd_tv_title);
        } else if (data.getItemType() == 2) {
            helper.setGone(R.id.idfd_tv_flow_type, false)
                    .setGone(R.id.idfd_tv_flow_type, true);

            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_item2));

            //2021/04/26去掉＋
//            String score = "+" +BigDecimal.valueOf(data.data.mbpoint)
//                    .stripTrailingZeros()
//                    .toPlainString();
            String score = BigDecimal.valueOf(data.data.mbpoint)
                    .stripTrailingZeros()
                    .toPlainString();
            if (data.data.mbpoint == 0) {
                score = "...";
            }
            helper.setText(R.id.idfd_tv_time, sf.format(data.data.billtime))
                    .setText(R.id.idfd_tv_user, TextUtils.isEmpty(data.data.nickname) ? data.data.loginname : data.data.nickname)
                    .setText(R.id.idfd_tv_dev, data.data.devicename)
                    .setText(R.id.idfd_tv_flow_type, mContext.getString(R.string.upstream_traffic) + "：" + data.data.bill_flow)
                    .setTextColor(R.id.idfd_tv_flow, mContext.getResources().getColor(R.color.text_orange))
                    .setText(R.id.idfd_tv_flow, score);
        }
    }
}