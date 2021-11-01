package net.sdvn.nascommon.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import net.sdvn.cmapi.util.CommonUtils;
import net.sdvn.nascommonlib.R;

public class SetSubnetLayout extends LinearLayout {

    private EditText mEtIp;
    private EditText mEtNetmask;
    private LinearLayout mLlBg;
    private ImageView mViewRemove;
    private Context mContext;

    public SetSubnetLayout(Context context) {
        this(context, null);
    }

    public SetSubnetLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SetSubnetLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.widget_set_subnet_layout, this);
        mLlBg = view.findViewById(R.id.subnet_widget_background);
        mEtIp = view.findViewById(R.id.subnet_widget_ip);
        mEtNetmask = view.findViewById(R.id.subnet_widget_netmask);
        mViewRemove = view.findViewById(R.id.subnet_widget_remove);
    }

    public void setEnabled(boolean enabled) {
//        mEtIp.setEnabled(enabled);
//        mEtNetmask.setEnabled(enabled);
//        mViewRemove.setEnabled(enabled);
        for (int i = 0; i < mLlBg.getChildCount(); i++) {
            View child = mLlBg.getChildAt(i);
            if (child instanceof ViewGroup) {
                for (int j = 0; j < ((ViewGroup) child).getChildCount(); j++) {
                    View childChild = ((ViewGroup) child).getChildAt(j);
                    if (childChild instanceof EditText || childChild instanceof ImageView) {
                        childChild.setEnabled(enabled);
                    } else {
                        childChild.setBackgroundColor(mContext.getResources().getColor(
                                enabled ? R.color.color_blue_150 : R.color.color_bg_grey350));
                    }
                }
            } else {
                if (child instanceof EditText || child instanceof ImageView) {
                    child.setEnabled(enabled);
                } else {
                    child.setBackgroundColor(mContext.getResources().getColor(
                            enabled ? R.color.color_blue_150 : R.color.color_bg_grey350));
                }
            }
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        mLlBg.setBackgroundColor(color);
    }

    public EditText getEtIp() {
        return mEtIp;
    }

    public EditText getEtNetmask() {
        return mEtNetmask;
    }


    @Nullable
    public String getIp() {
        String sIp = mEtIp.getText().toString().trim();
        if (CommonUtils.isIPAddress(sIp)) {
            String sMask = getNetmask();
            if (!TextUtils.isEmpty(sMask)) {
                String[] ipSplit = sIp.split("\\.");
                String[] maskSplit = sMask.split("\\.");
                if (ipSplit.length == 4 && maskSplit.length == 4) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        int ipBlock = Integer.valueOf(ipSplit[i]) & Integer.valueOf(maskSplit[i]);
                        sb.append(ipBlock);
                        if (i <= 2)
                            sb.append(".");
                    }
                    return sb.toString();
                }
            }
            return sIp;
        }
        return null;
    }

    @Nullable
    public String getNetmask() {
        String s = mEtNetmask.getText().toString().trim();
        if (CommonUtils.isIPAddress(s)) {
            return s;
        }
        try {
            Integer integer = Integer.valueOf(s);
            if (integer >= 0 && integer <= 32) {
                StringBuilder mask = new StringBuilder();
                StringBuilder binary = new StringBuilder();
                for (int i = 32, count = 0; i > 0; i--, integer--, count++) {
                    if (count == 8) {
                        count = 0;
                        mask.append(Integer.valueOf(binary.toString(), 2));
                        mask.append(".");
                        binary.delete(0, binary.length());
                    }
                    if (integer > 0) {
                        binary.append(1);
                    } else {
                        binary.append(0);
                    }
                }
                mask.append(Integer.valueOf(binary.toString(), 2));
                return mask.toString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void setRemoveListener(OnClickListener listener) {
        mViewRemove.setOnClickListener(listener);
    }
}
