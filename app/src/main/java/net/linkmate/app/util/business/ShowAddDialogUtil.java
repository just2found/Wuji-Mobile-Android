package net.linkmate.app.util.business;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.data.model.CircleDetail;
import net.linkmate.app.data.remote.CircleRemoteDataSource;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.ui.activity.circle.CreateCircleActivity;
import net.linkmate.app.ui.activity.circle.JoinCircleActivity;
import net.linkmate.app.ui.fragment.main.HomeFragment;
import net.linkmate.app.util.AddPopUtil;
import net.linkmate.app.util.ToastUtils;
import net.sdvn.common.internet.protocol.ShareBindResult;
import net.sdvn.nascommon.utils.AnimUtils;

public class ShowAddDialogUtil {

    public static void showAddDialog(final Context context, final int showNum) {
        final View dialogView = View.inflate(context, R.layout.dialog_edit, null);
        final Dialog mDialog = new Dialog(context, R.style.DialogTheme);
        final TextView tvTitle = dialogView.findViewById(R.id.txt_title);
        final EditText etContent = dialogView.findViewById(R.id.et_content);
        final TextView tvContent = dialogView.findViewById(R.id.tv_content);
        final LinearLayout llExtra = dialogView.findViewById(R.id.layout_extra);
        final TextView btnExtra = dialogView.findViewById(R.id.btn_extra);
        final boolean[] isExtra = {false};
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvContent.setVisibility(etContent.getText().length() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        switch (showNum) {
            case AddPopUtil.SHOW_ADD_NET:
                tvTitle.setText(R.string.add_share_circle);
                tvContent.setText(R.string.pls_input_share_code);
                llExtra.setVisibility(View.VISIBLE);
                btnExtra.setText(R.string.create_circle);
                btnExtra.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        synchronized (HomeFragment.class) {
                            context.startActivity(new Intent(context, CreateCircleActivity.class));
                            mDialog.dismiss();
//                            if (isExtra[0]) {
//                                tvTitle.setText(R.string.add_share_net);
//                                tvContent.setHint(R.string.pls_input_share_code);
//                                btnExtra.setText(R.string.create_new_net);
//                            } else {
//                                tvTitle.setText(R.string.create_new_net);
//                                tvContent.setHint(R.string.pls_input_net_name);
//                                btnExtra.setText(R.string.add_share_net);
//                            }
//                            isExtra[0] = !isExtra[0];

                        }
                    }
                });
                break;
            case AddPopUtil.SHOW_ADD_DEV:
                tvTitle.setText(R.string.add_share_device);
                tvContent.setHint(R.string.pls_input_share_code);
                llExtra.setVisibility(View.GONE);
                break;
//            case AddPopUtil.SHOW_ADD_CIRCLE:
//                tvTitle.setText(R.string.add_share_circle);
//                tvContent.setText(R.string.pls_input_share_code);
//                llExtra.setVisibility(View.VISIBLE);
//                btnExtra.setText(R.string.create_circle);
//                btnExtra.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        context.startActivity(new Intent(context, CreateCircleActivity.class));
//                        mDialog.dismiss();
//                    }
//                });
//                break;
        }

        TextView positiveBtn = dialogView.findViewById(R.id.positive);
        positiveBtn.setText(R.string.confirm);
        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = etContent.getText().toString().trim();
                if (TextUtils.isEmpty(str)) {
                    AnimUtils.sharkEditText(context, etContent);
                    AnimUtils.sharkEditText(context, tvContent);
                    return;
                }
                switch (showNum) {
                    case AddPopUtil.SHOW_ADD_NET:
//                        if (isExtra[0]) {
                        CircleRemoteDataSource remoteDataSource = new CircleRemoteDataSource();
                        remoteDataSource.getCircleDetial(null, str, null, new MyOkHttpListener<CircleDetail>() {
                            @Override
                            public void success(@Nullable Object tag, CircleDetail data) {
                                context.startActivity(new Intent(context, JoinCircleActivity.class)
                                        .putExtra("shareCode", str)
                                );
                            }
                        });
//                            NetManagerUtil.createNet(str, null, new MyOkHttpListener() {
//                                @Override
//                                public void success(Object tag, GsonBaseProtocol data) {
//                                    ToastUtils.showToast(R.string.create_success);
//                                }
//                            });
//                        } else {
//                            NetManagerUtil.bindNetworkBySC(str, null, new MyOkHttpListener() {
//                                @Override
//                                public void success(Object tag, GsonBaseProtocol mGsonBaseProtocol) {
//                                    ToastUtils.showToast(R.string.bind_success);
//                                }
//                            });
//                        }
                        break;
                    case AddPopUtil.SHOW_ADD_DEV:
                        DeviceUserUtil.bindDeviceBySC(str, null, new MyOkHttpListener<ShareBindResult>() {
                            @Override
                            public void success(Object tag, ShareBindResult mGsonBaseProtocol) {
                                ToastUtils.showToast(mGsonBaseProtocol.scanconfirm == 1 ?
                                        R.string.wait_for_dev_auth : R.string.bind_success);
                                DevManager.getInstance().initHardWareList(null);//扫码添加绑定设备
                            }
                        });
                        break;
                }
                mDialog.dismiss();
            }
        });

        TextView negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setText(R.string.cancel);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }
}
