package net.linkmate.app.ui.fragment.nasApp.aria;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.SwipeItemLayout;
import com.google.gson.reflect.TypeToken;

import net.linkmate.app.R;
import net.linkmate.app.ui.activity.nasApp.aria.AriaDetailsActivity;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.oneos.aria.AriaCmd;
import net.sdvn.nascommon.model.oneos.aria.AriaFile;
import net.sdvn.nascommon.model.oneos.aria.AriaInfo;
import net.sdvn.nascommon.model.oneos.aria.AriaUtils;
import net.sdvn.nascommon.model.oneos.aria.BitTorrent;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.log.Logger;
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
 * Aria2 Stopped Fragment
 *
 * @author shz
 * @since V1.6.21
 */
public class AriaStoppedFragment extends Fragment implements MenuPopupView.OnMenuClickListener {

    private static final String TAG = AriaStoppedFragment.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;

    private boolean isFragmentVisiable = true;

    //    private SwipeListView mListView;
    private RecyclerView mListView;
    private TextView mEmptyTxt;
    //    private AriaStoppedAdapter mAdapter;
    @NonNull
    private List<AriaInfo> ariaList = new ArrayList<>();
    private AriaCmd stoppedAriaCmd;
    @Nullable
    private AppCompatActivity activity;
    @Nullable
    private BaseQuickAdapter<AriaInfo, BaseViewHolder> mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //    private String baseUrl;
    @Nullable
    private String deviceId;

    @NonNull
    public static Fragment newInstance(String deviceId) {
        AriaStoppedFragment fragment = new AriaStoppedFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_transfer_child, container, false);
        activity = (AppCompatActivity) getActivity();

        initAriaCmdParam();

        initViews(view);
        return view;
    }

    private void initAriaCmdParam() {
        stoppedAriaCmd = new AriaCmd();
        stoppedAriaCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        stoppedAriaCmd.setAction(AriaCmd.AriaAction.GET_STOP_LIST);
        stoppedAriaCmd.setCount(1000);
        stoppedAriaCmd.setOffset(0);
        stoppedAriaCmd.setContents(AriaUtils.ARIA_PARAMS_GET_LIST);
    }

    private void initViews(View view) {
        mAdapter = new BaseQuickAdapter<AriaInfo, BaseViewHolder>(R.layout.item_listview_aria_record) {

            @Override
            protected void convert(@NonNull BaseViewHolder holder, @Nullable final AriaInfo info) {
                boolean isBTAria;
                String taskName = "";
                if (info == null) {
                    return;
                }
                BitTorrent bt = info.getBittorrent();
                if (null != bt) {
                    isBTAria = true;
                    if (null != bt.getInfo())
                        taskName = bt.getInfo().getName();
                } else {
                    isBTAria = false;
                    List<AriaFile> files = info.getFiles();
                    if (null != files && files.size() > 0) {
                        for (AriaFile file : files) {
                            String name = FileUtils.getFileName(file.getPath());
                            taskName += name + " ";
                        }
                    }
                }

                long completeLen = 0, totalLen = 0;

                try {
                    completeLen = Long.valueOf(info.getCompletedLength());
                    totalLen = Long.valueOf(info.getTotalLength());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String size = FileUtils.fmtFileSize(completeLen) + "/"
                        + FileUtils.fmtFileSize(totalLen);
                String status = info.getStatus();
                if (status.equalsIgnoreCase("complete")) {
                    // size += "已完成";
                    holder.setText(R.id.file_state, R.string.completed);
                    holder.setBackgroundRes(R.id.file_state, R.drawable.share_rect_green);

                } else if (status.equalsIgnoreCase("removed")) {
                    holder.setText(R.id.file_state, R.string.remove);
                    holder.setBackgroundRes(R.id.file_state, R.drawable.share_rect_gray);
                } else {
                    // size += "下载失败";
                    holder.setText(R.id.file_state, R.string.failed);
                    holder.setBackgroundRes(R.id.file_state, R.drawable.share_rect_red);
                }

                holder.setText(R.id.file_name, taskName);
                if (isBTAria) {
                    holder.setImageResource(R.id.file_icon, R.drawable.icon_aria_bt);
                } else {
                    holder.setImageResource(R.id.file_icon, FileUtils.fmtFileIcon(taskName));
                }
                holder.setText(R.id.file_size, size);

                holder.itemView.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (null != info) {
                            AriaCmd optCmd = new AriaCmd();
                            optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
                            optCmd.setContent(info.getGid());
                            optCmd.setAction(AriaCmd.AriaAction.DELETE);

                            notifyConfirmDeleteDialog(optCmd);
                        }
                    }
                });
                holder.addOnClickListener(R.id.layout_power_off);

            }
        };

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
                getStoppedAriaList();
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.LOGD(TAG, "On Configuration Changed");
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {

        }

        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.LOGD(TAG, "On Resume");
        isFragmentVisiable = true;
//        startUpdateUIThread();
        if (getArguments() != null)
            deviceId = getArguments().getString(AppConstants.SP_FIELD_DEVICE_ID);
        getStoppedAriaList();
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentVisiable = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentVisiable = false;
//        if (handler != null) {
//            handler.removeCallbacksAndMessages(null);
//            handler = null;
//        }
    }

    private void notifyConfirmDeleteDialog(@Nullable final AriaCmd cmd) {
        Activity activity = getActivity();
        if (null == cmd || null == activity) {
            return;
        }

        DialogUtils.showConfirmDialog(activity, R.string.tips, R.string.confirm_del_stopped_aria, R.string.confirm,
                R.string.cancel, new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            doOperateAriaTask(cmd);
                        }
                    }
                });
    }

    private void dismissProgressDelay() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
//                if (activity != null)
//                    activity.dismissLoading();
            }
        }, 1000);
    }

    private void doOperateAriaTask(@NonNull final AriaCmd optCmd) {
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {

            @Override
            public void onSuccess(String url, @NonNull LoginSession data) {
                try {
                    String baseUrl = data.getUrl();
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), optCmd.toJsonParam());
                    Request request = new Request.Builder().post(body).url(baseUrl + optCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            dismissProgressDelay();
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {

                            if (response.isSuccessful()) {
                                try {
                                    getStoppedAriaList();
                                    Logger.LOGD(TAG, "Operate Aria Result: " + response.body().string());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    showToast(R.string.file_not_found);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    showToast(R.string.app_exception);
                                }
                            }
                            dismissProgressDelay();


                        }
                    });
//                    if (activity != null)
//                        activity.showLoading();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showToast(R.string.file_not_found);
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(R.string.app_exception);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast(R.string.error_json_exception);
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(R.string.app_exception);
                }
            }
        });


    }


    private void getStoppedAriaList() {
        if (!isFragmentVisiable) return;
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener() {

            @Override
            public void onSuccess(String url, @NonNull LoginSession data) {
                try {
                    String baseUrl = data.getUrl();
                    RequestBody body = RequestBody.create(MediaType.parse(AriaUtils.ARIA_PARAMS_ENCODE), stoppedAriaCmd.toJsonParam());

                    final Request request = new Request.Builder().post(body)
                            .url(baseUrl + stoppedAriaCmd.getEndUrl()).build();
                    OkHttpClientIns.getApiClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Logger.LOGE(TAG, "Get Task Option Failed: " + e.getMessage());
                            if (mSwipeRefreshLayout != null)
                                mSwipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
                            try {
                                if (response.isSuccessful()) {
                                    ariaList.clear();
                                    JSONObject json = new JSONObject(response.body().string());
                                    if (!json.isNull("result")) {
                                        List<AriaInfo> list = getAriaList(json.getString("result"));
                                        if (null != list) {
                                            ariaList.addAll(list);
                                        }
                                    }
                                    if (activity != null)
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (ariaList.size() == 0) {
                                                    if (mEmptyTxt != null)
                                                        mEmptyTxt.setText(R.string.empty_transfer_list);
                                                }
                                                if (mSwipeRefreshLayout != null)
                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                if (mAdapter != null)
                                                    mAdapter.notifyDataSetChanged();

                                            }
                                        });
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
        });

    }

    @Nullable
    private ArrayList<AriaInfo> getAriaList(String json) {
        ArrayList<AriaInfo> list = new ArrayList<AriaInfo>();
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
        AriaCmd optCmd = new AriaCmd();
        optCmd.setEndUrl(AriaUtils.ARIA_END_URL);
        // ArrayList<String> mGids = new ArrayList<String>();
        // for (AriaInfo info : ariaList) {
        // mGids.add(info.getGid());
        // }
        // optCmd.setContentList(mGids);
        optCmd.setAction(AriaCmd.AriaAction.DELETE_ALL);

        notifyConfirmDeleteDialog(optCmd);
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
