package io.weline.internetdb;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.getkeepsafe.relinker.ReLinker;

import java.io.File;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import io.weline.internetdb.vo.MyObjectBox;
import timber.log.Timber;

public class IntrDBHelper {

    private static final String DB_DIR_NAME = "weline";
    private static final String DB_NAME_ENCRYPTED = "_db_encrypted";
    private static final boolean ENCRYPTED = false;
    private static final String OBJECT_BOX_INTERNET =BuildConfig.DEBUG? "ob_internet":"OB_INTERNET";
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static BoxStore sBoxStore;
    private static Application sApp;
    private static String userId;
    public static void init(@NonNull Application app) {
        sApp = app;
        Context context = app.getApplicationContext();
        File objectBox = context.getDatabasePath(OBJECT_BOX_INTERNET);
        sBoxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .baseDirectory(objectBox)
                .name(DB_DIR_NAME)
                .androidReLinker(new ReLinker.Logger() {
                    @Override
                    public void log(String message) {
                        Timber.tag("ReLinker").d(message);
                    }
                })
                .build();
        if (DEBUG && sBoxStore != null) {
            boolean started = new AndroidObjectBrowser(sBoxStore).start(context);
            Timber.d("ObjectBrowser " + "Started: " + started);
//            adb forward tcp:8090 tcp:8090
//            http://localhost:8090/index.html
            Timber.d("Using ObjectBox %s (%s)", BoxStore.getVersion(), BoxStore.getVersionNative());
        }
    }

    @NonNull
    public static BoxStore getBoxStore() {
        if (sBoxStore == null) {
            if (sApp == null) {
                throw new IllegalStateException("pls init before get");
            }
            init(sApp);
        }
        return sBoxStore;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        IntrDBHelper.userId = userId;
    }
}