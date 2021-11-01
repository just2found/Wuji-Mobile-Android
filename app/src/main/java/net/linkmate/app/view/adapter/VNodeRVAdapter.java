package net.linkmate.app.view.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.bean.VNodeBean;

import java.util.List;

public class VNodeRVAdapter extends BaseMultiItemQuickAdapter<VNodeBean, BaseViewHolder> {


    public VNodeRVAdapter(@Nullable List<VNodeBean> data) {
        super(data);
        addItemType(1, R.layout.item_user_title);
        addItemType(2, R.layout.item_user_mng);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, VNodeBean bean) {
        switch (bean.getItemType()) {
            case 1:
                holder.setText(R.id.txt_title, bean.getName());
                break;
            case 2:
                holder.setText(R.id.ium_tv_user, bean.getName())
                        .setImageResource(R.id.ium_iv_icon, bean.isSelected() ?
                                R.drawable.image_selected : R.drawable.ic_unselected);
                break;
        }
    }
}