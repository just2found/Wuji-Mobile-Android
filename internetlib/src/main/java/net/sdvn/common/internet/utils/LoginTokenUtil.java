package net.sdvn.common.internet.utils;


import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.SdvnHttpErrorNo;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.listener.CommonResultListener;
import net.sdvn.common.internet.loader.ApplyLoginTokenHttpLoader;
import net.sdvn.common.internet.protocol.TokenResultBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

public class LoginTokenUtil {
    //    private LoginTokenUtil() {
//    }
//
//    private static class InstanceHolder {
//        private static LoginTokenUtil instance = new LoginTokenUtil();
//    }
//
//    public static LoginTokenUtil getInstance() {
//        return InstanceHolder.instance;
//    }
    private static String ticket;

    @Nullable
    private static String token;
    @NonNull
    private static List<TokenCallback> tokenCallbacks = new CopyOnWriteArrayList<>();
    @NonNull
    private static volatile Boolean requesting = false;

    @NonNull
    public static String getToken() {
        return token == null ? "" : token;
    }

    public static void clearToken() {
        token = null;
        Timber.i("clearToken");
    }

    public synchronized static void getLoginToken(final @Nullable TokenCallback callback) {
        final String ticket = CMAPI.getInstance().getBaseInfo().getTicket();
        final String account = CMAPI.getInstance().getBaseInfo().getAccount();
        if (TextUtils.isEmpty(ticket)) {
            GsonBaseProtocol baseProtocol = new GsonBaseProtocol();
            baseProtocol.result = SdvnHttpErrorNo.EC_INVALID_TICKET;
            baseProtocol.errmsg = "ticket无效";
            if (callback != null)
                callback.error(baseProtocol);
            return;
        }
        if (!TextUtils.isEmpty(token) && Objects.equals(LoginTokenUtil.ticket, ticket)) {
//            Logger.LOGI("LoginToken", "There was LoginToken: " + token);
            if (callback != null) {
                callback.success(token);
            }
        } else {
            if (!requesting) {
                requesting = true;
                ApplyLoginTokenHttpLoader loader = new ApplyLoginTokenHttpLoader(TokenResultBean.class);
                loader.setParams(ticket, account);
                loader.executor(new CommonResultListener<TokenResultBean>() {
                    @Override
                    public void success(Object tag, @NonNull TokenResultBean bean) {
                        requesting = false;
                        LoginTokenUtil.ticket = ticket;
                        token = bean.token;
                        Timber.i("get LoginToken success: %s", token);
                        List<TokenCallback> list = new ArrayList<>(tokenCallbacks);
                        for (TokenCallback tokenCallback : list) {
                            if (tokenCallback != null) {
                                tokenCallback.success(token);
                            }
                        }
                        tokenCallbacks.removeAll(list);
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                        requesting = false;
                        Timber.e("get LoginToken error");
                        List<TokenCallback> list = new ArrayList<>(tokenCallbacks);
                        for (TokenCallback tokenCallback : list) {
                            if (tokenCallback != null) {
                                tokenCallback.error(mErrorProtocol);
                            }
                        }
                        tokenCallbacks.removeAll(list);

                    }
                });

            }
            tokenCallbacks.add(callback);
        }
    }

    public static void getLoginToken(TokenCallback tokenCallback, boolean clearToken) {
        if (clearToken)
            clearToken();
        getLoginToken(tokenCallback);
    }

    public interface TokenCallback {
        void success(String token);

        void error(GsonBaseProtocol protocol);
    }
}
