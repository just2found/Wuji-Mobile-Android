package net.linkmate.app.manager;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Network;
import net.sdvn.cmapi.protocal.EventObserver;
import net.sdvn.cmapi.protocal.SampleConnectStatusListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

@Deprecated
public class NetManager {

    private final EventObserver observer;
    private final SampleConnectStatusListener statusListener = new SampleConnectStatusListener() {
        @Override
        public void onDisconnected(int disconnectedReason) {
            if (netBeans != null) {
                netBeans.clear();
            }
        }
    };

    private List<Network> netBeans;
//    private NetRemoteDataSource mNetRemoteDataSource;
//    private ResultListener<BindNetsInfo> listener = new ResultListener<BindNetsInfo>() {
//        @Override
//        public void success(@Nullable Object tag, BindNetsInfo data) {
//            if (data.data != null) {
//                NetsRepo.saveData(data.data.getList());
//            }
//        }
//
//        @Override
//        public void error(@Nullable Object tag, GsonBaseProtocol baseProtocol) {
//
//        }
//    };

    private NetManager() {
        netBeans = new ArrayList<>();
//        mNetRemoteDataSource = new NetRemoteDataSource();
        observer = new EventObserver() {
            @Override
            public void onNetworkChanged() {
                initNets();
                notifyNetUpdateObserver();
            }
        };
        CMAPI.getInstance().addConnectionStatusListener(statusListener);
        CMAPI.getInstance().subscribe(observer);
        initNets();
//        EventBus.getDefault().register(this);
    }

//    @Subscribe
//    public void onEvent(NetChanged netChanged) {
//        refreshBindNetsInfo();
//    }

    private void initNets() {
        netBeans.clear();
        netBeans = new ArrayList<>(CMAPI.getInstance().getNetworkList());
        Collections.sort(netBeans, new Comparator<Network>() {
            @Override
            public int compare(Network o1, Network o2) {
                String userId = CMAPI.getInstance().getBaseInfo().getUserId();
                if (Objects.equals(o1.getUid(), o2.getUid())) {
                    return o1.getName().compareTo(o2.getName());
                }
                if (Objects.equals(userId, o1.getUid()))
                    return -1;
                if (Objects.equals(userId, o2.getUid()))
                    return 1;
                return o1.getUid().compareTo(o2.getUid());
            }
        });
//        refreshBindNetsInfo();
    }

//    private void refreshBindNetsInfo() {
//        mNetRemoteDataSource.getBindNetsInfo(this.listener);
//    }

    private static class SingleHolder {
        private static NetManager instance = new NetManager();
    }

    public static NetManager getInstance() {
        return SingleHolder.instance;
    }

    //获取网络设备集合的数据
    public List<Network> getNetBeans() {
        return netBeans;
    }

    //当前网络排在最前
    public List<Network> getNetBeansBySort() {
        List<Network> adapterNetworks = new ArrayList<>(netBeans);
        sortByCurrent(adapterNetworks);
        return adapterNetworks;
    }

    private void sortByCurrent(List<Network> adapterDevices) {
        Collections.sort(adapterDevices, new Comparator<Network>() {
            @Override
            public int compare(Network o1, Network o2) {
                if (o1.isCurrent()) {
                    return -1;
                } else if (o2.isCurrent()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    /**
     * 设备集合的观察者的操作接口(用于监听设备变化)
     */
    //定义数据集观察者接口
    public interface NetUpdateObserver {
        void onNetUpdate();
    }

    private final byte[] NetUpdateLock = new byte[0];
    //定义集合保存数据集观察者接口对象
    private WeakHashMap<NetUpdateObserver, Integer> weakHashMap = new WeakHashMap<>();

    //添加数据集观察者到集合中
    public synchronized void addNetUpdateObserver(NetUpdateObserver o) {
        synchronized (NetUpdateLock) {
            if (o == null)
                throw new NullPointerException();
            if (!weakHashMap.containsKey(o)) {
                weakHashMap.put(o, 0);
            }
        }
    }

    //从集合中移除数据集观察者
    public synchronized void deleteNetUpdateObserver(NetUpdateObserver o) {
        synchronized (NetUpdateLock) {
            weakHashMap.remove(o);
        }
    }

    //通知所有的数据集观察者消息已经发生改变
    private void notifyNetUpdateObserver() {
        synchronized (NetUpdateLock) {
            for (NetUpdateObserver o : weakHashMap.keySet()) {
                if (o != null) {
                    o.onNetUpdate();
                }
            }
        }
    }

    interface NetChanged {
    }
}
