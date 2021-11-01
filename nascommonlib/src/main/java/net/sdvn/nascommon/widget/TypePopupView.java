package net.sdvn.nascommon.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.sdvn.nascommon.model.FileTypeItem;
import net.sdvn.nascommon.utils.BlurUtil;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import libs.source.common.utils.SystemBarManager;

public class TypePopupView {
    private List<FileTypeItem> itemList;
    private int mTitleId;
    private Activity context;
    private SupportPopupWindow mPopupMenu;
    private PopupWindow.OnDismissListener mOnDismissListener;
    private BottomSheetDialog bottomSheetDialog;
    private OnItemClickListener mListener;

    public TypePopupView(@NonNull Activity context, List<FileTypeItem> itemList, int titleId) {
        this.context = context;
        this.itemList = itemList;
        this.mTitleId = titleId;
    }

    private View mBackLayout;

    private View initView(@NonNull Activity context, int titleId) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pop_type, null);
        final TextView titleText = view.findViewById(R.id.title);
        if (titleId > 0)
            titleText.setText(titleId);
        else {
            titleText.setVisibility(View.GONE);
        }
        mBackLayout = view.findViewById(R.id.layout_menu);
        mBackLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        GridView mGridView = view.findViewById(R.id.gridview);
        mGridView.setVisibility(View.VISIBLE);
        PopupMenuAdapter adapter = new PopupMenuAdapter();
        adapter.notifyDataSetChanged();
        mGridView.setAdapter(adapter);
        mGridView.setFocusableInTouchMode(true);
        mGridView.setFocusable(true);
        if (mListener != null) {
            mGridView.setOnItemClickListener(mListener);
        }
        return view;
    }

    private View initView2(@NonNull Activity context, int titleId) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pop_type2, null);
        final TextView titleText = view.findViewById(R.id.title);
        if (titleId > 0)
            titleText.setText(titleId);
        else {
            titleText.setVisibility(View.GONE);
        }
        mBackLayout = view.findViewById(R.id.layout_menu);
        mBackLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        GridView mGridView = view.findViewById(R.id.gridview);
        mGridView.setVisibility(View.VISIBLE);
        PopupMenuAdapter adapter = new PopupMenuAdapter();
        adapter.notifyDataSetChanged();
        mGridView.setAdapter(adapter);
        mGridView.setFocusableInTouchMode(true);
        mGridView.setFocusable(true);
        if (mListener != null) {
            mGridView.setOnItemClickListener(mListener);
        }
        View vBottomBackground = view.findViewById(R.id.vBottomBackground);
        view.findViewById(R.id.vBottomHeight).getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            @Override
            public void onDraw() {
                if (vBottomBackground.getMeasuredHeight() == 0) {
                    ViewGroup.LayoutParams layoutParams = vBottomBackground.getLayoutParams();
                    int height = layoutParams.height;
                    if (height == 0) {
                        layoutParams.height = (int) (1.8f*view.findViewById(R.id.vBottomHeight).getMeasuredHeight());
                        vBottomBackground.setLayoutParams(layoutParams);
                    }
                }
            }
        });
//        int width = Utils.getWindowsSize(context,true);
//        Bitmap b = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
//        b.eraseColor(Color.parseColor("#ffffff"));
//        b = BlurUtil.fastblur(context, b, 1);//0-25，表示模糊值
//        view.findViewById(R.id.vBottomBackground).setBackground(new BitmapDrawable(context.getResources(), b));
        return view;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * 毛玻璃效果
     *
     * @param parent
     */
    public void showPopupTop2(@NotNull View parent) {
        View contentView = initView2(context, mTitleId);
        if (mPopupMenu == null) {
            // 获取状态栏高度
            // 获取屏幕长和高
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            DisplayMetrics realmetrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getRealMetrics(realmetrics);

            // 获取状态栏高度
            int statusBarHeight = 0;
            int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (identifier > 0) {
                statusBarHeight = (int) context.getResources().getDimension(identifier);
            }
            //除虚拟菜单高度
            int height = metrics.heightPixels + statusBarHeight + (context.getResources().getDimensionPixelSize(R.dimen.common_4));

            if (realmetrics.heightPixels == metrics.heightPixels) height = realmetrics.heightPixels;

            mPopupMenu = new SupportPopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, height);
            mPopupMenu.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
            mPopupMenu.setTouchable(true);
            // ColorDrawable dw = new ColorDrawable(0x80000000);
            // mPopupMenu.setBackgroundDrawable(dw);
            //全屏
            mPopupMenu.setClippingEnabled(false);
        }
        background = takeScreenShot(context);
        if (background != null) {
            mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), background));
        } else {
            mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        }
        mPopupMenu.setContentView(contentView);
//        if (Build.VERSION.SDK_INT >= 24) {
//            //7.0以上版本需要适配
//            Rect visibleFrame = new Rect();
//            parent.getGlobalVisibleRect(visibleFrame);
//            int height = parent.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
//            mPopupMenu.setHeight(height);
//            mPopupMenu.showAsDropDown(parent);
//        } else {
//        mPopupMenu.showAsDropDown(parent);
        mPopupMenu.showAtLocation(parent, Gravity.TOP, 0, 0);
//        }
        mPopupMenu.setAnimationStyle(R.style.popup_window_anim);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dismiss();
            }
        });
        mPopupMenu.update();
    }

    public void showPopupTop(@NonNull View parent) {
        View contentView = initView(context, mTitleId);
        if (mPopupMenu == null) {
            mPopupMenu = new SupportPopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mPopupMenu.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
            mPopupMenu.setTouchable(true);

            // ColorDrawable dw = new ColorDrawable(0x80000000);
            // mPopupMenu.setBackgroundDrawable(dw);
            mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));

        }
        mPopupMenu.setContentView(contentView);
//        if (Build.VERSION.SDK_INT >= 24) {
//            //7.0以上版本需要适配
//            Rect visibleFrame = new Rect();
//            parent.getGlobalVisibleRect(visibleFrame);
//            int height = parent.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
//            mPopupMenu.setHeight(height);
//            mPopupMenu.showAsDropDown(parent);
//        } else {
        mPopupMenu.showAsDropDown(parent);
//        }
        mPopupMenu.setAnimationStyle(R.style.popup_window_anim);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dismiss();
            }
        });
        mPopupMenu.update();
    }


    public void dismiss() {
        if (mPopupMenu != null && mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        }
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
        if (background != null) {
            mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
            background.recycle();
        }
    }

    Bitmap background = null;

    public void showBottomDialog() {
//        bottomSheetDialog = new BottomSheetDialog(context);
//        View view = initView(context, mTitleId);
//
////        BottomSheetBehavior.from(view).setState(BottomSheetBehavior.STATE_EXPANDED);
//        bottomSheetDialog.setOnDismissListener(dialog -> {
//            dismiss();
//        });
//        bottomSheetDialog.show();
    }

    /**
     * 截屏
     *
     * @param activity
     * @return
     */
    public Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DisplayMetrics realmetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getRealMetrics(realmetrics);


        // 获取状态栏高度
        int statusBarHeight = 0;
        int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            statusBarHeight = (int) context.getResources().getDimension(identifier);
        }
        //除虚拟菜单高度
        int height = metrics.heightPixels + statusBarHeight;
        if (realmetrics.heightPixels == metrics.heightPixels) height = realmetrics.heightPixels;
        Bitmap b = Bitmap.createBitmap(b1, 0, 0, b1.getWidth(), Math.min(b1.getHeight(), height));
        Matrix matrix = new Matrix();
        matrix.postScale(0.2f, 0.2f); //长和宽放大缩小的比例
        // 压缩 丢弃一部分像素点
        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        b = BlurUtil.fastblur(activity, b, 24);//0-25，表示模糊值
        view.destroyDrawingCache();
        return b;
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public boolean isShow() {
        return mPopupMenu != null && mPopupMenu.isShowing();
    }

    private final class PopupMenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Nullable
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_popup, null);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.mTypeTxt = convertView.findViewById(R.id.txt_type);
                holder.mIconImageView = convertView.findViewById(R.id.iv_icon);
                holder.mTypeTxt.setPaddingRelative(0, context.getResources().getDimensionPixelSize(R.dimen.common_4), 0, 0);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FileTypeItem item = itemList.get(position);

            Object title = item.getTitle();
            if (title instanceof Integer) {
                holder.mTypeTxt.setText((Integer) title);
            } else {
                holder.mTypeTxt.setText((String) title);
            }
            holder.mTypeTxt.setTextColor(context.getResources().getColor(R.color.darker));
            holder.mIconImageView.setImageResource(item.getNormalIcon());
            return convertView;
        }

        private final class ViewHolder {
            ImageView mIconImageView;
            TextView mTypeTxt;
        }
    }
}
