package net.sdvn.nascommon.model.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactsSortAdapter extends BaseAdapter implements SectionIndexer {
    @Nullable
    private List<SortModel> mList;
    private List<SortModel> mSelectedList;
    private Context mContext;
    LayoutInflater mInflater;
    private View.OnClickListener mListener;
    private int txtAction;

    public ContactsSortAdapter(Context mContext, @Nullable List<SortModel> list, View.OnClickListener onClickListener) {
        this.mContext = mContext;
        this.mListener = onClickListener;
        mSelectedList = new ArrayList<>();
        if (list == null) {
            this.mList = new ArrayList<>();
        } else {
            this.mList = list;
        }
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(@Nullable List<SortModel> list) {
        if (list == null) {
            this.mList = new ArrayList<>();
        } else {
            this.mList = list;
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mList.size();
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Nullable
    public View getView(final int position, @Nullable View view, ViewGroup arg2) {
        ViewHolder viewHolder;
        final SortModel mContent = mList.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, null);
            viewHolder.tvTitle = view.findViewById(R.id.title);
            viewHolder.tvNumber = view.findViewById(R.id.number);
            viewHolder.tvLetter = view.findViewById(R.id.catalog);
            viewHolder.btnInvite = view.findViewById(R.id.btn_invite);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (txtAction != 0) {
            viewHolder.btnInvite.setText(txtAction);
        }
        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);

        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.sortLetters);
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }

        viewHolder.tvTitle.setText(this.mList.get(position).name);
        viewHolder.tvNumber.setText(this.mList.get(position).number);
        viewHolder.btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClick(view);
            }
        });
        viewHolder.btnInvite.setTag(position);

        return view;

    }

    public void setTxtAction(int txtAction) {
        this.txtAction = txtAction;
    }

    public static class ViewHolder {
        public TextView tvLetter;
        public TextView tvTitle;
        public TextView tvNumber;
        public TextView btnInvite;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return mList.get(position).sortLetters.charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mList.get(i).sortLetters;
            char firstChar = sortStr.toUpperCase(Locale.CHINESE).charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    @Nullable
    @Override
    public Object[] getSections() {
        return null;
    }

    private boolean isSelected(SortModel model) {
        return mSelectedList.contains(model);
        //return true;
    }

    public void toggleChecked(int position) {
        if (isSelected(mList.get(position))) {
            removeSelected(position);
        } else {
            setSelected(position);
        }

    }

    private void setSelected(int position) {
        if (!mSelectedList.contains(mList.get(position))) {
            mSelectedList.add(mList.get(position));
        }
    }

    private void removeSelected(int position) {
        mSelectedList.remove(mList.get(position));
    }

    public List<SortModel> getSelectedList() {
        return mSelectedList;
    }

}