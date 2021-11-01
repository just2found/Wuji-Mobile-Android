package io.weline.mediaplayer.internal.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.weline.mediaplayer.R;


public class SeekDialog extends BaseDialog {

    private TextView progressText;

    public SeekDialog(@NonNull Context context) {
        super(context);
    }

    public SeekDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_seek;
    }

    @Override
    protected void initView(View view) {
        progressText = view.findViewById(R.id.progress_text);
    }

    public void showSeekDialog(String progress, View anchorView) {

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP | Gravity.START;

        layoutParams.width = dp2px(getContext(), 120);
        layoutParams.height = dp2px(getContext(), 60);

        int videoWidth = anchorView.getWidth();
        int videoHeight = anchorView.getHeight();

        int statusBarHeight = dp2px(getContext(), 25);

        int[] location = getViewLocation(anchorView);

        layoutParams.x = location[0] + videoWidth / 2 - layoutParams.width / 2 - (videoWidth>videoHeight?statusBarHeight:0);
        layoutParams.y = location[1] + videoHeight / 2 - layoutParams.height / 2 - (videoWidth>videoHeight?0:statusBarHeight);
        getWindow().setAttributes(layoutParams);

        progressText.setText(progress);

        if (!isShowing()) {
            show();
        }
    }

}
