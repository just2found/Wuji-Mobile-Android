package net.linkmate.app.ui.activity.mine.score;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.bean.RechargeRecordBean;
import net.linkmate.app.util.business.ScoreOrderUtil;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.adapter.RechargeRecordRVAdapter;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.protocol.scorepay.UseRechargeRecordList;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.scorepaylib.score.ScoreAPIUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class RechargeRecordActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private ImageView ivLeft;
    private TextView tvTitle;
    private RelativeLayout rlTitle;
    private SwipeRefreshLayout mSrl;
    private RecyclerView mRv;

    private RechargeRecordRVAdapter mAdapter;
    private List<RechargeRecordBean> adapterData = new ArrayList<>();
    private List<UseRechargeRecordList.DataBean.ListBean> trueBeans = new ArrayList<>();

    private int currPage = 1;
    private final int pageSize = 10;
    private boolean hasMorePage;
    private RelativeLayout emptyView;
    private RelativeLayout errorView;
    private View mItbIvLeft;

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge_record);
        bindView(this.getWindow().getDecorView());
        tvTitle.setText(R.string.recharge_record);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        initView();
        initRv();
        initDatas();
    }

    private void initView() {
        emptyView = (RelativeLayout) View.inflate(this, R.layout.pager_empty_text, null);
        ((TextView) emptyView.findViewById(R.id.tv_tips)).setText(R.string.no_record);
        errorView = (RelativeLayout) View.inflate(this, R.layout.layout_error_view, null);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currPage = 1;
                trueBeans.clear();
                initDatas();
            }
        });
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currPage = 1;
                trueBeans.clear();
                initDatas();
            }
        });
    }

    private void initRv() {
        mAdapter = new RechargeRecordRVAdapter(adapterData);
        //点击device条目
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                RechargeRecordBean bean = adapterData.get(position);
                if (bean.itemType == 2) {
                    new ScoreOrderUtil(RechargeRecordActivity.this, bean,getSupportFragmentManager()).show();
                }
            }
        });
        RelativeLayout emptyView = (RelativeLayout) View.inflate(this, R.layout.pager_empty_text, null);
        ((TextView) emptyView.findViewById(R.id.tv_tips)).setText(R.string.no_record);
        mAdapter.setEmptyView(emptyView);
        mRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRv.setItemAnimator(null);
        mRv.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (hasMorePage) {
                    currPage++;
                    initDatas();
                } else {
                    mAdapter.loadMoreEnd(true);
                }
            }
        }, mRv);
    }

    private void initDatas() {
        ScoreAPIUtil.UserRechargeRecord(currPage, pageSize, this, UseRechargeRecordList.class,
                new MyOkHttpListener<UseRechargeRecordList>() {
                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        super.error(tag, baseProtocol);
                        mAdapter.setEmptyView(errorView);
                    }

                    @Override
                    public void success(Object tag, UseRechargeRecordList data) {
                        mAdapter.setEmptyView(emptyView);
                        hasMorePage = currPage < data.data.totalPage;
                        trueBeans.addAll(data.data.list);

                        List<RechargeRecordBean> beans = new ArrayList<>();
                        for (UseRechargeRecordList.DataBean.ListBean bean : trueBeans) {
                            RechargeRecordBean e = new RechargeRecordBean(bean);
                            if (!beans.contains(e)) {
                                beans.add(e);
                            }
                        }

                        Collections.sort(beans, new Comparator<RechargeRecordBean>() {
                            @Override
                            public int compare(RechargeRecordBean o1, RechargeRecordBean o2) {
                                return o1.data.createdate == o2.data.createdate ? 0 :
                                        (o1.data.createdate - o2.data.createdate > 0 ? -1 : 1);
                            }
                        });
                        for (int i = 0; i < beans.size(); i++) {
                            RechargeRecordBean current = beans.get(i);
                            if (i == 0 || !isSameMonth(beans.get(i - 1).data.createdate, current.data.createdate)) {
                                beans.add(i, new RechargeRecordBean(current.data.createdate));
                            }
                        }
                        adapterData.clear();
                        adapterData.addAll(beans);
                        mAdapter.setNewData(adapterData);
                        if (hasMorePage) {
                            mAdapter.loadMoreComplete();
                        } else {
                            mAdapter.loadMoreEnd(true);
                        }
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

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.itb_iv_left:
                onBackPressed();
                break;
        }
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
        mSrl = bindSource.findViewById(R.id.recharge_recode_srl);
        mRv = bindSource.findViewById(R.id.recharge_recode_rv);
        mItbIvLeft = bindSource.findViewById(R.id.itb_iv_left);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
