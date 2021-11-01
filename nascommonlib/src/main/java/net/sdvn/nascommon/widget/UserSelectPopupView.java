package net.sdvn.nascommon.widget;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.utils.ListViewUtils;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public abstract class UserSelectPopupView<T> {
    private static final String TAG = UserSelectPopupView.class.getSimpleName();
    private ListView mListView;
    protected ArrayList<T> userList;
    private RelativeLayout mBackLayout;
    private Activity context;
    private Button mInviteNewUserBtn;
    private Button mShareBtn;

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    private BaseAdapter mAdapter;
    @NonNull
    protected HashMap<Integer, Boolean> isSelected = new HashMap<>();
    private Dialog dialog;
    private String mgrname;

    public UserSelectPopupView(Activity context, int resIdTitle) {
        this.context = context;
        userList = new ArrayList<>();

        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_popup_users_select, null);
        TextView text_title = view.findViewById(R.id.txt_title);
        text_title.setText(resIdTitle);
        mBackLayout = view.findViewById(R.id.layout_list);
        mBackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mInviteNewUserBtn = view.findViewById(R.id.btn_new_user);
        mShareBtn = view.findViewById(R.id.btn_share);

        mListView = view.findViewById(R.id.listview_user);
        TextView emptyView = view.findViewById(R.id.txt_empty);
        mListView.setVisibility(View.VISIBLE);
        mListView.setEmptyView(emptyView);
        mAdapter = new PopupListAdapter<T>();
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
//        mPopupMenu = new PopupWindow(view, LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT);
//        mPopupMenu.setAnimationStyle(R.style.AnimationAlphaEnterAndExit);
//        mPopupMenu.setTouchable(true);
//        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context
//                .getResources(), (Bitmap) null));

        dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();

        Button mCancelBtn = view.findViewById(R.id.btn_cancel);
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    public void setPositiveButton(OnClickListener listener) {
        mShareBtn.setOnClickListener(listener);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    public void addUsers(@NonNull List<T> users) {
        userList.clear();
        userList.addAll(users);
        initDate();
    }

    private void initDate() {
        isSelected.clear();
        for (int i = 0; i < userList.size(); i++) {
            isSelected.put(i, false);
        }
        ListViewUtils.setListViewMaxHeight(context, mListView);
        mAdapter.notifyDataSetChanged();
    }

//    public void showPopupCenter(View parent) {
//        mPopupMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);
//        mPopupMenu.setFocusable(true);
//        mPopupMenu.setOutsideTouchable(true);
//        mPopupMenu.update();
//    }

    @NonNull
    public HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    public boolean isSelected(int pos) {
        final Boolean aBoolean = isSelected.get(pos);
        return aBoolean != null && aBoolean;
    }

    public void setMgrName(String mgrname) {
        this.mgrname = mgrname;
    }

    public void setIsAdmin(boolean isAdmin, OnClickListener listener) {
        if (isAdmin) {
            mInviteNewUserBtn.setVisibility(View.VISIBLE);
            mInviteNewUserBtn.setOnClickListener(listener);
        } else {
            mInviteNewUserBtn.setVisibility(View.GONE);
        }
    }

    public class PopupListAdapter<T> extends BaseAdapter {

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
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
                convertView = LayoutInflater.from(context).inflate(
                        getLayoutResId(), null);
                holder = createViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            bindViewHolder(holder, position);
            return convertView;
        }

    }

    protected abstract void bindViewHolder(ViewHolder holder, int position);

    public abstract static class ViewHolder {
        public View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
//    holder = new ViewHolder();
//    holder.userName = convertView.findViewById(R.id.share_user);
//    holder.userAccount = convertView.findViewById(R.id.share_user_account);
//    holder.userSelect = convertView.findViewById(R.id.select_user);

//          holder.userName.setTitleText(userList.get(position). ());
//            holder.userAccount.setTitleText(userList.get(position).getName());
//            holder.userSelect.setChecked(getIsSelected().get(position));

    public abstract int getLayoutResId();

    @NonNull
    public abstract UserSelectPopupView.ViewHolder createViewHolder(View view);
}
