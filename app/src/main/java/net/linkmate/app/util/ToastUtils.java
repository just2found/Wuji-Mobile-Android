package net.linkmate.app.util;

import android.widget.TextView;
import android.widget.Toast;

import net.linkmate.app.base.MyApplication;
import net.sdvn.cmapi.util.LogUtils;
import net.sdvn.common.internet.SdvnHttpErrorNo;


public class ToastUtils {
    private static Toast toast;
    private static int textview_id;

    /**
     * 强大的吐司，能够连续弹的吐司
     *
     * @param text
     */
    public static void showToast(String text) {
//        if (toast == null) {
//            toast = Toast.makeText(MyApplication.getContext(), text, Toast.LENGTH_SHORT);
//        } else {
//            toast.setText(text);//如果不为空，则直接改变当前toast的文本
//        }
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(MyApplication.getContext(), text, Toast.LENGTH_SHORT);
        toast.setText(text);
        toast.show();
    }

    public static void showError(int error) {
        showToast(SdvnHttpErrorNo.ec2String(error));
        LogUtils.e("OkHttp", MyApplication.getContext().getString(SdvnHttpErrorNo.ec2ResId(error)) + "(" + error + ")");
    }


    public static void showError(int error, String errmsg) {
        showToast(SdvnHttpErrorNo.ec2String(error, errmsg));
        LogUtils.e("OkHttp", MyApplication.getContext().getString(SdvnHttpErrorNo.ec2ResId(error), errmsg) + "(" + error + ")");
    }

    public static void showToast(int resId) {
        showToast(MyApplication.getContext().getString(resId));
    }

    public static void showCustomToast(String msg, int txtColor, int bgColor) {
        Toast toast = new Toast(MyApplication.getContext());
        TextView textView = toast.getView().findViewById(android.R.id.message);
        textView.setTextColor(txtColor);
        // 设置背景颜色
        toast.getView().setBackgroundColor(bgColor);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setText(msg);//如果不为空，则直接改变当前toast的文本
        toast.show();
    }

//    public static void show(Context context, String str) {
//        if (toast == null)
//            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
//        else
//            toast.setText(str);
//        if (textview_id == 0)
//            textview_id = Resources.getSystem().getIdentifier("message", "id", "android");
//        TextView viewById = (TextView) toast.getView().findViewById(textview_id);
//        if (viewById != null)
//            viewById.setGravity(Gravity.CENTER);
//        toast.show();
//    }

    public static void cancel() {
        if (toast != null)
            toast.cancel();
    }

//    public static void show(Context context, int resId) {
//        show(context, context.getResources().getString(resId));
//    }
}
