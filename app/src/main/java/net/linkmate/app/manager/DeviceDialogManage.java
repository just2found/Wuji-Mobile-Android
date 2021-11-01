package net.linkmate.app.manager;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentManager;

import net.linkmate.app.base.DevBoundType;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DevicelDetailActivity;
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper;
import net.linkmate.app.util.business.DeviceLocalDialogUtil;
import net.sdvn.nascommon.constant.AppConstants;

public class DeviceDialogManage {

    public static void showDeviceDetailDialog(Context context, int type, int position, DeviceBean bean, FragmentManager manager) {
//        if (bean.isVNode()) {
//            new DeviceVNodeDialogUtil(context, bean, position).showDetailDialog();
//        } else if (type == DevBoundType.ALL_BOUND_DEVICES) {
//            new DeviceDialogUtil(context, bean, position,manager).showDialog();
//        } else if (type == DevBoundType.LOCAL_DEVICES) {
//            new DeviceLocalDialogUtil(context, bean, position).showDetailDialog();
//        } else {
//            new DeviceDialogUtil(context, bean, position, manager).showDialog();
//        }
        if (bean.getTypeValue() == 3) {//云设备 直接进入状态
            Intent intent = new Intent(context, DevicelDetailActivity.class);
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, bean.getId());
            intent.putExtra(FunctionHelper.DEVICE_BOUND_TYPE, type);
            //bean无法子类序列化
            FunctionHelper.INSTANCE.setDeviceBeanTemp(bean);
            intent.putExtra(FunctionHelper.POSITION, position);
            intent.putExtra(FunctionHelper.FUNCTION, FunctionHelper.DEVICE_STATUS);
            context.startActivity(intent);

        } else if (type == DevBoundType.LOCAL_DEVICES) {
            new DeviceLocalDialogUtil(context, bean, position).showDetailDialog();
        } else {

            Intent intent = new Intent(context, DevicelDetailActivity.class);
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, bean.getId());
            intent.putExtra(FunctionHelper.DEVICE_BOUND_TYPE, type);
            //bean无法子类序列化
            FunctionHelper.INSTANCE.setDeviceBeanTemp(bean);
            intent.putExtra(FunctionHelper.POSITION, position);
            context.startActivity(intent);
        }
    }
}
