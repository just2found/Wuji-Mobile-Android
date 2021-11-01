package net.linkmate.app.manager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.linkmate.app.base.DevBoundType;
import net.linkmate.app.base.MyApplication;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.util.NetworkUtils;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.LocalDevice;
import net.sdvn.cmapi.protocal.EventObserver;
import net.sdvn.cmapi.protocal.SampleConnectStatusListener;
import net.sdvn.cmapi.util.CommonUtils;
import net.sdvn.common.IntrDBHelper;
import net.sdvn.common.data.model.CircleDevice;
import net.sdvn.common.data.remote.NetRemoteDataSource;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.HardWareDevicesHttpLoader;
import net.sdvn.common.internet.protocol.CloudDeviceBeans;
import net.sdvn.common.internet.protocol.GetUserInfoResultBean;
import net.sdvn.common.internet.protocol.HardWareInfo;
import net.sdvn.common.internet.protocol.entity.HardWareDevice;
import net.sdvn.common.repo.BriefRepo;
import net.sdvn.common.repo.DevicesRepo;
import net.sdvn.common.repo.InNetDevRepo;
import net.sdvn.common.vo.BindDeviceModel;
import net.sdvn.common.vo.BindDeviceModel_;
import net.sdvn.common.vo.InNetDeviceModel;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.DeviceInfoKeeper;
import net.sdvn.nascommon.db.objecbox.DeviceInfo;
import net.sdvn.nascommon.iface.EBRefreshHardWareDevice;
import net.sdvn.nascommon.iface.Result;
import net.sdvn.nascommon.utils.log.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.objectbox.android.ObjectBoxLiveData;
import io.objectbox.query.Query;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.weline.repo.repository.V5SysInfoRepo;
import libs.source.common.AppExecutors;
import libs.source.common.utils.EmptyUtils;
import timber.log.Timber;


public class DevManager {

    private static final String TAG = DevManager.class.getSimpleName();
    private Collection<HardWareDevice> mHdev = new HashSet<>();
    private ConcurrentHashMap<String, DeviceBean> mAllDeviceBeans = new ConcurrentHashMap<>();
    private HardWareDevicesHttpLoader httpLoader;
    private ObjectBoxLiveData<BindDeviceModel> mBindDeviceModelObjectBoxLiveData;
    private Observer<List<BindDeviceModel>> mListObserver;
    private boolean isFirst = false;

    @Nullable
    public DeviceBean getDeviceBean(@NonNull String devId) {
        for (DeviceBean deviceBean : deviceBeans) {
            if (Objects.equals(deviceBean.getId(), devId)) {
                return deviceBean;
            }
        }
        return null;
    }

    @Nullable
    public DeviceBean getDeviceBeanByDomain(@NonNull String domain) {
        for (DeviceBean deviceBean : deviceBeans) {
            if (Objects.equals(deviceBean.getDomain(), domain)) {
                return deviceBean;
            }
        }
        return null;
    }

    private static class SingleHolder {
        private static DevManager instance = new DevManager();
    }

    public static DevManager getInstance() {
        return SingleHolder.instance;
    }

    private final EventObserver observer;
    @NonNull
    private List<DeviceBean> deviceBeans;
    @NonNull
    private List<DeviceBean> boundDeviceBeans;
    @NonNull
    private List<DeviceBean> localDevBeans;

    //云设备
    @NonNull
    private List<DeviceBean> cloudDevBeans = null;

    private V2AgApiHttpLoader refreshCloudDevicesLoader = null;

    /**
     * 刷新云设备
     *
     * @param loaderStateListener
     */
    public void refreshCloudDevices(HttpLoader.HttpLoaderStateListener loaderStateListener) {
        cloudDevBeans = null;
        if (refreshCloudDevicesLoader != null) refreshCloudDevicesLoader.cancel();
        refreshCloudDevicesLoader = new V2AgApiHttpLoader(CloudDeviceBeans.class) {
            @Override
            public void setAction(String action) {
                super.setAction(action);
                bodyMap = new ConcurrentHashMap();
                put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
            }
            //            https://app.memenet.net:8445/v2/agapi/getuseryunen?partid=xxxx&appid=xxxxxxx
        };
        refreshCloudDevicesLoader.setAction("getuseryunen");
        refreshCloudDevicesLoader.setHttpLoaderStateListener(loaderStateListener);
        refreshCloudDevicesLoader.executor(new ResultListener<CloudDeviceBeans>() {

            @Override
            public void error(@Nullable Object tag, GsonBaseProtocol baseProtocol) {
                refreshCloudDevicesLoader = null;
            }

            @Override
            public void success(@Nullable Object tag, CloudDeviceBeans data) {
                cloudDevBeans = new ArrayList<>();
                refreshCloudDevicesLoader = null;
                boolean isCanceled = false;
                if (data.result == 0 && data.getData() != null && data.getData().getList() != null && data.getData().getList().size() > 0) {
                    GetUserInfoResultBean.DataBean userInfoBean = UserInfoManager.getInstance().getUserInfoBean();
                    for (HardWareDevice bean : data.getData().getList()) {
                        //状态开关赋值给子项
                        bean.setEnable(data.getData().isEnable());
                        DeviceBean device = new DeviceBean(bean.getDevicename(), "", 3,
                                0);
                        device.setId(bean.getDeviceid());
                        bean.setNickname(userInfoBean.nickname);
                        device.setHardData(bean);
                        device.setOwner(userInfoBean.loginname);
                        if (cloudDevBeans != null) {
                            cloudDevBeans.add(device);
                        } else {
                            isCanceled = true;
                            break;
                        }
                    }
                }
                if (!isCanceled) {
                    notifyDevUpdateObserver(DevBoundType.ALL_BOUND_DEVICES);
                }

            }
        });
    }

    private HardWareInfo hardWareInfo = new HardWareInfo();

    private final SampleConnectStatusListener statusListener = new SampleConnectStatusListener() {
        @Override
        public void onDisconnected(int disconnectedReason) {
            isInitting = false;
            isFirst = false;
            try {
                if (httpLoader != null)
                    httpLoader.cancel();
            } catch (Exception ignore) {
            }
            if (mBindDeviceModelObjectBoxLiveData != null && mListObserver != null) {
                mBindDeviceModelObjectBoxLiveData.removeObserver(mListObserver);
            }
            if (EventBus.getDefault().isRegistered(DevManager.this))
                EventBus.getDefault().unregister(DevManager.this);
            clear();

        }

        @Override
        public void onConnected() {
            super.onConnected();
            subscribeDevicesBind();
            if (!EventBus.getDefault().isRegistered(DevManager.this))
                EventBus.getDefault().register(DevManager.this);
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case AppConstants.LOCAL_BROADCAST_REMOVE_DEV:
                            String devId2 = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
                            deleteDeviceModel(devId2);
                            break;
                    }
                }
            }
        }
    };

    private void deleteDeviceModel(String devId) {
        mAllDeviceBeans.remove(devId);
        DevicesRepo.remove(devId);
    }

    @Subscribe
    public void onEvent(EBRefreshHardWareDevice ebRefreshHardWareDevice) {
        initHardWareList(null);//收到刷新onEvent(EBRefreshHardWareDevice
    }

    private NetRemoteDataSource mNetRemoteDataSource = null;

    public void refreshENServerData() {
        if (CMAPI.getInstance().getBaseInfo() == null || CMAPI.getInstance().getBaseInfo().getNetid() == null)
            return;
        //刷新主EN服务器数据
        if (mNetRemoteDataSource == null) mNetRemoteDataSource = new NetRemoteDataSource();
        mNetRemoteDataSource.getENDevices(CMAPI.getInstance().getBaseInfo().getNetid(), new ResultListener<CircleDevice>() {
            @Override
            public void success(@Nullable Object tag, CircleDevice data) {
                if (data != null && data.getData() != null && data.getData().getList() != null) {
                    InNetDevRepo.INSTANCE.saveData((String) tag, data.getData().getList());
                } else {
                    InNetDevRepo.INSTANCE.saveData((String) tag, null);
                }
                notifyDeviceStateChanged();
            }

            @Override
            public void error(@Nullable Object tag, GsonBaseProtocol baseProtocol) {
            }
        });
    }


    public void refreshENServerData(Function<Boolean, Void> callback) {
        if (CMAPI.getInstance().getBaseInfo() == null || CMAPI.getInstance().getBaseInfo().getNetid() == null)
            return;
        //刷新主EN服务器数据
        if (mNetRemoteDataSource == null) mNetRemoteDataSource = new NetRemoteDataSource();
        mNetRemoteDataSource.getENDevices(CMAPI.getInstance().getBaseInfo().getNetid(), new ResultListener<CircleDevice>() {
            @Override
            public void success(@Nullable Object tag, CircleDevice data) {
                if (data != null && data.getData() != null && data.getData().getList() != null) {
                    InNetDevRepo.INSTANCE.saveData((String) tag, data.getData().getList());
                } else {
                    InNetDevRepo.INSTANCE.saveData((String) tag, null);
                }
                notifyDeviceStateChanged();
                callback.apply(true);
            }

            @Override
            public void error(@Nullable Object tag, GsonBaseProtocol baseProtocol) {
                callback.apply(false);
            }
        });
    }

    @Subscribe
    public void onEvent(RefreshENServer toRefreshENServer) {
        refreshENServerData();//收到刷新ENServer
    }

    private void subscribeDevicesBind() {
        String userId = CMAPI.getInstance().getBaseInfo().getUserId();
        if (!EmptyUtils.isEmpty(userId)) {
            Query<BindDeviceModel> query = IntrDBHelper.getBoxStore().boxFor(BindDeviceModel.class).
                    query().equal(BindDeviceModel_.userId, userId)
                    .build();
            mBindDeviceModelObjectBoxLiveData = new ObjectBoxLiveData<>(query);
            mListObserver = bindDeviceModels -> {
                Disposable subscribe = Observable.fromIterable(bindDeviceModels)
                        .flatMap(bindDeviceModel -> Observable.just(DevicesRepo.transform(bindDeviceModel)))
                        .toList()
                        .subscribe((hardWareDevices, throwable) -> {
                            setHardWareInfo(hardWareDevices);
                        });
                addDisposable(subscribe);
            };
            mBindDeviceModelObjectBoxLiveData.observeForever(mListObserver);
        }
    }

    private CompositeDisposable compositeDisposable;

    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    protected void clear() {
        if (compositeDisposable != null) compositeDisposable.clear();
        if (deviceBeans != null) {
            deviceBeans.clear();
        }
        if (boundDeviceBeans != null) {
            boundDeviceBeans.clear();
        }
        if (localDevBeans != null) {
            localDevBeans.clear();
        }
        mAllDeviceBeans.clear();
        mHdev.clear();
        notifyDevUpdateObserver(DevBoundType.MY_DEVICES);
        notifyDevUpdateObserver(DevBoundType.SHARED_DEVICES);
        notifyDevUpdateObserver(DevBoundType.ALL_BOUND_DEVICES);
    }

    private DevManager() {
        deviceBeans = new ArrayList<>();
        boundDeviceBeans = new ArrayList<>();
        localDevBeans = new ArrayList<>();
        observer = new EventObserver() {
            @Override
            public void onDeviceChanged() {
                notifyDeviceStateChanged();
//                refreshDevicesSubnetStatus();
            }

            @Override
            public void onDeviceStatusChange(Device device) {
                boolean isOffline = "offline".equalsIgnoreCase(device.getStatus());
                boolean isOnline = "online".equalsIgnoreCase(device.getStatus());
                HardWareDevice wareDevice = null;
                for (HardWareDevice boundDeviceBean : mHdev) {
                    if (Objects.equals(boundDeviceBean.getDeviceid(), device.getId())) {
                        wareDevice = boundDeviceBean;
                        break;
                    }
                }
//                int dt = CommonUtils.getDeviceType(device.getDevClass());
//                final boolean isNotTerminal = dt != Constants.DT_MACOS &&
//                        dt != Constants.DT_LINUX &&
//                        dt != Constants.DT_WINDOWS &&
//                        dt != Constants.DT_ANDROID &&
//                        dt != Constants.DT_IOS;
                DeviceBean deviceBean = mAllDeviceBeans.get(device.getId());
                if (deviceBean != null) {
                    deviceBean.refreshData(device);
                }
                String owner = wareDevice == null ? null : wareDevice.getUserid();
                if (isOffline || (wareDevice == null && isOnline) ||
                        (!Objects.equals(owner, device.getAdminUserId()))) {
                    initHardWareList(null);//设备离线上线更改owner
                } else {
                    notifyDeviceStateChanged();
                }
                if (isOnline) {
                    asyncUpdateGlobalModelToBean(device.getId());
                }
            }

            @Override
            public void onNetworkChanged() {
                //切换圈子清理在线设备和圈内设备
                Iterator<Map.Entry<String, DeviceBean>> entryIterator = mAllDeviceBeans.entrySet().iterator();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, DeviceBean> entry = entryIterator.next();
                    DeviceBean deviceBean = entry.getValue();
                    if (deviceBean != null) {
                        if (deviceBean.getHardData() == null) {
                            entryIterator.remove();
                        } else {
                            deviceBean.setEnServer(null);
                            deviceBean.refreshData(new Device());
                            deviceBean.setId(entry.getKey());
                        }
                    }
                }
                notifyDeviceStateChanged();
            }
        };
        CMAPI.getInstance().subscribe(observer);
        CMAPI.getInstance().addConnectionStatusListener(statusListener);
        // TODO: 2020/5/29   监听网络变化,当WiFi可用时扫描局域网设备

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.LOCAL_BROADCAST_RELOGIN);
        filter.addAction(AppConstants.LOCAL_BROADCAST_REMOVE_DEV);
        Context context = MyApplication.getContext();
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filter);
    }

    private void refreshDevicesSubnetStatus() {
        long start = System.currentTimeMillis();
        final List<Device> devices = CMAPI.getInstance().getDevices();
//        HashMap<Pair<String, Device.SubNet>, Integer> map = new HashMap<>();
//        HashMap<Integer, Set<Pair<String, Device.SubNet>>> conflictMap = new HashMap<>();
        Map<Integer, Map<String, List<Device.SubNet>>> mapList = new HashMap<>();
        for (Device device : devices) {
            if (device.subNets != null && device.subNets.size() > 0) {
                for (Device.SubNet subNet : device.subNets) {
                    final int maskInt = CommonUtils.calcPrefixLengthByNetMask(subNet.mask);
                    if (maskInt > 0 && maskInt <= 32) {
                        final int netId = CommonUtils.calcNetIdByNetmask(subNet.net, subNet.mask);
                        Map<String, List<Device.SubNet>> stringListMap = mapList.get(netId);
                        if (stringListMap == null) {
                            stringListMap = new HashMap<>();
                            mapList.put(netId, stringListMap);
                        }
                        List<Device.SubNet> list = stringListMap.get(device.getId());
                        if (list == null) {
                            list = new ArrayList<>();
                            stringListMap.put(device.getId(), list);
                        }
                        list.add(subNet);

//                        final Pair<String, Device.SubNet> keyPair = new Pair<>(device.getId(), subNet);
//                        //  已包含相同的net id
//                        if (map.containsValue(netId)) {
//                            Set<Pair<String, Device.SubNet>> keyPairs = conflictMap.get(netId);
//                            //如果 相同的冲突列表为空 则新建
//                            if (keyPairs == null) {
//                                keyPairs = new HashSet<>();
//                                //从map 添加已存在的value
//                                for (Map.Entry<Pair<String, Device.SubNet>, Integer> entry : map.entrySet()) {
//                                    if (entry.getValue() == netId) {
//                                        keyPairs.add(entry.getKey());
//                                    }
//                                }
//                                conflictMap.put(netId, keyPairs);
//                            }
//                            keyPairs.add(keyPair);
//                        }
//                        map.put(keyPair, netId);
                    }
                }

            }
        }
        //与本地局域网冲突
        NetworkUtils.getNetmask();
        //设备之间的subnet冲突
        if (mapList.size() > 0) {
//            DialogUtil.showConflictSubnets(MyApplication.getContext(), conflictMap);
            Logger.LOGD(TAG, "mapList  ", mapList);
        }
        Logger.LOGD(TAG, "conflict main time consumed : ", System.currentTimeMillis() - start);
    }

    private int errorTime = 0;
    private boolean isInitting;
    private ResultListener<HardWareInfo> mListener;

    public boolean isInitting() {
        return isInitting;
    }

    public void notifyDeviceStateChanged() {
        initNetDevices();
        refreshDevicesData(mHdev);
    }

    private Disposable initHardWareListDisposable = null;

    public void initHardWareList(@Nullable final ResultListener<HardWareInfo> listener) {
        refreshENServerData();//刷新绑定设备列表
        if (isInitting || !CMAPI.getInstance().isConnected()) {
            if (listener != null) {
                if (mListener == null)
                    mListener = listener;
                if (hardWareInfo != null) {
                    listener.success(null, hardWareInfo);
                }
            }
        } else {
            isInitting = true;
            if (listener != null) {
                mListener = listener;
            }
            httpLoader = new HardWareDevicesHttpLoader(HardWareInfo.class);
            httpLoader.executor(new ResultListener<HardWareInfo>() {
                @Override
                public void success(Object tag, HardWareInfo hardWareInfo) {
                    if (initHardWareListDisposable != null && initHardWareListDisposable.isDisposed()) {
                        initHardWareListDisposable.dispose();
                    }
                    DevManager.this.hardWareInfo = hardWareInfo;
                    setHardWareInfo(hardWareInfo.devices);
                    AppExecutors.Companion.getInstance().diskIO()
                            .execute(() -> {
                                DevicesRepo.saveData(hardWareInfo.devices);
                            });
//                    SessionManager.getInstance().refreshDeviceModels(mHdev);
                    if (isFirst) {
                        initLocalDevData(listener);
                    }
//                    initNetDevices();
                    isInitting = false;
                    errorTime = 0;
                    if (listener != null) {
                        listener.success("HardWare", hardWareInfo);
                    } else if (mListener != null) {
                        mListener.success("HardWare", hardWareInfo);
                    }
                }

                @Override
                public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                    isInitting = false;
                    int error = mErrorProtocol.result;
                    int delay = errorTime * 2000;
                    if (errorTime++ > 5) {
                        errorTime = 0;
//                        ToastUtils.showToast("http error DevManager initHardWareList: " +
//                                SdvnHttpErrorNo.ec2String(error));
                        delay = 2 * 60 * 1000;
                    }
                    Disposable disposable = Single.timer(delay, TimeUnit.MILLISECONDS)
                            .subscribe(aLong -> initHardWareList(listener));
                    initHardWareListDisposable = disposable;
                    addDisposable(disposable);
                    if (listener != null) {
                        listener.error("HardWare", mErrorProtocol);
                    } else if (mListener != null) {
                        mListener.error("HardWare", mErrorProtocol);
                    }
                }
            });
        }
    }

    public void setHardWareInfo(Collection<HardWareDevice> hDevs) {
        mHdev.clear();
        mHdev.addAll(hDevs);
        refreshDevicesData(mHdev);
    }


    private synchronized void refreshDevicesData(Collection<HardWareDevice> hDevs) {
        List<DeviceBean> beans = new ArrayList<>();
        if (hDevs != null) {
            List<Device> deviceList = CMAPI.getInstance().getDevices();
            List<InNetDeviceModel> netDeviceModels = InNetDevRepo.INSTANCE.getNetDeviceModels(SDVNManager.getInstance().getNetworkId());
            boolean isNew;
            for (HardWareDevice hDev : hDevs) {
                isNew = false;
                String deviceId = hDev.getDeviceid();
                DeviceBean bean = mAllDeviceBeans.get(deviceId);
                if (bean == null) {
                    bean = new DeviceBean(hDev.getDevicename(), hDev.getOwner(), 2,
                            Integer.valueOf(hDev.getMgrlevel()));
                    bean.setId(deviceId);
                    mAllDeviceBeans.put(deviceId, bean);
                    isNew = true;
                }
                boolean isOnline = false;
                for (Device device : deviceList) {
                    if (deviceId != null && deviceId.equals(device.getId())) {
                        bean.refreshData(device);
                        isOnline = true;
                        break;
                    }
                }
                if (!isOnline && bean.isOnline()) {
                    bean.refreshData(new Device());
                    bean.setId(deviceId);
                }
                boolean isFoundInNetDev = false;
                //注意：对于非当前网络，这个en对象的数据可能是错误的
                for (InNetDeviceModel deviceModel : netDeviceModels) {
                    if (Objects.equals(deviceModel.getDeviceId(), deviceId)) {
                        bean.setEnServer(deviceModel);
                        isFoundInNetDev = true;
                        break;
                    }
                }
                if (!isFoundInNetDev && bean.getEnServer() != null) {
                    bean.setEnServer(null);
                }
                if (isNew) {
                    asyncUpdateGlobalModelToBean(bean);
                }
                bean.setHardData(hDev);
                beans.add(bean);
            }
        }
        boundDeviceBeans.clear();
        boundDeviceBeans.addAll(beans);
        notifyDevUpdateObserver(DevBoundType.MY_DEVICES);
        notifyDevUpdateObserver(DevBoundType.SHARED_DEVICES);
        notifyDevUpdateObserver(DevBoundType.ALL_BOUND_DEVICES);
        initNetDevices();
    }

    public synchronized void initNetDevices() {
        List<DeviceBean> beans = new ArrayList<>();
//        for (Device device : CMAPI.getInstance().getDevices()) {
//            final DeviceBean bean = new DeviceBean(device);
//            beans.add(bean);
//            for (DeviceBean hDev : boundDeviceBeans) {
//                if (hDev.getHardData() != null &&
//                        hDev.getHardData().getDeviceid() != null &&
//                        Objects.equals(hDev.getHardData().getDeviceid(), bean.getId())) {
//                    bean.setHardData(hDev.getHardData());
//                    break;
//                }
//            }
//
//            //注意：对于非当前网络，这个en对象的数据可能是错误的
//            bean.setEnServer(InNetDevRepo.INSTANCE.getNetDeviceModel(bean.getId()));
//        }
        List<InNetDeviceModel> netDeviceModels = InNetDevRepo.INSTANCE.getNetDeviceModels(SDVNManager.getInstance().getNetworkId());
        for (Device device : CMAPI.getInstance().getDevices()) {
            String deviceId = device.getId();
            DeviceBean bean = mAllDeviceBeans.get(deviceId);
            if (bean == null) {
                bean = new DeviceBean(device);
                mAllDeviceBeans.put(deviceId, bean);
                asyncUpdateGlobalModelToBean(bean);
            } else {
                bean.refreshData(device);
            }
            beans.add(bean);
            boolean isFoundBindDev = false;
            for (HardWareDevice hDev : mHdev) {
                if (hDev != null
                        && Objects.equals(hDev.getDeviceid(), deviceId)) {
                    bean.setHardData(hDev);
                    isFoundBindDev = true;
                    break;
                }
            }
            if (!isFoundBindDev && bean.getHardData() != null) {
                bean.setHardData(null);
            }
            boolean isFoundInNetDev = false;
            //注意：对于非当前网络，这个en对象的数据可能是错误的
            for (InNetDeviceModel deviceModel : netDeviceModels) {
                if (Objects.equals(deviceModel.getDeviceId(), deviceId)) {
                    bean.setEnServer(deviceModel);
                    isFoundInNetDev = true;
                    break;
                }
            }
            if (!isFoundInNetDev && bean.getEnServer() != null) {
                bean.setEnServer(null);
            }
        }

        deviceBeans.clear();
        deviceBeans.addAll(beans);
        notifyDevUpdateObserver(DevBoundType.IN_THIS_NET);
    }

    public void asyncUpdateGlobalModelToBean(@NonNull String devId) {
        DeviceBean bean = mAllDeviceBeans.get(devId);
        if (bean != null) {
            asyncUpdateGlobalModelToBean(bean);
        }
    }

    private final V5SysInfoRepo repo = new V5SysInfoRepo();

    public void asyncUpdateGlobalModelToBean(@NonNull DeviceBean bean) {
        if (bean.isNas()) {
            BriefManager.INSTANCE.requestRemoteWhenNoCacheBrief(bean.getId(), BriefRepo.FOR_DEVICE,BriefRepo.PORTRAIT_AND_BRIEF_TYPE);
            addDisposable(Observable.create((ObservableOnSubscribe<Result<DeviceInfo>>) emitter -> {
                DeviceInfo deviceInfo = DeviceInfoKeeper.get(bean.getId());
                emitter.onNext(new Result<>(deviceInfo));
            }).flatMap(o -> {
                if (o.data == null || o.data.getDevIntroduction() == null) {
                    return repo.loadDevIntroduction(bean.getId(), bean.getVip())
                            .subscribeOn(Schedulers.io())
                            .flatMap(dataDevIntroductionBaseProtocol -> {
                                if (dataDevIntroductionBaseProtocol.getResult()) {
                                    DeviceInfoKeeper.update(bean.getId(), dataDevIntroductionBaseProtocol.getData());
                                    DeviceInfo deviceInfo = DeviceInfoKeeper.get(bean.getId());
                                    return Observable.just(new Result<>(deviceInfo));
                                } else {
                                    return Observable.just(new Result<DeviceInfo>(null));
                                }
                            });
                } else {
                    return Observable.just(o);
                }
            }).subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        if (o instanceof Result) {
                            Object data = ((Result) o).data;
                            if (data instanceof DeviceInfo) {
                                bean.setGlobalMode((DeviceInfo) data);
                            }
                        }
                    }, Timber::e));
        }
    }

    @SuppressLint("CheckResult")
    public void initLocalDevData(final ResultListener<HardWareInfo> listener) {
        final Disposable disposable = Observable.create(new ObservableOnSubscribe<List<DeviceBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<DeviceBean>> e) throws Exception {
                List<LocalDevice> lDevs = CMAPI.getInstance().SearchLocalDevice(MyApplication.getContext());
                List<DeviceBean> beans = new ArrayList<>();
                if (lDevs != null) {
                    for (LocalDevice lDev : lDevs) {
                        if (lDev == null) continue;
                        DeviceBean bean = new DeviceBean(lDev.getName(), "", 2,
                                2);
                        bean.setLocalData(lDev);
                        for (HardWareDevice hDev : mHdev) {
                            if (Objects.equals(lDev.getDeviceSn(), hDev.getDevicesn())) {
                                bean.setHardData(hDev);
                                break;
                            }
                        }
                        beans.add(bean);
                    }
                }
                e.onNext(beans);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<DeviceBean>>() {
                    @Override
                    public void accept(List<DeviceBean> beans) {
                        localDevBeans.clear();
                        localDevBeans.addAll(beans);
                        notifyDevUpdateObserver(DevBoundType.LOCAL_DEVICES);
                        if (listener != null) {
                            listener.success("LocalDevData", hardWareInfo);
                        } else if (mListener != null) {
                            mListener.success("LocalDevData", hardWareInfo);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        addDisposable(disposable);
    }

    //获取网络设备集合的数据
    public List<DeviceBean> getDeviceBeans() {
        return deviceBeans;
    }

    //获取绑定设备集合的数据
    public List<DeviceBean> getBoundDeviceBeans() {
        return boundDeviceBeans;
    }

    //获取本地设备集合的数据
    public List<DeviceBean> getLocalDeviceBeans() {
        return localDevBeans;
    }

    //获取附带标题数据的所有当前网络中的设备
    public List<DeviceBean> getAdapterDevices() {
        return getAdapterDevices(true);
    }

    public List<DeviceBean> getAdapterDevices(boolean addTitle) {
        List<DeviceBean> adapterDevices = new ArrayList<>(deviceBeans);
        if (addTitle) {
            sortAndAddTitle(adapterDevices);
        } else {
            sortByStatus(adapterDevices);
        }
        return adapterDevices;
    }

    //获取附带标题数据的所有绑定设备
    public List<DeviceBean> getAllBoundAdapterDevices() {
        List<DeviceBean> adapterDevices = new ArrayList<>();
        if (cloudDevBeans != null) adapterDevices.addAll(cloudDevBeans);
        if (boundDeviceBeans != null) adapterDevices.addAll(boundDeviceBeans);
        //绑定的设备不显示客户端
        Iterator<DeviceBean> iterator = adapterDevices.iterator();
        while (iterator.hasNext()) {
            DeviceBean next = iterator.next();
            if (next.getType() == 2) {
                iterator.remove();
            }
        }
        sortAndAddTitle(adapterDevices);
        return adapterDevices;
    }

    //获取附带标题数据的所有绑定设备
    public ArrayList<DeviceBean> getAllBoundAdapterDevices2() {
        if (boundDeviceBeans == null) return new ArrayList<DeviceBean>();
        ArrayList<DeviceBean> adapterDevices = new ArrayList<>(boundDeviceBeans);
        //绑定的设备不显示客户端
        Iterator<DeviceBean> iterator = adapterDevices.iterator();
        while (iterator.hasNext()) {
            DeviceBean next = iterator.next();
            if (next == null || next.getHardData() == null || next.getType() == 2) {
                iterator.remove();
            }
        }
        Collections.sort(adapterDevices, new Comparator<DeviceBean>() {
            @Override
            public int compare(DeviceBean o1, DeviceBean o2) {
                Boolean isRealOnline1 = o1.getHardData() == null ? false : o1.getHardData().isRealOnline();
                Boolean isRealOnline2 = o2.getHardData() == null ? false : o2.getHardData().isRealOnline();
                if (o1.getMnglevel() != o2.getMnglevel() && (o1.isOwner() || o2.isOwner())) {//非owner不管
                    if (o1.isOwner()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if ((o1.getMnglevel() == 3) != (o2.getMnglevel() == 3)) {//待同意的在最后
                    if (o1.getMnglevel() != 3) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (o1.isOnline() != o2.isOnline()) {
                    if (o1.isOnline()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (isRealOnline1 != isRealOnline2) {
                    if (isRealOnline1) {//真实在线的在前的在前
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (o1.isDevDisable() != o2.isDevDisable()) {
                    if (!o1.isDevDisable()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (o1.getType() != o2.getType()) {
                    return o1.getType() - o2.getType();
                } else if (o1.isVNode() != o2.isVNode()) {
                    if (o1.isVNode()) {
                        return -1;
                    } else {
                        return 1;
                    }

                } else {
                    try {
                        String o1Name = o1.getName();
                        String o2Name = o2.getName();
                        return o1Name.compareTo(o2Name);
                    } catch (Exception e) {
                        return 0;
                    }
                }
            }
        });
        return adapterDevices;
    }

    //获取附带标题数据的所有我的设备
    public List<DeviceBean> getMyAdapterDevices() {
        return getMyAdapterDevices(true);
    }

    public List<DeviceBean> getMyAdapterDevices(boolean addTitle) {
        List<DeviceBean> adapterDevices = new ArrayList<>();
        //绑定的设备不显示客户端
        Iterator<DeviceBean> iterator = boundDeviceBeans.iterator();
        while (iterator.hasNext()) {
            DeviceBean next = iterator.next();
            if (next.getMnglevel() == 0) {
                adapterDevices.add(next);
            }
        }
        if (addTitle) {
            sortAndAddTitle(adapterDevices);
        } else {
            sortByStatus(adapterDevices);
        }
        return adapterDevices;
    }

    //获取附带标题数据的所有共享的设备
    public List<DeviceBean> getSharedAdapterDevices() {
        return getSharedAdapterDevices(true);
    }

    public List<DeviceBean> getSharedAdapterDevices(boolean addTitle) {
        List<DeviceBean> adapterDevices = new ArrayList<>();
        //绑定的设备不显示客户端
        Iterator<DeviceBean> iterator = boundDeviceBeans.iterator();
        while (iterator.hasNext()) {
            DeviceBean next = iterator.next();
            if (next.getMnglevel() != 0) {
                adapterDevices.add(next);
            }
        }
        if (addTitle) {
            sortAndAddTitle(adapterDevices);
        } else {
            sortByStatus(adapterDevices);
        }
        return adapterDevices;
    }

    private void sortAndAddTitle(List<DeviceBean> adapterDevices) {
        sort(adapterDevices);
        //添加分类标签条目
        for (int i = 0; i < adapterDevices.size(); i++) {
            DeviceBean current = adapterDevices.get(i);
            if ((i == 0 && current.getType() != -1) ||
                    (i != 0 && adapterDevices.get(i - 1).getType() != -1 &&
                            current.getType() != -1 &&
                            adapterDevices.get(i - 1).getType() != current.getType())) {
                adapterDevices.add(i, new DeviceBean("", "", -1, current.getType()));
            }
        }
    }

    private void sort(List<DeviceBean> adapterDevices) {
        Collections.sort(adapterDevices, new Comparator<DeviceBean>() {
            @Override
            public int compare(DeviceBean o1, DeviceBean o2) {
                int i = o1.getType() - o2.getType();
                //设备状态 1-正常 2-停用 5-解绑停用 6-欠费停用
                int o1DeviceStatus = o1.getHardData() == null ? 0 : o1.getHardData().getDevicestatus();
                int o2DeviceStatus = o2.getHardData() == null ? 0 : o2.getHardData().getDevicestatus();
                if (i != 0) {
                    return i;
                } else if (o1DeviceStatus != o2DeviceStatus) {
                    if (o1DeviceStatus < o2DeviceStatus) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (o1.isOnline() != o2.isOnline()) {
                    if (o1.isOnline()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (o1.isDevDisable() != o2.isDevDisable()) {
                    if (!o1.isDevDisable()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    final int i1 = o1.getMnglevel() - o2.getMnglevel();
                    if (i1 != 0) {
                        return i1;
                    } else if (o1.isVNode() != o2.isVNode()) {
                        if (o1.isVNode()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        try {
                            String o1Name = o1.getName();
                            String o2Name = o2.getName();
                            return o1Name.compareTo(o2Name);
                        } catch (Exception e) {
                            return 0;
                        }
                    }
                }
            }
        });
    }

    private void sortByStatus(List<DeviceBean> adapterDevices) {
        Collections.sort(adapterDevices, new Comparator<DeviceBean>() {
            @Override
            public int compare(DeviceBean o1, DeviceBean o2) {
                int i = o1.getType() - o2.getType();
                if (o1.isOnline() != o2.isOnline()) {
                    if (o1.isOnline()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (i != 0) {
                    return i;
                } else if (o1.isVNode() != o2.isVNode()) {
                    if (o1.isVNode()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    final int i1 = o1.getMnglevel() - o2.getMnglevel();
                    if (i1 != 0) {
                        return i1;
                    } else {
                        try {
                            String o1Name = o1.getName();
                            String o2Name = o2.getName();
                            return o1Name.compareTo(o2Name);
                        } catch (Exception e) {
                            return 0;
                        }
                    }
                }
            }
        });
    }

    //获取局域网中的设备
    public List<DeviceBean> getLocalDevices() {
        return localDevBeans;
    }

    /**
     * 设备集合的观察者的操作接口(用于监听设备变化)
     */
    //定义数据集观察者接口
    public interface DevUpdateObserver {
        void onDevUpdate(int devBoundType);
    }

    private final byte[] DevUpdateLock = new byte[0];
    //定义集合保存数据集观察者接口对象
    private WeakHashMap<DevUpdateObserver, Integer> weakHashMap = new WeakHashMap<>();

    //添加数据集观察者到集合中
    public synchronized void addDevUpdateObserver(DevUpdateObserver o) {
        synchronized (DevUpdateLock) {
            if (o == null)
                throw new NullPointerException();
            if (!weakHashMap.containsKey(o)) {
                weakHashMap.put(o, 0);
            }
        }
    }

    //从集合中移除数据集观察者
    public synchronized void deleteDevUpdateObserver(DevUpdateObserver o) {
        synchronized (DevUpdateLock) {
            weakHashMap.remove(o);
        }
    }

    //通知所有的数据集观察者消息已经发生改变
    private void notifyDevUpdateObserver(int type) {
        synchronized (DevUpdateLock) {
            for (DevUpdateObserver o : weakHashMap.keySet()) {
                if (o != null) {
                    o.onDevUpdate(type);
                }
            }
        }
    }
}
