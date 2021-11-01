package net.linkmate.app.util;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DialogUtil {
    public static void showSelectDialog(final Context context, int title,
                                        int posi, final OnDialogButtonClickListener posiListener,
                                        int nega, final OnDialogButtonClickListener negaListener) {
        showSelectDialog(context,
                title > 0 ? context.getString(title) : null,
                posi > 0 ? context.getString(posi) : null,
                posiListener,
                nega > 0 ? context.getString(nega) : null,
                negaListener, "");
    }

    public static void showSelectDialog(final Context context, String title,
                                        String posi, final OnDialogButtonClickListener posiListener,
                                        String nega, final OnDialogButtonClickListener negaListener) {
        showSelectDialog(context, title, posi, posiListener, nega, negaListener, "");
    }

    public static void showSelectDialog(final Context context, String title,
                                        String posi, final OnDialogButtonClickListener posiListener,
                                        String nega, final OnDialogButtonClickListener negaListener,
                                        String checkTips) {
        final View dialogView = View.inflate(context, R.layout.dialog_edit, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context, R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(true);
        AlertDialog mDialog = mDialogBuilder.create();
        final TextView tvTitle = dialogView.findViewById(R.id.txt_title);
        final EditText etContent = dialogView.findViewById(R.id.et_content);
        tvTitle.setText(title);
        dialogView.findViewById(R.id.fl_content).setVisibility(View.GONE);


        final CheckableImageButton cb = dialogView.findViewById(R.id.cb_check);
        cb.setChecked(false);
        if (!TextUtils.isEmpty(checkTips)) {
            dialogView.findViewById(R.id.ll_check).setVisibility(View.VISIBLE);
            ((TextView) dialogView.findViewById(R.id.tv_check)).setText(checkTips);
        }

        if (!TextUtils.isEmpty(posi)) {
            TextView positiveBtn = dialogView.findViewById(R.id.positive);
            positiveBtn.setText(posi);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (posiListener != null) {
                        posiListener.onClick(v, "", mDialog, cb.isChecked());
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
                        negaListener.onClick(v, "", mDialog, cb.isChecked());
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        }

        mDialog.show();
    }

    public static void showExtraSelectDialog(final Context context, String title,
                                             String posi, final OnDialogButtonClickListener posiListener,
                                             String nega, final OnDialogButtonClickListener negaListener,
                                             String extra, final OnDialogButtonClickListener extraListener) {
        showExtraSelectDialog(context, title, posi, posiListener, nega, negaListener, extra, extraListener, "");
    }

    public static void showExtraSelectDialog(final Context context, String title,
                                             String posi, final OnDialogButtonClickListener posiListener,
                                             String nega, final OnDialogButtonClickListener negaListener,
                                             String extra, final OnDialogButtonClickListener extraListener,
                                             String checkTips) {
        final View dialogView = View.inflate(context, R.layout.dialog_edit, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context, R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(true);
        AlertDialog mDialog = mDialogBuilder.create();
        final TextView tvTitle = dialogView.findViewById(R.id.txt_title);
        final EditText etContent = dialogView.findViewById(R.id.et_content);
        tvTitle.setText(title);
        dialogView.findViewById(R.id.fl_content).setVisibility(View.GONE);
        dialogView.findViewById(R.id.fl_extra).setVisibility(View.GONE);
        dialogView.findViewById(R.id.ll_check).setVisibility(View.GONE);


        final CheckableImageButton cb = dialogView.findViewById(R.id.cb_check);
        cb.setChecked(false);
        if (!TextUtils.isEmpty(checkTips)) {
            dialogView.findViewById(R.id.ll_check).setVisibility(View.VISIBLE);
            ((TextView) dialogView.findViewById(R.id.tv_check)).setText(checkTips);
        }

        if (!TextUtils.isEmpty(posi)) {
            TextView positiveBtn = dialogView.findViewById(R.id.positive);
            positiveBtn.setText(posi);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (posiListener != null) {
                        posiListener.onClick(v, "", mDialog, cb.isChecked());
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
                        negaListener.onClick(v, "", mDialog, cb.isChecked());
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        }

        if (!TextUtils.isEmpty(extra)) {
            TextView btnExtra = dialogView.findViewById(R.id.btn_extra);
            btnExtra.setText(extra);
            dialogView.findViewById(R.id.layout_extra).setVisibility(View.VISIBLE);
            btnExtra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (extraListener != null) {
                        extraListener.onClick(v, "", mDialog, cb.isChecked());
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        }
        mDialog.show();
    }

    public static void showEditDialog(final Context context, int title, String edit, int hint
            , int positive
            , OnDialogButtonClickListener positiveListener, int negative, OnDialogButtonClickListener negativeListener) {
        final String stringTitle = title > 0 ? context.getString(title) : "";
        final String stringHint = title > 0 ? context.getString(hint) : "";
        final String stringPositive = title > 0 ? context.getString(positive) : "";
        final String stringNegative = title > 0 ? context.getString(negative) : "";
        showEditDialog(context, stringTitle, edit, stringHint, stringPositive, positiveListener
                , stringNegative, negativeListener);
    }

    public static void showEditDialog(final Context context, String title, String edit, String hint,
                                      String posi, final OnDialogButtonClickListener posiListener,
                                      String nega, final OnDialogButtonClickListener negaListener) {
        final View dialogView = View.inflate(context, R.layout.dialog_edit, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context, R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(true);
        AlertDialog mDialog = mDialogBuilder.create();
        final TextView tvTitle = dialogView.findViewById(R.id.txt_title);
        final EditText etContent = dialogView.findViewById(R.id.et_content);
        final TextView tvContent = dialogView.findViewById(R.id.tv_content);
        tvTitle.setText(title);
        dialogView.findViewById(R.id.fl_content).setVisibility(View.VISIBLE);
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvContent.setVisibility(etContent.getText().length() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etContent.setText(edit);
        tvContent.setText(hint);
        etContent.requestFocus();
        InputMethodUtils.showKeyboard(context, etContent, 200);
        if (!TextUtils.isEmpty(posi)) {
            TextView positiveBtn = dialogView.findViewById(R.id.positive);
            positiveBtn.setText(posi);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (posiListener != null) {
                        posiListener.onClick(etContent, etContent.getText().toString().trim(), mDialog, false);
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
                        negaListener.onClick(v, etContent.getText().toString().trim(), mDialog, false);
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        }

        mDialog.show();
    }

    public static void showConflictSubnets(Context context, HashMap<Integer, Set<KeyPair<Device, Device.SubNet>>> mapList) {
        try {
            ToastHelper.showShortToastSafe("text : " + GsonUtils.encodeJSON(mapList));
        } catch (Exception e) {
            e.printStackTrace();
        }
        final View view = View.inflate(context, R.layout.layout_subnet, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context, R.style.DialogTheme);
        mDialogBuilder.setView(view);
        mDialogBuilder.setCancelable(true);
        AlertDialog mDialog = mDialogBuilder.create();
        View subBack = view.findViewById(R.id.ls_iv_back);
        RecyclerView subRv = view.findViewById(R.id.ls_rv);

        subBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        BaseSectionQuickAdapter<SectionEntity<KeyPair<Device, Device.SubNet>>, BaseViewHolder> adapter =
                new BaseSectionQuickAdapter<SectionEntity<KeyPair<Device, Device.SubNet>>, BaseViewHolder>
                        (R.layout.item_subnet_conflict, R.layout.item_line_string, null) {
                    @Override
                    protected void convert(BaseViewHolder baseViewHolder, SectionEntity<KeyPair<Device, Device.SubNet>> keyPairSectionEntity) {

                    }

                    @Override
                    protected void convertHead(BaseViewHolder baseViewHolder, SectionEntity sectionEntity) {

                    }


                };
        subRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        subRv.setItemAnimator(null);
        subRv.setAdapter(adapter);
        List<SectionEntity<KeyPair<Device, Device.SubNet>>> subNets = new ArrayList<>();
        for (Map.Entry<Integer, Set<KeyPair<Device, Device.SubNet>>> entry : mapList.entrySet()) {
            final Integer key = entry.getKey();
            final SectionEntity<KeyPair<Device, Device.SubNet>> headerSectionEntity = new SectionEntity<>(true, CommonUtils.ipv4IntToIp(key));
            subNets.add(headerSectionEntity);
            for (KeyPair<Device, Device.SubNet> keyPair : entry.getValue()) {
                subNets.add(new SectionEntity<>(keyPair));
            }
        }
        adapter.setNewData(subNets);


        mDialog.show();
    }

    public static void showSimpleEditDialog(@NotNull Context context, int title, int editHint, int confirm,
                                            final OnDialogButtonClickListener posiListener,
                                            final OnDialogButtonClickListener negaListener) {
        final View dialogView = View.inflate(context, R.layout.dialog_simple_edit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        builder.setView(dialogView);
        builder.setCancelable(true);
        final TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        final EditText etContent = dialogView.findViewById(R.id.et_content);
        tvTitle.setText(title);
        etContent.setHint(editHint);
        etContent.requestFocus();
        Dialog mDialog = builder.create();
        InputMethodUtils.showKeyboard(context, etContent, 200);
        if (confirm > 0) {
            TextView positiveBtn = dialogView.findViewById(R.id.positive);
            positiveBtn.setText(confirm);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (posiListener != null) {
                        posiListener.onClick(etContent, etContent.getText().toString().trim(), mDialog, false);
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        }
        dialogView.findViewById(R.id.iv_visible).setOnClickListener(v -> {
            UIUtils.togglePasswordStatus(v, etContent);
        });
        View negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negaListener != null) {
                    negaListener.onClick(v, etContent.getText().toString().trim(), mDialog, false);
                } else {
                    mDialog.dismiss();
                }
            }
        });

        mDialog.show();
    }

    public interface OnDialogButtonClickListener {
        void onClick(View v, String strEdit, Dialog dialog, boolean isCheck);
    }
}
