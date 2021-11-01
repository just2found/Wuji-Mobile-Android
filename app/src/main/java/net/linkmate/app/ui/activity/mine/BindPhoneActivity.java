package net.linkmate.app.ui.activity.mine;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hbb20.CountryCodePicker;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.business.RegisterUtil;
import net.linkmate.app.view.CountDownTextView;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.loader.SetUserPhoneHttpLoader;

import io.reactivex.disposables.Disposable;

import static net.linkmate.app.util.ToastUtils.showToast;

public class BindPhoneActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView itbIvLeft;
    private TextView itbTvTitle;
    private RelativeLayout itbRl;
    private CountryCodePicker ccp;
    private TextView tvCcp;
    private EditText abpEtPhone;
    private EditText abpEtCode;
    private CountDownTextView abpTvGetCode;
    private String countryCode;
    private String phoneNumber;
    private View mAbpTvCcp;
    private View mAbpTvGetCode;
    private View mAbpBtnConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_phone);
        bindView();
        itbTvTitle.setText(R.string.bind_phone);
        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);

        itbIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        abpTvGetCode.setNormalText(getString(R.string.get_verify_code))
                .setCountDownText(getString(R.string.get_verify_code) + "(", ")")
                .setCountDownClickable(false)
                .setCloseKeepCountDown(true)
                .setOnCountDownStartListener(new CountDownTextView.OnCountDownStartListener() {
                    @Override
                    public void onStart() {
                        abpEtCode.requestFocus();
                    }
                });


        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                tvCcp.setText("+" + ccp.getSelectedCountryCode());
            }
        });
        abpEtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                findViewById(R.id.abp_tv_phone).setVisibility(abpEtPhone.getText().length()>0?View.GONE:View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        abpEtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                findViewById(R.id.abp_tv_code).setVisibility(abpEtCode.getText().length()>0?View.GONE:View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected View getTopView() {
        return itbRl;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.abp_tv_ccp:
                ccp.launchCountrySelectionDialog();
                break;
            case R.id.abp_tv_get_code:
                getCode();
                break;
            case R.id.abp_btn_confirm:
                confirm();
                break;
        }
    }

    private void getCode() {
        phoneNumber = abpEtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            ToastUtils.showToast(R.string.error_phone_number_tips);
            return;
        }
        countryCode = ccp.getSelectedCountryCode();
        RegisterUtil.requestAuxCode(countryCode, phoneNumber, this, new MyOkHttpListener() {
            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                super.error(tag, baseProtocol);
                abpTvGetCode.setEnabled(true);
            }

            @Override
            public void success(Object tag, GsonBaseProtocol data) {
                abpTvGetCode.startCountDown(60);
            }
        });
    }

    public void confirm() {
        String phone = abpEtPhone.getText().toString().trim();
        String code = abpEtCode.getText().toString().trim();

        if (phone.isEmpty()) {
            showToast(getString(R.string.pls_input_your_phone_number));
            return;
        }
        if (code.isEmpty()) {
            showToast(getString(R.string.error_string_no_auxcode));
            return;
        }
        SetUserPhoneHttpLoader loader = new SetUserPhoneHttpLoader(GsonBaseProtocol.class);
        loader.setParams(phone, code);
        loader.setHttpLoaderStateListener(this);
        loader.executor(new MyOkHttpListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol data) {
                ToastUtils.showToast(R.string.bind_succeed);
                onBackPressed();
            }
        });
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        addDisposable(disposable);
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

    private void bindView() {
        itbIvLeft =  findViewById(R.id.itb_iv_left);
        itbTvTitle =  findViewById(R.id.itb_tv_title);
        itbRl =  findViewById(R.id.itb_rl);
        ccp =  findViewById(R.id.ccp);
        tvCcp =  findViewById(R.id.abp_tv_ccp);
        abpEtPhone =  findViewById(R.id.abp_et_phone);
        abpEtCode =  findViewById(R.id.abp_et_code);
        abpTvGetCode =  findViewById(R.id.abp_tv_get_code);
        mAbpTvCcp =  findViewById(R.id.abp_tv_ccp);
        mAbpTvGetCode =  findViewById(R.id.abp_tv_get_code);
        mAbpBtnConfirm =  findViewById(R.id.abp_btn_confirm);
        mAbpTvCcp.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAbpTvGetCode.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAbpBtnConfirm.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
