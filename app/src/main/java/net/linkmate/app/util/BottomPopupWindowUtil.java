package net.linkmate.app.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.linkmate.app.R;
import net.linkmate.app.view.FormRowLayout;
import net.linkmate.app.view.TextViewLayout;

import java.util.List;

public class BottomPopupWindowUtil {

    public static void showSelectDialog(final Activity activity, int[] texts, BPWonItemClickListener listener) {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.bottom_popup_window, null, false);
        PopupWindow popWin = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popWin.setOutsideTouchable(true);
        popWin.setTouchable(true);

        View.OnClickListener dismissListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWin.dismiss();
            }
        };
        View cancel = contentView.findViewById(R.id.pop_account_tv_cancel);
        cancel.setOnClickListener(dismissListener);

        LinearLayout llContent = contentView.findViewById(R.id.layout_content);
        for (int text : texts) {
            TextViewLayout tvl = new TextViewLayout(activity);
            tvl.setText(text);
            tvl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(text, popWin);
                }
            });
            llContent.addView(tvl);
        }

        popWin.setAnimationStyle(R.style.BottomPopupWindow);
        popWin.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        popWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowUtil.hintShadow(activity);
            }
        });
        WindowUtil.showShadow(activity);
    }

    public interface BPWonItemClickListener {
        void onClick(int txtId, PopupWindow popupWindow);
    }
}
