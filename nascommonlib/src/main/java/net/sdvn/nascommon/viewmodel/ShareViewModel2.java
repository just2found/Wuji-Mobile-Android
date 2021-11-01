package net.sdvn.nascommon.viewmodel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.gson.reflect.TypeToken;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.util.ClipboardUtils;
import net.sdvn.cmapi.util.LogUtils;
import net.sdvn.common.StoreViewUtils;
import net.sdvn.common.internet.SdvnHttpErrorNo;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.GsonBaseProtocolV2;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.SubscribeDeviceHttpLoader;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.common.repo.AccountRepo;
import net.sdvn.nascommon.LibApp;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.db.DBHelper;
import net.sdvn.nascommon.db.LiveDataDelegate;
import net.sdvn.nascommon.db.objecbox.SFDownload;
import net.sdvn.nascommon.db.objecbox.SFDownload_;
import net.sdvn.nascommon.db.objecbox.ShareElementV2;
import net.sdvn.nascommon.db.objecbox.ShareElementV2_;
import net.sdvn.nascommon.fileserver.FileServerApiService;
import net.sdvn.nascommon.fileserver.FileServerHelper;
import net.sdvn.nascommon.fileserver.FileShareBaseResult;
import net.sdvn.nascommon.fileserver.FileShareHelper;
import net.sdvn.nascommon.fileserver.RetrofitFactory;
import net.sdvn.nascommon.fileserver.constants.EntityType;
import net.sdvn.nascommon.fileserver.constants.FS_Config;
import net.sdvn.nascommon.fileserver.constants.FileServerErrorCode;
import net.sdvn.nascommon.fileserver.constants.HttpFileService;
import net.sdvn.nascommon.fileserver.constants.SharePathType;
import net.sdvn.nascommon.fileserver.data.DataCreate;
import net.sdvn.nascommon.fileserver.data.DataDownloadInfo;
import net.sdvn.nascommon.fileserver.data.DataShareDir;
import net.sdvn.nascommon.fileserver.data.DataShareProgress;
import net.sdvn.nascommon.fileserver.data.DataShared;
import net.sdvn.nascommon.fileserver.data.DataSharedInfo;
import net.sdvn.nascommon.iface.Callback;
import net.sdvn.nascommon.iface.ConsumerThrowable;
import net.sdvn.nascommon.iface.HttpException;
import net.sdvn.nascommon.iface.Result;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.MIMETypeUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import io.objectbox.relation.ToMany;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import libs.source.common.utils.Base64;
import okhttp3.ResponseBody;
import timber.log.Timber;

import static net.sdvn.nascommon.fileserver.constants.FS_Config.PAGE_SIZE;

public class ShareViewModel2 extends RxViewModel {
    private static final String TAG = ShareViewModel2.class.getSimpleName();
    private boolean debug = false;
    private final static String DEBUG_ACTION_SUFFIX = "_1";
    private Consumer<? super Disposable> mOnSubscribe = new Consumer<Disposable>() {
        @Override
        public void accept(Disposable disposable) {
            addDisposable(disposable);
        }
    };

    private final BoxStore mBoxStore;
    private LiveDataDelegate<ShareElementV2> mElementV2ObjectBoxLiveData;
    private String userId = null;

    public ShareViewModel2() {
        mBoxStore = DBHelper.getBoxStore();

    }

    public LiveDataDelegate<ShareElementV2> getShareElementV2s() {
        String account = AccountRepo.INSTANCE.getUserId();
        if (mElementV2ObjectBoxLiveData == null || !Objects.equals(userId, account)) {
            userId = account;
            final Query<ShareElementV2> query = mBoxStore
                    .boxFor(ShareElementV2.class)
                    .query()
                    .isNull(ShareElementV2_.userId)
                    .or()
                    .equal(ShareElementV2_.userId, account)
                    .orderDesc(ShareElementV2_.id)
                    .build();
            mElementV2ObjectBoxLiveData = new LiveDataDelegate<>(query);
        }
        return mElementV2ObjectBoxLiveData;
    }

    public LiveDataDelegate<ShareElementV2> getShareElementV2sInComplete() {
        String account = AccountRepo.INSTANCE.getUserId();
        if (mElementV2ObjectBoxLiveData == null || !Objects.equals(userId, account)) {
            userId = account;
            final Query<ShareElementV2> query = mBoxStore
                    .boxFor(ShareElementV2.class)
                    .query()
                    .equal(ShareElementV2_.type, EntityType.SHARE_FILE_V2_RECEIVE)
                    .or()
                    .equal(ShareElementV2_.type, EntityType.SHARE_FILE_V2_COPY)
                    .isNull(ShareElementV2_.userId)
                    .or()
                    .equal(ShareElementV2_.userId, account)
                    .notEqual(ShareElementV2_.state, TransferState.COMPLETE.name())
                    .notEqual(ShareElementV2_.state, TransferState.CANCELED.name())
                    .build();
            mElementV2ObjectBoxLiveData = new LiveDataDelegate<>(query);
        }
        return mElementV2ObjectBoxLiveData;
    }

    public LiveData<ShareElementV2> getShareElementV2s(String devId) {
        MediatorLiveData<ShareElementV2> liveData = new MediatorLiveData<>();
        liveData.addSource(getShareElementV2s(), shareElementV2s -> {
        });
        return liveData;
    }


    /*｛
               "token":"aaaabbb",
               "path":["/xxxxx","/a.xxxx","/bbbb/xxx.zz"],
               "to_user_id":["1893332233","123321313"],
               "period":72,
               "download":10,
               "user_id":"18983099540",
           ｝*/

    public Observable<FileShareBaseResult<DataCreate>>
    create(String devId, final Collection<String> paths,
           int sharePeriodTime, long shareDownloadCounter, @Nullable String password,
           SharePathType sharePathType, String... userIds) {
        final Map<String, Object> map = new ConcurrentHashMap<>();

        map.put("token", LoginTokenUtil.getToken());
//        map.put("user_id", SessionManager.getInstance().getUsername());
        map.put("path", paths);
        if (userIds != null && userIds.length > 0) {
            map.put("to_user_id", userIds);
        }
        map.put("period", sharePeriodTime * 24);
        map.put("download", shareDownloadCounter);
        if (!TextUtils.isEmpty(password)) {
            map.put("password", password);
        }
//        map.put("is_virtual_dir", sharePathType == SharePathType.VIRTUAL);
        map.put("share_path_type", sharePathType.getType());
        final String action = "create";
        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<FileShareBaseResult<DataCreate>>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult<DataCreate>> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService
                                    .create(getAction(action), map)
                                    .map(new Function<FileShareBaseResult<DataCreate>, FileShareBaseResult<DataCreate>>() {
                                        @Override
                                        public FileShareBaseResult<DataCreate> apply(FileShareBaseResult<DataCreate> result) {
                                            if (result.isSuccessful()) {
                                                result.getResult().setPaths(new ArrayList<>(paths));
                                            }
                                            return result;
                                        }
                                    });
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody, FileShareBaseResult<DataCreate>>() {
                                        @Override
                                        public FileShareBaseResult<DataCreate> apply(ResponseBody responseBody) throws Exception {
                                            final FileShareBaseResult<DataCreate> result = GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<DataCreate>>() {
                                                    }.getType());
                                            if (result.isSuccessful()) {
                                                result.getResult().setPaths(new ArrayList<>(paths));
                                            }
                                            return result;

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);

    }

    private Scheduler getScheduler() {
        return AndroidSchedulers.mainThread();
    }

    public Observable<FileShareBaseResult> cancel(String devId, @NonNull String ticket2) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("token", LoginTokenUtil.getToken());
        map.put("ticket_2", ticket2);
        final String action = "cancel";

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<? extends FileShareBaseResult>>() {
                    @Override
                    public ObservableSource<? extends FileShareBaseResult> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService
                                    .cancel(getAction(action), map)
                                    ;
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody, FileShareBaseResult<?>>() {
                                        @Override
                                        public FileShareBaseResult apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    public Observable<FileShareBaseResult<DataShared>> getList(String devId) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("token", LoginTokenUtil.getToken());
        map.put("user_id", SessionManager.getInstance().getUsername());
        final String action = "getList";

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<FileShareBaseResult<DataShared>>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult<DataShared>> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService
                                    .getList(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody, FileShareBaseResult<DataShared>>() {
                                        @Override
                                        public FileShareBaseResult<DataShared> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<DataShared>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    /*  {
            “ticket_1":"aaaaaaaa",
            "to_user_id":"xxxxxxxx"
            "path":"/"
        }*/
    public Observable<FileShareBaseResult<DataShareDir>> getShareDir
    (final String devId, final String toUserId, final String ticket1, final String path,
     final String password, final Context context, int pageSize, int page) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("to_user_id", SessionManager.getInstance().getUsername());
        map.put("token", LoginTokenUtil.getToken());
        map.put("ticket_1", ticket1);
        if (!TextUtils.isEmpty(path))
            map.put("path", path);
        if (!TextUtils.isEmpty(password))
            map.put("password", password);
        if (page > 0) {
            map.put("page", page);
        }
        if (pageSize > 0) {
            map.put("pages", pageSize);
        }
        final String action = "getShareDir";
        return Observable.intervalRange(0, 20,
                0, 500, TimeUnit.MILLISECONDS)
                .flatMap(new Function<Long, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(Long aLong) {
                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> emitter) {
                                Device device = new Device();
                                CMAPI.getInstance().getDeviceById(devId, device);
                                LogUtils.d(TAG, "deviceGetIP  " + device.toString());
                                emitter.onNext(device.getVip() + "");
                            }
                        });
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        return !s.isEmpty();
                    }
                })
                .take(1)
                .flatMap(new Function<String, ObservableSource<FileShareBaseResult<DataShareDir>>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult<DataShareDir>> apply(String s) throws Exception {

                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.getShareDir(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<DataShareDir>>() {
                                        @Override
                                        public FileShareBaseResult<DataShareDir> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<DataShareDir>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .flatMap(new Function<FileShareBaseResult<DataShareDir>, ObservableSource<FileShareBaseResult<DataShareDir>>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult<DataShareDir>>
                    apply(FileShareBaseResult<DataShareDir> fileShareBaseResult) {
                        if (fileShareBaseResult.getStatus() == FileServerErrorCode.MSG_ERROR_AUTH_PASSWORD) {
                            Logger.LOGD(TAG, "get share dir need auth password");
                            if (!TextUtils.isEmpty(password)) {
                                ToastHelper.showLongToastSafe(R.string.tip_password_error_retry);
                            }
                            return getPassword(context)
                                    .flatMap(new Function<String, ObservableSource<FileShareBaseResult<DataShareDir>>>() {
                                        @Override
                                        public ObservableSource<FileShareBaseResult<DataShareDir>> apply(String s) {
                                            return getShareDir(devId, toUserId, ticket1, path, s, context, PAGE_SIZE, 0);
                                        }
                                    });
                        } else if (fileShareBaseResult.isSuccessful()) {
                            DataShareDir result = fileShareBaseResult.getResult();
                            if (result != null) {
                                result.setPassword(password);
                            }
                        }
                        return Observable.just(fileShareBaseResult);
                    }
                }).doOnNext(new Consumer<FileShareBaseResult>() {
                    @Override
                    public void accept(FileShareBaseResult fileShareBaseResult) {
                        if (fileShareBaseResult.isSuccessful()) {
                            final Box<ShareElementV2> shareElementV2Box = mBoxStore.boxFor(ShareElementV2.class);
                            final ShareElementV2 unique = getShareElementV2ByTicket1(shareElementV2Box, ticket1);
                            if (unique != null && !TextUtils.isEmpty(password)) {
                                unique.setPassword(password);
                                putToDB(unique);
                                Logger.LOGD(TAG, "get share dir save password", password);
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    private ShareElementV2 getShareElementV2ByTicket1(Box<ShareElementV2> shareElementV2Box, String ticket1) {
        return shareElementV2Box.query()
                .equal(ShareElementV2_.ticket1, ticket1)
                .isNull(ShareElementV2_.userId)
                .or()
                .equal(ShareElementV2_.userId, DBHelper.getAccount())
                .build()
                .findFirst();
    }

    public Observable<FileShareBaseResult<DataSharedInfo>> getSharedInfo(final String devId, String
            ticket2) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("ticket_2", ticket2);
        map.put("token", LoginTokenUtil.getToken());
        final String action = "getSharedInfo";
        return Observable.intervalRange(0, 20, 0, 500, TimeUnit.MILLISECONDS)
                .flatMap(new Function<Long, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(Long aLong) {
                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> emitter) {
                                Device device = new Device();
                                CMAPI.getInstance().getDeviceById(devId, device);
                                if (!TextUtils.isEmpty(device.getVip())) {
                                    LogUtils.d(TAG, "deviceGetIP  " + device.toString());
                                    emitter.onNext(device.getVip());
                                    emitter.onComplete();
                                } else {
                                    emitter.onError(new NullPointerException("not found device"));
                                }
                            }
                        });
                    }
                }).takeUntil(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        return !TextUtils.isEmpty(s);
                    }
                }).flatMap(new Function<String, Observable<FileShareBaseResult<DataSharedInfo>>>() {
                    @Override
                    public Observable<FileShareBaseResult<DataSharedInfo>> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.getSharedInfo(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<DataSharedInfo>>() {
                                        @Override
                                        public FileShareBaseResult<DataSharedInfo> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<DataSharedInfo>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    //     {
    //             "ticket_2":"aaaaaaa" ,
    //             "to_user_id":"xxxxxxxx"，
    //             ”to_user_path": "/xxx"
    //         }
    public Observable<FileShareBaseResult> download
    (final String devId, final String ticket2, final String path, final List<String> downlaods,
     final boolean isPublicPath, final String password, final Context context) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("to_user_id", SessionManager.getInstance().getUsername());
        map.put("token", LoginTokenUtil.getToken());
        map.put("ticket_2", ticket2);
        if (path != null) {
            map.put("to_user_path", path);
        }
        map.put("is_public_path", isPublicPath ? 1 : 0);

        if (!EmptyUtils.isEmpty(downlaods)) {
            map.put("download_path", downlaods);
        }
        if (!TextUtils.isEmpty(password))
            map.put("password", password);
        final String action = "download";

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<? extends FileShareBaseResult>>() {
                    @Override
                    public ObservableSource<? extends FileShareBaseResult> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.download(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<?>>() {
                                        @Override
                                        public FileShareBaseResult<?> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<?>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .flatMap(new Function<FileShareBaseResult, ObservableSource<FileShareBaseResult>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult> apply(FileShareBaseResult fileShareBaseResult) {
                        if (fileShareBaseResult.getStatus() == FileServerErrorCode.MSG_ERROR_AUTH_PASSWORD) {
                            Logger.LOGD(TAG, "get share dir need auth password");
                            if (!TextUtils.isEmpty(password)) {
                                ToastHelper.showLongToastSafe(R.string.tip_password_error_retry);
                            }
                            return getPassword(context)
                                    .flatMap(new Function<String, ObservableSource<FileShareBaseResult>>() {
                                        @Override
                                        public ObservableSource<FileShareBaseResult> apply(String s) {
                                            return download(devId, ticket2, path, downlaods, isPublicPath, s, context);
                                        }
                                    });
                        }
                        return Observable.just(fileShareBaseResult);
                    }
                })
                .doOnNext(new Consumer<FileShareBaseResult>() {
                    @Override
                    public void accept(FileShareBaseResult fileShareBaseResult) {
                        if (fileShareBaseResult.isSuccessful()) {
                            final Box<ShareElementV2> shareElementV2Box = mBoxStore.boxFor(ShareElementV2.class);
                            final ShareElementV2 unique = findElementV2ByTicket2(shareElementV2Box, ticket2);
                            if (unique != null && !TextUtils.isEmpty(password)) {
                                unique.setPassword(password);
                                putToDB(unique);
                            }
                            doGetList(devId);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    public Observable<String> getPassword(final Context context) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                final String[] result = new String[1];
                getScheduler().createWorker().schedule(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.showEditDialog(context, 0, R.string.hint_enter_pwd,
                                0, R.string.confirm, R.string.cancel,
                                new DialogUtils.OnEditDialogClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, boolean isPositiveBtn, EditText mEditTextNew) {
                                        if (isPositiveBtn) {
                                            final String trim = mEditTextNew.getText().toString().trim();
                                            Logger.LOGD(TAG, "get share dir show input auth password 2");
                                            if (TextUtils.isEmpty(trim)) {
                                                AnimUtils.sharkEditText(context, mEditTextNew);
                                                mEditTextNew.requestFocus();
                                                return;
                                            }
                                            result[0] = trim;
                                            dialog.dismiss();
                                            Logger.LOGD(TAG, "get share dir show input auth password 3");
                                        }
                                        countDownLatch.countDown();
                                    }
                                });
                        Logger.LOGD(TAG, "get share dir show input auth password");
                    }
                });
                countDownLatch.await();
                if (!TextUtils.isEmpty(result[0])) {
                    emitter.onNext(result[0]);
                } else {
                    emitter.onError(new NullPointerException("user cancel input"));
                    emitter.onComplete();
                }
            }
        });
    }

    public Observable<FileShareBaseResult> addDownloadPath(String devId, String
            download_ticket,
                                                           List<String> download_paths) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("download_ticket", download_ticket);
        if (!EmptyUtils.isEmpty(download_paths)) {
            map.put("download_path", download_paths);
        }
        map.put("token", LoginTokenUtil.getToken());
        final String action = "addDownloadPath";

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<? extends FileShareBaseResult>>() {
                    @Override
                    public ObservableSource<? extends FileShareBaseResult> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.request(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<?>>() {
                                        @Override
                                        public FileShareBaseResult<?> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<?>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    public Observable<FileShareBaseResult> cancelDownload(String devId, @NonNull String...
            ticket) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("token", LoginTokenUtil.getToken());
        map.put("tickets", ticket);
        final String action = "cancelDownload";

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<? extends FileShareBaseResult>>() {
                    @Override
                    public ObservableSource<? extends FileShareBaseResult> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.request(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<?>>() {
                                        @Override
                                        public FileShareBaseResult<?> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<?>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    public Observable<FileShareBaseResult> pauseDownload(String devId, @NonNull String ticket) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("token", LoginTokenUtil.getToken());
        map.put("ticket", ticket);
        final String action = "stopDownload";

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<? extends FileShareBaseResult>>() {
                    @Override
                    public ObservableSource<? extends FileShareBaseResult> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.request(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<?>>() {
                                        @Override
                                        public FileShareBaseResult<?> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<?>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    public Observable<FileShareBaseResult> resumeDownload(String devId, @NonNull String ticket) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("token", LoginTokenUtil.getToken());
        map.put("ticket", ticket);
        final String action = "resumeDownload";

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<? extends FileShareBaseResult>>() {
                    @Override
                    public ObservableSource<? extends FileShareBaseResult> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.request(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<?>>() {
                                        @Override
                                        public FileShareBaseResult<?> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<?>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);
    }

    public Observable<FileShareBaseResult<DataShareProgress>> progress(String devId, String
            ticket) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("ticket", ticket);
        map.put("token", LoginTokenUtil.getToken());
        final String action = "progress";
        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<FileShareBaseResult<DataShareProgress>>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult<DataShareProgress>> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);
                        if (debug) {
                            return apiService.progress(getAction(action), map);
                        } else {
                            return apiService
                                    .requestEncrypt(getAction(action), encrypt(map))
                                    .map(new Function<ResponseBody,
                                            FileShareBaseResult<DataShareProgress>>() {
                                        @Override
                                        public FileShareBaseResult<DataShareProgress> apply(ResponseBody responseBody) throws Exception {
                                            return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                    new TypeToken<FileShareBaseResult<DataShareProgress>>() {
                                                    }.getType());

                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);

    }

    //    {
//        "ticket":"dl-1063_ccdade0722f29bd1487e0c7d2b2e11b0",
//            "path":"/"
//    }
    public Observable<FileShareBaseResult<DataDownloadInfo>> getDownloadInfo(String devId,
                                                                             String ticket,
                                                                             int infoId,
                                                                             String path,
                                                                             int pageSize,
                                                                             int page) {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("ticket", ticket);
        map.put("info_id", infoId);
        if (path != null)
            map.put("info_args", path);
        final String action = "getDownloadInfo";
        if (page > 0) {
            map.put("page", page);
        }
        if (pageSize > 0) {
            map.put("pages", pageSize);
        }

        return getVipById(devId)
                .flatMap(new Function<String, ObservableSource<FileShareBaseResult<DataDownloadInfo>>>() {
                    @Override
                    public ObservableSource<FileShareBaseResult<DataDownloadInfo>> apply(String s) throws Exception {
                        final FileServerApiService apiService = RetrofitFactory.createRetrofit(getHost(s))
                                .create(FileServerApiService.class);

                        return apiService
                                .requestEncrypt(getAction(action), encrypt(map))
                                .map(new Function<ResponseBody,
                                        FileShareBaseResult<DataDownloadInfo>>() {
                                    @Override
                                    public FileShareBaseResult<DataDownloadInfo> apply(ResponseBody responseBody) throws Exception {
                                        return GsonUtils.decodeJSONCatchException(responseBody.string(),
                                                new TypeToken<FileShareBaseResult<DataDownloadInfo>>() {
                                                }.getType());

                                    }
                                });

                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(mOnSubscribe);

    }

    private Observable<String> getVipById(final String devId) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(devId);
                String vip = null;
                if (deviceModel != null) {
                    final Device device = deviceModel.getDevice();
                    if (device != null) {
                        vip = device.getVip();
                    }
                } else {
                    throw new HttpException(FileServerErrorCode.ERR_DEVICE_NOT_FOUND, String.format("device(%s) not found", devId + ""));
                }
                Logger.LOGD(TAG, String.format("device vip is %s", vip + ""));
                if (!TextUtils.isEmpty(vip)) {
                    emitter.onNext(vip);
                } else {
                    throw new HttpException(FileServerErrorCode.ERR_DEVICE_OFFLINE, String.format("device(%s) offline", devId + ""));
                }
            }
        });
    }

    private String getHost(String sourceIp) {
        return HttpFileService.getHost(sourceIp);
    }

    public void subscribeDevice(final String t2, final boolean isAdd, final Consumer<ShareElementV2> consumer) {
        if (!isAdd) {
            final Box<ShareElementV2> shareElementV2Box = DBHelper.getBoxStore()
                    .boxFor(ShareElementV2.class);
            ShareElementV2 elementV2 = findElementV2ByTicket2(shareElementV2Box, t2);
            if (elementV2 != null && !TextUtils.isEmpty(elementV2.getSrcDevId())) {
                Device device = new Device();
                CMAPI.getInstance().getDeviceById(elementV2.getSrcDevId(), device);
                if (!TextUtils.isEmpty(device.getVip())) {
                    try {
                        consumer.accept(elementV2);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        SubscribeDeviceHttpLoader subscribeDeviceHttpLoader = new SubscribeDeviceHttpLoader(GsonBaseProtocolV2.class);
        subscribeDeviceHttpLoader.subscribe(t2, null);
        subscribeDeviceHttpLoader.executor(new ResultListener<GsonBaseProtocolV2>() {
            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                ToastHelper.showToast(SdvnHttpErrorNo.ec2String(baseProtocol.result));
                if (baseProtocol.result == SdvnHttpErrorNo.EC_INVALID_SHARE_T2) {
                    final Box<ShareElementV2> shareElementV2Box = DBHelper.getBoxStore()
                            .boxFor(ShareElementV2.class);
                    ShareElementV2 elementV2 = findElementV2ByTicket2(shareElementV2Box, t2);
                    if (elementV2 != null) {
                        elementV2.setState(TransferState.CANCELED);
                        try {
                            consumer.accept(elementV2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            /*"data":{
                    "t1":"",
                    "deviceid":""
                }*/
            @Override
            public void success(Object tag, GsonBaseProtocolV2 data) {
                try {
                    JSONObject jsonObject = new JSONObject(GsonUtils.encodeJSON(data.data));
                    final String t1 = jsonObject.getString("t1");
                    final String devId = jsonObject.getString("deviceid");
                    final Box<ShareElementV2> shareElementV2Box = DBHelper.getBoxStore()
                            .boxFor(ShareElementV2.class);
                    ShareElementV2 elementV2 = findElementV2ByTicket2(shareElementV2Box, t2);
                    int resId = R.string.tips_add_share_files;
                    if (elementV2 == null) {
                        elementV2 = new ShareElementV2(t1, t2, devId, EntityType.SHARE_FILE_V2_RECEIVE);
                        putToDB(elementV2);
                    } else {
                        resId = R.string.tips_added;
                        if (EmptyUtils.isEmpty(elementV2.getSrcDevId())
                                || EmptyUtils.isEmpty(elementV2.getTicket1())) {
                            elementV2.setSrcDevId(devId);
                            elementV2.setTicket1(t1);
                            putToDB(elementV2);
                        }
                    }
                    if (isAdd) {
                        ToastHelper.showToast(resId);
                    }
                    if (consumer != null) {
                        consumer.accept(elementV2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getAction(String action) {
        if (debug)
            action += DEBUG_ACTION_SUFFIX;
        return action;
    }


    public void refreshShare() {
        final List<DeviceModel> deviceModels = SessionManager.getInstance().getDeviceModels();
        for (DeviceModel deviceModel : deviceModels) {
            if (deviceModel != null && deviceModel.isOnline() && deviceModel.isShareV2Available()) {
                doGetList(deviceModel.getDevId());
            }
        }
    }

    public void doGetList(final String devId) {
        final Disposable subscribe = getList(devId)
                .observeOn(Schedulers.single())
                .subscribe(new Consumer<FileShareBaseResult<DataShared>>() {
                    @Override
                    public void accept(FileShareBaseResult<DataShared> dataSharedFileShareBaseResult) {
                        if (dataSharedFileShareBaseResult.isSuccessful()) {
                            final DataShared result = dataSharedFileShareBaseResult.getResult();
                            if (result != null) {
                                final Box<ShareElementV2> shareElementV2Box = DBHelper.getBoxStore()
                                        .boxFor(ShareElementV2.class);
                                final Box<SFDownload> sfDownloadBox = DBHelper.getBoxStore()
                                        .boxFor(SFDownload.class);
                                final List<ShareElementV2> sharedList = result.getSharedList();
                                List<ShareElementV2> updates = new ArrayList<>();
                                List<SFDownload> updatesDownload = new ArrayList<>();
                                if (sharedList != null) {
                                    for (ShareElementV2 v2 : sharedList) {
                                        if (v2 == null || EmptyUtils.isEmpty(v2.getTicket2()))
                                            continue;
                                        ShareElementV2 elementV2 = findElementV2ByTicket2(shareElementV2Box, v2.getTicket2());
                                        if (elementV2 == null) {
                                            elementV2 = v2;
                                        } else {
                                            elementV2.setPath(v2.getPath());
                                            elementV2.setTicket1(v2.getTicket1());
                                            elementV2.setTimestamp(v2.getTimestamp());
                                            elementV2.setMaxDownload(v2.getMaxDownload());
                                            elementV2.setRemainDownload(v2.getRemainDownload());
                                            elementV2.setRemainPeriod(v2.getRemainPeriod());
                                            elementV2.setPassword(v2.getPassword());
                                            elementV2.setSharePathType(v2.getSharePathType());
                                        }
                                        elementV2.setSrcDevId(devId);
                                        elementV2.setType(EntityType.SHARE_FILE_V2_SEND);
                                        updates.add(elementV2);

                                    }
                                }
                                final List<ShareElementV2> downloadList = result.getDownloadList();
                                if (downloadList != null) {
                                    for (ShareElementV2 v2 : downloadList) {
                                        if (v2 == null || EmptyUtils.isEmpty(v2.getTicket2()))
                                            continue;
                                        ShareElementV2 elementV2 = findElementV2ByTicket2(shareElementV2Box, v2.getTicket2());
                                        SFDownload sfDownload = getSFDownloadById(sfDownloadBox, v2);
                                        if (elementV2 == null) {
                                            elementV2 = v2;
                                            putToDB(elementV2);
                                        } else {
                                            elementV2.setTimestamp(v2.getTimestamp());
                                            elementV2.setToPath(v2.getToPath());
                                        }
                                        if (sfDownload == null) {
                                            sfDownload = new SFDownload();
                                            sfDownload.getShareElementV2().setTarget(elementV2);
                                            putInDB(sfDownload);
                                        }
                                        sfDownload.setToDevId(devId);
                                        sfDownload.setToken(v2.getDownloadId());
                                        sfDownload.setToPath(v2.getToPath());
                                        sfDownload.setTimestamp(v2.getTimestamp());
//                                        elementV2.setToDevId(devId);
//                                        elementV2.setDownloadId(v2.getDownloadId());
                                        elementV2.setType(EntityType.SHARE_FILE_V2_RECEIVE);
                                        elementV2.getSFDownloads().add(sfDownload);
                                        updatesDownload.add(sfDownload);
                                        updates.add(elementV2);
                                    }
                                }
                                putToDB(updates.toArray(new ShareElementV2[0]));
                                sfDownloadBox.put(updatesDownload);
                            }
                        }
                    }
                }, new ConsumerThrowable(TAG, "share getList"));
        addDisposable(subscribe);
    }

    private SFDownload getSFDownloadById(Box<SFDownload> sfDownloadBox, ShareElementV2 v2) {
        return sfDownloadBox.query()
                .equal(SFDownload_.token, v2.getDownloadId())
                .build()
                .findFirst();
    }

    private ShareElementV2 findElementV2ByTicket2(Box<ShareElementV2> shareElementV2Box, String ticket2) {
        return shareElementV2Box
                .query()
                .equal(ShareElementV2_.ticket2, ticket2)
                .isNull(ShareElementV2_.userId)
                .or()
                .equal(ShareElementV2_.userId, DBHelper.getAccount())
                .build()
                .findFirst();
    }


    public void showQRCode(Context context, String ticket2,
                           String sharePeriodTime, long shareDownloadCounter, String password) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_share_code_v2, null);
        final ImageView ivQrcode = view.findViewById(R.id.act_share_code_iv_qr);
        final TextView btnShare = view.findViewById(R.id.btn_share);
        final View store = view.findViewById(R.id.share_code_container);
        final TextView sc_tips_content = view.findViewById(R.id.sc_tips_content);
        final TextView share_counter = view.findViewById(R.id.share_counter);
        final TextView share_password = view.findViewById(R.id.share_password);
        final String content = context.getResources().getString(R.string.downloadURL) + "#filesc=" + ticket2;

        final Disposable subscribe = Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Bitmap> e) {
                Logger.LOGD(TAG, "view's width:" + view.getMeasuredWidth());
                Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(content,
                        context.getResources().getDisplayMetrics().widthPixels * 3 / 5);
                e.onNext(bitmap);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) {
                        ivQrcode.setImageBitmap(bitmap);
                        saveImageToFile(context, store, btnShare, ticket2);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Logger.LOGE(TAG, throwable, "gen share bitmap");
                    }
                });
        sc_tips_content.setText(sharePeriodTime);
        final String format = String.format("%s %s",
                shareDownloadCounter == FS_Config.CODE_DOWNLOAD_TIMES_UNLIMITED ?
                        context.getString(R.string.unlimited) : shareDownloadCounter,
                shareDownloadCounter == FS_Config.CODE_DOWNLOAD_TIMES_UNLIMITED ?
                        "" : context.getString(R.string.times));
        Logger.LOGD(TAG, "share code tips " + format);
        share_counter.setText(format);
        share_password.setText(TextUtils.isEmpty(password) ? "" : password);
        final String value = TextUtils.isEmpty(password)
                ? context.getString(R.string.empty)
                : String.format("%s:", context.getString(R.string.password));
        ((TextView) view.findViewById(R.id.textView4)).setText(value);
        share_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof TextView) {
                    String content = ((TextView) v).getText().toString().trim();
                    ClipboardUtils.copyToClipboard(v.getContext(), content);
                    ToastHelper.showLongToastSafe(R.string.copied);
                }
            }
        });

        final Dialog dialog = DialogUtils.showCustomDialog(context, view);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                subscribe.dispose();
            }
        });
        view.findViewById(R.id.im_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }


    private void saveImageToFile(Context context, View store, View opt, String
            s) {
        File mCacheDir = new File(FileUtils.getIconDir());
        if (!mCacheDir.exists())
            mCacheDir.mkdir();
        LogUtils.d("Share Code img", "cachepath : " + mCacheDir);
        String fileName = s + ".png";
        final File file = new File(mCacheDir, fileName);
        if (!file.exists()) {
            final Observable<Boolean> observable = new StoreViewUtils().viewSaveToImage(
                    store, file, "");
            if (observable != null)
                observable.subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (opt != null)
                            opt.setEnabled(aBoolean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (opt != null)
                            opt.setEnabled(false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        } else opt.setEnabled(true);
        opt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isFastClick(v, 600)) return;
                final File file = new File(mCacheDir, fileName);
                if (!file.exists()) {
                    return;
                }
                try {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileUtils.getFileProviderUri(file));
                    shareIntent.setType(MIMETypeUtils.getMIMEType(file.getName()));
                    context.startActivity(Intent.createChooser(shareIntent,
                            context.getResources().getText(R.string.share_to)));
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastHelper.showLongToast(R.string.error_app_not_found_to_open_file);
                }
            }
        });

    }

    public void putInDB(SFDownload... sfDownloads) {
        LibApp.Companion.getInstance().getAppExecutors().diskIO()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (SFDownload elementV2 : sfDownloads) {
                                elementV2.setUserId(DBHelper.getAccount());
                            }
                            DBHelper.getBoxStore()
                                    .boxFor(SFDownload.class)
                                    .put(sfDownloads);
                            Logger.LOGD(TAG, "putToDB");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    public void putToDB(final ShareElementV2... shareElementV2) {
        LibApp.Companion.getInstance().getAppExecutors().diskIO()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (ShareElementV2 elementV2 : shareElementV2) {
                                elementV2.setUserId(DBHelper.getAccount());
                            }
                            DBHelper.getBoxStore()
                                    .boxFor(ShareElementV2.class)
                                    .put(shareElementV2);
                            Logger.LOGD(TAG, "putToDB");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

    }


    public void removeFromDB(final ShareElementV2... shareElementV2) {
        LibApp.Companion.getInstance().getAppExecutors().diskIO()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        final BoxStore boxStore = DBHelper.getBoxStore();
                        final Box<SFDownload> sfDownloadBox = boxStore.boxFor(SFDownload.class);
                        final Box<ShareElementV2> shareElementV2Box = boxStore.boxFor(ShareElementV2.class);
                        List<SFDownload> sfDownloads1 = new ArrayList<>();
                        for (ShareElementV2 elementV2 : shareElementV2) {
                            if (elementV2.getType() == EntityType.SHARE_FILE_V2_RECEIVE) {
                                final ToMany<SFDownload> sfDownloads = elementV2.getSFDownloads();
                                sfDownloads1.addAll(sfDownloads);
                                sfDownloads.clear();
                                sfDownloads.applyChangesToDb();
                            }
                        }
                        sfDownloadBox.remove(sfDownloads1);
                        shareElementV2Box.remove(shareElementV2);
                        Logger.LOGD(TAG, "removeFromDB");
                    }
                });

    }

    public void showRemoveDialog(Context context, ShareElementV2
            mShareElementV2, Consumer<FileShareBaseResult> consumer) {

        if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
            DialogUtils.showConfirmDialog(context,
                    R.string.waring_remove_title,
                    mShareElementV2.getErrNo() > 0 ? FileServerErrorCode.getResId(mShareElementV2.getErrNo()) : 0,
                    R.string.confirm,
                    R.string.cancel,
                    new DialogUtils.OnDialogClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                            if (isPositiveBtn) {
                                doWithConsumer(removeShareItem(mShareElementV2), consumer);

                            }
                        }
                    }
            );

        } else {
            DialogUtils.showConfirmDialog(context, R.string.to_cancel_share, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                @Override
                public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                    if (isPositiveBtn) {
                        doWithConsumer(removeShareItem(mShareElementV2), consumer);
                    }
                }
            });
        }
    }

    private void doWithConsumer(Observable<FileShareBaseResult> removeShareItem, Consumer<FileShareBaseResult> consumer) {
        addDisposable(removeShareItem
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.isSuccessful()) {
                                consumer.accept(result);
                            } else {
                                ToastHelper.showToast(R.string.operate_failed);
                                try {
                                    consumer.accept(result);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        throwable -> {
                            ToastHelper.showToast(R.string.operate_failed);
                        }));
    }

    private Observable<FileShareBaseResult> removeShareItem(ShareElementV2 mShareElementV2) {
        if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_RECEIVE)) {
            if (!mShareElementV2.getSFDownloads().isEmpty()) {
                final Iterator<SFDownload> iterator = mShareElementV2.getSFDownloads().iterator();
                return Observable.just(iterator)
                        .map(new Function<Iterator<SFDownload>, Map<String, List<String>>>() {
                            @Override
                            public Map<String, List<String>> apply(Iterator<SFDownload> sfDownloadIterator) {
                                Map<String, List<String>> map = new HashMap<>();
                                while (iterator.hasNext()) {
                                    final SFDownload next = iterator.next();
                                    if (next != null && !TextUtils.isEmpty(next.getToken())
                                            && !TextUtils.isEmpty(next.getToDevId())) {
                                        List<String> strings = map.get(next.getToDevId());
                                        if (strings == null) {
                                            strings = new ArrayList<>();
                                            map.put(next.getToDevId(), strings);
                                        }
                                        strings.add(next.getToken());
                                    }
                                }
                                return map;
                            }
                        })
                        .flatMap(new Function<Map<String, List<String>>, ObservableSource<Map.Entry<String, List<String>>>>() {
                            @Override
                            public ObservableSource<Map.Entry<String, List<String>>> apply(Map<String, List<String>> stringListMap) {
                                return Observable.fromIterable(stringListMap.entrySet());
                            }
                        })
                        .filter(new Predicate<Map.Entry<String, List<String>>>() {
                            @Override
                            public boolean test(Map.Entry<String, List<String>> stringListEntry) {
                                return stringListEntry.getValue().size() > 0;
                            }
                        })
                        .flatMap(new Function<Map.Entry<String, List<String>>, ObservableSource<FileShareBaseResult>>() {
                            @Override
                            public ObservableSource<FileShareBaseResult> apply(Map.Entry<String, List<String>> stringListEntry) {
                                return cancelDownload(stringListEntry.getKey(), stringListEntry.getValue().toArray(new String[0]));
                            }
                        })
                        .flatMap(result -> {
                            if (mShareElementV2.isType(EntityType.SHARE_FILE_V2_COPY)) {
                                final Observable<FileShareBaseResult> observable = cancel(mShareElementV2.getSrcDevId(), mShareElementV2.getTicket2());
                                addDisposable(observable.subscribe(new Consumer<FileShareBaseResult>() {
                                    @Override
                                    public void accept(FileShareBaseResult result) throws Exception {
                                        if (result.isSuccessful()
                                                || result.getStatus() == FileServerErrorCode.MSG_ERROR_NO_TASK
                                                || result.getStatus() == FileServerErrorCode.MSG_ERROR_CANCEL_SHARED) {
                                            removeFromDB(mShareElementV2);
                                            result.setStatus(FileServerErrorCode.MSG_OK);
                                        }
                                    }
                                }, Timber::e));
                            }
                            if (result.isSuccessful()
                                    || result.getStatus() == FileServerErrorCode.MSG_ERROR_NO_TASK
                                    || result.getStatus() == FileServerErrorCode.MSG_ERROR_CANCEL_SHARED) {
                                removeFromDB(mShareElementV2);
                                result.setStatus(FileServerErrorCode.MSG_OK);
                            }
                            return Observable.just(result);
                        });

            } else {
                return Observable.create(emitter -> {
                            removeFromDB(mShareElementV2);
                            final FileShareBaseResult result = new FileShareBaseResult();
                            result.setStatus(FileServerErrorCode.MSG_OK);
                            emitter.onNext(result);
                        }
                );

            }
        } else {
            return cancel(mShareElementV2.getSrcDevId(), mShareElementV2.getTicket2())
                    .flatMap(result -> {
                        if (result.isSuccessful()
                                || result.getStatus() == FileServerErrorCode.MSG_ERROR_NO_TASK
                                || result.getStatus() == FileServerErrorCode.MSG_ERROR_CANCEL_SHARED) {
                            removeFromDB(mShareElementV2);
                            result.setStatus(FileServerErrorCode.MSG_OK);
                        }
                        return Observable.just(result);
                    });
        }
    }


    private String encrypt(Map<String, Object> map) throws Exception {
//        String appid = "d1EJnGGF0Eyn6Wxx";
//        String appkey = "6LJx909Q9KVBkVZC";
//        String aeskey = "OsBhtflT0P2WaOVY";
//        String jsonData = GsonUtils.encodeJSON(map);
//        Timber.tag(TAG).d("http_body :%s", jsonData);
//        final String encryptText = appid + jsonData;
//        final byte[] src = HMACSHA1.hmacSHA1(encryptText, appkey);
//        String sign = base64EncodeToString(Md5Utils.hexString(src).getBytes());
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("appid", appid);
//        params.put("sign", sign);
//        params.put("data", map);
//        String data2 = GsonUtils.encodeJSON(params);
//        Timber.d(data2);
//        final byte[] iv = new SecureRandom().generateSeed(16);
////        final byte[] iv =  "0000000000000000".getBytes();
//        byte[] encode = CryptAES.encode(aeskey.getBytes(), iv, data2.getBytes());
//        return base64EncodeToString(CryptAES.concat(iv, encode));
        return FileServerHelper.INSTANCE.encrypt(map);
    }

    private String base64EncodeToString(byte[] src) {
        String encode = Base64.encodeToString(src, Base64.DEFAULT);
        return encode.replaceAll("\n", "").replaceAll("\r", "").trim();
    }

    public void cancelAllCanceled(List<ShareElementV2> list) {
        if (list != null) {
            addDisposable(Observable.just(list)
                    .flatMap(new Function<List<ShareElementV2>, ObservableSource<ShareElementV2>>() {
                        @Override
                        public ObservableSource<ShareElementV2> apply(List<ShareElementV2> shareElementV2s) {
                            removeFromDB(shareElementV2s.toArray(new ShareElementV2[0]));
                            return Observable.fromIterable(shareElementV2s);
                        }
                    })
                    .flatMap(new Function<ShareElementV2, ObservableSource<FileShareBaseResult>>() {
                        @Override
                        public ObservableSource<FileShareBaseResult> apply(ShareElementV2 shareElementV2) {
                            return removeShareItem(shareElementV2);
                        }
                    }, new BiFunction<ShareElementV2, FileShareBaseResult, ShareElementV2>() {
                        @Override
                        public ShareElementV2 apply(ShareElementV2 shareElementV2, FileShareBaseResult fileShareBaseResult) {
                            if (fileShareBaseResult.isSuccessful())
                                return shareElementV2;
                            return null;
                        }
                    })
                    .filter(new Predicate<ShareElementV2>() {
                        @Override
                        public boolean test(ShareElementV2 shareElementV2) {
                            return shareElementV2 != null;
                        }
                    })
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<List<ShareElementV2>>() {
                        @Override
                        public void accept(List<ShareElementV2> shareElementV2s) {
                            Logger.LOGD(TAG, shareElementV2s);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Logger.LOGI(TAG, throwable, "one key clear exp");
                        }
                    }));

        }
    }

    @NonNull
    public Disposable version(@Nullable String toDevId, @NonNull Callback<Result<Boolean>> callback) {
        return getVipById(toDevId)
                .flatMap(FileShareHelper::version)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shareBaseResult -> {
                    if (shareBaseResult.isSuccessful()) {
                        Result<Boolean> result = new Result<>(true);
                        result.msg = shareBaseResult.getResult().getVersion();
                        callback.result(result);
                    } else {
                        Result<Boolean> t = new Result<>(shareBaseResult.getStatus(), shareBaseResult.getMsg());
                        callback.result(t);
                    }
                }, Timber::e);

    }
}


