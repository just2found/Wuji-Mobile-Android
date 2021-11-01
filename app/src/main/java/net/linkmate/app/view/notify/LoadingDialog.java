package net.linkmate.app.view.notify;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import net.linkmate.app.R;
import net.linkmate.app.view.ProgressView;


public class LoadingDialog extends Dialog {

    private TextView mTipTextView;
    private ImageView mIBCancel;

    public LoadingDialog(@NonNull Context context) {
        super(context);

    }

    public LoadingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public LoadingDialog(Context context, boolean cancelable, String msg) {
        super(context, R.style.loading_dialog);


        View contentView = getContentView();
        mTipTextView.setText(msg);// 设置加载信息

        this.setCancelable(cancelable);// 不可以用“返回键”取消
        this.setContentView(contentView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        Point point = new Point();
        dialogWindow.getWindowManager().getDefaultDisplay().getSize(point);
        lp.width = point.x;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);

    }

    @NonNull
    private View getContentView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog, null);// 得到加载view
        // xml中的ProgressView
        ImageView ivLoadingImage = view.findViewById(R.id.img);
        // 提示文字
        mTipTextView = view.findViewById(R.id.tipTextView);
        mIBCancel = view.findViewById(R.id.dialog_ib_cancel);
        //加载gif
        Glide.with(getContext()).asGif().load(R.drawable.loading).transition(DrawableTransitionOptions.withCrossFade()).into(ivLoadingImage);

        return view;
    }

    public void setTipMessage(String msg) {
        if (TextUtils.isEmpty(msg))
            mTipTextView.setVisibility(View.GONE);
        else
            mTipTextView.setVisibility(View.VISIBLE);
        mTipTextView.setText(msg);
    }

    public void setCancelVisibility(boolean show) {
        mIBCancel.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        mIBCancel.setOnClickListener(listener);
    }


}
