package net.linkmate.app.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import com.alibaba.android.arouter.launcher.ARouter;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.manager.MessageManager;
import net.linkmate.app.manager.PrivilegeManager;
import net.linkmate.app.manager.SDVNManager;
import net.linkmate.app.manager.UserInfoManager;
import net.linkmate.app.net.RetrofitSingleton;
import net.linkmate.app.receiver.DevNetworkBroadcastReceiver;
import net.linkmate.app.repository.SPRepo;
import net.linkmate.app.util.NetworkUtils;
import net.linkmate.app.util.UIUtils;
import net.sdvn.app.config.AppConfig;
import net.sdvn.common.SdvnApiInitializer;
import net.sdvn.common.internet.NetConfig;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.LibApp;
import net.sdvn.nascommon.iface.RefWatcherProvider;
import net.sdvn.nascommon.utils.CrashHandler;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.util.Objects;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.weline.repo.SessionCache;
import libs.source.common.LibCommonApp;
import libs.source.common.utils.RootCmd;
import timber.log.Timber;


public class MyApplication extends Application implements Configuration.Provider, RefWatcherProvider,
        CrashHandler.IExit {

    private static MyApplication instance;

    public static Context getContext() {
        return mContext;
    }

    private static Context mContext;

    private static Handler mGlobalHandler;

    public static Handler getGlobalHandler() {
        return mGlobalHandler;
    }


    private static boolean isWifi;

    public static boolean isWifi() {
        return isWifi;
    }

    //    private BoxStore boxStore;
    private RefWatcher refWatcher;

    private static String deviceId = null;
    public static String getDeviceId() {
        return deviceId;
    }
    public static void setDeviceId(String id) {
        deviceId = id;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .detectCustomSlowCalls()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
//            refWatcher = setupLeakCanary();
//            TooLargeTool.startLogging(this);
            Timber.plant(new Timber.DebugTree());
        } else {
            if (RootCmd.haveRoot()) {
//            Toast.makeText(this, R.string.can_not_run_on_root_dev,Toast.LENGTH_LONG).show();
                //退出程序
                exit();
                return;
            }
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
            CrashHandler.getInstance().init(this);
            CrashReport.initCrashReport(getApplicationContext(), "ca2eaa43a7", false);
            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    /*
                     * RxJava2的一个重要的设计理念是：不吃掉任何一个异常,即抛出的异常无人处理，便会导致程序崩溃
                     * 这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，
                     * 这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃
                     */
                    throwable.printStackTrace();
                }
            });
        }
        String processName = UIUtils.getProcessNameApi(this);
        //判断进程名，保证只有主进程运行
        if (!TextUtils.isEmpty(processName) && Objects.equals(processName, this.getPackageName())) {
            //在这里进行主进程初始化逻辑操作
            Timber.d("oncreate main Process");
            mainProcessInit();
        }

    }


    private void mainProcessInit() {
        mContext = getApplicationContext();
        mGlobalHandler = new Handler();
        instance = this;
        registerActivityLifecycleCallbacks(mCallback);
        init();
    }

    private void init() {
        DevNetworkBroadcastReceiver.getInstance().registerReceiver(getInstance());
        long start = System.currentTimeMillis();
        LibCommonApp.INSTANCE.onCreate(getInstance());
        Timber.d("app init consumed LibCommonApp: %s", System.currentTimeMillis() - start);
        LibApp.Companion.getInstance().onCreate(getInstance());
        LibApp.Companion.getInstance().initBriefDelegete(new BriefDelegeteImpl());
        Timber.d("app init consumed LibApp: %s", System.currentTimeMillis() - start);
        SDVNManager.getInstance().init(getInstance());
        Timber.d("app init consumed SDVNManager: %s", System.currentTimeMillis() - start);
        refreshNetworkTypeState();
        Timber.d("app init consumed refreshNetworkTypeState: %s", System.currentTimeMillis() - start);
        NetConfig.isPubTest = BuildConfig.isPubTest;
        OkHttpClientIns.init(getInstance(), AppConfig.host);
        Timber.d("app init consumed OkHttpClientIns: %s", System.currentTimeMillis() - start);
        SdvnApiInitializer.init(getInstance());
        Timber.d("app init consumed SdvnApiInitializer: %s", System.currentTimeMillis() - start);
        UserInfoManager.getInstance();
        DevManager.getInstance();
        MessageManager.getInstance();
        PrivilegeManager.getInstance();
        SPRepo.INSTANCE.init(getInstance());
        SPRepo.INSTANCE.setShowHomeAD(true);
        Schedulers.single().scheduleDirect(() -> {
            long start1 = System.currentTimeMillis();
            try {
                File file = new File(MyConstants.DEFAULT_EXTERNAL_FOLDER_IMG);
                if (!file.exists()) file.mkdirs();
                File cacheFile = new File(MyConstants.DEFAULT_FILE_CACHE_IMG);
                if (!file.exists()) cacheFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Timber.d("app init consumed cachedir single: %s", System.currentTimeMillis() - start1);
            if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
                ARouter.openLog();     // 打印日志
                ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
            }
            ARouter.init(getInstance()); // 尽可能早，推荐在Application中初始化
            Timber.d("app init consumed single: %s", System.currentTimeMillis() - start1);
        });
        Timber.d("app init consumed: %s", System.currentTimeMillis() - start);
    }

    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }


    public static void refreshNetworkTypeState() {
        isWifi = NetworkUtils.isWifi(mContext);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        try {
            unregisterActivityLifecycleCallbacks(mCallback);
            DevNetworkBroadcastReceiver.getInstance().unregisterReceiver(this);
//        stopService(new Intent(this, FileRecvService.class));
            OkHttpClientIns.cleanCooke();
            SDVNManager.getInstance().exit();
            LibApp.Companion.getInstance().onTerminate();
            SessionCache.Companion.getInstance().clear();
            RetrofitSingleton.Companion.getInstance().clear();
        } catch (Exception ignore) {

        }
    }

    private int started;
    private int resumed;
    private ActivityLifecycleCallbacks mCallback = new ActivityLifecycleCallbacks() {

        private final String TAG = ActivityLifecycleCallbacks.class.getSimpleName();

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//            if (activity.getClass() == MainActivity.class) {
//                mainActivityCreated = true;
//            }

            Logger.LOGD(TAG, "onActivityCreated :" + activity.getLocalClassName());
//            showLogBtn(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            ++started;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            ++resumed;
//            ActivityStatusManager.getInstance().setTopActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            --resumed;
        }

        @Override
        public void onActivityStopped(Activity activity) {
            --started;
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
//            Activity topActivity = ActivityStatusManager.getInstance().getTopActivity();
//            if (topActivity != null && activity.getClass() == topActivity.getClass())
//                ActivityStatusManager.getInstance().setTopActivity(null);
            Logger.LOGD(TAG, "onActivityDestroyed :" + activity.getLocalClassName());
        }
    };

    public boolean isApplicationInForeground() {
        return resumed > 0;
    }

    public void exit() {
        onTerminate();
        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public static MyApplication getInstance() {
        return instance;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(BuildConfig.DEBUG ? Log.VERBOSE : Log.ERROR)
                .build();
    }

    @Override
    public RefWatcher getRefWatcher() {
        return refWatcher;
    }


//    public BoxStore getBoxStore() {
//        return boxStore;
//    }
}
