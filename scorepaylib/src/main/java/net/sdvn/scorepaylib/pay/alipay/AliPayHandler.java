package net.sdvn.scorepaylib.pay.alipay;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import java.util.Map;

@Keep
public class AliPayHandler extends Handler {
    private final Result result;

    public AliPayHandler(Result result) {
        this.result = result;
    }

    @SuppressWarnings("unused")
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case AliPayAPIUtil.SDK_PAY_FLAG: {
                @SuppressWarnings("unchecked")
                PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                /**
                 * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                 */
                String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为9000则代表支付成功
                if (TextUtils.equals(resultStatus, "9000")) {
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                    result.succ();
                } else if (TextUtils.equals(resultStatus, "6001")) {
                    // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                    result.cancelled();
                } else {
                    //支付失败
                    Log.e("SDVNPay", "AliPay 支付错误 resjult" + payResult.getResult());
                    result.failed(payResult.getResult());
                }
                break;
            }

            default:
                break;
        }
    }

    @Keep
    public interface Result {
        void succ();

        void cancelled();

        void failed(String err);
    }
}
