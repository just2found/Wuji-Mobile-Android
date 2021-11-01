package net.linkmate.app.ui.activity.mine;

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
import net.linkmate.app.ui.fragment.PrivilegeFragment;
import net.linkmate.app.view.TipsBar;

public class PrivilegeActivity extends BaseActivity {
    private ImageView ivLeft;
    private TextView tvTitle;
    private RelativeLayout rlTitle;
    private FrameLayout mFl;
    private View mItbIvLeft;
    private View mItbIvRight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_has_a_framelayout);
        bindView(this.getWindow().getDecorView());
        tvTitle.setText(R.string.privileged_service);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);

        FragmentManager fm = getSupportFragmentManager();
        //开启事务
        FragmentTransaction ft = fm.beginTransaction();
        //将界面上的一块布局替换为Fragment
        ft.replace(R.id.dev_management_fl, PrivilegeFragment.newInstance(false), "");
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
        }
    }

    private void bindView(View bindSource) {
        ivLeft = bindSource.findViewById(R.id.itb_iv_left);
        tvTitle = bindSource.findViewById(R.id.itb_tv_title);
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
