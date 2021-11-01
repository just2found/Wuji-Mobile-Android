package net.linkmate.app.ui.activity.mine;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.view.TipsBar;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.model.FileManageAction;
import net.sdvn.nascommon.model.FileOrderType;
import net.sdvn.nascommon.model.phone.LocalFileManage;
import net.sdvn.nascommon.model.phone.LocalFileType;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.PermissionChecker;
import net.sdvn.nascommon.utils.SDCardUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommon.widget.CheckableImageButton;
import net.sdvn.nascommon.widget.FilePathPanel;
import net.sdvn.nascommon.widget.TitleBackLayout;

import org.view.libwidget.OnItemClickListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SetDownloadPathActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = SetDownloadPathActivity.class.getSimpleName();

    private ListView mListView;
    private Button mConfirmBtn;
    private TextView mPathText;
    private PathAdapter mAdapter;
    private TitleBackLayout layout_title;
    // private File root = null;
    /**
     * Current Dir Path, if null is RootDir
     */
    @Nullable
    private File curFile = null;
    @NonNull
    private List<File> mFileList = new ArrayList<File>();
    @Nullable
    private List<File> mSDCardList = null;
    @Nullable
    private String savePath = null;

    private boolean isLocal = true;
    @NonNull
    private LocalFileManage.OnManageCallback mFileManageCallback = new LocalFileManage.OnManageCallback() {
        @Override
        public void onStart(int resStrId) {
            if (resStrId > 0) {
                showLoading(resStrId);
            }
        }

        @Override
        public void onComplete(boolean isSuccess) {
            dismissLoading();
            if (isSuccess)
                refreshFileList(curFile);
        }
    };
    private FilePathPanel mPathPanel;
    private FileOrderType mOrderType;
    private boolean isListShown;
    @Nullable
    private String rootPath;
    private boolean isInit;

    protected int getLayoutId() {
        return (R.layout.activity_tool_set_download_path);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initViews();
        requestData();
    }

    @Nullable
    @Override
    protected View getTopView() {
        return layout_title;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void requestData() {
        PermissionChecker.checkPermission(this, strings -> {
            initData();
        }, strings -> {
            finish();
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        isInit = true;
    }

    private void initData() {
        isInit = true;
        mSDCardList = SDCardUtils.getSDCardList();
        if (null == mSDCardList || mSDCardList.size() == 0) {
            notifyNoSDCardDialog(this);
            return;
        }

        for (File root : mSDCardList) {
            Logger.LOGE(TAG, "---SD Path: " + root.getAbsolutePath());
        }

        curFile = null;
        refreshFileList(curFile);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInit) {
            requestData();
        }
        String path = SessionManager.getInstance().getDefaultDownloadPath();
        path = (TextUtils.isEmpty(path) ? getResources().getString(R.string.download_path_not_set) : path);
        mPathText.setText(String.format("%s%s", getResources().getString(R.string.current_path), path));
    }

    private void initViews() {
        layout_title = findViewById(R.id.layout_title);
        layout_title.setBackTitle(R.string.set_dir_to_download_path);
        layout_title.setOnClickBack(this);
        mConfirmBtn = this.findViewById(R.id.btn_confirm);
        mConfirmBtn.setOnClickListener(this);
        isLocal = true;

        mPathText = findViewById(R.id.text_path);
        mPathPanel = findViewById(R.id.act_set_down_path_panel);
        mPathPanel.setOnPathPanelClickListener(new FilePathPanel.OnPathPanelClickListener() {
            @Override
            public void onClick(@NonNull View view, @Nullable String path) {
                if (view.getId() == R.id.ibtn_new_folder) {
                    if (curFile != null && FileUtils.isExternalStorageWritable()) {
                        // New Folder Button Clicked
                        LocalFileManage localFileManage = new LocalFileManage(SetDownloadPathActivity.this, mPathPanel, mFileManageCallback);
                        localFileManage.manage(FileManageAction.MKDIR, curFile.getAbsolutePath());
                    }
                } else if (view.getId() == R.id.ibtn_order) {
//                    showOrderPopView(view);
                } else {
                    Logger.LOGD(TAG, ">>>>>Click Path: " + path + ", Root Path:" + rootPath);
                    if (null == path || rootPath == null) {
                        curFile = null;
                        rootPath = null;
                        refreshFileList(curFile);
                    } else {
                        File file = new File(path);
                        File root = new File(rootPath);
                        if (file.equals(root)) {
                            curFile = null;
                            rootPath = null;
                        } else {
                            curFile = file;
                        }
                        refreshFileList(curFile);
                    }
                }
            }
        });
        mPathPanel.showNewFolderButton(false);
        mPathPanel.showOrderButton(false);
        View mEmptyLayout = findViewById(R.id.layout_empty);
        mListView = findViewById(R.id.listview_path);
        mListView.setEmptyView(mEmptyLayout);
        mAdapter = new PathAdapter(this);
        mListView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((mFile, position, view) -> {
                    if (mFile.isDirectory()) {
                        if (null == curFile) {
                            rootPath = mFile.getParent();
                        }
                        refreshFileList(mFile);
                        savePath = mFile.getAbsolutePath();
                        mAdapter.notifyDataSetInvalidated();
                        mAdapter.notifyDataSetChanged();
                    }
                    updateConfirmBtn();
                }
        );
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                setDefaultDownloadPath();
                break;
            default:
                break;
        }
    }

    /**
     * Add a dialog box used to confirm the operation
     */
    protected void notifyNoSDCardDialog(final Context context) {

        DialogUtils.showNotifyDialog(context, R.string.tips, R.string.tips_no_sd_card, R.string.ok,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        SetDownloadPathActivity.this.finish();
                    }
                });
    }

    private void updateConfirmBtn() {
        if (savePath != null) {
            mConfirmBtn.setEnabled(true);
        } else {
            mConfirmBtn.setEnabled(false);
        }
    }

    private void setDefaultDownloadPath() {
        if (savePath == null || savePath.length() == 0) {
            ToastHelper.showToast(R.string.set_dir_to_download_path);
            return;
        }

        File file = new File(savePath);
        if (file.canWrite()) {
            if (SPHelper.put(AppConstants.SP_FIELD_DEFAULT_LOCAL_DOWNLOAD_PATH, savePath)) {
                ToastHelper.showToast(R.string.setting_success);
                SetDownloadPathActivity.this.finish();
            } else {
                ToastHelper.showToast(R.string.setting_failed);
            }
        } else {
            DialogUtils.showNotifyDialog(this, R.string.tips, R.string.error_path_without_write_permission, R.string.ok, null);
        }
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
            SetDownloadPathActivity.this.finish();
        } else {
            savePath = null;
            mAdapter.notifyDataSetInvalidated();
            mAdapter.notifyDataSetChanged();
            upLevel();
        }
    }

    public boolean isUpSDCardRoot(@Nullable File file) {
        if (null != file) {
            for (File root : mSDCardList) {
                Logger.LOGD(TAG, "----SDCard Dir: " + root.getAbsolutePath());
                Logger.LOGD(TAG, "----Uplevel Dir: " + file.getAbsolutePath());
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
            mPathPanel.showNewFolderButton(false);
        } else {
            loadFileList(mFile);
            mPathPanel.showNewFolderButton(true);
        }
        curFile = mFile;
        mPathPanel.updatePath(LocalFileType.PRIVATE, curFile != null ? curFile.getAbsolutePath() : null, rootPath);
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
            File[] list = mFile.listFiles(fileNameFilter);
            if (list != null) {
                files = Arrays.asList(list);
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
            }
            return false;
        }
    };

    public class PathAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        @NonNull
        private ArrayList<File> mList = new ArrayList<>();
        private SparseBooleanArray isSelected = new SparseBooleanArray();
        private OnItemClickListener<File> onItemClickListener;

        public PathAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public void setFileList(@Nullable List<File> list) {
            mList.clear();
            if (list != null) {
                mList.addAll(list);
            }

            initDate();
        }

        public SparseBooleanArray getIsSelected() {
            return isSelected;
        }

        public void setIsSelected(SparseBooleanArray isSelected) {
            this.isSelected = isSelected;
        }

        /**
         * init date of isSelected
         */
        private void initDate() {
            isSelected.clear();
            for (int i = 0; i < mList.size(); i++) {
                isSelected.put(i, false);
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
        }

        @Nullable
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {

            ViewHolder holder;
            final int selectedPosition = position;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_path, null);
                holder = new ViewHolder();
                holder.fileName = convertView.findViewById(R.id.file_name);
                holder.fileIcon = convertView.findViewById(R.id.file_icon);
                holder.fileSelect = convertView.findViewById(R.id.file_select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.fileSelect.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    boolean select = !getIsSelected().get(selectedPosition);

                    for (int i = 0; i < mList.size(); i++) {
                        if (getIsSelected().get(i)) {
                            getIsSelected().put(i, false);
                        }
                    }

                    getIsSelected().put(selectedPosition, select);

                    File mFile = mFileList.get(selectedPosition);
                    if (mFile.isDirectory() && mFile.exists() && select) {
                        savePath = mFile.getAbsolutePath();
                    } else {
                        savePath = null;
                    }
                    updateConfirmBtn();
                    notifyDataSetChanged();
                }
            });

            File mFile = mList.get(position);
            holder.fileName.setText(mFile.getName());
            holder.fileIcon.setImageResource(FileUtils.fmtFileIcon(mFile));

            holder.fileSelect.setChecked(getIsSelected().get(position));
            convertView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.OnItemClick(mFile, position, v);
                }
            });

            return convertView;
        }

    }
}
