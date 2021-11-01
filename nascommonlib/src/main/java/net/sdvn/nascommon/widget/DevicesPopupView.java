package net.sdvn.nascommon.widget;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.rxjava.rxlife.RxLife;

import net.sdvn.cmapi.Device;
import net.sdvn.common.repo.BriefRepo;
import net.sdvn.nascommon.LibApp;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.UserInfoKeeper;
import net.sdvn.nascommon.db.objecbox.UserInfo;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.iface.LoadingCallback;
import net.sdvn.nascommon.iface.OnResultListener;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.oneos.OneOSFileType;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.NotNull;
import org.view.libwidget.LeoRvAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.weline.devhelper.IconHelper;


public class DevicesPopupView {
    private final LoadingCallback loadingCallback;
    private View mLayoutMiddle2;
    private final TextView mTvPath, mTitle;
    private RecyclerView mRecyclerView;
    private ArrayList<DeviceModel> mList;
    private FragmentActivity context;
    private TextView mDownloadToPhone;
    private Button mConfirmBtn;
    public PopupListAdapter mAdapter;
    @NonNull
    private HashMap<Integer, Boolean> isSelected = new HashMap<>();
    @Nullable
    private PopDialogFragment dialog;
    //    private String appName = MyApplication.getAppContext().getResources().getString(R.string.app_name);
    private String mSelectDevId;
    private String mRealPath = AppConstants.SHARE_DOWNLOADS_PATH;
    @Nullable
    private OnResultListener mListener;
    private boolean isNeedPath = false;

    @SuppressLint("SetTextI18n")
    public DevicesPopupView(@NonNull final FragmentActivity context, LoadingCallback loadingCallback, final View rootView) {
        this.context = context;
        this.loadingCallback = loadingCallback;
        mList = new ArrayList<>();

        final View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_devices, null);
        ViewGroup container_list_view = view.findViewById(R.id.container_list_view);

        mDownloadToPhone = view.findViewById(R.id.btn_to_phone);
        mLayoutMiddle2 = view.findViewById(R.id.layout_middle2);
        mLayoutMiddle2.setVisibility(View.GONE);
        mConfirmBtn = view.findViewById(R.id.btn_confirm);
        mTitle = view.findViewById(R.id.popup_title);
        mRecyclerView = view.findViewById(R.id.listview_user);
        TextView emptyView = view.findViewById(R.id.txt_empty);
        emptyView.setText(context.getResources().getString(R.string.nullnull)
                + context.getResources().getString(R.string.device));
        container_list_view.setVisibility(View.VISIBLE);
        mTvPath = view.findViewById(R.id.tv_path);
        mTvPath.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mSelectDevId)) {
                    ToastHelper.showToast(R.string.tip_please_check_device);
                    return;
                }
                SessionManager.getInstance().getLoginSession(mSelectDevId, new GetSessionListener() {
                    @Override
                    public void onSuccess(String url, @NonNull final LoginSession data) {
                        ServerFileTreeView fileTreeView = new ServerFileTreeView(context, loadingCallback, data, R.string.tip_download_file, R.string.confirm);
                        fileTreeView.showPopupCenter();
                        fileTreeView.setOnPasteListener((toPath, _share_path_type) -> {
                            mRealPath = toPath;
                            DeviceModel model = SessionManager.getInstance().getDeviceModel(mSelectDevId);
                            String devMarkName = model != null ? model.getDevName() : "";
                            setTvPathText(mTvPath, toPath == null ? "" : toPath, devMarkName);
                            if (mListener != null) {
                                mListener.onResult(mRealPath);
                            }
                        });
                    }
                });
            }
        });
//        mListView.setEmptyView(emptyView);
        mAdapter = new PopupListAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((leoRvAdapter, view1, position) -> {
            //保存选中数据
            CheckableImageButton check = view.findViewById(R.id.select_box);
            boolean lastChecked = check.isChecked();
            check.toggle();
            boolean isSelect = check.isChecked();
            isSelected.clear();
            for (int i = 0; i < mList.size(); i++) {
                isSelected.put(i, false);
            }
            getIsSelected().put(position, isSelect);
            DeviceModel deviceModel = mList.get(position);
            mSelectDevId = isSelect ? deviceModel.getDevId() : "";
            setTvPathText(mTvPath, isSelect ? mRealPath : "", deviceModel.getDevName());
            mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount(), Collections.singletonList(deviceModel));
            return null;
        });
//        dialog = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        dialog.setOutsideTouchable(false);
//        PopupWindowCompat.showAsDropDown(dialog, rootView, 0, 0, Gravity.CENTER);
//        dialog = new Dialog(context, R.style.NasDialogTheme);
//        dialog.setContentView(view);
//        dialog.setCancelable(false);
//        dialog.show();
        dialog = PopDialogFragment.newInstance(true, view);
        dialog.show(context.getSupportFragmentManager(), "Devices");

        Button mCancelBtn = view.findViewById(R.id.btn_cancel);
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setNeedPath(boolean isNeedPath) {
        this.isNeedPath = isNeedPath;
    }

    private void setTvPathText(@NonNull TextView textView, @NonNull String pathName, String devMarkName) {
        if (!TextUtils.isEmpty(pathName) && isNeedPath) {
            textView.setVisibility(View.VISIBLE);
            String pathWithTypeName = OneOSFileType.getPathWithTypeName(pathName);
            if (!TextUtils.isEmpty(devMarkName))
                pathName = devMarkName + ":" + pathWithTypeName;
            textView.setText(Utils.setKeyWordColor(textView.getContext(), (R.color.primary), pathName, devMarkName));
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    public void setConfirmOnClickListener(@Nullable final OnResultListener<String> listener) {
        mListener = listener;
        mConfirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onResult(mRealPath);
                }
            }
        });
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void setTitleText(@StringRes int resid) {
        mTitle.setText(resid);
    }

    public void addList(@NonNull List<DeviceModel> deviceList) {
        mList.clear();
        mList.addAll(deviceList);
        initDate();
    }

    private void initDate() {
        isSelected.clear();
        for (int i = 0; i < mList.size(); i++) {
            isSelected.put(i, false);
        }
        mAdapter.setData(mList);
        mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount(), Collections.singletonList(mList));
    }

    @NonNull
    public HashMap<Integer, Boolean> getIsSelected() {
        return isSelected;
    }

    public void setDownloadToPhone(@Nullable OnClickListener listener) {
        mLayoutMiddle2.setVisibility(listener == null ? View.GONE : View.VISIBLE);
        mDownloadToPhone.setOnClickListener(listener);
    }

    public boolean isShowing() {
        return dialog != null;
    }

    public class PopupListAdapter extends LeoRvAdapter<DeviceModel> {

        @Override
        protected int getItemLayout(int position) {
            return R.layout.item_listview_choose_device;
        }

        @Override
        protected void bindData(@NotNull LeoRvAdapter.ItemHelper helper, @NotNull DeviceModel deviceModel, @org.jetbrains.annotations.Nullable List<Object> payloads) {
            View itemView = helper.getItemView();
            TextView tvName = helper.findViewById(R.id.tv_device_name);
            TextView tvIp = helper.findViewById(R.id.tv_device_ip);
            ShapeableImageView mImageView = helper.findViewById(R.id.iv_device);

            Device device = deviceModel.getDevice();
            int devClass = deviceModel.getDevClass();
            boolean isOnline = deviceModel.isOnline();
            String user = SPUtils.getValue(context, AppConstants.SP_FIELD_USERNAME);
            UserInfo userInfo = UserInfoKeeper.getUserInfo(user, deviceModel.getDevId());
            String devName = deviceModel.getDevName();
            if (SPUtils.getBoolean(AppConstants.SP_SHOW_REMARK_NAME, true)) {
                if (userInfo != null && !TextUtils.isEmpty(userInfo.getDevMarkName())) {
                    devName = userInfo.getDevMarkName();
                }
                tvName.setText(devName);
                deviceModel.getDevNameFromDB()
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(RxLife.as(itemView))
                        .subscribe(s -> {
                            helper.setText(R.id.tv_device_name, s);
                        }, throwable -> {
                        });
            } else {
                tvName.setText(devName);
            }
            int iconByeDevClass = IconHelper.getIconByeDevClass(devClass, isOnline, true);

            if (itemView.getTag() != deviceModel.getDevId()) {
                mImageView.setTag(null);
                itemView.setTag(deviceModel.getDevId());
            }
            if (mImageView.getTag() == null) mImageView.setImageResource(iconByeDevClass);
            LibApp.Companion.getInstance().getBriefDelegete().loadDeviceBrief(deviceModel.getDevId(), BriefRepo.getBrief(device.getId(), BriefRepo.FOR_DEVICE),
                    mImageView, null, iconByeDevClass, null, 0);

            if (device != null)
                tvIp.setText(device.getVip());
            Boolean aBoolean = getIsSelected().get(helper.getViewHolder().getAdapterPosition());
            helper.setChecked(R.id.select_box,aBoolean == null ? false : aBoolean);
        }

    }


}
