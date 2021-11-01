package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;

import java.util.List;

public class LoginUsersRVAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public LoginUsersRVAdapter(List<String> data) {
        super(R.layout.item_account_layout, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, String data) {
        helper.setText(R.id.item_account_tv_id, data)
                .addOnClickListener(R.id.item_account_tv_id)
                .addOnClickListener(R.id.item_account_ib_delete);
    }
}