package net.sdvn.nascommon.model.phone.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;

public class LocalFileGridAdapter extends LocalFileBaseAdapter {

    public LocalFileGridAdapter(Context context, List<LocalFile> fileList, ArrayList<LocalFile> selectedList) {
        super(context, fileList, selectedList, null);
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
        CheckBox mSelectCb;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_filelist, null);

            holder = new ViewHolder();
            holder.mIconView = convertView.findViewById(R.id.iv_icon);
            holder.mNameTxt = convertView.findViewById(R.id.txt_name);
            holder.mSelectCb = convertView.findViewById(R.id.cb_select);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LocalFile file = mFileList.get(position);
        holder.mNameTxt.setText(file.getName());
        holder.mIconView.setTag(file.getName());

        showFileIcon(holder.mIconView, file);
        if (isMultiChooseModel() && !file.isDirectory()) {
            holder.mSelectCb.setVisibility(View.VISIBLE);
            holder.mSelectCb.setChecked(getSelectedList().contains(file));
        } else {
            holder.mSelectCb.setVisibility(View.GONE);
        }

        return convertView;
    }
}
