package net.linkmate.app.view.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.common.internet.protocol.entity.ShareUser;

public class DevUserMngRVAdapter extends BaseQuickAdapter<ShareUser, BaseViewHolder> {


    public DevUserMngRVAdapter() {
        super(R.layout.item_user_mng);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final ShareUser data) {
        String username = TextUtils.isEmpty(data.username) ? data.email : data.username;
        String name = data.getFullName() + "(" + username + ")";
        helper.setText(R.id.ium_tv_user, name);
        helper.setGone(R.id.ium_iv_icon, false);
        if (data.mgrlevel == 0 || data.mgrlevel == 1) {
//            helper.setTextColor(R.id.ium_tv_user, mContext.getResources().getColor(R.color.text_light_gray));
            helper.setGone(R.id.iv_admin, true)
                    .setImageResource(R.id.iv_admin, data.mgrlevel == 0 ? R.drawable.icon_user_admin
                            : R.drawable.icon_user_master);
        } else {
//            helper.setTextColor(R.id.ium_tv_user, mContext.getResources().getColor(R.color.text_dark));
            helper.setGone(R.id.iv_admin, false);
        }
//        checkUser(data, helper);
//        helper.getConvertView().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (data.mgrlevel != 0 && data.mgrlevel != 1)
//                    data.isSelected = !data.isSelected;
//                checkUser(data, helper);
//            }
//        });
    }

//    private void checkUser(ShareUser data, BaseViewHolder helper) {
//        if (data.isSelected && data.mgrlevel != 0 && data.mgrlevel != 1) {
//            helper.setImageResource(R.id.ium_iv_icon, R.drawable.image_selected);
//        } else {
//            helper.setImageResource(R.id.ium_iv_icon, R.drawable.ic_unselected);
//        }
//    }
}