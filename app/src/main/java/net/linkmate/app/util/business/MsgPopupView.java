//package net.linkmate.app.util.business;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.PopupWindow;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//
//import net.linkmate.app.R;
//import net.linkmate.app.manager.MessageManager;
//import net.linkmate.app.ui.activity.message.SystemMessageActivity;
//import net.linkmate.app.ui.nas.share.ShareActivity;
//import net.linkmate.app.ui.nas.transfer.TransferActivity;
//import net.linkmate.app.util.MySPUtils;
//import net.sdvn.common.internet.protocol.entity.SdvnMessage;
//import net.sdvn.nascommon.utils.Utils;
//import net.sdvn.nascommon.widget.SupportPopupWindow;
//
//import java.util.List;
//
//public class MsgPopupView implements OnClickListener, MessageManager.MessagesListObserver {
//    private TextView tvMsgCount;
//    private TextView tvShareCount;
//    private TextView tvTransCount;
//    private Context context;
//    private PopupWindow mPopupMenu;
//    private PopupWindow.OnDismissListener mOnDismissListener;
//
//    public MsgPopupView(@NonNull Context context) {
//        this.context = context;
//        RelativeLayout layout = new RelativeLayout(context);
//        layout.setBackgroundColor(context.getResources().getColor(R.color.bg_pop_type));
//        layout.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//            }
//        });
//        View view = LayoutInflater.from(context).inflate(R.layout.include_msg_bar, null);
//        view.setBackgroundColor(context.getResources().getColor(R.color.bg_light));
//        tvMsgCount = view.findViewById(R.id.home_msg_tv_msg_count);
//        tvShareCount = view.findViewById(R.id.home_msg_tv_share_count);
//        tvTransCount = view.findViewById(R.id.home_msg_tv_trans_count);
//        view.findViewById(R.id.home_msg_system_msg).setOnClickListener(this);
//        view.findViewById(R.id.home_msg_file_share).setOnClickListener(this);
//        view.findViewById(R.id.home_msg_rl_upload_download).setOnClickListener(this);
//        layout.addView(view);
//
//        mPopupMenu = new SupportPopupWindow(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        mPopupMenu.setAnimationStyle(R.style.AnimAlphaEnterAndExit);
//        mPopupMenu.setTouchable(true);
//        mPopupMenu.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
//    }
//
//    public void showPopupTop(@NonNull View parent) {
//        mPopupMenu.showAsDropDown(parent);
//        mPopupMenu.setAnimationStyle(R.style.popup_window_anim);
//        mPopupMenu.setFocusable(true);
//        mPopupMenu.setOutsideTouchable(true);
//        mPopupMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                dismiss();
////                MessageManager.getInstance().deleteMessagesListObserver(MsgPopupView.this);
//            }
//        });
//        mPopupMenu.update();
////        MessageManager.getInstance().addMessagesListObserver(this);
//        onMessagesListChanged(MySPUtils.getMessageNewCount(), MessageManager.getInstance().getMessageslist());
//    }
//
//    public void dismiss() {
//        if (mPopupMenu != null && mPopupMenu.isShowing()) {
//            mPopupMenu.dismiss();
//        }
//        if (mOnDismissListener != null) {
//            mOnDismissListener.onDismiss();
//        }
//    }
//
//    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
//        mOnDismissListener = onDismissListener;
//    }
//
//    public boolean isShow() {
//        return mPopupMenu != null && mPopupMenu.isShowing();
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (Utils.isFastClick(v)) return;
//        if (context != null)
//            switch (v.getId()) {
//                case R.id.home_msg_system_msg:
//                    context.startActivity(new Intent(context, SystemMessageActivity.class));
//                    dismiss();
//                    break;
//                case R.id.home_msg_file_share:
//                    context.startActivity(new Intent(context, ShareActivity.class));
//                    dismiss();
//                    break;
//                case R.id.home_msg_rl_upload_download:
//                    context.startActivity(new Intent(context, TransferActivity.class));
//                    dismiss();
//                    break;
//            }
//    }
//
//    @SuppressLint("DefaultLocale")
//    @Override
//    public void onMessagesListChanged(int newCount, List<SdvnMessage> messages) {
//        if (context != null && tvMsgCount != null) {
//            tvMsgCount.setVisibility(newCount > 0 ? View.VISIBLE : View.GONE);
//            tvMsgCount.setText(String.format("%d", newCount));
//        }
//    }
//}
