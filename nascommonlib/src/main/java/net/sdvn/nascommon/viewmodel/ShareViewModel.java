package net.sdvn.nascommon.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.SdvnHttpErrorNo;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.listener.CommonResultListener;
import net.sdvn.common.internet.loader.GetDownloadHttpLoader;
import net.sdvn.common.internet.loader.GetShareFilesHttpLoader;
import net.sdvn.common.internet.loader.RemoveDownloadTokenHttpLoader;
import net.sdvn.common.internet.loader.RemoveShareTokenHttpLoader;
import net.sdvn.common.internet.protocol.DownloadTokenResultBean;
import net.sdvn.common.internet.protocol.GetShareFilesResultBean;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.DBHelper;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.iface.EventListener;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.ShareFileInfo;
import net.sdvn.nascommon.model.oneos.api.share.OneOSGetShareFileInfoAPI;
import net.sdvn.nascommon.model.oneos.api.share.OneOSNotifyShareFileAPI;
import net.sdvn.nascommon.model.oneos.api.share.ShareFileRecordsAPI;
import net.sdvn.nascommon.model.oneos.transfer.ShareElement;
import net.sdvn.nascommon.model.oneos.transfer.ShareElement_;
import net.sdvn.nascommon.model.oneos.transfer.TransferException;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.receiver.NetworkStateManager;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.exception.UniqueViolationException;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ShareViewModel extends AndroidViewModel {
    public static final String TAG = ShareViewModel.class.getSimpleName();
    private static final long TIME_RECORDS_REFRESH = 10 * 1000; //10s刷新一次下载记录
    public final MutableLiveData<List<ShareElement>> mServerShareElements = new MutableLiveData<>();
    public static final int BEAT_RUNNING_NORMAL_TIME = 1;
    @NonNull
    private final androidx.lifecycle.Observer<List<DeviceModel>> mObserver;
    @NonNull
    private Set<DeviceModel> mDeviceModels = new CopyOnWriteArraySet<>();
    private boolean isInit;
    private final Set<String> mDeleteList = new CopyOnWriteArraySet<>();
    private final Set<ShareElement> mShareList = new CopyOnWriteArraySet<>();
    private CompositeDisposable mCompositeDisposable;
    private final HashMap<String, Integer> mMapDataShareFileRecords = new HashMap<>();
    private Comparator<ShareElement> mShareElementComparator;
    private boolean mIsAvailable;
    private boolean mIsWifiAvailable;
    private boolean isRefresh;
    private int m_nXPeriod = -1;

    protected void addDisposable(@NonNull Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    public void dispose() {
        if (mCompositeDisposable != null) mCompositeDisposable.dispose();
    }

    public ShareViewModel(@NonNull Application application) {
        super(application);
        mObserver = new androidx.lifecycle.Observer<List<DeviceModel>>() {
            @Override
            public void onChanged(List<DeviceModel> deviceModels) {
                mDeviceModels.clear();
                if (deviceModels != null)
                    mDeviceModels.addAll(deviceModels);
            }
        };
        SessionManager.getInstance().registerDeviceDataObserver(mObserver);
        initData();
        mShareElementComparator = new Comparator<ShareElement>() {
            @Override
            public int compare(@NonNull ShareElement o1, @NonNull ShareElement o2) {
                try {
                    long date1 = Long.valueOf(o1.getShareDate());
                    long date2 = Long.valueOf(o2.getShareDate());
                    if (date1 != date2) {
                        return date1 > date2 ? -1 : 1;
                    } else {
                        return Integer.compare(o1.getState().ordinal(), o2.getState().ordinal());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };
        NetworkStateManager.Companion.getInstance().addNetworkStateChangedListener(new NetworkStateManager.OnNetworkStateChangedListener() {
            @Override
            public void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable) {
                mIsWifiAvailable = isWifiAvailable;
            }

            @Override
            public void onStatusConnection(int statusCode) {
                mIsAvailable = statusCode == NetworkStateManager.STATUS_CODE_ESTABLISHED;
            }
        });
    }

    @Nullable
    private synchronized Box<ShareElement> getStore() {
        BoxStore boxStore = DBHelper.getBoxStore();
        return boxStore != null ? boxStore.boxFor(ShareElement.class) : null;
    }

    //初始化分享文件列表, 保存在本地的数据  子线程读取数据
    private void initData() {
        //初始化一次
        if (isInit || getStore() == null) return;
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<List<ShareElement>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ShareElement>> emitter) {
                List<ShareElement> shareElements = new ArrayList<>();
                for (ShareElement copyFile : getStore().getAll()) {
                    if (TextUtils.isEmpty(copyFile.getShareToken()))
                        continue;
                    shareElements.add(copyFile);
                }
                emitter.onNext(shareElements);
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<ShareElement>>() {
                    @Override
                    public void accept(@NonNull List<ShareElement> copyFiles) {
                        mShareList.addAll(copyFiles);
                        isInit = true;
                    }
                });

    }

    public void request() {
        if (mCompositeDisposable != null)
            mCompositeDisposable.clear();
        request(BEAT_RUNNING_NORMAL_TIME);
    }

    /**
     * @param nXPeriod 1  normal
     *                 60 background
     *                 -1  stop
     */
    public void request(int nXPeriod) {
        Logger.LOGD(TAG, "nXPeriod :" + nXPeriod);
        m_nXPeriod = nXPeriod;
        if (mCompositeDisposable != null)
            mCompositeDisposable.clear();
        if (m_nXPeriod == -1 || getStore() == null) {
            return;
        }
        addDisposable(Observable.interval(1000, AppConstants.SHARE_FILE_REFRESH_PERIOD * nXPeriod, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
                        if (mIsAvailable)
                            refreshData();
                    }
                }));
        addDisposable(Observable.interval(1200, AppConstants.DEVICE_SHARE_FILE_REFRESH_PERIOD * nXPeriod, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
                        if (mIsAvailable)
                            getDeviceShareFileInfoList();
                    }
                }));

        addDisposable(Observable.interval(1300, AppConstants.DEVICE_SHARE_FILE_REFRESH_PERIOD * nXPeriod,
                TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    long mLong;

                    @Override
                    public void accept(Long aLong) {
                        if (!isInit && !(mIsAvailable)) return;
                        List<ShareElement> allList = new ArrayList<>(mShareList);
                        Set<ShareElement> copyFiles = new ArraySet<>();
                        Iterator<ShareElement> iterator = allList.iterator();
                        while (iterator.hasNext()) {
                            ShareElement shareElement = iterator.next();
                            if (mDeleteList.contains(shareElement.getShareToken())) {
                                iterator.remove();
                                continue;
                            }
                            copyFiles.add(shareElement);
                        }
                        try {
                            if ((mLong % (5 * 60)) == 0) {
                                getStore().put(copyFiles);
                            }
                            mLong++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (allList.size() > 1)
                            Collections.sort(allList, mShareElementComparator);
                        mServerShareElements.postValue(allList);
                    }
                }));

    }

    public void refresh() {
        isRefresh = true;
        refreshData();
        getDeviceShareFileInfoList();
    }


    public void refreshData() {
        //请求文件分享列表(包括收发)
        GetShareFilesHttpLoader loader = new GetShareFilesHttpLoader(GetShareFilesResultBean.class);
        loader.setParams(LoginTokenUtil.getToken());
        loader.setSchedulers(Schedulers.io());
        loader.executor(new CommonResultListener<GetShareFilesResultBean>() {
            @Override
            public void success(Object tag, @NonNull GetShareFilesResultBean shareFiles) {
                //获取下载路径
                String downloadPath = SessionManager.getInstance().getDefaultDownloadPath();

                List<ShareElement> list = new ArrayList<>();
                for (GetShareFilesResultBean.FilesBean fileBean : shareFiles.getFiles()) {
                    //null assert
                    if (fileBean == null) continue;
                    //如果from是当前登录的账号, 则该条数据为我的分享，否则为收到的分享
                    int state;
                    int shareType;
                    String account = CMAPI.getInstance().getBaseInfo().getAccount();
                    if (Objects.equals(account, fileBean.getFrom()) && Objects.equals(account, fileBean.getTo())) {
                        shareType = ShareElement.TYPE_SHARE_COPY;
                        state = ShareElement.STATE_SHARE_COPY;
                    } else if (Objects.equals(fileBean.getFrom(), account)) {
                        shareType = ShareElement.TYPE_SHARE_SEND;
                        state = ShareElement.STATE_SHARE_SEND;
                    } else if (Objects.equals(fileBean.getTo(), account)) {
                        shareType = ShareElement.TYPE_SHARE_RECEIVE;
                        state = ShareElement.STATE_SHARE_RECEIVE;
                    } else {
                        continue;
                    }
                    list.add(new ShareElement(
                            fileBean.getName(),
                            fileBean.getFrom(),
                            fileBean.getFromName(),
                            fileBean.getTo(),
                            fileBean.getToName(),
                            fileBean.getSharetoken(),
                            fileBean.getExpire(),
                            0,
                            Long.valueOf(fileBean.getSize()),
                            state,
                            downloadPath,
                            fileBean.getDeviceid(),
                            fileBean.type,
                            shareType,
                            fileBean.path
                            , fileBean.id
                    ));
                }
                refreshData(list);
            }

            @Override
            public void error(Object tag, @NonNull GsonBaseProtocol mErrorProtocol) {
                Logger.LOGE(TAG, mErrorProtocol.toString());
            }
        });

    }


    public void refreshData(@NonNull final List<ShareElement> beans) {
        if (!isInit || getStore() == null) return;
        a:
        for (ShareElement bean : beans) {
            if (mDeleteList.contains(bean.getShareToken()))
                continue;
            for (ShareElement element : mShareList) {
                if (Objects.equals(bean.getShareToken(), element.getShareToken())) {
                    //如果当前列表中已包含了, 则不添加
                    continue a;
                }
            }
            if (bean.getShareType() == ShareElement.TYPE_SHARE_COPY) {
                List<ShareElement> copyFiles = getStore().query()
                        .equal(ShareElement_.sourceId, bean.getSourceId())
                        .equal(ShareElement_.srcPath, bean.getSrcPath())
                        .equal(ShareElement_.shareType, ShareElement.TYPE_SHARE_COPY)
                        .isNull(ShareElement_.shareToken)
                        .orderDesc(ShareElement_.date)
                        .build().find();
                if (copyFiles.size() > 0) {
                    ShareElement copyFile = copyFiles.get(0);
                    if (copyFile.getState() == TransferState.NONE) {
                        bean.setToID(copyFile.getToID());
                        bean.setToPath(copyFile.getToPath());
                    } else {
                        bean.setState(copyFile.getState());
                    }
                    bean.setId(copyFile.getId());
                    bean.setDate(copyFile.getDate());
                }
            }
            update(bean);

//            ShareElemsKeeper.insert(bean);
            //添加新增的分享数据
            mShareList.add(bean);
            //如果收到的是其他人的文件 发送本地广播
            if (bean.getShareType() == ShareElement.TYPE_SHARE_RECEIVE) {
                Intent intent = new Intent();
                intent.setAction(AppConstants.TRANSMISSION_RECEIVE_NEW_FILE);
                LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
            }
        }
        b:
        for (ShareElement element : mShareList) {
            switch (element.getShareType()) {
                case ShareElement.TYPE_SHARE_SEND:
                    if (element.getShareState() == 0)
                        element.setShareState(ShareElement.STATE_SHARE_SEND);
                    break;
                case ShareElement.TYPE_SHARE_RECEIVE:
                    if (element.getShareState() == 0)
                        element.setShareState(ShareElement.STATE_SHARE_RECEIVE);
                    break;
                case ShareElement.TYPE_SHARE_COPY:
                    if (element.getShareState() == 0)
                        element.setShareState(ShareElement.STATE_SHARE_COPY);
                    break;
            }

            for (ShareElement bean : beans) {
                if (Objects.equals(bean.getShareToken(), element.getShareToken())) {
                    //如果当前列表中的这条数据，在新请求的数据集中存在，则跳过本次循环
                    continue b;
                }
            }
            //如果当前列表中的这条数据，在新请求的数据集中不存在，则状态改为失效
            element.setShareState(ShareElement.STATE_SHARE_EXPIRED);
            element.setState(TransferState.NONE);

        }
        Set<String> sourceIds = new ArraySet<>();
        Set<String> toIds = new ArraySet<>();
        for (ShareElement element : mShareList) {
            if (element.getShareType() != ShareElement.TYPE_SHARE_SEND) {
                if (!TextUtils.isEmpty(element.getToID())) {
                    toIds.add(element.getToID());
                }
            } else {
                if (!TextUtils.isEmpty(element.getSourceId())) {
                    sourceIds.add(element.getSourceId());
                }
            }
        }

        if (mDeviceModels != null) {
            for (DeviceModel deviceModel : mDeviceModels) {
                if (deviceModel.isOnline()) {
                    if (!sourceIds.contains(deviceModel.getDevId())) {
                        getDeviceShareFileInfo(deviceModel.getDevId(), false, true);
                    }
                    if (!toIds.contains(deviceModel.getDevId())) {
                        getDeviceShareFileInfo(deviceModel.getDevId(), true, true);
                    }
                }
            }

        }
    }

    private long count;

    public void getDeviceShareFileInfoList() {
        if (!isInit) return;
        if (!mIsWifiAvailable
                && !SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true)
                && count % 5 != 0) return;
        Set<String> toIds = new ArraySet<>();
        Set<String> sourceIds = new ArraySet<>();
        for (ShareElement element : mShareList) {
            //如果已过期
            if (element.getShareState() == ShareElement.STATE_SHARE_EXPIRED) {
                continue;
            }
            if (isRefresh /*|| BuildConfig.DEBUG*/
                    || (element.getShareType() != ShareElement.TYPE_SHARE_SEND
                    && (TransferState.START.equals(element.getState())
                    || (TransferState.WAIT.equals(element.getState()) && count % 3 == 0)
                    || count % 10 == 0))
                    || (element.getShareType() == ShareElement.TYPE_SHARE_SEND
                    && (TransferState.START.equals(element.getState())
                    || count % 3 == 0))) {
                if (element.getShareType() != ShareElement.TYPE_SHARE_SEND) {
                    if (!TextUtils.isEmpty(element.getToID())) {
                        toIds.add(element.getToID());
                    }
                } else {
                    if (!TextUtils.isEmpty(element.getSourceId())) {
                        sourceIds.add(element.getSourceId());
                    }
                }
            }
        }
        if (mDeviceModels != null && (toIds.size() > 0 || sourceIds.size() > 0)) {
            for (DeviceModel deviceModel : mDeviceModels) {
                if (deviceModel != null && deviceModel.isOnline()) {
                    if (sourceIds.contains(deviceModel.getDevId())) {
                        getDeviceShareFileInfo(deviceModel.getDevId(), false, isRefresh);
                    }
                    if (toIds.contains(deviceModel.getDevId())) {
                        getDeviceShareFileInfo(deviceModel.getDevId(), true, isRefresh);
                    }
                }
            }
        }
        count++;
        isRefresh = false;
    }

    @NonNull
    private Map<String, Long> deviceTimeRefresh = new HashMap<>();


    private void getDeviceShareFileInfo(@NonNull final String deviceId, final boolean isDownload, final boolean noCheck) {
        SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener(false) {
            @Override
            public void onSuccess(String url, final LoginSession loginSession) {

                OneOSGetShareFileInfoAPI shareFileInfoAPI = new OneOSGetShareFileInfoAPI(loginSession);
                shareFileInfoAPI.setResultListener(new OneOSGetShareFileInfoAPI.GetDownloadShareFileInfoListener() {
                    @Override
                    public void onSuccess(String url, @NonNull List<ShareFileInfo> list) {
                        HashMap<String, ShareFileInfo> shareFileInfoMap = new HashMap<>();
                        if (isDownload) {
                            for (ShareFileInfo info : list) {
                                if (info != null && info.file != null && !EmptyUtils.isEmpty(info.file.token))
                                    shareFileInfoMap.put(info.file.token, info);
                            }
                        } else {
                            if (list.size() > 0) {
                                for (ShareFileInfo info : list) {
                                    //写入当前源设备ID
                                    if (info == null || info.file == null) continue;
                                    info.file.deviceid = deviceId;
                                    shareFileInfoMap.put(info.file.sharetoken, info);
                                }
                            } else {
                                Iterator<Map.Entry<String, ShareFileInfo>> iterator = shareFileInfoMap.entrySet().iterator();
                                while (iterator.hasNext()) {
                                    Map.Entry<String, ShareFileInfo> entry = iterator.next();
                                    if (entry != null) {
                                        ShareFileInfo value = entry.getValue();
                                        if (value != null && Objects.equals(deviceId, (value.file.deviceid))) {
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                        }
                        refreshDLRecvData(deviceId, shareFileInfoMap, isDownload, loginSession);
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        Logger.LOGE(TAG, "shareFileInfo :" + isDownload, "errorNo :" + errorNo, errorMsg);
                    }
                });
                shareFileInfoAPI.getList(isDownload);
                //  获取文件下载次数统计
                if (!isDownload) {
                    queryDownloadCount(loginSession, deviceId, noCheck);
                }
            }
        });
    }

    private void queryDownloadCount(LoginSession loginSession, final String deviceId,
                                    boolean noCheck) {
        long currentTimeMillis = System.currentTimeMillis();
        Long aLong = deviceTimeRefresh.get(deviceId);
        final long lastTime = aLong == null ? 0 : aLong;
        if (noCheck || currentTimeMillis - lastTime > TIME_RECORDS_REFRESH) {
            deviceTimeRefresh.put(deviceId, currentTimeMillis);
            ShareFileRecordsAPI shareFileRecordsAPI = new ShareFileRecordsAPI(loginSession);
            shareFileRecordsAPI.setMapEventListener(new EventListener<Map<String, Integer>>() {
                @Override
                public void onStart(String url) {
                }

                @Override
                public void onSuccess(String url, @NonNull Map<String, Integer> data) {
                    for (Map.Entry<String, Integer> entry : data.entrySet()) {
                        String key = entry.getKey();
                        Integer value = entry.getValue();
                        mMapDataShareFileRecords.put(key, value);
                    }
                }

                @Override
                public void onFailure(String url, int errorNo, String errorMsg) {
                    deviceTimeRefresh.put(deviceId, lastTime);
                }
            });
            shareFileRecordsAPI.query();

        }
    }

    private void refreshDLRecvData(@NonNull final String deviceId, @NonNull HashMap<String, ShareFileInfo> shareFileInfoMap, boolean isDownload, final LoginSession loginSession) {
        if (isDownload) {
            if (shareFileInfoMap.size() > 0) {
                final List<String> expiredDownloads = new ArrayList<>();
                //当前设备的保存的downloadlist  大于或等于
                for (Map.Entry<String, ShareFileInfo> shareFileInfoEntry : shareFileInfoMap.entrySet()) {
                    String token = shareFileInfoEntry.getKey();
                    ShareFileInfo info = shareFileInfoEntry.getValue();
                    if (info == null || TextUtils.isEmpty(token))
                        continue;
                    ShareElement dlElement = null;
                    for (ShareElement element : mShareList) {
                        if (info.file != null && Objects.equals(element.getShareToken(), info.file.sharetoken)) {
                            dlElement = element;
                            break;
                        }
                    }
                    if (dlElement != null) {
                        if (dlElement.getShareState() == ShareElement.STATE_SHARE_EXPIRED) {
                            // 如果已过期  则跳过
                            continue;
                        }

                        if (info.state.length >= 0 && info.state.length <= dlElement.getFileSize()) {
                            dlElement.setLength(info.state.length);
                        }
                        if (!TextUtils.isEmpty(info.file.token)) {
                            dlElement.setDownloadToken(info.file.token);
                        }
                        if (!TextUtils.isEmpty(info.file.to_dir)) {
                            dlElement.setToPath(info.file.to_dir);
                        }
                        dlElement.setToID(deviceId);
                        dlElement.setSpeed(info.state.speed);
                        if (info.state.state == 0) {//排队等待
                            dlElement.setShareState(ShareElement.STATE_SHARE_DOWNLOADING);
                            dlElement.setState(TransferState.WAIT);
                        } else if (info.state.state == 1) {//开始
                            dlElement.setShareState(ShareElement.STATE_SHARE_DOWNLOADING);
                            dlElement.setState(TransferState.START);
                        } else if (info.state.state == 2) {//暂停
                            dlElement.setShareState(ShareElement.STATE_SHARE_DOWNLOADING);
                            dlElement.setState(TransferState.PAUSE);
                        } else if (info.state.state == 3) {//结束
                            if (dlElement.getShareState() != ShareElement.STATE_SHARE_COMPLETED)
                                dlElement.setDownloadedCount((dlElement.getDownloadedCount()) + 1);
                            dlElement.setShareState(ShareElement.STATE_SHARE_COMPLETED);
                            dlElement.setState(TransferState.COMPLETE);
                        } else if (info.state.state == -1//下载失败
                                || info.state.state == -3) {//Token 无效
                            dlElement.setShareState(ShareElement.STATE_SHARE_ERROR);
                            dlElement.setState(TransferState.FAILED);
                        } else if (info.state.state == -2) {//Token 过期
                            dlElement.setShareState(ShareElement.STATE_SHARE_EXPIRED);
                            dlElement.setState(TransferState.FAILED);
                        }
                        dlElement.setDlErrorCode(String.valueOf(info.state.err_code));
                        switch (info.state.err_code) {
                            case 0:
                                dlElement.setException(TransferException.NONE);
                                break;
                            case -401://获取源设备地址失败
                                dlElement.setException(TransferException.SOURCE_NOT_FOUND);
                                break;
                            case -402://Token 过期
                            case -403://Token 无效
                                dlElement.setException(TransferException.SOURCE_EXPIRED);
                                break;
                            case -410://请求下载失败
                                dlElement.setException(TransferException.FAILED_REQUEST_SERVER);
                                break;
                            case -411://连接源设备失败
                                dlElement.setException(TransferException.SOCKET_TIMEOUT);
                                break;
                            case -412://读取文件大小失败
                            case -413://创建临时文件失败
                            case -414://打开临时文件失败
                            case -415://读取文件流失败
                            case -416://写文件失败
                            case -417://下载文件大小与实际大小不匹配
                            case -418://重命名临时文件失败
                                dlElement.setException(TransferException.IO_EXCEPTION);
                                break;
                        }
                    } else {
                        long i = System.currentTimeMillis() / 1000;
                        if ((info.state.join_at + 8 * 24 * 60 * 60) < i
                                && count >= 10 * 5) {
                            expiredDownloads.add(info.file.sharetoken);
                        }
                    }
                }
                if (expiredDownloads.size() > 0) {
                    SessionManager.getInstance().getLoginSession(deviceId, new GetSessionListener(false) {
                        @Override
                        public void onSuccess(String url, LoginSession loginSession) {
                            OneOSNotifyShareFileAPI notifyDLAPI = new OneOSNotifyShareFileAPI(loginSession);
                            notifyDLAPI.setResultListener(new OneOSNotifyShareFileAPI.NotifyDownloadListener() {
                                @Override
                                public void onSuccess(String url, String method) {

                                }

                                @Override
                                public void onFailure(String url, String method, int errorNo, String errorMsg) {
                                    Logger.LOGE(TAG, method, " errorNo :" + errorNo, errorMsg);
                                }
                            });

                            notifyDLAPI.delete(expiredDownloads.toArray(new String[expiredDownloads.size()]));
                        }
                    });


                }
            }
            for (final ShareElement element : mShareList) {
                if (element.getShareState() == ShareElement.STATE_SHARE_EXPIRED) {
                    // 如果已过期  则跳过
                    continue;
                }
                long id = element.getId();
                //id ==0 说明为默认的id  是没有本地记录的不用去查询
                if (id == 0) continue;
                ShareElement copyFile = null;

                if (getStore() == null) continue;
                try {
                    copyFile = getStore().get(id);
                } catch (Exception e) {
                    e.printStackTrace();

                }
                if (element.getShareType() == ShareElement.TYPE_SHARE_COPY
                        && Objects.equals(deviceId, element.getToID())
                        && (element.getState() == TransferState.NONE)
                        && element.getShareState() == ShareElement.STATE_SHARE_COPY
                        && element.getLength() == 0
                        && copyFile != null
                        && copyFile.getDate() > 0
                        && copyFile.getDownloadedCount() == 0
                        && copyFile.getState() == TransferState.NONE) {
                    GetDownloadHttpLoader loader = new GetDownloadHttpLoader(DownloadTokenResultBean.class);
                    loader.setParams(LoginTokenUtil.getToken(), element.getShareToken(), element.getToID());
                    loader.executor(new CommonResultListener<DownloadTokenResultBean>() {
                        @Override
                        public void success(Object tag, @NonNull DownloadTokenResultBean bean) {
                            element.setDownloadToPhone(false);
                            element.setDownloadToken(bean.downloadtoken);
                            element.setShareState(ShareElement.STATE_SHARE_DOWNLOADING);
                            element.setState(TransferState.WAIT);
                            update(element);
                            OneOSNotifyShareFileAPI notifyDLAPI = new OneOSNotifyShareFileAPI(loginSession);
                            notifyDLAPI.setResultListener(new OneOSNotifyShareFileAPI.NotifyDownloadListener() {
                                @Override
                                public void onSuccess(String url, String method) {

                                }

                                @Override
                                public void onFailure(String url, String method, int errorNo, String errorMsg) {
                                    element.setShareState(ShareElement.STATE_SHARE_ERROR);
                                    element.setState(TransferState.FAILED);
                                    update(element);
                                }
                            });
                            notifyDLAPI.download(element.getShareToken(), element.getDownloadToken(), element.getSourceId()
                                    , element.getFileName(), element.getOwner()
                                    , element.getToPath());
                        }

                        @Override
                        public void error(Object tag, @NonNull GsonBaseProtocol mErrorProtocol) {
//                            ToastHelper.showToast((R.string.request_server_exception) /*+ ":downloadToken"*/);
                            Logger.LOGE(TAG, mErrorProtocol.toString());
                        }
                    });
                }
            }

        } else {
            for (ShareElement element : mShareList) {
                if (element.getShareType() != ShareElement.TYPE_SHARE_SEND) continue;
                if (element.getShareState() == ShareElement.STATE_SHARE_EXPIRED) {
                    continue;
                }
                String shareToken = element.getShareToken();
                Integer integer = mMapDataShareFileRecords.get(shareToken);
                if (integer != null) {
                    element.setDownloadedCount(integer);
                }
                if (shareFileInfoMap.size() == 0) {
                    element.setShareState(ShareElement.STATE_SHARE_SEND);
                    continue;
                }
                ShareFileInfo info = shareFileInfoMap.get(shareToken);
                if (info != null && info.file != null
                        && Objects.equals(info.file.name, element.getFileName())) {
                    element.setLength(info.state.length);
                    if (info.state.state == 1) {
                        element.setShareState(ShareElement.STATE_SHARE_TRANSFERRING);
                        element.setState(TransferState.START);
                    } else if (info.state.state == 3) {
                        element.setShareState(ShareElement.STATE_SHARE_SEND);
                        element.setState(TransferState.COMPLETE);
                    }
                }
            }
        }
    }

    public void update(@NonNull ShareElement dlElement) {
        if (getStore() == null) return;
        try {
            if (!mDeleteList.contains(dlElement.getShareToken()))
                getStore().put((dlElement));
        } catch (Exception e) {
            if (e instanceof UniqueViolationException) {
                ShareElement unique = getStore().query().equal(ShareElement_.shareToken, dlElement.getShareToken()).build().findFirst();
                if (unique != null) {
                    if (Long.valueOf(unique.getShareDate()) < Long.valueOf(dlElement.getShareDate())
                            && new Date().compareTo(new Date(Long.valueOf(unique.getShareDate()))) > 0) {
                        try {
                            getStore().remove(unique);
                            getStore().put(dlElement);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            e.printStackTrace();
        }
    }

    public boolean deleteFromDB(ShareElement dlElement) {
        if (getStore() == null) return false;
        try {
            getStore().remove((dlElement));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dispose();
        SessionManager.getInstance().unregisterDeviceDataObserver(mObserver);
    }

    public boolean delete(@NonNull ShareElement element) {
        if (!mDeleteList.contains(element.getShareToken())) {
            deleteFromDB(element);
            mShareList.remove(element);
            return mDeleteList.add(element.getShareToken());
        }
        return false;
    }

    public void cleanExpiredShare() {
        final Map<String, ArrayList<String>> mapDown = new ConcurrentHashMap<>();
        final Map<String, ArrayList<String>> mapUp = new ConcurrentHashMap<>();
        Iterator<ShareElement> iterator = mShareList.iterator();
        while (iterator.hasNext()) {
            final ShareElement element = iterator.next();
            if (element.getShareState() == ShareElement.STATE_SHARE_EXPIRED) {
                mShareList.remove(element);
                Observable.just(deleteFromDB(element))
                        .observeOn(Schedulers.single())
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Boolean aBoolean) {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.LOGE(TAG, e);
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                if (!TextUtils.isEmpty(element.getToID())
                        && !TextUtils.isEmpty(element.getDownloadToken())
                        && element.getShareType() != ShareElement.STATE_SHARE_SEND) {
                    ArrayList<String> strings = mapDown.get(element.getToID());
                    if (strings == null) {
                        strings = new ArrayList<>();
                        mapDown.put(element.getToID(), strings);
                    }
                    strings.add(element.getDownloadToken());
                }
                if (element.getShareType() != ShareElement.STATE_SHARE_RECEIVE
                        && !TextUtils.isEmpty(element.getSourceId())
                        && !TextUtils.isEmpty(element.getShareToken())) {
                    ArrayList<String> strings = mapUp.get(element.getSourceId());
                    if (strings == null) {
                        strings = new ArrayList<>();
                        mapUp.put(element.getSourceId(), strings);
                    }
                    strings.add(element.getShareToken());
                }
            }
        }
        for (final String s : mapDown.keySet()) {
            SessionManager.getInstance().getLoginSession(s, new GetSessionListener(false) {
                @Override
                public void onSuccess(String url, LoginSession loginSession) {
                    ArrayList<String> strings = mapDown.get(s);
                    OneOSNotifyShareFileAPI notifyDLAPI = new OneOSNotifyShareFileAPI(loginSession);
                    notifyDLAPI.setResultListener(new OneOSNotifyShareFileAPI.NotifyDownloadListener() {
                        @Override
                        public void onSuccess(String url, String method) {

                        }

                        @Override
                        public void onFailure(String url, String method, int errorNo, String errorMsg) {
                            Logger.LOGE(TAG, method, "errorNo :" + errorNo, errorMsg);
                        }
                    });

                    notifyDLAPI.delete(strings.toArray(new String[strings.size()]));
                }
            });
        }
        for (final String s : mapUp.keySet()) {
            SessionManager.getInstance().getLoginSession(s, new GetSessionListener(false) {
                @Override
                public void onSuccess(String url, LoginSession loginSession) {
                    ArrayList<String> strings = mapUp.get(s);
                    OneOSNotifyShareFileAPI notifyDLAPI = new OneOSNotifyShareFileAPI(loginSession);
                    notifyDLAPI.setResultListener(new OneOSNotifyShareFileAPI.NotifyDownloadListener() {
                        @Override
                        public void onSuccess(String url, String method) {

                        }

                        @Override
                        public void onFailure(String url, String method, int errorNo, String errorMsg) {
                            Logger.LOGE(TAG, method, "errorNo :" + errorNo, errorMsg);
                        }
                    });
                    notifyDLAPI.cancelUp(strings.toArray(new String[strings.size()]));
                }
            });
        }


    }

    public void removeDownloadToken(@NonNull ShareElement element) {
        RemoveDownloadTokenHttpLoader loader = new RemoveDownloadTokenHttpLoader(GsonBaseProtocol.class);
        loader.setParams(LoginTokenUtil.getToken(), element.getDownloadToken());
        loader.executor(new CommonResultListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol mBaseProtocol) {

            }

            @Override
            public void error(Object tag, @NonNull GsonBaseProtocol mErrorProtocol) {
                Logger.LOGE(TAG, mErrorProtocol.toString());
            }
        });
    }

    public void removeShare(@NonNull final ShareElement element) {
        RemoveShareTokenHttpLoader loader = new RemoveShareTokenHttpLoader(GsonBaseProtocol.class);
        loader.setParams(LoginTokenUtil.getToken(), element.getShareToken());
        loader.executor(new CommonResultListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol mBaseProtocol) {
                deleteFromDB(element);
                mShareList.remove(element);
            }

            @Override
            public void error(Object tag, @NonNull GsonBaseProtocol mErrorProtocol) {
                ToastHelper.showToast(SdvnHttpErrorNo.ec2String(mErrorProtocol.result));
                delete(element);
            }
        });
    }

    public void addToObserverQueue(ArrayList<OneOSFile> fileList, String sourceId, String toId, String tarPath) {
        if (!isRunningNormal()) {
            if (mCompositeDisposable != null)
                mCompositeDisposable.clear();
            request(2);
            isRefresh = true;
            addDisposable(Observable.just(10)
                    .delay(60, TimeUnit.SECONDS)
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) {
                            if (!isRunningNormal()) {
                                request(integer);
                                isRefresh = false;
                            }
                        }
                    }));
        }
    }

    private boolean isRunningNormal() {
        return m_nXPeriod == BEAT_RUNNING_NORMAL_TIME;
    }

}
