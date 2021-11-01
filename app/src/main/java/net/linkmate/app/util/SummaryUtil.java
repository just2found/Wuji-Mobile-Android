package net.linkmate.app.util;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.SectionEntity;

import net.linkmate.app.R;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.util.CommonUtils;
import net.sdvn.nascommon.model.KeyPair;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.InputMethodUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.widget.CheckableImageButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SummaryUtil {

    /**
     * 展示简介
     * @param context 上下文
     * @param title 简介标题
     * @param content   简介内容
     * @param editable  可否编辑
     * @param listener  编辑完成的回调
     */
    public static void showSummary(final Context context, String title, String content, boolean editable,
                                   final OnCommitEditListener listener) {
        final View dialogView = View.inflate(context, R.layout.dialog_summary, null);
        final Dialog mDialog = new Dialog(context, R.style.DialogTheme);
        final TextView tvTitle = dialogView.findViewById(R.id.text_title);
        final ImageView imgSetting = dialogView.findViewById(R.id.img_setting);
        final TextView tvContent = dialogView.findViewById(R.id.tv_content);
        final EditText etContent = dialogView.findViewById(R.id.et_content);
        final TextView tvPositive = dialogView.findViewById(R.id.positive);
        final TextView tvNegative = dialogView.findViewById(R.id.negative);
        final Group groupNegative = dialogView.findViewById(R.id.group_negative);

        final boolean[] isEdit = {false};
        tvTitle.setText(title);
        if (!TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        }
        if (editable) {
            etContent.setText(content);
            imgSetting.setVisibility(View.VISIBLE);
            imgSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isEdit[0] = true;
                    tvContent.setVisibility(View.GONE);
                    etContent.setVisibility(View.VISIBLE);
                    imgSetting.setVisibility(View.GONE);
                    groupNegative.setVisibility(View.VISIBLE);
                    InputMethodUtils.showKeyboard(context, etContent, 200);
                }
            });
        }
        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEdit[0]) {
                    mDialog.dismiss();
                } else {
                    if (listener != null)
                        listener.OnCommit(etContent.getText().toString().trim(), mDialog);
                }
            }
        });
        tvNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    public interface OnCommitEditListener {
        void OnCommit(String strEdit, Dialog dialog);
    }
}
