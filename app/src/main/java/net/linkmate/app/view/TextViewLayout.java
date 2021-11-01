package net.linkmate.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.linkmate.app.R;


public class TextViewLayout extends LinearLayout {

    private TextView mText;
    private Context mContext;

    public TextViewLayout(Context context) {
        this(context, null);
    }

    public TextViewLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.text_view_layout, this);
        mText = view.findViewById(R.id.tvl_tv);
    }

    public void setText(@StringRes int resid) {
        mText.setText(resid);
    }
}
