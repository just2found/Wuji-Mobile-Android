package net.sdvn.nascommon.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

@SuppressLint("AppCompatCustomView")
public class ScaleImageView extends ImageView {

    private final static float sScaleValue = 0.9f;

    public ScaleImageView(Context context) {
        this(context, null);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchSetSelected(boolean selected) {
        super.dispatchSetSelected(selected);
        if (selected) {
            onItemFocus();
        } else {
            onItemGetNormal();
        }
    }

    protected void onItemFocus() {

        if (Build.VERSION.SDK_INT >= 21) {
            //抬高Z轴
            ViewCompat.animate(this).scaleX(sScaleValue).scaleY(sScaleValue).translationZ(1).start();
        } else {
            ViewCompat.animate(this).scaleX(sScaleValue).scaleY(sScaleValue).start();
            ViewGroup parent = (ViewGroup) this.getParent();
            parent.requestLayout();
            parent.invalidate();
        }
    }

    protected void onItemGetNormal() {

        if (Build.VERSION.SDK_INT >= 21) {
            ViewCompat.animate(this).scaleX(1.0f).scaleY(1.0f).translationZ(0).start();
        } else {
            ViewCompat.animate(this).scaleX(1.0f).scaleY(1.0f).start();
            ViewGroup parent = (ViewGroup) this.getParent();
            if (parent != null) {
                parent.requestLayout();
                parent.invalidate();
            }
        }

    }

}
