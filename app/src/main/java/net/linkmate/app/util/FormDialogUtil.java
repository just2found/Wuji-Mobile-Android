package net.linkmate.app.util;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.linkmate.app.R;
import net.linkmate.app.view.FormRowLayout;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FormDialogUtil {

    public static void showSelectDialog(final Context context, int title, List<FormRowLayout.FormRowDate> dates,
                                        int posi, final OnDialogButtonClickListener posiListener,
                                        int nega, final OnDialogButtonClickListener negaListener) {
        showSelectDialog(context,
                title > 0 ? context.getString(title) : null,
                dates,
                posi > 0 ? context.getString(posi) : null,
                posiListener,
                nega > 0 ? context.getString(nega) : null,
                negaListener);
    }

    public static void showSelectDialog(final Context context, String title, List<FormRowLayout.FormRowDate> dates,
                                        String posi, final OnDialogButtonClickListener posiListener,
                                        String nega, final OnDialogButtonClickListener negaListener) {
        final View dialogView = View.inflate(context, R.layout.dialog_form, null);
        final Dialog mDialog = new Dialog(context, R.style.DialogTheme);

        if (!TextUtils.isEmpty(title)) {
            final TextView tvTitle = dialogView.findViewById(R.id.df_tv_title);
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }

        LinearLayout llContent = dialogView.findViewById(R.id.layout_content);
        for (FormRowLayout.FormRowDate date : dates) {
            FormRowLayout formRow = createFormRow(context, date);
            llContent.addView(formRow);
        }

        if (!TextUtils.isEmpty(posi)) {
            TextView positiveBtn = dialogView.findViewById(R.id.positive);
            positiveBtn.setText(posi);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (posiListener != null) {
                        posiListener.onClick(v, mDialog);
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        }

        if (!TextUtils.isEmpty(nega)) {
            TextView negativeBtn = dialogView.findViewById(R.id.negative);
            negativeBtn.setText(nega);
            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negaListener != null) {
                        negaListener.onClick(v, mDialog);
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        }

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private static FormRowLayout createFormRow(Context context, FormRowLayout.FormRowDate date) {
        FormRowLayout frl = new FormRowLayout(context);
        if (date != null) {
            frl.getTitle().setText(date.title);
            frl.getContent().setText(date.content);
        }
        return frl;
    }

    public interface OnDialogButtonClickListener {
        void onClick(View v, Dialog dialog);
    }
}
