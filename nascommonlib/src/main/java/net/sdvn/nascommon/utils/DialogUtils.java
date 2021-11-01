package net.sdvn.nascommon.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import com.hbb20.CountryCodePicker;

import net.sdvn.nascommon.model.contacts.SortModel;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommon.widget.CheckableImageButton;
import net.sdvn.nascommonlib.BuildConfig;
import net.sdvn.nascommonlib.R;

import java.util.List;

public class DialogUtils {
    private static final String TAG = DialogUtils.class.getSimpleName();

    public static final int RESOURCE_ID_NONE = -1;
//    @Nullable
//    private static WeakReference<Dialog> mDialogRef = null;

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param context   current context, it is necessary
     * @param contentId dialog content text resource id
     * @param notifyId  notify text resource id
     * @param mListener dialog button click listener
     */
    public static Dialog showNotifyDialog(@NonNull Context context, int titleId, int contentId,
                                          int notifyId, final OnDialogClickListener mListener) {
        try {
            String title = titleId > 0 ? context.getResources().getString(titleId) : null;
            String content = contentId > 0 ? context.getResources().getString(contentId) : null;
            String notify = notifyId > 0 ? context.getResources().getString(notifyId) : null;
            return showNotifyDialog(context, title, content, notify, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param context    current context, it is necessary
     * @param contentTxt dialog content text
     * @param notifyTxt  notify text
     * @param mListener  dialog button click listener
     */
    public static Dialog showNotifyDialog(@Nullable Context context, @Nullable String titleTxt, @Nullable String contentTxt,
                                          @Nullable String notifyTxt, @Nullable final OnDialogClickListener mListener) {
        if (context == null || contentTxt == null) {
            Logger.LOGE(TAG, "context or dialog content is null");
            return null;
        }
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notify, null);
//        Dialog mDialog = new Dialog(context, R.style.DialogTheme);
        AlertDialog.Builder mDialogBuilder =new  AlertDialog.Builder(context,R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(false);
        AlertDialog dialog = mDialogBuilder.create();

        TextView titleTextView = dialogView.findViewById(R.id.txt_title);
        TextView contentTextView = dialogView.findViewById(R.id.txt_content);

        if (!TextUtils.isEmpty(titleTxt)) {
            titleTextView.setText(titleTxt);
            titleTextView.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(contentTxt)) {
            contentTextView.setText(contentTxt);
            contentTextView.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(notifyTxt)) {
            TextView positiveBtn = dialogView.findViewById(R.id.positive);
            positiveBtn.setText(notifyTxt);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mListener != null) {
                        mListener.onClick(dialog, true);
                    }
                }
            });
        }

        dialog.show();
        return dialog;
    }

    public static void showWarningDialog(@NonNull Context context, int titleId, int contentId,
                                         int positiveId, int negativeId, final OnDialogClickListener mListener) {
        try {
            String title = titleId > 0 ? context.getResources().getString(titleId) : null;
            String content = contentId > 0 ? context.getResources().getString(contentId) : null;
            String positive = positiveId > 0 ? context.getResources().getString(positiveId) : null;
            String negative = negativeId > 0 ? context.getResources().getString(negativeId) : null;
            showConfirmDialog(context, true, title, content, positive, negative, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void showWarningDialog(Context context, String title, String content,
                                         String positive, String negative, final OnDialogClickListener mListener) {
        showConfirmDialog(context, true, title, content, positive, negative, mListener);
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param context    current context, it is necessary
     * @param contentId  dialog content text resource id
     * @param positiveId positive button text resource id
     * @param negativeId negative button text resource id
     * @param mListener  dialog button click listener
     */
    public static Dialog showConfirmDialog(@Nullable Context context, int titleId, int contentId,
                                         int positiveId, int negativeId, final OnDialogClickListener mListener) {
        if (context == null) {
            Logger.LOGE(TAG, "context is null !!!!!!!");
            return null;
        }
        try {
            String title = titleId > 0 ? context.getResources().getString(titleId) : null;
            String content = contentId > 0 ? context.getResources().getString(contentId) : null;
            String positive = positiveId > 0 ? context.getResources().getString(positiveId) : null;
            String negative = negativeId > 0 ? context.getResources().getString(negativeId) : null;
            return showConfirmDialog(context, false, title, content, positive, negative, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *  @param context    current context, it is necessary
     * @param positiveId positive button text resource id
     * @param negativeId negative button text resource id
     * @param mListener  dialog button click listener
     * @return
     */
    public static Dialog showConfirmDialog(@Nullable Context context, int titleId,
                                           int positiveId, int negativeId, final OnDialogClickListener mListener) {
        if (context == null) {
            Logger.LOGE(TAG, "context is null !!!!!!!");
            return null;
        }
        try {
            String title = titleId > 0 ? context.getResources().getString(titleId) : null;
            String positive = positiveId > 0 ? context.getResources().getString(positiveId) : null;
            String negative = negativeId > 0 ? context.getResources().getString(negativeId) : null;
          return  showConfirmDialog(context, false, title, null, positive, negative, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param context     current context, it is necessary
     * @param contentTxt  dialog content text
     * @param positiveTxt positive button text
     * @param negativeTxt negative button text
     * @param mListener   dialog button click listener
     */
    public static void showConfirmDialog(Context context, @Nullable String titleTxt, @Nullable String contentTxt,
                                         String positiveTxt, String negativeTxt, final OnDialogClickListener mListener) {
        showConfirmDialog(context, false, titleTxt, contentTxt, positiveTxt, negativeTxt, mListener);
    }


    private static Dialog showConfirmDialog(@Nullable Context context, boolean warning, @Nullable String titleTxt, @Nullable String contentTxt,
                                          @Nullable String positiveTxt, @Nullable String negativeTxt, @Nullable final OnDialogClickListener mListener) {
        if (context == null) {
            Logger.LOGE(TAG, "context is null");
            return null;
        }

        if (positiveTxt == null && negativeTxt == null) {
            Logger.LOGE(TAG, "positive and negative content is null");
            return null;
        }
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//            Logger.LOGE(TAG, "dismiss--showConfirmDialog ", contentTxt, positiveTxt);
//        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);
        AlertDialog.Builder mDialogBuilder =new  AlertDialog.Builder(context,R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(true);
        AlertDialog dialog = mDialogBuilder.create();

        TextView titleTextView = dialogView.findViewById(R.id.txt_title);
        TextView contentTextView = dialogView.findViewById(R.id.txt_content);

        if (!TextUtils.isEmpty(titleTxt)) {
            titleTextView.setText(titleTxt);
            titleTextView.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(contentTxt)) {
            contentTextView.setText(contentTxt);
            contentTextView.setVisibility(View.VISIBLE);
            if (warning) {
                contentTextView.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        TextView positiveBtn = dialogView.findViewById(R.id.positive);
        if (positiveTxt != null) {
            positiveBtn.setText(positiveTxt);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mListener != null) {
                        mListener.onClick(dialog, true);
                    }
                }
            });
        } else {
            positiveBtn.setVisibility(View.GONE);
        }

        TextView negativeBtn = dialogView.findViewById(R.id.negative);
        if (negativeTxt != null) {
            negativeBtn.setText(negativeTxt);
            negativeBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mListener != null) {
                        mListener.onClick(dialog, false);
                    }
                }
            });
            negativeBtn.setVisibility(View.VISIBLE);
        } else {
            negativeBtn.setVisibility(View.GONE);
        }

        if (positiveTxt != null && negativeTxt != null) {
            View line = dialogView.findViewById(R.id.spit_line);
            line.setVisibility(View.VISIBLE);
        }
        dialog.show();
        return dialog;
    }

    public static void showEditDialog(@NonNull Context context, int titleId, int hintId, int defContentId,
                                      int posId, int negId, final OnEditDialogClickListener mListener) {
        try {
            showEditDialog(context, titleId, hintId, defContentId, -1, posId, negId, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void showEditDialog(Context context, int titleId, int hintId, int defContentId, int tips,
                                      int posId, int negId, final OnEditDialogClickListener mListener) {
        try {
            Resources resources = context.getResources();
            String defContent = defContentId > 0 ? resources.getString(defContentId) : null;
            showEditDialog(context, titleId, hintId, defContent, tips, posId, negId, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void showEditDialog(Context context, int titleId, int hintId, String defaultContent, int tips,
                                      int posId, int negId, final OnEditDialogClickListener mListener) {
        try {
            Resources resources = context.getResources();
            String title = titleId > 0 ? resources.getString(titleId) : null;
            String hint = hintId > 0 ? resources.getString(hintId) : null;
            String positive = posId > 0 ? resources.getString(posId) : null;
            String negative = negId > 0 ? resources.getString(negId) : null;
            String tipsT = tips > 0 ? resources.getString(tips) : null;
            showEditDialog(context, title, hint, defaultContent, tipsT, positive, negative, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void showEditDialog(@NonNull Context context, int titleId, int hintId, String defaultContent,
                                      int posId, int negId, final OnEditDialogClickListener mListener) {
        try {
            showEditDialog(context, titleId, hintId, defaultContent, 0, posId, negId, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param context     current context, it is necessary
     * @param contentHint dialog content text
     * @param positiveTxt positive button text
     * @param negativeTxt negative button text
     * @param mListener   dialog button click listener
     */
    public static void showEditDialog(@Nullable Context context, @Nullable String titleTxt, @Nullable String contentHint, @Nullable String defaultContent, String tips,
                                      @Nullable String positiveTxt, @Nullable String negativeTxt, @Nullable final OnEditDialogClickListener mListener) {
        if (context == null) {
            Logger.LOGE(TAG, "context or dialog content is null");
            return;
        }

        if (positiveTxt == null || negativeTxt == null) {
            Logger.LOGE(TAG, "positive or negative content is null");
            return;
        }
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.nas_dialog_edit, null);
        AlertDialog.Builder mDialogBuilder =new  AlertDialog.Builder(context,R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(false);
        AlertDialog dialog = mDialogBuilder.create();

        TextView titleTextView = dialogView.findViewById(R.id.txt_title);
        final EditText contentEditText = dialogView.findViewById(R.id.et_content);
        TextView textViewTips = dialogView.findViewById(R.id.txt_tips);
        TextView tvContent = dialogView.findViewById(R.id.tv_content);
        textViewTips.setVisibility(TextUtils.isEmpty(tips) ? View.GONE : View.VISIBLE);

        textViewTips.setText(tips);

        titleTextView.setText(titleTxt != null ? titleTxt : "");
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvContent.setVisibility(contentEditText.getText().length()>0?View.GONE:View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (contentHint != null) {
            tvContent.setText(contentHint);
        }
        if (defaultContent != null) {
            contentEditText.setText(defaultContent);
            contentEditText.setSelection(0, defaultContent.length());
        }
        contentEditText.requestFocus();
        InputMethodUtils.showKeyboard(context, contentEditText, 200);

        if (positiveTxt != null) {
            TextView positiveBtn = dialogView.findViewById(R.id.positive);
            positiveBtn.setText(positiveTxt);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(dialog, true, contentEditText);
                    }
                }
            });
        }

        if (negativeTxt != null) {
            TextView negativeBtn = dialogView.findViewById(R.id.negative);
            negativeBtn.setText(negativeTxt);
            negativeBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mListener != null) {
                        mListener.onClick(dialog, false, contentEditText);
                    }
                }
            });
        }

        if (BuildConfig.DEBUG) {
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Logger.LOGD(TAG, "dialog dismiss");
                }
            });
        }
        dialog.show();
    }

    public static void showEditPwdDialog(@Nullable final Context context, int titleId, int tipsId, final int oldHintId, int hintId, int confirmHintId,
                                         final int posId, int negId, @Nullable final OnEditPWDDialogClickListener mListener) {
        if (context == null) {
            Logger.LOGE(TAG, "context or dialog content is null");
            return;
        }
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_pwd, null);
        AlertDialog.Builder mDialogBuilder =new  AlertDialog.Builder(context,R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(false);
        AlertDialog dialog = mDialogBuilder.create();

        TextView titleTextView = dialogView.findViewById(R.id.txt_title);
        TextView tipsTextView = dialogView.findViewById(R.id.txt_tips);
        final EditText etOldPwd = dialogView.findViewById(R.id.et_old_pwd);
        final EditText etNewPwd = dialogView.findViewById(R.id.et_pwd);
        final EditText etConfirmPwd = dialogView.findViewById(R.id.et_pwd_confirm);
        final TextView tvOldPwd = dialogView.findViewById(R.id.tv_old_pwd);
        final TextView tvNewPwd = dialogView.findViewById(R.id.tv_pwd);
        final TextView tvConfirmPwd = dialogView.findViewById(R.id.tv_pwd_confirm);
        etOldPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvOldPwd.setVisibility(etOldPwd.getText().length()>0?View.GONE:View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etNewPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvNewPwd.setVisibility(etNewPwd.getText().length()>0?View.GONE:View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etConfirmPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvConfirmPwd.setVisibility(etConfirmPwd.getText().length()>0?View.GONE:View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        titleTextView.setText(titleId);
        tipsTextView.setText(tipsId);
        etNewPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tvNewPwd.setText(hintId);
        etConfirmPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tvConfirmPwd.setText(confirmHintId);

        if (oldHintId != 0) {
            etOldPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tvOldPwd.setText(oldHintId);
            dialogView.findViewById(R.id.fl_old_pwd).setVisibility(View.VISIBLE);
            InputMethodUtils.showKeyboard(context, etOldPwd, 200);
        } else {
            dialogView.findViewById(R.id.fl_old_pwd).setVisibility(View.GONE);
            InputMethodUtils.showKeyboard(context, etNewPwd, 200);
        }

        TextView positiveBtn = dialogView.findViewById(R.id.positive);
        positiveBtn.setText(posId);
        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//                if (oldHintId != 0) {
//                    String oldPwd = SPHelper.get(Constants.SP_FIELD_PWD, "");
//                    if (!TextUtils.isEmpty(oldPwd) && !oldPwd.equals(etOldPwd.getText().toString())) {
//                        AnimUtils.sharkEditText(context, etOldPwd);
//                        etOldPwd.requestFocus();
//                        ToastHelper.showToast(R.string.tip_password_error);
//                        return;
//                    }
//                }
                String newPwd = etNewPwd.getText().toString().trim();
                if (EmptyUtils.isEmpty(newPwd)) {
                    AnimUtils.sharkEditText(context, etNewPwd);
                    etNewPwd.requestFocus();
                    return;
                }
                String cfPwd = etConfirmPwd.getText().toString().trim();
                if (EmptyUtils.isEmpty(cfPwd)) {
                    AnimUtils.sharkEditText(context, etConfirmPwd);
                    etConfirmPwd.requestFocus();
                    return;
                }
                if (!newPwd.equals(cfPwd)) {
                    ToastHelper.showToast(R.string.error_confirm_pwd);
                    return;
                }

                if (mListener != null) {
                    mListener.onClick(dialog, true, etNewPwd, etOldPwd);
                }
            }
        });

        TextView negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setText(negId);
        negativeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                if (mListener != null) {
                    mListener.onClick(dialog, false, etNewPwd, etOldPwd);
                }
            }
        });
        dialog.show();
    }

    // public static void showListDialog(Context context, int titleId, List<String> itemList, int
    // positiveId, final OnDialogClickListener mListener) {
    // try {
    // String title = titleId > 0 ? context.getResources().getString(titleId) : null;
    // String positive = positiveId > 0 ? context.getResources().getString(positiveId) : null;
    // showListDialog(context, title, itemList, positive, mListener);
    // } catch (NotFoundException e) {
    // e.printStackTrace();
    // }
    // }
    //


    public static void showListDialog(@Nullable Context context,
                                      @Nullable List<String> titleList,
                                      @Nullable List<String> contentList,
                                      String title, String tips,
                                      String top, String mid, String neg,
                                      @Nullable final OnMultiDialogClickListener mListener) {
        if (context == null || (titleList == null && contentList == null)) {
            Logger.LOGE(TAG, "context or dialog content is null");
            return;
        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_list, null);

        AlertDialog.Builder mDialogBuilder =new  AlertDialog.Builder(context,R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(false);
        AlertDialog dialog = mDialogBuilder.create();

        TextView titleTextView = dialogView.findViewById(R.id.txt_title);
        titleTextView.setText(title);
        titleTextView.setVisibility(View.VISIBLE);

        if (!EmptyUtils.isEmpty(tips)) {
            TextView tipsTextView = dialogView.findViewById(R.id.txt_tips);
            tipsTextView.setText(tips);
            tipsTextView.setVisibility(View.VISIBLE);
        }

        ListView mListView = dialogView.findViewById(R.id.listview);
        DialogListAdapter mAdapter = new DialogListAdapter(context, titleList, contentList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
//        setListViewMaxVisibleLines(context, mListView, 8);

        if (!EmptyUtils.isEmpty(top)) {
            LinearLayout layout = dialogView.findViewById(R.id.layout_multi_top);
            layout.setVisibility(View.VISIBLE);
            TextView mBtn = dialogView.findViewById(R.id.btn_multi_top);
            mBtn.setText(top);
            mBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mListener != null) {
                        mListener.onClick(dialog, 2);
                    }
                }
            });
        }

        if (!EmptyUtils.isEmpty(mid)) {
            LinearLayout layout = dialogView.findViewById(R.id.layout_multi_mid);
            layout.setVisibility(View.VISIBLE);
            TextView mBtn = dialogView.findViewById(R.id.btn_multi_mid);
            mBtn.setText(mid);
            mBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mListener != null) {
                        mListener.onClick(dialog, 1);
                    }
                }
            });
        }

        TextView negativeBan = dialogView.findViewById(R.id.btn_negative);
        negativeBan.setText(neg);
        negativeBan.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                if (mListener != null) {
                    mListener.onClick(dialog, 0);
                }
            }
        });

        dialog.show();
    }

    public static void showListDialog(@NonNull Context context,
                                      List<String> titleList,
                                      List<String> contentList,
                                      int titleId,
                                      int topId, int midId, int negId,
                                      final OnMultiDialogClickListener mListener) {
        Resources resources = context.getResources();
        showListDialog(
                context,
                titleList,
                contentList,
                titleId > 0 ? resources.getString(titleId) : null,
                null,
                topId > 0 ? resources.getString(topId) : null,
                midId > 0 ? resources.getString(midId) : null,
                negId > 0 ? resources.getString(negId) : null,
                mListener
        );
    }

    public static void showListDialog(Context context,
                                      List<String> contentList,
                                      String title, String tips,
                                      String top, String mid,
                                      String neg,
                                      final OnMultiDialogClickListener mListener) {
        showListDialog(context, null, contentList, title, tips, top, mid, neg, mListener);
    }

    private static void setListViewMaxVisibleLines(@Nullable Context context, @NonNull ListView listView, int maxLines) {
        if (context == null) {
            Logger.LOGE(TAG, "context is null !!!!!!!");
            return;
        }

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int itemCount = listAdapter.getCount();
        if (itemCount > maxLines) {
            int totalHeight;
            if (itemCount > 0) {
                View listItem = listAdapter.getView(0, null, listView);
                listItem.measure(0, 0);
                totalHeight = listItem.getMeasuredHeight() * maxLines + listView.getDividerHeight() * (maxLines + 1);
            } else {
                totalHeight = Utils.dipToPx(200);
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (maxLines - 1));
            listView.setLayoutParams(params);
        }
    }

//    public static void dismiss() {
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
//    }

    public static void showCheckDialog(@Nullable Context context,
                                       @StringRes int resIdTitle,
                                       int resIdTips,
                                       @StringRes int resIdPositive,
                                       @StringRes int resIdNegative,
                                       @Nullable final OnDialogCheckListener listener) {
        if (context == null) {
            Logger.LOGE(TAG, "context is null !!!!!!!");
            return;
        }
//        if (null != dialog && dialog.isShowing()) {
//            dialog.dismiss();
//            Logger.LOGE(TAG, "dismiss--showCheckDialog", context.getString(resIdTitle));
//        }
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_check, null);

        AlertDialog.Builder mDialogBuilder =new  AlertDialog.Builder(context,R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(false);
        AlertDialog dialog = mDialogBuilder.create();

        TextView title = dialogView.findViewById(R.id.txt_title);
        title.setText(context.getResources().getString(resIdTitle));
        final View layoutCheck = dialogView.findViewById(R.id.layout_check);
        TextView tips = dialogView.findViewById(R.id.dialog_tips);
        if (resIdTips > 0) {
            tips.setText(context.getResources().getString(resIdTips));
            layoutCheck.setVisibility(View.VISIBLE);
        } else {
            layoutCheck.setVisibility(View.GONE);
        }
        final CheckableImageButton checkBox = dialogView.findViewById(R.id.dialog_check);
        dialog.setContentView(dialogView);
        TextView positiveBtn = dialogView.findViewById(R.id.positive);
        positiveBtn.setText(resIdPositive);
        positiveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(true, checkBox.isChecked());
                }
            }
        });
        TextView negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setText(resIdNegative);
        negativeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//                mListView.hiddenRight();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Customized Dialog Click Listener
     *
     * @author shz
     */
    public interface OnDialogClickListener {
        /**
         * On dialog button click
         *
         * @param isPositiveBtn if true is positive button clicked, else is negative button clicked
         */
        void onClick(DialogInterface dialog, @NonNull boolean isPositiveBtn);
    }

    public interface OnMultiDialogClickListener {
        void onClick(DialogInterface dialog, int index);
    }

    public interface OnEditDialogClickListener {
        void onClick(DialogInterface dialog, boolean isPositiveBtn, EditText mEditTextNew);
    }

    public interface OnEditPWDDialogClickListener {
        void onClick(DialogInterface dialog, boolean isPositiveBtn, EditText mEditTextNew, EditText mEditTextOld);
    }

    public interface OnDateDialogClickListener {
        void onClick(DialogInterface dialog, boolean isPositiveBtn, String date);
    }

    public interface OnEditDoubleDialogClickListener {
        void onClick(DialogInterface dialog, boolean isPositiveBtn, EditText mEditText1, EditText mEditText2);
    }

    private static class DialogListAdapter extends BaseAdapter {
        public LayoutInflater mInflater;
        private List<String> mTitleList;
        private List<String> mContentList;

        public DialogListAdapter(Context context, List<String> titleList, List<String> contentList) {
            this.mTitleList = titleList;
            this.mContentList = contentList;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mContentList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView mTitleTxt;
            TextView mContentTxt;
        }

        @Nullable
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_dialog, null);

                holder = new ViewHolder();
                holder.mTitleTxt = convertView.findViewById(R.id.txt_title);
                holder.mContentTxt = convertView.findViewById(R.id.txt_content);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (null == mTitleList) {
                holder.mTitleTxt.setVisibility(View.GONE);
            } else {
                holder.mTitleTxt.setText(mTitleList.get(position));
                holder.mTitleTxt.setVisibility(View.VISIBLE);
            }
            holder.mContentTxt.setText(mContentList.get(position));

            return convertView;
        }
    }

//    public static void showDateDialog(@Nullable final Context context,
//                                      String titleId,
//                                      final String posId, String negId,
//                                      @NonNull final JSONArray dateList
//            , @Nullable final OnDateDialogClickListener mListener) {
//        if (context == null) {
//            Logger.LOGE(TAG, "context or dialog content is null");
//            return;
//        }
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
//        final String[] selectDate = new String[1];
//        selectDate[0] = null;
//        final ArrayList<String> haveDateList = new ArrayList<>();
//        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_date_select, null);
//        mDialog = new Dialog(context, R.style.NasDialogTheme);
//        TextView titleTextView = dialogView.findViewById(R.id.txt_title);
//
//        titleTextView.setText(titleId);
//        TextView positiveBtn = dialogView.findViewById(R.id.positive);
//        positiveBtn.setText(posId);
//        positiveBtn.setVisibility(View.VISIBLE);
//        positiveBtn.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (mListener != null) {
//                    if (!haveDateList.contains(selectDate[0])) {
//                        selectDate[0] = "nodata";
//                    }
//                    mListener.onClick(true, selectDate[0]);
//                }
//                mDialog.dismiss();
//            }
//        });
//
//
//        TextView negativeBtn = dialogView.findViewById(R.id.negative);
//        negativeBtn.setText(negId);
//        negativeBtn.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (mListener != null) {
//                    mListener.onClick(false, null);
//                }
//                mDialog.dismiss();
//            }
//        });
//
//        CalendarView gridCalendarView =
//                dialogView.findViewById(R.id.gridMonthView);
//        //事务数据填充
//        List<CalendarInfo> list = new ArrayList<CalendarInfo>();
//        for (int i = 0; i < dateList.length(); i++) {
//            try {
//                Logger.LOGD(TAG, "datelist: date ===  " + dateList.getString(i));
//                String[] splitList = dateList.getString(i).split("-");
//                int year = Integer.parseInt(splitList[0]);
//                int mon = Integer.parseInt(splitList[1]);
//                int day = Integer.parseInt(splitList[2]);
//                list.add(new CalendarInfo(year, mon, day, "yyy"));
//                haveDateList.add(dateList.getString(i));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        gridCalendarView.setCalendarInfos(list);
//        gridCalendarView.setDateClick(new MonthView.IDateClick() {
//
//            @Override
//            public void onClickOnDate(int year, int month, int day) {
//                String sMonth = String.valueOf(month);
//                String sDay = String.valueOf(day);
//                if (sMonth.length() == 1) {
//                    sMonth = "0" + sMonth;
//                }
//                if (sDay.length() == 1) {
//                    sDay = "0" + sDay;
//                }
//                selectDate[0] = year + "-" + sMonth + "-" + sDay;
//                Logger.LOGD(TAG, "onClickOnDate: 点击了" + selectDate[0]);
//            }
//        });
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
//                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        mDialog.setContentView(dialogView);
//        mDialog.setCancelable(false);
//        mDialog.show();
//
//    }

    public static void showContactDialog(@Nullable final Context context,
                                         @Nullable final SortModel sortModel,
                                         @StringRes int strTitle,
                                         @StringRes int strConfirm,
                                         @Nullable final OnDialogClickListener callBack) {
        if (context == null) {
            Logger.LOGE(TAG, "context or dialog content is null");
            return;
        }
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
        //邀请弹窗
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_user_invite, null);
        AlertDialog.Builder mDialogBuilder =new  AlertDialog.Builder(context,R.style.DialogTheme);
        mDialogBuilder.setView(dialogView);
        mDialogBuilder.setCancelable(false);
        AlertDialog dialog = mDialogBuilder.create();

        final EditText mEditText = dialogView.findViewById(R.id.et_content);
        final TextView txt_title = dialogView.findViewById(R.id.txt_title);
        final EditText mEditText_name = dialogView.findViewById(R.id.et_content_name);
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setText(sortModel != null ? sortModel.simpleNumber : "");
        mEditText_name.setText(sortModel != null ? sortModel.name : "");
        final CountryCodePicker ccp = dialogView.findViewById(R.id.country_code);
        if (sortModel != null && sortModel.number != null) {
            ccp.setFullNumber(sortModel.number);
        }
        ccp.registerCarrierNumberEditText(mEditText);
        Button positiveBtn = dialogView.findViewById(R.id.positive);
        txt_title.setText(strTitle);
        positiveBtn.setText(strConfirm);

        positiveBtn.setVisibility(View.VISIBLE);

        positiveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sortModel.name = mEditText_name.getText().toString().trim();
                sortModel.number = mEditText.getText().toString().trim();
                final String preTel = ccp.getSelectedCountryCode();
                String invitedUser = ccp.getFullNumber();
                final String tel = invitedUser.substring(preTel.length());
                if (tel.isEmpty() || !ccp.isValidFullNumber()) {
                    Toast.makeText(context, R.string.tip_input_correct_phone_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                sortModel.simpleNumber = tel;
                sortModel.countryCode = preTel;

                if (callBack != null) {
                    callBack.onClick(dialog, true);
                }
            }
        });

        Button negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onClick(dialog, false);
                }
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                InputMethodUtils.hideKeyboard(context, mEditText);
            }
        });
        dialog.show();
    }


    public interface OnDialogCheckListener {
        void onClick(boolean isPositiveBtn, boolean isChecked);
    }

    public static Dialog showCustomDialog(Context context, View dialogView) {
        if (context == null) {
            throw new NullPointerException("context or dialog content is null");
        }
//        if (null != mDialog && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
        Dialog mDialog = new AppCompatDialog(context, R.style.DialogTheme);
        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
        return mDialog;
    }
}
