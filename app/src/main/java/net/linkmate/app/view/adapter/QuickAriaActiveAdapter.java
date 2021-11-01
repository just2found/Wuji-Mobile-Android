package net.linkmate.app.view.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.sdvn.nascommon.model.oneos.aria.AriaFile;
import net.sdvn.nascommon.model.oneos.aria.AriaInfo;
import net.sdvn.nascommon.model.oneos.aria.BitTorrent;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.widget.CircleStateProgressBar;

import java.util.List;

public class QuickAriaActiveAdapter extends BaseQuickAdapter<AriaInfo, QuickAriaActiveAdapter.ViewHolder> {

    private OnAriaControlListener mListener;

    public QuickAriaActiveAdapter() {
        super(R.layout.item_listview_transfer);
    }

    @Override
    protected void convert(@NonNull ViewHolder holder, @NonNull final AriaInfo mElement) {
        boolean isBTAria = true;
        String taskName = "";
        BitTorrent bt = mElement.getBittorrent();
        if (null != bt) {
            isBTAria = true;
            if (null != bt.getInfo()) {
                taskName = bt.getInfo().getName();
            }
        } else {
            isBTAria = false;
            List<AriaFile> files = mElement.getFiles();
            if (null != files && files.size() > 0) {
                for (AriaFile file : files) {
                    String name = FileUtils.getFileName(file.getPath());
                    taskName += name + " ";
                }
            }
        }

        int ratio = 0;
        long completeLen = 0, totalLen = 0, speed = 0;

        try {
            completeLen = Long.valueOf(mElement.getCompletedLength());
            totalLen = Long.valueOf(mElement.getTotalLength());
            speed = Long.valueOf(mElement.getDownloadSpeed());
            if (totalLen <= 0) {
                ratio = 0;
            } else {
                ratio = (int) (completeLen * 100 / totalLen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String size = FileUtils.fmtFileSize(completeLen) + "/"
                + FileUtils.fmtFileSize(totalLen);
        holder.fileName.setText(taskName);
        holder.circleProgress.setProgress(ratio);
        if (isBTAria) {
            holder.fileIcon.setImageResource(R.drawable.icon_aria_bt);
        } else {
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(taskName));
        }
        holder.fileRatio.setText(ratio + "%");

        String status = mElement.getStatus();
        if (status.equalsIgnoreCase("active")) {
            size += "    " + FileUtils.fmtFileSize(speed) + "/s";
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.START);
        } else if (status.equalsIgnoreCase("waiting")) {
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.WAIT);
            holder.fileRatio.setText(R.string.waiting);
        } else if (status.equalsIgnoreCase("paused")) {
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.PAUSE);
            holder.fileRatio.setText(R.string.paused);
        } else if (status.equalsIgnoreCase("error")) {
            holder.circleProgress.setState(CircleStateProgressBar.ProgressState.FAILED);
            holder.fileRatio.setText(R.string.download_failed);
        }
        holder.fileSize.setText(size);
        // else if (status.equalsIgnoreCase("removed")) {
        // holder.circleProgress.setShareState(ProgressState.FAILED);
        // }
        holder.addOnClickListener(R.id.layout_power_off);
        holder.circleProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onControl(mElement, false);
                }
            }
        });

        holder.deleteTxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onControl(mElement, true);
                }
            }
        });

    }


    public static class ViewHolder extends BaseViewHolder {
        ViewGroup leftLayout;
        ViewGroup rightLayout;
        ImageView fileIcon;
        TextView fileName;
        TextView fileRatio;
        TextView fileSize;
        TextView deleteTxt;
        CircleStateProgressBar circleProgress;

        public ViewHolder(@NonNull View convertView) {
            super(convertView);
            leftLayout = convertView.findViewById(R.id.layout_power_off);
            rightLayout = convertView.findViewById(R.id.layout_right);
            fileIcon = convertView.findViewById(R.id.fileImage);
            fileName = convertView.findViewById(R.id.fileName);
            fileSize = convertView.findViewById(R.id.fileSize);
            fileRatio = convertView.findViewById(R.id.ratio);
            circleProgress = convertView.findViewById(R.id.progress);
            deleteTxt = convertView.findViewById(R.id.txt_delete);
        }
    }


    public void setOnAriaControlListener(OnAriaControlListener mListener) {
        this.mListener = mListener;
    }

    public interface OnAriaControlListener {
        void onControl(AriaInfo info, boolean isDel);
    }
}
