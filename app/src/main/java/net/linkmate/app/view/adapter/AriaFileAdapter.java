package net.linkmate.app.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.sdvn.nascommon.model.oneos.aria.AriaFile;
import net.sdvn.nascommon.utils.FileUtils;

import java.util.List;

public class AriaFileAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<AriaFile> mFileList;

    public AriaFileAdapter(Context context, List<AriaFile> fileList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mFileList = fileList;
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
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        CheckBox checkSelect;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_aria_file, null);

            holder = new ViewHolder();
            holder.fileIcon = convertView.findViewById(R.id.file_icon);
            holder.fileName = convertView.findViewById(R.id.file_name);
            holder.checkSelect = convertView.findViewById(R.id.file_select);
            holder.fileSize = convertView.findViewById(R.id.file_size);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AriaFile file = mFileList.get(position);
        holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(file.getPath()));
        String[] fileArr = file.getPath().split("/");
        String fileName = fileArr[fileArr.length - 1];
        holder.fileName.setText(fileName);
        long completeLen = 0, totalLen = 0;
        try {
            completeLen = Long.valueOf(file.getCompletedLength());
            totalLen = Long.valueOf(file.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.fileSize.setText(FileUtils.fmtFileSize(completeLen) + "/"
                + FileUtils.fmtFileSize(totalLen));

        return convertView;
    }

}
