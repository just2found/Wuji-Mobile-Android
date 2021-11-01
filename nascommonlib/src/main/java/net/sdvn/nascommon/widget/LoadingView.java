package net.sdvn.nascommon.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.lang.ref.WeakReference;

public class LoadingView {

    private static final String TAG = LoadingView.class.getSimpleName();

    private static final int NO_RESOURCES_ID = 0;
    private static final boolean DEFAULT_CANCELABLE = false;

    private static class LoadingViewHolder {
        static LoadingView INSTANCE = new LoadingView();
    }

    @Nullable
    private WeakReference<LoadingProgressDialog> mProgressDialog;

    private LoadingView() {
    }

    @NonNull
    public static LoadingView getInstance() {
        return LoadingViewHolder.INSTANCE;
    }

    public void show(Context context) {
        show(context, NO_RESOURCES_ID);
    }

    public void show(Context context, int msgId) {
        show(context, msgId, DEFAULT_CANCELABLE);
    }

    public void show(Context context, int msgId, boolean cancelable) {
        show(context, msgId, cancelable, -1, null);
    }

    public void show(Context context, int msgId, long timeout, DialogInterface.OnDismissListener listener) {
        show(context, msgId, false, timeout, listener);
    }

    public void show(@Nullable Context context, int msgId, boolean cancelable, long timeout, DialogInterface.OnDismissListener listener) {
        if (context == null) {
            return;
        }
        dismiss();
        LoadingProgressDialog dialog = new LoadingProgressDialog(context, msgId, cancelable, timeout);
        mProgressDialog = new WeakReference<>(dialog);
        dialog.setOnDismissListener(listener);
        try {
            dialog.show();
        } catch (Exception e) {
            Logger.LOGE(TAG, "TipDialog Exception: ", e);
        }
    }

    public void dismiss() {
        if (mProgressDialog != null) {
            final LoadingProgressDialog dialog = mProgressDialog.get();
            if (dialog != null)
                dialog.cancel();
            mProgressDialog = null;
        }
    }

    public void detachContext() {
        dismiss();
        mProgressDialog = null;
    }

    public boolean isShown() {
        if (mProgressDialog != null) {
            final LoadingProgressDialog dialog = mProgressDialog.get();
            if (dialog != null)
                return dialog.isShowing();
        }
        return false;
    }


    private static class LoadingProgressDialog extends Dialog {
        private ImageView mCircleProgressBar;
        private TextView mTipsTxt;
        private String tip;
        private long timeout = -1;
        private boolean isCancelable = DEFAULT_CANCELABLE;
        private Handler handler; /*{
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    if (timeout <= 0) {
                        dismiss();
                    } else {
                        mTipsTxt.setTitleText(tip + timeout + "s");
                    }
                }
            }
        }*/


        public LoadingProgressDialog(@NonNull Context context, int msgId, boolean isCancelable, long timeout) {
            super(context, R.style.loading_dialog);
            this.timeout = timeout;
            this.isCancelable = isCancelable;
            if (msgId > 0) {
                tip = getContext().getResources().getString(msgId);
            }
            handler = new Handler();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            View contentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_loading, null);// 得到加载view;
            setContentView(contentView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
            //加载gif
            mCircleProgressBar = findViewById(R.id.progressBar);

            Glide.with(getContext())
                    .asGif()
                    .load(R.drawable.loading)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mCircleProgressBar);

            if (!EmptyUtils.isEmpty(tip)) {
                mTipsTxt = findViewById(R.id.txt_tips);
                if (timeout > 0) {
                    handler.post(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            try {
                                if (timeout <= 0) {
                                    dismiss();
                                } else {
                                    mTipsTxt.setText(tip + timeout + "s");
                                }
                                handler.postDelayed(this, 1000);
                                timeout--;
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.w(TAG, "Timer thread error...");
                            }
                        }
                    });
                } else {
                    mTipsTxt.setText(tip);
                }
                mTipsTxt.setVisibility(View.VISIBLE);
            }
            findViewById(R.id.dialog_ib_cancel).setOnClickListener(v -> {
                dismiss();
            });
            setScreenBrightness();

            this.setCancelable(isCancelable);
            this.setCanceledOnTouchOutside(isCancelable);
        }

        private void setScreenBrightness() {
            Window window = getWindow();
            WindowManager.LayoutParams lp = null;
            if (window != null) {
                lp = window.getAttributes();
                lp.dimAmount = 0f;
                window.setAttributes(lp);
            }
        }

        @Override
        protected void onStart() {
            super.onStart();
            if (mCircleProgressBar != null) {
                mCircleProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            if (mCircleProgressBar != null) {
                mCircleProgressBar.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void dismiss() {
            super.dismiss();
            handler.removeCallbacksAndMessages(null);
        }
    }

}
