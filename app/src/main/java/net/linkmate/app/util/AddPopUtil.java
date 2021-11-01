package net.linkmate.app.util;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import net.linkmate.app.R;

public class AddPopUtil {
    public static final int SHOW_SCAN = 0b1000;
    public static final int SHOW_ADD_DEV = 0b0100;
    public static final int SHOW_ADD_NET = 0b0010;
    public static final int SHOW_STATUS = 0b0001;
    public static final int SHOW_ADD_CIRCLE = 0b10000;
    public static final int SHOW_ALL = 0b11111;

    /**
     * 用于展示标题栏右侧“+”号按钮的弹出的PopupWindow
     *
     * @param context  上下文
     * @param parent   以该控件为展示坐标（展示在其下方），一般以“+”控件为参数
     * @param showNum  内容中要显示的按钮编号，参考本类常量
     * @param listener 内容中按钮的点击事件
     */
    public static void showAddPop(Activity context, View parent, int showNum, final OnPopButtonClickListener listener) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_home_add, null, false);
        View view1 = contentView.findViewById(R.id.item_pop_add_1);
        View view2 = contentView.findViewById(R.id.item_pop_add_2);
        View view3 = contentView.findViewById(R.id.item_pop_add_3);
        View view4 = contentView.findViewById(R.id.item_pop_add_4);
        View view5 = contentView.findViewById(R.id.item_pop_add_5);

        view1.setVisibility(View.GONE);
        view2.setVisibility(View.GONE);
        view3.setVisibility(View.GONE);
        view4.setVisibility(View.GONE);
        view5.setVisibility(View.GONE);

        final PopupWindow window = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.setOutsideTouchable(true);
        window.setTouchable(true);
        window.setAnimationStyle(R.style.PopupWindowAnim);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowUtil.hintShadow(context);
            }
        });
        WindowUtil.showShadow(context);
        window.showAsDropDown(parent);

        showSomeView(showNum, listener, view1, window, SHOW_SCAN);
        showSomeView(showNum, listener, view2, window, SHOW_ADD_DEV);
        showSomeView(showNum, listener, view3, window, SHOW_ADD_NET);
//        showSomeView(showNum, listener, view4, window, SHOW_STATUS);
//        showSomeView(showNum, listener, view5, window, SHOW_ADD_CIRCLE);
    }

    private static void showSomeView(int showNum, final OnPopButtonClickListener listener, View view, final PopupWindow window, final int number) {
        if ((showNum & number) == number) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(v, number);
                    window.dismiss();
                }
            });
        }
    }

    public interface OnPopButtonClickListener {
        void onClick(View v, int clickNum);
    }
}
