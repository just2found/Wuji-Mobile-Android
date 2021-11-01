package net.sdvn.nascommon.model.phone;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.BackupFileKeeper;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.db.UserSettingsKeeper;
import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.db.objecbox.UserSettings;
import net.sdvn.nascommon.model.FileListChangeObserver;
import net.sdvn.nascommon.model.FileManageAction;
import net.sdvn.nascommon.model.oneos.backup.BackupPriority;
import net.sdvn.nascommon.model.oneos.backup.BackupType;
import net.sdvn.nascommon.model.oneos.user.LoginManage;
import net.sdvn.nascommon.model.phone.api.MakeDirAPI;
import net.sdvn.nascommon.model.phone.api.ShareFileAPI;
import net.sdvn.nascommon.service.NasService;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.InputMethodUtils;
import net.sdvn.nascommon.utils.SDCardUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommon.widget.LocalFileTreeView;
import net.sdvn.nascommonlib.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OneSpace OS File Manage
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class LocalFileManage {
    private static final String TAG = LocalFileManage.class.getSimpleName();

    private FragmentActivity mActivity;
    @Nullable
    private FileManageAction mAction;
    private View mRootView;
    private OnManageCallback callback;
    private List<LocalFile> fileList;
    @Nullable
    private LocalFileManageTask fileManageTask;
    private String mToId;
    private String uploadPath;
    @NonNull
    private LocalFileManageTask.OnLocalFileManageListener listener = new LocalFileManageTask.OnLocalFileManageListener() {
        @Override
        public void onStart(FileManageAction action) {
            int resStrId = 0;
            if (action == FileManageAction.DELETE) {
                resStrId = (R.string.deleting_file);
            } else if (action == FileManageAction.RENAME) {
                resStrId = (R.string.renaming_file);
            } else if (action == FileManageAction.MKDIR) {
                resStrId = (R.string.making_folder);
            } else if (action == FileManageAction.COPY) {
                resStrId = (R.string.copying_file);
            } else if (action == FileManageAction.MOVE) {
                resStrId = (R.string.moving_file);
            }
            if (callback != null) {
                callback.onStart(resStrId);
            }
        }

        @Override
        public void onComplete(boolean result, FileManageAction action, String errorMsg) {
            if (result) {
                if (action == FileManageAction.ATTRIBUTES) {
                    // {"result":true, "path":"/PS-AI-CDR","dirs":1,"files":10,"size":3476576309,"uid":1001,"gid":0}
                    try {
                        final LocalFile lFile = fileList.get(0);
                        final File file = lFile.getFile();
                        Resources resources = mActivity.getResources();
                        List<String> titleList = new ArrayList<>();
                        List<String> contentList = new ArrayList<>();
                        titleList.add(resources.getString(R.string.file_attr_path));
                        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "onComplete: position = " + file.getAbsolutePath());
                        contentList.add(file.getAbsolutePath());
                        titleList.add(resources.getString(R.string.file_attr_size));
                        contentList.add(FileUtils.fmtFileSize(file.length()) + " (" + file.length() + resources.getString(R.string.tail_file_attr_size_bytes) + ")");
                        titleList.add(resources.getString(R.string.file_attr_time));
                        contentList.add(FileUtils.formatTime(file.lastModified()));
                        titleList.add(resources.getString(R.string.file_attr_read));
                        contentList.add(file.canRead() ? "True" : "False");
                        titleList.add(resources.getString(R.string.file_attr_write));
                        contentList.add(file.canWrite() ? "True" : "False");

                        final LoginManage loginManage = LoginManage.getInstance();
                        int topId = 0;
                        if (loginManage.isLogin()) {
                            if (file.isDirectory()) {
                                if (lFile.isBackupDir()) {
                                    topId = R.string.cancel_backup_file;
                                } else if (file.canRead()) {
                                    topId = R.string.add_backup_file;
                                }
                            }
                        }

                        int midId = 0;
                        if (loginManage.isLogin()) {
                            if (file.isDirectory() && !lFile.isDownloadDir() && file.canWrite()) {
                                midId = R.string.set_dir_to_download_path;
                            }
                        }

                        DialogUtils.showListDialog(mActivity, titleList, contentList, R.string.tip_attr_file, topId, midId, R.string.ok,
                                new DialogUtils.OnMultiDialogClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int index) {
                                        if (index == 1) {
                                            setDownloadPath(file.getAbsolutePath());
                                        } else if (index == 2) {
                                            addOrRemoveBackup(lFile);
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastHelper.showToast(R.string.error_json_exception);
                    }
                } else if (action == FileManageAction.DELETE) {
                    ToastHelper.showToast(R.string.delete_file_success);
//                    mActivity.showTipView(R.string.delete_file_success, true);
                } else if (action == FileManageAction.RENAME) {
                    ToastHelper.showToast(R.string.rename_file_success);
//                    mActivity.showTipView(R.string.rename_file_success, true);
                } else if (action == FileManageAction.MKDIR) {
                    ToastHelper.showToast(R.string.new_folder_success);
//                    mActivity.showTipView(R.string.new_folder_success, true);
                } else if (action == FileManageAction.COPY) {
                    ToastHelper.showToast(R.string.copy_file_success);
//                    mActivity.showTipView(R.string.copy_file_success, true);
                } else if (action == FileManageAction.MOVE) {
                    ToastHelper.showToast(R.string.move_file_success);
//                    mActivity.showTipView(R.string.move_file_success, true);
                } else if (action == FileManageAction.SHARE) {
                    ToastHelper.showToast(R.string.share_file_success);
//                    mActivity.showTipView(R.string.share_file_success, true);
                }
                FileListChangeObserver.getInstance().FileListChange();
            } else {
                ToastHelper.showToast(R.string.operate_failed);
//                mActivity.showTipView(R.string.operate_failed, false);
            }

            if (null != callback) {
                callback.onComplete(result);
            }
        }
    };
    @Nullable
    private Long groupId;

    public LocalFileManage(FragmentActivity activity, View rootView, OnManageCallback callback) {
        this.mActivity = activity;
        this.mRootView = rootView;
        this.callback = callback;
    }

    public void manage(final LocalFileType type, @Nullable FileManageAction action
            , @NonNull final List<LocalFile> selectedList) {
        this.mAction = action;
        this.fileList = Collections.synchronizedList(selectedList);

        if (EmptyUtils.isEmpty(fileList) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.DELETE) {
            DialogUtils.showConfirmDialog(mActivity, R.string.tips, R.string.tip_delete_file, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        fileManageTask = new LocalFileManageTask(fileList, mAction, null, listener);
                        fileManageTask.execute(0);
                    }
                }
            });
        } else if (action == FileManageAction.RENAME) {
            final LocalFile file = fileList.get(0);
            DialogUtils.showEditDialog(mActivity, R.string.tip_rename_file, R.string.hint_rename_file, file.getName(),
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn, @NonNull EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String newName = mContentEditText.getText().toString().trim();
                                if (EmptyUtils.isEmpty(newName)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                } else {
                                    dialog.dismiss();
                                    fileManageTask = new LocalFileManageTask(fileList, mAction, newName, listener);
                                    fileManageTask.execute(0);
                                }
                            }
                        }
                    });
        } else if (action == FileManageAction.COPY) {
            LocalFileTreeView fileTreeView = new LocalFileTreeView(mActivity, R.string.tip_copy_file, R.string.paste);
            fileTreeView.showPopupCenter();
            fileTreeView.setOnPasteListener(new LocalFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageTask = new LocalFileManageTask(fileList, mAction, tarPath, listener);
                    fileManageTask.execute(0);
                }
            });
        } else if (action == FileManageAction.MOVE) {
            LocalFileTreeView fileTreeView = new LocalFileTreeView(mActivity, R.string.tip_move_file, R.string.paste);
            fileTreeView.showPopupCenter();
            fileTreeView.setOnPasteListener(new LocalFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath) {
                    fileManageTask = new LocalFileManageTask(fileList, mAction, tarPath, listener);
                    fileManageTask.execute(0);
                }
            });
        } else if (action == FileManageAction.UPLOAD) {
//            LoginManage loginManage = LoginManage.getInstance();
//            if (!loginManage.isLogin()) {
//                ToastHelper.showToast(R.string.please_login_onespace);
//            final LoginSession loginSession = LoginManage.getInstance().getLoginSession();
//            mActivity.showTipView(R.string.please_login_onespace, false);
//            } else {
//                LoginSession loginSession = loginManage.getLoginSession();
            if (!Utils.isWifiAvailable(mActivity)//非WiFi情况下提醒
                    && SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
                DialogUtils.showWarningDialog(mActivity, -1, R.string.confirm_upload_not_wifi,
                        R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                                if (isPositiveBtn) {
                                    uploadTo(mToId, uploadPath);
                                }
                            }
                        });
            } else {
                uploadTo(mToId, uploadPath);
            }
        } else if (action == FileManageAction.ATTRIBUTES) {
            listener.onComplete(true, action, null);
        } else if (action == FileManageAction.SHARE) {
            ShareFileAPI shareFileAPI = new ShareFileAPI();
            shareFileAPI.share(fileList, mActivity);
        }
    }

    public void manage(@Nullable final FileManageAction action, final String path) {
        this.mAction = action;

        if (EmptyUtils.isEmpty(path) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.MKDIR) {
            DialogUtils.showEditDialog(mActivity, R.string.tip_new_folder, R.string.hint_new_folder, R.string.default_new_folder,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn, @NonNull EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String name = mContentEditText.getText().toString().trim();
                                if (EmptyUtils.isEmpty(name)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                } else {
                                    MakeDirAPI mkDirAPI = new MakeDirAPI();
                                    Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "mkdir:  " + path + File.separator + name);
                                    boolean ret = mkDirAPI.mkdir(path + File.separator + name);
                                    listener.onComplete(ret, action, null);

                                    InputMethodUtils.hideKeyboard(mActivity, mContentEditText);
                                    dialog.dismiss();
                                }
                            }
                        }
                    });
        }

    }

    private void uploadTo(final String toId, final String toPath) {
//        mActivity.showLoading();

//                String names = "";
//                int count = fileList.size() >= 4 ? 4 : fileList.size();
//                for (int i = 0; i < count; i++) {
//                    names += fileList.get(i).getName() + " ";
//                }
//                new UndoBar.Builder(mActivity).setMessage(mActivity.getResources().getString(R.string.tip_start_upload) + names)
//                        .setListener(new UndoBar.StatusBarListener() {
//
//                            @Override
//                            public void onUndo(Parcelable token) {
//                            }
//
//                            @Override
//                            public void onItemClick() {
//                                Intent intent = new Intent(mActivity, TransferActivity.class);
//                                mActivity.startActivity(intent);
//                                //mActivity.controlActivity(VideoPlayActivity.ACTION_SHOW_TRANSFER_UPLOAD);
//                            }
//
//                            @Override
//                            public void onHide() {
//                            }
//                        }).show();
        NasService service = SessionManager.getInstance().getService();
        if (service != null) {
            service.addUploadTasks(toId, toPath, fileList,groupId, callback);
        }
        if (callback != null) {
            callback.onComplete(true);
        }
        if (fileList.size() > 100) {
            ToastHelper.showToast(R.string.tips_do_in_background);
        }
    }

    public void setDownloadPath(final String path) {
        DialogUtils.showConfirmDialog(mActivity, R.string.set_dir_to_download_path, R.string.confirm_set_to_download_path,
                R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            UserSettings settings = SessionManager.getInstance().getUserSettings();
                            settings.setDownloadPath(path);
                            UserSettingsKeeper.update(settings);
                            ToastHelper.showToast(R.string.setting_success);
//                            mActivity.showTipView(R.string.setting_success, true);
                            if (null != callback) {
                                callback.onComplete(true);
                            }
                        }
                    }
                });
    }

    public void addOrRemoveBackup(@NonNull final LocalFile lFile) {
        final boolean isDelete = lFile.isBackupDir();
        final File file = lFile.getFile();
        final NasService service = SessionManager.getInstance().getService();
        final String uid = LoginManage.getInstance().getLoginSession().getId();
        final String path = file.getAbsolutePath();
        if (isDelete) {
            DialogUtils.showConfirmDialog(mActivity, R.string.delete_backup_file, R.string.tips_confirm_delete_backup_dir,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                            BackupFile backupFile = BackupFileKeeper.deleteBackupFile(uid, path);
                            if (null != backupFile) {
                                if (service != null) {
                                    service.deleteBackupFile(backupFile);
                                }
                                ToastHelper.showToast(R.string.setting_success);
//                                mActivity.showTipView(R.string.setting_success, true);
                            } else {
                                ToastHelper.showToast(R.string.setting_failed);
//                                mActivity.showTipView(R.string.setting_failed, false);
                            }
                            if (null != callback) {
                                callback.onComplete(true);
                            }
                        }
                    });
        } else {
            if (file.canRead()) {
                File mDCIMDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                if (file.equals(mDCIMDir)) {
                    confirmBackupRepeat(service, uid, path, true);
                    return;
                }

                ArrayList<File> sdcards = SDCardUtils.getSDCardList();
                if (!EmptyUtils.isEmpty(sdcards)) {
                    for (File dir : sdcards) {
                        File mExternalDCIM = new File(dir, "DCIM");
                        if (file.equals(mExternalDCIM)) {
                            confirmBackupRepeat(service, uid, path, true);
                            return;
                        }
                    }
                }

                List<BackupFile> dbList = BackupFileKeeper.all(LoginManage.getInstance().getLoginSession().getId(), BackupType.FILE);
                if (null != dbList) {
                    for (BackupFile backupFile : dbList) {
                        String p = backupFile.getPath();
                        if (p.equals(path)) {
                            DialogUtils.showNotifyDialog(mActivity, R.string.tips, R.string.error_backup_dir_exist, R.string.ok, null);
                            return;
                        }

                        if (p.startsWith(path) || path.startsWith(p)) {
                            confirmBackupRepeat(service, uid, path, false);
                            return;
                        }
                    }
                }

                DialogUtils.showConfirmDialog(mActivity, R.string.confirm_backup, R.string.tips_confirm_add_backup_dir,
                        R.string.continue_add_backup, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                                if (isPositiveBtn) {
                                    addBackupFile(service, uid, path);
                                }
                            }
                        });
            }
        }
    }

    private void confirmBackupRepeat(@NonNull final NasService service, final String uid, @NonNull final String path, boolean isAlbum) {
        DialogUtils.showConfirmDialog(mActivity, R.string.confirm_backup, isAlbum ? R.string.tips_add_backup_album_repeat : R.string.tips_add_backup_dir_repeat,
                R.string.continue_add_backup, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            addBackupFile(service, uid, path);
                        }
                    }
                });
    }

    private void addBackupFile(@NonNull NasService service, String uid, @NonNull String path) {
        final BackupFile backupFile = new BackupFile(null, uid, path, true, BackupType.FILE, BackupPriority.MID, 0L, 0L);
        long id = BackupFileKeeper.insertBackupFile(backupFile);
        if (id > 0) {
            backupFile.setId(id);
            service.addBackupFile(backupFile);
            ToastHelper.showToast(R.string.setting_success);
//            mActivity.showTipView(R.string.setting_success, true);
        } else {
            ToastHelper.showToast(R.string.setting_failed);
//            mActivity.showTipView(R.string.setting_failed, false);
        }
        if (null != callback) {
            callback.onComplete(true);
        }
    }

    public void setUploadPath(@NonNull String mToId, @Nullable String path) {
       setUploadPath(mToId, path,null);
    }
    public void setUploadPath(@NonNull String mToId, @Nullable String path,@Nullable Long groupId) {
        this.mToId = mToId;
        this.uploadPath = path;
        this.groupId = groupId;
    }

    public void upload(final ArrayList<Uri> mUploadFiles) {
        if (!Utils.isWifiAvailable(mActivity)//非WiFi情况下提醒
                && SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
            DialogUtils.showWarningDialog(mActivity, -1, R.string.confirm_upload_not_wifi,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                            uploadTo(mToId, uploadPath, mUploadFiles, isPositiveBtn);
                            dialog.dismiss();
                        }
                    });
        } else {
            uploadTo(mToId, uploadPath, mUploadFiles, true);
        }
    }

    private void uploadTo(String toId, String toPath, ArrayList<Uri> mUploadFiles, boolean nonWifiAccess) {
        NasService service = SessionManager.getInstance().getService();
        if (service != null) {
            service.addUploadTasks(mUploadFiles, toId, toPath, callback, nonWifiAccess);
        }
        if (callback != null) {
            callback.onComplete(true);
        }
        if (mUploadFiles.size() > 100) {
            ToastHelper.showToast(R.string.tips_do_in_background);
        }
    }

    public interface OnManageCallback {
        void onStart(int resStrId);

        void onComplete(boolean isSuccess);
    }
}
