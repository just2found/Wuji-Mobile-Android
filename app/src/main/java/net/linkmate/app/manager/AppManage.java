package net.linkmate.app.manager;

import net.linkmate.app.R;
import net.linkmate.app.base.MyApplication;
import net.linkmate.app.bean.AppBean;
import net.linkmate.app.util.MySPUtils;

import java.util.ArrayList;
import java.util.List;

public class AppManage {

    private List<AppBean> myApps;
    private List<AppBean> AllApps;

    private AppManage() {
        init();
    }

    public void init() {
        int[] appNameIds = new int[]{R.string.scan_qr_code, R.string.add_resource,
                R.string.backup_photo, R.string.title_sync_contacts, /*R.string.title_sync_sms*/};
        int[] appIconIds = new int[]{R.drawable.icon_app_scan, R.drawable.icon_app_add,
                /*R.drawable.icon_app_share,*/ R.drawable.icon_app_photo_back, R.drawable.icon_app_contact_back,
                R.drawable.icon_app_sms_back, R.drawable.icon_app_file_trans, R.drawable.icon_app_im,
                R.drawable.icon_test_1, R.drawable.icon_test_2, R.drawable.icon_test_3, R.drawable.icon_test_4,
                R.drawable.icon_test_5, R.drawable.icon_test_6, R.drawable.icon_test_7, R.drawable.icon_test_8};

        myApps = new ArrayList<>();
        AllApps = new ArrayList<>();
        for (int i = 0; i < appNameIds.length; i++) {
            boolean tipsEnAble = MySPUtils.getBoolean(MyApplication.getContext(), MySPUtils.APP_TIP_ABLE + i, true);
            myApps.add(new AppBean(i, MyApplication.getContext().getString(appNameIds[i]),
                    appIconIds[i], tipsEnAble));
        }
//        myApps.add(new AppBean("扫一扫", R.drawable.icon_app_scan));
//        myApps.add(new AppBean("新增资源", R.drawable.icon_app_add));
//        myApps.add(new AppBean("资源分享", R.drawable.icon_app_share));
//        myApps.add(new AppBean("相册备份", R.drawable.icon_app_photo_back));
//        myApps.add(new AppBean("通讯录备份", R.drawable.icon_app_contact_back));
//        myApps.add(new AppBean("短信备份", R.drawable.icon_app_sms_back));
//        myApps.add(new AppBean("文件传输", R.drawable.icon_app_file_trans));
//        myApps.add(new AppBean("*即时通讯", R.drawable.icon_app_im));

        AllApps.addAll(myApps);
        AllApps.add(new AppBean("应用9", R.drawable.icon_test_1));
        AllApps.add(new AppBean("应用10", R.drawable.icon_test_2));
        AllApps.add(new AppBean("应用11", R.drawable.icon_test_3));
        AllApps.add(new AppBean("应用12", R.drawable.icon_test_4));
        AllApps.add(new AppBean("应用13", R.drawable.icon_test_5));
        AllApps.add(new AppBean("应用14", R.drawable.icon_test_6));
        AllApps.add(new AppBean("应用15", R.drawable.icon_test_7));
        AllApps.add(new AppBean("应用16", R.drawable.icon_test_8));
    }

    private static class SingleHolder {
        private static AppManage instance = new AppManage();
    }

    public static AppManage getInstance() {
        return SingleHolder.instance;
    }

    //获取我的应用集合的数据
    public List<AppBean> getMyApps() {
        return myApps;
    }

    //获取所有的应用集合的数据
    public List<AppBean> getAllApps() {
        return AllApps;
    }
}
