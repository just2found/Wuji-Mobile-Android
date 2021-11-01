package net.linkmate.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import net.linkmate.app.R;

public class ActivityItemLayout extends RelativeLayout {
    private RelativeLayout content;
    private ImageView ivIcon;
    private TextView tvTitle;
    private CusTextView tvTips;
    private ImageView ivRightArrow;

    public ActivityItemLayout(Context context) {
        this(context, null);
    }

    public ActivityItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        content = (RelativeLayout) View.inflate(context, R.layout.item_activity_layout, this);
        ivIcon = content.findViewById(R.id.ial_iv_icon);
        tvTitle = content.findViewById(R.id.ial_tv_title);
        tvTips = content.findViewById(R.id.ial_tv_tips);
        ivRightArrow = content.findViewById(R.id.ial_iv_right_arrow);


        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ial);
        if (typedArray!=null) {
            boolean ial_background_radius_top = typedArray.getBoolean(R.styleable.ial_background_radius_top, false);
            if (ial_background_radius_top) {
                content.setBackgroundColor(Color.TRANSPARENT);
                content.setBackgroundResource(R.drawable.bg_button_white_radius_top_14);
            } else {
                int ial_background_color = typedArray.getColor(R.styleable.ial_background_color, -1);
                if (ial_background_color != -1)
                    content.setBackgroundColor(ial_background_color);
                else
                    content.setBackgroundResource(R.color.bg_white);
            }
            boolean ial_background_radius_bottom = typedArray.getBoolean(R.styleable.ial_background_radius_bottom, false);
            if (ial_background_radius_bottom) {
                content.setBackgroundColor(Color.TRANSPARENT);
                content.setBackgroundResource(R.drawable.bg_button_white_radius_bottom_14);
            } else {
                int ial_background_color = typedArray.getColor(R.styleable.ial_background_color, -1);
                if (ial_background_color != -1)
                    content.setBackgroundColor(ial_background_color);
                else
                    content.setBackgroundResource(R.color.bg_white);
            }

            boolean ial_background_radius = typedArray.getBoolean(R.styleable.ial_background_radius, false);
            if (ial_background_radius) {
                content.setBackgroundColor(Color.TRANSPARENT);
                content.setBackgroundResource(R.drawable.bg_button_white_radius_14);
            } else {
                int ial_background_color = typedArray.getColor(R.styleable.ial_background_color, -1);
                if (ial_background_color != -1)
                    content.setBackgroundColor(ial_background_color);
                else
                    content.setBackgroundResource(R.color.bg_white);
            }

            int ial_iv_icon_visibility = typedArray.getInt(R.styleable.ial_iv_icon_visibility, VISIBLE);
            ivIcon.setVisibility(ial_iv_icon_visibility);
            int ial_iv_icon_src = typedArray.getResourceId(R.styleable.ial_iv_icon_src, -1);
            if (ial_iv_icon_src != -1)
                ivIcon.setImageResource(ial_iv_icon_src);

            String ial_tv_title_text = typedArray.getString(R.styleable.ial_tv_title_text);
            tvTitle.setText(ial_tv_title_text);

            String ial_tv_tips_text = typedArray.getString(R.styleable.ial_tv_tips_text);
            int ial_tv_tips_text_color = typedArray.getColor(R.styleable.ial_tv_tips_text_color, -1);
            if (ial_tv_tips_text_color != -1)
                tvTips.setTextColor(ial_tv_tips_text_color);

            if (TextUtils.isEmpty(ial_tv_tips_text)) {
                tvTips.setVisibility(GONE);
            } else {
                tvTips.setVisibility(VISIBLE);
                tvTips.setText(ial_tv_tips_text);
            }

            int ial_iv_right_arrow_visibility = typedArray.getInt(R.styleable.ial_iv_right_arrow_visibility, VISIBLE);
            ivRightArrow.setVisibility(ial_iv_right_arrow_visibility);

            typedArray.recycle();
        }
    }

    public void setTips(@StringRes int resid) {
        tvTips.setText(resid);
        tvTips.setVisibility(VISIBLE);
    }

    public void setTips(CharSequence text) {
        tvTips.setText(text);
        tvTips.setVisibility(VISIBLE);
    }

    public void setTitle(@StringRes int resid) {
        tvTitle.setText(resid);
        tvTitle.setVisibility(VISIBLE);
    }

    public void setTitle(CharSequence text) {
        tvTitle.setText(text);
        tvTitle.setVisibility(VISIBLE);
    }

    public CusTextView getTvTips() {
        return tvTips;
    }
}
