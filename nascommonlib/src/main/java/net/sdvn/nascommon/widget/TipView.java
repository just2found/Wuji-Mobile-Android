package net.sdvn.nascommon.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;


public class TipView {

    private static final String TAG = TipView.class.getSimpleName();

    private static final int DEFAULT_TIME = 1200;


    private PopupWindow mTipPop;

    private TipView() {
    }


    @Nullable
    public static TipView show(@NonNull Context context, View parent, int msgId, boolean isPositive) {
        return show(context, parent, context.getResources().getString(msgId), isPositive, null);
    }

    @Nullable
    public static TipView show(Context context, View parent, String msg, boolean isPositive) {
        return show(context, parent, msg, isPositive, null);
    }

    @Nullable
    public static TipView show(@NonNull Context context, View parent, int msgId, boolean isPositive, PopupWindow.OnDismissListener listener) {
        return show(context, parent, context.getResources().getString(msgId), isPositive, listener);
    }

    @Nullable
    public static TipView show(@Nullable Context context, @Nullable View parent, String msg, boolean isPositive, PopupWindow.OnDismissListener listener) {
        if (context == null || parent == null) {
            return null;
        }
        if (!parent.isActivated()) {
            return null;
        }
        final TipView tipView = new TipView();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pop_tip, null);
        tipView.mTipPop = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tipView.mTipPop.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        if (!EmptyUtils.isEmpty(msg)) {
            TextView mTipsTxt = view.findViewById(R.id.txt_tip);
            mTipsTxt.setText(msg);
            mTipsTxt.setVisibility(View.VISIBLE);
        }
        ImageView mImageView = view.findViewById(R.id.iv_tip);
        mImageView.setImageResource(isPositive ? R.drawable.ic_tip_positive : R.drawable.ic_tip_negtive);

        tipView.mTipPop.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
        tipView.mTipPop.setOnDismissListener(listener);
        tipView.mTipPop.showAtLocation(parent, Gravity.CENTER, 0, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tipView.dismiss();
            }
        }, DEFAULT_TIME);
        return tipView;
    }

    public void dismiss() {
        if (mTipPop != null && mTipPop.isShowing()) {
            mTipPop.dismiss();
            Logger.LOGD(TAG, " ----- pop dismiss -------");
        }
    }

    public boolean isShown() {
        return mTipPop != null && mTipPop.isShowing();
    }

}
