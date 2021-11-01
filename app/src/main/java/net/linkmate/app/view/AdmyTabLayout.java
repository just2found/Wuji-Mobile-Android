package net.linkmate.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.linkmate.app.R;

import java.util.ArrayList;
import java.util.List;

public class AdmyTabLayout extends LinearLayout {
    private List<String> tabStrings;
    private Context mContext;
    private float mTabTvTextSize = 12;
    private int mTabTvTextColorId = R.color.colorPrimary;
    private int mTabIndicatorBgColor = R.color.colorPrimary;

    public AdmyTabLayout(Context context) {
        this(context, null);
    }

    public AdmyTabLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmyTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setOrientation(HORIZONTAL);
        tabStrings = new ArrayList<>();
    }

    private void initTabLayout() {
        for (int i = 0; i < tabStrings.size(); i++) {
            LinearLayout tabLl = new LinearLayout(mContext);
            tabLl.setOrientation(VERTICAL);
            TextView tabTv = new TextView(mContext);
            tabTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTabTvTextSize);
            tabTv.setTextColor(mContext.getResources().getColor(mTabTvTextColorId));
            tabTv.setText(tabStrings.get(i));

            View tabIndicator = new View(mContext);
            tabIndicator.setBackgroundColor(mContext.getResources().getColor(mTabIndicatorBgColor));
            LayoutParams params = (LayoutParams) tabIndicator.getLayoutParams();
            params.width = tabTv.getWidth();
        }
    }

//    class TabView extends LinearLayout {
//        public TabView(Context context) {
//            this(context, null);
//        }
//
//        public TabView(Context context, @Nullable AttributeSet attrs) {
//            this(context, attrs, 0);
//        }
//
//        public TabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//            super(context, attrs, defStyleAttr);
//        }
//    }
}
