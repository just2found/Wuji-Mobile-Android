package net.linkmate.app.ui.activity.mine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.TipsBar;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.DeviceSettingsKeeper;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.db.objecbox.DeviceSettings;
import net.sdvn.nascommon.iface.Callback;
import net.sdvn.nascommon.iface.LoadingCallback;
import net.sdvn.nascommon.iface.OnResultListener;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
import net.sdvn.nascommon.service.NasService;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.PermissionChecker;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.widget.AnimCircleProgressBar;
import net.sdvn.nascommon.widget.DevicesPopupView;
import net.sdvn.nascommon.widget.kyleduo.SwitchButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BackupPhotoActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = BackupPhotoActivity.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;
    private static final int MSG_REFRESH_PROGRESS = 2;

    private RelativeLayout rlTitle;
    private ImageView itbIvLeft;
    private TextView itbTvTitle;
    private ImageView itbIvRight;
    private SwitchButton mSBSS, mSBWifi;
    private LinearLayout mProgressLayout, mCompleteLayout;
    private TextView mProgressTxt, mServerDirTxt, mCompleteTipTxt, mFileNameTxt;
    private AnimCircleProgressBar mProgressBar;

    //    private LoginSession mLoginSession;
    private boolean isFragmentVisible = true;

    @NonNull
    private OnTransferFileListener<UploadElement> listener = new OnTransferFileListener<UploadElement>() {

        @Override
        public void onStart(String url, UploadElement element) {
            updateBackupFileInfo(element, true);
        }

        @Override
        public void onTransmission(String url, UploadElement element) {
            updateBackupFileInfo(element, false);
        }

        @Override
        public void onComplete(String url, UploadElement element) {
        }
    };
    private List<DeviceModel> deviceList;
    private DevicesPopupView mShareMenu;
    private TextView devName;
    private View mViewChoiceAlbum;
    private View layout_location;

    private void updateBackupFileInfo(@Nullable final UploadElement element, final boolean start) {
        if (null != element) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final long size = element.getSize();
                    int progress;
                    if (size == 0) {
                        progress = 0;
                    } else
                        progress = (int) (element.getLength() * 100 / size);
                    String s = element.getSrcName();
                    if (!start) {
                        s += " (" + progress + "%)";
                    }
                    mFileNameTxt.setText(s);
                }
            });
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstants.REQUEST_CODE_SELECT_BACKUP_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    //do future
                }
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_backup_photo);
        initViews();
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    /**
     * init view by id
     */
    private void initViews() {
        rlTitle = findViewById(R.id.layout_title);
        itbIvLeft = findViewById(R.id.itb_iv_left);
        itbTvTitle = findViewById(R.id.itb_tv_title);
        itbIvRight = findViewById(R.id.itb_iv_right);
        mViewChoiceAlbum = findViewById(R.id.tv_select_backup_album_folder);
        mViewChoiceAlbum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick(v)) return;
                final Intent intent = new Intent(BackupPhotoActivity.this, ChoiceAlbumActivity.class);
                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_SELECT_BACKUP_ALBUM);
            }
        });

        itbTvTitle.setText(R.string.title_backup_photo);
        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);
        itbIvRight.setImageResource(R.drawable.ic_refresh_white_24dp);
        itbIvRight.setOnClickListener(this);
        itbIvLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick(v)) return;
                onBackPressed();
            }
        });

        mProgressLayout = findViewById(R.id.layout_progress);
        mCompleteLayout = findViewById(R.id.layout_complete);

        mProgressBar = findViewById(R.id.progressbar);
        mProgressTxt = findViewById(R.id.txt_progress);
        mCompleteTipTxt = findViewById(R.id.txt_complete_tips);
        mServerDirTxt = findViewById(R.id.txt_server_dir);
        mFileNameTxt = findViewById(R.id.txt_file_name);
        devName = findViewById(R.id.location_name);
        mSBWifi = findViewById(R.id.btn_wifi_backup);
        final BackupPhotoActivity mActivity = BackupPhotoActivity.this;
        layout_location = findViewById(R.id.layout_location);
        layout_location.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                if (Utils.isFastClick(v)) return;
                if (deviceList != null && deviceList.size() > 0) {
                    mShareMenu = new DevicesPopupView(mActivity, new LoadingCallback() {
                    }, v);
                    mShareMenu.setNeedPath(false);
                    mShareMenu.addList(deviceList);
                    mShareMenu.setTitleText(R.string.tv_select_device);
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
                                final String devId = deviceModel.getDevId();
                                if (!Objects.equals(devId, deviceId)) {
                                    deviceId = devId;
//                                    mBackupService.stopBackupAlbum();
//                                    mBackupService.startBackupAlbum(deviceId);
//                                    SPHelper.put(AppConstants.SP_FIELD_AUTO_BAK_ALBUM_CARE, false);
                                    changStatus();
                                }
                                mShareMenu.dismiss();
                            }
                        }
                    });
                } else {
                    ToastUtils.showToast(v.getContext().getResources().getString(R.string.nullnull)
                            + v.getContext().getResources().getString(R.string.app_name));
                }
            }
        });
        deviceList = SessionManager.getInstance().getOnlineDeviceModels();
        SessionManager.getInstance().liveDataDeviceModels.observe(this, deviceModels ->
        {
            deviceList = SessionManager.getInstance().getOnlineDeviceModels();
            if (mShareMenu != null) {
                mShareMenu.addList(deviceList);
            }
        });
        mSBSS = findViewById(R.id.btn_auto_backup);
        changStatus();
        mSBWifi.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    SPHelper.put(AppConstants.SP_FIELD_AUTO_BAK_ALBUM_CARE, isChecked);
                DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
                if (settings != null) {
                    settings.setIsBackupAlbumOnlyWifi(isChecked);
                    DeviceSettingsKeeper.update(settings);
                }
            }
        });
        mSBSS.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
//                    SPHelper.put(AppConstants.SP_FIELD_AUTO_BAK_ALBUM_CARE, isChecked);

                final DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
                if (settings != null) {
                    settings.setIsAutoBackupAlbum(isChecked);
                    DeviceSettingsKeeper.update(settings);
                }
                NasService mBackupService = SessionManager.getInstance().getService();
                if (isChecked) {
                    final String permission;
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                    PermissionChecker.checkPermission(mActivity, new Callback<List<String>>() {
                        @Override
                        public void result(List<String> strings) {
                            SPHelper.put(AppConstants.SP_FIELD_BAK_ALBUM_LAST_DEV_ID, deviceId);
                            if (mBackupService != null) {
                                mBackupService.startBackupAlbum(deviceId);
                                mBackupService.setOnBackupAlbumListener(listener);
                            }
                        }
                    }, new Callback<List<String>>() {
                        @Override
                        public void result(List<String> strings) {
                            showSettings();
                        }
                    }, permission);
                } else {
                    if (mBackupService != null) {
                        mBackupService.stopBackupAlbum();
                    }
                }

            }
        });


    }

    private void showSettings() {
        UiUtils.showStorageSettings(this);
    }

    private void changStatus() {
        if (!TextUtils.isEmpty(deviceId)) {
            final DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
            mSBSS.setEnabled(true);
//            isAutoBackup = SPHelper.get(AppConstants.SP_FIELD_AUTO_BAK_ALBUM_CARE, false);
            boolean isAutoBackup = settings != null ? settings.getIsAutoBackupAlbum() : false;
            mSBSS.setChecked(isAutoBackup);
//            isWifiBackup = SPHelper.get(AppConstants.SP_FIELD_BAK_ALBUM_ONLY_WIFI_CARE, true);
            boolean isWifiBackup = settings != null ? settings.getIsBackupAlbumOnlyWifi() : true;
            mSBWifi.setChecked(isWifiBackup);
            DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(deviceId);
            if (deviceModel != null)
                devName.setText(deviceModel.getDevName());
            String dir = getResources().getString(R.string.backup_dir_shown) + AppConstants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM;
            mServerDirTxt.setText(dir);
            mSBSS.setEnabled(true);
            mViewChoiceAlbum.setEnabled(true);
            mSBWifi.setEnabled(true);
        } else {
            mServerDirTxt.setText(R.string.tip_select_device);
            mSBSS.setEnabled(false);
            mViewChoiceAlbum.setEnabled(false);
            mSBWifi.setEnabled(false);
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        if (Utils.isFastClick(v)) return;
        switch (v.getId()) {
            case R.id.itb_iv_right:
                resetBackPhotoDialog();
                break;
            default:
                break;
        }
    }

    private void resetBackPhotoDialog() {
        DialogUtils.showConfirmDialog(this, R.string.title_reset_backup, R.string.tips_reset_backup, R.string.reset_now,
                R.string.cancel, new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            NasService mBackupService = SessionManager.getInstance().getService();
                            if (mBackupService != null) {
                                mBackupService.resetBackupAlbum(deviceId);
                                mBackupService.setOnBackupAlbumListener(listener);
                                ToastUtils.showToast(R.string.success_reset_backup);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        NasService mBackupService = SessionManager.getInstance().getService();
        if (mBackupService != null)
            mBackupService.setOnBackupAlbumListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentVisible = true;
        Message message = new Message();
        message.what = MSG_REFRESH_UI;
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentVisible = false;

    }

    @Override
    protected void onStop() {
        super.onStop();
        NasService mBackupService = SessionManager.getInstance().getService();
        if (mBackupService != null)
            mBackupService.removeOnBackupAlbumListener(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }


    private void refreshBackupView(int count) {
        mProgressTxt.setText(String.valueOf(count));
        if (count > 0) {
            // mBgView.setVisibility(View.GONE);
            mCompleteLayout.setVisibility(View.GONE);
            mProgressLayout.setVisibility(View.VISIBLE);
            mFileNameTxt.setVisibility(View.VISIBLE);
            itbIvRight.setVisibility(View.GONE);
            mServerDirTxt.setEnabled(false);
            mViewChoiceAlbum.setEnabled(false);
            layout_location.setEnabled(false);
        } else {
            mServerDirTxt.setEnabled(true);
            mViewChoiceAlbum.setEnabled(true);
            layout_location.setEnabled(true);
            mProgressLayout.setVisibility(View.GONE);
            mFileNameTxt.setVisibility(View.INVISIBLE);
            if (mSBSS.isChecked()) {
                mCompleteTipTxt.setText(R.string.backup_complete);
                itbIvRight.setVisibility(View.VISIBLE);
            } else {
                mCompleteTipTxt.setText(R.string.backup_closed);
                itbIvRight.setVisibility(View.GONE);
            }
            mCompleteLayout.setVisibility(View.VISIBLE);
            // mBgView.setVisibility(View.VISIBLE);
        }
    }


    private static final int REFRESH_FREQUENCY = 40; // 刷新频率，单位ms
    private static final int TIMES_PRE_SECONDS = 1000 / REFRESH_FREQUENCY; // 每秒刷新次数
    private static final int PROGRESS_PRE_TIMES = 100 / TIMES_PRE_SECONDS; // 刷新进度变化值基数
    private boolean isProgressUp = true;
    private boolean isBackup = false;

    @Nullable
    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        int times = 0;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
                    NasService mBackupService = SessionManager.getInstance().getService();
                    if (mBackupService != null) {
                        int count = mBackupService.getBackupAlbumCount();
                        isBackup = count > 0;
                        refreshBackupView(count);
                        if (times == 0)
                            refreshProgress();
                    }
                    break;
                case MSG_REFRESH_PROGRESS:
                    refreshProgress();
                    if (isBackup) {
                        int p = msg.arg1;
                        mProgressBar.setMainProgress(p * PROGRESS_PRE_TIMES);
                    } else {
                        mProgressBar.setMainProgress(0);
                    }
                    break;
            }
            super.handleMessage(msg);
        }

        private void refreshProgress() {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFragmentVisible) {
                        if (times == 0) {
                            Message message = new Message();
                            message.what = MSG_REFRESH_UI;
                            sendMessage(message);
                        }
                        Message message = new Message();
                        message.what = MSG_REFRESH_PROGRESS;
                        message.arg1 = times;
                        sendMessage(message);
                        if (times == 0) {
                            isProgressUp = true;
                        } else if (times == TIMES_PRE_SECONDS) {
                            isProgressUp = false;
                        }

                        if (isProgressUp) {
                            times++;
                        } else {
                            times--;
                        }
                    }
                }
            }, REFRESH_FREQUENCY);
        }

    };
}
