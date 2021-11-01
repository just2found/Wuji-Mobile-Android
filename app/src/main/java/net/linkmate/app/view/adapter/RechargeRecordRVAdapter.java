package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.bean.RechargeRecordBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class RechargeRecordRVAdapter extends BaseMultiItemQuickAdapter<RechargeRecordBean, BaseViewHolder> {

    public RechargeRecordRVAdapter(@Nullable List<RechargeRecordBean> data) {
        super(data);
        addItemType(1, R.layout.item_recharge_record_title);
        addItemType(2, R.layout.item_recharge_record);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, RechargeRecordBean data) {
        if (data.getItemType() == 1) {
            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_title));
            helper.setText(R.id.irrt_tv_title, sf.format(data.data.createdate));
        } else if (data.getItemType() == 2) {
            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_item));
            String title = data.data.paytype;
            if ("weixin".equals(data.data.paytype.toLowerCase())) {
                title = mContext.getString(R.string.wepay);
            } else if ("alipay".equals(data.data.paytype.toLowerCase())) {
                title = mContext.getString(R.string.alipay);
            } else if ("paypal".equals(data.data.paytype.toLowerCase())) {
                title = mContext.getString(R.string.paypal);
            } else if ("applepay".equals(data.data.paytype.toLowerCase())) {
                title = mContext.getString(R.string.applepay);
            } else if ("manual".equals(data.data.paytype.toLowerCase())) {
                title = mContext.getString(R.string.manual);
            }
            title += " - " +
                    BigDecimal.valueOf(data.data.totalfee)
//                            .setScale(2, RoundingMode.FLOOR)
                            .stripTrailingZeros()
                            .toPlainString()
                    + " " + data.data.currency;
            helper.setText(R.id.irr_tv_time, sf.format(data.data.createdate))
                    .setText(R.id.irr_tv_no, data.data.orderno)
                    .setText(R.id.irr_tv_text, title)
                    .setText(R.id.irr_tv_score, mContext.getString(R.string.get_score) + " " +
                            BigDecimal.valueOf(data.data.mbpoint)
//                                    .setScale(2, RoundingMode.FLOOR)
                                    .stripTrailingZeros()
                                    .toPlainString());
        }
    }
}