package net.sdvn.scorepaylib.pay.wechat;

import android.content.Context;

import androidx.annotation.Keep;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

@Keep
public class WXAPIUtil {
    private static boolean isRegister;
    private static String WX_APPID = "wx16b2f729207f57af";

    public static void setWxAPPID(String WX_APPID) {
        WXAPIUtil.WX_APPID = WX_APPID;
    }

    public static String getWxAPPID() {
        return WXAPIUtil.WX_APPID;
    }

    public static IWXAPI getWXAPI(Context context) {
        return WXAPIFactory.createWXAPI(context, WX_APPID);
    }

    /**
     * 判断微信是否安装
     *
     * @param context
     * @return true 已安装   false 未安装
     */
    public static boolean isWxAppInstalled(Context context) {
        return getWXAPI(context).isWXAppInstalled();
    }

    /**
     * 调支付的方法
     * <p>
     * 注意： 每次调用微信支付的时候都会校验 appid 、包名 和 应用签名的。 这三个必须保持一致才能够成功调起微信
     */
    public static int startWechatPay(Context context, String orderJson) {
        //微信支付初始化
        IWXAPI api = getWXAPI(context);
        if (!isRegister) {
            isRegister = true;
            api.registerApp(WX_APPID);
        }

        try {
            JSONObject json = new JSONObject(orderJson);
            if (json.has("appid")) {
                //这里的appid，替换成自己的即可

                //这里的bean，是服务器返回的json生成的bean
                PayReq req = new PayReq();
                req.appId = WX_APPID;
                req.partnerId = json.getString("partnerid");//1576991231
                req.prepayId = json.getString("prepayid");
                req.packageValue = "Sign=WXPay";//固定值
                req.nonceStr = json.getString("noncestr");
                req.timeStamp = json.getString("timestamp");
                req.sign = json.getString("sign");
                req.extData = "app data"; // optional

                //发起请求，调起微信前去支付
                api.sendReq(req);
                return 0;
            } else {
                return -1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
