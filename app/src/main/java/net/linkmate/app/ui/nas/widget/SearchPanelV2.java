package net.linkmate.app.ui.nas.widget;

import android.widget.RelativeLayout;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.sdvn.nascommon.widget.ClearEditText;

import libs.source.common.utils.InputMethodUtils;
import libs.source.common.utils.ToastHelper;

public class SearchPanelV2 extends RelativeLayout {

    private View mCancelBtn;
    private ClearEditText mSearchTxt;
    private OnSearchActionListener mListener;
    private Animation mShowAnim, mHideAnim;

    public SearchPanelV2(@NonNull Context context) {
        super(context);
    }

    public SearchPanelV2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_search_panel_v2, this, true);

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.push_top_in);
        mHideAnim = AnimationUtils.loadAnimation(context, R.anim.push_top_out);

        mSearchTxt = view.findViewById(R.id.search_edit);
        mCancelBtn = view.findViewById(R.id.btn_cancel_search);

        mSearchTxt.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(@NonNull TextView v, int actionId, KeyEvent event) {
                onSearch(v);
                return true;
            }
        });
        mSearchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                findViewById(R.id.search_edit_hint).setVisibility(mSearchTxt.getText().length() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        view.findViewById(R.id.iv_search).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch(mSearchTxt);
            }
        });

        mCancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(mSearchTxt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                hidePanel(true);
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
    }

    private void onSearch(@NonNull TextView v) {
        String filter = v.getText().toString().trim();
        if (!TextUtils.isEmpty(filter)) {
            if (mListener != null) {
                mListener.onSearch(filter);
            }
            hideKeyboard();
        } else {
            ToastHelper.showLongToastSafe(R.string.hint_search);
        }
    }

    public void cancel() {
        mCancelBtn.callOnClick();
    }

    public void showKeyboard() {
        mSearchTxt.requestFocus();
        InputMethodUtils.showKeyboard(getContext(), mSearchTxt);
    }

    public void hideKeyboard() {
        mSearchTxt.clearFocus();
        InputMethodUtils.hideKeyboard(getContext(), mSearchTxt);
    }

    public void setOnSearchListener(OnSearchActionListener mListener) {
        this.mListener = mListener;
    }

    public void setEnabled(boolean enabled) {
        mSearchTxt.setEnabled(enabled);
        mCancelBtn.setEnabled(enabled);
    }

    @Nullable
    public String getSearchFilter() {
        if (mSearchTxt == null) {
            return null;
        }

        return mSearchTxt.getText().toString().trim();
    }

    public void showPanel(boolean isAnim) {
        showPanel(isAnim, true);
    }

    /**
     * show search panel if is invisible
     *
     * @param isAnim if show with animation
     */
    public void showPanel(boolean isAnim, boolean showKeyBoard) {
        if (this.isShown()) {
            return;
        }

        this.setVisibility(View.VISIBLE);
        if (isAnim) {
            this.startAnimation(mShowAnim);
            mShowAnim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mListener != null) {
                        mListener.onVisible(true);
                    }
                    if (showKeyBoard) {
                        showKeyboard();
                    }
                }
            });
        } else {
            if (showKeyBoard) {
                showKeyboard();
            }
        }
    }

    public void hidePanel(boolean isAnim) {
        if (!this.isShown()) {
            return;
        }

        mSearchTxt.setText("");
        this.setVisibility(View.GONE);
        if (isAnim) {
            this.startAnimation(mHideAnim);
            mHideAnim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    hideKeyboard();
                }
            });
        } else {
            hideKeyboard();
        }

        if (mListener != null) {
            mListener.onVisible(false);
        }
    }

    public interface OnSearchActionListener {
        void onVisible(boolean visible);

        void onSearch(String filter);

        void onCancel();
    }
}
