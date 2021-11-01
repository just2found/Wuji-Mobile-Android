package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;

import java.util.List;

public class RechargeCurrencyTypeRVAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private int num;

    public void setCheckNumber(int num) {
        this.num = num;
    }

    public RechargeCurrencyTypeRVAdapter(List<String> data) {
        super(R.layout.item_recharge_currency, data);

    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final String data) {
        helper.setText(R.id.irc_tv_type, data);
        if (num == helper.getAdapterPosition()) {
            helper.setTextColor(R.id.irc_tv_type, mContext.getResources().getColor(R.color.primary))
                    .setBackgroundRes(R.id.irc_content, R.drawable.bg_check_btn_stroke_full_radius_blue);
        } else {
            helper.setTextColor(R.id.irc_tv_type, mContext.getResources().getColor(R.color.text_gray))
                    .setBackgroundRes(R.id.irc_content, R.drawable.bg_check_btn_stroke_full_radius_gray);
        }
    }
}