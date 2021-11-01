package net.linkmate.app.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.manager.PrivilegeManager;
import net.linkmate.app.manager.StatePager;
import net.linkmate.app.view.adapter.PrivilegeRVAdapter;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.protocol.AccountPrivilegeInfo;

import java.util.ArrayList;
import java.util.List;

public class PrivilegeFragment extends BaseFragment {
    private boolean isExpiringSoon;
    private RecyclerView rvMyDevices;
    private SwipeRefreshLayout mSrlDevice;

    private PrivilegeRVAdapter myDeviceAdapter;
    private StatePager mStatePager;
    private List<AccountPrivilegeInfo.AdapterBean> beans = new ArrayList<>();

    public static PrivilegeFragment newInstance(boolean isExpiringSoon) {
        Bundle args = new Bundle();
        args.putBoolean("isExpiringSoon", isExpiringSoon);
        PrivilegeFragment fragment = new PrivilegeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home_device;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        bindView(view);
        mSrlDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDevData();
            }
        });
        Bundle args = getArguments();
        if (args != null) {
            this.isExpiringSoon = args.getBoolean("isExpiringSoon");
            initBeans();
        }
        initRv();
        mStatePager = StatePager.builder(rvMyDevices)
                .emptyViewLayout(R.layout.pager_empty_text)
                .build();
        refreshDeviceView();
        refreshDevData();
        if (PrivilegeManager.getInstance().isInitting()) {
            mSrlDevice.setRefreshing(true);
        }

    }

    private void initBeans() {
        //初始化本地数据
        beans.clear();
        beans.addAll(PrivilegeManager.getInstance().getPrivilegeBeans());
    }

    private void refreshDevData() {
        mSrlDevice.setRefreshing(true);
        //请求新数据
        PrivilegeManager.getInstance().initPrivilege(new ResultListener<AccountPrivilegeInfo>() {
            @Override
            public void success(Object tag, AccountPrivilegeInfo data) {
                initBeans();
                refreshDeviceView();
                if (mSrlDevice != null)
                    mSrlDevice.setRefreshing(false);
            }

            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                if (mSrlDevice != null)
                    mSrlDevice.setRefreshing(false);
            }
        });
    }

    private void initRv() {
        myDeviceAdapter = new PrivilegeRVAdapter(beans);
        //点击device条目
        myDeviceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            }
        });
        myDeviceAdapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                return 2;
            }
        });
        GridLayoutManager layout = new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false);
        rvMyDevices.setLayoutManager(layout);
        rvMyDevices.setItemAnimator(null);
        rvMyDevices.setAdapter(myDeviceAdapter);
    }

    private void refreshDeviceView() {
        if (beans.size() == 0) {
            mStatePager.showEmpty().setText(R.id.tv_tips, getString(R.string.not_opened_service));
        } else {
            mStatePager.showSuccess();
            myDeviceAdapter.setNewData(beans);
        }
    }

    private void bindView(View bindSource) {
        rvMyDevices = bindSource.findViewById(R.id.home_rv_devices);
        mSrlDevice = bindSource.findViewById(R.id.home_srl_device);
    }
}
