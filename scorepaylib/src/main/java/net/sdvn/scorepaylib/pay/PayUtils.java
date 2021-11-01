package net.sdvn.scorepaylib.pay;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.scorepay.CommitPayPalOrderHttpLoader;
import net.sdvn.common.internet.loader.scorepay.GetPayOrderHttpLoader;

@Keep
public class PayUtils {

    public static void GetPayOrder(String sku, String paytype,
                                   String totalfee, String feetype, String mbpoint, String ordername, String amount,
                                   HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        GetPayOrderHttpLoader httpLoader = new GetPayOrderHttpLoader(parseClass);
        httpLoader.setParams1(sku, paytype,
                totalfee, feetype, mbpoint, ordername, amount);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void GetPayOrderForOtherApp(String sku, String paytype,
                                              String totalfee, String feetype, String mbpoint, String ordername,
                                              String payappid,
                                              HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        GetPayOrderHttpLoader httpLoader = new GetPayOrderHttpLoader(parseClass);
        httpLoader.setParams(sku, paytype,
                totalfee, feetype, mbpoint, ordername, payappid);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void CommitPayPalOrder(String transactionid, String orderno, int status,
                                         HttpLoader.HttpLoaderStateListener loaderStateListener
            , Class<? extends GsonBaseProtocol> parseClass, ResultListener listener) {
        CommitPayPalOrderHttpLoader httpLoader = new CommitPayPalOrderHttpLoader(parseClass);
        httpLoader.setParams(transactionid, orderno, status);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }
}
