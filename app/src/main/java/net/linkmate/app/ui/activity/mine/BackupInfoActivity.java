package net.linkmate.app.ui.activity.mine;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanzhenjie.permission.AndPermission;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.util.ToastUtil;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.BackupInfoKeeper;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.db.objecbox.BackupInfo;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.iface.LoadingCallback;
import net.sdvn.nascommon.iface.OnResultListener;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.FileManageAction;
import net.sdvn.nascommon.model.oneos.BaseResultModel;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.api.file.OneOSFileManageAPI;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoException;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoManager;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoStep;
import net.sdvn.nascommon.model.oneos.backup.info.BackupInfoType;
import net.sdvn.nascommon.model.oneos.backup.info.OnBackupInfoListener;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.PermissionChecker;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommon.widget.AnimCircleProgressBar;
import net.sdvn.nascommon.widget.DevicesPopupView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.weline.repo.data.model.BaseProtocol;
import io.weline.repo.net.V5Observer;
import io.weline.repo.repository.V5Repository;

/**
 * Backup Contacts or SMS Activity
 */
public class BackupInfoActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = BackupInfoActivity.class.getSimpleName();
    private static final boolean IS_LOG = Logger.Logd.BACKUP_SMS;
    public static final String EXTRA_BACKUP_INFO_TYPE = "is_backup_contacts";

    private RelativeLayout rlTitle;
    private ImageView itbIvLeft;
    private TextView itbTvTitle;
    private Button mBackupBtn, mRecoverBtn;
    private TextView mBackupTimeTxt;// , syncContactsState, recoverContactsState;
    private TextView mStateTxt, mProgressTxt;
    private AnimCircleProgressBar mAnimCircleProgressBar;
    protected String deviceId;

    @NonNull
    private BackupInfoType mBackupType = BackupInfoType.BACKUP_CONTACTS;
    @NonNull
    private BackupInfoType mRecoveryType = BackupInfoType.RECOVERY_CONTACTS;
    @NonNull
    private BackupInfoManager mBackupInfoManager;
    @NonNull
    private OnBackupInfoListener mListener = new OnBackupInfoListener() {
        @Override
        public void onStart(final BackupInfoType type) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBackupBtn.setEnabled(false);
                        mRecoverBtn.setEnabled(false);
                    }
                });
            }
        }

        @Override
        public void onBackup(final BackupInfoType type, final BackupInfoStep step, final int progress) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecoverBtn.setEnabled(false);
                        mBackupBtn.setEnabled(false);

                        if (step == BackupInfoStep.EXPORT) {
                            mAnimCircleProgressBar.setMainProgress(progress);
                            mProgressTxt.setText(String.valueOf(progress));
                            mStateTxt.setText(R.string.exporting);
                        } else if (step == BackupInfoStep.UPLOAD) {
                            mStateTxt.setText(R.string.syncing);
                        } else if (step == BackupInfoStep.DOWNLOAD) {
                            mAnimCircleProgressBar.setMainProgress(progress);
                            mProgressTxt.setText(String.valueOf(progress));
                            mStateTxt.setText(R.string.recover_prepare);
                        } else if (step == BackupInfoStep.IMPORT) {
                            mAnimCircleProgressBar.setMainProgress(progress);
                            mProgressTxt.setText(String.valueOf(progress));
                            mStateTxt.setText(R.string.recovering);
                        }
                    }
                });
            }
        }

        @Override
        public void onComplete(final BackupInfoType type, @Nullable final BackupInfoException exception) {
            if (type == mBackupType || type == mRecoveryType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecoverBtn.setEnabled(true);
                        mBackupBtn.setEnabled(true);

                        boolean success = (exception == null);
                        if (success) {
                            mAnimCircleProgressBar.setMainProgress(100);
                            mProgressTxt.setText(String.valueOf(100));
                        }

                        if (type == BackupInfoType.BACKUP_CONTACTS || type == BackupInfoType.BACKUP_SMS) {
                            if (success) {
                                mStateTxt.setText(R.string.sync_success);
                                ToastHelper.showLongToastSafe(R.string.sync_success);
                                setCompleteTime();
                            } else {
                                notifyFailedInfo(type, exception);
                            }
                        } else if (type == BackupInfoType.RECOVERY_CONTACTS || type == BackupInfoType.RECOVERY_SMS) {
                            if (success) {
                                mStateTxt.setText(R.string.recover_success);
                                ToastHelper.showLongToastSafe(R.string.recover_success);
                            } else {
                                notifyFailedInfo(type, exception);
                            }
                        }
                        dismissLoading();
                    }
                });
            }
        }
    };
    private TextView devName;
    private List<DeviceModel> deviceList;
    private DevicesPopupView mShareMenu;
    @Nullable
    private Observer<List<DeviceModel>> observer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_backup_info);

        Intent intent = getIntent();
        boolean isBackupContacts = intent.getBooleanExtra(EXTRA_BACKUP_INFO_TYPE, true);
        mBackupType = isBackupContacts ? BackupInfoType.BACKUP_CONTACTS : BackupInfoType.BACKUP_SMS;
        mRecoveryType = isBackupContacts ? BackupInfoType.RECOVERY_CONTACTS : BackupInfoType.RECOVERY_SMS;

        mBackupInfoManager = BackupInfoManager.getInstance();
//        mBackupInfoManager.setOnBackupInfoListener(deviceId,mListener);
        deviceId = SPHelper.get(mBackupType == BackupInfoType.BACKUP_CONTACTS
                ? AppConstants.SP_FIELD_BAK_INFO_CONTACT_LAST_DEV_ID
                : AppConstants.SP_FIELD_BAK_INFO_SMS_LAST_DEV_ID, deviceId);
        initViews();
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    @Override
    public void onResume() {
        super.onResume();
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener(false) {


            @Override
            public void onSuccess(String url, @NonNull final LoginSession loginSession) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mBackupInfoManager != null) {
                            mBackupInfoManager.setOnBackupInfoListener(deviceId, mListener);
                        }
                        BackupInfo mBackupHistory = BackupInfoKeeper.getBackupHistory(loginSession.getId(), mBackupType);
                        long time = 0;
                        if (mBackupHistory != null) {
                            time = mBackupHistory.getTime();
                        }
                        if (time <= 0) {
                            mBackupTimeTxt.setHint(R.string.not_sync);
                        } else {
                            mBackupTimeTxt.setText(FileUtils.formatTime(time, "yyyy/MM/dd HH:mm"));
                        }
                        queryNasHasBackupContacts(loginSession);
                        updateSyncButton(true);
                    }
                });

            }

        });
        if (!TextUtils.isEmpty(deviceId)) {
            DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(deviceId);
            if (deviceModel != null)
                devName.setText(deviceModel.getDevName());
        }
    }

    private void queryNasHasBackupContacts(LoginSession loginSession) {
        OneOSFileManageAPI.OnFileManageListener listener = new OneOSFileManageAPI.OnFileManageListener() {
            @Override
            public void onStart(String url, FileManageAction action) {

            }

            @Override
            public void onSuccess(String url, FileManageAction action, String response) {
                Type type = new TypeToken<BaseResultModel<OneOSFile>>() {
                }.getType();
                BaseResultModel<OneOSFile> result = null;
                try {
                    result = GsonUtils.decodeJSON(response, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (result != null && result.isSuccess()) {
                    OneOSFile file = result.data;
                    if (mBackupTimeTxt != null)
                        mBackupTimeTxt.setText(FileUtils.fmtTimeByZone(file.getTime()));
                }


            }

            @Override
            public void onFailure(String url, FileManageAction action, int errorNo, String errorMsg) {

            }
        };

        JSONArray paths = new JSONArray();
        //备份是在个人目录
        paths.put(AppConstants.BACKUP_INFO_ONEOS_ROOT_DIR + AppConstants.BACKUP_CONTACTS_FILE_NAME);
        V5Observer observer = new V5Observer<Object>(loginSession.getId()) {

            @Override
            public void isNotV5() {
                OneOSFileManageAPI oneOSFileManageAPI = new OneOSFileManageAPI(loginSession);
                oneOSFileManageAPI.setOnFileManageListener(listener);
                OneOSFile oneOSFile = new OneOSFile();
                oneOSFile.setPath(AppConstants.BACKUP_INFO_ONEOS_ROOT_DIR + AppConstants.BACKUP_CONTACTS_FILE_NAME);
                oneOSFileManageAPI.attr(oneOSFile.getPath());
            }

            @Override
            public void fail(@NotNull BaseProtocol<Object> result) {
                listener.onFailure("", FileManageAction.ATTRIBUTES, result.getError().getCode(), result.getError().getMsg());
            }

            @Override
            public void success(@NotNull BaseProtocol<Object> result) {
                listener.onSuccess("", FileManageAction.ATTRIBUTES, new Gson().toJson(result));
            }

            @Override
            public boolean retry() {
                V5Repository.Companion.INSTANCE().optFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), "attributes", paths, 0, this);
                return true;
            }
        };

        V5Repository.Companion.INSTANCE().optFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), "attributes", paths, 0, observer);

    }

    /**
     * Find views by id
     */
    private void initViews() {
        rlTitle = findViewById(R.id.layout_title);
        itbIvLeft = findViewById(R.id.itb_iv_left);
        itbTvTitle = findViewById(R.id.itb_tv_title);

        mStateTxt = findViewById(R.id.txt_state);
        mProgressTxt = findViewById(R.id.txt_progress);
        mAnimCircleProgressBar = findViewById(R.id.progressbar);

        mBackupBtn = findViewById(R.id.btn_sync_contacts);
        mBackupBtn.setOnClickListener(this);
        mBackupTimeTxt = findViewById(R.id.sync_time);

        mRecoverBtn = findViewById(R.id.btn_recover_contacts);
        mRecoverBtn.setOnClickListener(this);

        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);
        itbIvLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (mBackupType == BackupInfoType.BACKUP_SMS) {
            itbTvTitle.setText(R.string.title_sync_sms);
            mBackupBtn.setText(R.string.sync_sms_to_server);
            mRecoverBtn.setText(R.string.recover_sms_to_phone);
        } else {
            itbTvTitle.setText(R.string.title_sync_contacts);
            mBackupBtn.setText(R.string.sync_contacts_to_server);
            mRecoverBtn.setText(R.string.recover_contacts_to_phone);
        }
        devName = findViewById(R.id.location_name);
        findViewById(R.id.layout_location).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                if (Utils.isFastClick(v)) return;
                if (deviceList != null && deviceList.size() > 0) {
                    mShareMenu = new DevicesPopupView(BackupInfoActivity.this, new LoadingCallback() {
                    }, v);
                    mShareMenu.setNeedPath(false);
                    mShareMenu.addList(deviceList);
                    mShareMenu.setTitleText(R.string.backup);
                    mShareMenu.setConfirmOnClickListener(new OnResultListener<String>() {
                        @Override
                        public void onResult(String result) {
                            HashMap<Integer, Boolean> isSelected = mShareMenu.getIsSelected();
                            int position = -1;
                            for (Map.Entry<Integer, Boolean> entry : isSelected.entrySet()) {
                                if (entry.getValue()) {
                                    position = entry.getKey();
                                    break;
                                }
                            }
                            if (position >= 0 && position < deviceList.size()) {
                                DeviceModel deviceModel = deviceList.get(position);
                                devName.setText(deviceModel.getDevName());
                                deviceId = deviceModel.getDevId();
                                changStatus();
                                mShareMenu.dismiss();
                            }
                        }
                    });
                } else {
                    ToastHelper.showToast(v.getContext().getResources().getString(R.string.nullnull)
                            + v.getContext().getResources().getString(R.string.app_name));
                }
            }
        });
        deviceList = SessionManager.getInstance().getOnlineDeviceModels();
        observer = new Observer<List<DeviceModel>>() {
            @Override
            public void onChanged(List<DeviceModel> deviceModels) {
                deviceList = SessionManager.getInstance().getOnlineDeviceModels();
                if (mShareMenu != null)
                    mShareMenu.addList(deviceList);
            }
        };
        SessionManager.getInstance().liveDataDeviceModels.observe(this, observer);
    }

    private void changStatus() {
        if (!TextUtils.isEmpty(deviceId)) {
            SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
                @Override
                public void onStart(String url) {
                    showLoading();
                }

                @Override
                public void onSuccess(String url, @NonNull final LoginSession session) {
                    dismissLoading();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SPHelper.put(mBackupType == BackupInfoType.BACKUP_CONTACTS
                                    ? AppConstants.SP_FIELD_BAK_INFO_CONTACT_LAST_DEV_ID
                                    : AppConstants.SP_FIELD_BAK_INFO_SMS_LAST_DEV_ID, deviceId);
                            DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(deviceId);
                            if (deviceModel != null)
                                devName.setText(deviceModel.getDevName());
                            mBackupInfoManager.setOnBackupInfoListener(deviceId, mListener);
                            BackupInfo mBackupHistory = BackupInfoKeeper.getBackupHistory(session.getId(), mBackupType);
                            long time = 0;
                            if (mBackupHistory != null) {
                                time = mBackupHistory.getTime();
                            }
                            if (time <= 0) {
                                mBackupTimeTxt.setHint(R.string.not_sync);
                            } else {
                                mBackupTimeTxt.setText(FileUtils.formatTime(time, "yyyy/MM/dd HH:mm"));
                            }
                            queryNasHasBackupContacts(session);
                            updateSyncButton(true);
                        }
                    });

                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    super.onFailure(url, errorNo, errorMsg);
                    dismissLoading();
                    updateSyncButton(false);
                }
            });
            DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(deviceId);
            if (deviceModel != null)
                devName.setText(deviceModel.getDevName());

//            isAutoBackup = LoginManage.getInstance().getLoginSession().getUserSettings().getIsAutoBackupAlbum();
        } else {
            updateSyncButton(false);
        }
    }

    private void updateSyncButton(boolean isEnable) {
        mBackupBtn.setEnabled(isEnable);
        mRecoverBtn.setEnabled(isEnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (Utils.isFastClick(v)) return;
        switch (v.getId()) {
            case R.id.btn_sync_contacts:
                checkBackupPermissions(true);
                break;
            case R.id.btn_recover_contacts:
                checkBackupPermissions(false);
                break;
            default:
                break;
        }
    }

    private void checkBackupPermissions(final boolean isBackup) {
        final String permission;
        final boolean isContacts = mBackupType == BackupInfoType.BACKUP_CONTACTS;
        if (isBackup) {
            if (isContacts) {
                permission = Manifest.permission.READ_CONTACTS;
            } else {
                permission = Manifest.permission.READ_SMS;
            }
        } else {
            if (isContacts) {
                permission = Manifest.permission.WRITE_CONTACTS;
            } else {
                permission = Manifest.permission.READ_SMS;
            }
        }


        final BackupInfoActivity activity = this;
        PermissionChecker.checkPermission(this, strings -> {
            if (AndPermission.hasPermissions(activity, permission)) {
                if (isBackup) {
                    startBackup();
                } else {
                    startRecover();
                }
            } else {
                PermissionChecker.showSettingDialog(activity, Collections.singletonList(permission), strings1 -> {
                });
            }
        }, strings -> {
            int tip;
            if (isContacts) {
                tip = R.string.permission_denied_backup_contact;
            } else {
                tip = R.string.permission_denied_backup_sms;
            }
            ToastUtil.showToast(activity, activity.getString(tip));
        }, permission);
    }


    private void startBackup() {
        showLoading(R.string.backing_up);
        mBackupBtn.setEnabled(false);
        mRecoverBtn.setEnabled(false);
        if (mBackupType == BackupInfoType.BACKUP_CONTACTS) {
            mBackupInfoManager.startBackupContacts(deviceId, mListener);
            SPHelper.put(AppConstants.SP_FIELD_BAK_INFO_CONTACT_LAST_DEV_ID, deviceId);
        } else {
            mBackupInfoManager.startBackupSMS(deviceId, mListener);
            SPHelper.put(AppConstants.SP_FIELD_BAK_INFO_SMS_LAST_DEV_ID, deviceId);
        }
    }

    private void startRecover() {
        showLoading(R.string.recovering);
        mBackupBtn.setEnabled(false);
        mRecoverBtn.setEnabled(false);
        if (mBackupType == BackupInfoType.BACKUP_CONTACTS) {
            mBackupInfoManager.startRecoverContacts(deviceId, mListener);
            SPHelper.put(AppConstants.SP_FIELD_BAK_INFO_CONTACT_LAST_DEV_ID, deviceId);
        } else {
            mBackupInfoManager.startRecoverSMS(deviceId, mListener);
            SPHelper.put(AppConstants.SP_FIELD_BAK_INFO_SMS_LAST_DEV_ID, deviceId);
        }
    }

    /**
     * set complete time
     */
    private void setCompleteTime() {
        mBackupTimeTxt.setText(FileUtils.getCurFormatTime("yyyy/MM/dd HH:mm"));
    }

    private void notifyFailedInfo(@Nullable BackupInfoType type, @Nullable BackupInfoException ex) {
        if (null != ex && type != null) {
            int title;
            int content;
            if (type == BackupInfoType.BACKUP_CONTACTS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.ERROR_EXPORT) {
                    content = R.string.error_export_contacts;
                } else if (ex == BackupInfoException.NO_BACKUP) {
                    content = R.string.no_contact_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else if (type == BackupInfoType.RECOVERY_CONTACTS) {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.NO_RECOVERY) {
                    content = R.string.no_contact_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_contacts;
                }
            } else if (type == BackupInfoType.BACKUP_SMS) {
                title = R.string.sync_failed;
                if (ex == BackupInfoException.ERROR_EXPORT) {
                    content = R.string.error_export_sms;
                } else if (ex == BackupInfoException.NO_BACKUP) {
                    content = R.string.no_sms_to_sync;
                } else {
                    content = R.string.sync_exception_download;
                }
            } else {
                title = R.string.recover_failed;
                if (ex == BackupInfoException.NO_RECOVERY) {
                    content = R.string.no_sms_to_recover;
                } else if (ex == BackupInfoException.DOWNLOAD_ERROR) {
                    content = R.string.recovery_exception_upload;
                } else {
                    content = R.string.error_import_sms;
                }
            }

            mStateTxt.setText(content);
            DialogUtils.showNotifyDialog(this, title, content, R.string.ok, null);
        }
    }
}
