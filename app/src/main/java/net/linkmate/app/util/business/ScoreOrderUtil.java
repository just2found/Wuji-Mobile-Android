package net.linkmate.app.util.business;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.FragmentManager;

import net.linkmate.app.R;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.bean.RechargeRecordBean;
import net.linkmate.app.bean.ScoreGetRecordBean;
import net.linkmate.app.bean.ScoreUsedRecordBean;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity;
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DevicelDetailActivity;
import net.linkmate.app.view.DataItemLayout;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.protocol.scorepay.ScoreGetRecordList;
import net.sdvn.common.internet.protocol.scorepay.ScoreUseRecordList;
import net.sdvn.common.internet.protocol.scorepay.UseRechargeRecordList;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.viewmodel.DeviceViewModel;

import java.text.SimpleDateFormat;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class ScoreOrderUtil implements HttpLoader.HttpLoaderStateListener {

    private final Context mContext;
    private final Object bean;
    private FragmentManager mSupportFragmentManager;

    public ScoreOrderUtil(Context context, ScoreGetRecordBean bean, FragmentManager supportFragmentManager) {
        this.mContext = context;
        this.bean = bean;
        mSupportFragmentManager = supportFragmentManager;
    }

    public ScoreOrderUtil(Context context, ScoreUsedRecordBean bean, FragmentManager supportFragmentManager) {
        this.mContext = context;
        this.bean = bean;
        mSupportFragmentManager = supportFragmentManager;
    }

    public ScoreOrderUtil(Context context, RechargeRecordBean bean, FragmentManager supportFragmentManager) {
        this.mContext = context;
        this.bean = bean;
        mSupportFragmentManager = supportFragmentManager;
    }

    private Dialog detailDialog;
    private View sv;
    private View loadingView;
    private LinearLayout llContainer;

    public void show() {
        if (detailDialog != null && detailDialog.isShowing()) {
            return;
        }
        final View view = LayoutInflater.from(mContext).inflate(R.layout.layout_dialog_score_order, null);
        detailDialog = new AppCompatDialog(mContext, R.style.DialogTheme);
        detailDialog.setContentView(view);

        view.findViewById(R.id.score_order_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailDialog.dismiss();
            }
        });

        initView(view);

        detailDialog.show();
        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                final Window window = detailDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.width = (int) (metrics.widthPixels * 0.80);
                    window.setAttributes(params);
                    sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private void initView(View view) {
        sv = view.findViewById(R.id.score_order_sv);
        loadingView = view.findViewById(R.id.score_order_loading);
        llContainer = view.findViewById(R.id.score_order_container);
        DeviceViewModel deviceViewModel = new DeviceViewModel();
        if (bean instanceof ScoreGetRecordBean) {
            ScoreGetRecordList.DataBean.ListBean data = ((ScoreGetRecordBean) bean).data;
            if (!TextUtils.isEmpty(data.orderno)) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.order_no)).setText(data.orderno));
            }

            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time));
            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.create_date)).setText(sf.format(data.billdate)));

            String billType = data.billtype;
            if ("buy".equals(data.billtype)) {
                billType = mContext.getString(R.string.recharge);
            } else if ("devicebind".equals(data.billtype)) {
                billType = mContext.getString(R.string.score_for_bound_devices);
            } else if ("transferin".equals(data.billtype)) {
                billType = mContext.getString(R.string.score_transfer);
            } else if ("register".equals(data.billtype)) {
                billType = mContext.getString(R.string.Bonus_score_of_register);
            } else if ("flowreward".equals(data.billtype)) {
                billType = mContext.getString(R.string.reward_for_activities);
            } else {
                if (!TextUtils.isEmpty(data.title))
                    billType = data.title;
            }
            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.source)).setText(billType));

            if (!TextUtils.isEmpty(data.username)) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.title_enter_user)).setText(data.username));
            }

            if (!TextUtils.isEmpty(data.networkid)) {
                DataItemLayout dil = new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.circle_name))
                        .setText(data.networkname);
                dil.setText(data.networkname)
                        .setTextColorId(R.color.selector_txt_blue);
                dil.setDataOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mContext instanceof Activity) {
                            CircleDetialActivity.Companion.startActivity((Activity) mContext, new Intent(mContext, CircleDetialActivity.class)
                                    .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, data.networkid));
                        } else {
                            mContext.startActivity(new Intent(mContext, CircleDetialActivity.class)
                                    .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, data.networkid));
                        }
                    }
                });
                llContainer.addView(dil);
            }

            if (!TextUtils.isEmpty(data.vnodeid)) {
                DataItemLayout dil = new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.device));
                deviceViewModel.refreshDevNameById(data.vnodeid)
                        .subscribe(s -> dil.setText(s), throwable -> {
                            dil.setText(data.devicename);
                        });
                for (DeviceBean deviceBean : DevManager.getInstance().getAdapterDevices()) {
                    if (Objects.equals(data.vnodeid, deviceBean.getId())) {
                        dil.setTextColorId(R.color.selector_txt_blue);

                        dil.setDataOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, DevicelDetailActivity.class);
                                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceBean.getId());
                                if (mContext instanceof Activity) {
                                    DevicelDetailActivity.Companion.startActivity((Activity) mContext, intent);
                                } else {
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                        break;
                    }
                }
                llContainer.addView(dil);
            }

            if (!TextUtils.isEmpty(data.flow)) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.flow_used)).setText(data.flow));
            }

            if (!TextUtils.isEmpty(data.paytype)) {
                String payType = data.paytype;
                if ("weixin".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.wepay);
                } else if ("alipay".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.alipay);
                } else if ("paypal".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.paypal);
                } else if ("applepay".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.applepay);
                } else if ("manual".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.manual);
                }
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.pay_type)).setText(payType));
            }

            String score = "" + data.mbpoint;
            if (data.mbpoint == 0) {
                score = "...";
            }
            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.get_score)).setText(score));
        } else if (bean instanceof ScoreUsedRecordBean) {
            ScoreUseRecordList.DataBean.HistoryBean data = ((ScoreUsedRecordBean) bean).data;
            if (!TextUtils.isEmpty(data.orderno)) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.order_no)).setText(data.orderno));
            }

            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time));

            String billType = mContext.getString(R.string.vnodeapply);
            if ("vnodeflow".equals(data.billtype)) {
                billType = mContext.getString(R.string.vnodeflow);
                sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time_adapter_title2));
            } else if ("vnodeapply".equals(data.billtype)) {
                billType = mContext.getString(R.string.vnodeapply);
            } else if ("transferout".equals(data.billtype)) {
                billType = mContext.getString(R.string.score_transfer);
            } else {
                if (!TextUtils.isEmpty(data.title))
                    billType = data.title;
            }
            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.types_of_expenses)).setText(billType));

            if (data.billdate != 0) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.create_date)).setText(sf.format(data.billdate)));
            } else {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.create_date)).setText(mContext.getString(R.string.this_month)));
            }

            if (!TextUtils.isEmpty(data.username)) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.title_enter_user)).setText(data.username));
            }

            if (!TextUtils.isEmpty(data.networkid)) {
                DataItemLayout dil = new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.circle_name))
                        .setText(data.networkname);
                dil.setText(data.networkname)
                        .setTextColorId(R.color.selector_txt_blue);
                dil.setDataOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mContext instanceof Activity) {
                            CircleDetialActivity.Companion.startActivity((Activity) mContext, new Intent(mContext, CircleDetialActivity.class)
                                    .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, data.networkid));
                        } else {
                            mContext.startActivity(new Intent(mContext, CircleDetialActivity.class)
                                    .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, data.networkid));
                        }
                    }
                });
                llContainer.addView(dil);
            }

            if (!TextUtils.isEmpty(data.vnodeid)) {
                DataItemLayout dil = new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.device));
                deviceViewModel.refreshDevNameById(data.vnodeid)
                        .subscribe(s -> dil.setText(s), throwable -> {
                            dil.setText(data.devicename);
                        });
                for (DeviceBean deviceBean : DevManager.getInstance().getAdapterDevices()) {
                    if (Objects.equals(data.vnodeid, deviceBean.getId())) {
                        dil.setTextColorId(R.color.selector_txt_blue);

                        dil.setDataOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, DevicelDetailActivity.class);
                                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceBean.getId());
                                if (mContext instanceof Activity) {
                                    DevicelDetailActivity.Companion.startActivity((Activity) mContext, intent);
                                } else {
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                        break;
                    }
                }
                llContainer.addView(dil);
            }

            if (!TextUtils.isEmpty(data.flow)) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.flow_used)).setText(data.flow));
            }

            String score = "" + data.mbpoint;
            if (data.mbpoint == 0) {
                score = "...";
            }
            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.used_score)).setText(score));
        } else if (bean instanceof RechargeRecordBean) {
            UseRechargeRecordList.DataBean.ListBean data = ((RechargeRecordBean) bean).data;
            if (!TextUtils.isEmpty(data.orderno)) {
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.order_no)).setText(data.orderno));
            }

            SimpleDateFormat sf = new SimpleDateFormat(mContext.getString(R.string.fmt_time));
            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.create_date)).setText(sf.format(data.createdate)));

            if (!TextUtils.isEmpty(data.paytype)) {
                String payType = data.paytype;
                if ("weixin".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.wepay);
                } else if ("alipay".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.alipay);
                } else if ("paypal".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.paypal);
                } else if ("applepay".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.applepay);
                } else if ("manual".equals(data.paytype.toLowerCase())) {
                    payType = mContext.getString(R.string.manual);
                }
                llContainer.addView(new DataItemLayout(mContext)
                        .setTitle(mContext.getString(R.string.pay_type)).setText(payType));
            }

            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.currency)).setText("" + data.currency));

            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.recharge_amount)).setText("" + data.totalfee));

            String score = "" + data.mbpoint;
            if (data.mbpoint == 0) {
                score = "...";
            }
            llContainer.addView(new DataItemLayout(mContext)
                    .setTitle(mContext.getString(R.string.get_score)).setText(score));
        }

        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        if (loadingView != null)
            loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadComplete() {
        loadingView.setVisibility(View.GONE);
    }

    @Override
    public void onLoadError() {
        loadingView.setVisibility(View.GONE);
    }
}
