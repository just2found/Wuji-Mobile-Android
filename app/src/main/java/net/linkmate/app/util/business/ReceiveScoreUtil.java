package net.linkmate.app.util.business;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.util.FormDialogUtil;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.FormRowLayout;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.ClaimDeviceHttpLoader;
import net.sdvn.common.internet.loader.GetDeviceClaimInfoHttpLoader;
import net.sdvn.common.internet.protocol.DeviceClaimInfo;

import java.util.ArrayList;
import java.util.List;


public class ReceiveScoreUtil {

    public static void showReceiveScoreDialog(Context context, String deviceid,
                                              HttpLoader.HttpLoaderStateListener stateListener) {
        getDeviceClaimInfo(context, deviceid, stateListener, new MyOkHttpListener<DeviceClaimInfo>() {
            @Override
            public void success(@Nullable Object tag, DeviceClaimInfo data) {
                if (data.data.list.size() <= 0) {
                    ToastUtils.showToast(R.string.have_received);
                    return;
                }
                List<FormRowLayout.FormRowDate> dates = new ArrayList<>();
                for (DeviceClaimInfo.DataBean.ListBean bean : data.data.list) {
                    dates.add(new FormRowLayout.FormRowDate(bean.title, bean.value));
                }
                FormDialogUtil.showSelectDialog(context, R.string.pls_confirm_list, dates,
                        R.string.receive, new FormDialogUtil.OnDialogButtonClickListener() {
                            @Override
                            public void onClick(View v, Dialog dialog) {
                                claimDevice(deviceid, stateListener, new MyOkHttpListener<DeviceClaimInfo>() {
                                    @Override
                                    public void success(@Nullable Object tag, DeviceClaimInfo data) {
                                        ToastUtils.showToast(R.string.receive_success);
                                        dialog.dismiss();
//                                        DialogUtil.showSelectDialog(context, R.string.do_you_need_to_transfer_scores,
//                                                R.string.yes, new DialogUtil.OnDialogButtonClickListener() {
//                                                    @Override
//                                                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
//                                                        context.startActivity(new Intent(context, ScoreTransferActivity.class));
//                                                        dialog.dismiss();
//                                                    }
//                                                },
//                                                R.string.cancel, new DialogUtil.OnDialogButtonClickListener() {
//                                                    @Override
//                                                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
//                                                        dialog.dismiss();
//                                                    }
//                                                });
                                    }
                                });
                            }
                        }, R.string.cancel, new FormDialogUtil.OnDialogButtonClickListener() {
                            @Override
                            public void onClick(View v, Dialog dialog) {
                                dialog.dismiss();
                            }
                        });
            }
        });
    }

    /**
     * 获取设备认领信息
     *
     * @param context
     * @param deviceid
     * @param stateListener  请求状态回调
     * @param resultListener 请求结果回调
     */
    public static void getDeviceClaimInfo(Context context, String deviceid,
                                          HttpLoader.HttpLoaderStateListener stateListener,
                                          ResultListener<DeviceClaimInfo> resultListener) {
        GetDeviceClaimInfoHttpLoader httpLoader = new GetDeviceClaimInfoHttpLoader(DeviceClaimInfo.class);
        httpLoader.setParams(context, deviceid);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }

    /**
     * 领取积分
     *
     * @param deviceid
     * @param stateListener  请求状态回调
     * @param resultListener 请求结果回调
     */
    public static void claimDevice(String deviceid,
                                   HttpLoader.HttpLoaderStateListener stateListener,
                                   ResultListener<DeviceClaimInfo> resultListener) {
        ClaimDeviceHttpLoader httpLoader = new ClaimDeviceHttpLoader(DeviceClaimInfo.class);
        httpLoader.setParams(deviceid);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }
}
