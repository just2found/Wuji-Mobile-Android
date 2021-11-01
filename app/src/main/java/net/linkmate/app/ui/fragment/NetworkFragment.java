package net.linkmate.app.ui.fragment;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.manager.LoginManager;
import net.linkmate.app.manager.MessageManager;
import net.linkmate.app.manager.NetManager;
import net.linkmate.app.manager.StatePager;
import net.linkmate.app.ui.viewmodel.NetworkViewModel;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.business.ShareUtil;
import net.linkmate.app.view.adapter.HomeNetModelRVAdapter;
import net.sdvn.cmapi.Network;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.protocol.ShareCode;
import net.sdvn.common.repo.NetsRepo;
import net.sdvn.common.vo.NetworkModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class NetworkFragment extends BaseFragment implements NetManager.NetUpdateObserver
        , HttpLoader.HttpLoaderStateListener {
    private SwipeRefreshLayout mSrlDevice;
    private RecyclerView rvMyNet;
    private HomeNetModelRVAdapter networkListAdapter;
    private StatePager mStatePager;
    private List<Network> netList = new ArrayList<>();
    private BaseQuickAdapter.OnItemChildClickListener listener;
    //    private View footView;
    private NetworkViewModel networkViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkViewModel = new ViewModelProvider(this).get(NetworkViewModel.class);
        networkViewModel.getNetworkModels().observe(this, networkModels -> {
            mSrlDevice.setRefreshing(false);
            refreshNetView(networkModels);
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home_network;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mSrlDevice = view.findViewById(R.id.home_srl_device);
        rvMyNet = view.findViewById(R.id.home_rv_devices);
        initRv();
        mSrlDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNetList();
//                NetsRepo.INSTANCE.refreshNetList().setHttpLoaderStateListener(NetworkFragment.this);
//                mSrlDevice.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mSrlDevice.setRefreshing(false);
//                    }
//                }, 1000);
            }
        });
        mStatePager = StatePager.builder(mSrlDevice)
                .emptyViewLayout(R.layout.pager_empty_text)
//                .addRetryButtonId(R.id.home_iv_add)
//                .setRetryClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        ShowAddDialogUtil.showAddDialog(getContext(), AddPopUtil.SHOW_ADD_NET);
//                    }
//                })
                .build();


    }

    @Override
    public void refreshData() {
        refreshNetList();
    }

    private void refreshNetList() {
        if (LoginManager.getInstance().isLogined()) {
            NetsRepo.INSTANCE.refreshNetList().setHttpLoaderStateListener(NetworkFragment.this);//NetworkFragment
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        NetManager.getInstance().addNetUpdateObserver(this);
        onNetUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetManager.getInstance().deleteNetUpdateObserver(this);
    }

    private void initRv() {
        rvMyNet.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        networkListAdapter = new HomeNetModelRVAdapter(null);
        networkListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                NetworkModel network = (NetworkModel) adapter.getItem(position);
                if (network != null && listener != null) {
                    listener.onItemChildClick(adapter, view, position);
                }
            }
        });
        rvMyNet.setAdapter(networkListAdapter);
        rvMyNet.setItemAnimator(null);
    }

    private void initShareViewDate(Network network) {
        //获取分享码
        ShareUtil.showNetworkShareCode(network.getId(), null,
                new ResultListener<ShareCode>() {
                    @Override
                    public void success(Object tag, final ShareCode data) {
                        //生成二维码
                        String shareCode = data.sharecode;
                        ShareUtil.generateQRCode(getView(), MyConstants.EVENT_CODE_NETWORK, shareCode,
                                new ShareUtil.QRCodeResult() {
                                    @Override
                                    public void onGenerated(Bitmap bitmap, String tips) {
                                        showShareDialog(bitmap, tips, shareCode);
                                        MessageManager.getInstance().quickDelay();
                                    }
                                });
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        ToastUtils.showError(baseProtocol.result);
                    }
                });
    }

    private Dialog mngDialog;

    private void showShareDialog(Bitmap bitmap, String tips, String shareCode) {
        if (!isAdded() || getContext() == null) {
            return;
        }
        if (mngDialog != null && mngDialog.isShowing()) {
            return;
        }
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_share_code, null);
        mngDialog = new Dialog(getContext(), R.style.DialogTheme);
        mngDialog.setContentView(view);
        View shareImageContainer = view.findViewById(R.id.lsc_container);
        View shareBack = view.findViewById(R.id.lsc_iv_back);
        ImageView imgShareQR = view.findViewById(R.id.lsc_iv_qr);
        TextView tvShareCode = view.findViewById(R.id.lsc_tv_share_code);
        TextView tvShareTips = view.findViewById(R.id.lsc_tv_tips);
        Switch switchShareNeedAuth = view.findViewById(R.id.lsc_switch_share_need_Auth);
        Switch switchShare = view.findViewById(R.id.lsc_switch_share);
        Button shareBtn = view.findViewById(R.id.lsc_btn_share);
        View ivBack = view.findViewById(R.id.iv_back);
        shareBtn.setEnabled(true);
        imgShareQR.setImageBitmap(bitmap);
        tvShareCode.setText(shareCode);
        tvShareTips.setText(tips);
        shareImageContainer.setVisibility(View.VISIBLE);
        shareBack.setVisibility(View.GONE);
        switchShare.setVisibility(View.GONE);
        switchShareNeedAuth.setVisibility(View.GONE);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存二维码到本地
                ShareUtil.saveAndShareImg(shareImageContainer, shareCode, null);
            }
        });
        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(v -> {
            mngDialog.dismiss();
        });
        mngDialog.show();
    }

    @Override
    public void onNetUpdate() {
        initBeans();
//        refreshNetView(networkModels);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initBeans() {
        netList.clear();
        netList.addAll(NetManager.getInstance().getNetBeansBySort());
    }

    public void setOnNetSelect(BaseQuickAdapter.OnItemChildClickListener listener) {
        this.listener = listener;
    }

    private void refreshNetView(List<NetworkModel> networkModels) {
        if (networkModels == null || networkModels.size() == 0) {
            mStatePager.showEmpty().setText(R.id.tv_tips, R.string.tips_no_network);
//            networkListAdapter.removeFooterView(footView);
        } else {
            mStatePager.showSuccess();
            networkListAdapter.setNewData(networkModels);
//            footView = View.inflate(getContext(), R.layout.item_home_network_foot, null);
//            networkListAdapter.addFooterView(footView);
        }
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        addDisposable(disposable);
    }

    @Override
    public void onLoadComplete() {
        mSrlDevice.setRefreshing(false);
    }

    @Override
    public void onLoadError() {
        mSrlDevice.setRefreshing(false);
    }
}

