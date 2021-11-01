package net.linkmate.app.ui.activity.nasApp.aria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.ui.fragment.nasApp.aria.AriaActiveFragment;
import net.linkmate.app.ui.fragment.nasApp.aria.AriaStoppedFragment;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.oneos.aria.AriaCmd;
import net.sdvn.nascommon.model.oneos.aria.AriaUtils;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.widget.MenuPopupView;
import net.sdvn.nascommon.widget.SettingsPopupView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AriaActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = AriaActivity.class.getSimpleName();
    private RadioGroup mTransOrCompleteGroup;
    @Nullable
    private Fragment mActiveFragment, mRecordFragment, mCurFragment;
    private MenuPopupView mMenuView;
    private SettingsPopupView mSettingsView;
    private static final int[] TRANS_CONTROL_TITLE = new int[]{R.string.start_all, R.string.pause_all, R.string.delete_all, R.string.settings};
    private static final int[] TRANS_CONTROL_ICON = new int[]{R.drawable.ic_title_menu_download, R.drawable.ic_title_menu_pause, R.drawable.ic_title_menu_delete,
            R.drawable.icon_title_settings};
    private static final int[] RECORD_CONTROL_TITLE = new int[]{R.string.delete_all, R.string.settings};
    private static final int[] RECORD_CONTROL_ICON = new int[]{R.drawable.ic_title_menu_delete, R.drawable.icon_title_settings};
    private String deviceId;
    private View topView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_aria);
        topView = findViewById(R.id.top_view);

        FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment active = fragmentManager.findFragmentByTag(String.valueOf(false));
        final Fragment record = fragmentManager.findFragmentByTag(String.valueOf(true));

        Intent intent = getIntent();
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
        Bundle args = new Bundle();
        args.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
        if (active != null)
            active.setArguments(args);
        if (record != null)
            record.setArguments(args);
        mActiveFragment = active == null ? AriaActiveFragment.newInstance(deviceId) : active;
        mRecordFragment = record == null ? AriaStoppedFragment.newInstance(deviceId) : record;

        ImageButton mBackBtn = findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        TextView mBackTxt = findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(this);

        ImageButton mAddBtn = findViewById(R.id.btn_add_task);
        mAddBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AriaActivity.this, AddAriaTaskActivity.class);
                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
                startActivity(intent);
            }
        });

        ImageButton mCtrlBtn = findViewById(R.id.btn_control_task);
        mCtrlBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCurFragment instanceof MenuPopupView.OnMenuClickListener) {
                    mMenuView = new MenuPopupView(AriaActivity.this, Utils.dipToPx(130));
                    if (mCurFragment instanceof AriaActiveFragment) {
                        mMenuView.setMenuItems(TRANS_CONTROL_TITLE, TRANS_CONTROL_ICON);
                    } else {
                        mMenuView.setMenuItems(RECORD_CONTROL_TITLE, RECORD_CONTROL_ICON);
                    }
                    mMenuView.setOnMenuClickListener(new MenuPopupView.OnMenuClickListener() {

                        @Override
                        public void onMenuClick(int index, View view) {
                            if (mCurFragment instanceof AriaActiveFragment) {
                                if (index == 3) {
                                    getGlobalOption();
                                    return;
                                }
                            } else {
                                if (index == 1) {
                                    getGlobalOption();
                                    return;
                                }
                            }

                            ((MenuPopupView.OnMenuClickListener) mCurFragment).onMenuClick(index, view);
                        }
                    });
                    //适配反转布局
                    mMenuView.showPopupDown(v, -1, v.getLayoutDirection() ==View.LAYOUT_DIRECTION_LTR);
                }
            }
        });

        mTransOrCompleteGroup = findViewById(R.id.segmented_radiogroup);
        mTransOrCompleteGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onChangeFragment(checkedId == R.id.rb_record);
            }
        });
        ((TextView) findViewById(R.id.rb_downloading)).setText(getString(R.string.downloading_list, ""));
        onChangeFragment(false);
    }

    @Override
    protected View getTopView() {
        return topView;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.txt_title_back:
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    public void onChangeFragment(boolean isRecord) {
        Fragment mFragment;
        if (isRecord) {
            mFragment = mRecordFragment;
        } else {
            mFragment = mActiveFragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurFragment != null) {
            mCurFragment.onPause();
            transaction.hide(mCurFragment);
        }

        if (!mFragment.isAdded()) {
            transaction.add(R.id.transfer_frame_layout, mFragment, String.valueOf(isRecord));
        } else {
            mFragment.onResume();
        }

        transaction.show(mFragment);
        mCurFragment = mFragment;

        transaction.commit();
    }

    private void getGlobalOption() {
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
            @Override
            public void onSuccess(String url, @NonNull LoginSession data) {
                String baseUrl = data.getUrl();
                AriaCmd optAriaCmd = new AriaCmd();
                optAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
                optAriaCmd.setAction(AriaCmd.AriaAction.GET_GLOBAL_OPTION);
                try {
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), optAriaCmd.toJsonParam());
                    final Request request = new Request.Builder().post(body).url(baseUrl + optAriaCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            dismissLoading();
                            e.printStackTrace();

                            showToast(R.string.error_request_aria_params);


                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            dismissLoading();
                            // 1. 并发数: max-concurrent-downloads
                            // 2. 上传速度限制: max-upload-limit
                            // 3. 上传速度限制: max-download-limit
                            // 4. 最小分片大小: piece-length
                            if (response.isSuccessful()) {
                                try {
                                    String body = response.body().string();
//                           Logger.LOGD(TAG, "Get Global Opreate Result: " + body);
                                    final HashMap<String, String> paramMap = new HashMap<String, String>();
                                    JSONObject json = new JSONObject(body).getJSONObject("result");
                                    paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT, json.getString(AriaUtils.ARIA_KEY_GLOBAL_MAX_UPLOAD_LIMIT));
                                    paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT, json.getString(AriaUtils.ARIA_KEY_GLOBAL_MAX_DOWNLOAD_LIMIT));
                                    paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT, json.getString(AriaUtils.ARIA_KEY_GLOBAL_MAX_CUR_CONNECT));
                                    paramMap.put(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE, json.getString(AriaUtils.ARIA_KEY_GLOBAL_SPLIT_SIZE));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSettingsView = new SettingsPopupView(AriaActivity.this, new OnClickListener() {

                                                @Override
                                                public void onClick(View v) {
                                                    mSettingsView.dismiss();
                                                    setGlobalOption(mSettingsView.getChangeSettings());
                                                }
                                            });
                                            mSettingsView.updateSettings(paramMap);
                                            mSettingsView.showPopupCenter(mTransOrCompleteGroup);
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast(R.string.error_request_aria_params);
                                }
                            } else {
                                showToast(R.string.error_request_aria_params);
                            }
                        }
                    });

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

    private void setGlobalOption(@Nullable final HashMap<String, String> paramMap) {
        if (null == paramMap || paramMap.size() <= 0) {
            return;
        }
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {

            @Override
            public void onSuccess(String url, @NonNull LoginSession data) {
                String baseUrl = data.getUrl();

                AriaCmd optAriaCmd = new AriaCmd();
                optAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
                optAriaCmd.setAction(AriaCmd.AriaAction.SET_GLOBAL_OPTION);
                JSONObject json = new JSONObject();
                try {
                    Iterator<String> iter = paramMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        json.put(key, paramMap.get(key));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                optAriaCmd.setAttrJson(json);

                try {
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), optAriaCmd.toJsonParam());
                    final Request request = new Request.Builder().post(body).url(baseUrl + optAriaCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            dismissLoading();
//                    Logger.LOGE(TAG, "Set Global Opreate Exception", t);
                            showToast(R.string.error_set_aria_params);
                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            dismissLoading();
                            if (response.isSuccessful()) {
//                       Logger.LOGD(TAG, "Set Global Opreate Result: " + result);
                                showToast(R.string.success_save_aria_setting);
                            } else {
                                showToast(R.string.error_set_aria_params);
                            }
                        }
                    });
                    showLoading(R.string.request_set_aria_settings);

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

    private void showToast(@StringRes final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.showToast(resId);
            }
        });
    }
}
