package net.linkmate.app.ui.scan;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.rxjava.rxlife.RxLife;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.manager.SDVNManager;
import net.linkmate.app.ui.activity.circle.JoinCircleActivity;
import net.linkmate.app.ui.nas.share.ShareActivity;
import net.linkmate.app.ui.viewmodel.TorrentsViewModel;
import net.linkmate.app.util.CheckStatus;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.JsonUtil;
import net.linkmate.app.util.OpenFiles;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.business.DeviceUserUtil;
import net.linkmate.app.util.business.NetManagerUtil;
import net.linkmate.app.util.business.ReceiveScoreUtil;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Network;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.common.ErrorCode;
import net.sdvn.common.internet.SdvnHttpErrorNo;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.CommonResultListener;
import net.sdvn.common.internet.protocol.ShareBindResult;
import net.sdvn.common.internet.protocol.SnBindResult;
import net.sdvn.common.repo.NetsRepo;
import net.sdvn.common.vo.NetworkModel;
import net.sdvn.nascommon.db.objecbox.ShareElementV2;
import net.sdvn.nascommon.iface.Callback;
import net.sdvn.nascommon.iface.Result;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.viewmodel.ShareViewModel2;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import libs.source.common.livedata.Status;
import timber.log.Timber;


public class ScanActivity extends BaseActivity implements CaptureFragment.ICaptureStatus,
        HttpLoader.HttpLoaderStateListener {
    static final int REQUEST_CODE = 10001;
    private final String CaptureTag = "capture";
    private ImageView ivLeft;
    private TextView tvTitle;
    private View rlTitle;
    private String photo_path;
    private NavController navController;
    private ScanViewModel scanViewModel;
    private boolean isInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        scanViewModel = new ViewModelProvider(this).get(ScanViewModel.class);
        initView();
        tvTitle.setText(R.string.scan_qr_code);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        navController = Navigation.findNavController(this, R.id.activity_container);
        scanViewModel.getLiveDataCaptureResult().observe(this, this::onSuccess);
        scanViewModel.getLiveDataHttpLoad().observe(this, objectResource -> {
            if (objectResource.getStatus() == Status.SUCCESS) {
                onLoadComplete();
            } else if (objectResource.getStatus() == Status.ERROR) {
                onLoadError();
            } else if (objectResource.getStatus() == Status.LOADING) {
                onLoadStart((Disposable) objectResource.getData());
            }
        });
//        mCaptureFragment = new CaptureFragment();
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.activity_scan_container, mCaptureFragment, CaptureTag)
//                .commit();
//        mCaptureFragment.setCaptureStatus(this);
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    private void initView() {
        bindView();

    }

    public void captureRestart() {
        if (navController.getCurrentDestination().getId() == R.id.captureFragment) {
            scanViewModel.restartPreview();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!CMAPI.getInstance().isConnected()) {
            ToastUtils.showToast(R.string.tip_wait_for_service_connect);
            finish();
            return;
        }
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }


    public void openAlbumGetQRImage(View view) {
        Intent innerIntent = new Intent(); // "android.intent.action.GET_CONTENT"
        if (Build.VERSION.SDK_INT < 19) {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                addDisposable(Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        if (data != null) {
                            String[] proj = {MediaStore.Images.Media.DATA};
                            // 获取选中图片的路径
                            Uri uri = data.getData();
                            if (uri != null) {
                                Cursor cursor = getContentResolver().query(uri,
                                        proj, null, null, null);
                                if (cursor != null) {
                                    if (cursor.moveToFirst()) {
                                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                                        photo_path = cursor.getString(column_index);
                                        if (photo_path == null) {
                                            photo_path = OpenFiles.getPath(getApplicationContext(),
                                                    uri);
                                        }
                                    }
                                    cursor.close();
                                }
                            }
                        }
                        String result = QRCodeDecoder.syncDecodeQRCode(photo_path);
                        if (result == null) {
                            result = "";
                        }
                        emitter.onNext(result);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String result) {
                                // LogUtils.i("123result", result.getText());
                                // 数据返回
                                if (TextUtils.isEmpty(result))
                                    ToastHelper.showLongToastSafe(R.string.image_have_qrcode);
                                else
                                    onSuccess(result.trim());
                            }
                        }, Timber::d));
            }

        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onLoadStart(Disposable disposable) {
        showLoading(R.string.send_request);
    }

    @Override
    public void onLoadComplete() {
        dismissLoading();
    }

    @Override
    public void onLoadError() {
        dismissLoading();
        captureRestart();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onSuccess(String msg) {
        try {
            Timber.d("Scan-Result >>>%s<<<", msg);
            Timber.d("Scan-Result-trim >>>%s<<<", msg.trim());
            HashMap<String, Object> map = new HashMap<>();
            String patternShareCode = "([0-9a-zA-Z]{8})";
            String[] split = msg.split("#");
            for (int i = 0; i < split.length; i++) {
                if (split[i].startsWith("netsc=")) {
                    split = split[i].split("=");
                    if ("netsc".equals(split[0]) &&
                            Pattern.compile(patternShareCode).matcher(split[1]).matches()) {
                        map.put("sc", split[1]);
                        bindNetworkBySC(map);
                        return;
                    }
                } else if (split[i].startsWith("cirsc=")) {
                    split = split[i].split("=");
                    if ("cirsc".equals(split[0]) &&
                            Pattern.compile(patternShareCode).matcher(split[1]).matches()) {
                        map.put("sc", split[1]);
                        startActivity(new Intent(this, JoinCircleActivity.class)
                                .putExtra("shareCode", split[1]));
                        finish();
                        return;
                    }

                } else if (split[i].startsWith("devsc=")) {
                    split = split[i].split("=");
                    if ("devsc".equals(split[0]) &&
                            Pattern.compile(patternShareCode).matcher(split[1]).matches()) {
                        map.put("sc", split[1]);
                        bindDeviceBySC(map);
                        return;
                    }
                } else if (split[i].startsWith("filesc=")) {
                    String[] strings = split[i].split("=");
                    if ("filesc".equals(strings[0])) {
                        final String t2 = strings[1];
                        final ShareViewModel2 shareViewModel2 = ViewModelProviders.of(ScanActivity.this).get(ShareViewModel2.class);
                        shareViewModel2.subscribeDevice(t2, true, new Consumer<ShareElementV2>() {
                            @Override
                            public void accept(ShareElementV2 v2) throws Exception {
                                startActivity(new Intent(ScanActivity.this, ShareActivity.class));
                                finish();
                            }
                        });
                        return;
                    }
                } else if (split[i].startsWith("tsc=")) {
                    String[] strings = split[i].split("=");
                    if ("tsc".equals(strings[0])) {
                        String[] strings2 = strings[1].split("_");
                        Runnable task1 = () -> {
                            TorrentsViewModel torrentsViewModel = ViewModelProviders.of(this).get(TorrentsViewModel.class);
                            showLoading();
                            torrentsViewModel.showScanQRCodeResult(this, strings2[0], strings2[1], new Callback<Result<Boolean>>() {
                                @Override
                                public void result(Result<Boolean> booleanResult) {
                                    dismissLoading();
                                    if (booleanResult.isSuccess()) {
//                                    finish();
                                        captureRestart();
                                    } else {
                                        captureRestart();
                                    }
                                }
                            });
                        };
                        Runnable task2 = () -> {
                            DeviceBean bean = DevManager.getInstance().getDeviceBean(strings2[3]);
                            if (bean != null) {
                                CheckStatus.INSTANCE.checkDeviceStatus(this,
                                        getSupportFragmentManager(), bean,
                                        isNormalStatus -> {//检查状态
                                            if (isNormalStatus) {
                                                task1.run();
                                            } else {
                                                captureRestart();
                                            }
                                            return null;
                                        }, null);
                                return;
                            } else {
                                ToastHelper.showLongToast(R.string.error_share_src_dev_not_found);
                                captureRestart();
                                return;
                            }
                        };
                        Runnable task3 = () -> {
                            if (strings2.length == 4 && !strings2[3].isEmpty()) {
                                task2.run();
                            } else {
                                task1.run();
                            }
                        };
                        if (strings2.length == 4 && !strings2[2].isEmpty()) {
                            String netId = strings2[2];
                            if (!SDVNManager.getInstance().isCurrentNet(netId)) {
                                NetworkModel networkModel = NetsRepo.INSTANCE.getNetwork(netId);
                                if (networkModel != null && networkModel.isWaitingForConsent()) {
                                    ToastHelper.showLongToast(R.string.ec_api_not_authorized);
                                    captureRestart();
                                    return;
                                }
                                Network bean = SDVNManager.getInstance().getNetById(netId);
                                if (bean != null) {
                                    //show join circle
                                    DialogUtil.showSelectDialog(this,
//                                            String.format(, bean.getName()),
                                            getString(R.string.tips_switch_to_circle),
                                            getString(R.string.confirm), (v, strEdit, dialog, isCheck) -> {
                                                dialog.dismiss();
                                                CMAPI.getInstance().switchNetwork(bean.getId(), error -> {
                                                    dismissLoading();
                                                    if (error != Constants.CE_SUCC) {
                                                        ToastUtils.showToast(getString(ErrorCode.error2String(error)));
                                                        captureRestart();
                                                    } else {
//                                                        ToastHelper.showLongToast(R.string.pls_scan_qrcode_again);
                                                        captureRestart();
                                                        Observable.timer(1000, TimeUnit.SECONDS)
                                                                .subscribeOn(Schedulers.single())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .as(RxLife.as(ScanActivity.this))
                                                                .subscribe(aLong -> {
                                                                    task3.run();
                                                                });
                                                    }
                                                });
                                            },
                                            getString(R.string.cancel), null);
                                    setStatus(LoadingStatus.CHANGE_VIRTUAL_NETWORK);
                                } else {
                                    ToastHelper.showLongToast(R.string.did_not_join);
                                    captureRestart();
                                }
                                return;
                            }
                        }
                        task3.run();

                    }
                    return;
                }
            }
            // ver=1_appId=MK1G8WBWU8OU33SOHIM2_SN=SP03BV100001000A
            String patternBind = "ver=\\d*_appId=([0-9A-Z]{20})_SN=([0-9a-zA-Z.\\-]+)";
            if (Pattern.compile(patternBind).matcher(msg).matches()) {
                String[] strings = msg.split("_");
                for (String string : strings) {
                    String[] keyValues = string.split("=");
                    map.put(keyValues[0].trim(), keyValues[1].trim());
                }
                bindDeviceBySN(map);
                return;
            }
            String patternBind2 = "V\\d*_([0-9A-Z]{6})_([0-9a-zA-Z.\\-]+)";
            if (Pattern.compile(patternBind2).matcher(msg).matches()) {
                String[] strings = msg.split("_");
                map.put("ver", strings[0]);
                map.put("appId", strings[1]);
                String SN = strings[2];
                if (SN.contains("SNM8X4CCR")) {
                    SN = SN.replaceFirst("SNM8X4CCR", "M8X4CCR");
                }
                map.put("SN", SN);
                bindDeviceBySN(map);
                return;
            }
            // ver=1_ot=100_act=qrcode_uuid=faacda8d-0777-45b6-a8f2-8ba9a61eb571

            String patternAuth = "ver=\\d*_ot=\\d{1,3}_act=([a-z]{6})_uuid=([0-9a-z-]{36})";
            if (Pattern.compile(patternAuth).matcher(msg).matches()) {
                String[] strings = msg.split("_");
                for (String string : strings) {
                    String[] keyValues = string.split("=");
                    map.put(keyValues[0].trim(), keyValues[1].trim());
                }
//                authSignInRequest(map);
                authSignIn(msg);
                return;
            }
            if (JsonUtil.isJsonStr(msg)) {
                map = JsonUtil.parseJsonToMap(msg);
                String action = (String) map.get("action");
                if (!TextUtils.isEmpty(action)) {
                    switch (action) {
                        case "qrcode":
//                            authSignInRequest(map);
                            authSignIn(msg);
                            return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ToastUtils.showToast(String.format("%s: %s", getString(R.string.ec_invalid_qrcode), msg));
        captureRestart();
    }

    private void authSignIn(String msg) {
        Bundle bundle = new ApplyLogonFragmentArgs(msg).toBundle();
        navController.navigate(R.id.action_captureFragment_to_applyLogonFragment, bundle);
    }


    private void showResult(int resIdIv, int resIdOperation, int resIdOperationTip) {
        showResult(resIdIv, resIdOperation, resIdOperationTip, -1, null);
    }

    private void showResult(int resIdIv, int resIdOperation, int resIdOperationTip, int resid, View.OnClickListener listener) {
        ScanResultOperatorFragmentArgs args = new ScanResultOperatorFragmentArgs(resIdIv, resIdOperation, resIdOperationTip);
        navController.navigate(R.id.action_captureFragment_to_scanResultOperatorFragment, args.toBundle());
    }

    private void bindNetworkBySC(HashMap<String, Object> map) {
        NetManagerUtil.bindNetworkBySC(String.valueOf(map.get("sc")), this,
                new ScanResultListener() {

                    @Override
                    public void success(Object tag, GsonBaseProtocol mGsonBaseProtocol) {
                        showResult(R.drawable.default_page_successful
                                , R.string.request_succ
                                , R.string.wait_for_net_auth);
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                        super.error(tag, mErrorProtocol);
                        if (mErrorProtocol.result == SdvnHttpErrorNo.EC_INVALID_PARAMS) {
                            ToastUtils.showToast(getString(R.string.qc_code_invalid));
                        } else
                            ToastUtils.showError(mErrorProtocol.result);
                    }
                });
    }

    private void bindDeviceBySC(HashMap<String, Object> map) {
        DeviceUserUtil.bindDeviceBySC(String.valueOf(map.get("sc")), this,
                new ScanResultListener<ShareBindResult>() {
                    @Override
                    public void success(Object tag, ShareBindResult mGsonBaseProtocol) {
                        showResult(R.drawable.default_page_successful
                                , R.string.request_succ
                                , mGsonBaseProtocol.scanconfirm == 1 ?
                                        R.string.wait_for_dev_auth : -1);
                        DevManager.getInstance().initHardWareList(null);//扫码添加绑定设备SC
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                        super.error(tag, mErrorProtocol);
                        if (mErrorProtocol.result == SdvnHttpErrorNo.EC_DEVICE_HAS_BEEN_BINDED
                                && mErrorProtocol instanceof ShareBindResult) {
                            int resId = SdvnHttpErrorNo.ec2ResId(mErrorProtocol.result);
                            String string = Utils.getApp().getString(resId);
                            ToastUtils.showToast(String.format(string + "(%s)", ((ShareBindResult) mErrorProtocol).domain));
                        } else {
                            ToastUtils.showError(mErrorProtocol.result);
                        }
                    }
                });
    }

    private void bindDeviceBySN(HashMap<String, Object> map) {
        String ver = String.valueOf(map.get("ver"));
        String sn = String.valueOf(map.get("SN"));
        String appId = String.valueOf(map.get("appId"));
        ScanResultListener<SnBindResult> listener = new ScanResultListener<SnBindResult>() {
            @Override
            public void success(Object tag, SnBindResult bean) {
                showResult(R.drawable.default_page_successful
                        , R.string.bind_success
                        , bean.scanconfirm == 1 ?
                                R.string.wait_for_dev_auth : -1
                        , R.string.receive_score
                        , TextUtils.isEmpty(bean.gainmbp_url) ?
                                null : (View.OnClickListener) v -> {
                            ReceiveScoreUtil.showReceiveScoreDialog(ScanActivity.this,
                                    bean.deviceid,
                                    ScanActivity.this);
//                            Locale curLocale = ScanActivity.this.getResources().getConfiguration().locale;
//                            String language = curLocale.getLanguage();
//                            String script = curLocale.getScript();
//                            String country = curLocale.getCountry();//"CN""TW"
//                            String lang;
//                            if ("zh".equals(language) &&
//                                    (!"cn".equals(country.toLowerCase()) || "hant".equals(script.toLowerCase()))) {
//                                lang = "tw";
//                            } else {
//                                lang = language;
//                            }
//                            BaseInfo baseinfo = CMAPI.getInstance().getBaseInfo();
//                            Intent i = new Intent(ScanActivity.this, WebActivity.class);
//                            i.putExtra("url", bean.gainmbp_url
//                                    .replace("{0}", baseinfo.getTicket())
//                                    .replace("{1}", lang)
//                                    .replace("\\u0026", "&"));
//                            i.putExtra("title", getString(R.string.receive_score));
//                            i.putExtra("ConnectionState", true);
//                            i.putExtra("enableScript", true);
//                            i.putExtra("hasFullTitle", false);
//                            i.putExtra("sllType", "app");
//                            ScanActivity.this.startActivity(i);
                        });
                DevManager.getInstance().initHardWareList(null);//扫码添加绑定设备SN
            }

            @Override
            public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                super.error(tag, mErrorProtocol);
                if (mErrorProtocol.result == SdvnHttpErrorNo.EC_DEVICE_HAS_BEEN_BINDED
                        && mErrorProtocol instanceof SnBindResult) {
                    int resId = SdvnHttpErrorNo.ec2ResId(mErrorProtocol.result);
                    String string = Utils.getApp().getString(resId);
                    ToastUtils.showToast(String.format(string + "(%s)", ((SnBindResult) mErrorProtocol).domain));
                } else {
                    ToastUtils.showError(mErrorProtocol.result);
                }
            }
        };
        if ("V2".equals(ver)) {
            DeviceUserUtil.bindDeviceV2BySn(sn, appId, this, listener);
        } else {
            DeviceUserUtil.bindDeviceBySn(sn, appId, this, listener);
        }
    }


    @Override
    public void onOpenCameraError() {
        ToastUtils.showToast(R.string.open_camera_error);
    }

    private void bindView() {
        ivLeft = findViewById(R.id.itb_iv_left);
        tvTitle = findViewById(R.id.itb_tv_title);
        rlTitle = findViewById(R.id.itb_rl);
    }

    public abstract class ScanResultListener<T extends GsonBaseProtocol> extends CommonResultListener<T> {
        @Override
        public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
            captureRestart();
        }
    }
}
