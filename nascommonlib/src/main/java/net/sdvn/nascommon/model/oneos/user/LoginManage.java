package net.sdvn.nascommon.model.oneos.user;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.UserInfoKeeper;
import net.sdvn.nascommon.db.objecbox.UserInfo;
import net.sdvn.nascommon.model.oneos.event.EventMsgManager;
import net.sdvn.nascommon.service.NasService;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Used to manage user login information.
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/11.
 */
public class LoginManage {
    @Nullable
    private LoginSession loginSession = null;

    //    private OneServerUserInfo oneServerUserInfo = null;

    /**
     * Singleton instance of {@link LoginManage}
     */
    @NonNull
    private static LoginManage INSTANCE = new LoginManage();

    /**
     * Get singleton instance of the class.
     *
     * @return {@link LoginManage} Instance
     */
    @NonNull
    public static LoginManage getInstance() {
        return LoginManage.INSTANCE;
    }

    private LoginManage() {
    }

    /**
     * Is logged OneSpace
     *
     * @return {@code true} if logged, {@code false} otherwise
     */
    public boolean isLogin() {
        if (loginSession == null) {
            return false;
        }
        return loginSession.isLogin();
    }

    public boolean isAdmin() {
        if (loginSession == null)
            return false;
        if (loginSession.getUserInfo() == null)
            return false;
        return loginSession.getUserInfo().getAdmin() == 1;
    }

    public boolean isLogin(boolean isNeedsTips) {
        if (isLogin()) {
            return true;
        } else {
            if (isNeedsTips) {
                ToastHelper.showToast(R.string.tip_wait_for_service_connect);
            }
            return false;
        }
    }

    /**
     * Logout OneSpace
     *
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean logout() {
        NasService mTransferService = SessionManager.getInstance().getService();
        if (mTransferService != null) {
            mTransferService.notifyUserLogout();
        }
        if (isLogin()) {
            UserInfo info = loginSession.getUserInfo();
            info.setIsLogout(true);
            UserInfoKeeper.update(info);
            loginSession.setSession("");
            return true;
        }
        return false;
    }

    /**
     * Save User {@link LoginSession} Information
     * <p/>
     * You should update it only when the user login successful, and it will be saved for later use.
     *
     * @param loginSession {@link LoginSession}
     */
    public void setLoginSession(@NonNull LoginSession loginSession) {
        if (this.loginSession == null || !Objects.equals(this.loginSession, loginSession)
                || !Objects.equals(this.loginSession.getIp(), loginSession.getIp())
                || !Objects.equals(this.loginSession.getSession(), loginSession.getSession())) {
            notifyObservers();
        }
        this.loginSession = loginSession;
        if (isLogin()) {
            final String mac = loginSession.getId();
            SPUtils.setValue(Utils.getApp(), AppConstants.SP_FIELD_DEVICE_ID, mac);
            EventMsgManager.Companion.getInstance().startReceive(mac);
        }
    }

    private void notifyObservers() {
        final List<ObserverLoginSessionState> observers = mObserverLoginSessionStates;
        for (ObserverLoginSessionState observer : observers) {
            if (observer != null)
                observer.onChange();
        }
    }

    public void addObserverLoginSessionState(@Nullable ObserverLoginSessionState o) {
        if (o != null)
            mObserverLoginSessionStates.add(o);
    }

    public void removeObserverLoginSessionState(ObserverLoginSessionState o) {
        try {
            mObserverLoginSessionStates.remove(o);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @NonNull
    private List<ObserverLoginSessionState> mObserverLoginSessionStates = new ArrayList<>();

    public boolean isLogin(@Nullable LoginSession loginSession) {
        if (loginSession == null) {
            return false;
        }
        if (loginSession.getUserInfo() == null || loginSession.getDeviceInfo() == null) {
            return false;
        }
        return !TextUtils.isEmpty(loginSession.getSession());
    }

    public interface ObserverLoginSessionState {
        void onChange();
    }

    /**
     * Get the saved {@link LoginSession}
     *
     * @return {@link LoginSession}
     */
    @Nullable
    public LoginSession getLoginSession() {
        return this.loginSession;
    }


    public boolean isHttp() {
        return null == loginSession || !loginSession.isSSUDPDevice();
    }

}
