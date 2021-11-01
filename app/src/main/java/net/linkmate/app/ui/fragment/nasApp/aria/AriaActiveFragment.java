package net.linkmate.app.ui.fragment.nasApp.aria;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.SwipeItemLayout;
import com.google.gson.reflect.TypeToken;

import net.linkmate.app.R;
import net.linkmate.app.ui.activity.nasApp.aria.AriaDetailsActivity;
import net.linkmate.app.view.adapter.QuickAriaActiveAdapter;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.oneos.aria.AriaCmd;
import net.sdvn.nascommon.model.oneos.aria.AriaInfo;
import net.sdvn.nascommon.model.oneos.aria.AriaUtils;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.widget.MenuPopupView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Aria2 Active Fragment
 *
 * @author shz
 * @since V1.6.21
 */
public class AriaActiveFragment extends Fragment implements MenuPopupView.OnMenuClickListener {

    private static final String TAG = AriaActiveFragment.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 0x001;
    private static final int MSG_DISMISS_LOADING = 0x004;
    private static final int MSG_REFRESH_DATA = 0x008;


    private RecyclerView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //    private SwipeListView mListView;
    private TextView mEmptyTxt;
    //    private AriaActiveAdapter mAdapter;
    private QuickAriaActiveAdapter mAdapter;
    @NonNull
    private List<AriaInfo> ariaList = new ArrayList<>();
    @Nullable
    private List<AriaInfo> activeList = new ArrayList<>();
    @Nullable
    private List<AriaInfo> waitingList = new ArrayList<>();
    private AriaCmd activeAriaCmd, waitingAriaCmd;
    @Nullable
    private AppCompatActivity mActivity;
    @Nullable
    private Handler mHandler;
    //    private String baseUrl;
    @Nullable
    private String deviceId;

    @NonNull
    public static Fragment newInstance(String deviceId) {
        AriaActiveFragment fragment = new AriaActiveFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_REFRESH_UI:
                        if (isVisible()) mHandler.sendEmptyMessageDelayed(MSG_REFRESH_UI, 2000);
                        getActiveAriaList();
                        getWaitingAriaList();
                        break;
                    case MSG_DISMISS_LOADING:
//                        if (mActivity != null)
//                            mActivity.dismissLoading();
                        break;
                    case MSG_REFRESH_DATA:
                        showActiveAndWaitingList();
                    default:
                        super.handleMessage(msg);
                }

            }
        };

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_transfer_child, container, false);
        mActivity = (AppCompatActivity) getActivity();

        initAriaCmdParam();

        initViews(view);

        return view;
    }

    private void initAriaCmdParam() {
        activeAriaCmd = new AriaCmd();
        activeAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        activeAriaCmd.setAction(AriaCmd.AriaAction.GET_ACTIVE_LIST);
        activeAriaCmd.setContents(AriaUtils.ARIA_PARAMS_GET_LIST);

        waitingAriaCmd = new AriaCmd();
        waitingAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        waitingAriaCmd.setAction(AriaCmd.AriaAction.GET_WAITING_LIST);
        waitingAriaCmd.setOffset(0);
        waitingAriaCmd.setCount(1000);
        waitingAriaCmd.setContents(AriaUtils.ARIA_PARAMS_GET_LIST);
    }

    private void initViews(View view) {
//        View mEmptyView = view.findViewById(R.id.layout_empty);
//        mEmptyTxt = view.findViewById(R.id.txt_empty);
//        mListView = view.findViewById(R.id.recycle_view);
//        mListView.setEmptyView(mEmptyView);
//        mAdapter = new AriaActiveAdapter(getActivity(), ariaList, mListView.getRightViewWidth());
//        mListView.setAdapter(mAdapter);

//        mListView.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                if (arg2 < ariaList.size()) {
//                    String gid = ariaList.get(arg2).getGid();
//                    Intent intent = new Intent(getActivity(), AriaDetailsActivity.class);
//                    intent.putExtra("TaskGid", gid);
//                    startActivity(intent);
//                }
//            }
//        });
        mAdapter = new QuickAriaActiveAdapter();
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (view.getId() == R.id.layout_power_off)
                    if (position >= 0 && position < ariaList.size()) {
                        String gid = ariaList.get(position).getGid();
                        Intent intent = new Intent(getActivity(), AriaDetailsActivity.class);
                        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
                        intent.putExtra("TaskGid", gid);
                        startActivity(intent);
                    }
            }


        });
        mAdapter.setOnAriaControlListener(new QuickAriaActiveAdapter.OnAriaControlListener() {

            @Override
            public void onControl(@Nullable AriaInfo info, boolean isDel) {
                if (null != info) {
                    AriaCmd optCmd = new AriaCmd();
                    optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
                    optCmd.setContent(info.getGid());

                    if (isDel) {
                        optCmd.setAction(AriaCmd.AriaAction.REMOVE);
                        notifyConfirmDeleteDialog(optCmd);
                    } else {
                        String status = info.getStatus();
                        if (status.equalsIgnoreCase("active")) {
                            optCmd.setAction(AriaCmd.AriaAction.PAUSE);
                        } else if (status.equalsIgnoreCase("waiting")) {
                            optCmd.setAction(AriaCmd.AriaAction.PAUSE);
                        } else if (status.equalsIgnoreCase("paused")) {
                            optCmd.setAction(AriaCmd.AriaAction.RESUME);
                        }
                        doOperateAriaTask(optCmd);
                    }
                }
            }
        });
        View mEmptyView = LayoutInflater.from(view.getContext()).inflate(R.layout.layout_empty_view, null);
        mEmptyTxt = mEmptyView.findViewById(R.id.txt_empty);
        mListView = view.findViewById(R.id.recycle_view);
        mAdapter.setEmptyView(mEmptyView);
        mListView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(mListView.getContext()));
        mListView.setLayoutManager(new LinearLayoutManager(mListView.getContext()));
        mListView.addItemDecoration(new DividerItemDecoration(mListView.getContext(), LinearLayoutManager.VERTICAL));
        mListView.setAdapter(mAdapter);
        mAdapter.setNewData(ariaList);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActiveAriaList();
                getWaitingAriaList();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
        if (mListView != null && mAdapter != null) {
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
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
        startOrStopUpdateUIThread(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        startOrStopUpdateUIThread(false);
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();

    }

    private void startOrStopUpdateUIThread(boolean start) {
        if (mHandler != null) {
            if (start) {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
                mHandler.sendEmptyMessageDelayed(MSG_REFRESH_UI, 1000);
            } else {
                mHandler.removeMessages(MSG_REFRESH_UI);
            }
        }
    }


    private void notifyConfirmDeleteDialog(@Nullable final AriaCmd cmd) {
        Activity activity = getActivity();
        if (null == cmd || null == activity) {
            return;
        }

        DialogUtils.showConfirmDialog(activity, DialogUtils.RESOURCE_ID_NONE,
                R.string.confirm_del_active_aria, R.string.confirm, R.string.cancel,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            doOperateAriaTask(cmd);
                        }
                    }
                });
    }

    private void dismissProgressDelay() {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_DISMISS_LOADING, 2000);
        }
    }

    private void doOperateAriaTask(@NonNull final AriaCmd optCmd) {
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
            @Override
            public void onSuccess(String url, @NonNull LoginSession loginSession) {
                try {
                    String baseUrl = loginSession.getUrl();
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), optCmd.toJsonParam());
                    final Request request = new Request.Builder().post(body).url(baseUrl + optCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                            dismissProgressDelay();
                        }

                        @Override
                        public void onResponse(Call call, Response response) {

                            dismissProgressDelay();

                        }
                    });
//                    if (mActivity != null)
//                        mActivity.showLoading(R.string.loading);
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

    private void showActiveAndWaitingList() {

        ariaList.clear();
        if (activeList != null) {
            ariaList.addAll(activeList);
        }
        if (waitingList != null) {
            ariaList.addAll(waitingList);
        }

        if (ariaList.size() == 0) {
            mEmptyTxt.setText(R.string.empty_transfer_list);
        }

        refreshUI();
    }

    private void getActiveAriaList() {
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
            @Override
            public void onSuccess(String url, @NonNull LoginSession loginSession) {
                try {
                    String baseUrl = loginSession.getUrl();

                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), activeAriaCmd.toJsonParam());
                    final Request request = new Request.Builder().post(body).url(baseUrl + activeAriaCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (mActivity != null) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mEmptyTxt.setText(R.string.connect_aria_exception);
                                        activeList.clear();
                                        if (mHandler != null) {
                                            mHandler.sendEmptyMessage(MSG_REFRESH_DATA);
                                        }
                                    }
                                });
                            }

                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
                            if (response.isSuccessful()) {
                                try {
                                    String body = response.body().string();
                                    JSONObject json = new JSONObject(body);
                                    if (!json.isNull("result")) {
                                        activeList = getAriaList(json.getString("result"));
                                    }
                                    if (mHandler != null) {
                                        mHandler.sendEmptyMessage(MSG_REFRESH_DATA);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showToast(R.string.error_json_exception);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    showToast(R.string.app_exception);
                                }
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

        });
    }

    private void refreshUI() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getWaitingAriaList() {
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {
            @Override
            public void onSuccess(String url, @NonNull LoginSession loginSession) {
                try {
                    String baseUrl = loginSession.getUrl();
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), waitingAriaCmd.toJsonParam());
                    final Request request = new Request.Builder().post(body).url(baseUrl + waitingAriaCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
//                    dismissProgressDelay();
                            if (mActivity != null) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mEmptyTxt.setText(R.string.connect_aria_exception);
                                        if (waitingList != null) {
                                            waitingList.clear();
                                        }
                                        if (mHandler != null) {
                                            mHandler.sendEmptyMessage(MSG_REFRESH_DATA);
                                        }
                                    }
                                });
                            }


                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
                            if (response.isSuccessful()) {
                                try {
                                    JSONObject json = new JSONObject(response.body().string());
                                    if (json.has("result") && !json.isNull("result")) {
                                        waitingList = getAriaList(json.getString("result"));
                                    }
                                    if (mHandler != null) {
                                        mHandler.sendEmptyMessage(MSG_REFRESH_DATA);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showToast(R.string.error_json_exception);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast(R.string.app_exception);
                                }
                            }

//                    dismissProgressDelay();
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
        });

    }

    private void operateAllAriaTask(int index) {
        AriaCmd optCmd = new AriaCmd();
        optCmd.setEndUrl(AriaUtils.ARIA_END_URL);

        switch (index) {
            case 0:
                optCmd.setAction(AriaCmd.AriaAction.RESUME_ALL);
                doOperateAriaTask(optCmd);
                break;
            case 1:
                optCmd.setAction(AriaCmd.AriaAction.PAUSE_ALL);
                doOperateAriaTask(optCmd);
                break;
            case 2:
                ArrayList<String> mGids = new ArrayList<>();
                synchronized (AriaActiveFragment.this) {
                    for (AriaInfo info : ariaList) {
                        mGids.add(info.getGid());
                    }
                }
                optCmd.setContentList(mGids);
                optCmd.setAction(AriaCmd.AriaAction.REMOVE);
                notifyConfirmDeleteDialog(optCmd);
                break;
        }
    }

    @Nullable
    private ArrayList<AriaInfo> getAriaList(String json) {
        ArrayList<AriaInfo> list = new ArrayList<>();
        Type typeOfT = new TypeToken<List<AriaInfo>>() {
        }.getType();
        try {
            list = GsonUtils.decodeJSON(json, typeOfT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void onMenuClick(int index, View view) {
        operateAllAriaTask(index);
    }

    private void showToast(@StringRes final int resId) {
        if (getActivity() != null)
            getActivity().
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastHelper.showToast(resId);
                        }
                    });
    }
}
