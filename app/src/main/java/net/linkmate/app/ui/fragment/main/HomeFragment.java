package net.linkmate.app.ui.fragment.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import net.linkmate.app.R;
import net.linkmate.app.base.DevBoundType;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.base.OnStatusChange;
import net.linkmate.app.manager.LoginManager;
import net.linkmate.app.ui.activity.HomeStatusActivity;
import net.linkmate.app.ui.activity.mine.NetMngActivity;
import net.linkmate.app.ui.fragment.BaseFragment;
import net.linkmate.app.ui.fragment.DeviceFragment;
import net.linkmate.app.ui.fragment.PlsLoginFragment;
import net.linkmate.app.ui.scan.ScanActivity;
import net.linkmate.app.util.AddPopUtil;
import net.linkmate.app.util.Dp2PxUtils;
import net.linkmate.app.util.MySPUtils;
import net.linkmate.app.util.UIUtils;
import net.linkmate.app.util.business.ShowAddDialogUtil;
import net.linkmate.app.view.SwitchDevTypeBar;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.Network;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.nascommon.utils.Utils;

import java.util.List;

public class HomeFragment extends BaseFragment implements OnStatusChange {

    private SwipeRefreshLayout mSrlDevice;
    private RelativeLayout mRlParent;
    private TextView mTvWeLine;
    private TextView mTvTitle;
    private ImageView mIvTitleNet;
    private ImageView mIvTitleRight;
    private LinearLayout mLlTitle;
    private LinearLayout mLlStatus;
    private LinearLayout mLlStatusScan;
    private LinearLayout mLlStatusStatus;
    private LinearLayout mLlStatusNet;
    private LinearLayout mLlStatusAdd;
    private CollapsingToolbarLayout mCtl;
    private AppBarLayout mAppBar;
    private ViewGroup mVpPanel;
    //    @BindView(R.id.home_rv_devices)
//    RecyclerView rvMyDevices;
    private
    SwitchDevTypeBar mSDTBar;
    private FrameLayout mFl;
    private FrameLayout mBanner;

    private BaseFragment mCurrentFragment;
    private boolean loggedin;
    private int statueBarHeight;
    private TipsBar mTipsBar = null;
    private int devBoundType = DevBoundType.IN_THIS_NET;
    private DeviceFragment netFragment;
    private DeviceFragment myFragment;
    private DeviceFragment shareFragment;
    private PlsLoginFragment loginFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    public TipsBar getHomeTipsBar() {
        return mTipsBar;
    }

    private View mHomeIvTitleRight;

    private void initFragments() {
        Fragment fragment1 = getChildFragmentManager().findFragmentByTag("netFragment");
        if (!(fragment1 instanceof DeviceFragment))
            fragment1 = DeviceFragment.newInstance(DevBoundType.IN_THIS_NET, false);
        netFragment = (DeviceFragment) fragment1;
        Fragment fragment2 = getChildFragmentManager().findFragmentByTag("myFragment");
        if (!(fragment2 instanceof DeviceFragment))
            fragment2 = DeviceFragment.newInstance(DevBoundType.MY_DEVICES, false);
        myFragment = (DeviceFragment) fragment2;
        Fragment fragment3 = getChildFragmentManager().findFragmentByTag("shareFragment");
        if (!(fragment3 instanceof DeviceFragment))
            fragment3 = DeviceFragment.newInstance(DevBoundType.SHARED_DEVICES, false);
        shareFragment = (DeviceFragment) fragment3;
        Fragment fragment4 = getChildFragmentManager().findFragmentByTag("loginFragment");
        if (!(fragment4 instanceof PlsLoginFragment))
            fragment4 = new PlsLoginFragment();
        loginFragment = (PlsLoginFragment) fragment4;

        netFragment.setSwipeRefreshLayout(mSrlDevice);
        myFragment.setSwipeRefreshLayout(mSrlDevice);
        shareFragment.setSwipeRefreshLayout(mSrlDevice);
    }

    private void showFragment(BaseFragment fragment, String tag) {
        if (fragment == null) {
            return;
        }
        if (mCurrentFragment == fragment) {
            return;
        }
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            ft.hide(mCurrentFragment);
            if (mCurrentFragment instanceof DeviceFragment) {
                mSrlDevice.setOnRefreshListener(null);
            }
        }
        if (!fragment.isAdded()) {
            ft.add(R.id.home_dev_fl, fragment, tag);
        } else {
            ft.show(fragment);
        }
        if (fragment instanceof DeviceFragment) {
            mSrlDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    ((DeviceFragment) fragment).refreshDevData();
                }
            });
        }
        mCurrentFragment = fragment;
        ft.commit();
    }

    private View mHomeLlTitle;

    private void switchFragment(int type) {
        DeviceFragment fragment = null;
        String tag = null;
        switch (type) {
            case DevBoundType.IN_THIS_NET:
                fragment = netFragment;
                tag = "netFragment";
                break;
            case DevBoundType.MY_DEVICES:
                fragment = myFragment;
                tag = "myFragment";
                break;
            case DevBoundType.SHARED_DEVICES:
                fragment = shareFragment;
                tag = "shareFragment";
                break;
        }
        if (fragment != null) {
            devBoundType = type;
            mSDTBar.setDevBoundType(devBoundType);
            showFragment(fragment, tag);
        }
    }

    @Override
    protected View getTopView() {
        return mCtl;
    }

    @Override
    public void onLoginStatusChange(boolean loggedin) {
        this.loggedin = loggedin;
        if (loggedin) {
            setAppBarEnable(true);
            mSrlDevice.setEnabled(true);
            mLlStatus.setVisibility(View.VISIBLE);
            mSDTBar.setVisibility(View.VISIBLE);
            switchFragment(devBoundType);
        } else {
            mAppBar.setExpanded(false);
//            setAppBarEnable(false);
            mSrlDevice.setEnabled(false);
            mLlStatus.setVisibility(View.GONE);
            mSDTBar.setVisibility(View.GONE);
            showFragment(loginFragment, "loginFragment");
        }
    }

    public void initData() {
        if (isAdded()) {
            initFragments();
        }
    }

    private boolean expanded = true;
    private View mHomeAppBar;
    private View mHomeLlStatus1;
    private View mHomeLlStatus2;
    private View mHomeLlStatus3;
    private View mHomeLlStatus4;
    private View mHomeIvTitleNet;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginManager.getInstance().loginedData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedin) {
                onLoginStatusChange(loggedin);
            }
        });

    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        bindView(view);
        initEvent();
        mTipsBar = view.findViewById(R.id.tipsBar);
        mSDTBar.setActivity(getActivity(), mRlParent);
        mSDTBar.setDevBoundType(devBoundType);
        mSDTBar.setSwitchListaner(new SwitchDevTypeBar.SwitchListaner() {
            @Override
            public void onSwitch(int type) {
                switchFragment(type);
            }
        });
        initFragments();
        if (mCurrentFragment == null) {
            showFragment(netFragment, "netFragment");
        }

        mCtl.setMinimumHeight(Dp2PxUtils.dp2px(requireContext(), 48) + UIUtils.getStatueBarHeight(requireContext()));
//        String tag = "ads_fragment";
//        Fragment ads_fragment = getChildFragmentManager().findFragmentByTag(tag);
//        CarouselAdsFragment adsFragment = ads_fragment != null ? (CarouselAdsFragment) ads_fragment : new CarouselAdsFragment();
//        getChildFragmentManager().beginTransaction()
//                .replace(R.id.bottom_container, adsFragment, tag)
//                .commitAllowingStateLoss();

    }

    private void initEvent() {
        mAppBar.setExpanded(true);
        mAppBar.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if (!loggedin) {
                    return;
                }
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                //i = 0是表示完全展开，向上折叠到最小时 i = -totalScrollRange
                if (i == 0) {
                    mSrlDevice.setEnabled(true);
                } else {
                    if (i == -totalScrollRange) {
                        mSrlDevice.setRefreshing(false);
                    }
                    mSrlDevice.setEnabled(false);
                }

                expanded = i >= -totalScrollRange / 2;

                float alphaShow = ((float) i + totalScrollRange) / totalScrollRange;
                float alphaShow1 = alphaShow < 0.5 ? 0 : (float) ((alphaShow - 0.5) * 2);
                mLlStatus.setAlpha(alphaShow1);
                mTvWeLine.setAlpha(alphaShow1);

                float alphaShow2 = 1 - alphaShow;
                alphaShow2 = alphaShow2 < 0.5 ? 0 : (float) ((alphaShow2 - 0.5) * 2);
                mTvTitle.setAlpha(alphaShow2);
                mIvTitleNet.setAlpha(alphaShow2);
                if (alphaShow2 > 0) {
                    mIvTitleNet.setVisibility(View.VISIBLE);
                } else {
                    mIvTitleNet.setVisibility(View.GONE);
                }

                float range = ((float) i + totalScrollRange) / totalScrollRange;
//                mIvTitleBottom.setRotation(90 + (1 - range) * 180);

                Context context = appBarLayout.getContext();
                int min = Dp2PxUtils.dp2px(context, 48);
                int max = Dp2PxUtils.dp2px(context, 48) + mLlStatus.getHeight();
                int statueBarHeight = getStatueBarHeight(context);
                int titleHeight = (int) (range * (max - min) + min + statueBarHeight);
                mSDTBar.setWindowLocation(titleHeight);

                if (mBanner.getVisibility() == View.VISIBLE) {
                    onBannerShow(range);
                } else {
                    onBannerClose();
                }
            }
        });
        mVpPanel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                refreshAppBarStatus();
            }
        });
    }

    public void onBannerShow(float range) {
//        if (range < -1) {
//            range = expanded ? 1 : 0;
//        }
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFl.getLayoutParams();
//        params.bottomMargin = (int) (range * mLlStatus.getHeight() + mBanner.getHeight());
//        mFl.setLayoutParams(params);
    }

    public void onBannerClose() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFl.getLayoutParams();
        params.bottomMargin = 0;
        mFl.setLayoutParams(params);
    }

    private int getStatueBarHeight(Context context) {
        if (statueBarHeight == 0) {
            statueBarHeight = UIUtils.getStatueBarHeight(context);
        }
        return statueBarHeight;
    }

    private Network getCurrentNetwork() {
        for (Network network : CMAPI.getInstance().getNetworkList()) {
            if (network.isCurrent())
                return network;
        }
        return null;
    }

    private String getCurrentSNName() {
        List<Device> currentSmartNode = CMAPI.getInstance().getRealtimeInfo().getCurrentSmartNode();
        if (currentSmartNode.size() == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentSmartNode.size(); i++) {
            Device device = currentSmartNode.get(i);
            if (device.getSelectable() || device.getDeviceType() == Constants.DT_V_NODE) {
                if (!TextUtils.isEmpty(sb.toString()))
                    sb.append(", ");
                sb.append(device.getName());
            }
        }
        return sb.toString();
    }

    private void setAppBarEnable(boolean enable) {
        //禁止
        View childAt = mAppBar.getChildAt(0);
        AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) childAt.getLayoutParams();
        if (enable) {
            //启用
            layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                    AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        } else {
            layoutParams.setScrollFlags(0);
        }
        childAt.setLayoutParams(layoutParams);
    }

    private void refreshAppBarStatus() {
//        Context context = getContext();
//        if (context == null) {
//            return;
//        }
//        int max = Dp2PxUtils.dp2px(context, 48) + mLlStatus.getHeight();
//        int height = mFl.getHeight();
//        int height1 = mRlParent.getHeight();
//        if (height > height1 - max) {
//            setAppBarEnable(true);
//        } else {
//            setAppBarEnable(false);
//        }
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        if (MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
            switch (view.getId()) {
                case R.id.home_ll_title:
                    //防止点击穿透
                    break;
                case R.id.home_iv_title_right:
                    showAddPop();
                    break;
                case R.id.home_app_bar:
                    mAppBar.setExpanded(!expanded);
                    break;
                case R.id.home_ll_status_1:
                    goToScan();
                    break;
                case R.id.home_ll_status_2:
                    showStatus();
                    break;
                case R.id.home_ll_status_3:
                case R.id.home_iv_title_net:
                    switchNet();
                    break;
                case R.id.home_ll_status_4:
                    ShowAddDialogUtil.showAddDialog(getContext(), AddPopUtil.SHOW_ADD_DEV);
                    break;
            }
        } else {
            LoginManager.getInstance().showDialog(getContext());
        }
    }

    private void showAddPop() {
        AddPopUtil.showAddPop(requireActivity(), mIvTitleRight, AddPopUtil.SHOW_ALL, new AddPopUtil.OnPopButtonClickListener() {
            @Override
            public void onClick(View v, int clickNum) {
                if (Utils.isFastClick(v)) {
                    return;
                }
                switch (clickNum) {
                    case AddPopUtil.SHOW_SCAN:
                        goToScan();
                        break;
                    case AddPopUtil.SHOW_ADD_DEV:
                        ShowAddDialogUtil.showAddDialog(getContext(), clickNum);
                        break;
                    case AddPopUtil.SHOW_ADD_NET:
                        ShowAddDialogUtil.showAddDialog(getContext(), clickNum);
                        break;
                    case AddPopUtil.SHOW_STATUS:
                        showStatus();
                        break;
//                    case AddPopUtil.SHOW_ADD_CIRCLE:
//                        startActivity(new Intent(getContext(), CreateCircleActivity.class));
//                        break;
                }
            }
        });
    }

    private void goToScan() {
        Intent intent = new Intent(getContext(), ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showStatus() {
        startActivity(new Intent(getContext(), HomeStatusActivity.class));
    }

    private void switchNet() {
        startActivity(new Intent(getContext(), NetMngActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        onLoginStatusChange(MySPUtils.getBoolean(MyConstants.IS_LOGINED));
    }

    private void bindView(View bindSource) {
        mSrlDevice = bindSource.findViewById(R.id.home_srl);
        mRlParent = bindSource.findViewById(R.id.home_rl_parent);
        mTvWeLine = bindSource.findViewById(R.id.home_tv_weline);
        mTvTitle = bindSource.findViewById(R.id.home_tv_title);
        mIvTitleNet = bindSource.findViewById(R.id.home_iv_title_net);
        mIvTitleRight = bindSource.findViewById(R.id.home_iv_title_right);
        mLlTitle = bindSource.findViewById(R.id.home_ll_title);
        mLlStatus = bindSource.findViewById(R.id.home_ll_status);
        mLlStatusScan = bindSource.findViewById(R.id.home_ll_status_1);
        mLlStatusStatus = bindSource.findViewById(R.id.home_ll_status_2);
        mLlStatusNet = bindSource.findViewById(R.id.home_ll_status_3);
        mLlStatusAdd = bindSource.findViewById(R.id.home_ll_status_4);
        mCtl = bindSource.findViewById(R.id.home_ctl);
        mAppBar = bindSource.findViewById(R.id.home_app_bar);
        mVpPanel = bindSource.findViewById(R.id.ll_home_vp);
        mSDTBar = bindSource.findViewById(R.id.home_sdtb);
        mFl = bindSource.findViewById(R.id.home_dev_fl);
        mBanner = bindSource.findViewById(R.id.bottom_container);
        mHomeIvTitleRight = bindSource.findViewById(R.id.home_iv_title_right);
        mHomeLlTitle = bindSource.findViewById(R.id.home_ll_title);
        mHomeAppBar = bindSource.findViewById(R.id.home_app_bar);
        mHomeLlStatus1 = bindSource.findViewById(R.id.home_ll_status_1);
        mHomeLlStatus2 = bindSource.findViewById(R.id.home_ll_status_2);
        mHomeLlStatus3 = bindSource.findViewById(R.id.home_ll_status_3);
        mHomeLlStatus4 = bindSource.findViewById(R.id.home_ll_status_4);
        mHomeIvTitleNet = bindSource.findViewById(R.id.home_iv_title_net);
        mHomeIvTitleRight.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mHomeLlTitle.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mHomeAppBar.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mHomeLlStatus1.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mHomeLlStatus2.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mHomeLlStatus3.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mHomeLlStatus4.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mHomeIvTitleNet.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
