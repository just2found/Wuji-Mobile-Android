package net.linkmate.app.ui.activity.mine;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity;
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper;
import net.linkmate.app.ui.fragment.NetworkFragment;
import net.linkmate.app.ui.scan.ScanActivity;
import net.linkmate.app.util.AddPopUtil;
import net.linkmate.app.util.CheckStatus;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.NetworkUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.business.ShowAddDialogUtil;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.common.ErrorCode;
import net.sdvn.common.vo.NetworkModel;
import net.sdvn.nascommon.utils.Utils;


public class NetMngActivity extends BaseActivity {
    private ImageView ivLeft;
    private TextView tvTitle;
    private ImageView ivRight;
    private RelativeLayout rlTitle;
    private FrameLayout mFl;
    private View mItbIvLeft;

    @Override
    protected void onDisconnected() {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private NetworkFragment fragment = null;
    private View mItbIvRight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_has_a_framelayout);
        bindView(this.getWindow().getDecorView());
        tvTitle.setText(R.string.circle_manager);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        ivRight.setImageResource(R.drawable.icon_device_more);

        initFragment();
    }

    private void initFragment() {
        fragment = new NetworkFragment();
        fragment.setOnNetSelect(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (Utils.isFastClick(view)) return;
                final NetworkModel bean = (NetworkModel) adapter.getItem(position);
                if (bean.netStatus == 0 && view.getId() == R.id.ihn_content) {
                    if (bean.userStatus != 0) {//待同意圈子
                        CircleDetialActivity.Companion.startActivityForResult(NetMngActivity.this, new Intent(NetMngActivity.this, CircleDetialActivity.class)
                                .putExtra(FunctionHelper.NETWORK_ID, bean.netId), FunctionHelper.CIRCLE_PURCHASE_FLOW);
                    } else if (NetworkUtils.checkNetwork(NetMngActivity.this)) {
                        CheckStatus.INSTANCE.checkCircleStatus(NetMngActivity.this, getSupportFragmentManager(), bean, normalStatus -> {
                            //状态正常
                            if (normalStatus) switchNetNow(bean);
                            return null;
                        }, nextChoice -> {
                            //用户选择不购买流量
                            if (!nextChoice) switchNetNow(bean);
                            return null;
                        });
                    } else {
                        ToastUtils.showToast(R.string.error_string_no_network);
                    }
                } else if (view.getId() == R.id.ihn_iv_setting) {
//                    if(bean.getName().contains("圈子")||bean.getName().equals("高清电影")||bean.getName().equals("库噢噢噢")){
                    //	flowstatus:圈子中流量收费的状态，0为正常，1为已到期，2为未选购
                    CircleDetialActivity.Companion.startActivityForResult(NetMngActivity.this, new Intent(NetMngActivity.this, CircleDetialActivity.class)
                            .putExtra(FunctionHelper.NETWORK_ID, bean.netId), FunctionHelper.CIRCLE_PURCHASE_FLOW);
//                    }

//                    }else{
//                        new NetworkManagerDialogUtil(NetMngActivity.this, bean, position).showDetailDialog();
//                    }

                }
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        //开启事务
        FragmentTransaction ft = fm.beginTransaction();
        //将界面上的一块布局替换为Fragment
        ft.replace(R.id.dev_management_fl, fragment, "");
        ft.commit();
    }

    private void switchNetwork(NetworkModel bean) {
        if (bean == null) {
            return;
        }
        if (!bean.isCurrent) {
            DialogUtil.showSelectDialog(this,
                    String.format(getString(R.string.tips_switch_account), bean.netName),
                    getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
                        @Override
                        public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                            dialog.dismiss();
                            switchNetNow(bean);
                        }
                    },
                    getString(R.string.cancel), null);
            setStatus(LoadingStatus.CHANGE_VIRTUAL_NETWORK);
        }
    }

    private void switchNetNow(NetworkModel bean) {
        if (bean.isCurrent) {
            if (!isFinishing()) {
                finish();
            }
        } else {
            showLoading(R.string._switch);
            CMAPI.getInstance().switchNetwork(bean.netId, new net.sdvn.cmapi.protocal.ResultListener() {
                @Override
                public void onError(int error) {
                    dismissLoading();
                    if (error != Constants.CE_SUCC) {
                        ToastUtils.showToast(getString(ErrorCode.error2String(error)));
                    } else {
                        if (!isFinishing()) {
                            finish();
                        }
                    }
                }
            });
        }
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.itb_iv_left:
                onBackPressed();
                break;
            case R.id.itb_iv_right:
                showAddPop();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fragment != null) fragment.refreshData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == FunctionHelper.CIRCLE_PURCHASE_FLOW && resultCode == RESULT_OK) {
//            if (fragment != null) fragment.refreshData();
//        }
    }

    private void showAddPop() {
        AddPopUtil.showAddPop(this, ivRight, AddPopUtil.SHOW_SCAN | AddPopUtil.SHOW_ADD_NET, new AddPopUtil.OnPopButtonClickListener() {
            @Override
            public void onClick(View v, int clickNum) {
                switch (clickNum) {
                    case AddPopUtil.SHOW_SCAN:
                        goToScan();
                        break;
                    case AddPopUtil.SHOW_ADD_DEV:
                        break;
                    case AddPopUtil.SHOW_ADD_NET:
                        ShowAddDialogUtil.showAddDialog(NetMngActivity.this, clickNum);
                        break;
//                    case AddPopUtil.SHOW_ADD_CIRCLE:
//                        startActivity(new Intent(NetMngActivity.this, CreateCircleActivity.class));
//                        break;
                }
            }
        });
    }

    private void goToScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void bindView(View bindSource) {
        ivLeft = bindSource.findViewById(R.id.itb_iv_left);
        tvTitle = bindSource.findViewById(R.id.itb_tv_title);
        ivRight = bindSource.findViewById(R.id.itb_iv_right);
        rlTitle = bindSource.findViewById(R.id.itb_rl);
        mFl = bindSource.findViewById(R.id.dev_management_fl);
        mItbIvLeft = bindSource.findViewById(R.id.itb_iv_left);
        mItbIvRight = bindSource.findViewById(R.id.itb_iv_right);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mItbIvRight.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
