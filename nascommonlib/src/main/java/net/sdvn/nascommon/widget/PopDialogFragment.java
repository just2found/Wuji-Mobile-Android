package net.sdvn.nascommon.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.NotNull;


/**
 * Created by yun on 18/05/19.
 */

public class PopDialogFragment extends DialogFragment {
    private boolean mIsWithoutPadding;
    private View view;
    private DialogInterface.OnDismissListener mOnDismissListener;

    public View getCustomView() {
        return view;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog == null) {
            dialog = new Dialog(requireContext(), mIsWithoutPadding ? R.style.DialogThemeWithoutPadding : R.style.DialogTheme);
            if (view != null)
                dialog.setContentView(view);
        }
        return dialog;
    }

    @NonNull
    public static PopDialogFragment newInstance(boolean withoutPadding, @NonNull View view) {

        PopDialogFragment popDialogFragment = new PopDialogFragment();
        popDialogFragment.mIsWithoutPadding = withoutPadding;
        popDialogFragment.view = view;
        return popDialogFragment;
    }

    @Override
    public void onDismiss(@NotNull DialogInterface dialog) {
        if (mOnDismissListener != null)
            mOnDismissListener.onDismiss(dialog);
        super.onDismiss(dialog);
    }

    public void addDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public boolean isShowing() {
        return getDialog() != null && getDialog().isShowing();
    }
}
