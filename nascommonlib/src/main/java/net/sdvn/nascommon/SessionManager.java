package net.sdvn.nascommon;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.sdvn.cmapi.BaseInfo;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.protocal.EventObserver;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.DeviceSharedUsersHttpLoader;
import net.sdvn.common.internet.loader.HardWareDevicesHttpLoader;
import net.sdvn.common.internet.protocol.SharedUserList;
import net.sdvn.common.internet.protocol.entity.HardWareDevice;
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL;
import net.sdvn.common.internet.protocol.entity.ShareUser;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.common.repo.AccountRepo;
import net.sdvn.common.repo.DevicesRepo;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.db.DBHelper;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.db.TransferHistoryKeeper;
import net.sdvn.nascommon.db.UserInfoKeeper;
import net.sdvn.nascommon.db.UserSettingsKeeper;
import net.sdvn.nascommon.db.objecbox.DeviceInfo;
import net.sdvn.nascommon.db.objecbox.UserInfo;
import net.sdvn.nascommon.db.objecbox.UserSettings;
import net.sdvn.nascommon.iface.EBRefreshHardWareDevice;
import net.sdvn.nascommon.iface.EventListener;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.model.eventbus.DevHDAddNewUsers;
import net.sdvn.nascommon.model.oneos.DataFile;
import net.sdvn.nascommon.model.oneos.DevAttrInfo;
import net.sdvn.nascommon.model.oneos.api.user.OneOSLoginAPI;
import net.sdvn.nascommon.model.oneos.event.EventMsgManager;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.receiver.NetworkStateManager;
import net.sdvn.nascommon.rx.RxWork;
import net.sdvn.nascommon.service.NasService;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.SDCardUtils;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommon.viewmodel.NasLanAccessViewModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.weline.devhelper.DevTypeHelper;
import io.weline.repo.SessionCache;
import libs.source.common.InterfaceDeps;
import libs.source.common.utils.ThreadUtils;
import timber.log.Timber;


/**
 * Created by yun on 18/05/19.
 */

public class SessionManager extends RxWork implements InterfaceDeps, LifecycleOwner {
    public static final String TAG = SessionManager.class.getSimpleName();
    private static SessionManager sInstance;
    @Nullable
    private DeviceModel mDeviceModel;//选中的 设备

    private ConcurrentHashMap<String, DeviceModel> mSessions;  //
    private List<DeviceModel> mDeviceModels;
    private MutableLiveData<List<DeviceModel>> _mDataSetObservable;
    public LiveData<List<DeviceModel>> liveDataDeviceModels;
    private LocalBroadcastReceiver mReceiver;
    private ConcurrentHashMap<String, String> mIP_IDMap;
    private boolean mIsLogin;
    private WeakReference<Context> mContextRef;
    private UserSettings mUserSettings;
    private Handler mHandler;
    @Nullable
    private String username;
    private String defaultDownloadPath;
    private String userId;
    private String lastUserId;
    LifecycleRegistry mLifecycleRegistry;

    private SessionManager() {
        mSessions = new ConcurrentHashMap<>();
        mDeviceModels = new ArrayList<>();
        _mDataSetObservable = new MutableLiveData<>();
        liveDataDeviceModels = _mDataSetObservable;
        mReceiver = new LocalBroadcastReceiver();
        mIP_IDMap = new ConcurrentHashMap<>();
        mHandler = new Handler(Looper.getMainLooper());
        initLifecycle();
    }

    @Nullable
    public String getDeviceIdByVip(String vip) {
        if (TextUtils.isEmpty(vip)) return null;
        return mIP_IDMap.get(vip);
    }

    public String getDeviceVipById(String devId) {
        if (TextUtils.isEmpty(devId)) return null;
        DeviceModel deviceModel = mSessions.get(devId);
        if (deviceModel != null && deviceModel.isOnline()
                && EmptyUtils.isNotEmpty(deviceModel.getDevVip())) {
            return deviceModel.getDevVip();
        }
        Device device = new Device();
        CMAPI.getInstance().getDeviceById(devId, device);
        return device.getVip();
    }

    public void setIsLogin(boolean isLogin) {
        mIsLogin = isLogin;
    }

    public boolean isLogin() {
        return mIsLogin;
    }

    public boolean isShareV2Available(String devId) {
        if (!TextUtils.isEmpty(devId)) {
            final DeviceModel deviceModel = mSessions.get(devId);
            if (deviceModel != null && deviceModel.getLoginSession() != null) {
                return deviceModel.getLoginSession().isShareV2Available();
            }
        }
        return false;
    }

    @NonNull
    public String getDefaultDownloadPath() {
        if (defaultDownloadPath == null) {
            String path = SPHelper.get(AppConstants.SP_FIELD_DEFAULT_LOCAL_DOWNLOAD_PATH, null);
            if (path == null) {
                path = SDCardUtils.createDefaultDownloadPath(username);
            }
            defaultDownloadPath = path;
        }
        return defaultDownloadPath;
    }

    public String getDefaultDownloadPathByID(String id, DataFile file) {
        DeviceModel deviceModel = mSessions.get(id);
        String sub = deviceModel != null && !EmptyUtils.isEmpty(deviceModel.getDevSn()) ? deviceModel.getDevSn() : id;
        String downloadPath = String.format("%s/%s", getDefaultDownloadPath(), sub);
        String filePath = file.getPath();
        if (!EmptyUtils.isEmpty(filePath)) {
            int endIndex = filePath.length() - file.getName().length();
            if (endIndex > 0) {
                String path = filePath.substring(0, endIndex);
                String toPath = String.format("%s/%s", downloadPath, path);
                String other = File.separator + File.separator;
                while (toPath.contains(other)) {
                    toPath = toPath.replace(other, File.separator);
                }
                while (toPath.endsWith(File.separator)) {
                    toPath = toPath.substring(0, toPath.length() - 1);
                }
                return toPath;
            }
        }
        return downloadPath;
    }


    public void removeAccount() {
        logoutCurrentAccount();
        SessionManager.getInstance().setIsLogin(false);
        SPUtils.setBoolean(AppConstants.SP_FIELD_IS_LOGINED, false);
        clearData(username, userId);

    }

    private void clearData(String username, String userId) {
//        BoxStore boxStore = DBHelper.getBoxStore();
//        if (boxStore != null) {
//            boxStore.close();
//            if (boxStore.isClosed())
//                boxStore.deleteAllFiles();
//        }
        SPHelper.clearSPFile(userId);
    }

    public String genDevAccountTag(String vip) {
        final String id = mIP_IDMap.get(vip);
        final String account = userId;
        return id + File.separator + account;
    }

    public UserSettings getUserSettings() {
        return mUserSettings;
    }

    public String getUsername() {
        if (username != null) {
            return username;
        }
        return AccountRepo.INSTANCE.getUserId();
    }

    public void setUsername(String username) {
        if (!EmptyUtils.isEmpty(username)) {
            this.username = username;
        }
    }

    public String getUserId() {
        if (userId != null) {
            return userId;
        }
        return AccountRepo.INSTANCE.getUserId();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    private void initLifecycle() {
        mLifecycleRegistry = new LifecycleRegistry(this);
    }

    public void rebootDevice(String url) {
        if (url != null) {
            return;
        }
        String pattern = "\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        String vip = null;
        if (matcher.find()) {
            vip = matcher.group();
        }
        if (vip != null) {
            String devId = getDeviceIdByVip(vip);
            DeviceModel deviceModel = getDeviceModel(devId);
            if (deviceModel != null && deviceModel.isOwner()) {
                CMAPI.getInstance().rebootDevice(vip);
            }
        }
    }

    public void removeSession(String toDevId) {
        DeviceModel model = mSessions.get(toDevId);
        if (model != null) {
            LoginSession loginSession = model.getLoginSession();
            if (loginSession != null) {
                loginSession.setSession("");
            }
        }
        SessionCache.Companion.getInstance().remove(toDevId);
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, @Nullable Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case AppConstants.LOCAL_BROADCAST_REMOVE_DEV:
                            String devId2 = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
                            deleteDeviceModel(devId2);

                            break;
                        case AppConstants.LOCAL_BROADCAST_RELOGIN:
                            Logger.LOGD(TAG, action);
                            final String devId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getLoginSession(devId, new GetSessionListener(false) {
                                        @Override
                                        public void onSuccess(String url, LoginSession data) {

                                        }
                                    });
                                }
                            }, 1000);

                            break;
                    }
                }
            }
        }
    }

//    @Nullable
//    private Timer timer;

    public void init(Context context) {
        mContextRef = new WeakReference<>(context);
//        refreshShareDevice(new ArrayList<GetShareFilesResultBean.FilesBean>());
        long start = System.currentTimeMillis();
        BaseInfo baseInfo = CMAPI.getInstance().getBaseInfo();
        username = baseInfo.getAccount();
        userId = baseInfo.getUserId();
        if (!Objects.equals(lastUserId, userId)) {
            clearLastUserData();
        }
        SPUtils.setValue(context, AppConstants.SP_FIELD_USER_ID, userId);
        SPUtils.setValue(context, AppConstants.SP_FIELD_USERNAME, username);
        bindService();

        mHandler.removeCallbacksAndMessages(null);
        mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        clear();
        CMAPI.getInstance().subscribe(mEventObserver);
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.LOCAL_BROADCAST_RELOGIN);
        filter.addAction(AppConstants.LOCAL_BROADCAST_REMOVE_DEV);
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filter);

        DBHelper.refreshAccount(userId);
//        IntrDBHelper.setUserId(userId);
        if (!EmptyUtils.isEmpty(userId)) {
            DevicesRepo.getDeviceModels().observe(this, bindDeviceModels -> {
                Disposable subscribe = Observable.fromIterable(bindDeviceModels)
                        .flatMap(bindDeviceModel -> Observable.just(DevicesRepo.transform(bindDeviceModel)))
                        .toList()
                        .subscribe((hardWareDevices, throwable) -> {
                            refreshDeviceModels(hardWareDevices);
                        });
                Timber.d("binds: " + GsonUtils.encodeJSON(bindDeviceModels));
            });
        }
        mUserSettings = UserSettingsKeeper.getSettings(username);
        if (mUserSettings == null)
            mUserSettings = UserSettingsKeeper.insertDefault(username);
        Timber.d("consumed : %s", System.currentTimeMillis() - start);

    }

    private void clearLastUserData() {
        mSessions.clear();
        mDeviceModels.clear();
        mDeviceModel = null;
        mIP_IDMap.clear();
        defaultDownloadPath = null;
        lastUserId = userId;
        SessionCache.Companion.getInstance().clear();
    }


    @NonNull
    public List<DeviceModel> getDeviceModels() {
        return Collections.unmodifiableList(mDeviceModels);
    }

    @NonNull
    public List<DeviceModel> getOnlineDeviceModels() {
        List<DeviceModel> models = new ArrayList<>();
        for (DeviceModel deviceModel : mDeviceModels) {
            if (deviceModel.isOnline() && deviceModel.isEnableUseSpace()) {
                models.add(deviceModel);
            }
        }
        return models;
    }

    public @Nullable
    DeviceModel getSelectDeviceModel() {
        return mDeviceModel;
    }

    public void setSelectDeviceModel(String devId) {
        setSelectDeviceModel(mSessions.get(devId));
    }

    public void setSelectDeviceModel(DeviceModel deviceModel) {
        this.mDeviceModel = deviceModel;
        if (deviceModel != null) {
            SPUtils.setValue(Utils.getApp(), AppConstants.SP_FIELD_DEVICE_ID, deviceModel.getDevId());
            if (getService() != null)
                getService().notifyUserLogin(deviceModel.getDevId());
        }
    }

    public boolean isLogin(@Nullable String devId) {
        if (devId == null) {
            Logger.p(Logger.Level.WARN, TAG, " mDevId  is null ");
            return false;
        }
        if (!isLogin()) {
            return false;
        }
        DeviceModel deviceModel = mSessions.get(devId);
        if (deviceModel == null)
            return false;
        LoginSession loginSession = deviceModel.getLoginSession();
        return loginSession != null && loginSession.isLogin();
    }


    public static SessionManager getInstance() {
        if (sInstance == null)
            synchronized (SessionManager.class) {
                if (sInstance == null)
                    sInstance = new SessionManager();
            }
        return sInstance;
    }


    @Nullable
    public LoginSession getLoginSession(@Nullable String devID) {
        if (devID == null) return null;
        DeviceModel deviceModel = mSessions.get(devID);
        if (deviceModel == null) return null;
        return deviceModel.getLoginSession();
    }

    public void getLoginSession(@NonNull final String devID, @NonNull final GetSessionListener eventListener) {
        if (TextUtils.isEmpty(devID)) {
            Logger.p(Logger.Level.WARN, TAG, " mDevId  is null ");
            if (eventListener != null) eventListener.onFailure("null", -1, "mDevId  is Null");
            return;
        }
        if (NetworkStateManager.Companion.getInstance().isEstablished()) {
            DeviceModel deviceModel = mSessions.get(devID);

            Device device = new Device();
            CMAPI.getInstance().getDeviceById(devID, device);
            if (deviceModel != null) {
                deviceModel.setDevice(device);
                if (deviceModel.isOnline()) {
                    LoginSession loginSession = deviceModel.getLoginSession();
                    boolean isV5 = DevTypeHelper.isAndroidTV(deviceModel.getDevClass())
                            || DevTypeHelper.isWebApi(deviceModel.getDevClass());
                    boolean isOneOs = DevTypeHelper.isOneOSNas(deviceModel.getDevClass());
                    long currentTimeMillis = System.currentTimeMillis();
                    if (loginSession == null
                            || loginSession.getUserInfo() == null
                            || loginSession.getDeviceInfo() == null
                            || TextUtils.isEmpty(loginSession.getSession())
                            || (currentTimeMillis - loginSession.getLoginTime() >= AppConstants.SESSION_LIVE_TIME)) {
                        login(devID, eventListener, isV5, isOneOs);
                    } else {
                        //请求V5的Token,不论是不是V5，先请求
                        SessionCache.Companion.getInstance().getOrAsynRequest(devID, loginSession.getIp(), LoginTokenUtil.getToken(), null);
                        //获取是否是V5的缓存，优先其它接口，先缓存下来
//                        SessionCache.Companion.getInstance().isV5OrAsynRequest(devID, loginSession.getIp(), new Function<Boolean, Void>() {
//                            @Override
//                            public Void apply(Boolean input) {
                        //等V5请求后再回调
                        if (deviceModel.getDevice() != null) {
                            if (loginSession.getDeviceInfo() != null) {
                                loginSession.getDeviceInfo().setVIp(deviceModel.getDevice().getVip());
                            }
                            boolean isRealV5Enable = SessionCache.Companion.getInstance().isV5(devID);
                            //兼容android tv nas 1.0
                            loginSession.setV5(isRealV5Enable || isV5);
                            eventListener.onSuccess("null", loginSession);
                        }
//                                return null;
//                            }
//                        });
                    }
                } else {
                    eventListener.onFailure("null", HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, "device is offline");
                }
            } else if (device.isOnline()) {
                updateDeviceModels(new HttpLoader.HttpLoaderStateListener() {
                    @Override
                    public void onLoadStart(Disposable disposable) {
                        addDisposable(disposable);
                    }

                    @Override
                    public void onLoadComplete() {
                    }

                    @Override
                    public void onLoadError() {
                        eventListener.onFailure("null", HttpErrorNo.ERR_ONE_REQUEST, "获取设备列表失败");
                    }
                });
                registerDeviceDataObserver(new Observer<List<DeviceModel>>() {
                    @Override
                    public void onChanged(List<DeviceModel> deviceModels) {
                        unregisterDeviceDataObserver(this);
                        //请求一次 如果不存在则这一轮真不存在此设备
                        DeviceModel deviceModel = mSessions.get(devID);
                        if (deviceModel == null) {
                            eventListener.onFailure("null", HttpErrorNo.ERR_ONE_REQUEST, "Device not found");
                        } else {
                            getLoginSession(devID, eventListener);
                        }
                    }

                });
            } else {
                eventListener.onFailure("null", HttpErrorNo.ERR_ONE_REQUEST, "Device maybe removed");
            }
        } else {
            eventListener.onFailure("null", HttpErrorNo.ERR_ONE_NO_LOGIN, "logout");
        }

    }

    @Nullable
    public DeviceModel updateLoginSession(@NonNull String key, @NonNull LoginSession loginSession) {
        DeviceModel deviceModel = mSessions.get(key);
        if (deviceModel == null) {
            return null;
        }
        if (deviceModel.getLoginSession() == null)
            deviceModel.setLoginSession(loginSession);
        else
            deviceModel.getLoginSession().refreshData(loginSession);
        return mSessions.put(key, deviceModel);
    }

    /**
     * 移除设备
     */
    @Nullable
    public DeviceModel deleteDeviceModel(@NonNull String key) {
        if (!TextUtils.isEmpty(key)) {
            DevicesRepo.remove(key);
            TransferHistoryKeeper.deleteByDevId(key);

            updateDeviceModels(null);
            return mSessions.remove(key);
        }
        return null;
    }

    @Nullable
    public DeviceModel getDeviceModel(@Nullable String devID) {
        if (!TextUtils.isEmpty(devID)) return mSessions.get(devID);
        return null;
    }

    private HardWareDevicesHttpLoader devicesHttpLoader;
    private WeakHashMap<HttpLoader.HttpLoaderStateListener, Object> mHttpLoaderStateListeners;

    public void updateDeviceModels(@Nullable final HttpLoader.HttpLoaderStateListener listener) {
        if (NetworkStateManager.Companion.getInstance().isEstablished()) {
//            if (devicesHttpLoader != null && listener == null) return;
//            if (devicesHttpLoader == null) {
//                devicesHttpLoader = new HardWareDevicesHttpLoader(HardWareInfo.class);
//                devicesHttpLoader.setHttpLoaderStateListener(new HttpLoader.HttpLoaderStateListener() {
//                    @Override
//                    public void onLoadStart(Disposable disposable) {
//                        addDisposable(disposable);
//                        runOnUI(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mHttpLoaderStateListeners != null) {
//                                    for (HttpLoader.HttpLoaderStateListener listener : mHttpLoaderStateListeners.keySet()) {
//                                        if (listener != null)
//                                            listener.onLoadStart(disposable);
//                                    }
//
//                                }
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onLoadComplete() {
//                        runOnUI(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mHttpLoaderStateListeners != null) {
//                                    for (HttpLoader.HttpLoaderStateListener listener : mHttpLoaderStateListeners.keySet()) {
//                                        if (listener != null)
//                                            listener.onLoadComplete();
//                                    }
//
//                                }
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onLoadError() {
//                        runOnUI(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mHttpLoaderStateListeners != null) {
//                                    for (HttpLoader.HttpLoaderStateListener listener : mHttpLoaderStateListeners.keySet()) {
//                                        if (listener != null)
//                                            listener.onLoadError();
//                                    }
//
//                                }
//                            }
//                        });
//                    }
//                });
//                devicesHttpLoader.executor(new CommonResultListener<HardWareInfo>() {
//                    @Override
//                    public void success(Object tag, @NonNull HardWareInfo mGsonBaseProtocol) {
//                        List<HardWareDevice> hardWareDevices = mGsonBaseProtocol.devices;
//                        refreshDeviceModels(hardWareDevices);
//                        devicesHttpLoader = null;
//                    }
//
//                    @Override
//                    public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
//                        devicesHttpLoader = null;
//                    }
//                });
//                mHttpLoaderStateListeners = new WeakHashMap<>();
//            } else {
//                if (mHttpLoaderStateListeners != null) {
//                    if (listener != null) {
//                        mHttpLoaderStateListeners.put(listener, devicesHttpLoader);
//                    }
//                }
//            }
            EventBus.getDefault().post(new EBRefreshHardWareDevice());
        } else {
            if (listener != null)
                listener.onLoadError();

        }
    }

    public void refreshDeviceModels(@Nullable Collection<HardWareDevice> hardWareDevices) {
        List<DeviceModel> newDeviceInfos = new ArrayList<>();
        List<Device> devices = new ArrayList<>();
        List<Device> deviceList = CMAPI.getInstance().getDevices();
        if (deviceList != null) for (Device device : deviceList) {
            if (UiUtils.isNas(device.getDevClass()) || UiUtils.isNasByFeature(device.getFeature())) {
                devices.add(device);
            }
        }

//        int[] myselfDevicesCount = {0}, myselfOnlineDevicesCount = {0}, othersOnlineDevicesCount = {0};
        if (hardWareDevices == null) {
            for (DeviceModel deviceModel : mDeviceModels) {
                Iterator<Device> iterator = devices.iterator();
                if (deviceModel == null) continue;
                boolean isFound = false;
                while (iterator.hasNext()) {
                    Device device = iterator.next();
                    if (Objects.equals(device.getId(), deviceModel.getDevId())) {
                        isFound = true;
                        deviceModel.setDevice(device);
                        iterator.remove();
                        break;
                    }
                }
                if (!isFound) {
                    deviceModel.setDevice(null);
                }
                newDeviceInfos.add(deviceModel);
//                queryDevNameFromDB(deviceModel);
//                deviceToCompare(newDeviceInfos, myselfDevicesCount, myselfOnlineDevicesCount
//                        , othersOnlineDevicesCount, deviceModel);
            }

        } else {
            for (HardWareDevice hardWareDevice : hardWareDevices) {
                Iterator<Device> iterator = devices.iterator();
//                if (UiUtils.isNas(hardWareDevice.getOstype())) {
                DeviceModel deviceModel = mSessions.get(hardWareDevice.getDeviceid());
                if (deviceModel == null) {
                    deviceModel = new DeviceModel(hardWareDevice.getDeviceid());
                    mSessions.put(deviceModel.getDevId(), deviceModel);
                }
                deviceModel.setWareDevice(hardWareDevice);
                boolean isFound = false;
                while (iterator.hasNext()) {
                    Device device = iterator.next();
                    if (Objects.equals(device.getId(), deviceModel.getDevId())) {
                        isFound = true;
                        deviceModel.setDevice(device);
                        iterator.remove();
                        break;
                    }
                }
                if (!isFound) {
                    deviceModel.setDevice(null);
                }
                //check
                if (newDeviceInfos.contains(deviceModel))
                    continue;
                newDeviceInfos.add(deviceModel);
//                    queryDevNameFromDB(deviceModel);

//                    deviceToCompare(newDeviceInfos, myselfDevicesCount, myselfOnlineDevicesCount
//                            , othersOnlineDevicesCount, deviceModel);
//                }
            }
        }
        Iterator<Device> iterator = devices.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            String id = device.getId();
            DeviceModel deviceModel = mSessions.get(id);
            if (deviceModel == null) {
                deviceModel = new DeviceModel(id);
                mSessions.put(deviceModel.getDevId(), deviceModel);
            }
            if (Objects.equals(id, deviceModel.getDevId())) {
                deviceModel.setDevice(device);
                iterator.remove();
            }
            //check
            if (newDeviceInfos.contains(deviceModel))
                continue;
            newDeviceInfos.add(deviceModel);
        }

        final long start = System.currentTimeMillis();
        Collections.sort(newDeviceInfos, new Comparator<DeviceModel>() {
            @Override
            public int compare(DeviceModel o1, DeviceModel o2) {
                try {
                    if (o1.isOnline() != o2.isOnline()) {
                        if (o1.isOnline()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        String b1 = o1.getWareDevice() != null ? o1.getWareDevice().getMgrlevel() : MGR_LEVEL.COMMON;
                        String b2 = o2.getWareDevice() != null ? o2.getWareDevice().getMgrlevel() : MGR_LEVEL.COMMON;
                        final int level1 = Integer.parseInt(b1);
                        final int level2 = Integer.parseInt(b2);
                        final int i = level1 - level2;
                        if (i != 0) {
                            return i;
                        } else {
                            String o1Name = o1.getDevName();
                            String o2Name = o2.getDevName();
                            return o1Name.compareTo(o2Name);
                        }
                    }
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        Logger.LOGD(TAG, "deviceModel sort consumed :" + (System.currentTimeMillis() - start));
        mDeviceModels.clear();
        mDeviceModels.addAll(newDeviceInfos);
        _mDataSetObservable.postValue(mDeviceModels);
    }


    private void deviceToCompare(@NonNull List<DeviceModel> newDeviceInfos, int[] myselfDevicesCount
            , int[] myselfOnlineDevicesCount, int[] othersOnlineDevicesCount, DeviceModel deviceModel) {
        if (deviceModel.isOwner()) {
            if (deviceModel.isOnline()) {
                newDeviceInfos.add(myselfOnlineDevicesCount[0], deviceModel);
                myselfOnlineDevicesCount[0]++;
            } else
                newDeviceInfos.add(myselfDevicesCount[0], deviceModel);
            myselfDevicesCount[0]++;
        } else {
            if (deviceModel.isOnline()) {
                newDeviceInfos.add(myselfDevicesCount[0] + othersOnlineDevicesCount[0], deviceModel);
                othersOnlineDevicesCount[0]++;
            } else
                newDeviceInfos.add(deviceModel);
        }
    }

    private void queryDevNameFromDB(DeviceModel deviceModel) {
        String devName = deviceModel.getDevName();
        addDisposable(deviceModel.getDevNameFromDB().subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!TextUtils.isEmpty(s) && !Objects.equals(devName, s))
                    if (_mDataSetObservable != null)
                        _mDataSetObservable.postValue(mDeviceModels);
            }
        }));
    }


    public void login(@NonNull final String deviceId, @Nullable final EventListener<LoginSession> eventListener, boolean isV5Anyway, boolean isOneOs) {

        LoginTokenUtil.getLoginToken(new LoginTokenUtil.TokenCallback() {
            @SuppressLint("CheckResult")
            @Override
            public void success(String token) {
//                DeviceModel deviceInfos = mSessions.get(deviceId);
                Device device = new Device();
                CMAPI.getInstance().getDeviceById(deviceId, device);
                final String vip = device.getVip();
                if (!device.isOnline()) {
                    if (eventListener != null)
                        eventListener.onFailure("", HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, "device is offline");
                    return;
                }
                DeviceModel deviceModel = mSessions.get(deviceId);
                if (deviceModel != null) {
                    if (!deviceModel.isRequestSession()) {
                        login(eventListener, vip, token, deviceId, isV5Anyway, isOneOs);
                    } else {
                        deviceModel.addEventListener(eventListener);
                    }
                } else {
                    if (eventListener != null) {
                        eventListener.onFailure("", HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, "device is offline");
                    }

                }
            }

            @Override
            public void error(GsonBaseProtocol protocol) {
                if (eventListener != null) {
                    eventListener.onFailure("", HttpErrorNo.ERR_ONE_REQUEST, "get token failure");
                }

            }
        });
    }

    private void login(@Nullable final EventListener<LoginSession> eventListener, final String ip,
                       String token, @NonNull final String deviceId, boolean isV5, boolean isOneOs) {

        OneOSLoginAPI.OnLoginListener listener = new OneOSLoginAPI.OnLoginListener() {

            @Override
            public void onStart(final String url) {
                runOnUI(new Runnable() {
                    @Override
                    public void run() {
                        if (eventListener != null) {
                            eventListener.onStart(url);
                        }
                    }
                });

            }

            @Override
            public void onSuccess(final String url, @NonNull final LoginSession loginSession) {
                loginSession.setV5(isV5);
                updateLoginSession(deviceId, loginSession);

                long currentTime = System.currentTimeMillis();
                long delayTime = AppConstants.SESSION_LIVE_TIME - (currentTime - loginSession.getLoginTime());
                if (delayTime < 0) {
                    delayTime = 0;
                }
                Logger.p(Logger.Level.INFO, Logger.Logd.DEBUG, "loginSession refresh session",
                        "after time : " + delayTime);

                timerScheduleRefresh(delayTime, deviceId);
                checkHasAddOldHD(loginSession);

                DeviceModel deviceModel = mSessions.get(deviceId);
                if (deviceModel != null) {
                    List<EventListener<LoginSession>> listeners = deviceModel.getListeners();
                    if (listeners != null)
                        for (EventListener<LoginSession> listener : listeners) {
                            if (listener != null) {
                                listener.onSuccess(url, loginSession);
                                deviceModel.removeEventListener(listener);
                            }
                        }
                    deviceModel.setRequestSession(false);
                    if (deviceModel.isOwner() && isOneOs) {
                        NasLanAccessViewModel.randomAdmin(loginSession);
                        DevAttrInfo devAttrInfo = loginSession.getDevAttrInfo();
                        if (devAttrInfo != null) {
                            deviceModel.setDevSn(devAttrInfo.sys.devicesn);
                        }
                    }
                    queryDevNameFromDB(deviceModel);
                }
                EventMsgManager.Companion.getInstance().startReceive(deviceId);
                runOnUI(new Runnable() {
                    @Override
                    public void run() {
                        if (eventListener != null)
                            eventListener.onSuccess(url, loginSession);
                    }
                });

            }

            @Override
            public void onFailure(final String url, final int errorNo, final String errorMsg) {
                DeviceModel deviceModel = mSessions.get(deviceId);
                if (deviceModel != null) {
                    List<EventListener<LoginSession>> listeners = deviceModel.getListeners();
                    if (listeners != null) {
                        for (EventListener<LoginSession> listener : listeners) {
                            if (listener != null) {
                                listener.onFailure(url, errorNo, errorMsg);
                                deviceModel.removeEventListener(listener);
                            }
                        }
                    }
                    deviceModel.setRequestSession(false);
                }
                runOnUI(new Runnable() {
                    @Override
                    public void run() {
                        if (eventListener != null)
                            eventListener.onFailure(url, errorNo, errorMsg);
                    }
                });

            }
        };
        final OneOSLoginAPI loginAPI = new OneOSLoginAPI(ip, OneOSAPIs.ONE_API_DEFAULT_PORT, token, deviceId);
        loginAPI.setTrueUser(username);
        loginAPI.setOnLoginListener(listener);
        loginAPI.access(AppConstants.DOMAIN_DEVICE_VIP, isV5, isOneOs);
        DeviceModel deviceModel = mSessions.get(deviceId);
        if (deviceModel != null) {
            deviceModel.setRequestSession(true);
        }


    }


    private void checkHasAddOldHD(@NonNull final LoginSession loginSession) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                final DevAttrInfo devAttrInfo = loginSession.getDevAttrInfo();
                if (devAttrInfo != null && devAttrInfo.sys != null && devAttrInfo.hd != null) {
                    if (!Objects.equals(devAttrInfo.sys, devAttrInfo.hd)//两者不相同
                            && devAttrInfo.sys.sameManufacturer(devAttrInfo.hd)) {//在同一厂商下
                        devAttrInfo.devId = loginSession.getId();
                        String devicesnHDSN = devAttrInfo.hd.devicesn;
                        DeviceModel deviceModelNew = null;
                        DeviceModel deviceModelOld = null;

                        Enumeration<DeviceModel> elements = mSessions.elements();
                        while (elements.hasMoreElements()) {
//                        for (DeviceModel deviceModel : mDeviceModels) {
                            DeviceModel deviceModel = elements.nextElement();
                            if (deviceModel == null) continue;
                            if (Objects.equals(deviceModel.getDevId(), devAttrInfo.devId)) {
                                deviceModelNew = deviceModel;
                            }
                            if (deviceModel.getWareDevice() != null &&
                                    Objects.equals(deviceModel.getDevSn(), devicesnHDSN)) {
                                deviceModelOld = deviceModel;
                            }
                        }


                        if (deviceModelNew != null && deviceModelOld != null) {
                            if (!Objects.equals(deviceModelOld.getDevId(), deviceModelNew.getDevId())) {//not same device
                                final HardWareDevice hardWareDevice = deviceModelNew.getWareDevice();
                                final HardWareDevice oldWareDevice = deviceModelOld.getWareDevice();
                                if (hardWareDevice != null && oldWareDevice != null) {
                                    if (Objects.equals(hardWareDevice.getUserid(), userId)
                                            && Objects.equals(oldWareDevice.getUserid(), userId)) {//devices's owner same

                                        final DeviceModel finalDeviceModelNew = deviceModelNew;
                                        final DeviceModel finalDeviceModelOld = deviceModelOld;
                                        ResultListener<SharedUserList> resultListener = new ResultListener<SharedUserList>() {
                                            SharedUserList dataNew;
                                            SharedUserList dataOld;

                                            @Override
                                            public void success(Object tag, SharedUserList data) {
                                                if (Objects.equals(finalDeviceModelNew.getDevId(), tag)) {
                                                    dataNew = data;
                                                }
                                                if (Objects.equals(finalDeviceModelOld.getDevId(), tag)) {
                                                    dataOld = data;
                                                }
                                                if (dataNew != null && dataOld != null) {
                                                    Iterator<ShareUser> iterator = dataOld.users.iterator();
                                                    while (iterator.hasNext()) {
                                                        ShareUser next = iterator.next();
                                                        if (TextUtils.isEmpty(next.username)) {
                                                            iterator.remove();
                                                            continue;
                                                        }
                                                        for (ShareUser user : dataNew.users) {
                                                            if (Objects.equals(user.userid, next.userid)) {
                                                                iterator.remove();
                                                            }
                                                        }
                                                    }
                                                    if (dataOld.users.size() > 0) {
                                                        DevHDAddNewUsers devHDAddNewUsers = new DevHDAddNewUsers();
                                                        devHDAddNewUsers.deviceModelNew = finalDeviceModelNew;
                                                        devHDAddNewUsers.deviceModelOld = finalDeviceModelOld;
                                                        devHDAddNewUsers.newUsers = dataOld.users;
                                                        devHDAddNewUsers.devAttrInfo = devAttrInfo;
                                                        EventBus.getDefault().post(devHDAddNewUsers);
                                                    }
                                                }
                                            }


                                            @Override
                                            public void error(Object tag, GsonBaseProtocol baseProtocol) {

                                            }
                                        };
                                        DeviceSharedUsersHttpLoader sharedUsersHttpLoader = new DeviceSharedUsersHttpLoader(SharedUserList.class);
                                        sharedUsersHttpLoader.setParams(deviceModelNew.getDevId());
                                        sharedUsersHttpLoader.executor(resultListener);
                                        DeviceSharedUsersHttpLoader sharedUsersHttpLoader2 = new DeviceSharedUsersHttpLoader(SharedUserList.class);
                                        sharedUsersHttpLoader2.setParams(deviceModelOld.getDevId());
                                        sharedUsersHttpLoader2.executor(resultListener);

                                    }
                                }
                            }
                        }


                    }
                }
            }
        };
        Runnable task = new Runnable() {
            @Override
            public void run() {
                AsyncTask.execute(runnable);
            }
        };
        mHandler.postDelayed(task, 10 * 1000);
    }

    public void registerDeviceDataObserver(Observer<List<DeviceModel>> observer) {
        try {
            ThreadUtils.ensureRunOnMainThread(() -> liveDataDeviceModels.observeForever(observer));
        } catch (Exception e) {
            Logger.p(Logger.Level.ERROR, "SessionMan-register deviceData", e.getMessage());
        }
    }

    public void unregisterDeviceDataObserver(Observer<List<DeviceModel>> observer) {
        try {
            ThreadUtils.ensureRunOnMainThread(() ->
                    liveDataDeviceModels.removeObserver(observer));
        } catch (Exception e) {
            Logger.p(Logger.Level.ERROR, "SessionMan-unregister", e.getMessage());
        }
    }

    @NonNull
    private final EventObserver mEventObserver = new EventObserver() {
        @Override
        public void onDeviceChanged() {
            refreshDeviceModels(null);
            if (mContextRef != null) {
                Context context = mContextRef.get();
                if (getOnlineDeviceModels().size() > 0 && context != null)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AppConstants.NOTIFY_DEVICE_READY_GO_FINDER));
            }
        }

        @Override
        public void onDeviceStatusChange(Device device) {
            if (UiUtils.isNas(device.getDevClass()) || UiUtils.isNasByFeature(device.getFeature())) {
                if (!TextUtils.isEmpty(device.getVip()) && !TextUtils.isEmpty(device.getId())) {
                    mIP_IDMap.put(device.getVip(), device.getId());
                }
                DeviceModel deviceModel = mSessions.get(device.getId());
                if (deviceModel != null) {
                    if (!device.isOnline()) {
                        LoginSession loginSession = deviceModel.getLoginSession();
                        if (loginSession != null) {
                            UserInfo info = loginSession.getUserInfo();
                            if (info != null) {
                                info.setIsLogout(true);
                                UserInfoKeeper.update(info);
                            }
                            removeSession(device.getId());
                        }
                        EventMsgManager.Companion.getInstance().stopReceive(device.getId());
                        updateDeviceModels(null);
                    } else {
                        LoginSession loginSession = deviceModel.getLoginSession();
                        if (loginSession != null) {
                            DeviceInfo deviceInfo = loginSession.getDeviceInfo();
                            if (deviceInfo != null) {
                                deviceInfo.setVIp(device.getVip());
                                deviceInfo.setLanIp(device.getPriIp());
                            }
                        }
                    }
                    deviceModel.setDevice(device);
                    _mDataSetObservable.postValue(mDeviceModels);
                } else {
                    updateDeviceModels(null);
                }
            }
        }

        @Override
        public void onNetworkChanged() {
            mDeviceModels.clear();
            mDeviceModel = null;
            refreshDeviceModels(null);
            EventMsgManager.Companion.getInstance().stopReceive();
        }
    };

    public void logoutCurrentAccount() {
        mSessions.clear();
        mDeviceModels.clear();
//        _mDataSetObservable.removeObserver();
        lastUserId = userId;
        mLifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        unbindService();
        EventBus.getDefault().removeAllStickyEvents();
        CMAPI.getInstance().unsubscribe(mEventObserver);
//        if (timer != null) {
//            timer.cancel();
//            timer = null;
//        }
        mHandler.removeCallbacksAndMessages(null);
        dispose();
        Context context = mContextRef.get();
        if (context != null)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(mReceiver);
    }

//--------------------------------------------------------------------------//

    private void timerScheduleRefresh(long delayTime, @NonNull final String devId) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SessionManager.getInstance().getLoginSession(devId
                        , new GetSessionListener(false) {


                            @Override
                            public void onSuccess(String url, @NonNull LoginSession data) {

                                Logger.p(Logger.Level.ERROR, Logger.Logd.DEBUG, "loginSession refresh session",
                                        data.getLoginTime() + " " + data.getSession());

                            }

                            @Override
                            public void onFailure(String url, int errorNo, String errorMsg) {
                                if (errorNo == HttpErrorNo.ERR_ONE_REQUEST)
                                    timerScheduleRefresh(60 * 1000, devId);
                                try {
                                    Logger.p(Logger.Level.ERROR, Logger.Logd.DEBUG, "loginSession refresh session", errorMsg);
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            }
                        });

            }
        }, /*AppConstants.SESSION_LIVE_TIME*/ delayTime);

    }

    public void runOnUI(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    private static boolean mIsServiceBound = false;
    private MutableLiveData<NasService> mServiceLiveData = new MutableLiveData<>();
    @NonNull
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceLiveData.postValue(null);
            mIsServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Service Connected");
            NasService.ServiceBinder binder = (NasService.ServiceBinder) service;
            mServiceLiveData.postValue(binder.getService());
            mIsServiceBound = true;
        }
    };

    /**
     * Bind NasService for download/upload/backup...
     */
    public void bindService() {
        Logger.p(Logger.Level.INFO, Logger.Logd.DEBUG, TAG, "Bind Transfer Service");
        Context context = mContextRef.get();
        if (context != null) {
            Intent intent = new Intent(context, NasService.class);
            if (context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Bind service success");
            } else {
                Logger.p(Logger.Level.ERROR, Logger.Logd.DEBUG, TAG, "Bind service failure");
            }

        }
    }

    public void unbindService() {
        if (mIsServiceBound) {
            Context context = mContextRef.get();
            if (context != null) {
                context.unbindService(mConnection);
            }
            if (mServiceLiveData.getValue() != null) {
                mServiceLiveData.getValue().stopSelf();
            }
            mIsServiceBound = false;
        }
    }

    public NasService getService() {
        if (mIsServiceBound && mServiceLiveData != null) {
            return mServiceLiveData.getValue();
        }
        return null;
    }

    public LiveData<NasService> getServiceLiveData() {
        return mServiceLiveData;
    }
}
