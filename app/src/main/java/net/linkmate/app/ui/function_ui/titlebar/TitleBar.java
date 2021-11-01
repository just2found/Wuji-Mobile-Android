package net.linkmate.app.ui.function_ui.titlebar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import net.linkmate.app.R;
import net.linkmate.app.util.Dp2PxUtils;
import net.linkmate.app.view.SquareImageView;


/**
 * create by: 86136
 * create time: 2021/4/1 10:54
 * Function description:
 * 这个只接受左边图标增加
 */
public class TitleBar extends FrameLayout {
    private ImageView mBackImg;
    private TextView mTitleTv;
    private float mTextSize;
    private int mTextColor;

    private int backBackgroundId = R.drawable.bg_title_bar_gradient;//设置背景颜色的ID
    private int mPadding = 10;//间隔 单位是DP

    private LinearLayout rootView;

    public TitleBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttr(context, attrs);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (getBackground() == null)
            setBackground(context.getDrawable(backBackgroundId));//这里设置背景色，会覆盖掉之前设置的
        if (inflater != null) {
            inflater.inflate(R.layout.component_title_bar, this, true);
            rootView = findViewById(R.id.root_view);
            mBackImg = findViewById(R.id.back_img);
            mTitleTv = findViewById(R.id.title_tv);
        }
        mPadding = Dp2PxUtils.dp2px(getContext(), mPadding);
    }


    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray _TypedArray = context.obtainStyledAttributes(attrs, R.styleable.title_bar);

        mTextColor = _TypedArray.getColor(R.styleable.title_bar_text_color, ContextCompat.getColor(context, R.color.title_text_color));

        mTextSize = _TypedArray.getDimension(R.styleable.title_bar_text_size, 0);
        Drawable drawable  = _TypedArray.getDrawable(R.styleable.title_bar_img_drawable);
        boolean showBack   = _TypedArray.getBoolean(R.styleable.title_bar_show_back, true);
        String textContent = _TypedArray.getString(R.styleable.title_bar_text_content);
        if (!TextUtils.isEmpty(textContent)) mTitleTv.setText(textContent);
        mTitleTv.setTextColor(mTextColor);
        if (mTextSize > 0) mTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        if (showBack) {
            mBackImg.setVisibility(VISIBLE);
            if (drawable != null) mBackImg.setImageDrawable(drawable);
        }
        _TypedArray.recycle();
    }


    //添加右边的按钮事件  也可以直接用addView添加
    @NonNull
    public ImageView addRightImgButton(int resId, @Nullable OnClickListener onClickListener) {
        LayoutParams layoutParams = new LayoutParams(Dp2PxUtils.dp2px(getContext(),48),Dp2PxUtils.dp2px(getContext(),48));
        SquareImageView imageView = new SquareImageView(getContext());
        imageView.setLayoutParams(layoutParams);
        int padding =Dp2PxUtils.dp2px(getContext(),12);
        imageView.setPadding(padding, padding, padding, padding);
        ColorStateList csl= AppCompatResources.getColorStateList(getContext(),R.color.title_icon_color);
        imageView.setImageTintList(csl);
        imageView.setImageTintMode(PorterDuff.Mode.SRC_IN);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), resId));
        imageView.setOnClickListener(onClickListener);
        imageView.setBackgroundResource(R.drawable.bg_ripple_trans_gray);
        rootView.addView(imageView);
        return imageView;
    }


    //添加右边的按钮事件  也可以直接用addView添加
    @NonNull
    public TextView addRightTextButton(@Nullable String text, @Nullable OnClickListener onClickListener) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTv.getTextSize() * 8 / 10);
        textView.setPadding(mPadding, 0, mPadding, 0);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextColor(mTextColor);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundResource(R.drawable.bg_ripple_trans_gray);
        if(onClickListener!=null)
        textView.setOnClickListener(onClickListener);
        rootView.addView(textView);
        return textView;
    }
    @NonNull
    public TextView addRightTextButton(@Nullable String text) {
        return addRightTextButton(text,null);
    }


    public void setBackListener(OnClickListener onClickListener) {
        mBackImg.setOnClickListener(onClickListener);
        if (mBackImg.getVisibility() == GONE) {
            mBackImg.setVisibility(VISIBLE);
        }
    }


    public void setBackViewVisibility(boolean visibility) {
        if (visibility) {
            mBackImg.setVisibility(VISIBLE);
        } else {
            mBackImg.setVisibility(GONE);
        }
    }

    public void setBackViewResId(int resId) {
        mBackImg.setImageDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    public void setTitleText(String str) {
        mTitleTv.setText(str);
    }

    public void setTitleText(int resId) {
        mTitleTv.setText(resId);
    }

}
