package net.sdvn.nascommon.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.oneos.aria.AriaUtils;
import net.sdvn.nascommonlib.R;

import java.util.HashMap;
import java.util.Objects;

public class SettingsPopupView {

    private PopupWindow mPopupMenu;
    private EditText mUpSpeedTxt, mDownSpeedTxt, mCountTxt, mPieceTxt;

    public SettingsPopupView(@NonNull Context context, OnClickListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_aria_settings, null);
        RelativeLayout mBackLayout = view.findViewById(R.id.layout_list);
        mBackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button mSaveBtn = view.findViewById(R.id.btn_save_settings);
        mSaveBtn.setOnClickListener(listener);

        mUpSpeedTxt = view.findViewById(R.id.et_download);
        mDownSpeedTxt = view.findViewById(R.id.et_upload);
        mCountTxt = view.findViewById(R.id.et_max_count);
        mPieceTxt = view.findViewById(R.id.et_piece_len);

        mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupMenu.setAnimationStyle(R.style.AnimationAlphaEnterAndExit);
        mPopupMenu.setTouchable(true);
        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
    }

    public void dismiss() {
        if (mPopupMenu != null && mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        }
    }

    public void showPopupCenter(View parent) {
        mPopupMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.update();
    }

    @Nullable
    private HashMap<String, String> mParamsMap = null;

    public void updateSettings(@NonNull HashMap<String, String> paramsMap) {
        this.mParamsMap = paramsMap;
        mUpSpeedTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT));
        mDownSpeedTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT));
        mCountTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT));
        mPieceTxt.setText(paramsMap.get(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE));
    }

    @Nullable
    public HashMap<String, String> getChangeSettings() {
        if (this.mParamsMap == null) {
            return null;
        }

        HashMap<String, String> newParamsMap = new HashMap<String, String>();
        String oUpSpeed = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT);
        String nUpSpeed = mUpSpeedTxt.getText().toString().trim();
        if (!Objects.equals(oUpSpeed, nUpSpeed)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT, nUpSpeed);
        }

        String oDownSpeed = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT);
        String nDownSpeed = mDownSpeedTxt.getText().toString().trim();
        if (!Objects.equals(oDownSpeed, nDownSpeed)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT, nDownSpeed);
        }

        String oCount = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT);
        String nCount = mCountTxt.getText().toString().trim();
        if (!Objects.equals(oCount, nCount)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT, nCount);
        }

        String oPiece = this.mParamsMap.get(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE);
        String nPiece = mPieceTxt.getText().toString().trim();
        if (!Objects.equals(oPiece, nPiece)) {
            newParamsMap.put(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE, nPiece);
        }

        return newParamsMap;
    }

}
