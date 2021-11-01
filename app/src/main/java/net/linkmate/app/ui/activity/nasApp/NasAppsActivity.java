package net.linkmate.app.ui.activity.nasApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.ui.fragment.nasApp.NasAppFragment;
import net.linkmate.app.ui.fragment.nasApp.NasPluginFragment;
import net.linkmate.app.view.TipsBar;
import net.sdvn.nascommon.constant.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class NasAppsActivity extends BaseActivity {
    private RelativeLayout rlTitle;
    private ImageView itbIvLeft;
    private TextView itbTvTitle;

    private FragmentManager fragmentManager;
    @NonNull
    private List<Fragment> mFragmentList = new ArrayList<>();
    private Fragment mCurFragment;
    private String deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_app);
        initViews();
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void initViews() {
        rlTitle = findViewById(R.id.itb_rl);
        itbIvLeft = findViewById(R.id.itb_iv_left);
        itbTvTitle = findViewById(R.id.itb_tv_title);

        itbTvTitle.setText(R.string.app_mng);
        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);
        itbIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
        NasPluginFragment mPluginFragment;
        NasAppFragment mAppFragment;
        fragmentManager = getSupportFragmentManager();
        mPluginFragment = (NasPluginFragment) fragmentManager.findFragmentByTag(String.valueOf(true));
        mAppFragment = (NasAppFragment) fragmentManager.findFragmentByTag(String.valueOf(false));
        if (mPluginFragment == null)
            mPluginFragment = NasPluginFragment.newInstance(deviceId);
        if (mAppFragment == null)
            mAppFragment = new NasAppFragment();
        mFragmentList.clear();
        mFragmentList.add(mPluginFragment);
        mFragmentList.add(mAppFragment);
        switchFragment(true);
    }

    private void switchFragment(boolean isServerPlugin) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = mFragmentList.get(isServerPlugin ? 0 : 1);

        if (mCurFragment != null) {
            mCurFragment.onPause();
        }

        if (!fragment.isAdded()) {
            transaction.add(R.id.layout_content, fragment, String.valueOf(isServerPlugin));
        } else {
            fragment.onResume();
        }

        for (Fragment ft : mFragmentList) {
            if (fragment == ft) {
                transaction.show(ft);
                mCurFragment = fragment;
            } else {
                transaction.hide(ft);
            }
        }

        transaction.commitAllowingStateLoss();
    }
}
