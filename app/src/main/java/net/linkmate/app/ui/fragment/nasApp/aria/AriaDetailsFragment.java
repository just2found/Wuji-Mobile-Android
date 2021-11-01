package net.linkmate.app.ui.fragment.nasApp.aria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import net.linkmate.app.R;
import net.linkmate.app.ui.activity.nasApp.aria.AriaDetailsActivity;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.oneos.aria.AriaCmd;
import net.sdvn.nascommon.model.oneos.aria.AriaFile;
import net.sdvn.nascommon.model.oneos.aria.AriaStatus;
import net.sdvn.nascommon.model.oneos.aria.AriaUtils;
import net.sdvn.nascommon.model.oneos.aria.BitTorrent;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Aria2 Task Details Fragment
 *
 * @author shz
 * @since V1.6.21
 */
public class AriaDetailsFragment extends Fragment implements AriaDetailsActivity.OnAriaTaskChangedListener {

    private static final String TAG = AriaDetailsFragment.class.getSimpleName();
    private ImageView mIconView;
    private TextView mNameTxt, mSizeTxt, mSpeedTxt, mTimeTxt, mDirTxt, mPeersTxt, mPeerLenTxt,
            mConnectionsTxt;
    private Button mSaveBtn;
    private ViewGroup mCtrlLayout;
    private EditText mUploadEditText, mDownloadEditText;
    private boolean hasRequestLimit = false;
    private AriaStatus ariaStatus;
    private String uploadLimit = "0", downloadLimit = "0";
    @Nullable
    private AppCompatActivity activity;
    @Nullable
    private String deviceId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aria_details, container, false);
        activity = (AppCompatActivity) getActivity();
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        mCtrlLayout = view.findViewById(R.id.layout_ctrl);
        mIconView = view.findViewById(R.id.iv_icon);
        // mStateView = (ImageView) view.findViewById(R.id.iv_state);
        mNameTxt = view.findViewById(R.id.tv_name);
        mSizeTxt = view.findViewById(R.id.tv_size);
        mSpeedTxt = view.findViewById(R.id.tv_speed);
        mTimeTxt = view.findViewById(R.id.tv_time);
        mDirTxt = view.findViewById(R.id.tv_dir);
        mPeersTxt = view.findViewById(R.id.tv_peers);
        mPeerLenTxt = view.findViewById(R.id.tv_peer_len);
        mConnectionsTxt = view.findViewById(R.id.tv_connections);
        mUploadEditText = view.findViewById(R.id.et_upload);
        mDownloadEditText = view.findViewById(R.id.et_download);
        mSaveBtn = view.findViewById(R.id.btn_save_settings);
        mSaveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String upLimit = mUploadEditText.getText().toString().trim();
                String downLimit = mDownloadEditText.getText().toString().trim();
                if (EmptyUtils.isEmpty(upLimit) || EmptyUtils.isEmpty(downLimit)) {
                    showToast(R.string.please_enter_limit);
                } else {
                    setAriaTaskOption(upLimit, downLimit);
                }
            }
        });
    }

    private void updateAriaParamsUI() {
        if (null == ariaStatus) {
            return;
        }

        boolean isBTAria;
        String sizeStr = null, speedStr = null, timeStr = "INF";
        String taskName = "";
        BitTorrent bt = ariaStatus.getBittorrent();
        if (null != bt) {
            isBTAria = true;
            if (null != bt.getInfo()) {
                taskName = bt.getInfo().getName();
            }
        } else {
            isBTAria = false;
            List<AriaFile> files = ariaStatus.getFiles();
            if (null != files && files.size() > 0) {
                taskName = FileUtils.getFileName(files.get(0).getPath());
            }
        }
        try {
            long completeLen, totalLen;
            completeLen = Long.valueOf(ariaStatus.getCompletedLength());
            totalLen = Long.valueOf(ariaStatus.getTotalLength());
            sizeStr = FileUtils.fmtFileSize(completeLen) + "/"
                    + FileUtils.fmtFileSize(totalLen);

            String status = ariaStatus.getStatus();
            if (status.equalsIgnoreCase("complete")) {
                mCtrlLayout.setVisibility(View.GONE);
                speedStr = getString(R.string.completed);
            } else if (status.equalsIgnoreCase("removed")) {
                mCtrlLayout.setVisibility(View.GONE);
                speedStr = getString(R.string.expired);
            } else if (status.equalsIgnoreCase("error")) {
                mCtrlLayout.setVisibility(View.GONE);
                speedStr = getString(R.string.download_failed);
            } else {
                mCtrlLayout.setVisibility(View.VISIBLE);

                if (status.equalsIgnoreCase("paused")) {
                    speedStr = getString(R.string.paused);
                } else {
                    long speed = Long.valueOf(ariaStatus.getDownloadSpeed());
                    speedStr = FileUtils.fmtFileSize(speed) + "/s";
                    if (speed <= 0 || completeLen < totalLen) {
                        timeStr = "INF";
                    } else {
                        timeStr = FileUtils.formatTime((totalLen - completeLen) / speed);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mIconView.setImageResource(isBTAria ? R.drawable.icon_aria_bt : FileUtils.fmtFileIcon(taskName));
        mNameTxt.setText(taskName);
        mSpeedTxt.setText(speedStr);
        mSizeTxt.setText(sizeStr);
        mTimeTxt.setText(timeStr);
        mDirTxt.setText(ariaStatus.getDir());
        mConnectionsTxt.setText(ariaStatus.getConnections());
        mPeersTxt.setText(ariaStatus.getNumPieces());
        mPeerLenTxt.setText(ariaStatus.getPieceLength());

        if (!hasRequestLimit) {
            hasRequestLimit = true;
            getAriaTaskOption(ariaStatus.getGid());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null)
            deviceId = getArguments().getString(AppConstants.SP_FIELD_DEVICE_ID);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAriaChanged(AriaStatus ariaStatus) {
        this.ariaStatus = ariaStatus;
        if (isResumed())
            updateAriaParamsUI();
    }

    private void getAriaTaskOption(String taskGid) {
        AriaCmd optCmd = new AriaCmd();
        optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        optCmd.setAction(AriaCmd.AriaAction.GET_TASK_OPTION);
        optCmd.setContent(taskGid);

        try {
//            if (!LoginManage.getInstance().isLogin()) {
//                showToast(R.string.tip_wait_for_service_connect);
//                return;
//            }
//
//            String baseUrl = LoginManage.getInstance().getLoginSession().getUrl();
            final DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(deviceId);
            if (!(deviceModel!=null && deviceModel.getDevice()!=null &&deviceModel.getDevice().isOnline())){
                showToast(R.string.tip_wait_for_service_connect);
                return;
            }
            final String baseUrl = OneOSAPIs.baseUrl(deviceModel.getDevice().getDomain());
            RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), optCmd.toJsonParam());

            Request request = new Request.Builder().url(baseUrl + optCmd.getEndUrl()).post(body).build();
            OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.LOGE(TAG, "Get Task Option Failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, final Response response) {
                    try {
                        if (response.isSuccessful()) {
                            String body = response.body().string();
//                           Logger.LOGD(TAG, "Get Task Option Result: " + body);
                            JSONObject json = new JSONObject(body).getJSONObject("result");
                            uploadLimit = json.getString("max-upload-limit");
                            downloadLimit = json.getString("max-download-limit");
                            mUploadEditText.setText(uploadLimit);
                            mDownloadEditText.setText(downloadLimit);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        showToast(R.string.file_not_found);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showToast(R.string.app_exception);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast(R.string.error_json_exception);
                    }
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast(R.string.file_not_found);
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(R.string.error_json_exception);
        } catch (IOException e) {
            e.printStackTrace();
            showToast(R.string.app_exception);
        }
    }


    private void setAriaTaskOption(String upLimit, String downLimit) {
        final AriaCmd optCmd = new AriaCmd();
        optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        optCmd.setAction(AriaCmd.AriaAction.SET_TASK_OPTION);
        JSONObject json = new JSONObject();
        try {
            json.put("max-download-limit", downLimit);
            json.put("max-upload-limit", upLimit);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        optCmd.setContent(ariaStatus.getGid());
        optCmd.setAttrJson(json);

        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
            @Override
            public void onSuccess(String url, @NonNull LoginSession loginSession) {
                try {
                    String baseUrl = loginSession.getUrl();

                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), optCmd.toJsonParam());

                    Request request = new Request.Builder().url(baseUrl + optCmd.getEndUrl())
                            .post(body).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            Logger.LOGE(TAG, "Set Task Option Failed: " + e.getMessage());
                            showToast(R.string.tip_request_failed);
//                            if (activity != null)
//                                activity.dismissLoading();
                            if (e instanceof FileNotFoundException) {
                                e.printStackTrace();
                                showToast(R.string.file_not_found);
                            }
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
//                            if (activity != null)
//                                activity.dismissLoading();
                            if (response.isSuccessful()) {
                                try {
                                    String body = response.body().string();
//                           Logger.LOGD(TAG, "Set Task Option Result: " + body);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    showToast(R.string.file_not_found);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    showToast(R.string.app_exception);
                                }
                                showToast(R.string.success_save_aria_setting);
                            }
                        }
                    });
//                    if (activity != null)
//                        activity.showLoading(R.string.loading);

                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast(R.string.error_json_exception);
                } catch (IOException e) {
                    e.printStackTrace();
                    e.printStackTrace();
                    showToast(R.string.app_exception);
                }
            }
        });

    }

    private void showToast(@StringRes final int resId) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastHelper.showToast(resId);
                }
            });
    }
}
