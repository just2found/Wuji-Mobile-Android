package net.sdvn.nascommon.db;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.objecbox.DeviceInfo;
import net.sdvn.nascommon.db.objecbox.DeviceInfo_;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;
import io.weline.repo.data.model.DataDevIntroduction;


/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DeviceInfoKeeper {

    private static final String TAG = DeviceInfoKeeper.class.getSimpleName();

    public static List<DeviceInfo> all() {
        Box<DeviceInfo> dao = DBHelper.getBoxStore().boxFor(DeviceInfo.class);
        QueryBuilder<DeviceInfo> queryBuilder = dao.query();
        queryBuilder.orderDesc(DeviceInfo_.time);

        return queryBuilder.build().find();
    }

    public static DeviceInfo query(String mac) {
        if (TextUtils.isEmpty(mac)) {
            Logger.p(Logger.Level.ERROR, Logger.Logd.DAO, TAG, "MAC is null");
            return null;
        }
        Box<DeviceInfo> dao = DBHelper.getBoxStore().boxFor(DeviceInfo.class);
        QueryBuilder<DeviceInfo> queryBuilder = dao.query();
        queryBuilder.equal(DeviceInfo_.mac, (mac));
        return queryBuilder.build().findFirst();
    }

    public static boolean insert(@Nullable DeviceInfo info) {
        if (null != info) {
            Box<DeviceInfo> dao = DBHelper.getBoxStore().boxFor(DeviceInfo.class);
            return dao.put(info) > 0;
        }

        return false;
    }

    public static boolean update(@Nullable DeviceInfo info) {
        if (null != info) {
            Box<DeviceInfo> dao = DBHelper.getBoxStore().boxFor(DeviceInfo.class);
            dao.put(info);
            return true;
        }

        return false;
    }

//    public static boolean insertOrReplace(DeviceInfo info) {
//        if (info != null) {
//            DeviceInfoDao dao = DBHelper.getDaoSession().getDeviceInfoDao();
//            return dao.insertOrReplace(info) > 0;
//        }
//
//        return false;
//    }

    public static boolean delete(@Nullable DeviceInfo info) {
        if (info != null) {
            Box<DeviceInfo> dao = DBHelper.getBoxStore().boxFor(DeviceInfo.class);
            dao.remove(info);

            return true;
        }

        return false;
    }

    public static boolean update(String devId, DataDevIntroduction devIntroduction) {
        Box<DeviceInfo> dao = DBHelper.getBoxStore().boxFor(DeviceInfo.class);
        DeviceInfo deviceInfo = get(devId);
        if (deviceInfo == null) {
            deviceInfo = new DeviceInfo(devId);
        }
        deviceInfo.setDevIntroduction(devIntroduction);
        dao.put(deviceInfo);
        return true;
    }

    public static DeviceInfo get(String devId) {
        Box<DeviceInfo> dao = DBHelper.getBoxStore().boxFor(DeviceInfo.class);
        return dao.query()
                .equal(DeviceInfo_.mac, devId)
                .build()
                .findFirst();
    }
}
