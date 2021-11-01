package net.linkmate.app.view.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.common.vo.NetworkModel;

import java.util.List;

public class HomeNetModelRVAdapter extends BaseQuickAdapter<NetworkModel, BaseViewHolder> {

    public HomeNetModelRVAdapter(@Nullable List<NetworkModel> data) {
        super(R.layout.item_home_network, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, NetworkModel data) {
        helper.setGone(R.id.ihn_view_item_head, false);
        helper.setGone(R.id.ihn_view_item_foot, false);
        if (helper.getLayoutPosition() == 0) {
            helper.setGone(R.id.ihn_view_item_head, true);
        }
        if (helper.getLayoutPosition() == getItemCount() - 1) {
            helper.setGone(R.id.ihn_view_item_foot, true);
        }
        helper.setText(R.id.ihn_tv_name, data.netName)
                .setText(R.id.ihn_tv_user, TextUtils.isEmpty(data.nickname) ? data.loginName : data.nickname)

                .setBackgroundRes(R.id.ihn_content,
                        data.isCurrent ? R.drawable.bg_item_network_stroke : R.drawable.bg_item_network)
                .setGone(R.id.ihn_iv_checked, data.isCurrent);
        helper.setGone(R.id.ihn_tv_hint, false);
        if (data.isDevSepCharge == false) {
            if (data.flowStatus == 1) {
                helper.setGone(R.id.ihn_tv_hint, true);
                helper.setText(R.id.ihn_tv_hint, mContext.getString(R.string.flow_is_expired));
            } else if (data.flowStatus == -1) {
                helper.setGone(R.id.ihn_tv_hint, true);
                helper.setText(R.id.ihn_tv_hint, mContext.getString(R.string.not_purchase_circle_flow));
            }
        }
        if(data.userStatus == 1){//等待同意
            helper.setGone(R.id.ihn_tv_hint, true);
            helper.setText(R.id.ihn_tv_hint, mContext.getString(R.string.wait_for_consent));
        }

        helper.addOnClickListener(R.id.ihn_content);
        helper.addOnClickListener(R.id.ihn_iv_setting);
    }
}