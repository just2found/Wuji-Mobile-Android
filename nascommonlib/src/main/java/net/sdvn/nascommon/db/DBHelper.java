package net.sdvn.nascommon.db;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getkeepsafe.relinker.ReLinker;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.BuildConfig;

import java.io.File;
import java.util.Objects;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import libs.source.common.AppExecutors;
import timber.log.Timber;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DBHelper {
    private static final String TAG = DBHelper.class.getSimpleName();

    private static final String DB_NAME = "weline";
    private static final String DB_NAME_ENCRYPTED = "_oneos_db_encrypted";
    private static final boolean ENCRYPTED = false;
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String account = "";
    private static BoxStore sBoxStore;
    private static MutableLiveData<BoxStore> _sStoreMutableLiveData = new MutableLiveData<>();
    public static LiveData<BoxStore> sStoreLiveData = _sStoreMutableLiveData;

    public static void init() {
        AppExecutors.Companion.getInstance().diskIO().execute(() -> {
            File objectBox = Utils.getApp().getDatabasePath(AppConstants.OBJECT_BOX_NAS);
            sBoxStore = MyObjectBox.builder()
                    .androidContext(Utils.getApp().getApplicationContext())
                    .baseDirectory(objectBox)
                    .name(DB_NAME)
                    .androidReLinker(new ReLinker.Logger() {
                        @Override
                        public void log(String message) {
                            Timber.tag("ReLinker").d(message);
                        }
                    })
                    .build();
            if (DEBUG && sBoxStore != null) {
                boolean started = new AndroidObjectBrowser(sBoxStore).start(Utils.getApp());
                Logger.LOGI("ObjectBrowser", "Started: " + started);
//            adb forward tcp:8090 tcp:8090
//            http://localhost:8090/index.html
                Timber.d("Using ObjectBox %s (%s)", BoxStore.getVersion(), BoxStore.getVersionNative());
            }
            _sStoreMutableLiveData.postValue(sBoxStore);
        });

    }

    @NonNull
    public synchronized static BoxStore getBoxStore() {
        if (sBoxStore == null) {
            String account = SPUtils.getValue(Utils.getApp(), AppConstants.SP_FIELD_USER_ID);
            refreshAccount(account);
            //            if (TextUtils.isEmpty(userId)) userId = DB_NAME;
//            return getBoxDB(userId);
            init();
        }
        return sBoxStore;
    }

    @NonNull
    public static String getAccount() {
        return account;
    }


    public static void refreshAccount(String paccount) {
        if (TextUtils.isEmpty(paccount)) return;
        if (Objects.equals(account, paccount)) return;
        account = paccount;
    }

    public static BoxStore getBoxDB(String account) {
        if (sBoxStore != null) {
            sBoxStore.close();
        }
        File objectBox = Utils.getApp().getDatabasePath(AppConstants.OBJECT_BOX_NAS);

        return MyObjectBox.builder()
                .androidContext(Utils.getApp().getApplicationContext())
                .baseDirectory(objectBox)
                .name(account)
                .androidReLinker(new ReLinker.Logger() {
                    @Override
                    public void log(String message) {
                        Timber.tag("ReLinker").d(message);
                    }
                })
                .build();
    }

}
