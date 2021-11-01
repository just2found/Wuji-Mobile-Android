package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.cmapi.Device;

public class SubnetRVAdapter extends BaseQuickAdapter<Device.SubNet, BaseViewHolder> {


    public SubnetRVAdapter() {
        super(R.layout.item_subnet);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final Device.SubNet data) {
        helper.setText(R.id.is_ip, data.net)
                .setText(R.id.is_mask, data.mask);
    }
}