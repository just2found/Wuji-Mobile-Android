package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.cmapi.CMAPI;

import java.util.Objects;

public class AccountPopRVAdapter extends BaseQuickAdapter<String, BaseViewHolder> {


    public AccountPopRVAdapter() {
        super(R.layout.item_user_mng);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final String data) {
        helper.setText(R.id.ium_tv_user, data);
        helper.getConvertView().setBackgroundResource(R.color.bg_white);
        checkUser(data, helper);
    }

    private void checkUser(String data, BaseViewHolder helper) {
        if (Objects.equals(data, CMAPI.getInstance().getBaseInfo().getAccount())) {
            helper.setImageResource(R.id.ium_iv_icon, R.drawable.image_selected);
        } else {
            helper.setImageResource(R.id.ium_iv_icon, R.drawable.ic_unselected);
        }
    }
}