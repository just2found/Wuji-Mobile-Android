package net.sdvn.nascommon.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.FileTypeItem;
import net.sdvn.nascommon.model.oneos.OneOSHardDisk;
import net.sdvn.nascommon.model.oneos.api.sys.OneOSSpaceAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.model.phone.LocalFileType;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;

public class SelectMangeTypeView extends RelativeLayout {
    @NonNull
    private ArrayList<FileTypeItem> itemList = new ArrayList<>();
    private Context context;
    private GridView mGridView;
    private View mBackLayout;
    private Animation mInAnim, mOutAnim;
    private ImageView mClose;
    private TextView mUserSpaceTv;
    private boolean isShow;

    public SelectMangeTypeView(Context context) {
        super(context);
    }

    public SelectMangeTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        FileTypeItem picItem = new FileTypeItem(R.string.file_type_pic, R.drawable.ic_pop_pic, R.drawable.ic_pop_pic, LocalFileType.PICTURE);
        itemList.add(picItem);
        FileTypeItem videoItem = new FileTypeItem(R.string.file_type_video, R.drawable.ic_pop_video, R.drawable.ic_pop_video, LocalFileType.VIDEO);
        itemList.add(videoItem);
        FileTypeItem audioItem = new FileTypeItem(R.string.file_type_audio, R.drawable.ic_pop_audio, R.drawable.ic_pop_audio, LocalFileType.AUDIO);
        itemList.add(audioItem);
        FileTypeItem docItem = new FileTypeItem(R.string.file_type_doc, R.drawable.ic_pop_word, R.drawable.ic_pop_word, LocalFileType.DOC);
        itemList.add(docItem);
        FileTypeItem privateItem = new FileTypeItem(R.string.file_type_all, R.drawable.ic_pop_folder, R.drawable.ic_pop_folder, LocalFileType.PRIVATE);
        itemList.add(privateItem);
        FileTypeItem appItem = new FileTypeItem(R.string.action_new_folder, R.drawable.ic_pop_new, R.drawable.ic_pop_new, LocalFileType.NEW_FOLDER);
        itemList.add(appItem);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_manage_type, this, true);
        mBackLayout = view.findViewById(R.id.layout_menu);
        mBackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mGridView = view.findViewById(R.id.gridview);
        mGridView.setAdapter(new PopupMenuAdapter());
        mGridView.setFocusableInTouchMode(true);
        mGridView.setFocusable(true);

        mClose = findViewById(R.id.iv_close);
        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mInAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);
        mInAnim.setDuration(400);

        mOutAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_to_bottom);
        mOutAnim.setDuration(400);

        mUserSpaceTv = findViewById(R.id.tv_user_space);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mGridView.setOnItemClickListener(listener);
    }

    public boolean isShow() {
        return isShow;
    }

    public void dismiss() {
        if (isShow) {
            isShow = false;
            mBackLayout.startAnimation(mOutAnim);
            mBackLayout.setVisibility(INVISIBLE);
            showStatusChangeListener.onHide();
        }
    }

    public void showTypeView(LoginSession loginSession) {
        if (!isShow) {
            isShow = true;
            getUserSpace(loginSession);
            mBackLayout.startAnimation(mInAnim);
            mBackLayout.setVisibility(VISIBLE);
            showStatusChangeListener.onShow();
        }
    }

    public void setShowStatusChangeListener(ShowStatusChangeListener listener) {
        showStatusChangeListener = listener;
    }

    private ShowStatusChangeListener showStatusChangeListener;

    public interface ShowStatusChangeListener {
        void onShow();

        void onHide();
    }

    private final class PopupMenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Nullable
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_popup, null);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.mTypeTxt = convertView.findViewById(R.id.txt_type);
                holder.mIconImageView = convertView.findViewById(R.id.iv_icon);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FileTypeItem item = itemList.get(position);
            holder.mTypeTxt.setText(item.getTitle());
            Resources resources = context.getResources();
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_selected}, resources.getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{android.R.attr.state_pressed}, resources.getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{}, resources.getDrawable(item.getNormalIcon()));
            holder.mIconImageView.setImageDrawable(drawable);

            return convertView;
        }

        private final class ViewHolder {
            ImageView mIconImageView;
            TextView mTypeTxt;
        }
    }

    private void getUserSpace(LoginSession loginSession) {
        if (loginSession!=null&&loginSession.isLogin()) {
            OneOSSpaceAPI spaceAPI = new OneOSSpaceAPI(loginSession);
            spaceAPI.setOnSpaceListener(new OneOSSpaceAPI.OnSpaceListener() {
                @Override
                public void onStart(String url) {

                }

                @Override
                public void onSuccess(String url, boolean isOneOSSpace, @NonNull OneOSHardDisk hd1, OneOSHardDisk hd2) {
                    long total = hd1.getTotal();
                    long used = hd1.getUsed();
                    if (total == 0) {
                        mUserSpaceTv.setVisibility(INVISIBLE);
                    } else {
                        mUserSpaceTv.setVisibility(VISIBLE);
                        String space = FileUtils.fmtFileSize(total - used);
                        mUserSpaceTv.setText(getResources().getString(R.string.tv_user_space) + " " + space);
                    }
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {

                }
            });

            spaceAPI.query(false);
        }
    }
}
