package net.linkmate.app.util.business;

import androidx.annotation.NonNull;

import net.sdvn.common.data.remote.UserRemoteDataSource;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.RegisterHttpLoader;

public class RegisterUtil {
    public static void requestAuxCode(String countryCode, String phone,
                                      HttpLoader.HttpLoaderStateListener loaderStateListener,
                                      ResultListener listener) {
        RegisterHttpLoader loader = new RegisterHttpLoader(GsonBaseProtocol.class);
        loader.setParamsRequestAuxCode(countryCode, phone);
        loader.setHttpLoaderStateListener(loaderStateListener);
        loader.executor(listener);
    }

    public static void requestAuxCode(String email,
                                      HttpLoader.HttpLoaderStateListener loaderStateListener,
                                      ResultListener listener) {
        RegisterHttpLoader loader = new RegisterHttpLoader(GsonBaseProtocol.class);
        loader.setParamsRequestAuxCode(email);
        loader.setHttpLoaderStateListener(loaderStateListener);
        loader.executor(listener);
    }

    public static void authAuxCode(String countryCode, String phone, String auxcode,
                                   HttpLoader.HttpLoaderStateListener loaderStateListener,
                                   ResultListener listener) {
        RegisterHttpLoader loader = new RegisterHttpLoader(GsonBaseProtocol.class);
        loader.setParamsAuthAuxCode(countryCode, phone, auxcode);
        loader.setHttpLoaderStateListener(loaderStateListener);
        loader.executor(listener);
    }

    public static void authAuxCode(String email, String auxcode,
                                   HttpLoader.HttpLoaderStateListener loaderStateListener,
                                   ResultListener listener) {
        RegisterHttpLoader loader = new RegisterHttpLoader(GsonBaseProtocol.class);
        loader.setParamsAuthAuxCode(email, auxcode);
        loader.setHttpLoaderStateListener(loaderStateListener);
        loader.executor(listener);
    }

    public static void register(String countryCode, String account, String phone, String auxcode,
                                String nickName, String password, String firstname, String lastname,
                                HttpLoader.HttpLoaderStateListener loaderStateListener,
                                ResultListener listener) {
//        RegisterHttpLoader loader = new RegisterHttpLoader(GsonBaseProtocol.class);
//        loader.setParamsAccRgister(countryCode, phone, auxcode, nickName, password);
//        loader.setHttpLoaderStateListener(loaderStateListener);
//        loader.executor(listener);
        UserRemoteDataSource loader = new UserRemoteDataSource();
        loader.registerV2(account, phone, null, password, firstname, lastname, nickName, auxcode, listener)
                .setHttpLoaderStateListener(loaderStateListener);

    }

    public static void register(String account, String email, @NonNull String auxcode,
                                String nickName, String password, String firstname, String lastname,
                                HttpLoader.HttpLoaderStateListener loaderStateListener,
                                ResultListener listener) {
//        RegisterHttpLoader loader = new RegisterHttpLoader(GsonBaseProtocol.class);
//        loader.setParamsAccRgister(email, auxcode, nickName, password);
//        loader.setHttpLoaderStateListener(loaderStateListener);
//        loader.executor(listener);
        UserRemoteDataSource loader = new UserRemoteDataSource();
        loader.registerV2(account, null, email, password, firstname, lastname, nickName, auxcode, listener)
                .setHttpLoaderStateListener(loaderStateListener);
    }
}
