package net.sdvn.nascommon.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.accessibility.AccessibilityEventCompat;

import net.sdvn.nascommonlib.R;


/**
 * Created by gaoyun@eli-tech.com on 2016/3/14.
 */
public class ImageCheckBox extends LinearLayout implements Checkable {

    public ImageView getIv() {
        return iv;
    }

    private SquareImageView iv;
    private TextView tv;
    private OnImageCheckedChangedListener listener;
    private int checkedBgId;
    private int uncheckedBgId;
    private int textId;
    private boolean checked;
    private Context context;

    public ImageCheckBox(Context context) {
        this(context, null);
    }

    public ImageCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view = View.inflate(context, R.layout.layout_image_check_box, this);
        iv = view.findViewById(R.id.image_check_box_img);
        tv = view.findViewById(R.id.image_check_box_tv);
        init(attrs);
        setFocusable(true);
    }

    private void init(AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.ImageCheckBox);
        checked = t.getBoolean(R.styleable.ImageCheckBox_checked, false);
        checkedBgId = t.getResourceId(R.styleable.ImageCheckBox_checkedResId, 0);
        uncheckedBgId = t.getResourceId(R.styleable.ImageCheckBox_uncheckedResId, 0);
        textId = t.getResourceId(R.styleable.ImageCheckBox_bottomText, 0);
        t.recycle();
        if (textId != 0)
            tv.setText(textId);

        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                checked = !checked;
                updateCheckState();
                if (null != listener) {
                    listener.onChecked(ImageCheckBox.this, checked);
                }
            }
        });
        updateCheckState();
    }

    private void updateCheckState() {
        if (checked) {
            iv.setImageResource(checkedBgId);
            tv.setTextColor(context.getResources().getColor(R.color.tab_blue));
        } else {
            iv.setImageResource(uncheckedBgId);
            tv.setTextColor(context.getResources().getColor(R.color.tab_gray));
        }
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            refreshDrawableState();
            sendAccessibilityEvent(AccessibilityEventCompat.TYPE_WINDOW_CONTENT_CHANGED);
        }
    }

    public void setOnImageCheckedChangedListener(OnImageCheckedChangedListener listener) {
        this.listener = listener;
    }

    public interface OnImageCheckedChangedListener {
        void onChecked(ImageCheckBox imageView, boolean checked);
    }
}
