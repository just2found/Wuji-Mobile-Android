package net.linkmate.app.ui.activity.mine.score;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.ui.fragment.ads.AdsListFragment;
import net.linkmate.app.ui.fragment.ads.AdsViewModel;
import net.linkmate.app.util.Dp2PxUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.WindowUtil;
import net.linkmate.app.util.business.RechargeTypeUtils;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.adapter.RechargeAmountRVAdapter;
import net.linkmate.app.view.adapter.RechargeCurrencyTypeRVAdapter;
import net.linkmate.app.view.adapter.RechargeTypePopRVAdapter;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.protocol.scorepay.PayOrderInfo;
import net.sdvn.common.internet.protocol.scorepay.ScoreConversion;
import net.sdvn.nascommon.utils.InputMethodUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.scorepaylib.pay.PayUtils;
import net.sdvn.scorepaylib.pay.alipay.AliPayAPIUtil;
import net.sdvn.scorepaylib.pay.alipay.AliPayHandler;
import net.sdvn.scorepaylib.pay.paypal.PayPalUtils;
import net.sdvn.scorepaylib.pay.wechat.WXAPIUtil;
import net.sdvn.scorepaylib.score.ScoreAPIUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public class RechargeActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView ivLeft;
    private TextView tvTitle;
    private RelativeLayout rlTitle;
    private ImageView rechargeIvType;
    private TextView rechargeTvType;
    private RecyclerView mRvType;
    private RecyclerView mRv;
    private TextView mTvTotal;
    private Button mBtnPay;
    private SwipeRefreshLayout mSrl;
    private NestedScrollView mNestedScrollView;

    private PopupWindow popWin;
    private int switchPayType = 1;
    private String currencyType = "RMB";//人民币CNY，美元USD
    private static String[] currencyTypes = new String[]{"RMB", "USD"};
    private static int[] currencyTypeTextIds = new int[]{R.string.rmb, R.string.usd};

    private RechargeCurrencyTypeRVAdapter mAdapterCType;
    private RechargeAmountRVAdapter mAdapter;
    private List<ScoreConversion.DataBean.ListBean> datas = new ArrayList<>();
    private AdsViewModel adsViewModel;

    private ScoreConversion.DataBean.ListBean checkedData = new ScoreConversion.DataBean.ListBean();
    private View mItbIvLeft;

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PayPalUtils.getInstance().stopPayPalService(this);
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private View mRechargeLlPayType;

    private void initRVType() {
        List<String> strings = new ArrayList<>();
        if (switchPayType != 3) {
            strings.add(getString(currencyTypeTextIds[0]));
        } else {
            strings.add(getString(currencyTypeTextIds[1]));
        }
        mAdapterCType = new RechargeCurrencyTypeRVAdapter(strings);
        mAdapterCType.setCheckNumber(0);
        //点击device条目
        mAdapterCType.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mAdapterCType.setCheckNumber(position);
                if (switchPayType != 3) {
                    currencyType = currencyTypes[0];
                } else {
                    currencyType = currencyTypes[1];
                }
                mAdapterCType.notifyDataSetChanged();
                initData();
            }
        });
        mRvType.setLayoutManager(new GridLayoutManager(this, 3, RecyclerView.VERTICAL, false));
        mRvType.setItemAnimator(null);
        mRvType.setAdapter(mAdapterCType);
    }

    private void initRV() {
        mAdapter = new RechargeAmountRVAdapter(datas);
        mAdapter.setCurrencyType(currencyType);
        mAdapter.setCheckNumber(0);
        //点击device条目
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position == -1) {//输入框正在输入触发底部显示总计
                    initTotal();
                } else {
                    mAdapter.setCheckNumber(position);
                    checkedData = datas.get(position);
                    if (checkedData.amountmode == 1) {
//                        int[] screenLocation = new int[2];
//                        //getLocationOnScreen屏幕顶端开始,包括了通知栏的高度。
//                        view.getLocationOnScreen(screenLocation);
//                        int itemBottomY = screenLocation[1] + view.getMeasuredHeight();
//                        mNestedScrollView.smoothScrollBy(0, itemBottomY);
                    } else {
                        //自定义充值清空
                        mAdapter.clearCustomValue();
                        //关闭虚拟键盘
                        InputMethodUtils.hideKeyboard(RechargeActivity.this);
                    }
                    mAdapter.notifyDataSetChanged();
                    initTotal();
                }
            }
        });

        mRv.setLayoutManager(new GridLayoutManager(this, 3, RecyclerView.VERTICAL, false));
        mRv.setItemAnimator(null);
        mRv.setAdapter(mAdapter);
    }

    long errorTime;
    private View mRechargeBtnPay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        bindView(this.getWindow().getDecorView());
        tvTitle.setText(R.string.recharge);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        adsViewModel = new ViewModelProvider(this).get(AdsViewModel.class);
        initView();
        initRVType();
        initRV();
        PayPalUtils.getInstance().startPayPalService(this);
        PayPalUtils.getInstance().hasNoCommitedOrder(this, CMAPI.getInstance().getBaseInfo().getUserId());
    }

    private void initView() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
                if (adsViewModel != null) {
                    adsViewModel.refreshBanner();
                }
            }
        });
        rechargeIvType.setImageResource(RechargeTypeUtils.getTypeIconId(switchPayType));
        rechargeTvType.setText(RechargeTypeUtils.getTypeTextId(switchPayType));

        String tag = "ads_fragment";
        Fragment ads_fragment = getSupportFragmentManager().findFragmentByTag(tag);
        AdsListFragment adsFragment = ads_fragment != null ? (AdsListFragment) ads_fragment : new AdsListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottom_container, adsFragment, tag)
                .commitAllowingStateLoss();
        mBtnPay.setEnabled(false);
    }

    private void initData() {
        errorTime = 0;
        ScoreAPIUtil.ScoreConversion(currencyType,
                this, ScoreConversion.class,
                new MyOkHttpListener<ScoreConversion>() {
                    @Override
                    public void success(Object tag, ScoreConversion data) {
                        mBtnPay.setEnabled(true);
                        datas.clear();
                        //自定义充值排序到最后
                        Collections.sort(data.data.list, new Comparator<ScoreConversion.DataBean.ListBean>() {
                            @Override
                            public int compare(ScoreConversion.DataBean.ListBean o1, ScoreConversion.DataBean.ListBean o2) {
                                if (o1.amountmode < o2.amountmode) {
                                    return -1;
                                } else {
                                    return 1;
                                }
                            }
                        });

                        int total = data.data.list == null ? 0 : data.data.list.size();
                        int lastIndex = -1;
                        for (int i = 0; i < total; i++) {
                            if (data.data.list.get(i).amountmode == 1) {
                                lastIndex = i;
                                break;
                            }
                        }
                        if (lastIndex == -1 || lastIndex == total - 1) {//添加全部
                            datas.addAll(data.data.list);
                        } else {//移除多个自定义充值
                            datas.addAll(data.data.list.subList(0, lastIndex + 1));
                        }
                        mAdapter.setNewData(datas);
                        if (datas.size() == 0) {
                            checkedData = datas.get(0);
                            mAdapter.setCheckNumber(0);
                        } else if (mAdapter.getCheckNum() >= datas.size()) {
                            checkedData = datas.get(datas.size() - 1);
                            mAdapter.setCheckNumber(datas.size() - 1);
                        } else {
                            checkedData = datas.get(mAdapter.getCheckNum());
                        }
                        initTotal();
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        mBtnPay.setEnabled(false);
                        if (CMAPI.getInstance().isConnected()) {
                            errorTime++;
                            long delay = errorTime * 2000;
                            if (errorTime < 4) {
                                Disposable disposable = Single.timer(delay, TimeUnit.MILLISECONDS)
                                        .subscribe(aLong ->
                                                ScoreAPIUtil.ScoreConversion(currencyType,
                                                        RechargeActivity.this, ScoreConversion.class, this));
                                addDisposable(disposable);
                            }
                        }
                    }
                });
    }

    private void initTotal() {
        String currency = "￥";
        if ("USD".equals(currencyType)) {
            currency = "$";
        }
        //倍数
        double multiple = checkedData.amountmode == 1 ? mAdapter.getCustomValue() : 1;

        String total = new DecimalFormat("#.00").format(multiple * checkedData.price);
        if (total.startsWith(".")) total = "0" + total;
        if (total.endsWith(".00")) total = total.substring(0, total.length() - 3);

        mTvTotal.setText(getString(R.string.total) + ": " + currency + total);
        //倍数为0是，表示未输入份数，按钮不可用
        mRechargeBtnPay.setEnabled(multiple != 0);
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.itb_iv_left:
                onBackPressed();
                break;
            case R.id.recharge_ll_pay_type:
                showSwitchPop();
                break;
            case R.id.recharge_btn_pay:
                long increase = checkedData.amountmode == 1 ? mAdapter.getCustomValue() : 1;
                String orderName = getString(R.string.recharge) + " "
                        + BigDecimal.valueOf(increase * checkedData.mbpoint)
                        .setScale(2, RoundingMode.FLOOR)
                        .stripTrailingZeros()
                        .toPlainString()
                        + " " + getString(R.string.score);

                String point = BigDecimal.valueOf(increase * checkedData.mbpoint)
                        .setScale(2, RoundingMode.FLOOR)
                        .stripTrailingZeros()
                        .toPlainString();

                String price = new DecimalFormat("#.00").format(increase * checkedData.price);
                if (price.startsWith(".")) price = "0" + price;
                if (price.endsWith(".00")) price = price.substring(0, price.length() - 3);

                switch (switchPayType) {
                    case 1:
                        PayUtils.GetPayOrder(checkedData.sku, "13", price,
                                currencyType, point, orderName,
                                String.valueOf(increase), null, PayOrderInfo.class,
                                new MyOkHttpListener<PayOrderInfo>() {
                                    @Override
                                    public void success(Object tag, PayOrderInfo data) {
                                        AliPayAPIUtil.pay(RechargeActivity.this, new AliPayHandler(new AliPayHandler.Result() {
                                            @Override
                                            public void succ() {
                                                ToastUtils.showToast(R.string.pay_succ);
                                            }

                                            @Override
                                            public void cancelled() {
                                                ToastUtils.showToast(R.string.pay_cancelled);
                                            }

                                            @Override
                                            public void failed(String err) {
                                                ToastUtils.showToast(R.string.pay_failed);
                                            }
                                        }), data.data.result);
                                    }
                                });
                        break;
                    case 2:
                        if (!WXAPIUtil.isWxAppInstalled(this)) {
                            ToastUtils.showToast(R.string.wx_no_installed);
                            return;
                        }
                        PayUtils.GetPayOrder(checkedData.sku, "24", price,
                                currencyType, point, orderName,
                                String.valueOf(increase), null, PayOrderInfo.class,
                                new MyOkHttpListener<PayOrderInfo>() {
                                    @Override
                                    public void success(Object tag, PayOrderInfo data) {
                                        WXAPIUtil.startWechatPay(RechargeActivity.this, data.data.result);
                                        int result = WXAPIUtil.startWechatPay(RechargeActivity.this, data.data.result);
                                        if (result != 0) {
                                            ToastUtils.showToast(R.string.pay_order_error);
                                        }
                                    }
                                });
                        break;
                    case 3:
                        if (PayPalUtils.getInstance().hasNoCommitedOrder(this,
                                CMAPI.getInstance().getBaseInfo().getUserId())
                                || PayPalUtils.getInstance().isCommitOldOrder()) {
                            ToastUtils.showToast(R.string.loading);
                        } else {
                            PayUtils.GetPayOrder(checkedData.sku, "41", price,
                                    currencyType, point, orderName,
                                    String.valueOf(increase), null, PayOrderInfo.class,
                                    new MyOkHttpListener<PayOrderInfo>() {
                                        @Override
                                        public void success(Object tag, PayOrderInfo data) {
                                            try {
                                                JSONTokener parser = new JSONTokener(data.data.result);
                                                JSONObject jo = (JSONObject) parser.nextValue();
                                                String orderno = jo.getString("orderno");
                                                PayPalUtils.getInstance().setSdvnorderno(RechargeActivity.this, orderno);
                                                PayPalUtils.getInstance().doPayPalPay(RechargeActivity.this,
                                                        checkedData.price, currencyType, orderName, orderno);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                        break;
                }
                break;
        }
    }

    private void showSwitchPop() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.layout_switch_account, null, false);
        popWin = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popWin.setOutsideTouchable(true);
        popWin.setTouchable(true);

        View.OnClickListener dismissListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWin.dismiss();
            }
        };
        View out = contentView.findViewById(R.id.pop_account_view_out);
        out.setOnClickListener(dismissListener);
        View cancel = contentView.findViewById(R.id.pop_account_tv_cancel);
        cancel.setOnClickListener(dismissListener);

        RecyclerView rv = contentView.findViewById(R.id.pop_account_rv);
        RechargeTypePopRVAdapter adapter = new RechargeTypePopRVAdapter();
        adapter.setType(switchPayType);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                switchPayType = (int) baseQuickAdapter.getData().get(i);
                initView();
                List<String> strings = new ArrayList<>();
                if (switchPayType != 3) {
                    strings.add(getString(currencyTypeTextIds[0]));
                    currencyType = currencyTypes[0];
                } else {
                    strings.add(getString(currencyTypeTextIds[1]));
                    currencyType = currencyTypes[1];
                }
                mAdapterCType.setCheckNumber(0);
                mAdapterCType.setNewData(strings);
                mAdapter.setCurrencyType(currencyType);
                initData();
                if (popWin != null) {
                    popWin.dismiss();
                }
            }
        });
        LinearLayoutManager layout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv.setLayoutManager(layout);
        rv.setItemAnimator(null);

        //尾部留白
        View footerView = new View(this);
        adapter.addFooterView(footerView);
        ViewGroup.LayoutParams layoutParams = footerView.getLayoutParams();
        layoutParams.height = Dp2PxUtils.dp2px(this, 24);
        footerView.setLayoutParams(layoutParams);

        rv.setAdapter(adapter);
        adapter.setNewData(RechargeTypeUtils.getTypes());

        popWin.setAnimationStyle(R.style.BottomPopupWindow);
        popWin.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        popWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowUtil.hintShadow(RechargeActivity.this);
            }
        });
        WindowUtil.showShadow(RechargeActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PayPalUtils.getInstance().confirmPayResult(this, requestCode, resultCode, data, new PayPalUtils.DoResult() {
            @Override
            public void succ(String id) {
                PayPalUtils.getInstance().savePaypalResultInfo(RechargeActivity.this,
                        CMAPI.getInstance().getBaseInfo().getUserId(), id, MyConstants.PAYPAL_LOG_PATH);
                PayPalUtils.getInstance().commitOrder(RechargeActivity.this,
                        CMAPI.getInstance().getBaseInfo().getUserId(),
                        PayPalUtils.getInstance().getSdvnorderno(RechargeActivity.this), id,
                        new HttpLoader.HttpLoaderStateListener() {
                            @Override
                            public void onLoadStart(Disposable disposable) {
                                showLoading();
                            }

                            @Override
                            public void onLoadComplete() {
                                dismissLoading();
                            }

                            @Override
                            public void onLoadError() {
                                dismissLoading();
                            }
                        }, new MyOkHttpListener() {

                            @Override
                            public void success(Object tag, GsonBaseProtocol data) {
                                ToastUtils.showToast(R.string.pay_succ);
                            }
                        });
            }

            @Override
            public void failed(String err) {
                ToastUtils.showToast(R.string.pay_failed);
            }

            @Override
            public void cancelled() {
                ToastUtils.showToast(R.string.pay_cancelled);
            }
        });
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        mSrl.setRefreshing(true);
    }

    @Override
    public void onLoadComplete() {
        mSrl.setRefreshing(false);
    }

    @Override
    public void onLoadError() {
        mSrl.setRefreshing(false);
    }

    private void bindView(View bindSource) {
        ivLeft = bindSource.findViewById(R.id.itb_iv_left);
        tvTitle = bindSource.findViewById(R.id.itb_tv_title);
        rlTitle = bindSource.findViewById(R.id.itb_rl);
        rechargeIvType = bindSource.findViewById(R.id.recharge_iv_type);
        rechargeTvType = bindSource.findViewById(R.id.recharge_tv_type);
        mRvType = bindSource.findViewById(R.id.recharge_rv_currency);
        mRv = bindSource.findViewById(R.id.recharge_rv);
        mTvTotal = bindSource.findViewById(R.id.recharge_tv_total);
        mBtnPay = bindSource.findViewById(R.id.recharge_btn_pay);
        mSrl = bindSource.findViewById(R.id.recharge_srl);
        mItbIvLeft = bindSource.findViewById(R.id.itb_iv_left);
        mRechargeLlPayType = bindSource.findViewById(R.id.recharge_ll_pay_type);
        mRechargeBtnPay = bindSource.findViewById(R.id.recharge_btn_pay);
        mNestedScrollView = bindSource.findViewById(R.id.mNestedScrollView);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mRechargeLlPayType.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mRechargeBtnPay.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
