package net.sdvn.nascommon.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.sdvn.nascommonlib.R;


/**
 * Title back layout
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class TitleBackLayout extends ConstraintLayout {
//    private SparseArray<View> viewCache = new SparseArray<>();
    private ViewGroup mBackLayout;
    @NonNull
    public TextView mTitleTxt, mBackTxt, mTxtRight;
    @NonNull
    public ImageView mBackIBtn, mRightIBtn, mRightIBtn1;

    public TitleBackLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TitleBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (inflater != null) {
            inflater.inflate(R.layout.layout_title_back, this, true);
            mBackLayout = findViewById(R.id.layout_title_left);
            mTitleTxt = findViewById(R.id.txt_title);
            mBackTxt = findViewById(R.id.txt_title_back);
            mBackIBtn = findViewById(R.id.ibtn_back);
            mRightIBtn = findViewById(R.id.ibtn_title_right);
            mRightIBtn1 = findViewById(R.id.ibtn_title_right1);
            mTxtRight = findViewById(R.id.txt_title_right);
        }
    }

    /**
     * Set click left back
     *
     * @param activity
     */
    public void setOnClickBack(@Nullable final Activity activity) {
        if (null != activity) {
           /* mBackLayout.setPositiveButton(new OnClickListener() {
                @Override
                public void onItemClick(View v) {
                    activity.finish();
                }
            });*/
            mBackIBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.finish();
                }
            });
//            StatusBarUtils.from(activity).setTransparentStatusBar(true).setActionbarView(this).process();
        }
    }

    public TitleBackLayout setOnBackClickListener(OnClickListener listener) {
        mBackLayout.setOnClickListener(listener);
        return this;
    }

    public void setOnRightClickListener(OnClickListener listener) {
        mRightIBtn.setOnClickListener(listener);
    }

    public void setOnRight1ClickListener(OnClickListener listener) {
        mRightIBtn1.setOnClickListener(listener);
    }

    @NonNull
    public TitleBackLayout setTitle(@StringRes int resid) {
        mTitleTxt.setText(resid);
        return this;
    }

    @NonNull
    public TitleBackLayout setTitle(String title) {
        mTitleTxt.setText(title);
        return this;
    }

    @NonNull
    public TitleBackLayout setBackTitle(@StringRes int resid) {
        mBackTxt.setText(resid);
        return this;
    }

    @NonNull
    public TitleBackLayout setBackTitle(@Nullable String str) {
        mBackTxt.setText(str);
        return this;
    }

    @NonNull
    public TitleBackLayout setBackVisible(boolean visible) {
        mBackLayout.setVisibility(visible ? VISIBLE : GONE);
        return this;
    }

    @NonNull
    public TitleBackLayout setRightButton(@DrawableRes int resid) {
        mRightIBtn.setImageResource(resid);
        return this;
    }

    @NonNull
    public TitleBackLayout setRightButtonVisible(int visibility) {
        mRightIBtn.setVisibility(visibility);
        return this;
    }

    @NonNull
    public TitleBackLayout setRightButton1(@DrawableRes int resid) {
        mRightIBtn1.setImageResource(resid);
        return this;
    }

    @NonNull
    public TitleBackLayout setRightButton1Visible(int visibility) {
        mRightIBtn1.setVisibility(visibility);
        return this;
    }

    @NonNull
    public TitleBackLayout setRightTextVisible(int visibility) {
        mTxtRight.setVisibility(visibility);
        return this;
    }

    @NonNull
    public TitleBackLayout setRightText(@StringRes int resId) {
        mTxtRight.setText(resId);
        return this;
    }

    @NonNull
    public TitleBackLayout setRightText(String content) {
        mTxtRight.setText(content);
        return this;
    }

    public TitleBackLayout setOnRightTextClickListener(OnClickListener listener) {
        mTxtRight.setOnClickListener(listener);
        return this;
    }

//    public <V extends View> V findViewByIdRes(@IdRes int viewId) {
//        View v = viewCache.get(viewId);
//        V view = null;
//        if (v == null) {
//            view = findViewById(viewId);
//            if (view == null) {
//                String entryName = getResources().getResourceEntryName(viewId);
//                throw new NullPointerException("id: R.id." + entryName + "can not find in this item!");
//            }
//            viewCache.put(viewId, view);
//        } else {
//            view = (V) v;
//        }
//        return view;
//    }
}
