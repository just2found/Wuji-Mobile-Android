package net.linkmate.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.linkmate.app.R;


public class FormRowLayout extends LinearLayout {

    private TextView mTvTitle;
    private TextView mTvContent;
    private LinearLayout mRlBg;
    private Context mContext;

    public FormRowLayout(Context context) {
        this(context, null);
    }

    public FormRowLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FormRowLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.widget_form_row_layout, this);
        mRlBg = view.findViewById(R.id.subnet_widget_background);
        mTvTitle = view.findViewById(R.id.from_row_tv_title);
        mTvContent = view.findViewById(R.id.from_row_tv_content);
    }

    @Override
    public void setBackgroundColor(int color) {
        mRlBg.setBackgroundColor(color);
    }

    public TextView getTitle() {
        return mTvTitle;
    }

    public TextView getContent() {
        return mTvContent;
    }

    public static class FormRowDate {
        public String title;
        public String content;

        public FormRowDate(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}
