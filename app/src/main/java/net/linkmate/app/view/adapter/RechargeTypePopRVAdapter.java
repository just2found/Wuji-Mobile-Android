package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.util.business.RechargeTypeUtils;

public class RechargeTypePopRVAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {


    private int switchType;

    public void setType(int switchType) {
        this.switchType = switchType;
    }

    public RechargeTypePopRVAdapter() {
        super(R.layout.item_recharge_type);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final Integer type) {
        helper.getConvertView().setBackgroundResource(R.color.bg_white);
        helper.setText(R.id.irt_tv_type, RechargeTypeUtils.getTypeTextId(type))
        .setImageResource(R.id.irt_iv_type, RechargeTypeUtils.getTypeIconId(type));
        if (switchType == type) {
            helper.setImageResource(R.id.irt_iv_icon, R.drawable.image_selected);
        } else {
            helper.setImageResource(R.id.irt_iv_icon, R.drawable.ic_unselected);
        }
    }
}