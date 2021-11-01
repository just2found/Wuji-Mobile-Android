package net.sdvn.scorepaylib.pay.alipay;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Keep;

import com.alipay.sdk.app.PayTask;

import java.util.Map;

@Keep
public class AliPayAPIUtil {
    ;
    public static final int SDK_PAY_FLAG = 1;
    private static String ALI_APPID = "";

    public static void setAliAPPID(String ALI_APPID) {
        AliPayAPIUtil.ALI_APPID = ALI_APPID;
    }

    public static String getAliAPPID() {
        return AliPayAPIUtil.ALI_APPID;
    }

    /**
     * 调支付的方法
     */
    @Keep
    public static void pay(final Activity activity, final Handler handler, final String orderInfo) {
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(activity);
                Map<String, String> result = alipay.payV2(orderInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
}
