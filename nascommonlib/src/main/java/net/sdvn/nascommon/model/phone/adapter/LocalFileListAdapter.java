package net.sdvn.nascommon.model.phone.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;

public class LocalFileListAdapter extends LocalFileBaseAdapter {

    public LocalFileListAdapter(Context context, List<LocalFile> fileList, ArrayList<LocalFile> selectedList, OnMultiChooseClickListener listener) {
        super(context, fileList, selectedList, listener);
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        ImageView mIconView;
        TextView mNameTxt;
        TextView mTimeTxt;
        TextView mSizeTxt;
        CheckBox mSelectCb;
        ImageButton mSelectIBtn;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_filelist, null);

            holder = new ViewHolder();
            holder.mIconView = convertView.findViewById(R.id.iv_icon);
            holder.mNameTxt = convertView.findViewById(R.id.txt_name);
            holder.mSelectCb = convertView.findViewById(R.id.cb_select);
            holder.mSizeTxt = convertView.findViewById(R.id.txt_size);
            holder.mTimeTxt = convertView.findViewById(R.id.txt_time);
            holder.mSelectIBtn = convertView.findViewById(R.id.ibtn_select);
            holder.mSelectIBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(v);
                    }
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mSelectIBtn.setTag(position); // for get Select Button Index

        LocalFile file = mFileList.get(position);
        holder.mNameTxt.setText(file.getName());
//        holder.mIconView.setTag(file.getName());
        holder.mTimeTxt.setText(FileUtils.formatTime(file.lastModified()));
        holder.mSizeTxt.setText(file.isDirectory() ? "" : FileUtils.fmtFileSize(file.length()));

        showFileIcon(holder.mIconView, file);
        if (isMultiChooseModel()) {
            holder.mSelectIBtn.setVisibility(View.GONE);
            holder.mSelectCb.setVisibility(View.VISIBLE);
            holder.mSelectCb.setChecked(getSelectedList().contains(file));
        } else {
            holder.mSelectCb.setVisibility(View.GONE);
            holder.mSelectIBtn.setVisibility(View.VISIBLE);
        }

        if (file.isDirectory()) {
            holder.mSelectIBtn.setVisibility(View.GONE);
            holder.mSelectCb.setVisibility(View.GONE);
        }

        return convertView;
    }
}
