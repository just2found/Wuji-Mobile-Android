package net.linkmate.app.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.base.MyOkHttpListener;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.protocal.SampleConnectStatusListener;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.GetUserInfoHttpLoader;
import net.sdvn.common.internet.protocol.GetUserInfoResultBean;
import net.sdvn.nascommon.SessionManager;

import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 特权服务信息管理
 */
public class UserInfoManager {

    private static class SingleHolder {
        private static UserInfoManager instance = new UserInfoManager();
    }

    public static UserInfoManager getInstance() {
        return SingleHolder.instance;
    }

    private GetUserInfoResultBean mInfo;
    private V2AgApiHttpLoader httpLoader;
    private CompositeDisposable compositeDisposable;

    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    protected void dispose() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }


    private UserInfoManager() {
        CMAPI.getInstance().addConnectionStatusListener(statusListener);
    }

    private final SampleConnectStatusListener statusListener = new SampleConnectStatusListener() {
        @Override
        public void onConnected() {
            initUserInfo(null);
        }

        @Override
        public void onDisconnected(int disconnectedReason) {
            isInitting = false;
            mInfo = null;
            try {
                httpLoader.cancel();
            } catch (Exception ignore) {
            }
            dispose();
        }
    };

    private int errorTime = 0;
    private boolean isInitting;
    private ResultListener<GetUserInfoResultBean> mListener;

    public boolean isInitting() {
        return isInitting;
    }

    public void initUserInfo(@Nullable final ResultListener<GetUserInfoResultBean> listener) {
        if (isInitting || !CMAPI.getInstance().isConnected()) {
            if (listener != null) {
                if (mListener == null)
                    mListener = listener;
                if (mInfo != null) {
                    listener.success(null, mInfo);
                }
            }
        } else {
            isInitting = true;
            if (listener != null) {
                mListener = listener;
            }
            httpLoader = new GetUserInfoHttpLoader(GetUserInfoResultBean.class);
            httpLoader.executor(new MyOkHttpListener<GetUserInfoResultBean>() {
                @Override
                public void success(Object tag, GetUserInfoResultBean data) {
                    mInfo = data;
                    isInitting = false;
                    errorTime = 0;
                    SessionManager.getInstance().setUsername(data.data.loginname);
                    if (listener != null) {
                        listener.success("UserInfo", data);
                    } else if (mListener != null) {
                        mListener.success("UserInfo", data);
                    }
                    notifyUserInfoObserver();
                }

                @Override
                public void error(Object tag, GsonBaseProtocol baseProtocol) {
                    isInitting = false;
                    errorTime++;
                    int error = baseProtocol.result;
                    int delay = errorTime * 2000;
                    if (errorTime > 5) {
                        errorTime = 0;
//                        ToastUtils.showToast("http error UserInfoManager initUserInfo: " +
//                                SdvnHttpErrorNo.ec2String(error));
                        delay = 2 * 60 * 1000;
                    }
                    Disposable disposable = Single.timer(delay, TimeUnit.MILLISECONDS)
                            .subscribe(aLong -> initUserInfo(listener));
                    addDisposable(disposable);
                    if (listener != null) {
                        listener.error("UserInfo", baseProtocol);
                    } else if (mListener != null) {
                        mListener.error("UserInfo", baseProtocol);
                    }
                }
            });
        }
    }

    public GetUserInfoResultBean.DataBean getUserInfoBean() {
        GetUserInfoResultBean.DataBean data = null;
        if (mInfo != null && mInfo.data != null) {
            data = mInfo.data;
        }
        return data;
    }

    public interface UserInfoObserver {
        void showUserInfo();
    }

    private final byte[] UserInfoLock = new byte[0];
    private WeakHashMap<UserInfoObserver, Integer> weakHashMap = new WeakHashMap<>();

    public synchronized void addUserInfoObserver(UserInfoObserver o) {
        synchronized (UserInfoLock) {
            if (o == null)
                throw new NullPointerException();
            if (!weakHashMap.containsKey(o)) {
                weakHashMap.put(o, 0);
            }
        }
    }

    public synchronized void deleteUserInfoObserver(UserInfoObserver o) {
        synchronized (UserInfoLock) {
            weakHashMap.remove(o);
        }
    }

    private void notifyUserInfoObserver() {
        synchronized (UserInfoLock) {
            for (UserInfoObserver o : weakHashMap.keySet()) {
                if (o != null) {
                    o.showUserInfo();
                }
            }
        }
    }
}
