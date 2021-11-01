package net.linkmate.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import net.linkmate.app.R;

public class TitleTextView extends RelativeLayout {
    private RelativeLayout content;
    private ImageView ivIcon;
    private TextView tvTitle;
    private TextView tvContent;

    public TitleTextView(Context context) {
        this(context, null);
    }

    public TitleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        content = (RelativeLayout) View.inflate(context, R.layout.view_title_text, this);
        ivIcon = content.findViewById(R.id.vtt_iv_icon);
        tvTitle = content.findViewById(R.id.vtt_tv_title);
        tvContent = content.findViewById(R.id.tvv_tv_content);


        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ttv);

        int ial_iv_icon_visibility = typedArray.getInt(R.styleable.ttv_ttv_icon_visibility, VISIBLE);
        ivIcon.setVisibility(ial_iv_icon_visibility);
        int ial_iv_icon_src = typedArray.getResourceId(R.styleable.ttv_ttv_icon_src, -1);
        if (ial_iv_icon_src != -1)
            ivIcon.setImageResource(ial_iv_icon_src);

        String ial_tv_title_text = typedArray.getString(R.styleable.ttv_ttv_title_text);
        tvTitle.setText(ial_tv_title_text);

        String ial_tv_tips_text = typedArray.getString(R.styleable.ttv_ttv_content_text);
        if (TextUtils.isEmpty(ial_tv_tips_text)) {
            tvContent.setVisibility(GONE);
        } else {
            tvContent.setVisibility(VISIBLE);
            tvContent.setText(ial_tv_tips_text);
        }
        typedArray.recycle();
    }

    public void setTitle(@StringRes int resid) {
        tvTitle.setText(resid);
        tvTitle.setVisibility(VISIBLE);
    }

    public void setTitle(CharSequence text) {
        tvTitle.setText(text);
        tvTitle.setVisibility(VISIBLE);
    }

    public void setContent(@StringRes int resid) {
        tvContent.setText(resid);
        tvContent.setVisibility(VISIBLE);
    }

    public void setContent(CharSequence text) {
        tvContent.setText(text);
        tvContent.setVisibility(VISIBLE);
    }
}
