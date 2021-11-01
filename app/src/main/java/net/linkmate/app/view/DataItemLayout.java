package net.linkmate.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import net.linkmate.app.R;

public class DataItemLayout extends LinearLayout {
    public TextView mTv;
    public TextView mTvData;
    public ImageView mIv;
    private View contentView;

    public DataItemLayout(Context context) {
        this(context, null);
    }

    public DataItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contentView = View.inflate(context, R.layout.item_date_layout, this);
        mTv = contentView.findViewById(R.id.dil_tv);
        mTvData = contentView.findViewById(R.id.dil_tv_data);
        mIv = contentView.findViewById(R.id.dil_iv);


        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.dil);
        int dil_bg = typedArray.getColor(R.styleable.dil_bg, -1);
        if (dil_bg != -1)
            contentView.setBackgroundColor(dil_bg);
        else
            contentView.setBackgroundResource(R.color.color_bg_grey50);

        int dil_iv_src = typedArray.getResourceId(R.styleable.dil_iv_src, -1);
        if (dil_iv_src != -1) {
            mIv.setImageResource(dil_iv_src);
            mIv.setVisibility(VISIBLE);
        } else {
            mIv.setVisibility(GONE);
        }

        String dil_tv_text = typedArray.getString(R.styleable.dil_tv_text);
        mTv.setText(dil_tv_text);

        String dil_tv_data_text = typedArray.getString(R.styleable.dil_tv_data_text);
        mTvData.setText(dil_tv_data_text);

        typedArray.recycle();
    }

    public DataItemLayout setTitle(CharSequence text) {
        mTv.setText(text);
        return this;
    }

    public DataItemLayout setText(CharSequence text) {
        mTvData.setText(text);
        return this;
    }

    public DataItemLayout setTextColorId(@ColorRes int color) {
        mTvData.setTextColor(getResources().getColor(color));
        return this;
    }

    public void setDataOnClickListener(@Nullable OnClickListener l) {
        mTvData.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (l != null)
                    l.onClick(contentView);
            }
        });
    }
}
