package net.linkmate.app.util.business;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.rxjava.rxlife.RxLife;

import net.linkmate.app.R;
import net.linkmate.app.base.MyApplication;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.util.FileUtils;
import net.linkmate.app.util.OpenFiles;
import net.linkmate.app.util.StoreViewUtils;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.EnableShareHttpLoader;
import net.sdvn.common.internet.loader.GetNetworkShareCodeHttpLoader;
import net.sdvn.common.internet.loader.GetShareCodeHttpLoader;
import net.sdvn.common.internet.loader.SetScanConfirmHttpLoader;
import net.sdvn.common.internet.protocol.ShareCode;

import java.io.File;
import java.util.Date;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class ShareUtil {

    //获取设备分享码
    public static void getDeviceShareCode(String deviceid,
                                          HttpLoader.HttpLoaderStateListener stateListener,
                                          ResultListener<ShareCode> resultListener) {
        GetShareCodeHttpLoader httpLoader = new GetShareCodeHttpLoader(ShareCode.class);
        httpLoader.setParams(deviceid);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }

    //获取网络分享码
    public static void showNetworkShareCode(String networkid,
                                            HttpLoader.HttpLoaderStateListener stateListener,
                                            ResultListener<ShareCode> resultListener) {
        GetNetworkShareCodeHttpLoader httpLoader = new GetNetworkShareCodeHttpLoader(ShareCode.class);
        httpLoader.setParams(networkid);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }

    //生成二维码
    @SuppressLint("CheckResult")
    public static void generateQRCode(View view, int event, final String sharecode, final QRCodeResult result) {
        if (TextUtils.isEmpty(sharecode)) return;
        final String tips;
        final String content;
        if (event == MyConstants.EVENT_CODE_HARDWAER_DEVICE) {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#devsc=" + sharecode;
            String string = MyApplication.getContext().getString(R.string.share_code_time_tips);
            Date date = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
            String formatTime = MyConstants.sdf.format(date);
            String expiration = view.getContext().getString(R.string.expiration, formatTime);
            tips = string + "\n" + expiration;

        } else {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#netsc=" + sharecode;
            tips = MyApplication.getContext().getString(R.string.share_code_tips);
        }
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> e) {
                Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(content,
                        MyApplication.getContext().getResources().getDisplayMetrics().widthPixels * 5 / 6);
                e.onNext(bitmap);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxLife.as(view))
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) {
                        result.onGenerated(bitmap, tips);
                    }
                });
    }

    //生成二维码
    @SuppressLint("CheckResult")
    public static void generateQRCode(LifecycleOwner lifecycleOwner, int event, final String sharecode, final QRCodeResult result) {
        if (TextUtils.isEmpty(sharecode)) return;
        final String tips;
        final String content;
        if (event == MyConstants.EVENT_CODE_HARDWAER_DEVICE) {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#devsc=" + sharecode;
            String string = MyApplication.getContext().getString(R.string.share_code_time_tips);
            Date date = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
            String formatTime = MyConstants.sdf.format(date);
            String expiration = MyApplication.getContext().getString(R.string.expiration, formatTime);
            tips = string + "\n" + expiration;
        } else if (event == MyConstants.EVENT_CODE_CIRCLE_NETWORK) {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#cirsc=" + sharecode;
            tips = MyApplication.getContext().getString(R.string.share_code_tips);
        } else {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#netsc=" + sharecode;
            tips = MyApplication.getContext().getString(R.string.share_code_tips);
        }
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> e) {
                Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(content,
                        MyApplication.getContext().getResources().getDisplayMetrics().widthPixels * 5 / 6);
                e.onNext(bitmap);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxLife.as(lifecycleOwner))
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) {
                        result.onGenerated(bitmap, tips);
                    }
                });
    }

    //生成二维码
    @SuppressLint("CheckResult")
    public static void generateQRCode(LifecycleOwner lifecycleOwner, int event, final String sharecode, long expireTime, final QRCodeResult result) {
        if (TextUtils.isEmpty(sharecode)) return;
        final String tips;
        final String content;
        if (event == MyConstants.EVENT_CODE_HARDWAER_DEVICE) {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#devsc=" + sharecode;

            Date date = new Date(expireTime * 1000);
            String formatTime = MyConstants.sdf.format(date);
            String expiration = MyApplication.getContext().getString(R.string.expiration, expireTime == 0 ? "" : formatTime);
            tips = expiration;
        } else if (event == MyConstants.EVENT_CODE_CIRCLE_NETWORK) {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#cirsc=" + sharecode;
            Date date = new Date(expireTime * 1000);
            String formatTime = MyConstants.sdf.format(date);
            String expiration = MyApplication.getContext().getString(R.string.expiration, expireTime == 0 ? "" : formatTime);
            tips = expiration;
        } else if (event == MyConstants.EVENT_CODE_MY_IDENTIFY_CODE) {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#uidc=" + sharecode;
            tips = "";
        } else if (event == MyConstants.EVENT_CODE_DEVICE_CODE) {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#devidc=" + sharecode;
            tips = "";
        } else {
            content = MyApplication.getContext().getResources().getString(R.string.downloadURL) + "#netsc=" + sharecode;
            tips = MyApplication.getContext().getString(R.string.share_code_tips);
        }
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> e) {
                Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(content,
                        MyApplication.getContext().getResources().getDisplayMetrics().widthPixels * 5 / 6);
                e.onNext(bitmap);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxLife.as(lifecycleOwner))
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) {
                        result.onGenerated(bitmap, tips);
                    }
                });
    }

    public interface QRCodeResult {
        void onGenerated(Bitmap bitmap, String tips);
    }

    //保存并分享二维码图片
    public static void saveAndShareImg(View container, final String sharecode, final SaveImageResult result) {
        saveImageToFile(container, sharecode, new SaveImageResult() {
            @Override
            public void onSuccess() {
                shareFile(container.getContext(), sharecode);
                if (result != null)
                    result.onSuccess();
            }

            @Override
            public void onError() {
                if (result != null)
                    result.onError();
            }
        });
    }

    public static void saveImageToFile(View container, String sharecode, final SaveImageResult result) {
        String fileName = sharecode + ".png";
        final File file = new File(FileUtils.getCacheDir(), fileName);
        if (!file.exists())
            new StoreViewUtils()
                    .viewSaveToImage(container, file, "")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(RxLife.as(container))
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            result.onSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            result.onError();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        else result.onSuccess();
    }

    public static void saveImageToFile(View container, LifecycleOwner lifecycleOwner, String sharecode, final SaveImageResult result) {
        String fileName = sharecode + ".png";
        final File file = new File(FileUtils.getCacheDir(), fileName);
        if (!file.exists()) {
            Observable<Boolean> toImageObserable = new StoreViewUtils()
                    .viewSaveToImage2(container, file, "");
            if (toImageObserable != null) {
                toImageObserable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(RxLife.as(lifecycleOwner))
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                container.destroyDrawingCache();
                                container.setDrawingCacheEnabled(false);
                                container.invalidate();
                                container.requestLayout();
                                result.onSuccess();
                            }

                            @Override
                            public void onError(Throwable e) {
                                container.destroyDrawingCache();
                                container.setDrawingCacheEnabled(false);
                                container.invalidate();
                                container.requestLayout();
                                result.onError();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else {
                result.onError();
            }
        } else result.onSuccess();
    }

    public static void saveImageToFileForPicture(View container, LifecycleOwner lifecycleOwner, String sharecode, final SaveImageResult result) {
        String fileName = sharecode + ".png";
        //存储到图片库中，可以显示在相册中
        File dirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_PICTURES);
        if (!dirFile.exists()) dirFile.mkdirs();
        final File file = new File(dirFile.getAbsolutePath(), fileName);
        Observable<Boolean> toImageObserable = new StoreViewUtils()
                .viewSaveToImage(container, file, "");
        if (file.exists()) file.delete();
        if (toImageObserable != null) {
            toImageObserable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(RxLife.as(lifecycleOwner))
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            container.destroyDrawingCache();
                            container.setDrawingCacheEnabled(false);
                            container.invalidate();
                            container.requestLayout();
                            result.onSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            container.destroyDrawingCache();
                            container.setDrawingCacheEnabled(false);
                            container.invalidate();
                            container.requestLayout();
                            result.onError();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } else {
            result.onError();
        }
    }

    public static void shareFile(Context context, String sharecode) {
        String fileName = sharecode + ".png";
        File file = new File(FileUtils.getCacheDir(), fileName);
        if (!file.exists()) {
            return;
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, OpenFiles.getFileProviderUri(file, shareIntent, context));
        shareIntent.setType(OpenFiles.getMIMEType(file));
        Intent chooser = Intent.createChooser(shareIntent,
                MyApplication.getContext().getResources().getText(R.string.send_to));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    public interface SaveImageResult {
        void onSuccess();

        void onError();
    }

    public static void savedEnableShareState(String deviceid, final boolean isChecked,
                                             HttpLoader.HttpLoaderStateListener stateListener, ResultListener listener) {
        EnableShareHttpLoader httpLoader = new EnableShareHttpLoader(GsonBaseProtocol.class);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.setParams(isChecked, deviceid);
        httpLoader.executor(listener);
    }


    public static void savedScanConfirmState(String deviceid, final boolean isChecked,
                                             HttpLoader.HttpLoaderStateListener stateListener, ResultListener listener) {
        SetScanConfirmHttpLoader httpLoader = new SetScanConfirmHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(isChecked, deviceid);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(listener);
    }
}
