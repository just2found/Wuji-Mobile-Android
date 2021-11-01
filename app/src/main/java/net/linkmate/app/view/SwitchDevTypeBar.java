package net.linkmate.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.linkmate.app.R;
import net.linkmate.app.base.DevBoundType;
import net.linkmate.app.util.Dp2PxUtils;
import net.linkmate.app.util.WindowUtil;

public class SwitchDevTypeBar extends LinearLayout {
    private View contentView;
    private Activity activity;
    private Context context;
    private PopupWindow window;
    private View parent;
    private SwitchListaner switchListaner;
    private int devBoundType;

    private View windowLayout;
    private TextView windowTitle;
    private View windowArrow;
    private TextView viewTitle;
    private View rlCheck;
    private View viewBack;
    private TextView tvCheck1;
    private TextView tvCheck2;
    private TextView tvCheck3;
    private View windowView;

    public SwitchDevTypeBar(Context context) {
        this(context, null);
    }

    public SwitchDevTypeBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchDevTypeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        contentView = View.inflate(context, R.layout.layout_switch_dev_type_bar, this);
        viewTitle = contentView.findViewById(R.id.lsdtb_tv_title);
        contentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(true);
            }
        });
    }

    public void setActivity(Activity activity, View parent) {
        this.activity = activity;
        this.parent = parent;
    }

    public void setWindowLocation(int y) {
        if (window != null && window.isShowing()) {
            window.update(Dp2PxUtils.dp2px(context, 12), y + Dp2PxUtils.dp2px(context, 12), -1, -1);
        }
    }

    public void expand(boolean expand) {
        if (expand) {
            if (window != null && window.isShowing()) {
                return;
            }
            windowView = LayoutInflater.from(context).inflate(R.layout.layout_switch_dev_type_bar_popup, null, false);
            rlCheck = windowView.findViewById(R.id.lsdtbp_rl_check);
            viewBack = windowView.findViewById(R.id.lsdtbp_iv_back);
            windowLayout = windowView.findViewById(R.id.lsdtbp_ll_title);
            windowTitle = windowView.findViewById(R.id.lsdtbp_tv_title);
            windowArrow = windowView.findViewById(R.id.lsdtbp_iv_right_arrow);
            tvCheck1 = windowView.findViewById(R.id.lsdtbp_tv_check_1);
            tvCheck2 = windowView.findViewById(R.id.lsdtbp_tv_check_2);
            tvCheck3 = windowView.findViewById(R.id.lsdtbp_tv_check_3);
            initCheckView();

            window = new PopupWindow(windowView, getWidth() - Dp2PxUtils.dp2px(context, 24), LayoutParams.WRAP_CONTENT, true);
            window.setOutsideTouchable(true);
            window.setTouchable(true);
            window.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean inBar = eventInBar(event);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            if (!inBar) {
                                dismiss();
                                return true;
                            }
                            return false;
                        case MotionEvent.ACTION_DOWN:
                            return !inBar;
                        case MotionEvent.ACTION_MOVE:
                            return true;
                    }
                    return false;
                }
            });
            windowView.setFocusable(true);
            windowView.setFocusableInTouchMode(true);
            windowView.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismiss();
                        return true;
                    }
                    return false;
                }
            });
            windowLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            window.setAnimationStyle(0);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    showShadow(false);
                    parent.setOnTouchListener(null);
                }
            });
//            addOnLayoutChangeListener(new OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    Log.e("onLayoutChange", "left:" + left + " right:" + right +
//                            " top:" + top + " bottom:" + bottom);
//                    Log.e("onLayoutChange", "getLeft:" + getLeft() + " getRight:"
//                            + getRight() + " getTop:" + getTop() + " getBottom:" + getBottom());
//                    getLeft:0 getRight:720 getTop:0 getBottom:144
//                    x:0 y:146 getHeight:144 getWidth:720
//                }
//            });
            showShadow(true);
            window.showAsDropDown(this, Dp2PxUtils.dp2px(context, 12), -getHeight() + Dp2PxUtils.dp2px(context, 12), Gravity.CENTER);
            show();
        } else {
            if (window != null && window.isShowing()) {
                dismiss();
            }
        }
    }

    private void initCheckView() {
        switch (devBoundType) {
            case DevBoundType.IN_THIS_NET:
                refreshCheckViewStyle(tvCheck1, tvCheck2, tvCheck3);
                break;
            case DevBoundType.MY_DEVICES:
                refreshCheckViewStyle(tvCheck2, tvCheck1, tvCheck3);
                break;
            case DevBoundType.SHARED_DEVICES:
                refreshCheckViewStyle(tvCheck3, tvCheck1, tvCheck2);
                break;
            default:
                break;
        }
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.lsdtbp_tv_check_1:
                        refreshCheckViewStyle(tvCheck1, tvCheck2, tvCheck3);
                        switchListaner.onSwitch(DevBoundType.IN_THIS_NET);
                        break;
                    case R.id.lsdtbp_tv_check_2:
                        refreshCheckViewStyle(tvCheck2, tvCheck1, tvCheck3);
                        switchListaner.onSwitch(DevBoundType.MY_DEVICES);
                        break;
                    case R.id.lsdtbp_tv_check_3:
                        refreshCheckViewStyle(tvCheck3, tvCheck1, tvCheck2);
                        switchListaner.onSwitch(DevBoundType.SHARED_DEVICES);
                        break;
                }
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }, 100);
            }
        };
        tvCheck1.setOnClickListener(listener);
        tvCheck2.setOnClickListener(listener);
        tvCheck3.setOnClickListener(listener);
    }

    private boolean animating = false;

    private void show() {
        if (animating)
            return;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                anim(value);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
            }
        });
        animator.start();
    }

    private void dismiss() {
        if (animating || window == null)
            return;
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                anim(value);
//                if (tvCheck1.getTop() < viewBack.getBottom() + Dp2PxUtils.dp2px(getContext(), 4)) {
//                    int bottom = viewBack.getBottom() - Dp2PxUtils.dp2px(getContext(), 4);
//                    if (bottom >= tvCheck1.getTop() + Dp2PxUtils.dp2px(getContext(), 36)) {
//                        bottom = tvCheck1.getTop() + Dp2PxUtils.dp2px(getContext(), 36);
//                    }
//                    tvCheck1.setBottom(bottom);
//                    tvCheck2.setBottom(bottom);
//                    tvCheck3.setBottom(bottom);
//                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
                window.dismiss();
            }
        });
        animator.start();
    }

    private void anim(float value) {
        int height = Dp2PxUtils.dp2px(getContext(), 60);
        windowArrow.setRotation(value * 180);
        ViewGroup.LayoutParams params = viewBack.getLayoutParams();
        params.height = (int) ((value + 1) * 0.5 * height);
        viewBack.setLayoutParams(params);
        tvCheck1.setAlpha(value);
        tvCheck2.setAlpha(value);
        tvCheck3.setAlpha(value);
        rlCheck.setAlpha(value);
    }

    private void refreshCheckViewStyle(TextView primary, TextView light1, TextView light2) {
        viewTitle.setText(primary.getText());
        if (windowTitle != null) {
            windowTitle.setText(primary.getText());
        }
        primary.setBackgroundResource(R.drawable.bg_square_full_radius_primary);
        light1.setBackgroundResource(R.drawable.bg_square_full_radius_light);
        light2.setBackgroundResource(R.drawable.bg_square_full_radius_light);
        primary.setTextColor(getResources().getColor(R.color.white));
        light1.setTextColor(getResources().getColor(R.color.text_dark));
        light2.setTextColor(getResources().getColor(R.color.text_dark));
    }

    public boolean eventInBar(MotionEvent ev) {
        int[] i = new int[2];
        getLocationOnScreen(i);
        int x = i[0];
        int y = i[1];
        float y1 = ev.getRawY();
        int height = Dp2PxUtils.dp2px(getContext(), 92);
        int topMargin = Dp2PxUtils.dp2px(getContext(), 12);
        if (y1 >= (y + topMargin) && y1 <= (y + height + topMargin))
            return true;
        return false;
    }

    public boolean isShowing() {
        return window != null && window.isShowing();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void showShadow(boolean show) {
        if (activity != null) {
            if (show) {
                WindowUtil.showShadow(activity);
            } else {
                WindowUtil.hintShadow(activity);
            }
        }
    }

    public void setDevBoundType(int devBoundType) {
        this.devBoundType = devBoundType;
    }

    public void setSwitchListaner(SwitchListaner switchListaner) {
        this.switchListaner = switchListaner;
    }

    public interface SwitchListaner {
        void onSwitch(int type);
    }
}
