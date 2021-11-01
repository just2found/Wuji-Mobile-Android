package net.linkmate.app.view;//package net.linkmate.app.view;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.support.annotation.Nullable;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.AttributeSet;
//import android.util.TypedValue;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import net.linkmate.app.R;
//import net.linkmate.app.adapter.HomeDeviceListAdapter;
//import net.linkmate.app.bean.DeviceBean;
//import net.linkmate.app.util.Dp2PxUtils;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import static net.linkmate.app.base.TempVariable.showBoundLabel;
//
//public class DeviceRecyclerViews extends LinearLayout {
//    private List<DeviceBean> deviceList;
//    private Context context;
//
//    public DeviceRecyclerViews(Context context) {
//        this(context, null);
//    }
//
//    public DeviceRecyclerViews(Context context, @Nullable AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public DeviceRecyclerViews(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.context = context;
//        setOrientation(VERTICAL);
//        initView();
//    }
//
//    private void initView() {
//        if (deviceList == null || deviceList.size() == 0)
//            return;
//        Collections.sort(deviceList, new Comparator<DeviceBean>() {
//            @Override
//            public int compare(DeviceBean o1, DeviceBean o2) {
//                int i = o1.type - o2.type;
//                return i != 0 ? i : o1.mnglevel - o2.mnglevel;
//            }
//        });
//        removeAllViews();
//        RecyclerView rv = null;
//        List<DeviceBean> tempList = null;
//        for (int i = 0; i < deviceList.size(); i++) {
//            DeviceBean bean = deviceList.get(i);
//            if (i == 0 || !isSameType(bean.type, deviceList.get(i - 1).type)) {
//                tempList = new ArrayList<>();
//                rv = addNewUserViewGroup(bean.user, bean.type);
//            }
//            tempList.add(bean);
//            if (i == deviceList.size() - 1 || !isSameType(bean.type, deviceList.get(i + 1).type)) {
//                HomeDeviceListAdapter adapter = new HomeDeviceListAdapter(tempList, 1);
//                adapter.setOnItemViewClickListener(new HomeDeviceListAdapter.OnItemViewClickListener() {
//                    @Override
//                    public void onClick(int position, DeviceBean bean) {
//                        gotoDevicePager(position, bean);
//                    }
//
//                    @Override
//                    public void onLongClick(int position, DeviceBean bean) {
//                        showDeviceDetailDialog(position, bean);
//                    }
//
//                    @Override
//                    public void onMoreClick(int position, DeviceBean bean) {
//
//                    }
//                });
//                rv.setAdapter(adapter);
//            }
//        }
//        invalidate();
//    }
//
//    private boolean isSameType(int type1, int type2) {
////        if ((type1 == 0 || type1 == 1) && (type2 == 0 || type2 == 1)) {
////            return true;
////        }
//        return type1 == type2;
//    }
//
//    private RecyclerView addNewUserViewGroup(String userName, int type) {
//        String titleName = "";
////        int llBgId = R.drawable.bg_device_list_item_blue;
//        int llBgId = R.drawable.bg_square_full_radius_gray_ring;
//        if (type == 0) {
//            titleName = "节点";
//        } else if (type == 1) {
//            titleName = "资源";
//        } else if (type == 2) {
//            titleName = "设备";
//        }
//        LinearLayout ll = new LinearLayout(context);
//        ll.setElement(titleName);
//        ll.setOrientation(VERTICAL);
////        ll.setBackgroundResource(llBgId);
////        ll.setPadding(Dp2PxUtils.dp2px(context, 3), 0, Dp2PxUtils.dp2px(context, 1), 0);
//
////        TextView tv = new TextView(context);
////        tv.setText(titleName);
////        tv.setTextColor(context.getResources().getColor(R.color.text_white));
////        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
////        ll.addView(tv);
//
//        TextView tv = new TextView(context);
//        tv.setText(titleName);
//        tv.setTextColor(context.getResources().getColor(R.color.text_dark_gray));
//        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
//        tv.getPaint().setFakeBoldText(true);
//        ll.addView(tv);
//        ViewGroup.MarginLayoutParams tvParams = (ViewGroup.MarginLayoutParams) tv.getLayoutParams();
//        tvParams.setMargins(Dp2PxUtils.dp2px(context, 6), Dp2PxUtils.dp2px(context, 4), 0, Dp2PxUtils.dp2px(context, 6));
//
//        RecyclerView rv = new RecyclerView(context);
//        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        rv.setBackgroundResource(llBgId);
//        ll.addView(rv);
//
//        addView(ll);
//        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) ll.getLayoutParams();
//        layoutParams.setMargins(Dp2PxUtils.dp2px(context, 4), 0,
//                Dp2PxUtils.dp2px(context, 4), Dp2PxUtils.dp2px(context, 4));
//        return rv;
//    }
//
//    public void setDevices(List<DeviceBean> list) {
//        deviceList = list;
//        initView();
//    }
//
//    public void notifyDataSetChanged() {
//
//    }
//
//    public int getTagViewTop(String element) {
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            if (element.equals(child.getElement())) {
//                return child.getTop();
//            }
//        }
//        return 0;
//    }
//
//    private void gotoDevicePager(int position, DeviceBean bean) {
//        if (bean.type == 0) {
//            showDeviceDetailDialog(position, bean);
//        } else {
//            Toast.makeText(context, "跳转页面", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void showDeviceDetailDialog(int position, DeviceBean bean) {
//        final LayoutInflater inflater = LayoutInflater.from(context);
//        final View view = inflater.inflate(R.layout.dialog_device_detail, null);
//        final Dialog mDialog = new Dialog(getContext(), R.style.DialogTheme);
//        ImageView ivIcon = view.findViewById(R.id.dialog_device_detail_img_icon);
//        TextView tvName = view.findViewById(R.id.dialog_device_detail_tv_name);
//        TextView tvOwner = view.findViewById(R.id.dialog_device_detail_tv_owner);
//        TextView tvIsMine = view.findViewById(R.id.dialog_device_detail_tv_is_mine);
//        switch (bean.type) {
//            case 0:
//                ivIcon.setImageResource(R.drawable.icon_smartnode);
//                view.findViewById(R.id.dialog_device_detail_ll_sn).setVisibility(VISIBLE);
//                break;
//            case 1:
//                ivIcon.setImageResource(R.drawable.icon_device_green);
//                break;
//            case 2:
//                ivIcon.setImageResource(R.drawable.icon_device_androidphone_grey);
//                break;
//        }
//        tvName.setText(bean.name);
//        tvOwner.setText(bean.user);
//        tvIsMine.setVisibility(showBoundLabel && 2 != bean.mnglevel ? View.VISIBLE : View.GONE);
//        if (showBoundLabel) {
//            tvIsMine.setText(0 == bean.mnglevel ? "mine" : "bound");
//            tvIsMine.setBackgroundResource(0 == bean.mnglevel ?
//                    R.drawable.bg_right_top_lab_green : R.drawable.bg_right_top_lab_green);
//        }
//
//        mDialog.setContentView(view);
//        mDialog.setCancelable(true);
//        mDialog.setCanceledOnTouchOutside(true);
//        mDialog.show();
//    }
//}
