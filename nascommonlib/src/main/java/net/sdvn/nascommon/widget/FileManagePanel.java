package net.sdvn.nascommon.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.sdvn.nascommon.model.FileManageAction;
import net.sdvn.nascommon.model.FileManageItem;
import net.sdvn.nascommon.model.FileManageItemGenerator;
import net.sdvn.nascommon.model.oneos.DataFile;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.OneOSFileType;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommon.model.phone.LocalFileType;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FileManagePanel extends FrameLayout {

    private OnFileManageListener mListener;

    private Animation mShowAnim, mHideAnim;
    private View mRootView;
    private int mHeight;
    private RecyclerView mContentRV;
    private BaseQuickAdapter<FileManageItem, BaseViewHolder> adapter;
    private List<?> selectedList;
    private int spanCount;
    private int lineHeight;
    private int defaultBackgroundColor = R.color.transparent;
    private RecyclerView.OnScrollListener mListener1;
    private View mViewLeft;
    private View mViewRight;
    private boolean isShowing;


    public void setPerm(int perm) {
        this.perm = perm;
    }

    private int  perm=-1;//权限的INT交集


    public FileManagePanel(@NonNull Context context) {
        this(context, null);
    }

    public FileManagePanel(@NonNull Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        super(context, attrs);

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_in);
        mHideAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_out);
        mHideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isShown() && !isShowing) {
                    FileManagePanel.this.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView = LayoutInflater.from(context).inflate(R.layout.layout_file_manage, this, true);
//        mContainerLayout = mRootView.findViewById(R.id.layout_root_manage);
//        mContainerLayout.setVisibility(GONE);
        mContentRV = mRootView.findViewById(R.id.content_view);
        mContentRV.setVisibility(VISIBLE);
        lineHeight = (int) (50 * context.getResources().getDisplayMetrics().density + 0.5);
        mHeight = lineHeight;
        spanCount = 5;
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mContentRV.setLayoutManager(gridLayoutManager);
        mViewLeft = mRootView.findViewById(R.id.iv_arrow_left);
        mViewRight = mRootView.findViewById(R.id.iv_arrow_right);

        adapter = new BaseQuickAdapter<FileManageItem, BaseViewHolder>(R.layout.item_grid_operate) {

            @Override
            protected void convert(@NonNull BaseViewHolder helper, @NonNull final FileManageItem item) {
                int txtSize = getResources().getDimensionPixelSize(R.dimen.text_size_minmin);
                ColorStateList txtColors = getResources().getColorStateList(R.color.selector_white_to_gray);
                TextView mButton = helper.itemView.findViewById(R.id.grid_btn);
//                mButton.setId(item.getId());
                mButton.setTag(item.getAction());
                mButton.setText(item.getTxtId());
                mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
                mButton.setTextColor(txtColors);
                // Button mIconImageView with different state
                StateListDrawable drawable = new StateListDrawable();
                drawable.addState(new int[]{}, getResources().getDrawable(item.getNormalIcon()));
                mButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                mButton.setBackground(getResources().getDrawable(R.drawable.bg_ripple_trans_gray));
                mButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        if (Utils.isFastClick(v)) return;
                        if (null != mListener) {
                            mListener.onClick(v, selectedList, item.getAction());
                        }
                    }
                });
            }
        };
        TextView mEmptyTxt = new TextView(getContext());
        mEmptyTxt.setText(R.string.tip_select_file);
        mEmptyTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
        mEmptyTxt.setTextColor(getResources().getColor(R.color.gray));
        mEmptyTxt.setGravity(Gravity.CENTER);
        adapter.setEmptyView(mEmptyTxt);
        mContentRV.setAdapter(adapter);
        //设置默认背景色
        setBackgroundColor(getResources().getColor(defaultBackgroundColor));
        mListener1 = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateRv(recyclerView, mViewLeft, mViewRight);
            }

            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        };
        mContentRV.addOnScrollListener(mListener1);
    }

    private void updateRv(@NotNull RecyclerView recyclerView, View viewLeft, View viewRight) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        //判断是当前layoutManager是否为LinearLayoutManager
        // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            final RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null) {
                final int itemCount = adapter.getItemCount();
                View firstView = linearManager.findViewByPosition(0);
                View lastView = linearManager.findViewByPosition(itemCount - 1);
                boolean b = firstView == null || firstView.getLeft() < 0;
                viewLeft.setVisibility(b ? VISIBLE : GONE);
                boolean b1 = lastView == null || lastView.getRight() > (recyclerView.getWidth() - recyclerView.getPaddingLeft());
                viewRight.setVisibility(b1 ? VISIBLE : GONE);
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        super.measureChildren(widthMeasureSpec, heightMeasureSpec);
    }


    public void setOnOperateListener(OnFileManageListener mListener) {
        this.mListener = mListener;
    }

    public void updatePanelItems(@NotNull OneOSFileType fileType, @NotNull List<DataFile> selectedList, @Nullable LoginSession mLoginSession) {
        this.selectedList = selectedList;

        final ArrayList<FileManageItem> mList;
        if (perm <0) {
            mList = FileManageItemGenerator.INSTANCE.generate(fileType, selectedList, mLoginSession,false);
        } else {
            mList = FileManageItemGenerator.INSTANCE.generate(fileType, selectedList, mLoginSession, perm);
        }

        adapter.setNewData(mList);
//        double ceil = Math.ceil(adapter.getItemCount() * 1f / spanCount);
//        mHeight = (int) (ceil * lineHeight);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRv(mContentRV, mViewLeft, mViewRight);
            }
        }, 50);
    }


    public void updatePanelItems(@NotNull OneOSFileType fileType, @NotNull List<DataFile> selectedList, @Nullable LoginSession mLoginSession,boolean isGroup) {
        this.selectedList = selectedList;

        final ArrayList<FileManageItem> mList;
            mList = FileManageItemGenerator.INSTANCE.generate(fileType, selectedList, mLoginSession,isGroup);
        adapter.setNewData(mList);
//        double ceil = Math.ceil(adapter.getItemCount() * 1f / spanCount);
//        mHeight = (int) (ceil * lineHeight);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                updateRv(mContentRV, mViewLeft, mViewRight);
            }
        }, 50);
    }

    public void updatePanelItems(OneOSFileType fileType, @NonNull final ArrayList<DataFile> selectedList) {

//
//        this.mContainerLayout.removeAllViews();
//        if (EmptyUtils.isEmpty(mList)) {
//            TextView mEmptyTxt = new TextView(getContext());
//            mEmptyTxt.setTitleText(R.string.tip_select_file);
//            mEmptyTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
//            mEmptyTxt.setTextColor(getResources().getColor(R.color.gray));
//            mContainerLayout.addView(mEmptyTxt);
//            return;
//        }
//
//        int padding = Utils.dipToPx(2);
//        int txtSize = getResources().getDimensionPixelSize(R.dimen.text_size_min);
//        ColorStateList txtColors = getResources().getColorStateList(R.color.selector_gray_to_primary);
//        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
//        for (FileManageItem item : mList) {
//            Button mButton = new Button(getContext());
//            mButton.setId(item.getId());
//            mButton.setTag(item.getAction());
//            mButton.setTitleText(item.getTxtId());
//            mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
//            mButton.setTextColor(txtColors);
//            mButton.setLayoutParams(mLayoutParams);
//            // Button mIconImageView with different state
//            StateListDrawable drawable = new StateListDrawable();
//            drawable.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(item.getPressedIcon()));
//            drawable.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(item.getPressedIcon()));
//            drawable.addState(new int[]{}, getResources().getDrawable(item.getNormalIcon()));
//            mButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
//            mButton.setBackgroundResource(android.R.color.transparent);
//            mButton.setPadding(padding, padding, padding, padding);
//            mButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onItemClick(View v) {
//                    if (null != mListener) {
//                        mListener.onItemClick(v, selectedList, (FileManageAction) v.getTag());
//                    }
//                }
//            });
//            mContainerLayout.addView(mButton);
//        }
    }

    public void updatePanelItemsMore(OneOSFileType fileType, final ArrayList<OneOSFile> selectedList) {
//        ArrayList<FileManageItem> mList = FileManageItemGenerator.generateMore(fileType, selectedList);
//        this.mContainerLayout.removeAllViews();
//        if (EmptyUtils.isEmpty(mList)) {
//            TextView mEmptyTxt = new TextView(getContext());
//            mEmptyTxt.setTitleText(R.string.tip_select_file);
//            mEmptyTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
//            mEmptyTxt.setTextColor(getResources().getColor(R.color.gray));
//            mContainerLayout.addView(mEmptyTxt);
//            return;
//        }
//
//        int padding = Utils.dipToPx(2);
//        int txtSize = getResources().getDimensionPixelSize(R.dimen.text_size_min);
//        ColorStateList txtColors = getResources().getColorStateList(R.color.selector_gray_to_primary);
//        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
//        for (FileManageItem item : mList) {
//            Button mButton = new Button(getContext());
//            mButton.setId(item.getId());
//            mButton.setTag(item.getAction());
//            mButton.setTitleText(item.getTxtId());
//            mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
//            mButton.setTextColor(txtColors);
//            mButton.setLayoutParams(mLayoutParams);
//            // Button mIconImageView with different state
//            StateListDrawable drawable = new StateListDrawable();
//            drawable.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(item.getPressedIcon()));
//            drawable.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(item.getPressedIcon()));
//            drawable.addState(new int[]{}, getResources().getDrawable(item.getNormalIcon()));
//            mButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
//            mButton.setBackgroundResource(android.R.color.transparent);
//            mButton.setPadding(padding, padding, padding, padding);
//            mButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onItemClick(View v) {
//                    if (null != mListener) {
//                        mListener.onItemClick(v, selectedList, (FileManageAction) v.getTag());
//                    }
//                }
//            });
//            mContainerLayout.addView(mButton);
//        }
    }

    public void updatePanelItems(LocalFileType fileType, @NonNull final ArrayList<LocalFile> selectedList) {
        this.selectedList = selectedList;

        ArrayList<FileManageItem> mList = FileManageItemGenerator.INSTANCE.generate(fileType, selectedList);
        adapter.setNewData(mList);
//        double ceil = Math.ceil(adapter.getItemCount() * 1f / spanCount);
//        mHeight = (int) (ceil * lineHeight);
//        this.mContainerLayout.removeAllViews();
//        if (EmptyUtils.isEmpty(mList)) {
//            TextView mEmptyTxt = new TextView(getContext());
//            mEmptyTxt.setTitleText(R.string.tip_select_file);
//            mEmptyTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
//            mEmptyTxt.setTextColor(getResources().getColor(R.color.gray));
//            mContainerLayout.addView(mEmptyTxt);
//            return;
//        }
//
//        int padding = Utils.dipToPx(2);
//        int txtSize = getResources().getDimensionPixelSize(R.dimen.text_size_min);
//        ColorStateList txtColors = getResources().getColorStateList(R.color.selector_gray_to_primary);
//        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
//        for (FileManageItem item : mList) {
//            Button mButton = new Button(getContext());
//            mButton.setId(item.getId());
//            mButton.setTag(item.getAction());
//            mButton.setTitleText(item.getTxtId());
//            mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
//            mButton.setTextColor(txtColors);
//            mButton.setLayoutParams(mLayoutParams);
//            // Button mIconImageView with different state
//            StateListDrawable drawable = new StateListDrawable();
//            drawable.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(item.getPressedIcon()));
//            drawable.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(item.getPressedIcon()));
//            drawable.addState(new int[]{}, getResources().getDrawable(item.getNormalIcon()));
//            mButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
//            mButton.setBackgroundResource(android.R.color.transparent);
//            mButton.setPadding(padding, padding, padding, padding);
//            mButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onItemClick(View v) {
//                    if (null != mListener) {
//                        mListener.onItemClick(v, selectedList, (FileManageAction) v.getTag());
//                    }
//                }
//            });
//            mContainerLayout.addView(mButton);
//        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void showPanel() {
        isShowing = true;
        if (!this.isShown()) {
            FileManagePanel.this.setVisibility(View.VISIBLE);
//            mRootView.setPadding(0, 0, 0, -mHeight);
            showOrHidePanel(true);
        }
    }

    public void hidePanel() {
        isShowing = false;
        if (this.isShown()) {
            showOrHidePanel(false);

            if (mListener != null) {
                mListener.onDismiss();
            }
        }
    }

    private void showOrHidePanel(final boolean isShow) {
        if (isShow) {
            this.startAnimation(mShowAnim);
        } else {
            this.startAnimation(mHideAnim);
        }

//        int firstInt;
//        int finalInt;
//        if (isShow) {
//            firstInt = mHeight;
//            finalInt = 0;
//        } else {
//            firstInt = 0;
//            finalInt = mHeight;
//        }
//        ValueAnimator animator = ValueAnimator.ofInt(firstInt, finalInt);
//        animator.setDuration(300);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
//                int newPadding = (int) animation.getAnimatedValue();
//                mRootView.setPadding(0, 0, 0, -newPadding);
//            }
//        });
//        animator.start();
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                if (isShow) {
//                    FileManagePanel.this.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (!isShow) {
//                    FileManagePanel.this.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//            }
//        });
    }


    public interface OnFileManageListener<T> {
        void onClick(View view, List<T> selectedList, FileManageAction action);

        void onDismiss();
    }

}
