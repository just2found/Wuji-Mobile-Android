package net.sdvn.nascommon.model.phone.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;

public class LocalFileBaseAdapter extends BaseAdapter {
    private static final String TAG = LocalFileBaseAdapter.class.getSimpleName();

    public LayoutInflater mInflater;
    public Context context;
    @Nullable
    public List<LocalFile> mFileList = null;
    @Nullable
    public ArrayList<LocalFile> mSelectedList = null;
    private boolean isMultiChoose = false;
    @Nullable
    public OnMultiChooseClickListener mListener = null;

    public LocalFileBaseAdapter(Context context, List<LocalFile> fileList, ArrayList<LocalFile> selectedList, OnMultiChooseClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mListener = listener;
        this.mFileList = fileList;
        this.mSelectedList = selectedList;
        clearSelectedList();
    }

    /**
     * init Selected Map
     */
    private void clearSelectedList() {
        if (mSelectedList == null) {
            Logger.LOGE(TAG, "Selected List is NULL");
            return;
        }
        mSelectedList.clear();
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void notifyDataSetChanged(boolean addItem) {
        if (addItem) {
            clearSelectedList();
        }

        notifyDataSetChanged();
    }

    public void setIsMultiModel(boolean isMulti) {
        if (this.isMultiChoose != isMulti) {
            this.isMultiChoose = isMulti;
            if (isMulti) {
                clearSelectedList();
            }
            notifyDataSetChanged();
        }
    }

    public boolean isMultiChooseModel() {
        return this.isMultiChoose;
    }

    @Nullable
    public ArrayList<LocalFile> getSelectedList() {
        if (isMultiChooseModel()) {
            return mSelectedList;
        }

        return null;
    }

    public int getSelectedCount() {
        int count = 0;
        if (isMultiChoose && null != mSelectedList) {
            count = mSelectedList.size();
        }

        return count;
    }

    public void selectAllItem(boolean isSelectAll) {
        if (isMultiChoose && null != mSelectedList) {
            mSelectedList.clear();
            if (isSelectAll) {
                mSelectedList.addAll(mFileList);
            }
        }
    }

    public void selectAllFile(boolean isSelectAll) {
        if (isMultiChoose && null != mSelectedList) {
            mSelectedList.clear();
            if (isSelectAll) {
                for (int i = 0; i < mFileList.size(); i++) {
                    LocalFile file = mFileList.get(i);
                    if (!file.isDirectory()) {
                        mSelectedList.add(file);
                    }
                }
//                mSelectedList.addAll(mFileList);
            }
        }
    }

    public void showFileIcon(@NonNull ImageView imageView, @NonNull LocalFile file) {
        //                ((SimpleDraweeView) imageView).setImageURI(Uri.fromFile(file.getFile()));
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(file.getFile()))
//                .setResizeOptions(new ResizeOptions(200, 200))
//                .build();
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setImageRequest(request)
//                .setAutoPlayAnimations(true)
//                .setOldController(((SimpleDraweeView) imageView).getController())
//                .setControllerListener(new BaseControllerListener<ImageInfo>())
//                .build();
        if (FileUtils.isGifFile(file.getName())) {
            if (AppConstants.DISPLAY_IMAGE_WITH_GLIDE) {
                //imageView.setTag(null);
                Glide.with(context)
                        .load(Uri.fromFile(file.getFile()))
                        .apply(new RequestOptions().error(R.drawable.icon_file_pic_default))
                        .into(imageView);
//                ((SimpleDraweeView)imageView).setController(controller);
            } else {
//                HttpBitmap.getInstance().display(imageView, file.getPath());
            }
        } else if (FileUtils.isPictureFile(file.getName())) {
            if (AppConstants.DISPLAY_IMAGE_WITH_GLIDE) {
                //imageView.setTag(null);
                Glide.with(context)
                        .load(Uri.fromFile(file.getFile()))
                        .apply(new RequestOptions().error(R.drawable.icon_file_pic_default).centerCrop())
                        .into(imageView);
//                ((SimpleDraweeView) imageView).setImageURI(Uri.fromFile(file.getFile()));
//                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(file.getFile()))
//                        .setResizeOptions(new ResizeOptions(200, 200))
//                        .build();
//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setImageRequest(request)
//                        .setOldController(((SimpleDraweeView) imageView).getController())
//                        .setControllerListener(new BaseControllerListener<ImageInfo>())
//                        .build();
//                ((SimpleDraweeView) imageView).setController(controller);
            } else {
//                HttpBitmap.getInstance().display(imageView, file.getPath());
            }
        } else {
            if (AppConstants.DISPLAY_IMAGE_WITH_GLIDE && FileUtils.isVideoFile(file.getName())) {
                //imageView.setTag(null);
                Glide.with(context)
                        .load(Uri.fromFile(file.getFile()))
                        .apply(new RequestOptions().error(R.drawable.icon_file_video))
                        .into(imageView);
//                ((SimpleDraweeView) imageView).setImageURI(Uri.fromFile(file.getFile()));
//                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(file.getFile()))
//                        .setResizeOptions(new ResizeOptions(200, 200))
//                        .build();
//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setImageRequest(request)
//                        .setOldController(((SimpleDraweeView) imageView).getController())
//                        .setControllerListener(new BaseControllerListener<ImageInfo>())
//                        .build();
//                ((SimpleDraweeView) imageView).setController(controller);
            } else {
                int icon;
                if (file.isDirectory()) {
                    if (file.isDownloadDir()) {
                        icon = R.drawable.icon_file_folder_download;
                    } else if (file.isBackupDir()) {
                        icon = R.drawable.icon_file_folder_backup;
                    } else {
                        icon = R.drawable.icon_file_folder;
                    }
                } else {
                    icon = FileUtils.fmtFileIcon(file.getName());
                }
                imageView.setImageResource(icon);
            }
        }
    }

    public interface OnMultiChooseClickListener {
        void onClick(View view);
    }
}
