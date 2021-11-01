//package net.linkmate.app.view.adapter;
//
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.chad.library.adapter.base.BaseQuickAdapter;
//import com.chad.library.adapter.base.BaseViewHolder;
//
//import net.linkmate.app.R;
//import net.linkmate.app.base.MyConstants;
//import net.linkmate.app.manager.MessageManager;
//import net.linkmate.app.ui.activity.message.SystemMessageActivity;
//import net.linkmate.app.util.MySPUtils;
//import net.linkmate.app.util.ToastUtils;
//import net.sdvn.common.internet.SdvnHttpErrorNo;
//import net.sdvn.common.internet.core.GsonBaseProtocol;
//import net.sdvn.common.internet.listener.ResultListener;
//import net.sdvn.common.internet.loader.ProcessNewsHttpLoader;
//import net.sdvn.common.internet.protocol.ProcessNewsErrorResult;
//import net.sdvn.common.internet.protocol.entity.SdvnMessage;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//
//import io.weline.internetdb.vo.SdvnMessageModel;
//
//public class SystemMsgRVAdapter extends BaseQuickAdapter<SdvnMessageModel, BaseViewHolder> {
//
//    private final SystemMessageActivity activity;
//    private boolean isEdit = false;
//
//    public SystemMsgRVAdapter(SystemMessageActivity activity, @Nullable List<SdvnMessageModel> data) {
//        super(R.layout.item_system_msg, data);
//        this.activity = activity;
//    }
//
//    public boolean getEditMode() {
//        return this.isEdit;
//    }
//
//    public void editMode(boolean isEdit) {
//        this.isEdit = isEdit;
//    }
//
//    public void removeSelect() {
//        editMode(false);
//        List<String> ids = new ArrayList<>();
//        List<SdvnMessageModel> data = new ArrayList<>(getData());
//        Iterator<SdvnMessageModel> iterator = data.iterator();
//        while (iterator.hasNext()) {
//            SdvnMessageModel next = iterator.next();
//            if (next.isSelect()) {
//                ids.add(next.getMsgId());
//                iterator.remove();
//            }
//        }
//        if (ids.size() > 0) {
//            MessageManager.getInstance().removeMessages(ids);
//            List<String> list = MySPUtils.getMessageDeleteIdsKey();
//            list.addAll(ids);
//            MySPUtils.saveMessageDeleteIds(list);
//        }
//        setNewData(data);
//        notifyDataSetChanged();
//    }
//
//    public void remove(String id) {
//        editMode(false);
//        List<String> ids = new ArrayList<>();
//        Iterator<SdvnMessageModel> iterator = getData().iterator();
//        while (iterator.hasNext()) {
//            SdvnMessageModel next = iterator.next();
//            if (next.getMsgId().equals(id)) {
//                ids.add(next.getMsgId());
//                iterator.remove();
//                break;
//            }
//        }
//        MessageManager.getInstance().removeMessages(ids);
//        List<String> list = MySPUtils.getMessageDeleteIdsKey();
//        list.addAll(ids);
//        MySPUtils.saveMessageDeleteIds(list);
//        notifyDataSetChanged();
//    }
//
//    @Override
//    protected void convert(@NonNull final BaseViewHolder helper, final SdvnMessageModel data) {
//        TextView mtvNega = helper.getView(R.id.tv_msg_nega);
//        TextView mtvPosi = helper.getView(R.id.tv_msg_posi);
//        TextView mtvStatus = helper.getView(R.id.tv_msg_status);
//
//        helper.setText(R.id.tv_msg_title, data.getUsername())
//                .setText(R.id.tv_msg_content, data.getMessage());
//
//        if (data.getTimestamp() > 0) {
//            String format = MyConstants.sdf.format(new Date(data.getTimestamp() * 1000));
//            helper.setText(R.id.tv_msg_time, format);
//        }
//
//        helper.setVisible(R.id.iv_msg_selected, isEdit)
//                .setImageResource(R.id.iv_msg_selected, data.isSelect() ?
//                        R.drawable.icon_selected : R.drawable.icon_nomynet);
//
//        mtvPosi.setVisibility(View.GONE);
//        mtvNega.setVisibility(View.GONE);
//        helper.setTextColor(R.id.tv_msg_status, mContext.getResources().getColor(R.color.text_dark_gray));
//        if (data.getStatus().equals(SdvnMessage.MESSAGE_STATUS_WAIT)) {
//            mtvStatus.setVisibility(View.GONE);
//            mtvPosi.setVisibility(View.VISIBLE);
//            mtvNega.setVisibility(View.VISIBLE);
//            helper.setVisible(R.id.iv_msg_selected, false);
//        } else {
//            if (!isEdit) {
//                if (data.getStatus().equals(SdvnMessage.MESSAGE_STATUS_AGREE)) {
//                    agree(helper);
//                    mtvStatus.setVisibility(View.VISIBLE);
//                } else {
//                    disagree(helper);
//                    mtvStatus.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//
//        if (!data.getType().equals(SdvnMessage.APPLY2NET) &&
//                !data.getType().equals(SdvnMessage.INVITE2NET) &&
//                !data.getType().equals(SdvnMessage.BIND_DEV) &&
//                !data.getType().equals(SdvnMessage.BIND_MGR)) {
//            mtvPosi.setVisibility(View.INVISIBLE);
//            mtvNega.setVisibility(View.INVISIBLE);
//            mtvStatus.setVisibility(View.INVISIBLE);
//        }
//        helper.getView(R.id.rl_msg_bg).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isEdit && !data.getStatus().equals(SdvnMessage.MESSAGE_STATUS_WAIT)) {
//                    SystemMsgRVAdapter.this.selectItem(data, helper);
//                }
//            }
//        });
//        helper.addOnLongClickListener(R.id.rl_msg_bg);
//        mtvPosi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (isEdit) {
////                    SystemMsgRVAdapter.this.selectItem(data, helper);
////                } else {
//                data.setStatus(SdvnMessage.MESSAGE_STATUS_AGREE);
//                MessageManager.getInstance().setMessagesStatus(data.getMsgId(), SdvnMessage.MESSAGE_STATUS_AGREE);
//                SystemMsgRVAdapter.this.agree(helper);
//                SystemMsgRVAdapter.this.processMessage(data, SdvnMessage.MESSAGE_STATUS_AGREE);
////                }
//            }
//        });
//        mtvNega.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (isEdit) {
////                    SystemMsgRVAdapter.this.selectItem(data, helper);
////                } else {
//                data.setStatus(SdvnMessage.MESSAGE_STATUS_DISAGREE);
//                MessageManager.getInstance().setMessagesStatus(data.getMsgId(), SdvnMessage.MESSAGE_STATUS_DISAGREE);
//                SystemMsgRVAdapter.this.disagree(helper);
//                SystemMsgRVAdapter.this.processMessage(data, SdvnMessage.MESSAGE_STATUS_DISAGREE);
////                }
//            }
//        });
//    }
//
//    private void agree(BaseViewHolder helper) {
//        messageStatusChange(helper);
//        helper.setText(R.id.tv_msg_status, R.string.agreed)
//                .setTextColor(R.id.tv_msg_status, mContext.getResources().getColor(R.color.text_green));
//    }
//
//    private void disagree(BaseViewHolder helper) {
//        messageStatusChange(helper);
//        helper.setText(R.id.tv_msg_status, R.string.disagreed)
//                .setTextColor(R.id.tv_msg_status, mContext.getResources().getColor(R.color.text_red));
//    }
//
//    private void messageStatusChange(BaseViewHolder helper) {
//        helper.setGone(R.id.tv_msg_nega, false)
//                .setGone(R.id.tv_msg_posi, false)
//                .setGone(R.id.tv_msg_status, true);
//    }
//
//    private void selectItem(SdvnMessageModel data, BaseViewHolder helper) {
//        if (data.isSelect()) {
//            data.setSelect(false);
//            helper.setImageResource(R.id.iv_msg_selected, R.drawable.icon_nomynet);
//        } else {
//            data.setSelect(true);
//            helper.setImageResource(R.id.iv_msg_selected, R.drawable.icon_selected);
//        }
//    }
//
//    private void processMessage(final SdvnMessageModel data, String process) {
//        ProcessNewsHttpLoader loader = new ProcessNewsHttpLoader(ProcessNewsErrorResult.class);
//        loader.setHttpLoaderStateListener(activity);
//        loader.setParams(data.getMsgId(), process);
//        loader.executor(new ResultListener() {
//            @Override
//            public void success(Object tag, GsonBaseProtocol data) {
//            }
//
//            @Override
//            public void error(Object tag, GsonBaseProtocol baseProtocol) {
//                if (baseProtocol instanceof ProcessNewsErrorResult) {
//                    ProcessNewsErrorResult errorResult = (ProcessNewsErrorResult) baseProtocol;
//                    if (errorResult.result == SdvnHttpErrorNo.EC_NEWS_HAS_BEEN_PROCESSED && errorResult.process != null) {
//                        MessageManager.getInstance().setMessagesStatus(data.getMsgId(), errorResult.process);
//                        synchronized (SystemMessageActivity.class) {
////                            getData().clear();
////                            getData().addAll(MessageManager.getInstance().getMessageslist());
//                            notifyDataSetChanged();
//                        }
//                    } else if (errorResult.result == SdvnHttpErrorNo.EC_NEWSID_NOT_FIND) {
//                        remove(data.getMsgId());
//                    }
//                }
//                ToastUtils.showError(baseProtocol.result);
//            }
//        });
//    }
//}