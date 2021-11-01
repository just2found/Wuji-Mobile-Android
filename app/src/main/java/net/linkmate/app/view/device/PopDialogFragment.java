package net.linkmate.app.view.device;//package net.linkmate.app.view.device;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.DialogFragment;
//import android.view.View;
//
//import net.linkmate.app.R;
//
///**
// * Created by yun on 18/05/19.
// */
//
//public class PopDialogFragment extends DialogFragment {
//    private boolean mIsWithoutPadding;
//    private View view;
//    private DialogInterface.OnDismissListener mOnDismissListener;
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog mDialog = new Dialog(getContext(), mIsWithoutPadding ? R.style.DialogThemeWithoutPadding : R.style.DialogTheme);
//        mDialog.setContentView(view);
//        return mDialog;
//    }
//
//    public static PopDialogFragment newInstance(boolean withoutPadding, @NonNull View view) {
//
//        PopDialogFragment popDialogFragment = new PopDialogFragment();
//        popDialogFragment.mIsWithoutPadding = withoutPadding;
//        popDialogFragment.view = view;
//        return popDialogFragment;
//    }
//
//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        super.onDismiss(dialog);
//        if (mOnDismissListener != null)
//            mOnDismissListener.onDismiss(dialog);
//    }
//
//    public void addDismissListener(DialogInterface.OnDismissListener onDismissListener) {
//        mOnDismissListener = onDismissListener;
//    }
//}
