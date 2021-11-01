package net.linkmate.app.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.util.business.PrivilegeUtil;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.protocal.SampleConnectStatusListener;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.protocol.AccountPrivilegeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 特权服务信息管理
 */
public class PrivilegeManager {

    private static class SingleHolder {
        private static PrivilegeManager instance = new PrivilegeManager();
    }

    public static PrivilegeManager getInstance() {
        return SingleHolder.instance;
    }

    private List<AccountPrivilegeInfo.AdapterBean> privilegeBeans;
    private AccountPrivilegeInfo mInfo;
    private V2AgApiHttpLoader httpLoader;
    private CompositeDisposable compositeDisposable;

    private long serverCurrentTime;
    public static final long WEEK_TIME = 1000 * 60 * 60 * 24 * 7;

    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    protected void dispose() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }


    private PrivilegeManager() {
        privilegeBeans = new ArrayList<>();
        CMAPI.getInstance().addConnectionStatusListener(statusListener);
    }

    private final SampleConnectStatusListener statusListener = new SampleConnectStatusListener() {
        @Override
        public void onConnected() {
            initPrivilege(null);
        }

        @Override
        public void onDisconnected(int disconnectedReason) {
            isInitting = false;
            mInfo = null;
            if (privilegeBeans != null) {
                privilegeBeans.clear();
            }
            try {
                httpLoader.cancel();
            } catch (Exception ignore) {
            }
            dispose();
        }
    };

    private int errorTime = 0;
    private boolean isInitting;
    private boolean isPrompted;
    private ResultListener<AccountPrivilegeInfo> mListener;

    public boolean isInitting() {
        return isInitting;
    }

    public boolean isPrompted() {
        return isPrompted;
    }

    public void setPrompted(boolean isPrompted) {
        this.isPrompted = isPrompted;
    }

    public void initPrivilege(@Nullable final ResultListener<AccountPrivilegeInfo> listener) {
        if (isInitting || !CMAPI.getInstance().isConnected()) {
            if (listener != null) {
                if (mListener == null)
                    mListener = listener;
                if (mInfo!=null) {
                    listener.success(null, mInfo);
                }
            }
        } else {
            isInitting = true;
            if (listener != null) {
                mListener = listener;
            }
            httpLoader = PrivilegeUtil.getAccountInfo(null, new ResultListener<AccountPrivilegeInfo>() {
                @Override
                public void success(Object tag, AccountPrivilegeInfo data) {
                    mInfo = data;
                    isInitting = false;
                    errorTime = 0;
                    if (listener != null) {
                        listener.success("Privilege", data);
                    } else if (mListener != null) {
                        mListener.success("Privilege", data);
                    }
                    if (!isPrompted && getExpiringBeans().size() >= 1) {
                        notifyPrivilegeObserver();
                    }
                }

                @Override
                public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                    isInitting = false;
                    errorTime++;
                    int error = mErrorProtocol.result;
                    int delay = errorTime * 2000;
                    if (errorTime > 5) {
                        errorTime = 0;
//                        ToastUtils.showToast("http error PrivilegeManager initPrivilege: " +
//                                SdvnHttpErrorNo.ec2String(error));
                        delay = 2 * 60 * 1000;
                    }
                    Disposable disposable = Single.timer(delay, TimeUnit.MILLISECONDS)
                            .subscribe(aLong -> initPrivilege(listener));
                    addDisposable(disposable);
                    if (listener != null) {
                        listener.error("Privilege", mErrorProtocol);
                    } else if (mListener != null) {
                        mListener.error("Privilege", mErrorProtocol);
                    }
                }
            });
        }
    }

    public List<AccountPrivilegeInfo.AdapterBean> getPrivilegeBeans() {
        if (mInfo != null && mInfo.data != null) {
            AccountPrivilegeInfo.DataBean data = mInfo.data;
            privilegeBeans.clear();
            if (data.service != null) {
                serverCurrentTime = data.service.current;
                privilegeBeans.add(data.service);
            }
            if (data.vnodes != null) {
                ArrayList<AccountPrivilegeInfo.VnodesBean> beans = new ArrayList<>(data.vnodes);
                for (AccountPrivilegeInfo.VnodesBean bean : beans) {
                    if (bean.getExpired() > serverCurrentTime) {
                        if (bean.getExpired() - WEEK_TIME < serverCurrentTime) {
                            bean.setStatus(1);
                        } else {
                            bean.setStatus(0);
                        }
                    } else {
                        bean.setStatus(2);
                    }
                }
                privilegeBeans.addAll(beans);
            }
            if (data.devices != null) {
                privilegeBeans.addAll(data.devices);
            }
        }
        return new ArrayList<>(privilegeBeans);
    }

    public long getServerCurrentTime() {
        return serverCurrentTime;
    }

    public List<AccountPrivilegeInfo.AdapterBean> getExpiringBeans() {
        List<AccountPrivilegeInfo.AdapterBean> beanList = new ArrayList<>();
        for (AccountPrivilegeInfo.AdapterBean bean : getPrivilegeBeans()) {
            if (bean.getStatus() == 1) {
                beanList.add(bean);
            }
        }
        return beanList;
    }

    public interface PrivilegeObserver {
        void showPrivilege();
    }

    private final byte[] PrivilegeLock = new byte[0];
    private WeakHashMap<PrivilegeObserver, Integer> weakHashMap = new WeakHashMap<>();

    public synchronized void addPrivilegeObserver(PrivilegeObserver o) {
        synchronized (PrivilegeLock) {
            if (o == null)
                throw new NullPointerException();
            if (!weakHashMap.containsKey(o)) {
                weakHashMap.put(o, 0);
            }
        }
    }

    public synchronized void deletePrivilegeObserver(PrivilegeObserver o) {
        synchronized (PrivilegeLock) {
            weakHashMap.remove(o);
        }
    }

    private void notifyPrivilegeObserver() {
        synchronized (PrivilegeLock) {
            for (PrivilegeObserver o : weakHashMap.keySet()) {
                if (o != null) {
                    o.showPrivilege();
                }
            }
        }
    }
}
