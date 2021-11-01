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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.oneos.OneOSUser;
import net.sdvn.nascommon.utils.ListViewUtils;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.HashMap;


public class SharePopupView {
    private static final String TAG = SharePopupView.class.getSimpleName();
    private ListView mListView;
    private ArrayList<OneOSUser> userList;
    private RelativeLayout mBackLayout;
    private Activity context;
    private Button mInviteNewUserBtn;
    private Button mShareBtn;
    public PopupListAdapter mAdapter;
    @NonNull
    private HashMap<Integer, Boolean> isSelected = new HashMap<>();
    private Dialog dialog;
    private String mgrname;

    public SharePopupView(Activity context) {
        this.context = context;
        userList = new ArrayList<>();

        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_popup_share, null);
//        View view = LayoutInflater.from(context).inflate(
//                R.layout.layout_popup_share, null);
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
        mAdapter = new PopupListAdapter();
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

    public void setOnClickListener(OnClickListener listener) {
        mShareBtn.setOnClickListener(listener);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void addUsers(@NonNull ArrayList<OneOSUser> users) {
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

    public class PopupListAdapter extends BaseAdapter {

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

        private class ViewHolder {
            TextView userName;
            TextView userAccount;
            CheckBox userSelect;
        }

        @Nullable
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.item_listview_share, null);
                holder = new ViewHolder();
                holder.userName = convertView.findViewById(R.id.share_user);
                holder.userAccount = convertView.findViewById(R.id.share_user_account);
                holder.userSelect = convertView.findViewById(R.id.select_user);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//            if (!TextUtils.isEmpty(mgrname) && userList.get(position).equals("admin")) {
//                holder.userName.setTitleText(mgrname);
//            } else {
            holder.userName.setText(userList.get(position).getMarkName());
            holder.userAccount.setText(userList.get(position).getName());
//            }
            holder.userSelect.setChecked(getIsSelected().get(position));
            return convertView;
        }

    }
}
