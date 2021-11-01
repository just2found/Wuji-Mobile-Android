package net.linkmate.app.ui.activity.mine;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.TipsBar;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.DeviceSettingsKeeper;
import net.sdvn.nascommon.db.objecbox.DeviceSettings;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.model.phone.media.AlbumFile;
import net.sdvn.nascommon.model.phone.media.AlbumFolder;
import net.sdvn.nascommon.model.phone.media.MediaReadTask;
import net.sdvn.nascommon.model.phone.media.MediaReader;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.PermissionChecker;
import net.sdvn.nascommon.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChoiceAlbumActivity extends BaseActivity {
    private RelativeLayout rlTitle;
    private ImageView itbIvLeft;
    private TextView itbTvTitle;
    private Set<String> mCheckedFolders;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BaseQuickAdapter<AlbumFolder, BaseViewHolder> baseQuickAdapter;
    private String deviceId;
    @Nullable
    private MediaReadTask mediaReadTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_album);
        initView();
        initData();
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    private void initData() {
        mCheckedFolders = new ArraySet<>();
        Intent intent = getIntent();
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
        if (deviceId != null) {
            DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
            final List<String> backupAlbumPaths;
            if (settings != null) {
                backupAlbumPaths = settings.getBackupAlbumPaths();
                if (backupAlbumPaths != null) {
                    mCheckedFolders.addAll(backupAlbumPaths);
                }
            }
        }
//        final String json = SPHelper.get(AppConstants.SP_FIELD_CHOICE_BACKUP_ALBUM_PATHS, "[]");
//        final List<String> list = GsonUtils.decodeJSON(json, new TypeToken<List<String>>() {
//        }.getType());
//
//        if (list != null) {
//            mCheckedFolders.addAll(list);
//        }
        queryAlbums();
    }

    private void queryAlbums() {
        PermissionChecker.checkPermission(this,strings -> {
            if (mediaReadTask == null || mediaReadTask.getStatus() == AsyncTask.Status.FINISHED) {
                final MediaReader mediaReader = new MediaReader(this, null, null, null, false);
                mediaReadTask = new MediaReadTask(MediaReadTask.FUNCTION_CHOICE_ALBUM, null, mediaReader, new MediaReadTask.Callback() {
                    @Override
                    public void onScanCallback(@NonNull ArrayList<AlbumFolder> albumFolders, ArrayList<AlbumFile> checkedFiles) {
                        final Iterator<AlbumFolder> iterator = albumFolders.iterator();
                        while (iterator.hasNext()) {
                            AlbumFolder item = iterator.next();
                            if (TextUtils.isEmpty(item.getPath())) {
                                iterator.remove();
                            } else {
                                item.setChecked(mCheckedFolders.contains(item.getPath()));
                            }
                        }

                        if (baseQuickAdapter != null) {
                            baseQuickAdapter.setNewData(albumFolders);
                        }
                        if (mSwipeRefreshLayout != null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
            }
            if (mediaReadTask.getStatus() == AsyncTask.Status.PENDING) {
                mediaReadTask.execute();
                mSwipeRefreshLayout.setRefreshing(true);
            }
        },strings -> {
            UiUtils.showStorageSettings(ChoiceAlbumActivity.this);
        },Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void initView() {
        rlTitle = findViewById(R.id.itb_rl);
        itbIvLeft = findViewById(R.id.itb_iv_left);
        itbTvTitle = findViewById(R.id.itb_tv_title);
        itbTvTitle.setText(R.string.select_backup_album_folder);
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);
        itbIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mRecyclerView = findViewById(R.id.recycle_view);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryAlbums();
            }
        });
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick(v)) return;
                final List<String> checkedFolders = new ArrayList<>(mCheckedFolders);
                final Iterator<String> iterator = checkedFolders.iterator();
                while (iterator.hasNext()) {
                    final String next = iterator.next();
                    for (String checkedFolder : checkedFolders) {
                        if (next.equals(checkedFolder)) continue;
                        if (next.startsWith(checkedFolder)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
//                SPHelper.put(AppConstants.SP_FIELD_CHOICE_BACKUP_ALBUM_PATHS, json);
                if (deviceId != null) {
                    DeviceSettings settings = DeviceSettingsKeeper.getSettings(deviceId);
                    if (settings != null) {
                        settings.setBackupAlbumPaths(checkedFolders);
                        DeviceSettingsKeeper.update(settings);
                    }
                }
                Intent intent = new Intent();
                ChoiceAlbumActivity.this.setResult(Activity.RESULT_OK, intent);
                ToastUtils.showToast(R.string.setting_success);
                finish();
            }
        });
        final LinearLayoutManager layout = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), layout.getOrientation()));
        baseQuickAdapter = new BaseQuickAdapter<AlbumFolder, BaseViewHolder>(R.layout.item_album_view) {
            @Override
            protected void convert(@NonNull BaseViewHolder helper, @NonNull final AlbumFolder item) {
                helper.setText(R.id.tv_name, item.getName());
                helper.setChecked(R.id.cb_choice, item.isChecked());
                final AlbumFile albumFile = item.getAlbumFiles().get(0);
                File file;
                final String thumbPath = albumFile.getThumbPath();
                if (!TextUtils.isEmpty(thumbPath)) {
                    file = new File(thumbPath);
                } else {
                    final String path = albumFile.getPath();
                    file = new File(path);
                }
                Uri url = Uri.fromFile(file);
                Glide.with(ChoiceAlbumActivity.this)
                        .load(url)
                        .apply(new RequestOptions()
                                //加载失败的图片
                                .error(R.drawable.icon_file_pic_default)
                                //缓存图片
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                //加载完成之前的占位图
                                .placeholder(R.drawable.icon_file_pic_default)
                                //居中模式
                                .centerCrop())
                        .into((ImageView) helper.getView(R.id.iv_icon));
                helper.addOnClickListener(R.id.cb_choice);

            }
        };
        baseQuickAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, View view, int position) {
                choiceItem(adapter, position);
            }
        });
        baseQuickAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (view.getId() == R.id.cb_choice) {
                    choiceItem(adapter, position);
                }
            }
        });
        mRecyclerView.setAdapter(baseQuickAdapter);

    }

    private void choiceItem(BaseQuickAdapter adapter, int position) {
        final AlbumFolder o = (AlbumFolder) adapter.getData().get(position);
        boolean checked = o.isChecked();
        o.setChecked(!checked);
        if (!checked) {
            mCheckedFolders.add(o.getPath());
        } else {
            mCheckedFolders.remove(o.getPath());
        }
        String json = GsonUtils.encodeJSONCatchEx(mCheckedFolders);
        adapter.notifyItemChanged(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaReadTask != null) {
            mediaReadTask.cancel(true);
        }
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }
}
