package org.view.libwidget.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.view.libwidget.R;

import java.util.regex.Pattern;


/**
 * create by: 86136
 * create time: 2021/3/31 16:25
 * Function description:
 */

public class InputItem1 extends FrameLayout {

    private static int NOT_SET = -1;

    private static String regNumLetter6_20 = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";
    private static String regNumLetterAndChar = "^(?=.*([a-zA-Z].*))(?=.*[0-9].*)(?=.*([*/+.,<>~!@#$%^&()_=?;:'\"`\\[\\]{}|\\\\-]).*)[a-zA-Z0-9-*/+.,<>~!@#$%^&()_=?;:'\"`\\[\\]{}|\\\\-]{8,32}$";

    private int maxLength = -1;

    private EditText mEditText;
    private ImageView mImageView;
    private ImageView mClearView;
    private View mSeparatorView;

    private int mTextColor;
    private int mSeparatorColor;
    private int mSeparatorFocusColor;
    private Drawable mDrawable;
    private Drawable mDrawable1;
    private String mSameStr;
    private int mNonComplianceColor;
    private Pattern mCompile;//正则表达式约束字符串


    public MutableLiveData<CheckData> liveData;

    private boolean isChecked;

    public InputItem1(@NonNull Context context) {
        super(context);
        init(context);
    }

    public InputItem1(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttr(context, attrs);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (inflater != null) {
            inflater.inflate(R.layout.component_input_item1, this, true);
            mEditText = findViewById(R.id.edit);
            mImageView = findViewById(R.id.status_img);
            mSeparatorView = findViewById(R.id.separator_view);
            mClearView = findViewById(R.id.clear_img);
        }
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.requestFocus();
            }
        });
        liveData = new MutableLiveData<>();
    }


    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray _TypedArray = context.obtainStyledAttributes(attrs, R.styleable.input_item);
        mTextColor = _TypedArray.getColor(R.styleable.input_item_text_color, NOT_SET);

        float TextSize = _TypedArray.getDimension(R.styleable.input_item_text_size, NOT_SET);

        mDrawable = _TypedArray.getDrawable(R.styleable.input_item_img_drawable);
        mDrawable1 = _TypedArray.getDrawable(R.styleable.input_item_img_drawable1);

        Drawable clearDrawable = _TypedArray.getDrawable(R.styleable.input_item_clear_drawable);

        mSeparatorColor = _TypedArray.getColor(R.styleable.input_item_separator_color, NOT_SET);
        mSeparatorFocusColor = _TypedArray.getColor(R.styleable.input_item_separator_focus_color, NOT_SET);
        if (mSeparatorColor == NOT_SET) mSeparatorColor = Color.parseColor("#B3B3B3");
        if (mSeparatorFocusColor == NOT_SET) mSeparatorFocusColor = Color.parseColor("#0C81FB");

        int RegularCheckType = _TypedArray.getInt(R.styleable.input_item_regular_check, NOT_SET);
        getRegularCheck(RegularCheckType);

        mNonComplianceColor = _TypedArray.getColor(R.styleable.input_item_non_compliance_color, NOT_SET);
        if (mNonComplianceColor == NOT_SET) mNonComplianceColor = Color.RED;

        String hint = _TypedArray.getString(R.styleable.input_item_text_hint);
        String content = _TypedArray.getString(R.styleable.input_item_text_content);

        if (mTextColor != NOT_SET) mEditText.setTextColor(mTextColor);
        if (TextSize != NOT_SET) mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, TextSize);

        if (!TextUtils.isEmpty(hint)) mEditText.setHint(hint);
        if (!TextUtils.isEmpty(content)) mEditText.setText(content);

        if (mDrawable != null) {
            mImageView.setVisibility(VISIBLE);
            mImageView.setImageDrawable(mDrawable);
        }
        if (clearDrawable != null) {
            mClearView.setVisibility(VISIBLE);
            mClearView.setImageDrawable(clearDrawable);
            mClearView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditText.setText("");
                }
            });
        }

        if(mImageView.getVisibility()!=VISIBLE && mClearView.getVisibility()==VISIBLE)
        {
            FrameLayout.LayoutParams params= (LayoutParams) mClearView.getLayoutParams();
            params.setMarginEnd(0);
            mClearView.setLayoutParams(params);
        }

        //根据有限显示的图标数动态设置PADDING
        if (mImageView.getVisibility() == GONE && mClearView.getVisibility() == GONE) {
            mEditText.setPadding(mEditText.getPaddingLeft(), mEditText.getPaddingTop(), mEditText.getPaddingRight() / 10, mEditText.getPaddingBottom());
        } else if (mImageView.getVisibility() == VISIBLE && mClearView.getVisibility() == VISIBLE) {
        } else {
            mEditText.setPadding(mEditText.getPaddingLeft(), mEditText.getPaddingTop(), mEditText.getPaddingRight() / 2, mEditText.getPaddingBottom());
        }

        if (mSeparatorColor != NOT_SET) mSeparatorView.setBackgroundColor(mSeparatorColor);

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String valueStr = mEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(valueStr)) {
                        mEditText.setTextColor(mTextColor);
                        mSeparatorView.setBackgroundColor(mSeparatorColor);
                        liveData.postValue(new CheckData(false, valueStr));
                    } else if (!TextUtils.isEmpty(mSameStr)) {
                        if (mSameStr.equals(valueStr)) {
                            mEditText.setTextColor(mTextColor);
                            mSeparatorView.setBackgroundColor(mSeparatorColor);
                            liveData.postValue(new CheckData(true, valueStr));
                        } else {
                            mEditText.setTextColor(mNonComplianceColor);
                            mSeparatorView.setBackgroundColor(mNonComplianceColor);
                            liveData.postValue(new CheckData(false, valueStr));
                        }
                    } else if (mCompile != null) {
                        if (mCompile.matcher(valueStr).matches()) {
                            mEditText.setTextColor(mTextColor);
                            mSeparatorView.setBackgroundColor(mSeparatorColor);
                            liveData.postValue(new CheckData(true, valueStr));
                        } else {
                            mEditText.setTextColor(mNonComplianceColor);
                            mSeparatorView.setBackgroundColor(mNonComplianceColor);
                            liveData.postValue(new CheckData(false, valueStr));
                        }
                    } else if (maxLength > 0) {
                        if (valueStr.length() > maxLength) {
                            mEditText.setTextColor(mNonComplianceColor);
                            mSeparatorView.setBackgroundColor(mNonComplianceColor);
                            liveData.postValue(new CheckData(false, valueStr));
                        } else {
                            mEditText.setTextColor(mTextColor);
                            mSeparatorView.setBackgroundColor(mSeparatorColor);
                            liveData.postValue(new CheckData(true, valueStr));
                        }
                    } else {
                        mEditText.setTextColor(mTextColor);
                        mSeparatorView.setBackgroundColor(mSeparatorColor);
                        liveData.postValue(new CheckData(true, valueStr));
                    }
                } else {
                    mEditText.setTextColor(mTextColor);
                    mSeparatorView.setBackgroundColor(mSeparatorFocusColor);
                }
            }
        });
        if (mDrawable != null && mDrawable1 != null) {
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
            isChecked = false;
            mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    isChecked = !isChecked;
                    int position = mEditText.getSelectionStart();
                    if (isChecked) {
                        mEditText.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        mImageView.setImageDrawable(mDrawable1);
                    } else {
                        mEditText.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        mImageView.setImageDrawable(mDrawable);
                    }
                    mEditText.setSelection(position);
                }
            });
        }
        _TypedArray.recycle();
    }


    private void getRegularCheck(int regularCheckType) {
        switch (regularCheckType) {
            case 1:
                mCompile = Pattern.compile(regNumLetter6_20);
                maxLength = 20;
                break;
            case 2:
                maxLength = 32;
                break;
            case 3:
                mCompile = Pattern.compile(regNumLetterAndChar);
                maxLength = 32;
                break;
            default:
        }
        return;
    }

    public boolean checkData() {
        if (!TextUtils.isEmpty(mSameStr)) {
            if (mSameStr.equals(mEditText.getText().toString())) {
                return true;
            } else {
                return false;
            }
        } else if (mCompile != null) {
            if (mCompile.matcher(mEditText.getText().toString()).matches()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public String getTextValue() {
        return mEditText.getText().toString().trim();
    }


    @Nullable
    public String getCheckValue() {
        String valueStr = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(valueStr)) {
            mEditText.setTextColor(mTextColor);
            mSeparatorView.setBackgroundColor(mSeparatorColor);
            return null;
        } else if (!TextUtils.isEmpty(mSameStr)) {
            if (mSameStr.equals(valueStr)) {
                mEditText.setTextColor(mTextColor);
                mSeparatorView.setBackgroundColor(mSeparatorColor);
                return valueStr;
            } else {
                mEditText.setTextColor(mNonComplianceColor);
                mSeparatorView.setBackgroundColor(mNonComplianceColor);
                return null;
            }
        } else if (mCompile != null) {
            if (mCompile.matcher(valueStr).matches()) {
                mEditText.setTextColor(mTextColor);
                mSeparatorView.setBackgroundColor(mSeparatorColor);
                return valueStr;
            } else {
                mEditText.setTextColor(mNonComplianceColor);
                mSeparatorView.setBackgroundColor(mNonComplianceColor);
                return null;
            }
        } else if (maxLength > 0) {
            if (valueStr.length() > maxLength) {
                mEditText.setTextColor(mNonComplianceColor);
                mSeparatorView.setBackgroundColor(mNonComplianceColor);
                liveData.postValue(new CheckData(false, valueStr));
                return null;
            } else {
                mEditText.setTextColor(mTextColor);
                mSeparatorView.setBackgroundColor(mSeparatorColor);
                liveData.postValue(new CheckData(true, valueStr));
                return valueStr;
            }
        } else {
            mEditText.setTextColor(mTextColor);
            mSeparatorView.setBackgroundColor(mSeparatorColor);
            return valueStr;
        }

    }


//    public CheckData getCheckData() {
//        String valueStr = mEditText.getText().toString().trim();
//        if (TextUtils.isEmpty(valueStr)) {
//            mEditText.setTextColor(mTextColor);
//            mSeparatorView.setBackgroundColor(mSeparatorColor);
//            return new CheckData(false, valueStr);
//        } else if (!TextUtils.isEmpty(mSameStr)) {
//            if (mSameStr.equals(valueStr)) {
//                mEditText.setTextColor(mTextColor);
//                mSeparatorView.setBackgroundColor(mSeparatorColor);
//                return new CheckData(true, valueStr);
//            } else {
//                mEditText.setTextColor(mNonComplianceColor);
//                mSeparatorView.setBackgroundColor(mNonComplianceColor);
//                return new CheckData(false, valueStr);
//            }
//        } else if (mCompile != null) {
//            if (mCompile.matcher(valueStr).matches()) {
//                mEditText.setTextColor(mTextColor);
//                mSeparatorView.setBackgroundColor(mSeparatorColor);
//                return new CheckData(true, valueStr);
//            } else {
//                mEditText.setTextColor(mNonComplianceColor);
//                mSeparatorView.setBackgroundColor(mNonComplianceColor);
//                return new CheckData(false, valueStr);
//            }
//        } else {
//            mEditText.setTextColor(mTextColor);
//            mSeparatorView.setBackgroundColor(mSeparatorColor);
//            return new CheckData(true, valueStr);
//        }
//    }

    public void setSameStr(String sameStr) {
        mSameStr = sameStr;
        String valueStr = mEditText.getText().toString().trim();
        if (hasFocus()) {
            return;
        }
        if (!TextUtils.isEmpty(mSameStr)) {
            if (mSameStr.equals(valueStr)) {
                mEditText.setTextColor(mTextColor);
                mSeparatorView.setBackgroundColor(mSeparatorColor);
                liveData.postValue(new CheckData(true, valueStr));
            } else {
                mEditText.setTextColor(mNonComplianceColor);
                mSeparatorView.setBackgroundColor(mNonComplianceColor);
                liveData.postValue(new CheckData(false, valueStr));
            }
        }

    }
}
