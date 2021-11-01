package net.linkmate.app.ui.activity.message;//package net.linkmate.app.ui.activity.message;
//
//import android.app.Dialog;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.annotation.Nullable;
//import androidx.lifecycle.ViewModelProviders;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.chad.library.adapter.base.BaseQuickAdapter;
//
//import net.linkmate.app.R;
//import net.linkmate.app.base.BaseActivity;
//import net.linkmate.app.manager.MessageManager;
//import net.linkmate.app.ui.viewmodel.SystemMessageViewModel;
//import net.linkmate.app.util.DialogUtil;
//import net.linkmate.app.view.adapter.SystemMsgRVAdapter;
//import net.sdvn.common.internet.core.HttpLoader;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import io.reactivex.disposables.Disposable;
//import io.weline.internetdb.vo.SdvnMessageModel;
//
//public class SystemMessageActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
//    @BindView(R.id.itb_iv_left)
//    ImageView ivLeft;
//    @BindView(R.id.itb_tv_title)
//    TextView tvTitle;
//    @BindView(R.id.itb_iv_right)
//    ImageView ivRight;
//    @BindView(R.id.itb_rl)
//    RelativeLayout rlTitle;
//    @BindView(R.id.system_msg_rv)
//    RecyclerView mRv;
//    @BindView(R.id.system_msg_srl)
//    SwipeRefreshLayout mSrl;
//    private SystemMsgRVAdapter mAdapter;
//    private List<SdvnMessageModel> mMessages = new ArrayList<>();
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_system_message);
//        tvTitle.setText(R.string.system_msg);
//        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
//        ivLeft.setVisibility(View.VISIBLE);
//        ivLeft.setImageResource(R.drawable.icon_return);
//        ivRight.setImageResource(R.drawable.edit);
//        initRv();
//        getMessage();
//        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getMessage();
//            }
//        });
//        SystemMessageViewModel systemMessageViewModel = ViewModelProviders.of(this).get(SystemMessageViewModel.class);
//        systemMessageViewModel
//                .getMessagesLiveData()
//                .observe(this, this::onMessagesListChanged);
//    }
//
//    @Override
//    protected View getTopView() {
//        return rlTitle;
//    }
//
//    private void initRv() {
//        mRv.setLayoutManager(new LinearLayoutManager(this));
////        mMessages.addAll(MessageManager.getInstance().getMessageslist());
//        ivRight.setVisibility(mMessages.isEmpty() ? View.GONE : View.VISIBLE);
//        mAdapter = new SystemMsgRVAdapter(SystemMessageActivity.this, mMessages);
//        View emptyView = LayoutInflater.from(this).inflate(R.layout.pager_empty_text, null);
//        ((TextView) emptyView.findViewById(R.id.tv_tips)).setText(R.string.no_msg);
//        mAdapter.setEmptyView(emptyView);
//        mRv.setAdapter(mAdapter);
//        mAdapter.setOnItemChildLongClickListener(new BaseQuickAdapter.OnItemChildLongClickListener() {
//            @Override
//            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
//                switch (view.getId()) {
//                    case R.id.rl_msg_bg:
//                        if (!mAdapter.getEditMode()) {
//                            final SdvnMessageModel item = mAdapter.getItem(position);
//                            if (item != null) {
//                                item.setSelect(true);
//                                mAdapter.editMode(true);
//                                ivRight.setImageResource(R.drawable.icon_remove);
//                                mAdapter.notifyDataSetChanged();
//                                return true;
//                            }
//                        }
//                        break;
//                }
//                return false;
//            }
//        });
//    }
//
//    private void getMessage() {
//        MessageManager.getInstance().refreshMessage(null, this);
//    }
//
//    @OnClick({R.id.itb_iv_left, R.id.itb_iv_right})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.itb_iv_left:
//                onBackPressed();
//                break;
//            case R.id.itb_iv_right:
//                if (!mSrl.isRefreshing()) {
//                    if (mAdapter.getEditMode()) {
//                        if (hasSelect()) {
//                            DialogUtil.showSelectDialog(this, getString(R.string.delete_this_selected_msg),
//                                    getString(R.string.ok), new DialogUtil.OnDialogButtonClickListener() {
//                                        @Override
//                                        public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
//                                            ivRight.setImageResource(R.drawable.edit);
//                                            mAdapter.removeSelect();
//                                            dialog.dismiss();
//                                        }
//                                    },
//                                    getString(R.string.cancel), new DialogUtil.OnDialogButtonClickListener() {
//                                        @Override
//                                        public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
//                                            mAdapter.editMode(false);
//                                            ivRight.setImageResource(R.drawable.edit);
//                                            mAdapter.notifyDataSetChanged();
//                                            dialog.dismiss();
//                                        }
//                                    });
//                        } else {
//                            ivRight.setImageResource(R.drawable.edit);
//                            mAdapter.removeSelect();
//                        }
//                    } else {
//                        mAdapter.editMode(true);
//                        ivRight.setImageResource(R.drawable.icon_remove);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                }
//                break;
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (mAdapter.getEditMode()) {
//            mAdapter.editMode(false);
//            ivRight.setImageResource(R.drawable.edit);
//            mAdapter.notifyDataSetChanged();
//        } else {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
////        MessageManager.getInstance().clearMessagesCount();
//        super.onDestroy();
//    }
//
//    @Override
//    public void onLoadStart(Disposable disposable) {
//        if (mSrl != null && !mSrl.isRefreshing()) {
//            mSrl.setRefreshing(true);
//        }
//        addDisposable(disposable);
//    }
//
//    @Override
//    public void onLoadComplete() {
//        if (mSrl != null && mSrl.isRefreshing()) {
//            mSrl.setRefreshing(false);
//        }
//    }
//
//    @Override
//    public void onLoadError() {
//        if (mSrl != null && mSrl.isRefreshing()) {
//            mSrl.setRefreshing(false);
//        }
//    }
//
//    private boolean hasSelect() {
//        for (SdvnMessageModel mMessage : mMessages) {
//            if (mMessage.isSelect()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void onMessagesListChanged(List<SdvnMessageModel> messages) {
//        mMessages.clear();
//        if (messages != null)
//            mMessages.addAll(messages);
//        ivRight.setVisibility(mMessages.isEmpty() ? View.GONE : View.VISIBLE);
//        mAdapter.setNewData(mMessages);
//    }
//}
