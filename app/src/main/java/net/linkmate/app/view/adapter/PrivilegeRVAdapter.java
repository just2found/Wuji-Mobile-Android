package net.linkmate.app.view.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.util.FormatUtils;
import net.linkmate.app.util.business.VIPDialogUtil;
import net.sdvn.common.internet.protocol.AccountPrivilegeInfo;

import java.util.List;

public class PrivilegeRVAdapter extends BaseQuickAdapter<AccountPrivilegeInfo.AdapterBean, BaseViewHolder> {

    public PrivilegeRVAdapter(@Nullable List<AccountPrivilegeInfo.AdapterBean> data) {
        super(R.layout.item_privilege, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, AccountPrivilegeInfo.AdapterBean data) {
        helper.setGone(R.id.ip_view_item_head, false);
        helper.setGone(R.id.ip_view_item_foot, false);
        if (helper.getLayoutPosition() == 0) {
            helper.setGone(R.id.ip_view_item_head, true);
        }
        if (helper.getLayoutPosition() == getItemCount() - 1) {
            helper.setGone(R.id.ip_view_item_foot, true);
        }

        String name = data.getName();
        if (TextUtils.isEmpty(name))
            name = mContext.getString(R.string.virtual_net_service);
        helper.setText(R.id.ip_tv_name, name)
                .setGone(R.id.layout_sn, !TextUtils.isEmpty(data.getSN()))
                .setText(R.id.ip_tv_sn, data.getSN())
                .setText(R.id.ip_tv_date, VIPDialogUtil.getDateString(data.getExpired()));

        helper.setGone(R.id.ip_tv_status, true);

        if (data.getStatus() == 0) {
            helper.setTextColor(R.id.ip_tv_status, mContext.getResources().getColor(R.color.light_green))
                    .setText(R.id.ip_tv_status, R.string.service_opened);
        } else if (data.getStatus() == 1) {
            helper.setTextColor(R.id.ip_tv_status, mContext.getResources().getColor(R.color.text_orange))
                    .setText(R.id.ip_tv_status, R.string.service_expiring);
        } else {
            helper.setTextColor(R.id.ip_tv_status, mContext.getResources().getColor(R.color.text_red))
                    .setText(R.id.ip_tv_status, R.string.service_expired);
        }

        boolean isBit = false;
        if (data.getUnits() != null) {
            if (data.getUnits().endsWith("b")) {
                isBit = true;
            }
        }
        helper.setGone(R.id.layout_flow_usable, data.getFlowUsable() != 0)
                .setText(R.id.ip_tv_flow_usable, FormatUtils.getSizeFormat(data.getFlowUsable(), isBit))
                .setGone(R.id.layout_flow_used, data.getFlowUsable() != 0)
                .setText(R.id.ip_tv_flow_used, FormatUtils.getSizeFormat(data.getFlowUsed(), isBit));
    }
}