package net.sdvn.nascommon.model.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl;
import net.sdvn.nascommon.model.oneos.OneOSFileType;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferControlListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferElement;
import net.sdvn.nascommon.model.oneos.transfer.TransferException;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.MIMETypeUtils;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.util.List;

public class QuickTransmissionAdapter extends BaseQuickAdapter<TransferElement, QuickTransmissionAdapter.ViewHolder> {

    private static final String TAG = QuickTransmissionAdapter.class.getSimpleName();
    private Context context;

    private boolean isDownload;
    private OnTransferControlListener mListener;
    private boolean debug = false;


    public QuickTransmissionAdapter(Context context) {
        super(R.layout.item_transfer);
        this.context = context;
    }

    public void setTransferList(List<TransferElement> list, boolean isDownload) {
        this.isDownload = isDownload;
        setNewData(list);
    }


    @Override
    protected void convert(@NonNull ViewHolder helper, TransferElement item) {
        helper.bindData(item);
    }


    public class ViewHolder extends BaseViewHolder {
        //        ViewGroup leftLayout;
//        ViewGroup rightLayout;
        ImageView fileIcon;
        TextView fileName;
        //        TextView fileRatio;
        TextView fileSize;
        ProgressBar circleProgress;
        //        TextView deleteTxt;
        TextView fileState;
        @Nullable
        private TransferElement mElement;
        @Nullable
        private TransferElement.TransferStateObserver transferStateObserver;
        @Nullable
        private View.OnClickListener onClickListener;

        public ViewHolder(@NonNull View convertView) {
            super(convertView);
//            this.leftLayout = convertView.findViewById(R.id.layout_power_off);
//            this.rightLayout = convertView.findViewById(R.id.layout_right);
            this.fileIcon = convertView.findViewById(R.id.fileImage);
            this.fileName = convertView.findViewById(R.id.fileName);
            this.fileSize = convertView.findViewById(R.id.fileSize);
//            this.fileRatio = convertView.findViewById(R.id.ratio);
            this.circleProgress = convertView.findViewById(R.id.progress);
            this.circleProgress = convertView.findViewById(R.id.progress);
//            this.deleteTxt = convertView.findViewById(R.id.txt_delete);
            this.fileState = convertView.findViewById(R.id.file_state);
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        private void bindData(final TransferElement element) {
            if (this.mElement != null) {
                mElement.removeTransferStateObserver(transferStateObserver);
                mElement = null;
            }

            this.mElement = element;
            if (mElement != null) {
                transferStateObserver = new TransferElement.TransferStateObserver() {
                    @SuppressLint({"SetTextI18n", "DefaultLocale"})
                    @Override
                    public void onChanged(Object tag) {
                        if (itemView != null)
                            itemView.post(new Runnable() {
                                @Override
                                public void run() {
                                    refreshUI(ViewHolder.this, mElement);
                                }
                            });
                    }

                };

                mElement.addTransferStateObserver(transferStateObserver);
                if (onClickListener == null)
                    onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v == itemView) {
                                if (mElement != null) {
                                    TransferState state = mElement.getState();
                                    if (state == TransferState.PAUSE) {
                                        if (mListener != null) {
                                            mListener.onContinue(mElement);
                                        }
                                    } else if (state == TransferState.START) {
                                        if (mListener != null) {
                                            mListener.onPause(mElement);
                                        }
                                    } else if (state == TransferState.FAILED) {
                                        if (mListener != null) {
                                            mListener.onRestart(mElement);
                                        }
                                    } else if (state == TransferState.WAIT) {
                                        if (mListener != null) {
                                            mListener.onPause(mElement);
                                        }
                                    }
                                }
                            } /*else if (v == deleteTxt) {
                                if (mListener != null) {
                                    mListener.onCancel(mElement);
                                }
                            }*/
                        }

                    };
                itemView.setOnClickListener(onClickListener);
//                deleteTxt.setOnClickListener(onClickListener);
            }


            refreshUI(this, mElement);

        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void refreshUI(@NonNull ViewHolder holder, @Nullable TransferElement mElement) {
        if (mElement != null) {
            Context context = holder.itemView.getContext();
            String name = mElement.getSrcName();
            float ratio = getLoadRatio(mElement);
            Logger.p(Logger.Level.DEBUG, debug, TAG, " ratio : " + ratio);
            holder.fileName.setText(name);
            String toPath;
            if (isDownload) {
                toPath = String.format(context.getString(R.string.download_to__), mElement.getToPath());
            } else {
                String devName = "";
                DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(mElement.getDevId());
                if (deviceModel != null) {
                    devName = deviceModel.getDevName();
                }
                devName = devName + " ";
                String pathWithTypeName = OneOSFileType.getPathWithTypeName(mElement.getToPath());
                toPath = String.format(context.getString(R.string.upload_to__), devName, pathWithTypeName);
            }
            holder.setGone(R.id.textView_path, !TextUtils.isEmpty(toPath));
            holder.setText(R.id.textView_path, null);
//            if (!TextUtils.isEmpty(toPath)) {
//            }
            final LoginSession loginSession = SessionManager.getInstance().getLoginSession(mElement.getDevId());
            if (loginSession != null && loginSession.isLogin() && (MIMETypeUtils.isImageFile(name) || MIMETypeUtils.isVideoFile(name))) {
                mElement.setThumbUri(Uri.parse(OneOSAPIs.genThumbnailUrl(loginSession, mElement.getSrcPath())));
            }
//                holder.fileIcon.setImageURI(mElement.getThumbUri());
            Glide.with(holder.fileIcon)
                    .load(mElement.getThumbUri() != null ? new EliCacheGlideUrl(mElement.getThumbUri().getPath()) : FileUtils.fmtFileIcon(name))
                    .placeholder(FileUtils.fmtFileIcon(name))
                    .into(holder.fileIcon);
            holder.fileSize.setText(FileUtils.fmtFileSize(mElement.getLength()) +
                    "/" + FileUtils.fmtFileSize(mElement.getSize()));
            //只有在进行时才显示进度
            holder.circleProgress.setProgress((int) (ratio + 0.5f));
//                holder.fileRatio.setTitleText(FileUtils.fmtFileSize(mElement.getSpeed()));
            TransferState state = mElement.getState();
            holder.circleProgress.setVisibility(View.VISIBLE);
            if (state == TransferState.PAUSE) {
//                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.PAUSE);
                if (isDownload) {
                    holder.fileState.setText(context.getResources().getString(R.string.paused));
                } else {
                    holder.fileState.setText(context.getResources().getString(R.string.paused));
                }
            } else if (state == TransferState.START) {
//                holder.fileRatio.setTitleText(String.format("%.2f", ratio) + "%");
                holder.fileState.setText(FileUtils.fmtFileSpeed(mElement.getSpeed()) + "/s");
//                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.START);
            } else if (state == TransferState.WAIT) {
//                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.WAIT);
                holder.fileState.setText(context.getResources().getString(R.string.waiting));
            } else if (state == TransferState.FAILED) {
//                holder.circleProgress.setState(CircleStateProgressBar.ProgressState.FAILED);
                holder.fileState.setText(getFailedInfo(context, mElement));
            } else if (state == TransferState.COMPLETE) {
                holder.circleProgress.setVisibility(View.GONE);
                holder.setText(R.id.textView_path, toPath);
                holder.fileState.setText("");
            }
        }
    }


    private float getLoadRatio(TransferElement mElement) {
        float cur = mElement.getLength();
        float total = mElement.getSize();
        return ((cur / total) * 100);
    }

    private String getFailedInfo(Context context, @NonNull TransferElement mElement) {
        String failedInfo = null;

        if (!Utils.isWifiAvailable(context) && SPUtils.getBoolean(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
            mElement.setException(TransferException.WIFI_UNAVAILABLE);
        }

        TransferException failedId = mElement.getException();
        if (failedId == TransferException.NONE) {
            return null;
        } else if (failedId == TransferException.LOCAL_SPACE_INSUFFICIENT) {
            failedInfo = context.getResources().getString(R.string.local_space_insufficient);
        } else if (failedId == TransferException.SERVER_SPACE_INSUFFICIENT) {
            failedInfo = context.getResources().getString(R.string.server_space_insufficient);
        } else if (failedId == TransferException.FAILED_REQUEST_SERVER) {
            failedInfo = context.getResources().getString(R.string.request_server_exception);
        } else if (failedId == TransferException.ENCODING_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.decoding_exception);
        } else if (failedId == TransferException.IO_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.io_exception);
        } else if (failedId == TransferException.FILE_NOT_FOUND) {
            if (isDownload) {
                failedInfo = context.getResources().getString(R.string.touch_file_failed);
            } else {
                failedInfo = context.getResources().getString(R.string.file_not_found);
            }
        } else if (failedId == TransferException.SERVER_FILE_NOT_FOUND) {
            failedInfo = context.getResources().getString(R.string.file_not_found);
        } else if (failedId == TransferException.UNKNOWN_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.unknown_exception);
        } else if (failedId == TransferException.SOCKET_TIMEOUT) {
            failedInfo = context.getResources().getString(R.string.socket_timeout);
        } else if (failedId == TransferException.WIFI_UNAVAILABLE) {
            failedInfo = context.getResources().getString(R.string.wifi_connect_break);
        }

        return failedInfo;
    }

    public void setOnControlListener(OnTransferControlListener mListener) {
        this.mListener = mListener;
    }
}
