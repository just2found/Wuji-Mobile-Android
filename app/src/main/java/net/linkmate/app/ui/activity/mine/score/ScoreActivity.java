package net.linkmate.app.ui.activity.mine.score;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.protocol.scorepay.UserScore;
import net.sdvn.scorepaylib.score.ScoreAPIUtil;

import java.math.BigDecimal;

import io.reactivex.disposables.Disposable;

public class ScoreActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView ivLeft;
    private TextView tvTitle;
    private LinearLayout llTitle;
    private TextView tvScore;
    private SwipeRefreshLayout mSrl;
    private View mScoreIvLeft;
    private View mScoreIvRight;
    private View mScoreAilRecharge;
    private View mScoreAilScoreTransfer;
    private View mScoreAilRechargeRecord;
    private View mScoreAilGetRecord;
    private View mScoreAilExpensesRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        bindView(this.getWindow().getDecorView());
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void initView() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });
    }

    private void initData() {
        ScoreAPIUtil.getScore(this, UserScore.class, new MyOkHttpListener<UserScore>() {
            @Override
            public void success(Object tag, UserScore data) {
                tvScore.setText(BigDecimal.valueOf(data.data.mbpoint)
//                        .setScale(2, RoundingMode.FLOOR)
                        .stripTrailingZeros()
                        .toPlainString());
            }

            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                super.error(tag, baseProtocol);
                tvScore.setText(R.string.load_failed);
            }
        });
    }

    @Override
    protected View getTopView() {
        return llTitle;
    }

    private void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.score_iv_left:
                onBackPressed();
                break;
            case R.id.score_iv_right:
                initData();
                break;
            case R.id.score_ail_recharge:
                startActivity(new Intent(this, RechargeActivity.class));
                break;
            case R.id.score_ail_score_transfer:
                startActivity(new Intent(this, ScoreTransferActivity.class));
                break;
            case R.id.score_ail_recharge_record:
                startActivity(new Intent(this, RechargeRecordActivity.class));
                break;
            case R.id.score_ail_get_record:
                startActivity(new Intent(this, ScoreGetRecordActivity.class));
                break;
            case R.id.score_ail_expenses_record:
                startActivity(new Intent(this, ScoreUsedRecordActivity.class));
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
        ivLeft = bindSource.findViewById(R.id.score_iv_left);
        tvTitle = bindSource.findViewById(R.id.score_tv_title);
        llTitle = bindSource.findViewById(R.id.score_ll_title);
        tvScore = bindSource.findViewById(R.id.score_tv_score);
        mSrl = bindSource.findViewById(R.id.score_recode_srl);
        mScoreIvLeft = bindSource.findViewById(R.id.score_iv_left);
        mScoreIvRight = bindSource.findViewById(R.id.score_iv_right);
        mScoreAilRecharge = bindSource.findViewById(R.id.score_ail_recharge);
        mScoreAilScoreTransfer = bindSource.findViewById(R.id.score_ail_score_transfer);
        mScoreAilRechargeRecord = bindSource.findViewById(R.id.score_ail_recharge_record);
        mScoreAilGetRecord = bindSource.findViewById(R.id.score_ail_get_record);
        mScoreAilExpensesRecord = bindSource.findViewById(R.id.score_ail_expenses_record);
        mScoreIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mScoreIvRight.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mScoreAilRecharge.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mScoreAilScoreTransfer.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mScoreAilRechargeRecord.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mScoreAilGetRecord.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mScoreAilExpensesRecord.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
