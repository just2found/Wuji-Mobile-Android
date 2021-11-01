package net.sdvn.scorepaylib.pay.paypal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import net.sdvn.common.internet.SdvnHttpErrorNo;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.scorepaylib.pay.PayUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Create by Moosen on 09/11/2016
 */
public class PayPalUtils {

    private static final String TAG = "PayPalHelper";
    //配置何种支付环境，一般沙盒，正式
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    //你所注册的APP Id
    private static final String CONFIG_CLIENT_ID = "Acn-k7Jp-sGY-j4psp1BRn1gnfy8BvXcKVVJZ3u8shPMz7yZ9R0frzz6l8B90nhY6Kd5TTRHpsG-JkbV";
    //test
//    private static final String CONFIG_CLIENT_ID = "AbGHE9HciDeQXvNTOjF2l5cZfRemBqD30afF96IAap49lowUoBcuDlwiB3r3dU9VoTbQ-w7b82R05v3K";
    //    paypal.secret       = EJi5kfnzLb9TX3C3ScDtwimApBzkKF9po02EBJSVaDVfzXJrQJ7uAf15-vUgNw27F0sbkmu_rYu300Hh
    private static final int REQUEST_CODE_PAYMENT = 1;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID);

    private final static String mSPName = "PAYPAL_SP";
    private final static int mSPMode = Context.MODE_PRIVATE;
    private final static String mSPSdvnOrderNo = "SDVN_ORDER_NO";
    private final static String mSPPaypalResultInfo = "PAYPAL_RESULT_INFO&";

    private static PayPalUtils payPalHelper;
    private String sdvnorderno;
    private PayPalResultInfo paypalResultInfo;
    private Handler threadHandler;

    private PayPalUtils() {
        HandlerThread mHandlerThread = new HandlerThread("Thread MessageManager");
        mHandlerThread.start();
        threadHandler = new Handler(mHandlerThread.getLooper());
    }

    public static PayPalUtils getInstance() {
        if (payPalHelper == null) {
            synchronized (PayPalUtils.class) {
                payPalHelper = new PayPalUtils();
            }
        }
        return payPalHelper;
    }

    /**
     * 启动PayPal服务
     *
     * @param context
     */
    public void startPayPalService(Context context) {
        Intent intent = new Intent(context, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        context.startService(intent);
    }

    /**
     * 停止PayPal服务  sdfsdfsdssaaass
     *
     * @param context
     */
    public void stopPayPalService(Context context) {
        context.stopService(new Intent(context, PayPalService.class));
    }

    /**
     * 开始执行支付操作
     *
     * @param context
     * @param p           价格
     * @param currency    币种（"USD"）
     * @param goodsName   商品名
     * @param orderNumber sku
     */
    public void doPayPalPay(Context context, double p, String currency, String goodsName, String orderNumber) {
        BigDecimal price = new BigDecimal(Double.toString(p));
        PayPalItem[] items = {new PayPalItem(goodsName, 1, price, currency, orderNumber)};
        PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails(new BigDecimal(0), price, new BigDecimal(0));
        PayPalPayment payment = new PayPalPayment(price, currency, goodsName, PayPalPayment.PAYMENT_INTENT_SALE);
        payment.items(items).paymentDetails(paymentDetails);
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    /**
     * 处理支付之后的结果
     *
     * @param context
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void confirmPayResult(final Context context, int requestCode, int resultCode, Intent data, final DoResult doResult) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));

                        JSONObject jsonObject = confirm.toJSONObject();
                        if (jsonObject != null) {
                            JSONObject response = jsonObject.optJSONObject("response");
                            if (response != null) {
                                String id = response.optString("id");
                                doResult.succ(id);
                                return;
                            }
                        }
                        doResult.failed("no response");
                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                        doResult.failed("an extremely unlikely failure occurred: " + e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
                doResult.cancelled();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                doResult.failed("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    //记录账单号
    public void setSdvnorderno(Context context, String sdvnorderno) {
        SharedPreferences sp = context.getSharedPreferences(mSPName, mSPMode);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(mSPSdvnOrderNo, sdvnorderno);
        edit.apply();
        this.sdvnorderno = sdvnorderno;
    }

    public String getSdvnorderno(Context context) {
        SharedPreferences sp = context.getSharedPreferences(mSPName, mSPMode);
        return sp.getString(mSPSdvnOrderNo, "");
    }

    //记录账单号以及支付回执
    public void savePaypalResultInfo(Context context, String userid, String paypalid, String logPath) {
        SharedPreferences sp = context.getSharedPreferences(mSPName, mSPMode);
        SharedPreferences.Editor edit = sp.edit();
        String value = getSdvnorderno(context) + " & " + paypalid;
        edit.putString(mSPPaypalResultInfo + userid, value);
        edit.apply();
        saveInStorage(value, logPath);
    }

    //记录账单号以及支付回执于手机存储
    private void saveInStorage(final String value, final String logPath) {
        threadHandler.post(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    File dataDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + logPath);
                    if (!dataDir.exists()) {
                        dataDir.mkdirs();
                    }
                    long freeSpace = Environment.getExternalStorageDirectory().getFreeSpace();
                    if (freeSpace < 1024 * 1024) {
                        return;
                    }
                    Calendar cal = Calendar.getInstance();
                    String timeString = cal.get(Calendar.YEAR) + "_" +
                            (cal.get(Calendar.MONTH) + 1) + "_" + cal.get(Calendar.DAY_OF_MONTH) + "_" +
                            (cal.get(Calendar.HOUR_OF_DAY)) + "_" + cal.get(Calendar.MINUTE) + "_" +
                            cal.get(Calendar.SECOND);
                    File file = new File(dataDir, "PayPalLog" + ".txt");
                    fos = new FileOutputStream(file, true);
                    fos.write((timeString + " " + value + "\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void clearPaypalResultInfo(Context context, String userid) {
        SharedPreferences sp = context.getSharedPreferences(mSPName, mSPMode);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(mSPPaypalResultInfo + userid, "");
        edit.apply();
    }

    public PayPalResultInfo getPaypalResultInfo(Context context, String userid) {
        SharedPreferences sp = context.getSharedPreferences(mSPName, mSPMode);
        String value = sp.getString(mSPPaypalResultInfo + userid, "");
        PayPalResultInfo info = null;
        if (value != null && value.contains(" & ")) {
            String[] split = value.split(" & ");
            info = new PayPalResultInfo(split[0], split[1]);
        }
        return info;
    }

    private boolean isCommitOldOrder;

    public boolean isCommitOldOrder() {
        return isCommitOldOrder;
    }

    public boolean hasNoCommitedOrder(Context context, String userid) {
        if (!isCommitOldOrder) {
            PayPalResultInfo info = getPaypalResultInfo(context, userid);
            boolean b = info != null && !TextUtils.isEmpty(info.sdvnorderno) && !TextUtils.isEmpty(info.paypalid);
            if (b) {
                isCommitOldOrder = true;
                commitOrder(context, userid, info.sdvnorderno, info.paypalid, null, new ResultListener() {
                    @Override
                    public void success(Object tag, GsonBaseProtocol data) {
                        isCommitOldOrder = false;
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        isCommitOldOrder = false;
                    }
                });
            }
            return b;
        }
        return true;
    }

    public void commitOrder(final Context context, final String userid, String orderno, String id,
                            HttpLoader.HttpLoaderStateListener loaderStateListener, final ResultListener listener) {
        PayUtils.CommitPayPalOrder(id, orderno,
                0, loaderStateListener, GsonBaseProtocol.class,
                new ResultListener() {
                    @Override
                    public void success(Object tag, GsonBaseProtocol data) {
                        PayPalUtils.getInstance().clearPaypalResultInfo(context, userid);
                        if (listener != null) {
                            listener.success(tag, data);
                        }
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        if (baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_THIRD_SERVER_ERROR
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_THIRD_DATE_ERROR
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_AMOUNT_ERROR
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_SERVER_ERROR
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_ID_ERROR
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_NOT_EXIST
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_SUBMITTED
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_TYPE_ERROR
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_PAY_FAILD
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_USER_ERROR
                                || baseProtocol.result == SdvnHttpErrorNo.EC_ORDER_AMOUNT_OVER_LIMIT) {
                            PayPalUtils.getInstance().clearPaypalResultInfo(context, userid);
                        }
                        if (listener != null) {
                            listener.error(tag, baseProtocol);
                        }
                    }
                });
    }

    /**
     * c处理完结果之后回调
     */
    public interface DoResult {
        void succ(String id);

        //网络异常或者json返回有问题
        void failed(String err);

        //用户取消支付
        void cancelled();
    }

}