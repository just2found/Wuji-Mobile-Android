package net.linkmate.app.ui.activity.mine;

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

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.DevBoundType;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.ui.fragment.DeviceFragment;
import net.linkmate.app.ui.scan.ScanActivity;
import net.linkmate.app.util.AddPopUtil;
import net.linkmate.app.util.business.ShowAddDialogUtil;
import net.linkmate.app.view.TipsBar;

public class DevMngActivity extends BaseActivity {
    private ImageView ivLeft;
    private TextView tvTitle;
    private ImageView ivRight;
    private RelativeLayout rlTitle;
    private FrameLayout mFl;
    private View mItbIvLeft;
    private View mItbIvRight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_has_a_framelayout);
        bindView();
        //刷新云设备
        DevManager.getInstance().refreshCloudDevices(null);
        tvTitle.setText(R.string.dev_mng);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        ivRight.setImageResource(R.drawable.icon_device_more);

        FragmentManager fm = getSupportFragmentManager();
        //开启事务
        FragmentTransaction ft = fm.beginTransaction();
        //将界面上的一块布局替换为Fragment
        ft.replace(R.id.dev_management_fl, DeviceFragment.newInstance(DevBoundType.ALL_BOUND_DEVICES), "");
        ft.commit();
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

    private void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.itb_iv_left:
                onBackPressed();
                break;
            case R.id.itb_iv_right:
                showAddPop();
                break;
        }
    }

    private void showAddPop() {
        AddPopUtil.showAddPop(this, ivRight, AddPopUtil.SHOW_SCAN | AddPopUtil.SHOW_ADD_DEV, new AddPopUtil.OnPopButtonClickListener() {
            @Override
            public void onClick(View v, int clickNum) {
                switch (clickNum) {
                    case AddPopUtil.SHOW_SCAN:
                        goToScan();
                        break;
                    case AddPopUtil.SHOW_ADD_DEV:
                        ShowAddDialogUtil.showAddDialog(DevMngActivity.this, clickNum);
                        break;
                    case AddPopUtil.SHOW_ADD_NET:
                        break;
                }
            }
        });
    }

    private void goToScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void bindView() {
        ivLeft =  findViewById(R.id.itb_iv_left);
        tvTitle =  findViewById(R.id.itb_tv_title);
        ivRight =  findViewById(R.id.itb_iv_right);
        rlTitle =  findViewById(R.id.itb_rl);
        mFl =  findViewById(R.id.dev_management_fl);
        mItbIvLeft =  findViewById(R.id.itb_iv_left);
        mItbIvRight =  findViewById(R.id.itb_iv_right);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mItbIvRight.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
