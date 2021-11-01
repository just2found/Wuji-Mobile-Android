package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.bean.AppBean;

import java.util.List;

public class AppRVAdapter extends BaseQuickAdapter<AppBean, BaseViewHolder> {

    public AppRVAdapter(@Nullable List<AppBean> data) {
        super(R.layout.item_me_more_app, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, AppBean bean) {
        helper.setImageResource(R.id.item_iv_app_icon, bean.iconRes)
                .setText(R.id.item_tv_app_name, bean.name);
    }
}
