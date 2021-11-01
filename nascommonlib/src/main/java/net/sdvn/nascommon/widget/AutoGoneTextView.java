package net.sdvn.nascommon.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class AutoGoneTextView extends TextView {
    public AutoGoneTextView(@NonNull Context context) {
        this(context,null);
    }

    public AutoGoneTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);

    }

    public AutoGoneTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AutoGoneTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (TextUtils.isEmpty(getText()))
            setVisibility(GONE);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    setVisibility(GONE);
                } else {
                    setVisibility(VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

}
