package net.linkmate.app.ui.activity.mine.score;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.protocol.FindUserResultBean;
import net.sdvn.common.internet.protocol.scorepay.UserScore;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.scorepaylib.score.ScoreAPIUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

import static net.linkmate.app.util.ToastUtils.showToast;

public class ScoreTransferActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView ivLeft;
    private TextView tvTitle;
    private RelativeLayout rlTitle;
    private TextView mTvScore;
    private LinearLayout mLlFind;
    private EditText mEtFind;
    private View mLineAccount;
    private TextView mTvAccountTo;
    private TextView mTvPhone;
    private LinearLayout mLlPhone;
    private View mLinePhone;
    private TextView mTvEmail;
    private LinearLayout mLlEmail;
    private View mLineEmail;
    private TextView mTvNick;
    private EditText mEtAmount;
    private TextView mTvFee;
    private LinearLayout mllStepTwo;
    private Button mBtn;

    private int step = 1;

    private long changeMillis = 0;
    private boolean isChange = false;
    private UserScore.DataBean userData;
    private FindUserResultBean.DataBean findUserBean;
    private View mItbIvLeft;

    private void initEvent() {
        mEtFind.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                findViewById(R.id.score_transfer_tv_find).setVisibility(mEtFind.getText().length()>0?
                        View.GONE:View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String score = BigDecimal.valueOf(userData.transferable_mbp)
                            .stripTrailingZeros()
                            .toPlainString();
                    String trim = s.toString().trim();
                    String[] split = trim.split("\\.");
                    int length = 0;
                    if (split.length > 1) {
                        length = split[1].length();
                    }

                    if (Double.valueOf(trim) < 0) {
                        mEtAmount.setText("0");
                    } else if (userData.transferable_mbp < 0 && Double.valueOf(trim) > 0) {
                        mEtAmount.setText("0");
                        showToast(R.string.ec_no_enough_mbpoion_to_tranfer);
                    } else if (length > 4) {
                        String text = BigDecimal.valueOf(Double.valueOf(trim))
                                .setScale(4, RoundingMode.FLOOR)
                                .stripTrailingZeros()
                                .toPlainString();
                        mEtAmount.setText(text);
                        mEtAmount.setSelection(text.length());
                    } else if (userData.transferable_mbp >= 0
                            && Double.valueOf(trim) >= userData.transferable_mbp
                            && !Objects.equals(trim, score)) {
                        mEtAmount.setText(score);
                    } else {
                        changeMillis = System.currentTimeMillis();
                        isChange = true;
                    }
                } catch (Exception e) {
                    changeMillis = System.currentTimeMillis();
                    isChange = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis();
                if (isChange && millis - changeMillis >= 500) {
                    isChange = false;
                    initFee();
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private void initFee() {
        String amountStr = mEtAmount.getText().toString().trim();
        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception ignored) {
        }
        if (amount == 0) {
            mTvFee.setText("");
        } else {
            mTvFee.setText(BigDecimal.valueOf(amount * userData.service_charge)
                    .setScale(7, RoundingMode.FLOOR)
                    .stripTrailingZeros()
                    .toPlainString());
        }
    }

    long errorTime;
    private View mScoreTransferBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_transfer);
        bindView(this.getWindow().getDecorView());
        tvTitle.setText(R.string.score_transfer);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        initEvent();
        initData();
    }

    private void initData() {
        errorTime = 0;
        ScoreAPIUtil.getScore(this, UserScore.class, new MyOkHttpListener<UserScore>() {
            @Override
            public void success(Object tag, UserScore data) {
                userData = data.data;
                mTvScore.setText(BigDecimal.valueOf(userData.transferable_mbp)
//                        .setScale(2, RoundingMode.FLOOR)
                        .stripTrailingZeros()
                        .toPlainString());
                mTvFee.setText(getString(R.string.service_fee_tips)
                        .replace("$FEE$",
                                BigDecimal.valueOf(data.data.service_charge * 100)
                                        .setScale(2, RoundingMode.FLOOR)
                                        .stripTrailingZeros()
                                        .toPlainString()
                        )
                );
            }
        });

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
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.itb_iv_left:
                onBackPressed();
                break;
            case R.id.score_transfer_btn:
                switch (step) {
                    case 1:
                        findUser();
                        break;
                    case 2:
                        commit();
                        break;
                }
                break;
        }
    }

    private void findUser() {
        String account = mEtFind.getText().toString().trim();
        if (account.isEmpty()) {
            showToast(getString(R.string.error_string_empty_account));
            return;
        }
        ScoreAPIUtil.findUser(account, this, FindUserResultBean.class,
                new MyOkHttpListener<FindUserResultBean>() {
                    @Override
                    public void success(Object tag, FindUserResultBean data) {
                        if (Objects.equals(data.data.userid, CMAPI.getInstance().getBaseInfo().getUserId())) {
                            showToast(getString(R.string.error_transfer_to_yourself));
                            return;
                        }
                        step = 2;
                        mLlFind.setVisibility(View.GONE);
                        mLineAccount.setVisibility(View.GONE);
                        mllStepTwo.setVisibility(View.VISIBLE);
                        mEtAmount.requestFocus();
                        mBtn.setText(R.string.commit);
                        findUserBean = data.data;
                        if (TextUtils.isEmpty(findUserBean.loginname)) {
                            mTvAccountTo.setVisibility(View.GONE);
                        } else {
                            mTvAccountTo.setText(findUserBean.loginname);
                            mTvAccountTo.setVisibility(View.VISIBLE);
                        }
                        if (TextUtils.isEmpty(findUserBean.phone)) {
                            mLlPhone.setVisibility(View.GONE);
                            mLinePhone.setVisibility(View.GONE);
                        } else {
                            mTvPhone.setText(findUserBean.phone);
                            mLlPhone.setVisibility(View.VISIBLE);
                            mLinePhone.setVisibility(View.VISIBLE);
                        }
                        if (TextUtils.isEmpty(findUserBean.email)) {
                            mLlEmail.setVisibility(View.GONE);
                            mLineEmail.setVisibility(View.GONE);
                        } else {
                            mTvEmail.setText(findUserBean.email);
                            mLlEmail.setVisibility(View.VISIBLE);
                            mLineEmail.setVisibility(View.VISIBLE);
                        }
                        mTvNick.setText(findUserBean.nickname);
                    }
                });
    }

    private void commit() {
        if (TextUtils.isEmpty(findUserBean.userid)) {
            return;
        }
        String amountStr = mEtAmount.getText().toString().trim();
        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (Exception ignored) {
        }
        if (amount <= 0) {
            showToast(getString(R.string.pls_enter_a_amount));
            return;
        }
        ScoreAPIUtil.transferScore(findUserBean.userid, amount, this, new MyOkHttpListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol data) {
                showToast(getString(R.string.score_transfer_succ));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        switch (step) {
            case 1:
                super.onBackPressed();
            case 2:
                step = 1;
                mBtn.setText(R.string.next);
                mLlFind.setVisibility(View.VISIBLE);
                mLineAccount.setVisibility(View.VISIBLE);
                mEtFind.requestFocus();
                mllStepTwo.setVisibility(View.GONE);
                break;
        }
    }

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

    private void bindView(View bindSource) {
        ivLeft = bindSource.findViewById(R.id.itb_iv_left);
        tvTitle = bindSource.findViewById(R.id.itb_tv_title);
        rlTitle = bindSource.findViewById(R.id.itb_rl);
        mTvScore = bindSource.findViewById(R.id.score_transfer_tv_score);
        mLlFind = bindSource.findViewById(R.id.score_transfer_ll_find);
        mEtFind = bindSource.findViewById(R.id.score_transfer_et_find);
        mLineAccount = bindSource.findViewById(R.id.score_transfer_line_account);
        mTvAccountTo = bindSource.findViewById(R.id.score_transfer_tv_account_to);
        mTvPhone = bindSource.findViewById(R.id.score_transfer_tv_phone);
        mLlPhone = bindSource.findViewById(R.id.score_transfer_ll_phone);
        mLinePhone = bindSource.findViewById(R.id.score_transfer_line_phone);
        mTvEmail = bindSource.findViewById(R.id.score_transfer_tv_email);
        mLlEmail = bindSource.findViewById(R.id.score_transfer_ll_email);
        mLineEmail = bindSource.findViewById(R.id.score_transfer_line_email);
        mTvNick = bindSource.findViewById(R.id.score_transfer_tv_nick);
        mEtAmount = bindSource.findViewById(R.id.score_transfer_et_amount);
        mTvFee = bindSource.findViewById(R.id.score_transfer_tv_fee);
        mllStepTwo = bindSource.findViewById(R.id.transfer_ll_step_two);
        mBtn = bindSource.findViewById(R.id.score_transfer_btn);
        mItbIvLeft = bindSource.findViewById(R.id.itb_iv_left);
        mScoreTransferBtn = bindSource.findViewById(R.id.score_transfer_btn);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mScoreTransferBtn.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
