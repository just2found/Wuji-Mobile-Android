package net.linkmate.app.ui.activity.nasApp.aria;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.view.TipsBar;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.PermissionChecker;
import net.sdvn.nascommon.utils.SDCardUtils;
import net.sdvn.nascommon.widget.CheckableImageButton;

import org.view.libwidget.OnItemClickListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectTorrentActivity extends BaseActivity {
    private static final String TAG = "SetDownloadPathActivity";

    private static final String SUFFIX_TORRENT = ".torrent";

    private ListView mListView;
    private PathAdapter mAdapter;
    @Nullable
    private File curFile = null;
    @NonNull
    private ArrayList<File> mFileList = new ArrayList<File>();
    @Nullable
    private List<File> mSDCardList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_select_torrent);
        initViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkStoragePermission();
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void checkStoragePermission() {
        PermissionChecker.checkPermission(this, strings -> {
            mSDCardList = SDCardUtils.getSDCardList();
            if (null == mSDCardList || mSDCardList.size() == 0) {
                notifyNoSDCardDialog();
                return;
            }
            curFile = null;
            refreshFileList(curFile);
        }, strings -> {
            UiUtils.showStorageSettings(this);
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private RelativeLayout rlTitle;
    private ImageView itbIvLeft;
    private TextView itbTvTitle;

    private void initViews() {
        rlTitle = findViewById(R.id.itb_rl);
        itbIvLeft = findViewById(R.id.itb_iv_left);
        itbTvTitle = findViewById(R.id.itb_tv_title);

        itbTvTitle.setText(R.string.select_torrent_file);
        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);
        itbIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mListView = findViewById(R.id.listview_path);
        mAdapter = new PathAdapter(this);
        mListView.setAdapter(mAdapter);
        TextView mEmptyView = findViewById(R.id.txt_content_empty);
        mListView.setEmptyView(mEmptyView);
        mAdapter.setOnItemClickListener( (mFile, position, view) -> {
            if (mFile.isDirectory()) {
                refreshFileList(mFile);
                mAdapter.notifyDataSetInvalidated();
                mAdapter.notifyDataSetChanged();
            } else {
                if (mFile.exists() && mFile.getName().endsWith(SUFFIX_TORRENT)) {
                    if (mFile != null) {
                        Intent intent = new Intent();
                        intent.putExtra("TorrentPath", mFile.getAbsolutePath());
                        intent.putExtra("TorrentName", mFile.getName());
                        setResult(RESULT_OK, intent);
                        SelectTorrentActivity.this.finish();
                    }
                }
            }
        } );
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    /**
     * Add a dialog box used to confirm the operation
     */
    protected void notifyNoSDCardDialog() {
        DialogUtils.showNotifyDialog(this, R.string.tips, R.string.tips_no_sd_card,
                R.string.ok, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        SelectTorrentActivity.this.finish();
                    }
                });
    }

    /**
     * back to previous file tree
     */
    private void upLevel() {
        if (null != curFile) {
            File parentFile = null;
            if (isUpSDCardRoot(curFile)) {
                parentFile = null; // for back to SDCard List
            } else {
                parentFile = curFile.getParentFile();
            }

            refreshFileList(parentFile);
        }
    }

    @Override
    public void onBackPressed() {
        if (null == curFile) {
            SelectTorrentActivity.this.finish();
        } else {
            mAdapter.notifyDataSetInvalidated();
            mAdapter.notifyDataSetChanged();
            upLevel();
        }
    }

    public boolean isUpSDCardRoot(@Nullable File file) {
        if (null != file) {
            for (File root : mSDCardList) {
                if (root.getAbsolutePath().equals(file.getAbsolutePath())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * refresh the file list
     */
    public void refreshFileList(@Nullable File mFile) {
        if (mFile == null) {
            mFileList.clear();
            mFileList.addAll(mSDCardList);
        } else {
            loadFileList(mFile);
        }
        curFile = mFile;
        mAdapter.setFileList(mFileList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Load file list by file path
     */
    private void loadFileList(@Nullable File mFile) {
        if (mFile != null) {
            mFileList.clear();
            List<File> list = orderFilesByName(mFile);

            if (list != null) {
                mFileList.addAll(list);
            }
        }
    }

    /**
     * Sort files, order by file name
     */
    @Nullable
    private List<File> orderFilesByName(@Nullable File mFile) {
        List<File> files = null;
        if (mFile != null) {
            files = Arrays.asList(mFile.listFiles(fileNameFilter));
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(@NonNull File file1, @NonNull File file2) {
                    if (file1.isDirectory() && file2.isFile())
                        return -1;
                    if (file1.isFile() && file2.isDirectory())
                        return 1;
                    return file1.getName().compareTo(file2.getName());
                }
            });
        }
        return files;
    }

    @NonNull
    FilenameFilter fileNameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, @NonNull String filename) {
            File file = new File(dir.getAbsolutePath() + File.separator + filename);
            if (file.isDirectory()) {
                return !filename.startsWith(".");
            } else {
                return filename.endsWith(SUFFIX_TORRENT);
            }
        }
    };

    public class PathAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        @NonNull
        private ArrayList<File> mList = new ArrayList<File>();
        private OnItemClickListener<File> onItemClickListener;

        public PathAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public void setFileList(@Nullable ArrayList<File> list) {
            mList.clear();
            if (list != null) {
                mList.addAll(list);
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setOnItemClickListener(OnItemClickListener<File> onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        class ViewHolder {
            TextView fileName;
            ImageView fileIcon;
            CheckableImageButton fileSelect;
            RelativeLayout layout;
        }

        @Nullable
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_path, null);
                holder = new ViewHolder();
                holder.fileName = convertView.findViewById(R.id.file_name);
                holder.fileIcon = convertView.findViewById(R.id.file_icon);
                holder.fileSelect = convertView.findViewById(R.id.file_select);
                holder.layout = convertView.findViewById(R.id.layout_path);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            File mFile = mList.get(position);
            holder.fileName.setText(mFile.getName());
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(mFile));
            holder.fileSelect.setVisibility(View.GONE);
            convertView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.OnItemClick(mFile, position, v);
                }
            });
            return convertView;
        }
    }

}
