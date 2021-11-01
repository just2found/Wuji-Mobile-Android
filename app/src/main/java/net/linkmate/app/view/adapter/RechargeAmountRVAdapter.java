package net.linkmate.app.view.adapter;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.common.internet.protocol.scorepay.ScoreConversion;
import net.sdvn.nascommon.utils.InputMethodUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class RechargeAmountRVAdapter extends BaseQuickAdapter<ScoreConversion.DataBean.ListBean, BaseViewHolder> {

    private int num;
    private String currencyType;

    public void setCheckNumber(int num) {
        this.num = num;
    }

    public int getCheckNum() {
        return num;
    }

    public RechargeAmountRVAdapter(List<ScoreConversion.DataBean.ListBean> data) {
        super(R.layout.item_recharge_amount, data);

    }

    private String customValue = "";

    private final float MAX_PRICE = 1000000L;//最大价格不超过100万
    private final long MAX_AMOUNT = 10000000000L;//限制最大份额

    /**
     * 获取自定义数额，份额
     *
     * @return
     */
    public long getCustomValue() {
        long value = MAX_AMOUNT;
        try {
            Double valueDouble = Double.valueOf(customValue.trim());
            value = valueDouble > MAX_AMOUNT ? MAX_AMOUNT : valueDouble.longValue();
        } catch (Exception e) {
        }
        return TextUtils.isEmpty(customValue) ? 0 : value;
    }

    /**
     * 自定义充值清空
     */
    public void clearCustomValue() {
        customValue = "";
    }

    /**
     * 缩放文字大小，setText后都需要调用
     *
     * @param textView
     */
    private void initAutoSizeTextTypeUniform(TextView textView) {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView, 1, 26, 1, TypedValue.COMPLEX_UNIT_SP);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, final ScoreConversion.DataBean.ListBean data) {
        final String currency = "USD".equals(currencyType) ? "$" : "￥";
        TextView tvAmount = helper.getView(R.id.ira_tv_amount);

        String mbpoint = BigDecimal.valueOf(data.mbpoint)
                .stripTrailingZeros()
                .toPlainString();
        if (data.original_mbpoint > 0) {
            mbpoint = BigDecimal.valueOf(data.original_mbpoint)
                    .stripTrailingZeros()
                    .toPlainString();
        }
        String total = BigDecimal.valueOf(data.price)
                .stripTrailingZeros()
                .toPlainString();
        if (data.amountmode == 1) {
            if (TextUtils.isEmpty(customValue) || getCustomValue() == 0) {//未输入显示自定义
                total = mContext.getString(R.string.customize);
            } else {//输入了份数大于1
                total = new DecimalFormat("#.00").format(data.price * getCustomValue());
                if (total.startsWith(".")) total = "0" + total;
                if (total.endsWith(".00")) total = total.substring(0, total.length() - 3);
            }
            helper.setGone(R.id.ira_tv_score, false);
            EditText etScore = helper.getView(R.id.etScore);
            etScore.setText(TextUtils.isEmpty(customValue) ? "0" : customValue);
            etScore.setVisibility(View.VISIBLE);
            etScore.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    customValue = etScore.getText().toString();
                    //限制，总价格不超过MAX_PRICE
                    String value = String.valueOf(getCustomValue() * data.price > MAX_PRICE ? ((Double) (MAX_PRICE / data.price)).longValue() : getCustomValue());
                    if (!value.equals(customValue)) {
                        customValue = value;
                        etScore.setText(value);
                        etScore.setSelection(etScore.length());
                    } else {
                        String total = new DecimalFormat("#.00").format(data.price * getCustomValue());
                        if (total.startsWith(".")) total = "0" + total;
                        if (total.endsWith(".00")) total = total.substring(0, total.length() - 3);
                        if (TextUtils.isEmpty(customValue) || getCustomValue() == 0) {//未输入显示自定义
                            total = mContext.getString(R.string.customize);
                        }
                        if (total.equals(mContext.getString(R.string.customize))) {
                            helper.setText(R.id.ira_tv_amount, total);
                        } else {
                            SpannableString spannableString = new SpannableString(currency + " " + total);
                            int currencyTextSize = mContext.getResources().getDimensionPixelSize(R.dimen.txt_13);
                            //指定currency字体大小
                            spannableString.setSpan(new AbsoluteSizeSpan(currencyTextSize), 0, 1, 0);
                            //currency不加粗
                            spannableString.setSpan(new StyleSpan(Typeface.NORMAL), 0, 1, 0);
                            helper.setText(R.id.ira_tv_amount, spannableString);
                        }
                        //触发底部显示
                        setOnItemClick(etScore, -1);
                        initAutoSizeTextTypeUniform(tvAmount);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            if (num == helper.getAdapterPosition()) {
                etScore.requestFocus();
                etScore.setSelection(etScore.length());
                InputMethodUtils.showKeyboard(mContext, etScore, 100);
            } else {
                etScore.clearFocus();
            }
        } else {
            helper.setGone(R.id.ira_tv_score, true);
            helper.setGone(R.id.etScore, false);
        }

        if (total.equals(mContext.getString(R.string.customize))) {
            helper.setText(R.id.ira_tv_amount, total)
                    .setText(R.id.ira_tv_score, /*mContext.getString(R.string.score) + " " +*/mbpoint);
            initAutoSizeTextTypeUniform(tvAmount);
        } else {
            SpannableString spannableString = new SpannableString(currency + " " + total);
            int currencyTextSize = mContext.getResources().getDimensionPixelSize(R.dimen.txt_13);
            //指定currency字体大小
            spannableString.setSpan(new AbsoluteSizeSpan(currencyTextSize), 0, 1, 0);
            //currency不加粗
            spannableString.setSpan(new StyleSpan(Typeface.NORMAL), 0, 1, 0);
            helper.setText(R.id.ira_tv_amount, spannableString)
                    .setText(R.id.ira_tv_score, /*mContext.getString(R.string.score) + " " +*/mbpoint);
            initAutoSizeTextTypeUniform(tvAmount);
        }
        if (data.original_price > 0) {
            helper.setVisible(R.id.ira_tv_original_amount, true)
                    .setText(R.id.ira_tv_original_amount, currency + BigDecimal.valueOf(data.original_price)
                            .stripTrailingZeros()
                            .toPlainString());
            ((TextView) helper.getView(R.id.ira_tv_original_amount))
                    .getPaint()
                    .setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            helper.setVisible(R.id.ira_tv_original_amount, false);
        }

        if (data.reward_mbpoint > 0) {
            helper.setGone(R.id.ira_tv_reward_score, true)
                    .setText(R.id.ira_tv_reward_score, "+" + BigDecimal.valueOf(data.reward_mbpoint)
                            .stripTrailingZeros()
                            .toPlainString());
        } else {
            helper.setGone(R.id.ira_tv_reward_score, false);
        }

        if (num == helper.getAdapterPosition()) {
            helper.setBackgroundRes(R.id.ihd_content, R.drawable.bg_check_btn_stroke_full_radius_blue);
        } else {
            helper.setBackgroundRes(R.id.ihd_content, R.drawable.bg_check_btn_stroke_full_radius_gray);
        }

    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }
}