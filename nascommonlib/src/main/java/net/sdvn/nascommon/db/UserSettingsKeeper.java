package net.sdvn.nascommon.db;

import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.objecbox.UserSettings;
import net.sdvn.nascommon.db.objecbox.UserSettings_;
import net.sdvn.nascommon.model.FileOrderType;
import net.sdvn.nascommon.model.FileViewerType;
import net.sdvn.nascommon.utils.SDCardUtils;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;


/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class UserSettingsKeeper {

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
     * @return {@link UserSettings} or {@code null}
     */
    @Nullable
    public static UserSettings getSettings(String user) {
        Box<UserSettings> dao = DBHelper.getBoxStore().boxFor(UserSettings.class);
        QueryBuilder<UserSettings> queryBuilder = dao.query();
        queryBuilder.equal(UserSettings_.userId,DBHelper.getAccount());
        queryBuilder.equal(UserSettings_.devId, (user));

        return queryBuilder.build().findFirst();
    }

    /**
     * Insert a Default {@link UserSettings} into database by user ID
     *
     * @return {@link UserSettings} or {@code null}
     */
    public static UserSettings insertDefault( String user) {
        String path = SDCardUtils.createDefaultDownloadPath(user);
        String sharePath = /*OneOSFileType.getRootPath(OneOSFileType.PRIVATE)+*/ AppConstants.SHARE_DOWNLOADS_PATH;
        UserSettings settings = new UserSettings(user, path, sharePath, false,
                false, false, true,
                true, true, getFileOrderTypeID(FileOrderType.NAME),
                getFileViewerTypeID(FileViewerType.LIST), System.currentTimeMillis());
        settings.setUserId(DBHelper.getAccount());
        Box<UserSettings> dao = DBHelper.getBoxStore().boxFor(UserSettings.class);
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
    public static boolean update(@Nullable UserSettings user) {
        if (null == user) {
            return false;
        }
        user.setUserId(DBHelper.getAccount());
        Box<UserSettings> dao = DBHelper.getBoxStore().boxFor(UserSettings.class);
        dao.put(user);
        return true;
    }
}
