package net.linkmate.app.ui.fragment.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.linkmate.app.R;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.data.ScoreHelper;
import net.linkmate.app.manager.LoginManager;
import net.linkmate.app.manager.UserInfoManager;
import net.linkmate.app.ui.activity.LoginActivity;
import net.linkmate.app.ui.activity.mine.AccountActivity;
import net.linkmate.app.ui.activity.mine.BackupInfoActivity;
import net.linkmate.app.ui.activity.mine.BackupPhotoActivity;
import net.linkmate.app.ui.activity.mine.DevFlowDetailsActivity;
import net.linkmate.app.ui.activity.mine.DevMngActivity;
import net.linkmate.app.ui.activity.mine.LocalDevDiagnosisActivity;
import net.linkmate.app.ui.activity.mine.NetMngActivity;
import net.linkmate.app.ui.activity.mine.PrivilegeActivity;
import net.linkmate.app.ui.activity.mine.SettingActivity;
import net.linkmate.app.ui.activity.mine.score.ScoreActivity;
import net.linkmate.app.ui.fragment.BaseFragment;
import net.linkmate.app.ui.viewmodel.UserInfoViewModel;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.MySPUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.ActivityItemLayout;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.BaseInfo;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.GetUserInfoHttpLoader;
import net.sdvn.common.internet.protocol.GetUserInfoResultBean;
import net.sdvn.common.internet.protocol.scorepay.UserScore;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.service.NasService;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.scorepaylib.score.ScoreAPIUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public class MeFragment extends BaseFragment {
    private TextView tvTitle;
    private ImageView ivRight;
    private View itbRl;
    private TextView meTvUserName;
    private TextView meTvAccount;
    private SwipeRefreshLayout mSrl;
    private ActivityItemLayout meAilScore;
    private ActivityItemLayout meAilPrivile;
    //    @BindView(R.id.me_ail_cp)
//    ActivityItemLayout meAilCp;
    private
    ActivityItemLayout meAilNetManager;
    private ActivityItemLayout meAilDevManager;
    private ActivityItemLayout meAilLocalDevManager;
    private ActivityItemLayout meAilPhoto;
    private ActivityItemLayout meAilContacts;
    private boolean isRefresh;
    private boolean isLowScoreTipsShow;
    private UserInfoViewModel mUserInfoViewModel;
    private TipsBar tipsBar = null;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_me_new;
    }

    @Override
    protected View getTopView() {
        return itbRl;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        mUserInfoViewModel.getUserInfoLiveData().observe(this, getUserInfoResultBean -> {
            if (getUserInfoResultBean != null) {
                GetUserInfoResultBean.DataBean userInfoBean = getUserInfoResultBean.data;
                if (userInfoBean != null && !TextUtils.isEmpty(userInfoBean.loginname)) {
                    initName(userInfoBean.nickname, userInfoBean.loginname);
                }
            }
        });
        LoginManager.getInstance().loginedData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLogined) {
                onLoginStatusChange(isLogined);
                if (isLogined) {
                    BaseInfo baseInfo = CMAPI.getInstance().getBaseInfo();
                    if (baseInfo != null) {
                        String userId = baseInfo.getUserId();
                        String ticket = baseInfo.getTicket();
                        if (isLogined && !EmptyUtils.isEmpty(userId) && !EmptyUtils.isEmpty(ticket)) {
                            mUserInfoViewModel.loadUserInfo(userId, ticket);
                        }
                    }
                }
            }
        });
    }

    private View mItbIvRight;

    @Override
    public void onStart() {
        super.onStart();
        onLoginStatusChange(MySPUtils.getBoolean(MyConstants.IS_LOGINED));
    }

    @Override
    public void onStop() {
        super.onStop();
        mSrl.setRefreshing(false);
    }

    @Nullable
    @Override
    protected TipsBar getHomeTipsBar() {
        return tipsBar;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onLoginStatusChange(boolean loggedin) {
        if (loggedin) {
            mSrl.setRefreshing(true);
            initData();
        } else {
            int status = CMAPI.getInstance().getBaseInfo().getStatus();
            if (status == Constants.CS_CONNECTING ||
                    status == Constants.CS_CONNECTED ||
                    status == Constants.CS_AUTHENTICATED ||
                    status == Constants.CS_WAIT_RECONNECTING ||
                    status == Constants.CS_ESTABLISHED) {
                meTvUserName.setText(R.string.loading);
                meTvAccount.setText("");
            } else {
                meTvUserName.setText(R.string.click_to_login);
                meTvAccount.setText(R.string.use_more_features);
            }
            mSrl.setEnabled(false);
            meAilScore.setTips("");
            isLowScoreTipsShow = false;
            isRefresh = true;
        }
    }

    int refreshProgress = 0;
    long newRefreshTime = 0;
    private long errorTime1;
    private long errorTime2;
    private View mMeRlMyInfo;
    private View mMeAilNetManager;
    private View mMeAilDevManager;
    private View mMeAilDevFlowDetails;
    private View mMeAilLocalDevManager;
    private View mMeAilPrivile;
    private View mMeAilScore;
    private View mMeAilBackupPhoto;
    private View mMeAilBackupContacts;

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        bindView(view);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        tvTitle.setText(R.string.me);
        ivRight.setImageResource(R.drawable.icon_me_setting);
        tipsBar = view.findViewById(R.id.tipsBar1);
        initEvent();
    }

    public void initData() {
        if (isAdded() && CMAPI.getInstance().getBaseInfo() != null) {
            long refreshTime = System.currentTimeMillis();
            if (refreshTime - newRefreshTime < 2000) {
                refreshProgress++;
                setSrlRefreshProgress(newRefreshTime);
                return;
            }
            newRefreshTime = refreshTime;

            if (getString(R.string.click_to_login).contentEquals(meTvUserName.getText())) {
                meTvUserName.setText(R.string.loading);
                meTvAccount.setText("");
            }

            refreshProgress = 2;
            errorTime1 = 0;
            errorTime2 = 0;
            GetUserInfoResultBean.DataBean userInfoBean = UserInfoManager.getInstance().getUserInfoBean();
            if (!isRefresh && userInfoBean != null && !TextUtils.isEmpty(userInfoBean.loginname)) {
                initName(userInfoBean.nickname, userInfoBean.loginname);
                setSrlRefreshProgress(refreshTime);
            } else {
                GetUserInfoHttpLoader httpLoader = new GetUserInfoHttpLoader(GetUserInfoResultBean.class);
                httpLoader.executor(new ResultListener<GetUserInfoResultBean>() {
                    @Override
                    public void success(Object tag, GetUserInfoResultBean data) {
                        initName(data.data.nickname, data.data.loginname);
                        setSrlRefreshProgress(refreshTime);
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        if (CMAPI.getInstance().isConnected()) {
                            errorTime1++;
                            long delay = errorTime1 * 2000;
                            if (errorTime1 < 4) {
                                initName(getString(R.string.loading), "");
                                Disposable disposable = Single.timer(delay, TimeUnit.MILLISECONDS)
                                        .subscribe(aLong ->
                                                new GetUserInfoHttpLoader(GetUserInfoResultBean.class).executor(this));
                                addDisposable(disposable);
                            } else {
                                initName(getString(R.string.load_failed), "");
                            }
                        }
                    }
                });
            }

            ScoreAPIUtil.getScore(null, UserScore.class, new ResultListener<UserScore>() {
                @Override
                public void success(Object tag, UserScore data) {
                    meAilScore.setTips(BigDecimal.valueOf(data.data.mbpoint)
//                            .setScale(2, RoundingMode.FLOOR)
                            .stripTrailingZeros()
                            .toPlainString());
                    setSrlRefreshProgress(refreshTime);

                    if (data.data.mbpoint <= 0 && !isLowScoreTipsShow) {
                        isLowScoreTipsShow = true;
                        ScoreHelper.showNeedMBPointDialog(getContext());
                    }
                }

                @Override
                public void error(Object tag, GsonBaseProtocol baseProtocol) {
                    if (CMAPI.getInstance().isConnected()) {
                        errorTime2++;
                        long delay = errorTime2 * 2000;
                        if (errorTime2 < 4) {
                            meAilScore.setTips("");
                            Disposable disposable = Single.timer(delay, TimeUnit.MILLISECONDS)
                                    .subscribe(aLong ->
                                            ScoreAPIUtil.getScore(null, UserScore.class, this));
                            addDisposable(disposable);
                        } else {
                            meAilScore.setTips("");
                        }
                    }
                }
            });
            isRefresh = false;
        }
    }

    private void initName(String nickname, String loginname) {
        meTvUserName.setText(nickname);
        meTvAccount.setText(loginname);
    }

    public void setSrlRefreshProgress(long refreshTime) {
        if (refreshTime == newRefreshTime) {
            refreshProgress--;
            if (refreshProgress <= 0) {
                if (mSrl != null)
                    mSrl.setRefreshing(false);
            } else if (!mSrl.isRefreshing()) {
                if (mSrl != null)
                    mSrl.setRefreshing(true);
            }
        }
    }

    private void initEvent() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                initData();
                isRefresh = false;
            }
        });
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        if (MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
            switch (view.getId()) {
                case R.id.itb_iv_right:
                    startActivity(new Intent(view.getContext(), SettingActivity.class));
                    break;
                case R.id.me_rl_my_info:
                    startActivity(new Intent(view.getContext(), AccountActivity.class));
                    break;
                case R.id.me_ail_score:
                    startActivity(new Intent(getActivity(), ScoreActivity.class));
                    break;
                case R.id.me_ail_privile:
                    startActivity(new Intent(getActivity(), PrivilegeActivity.class));
                    break;
//                case R.id.me_ail_cp:
//                    BaseInfo baseinfo = CMAPI.getInstance().getBaseInfo();
//                    Intent intent = new Intent(view.getContext(), WebActivity.class);
//                    intent.putExtra("url", getString(R.string.ControlPanelURL) + baseinfo.getTicket());
//                    intent.putExtra("title", getString(R.string.control_panel));
//                    intent.putExtra("ConnectionState", true);
//                    intent.putExtra("enableScript", true);
//                    intent.putExtra("hasFullTitle", false);
//                    intent.putExtra("sllType", "app");
//                    startActivity(intent);
//                    break;
                case R.id.me_ail_net_manager:
                    startActivity(new Intent(getActivity(), NetMngActivity.class));
                    break;
                case R.id.me_ail_dev_manager:
                    startActivity(new Intent(getActivity(), DevMngActivity.class));
                    break;
                case R.id.me_ail_dev_flow_details:
                    startActivity(new Intent(getActivity(), DevFlowDetailsActivity.class));
                    break;
                case R.id.me_ail_local_dev_manager:
                    gotoLocalDevMng();
                    break;
                case R.id.me_ail_backup_photo:
                    Intent intent = new Intent(getContext(), BackupPhotoActivity.class);
                    NasService service = SessionManager.getInstance().getService();
                    String devId0 = null;
                    if (service != null) {
                        devId0 = service.getBackupAlbumTarget();
                    }
                    if (devId0 == null)
                        devId0 = SPHelper.get(AppConstants.SP_FIELD_BAK_ALBUM_LAST_DEV_ID, null);
                    if (devId0 != null)
                        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId0);
                    startActivity(intent);
                    break;
                case R.id.me_ail_backup_contacts:
                    intent = new Intent(getContext(), BackupInfoActivity.class);
                    String devId = SPHelper.get(AppConstants.SP_FIELD_BAK_INFO_CONTACT_LAST_DEV_ID, null);
                    if (devId != null)
                        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId);
                    intent.putExtra(BackupInfoActivity.EXTRA_BACKUP_INFO_TYPE, true);
                    startActivity(intent);
                    break;
            }
        } else {
            if (view.getId() == R.id.me_rl_my_info) {
                startActivity(new Intent(getContext(), LoginActivity.class));
            } else {
                LoginManager.getInstance().showDialog(getContext());
            }
        }
    }

    private void gotoLocalDevMng() {
        BaseInfo baseinfo = CMAPI.getInstance().getBaseInfo();
        boolean currentSmartNode = false;
        List<Device> smartNodeList = CMAPI.getInstance().getSmartNodeList();
        for (int i = 0; i < smartNodeList.size(); i++) {
            String id = smartNodeList.get(i).getId();
            if (baseinfo.hadSelectedSn(id)) {
                currentSmartNode = true;
                break;
            }
        }
        if (currentSmartNode) {
            cancelSelectNodes();
        } else {
            startActivity(new Intent(getActivity(), LocalDevDiagnosisActivity.class));
        }
    }

    private void cancelSelectNodes() {
        DialogUtil.showSelectDialog(getContext(), getString(R.string.unselect_snode_msg),
                getString(R.string.yes), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                        dialog.dismiss();
                        Boolean isSuccess = CMAPI.getInstance().clearSmartNode();
                        if (isSuccess) {
                            startActivity(new Intent(getActivity(), LocalDevDiagnosisActivity.class));
                        } else {
                            ToastUtils.showToast(getString(R.string.unselect_snode_title) + getString(R.string.fail));
                        }
                    }
                },
                getString(R.string.no), null);
    }

    private void bindView(View view) {
        tvTitle = view.findViewById(R.id.itb_tv_title);
        ivRight = view.findViewById(R.id.itb_iv_right);
        itbRl = view.findViewById(R.id.itb_rl);
        meTvUserName = view.findViewById(R.id.me_tv_user_name);
        meTvAccount = view.findViewById(R.id.me_tv_account);
        mSrl = view.findViewById(R.id.me_srl);
        meAilScore = view.findViewById(R.id.me_ail_score);
        meAilPrivile = view.findViewById(R.id.me_ail_privile);
        meAilNetManager = view.findViewById(R.id.me_ail_net_manager);
        meAilDevManager = view.findViewById(R.id.me_ail_dev_manager);
        meAilLocalDevManager = view.findViewById(R.id.me_ail_local_dev_manager);
        meAilPhoto = view.findViewById(R.id.me_ail_backup_photo);
        meAilContacts = view.findViewById(R.id.me_ail_backup_contacts);
        mItbIvRight = view.findViewById(R.id.itb_iv_right);
        mMeRlMyInfo = view.findViewById(R.id.me_rl_my_info);
        mMeAilNetManager = view.findViewById(R.id.me_ail_net_manager);
        mMeAilDevManager = view.findViewById(R.id.me_ail_dev_manager);
        mMeAilDevFlowDetails = view.findViewById(R.id.me_ail_dev_flow_details);
        mMeAilLocalDevManager = view.findViewById(R.id.me_ail_local_dev_manager);
        mMeAilPrivile = view.findViewById(R.id.me_ail_privile);
        mMeAilScore = view.findViewById(R.id.me_ail_score);
        mMeAilBackupPhoto = view.findViewById(R.id.me_ail_backup_photo);
        mMeAilBackupContacts = view.findViewById(R.id.me_ail_backup_contacts);
        mItbIvRight.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeRlMyInfo.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilNetManager.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilDevManager.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilDevFlowDetails.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilLocalDevManager.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilPrivile.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilScore.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilBackupPhoto.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mMeAilBackupContacts.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
