package io.weline.mediaplayer.internal.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import io.weline.mediaplayer.R;


public class VolumeDialog extends BaseDialog {

    protected ProgressBar volumeProgressBar;

    public VolumeDialog(@NonNull Context context) {
        super(context);
    }

    public VolumeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_volume;
    }

    @Override
    protected void initView(View view) {
        volumeProgressBar = view.findViewById(R.id.volumn_progress_bar);
    }

    public void showVolumeDialog(int volumeProgress, View anchorView) {

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP | Gravity.START;

        layoutParams.width = dp2px(getContext(), 200);
        layoutParams.height = dp2px(getContext(), 100);

        int videoWidth = anchorView.getWidth();
        int videoHeight = anchorView.getHeight();

        int statusBarHeight = dp2px(getContext(), 25);

        int[] location = getViewLocation(anchorView);

        layoutParams.x = location[0] + videoWidth / 2 - layoutParams.width / 2 - (videoWidth>videoHeight?statusBarHeight:0);
        layoutParams.y = location[1] + videoHeight / 2 - layoutParams.height / 2 - (videoWidth>videoHeight?0:statusBarHeight);
        getWindow().setAttributes(layoutParams);

        volumeProgressBar.setProgress(volumeProgress);

        if (!isShowing()) {
            show();
        }
    }

}
