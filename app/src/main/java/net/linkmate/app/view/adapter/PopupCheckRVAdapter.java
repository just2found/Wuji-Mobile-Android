package net.linkmate.app.view.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.bean.DeviceBean;
import net.sdvn.nascommon.viewmodel.DeviceViewModel;

import java.util.List;
import java.util.Objects;

public class PopupCheckRVAdapter extends BaseQuickAdapter<DeviceBean, BaseViewHolder> {
    private DeviceViewModel mDeviceViewModel;
    private final String deviceid;

    public PopupCheckRVAdapter(@Nullable List<DeviceBean> data, String deviceid) {
        super(R.layout.item_popup_rv_check, data);
        this.deviceid = deviceid;
        mDeviceViewModel = new DeviceViewModel();
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, @NonNull DeviceBean data) {
        String id = data.getId();
        View view = helper.getView(R.id.iprc_tv);
        view.setTag(id);
        helper.setText(R.id.iprc_tv, data.getName());
        mDeviceViewModel.refreshDevNameById(id)
                .subscribe(s -> {
                    View view2 = helper.getView(R.id.iprc_tv);
                    if (view2 != null && Objects.equals(view2.getTag(), id)) {
                        helper.setText(R.id.iprc_tv, s);
                    }
                }, throwable -> {

                });
        helper.setVisible(R.id.iprc_iv, Objects.equals(deviceid, id));
    }
}