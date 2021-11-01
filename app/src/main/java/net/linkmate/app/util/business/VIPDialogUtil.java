package net.linkmate.app.util.business;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.base.MyApplication;
import net.linkmate.app.manager.PrivilegeManager;
import net.linkmate.app.ui.activity.mine.PrivilegeActivity;
import net.linkmate.app.view.adapter.PrivilegeRVAdapter;
import net.sdvn.common.internet.protocol.AccountPrivilegeInfo;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VIPDialogUtil {
    private static Dialog sDialog;
    private static boolean isShowed;

    public static void showVIPNotify(@NonNull Context context) {
        if (sDialog != null && sDialog.isShowing()) {
            sDialog.dismiss();
        }
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_vip, null);
        sDialog = new AppCompatDialog(context, R.style.DialogTheme);
        RecyclerView dvRv = dialogView.findViewById(R.id.dv_rv);
        RelativeLayout dvEmpty = dialogView.findViewById(R.id.dv_empty);


        PrivilegeRVAdapter myDeviceAdapter;

        List<AccountPrivilegeInfo.AdapterBean> beans = PrivilegeManager.getInstance().getExpiringBeans();
        myDeviceAdapter = new PrivilegeRVAdapter(beans);
        //点击device条目
        myDeviceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            }
        });
        myDeviceAdapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                return 2;
            }
        });
        if (beans.size() == 0) {
            dvEmpty.setVisibility(View.VISIBLE);
            dvRv.setVisibility(View.GONE);//R.string.not_opened_service
        } else {
            dvEmpty.setVisibility(View.GONE);
            dvRv.setVisibility(View.VISIBLE);
        }

        GridLayoutManager layout = new GridLayoutManager(context, 2, RecyclerView.VERTICAL, false);
        dvRv.setLayoutManager(layout);
        dvRv.setItemAnimator(null);
        dvRv.setAdapter(myDeviceAdapter);

        dialogView.findViewById(R.id.dv_btn_detail)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(context, PrivilegeActivity.class));
                        sDialog.dismiss();
                    }
                });
        dialogView.findViewById(R.id.dv_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sDialog.dismiss();
            }
        });
        sDialog.setContentView(dialogView);
        sDialog.setCancelable(true);
        sDialog.setCanceledOnTouchOutside(true);
        sDialog.show();
        dvRv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                final Window window = sDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.width = (int) (metrics.widthPixels * 0.80);
                    window.setAttributes(params);
                    dvRv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    @NotNull
    private static String getRemainingTimeString(@NonNull Context context, long period) {
        String remainingText;
        if (period > 24 * 60 * 60 * 1000) {
            int day = (int) (period / (24 * 60 * 60 * 1000));
            remainingText = context.getResources().getQuantityString(R.plurals.plurals_day, day);
        } else if (period > 60 * 60 * 1000) {
            int hour = (int) (period / (60 * 60 * 1000));
            remainingText = context.getResources().getQuantityString(R.plurals.plurals_hour, hour);
        } else if (period > 0) {
            int min = (int) (period / (60 * 1000));
            remainingText = context.getResources().getQuantityString(R.plurals.plurals_minutes, min);
        } else {
            remainingText = context.getResources().getString(R.string.expired);
        }
        return remainingText;
    }

    //剩余时长
    private static long getPeriod(long validityTimeMillis) {
        return validityTimeMillis - System.currentTimeMillis();
    }

    //日期文本格式化
    public static String getDateString(long timeMillis) {
        if (timeMillis == 0)
            return MyApplication.getContext().getString(R.string.indefinitely);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(new Date(timeMillis));
    }

    public static boolean isIsShowed() {
        return isShowed;
    }

    public static void setIsShowed(boolean isShowed) {
        VIPDialogUtil.isShowed = isShowed;
    }
}
