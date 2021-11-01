package net.sdvn.scorepaylib.score;

import android.content.Context;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.FindUserHttpLoader;
import net.sdvn.common.internet.loader.TransferScoreHttpLoader;
import net.sdvn.common.internet.loader.scorepay.GetUserScoreHttpLoader;
import net.sdvn.common.internet.loader.scorepay.ScoreConversionHttpLoader;
import net.sdvn.common.internet.loader.scorepay.ScoreGetRecordHttpLoader;
import net.sdvn.common.internet.loader.scorepay.ScoreUseRecordHttpLoader;
import net.sdvn.common.internet.loader.scorepay.UserRechargeRecordHttpLoader;

@Keep
public class ScoreAPIUtil {

    public static void getScore(HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        GetUserScoreHttpLoader httpLoader = new GetUserScoreHttpLoader(parseClass);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void ScoreConversion(String currency, HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        ScoreConversionHttpLoader httpLoader = new ScoreConversionHttpLoader(parseClass);
        httpLoader.setParams(currency);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void ScoreGetRecord(Context ctx, int pageNumber, int pageSize, HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        ScoreGetRecordHttpLoader httpLoader = new ScoreGetRecordHttpLoader(parseClass);
        httpLoader.setParams(ctx, pageNumber, pageSize);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void ScoreUseRecord(Context ctx, int pageNumber, int pageSize, HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        ScoreUseRecordHttpLoader httpLoader = new ScoreUseRecordHttpLoader(parseClass);
        httpLoader.setParams(ctx, pageNumber, pageSize);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void UserRechargeRecord(int pageNumber, int pageSize, HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        UserRechargeRecordHttpLoader httpLoader = new UserRechargeRecordHttpLoader(parseClass);
        httpLoader.setParams(pageNumber, pageSize);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void findUser(String account, HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        FindUserHttpLoader httpLoader = new FindUserHttpLoader(parseClass);
        httpLoader.setParams(account);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void transferScore(String userid, double amount, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        TransferScoreHttpLoader httpLoader = new TransferScoreHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(userid, amount);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }
}
