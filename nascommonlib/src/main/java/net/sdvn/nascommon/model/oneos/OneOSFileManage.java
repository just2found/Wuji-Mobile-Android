package net.sdvn.nascommon.model.oneos;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;
import com.rxjava.rxlife.RxLife;

import net.sdvn.cmapi.util.LogUtils;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.InviteUserHttpLoader;
import net.sdvn.common.internet.protocol.InviteUserResultBean;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.nascommon.LibApp;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.db.objecbox.ShareElementV2;
import net.sdvn.nascommon.fileserver.FileShareBaseResult;
import net.sdvn.nascommon.fileserver.constants.EntityType;
import net.sdvn.nascommon.fileserver.constants.FS_Config;
import net.sdvn.nascommon.fileserver.constants.FileServerErrorCode;
import net.sdvn.nascommon.fileserver.constants.SharePathType;
import net.sdvn.nascommon.fileserver.data.DataCreate;
import net.sdvn.nascommon.iface.Callback;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.iface.ILoadingCallback;
import net.sdvn.nascommon.iface.LoadingCallback;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.FileListChangeObserver;
import net.sdvn.nascommon.model.FileManageAction;
import net.sdvn.nascommon.model.KeyPair;
import net.sdvn.nascommon.model.MsgGenerator;
import net.sdvn.nascommon.model.PathTypeCompat;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.model.adapter.QuickTransmissionAdapter;
import net.sdvn.nascommon.model.contacts.InviteCallBack;
import net.sdvn.nascommon.model.contacts.SortModel;
import net.sdvn.nascommon.model.oneos.api.file.OneOSFileManageAPI;
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI;
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferControlListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferElement;
import net.sdvn.nascommon.model.oneos.transfer.TransferManager;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.receiver.NetworkStateManager;
import net.sdvn.nascommon.repository.NasRepository;
import net.sdvn.nascommon.service.NasService;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.InputMethodUtils;
import net.sdvn.nascommon.utils.MIMETypeUtils;
import net.sdvn.nascommon.utils.PermissionChecker;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.SmsUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommon.viewmodel.DownloadFileViewModel;
import net.sdvn.nascommon.viewmodel.ShareViewModel2;
import net.sdvn.nascommon.viewmodel.UserModel;
import net.sdvn.nascommon.widget.CheckableImageButton;
import net.sdvn.nascommon.widget.InvitePopupView;
import net.sdvn.nascommon.widget.ServerFileTreeView;
import net.sdvn.nascommon.widget.SharePopupView;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.view.libwidget.badgeview.DisplayUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.weline.repo.SessionCache;
import io.weline.repo.api.TagCMD;
import io.weline.repo.api.V5HttpErrorNoKt;
import io.weline.repo.data.model.BaseProtocol;
import io.weline.repo.data.model.Error;
import io.weline.repo.data.model.OptTagFileError;
import io.weline.repo.data.model.TagFileError;
import io.weline.repo.net.V5Observer;
import io.weline.repo.repository.V5Repository;
import libs.source.common.AppExecutors;
import libs.source.common.livedata.Resource;
import libs.source.common.livedata.Status;
import timber.log.Timber;

import static net.sdvn.nascommon.widget.DeviceSelectDialogKt.SELF;


/**
 * Class for management OneSpace File
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/21.
 */
public class OneOSFileManage {
    private static final String TAG = OneOSFileManage.class.getSimpleName();
    private final ILoadingCallback loadingCallback;

    private FragmentActivity mActivity;
    private LoginSession loginSession;
    private Long mGroupId;
    private String mDevId;
    @Nullable
    private FileManageAction action;
    private View mRootView;
    private OnManageCallback callback;
    private List<OneOSFile> fileList;
    private OneOSFileManageAPI fileManageAPI;
    @NonNull
    private ArrayList<OneOSUser> mUserList = new ArrayList<>();


    private OneOSFileManageAPI.OnFileManageListener mListener = new OneOSFileManageAPI.OnFileManageListener() {
        @Override
        public void onStart(String url, FileManageAction action) {
            showLoading(action);
        }

        @Override
        public void onSuccess(String url, FileManageAction action, String response) {
            showSuccess(action, response, null);
        }

        @Override
        public void onFailure(String url, FileManageAction action, int errorNo, String errorMsg) {
            showFailure(action, errorNo, errorMsg);
        }
    };
    private final NasRepository mNasRepository;
    private final Observer<Resource<ActionResultModel<OneOSFile>>> mObserver;

    private void showFailure(FileManageAction action, int errorNo, String errorMsg) {
        loadingCallback.dismissLoading();
        if (errorNo == HttpErrorNo.ERR_ONE_REQUEST) {
            if (action == FileManageAction.ATTRIBUTES) {
                ToastHelper.showToast(R.string.operate_failed);
            } else if (action == FileManageAction.DELETE) {
                ToastHelper.showToast(R.string.delete_file_failed, Toast.LENGTH_SHORT);
            } else if (action == FileManageAction.DELETE_SHIFT) {
                ToastHelper.showToast(R.string.delete_file_failed);
            } else if (action == FileManageAction.RENAME) {
                ToastHelper.showToast(R.string.rename_file_failed);
            } else if (action == FileManageAction.MKDIR) {
                ToastHelper.showToast(R.string.new_folder_failed);
            } else if (action == FileManageAction.ENCRYPT) {
                ToastHelper.showToast(R.string.encrypt_file_failed);
            } else if (action == FileManageAction.DECRYPT) {
                ToastHelper.showToast(R.string.decrypt_file_failed);
            } else if (action == FileManageAction.COPY) {
                ToastHelper.showToast(R.string.copy_file_failed);
            } else if (action == FileManageAction.MOVE) {
                ToastHelper.showToast(R.string.move_file_failed);
            } else if (action == FileManageAction.CHMOD) {
                ToastHelper.showToast(R.string.operate_failed);
            } else if (action == FileManageAction.SHARE) {
                ToastHelper.showToast(R.string.operate_failed);
            } else if (action == FileManageAction.CLEAN_RECYCLE) {
                ToastHelper.showToast(R.string.clean_recycle_failed);
            } else if (action == FileManageAction.DOWNLOAD) {
                ToastHelper.showToast(R.string.operate_failed);
            } else {
                ToastHelper.showToast(R.string.operate_failed);
            }
        } else {
            errorMsg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
            ToastHelper.showToast(errorMsg);
        }
        if (null != callback) {
            callback.onComplete(false);
        }
    }

    private void showSuccess(FileManageAction action, String response, OneOSFile oneOSFile) {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "OnFileManageListener success: Action=" + action + ", Response=" + response);
        loadingCallback.dismissLoading();
        if (action == FileManageAction.ATTRIBUTES) {
            // {"result":true, "path":"/PS-AI-CDR","dirs":1,"files":10,"size":3476576309,"uid":1001,"gid":0}
            try {
                OneOSFile file = fileList.get(0);
                Resources resources = mActivity.getResources();
                List<String> titleList = new ArrayList<>();
                List<String> contentList = new ArrayList<>();

                if (oneOSFile == null && !EmptyUtils.isEmpty(response)) {
                    JSONObject json = new JSONObject(response);
                    String datajson = json.getString("data");
                    oneOSFile = GsonUtils.decodeJSONWithoutCatchException(datajson, OneOSFile.class);
                }
                titleList.add(resources.getString(R.string.file_attr_path));
                String pathWithTypeName = OneOSFileType.getPathWithTypeName(file.getAllPath());
                while (EmptyUtils.isNotEmpty(pathWithTypeName) && pathWithTypeName.endsWith(File.separator)) {
                    pathWithTypeName = pathWithTypeName.substring(0, pathWithTypeName.length() - 1);
                }
                contentList.add(pathWithTypeName);
                titleList.add(resources.getString(R.string.file_attr_size));
                long size = oneOSFile != null ? oneOSFile.getSize() : file.getSize();
                contentList.add(FileUtils.fmtFileSize(size) + " (" + size + resources.getString(R.string.tail_file_attr_size_bytes) + ")");
                if (oneOSFile.getCttime() > 0 || oneOSFile.getTime() > 0) {
                    titleList.add(resources.getString(R.string.file_attr_time));
                    long time = Math.max(oneOSFile.getCttime(), file.getCttime());
                    if (time == 0) {
                        time = oneOSFile.getTime();
                    }
                    contentList.add(FileUtils.fmtTimeByZone(time));
                }
                if (file.isDirectory() && oneOSFile != null) {
                    titleList.add(resources.getString(R.string.file_attr_folders));
                    contentList.add(oneOSFile.getDirs() + resources.getString(R.string.tail_file_attr_folders));
                    titleList.add(resources.getString(R.string.file_attr_files));
                    contentList.add(oneOSFile.getFiles() + resources.getString(R.string.tail_file_attr_files));
                }
//                    titleList.add(resources.getString(R.string.file_attr_permission));
//                    contentList.add(file.getPerm());
//                    titleList.add(resources.getString(R.string.file_attr_uid));
//
//                    contentList.add(datajson.getString("uid"));
//                    titleList.add(resources.getString(R.string.file_attr_gid));
//                    contentList.add(datajson.getString("gid"));
                DialogUtils.showListDialog(mActivity, titleList, contentList, R.string.tip_attr_file, 0, 0, R.string.ok, null);

            } catch (Exception e) {
                e.printStackTrace();
                ToastHelper.showToast(R.string.error_json_exception);
            }
        } else if (action == FileManageAction.DELETE) {
            ToastHelper.showToast(R.string.delete_file_success, Toast.LENGTH_SHORT);
        } else if (action == FileManageAction.DELETE_SHIFT) {
            ToastHelper.showToast(R.string.delete_file_success);
        } else if (action == FileManageAction.RENAME) {
            ToastHelper.showToast(R.string.rename_file_success);
        } else if (action == FileManageAction.MKDIR) {
            ToastHelper.showToast(R.string.new_folder_success);
        } else if (action == FileManageAction.ENCRYPT) {
            ToastHelper.showToast(R.string.encrypt_file_success);
        } else if (action == FileManageAction.DECRYPT) {
            ToastHelper.showToast(R.string.decrypt_file_success);
        } else if (action == FileManageAction.COPY) {
            ToastHelper.showToast(R.string.copy_file_success);
        } else if (action == FileManageAction.MOVE) {
            ToastHelper.showToast(R.string.move_file_success);
        } else if (action == FileManageAction.CHMOD) {
            ToastHelper.showToast(R.string.chmod_file_success);
        } else if (action == FileManageAction.SHARE) {
            ToastHelper.showToast(R.string.share_file_success);
        } else if (action == FileManageAction.CLEAN_RECYCLE) {
            ToastHelper.showToast(R.string.clean_recycle_success);
        } else if (action == FileManageAction.DOWNLOAD) {
            ToastHelper.showToast(R.string.Added_to_the_queue_being_download);
        }
        FileListChangeObserver.getInstance().FileListChange();

        if (null != callback) {
            callback.onComplete(true);
        }
    }

    private void showLoading(FileManageAction action) {
        int msgId = R.string.loading;
        if (action == FileManageAction.ATTRIBUTES) {
            msgId = R.string.getting_file_attr;
        } else if (action == FileManageAction.DELETE) {
            msgId = R.string.deleting_file;
        } else if (action == FileManageAction.DELETE_SHIFT) {
            msgId = R.string.deleting_file;
        } else if (action == FileManageAction.RENAME) {
            msgId = R.string.renaming_file;
        } else if (action == FileManageAction.MKDIR) {
            msgId = R.string.making_folder;
        } else if (action == FileManageAction.ENCRYPT) {
            msgId = R.string.encrypting_file;
        } else if (action == FileManageAction.DECRYPT) {
            msgId = R.string.decrypting_file;
        } else if (action == FileManageAction.COPY) {
            msgId = R.string.copying_file;
        } else if (action == FileManageAction.MOVE) {
            msgId = R.string.moving_file;
        } else if (action == FileManageAction.CHMOD) {
            msgId = R.string.chmod_ing_file;
        } else if (action == FileManageAction.CLEAN_RECYCLE) {
            msgId = R.string.cleaning_recycle;
        }
        loadingCallback.showLoading(msgId, true);
    }

    private OneOSFileType fileType;

    public OneOSFileManage(FragmentActivity activity, ILoadingCallback loadingCallback, @NonNull LoginSession loginSession, View rootView, OnManageCallback callback) {
        this.mActivity = activity;
        if (loadingCallback == null) {
            if (activity instanceof ILoadingCallback) {
                this.loadingCallback = (ILoadingCallback) activity;
            } else {
                this.loadingCallback = new LoadingCallback() {
                };
            }
        } else {
            this.loadingCallback = loadingCallback;
        }
        this.loginSession = loginSession;
        this.mDevId = loginSession.getId();
        this.mRootView = rootView;
        this.callback = callback;
        fileManageAPI = new OneOSFileManageAPI(loginSession);
        fileManageAPI.setOnFileManageListener(mListener);

        mNasRepository = new NasRepository(SessionManager.getInstance().getUserId(), LibApp.Companion.getInstance().getAppExecutors());
        mObserver = actionResultModelResource -> {
            if (actionResultModelResource.getStatus() == Status.LOADING) {
                showLoading(action);
            } else if (actionResultModelResource.getStatus() == Status.SUCCESS) {
                BaseResultModel<OneOSFile> data = actionResultModelResource.getData();
                if (data != null) {
                    if (data.isSuccess()) {
                        showSuccess(action, null, data.data);
                    } else {
                        if (data.getError() != null) {
                            showFailure(action, data.getError().getCode(), data.getError().getMsg());
                        } else {
                            showFailure(action, HttpErrorNo.UNKNOWN_EXCEPTION, "");
                        }
                    }
                } else {
                    showFailure(action, HttpErrorNo.UNKNOWN_EXCEPTION, "");
                }
            } else if (actionResultModelResource.getStatus() == Status.ERROR) {
                showFailure(action, HttpErrorNo.UNKNOWN_EXCEPTION, actionResultModelResource.getMessage());
            }

        };
    }

    public OneOSFileManage(FragmentActivity activity, ILoadingCallback loadingCallback, @NonNull LoginSession loginSession, View rootView, OnManageCallback callback, Long groupId) {
        this.mGroupId = groupId;

        this.mActivity = activity;
        if (loadingCallback == null) {
            if (activity instanceof ILoadingCallback) {
                this.loadingCallback = (ILoadingCallback) activity;
            } else {
                this.loadingCallback = new LoadingCallback() {
                };
            }
        } else {
            this.loadingCallback = loadingCallback;
        }
        this.loginSession = loginSession;
        this.mDevId = loginSession.getId();
        this.mRootView = rootView;
        this.callback = callback;
        fileManageAPI = new OneOSFileManageAPI(loginSession);
        fileManageAPI.setOnFileManageListener(mListener);

        mNasRepository = new NasRepository(SessionManager.getInstance().getUserId(), LibApp.Companion.getInstance().getAppExecutors());
        mObserver = actionResultModelResource -> {
            if (actionResultModelResource.getStatus() == Status.LOADING) {
                showLoading(action);
            } else if (actionResultModelResource.getStatus() == Status.SUCCESS) {
                BaseResultModel<OneOSFile> data = actionResultModelResource.getData();
                if (data != null) {
                    if (data.isSuccess()) {
                        showSuccess(action, null, data.data);
                    } else {
                        if (data.getError() != null) {
                            showFailure(action, data.getError().getCode(), data.getError().getMsg());
                        } else {
                            showFailure(action, HttpErrorNo.UNKNOWN_EXCEPTION, "");
                        }
                    }
                } else {
                    showFailure(action, HttpErrorNo.UNKNOWN_EXCEPTION, "");
                }
            } else if (actionResultModelResource.getStatus() == Status.ERROR) {
                showFailure(action, HttpErrorNo.UNKNOWN_EXCEPTION, actionResultModelResource.getMessage());
            }

        };
    }

    public void doOnlineExtract(OneOSFile file) {
        ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loadingCallback, loginSession, R.string.tip_extract_file, R.string.confirm);
        fileTreeView.showPopupCenter();
        fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
            @Override
            public void onPaste(@org.jetbrains.annotations.Nullable String tarPath, int _share_path_type) {
                boolean isV5 = SessionCache.Companion.getInstance().isV5(loginSession.getId());
                if (isV5) {
                    //去掉public
                    String toPath = PathTypeCompat.getV5Path(tarPath);
                    String path = PathTypeCompat.getV5Path(file.getPath());//file.getPath().startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR) ? file.getPath().replaceFirst(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, "/") : file.getPath();
                    V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {
                            super.onSubscribe(d);
                            mListener.onStart("", action);
                        }

                        @Override
                        public void isNotV5() {

                        }

                        @Override
                        public void fail(@NotNull BaseProtocol<Object> result) {
                            mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                        }

                        @Override
                        public void success(@NotNull BaseProtocol<Object> result) {
                            mListener.onSuccess("", action, "");
                        }

                    };
                    V5Repository.Companion.INSTANCE().extractFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), path, file.getShare_path_type(), toPath, _share_path_type, observer);
                } else {
                    fileManageAPI.extract(file, tarPath);
                }
                LogUtils.d("tarPath", tarPath);
            }
        });

    }

    public void manage(final OneOSFileType type, @Nullable FileManageAction action, @NonNull final List<OneOSFile> selectedList) {
        this.action = action;
        this.fileList = selectedList;
        fileType = type;
        final String id = loginSession.getId();
        if (EmptyUtils.isEmpty(selectedList) || action == null || EmptyUtils.isEmpty(id)) {
            if (null != callback) {
                callback.onComplete(false);
            }
            return;
        }
        int tempPathType = 0;
        if (selectedList != null && selectedList.size() > 0) {
            tempPathType = selectedList.get(0).getShare_path_type();
        }
        final int share_path_type = tempPathType;
        boolean isV5 = SessionCache.Companion.getInstance().isV5(id);
        if (action == FileManageAction.ATTRIBUTES) {
            //V5参数
            String mPath = selectedList.get(0).getPath();
            mPath = OneOSAPIs.getV5Path(mPath);
            JSONArray pathList = new JSONArray();
            pathList.put(mPath);
            String finalMPath = mPath;
            if (isV5) {
                V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                    public void onSubscribe(@NotNull Disposable d) {
                        super.onSubscribe(d);
                        mListener.onStart("", action);
                    }

                    @Override
                    public void isNotV5() {

                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        mListener.onSuccess("", action, new Gson().toJson(result));
                    }

                };

                V5Repository.Companion.INSTANCE().optFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), "attributes", pathList, share_path_type, observer, mGroupId);

            } else {
                if (loginSession.isV5()) {
                    mNasRepository.manageFile(id, loginSession.getSession(), action, new String[]{finalMPath},
                            null, null, null).observe(mActivity, mObserver);
                } else {
                    fileManageAPI.attr(selectedList.get(0).getPath());
                }
            }

        }
        else if (action == FileManageAction.DELETE) {
            int resIdTips = (type == OneOSFileType.PRIVATE || type == OneOSFileType.GROUP) ? R.string.permanently_deleted : -1;
            if(type == OneOSFileType.GROUP && selectedList.get(0).getPath().startsWith(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR))
            {
                resIdTips=-1;
            }
            DialogUtils.showCheckDialog(mActivity,
                    R.string.tip_delete_file,
                    resIdTips,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnDialogCheckListener() {
                        @Override
                        public void onClick(boolean isPositiveBtn, boolean isChecked) {
                            if (isPositiveBtn) {
                                boolean isDelShift = isChecked
                                        || (type != OneOSFileType.PRIVATE && type != OneOSFileType.GROUP);
                                if (isV5) {
                                    Map<Integer, JSONArray> v5MappingData = getV5MappingData();
                                    V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                                        public void onSubscribe(@NotNull Disposable d) {
                                            super.onSubscribe(d);
                                            mListener.onStart("", FileManageAction.DELETE);
                                        }

                                        @Override
                                        public void isNotV5() {

                                        }

                                        @Override
                                        public void fail(@NotNull BaseProtocol<Object> result) {
                                            mListener.onFailure("", FileManageAction.DELETE, result.getError().getCode(), result.getError().getMsg());
                                        }

                                        @Override
                                        public void success(@NotNull BaseProtocol<Object> result) {
                                            mListener.onSuccess("", FileManageAction.DELETE, "");

                                        }

                                    };
                                    String action = isDelShift ? "deleteshift" : "delete";
                                    for (Map.Entry<Integer, JSONArray> entry : v5MappingData.entrySet()) {
                                        V5Repository.Companion.INSTANCE().optFile(id, loginSession.getIp(), LoginTokenUtil.getToken(), action, entry.getValue(), entry.getKey(), observer, mGroupId);
                                    }

                                } else {
                                    if (loginSession.isV5()) {
                                        mNasRepository.manageFile(id, loginSession.getSession(),
                                                isChecked ? FileManageAction.DELETE_SHIFT : action, getPaths(selectedList),
                                                null, null, null).
                                                observe(mActivity, mObserver);

                                    } else {
                                        fileManageAPI.delete(selectedList, isDelShift);
                                    }
                                }
                            }

                            //清理缓存, 防止重名显示异常
//                        ImagePipeline imagePipeline = Fresco.getImagePipeline();
//                        imagePipeline.clearMemoryCaches();
//                        for (OneOSFile oneOSFile : selectedList) {
//                            imagePipeline.evictFromDiskCache(ImageRequest.fromUri(Uri.parse(OneOSAPIs.genThumbnailUrl(loginSession, oneOSFile))));
//
//                        }
                        }
                    });
        }
        else if (action == FileManageAction.RENAME) {
            final OneOSFile file = selectedList.get(0);
            final String name = file.getName();
            DialogUtils.showEditDialog(mActivity, R.string.tip_rename_file, R.string.hint_rename_file, name,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn, @NonNull EditText mContentEditText) {
                            if (isPositiveBtn) {
                                final String newName = mContentEditText.getText().toString().trim();
                                int resId = 0;
                                if (EmptyUtils.isEmpty(newName)) {
                                    resId = R.string.name_cannot_empty;
                                }
                                if (".".equals(newName) || "..".equals(newName)) {
                                    resId = R.string.invalid_name;
                                }
                                if (newName.contains(File.separator)) {
                                    resId = R.string.cannot_contains_file_separator;
                                }
                                if (resId != 0) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                    ToastHelper.showLongToast(resId);
                                    return;
                                }
                                if (!Objects.equals(newName, name)) {
                                    OneOSFileManageAPI.OnFileManageListener onFileManageListener = new OneOSFileManageAPI.OnFileManageListener() {
                                        @Override
                                        public void onStart(String url, FileManageAction action) {
                                            if (mListener != null)
                                                mListener.onStart(url, action);
                                        }

                                        @Override
                                        public void onSuccess(String url, FileManageAction action, String response) {
                                            if (mListener != null) {
                                                mListener.onSuccess(url, action, response);
                                                renameFile(file, newName, name);
                                            }
                                        }

                                        @Override
                                        public void onFailure(String url, FileManageAction action, int errorNo, String errorMsg) {
                                            if (mListener != null)
                                                mListener.onFailure(url, action, errorNo, errorMsg);
                                        }
                                    };
                                    if (isV5) {
                                        JSONArray path = new JSONArray();
                                        String mPath = file.getPath();
                                        if (mPath.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
                                            //去掉public前缀
                                            mPath = mPath.substring(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR.length() - 1);
                                        }
                                        path.put(mPath);

                                        V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                                            @Override
                                            public void onSubscribe(@NotNull Disposable d) {
                                                super.onSubscribe(d);
                                                onFileManageListener.onStart("", action);
                                            }

                                            @Override
                                            public void isNotV5() {

                                            }

                                            @Override
                                            public void fail(@NotNull BaseProtocol<Object> result) {
                                                onFileManageListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                                            }

                                            @Override
                                            public void success(@NotNull BaseProtocol<Object> result) {
                                                onFileManageListener.onSuccess("", action, "");
                                            }

                                        };
                                        V5Repository.Companion.INSTANCE().renameFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), "rename", path, newName, share_path_type, mGroupId, observer);

                                    } else {
                                        if (loginSession.isV5()) {
                                            mNasRepository.manageFile(id, loginSession.getSession(),
                                                    action, new String[]{file.getPath()},
                                                    null, null, newName)
                                                    .observe(mActivity, actionResultModelResource -> {
                                                        mObserver.onChanged(actionResultModelResource);
                                                        try {
                                                            if (actionResultModelResource.getStatus() == Status.SUCCESS
                                                                    && actionResultModelResource.getData().isSuccess()) {
                                                                renameFile(file, newName, name);
                                                            }
                                                        } catch (Exception ignore) {
                                                            ignore.printStackTrace();
                                                        }
                                                    });
                                        } else {
                                            fileManageAPI.setOnFileManageListener(onFileManageListener);
                                            fileManageAPI.rename(file, newName);
                                        }
                                    }
                                }
                            }
                            dialog.dismiss();
                        }
//                        }
                    });
        }
        else if (action == FileManageAction.ENCRYPT) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditPwdDialog(mActivity, R.string.tip_encrypt_file, R.string.warning_encrypt_file, 0, R.string.hint_encrypt_pwd, R.string.hint_confirm_encrypt_pwd,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditPWDDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn, @NonNull EditText mContentEditText, EditText oldEditText) {
                            if (isPositiveBtn) {
                                String pwd = mContentEditText.getText().toString().trim();
                                if (EmptyUtils.isEmpty(pwd)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                } else {
                                    fileManageAPI.crypt(file, pwd, true);
                                    dialog.dismiss();
                                }
                            }
                        }
                    });
        }
        else if (action == FileManageAction.DECRYPT) {
            final OneOSFile file = selectedList.get(0);
            DialogUtils.showEditDialog(mActivity, R.string.tip_decrypt_file, R.string.hint_decrypt_pwd, null,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn, @NonNull EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String pwd = mContentEditText.getText().toString().trim();
                                if (EmptyUtils.isEmpty(pwd)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                    mContentEditText.requestFocus();
                                } else {
                                    fileManageAPI.crypt(file, pwd, false);
                                    dialog.dismiss();
                                }
                            }
                        }
                    });
        }
        else if (action == FileManageAction.COPY) {
            boolean isShareDownloadEnable = (fileType == OneOSFileType.PRIVATE || fileType == OneOSFileType.PUBLIC);
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loadingCallback, loginSession, R.string.tip_copy_file, R.string.paste, 0, isShareDownloadEnable);
            fileTreeView.showPopupCenter();
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath, int _share_path_type) {
                    int des_path_type = _share_path_type;
                    copyOrMoveTo(action,tarPath, des_path_type, selectedList);
                }

            });
            fileTreeView.setShareListener(new ServerFileTreeView.OnShareFileListener() {
                @Override
                public void onShare(String sourceId, String toId, String tarPath, int type) {
                    share(mActivity, selectedList, sourceId, toId, tarPath, type);
                }
            });

        }
        else if (action == FileManageAction.MOVE) {
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loadingCallback, loginSession, R.string.tip_move_file, R.string.move_file);
            fileTreeView.showPopupCenter();
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath, int _share_path_type) {
                    if (isV5) {
                        Map<Integer, JSONArray> v5MappingData = getV5MappingData();
                        String toDir = OneOSAPIs.getV5Path(tarPath);
                        int des_path_type = _share_path_type;
                        V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                            @Override
                            public void onSubscribe(@NotNull Disposable d) {
                                super.onSubscribe(d);
                                mListener.onStart("", action);
                            }

                            @Override
                            public void isNotV5() {

                            }

                            @Override
                            public void fail(@NotNull BaseProtocol<Object> result) {
                                mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                            }

                            @Override
                            public void success(@NotNull BaseProtocol<Object> result) {
                                mListener.onSuccess("", action, "");

                            }

                        };
                        for (Map.Entry<Integer, JSONArray> entry : v5MappingData.entrySet()) {
                            V5Repository.Companion.INSTANCE().copyOrMoveFile(loginSession.getId(),
                                    loginSession.getIp(), LoginTokenUtil.getToken(), "move",
                                    entry.getValue(), entry.getKey(), toDir, des_path_type, observer);
                        }
                    } else {
                        if (loginSession.isV5()) {
                            mNasRepository.manageFile(id, loginSession.getSession(),
                                    action, getPaths(selectedList),
                                    tarPath, null, null)
                                    .observe(mActivity, mObserver);
                        } else {
                            fileManageAPI.move(selectedList, tarPath);
                        }
                    }
                }
            });

        }
        else if (action == FileManageAction.EXTRACT) {
            final OneOSFile file = selectedList.get(0);
            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loadingCallback, loginSession, R.string.tip_extract_file, R.string.confirm);
            fileTreeView.showPopupCenter();
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath, int _share_path_type) {
                    if (isV5) {
                        //去掉public
                        String toPath = OneOSAPIs.getV5Path(tarPath);
                        String path = OneOSAPIs.getV5Path(file.getPath());
                        V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                            @Override
                            public void onSubscribe(@NotNull Disposable d) {
                                super.onSubscribe(d);
                                mListener.onStart("", action);
                            }

                            @Override
                            public void isNotV5() {

                            }

                            @Override
                            public void fail(@NotNull BaseProtocol<Object> result) {
                                mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                            }

                            @Override
                            public void success(@NotNull BaseProtocol<Object> result) {
                                mListener.onSuccess("", action, "");
                            }

                        };
                        V5Repository.Companion.INSTANCE().extractFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), path, file.getShare_path_type(), toPath, _share_path_type, observer);
                    } else {
                        fileManageAPI.extract(file, tarPath);
                    }
                }
            });
        }
        else if (action == FileManageAction.ARCHIVER) {

            ServerFileTreeView fileTreeView = new ServerFileTreeView(mActivity, loadingCallback, loginSession, R.string.tip_archiver_file, R.string.confirm);
            fileTreeView.showPopupCenter();
            fileTreeView.setOnPasteListener(new ServerFileTreeView.OnPasteFileListener() {
                @Override
                public void onPaste(String tarPath, int _share_path_type) {
                    //去掉public
                    String toPath = OneOSAPIs.getV5Path(tarPath);
                    V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {
                            super.onSubscribe(d);
                            mListener.onStart("", action);
                        }

                        @Override
                        public void isNotV5() {
                            mListener.onFailure("", action, HttpErrorNo.ERR_ONEOS_VERSION, "Version is too low");
                        }

                        @Override
                        public void fail(@NotNull BaseProtocol<Object> result) {
                            mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                        }

                        @Override
                        public void success(@NotNull BaseProtocol<Object> result) {
                            mListener.onSuccess("", action, "");
                        }

                    };
                    OneOSFile oneOSFile = selectedList.get(0);
                    V5Repository.Companion.INSTANCE().archiverFile(loginSession.getId(), loginSession.getIp(),
                            LoginTokenUtil.getToken(), Arrays.asList(getPaths(selectedList)),
                            oneOSFile.getShare_path_type(),
                            toPath + "/archiver.zip", _share_path_type, observer);
                }
            });
        }
        else if (action == FileManageAction.CLEAN_RECYCLE) {
            DialogUtils.showConfirmDialog(mActivity, R.string.title_clean_recycle_file, R.string.tip_clean_recycle_file, R.string.clean_now, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        if (isV5) {
                            V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                                @Override
                                public void onSubscribe(@NotNull Disposable d) {
                                    super.onSubscribe(d);
                                    mListener.onStart("", action);
                                }

                                @Override
                                public void isNotV5() {

                                }

                                @Override
                                public void fail(@NotNull BaseProtocol<Object> result) {
                                    mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                                }

                                @Override
                                public void success(@NotNull BaseProtocol<Object> result) {
                                    mListener.onSuccess("", action, "");
                                }

                            };
                            V5Repository.Companion.INSTANCE().cleanRecycleFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), mGroupId, observer);
                        } else {
                            fileManageAPI.cleanRecycle();
                        }
                    }
                }
            });
        }
        else if (action == FileManageAction.RESTORE_RECYCLE) {
            List<String> paths = getPathsList(selectedList);
            if (isV5) {
                V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {
                        super.onSubscribe(d);
                        mListener.onStart("", action);
                    }

                    @Override
                    public void isNotV5() {

                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        mListener.onSuccess("", action, "");
                    }

                };
                V5Repository.Companion.INSTANCE().restoreRecycleFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), share_path_type, paths, mGroupId, observer);
            } else {
                fileManageAPI.restoreRecycle(paths);
            }

        }
        else if (action == FileManageAction.CHMOD) {
            chmodFile(selectedList.get(0));
        }
        else if (action == FileManageAction.SHARE) {
            PermissionChecker.checkPermission(mActivity, o -> shareFile(selectedList), o -> UiUtils.showStorageSettings(mActivity), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } /*else if (action == FileManageAction.SHARING) {
//            getUserList(mActivity, loginSession, R.string.tip_copy_file, R.string.share_file);
            shareToUserDialog(mActivity, loginSession.getUserInfo().getAdmin() == 1
                    , selectedList, loginSession);
        }*/
        else if (action == FileManageAction.DOWNLOAD) {

            if (!Utils.isWifiAvailable(mActivity) && SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
                DialogUtils.showConfirmDialog(mActivity, R.string.tips, R.string.confirm_download_not_wifi, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            downloadFiles(selectedList);
                        }
                    }
                });
            } else {
                downloadFiles(selectedList);
            }
        }
        else if (action == FileManageAction.FAVORITE || action == FileManageAction.UNFAVORITE) {
            boolean isFavorite = action == FileManageAction.FAVORITE;
            V5Observer<OptTagFileError> observer = new V5Observer<OptTagFileError>(loginSession.getId()) {
                @Override
                public void onSubscribe(@NotNull Disposable d) {
                    super.onSubscribe(d);
                    mListener.onStart("", action);
                }

                @Override
                public void isNotV5() {

                }

                @Override
                public void fail(@NotNull BaseProtocol<OptTagFileError> result) {
                    Error error = result.getError();
                    if (error != null) {
                        mListener.onFailure("", action, error.getCode(), error.getMsg());
                    } else {
                        OptTagFileError data = result.getData();
                        if (data != null) {
                            List<Integer> codes = new ArrayList<>();
                            List<TagFileError> failed = data.getFailed();
                            for (OneOSFile oneOSFile : selectedList) {
                                Iterator<TagFileError> iterator = failed.iterator();
                                while (iterator.hasNext()) {
                                    TagFileError tagFileError = iterator.next();
                                    if (Objects.equals(oneOSFile.getPath(), tagFileError.getPath()) &&
                                            oneOSFile.getShare_path_type() == tagFileError.getSharePathType()) {
                                        int code = tagFileError.getError().getCode();
                                        switch (code) {
                                            case V5HttpErrorNoKt.V5_ERR_FILE_HAVE_THIS_TAG:
                                            case V5HttpErrorNoKt.V5_ERR_FILE_NOT_EXISTED:
                                                oneOSFile.setFavorite(isFavorite);
                                                break;
                                            default:
                                                codes.add(code);
                                        }
                                        iterator.remove();
                                        break;
                                    }
                                }
                            }
                            if (EmptyUtils.isNotEmpty(codes)) {
                                mListener.onFailure("", action, codes.get(0), "");
                                return;
                            } else {
                                mListener.onSuccess("", action, "");
                                return;
                            }
                        }
                    }
                    mListener.onFailure("", action, V5HttpErrorNoKt.V5_ERR_OPERATOR, "");
                }

                @Override
                public void success(@NotNull BaseProtocol<OptTagFileError> result) {
                    for (OneOSFile oneOSFile : selectedList) {
                        oneOSFile.setFavorite(isFavorite);
                    }
                    mListener.onSuccess("", action, "");
                }

            };
            List<String> pathList0 = new ArrayList<>();
            List<String> pathList2 = new ArrayList<>();

            boolean needCheck = fileType != OneOSFileType.FAVORITES; //非收藏夹需要检查是否已收藏
            if (!EmptyUtils.isEmpty(selectedList)) {
                for (OneOSFile file : selectedList) {
                    if (needCheck && isFavorite == file.isFavorite()) {
                        continue;
                    }
                    String path = file.getPath();
                    if (path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
                        //去掉public前缀
                        path = path.substring(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR.length() - 1);
                    }
                    if (file.getShare_path_type() == SharePathType.PUBLIC.getType()) {
                        pathList2.add(path);
                    }
                    if (file.getShare_path_type() == SharePathType.USER.getType()) {
                        pathList0.add(path);
                    }
                }
            }
            TagCMD tagCMD = isFavorite ? TagCMD.ADD : TagCMD.REMOVE;
            if (pathList0.size() > 0) {
                V5Repository.Companion.INSTANCE().fileOptTag(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(),
                        tagCMD, loginSession.getUserInfo().getFavoriteId(),
                        SharePathType.USER.getType(), pathList0, observer);
            }
            if (pathList2.size() > 0) {
                V5Repository.Companion.INSTANCE().fileOptTag(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(),
                        tagCMD, loginSession.getUserInfo().getFavoriteId(),
                        SharePathType.PUBLIC.getType(), pathList2, observer);
            }

        }
    }

    private Map<Integer, JSONArray> getV5MappingData() {
        Map<Integer, JSONArray> map = new HashMap<>();
        if (!EmptyUtils.isEmpty(fileList)) {
            for (OneOSFile file : fileList) {
                String path = file.getPath();
                int share_path_type1 = file.getShare_path_type();
                JSONArray jsonArray = map.get(share_path_type1);
                if (jsonArray == null) {
                    jsonArray = new JSONArray();
                    map.put(share_path_type1, jsonArray);
                }
                jsonArray.put(path);
            }
        }
        return map;
    }

    private void renameFile(OneOSFile file, String newName, String name) {
        File localFile = file.getLocalFile();
        if (localFile != null && localFile.exists()) {
            localFile.renameTo(new File(localFile.getParent(), newName));
        }
        file.setName(newName);
        String path = file.getPath();
        file.setPath(path.substring(0, path.length() - name.length()) + newName);
    }

    private String[] getPaths(List<OneOSFile> selectedList) {
        List<String> paths = new ArrayList<>();
        for (OneOSFile oneOSFile : selectedList) {
            paths.add(oneOSFile.getPath());
        }
        return getPathsList(selectedList).toArray(new String[0]);
    }

    private List<String> getPathsList(List<OneOSFile> selectedList) {
        List<String> paths = new ArrayList<>();
        for (OneOSFile oneOSFile : selectedList) {
            paths.add(oneOSFile.getPath());
        }
        return paths;
    }

    private KeyPair<List<Integer>, List<String>> getNasV3Paths(List<OneOSFile> selectedList) {
        int initialCapacity = selectedList.size();
        List<String> paths = new ArrayList<>(initialCapacity);
        List<Integer> pathTypes = new ArrayList<>(initialCapacity);
        for (OneOSFile oneOSFile : selectedList) {
            pathTypes.add(oneOSFile.getShare_path_type());
            paths.add(oneOSFile.getPath());
        }
        return new KeyPair<>(pathTypes, paths);
    }


    private void share(@NonNull final FragmentActivity activity, @NonNull final List<OneOSFile> fileList,
                       final String sourceId, final String toId, final String tarPath, int type) {
        Logger.LOGD(TAG, "Share :" + sourceId, toId, tarPath, type);
//        Observable.create(new ObservableOnSubscribe<Pair<FriendItem, Boolean>>() {
//            @Override
//            public void subscribe(@NonNull final ObservableEmitter<Pair<FriendItem, Boolean>> emitter) {
//                final String phone = CMAPI.getInstance().getBaseInfo().getAccount();
//                final List<String> paths = new ArrayList<>();
//                boolean hasDirs = false;
//                for (OneOSFile oneOSFile : fileList) {
//                    if (!oneOSFile.isDirectory())
//                    paths.add(oneOSFile.getPath());
//                    else {
//                        hasDirs = true;
//                    }
//                }
//                if (hasDirs) {
//                    ToastHelper.showToast(R.string.tips_copy_file_to_remote_device);
//                }
//
//                if (paths.size() > 0) {
//                    SessionManager.getInstance().getLoginSession(sourceId, new GetSessionListener() {
//                        @Override
//                        public void onSuccess(String url, @NonNull final LoginSession loginSession) {
//                            OneOSGetFileInfoAPI fileInfoAPI = new OneOSGetFileInfoAPI(loginSession);
//                            fileInfoAPI.setOnGetFileInfoListener(new OneOSGetFileInfoAPI.OnGetFileInfoListener() {
//                                @Override
//                                public void onStart(String url) {
//                                }
//
//                                @Override
//                                public void onSuccess(String url, @NonNull ArrayList<FileInfo> fileInfos) {
//                                    List<ShareFileBean> beans = new ArrayList<>();
//                                    for (FileInfo fileInfo : fileInfos) {
//                                        for (OneOSFile oneOSFile : fileList) {
//                                            if (Objects.equals(oneOSFile.getPath(), fileInfo.path)) {
//                                                //此处循环为了查找对于文件id
//                                                ShareFileBean bean = new ShareFileBean(oneOSFile.getName(),
//                                                        String.valueOf(oneOSFile.getSize()),
//                                                        oneOSFile.getPath(),
//                                                        "0",
//                                                        loginSession.getUserInfo().getUsername(),
//                                                        String.valueOf(fileInfo.id));
//                                                beans.add(bean);
//                                                break;
//                                            }
//                                        }
//                                    }
//                                    String token = LoginTokenUtil.getToken();
//                                    String deviceid = loginSession.getId();
//                                    String from = CMAPI.getInstance().getBaseInfo().getUserId();
//                                    String to = phone;
//                                    //提交分享
//                                    ApplyShareFileHttpLoader loader = new ApplyShareFileHttpLoader(ShareTokenResultBean.class);
//                                    loader.setParams(token, beans, deviceid, from, to, 7 * 24);
//                                    loader.executor(new CommonResultListener<ShareTokenResultBean>() {
//                                        @Override
//                                        public void success(Object tag, ShareTokenResultBean mGsonBaseProtocol) {
//                                            ToastHelper.showToast(R.string.copy_file_add_to_queue);
//                                            emitter.onNext(new Pair<>(new FriendItem(phone), true));
//                                        }
//
//                                        @Override
//                                        public void error(Object tag, @NonNull GsonBaseProtocol mErrorProtocol) {
//                                            ToastHelper.showToast(SdvnHttpErrorNo.ec2String(mErrorProtocol.result));
//                                            emitter.onNext(new Pair<>(new FriendItem(phone), false));
//                                        }
//                                    });
//                                }
//
//                                @Override
//                                public void onFailure(String url, int errorNo, String errorMsg) {
//                                    emitter.onNext(new Pair<>(new FriendItem(phone), false));
//                                    ToastHelper.showToast(HttpErrorNo.getResultMsg(true, errorNo, errorMsg));
//                                }
//                            });
//                            fileInfoAPI.getFileInfo(paths);
//                        }
//
//                        @Override
//                        public void onFailure(String url, int errorNo, String errorMsg) {
//                            super.onFailure(url, errorNo, errorMsg);
//                            emitter.onNext(new Pair<>(new FriendItem(phone), false));
//                        }
//                    });
//                } else {
//                    emitter.onNext(new Pair<>(new FriendItem(phone), false));
//                }
//            }
//        })
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(new io.reactivex.Observer<Pair<FriendItem, Boolean>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull Pair<FriendItem, Boolean> friendItemBooleanPair) {
//                        if (friendItemBooleanPair.second) {
//
//                            BoxStore boxStore = DBHelper.getBoxStore();
//                            Box<ShareElement> copyFileBox = null;
//                            if (boxStore != null) {
//                                copyFileBox = boxStore.boxFor(ShareElement.class);
//                                for (OneOSFile oneOSFile : fileList) {
//                                    ShareElement file = new ShareElement(oneOSFile.getPath(), sourceId, toId, tarPath, System.currentTimeMillis() / 1000, ShareElement.TYPE_SHARE_COPY);
//                                    copyFileBox.put(file);
//                                }
//                                ShareViewModel mShareViewModel = ViewModelProviders.of(activity).get(ShareViewModel.class);
//                                mShareViewModel.addToObserverQueue(fileList, sourceId, toId, tarPath);
//                                LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(AppConstants.DEV_TO_DEV_FILE_COPY));
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
        this.loadingCallback.showLoading();
        final ShareViewModel2 shareViewModel2 = ViewModelProviders.of(activity).
                get(ShareViewModel2.class);
        String username = SessionManager.getInstance().getUsername();
        String pwd = Utils.genRandomNum(4);

        Disposable subscribe = getFileShareBaseResultObservable(sourceId, fileList, 7, 1,
                pwd, shareViewModel2, username)
                .flatMap(dataCreate -> {
                    if (dataCreate.isSuccessful()) {
                        String ticket2 = dataCreate.getResult().getTicket2();
                        DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(toId);
                        boolean isPublic = tarPath != null && tarPath.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)
                                || type == SharePathType.PUBLIC.getType()
                                || (deviceModel != null && UiUtils.isAndroidTV(deviceModel.getDevClass())
                                && !SessionCache.Companion.getInstance().isV5(toId));
                        String toPath = tarPath;
                        if (isPublic && tarPath != null) {
                            toPath = tarPath.replaceFirst(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, "/");
                        }
                        return shareViewModel2.download(toId, ticket2,
                                toPath, Collections.singletonList("/"), isPublic, pwd, activity);

                    }
                    return Observable.just(dataCreate);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FileShareBaseResult>() {
                    @Override
                    public void accept(FileShareBaseResult result) throws Exception {
                        loadingCallback.dismissLoading();
                        if (result.isSuccessful()) {
                            ToastHelper.showShortToastSafe(R.string.wait_for_copy_finish);
                        } else {
                            ToastHelper.showShortToastSafe(FileServerErrorCode.getString(result.getStatus()));
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        loadingCallback.dismissLoading();
                        ToastHelper.showShortToastSafe(R.string.unknown_exception);
                    }
                });
        shareViewModel2.addDisposable(subscribe);

    }

    public void manage(@Nullable FileManageAction action, @NonNull final String path, final int share_path_type) {
        this.action = action;
        if (EmptyUtils.isEmpty(path) || action == null) {
            if (null != callback) {
                callback.onComplete(true);
            }
            return;
        }

        if (action == FileManageAction.MKDIR) {
            DialogUtils.showEditDialog(mActivity, R.string.tip_new_folder, R.string.hint_new_folder, R.string.default_new_folder,
                    R.string.confirm, R.string.cancel, new DialogUtils.OnEditDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn, @NonNull EditText mContentEditText) {
                            if (isPositiveBtn) {
                                String newName = mContentEditText.getText().toString().trim();
                                if (EmptyUtils.isEmpty(newName)) {
                                    AnimUtils.sharkEditText(mActivity, mContentEditText);
                                } else {
                                    Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "MkDir: " + path + ", Name: " + newName);
                                    SessionManager.getInstance().getLoginSession(mDevId, new GetSessionListener() {
                                        @Override
                                        public void onSuccess(String url, LoginSession loginSession) {
                                            //M5 path
                                            String newPath = path;
                                            if (path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
                                                //去掉public前缀
                                                newPath = path.substring(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR.length() - 1);
                                            }
                                            if (!path.endsWith(File.separator)) {
                                                newPath += File.separator;
                                            }
                                            newPath = newPath + newName;
                                            JSONArray pathList = new JSONArray();
                                            pathList.put(newPath);

                                            V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                                                public void onSubscribe(@NotNull Disposable d) {
                                                    super.onSubscribe(d);
                                                    mListener.onStart("", action);
                                                }

                                                @Override
                                                public void isNotV5() {
                                                    if (loginSession.isV5()) {
                                                        String newPath = path;
                                                        if (!path.endsWith(File.separator)) {
                                                            newPath += File.separator;
                                                        }
                                                        newPath = newPath + newName;
                                                        mNasRepository.manageFile(mDevId,
                                                                loginSession.getSession(), action,
                                                                new String[]{newPath},
                                                                null, null, null)
                                                                .observe(mActivity, mObserver);

                                                    } else {
                                                        fileManageAPI.mkdir(path, newName);
                                                    }

                                                }

                                                @Override
                                                public void fail(@NotNull BaseProtocol<Object> result) {
                                                    mListener.onFailure("", action, result.getError().getCode(), result.getError().getMsg());
                                                }

                                                @Override
                                                public void success(@NotNull BaseProtocol<Object> result) {
                                                    mListener.onSuccess("", action, "");

                                                }

                                            };

                                            V5Repository.Companion.INSTANCE().optFile(loginSession.getId(), loginSession.getIp(), LoginTokenUtil.getToken(), "mkdir", pathList, share_path_type, observer, mGroupId);
                                            InputMethodUtils.hideKeyboard(mActivity, mContentEditText);
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            }
                        }
                    });
        }
    }

    private void downloadFiles(final List<OneOSFile> selectedList) {
        PermissionChecker.checkPermission(mActivity, new Callback() {
            @Override
            public void result(Object o) {
//                StringBuilder names = new StringBuilder();
//                int count = selectedList.size() >= 4 ? 4 : selectedList.size();
//                for (int i = 0; i < count; i++) {
//                    names.append(selectedList.get(i).getName()).append(" ");
//                }
                NasService service = SessionManager.getInstance().getService();
                boolean isSuccess = false;
                if (service != null) {
                    service.addDownloadTasks(selectedList, loginSession.getId(), mGroupId);
                    isSuccess = true;
                }
                if (null != callback) {
                    callback.onComplete(isSuccess);
                }
            }
        }, new Callback() {
            @Override
            public void result(Object o) {
                UiUtils.showStorageSettings(mActivity);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);


    }


    private void chmodFile(final OneOSFile file) {
        Timber.d("Chmod File: " + file.getName() + ", Permission: " + file.getPerm());
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_chmod_file, null);
        final Dialog mDialog = new Dialog(mActivity, R.style.DialogTheme);
        final CheckableImageButton mGroupReadBox = dialogView.findViewById(R.id.cb_group_read);
        mGroupReadBox.setChecked(file.isGroupRead());
        final CheckableImageButton mGroupWriteBox = dialogView.findViewById(R.id.cb_group_write);
        mGroupWriteBox.setChecked(file.isGroupWrite());
        final CheckableImageButton mOtherReadBox = dialogView.findViewById(R.id.cb_other_read);
        mOtherReadBox.setChecked(file.isOtherRead());
        final CheckableImageButton mOtherWriteBox = dialogView.findViewById(R.id.cb_other_write);
        mOtherWriteBox.setChecked(file.isOtherWrite());

        TextView positiveBtn = dialogView.findViewById(R.id.positive);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean isGroupRead = mGroupReadBox.isChecked();
                boolean isGroupWrite = mGroupWriteBox.isChecked();
                boolean isOtherRead = mOtherReadBox.isChecked();
                boolean isOtherWrite = mOtherWriteBox.isChecked();
                String group = (isGroupRead ? "r" : "-") + (isGroupWrite ? "w" : "-");
                String other = (isOtherRead ? "r" : "-") + (isOtherWrite ? "w" : "-");
                fileManageAPI.chmod(file, group, other);
                mDialog.dismiss();
            }
        });

        TextView negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    /**
     * =====================================分享相关操作===========================================
     */

    private void shareFile(@NonNull final List<OneOSFile> selectedList) {
        int positiveTxt = fileType == OneOSFileType.PUBLIC ? R.string.invite_user : R.string.in_this_app;
        final boolean shareV2Available = loginSession.isShareV2Available();
        String id = loginSession.getId();
        DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(id);
        if (deviceModel != null && deviceModel.isEnableUseSpace() && !deviceModel.isSrcProvide() && shareV2Available
                && OneOSFileType.isSharedDir(fileType)) {
            positiveTxt = R.string.qrcode;
        } else {
            positiveTxt = -1;
        }
        DialogUtils.showConfirmDialog(mActivity, R.string.share_to, positiveTxt, R.string.other_ways, new DialogUtils.OnDialogClickListener() {
            @Override
            public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                if (isPositiveBtn) {
                    //应用内分享
//                    if (fileType == OneOSFileType.PUBLIC) {
//                        if (shareV2Available)
//                            shareByQRCode(selectedList);
//                        else
//                            getContacts(selectedList);
//                    } else {
////                        boolean isAdmin = SPUtils.isAdmin(mActivity);
////                        shareToUserDialog(mActivity, isAdmin, selectedList, loginSession);
//
//                        if (!shareV2Available)
//                            shareToFriends(selectedList);
//                        else
//                            shareByQRCode(selectedList);
//                    }
                    shareByQRCode(selectedList);
                } else {

                    //其他方式
                    final Disposable subscribe = Observable.fromIterable(selectedList)
                            .filter(new Predicate<OneOSFile>() {
                                @Override
                                public boolean test(OneOSFile oneOSFile) {
                                    final boolean b = !oneOSFile.isDirectory();
                                    if (!b)
                                        Logger.LOGD(TAG, "filter dir: Dir path >>" + oneOSFile.getPath());
                                    return b;
                                }
                            })
                            .toList()
                            .doAfterSuccess(new Consumer<List<OneOSFile>>() {
                                @Override
                                public void accept(List<OneOSFile> oneOSFiles) {
                                    Logger.LOGD(TAG, "filter dir:doAfterSuccess");
                                    if (oneOSFiles.size() < selectedList.size())
                                        ToastHelper.showToast(R.string.tips_share_to_other_app_not_support_dirs);
                                }
                            })
                            .as(RxLife.asOnMain(mActivity))
                            .subscribe(new Consumer<List<OneOSFile>>() {
                                @Override
                                public void accept(List<OneOSFile> oneOSFiles) {
//                                    shareByOtherWays(oneOSFiles);
                                    DownloadFileViewModel downloadFileViewModel = new ViewModelProvider(mActivity).get(DownloadFileViewModel.class);
                                    downloadFileViewModel.shareByOtherWays(mActivity, oneOSFiles, loginSession);
                                    Logger.LOGD(TAG, "filter dir:subscribe");
                                }
                            });

                }
            }
        });

    }

    private void shareByQRCode(final List<OneOSFile> selectedList) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_share_qrcode_config, null);
        TextView negative = view.findViewById(R.id.negative);
        TextView positive = view.findViewById(R.id.positive);
        final EditText et_line0_edit = view.findViewById(R.id.et_line0_edit);
        final EditText et_line1_edit = view.findViewById(R.id.et_line1_edit);
        final RadioGroup radioGroup = view.findViewById(R.id.radio_group_pwd);

        final int[] arr = new int[2];
        arr[0] = 1;
        arr[1] = 1;
        et_line0_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view.findViewById(R.id.tv_line0_edit).setVisibility(et_line0_edit.getText().length() > 0 ? View.GONE : View.VISIBLE);
                if (TextUtils.isEmpty(s)) {
                    arr[0] = 1;
                    return;
                }
                try {
                    final int i = Integer.parseInt(String.valueOf(s));
                    if (i < 1 || i > 7) {
                        AnimUtils.sharkEditText(mActivity, et_line0_edit);
                        if (i < 1) {
                            et_line0_edit.setText("1");
                            arr[0] = 1;
                        }
                        if (i > 7) {
                            et_line0_edit.setText("7");
                            arr[0] = 7;
                        }
                    } else {
                        arr[0] = i;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AnimUtils.sharkEditText(mActivity, et_line0_edit);
                    arr[0] = 1;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_line1_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view.findViewById(R.id.tv_line1_edit).setVisibility(et_line1_edit.getText().length() > 0 ? View.GONE : View.VISIBLE);
                if (TextUtils.isEmpty(s)) {
                    arr[1] = 1;
                    return;
                }
                try {
                    final int i = Integer.parseInt(String.valueOf(s));
                    arr[1] = i;
                } catch (Exception e) {
                    e.printStackTrace();
                    et_line1_edit.setText("1");
                    ToastHelper.showToast(R.string.tip_params_too_big);
                    AnimUtils.sharkEditText(mActivity, et_line1_edit);
                    arr[1] = 1;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        negative.setText(R.string.cancel);
        positive.setText(R.string.next);
        final Dialog dialog = DialogUtils.showCustomDialog(mActivity, view);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.negative) {
                    dialog.dismiss();
                } else {
                    if (v.getId() == R.id.positive) {
                        if (!NetworkStateManager.Companion.getInstance().isEstablished()) {
                            ToastHelper.showLongToastSafe(R.string.tip_wait_for_service_connect);
                            return;
                        }
                        final boolean checkedPrivateRadioButton =
                                radioGroup.getCheckedRadioButtonId() != R.id.radio_public;
                        String pwd = null;
                        if (checkedPrivateRadioButton)
                            pwd = Utils.genRandomNum(4);
                        int sharePeriodTime = arr[0];
                        long shareDownloadCounter = arr[1];
                        if (shareDownloadCounter <= 0) {
                            shareDownloadCounter = FS_Config.CODE_DOWNLOAD_TIMES_UNLIMITED;
                        }
                        final String id = loginSession.getId();
                        final View refreshView = view.findViewById(R.id.layout_refresh_view);
                        requestCreateShare(new UserDialogCallBack() {
                            @Override
                            public void onComplete(boolean isSuccess) {
                                if (isSuccess) {
                                    refreshView.setVisibility(View.GONE);
                                } else {
                                    ToastHelper.showToast(R.string.operate_failed);
                                }
                                dialog.dismiss();
                            }
                        }, id, selectedList, sharePeriodTime, shareDownloadCounter, pwd);
                        refreshView.setVisibility(View.VISIBLE);

                    }
                }
            }
        };
        negative.setOnClickListener(clickListener);
        positive.setOnClickListener(clickListener);

    }

    private void requestCreateShare(UserDialogCallBack callBack, final String id, List<OneOSFile> selectedList,
                                    final int sharePeriodTime, final long shareDownloadCounter, final String pwd) {
        final ShareViewModel2 shareViewModel2 = ViewModelProviders.of(mActivity).
                get(ShareViewModel2.class);
        final Disposable subscribe =
                getFileShareBaseResultObservable(id, selectedList, sharePeriodTime, shareDownloadCounter, pwd, shareViewModel2)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<FileShareBaseResult<DataCreate>>() {
                            @Override
                            public void accept(FileShareBaseResult<DataCreate> fileShareBaseResult) {
                                if (fileShareBaseResult.isSuccessful()) {
                                    callBack.onComplete(true);
                                    if (fileShareBaseResult.getResult() != null) {
                                        final DataCreate result = fileShareBaseResult.getResult();
                                        String ticket2 = result.getTicket2();
                                        String sharePeriod = String.format("%s %s", sharePeriodTime,
                                                mActivity.getString(R.string.day));
                                        shareViewModel2.showQRCode(mActivity, ticket2, sharePeriod,
                                                shareDownloadCounter, pwd);
                                        final ShareElementV2 entity = new ShareElementV2(
                                                null, ticket2, id, EntityType.SHARE_FILE_V2_SEND);
                                        entity.setPath(result.getPaths());
                                        shareViewModel2.putToDB(entity);
                                        shareViewModel2.doGetList(id);
                                    }
                                } else {
                                    ToastHelper.showLongToast(FileServerErrorCode.getString(fileShareBaseResult.getStatus()));
                                    callBack.onComplete(true);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Logger.LOGE(TAG, throwable, "create share");
                                callBack.onComplete(false);
                            }
                        });
        shareViewModel2.addDisposable(subscribe);
    }

    private Observable<FileShareBaseResult<DataCreate>>
    getFileShareBaseResultObservable(String id,
                                     List<OneOSFile> selectedList, int sharePeriodTime,
                                     long shareDownloadCounter, String pwd,
                                     ShareViewModel2 shareViewModel2, String... toUserIds) {
        DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(id);
        boolean isNasV3 = SessionCache.Companion.getInstance().isNasV3(id);
        boolean isPublic = (deviceModel != null && UiUtils.isAndroidTV(deviceModel.getDevClass()));
        SharePathType type = SharePathType.USER;
        if (fileType == OneOSFileType.PUBLIC || (isPublic && !isNasV3)) {
            type = SharePathType.PUBLIC;
        } else if (fileType == OneOSFileType.EXTERNAL_STORAGE) {
            type = SharePathType.EXTERNAL_STORAGE;
        } else if (fileType == OneOSFileType.GROUP) {
            type = SharePathType.GROUP;
        } else if (OneOSFileType.isDB(fileType)) {
            type = SharePathType.VIRTUAL;
        }
        SharePathType finalType = type;
        return Observable.fromIterable(selectedList)
                .map(new Function<OneOSFile, String>() {
                    @Override
                    public String apply(OneOSFile oneOSFile) {
                        String path = oneOSFile.getPath();
//                                if (oneOSFile.isDirectory()) {
//                                    if (!path.endsWith(File.separator)) {
//                                        path = path + File.separator;
//                                    }
//                                }
                        if (OneOSFileType.getTypeByPath(path) == OneOSFileType.PUBLIC) {
                            path = path.replaceFirst(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, "/");
                        }
                        return path;
                    }
                })
                .toList()
                .toObservable()
                .flatMap(new Function<List<String>, ObservableSource<FileShareBaseResult<DataCreate>>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult<DataCreate>> apply(List<String> paths) throws Exception {

                        return shareViewModel2.create(id, paths, sharePeriodTime, shareDownloadCounter, pwd, finalType, toUserIds);
                    }
                })
                ;
    }


    private void shareToFriends(@NonNull ArrayList<OneOSFile> selectedList) {
//        Intent intent = new Intent(mActivity, ShareToActivity.class);
//        intent.putExtra("FileList", FileInfoHolder.SHARE_FILE);
//        FileInfoHolder.getInstance().save(FileInfoHolder.SHARE_FILE, selectedList);
//        mActivity.startActivity(intent);
    }

    private UserModel mUserModel;

    public void shareToUserDialog(@NonNull final AppCompatActivity context, final boolean isAdmin, @NonNull final ArrayList<OneOSFile> selectedList, @NonNull final LoginSession loginSession) {
//        loadingCallback.showLoading();
//                            //获取服务器中的用户列表
//                            DeviceSharedUsersHttpLoader loader = new DeviceSharedUsersHttpLoader(SharedUserList.class);
//        loader.setParams(loginSession.getId());
//        loader.executor(new CommonResultListener<SharedUserList>() {
//                                String mgrname;
//
//                                @Override
//            public void success(Object tag, final SharedUserList sharedUserList) {
//                for (ShareUser user : sharedUserList.users) {
//                    if (user.mgrlevel == 0) {
//                        //保存管理员的账号
//                        mgrname = user.username;
//                        break;
//                    }
//                }
//
//                //获取设备中的用户列表
//                final String loginName = loginSession.getUserInfo().getUsername();
//                final OneOSListUserAPI listUserAPI = new OneOSListUserAPI(loginSession);
//                listUserAPI.setOnListUserListener(new OneOSListUserAPI.OnListUserListener() {
//                    @Override
//                    public void onStart(String url) {
//                    }
//
//                    @Override
//                    public void onSuccess(String url, List<OneOSUser> users) {
//                        loadingCallback.dismissLoading();
//                        mUserList.clear();
//                        if (null != users) {
//                            List<OneOSUser> newUsers = new ArrayList<>();
////                            for (OneOSUser user : users) {
////                                if (user.getName().equals("admin")) {
////                                    newUsers.add(user);
////                                    break;
////                                }
////                            }
////                            users.removeAll(newUsers);
//                            for (ShareUser user : sharedUserList.users) {
//                                if (user != null && !TextUtils.isEmpty(user.username)
//                                        && !loginName.equals(user.username)) {
//
//                                    for (OneOSUser oneOSUser : users) {
//                                        if (user.username.equals(oneOSUser.getName())) {
//                                            if (newUsers.contains(oneOSUser)) break;
//                                            //只添加服务器中存在绑定关系的用户
//                                            int isAdmin = user.mgrlevel == 2 ? 0 : 1;
//                                            oneOSUser.setIsAdmin(isAdmin);
//                                            if (TextUtils.isEmpty(oneOSUser.getMarkName())) {
//                                                oneOSUser.setMarkName(user.getFullName());
//                                            }
//                                            switch (user.mgrlevel) {
//                                                case 0:
//                                                    newUsers.add(0, oneOSUser);
//                                                    break;
//                                                case 1:
//                                                    newUsers.add(1, oneOSUser);
//                                                    break;
//                                                case 2:
//                                                    newUsers.add(oneOSUser);
//                                                    break;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            mUserList.addAll(newUsers);
////                            Iterator<OneOSUser> iterator = mUserList.iterator();
////                            while (iterator.hasNext()) {
////                                OneOSUser oneOSUser = iterator.next();
////                                if ((oneOSUser.getName()).equals(mgrname)) {
////                                    iterator.remove();
////                                }
////                            }
//
//                            final String[] osusers = new String[mUserList.size()];
//                            final String[] usersId = new String[mUserList.size()];
//                            final Map<String, String> userMap = new HashMap<>();
//                            for (int i = 0; i < osusers.length; i++) {
//                                osusers[i] = mUserList.get(i).getMarkName();
//                                usersId[i] = mUserList.get(i).getName();
//                                userMap.put(usersId[i], osusers[i]);
//                            }
//
//                            final SharePopupView mShareMenu = new SharePopupView(mActivity);
//                            mShareMenu.setMgrName(mgrname);
//                            mShareMenu.addUsers(mUserList);
//                            mShareMenu.setIsAdmin(isAdmin, new View.OnClickListener() {
//                                @Override
//                                public void onItemClick(View v) {
//                                    mShareMenu.dismiss();
//                                    //邀请并分享新用户
//                                    //检查通讯录权限
//                                    getContacts(selectedList);
//                                }
//                            });
//                            mShareMenu.setOnClickListener(new View.OnClickListener() {
//
//                                @Override
//                                public void onItemClick(View view) {
//                                    final ArrayList<String> shareUser = new ArrayList<>();
//                                    HashMap<Integer, Boolean> select = mShareMenu.getIsSelected();
//                                    for (HashMap.Entry<Integer, Boolean> entry : select.entrySet()) {
//                                        if (entry.getValue()) {
//                                            shareUser.add(usersId[entry.getKey()]);
//                                        }
//                                    }
//
//                                    if (shareUser.size() == 0) {
//                                        ToastHelper.showToast(R.string.tip_please_check_user);
//                                    } else {
//                                        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "value=" + shareUser);
//                                        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "filelist=" + selectedList);
//                                        fileManageAPI.share(selectedList, shareUser);
//
//                                        mShareMenu.dismiss();
//                                    }
//                                }
//
//                            });
//
//                            mShareMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                                @Override
//                                public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
//                                    CheckBox check = view.findViewById(R.id.select_user);
//                                    check.toggle();
//                                    boolean isSelect = check.isChecked();
//                                    mShareMenu.getIsSelected().put(position, isSelect);
//                                    mShareMenu.mAdapter.notifyDataSetChanged();
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(String url, int errorNo, String errorMsg) {
//                        loadingCallback.dismissLoading();
//                    }
//
//                });
//                listUserAPI.list();
//            }
//
//            @Override
//            public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
//                loadingCallback.dismissLoading();
//            }
//        });
        if (mUserModel == null) {
            mUserModel = new UserModel(context.getApplication());
        }
        mUserModel.getMUserList().observe(context, new Observer<List<OneOSUser>>() {
            @Override
            public void onChanged(@Nullable List<OneOSUser> oneOSUsers) {
                mUserList.clear();
                if (oneOSUsers != null)
                    mUserList.addAll(oneOSUsers);
                final String[] osusers = new String[mUserList.size()];
                final String[] usersId = new String[mUserList.size()];
//                final Map<String, String> userMap = new HashMap<>();
                for (int i = 0; i < osusers.length; i++) {
                    osusers[i] = mUserList.get(i).getMarkName();
                    usersId[i] = mUserList.get(i).getName();
//                    userMap.put(usersId[i], osusers[i]);
                }

                final SharePopupView mShareMenu = new SharePopupView(mActivity);
                mShareMenu.addUsers(mUserList);
                mShareMenu.setIsAdmin(isAdmin, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mShareMenu.dismiss();
                        //邀请并分享新用户
                        //检查通讯录权限
                        getContacts(selectedList);
                    }
                });
                mShareMenu.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        final ArrayList<String> shareUser = new ArrayList<>();
                        HashMap<Integer, Boolean> select = mShareMenu.getIsSelected();
                        for (HashMap.Entry<Integer, Boolean> entry : select.entrySet()) {
                            if (entry.getValue()) {
                                shareUser.add(usersId[entry.getKey()]);
                            }
                        }

                        if (shareUser.size() == 0) {
                            ToastHelper.showToast(R.string.tip_please_check_user);
                        } else {
                            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "value=" + shareUser);
                            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "filelist=" + selectedList);
                            fileManageAPI.share(selectedList, shareUser);

                            mShareMenu.dismiss();
                        }
                    }

                });

                mShareMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, @NonNull View view, int position, long arg3) {
                        CheckBox check = view.findViewById(R.id.select_user);
                        check.toggle();
                        boolean isSelect = check.isChecked();
                        mShareMenu.getIsSelected().put(position, isSelect);
                        mShareMenu.mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        mUserModel.getUserList(context, loadingCallback, loginSession.getId(), loginSession, false);
    }

    /**
     * =====================================邀请相关操作===========================================
     */

    private String invitedUser;

    private InvitePopupView invitePopupView;

    private void getContacts(@NonNull final ArrayList<OneOSFile> selectedList) {
        invitePopupView = new InvitePopupView(mActivity);
        invitePopupView.setOnInviteCallBack(new InviteCallBack() {
            @Override
            public void onInviteCallBack(@Nullable SortModel sortModel) {
                showAddUserDialog(selectedList, sortModel != null ? sortModel : new SortModel());
            }
        });
    }

    public void manage(@NotNull FileManageAction action, @Nullable String devID, int sharePathType, @Nullable String path, @NotNull List<OneOSFile> selectedList,OneOSFileType type) {
        this.action = action;
        this.fileList = selectedList;
        this.fileType = type;
        if (action == FileManageAction.DOWNLOAD) {
            if (EmptyUtils.isEmpty(devID) || Objects.equals(devID, SELF)) {
                if (!Utils.isWifiAvailable(mActivity) && SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)) {
                    DialogUtils.showConfirmDialog(mActivity, R.string.tips, R.string.confirm_download_not_wifi, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                            if (isPositiveBtn) {
                                downloadFiles(selectedList);
                            }
                        }
                    });
                } else {
                    downloadFiles(selectedList);
                }
            } else {
                share(mActivity, selectedList, loginSession.getId(), devID, path, sharePathType);
            }
        } else if (action == FileManageAction.MOVE || action == FileManageAction.COPY) {
            copyOrMoveTo(action, path, sharePathType, selectedList);
        }
    }


    private void copyOrMoveTo(FileManageAction action, String tarPath, int des_path_type, List<OneOSFile> selectedList) {
        if (selectedList.size() == 1) {
            OneOSFile oneOSFile = selectedList.get(0);
            if (oneOSFile.isDirectory() && FileUtils.pathIsPrefix(tarPath, oneOSFile.getPath())) {
                ToastHelper.showLongToast(R.string.folder_cannot_be_copied_into_itself);
                return;
            }
        }

        if (SessionCache.Companion.getInstance().isV5(loginSession.getId())) {
            //V5
            Map<Integer, JSONArray> v5MappingData = getV5MappingData();
            int tarPathType = OneOSAPIs.getSharePathType(tarPath);
            String toDir = OneOSAPIs.getV5Path(tarPath);
            V5Observer observer = new V5Observer<Object>(loginSession.getId()) {
                @Override
                public void onSubscribe(@NotNull Disposable d) {
                    super.onSubscribe(d);
                    mListener.onStart("", action);
                }

                @Override
                public void isNotV5() {

                }

                @Override
                public void fail(@NotNull BaseProtocol<Object> result) {
                    mListener.onFailure("", action, result.getError().getCode(),
                            result.getError().getMsg());
                }

                @Override
                public void success(@NotNull BaseProtocol<Object> result) {
                    mListener.onSuccess("", action, "");

                }

            };
            for (Map.Entry<Integer, JSONArray> entry : v5MappingData.entrySet()) {
                V5Repository.Companion.INSTANCE().copyOrMoveFile(loginSession.getId(),
                        loginSession.getIp(), LoginTokenUtil.getToken(), action.name().toLowerCase(Locale.ENGLISH),
                        entry.getValue(), entry.getKey(), toDir, des_path_type, observer);
            }

//                    }
        } else {
            if (loginSession.isV5()) {
                mNasRepository.manageFile(loginSession.getId(), loginSession.getSession(),
                        action, getPaths(selectedList),
                        tarPath, null, null)
                        .observe(mActivity, mObserver);
            } else {
                if (action == FileManageAction.COPY)
                    fileManageAPI.copy(selectedList, tarPath);
                else if (action == FileManageAction.MOVE) {
                    fileManageAPI.move(selectedList, tarPath);
                }
            }
        }
    }

    public interface UserDialogCallBack {
        void onComplete(boolean isSuccess);
    }

    public void showAddUserDialog(@Nullable String number, @NonNull final UserDialogCallBack callBack) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_user_invite, null);
        final Dialog mDialog = new Dialog(mActivity, R.style.DialogTheme);
        final EditText mEditText = dialogView.findViewById(R.id.et_content);
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setText(number);

        final CountryCodePicker ccp = dialogView.findViewById(R.id.country_code);
        if (number != null) {
            ccp.setFullNumber(number);
        }
        ccp.registerCarrierNumberEditText(mEditText);

        Button positiveBtn = dialogView.findViewById(R.id.positive);
        positiveBtn.setVisibility(View.VISIBLE);

        positiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String preTel = ccp.getSelectedCountryCode();
                invitedUser = ccp.getFullNumber();
                final String tel = mEditText.getText().toString().trim();
                String username = SPUtils.getValue(mActivity, AppConstants.SP_FIELD_USERNAME);
                if (tel.isEmpty() || !ccp.isValidFullNumber() || Objects.equals(username, tel)) {
                    Toast.makeText(mActivity, R.string.tip_input_correct_phone_number, Toast.LENGTH_SHORT).show();
                    callBack.onComplete(false);
                    return;
                }
                mDialog.dismiss();

                String deviceId = null;
                deviceId = (SPUtils.getValue(mActivity, AppConstants.SP_FIELD_DEVICE_ID));

                if (deviceId == null) {
                    callBack.onComplete(false);
                    return;
                }

                loadingCallback.showLoading();

                final String finalDeviceId = deviceId;
                OneOSUserManageAPI.OnUserManageListener listener = new OneOSUserManageAPI.OnUserManageListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, String cmd) {
                        //邀请用户
                        InviteUserHttpLoader inviteUserHttpLoader = new InviteUserHttpLoader(InviteUserResultBean.class);
//                        inviteUserHttpLoader.setParams(tel, finalDeviceId, SPUtils.getValue(mActivity, "sn"));
                        inviteUserHttpLoader.setParams(tel, finalDeviceId);
                        inviteUserHttpLoader.executor(new ResultListener<InviteUserResultBean>() {

                            @Override
                            public void success(Object tag, @NonNull InviteUserResultBean inviteUserResultBean) {
                                loadingCallback.dismissLoading();
                                callBack.onComplete(true);
                                notifySuccess(inviteUserResultBean.newuser);
                            }

                            @Override
                            public void error(Object tag, @NonNull GsonBaseProtocol mErrorProtocol) {
                                callBack.onComplete(false);
                                ToastHelper.showToast(mErrorProtocol.errmsg);
                                loadingCallback.dismissLoading();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        loadingCallback.dismissLoading();
                        callBack.onComplete(false);
//                        if ("User is exist".equals(errorMsg)) {
                        errorMsg = HttpErrorNo.getResultMsg(errorNo, errorMsg);
                        ToastHelper.showToast(errorMsg);
                    }
                };
                V5Observer v5Observer = new V5Observer<Object>(loginSession.getId()) {
                    @Override
                    public void isNotV5() {
                        //添加用户
                        OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(loginSession);
                        manageAPI.setOnUserManageListener(listener);
                        manageAPI.add(tel, "123456");
                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        listener.onFailure(null, result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        listener.onSuccess(null, null);
                    }

                    @Override
                    public boolean retry() {
                        V5Repository.Companion.INSTANCE().addUser(loginSession.getId(), loginSession.getIp(), loginSession.getSession(),
                                tel, "123456", 0, this);
                        return true;
                    }
                };

                //添加用户
                V5Repository.Companion.INSTANCE().addUser(loginSession.getId(), loginSession.getIp(), loginSession.getSession(),
                        tel, "123456", 0, v5Observer);

                InputMethodUtils.hideKeyboard(mActivity, mEditText);
            }
        });

        Button negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodUtils.hideKeyboard(mActivity, mEditText);
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }


    private void showAddUserDialog(@NonNull final ArrayList<OneOSFile> fileList, SortModel sortModel) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_user_invite, null);
        final Dialog mDialog = new Dialog(mActivity, R.style.DialogTheme);
        final EditText mEditText = dialogView.findViewById(R.id.et_content);
        final EditText mEditTextName = dialogView.findViewById(R.id.et_content_name);
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setText(sortModel.simpleNumber);
        mEditTextName.setText(sortModel.name);
//        InputMethodUtils.showKeyboard(this, mEditText, 200);

        final CountryCodePicker ccp = dialogView.findViewById(R.id.country_code);
        ccp.registerCarrierNumberEditText(mEditText);

        Button positiveBtn = dialogView.findViewById(R.id.positive);
        positiveBtn.setVisibility(View.VISIBLE);

        positiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Utils.isFastClick(v)) return;
                String preTel = ccp.getSelectedCountryCode();
                invitedUser = ccp.getFullNumber();
                final String tel = invitedUser.substring(preTel.length());
                String username = SPUtils.getValue(mActivity, AppConstants.SP_FIELD_USERNAME);
                if (tel.isEmpty() || !ccp.isValidFullNumber() || Objects.equals(username, tel)) {
                    Toast.makeText(mActivity, R.string.tip_input_correct_phone_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                mDialog.dismiss();
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "onItemClick: invite user preTel = " + preTel);
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "onItemClick: invite user phone = " + tel);

                String deviceId = null;
                deviceId = SPUtils.getValue(mActivity, AppConstants.SP_FIELD_DEVICE_ID);
                if (deviceId == null) {
                    return;
                }

                loadingCallback.showLoading();
                final String finalDeviceId = deviceId;
                OneOSUserManageAPI.OnUserManageListener listener = new OneOSUserManageAPI.OnUserManageListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, String cmd) {
                        //邀请用户
                        InviteUserHttpLoader inviteUserHttpLoader = new InviteUserHttpLoader(InviteUserResultBean.class);
//                        inviteUserHttpLoader.setParams(tel, finalDeviceId, SPUtils.getValue(mActivity, "sn"));
                        inviteUserHttpLoader.setParams(tel, finalDeviceId);
                        inviteUserHttpLoader.executor(new ResultListener<InviteUserResultBean>() {

                            @Override
                            public void success(Object tag, @NonNull InviteUserResultBean inviteUserResultBean) {
                                loadingCallback.dismissLoading();
                                if (fileType == OneOSFileType.PUBLIC) {
                                    notifySuccess(inviteUserResultBean.newuser);
                                } else {
                                    //分享文件
                                    ArrayList<String> shareUser = new ArrayList<>();
                                    shareUser.add(tel);
                                    fileManageAPI.share(fileList, shareUser);
                                    notifySuccess(inviteUserResultBean.newuser, fileList);
                                }
                                if (invitePopupView != null)
                                    invitePopupView.dismiss();
                            }

                            @Override
                            public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                                ToastHelper.showToast(R.string.tip_request_failed);
                                loadingCallback.dismissLoading();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        loadingCallback.dismissLoading();
                        if ("User is exist".equals(errorMsg)) {
                            ArrayList<String> shareUser = new ArrayList<>();
                            shareUser.add(tel);
                            fileManageAPI.share(fileList, shareUser);
                            if (invitePopupView != null)
                                invitePopupView.dismiss();
                        } else {
                            errorMsg = HttpErrorNo.getResultMsg(errorNo, errorMsg);
                            ToastHelper.showToast(errorMsg);
                        }
                    }
                };
                V5Observer v5Observer = new V5Observer<Object>(loginSession.getId()) {
                    @Override
                    public void isNotV5() {
                        OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(loginSession);
                        manageAPI.setOnUserManageListener(listener);
                        manageAPI.add(tel, "123456");
                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        listener.onFailure(null, result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        listener.onSuccess(null, null);
                    }

                    @Override
                    public boolean retry() {
                        V5Repository.Companion.INSTANCE().addUser(loginSession.getId(), loginSession.getIp(), loginSession.getSession(),
                                tel, "123456", 0, this);
                        return true;
                    }
                };

                //添加用户
                V5Repository.Companion.INSTANCE().addUser(loginSession.getId(), loginSession.getIp(), loginSession.getSession(),
                        tel, "123456", 0, v5Observer);

                InputMethodUtils.hideKeyboard(mActivity, mEditText);
            }
        });

        Button negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodUtils.hideKeyboard(mActivity, mEditText);
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private void notifySuccess(boolean isNewUser) {
        notifySuccess(isNewUser, null);
    }

    private void notifySuccess(final boolean isNewUser, final ArrayList<OneOSFile> fileList) {
        int contentId = isNewUser ? R.string.pls_notify_new_user : R.string.add_user_success;
        DialogUtils.showConfirmDialog(mActivity, R.string.tip_invite_user_succeed, contentId, R.string.info_sms_to_user, R.string.cancel,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            //检查短信权限并发送短信
                            String msg = MsgGenerator.genFiles(mActivity, isNewUser, isNewUser, fileList);
                            SmsUtils.init(mActivity).sendInviteSms(invitedUser, msg);
                        }
                    }
                });
    }

    /**
     * =====================================使用系统分享===========================================
     *
     * @param selectedList
     */
    private void shareByOtherWays(@NonNull final List<OneOSFile> selectedList) {
        ArrayList<OneOSFile> oneOSFiles = checkFileDownloaded(selectedList);
        if (oneOSFiles.size() > 0) {
            showHasNotDownloadedFile(selectedList);
        } else {
            if (selectedList.size() > 1) {
                Set<String> heads = new ArraySet<>();
                String regex = "/[a-z0-9*.-]+";
                HashSet<String> types = new HashSet<>();
                ArrayList<Uri> uris = new ArrayList<>();
                boolean isNeedCalc = true;
                for (OneOSFile oneOSFile : selectedList) {
                    File localFile = oneOSFile.getLocalFile();
                    if (localFile != null && localFile.exists()) {
                        Uri uri = FileUtils.getFileProviderUri(localFile);
                        uris.add(uri);
                        if (isNeedCalc) {
                            String type = MIMETypeUtils.getMIMEType(localFile.getName());
                            if ("*/*".equals(type)) {
                                types.add(type);
                                isNeedCalc = false;
                            }
                            types.add(type);
                            String head = type.replaceAll(regex, "");
                            /*
                             * image/png --\
                             *              |-> (image/*);
                             * image/jpg --/
                             *
                             * text/plain --\
                             *               |-> (* /*)
                             * image/png  --/
                             *
                             * */
                            if (!heads.contains(head)) {
                                if (heads.size() > 0) {
                                    types.clear();
                                    types.add("*/*");
                                    isNeedCalc = false;
                                } else {
                                    heads.add(head);
                                    types.add(type);
                                }
                            }

                        }
                    }
                }

                Intent mulIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                mulIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                StringBuilder typeStr = new StringBuilder();
                String next = types.iterator().next();
                if (types.size() > 1) {
                    String type = next.replaceFirst(regex, "/*");
                    typeStr.append(type);
                } else {
                    typeStr.append(next);
                }
                Logger.LOGD(TAG, "type:" + typeStr.toString(), " uris: " + uris.toString());
                mulIntent.setType(typeStr.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mulIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                mActivity.startActivity(Intent.createChooser(mulIntent, ""));
            } else if (selectedList.size() == 1) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                OneOSFile oneOSFile = selectedList.get(0);
                String type = MIMETypeUtils.getMIMEType(oneOSFile.getLocalFile().getName());
                Uri uri = FileUtils.getFileProviderUri(oneOSFile.getLocalFile());
                intent.setType(type);
                Logger.LOGD(TAG, "type:" + type, " uri: " + uri);

                intent.putExtra(Intent.EXTRA_STREAM, uri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                mActivity.startActivity(Intent.createChooser(intent, ""));
            }
        }
    }

    @NonNull
    private ArrayList<OneOSFile> checkFileDownloaded(List<OneOSFile> selectedList) {
        ArrayList<OneOSFile> files = new ArrayList<>();
        for (OneOSFile oneOSFile : selectedList) {
            if (!oneOSFile.hasLocalFile())
                files.add(oneOSFile);
        }
        return files;
    }


    private void showHasNotDownloadedFile(@NonNull final List<OneOSFile> selectedListAll) {
        final ArrayList<OneOSFile> selectedList = checkFileDownloaded(selectedListAll);
        final Dialog dialog = new Dialog(mActivity, R.style.DialogTheme);
        View contentView = mActivity.getLayoutInflater().inflate(R.layout.dialog_show_downloadfile, null);
        final int screenHeight = DisplayUtil.getScreenHeight(contentView.getContext());
        contentView.findViewById(R.id.relativeLayout8).setMinimumHeight(screenHeight * 2 / 3);
        RecyclerView recyclerView = contentView.findViewById(R.id.recycle_view);
        TextView text_title = contentView.findViewById(R.id.text_title);
        text_title.setVisibility(View.VISIBLE);
        text_title.setText(R.string.download);
        final View share = contentView.findViewById(R.id.positive);
        contentView.findViewById(R.id.spit_line).setVisibility(View.GONE);
        share.setVisibility(View.GONE);
        share.setEnabled(false);
        View cancel = contentView.findViewById(R.id.negative);
        final QuickTransmissionAdapter adapter = new QuickTransmissionAdapter(recyclerView.getContext());
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareByOtherWays(selectedListAll);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        recyclerView.setAdapter(adapter);
//        recyclerView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(recyclerView.getContext()));
        RecyclerView.LayoutManager layout = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layout);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), layout.getLayoutDirection()));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(contentView, params);
//        int totalHeight  = recyclerView.computeVerticalScrollRange();
//        int maxHeight = Utils.getWindowsSize(mActivity, false) * 2 / 3;
//        totalHeight = totalHeight > maxHeight ? maxHeight : totalHeight;
//        ViewGroup.LayoutParams params1 = recyclerView.getLayoutParams();
//        params1.height = totalHeight;
//        recyclerView.setLayoutParams(params1);
        dialog.setCancelable(false);
        dialog.show();
        final List<TransferElement> elements = new ArrayList<>(selectedList.size());
        final NasService service = SessionManager.getInstance().getService();
        final List<DownloadElement> downloadElements = new ArrayList<>();
        final HashMap<Integer, OneOSFile> hashcodes = new HashMap<>();
        if (service != null) {
            service.addDownloadCompleteListener(new TransferManager.OnTransferCompleteListener<DownloadElement>() {
                @Override
                public void onComplete(boolean isDownload, @NonNull DownloadElement element) {
                    List<TransferElement> data = adapter.getData();
                    boolean isAllComplete = true;
                    for (OneOSFile oneOSFile : selectedListAll) {
                        if (Objects.equals(oneOSFile.getAllPath(), element.getSrcPath())) {
                            oneOSFile.setLocalFile(element.getDownloadFile());
                        }
                    }
                    for (TransferElement transferElement : data) {
                        if (transferElement.getState() != TransferState.COMPLETE) {
                            isAllComplete = false;
                            break;
                        }
                    }

                    if (isAllComplete) {
//                        share.setEnabled(true);
                        if (dialog.isShowing()) {
                            shareByOtherWays(selectedListAll);
                            dialog.dismiss();
                            for (DownloadElement downloadElement : downloadElements) {
                                service.continueDownload(downloadElement.getTag());
                            }
                        }
                    }
                }
            });
        }
        final Disposable disposable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                if (service != null) {
                    //暂停当前正在下载的
                    List<DownloadElement> downloadList = service.getDownloadList();
                    for (DownloadElement downloadElement : downloadList) {
                        if (downloadElement.getState() == TransferState.START || downloadElement.getState() == TransferState.WAIT) {
                            downloadElements.add(downloadElement);
                            service.pauseDownload(downloadElement.getTag());
                        }
                    }
                    int priority = Priority.UI_NORMAL + selectedList.size();
                    //添加当前未下载的文件
                    for (OneOSFile file : selectedList) {
                        int result = service.addDownloadTask(file, priority--, loginSession.getId());
                        hashcodes.put(result, file);
                    }
                    //开始所有的未下载的任务
                    List<DownloadElement> downloadList2 = service.getDownloadList();
                    for (DownloadElement element : downloadList2) {
                        for (OneOSFile oneOSFile : selectedList) {
                            if (Objects.equals(element.getSrcPath(), oneOSFile.getAllPath())) {
                                service.continueDownload(element.getSrcPath());
                                elements.add(element);
                                break;
                            }

                        }
                    }
                    Collections.sort(elements, new Comparator<TransferElement>() {
                        @Override
                        public int compare(@Nullable TransferElement o1, @Nullable TransferElement o2) {
                            if (o1 != null && o2 != null) {
                                return (o2.getPriority() - o1.getPriority());
                            }
                            return 0;
                        }
                    });
                }
                emitter.onNext(true);
            }
        }).subscribeOn(Schedulers.from(AppExecutors.Companion.getInstance().diskIO()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccess) {
                        if (isSuccess) {
                            adapter.setTransferList(elements, true);
                        }
                    }
                });

        adapter.setOnControlListener(new OnTransferControlListener() {
            @Override
            public void onPause(@NonNull TransferElement element) {
                if (service != null)
                    service.pauseDownload(element.getTag());
            }

            @Override
            public void onContinue(@NonNull TransferElement element) {
                if (service != null)
                    service.continueDownload(element.getTag());
            }

            @Override
            public void onRestart(@NonNull TransferElement element) {
                if (service != null) {
                    service.pauseDownload(element.getTag());
                    service.continueDownload(element.getTag());
                }
            }

            @Override
            public void onCancel(@NonNull TransferElement element) {
                if (service != null) {
                    service.cancelDownload(element.getTag());
                    Iterator<OneOSFile> iterator = selectedListAll.iterator();
                    while (iterator.hasNext()) {
                        final OneOSFile osFile = iterator.next();
                        if (osFile != null) {
                            if (Objects.equals(osFile.getAllPath(), element.getSrcPath()))
                                iterator.remove();
                        }
                    }
                    List<TransferElement> data = adapter.getData();
                    int index = data.indexOf(element);
                    if (index >= 0 && index < data.size())
                        adapter.remove(index);
                    ToastHelper.showToast(mActivity.getString(R.string.cancel_download) + " " + element.getSrcName());
                }
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                disposable.dispose();
                if (service != null) {
                    List<TransferElement> data = adapter.getData();
                    for (TransferElement element : data) {
                        OneOSFile file = hashcodes.get(element.hashCode());
                        if (file != null && Objects.equals(element.getSrcPath(), file.getAllPath())
                                && !TransferState.COMPLETE.equals(element.getState()))
                            service.cancelDownload(element.getTag());

                    }
//                    for (DownloadElement downloadElement : downloadElements) {
//                        if (TransferState.PAUSE.equals(downloadElement.getShareState()))
//                            service.continueDownload(downloadElement.getSrcPath());
//                    }
                }
            }
        });
    }

    @Keep
    public interface OnManageCallback {
        void onComplete(boolean isSuccess);
    }
}
