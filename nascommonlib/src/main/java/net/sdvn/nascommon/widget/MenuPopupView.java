package net.sdvn.nascommon.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class MenuPopupView implements PopupWindow.OnDismissListener {
    private ArrayList<Integer> itemList;
    private ArrayList<Integer> resList;
    private ArrayList<Integer> countList;
    private Context context;
    private PopupWindow mPopupMenu;
    private ListView mListView;
    private int mark = -1;
    private boolean showAllNum;
    private final MenuPopupViewAdapter mMenuPopupViewAdapter;
    private int mColorRes = R.color.light_gray;

    public MenuPopupView(@NonNull Context context, int width) {
        this.context = context;

        itemList = new ArrayList<>();
        resList = new ArrayList<>();
        countList = new ArrayList<>();

        View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_menu, null);
        mListView = view.findViewById(R.id.listview_menu);
        mListView.setVisibility(View.VISIBLE);
        mMenuPopupViewAdapter = new MenuPopupViewAdapter();
        mListView.setAdapter(mMenuPopupViewAdapter);
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(true);
        if (width > 0)
            mPopupMenu = new PopupWindow(view, width, LayoutParams.WRAP_CONTENT);
        else
            mPopupMenu = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
    }

    public void setMenuItems(int[] title, int[] resId) {
        setMenuItems(title, resId, null);
    }

    public void setMenuItems(@Nullable int[] title, @Nullable int[] resId, @Nullable int[] counts) {
        if (null == title && null == resId) {
            return;
        }

        if (title != null) {
            for (int i = 0; i < title.length; i++) {
                itemList.add(title[i]);
                if (resId != null && i < resId.length) {
                    resList.add(resId[i]);
                } else {
                    resList.add(-1);
                }
                if (counts != null && i < counts.length) {
                    countList.add(counts[i]);
                } else {
                    countList.add(-1);
                }
            }
        }
        mMenuPopupViewAdapter.notifyDataSetChanged();
    }

    public void setMenuCounts(Map<Integer, Integer> map) {
        for (Map.Entry<Integer, Integer> integerIntegerEntry : map.entrySet()) {
            for (int i = 0; i < itemList.size(); i++) {
                if (Objects.equals(itemList.get(i), integerIntegerEntry.getKey())) {
                    countList.set(i, integerIntegerEntry.getValue());
                    break;
                }
            }
        }
        mMenuPopupViewAdapter.notifyDataSetChanged();
    }

    public void setCountTextColor(@ColorRes int colorRes) {
        mColorRes = colorRes;
    }

    @Nullable
    public String getPopupItem(int index) {
        if (index < itemList.size()) {
            return context.getResources().getString(itemList.get(index));
        }

        return null;
    }

    public void showPopupDown(View parent, int mark, boolean isAlignRight) {
        this.mark = mark;
        if (isAlignRight) {
            mListView.setBackgroundResource(R.drawable.bg_pop_right);
        } else {
            mListView.setBackgroundResource(R.drawable.bg_pop_left);
        }
        mPopupMenu.showAsDropDown(parent);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.update();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        mPopupMenu.setOnDismissListener(onDismissListener);
        mPopupMenu.setOnDismissListener(onDismissListener);
    }

    public void dismiss() {
        if (mPopupMenu != null && mPopupMenu.isShowing()) {
            mPopupMenu.dismiss();
        }
    }

    public void setOnMenuClickListener(@Nullable final OnMenuClickListener listener) {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (null != listener) {
                    listener.onMenuClick(arg2, arg1);
                }

                dismiss();
            }
        });
    }

    public boolean isShowAllNum() {
        return showAllNum;
    }

    public void setShowAllNum(boolean showAllNum) {
        this.showAllNum = showAllNum;
    }

    @Override
    public void onDismiss() {

    }

    private class MenuPopupViewAdapter extends BaseAdapter {

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
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_listview_menu, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTitleTxt.setText(itemList.get(position));
            if (position < resList.size()) {
                if (resList.get(position) > 0) {
                    holder.mIconImage.setVisibility(View.VISIBLE);
                    holder.mIconImage.setImageResource(resList.get(position));
                } else {
                    holder.mIconImage.setVisibility(View.GONE);
                }
            }
            if (position < countList.size()) {
                if (countList.get(position) > 0) {
                    if (!showAllNum && countList.get(position) > 99) {
                        holder.mCountTxt.setText("99+");
                    } else {
                        holder.mCountTxt.setText(String.valueOf(countList.get(position)));
                    }
                    holder.mCountTxt.setVisibility(View.VISIBLE);
                } else {
                    holder.mCountTxt.setVisibility(View.GONE);
                }
            }

            if (mark == position) {
                holder.mTitleTxt.setTextColor(context.getResources().getColor(R.color.primary));
            } else {
                holder.mTitleTxt.setTextColor(context.getResources().getColor(R.color.gray));
            }

            return convertView;
        }

    }

    static class ViewHolder {
        TextView mTitleTxt;
        ImageView mIconImage;
        TextView mCountTxt;

        public ViewHolder(View convertView) {
            this.mTitleTxt = convertView.findViewById(R.id.txt_title);
            this.mIconImage = convertView.findViewById(R.id.iv_icon);
            this.mCountTxt = convertView.findViewById(R.id.txt_count);
        }
    }

    /**
     * interface for listener menu click
     *
     * @author shz
     */
    public interface OnMenuClickListener {
        void onMenuClick(int index, View view);
    }
}
