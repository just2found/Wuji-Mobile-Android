package net.linkmate.app.view.adapter;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.protocol.entity.NetMember;

public class NetUserMngRVAdapter extends BaseQuickAdapter<NetMember, BaseViewHolder> {


    public NetUserMngRVAdapter() {
        super(R.layout.item_user_mng);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final NetMember data) {
        String username = TextUtils.isEmpty(data.username) ? data.email : data.username;
        String name = data.firstname + " " + data.lastname + "(" + username + ")";
        helper.setText(R.id.ium_tv_user, name);
        if (isAdmin(data.userid)) {
            helper.setTextColor(R.id.ium_tv_user, mContext.getResources().getColor(R.color.text_light_gray));
        } else {
            helper.setTextColor(R.id.ium_tv_user, mContext.getResources().getColor(R.color.text_dark));
        }
        checkUser(data, helper);
        helper.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdmin(data.userid))
                    data.isSelected = !data.isSelected;
                checkUser(data, helper);
            }
        });
    }

    private void checkUser(NetMember data, BaseViewHolder helper) {
        if (data.isSelected && !isAdmin(data.userid)) {
            helper.setImageResource(R.id.ium_iv_icon, R.drawable.image_selected);
        } else {
            helper.setImageResource(R.id.ium_iv_icon, R.drawable.ic_unselected);
        }
    }

    private boolean isAdmin(String userid) {
        return CMAPI.getInstance().getBaseInfo().getUserId().equals(userid);
    }
}