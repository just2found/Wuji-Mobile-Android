package net.linkmate.app.ui.activity.nasApp.aria;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.reflect.TypeToken;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.ui.fragment.nasApp.aria.AriaDetailsFragment;
import net.linkmate.app.ui.fragment.nasApp.aria.AriaFilesFragment;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.oneos.aria.AriaCmd;
import net.sdvn.nascommon.model.oneos.aria.AriaStatus;
import net.sdvn.nascommon.model.oneos.aria.AriaUtils;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.widget.TitleBackLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AriaDetailsActivity extends BaseActivity {
    public static final String TAG_DETAILS = "details";
    public static final String TAG_FILES = "files";
    private static final String TAG = AriaDetailsActivity.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;

    @Nullable
    private String baseUrl = null;
    private RadioGroup mRadioGroup;
    @Nullable
    private Fragment mFilesFragment, mDetailsFragment, mCurFagment;
    @Nullable
    private String taskGid = null;
    @Nullable
    private AriaStatus mAriaStatus;
    private boolean isVisible = true;
    private UIThread uiThread;
    private String deviceId;
    private TitleBackLayout mTitleLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_aria_details);

        Intent intent = getIntent();
        if (null != intent) {
            taskGid = intent.getStringExtra("TaskGid");
            deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
        }

        if (null == taskGid) {
            showToast(R.string.app_exception);
            this.finish();
            return;
        }

//        baseUrl = LoginManage.getInstance().getLoginSession().getUrl();

        initView();

        getAriaTaskDetails();
    }

    @Override
    protected View getTopView() {
        return mTitleLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        startUpdateUIThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
//        mThread.interrupt();
//        mThread = null;
        if (handler != null) {
            handler.removeCallbacks(uiThread);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isVisible = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void initView() {
        mDetailsFragment = getSupportFragmentManager().findFragmentByTag(TAG_DETAILS);
        if (mDetailsFragment == null)
            mDetailsFragment = new AriaDetailsFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
        mDetailsFragment.setArguments(args);
        mFilesFragment = getSupportFragmentManager().findFragmentByTag(TAG_FILES);
        if (mFilesFragment == null)
            mFilesFragment = new AriaFilesFragment();

        mTitleLayout = findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
//        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setBackTitle(R.string.title_aria_task_details);
//        mTitleLayout = findViewById(R.id.top_view);
        mRadioGroup = findViewById(R.id.rg_aria_details);
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onChangeFragment(checkedId == R.id.rb_details);
            }
        });

        onChangeFragment(true);
    }

    public void onChangeFragment(boolean isDetails) {
        Fragment mFragment;
        if (isDetails) {
            mFragment = mDetailsFragment;
        } else {
            mFragment = mFilesFragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurFagment != null) {
            mCurFagment.onPause();
            transaction.hide(mCurFagment);
        }

        if (!mFragment.isAdded()) {
            transaction.add(R.id.transfer_frame_layout, mFragment, isDetails ? TAG_DETAILS : TAG_FILES);
        } else {
            mFragment.onResume();
        }

        transaction.show(mFragment);
        mCurFagment = mFragment;
        notifyCurFragment();

        transaction.commit();
    }

    private void startUpdateUIThread() {
//        mThread = new Thread(new UIThread());
//        mThread.start();
        if (uiThread == null)
            uiThread = new UIThread();
        if (!isRunning) {
            handler.removeCallbacks(uiThread);
            handler.post(uiThread);
        }
    }

    private void notifyCurFragment() {
        if (null != mAriaStatus && mCurFagment instanceof OnAriaTaskChangedListener) {
            ((OnAriaTaskChangedListener) mCurFagment).onAriaChanged(mAriaStatus);
        }
    }

    private boolean isRunning = false;

    public class UIThread implements Runnable {
        @Override
        public void run() {
            if (isVisible) {
                try {
                    Message message = new Message();
                    message.what = MSG_REFRESH_UI;
                    handler.sendMessage(message);
                    handler.postDelayed(this, 5000);
                    isRunning = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isRunning = false;
            }

        }
    }

    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
                    getAriaTaskDetails();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void getAriaTaskDetails() {
        final AriaCmd detailsCmd = new AriaCmd();
        detailsCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        detailsCmd.setAction(AriaCmd.AriaAction.GET_TASK_STATUS);
        detailsCmd.setContent(taskGid);

        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
            @Override
            public void onSuccess(String url, @NonNull LoginSession loginSession) {
                try {
                    String baseUrl = loginSession.getUrl();
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), detailsCmd.toJsonParam());
                    Request request = new Request.Builder().post(body).url(baseUrl + detailsCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            showToast(R.string.tip_request_failed);
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
                            Type typeOfT = new TypeToken<AriaStatus>() {
                            }.getType();
                            try {
                                if (response.isSuccessful()) {
                                    String body = response.body().string();
//                           Logger.LOGD(TAG, "Get Task Status Result: " + body);
                                    JSONObject json = new JSONObject(body);
                                    mAriaStatus = GsonUtils.decodeJSON(json.getString("result"), typeOfT);
                                    notifyCurFragment();
//                           Logger.LOGD(TAG, "Task Status Details: Dir=" + mAriaStatus.getDir()
//                                    + "; Speed=" + mAriaStatus.getDownloadSpeed() + "; Files="
//                                    + mAriaStatus.getFiles());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast(R.string.error_json_exception);
                            }

                        }
                    });

           /* finalHttp.post(baseUrl + detailsCmd.getEndUrl(),
                    new StringEntity(detailsCmd.toJsonParam()), AriaUtils.ARIA_PARAMS_ENCODE,
                    new AjaxCallBack<String>() {

                        public void onStart() {
                        }

                        @Override
                        public void onSuccess(String result) {
                           Logger.LOGD(TAG, "Get Task Status Result: " + result);
                            Type typeOfT = new TypeToken<AriaStatus>() {
                            }.getType();
                            try {
                                JSONObject json = new JSONObject(result);
                                mAriaStatus = GsonUtils.decodeJSON(json.getString("result"), typeOfT);
                                notifyCurFragment();
                               Logger.LOGD(TAG, "Task Status Details: Dir=" + mAriaStatus.getDir()
                                        + "; Speed=" + mAriaStatus.getDownloadSpeed() + "; Files="
                                        + mAriaStatus.getFiles());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            Logger.LOGE(TAG, "Get Task Status Failed: " + strMsg);
                            Logger.LOGE(TAG, "Exception", t);
                        }

                    });*/

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
        });

    }

    public interface OnAriaTaskChangedListener {
        void onAriaChanged(AriaStatus ariaStatus);
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
