package net.linkmate.app.util.business;

import androidx.annotation.Nullable;

import net.sdvn.common.data.remote.UserRemoteDataSource;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.ForgetPasswordHttpLoader;

import org.jetbrains.annotations.NotNull;

public class ResetPasswordUtil {
    public static void requestAuxCode(String countryCode, String phone,
                                      HttpLoader.HttpLoaderStateListener loaderStateListener,
                                      ResultListener listener) {
        ForgetPasswordHttpLoader loader = new ForgetPasswordHttpLoader(GsonBaseProtocol.class);
        loader.setParamsRequestResetPwdAuxCode(countryCode, phone);
        loader.setHttpLoaderStateListener(loaderStateListener);
        loader.executor(listener);
    }

    public static void requestAuxCode(String email,
                                      HttpLoader.HttpLoaderStateListener loaderStateListener,
                                      ResultListener listener) {
        ForgetPasswordHttpLoader loader = new ForgetPasswordHttpLoader(GsonBaseProtocol.class);
        loader.setParamsRequestResetPwdAuxCode(email);
        loader.setHttpLoaderStateListener(loaderStateListener);
        loader.executor(listener);
    }

    public static void reset(@Nullable String phone, @Nullable String email,
                             @NotNull String auxcode, @NotNull String password,
                             HttpLoader.HttpLoaderStateListener loaderStateListener,
                             @NotNull ResultListener listener) {
//        ResetPasswordHttpLoader loader = new ResetPasswordHttpLoader(GsonBaseProtocol.class);
//        loader.setParamsRequestResetPwd(phone, email, auxcode, password);
//        loader.setHttpLoaderStateListener(loaderStateListener);
//        loader.executor(listener);
        UserRemoteDataSource loader = new UserRemoteDataSource();
        loader.resetPasswd(phone,email, auxcode,password, listener)
                .setHttpLoaderStateListener(loaderStateListener);


    }
}
