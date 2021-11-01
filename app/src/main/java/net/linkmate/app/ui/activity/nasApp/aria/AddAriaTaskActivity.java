package net.linkmate.app.ui.activity.nasApp.aria;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.oneos.aria.AriaCmd;
import net.sdvn.nascommon.model.oneos.aria.AriaUtils;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.log.Logger;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddAriaTaskActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = AddAriaTaskActivity.class.getSimpleName();

    private RelativeLayout rlTitle;
    private ImageView itbIvLeft;
    private TextView itbTvTitle;

    private EditText mUriTxt;
    private TextView mTipsTxt;
    @Nullable
    private String torrentFilePath = null;
    @Nullable
    private String torrentFileName = null;
    private String deviceId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_aria_add_task);
        initViews();
    }

    private void initViews() {
        rlTitle = findViewById(R.id.top_view);
        itbIvLeft = findViewById(R.id.itb_iv_left);
        itbTvTitle = findViewById(R.id.itb_tv_title);

        itbTvTitle.setText(R.string.add_aria_download);
        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);
        itbIvLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);

        mUriTxt = findViewById(R.id.editext_uri);
        mTipsTxt = findViewById(R.id.txt_offline_tips);

        Button mTorrentBtn = findViewById(R.id.btn_torrent);
        mTorrentBtn.setOnClickListener(this);

        Button mDownloadBtn = findViewById(R.id.btn_download);
        mDownloadBtn.setOnClickListener(this);
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btn_torrent:
                Intent intent = new Intent(this, SelectTorrentActivity.class);
                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_download:
                addOfflineDownload();
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    torrentFilePath = bundle.getString("TorrentPath");
                    torrentFileName = bundle.getString("TorrentName");
                    Logger.LOGD(TAG, "Select Torrent Result: " + torrentFilePath);
                    mUriTxt.getText().clear();
                    mUriTxt.setHint(torrentFileName);
                }
                break;
            default:
                break;
        }
    }

    private void addOfflineDownload() {
        AriaCmd mInfo = new AriaCmd();
        mInfo.setEndUrl(AriaUtils.ARIA_END_URL);
        String offlineUri = mUriTxt.getText().toString();
        if (!TextUtils.isEmpty(offlineUri)) {
            mInfo.setAction(AriaCmd.AriaAction.ADD_URI);
            mInfo.setContent(offlineUri);
        } else if (!TextUtils.isEmpty(torrentFilePath)) {
            mInfo.setAction(AriaCmd.AriaAction.ADD_TORRENT);
            mInfo.setContent(torrentFilePath);
        } else {
            showToast(R.string.aria_download_tips);
            return;
        }

        doAddAriaDownload(mInfo);
    }

    private void notifyDownloadResult(@NonNull final AriaCmd mInfo, final boolean isSuccess) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> contentList = mInfo.getContentList();
                String tips = "";
                for (int i = 0; i < contentList.size(); i++) {
                    tips += contentList.get(i) + " ";
                }

                String fmt;
                if (isSuccess) {
                    fmt = getString(R.string.fmt_add_aria_task_success);
                } else {
                    fmt = getString(R.string.fmt_add_aria_task_failed);
                }
                tips = String.format(fmt, tips);
                mTipsTxt.setText(tips);
                mTipsTxt.setVisibility(View.VISIBLE);
                ToastHelper.showToast(tips);
                dismissLoading();
            }
        });

    }

    private void doAddAriaDownload(@NonNull final AriaCmd cmd) {

        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
            @Override
            public void onSuccess(String url, @NonNull LoginSession loginSession) {
                try {
                    String baseUrl = loginSession.getUrl();
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), cmd.toJsonParam());
                    final Request request = new Request.Builder().post(body).url(baseUrl + cmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            dismissLoading();
                            notifyDownloadResult(cmd, false);
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {

                            dismissLoading();
                            if (response.isSuccessful()) {
                                notifyDownloadResult(cmd, true);
                            } else {
                                notifyDownloadResult(cmd, false);
                            }

                        }
                    });

                    showLoading(R.string.loading);

                } catch (FileNotFoundException e) {
                    dismissLoading();
                    e.printStackTrace();
                    showToast(R.string.file_not_found);
                } catch (JSONException e) {
                    dismissLoading();
                    e.printStackTrace();
                    showToast(R.string.error_json_exception);
                } catch (IOException e) {
                    dismissLoading();
                    e.printStackTrace();
                    showToast(R.string.app_exception);
                }
            }
        });

    }

    private void showToast(@StringRes final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.showToast(resId);
            }
        });
    }
}
