package net.sdvn.nascommon.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.PathTypeCompat;
import net.sdvn.nascommon.model.oneos.OneOSFileType;
import net.sdvn.nascommon.model.phone.LocalFileType;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.io.File;

public class FilePathPanel extends RelativeLayout {
    private static final String TAG = FilePathPanel.class.getSimpleName();
    private Context mContext;
    private LinearLayout mPathLayout;
    //    private View mRightLineView, mLeftLineView;
    private ImageButton mNewFolderBtn, mOrderBtn;
    private OnPathPanelClickListener mListener;

    @Nullable
    private String path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
    @Nullable
    private String mSDCardRootDirShownName = null;
    @Nullable
    private String mDownloadRootDirShownName = null;
    @Nullable
    private String mPrivateRootDirShownName = null;
    @Nullable
    private String mPublicRootDirShownName = null;
    @Nullable
    private String mRecycleRootDirShownName = null;
    @Nullable
    private String mExtenalStorageRootDirShownName = null;
    @Nullable
    private String mSafeBoxDirShownName = null;

    @Nullable
    private String mGroupDirShownName = null;

    public void setGroupDirShownName(@Nullable String groupDirShownName) {
        if (!TextUtils.isEmpty(groupDirShownName)) {
            this.mGroupDirShownName = groupDirShownName;
        }
    }

    @Nullable
    private String mPrefixName = null;
    private int pathMaxWidth = 0, pathMinWidth = 0, pathBtnPadding = 0;

    public FilePathPanel(@NonNull Context context) {
        super(context);
    }

    public FilePathPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_path_panel, this, true);

        mPrivateRootDirShownName = getResources().getString(R.string.root_dir_name_private);
        mPublicRootDirShownName = getResources().getString(R.string.root_dir_name_public);
        mRecycleRootDirShownName = getResources().getString(R.string.root_dir_name_recycle);
        mSDCardRootDirShownName = getResources().getString(R.string.root_dir_name_sdcard);
        mDownloadRootDirShownName = getResources().getString(R.string.root_dir_name_download);
        mExtenalStorageRootDirShownName = getResources().getString(R.string.external_storage);
        mGroupDirShownName = getResources().getString(R.string.group);
        mSafeBoxDirShownName = getResources().getString(R.string.root_dir_name_safe_box);
        pathMaxWidth = dipToPx(120);
        pathMinWidth = dipToPx(30);
        pathBtnPadding = dipToPx(5);

        mPathLayout = view.findViewById(R.id.layout_file_path);
//      mRightLineView = findViewById(R.id.view_path_mid_line);
        mNewFolderBtn = findViewById(R.id.ibtn_new_folder);

        mNewFolderBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onClick(v, null);
                }
            }
        });
//        mLeftLineView = findViewById(R.id.view_order_line);
        mOrderBtn = findViewById(R.id.ibtn_order);
        mOrderBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onClick(v, null);
                }
            }
        });
        setMinimumHeight(dipToPx(getContext().getResources().getDimension(R.dimen.layout_path_height)));
    }

    public int dipToPx(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * for Local File Path
     *
     * @param path
     * @param rootPath
     */
    public void updatePath(LocalFileType type, String path, String rootPath) {
        this.path = path;
        setVisibility(View.VISIBLE);
        genFilePathLayout(type, rootPath);
    }

    public void updatePath(OneOSFileType type, String path) {
        this.type = type;
        this.path = path;
        if (EmptyUtils.isEmpty(this.path)) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            genFilePathLayout(type);
        }
    }

    public int getSharePathType() {
        if (type != null) {
            return PathTypeCompat.getSharePathType(type).getType();
        }
        return 0;
    }

    private OneOSFileType type = null;

    public void updatePath(OneOSFileType type, String path, String prefixName) {
        this.path = path;
        this.mPrefixName = prefixName;
        this.type = type;
        if (EmptyUtils.isEmpty(this.path) && EmptyUtils.isEmpty(this.mPrefixName)) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            genFilePathLayout(type);
        }
    }

    String rootShownName;

    public void updatePath(String rootShownName, @Nullable String path) {
        this.rootShownName = rootShownName;
        this.path = path;
        if (path == null) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            genFilePathLayout(null);
        }
    }

    public void showNewFolderButton(boolean isShown) {
//        mNewFolderBtn.setVisibility(INVISIBLE);
//        mRightLineView.setVisibility(INVISIBLE);
        mNewFolderBtn.setVisibility(isShown ? View.VISIBLE : View.GONE);
//        mRightLineView.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    public void showOrderButton(boolean isShown) {
        mOrderBtn.setVisibility(isShown ? View.VISIBLE : View.GONE);
//        mLeftLineView.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    public void setOrderButtonRes(@DrawableRes int resId) {
        mOrderBtn.setImageResource(resId);
    }

    public void setNewFolderButtonRes(@DrawableRes int resId) {
        mNewFolderBtn.setImageResource(resId);
    }

    public void setOnPathPanelClickListener(OnPathPanelClickListener listener) {
        this.mListener = listener;
    }

    private void genFilePathLayout(@Nullable OneOSFileType type) {
        Logger.LOGI(TAG, "Original Path:" + path);
        mPathLayout.removeAllViews();
        boolean isConverted = false;
        final String rootStr;
        String rootShownName;
        if (type == null || path == null) {
            rootStr = "";
            rootShownName = this.rootShownName;
        } else if (type == OneOSFileType.PUBLIC) {
            rootStr = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
            rootShownName = mPublicRootDirShownName;
            if (!path.startsWith(rootStr)) {//因V5添加，没有绝对路径
                path = rootStr + path;
                isConverted = true;
            }
        } else if (type == OneOSFileType.RECYCLE) {
            rootStr = OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR;
            rootShownName = mRecycleRootDirShownName;
            if (!path.startsWith(rootStr)) {//因V5添加，没有绝对路径
                path = rootStr + path;
                isConverted = true;
            }
        } else if (type == OneOSFileType.EXTERNAL_STORAGE) {
            rootStr = OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR;
            rootShownName = mExtenalStorageRootDirShownName;
            if (!path.startsWith(rootStr)) {//因V5添加，没有绝对路径
                path = rootStr + path;
                isConverted = true;
            }
        } else if (type == OneOSFileType.SAFE) {
            rootStr = OneOSAPIs.ONE_OS_SAFE_ROOT_DIR;
            rootShownName = mSafeBoxDirShownName;
            if (!path.startsWith(rootStr)) {//因V5添加，没有绝对路径
                path = rootStr + path;
                isConverted = true;
            }
        } else if (type == OneOSFileType.GROUP) {
            rootStr = OneOSAPIs.ONE_OS_GROUP_ROOT_DIR;
            rootShownName = mGroupDirShownName;
            if (!path.startsWith(rootStr)) {//因V5添加，没有绝对路径
                path = rootStr + path;
                isConverted = true;
            }
        } else {
            rootStr = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
            rootShownName = mPrivateRootDirShownName;
        }

        try {
            final boolean hasPrefix = !EmptyUtils.isEmpty(mPrefixName);
            String shownPath;
            if (null != path) {
                if (hasPrefix) {
                    rootShownName = mPrefixName + File.separator + rootShownName;
                }

                shownPath = path.replaceFirst(rootStr, rootShownName + File.separator);
            } else {
                shownPath = mPrefixName + File.separator;
            }
            shownPath = shownPath.replaceAll(File.separator + File.separator, File.separator);

            Logger.LOGD(TAG, "Add srcPath button:" + shownPath);

            final String[] pathItems = shownPath.split(File.separator);
            int length = pathItems.length;
            TextView[] pathBtn = new TextView[length];
            Resources resource = getResources();
            ColorStateList csl = resource.getColorStateList(R.color.selector_black_to_primary);

            for (int i = 0; i < length; ++i) {
                pathBtn[i] = new TextView(getContext());
                pathBtn[i].setTag(i);
                pathBtn[i].setText(pathItems[i]);
                pathBtn[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
                pathBtn[i].setMaxWidth(pathMaxWidth);
                pathBtn[i].setMinWidth(pathMinWidth);
                pathBtn[i].setPadding(pathBtnPadding, 0, pathBtnPadding, 0);
                pathBtn[i].setSingleLine(true);
                pathBtn[i].setEllipsize(TextUtils.TruncateAt.END);
                pathBtn[i].setTextColor(csl);
                pathBtn[i].setGravity(Gravity.CENTER);
                if (i < length - 1) {
                    pathBtn[i].setBackgroundResource(R.drawable.bg_path_item);
                }
                mPathLayout.addView(pathBtn[i]);
                boolean finalIsConverted = isConverted;
                pathBtn[i].setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        int i = (Integer) v.getTag();
                        int j = 1;
                        if (hasPrefix) {
                            j++;
                            if (i == 0) {
                                if (null != mListener) {
                                    mListener.onClick(v, null);
                                }
                                return;
                            }
                        }
                        StringBuilder tarPath = new StringBuilder(rootStr);
                        for (; j <= i; j++) {
                            tarPath.append(pathItems[j]).append(File.separator);
                        }

                        Logger.LOGD(TAG, "Click target srcPath is " + tarPath);
                        if (null != mListener) {
                            String result = tarPath.toString();
                            if (finalIsConverted) {
                                result = OneOSAPIs.getV5Path(result);
                            }
                            mListener.onClick(v, result);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.getStackTrace();
            Logger.LOGE(TAG, "Generate Path Layout Exception: ", e);
        }
    }

    /**
     * for Local File Path
     *
     * @param rootPath
     */
    private void genFilePathLayout(LocalFileType type, @Nullable String rootPath) {
        Logger.LOGD(TAG, "Original Path:" + path + ", Root Path:" + rootPath);
        mPathLayout.removeAllViews();

        final String rootStr = rootPath;
        String rootShownName = type == LocalFileType.PRIVATE ? mSDCardRootDirShownName : mDownloadRootDirShownName;

        try {
            final boolean hasPrefix = !EmptyUtils.isEmpty(mPrefixName);
            String shownPath;
            if (null == path) {
                if (hasPrefix) {
                    shownPath = mPrefixName + File.separator + rootShownName;
                } else {
                    shownPath = rootShownName;
                }
            } else {
                String relativePath = path;
                if (null != rootPath) {
                    relativePath = path.replaceFirst(rootStr, "");
                }
                Logger.LOGD(TAG, "Relative Path:" + relativePath);
                if (hasPrefix) {
                    shownPath = mPrefixName + File.separator + rootShownName + relativePath;
                } else {
                    shownPath = rootShownName + relativePath;
                }
            }
            Logger.LOGD(TAG, "Add Path button:" + shownPath);

            final String[] pathItems = shownPath.split(File.separator);
            int length = pathItems.length;
            TextView[] pathBtn = new TextView[length];
            Resources resource = getResources();
            ColorStateList csl = resource.getColorStateList(R.color.selector_black_to_primary);

            for (int i = 0; i < length; ++i) {
                pathBtn[i] = new TextView(getContext());
                pathBtn[i].setTag(i);
                pathBtn[i].setText(pathItems[i]);
                pathBtn[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
                pathBtn[i].setMaxWidth(pathMaxWidth);
                pathBtn[i].setMinWidth(pathMinWidth);
                pathBtn[i].setPadding(pathBtnPadding, 0, pathBtnPadding, 0);
                pathBtn[i].setSingleLine(true);
                pathBtn[i].setEllipsize(TextUtils.TruncateAt.END);
                pathBtn[i].setTextColor(csl);
                pathBtn[i].setGravity(Gravity.CENTER);
                if (i < length - 1) {
                    pathBtn[i].setBackgroundResource(R.drawable.bg_path_item);
                }
                pathBtn[i].setAllCaps(false);
                mPathLayout.addView(pathBtn[i]);
                pathBtn[i].setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        int i = (Integer) v.getTag();
                        int j = 1;
                        if (hasPrefix) {
                            j++;
                            if (i == 0) {
                                if (null != mListener) {
                                    mListener.onClick(v, null);
                                }
                                return;
                            }
                        }
                        StringBuilder tarPath = new StringBuilder();
                        if (null != rootStr) {
                            tarPath.append(rootStr).append(File.separator);
                        }

                        for (; j <= i; j++) {
                            tarPath.append(pathItems[j]).append(File.separator);
                        }

                        Logger.LOGD(TAG, "Click target path is " + tarPath);
                        if (null != mListener) {
                            mListener.onClick(v, tarPath.toString());
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.getStackTrace();
            Logger.LOGE(TAG, "Generate Path Layout Exception: ", e);
        }
    }

    public interface OnPathPanelClickListener {
        void onClick(View view,@Nullable String path);
    }
}
