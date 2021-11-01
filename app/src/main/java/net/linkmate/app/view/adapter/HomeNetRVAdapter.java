package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.cmapi.Network;

import java.util.List;

public class HomeNetRVAdapter extends BaseQuickAdapter<Network, BaseViewHolder> {

    public HomeNetRVAdapter(@Nullable List<Network> data) {
        super(R.layout.item_home_network, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, Network data) {
        helper.setGone(R.id.ihn_view_item_head, false);
        helper.setGone(R.id.ihn_view_item_foot, false);
        if (helper.getLayoutPosition() == 0) {
            helper.setGone(R.id.ihn_view_item_head, true);
        }
        if (helper.getLayoutPosition() == getItemCount() - 1) {
            helper.setGone(R.id.ihn_view_item_foot, true);
        }
        helper.setText(R.id.ihn_tv_name, data.getName())
                .setText(R.id.ihn_tv_user, data.getOwner())
//                .setText(R.id.ihn_tv_hint,"")
                .setBackgroundRes(R.id.ihn_content,
                        data.isCurrent() ? R.drawable.bg_item_network_stroke : R.drawable.bg_item_network)
                .setGone(R.id.ihn_iv_checked, data.isCurrent());

        helper.addOnClickListener(R.id.ihn_content);
        helper.addOnClickListener(R.id.ihn_iv_setting);
    }
}