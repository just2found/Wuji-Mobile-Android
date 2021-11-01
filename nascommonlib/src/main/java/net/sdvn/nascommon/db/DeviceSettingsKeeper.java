package net.sdvn.nascommon.db;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.objecbox.DeviceSettings;
import net.sdvn.nascommon.db.objecbox.DeviceSettings_;
import net.sdvn.nascommon.model.FileOrderType;
import net.sdvn.nascommon.model.FileViewerType;
import net.sdvn.nascommon.utils.SDCardUtils;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;


/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class DeviceSettingsKeeper {

    public static int getFileOrderTypeID(FileOrderType type) {
        if (type == FileOrderType.NAME) {
            return 0;
        }

        return 1;
    }

    public static int getFileViewerTypeID(FileViewerType type) {
        if (type == FileViewerType.LIST) {
            return 0;
        }

        return 1;
    }

    /**
     * Query user settings by ID
     *
     * @return {@link DeviceSettings} or {@code null}
     */
    @Nullable
    public static DeviceSettings getSettings(String devId) {
        Box<DeviceSettings> dao = DBHelper.getBoxStore().boxFor(DeviceSettings.class);
        QueryBuilder<DeviceSettings> queryBuilder = dao.query();
        queryBuilder.equal(DeviceSettings_.userId, DBHelper.getAccount());
        queryBuilder.equal(DeviceSettings_.devId, (devId));

        return queryBuilder.build().findFirst();
    }

    /**
     * Insert a Default {@link DeviceSettings} into database by user ID
     *
     * @param devId {@link net.sdvn.cmapi.Device}.ID
     * @return {@link DeviceSettings} or {@code null}
     */
    public static DeviceSettings insertDefault(String devId, String user) {
        String path = SDCardUtils.createDefaultDownloadPath(user);
        String sharePath = /*OneOSFileType.getRootPath(OneOSFileType.PRIVATE)+*/ AppConstants.SHARE_DOWNLOADS_PATH;
        DeviceSettings settings = new DeviceSettings(devId, path, sharePath, false,
                false, false, true,
                true, true, getFileOrderTypeID(FileOrderType.NAME),
                getFileViewerTypeID(FileViewerType.LIST), System.currentTimeMillis());
        settings.setUserId(DBHelper.getAccount());
        Box<DeviceSettings> dao = DBHelper.getBoxStore().boxFor(DeviceSettings.class);
        if (dao.put(settings) > 0) {
            return settings;
        }

        return null;
    }

    /**
     * Update user information
     *
     * @param user
     * @return
     */
    public static boolean update(@Nullable DeviceSettings user) {
        if (null == user) {
            return false;
        }
        user.setUserId(DBHelper.getAccount());
        Box<DeviceSettings> dao = DBHelper.getBoxStore().boxFor(DeviceSettings.class);
        dao.put(user);
        return true;
    }
}
