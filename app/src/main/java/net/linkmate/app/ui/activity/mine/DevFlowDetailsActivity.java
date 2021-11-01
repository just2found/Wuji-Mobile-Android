package net.linkmate.app.ui.activity.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.SimpleCallback;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.bean.DevFlowDetailsBean;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.util.FormatUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.WindowUtil;
import net.linkmate.app.view.CusTextView;
import net.linkmate.app.view.DeviceFlowDetailFilterDialog;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.adapter.DevFlowDetailsRVAdapter;
import net.linkmate.app.view.adapter.PopupCheckRVAdapter;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.loader.GetFlowIncomeDetailHttpLoader;
import net.sdvn.common.internet.loader.GetFlowIncomeStatHttpLoader;
import net.sdvn.common.internet.protocol.flow.DevFlowDetailList;
import net.sdvn.nascommon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class DevFlowDetailsActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView ivLeft;
    private TextView tvTitle;
    private ImageView ivRight;
    private LinearLayout rlTitle;
    private LinearLayout mLlExpabd;
    private CusTextView mTvExpabd;
    private RecyclerView mRv;
    private SwipeRefreshLayout mSrl;

    private DevFlowDetailsRVAdapter mAdapter;
    private List<DevFlowDetailsBean> adapterData = new ArrayList<>();
    private List<DevFlowDetailList.DataBean.ListBean> totalBeans = new ArrayList<>();
    private List<DevFlowDetailList.DataBean.ListBean> trueBeans = new ArrayList<>();
    private String currentTime;

    private int currPage = 1;
    private final int pageSize = 10;
    private boolean hasMorePage;
    private boolean hasMoreMonth;
    private RelativeLayout emptyView;
    private RelativeLayout errorView;
    private String lastVaildMonth = "";
    private String peroid = "month";
    private String checkedDevId = "";
    private String checkedUserId = "";
    private String checkedDevName = "";
    private List<String> options1Items = new ArrayList<>();
    private List<List<String>> options2Items = new ArrayList<>();
    private List<List<List<String>>> options3Items = new ArrayList<>();
    private OptionsPickerView pvOptions;
    private View mItbIvLeft;

    private void initView() {
        emptyView = (RelativeLayout) View.inflate(this, R.layout.pager_empty_text, null);
        ((TextView) emptyView.findViewById(R.id.tv_tips)).setText(R.string.no_record);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currPage = 1;
                lastVaildMonth = "";
                totalBeans.clear();
                trueBeans.clear();
                initTotal();
            }
        });
        errorView = (RelativeLayout) View.inflate(this, R.layout.layout_error_view, null);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currPage = 1;
                lastVaildMonth = "";
                totalBeans.clear();
                trueBeans.clear();
                initTotal();
            }
        });
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currPage = 1;
                lastVaildMonth = "";
                totalBeans.clear();
                trueBeans.clear();
                initTotal();
            }
        });
    }

    private void initRv() {
        mAdapter = new DevFlowDetailsRVAdapter(adapterData);
        //点击device条目
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (view.getId() == R.id.idfd_tv_title) {
                    if (options1Items.size() != 0 && options2Items.size() != 0) {
                        pvOptions = new OptionsPickerBuilder(DevFlowDetailsActivity.this, new OnOptionsSelectListener() {
                            @Override
                            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                                lastVaildMonth = options1Items.get(options1)
                                        + "-"
                                        + FormatUtils.monthFormatToNumber(DevFlowDetailsActivity.this,
                                        options2Items.get(options1).get(option2));
                                currPage = 1;
                                trueBeans.clear();
                                initTotal();
                            }
                        })
                                .setLayoutRes(R.layout.layout_pickerview_custom_options, new CustomListener() {
                                    @Override
                                    public void customLayout(View v) {
                                        //自定义布局中的控件初始化及事件处理
                                        Button tvSubmit = (Button) v.findViewById(R.id.btnSubmit);
                                        Button ivCancel = (Button) v.findViewById(R.id.btnCancel);
                                        String language = getResources().getConfiguration().locale.getLanguage();
                                        if (!"zh".equals(language) && !"ja".equals(language) && !"ko".equals(language)) {
                                            v.findViewById(R.id.tv_year).setVisibility(View.GONE);
                                            v.findViewById(R.id.tv_month).setVisibility(View.GONE);
                                        }
                                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                pvOptions.returnData();
                                                pvOptions.dismiss();
                                            }
                                        });
                                        ivCancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                pvOptions.dismiss();
                                            }
                                        });
                                    }
                                })
                                //设置可见数量为3,避免被部分手机导航栏挡住
                                .setItemVisibleCount(3)
                                .build();
                        pvOptions.setPicker(options1Items, options2Items, null);
                        try {
                            String billdate = ((TextView) view).getText().toString().trim();
                            SimpleDateFormat sf = new SimpleDateFormat(getString(R.string.fmt_time_adapter_title));
                            Date date = sf.parse(billdate);
                            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
                            SimpleDateFormat monthFormat = new SimpleDateFormat(
                                    "zh".equals(getResources().getConfiguration().locale.getLanguage()) ?
                                            "MM" : "MMM", Locale.US);
                            String year = yearFormat.format(date);
                            String month = monthFormat.format(date);
                            month = FormatUtils.monthFormatToEn(DevFlowDetailsActivity.this, month);
                            int option1 = options1Items.indexOf(year);
                            int option2 = option1 >= 0 ? options2Items.get(option1).indexOf(month) : 0;
                            pvOptions.setSelectOptions(option1, option2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        pvOptions.show();
                    }
                }
            }
        });
        mAdapter.setEmptyView(emptyView);
        mRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRv.setItemAnimator(null);
        mRv.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (hasMorePage) {
                    currPage++;
                    initData();
                } else if (hasMoreMonth) {
                    currPage = 1;
                    initData();
                } else {
                    mAdapter.loadMoreEnd(true);
                }
            }
        }, mRv);
    }

    private void initTotal() {
        GetFlowIncomeStatHttpLoader loader = new GetFlowIncomeStatHttpLoader(DevFlowDetailList.class);
        loader.setHttpLoaderStateListener(this);
        loader.setParams2(checkedDevId, checkedUserId, peroid, "", 1, 24);
        loader.executor(new MyOkHttpListener<DevFlowDetailList>() {
            @Override
            public void success(Object tag, DevFlowDetailList data) {
                totalBeans.clear();
                totalBeans.addAll(data.data.list);
                initData();
                initDateOption();
            }

            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                super.error(tag, baseProtocol);
            }
        });
    }

    private void initDateOption() {
        for (DevFlowDetailList.DataBean.ListBean bean : totalBeans) {
            String[] split = bean.billdate.split("-");
            String year = split[0];
            if (!options1Items.contains(year)) {
                options1Items.add(year);
            }
        }
        for (String item : options1Items) {
            ArrayList<String> e = new ArrayList<>();
            for (DevFlowDetailList.DataBean.ListBean bean : totalBeans) {
                String[] split = bean.billdate.split("-");
                String year = split[0];
                String month = split[1];
                month = FormatUtils.monthFormatToEn(this, month);
                if (Objects.equals(item, year) && !e.contains(month)) {
                    e.add(month);
                }
            }
            options2Items.add(e);
        }
    }

    private void initData() {
        GetFlowIncomeDetailHttpLoader loader = new GetFlowIncomeDetailHttpLoader(DevFlowDetailList.class);
        loader.setHttpLoaderStateListener(this);
        loader.setParams2(checkedDevId, checkedUserId, lastVaildMonth, currPage, pageSize);
        loader.executor(new MyOkHttpListener<DevFlowDetailList>() {
            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                super.error(tag, baseProtocol);
                mAdapter.setEmptyView(errorView);
            }

            @Override
            public void success(Object tag, DevFlowDetailList data) {
                mAdapter.setEmptyView(emptyView);
                if (currPage < data.data.totalPage) {
                    hasMorePage = true;
                } else {
                    hasMorePage = false;
                    if (!Objects.equals(lastVaildMonth, data.data.lastVaildMonth)) {
                        lastVaildMonth = data.data.lastVaildMonth;
                        hasMoreMonth = !TextUtils.isEmpty(data.data.lastVaildMonth);
                    } else {
                        hasMoreMonth = false;
                    }
                }

                trueBeans.addAll(data.data.list);

                List<DevFlowDetailsBean> beans = new ArrayList<>();
                for (DevFlowDetailList.DataBean.ListBean bean : trueBeans) {
                    DevFlowDetailsBean e = new DevFlowDetailsBean(bean);
                    if (!beans.contains(e)) {
                        beans.add(e);
                    }
                }

                Collections.sort(beans, new Comparator<DevFlowDetailsBean>() {
                    @Override
                    public int compare(DevFlowDetailsBean o1, DevFlowDetailsBean o2) {
                        return o1.data.billtime == o2.data.billtime ? 0 : (o1.data.billtime - o2.data.billtime > 0 ? -1 : 1);
                    }
                });
                for (int i = 0; i < beans.size(); i++) {
                    DevFlowDetailsBean current = beans.get(i);
                    if (i == 0 || !isSameMonth(beans.get(i - 1).data.billtime, current.data.billtime)) {
                        DevFlowDetailsBean element = new DevFlowDetailsBean(getTitleBean(current));
                        element.itemType = 1;
                        beans.add(i, element);
                    }
                }
                adapterData.clear();
                if (beans.size() == 0 && hasMoreMonth) {
                    initData();
                } else if (hasMorePage || hasMoreMonth) {
                    adapterData.addAll(beans);
                    mAdapter.setNewData(adapterData);
                    mAdapter.loadMoreComplete();
                } else {
                    adapterData.addAll(beans);
                    mAdapter.setNewData(adapterData);
                    mAdapter.loadMoreEnd(true);
                }
            }

            private DevFlowDetailList.DataBean.ListBean getTitleBean(DevFlowDetailsBean current) {
                for (DevFlowDetailList.DataBean.ListBean totalBean : totalBeans) {
                    if (isSameMonth(current.data.billtime, totalBean.billtime)) {
                        return totalBean;
                    }
                }
                return new DevFlowDetailsBean(current.data.billtime).data;
            }
        });
    }

    public static boolean isSameMonth(long timeMillis1, long timeMillis2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(timeMillis1);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(timeMillis2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
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

    private View mItbIvRight;


    private DeviceFlowDetailFilterDialog filterPopupView = null;

    /**
     * 刷新过滤弹框
     *
     * @param v
     */
    private void showFilterPopupView(View v) {
        if (filterPopupView != null && filterPopupView.isShow()) {//正在显示，关闭弹框
            filterPopupView.dismiss();
            return;
        }
        filterPopupView = null;
        filterPopupView = (DeviceFlowDetailFilterDialog) new XPopup.Builder(this)
                .atView(v)
//                .dismissOnTouchOutside(false)
                .setPopupCallback(new SimpleCallback() {
                    @Override
                    public void onShow() {
                    }

                    @Override
                    public void onDismiss() {
                    }
                })
                .asCustom(new DeviceFlowDetailFilterDialog(this));
        filterPopupView.setFilterListener(new DeviceFlowDetailFilterDialog.FilterListener() {
            @Override
            public void confirm(@NotNull String deviceId, @NotNull String deviceName, @NotNull String userId) {
                if (!checkedDevId.equals(deviceId) || !checkedUserId.equals(userId)) {
                    checkedDevId = deviceId;
                    checkedUserId = userId;
                    currPage = 1;
                    lastVaildMonth = "";
                    totalBeans.clear();
                    trueBeans.clear();
                    checkedDevName = deviceName;
                    initFilterFontStyle();
                    initTotal();
                }
            }
        });
        filterPopupView.initData(checkedDevId, checkedUserId);
        filterPopupView.show();
    }

    /**
     * 过滤字体样式
     */
    private void initFilterFontStyle() {
        if (TextUtils.isEmpty(checkedDevId)) {
            mTvExpabd.setTextColor(getResources().getColor(R.color.text_dark));
            mTvExpabd.setEndDrawable(getResources().getDrawable(R.drawable.ic_filter_unselected, null));
        } else {
            mTvExpabd.setTextColor(getResources().getColor(R.color.color_0C81FB));
            mTvExpabd.setEndDrawable(getResources().getDrawable(R.drawable.ic_filter_selected, null));
        }

    }

    private void showDevPop() {
        if (!DevManager.getInstance().isInitting()) {
            List<DeviceBean> deviceBeans = new ArrayList<>();
            for (DeviceBean bean : DevManager.getInstance().getBoundDeviceBeans()) {
                if (bean.getHardData() != null && bean.getHardData().isEN() && bean.getMnglevel() == 0) {
                    deviceBeans.add(bean);
                }
            }

            DeviceBean bean = new DeviceBean(getString(R.string.all_dev), "", -1, 0);
            bean.setId("");
            deviceBeans.add(0, bean);

            View contentView = LayoutInflater.from(this).inflate(R.layout.popup_rv_check, null, false);
            final PopupWindow window = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            RecyclerView rv = contentView.findViewById(R.id.popup_rv);
            PopupCheckRVAdapter adapter = new PopupCheckRVAdapter(deviceBeans, checkedDevId);
            //点击device条目
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    String deviceId = ((DeviceBean) adapter.getData().get(position)).getId();
                    if (!checkedDevId.equals(deviceId)) {
                        checkedDevId = deviceId;
                        currPage = 1;
                        lastVaildMonth = "";
                        totalBeans.clear();
                        trueBeans.clear();
                        checkedDevName = ((DeviceBean) adapter.getData().get(position)).getName();
                        initTotal();
                    }
                    window.dismiss();
                }
            });
            rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            rv.setItemAnimator(null);
            rv.setAdapter(adapter);

            window.setOutsideTouchable(true);
            window.setTouchable(true);
            window.setAnimationStyle(R.style.PopupWindowAnim);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowUtil.hintShadow(DevFlowDetailsActivity.this);
                }
            });
            WindowUtil.showShadow(this);
            window.showAsDropDown(mLlExpabd, 0, 0);
        } else {
            ToastUtils.showToast(R.string.loading_data);
        }
    }

    private int loadTimes = 0;
    private View mAdfdLlExpand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_flow_details);
        bindView();
        tvTitle.setText(R.string.dev_flow_details);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        ivRight.setImageResource(R.drawable.icon_statistics);

        String checkedDevId = getIntent().getStringExtra("checkedDevId");
        if (!TextUtils.isEmpty(checkedDevId)) {
            this.checkedDevId = checkedDevId;
            initFilterFontStyle();
        }
        String checkedDevName = getIntent().getStringExtra("checkedDevName");
        if (!TextUtils.isEmpty(checkedDevName)) {
            this.checkedDevName = checkedDevName;
        }
        initView();
        initRv();
        initTotal();
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.itb_iv_left:
                onBackPressed();
                break;
            case R.id.itb_iv_right:
                Intent intent = new Intent(this, DevFlowStatActivity.class);
                if (!TextUtils.isEmpty(checkedDevId)) {
                    intent.putExtra("checkedDevId", checkedDevId);
                }
                if (!TextUtils.isEmpty(checkedDevName)) {
                    intent.putExtra("checkedDevName", checkedDevName);
                }
                if (!TextUtils.isEmpty(checkedUserId)) {
                    intent.putExtra("checkedUserId", checkedUserId);
                }
                startActivity(intent);
                break;
            case R.id.adfd_ll_expand:
                showFilterPopupView(view);
//                showDevPop();
                break;
        }
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        loadTimes++;
        mSrl.setRefreshing(true);
    }

    @Override
    public void onLoadComplete() {
        if (--loadTimes <= 0)
            mSrl.setRefreshing(false);
    }

    @Override
    public void onLoadError() {
        if (--loadTimes <= 0)
            mSrl.setRefreshing(false);
    }

    private void bindView() {
        ivLeft = findViewById(R.id.itb_iv_left);
        tvTitle = findViewById(R.id.itb_tv_title);
        ivRight = findViewById(R.id.itb_iv_right);
        rlTitle = findViewById(R.id.itb_rl);
        mLlExpabd = findViewById(R.id.adfd_ll_expand);
        mTvExpabd = findViewById(R.id.adfd_tv_expand);
        mRv = findViewById(R.id.adfd_rv_devices);
        mSrl = findViewById(R.id.adfd_srl_device);
        mItbIvLeft = findViewById(R.id.itb_iv_left);
        mItbIvRight = findViewById(R.id.itb_iv_right);
        mAdfdLlExpand = findViewById(R.id.adfd_ll_expand);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mItbIvRight.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAdfdLlExpand.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
