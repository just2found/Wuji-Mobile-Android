package net.linkmate.app.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

import timber.log.Timber;

public class CarouselView extends FrameLayout implements ViewPager.OnPageChangeListener {
    private Drawable mUnselectedDrawable;
    private Drawable mSelectedDrawable;
    private List<Integer> mImageList;
    private innerViewPager mViewPager;
    private ImageAdapter mAdapter;
    private boolean mAutoScroll = true;
    private boolean showIndicator = false;
    private ImageLoader mImageLoader;
    private int mScrollInterval = 3000; //自动轮播间隔3秒
    private int mCurrentItem;
    private OnPageClickListener mOnPageClickListener;
    private RecyclerView indicatorContainer;
    private IndicatorAdapter indicatorAdapter;
    private int bannerSize = 0;
    private int mSelectedColor = 0xFF11A3FD;
    private int mUnselectedColor = Color.GRAY;

    public CarouselView(Context context) {
        this(context, null);
    }

    public CarouselView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarouselView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (mSelectedDrawable == null) {
            //绘制默认选中状态图形
            GradientDrawable selectedGradientDrawable = new GradientDrawable();
            selectedGradientDrawable.setShape(GradientDrawable.OVAL);
            selectedGradientDrawable.setColor(mSelectedColor);
            selectedGradientDrawable.setSize(dp2px(5), dp2px(5));
            selectedGradientDrawable.setCornerRadius(dp2px(5) / 2f);
            mSelectedDrawable = new LayerDrawable(new Drawable[]{selectedGradientDrawable});
        }
        if (mUnselectedDrawable == null) {
            //绘制默认未选中状态图形
            GradientDrawable unSelectedGradientDrawable = new GradientDrawable();
            unSelectedGradientDrawable.setShape(GradientDrawable.OVAL);
            unSelectedGradientDrawable.setColor(mUnselectedColor);
            unSelectedGradientDrawable.setSize(dp2px(5), dp2px(5));
            unSelectedGradientDrawable.setCornerRadius(dp2px(5) / 2f);
            mUnselectedDrawable = new LayerDrawable(new Drawable[]{unSelectedGradientDrawable});
        }
    }

    /**
     * 必须实现图片加载功能
     *
     * @param imageLoader
     * @return
     */
    public CarouselView initImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
        return this;
    }

    protected int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    public void setImageList(List<Integer> imageList) {
        mImageList = imageList;
        if (mViewPager == null) {
            mViewPager = new innerViewPager(getContext());
            this.addView(mViewPager);
            mViewPager.setLayoutParams(new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mAdapter = new ImageAdapter();
            mViewPager.setAdapter(mAdapter);
            mCurrentItem = mImageList.size() * 50000;
            mViewPager.setCurrentItem(mCurrentItem);
            mViewPager.addOnPageChangeListener(this);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        bannerSize = imageList.size();
        if (showIndicator && bannerSize > 1) {
            showIndicatorContainer();
        }

        if (mAutoScroll && bannerSize > 1) {
            openAutoScroll();
        } else {
            closeAutoScroll();
        }
    }

    private void showIndicatorContainer() {
        if (indicatorContainer == null) {
            //指示器部分
            indicatorContainer = new RecyclerView(getContext());
            LinearLayoutManager indicatorLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            indicatorContainer.setLayoutManager(indicatorLayoutManager);
            indicatorAdapter = new IndicatorAdapter();
            indicatorContainer.setAdapter(indicatorAdapter);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.bottomMargin = dp2px(6);
            addView(indicatorContainer, params);
            Timber.d("add indicatorContainer");
        }
        indicatorContainer.setVisibility(showIndicator ? VISIBLE : GONE);
        indicatorAdapter.notifyDataSetChanged();
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void setOnPageClickListener(OnPageClickListener onPageClickListener) {
        mOnPageClickListener = onPageClickListener;
    }

    //设置是否显示指示器
    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
        if (showIndicator && mViewPager != null) {
            showIndicatorContainer();
        }
        if (indicatorContainer != null) {
            indicatorContainer.setVisibility(showIndicator ? VISIBLE : GONE);
        }
    }

    /**
     * 设置自动轮播间隔时长
     *
     * @param interval 单位 ms
     */
    public void setScrollInterval(int interval) {
        mScrollInterval = interval;
    }

    /**
     * 开启自动轮播
     */
    public void openAutoScroll() {
        mAutoScroll = true;
        autoScroll();
    }

    /**
     * 关闭自动轮播
     */
    public void closeAutoScroll() {
        mAutoScroll = false;
        removeCallbacks(autoScrollMission);
    }

    private void autoScroll() {
        removeCallbacks(autoScrollMission);
        postDelayed(autoScrollMission, mScrollInterval);
    }

    private Runnable autoScrollMission = new Runnable() {

        @Override
        public void run() {
            if (mImageList != null) {
                int size = mImageList.size();
                if (size > 1) {
                    mCurrentItem++;
                    if (mCurrentItem > size * 100000L) {
                        mCurrentItem = size * 50000 + 1;
                    }
                    mViewPager.setCurrentItem(mCurrentItem);
                }
            }
            if (mAutoScroll) {
                autoScroll();
            }
        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentItem = position;
        if (showIndicator) {
            refreshIndicator();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class ImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (mImageList != null) {
                if (mImageList.size() > 1) {
                    return mImageList.size() * 100000;
                } else {
                    return mImageList.size();
                }
            }
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int i = position % mImageList.size();
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            container.addView(imageView);
            picLoader(imageView, mImageList.get(i));
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private void picLoader(ImageView imageView, int imageResId) {
            if (mImageLoader != null) {
                mImageLoader.loadImage(imageView, imageResId);
            }
        }

    }

    private class innerViewPager extends ViewPager {
        private GestureDetector mGestureDetector =
                new GestureDetector(getContext(),
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                if (mOnPageClickListener == null) return false;
                                int index = mCurrentItem % mImageList.size();
                                mOnPageClickListener.onClick(index, mImageList.get(index));
                                return true;
                            }
                        });

        public innerViewPager(Context context) {
            this(context, null);
        }

        public innerViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (mGestureDetector.onTouchEvent(ev)) {
                return true;
            }
            if (!mAutoScroll) return super.onTouchEvent(ev);
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //暂时关闭自动滑动
                    removeCallbacks(autoScrollMission);
                    break;
                case MotionEvent.ACTION_UP:
                    //开启自动滑动
                    autoScroll();
                    break;
            }
            return super.onTouchEvent(ev);
        }
    }

    /**
     * 标示点适配器
     */
    protected class IndicatorAdapter extends RecyclerView.Adapter {

        int currentPosition = 0;

        public void setPosition(int currentPosition) {
            this.currentPosition = currentPosition;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ImageView bannerPoint = new ImageView(getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            int indicatorMargin = dp2px(2);
            lp.setMargins(indicatorMargin, indicatorMargin, indicatorMargin, indicatorMargin);
            bannerPoint.setLayoutParams(lp);
            return new RecyclerView.ViewHolder(bannerPoint) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ImageView bannerPoint = (ImageView) holder.itemView;
            bannerPoint.setImageDrawable(currentPosition == position ? mSelectedDrawable : mUnselectedDrawable);

        }

        @Override
        public int getItemCount() {
            return bannerSize;
        }
    }

    /**
     * 改变导航的指示点
     */
    protected synchronized void refreshIndicator() {
        if (showIndicator && bannerSize > 1) {
            indicatorAdapter.setPosition(mCurrentItem % bannerSize);
            indicatorAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 图片加载器用户自己实现
     */
    public interface ImageLoader {
        void loadImage(ImageView imageView, int imageResId);
    }

    public interface OnPageClickListener {
        void onClick(int index, int imageResId);
    }
}