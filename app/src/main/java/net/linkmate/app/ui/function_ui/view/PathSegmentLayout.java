package net.linkmate.app.ui.function_ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.sdvn.nascommonlib.R;

import java.io.File;

/**
 * create by: nwq
 * create time: 2021/4/21 13:53
 * Function description:路劲进行分段式展示方便进行回退
 */

public class PathSegmentLayout extends LinearLayout {


    private String[] pathItems;
    private SparseArray<TextView> textViews = new SparseArray<>();
    private ColorStateList csl;
    private OnClickListener mOnClickListener;
    private int pathMaxWidth = 0, pathMinWidth = 0, pathBtnPadding = 0;
    private int mStartPosition = 0;
    private OnPathPanelClickListener mListener;
    private String prefix = null;

    public PathSegmentLayout(Context context) {
        super(context);
        init();
    }

    /*
     * 设置path 前缀
     * */
    public void setPrefix(@Nullable String prefix) {
        this.prefix = prefix;
    }

    public void setOnPathPanelClickListener(OnPathPanelClickListener mListener) {
        this.mListener = mListener;
    }

    public PathSegmentLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PathSegmentLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    //入口
    // textView.setText(pathItems[i]);
    public void setPath(String pathStr) {
        pathStr = pathStr.replaceAll(File.separator + File.separator, File.separator);
        removeAllViews();
        pathItems = pathStr.split(File.separator);
        if (prefix != null) {
            TextView textView = getTextView(Integer.MAX_VALUE);
            textView.setText(prefix);
            addView(textView);
            textView.setOnClickListener(v -> {
                mListener.onClick(v, null);
            });
        }
        for (int i = 0; i < pathItems.length; ++i) {
            TextView textView = getTextView(i);
            textView.setText(pathItems[i]);
            addView(textView);
            textView.setOnClickListener(getOnClickListener());
        }
    }


    public void setStartPosition(int position) {
        mStartPosition = position;
    }


    private ColorStateList getColorStateList() {
        if (csl == null) {
            csl = getResources().getColorStateList(R.color.selector_black_to_primary);
        }
        return csl;
    }


    public Boolean canBack() {
        if (pathItems != null && pathItems.length > 1) {
            mListener.onClick(this, builderPathStr(mStartPosition, pathItems.length - 2));
            return true;
        } else if (prefix != null && pathItems.length > 0) {
            mListener.onClick(this, null);
            return true;
        } else
            return false;
    }

    private void init() {
        pathMaxWidth = dipToPx(120);
        pathMinWidth = dipToPx(30);
        pathBtnPadding = dipToPx(5);
        setOrientation(LinearLayout.HORIZONTAL);
    }


    private int dipToPx(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private TextView getTextView(int position) {
        TextView textView = textViews.get(position);
        if (textView == null) {
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            textView = new TextView(getContext());
            textView.setLayoutParams(layoutParams);
            textView.setTag(position);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
            textView.setMaxWidth(pathMaxWidth);
            textView.setMinWidth(pathMinWidth);
            textView.setPadding(pathBtnPadding, 0, pathBtnPadding, 0);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(getColorStateList());
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.bg_path_item);
            textViews.put(position, textView);
        }
        return textView;
    }


    private OnClickListener getOnClickListener() {
        if (mOnClickListener == null) {
            mOnClickListener = v -> {
                int i = (Integer) v.getTag();
                mListener.onClick(v, builderPathStr(mStartPosition, i));
            };
        }
        return mOnClickListener;
    }

    private String builderPathStr(int startPosition, int endPosition) {
        StringBuilder tarPath = new StringBuilder(File.separator);
        for (; startPosition <= endPosition; startPosition++) {
            tarPath.append(pathItems[startPosition]).append(File.separator);
        }
        return tarPath.toString();
    }


    /**
     * 这个是给外部用的接口
     */
    public interface OnPathPanelClickListener {
        void onClick(View view, @Nullable String path);
    }
}
