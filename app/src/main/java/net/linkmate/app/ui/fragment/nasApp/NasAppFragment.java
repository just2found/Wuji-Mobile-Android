package net.linkmate.app.ui.fragment.nasApp;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import net.linkmate.app.R;
import net.sdvn.nascommon.model.phone.AppInfo;
import net.sdvn.nascommon.model.phone.adapter.AppAdapter;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.widget.SwipeListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Local App Management Fragment
 *
 * @author shz
 */
public class NasAppFragment extends Fragment implements OnItemClickListener {
    private static final String TAG = "AppActivity";

    private static final int FILTER_ALL_APP = 0;
    private static final int FILTER_SYSTEM_APP = 1;
    private static final int FILTER_THIRD_APP = 2;
    private static final int FILTER_SDCARD_APP = 3;

    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_2_1_less = "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_2_2 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    private AppCompatActivity activity;
    private SwipeListView mListView;
    @NonNull
    private List<AppInfo> mAppList = new ArrayList<>();
    private AppAdapter mAdapter;
    private PackageManager pkManager;
    private InstallerReceiver installerReceiver;
    private LoadAppTask mLoadTask;

    private long totalSize = 0;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tool_app, container, false);

        initViews(view);
        registerInstallerReceiver();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        showInstalledAppDetails(getActivity(), mAppList.get(arg2).getPkName());
    }

    @Override
    public void onStart() {
        super.onStart();
        pkManager = activity.getPackageManager();
        refreshAppList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (installerReceiver != null) {
            activity.unregisterReceiver(installerReceiver);
        }
        super.onDestroy();
    }

    private void initViews(View view) {
        mListView = view.findViewById(R.id.list_app);
        mAdapter = new AppAdapter(activity, mListView.getRightViewWidth());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void registerInstallerReceiver() {
        installerReceiver = new InstallerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        activity.registerReceiver(installerReceiver, filter);
    }

    private void refreshAppList() {
        mListView.hiddenRight();
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
        }
        mLoadTask = new LoadAppTask();
        mLoadTask.execute(0);
    }

    /**
     * Call system interface displays detailed information about the installed applications It is
     * different between Android 2.1, Android 2.2 and Android 2.3 or later
     *
     * @param context
     * @param packageName application package name
     */
    private void showInstalledAppDetails(@NonNull Context context, String packageName) {
        try {
            Intent intent = new Intent();
            int apiLevel = Build.VERSION.SDK_INT;
            if (apiLevel >= 9) {
                // For Android 2.3（ApiLevel 9）or later
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts(SCHEME, packageName, null);
                intent.setData(uri);
            } else {
                // For Android 2.3 or less, the use of non-public interface
                // It is different between Android 2.1 and 2.2 used in
                // InstalledAppDetails
                // APP_PKG_NAME
                String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_2_2 : APP_PKG_NAME_2_1_less);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
                intent.putExtra(appPkgName, packageName);
            }

            context.startActivity(intent);

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            ToastHelper.showToast(R.string.error_get_app_details);
        }
    }

    private void queryFilterAppInfo(int filter) {
        List<PackageInfo> pkInfoList = pkManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        /*
         * List<ApplicationInfo> appList = pkManager
         * .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
         * Collections.sort(appList, new ApplicationInfo.DisplayNameComparator(pkManager));
         */
        mAppList.clear();

        for (PackageInfo pkInfo : pkInfoList) {
            ApplicationInfo applicationInfo = pkInfo.applicationInfo;
            boolean isAdd = false;

            switch (filter) {
                case FILTER_ALL_APP:
                    isAdd = true;
                    break;
                case FILTER_SYSTEM_APP:
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        isAdd = true;
                    }
                    break;
                case FILTER_THIRD_APP:
                    // 非系统程序, 本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0
                            || (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                        isAdd = true;
                    }
                    break;
                case FILTER_SDCARD_APP:
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        isAdd = true;
                    }
                    break;
            }

            if (isAdd) {
                AppInfo appInfo = new AppInfo();
                appInfo.setAppName(applicationInfo.loadLabel(pkManager).toString());
                appInfo.setAppIcon(applicationInfo.loadIcon(pkManager));
                appInfo.setPkName(applicationInfo.packageName);
                appInfo.setAppVersion(getString(R.string.version_name) + pkInfo.versionName);
                appInfo.setIntent(pkManager.getLaunchIntentForPackage(appInfo.getPkName()));
                // try {
                // queryPacakgeSize(appInfo.pkName);
                // } catch (Exception e) {
                // e.printStackTrace();
                // totalSize = 0;
                // }
                appInfo.setAppSize(totalSize);
                if (activity != null && Objects.equals(activity.getPackageName(), appInfo.getPkName())) {
                    mAppList.add(appInfo);
                }
            }
        }
    }

    private class LoadAppTask extends AsyncTask<Integer, Integer, String[]> {

        @Override
        public void onPreExecute() {
            super.onPreExecute();
//            if (activity != null)
//                activity.showLoading(R.string.getting_app_list);
        }

        @Nullable
        @Override
        protected String[] doInBackground(Integer... param) {
            // queryAppInfo();
            queryFilterAppInfo(FILTER_THIRD_APP);
            return null;
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (mAdapter != null) {
                mAdapter.setAppList(mAppList);
                mAdapter.notifyDataSetChanged();
            }
//            if (activity != null)
//                activity.dismissLoading();
        }
    }

    public class InstallerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, @Nullable Intent intent) {
            if (intent != null) {
                if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())
                        || ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction()))) {
                    refreshAppList();
                }
            }
        }
    }
}
