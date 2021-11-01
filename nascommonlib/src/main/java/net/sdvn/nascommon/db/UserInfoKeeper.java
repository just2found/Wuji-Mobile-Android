package net.sdvn.nascommon.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.objecbox.UserInfo;
import net.sdvn.nascommon.db.objecbox.UserInfo_;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;


/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class UserInfoKeeper {
    private static final String TAG = UserInfoKeeper.class.getSimpleName();

    public static void logUserInfo(String tag) {
        Box<UserInfo> dao = DBHelper.getBoxStore().boxFor(UserInfo.class);
        QueryBuilder<UserInfo> queryBuilder = dao.query();
        queryBuilder.equal(UserInfo_.userId,DBHelper.getAccount());
        List<UserInfo> list = queryBuilder.build().find();
        for (UserInfo info : list) {
            Logger.LOGI(tag, ">>>>>UserInfo {name=" + info.getName() + ", mac=" + info.getMac() + "}");
        }
    }

    /**
     * List Active Users Users by ID Desc
     *
     * @return user list
     */
    public static List<UserInfo> activeUsers() {
        Box<UserInfo> dao = DBHelper.getBoxStore().boxFor(UserInfo.class);
        QueryBuilder<UserInfo> queryBuilder = dao.query();
        queryBuilder.orderDesc(UserInfo_.time);
        queryBuilder.equal(UserInfo_.isActive, (true));

        return queryBuilder.build().find();
    }

    /**
     * Query Last login user
     *
     * @return last login user
     */
    public static UserInfo lastUser() {
        Box<UserInfo> dao = DBHelper.getBoxStore().boxFor(UserInfo.class);
        QueryBuilder<UserInfo> queryBuilder = dao.query();
        queryBuilder.orderDesc(UserInfo_.time);
        List<UserInfo> userInfos = queryBuilder.build().find(0, 1);
        return userInfos.get(0);
    }

    /**
     * Get user from database by user and mac
     *
     * @param user user toPath
     * @param mac  mac address
     * @return UserInfo or NULL
     */
    @Nullable
    public static UserInfo getUserInfo(String user, String mac) {
        try {
            Box<UserInfo> dao = DBHelper.getBoxStore().boxFor(UserInfo.class);
            QueryBuilder<UserInfo> queryBuilder = dao.query();
            queryBuilder.equal(UserInfo_.userId, DBHelper.getAccount());
            queryBuilder.equal(UserInfo_.name, (user));
            queryBuilder.equal(UserInfo_.mac, (mac));
            queryBuilder.orderDesc(UserInfo_.login_time);
            return queryBuilder.build().findFirst();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Insert a user into Database if it does not exist or replace it.
     *
     * @param info
     * @return user ID or -1
     */
    public static long insert(@Nullable UserInfo info) {
        if (info != null) {
            Logger.p(Logger.Level.INFO, true, TAG, "Insert New User: " + info.toString());
            info.setUserId(DBHelper.getAccount());
            Box<UserInfo> dao = DBHelper.getBoxStore().boxFor(UserInfo.class);
            return dao.put(info);
        }
        return -1;
    }

    /**
     * Update user information
     *
     * @param user
     * @return
     */
    public static boolean update(@Nullable UserInfo user) {
        if (null == user) {
            return false;
        }
        user.setUserId(DBHelper.getAccount());
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Update user: " + user);
        Box<UserInfo> dao = DBHelper.getBoxStore().boxFor(UserInfo.class);
        dao.put(user);
        return true;
    }

    /**
     * Set the user is not active
     *
     * @param info
     * @return
     */
    public static boolean unActive(@Nullable UserInfo info) {
        if (info == null) {
            return false;
        }

        Box<UserInfo> dao = DBHelper.getBoxStore().boxFor(UserInfo.class);
        info.setIsActive(false);
        dao.put(info);
        return true;
    }

    public static boolean saveDevMarkInfo(@NonNull String user, @NonNull String mac, @Nullable String markName, @Nullable String markDesc, boolean isNeedUpdateAfterLogin) {
        UserInfo userInfo = UserInfoKeeper.getUserInfo(user, mac);
        boolean isNew = false;
        if (userInfo == null) {
            userInfo = new UserInfo(user, mac);
            isNew = true;
        }
        userInfo.setDevInfoChanged(isNeedUpdateAfterLogin);
        if (markName != null)
            userInfo.setDevMarkName(markName);
        if (markDesc != null)
            userInfo.setDevMarkDesc(markDesc);
        if (isNew) {
            return UserInfoKeeper.insert(userInfo) != -1;
        } else {
            return UserInfoKeeper.update(userInfo);
        }


    }
}
